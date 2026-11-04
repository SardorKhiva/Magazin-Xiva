package sardor.xiva.dukono.ui;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import sardor.xiva.dukono.R;
import sardor.xiva.dukono.data.Repo;
import sardor.xiva.dukono.data.db.Category;
import sardor.xiva.dukono.data.db.Product;
import sardor.xiva.dukono.data.db.Unit;
import sardor.xiva.dukono.databinding.ActivityProductEditBinding;
import sardor.xiva.dukono.util.AppExecutors;
import sardor.xiva.dukono.util.Fmt;
import sardor.xiva.dukono.util.Ui;
import sardor.xiva.dukono.util.UzText;

/** Tovar qo'shish yoki tahrirlash. */
public class ProductEditActivity extends AppCompatActivity {

    public static final String EXTRA_ID = "product_id";
    public static final String EXTRA_NAME = "prefill_name";
    public static final String EXTRA_CODE = "prefill_code";

    private static final String STATE_UNIT = "state_unit";
    private static final String STATE_CATEGORY = "state_category";
    private static final String STATE_EXPIRY = "state_expiry";
    private static final String STATE_SIGNATURE = "state_signature";

    private ActivityProductEditBinding b;
    private long productId;
    private Product existing;

    private final List<Unit> units = new ArrayList<>();
    private final List<Category> categories = new ArrayList<>();

    private Long selectedUnitId;
    private Long selectedCategoryId;
    private Long expiryEpochDay;

