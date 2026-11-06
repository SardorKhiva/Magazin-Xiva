package sardor.xiva.dukono.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import sardor.xiva.dukono.DukonApp;
import sardor.xiva.dukono.R;
import sardor.xiva.dukono.databinding.ActivitySettingsBinding;
import sardor.xiva.dukono.databinding.DialogChangePinBinding;
import sardor.xiva.dukono.databinding.DialogNumberBinding;
import sardor.xiva.dukono.util.AppExecutors;
import sardor.xiva.dukono.util.AppPrefs;
import sardor.xiva.dukono.util.CsvHelper;
import sardor.xiva.dukono.util.Fmt;
import sardor.xiva.dukono.util.PinManager;

/** Sozlamalar. */
public class SettingsActivity extends AppCompatActivity {

    private ActivitySettingsBinding b;
    private ActivityResultLauncher<String> exportCsvLauncher;
    private ActivityResultLauncher<String[]> importCsvLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        exportCsvLauncher = registerForActivityResult(
                new ActivityResultContracts.CreateDocument("text/csv"),
                uri -> {
                    if (uri != null) {
                        AppExecutors.io(() -> {
                            try {
                                int count = CsvHelper.exportProductsToCsv(this, uri);
                                AppExecutors.main(() -> Toast.makeText(this,
                                        getString(R.string.export_success, count), Toast.LENGTH_SHORT).show());
                            } catch (Exception e) {
                                AppExecutors.main(() -> Toast.makeText(this,
                                        R.string.export_error, Toast.LENGTH_SHORT).show());
                            }
                        });
                    }
                });

        importCsvLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                uri -> {
                    if (uri != null) {
                        AppExecutors.io(() -> {
                            try {
                                int count = CsvHelper.importProductsFromCsv(this, uri);
                                AppExecutors.main(() -> Toast.makeText(this,
                                        getString(R.string.import_success, count), Toast.LENGTH_SHORT).show());
                            } catch (Exception e) {
                                AppExecutors.main(() -> Toast.makeText(this,
                                        R.string.import_error, Toast.LENGTH_SHORT).show());
                            }
                        });
                    }
                });

        b.toolbar.setNavigationOnClickListener(v -> finish());

        b.languageButton.setOnClickListener(v -> chooseLanguage());
        b.themeButton.setOnClickListener(v -> chooseTheme());
        b.lowStockButton.setOnClickListener(v -> editLowStock());
        b.expiryButton.setOnClickListener(v -> editExpiryDays());
        b.autoLockButton.setOnClickListener(v -> chooseAutoLock());
        b.changePinButton.setOnClickListener(v -> changePin());
        b.unitsButton.setOnClickListener(v -> openReference(ReferenceActivity.TYPE_UNIT));
        b.categoriesButton.setOnClickListener(v -> openReference(ReferenceActivity.TYPE_CATEGORY));
        b.exportCsvButton.setOnClickListener(v -> exportCsvLauncher.launch("tovarlar.csv"));
        b.importCsvButton.setOnClickListener(v -> importCsvLauncher.launch(new String[]{"text/csv", "text/comma-separated-values", "text/plain", "*/*"}));
        b.clearImagesButton.setOnClickListener(v -> confirmClearImages());
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshLabels();
    }

    private void refreshLabels() {
        boolean cyr = AppPrefs.LANG_CYR.equals(AppPrefs.getLang());
        b.languageButton.setText(getString(R.string.settings_button_value,
                getString(R.string.language),
                getString(cyr ? R.string.language_cyr : R.string.language_lat)));

        int theme = AppPrefs.getTheme();
        int themeRes = theme == AppPrefs.THEME_LIGHT ? R.string.theme_light
                : theme == AppPrefs.THEME_DARK ? R.string.theme_dark : R.string.theme_system;
        b.themeButton.setText(getString(R.string.settings_button_value,
                getString(R.string.theme), getString(themeRes)));

        b.lowStockButton.setText(getString(R.string.settings_button_value,
                getString(R.string.low_stock_limit),
                Fmt.qty(AppPrefs.getLowStockLimit(), true)));

        b.expiryButton.setText(getString(R.string.settings_button_value,
                getString(R.string.expiry_warning_days),
                getString(R.string.days_short, AppPrefs.getExpiryWarningDays())));

        long lock = AppPrefs.getAutoLockMs();
        int lockRes = lock == AppPrefs.LOCK_IMMEDIATE ? R.string.lock_immediate
                : lock == AppPrefs.LOCK_5_MIN ? R.string.lock_5min : R.string.lock_1min;
        b.autoLockButton.setText(getString(R.string.settings_button_value,
                getString(R.string.auto_lock), getString(lockRes)));
    }

    private void chooseLanguage() {
        String[] items = {
                getString(R.string.language_lat),
                getString(R.string.language_cyr)
        };
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.language)
                .setItems(items, (d, which) -> {
                    String lang = which == 0 ? AppPrefs.LANG_LAT : AppPrefs.LANG_CYR;
                    AppPrefs.setLang(lang);
                    DukonApp.applyLanguage(lang);
                })
                .show();
    }

    private void chooseTheme() {
        String[] items = {
                getString(R.string.theme_system),
                getString(R.string.theme_light),
                getString(R.string.theme_dark)
        };
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.theme)
                .setItems(items, (d, which) -> {
                    int theme = which == 1 ? AppPrefs.THEME_LIGHT
                            : which == 2 ? AppPrefs.THEME_DARK : AppPrefs.THEME_SYSTEM;
                    AppPrefs.setTheme(theme);
                    DukonApp.applyTheme(theme);
                })
                .show();
    }

    private void editLowStock() {
        DialogNumberBinding db = DialogNumberBinding.inflate(getLayoutInflater());
        db.numberInput.setText(Fmt.qty(AppPrefs.getLowStockLimit(), true));
        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.low_stock_limit)
                .setView(db.getRoot())
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.save, null)
                .create();
        dialog.setOnShowListener(x -> dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    Double value = Fmt.parseQty(textOf(db));
                    if (value == null) {
                        db.numberLayout.setError(getString(R.string.qty_invalid));
                        return;
                    }
                    AppPrefs.setLowStockLimit(value.floatValue());
                    dialog.dismiss();
                    refreshLabels();
                }));
        dialog.show();
    }

    private void editExpiryDays() {
        DialogNumberBinding db = DialogNumberBinding.inflate(getLayoutInflater());
        db.numberInput.setText(String.valueOf(AppPrefs.getExpiryWarningDays()));
        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.expiry_warning_days)
                .setView(db.getRoot())
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.save, null)
                .create();
        dialog.setOnShowListener(x -> dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    Double value = Fmt.parseQty(textOf(db));
                    if (value == null) {
                        db.numberLayout.setError(getString(R.string.qty_invalid));
                        return;
                    }
                    AppPrefs.setExpiryWarningDays((int) Math.max(0,
                            Math.min(36500, Math.round(value))));
                    dialog.dismiss();
                    refreshLabels();
                }));
        dialog.show();
    }

    private void chooseAutoLock() {
        String[] items = {
                getString(R.string.lock_immediate),
                getString(R.string.lock_1min),
                getString(R.string.lock_5min)
        };
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.auto_lock)
                .setItems(items, (d, which) -> {
                    long value = which == 0 ? AppPrefs.LOCK_IMMEDIATE
                            : which == 2 ? AppPrefs.LOCK_5_MIN : AppPrefs.LOCK_1_MIN;
                    AppPrefs.setAutoLockMs(value);
                    refreshLabels();
                })
                .show();
    }

    private void changePin() {
        DialogChangePinBinding db = DialogChangePinBinding.inflate(getLayoutInflater());
        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.change_pin)
                .setView(db.getRoot())
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.change, null)
                .create();
        dialog.setOnShowListener(x -> dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    db.currentPinLayout.setError(null);
                    db.newPinLayout.setError(null);
                    db.repeatPinLayout.setError(null);

                    String current = pinOf(db.currentPinInput);
                    String newPin = pinOf(db.newPinInput);
                    String repeat = pinOf(db.repeatPinInput);

                    if (current.length() != 4) {
                        db.currentPinLayout.setError(getString(R.string.pin_4digits));
                        return;
                    }
                    if (!PinManager.verify(current)) {
                        db.currentPinLayout.setError(PinManager.isLocked()
                                ? getString(R.string.pin_locked, PinManager.remainingLockSeconds())
                                : getString(R.string.pin_change_wrong));
                        return;
                    }
                    if (newPin.length() != 4) {
                        db.newPinLayout.setError(getString(R.string.pin_4digits));
                        return;
                    }
                    if (!newPin.equals(repeat)) {
                        db.repeatPinLayout.setError(getString(R.string.pin_mismatch));
                        return;
                    }
                    PinManager.setPin(newPin);
                    Toast.makeText(this, R.string.pin_changed, Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }));
        dialog.show();
    }

    private void openReference(int type) {
        Intent intent = new Intent(this, ReferenceActivity.class);
        intent.putExtra(ReferenceActivity.EXTRA_TYPE, type);
        startActivity(intent);
    }

    private void confirmClearImages() {
        Dialogs.confirm(this, R.string.clear_all_images,
                getString(R.string.clear_images_confirm),
                R.string.delete,
                () -> AppExecutors.io(() -> {
                    sardor.xiva.dukono.data.Repo.get().clearAllProductImages(this);
                    AppExecutors.main(() -> Toast.makeText(this, R.string.images_cleared, Toast.LENGTH_SHORT).show());
                }));
    }

    private static String textOf(DialogNumberBinding db) {
        return db.numberInput.getText() == null ? "" : db.numberInput.getText().toString();
    }

    private static String pinOf(android.widget.EditText input) {
        String s = input.getText() == null ? "" : input.getText().toString();
        return s.replaceAll("\\D", "");
    }
}
