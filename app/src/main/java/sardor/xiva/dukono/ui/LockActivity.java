package sardor.xiva.dukono.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputFilter;
import android.view.View;
import android.view.inputmethod.EditorInfo;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import sardor.xiva.dukono.R;
import sardor.xiva.dukono.databinding.ActivityLockBinding;
import sardor.xiva.dukono.util.AppLock;
import sardor.xiva.dukono.util.PinManager;
import sardor.xiva.dukono.util.SimpleWatcher;
import sardor.xiva.dukono.util.Ui;

/** PIN o'rnatish (birinchi ishga tushish) yoki kirish. */
public class LockActivity extends AppCompatActivity {

    private ActivityLockBinding b;
    private boolean settingUp;
    private boolean confirmStage;
    private String firstPin;
    private boolean lockMessageShown;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable ticker = new Runnable() {
        @Override
        public void run() {
            updateLockUi();
            if (PinManager.isLocked()) {
                handler.postDelayed(this, 1000);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityLockBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finishAffinity();
            }
        });

        settingUp = !PinManager.hasPin();
        updateTitles();

        b.pinInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
        b.pinInput.addTextChangedListener(new SimpleWatcher() {
            @Override
            protected void onChanged(String text) {
                String digits = text.replaceAll("\\D", "");
                if (!digits.equals(text)) {
                    b.pinInput.setText(digits);
                    b.pinInput.setSelection(digits.length());
                    return;
                }
                if (!lockMessageShown) b.errorText.setVisibility(View.GONE);
                if (digits.length() == 4) submit();
            }
        });
        b.pinInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                submit();
                return true;
            }
            return false;
        });
        b.submitButton.setOnClickListener(v -> submit());

        updateLockUi();
        if (PinManager.isLocked()) handler.post(ticker);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateLockUi();
        b.pinInput.postDelayed(() -> {
            if (!isFinishing()) Ui.showKeyboard(b.pinInput);
        }, 200);
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(ticker);
        super.onDestroy();
    }

    private String pin() {
        return b.pinInput.getText() == null ? "" : b.pinInput.getText().toString();
    }

    private void submit() {
        if (PinManager.isLocked()) {
            updateLockUi();
            return;
        }
        String value = pin();
        if (value.length() != 4) {
            showError(getString(R.string.pin_4digits));
            return;
        }
        if (settingUp) {
            submitSetup(value);
        } else {
            submitLogin(value);
        }
    }

    private void submitSetup(String value) {
        if (!confirmStage) {
            firstPin = value;
            confirmStage = true;
            clearInput();
            updateTitles();
            return;
        }
        if (value.equals(firstPin)) {
            PinManager.setPin(value);
            AppLock.unlock();
            finish();
        } else {
            firstPin = null;
            confirmStage = false;
            clearInput();
            updateTitles();
            showError(getString(R.string.pin_mismatch));
        }
    }

    private void submitLogin(String value) {
        if (PinManager.verify(value)) {
            AppLock.unlock();
            finish();
        } else {
            clearInput();
            showError(getString(R.string.pin_wrong));
            if (PinManager.isLocked()) {
                updateLockUi();
                handler.post(ticker);
            }
        }
    }

    private void updateTitles() {
        if (settingUp && !confirmStage) {
            b.title.setText(R.string.pin_set_title);
            b.subtitle.setText(R.string.pin_set_subtitle);
            b.submitButton.setText(R.string.pin_set_button);
        } else if (settingUp) {
            b.title.setText(R.string.pin_repeat_title);
            b.subtitle.setText(R.string.pin_repeat_subtitle);
            b.submitButton.setText(R.string.pin_confirm_button);
        } else {
            b.title.setText(R.string.pin_enter_title);
            b.subtitle.setText(R.string.pin_enter_subtitle);
            b.submitButton.setText(R.string.enter);
        }
    }

    private void updateLockUi() {
        boolean locked = PinManager.isLocked();
        b.pinInput.setEnabled(!locked);
        b.submitButton.setEnabled(!locked);
        if (locked) {
            lockMessageShown = true;
            b.errorText.setVisibility(View.VISIBLE);
            b.errorText.setText(getString(R.string.pin_locked,
                    PinManager.remainingLockSeconds()));
        } else if (lockMessageShown) {
            lockMessageShown = false;
            b.errorText.setVisibility(View.GONE);
        }
    }

    private void showError(String message) {
        b.errorText.setVisibility(View.VISIBLE);
        b.errorText.setText(message);
    }

    private void clearInput() {
        b.pinInput.setText("");
    }
}
