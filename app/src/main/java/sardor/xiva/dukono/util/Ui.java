package sardor.xiva.dukono.util;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

/** Kichik UI yordamchilari. */
public final class Ui {

    public static final String DASH = "\u2014";

    private Ui() {}

    /** Bo'sh matn o'rniga "—" qo'yadi. */
    public static void text(TextView view, String value) {
        if (value == null || value.trim().isEmpty()) {
            view.setText(DASH);
        } else {
            view.setText(value);
        }
    }

    public static void hideKeyboard(Activity activity) {
        View focus = activity.getCurrentFocus();
        if (focus == null) focus = activity.getWindow().getDecorView();
        InputMethodManager imm = (InputMethodManager)
                activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(focus.getWindowToken(), 0);
    }

    public static void showKeyboard(View view) {
        if (view == null) return;
        view.requestFocus();
        InputMethodManager imm = (InputMethodManager)
                view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
    }
}
