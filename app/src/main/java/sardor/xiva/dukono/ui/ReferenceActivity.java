package sardor.xiva.dukono.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

import sardor.xiva.dukono.R;
import sardor.xiva.dukono.data.Repo;
import sardor.xiva.dukono.data.db.Category;
import sardor.xiva.dukono.data.db.RefItem;
import sardor.xiva.dukono.data.db.Unit;
import sardor.xiva.dukono.databinding.ActivityReferenceBinding;
import sardor.xiva.dukono.databinding.DialogRefEditBinding;
import sardor.xiva.dukono.util.AppExecutors;

/** Birliklar yoki kategoriyalar ro'yxati. */
public class ReferenceActivity extends AppCompatActivity implements RefAdapter.Listener {

    public static final String EXTRA_TYPE = "ref_type";
    public static final int TYPE_UNIT = 1;
    public static final int TYPE_CATEGORY = 2;

    private ActivityReferenceBinding b;
    private RefAdapter adapter;
    private int type;
    private boolean unitMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityReferenceBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        type = getIntent().getIntExtra(EXTRA_TYPE, TYPE_UNIT);
        unitMode = type == TYPE_UNIT;

        b.toolbar.setTitle(unitMode ? R.string.units : R.string.categories);
        b.toolbar.setNavigationOnClickListener(v -> finish());

        adapter = new RefAdapter(this, unitMode);
        b.list.setLayoutManager(new LinearLayoutManager(this));
        b.list.setAdapter(adapter);
        b.list.setItemAnimator(null);

        b.fab.setOnClickListener(v -> showEditDialog(null));
        load();
    }

    private void load() {
        final boolean unit = unitMode;
        AppExecutors.io(() -> {
            List<RefItem> items = unit
                    ? Repo.get().unitsWithUsage()
                    : Repo.get().categoriesWithUsage();
            AppExecutors.main(() -> {
                if (isFinishing() || isDestroyed()) return;
                adapter.submitList(items);
                b.emptyText.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
            });
        });
    }

    private void showEditDialog(RefItem item) {
        DialogRefEditBinding db = DialogRefEditBinding.inflate(getLayoutInflater());
        db.nameInput.setText(item != null ? item.name : "");
        db.fractionSwitch.setVisibility(unitMode ? View.VISIBLE : View.GONE);
        db.fractionSwitch.setChecked(item != null && item.allowFraction);

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this)
                .setTitle(item == null
                        ? (unitMode ? R.string.add_unit : R.string.add_category)
                        : (unitMode ? R.string.edit_unit : R.string.edit_category))
                .setView(db.getRoot())
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.save, null);
        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(x -> dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    db.nameLayout.setError(null);
                    String name = db.nameInput.getText() == null
                            ? "" : db.nameInput.getText().toString().trim();
                    if (name.isEmpty()) {
                        db.nameLayout.setError(getString(R.string.name_required));
                        return;
                    }
                    boolean fraction = db.fractionSwitch.isChecked();
                    long excludeId = item != null ? item.id : 0;
                    final boolean unit = unitMode;
                    AppExecutors.io(() -> {
                        Repo repo = Repo.get();
                        int dup = unit
                                ? repo.countUnitName(name, excludeId)
                                : repo.countCategoryName(name, excludeId);
                        if (dup > 0) {
                            AppExecutors.main(() -> db.nameLayout.setError(
                                    getString(R.string.name_exists)));
                            return;
                        }
                        if (unit) {
                            Unit u = new Unit();
                            u.id = item != null ? item.id : 0;
                            u.name = name;
                            u.allowFraction = fraction;
                            repo.saveUnit(u);
                        } else {
                            Category c = new Category();
                            c.id = item != null ? item.id : 0;
                            c.name = name;
                            repo.saveCategory(c);
                        }
                        AppExecutors.main(() -> {
                            dialog.dismiss();
                            load();
                        });
                    });
                }));
        dialog.show();
    }

    @Override
    public void onEdit(RefItem item) {
        showEditDialog(item);
    }

    @Override
    public void onDelete(RefItem item) {
        if (unitMode) {
            if (item.usageCount > 0) {
                Dialogs.alert(this, R.string.units,
                        getString(R.string.unit_in_use, item.usageCount));
                return;
            }
            Dialogs.confirm(this, R.string.delete,
                    getString(R.string.delete_confirm_message, item.name),
                    R.string.delete, () -> deleteItem(item));
        } else {
            if (item.usageCount > 0) {
                Dialogs.confirm(this, R.string.delete,
                        getString(R.string.delete_category_warning, item.usageCount),
                        R.string.delete, () -> deleteItem(item));
            } else {
                Dialogs.confirm(this, R.string.delete,
                        getString(R.string.delete_confirm_message, item.name),
                        R.string.delete, () -> deleteItem(item));
            }
        }
    }

    private void deleteItem(RefItem item) {
        final boolean unit = unitMode;
        AppExecutors.io(() -> {
            if (unit) {
                Repo.get().deleteUnit(item.id);
            } else {
                Repo.get().deleteCategory(item.id);
            }
            AppExecutors.main(() -> {
                Toast.makeText(this, R.string.deleted, Toast.LENGTH_SHORT).show();
                load();
            });
        });
    }
}