    private String initialSignature = "";
    private boolean restored;
    private boolean loading = true;
    private boolean closing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityProductEditBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                attemptClose();
            }
        });

        productId = getIntent().getLongExtra(EXTRA_ID, 0);

        b.toolbar.setNavigationOnClickListener(v -> attemptClose());
        b.toolbar.inflateMenu(R.menu.menu_edit);
        b.toolbar.getMenu().findItem(R.id.action_delete).setVisible(productId > 0);
        b.toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_delete) {
                confirmDelete();
                return true;
            }
            return false;
        });

        b.cancelButton.setOnClickListener(v -> attemptClose());
        b.saveButton.setOnClickListener(v -> attemptSave());

        b.expiryInput.setFocusable(false);
        b.expiryInput.setClickable(true);
        b.expiryInput.setOnClickListener(v -> pickDate());
        b.expiryLayout.setEndIconOnClickListener(v -> {
            expiryEpochDay = null;
            renderExpiry();
        });

        b.unitInput.setOnItemClickListener((parent, view, position, id) -> {
            if (position >= 0 && position < units.size()) {
                selectedUnitId = units.get(position).id;
                b.unitLayout.setError(null);
            }
        });
        b.unitInput.setOnClickListener(v -> b.unitInput.showDropDown());

        b.categoryInput.setOnItemClickListener((parent, view, position, id) -> {
            if (position == 0) {
                selectedCategoryId = null;
            } else if (position - 1 < categories.size()) {
                selectedCategoryId = categories.get(position - 1).id;
            }
            b.categoryLayout.setError(null);
        });
        b.categoryInput.setOnClickListener(v -> b.categoryInput.showDropDown());

        if (savedInstanceState != null) {
            restored = true;
            long u = savedInstanceState.getLong(STATE_UNIT, -1);
            selectedUnitId = u >= 0 ? u : null;
            long c = savedInstanceState.getLong(STATE_CATEGORY, -1);
            selectedCategoryId = c >= 0 ? c : null;
            long e = savedInstanceState.getLong(STATE_EXPIRY, -1);
            expiryEpochDay = e >= 0 ? e : null;
            initialSignature = savedInstanceState.getString(STATE_SIGNATURE, "");
        }

        loadData();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(STATE_UNIT, selectedUnitId == null ? -1 : selectedUnitId);
        outState.putLong(STATE_CATEGORY, selectedCategoryId == null ? -1 : selectedCategoryId);
        outState.putLong(STATE_EXPIRY, expiryEpochDay == null ? -1 : expiryEpochDay);
        outState.putString(STATE_SIGNATURE, initialSignature);
    }

    private void loadData() {
        final long id = productId;
        AppExecutors.io(() -> {
            List<Unit> u = Repo.get().allUnits();
            List<Category> c = Repo.get().allCategories();
            Product p = id > 0 ? Repo.get().getProduct(id) : null;
            AppExecutors.main(() -> {
                if (isFinishing() || isDestroyed()) return;
                if (id > 0 && p == null) {
                    Toast.makeText(this, R.string.error_generic, Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                units.clear();
                units.addAll(u);
                categories.clear();
                categories.addAll(c);
                existing = p;
                setupDropdowns();
                applyData();
                loading = false;
            });
        });
    }

    private void setupDropdowns() {
        List<String> unitNames = new ArrayList<>();
        for (Unit u : units) unitNames.add(u.name);
        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_list_item_1, unitNames);
        b.unitInput.setAdapter(unitAdapter);

        List<String> categoryNames = new ArrayList<>();
        categoryNames.add(getString(R.string.no_category));
        for (Category c : categories) categoryNames.add(c.name);
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_list_item_1, categoryNames);
        b.categoryInput.setAdapter(categoryAdapter);
    }

    private void applyData() {
        if (existing != null) {
            b.toolbar.setTitle(R.string.title_edit_product);
            selectedUnitId = existing.unitId;
            selectedCategoryId = existing.categoryId;
            expiryEpochDay = existing.expiryDate;
            if (!restored) {
                b.nameInput.setText(existing.name);
                b.codeInput.setText(existing.code);
                b.priceInput.setText(String.valueOf(existing.price));
                b.manufacturerInput.setText(existing.manufacturer);
                b.countryInput.setText(existing.country);
                b.noteInput.setText(existing.note);
                Unit unit = findUnit(existing.unitId);
                boolean fraction = unit != null && unit.allowFraction;
                b.qtyInput.setText(Fmt.qty(existing.initialQty, fraction));
                b.balanceInput.setText(Fmt.qty(existing.balance, fraction));
            }
        } else {
            b.toolbar.setTitle(R.string.title_add_product);
            if (!restored) {
                if (!units.isEmpty()) {
                    selectedUnitId = units.get(0).id;
                }
                selectedCategoryId = null;
                expiryEpochDay = null;
                String preName = getIntent().getStringExtra(EXTRA_NAME);
                String preCode = getIntent().getStringExtra(EXTRA_CODE);
                if (!TextUtils.isEmpty(preName)) b.nameInput.setText(preName);
                if (!TextUtils.isEmpty(preCode)) b.codeInput.setText(preCode);
                b.qtyInput.setText("0");
                b.balanceInput.setText("0");
                b.priceInput.setText("0");
            }
        }
        renderDropdownLabels();
        renderExpiry();
        if (!restored) {
            initialSignature = signature();
        }
    }

    private Unit findUnit(Long id) {
        if (id == null) return null;
        for (Unit u : units) if (u.id == id) return u;
        return null;
    }

    private void renderDropdownLabels() {
        Unit unit = findUnit(selectedUnitId);
        if (unit != null) {
            b.unitInput.setText(unit.name, false);
        } else if (!units.isEmpty()) {
            selectedUnitId = units.get(0).id;
            b.unitInput.setText(units.get(0).name, false);
        } else {
            b.unitInput.setText("", false);
        }

        String categoryLabel = getString(R.string.no_category);
        if (selectedCategoryId != null) {
            for (Category c : categories) {
                if (c.id == selectedCategoryId) {
                    categoryLabel = c.name;
                    break;
                }
            }
        }
        b.categoryInput.setText(categoryLabel, false);
    }

    private void renderExpiry() {
        if (expiryEpochDay == null) {
            b.expiryInput.setText("");
            b.expiryLayout.setEndIconVisible(false);
        } else {
            b.expiryInput.setText(Fmt.date(expiryEpochDay));
            b.expiryLayout.setEndIconVisible(true);
        }
    }

    private void pickDate() {
        long selection = expiryEpochDay != null
                ? expiryEpochDay * Fmt.DAY_MS
                : MaterialDatePicker.todayInUtcMilliseconds();
        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(R.string.date_picker_title)
                .setSelection(selection)
                .build();
        picker.addOnPositiveButtonClickListener(value -> {
            if (value != null) {
                expiryEpochDay = value / Fmt.DAY_MS;
                renderExpiry();
            }
        });
        picker.show(getSupportFragmentManager(), "expiry_date");
    }

    private String text(TextInputEditText input) {
        return input.getText() == null ? "" : input.getText().toString().trim();
    }

    private String nullable(String s) {
        return s == null || s.trim().isEmpty() ? null : s.trim();
    }

    private String signature() {
        return text(b.nameInput) + '\u0001'
                + text(b.codeInput) + '\u0001'
                + text(b.priceInput) + '\u0001'
                + (selectedUnitId == null ? "" : selectedUnitId) + '\u0001'
                + text(b.qtyInput) + '\u0001'
                + text(b.balanceInput) + '\u0001'
                + (selectedCategoryId == null ? "" : selectedCategoryId) + '\u0001'
                + text(b.manufacturerInput) + '\u0001'
                + text(b.countryInput) + '\u0001'
                + (expiryEpochDay == null ? "" : expiryEpochDay) + '\u0001'
                + text(b.noteInput);
    }

    private void clearErrors() {
        b.nameLayout.setError(null);
        b.codeLayout.setError(null);
        b.priceLayout.setError(null);
        b.unitLayout.setError(null);
        b.qtyLayout.setError(null);
        b.balanceLayout.setError(null);
    }

    private void attemptSave() {
        if (loading) return;
        clearErrors();

        String name = text(b.nameInput);
        if (name.isEmpty()) {
            b.nameLayout.setError(getString(R.string.name_required));
            return;
        }
        String code = text(b.codeInput);
        if (code.isEmpty()) {
            b.codeLayout.setError(getString(R.string.code_required));
            return;
        }
        Long price = Fmt.parseMoney(text(b.priceInput));
        if (price == null) {
            b.priceLayout.setError(getString(R.string.price_invalid));
            return;
        }
        Unit unit = findUnit(selectedUnitId);
        if (unit == null) {
            b.unitLayout.setError(getString(R.string.unit_required));
            return;
        }
        Double qty = Fmt.parseQty(text(b.qtyInput));
        if (qty == null) {
            b.qtyLayout.setError(getString(R.string.qty_invalid));
            return;
        }
        if (!unit.allowFraction && !isWhole(qty)) {
            b.qtyLayout.setError(getString(R.string.fraction_not_allowed));
            return;
        }
        Double balance = Fmt.parseQty(text(b.balanceInput));
        if (balance == null) {
            b.balanceLayout.setError(getString(R.string.qty_invalid));
            return;
        }
        if (!unit.allowFraction && !isWhole(balance)) {
            b.balanceLayout.setError(getString(R.string.fraction_not_allowed));
            return;
        }

        final double qtyValue = round3(qty);
        final double balanceValue = round3(balance);
        final String nameSort = UzText.sortKey(name);
        final String manufacturer = nullable(text(b.manufacturerInput));
        final String country = nullable(text(b.countryInput));
        final String note = nullable(text(b.noteInput));
        final String searchText = UzText.normalize(name + " " + code + " "
                + (manufacturer == null ? "" : manufacturer) + " "
                + (country == null ? "" : country) + " "
                + (note == null ? "" : note));

        b.saveButton.setEnabled(false);
        final long excludeId = productId;
        AppExecutors.io(() -> {
            Repo repo = Repo.get();
            int codeDup = repo.countCode(code, excludeId);
            int nameDup = repo.countSameName(nameSort, excludeId);
            AppExecutors.main(() -> {
                b.saveButton.setEnabled(true);
                if (isFinishing() || isDestroyed()) return;
                if (codeDup > 0) {
                    b.codeLayout.setError(getString(R.string.code_exists));
                    return;
                }
                Product p = new Product();
                p.id = productId;
                p.name = name;
                p.nameSort = nameSort;
                p.code = code;
                p.price = price;
                p.unitId = unit.id;
                p.initialQty = qtyValue;
                p.balance = balanceValue;
                p.categoryId = selectedCategoryId;
                p.manufacturer = manufacturer;
                p.country = country;
                p.expiryDate = expiryEpochDay;
                p.note = note;
                p.imagePath = existing != null ? existing.imagePath : null;
                p.searchText = searchText;

                if (nameDup > 0) {
                    new MaterialAlertDialogBuilder(this)
                            .setTitle(R.string.duplicate_name_title)
                            .setMessage(getString(R.string.duplicate_name_message, name))
                            .setNegativeButton(R.string.cancel, null)
                            .setPositiveButton(R.string.save_anyway, (d, w) -> saveNow(p))
                            .show();
                } else {
                    saveNow(p);
                }
            });
        });
    }

    private void saveNow(Product p) {
        if (existing == null) {
            commitSave(p, null, null);
            return;
        }
        if (Math.abs(p.balance - existing.balance) < 0.0000001) {
            commitSave(p, null, null);
            return;
        }
        Unit unit = findUnit(selectedUnitId);
        boolean fraction = unit != null && unit.allowFraction;
        String unitName = unit == null ? "" : unit.name;
        Dialogs.stockReason(this, existing.balance, p.balance, fraction, unitName,
                (reason, note) -> commitSave(p, reason, note));
    }

    private void commitSave(Product p, String reason, String note) {
        b.saveButton.setEnabled(false);
        final boolean isNew = existing == null;
        AppExecutors.io(() -> {
            try {
                Repo.get().saveProduct(p, isNew, reason, note);
            } catch (Exception e) {
                AppExecutors.main(() -> {
                    b.saveButton.setEnabled(true);
                    Toast.makeText(this, R.string.error_generic, Toast.LENGTH_SHORT).show();
                });
                return;
            }
            AppExecutors.main(() -> {
                Toast.makeText(this, R.string.saved, Toast.LENGTH_SHORT).show();
                closing = true;
                setResult(Activity.RESULT_OK);
                finish();
            });
        });
    }

    private void confirmDelete() {
        if (productId <= 0) return;
        String name = existing != null ? existing.name : text(b.nameInput);
        Dialogs.confirm(this, R.string.delete,
                getString(R.string.delete_confirm_message, name),
                R.string.delete,
                () -> AppExecutors.io(() -> {
                    Repo.get().deleteProduct(productId);
                    AppExecutors.main(() -> {
                        Toast.makeText(this, R.string.deleted, Toast.LENGTH_SHORT).show();
                        closing = true;
                        setResult(Activity.RESULT_OK);
                        finish();
                    });
                }));
    }

    private void attemptClose() {
        if (loading || closing) {
            finish();
            return;
        }
        if (!hasChanges()) {
            finish();
            return;
        }
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.unsaved_changes)
                .setNegativeButton(R.string.stay, null)
                .setNeutralButton(R.string.exit_without_saving, (d, w) -> finish())
                .setPositiveButton(R.string.save, (d, w) -> attemptSave())
                .show();
    }

    private boolean hasChanges() {
        return !signature().equals(initialSignature);
    }

    private static boolean isWhole(double value) {
        return Math.abs(value - Math.rint(value)) < 0.0000001;
    }

    private static double round3(double value) {
        return BigDecimal.valueOf(value).setScale(3, RoundingMode.HALF_UP).doubleValue();
    }
}
