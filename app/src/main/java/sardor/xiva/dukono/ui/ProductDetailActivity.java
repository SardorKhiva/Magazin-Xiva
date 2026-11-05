package sardor.xiva.dukono.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.List;

import sardor.xiva.dukono.R;
import sardor.xiva.dukono.data.Repo;
import sardor.xiva.dukono.data.db.Category;
import sardor.xiva.dukono.data.db.Product;
import sardor.xiva.dukono.data.db.StockMovement;
import sardor.xiva.dukono.data.db.Unit;
import sardor.xiva.dukono.databinding.ActivityProductDetailBinding;
import sardor.xiva.dukono.databinding.ItemMovementBinding;
import sardor.xiva.dukono.util.AppExecutors;
import sardor.xiva.dukono.util.Fmt;
import sardor.xiva.dukono.util.Labels;
import sardor.xiva.dukono.util.Ui;

/** Tovarning to'liq ko'rinishi (faqat o'qish). */
public class ProductDetailActivity extends AppCompatActivity {

    public static final String EXTRA_ID = "product_id";

    private ActivityProductDetailBinding b;
    private long productId;
    private ActivityResultLauncher<Intent> editLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityProductDetailBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        productId = getIntent().getLongExtra(EXTRA_ID, 0);

        b.toolbar.setNavigationOnClickListener(v -> finish());
        b.toolbar.inflateMenu(R.menu.menu_edit);
        b.toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_delete) {
                confirmDelete();
                return true;
            }
            return false;
        });

        editLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        setResult(Activity.RESULT_OK);
                        finish();
                    } else {
                        load();
                    }
                });

        b.editButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProductEditActivity.class);
            intent.putExtra(ProductEditActivity.EXTRA_ID, productId);
            editLauncher.launch(intent);
        });
        b.deleteButton.setOnClickListener(v -> confirmDelete());

        load();
    }

    private void load() {
        final long id = productId;
        AppExecutors.io(() -> {
            Repo repo = Repo.get();
            Product product = repo.getProduct(id);
            Unit unit = product != null ? repo.getUnit(product.unitId) : null;
            Category category = product != null && product.categoryId != null
                    ? repo.getCategory(product.categoryId) : null;
            List<StockMovement> movements = product != null
                    ? repo.recentMovements(id, 30) : java.util.Collections.emptyList();
            AppExecutors.main(() -> {
                if (isFinishing() || isDestroyed()) return;
                if (product == null) {
                    Toast.makeText(this, R.string.error_generic, Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                bind(product, unit, category, movements);
            });
        });
    }

    private void bind(Product p, Unit unit, Category category, List<StockMovement> movements) {
        boolean fraction = unit != null && unit.allowFraction;
        String unitName = unit == null ? "" : unit.name;

        if (p.imagePath != null && new java.io.File(p.imagePath).exists()) {
            b.productImage.setVisibility(View.VISIBLE);
            int screenWidth = getResources().getDisplayMetrics().widthPixels;
            int imageWidth = (int) (screenWidth * 0.70);
            b.productImage.getLayoutParams().width = imageWidth;
            b.productImage.requestLayout();
            sardor.xiva.dukono.util.ImageUtil.loadProductImage(b.productImage, p.imagePath);
        } else {
            b.productImage.setVisibility(View.GONE);
        }

        b.productName.setText(p.name);
        b.codeText.setText(getString(R.string.code_value, p.code));
        b.priceText.setText(getString(R.string.price_value,
                getString(R.string.money_format, Fmt.money(p.price))));
        b.qtyText.setText(getString(R.string.qty_with_unit,
                Fmt.qty(p.initialQty, fraction), unitName));
        b.balanceText.setText(getString(R.string.qty_with_unit,
                Fmt.qty(p.balance, fraction), unitName));
        b.categoryText.setText(category != null ? category.name : Ui.DASH);
        Ui.text(b.manufacturerText, p.manufacturer);
        Ui.text(b.countryText, p.country);
        Ui.text(b.noteText, p.note);

        if (p.expiryDate == null) {
            b.expiryText.setText(Ui.DASH);
        } else {
            long left = Fmt.daysLeft(p.expiryDate);
            String status;
            if (left < 0) {
                status = getString(R.string.expiry_overdue_days, (int) Math.abs(left));
            } else if (left == 0) {
                status = getString(R.string.expiry_today);
            } else {
                status = getString(R.string.expiry_near, (int) left);
            }
            b.expiryText.setText(getString(R.string.date_with_status,
                    Fmt.date(p.expiryDate), status));
        }

        b.movementsContainer.removeAllViews();
        b.movementsEmpty.setVisibility(movements.isEmpty() ? View.VISIBLE : View.GONE);
        for (StockMovement m : movements) {
            ItemMovementBinding row = ItemMovementBinding.inflate(
                    getLayoutInflater(), b.movementsContainer, false);
            row.movementTitle.setText(getString(R.string.movement_title_format,
                    Fmt.dateTime(m.movedAt), getString(Labels.reason(m.reason))));
            String note = m.note == null || m.note.trim().isEmpty() ? "" : " · " + m.note;
            row.movementDelta.setText(getString(R.string.movement_delta_format,
                    Fmt.signedQty(m.delta, fraction), unitName) + note);
            int color = ContextCompat.getColor(this,
                    m.delta >= 0 ? R.color.movement_in : R.color.movement_out);
            row.movementDelta.setTextColor(color);
            b.movementsContainer.addView(row.getRoot());
        }
    }

    private void confirmDelete() {
        if (productId <= 0) return;
        CharSequence name = b.productName.getText();
        Dialogs.confirm(this, R.string.delete,
                getString(R.string.delete_confirm_message,
                        name == null ? "" : name),
                R.string.delete,
                () -> AppExecutors.io(() -> {
                    Repo.get().deleteProduct(productId);
                    AppExecutors.main(() -> {
                        Toast.makeText(this, R.string.deleted, Toast.LENGTH_SHORT).show();
                        setResult(Activity.RESULT_OK);
                        finish();
                    });
                }));
    }
}
