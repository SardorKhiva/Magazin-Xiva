package sardor.xiva.dukono.ui;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.StringRes;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;

import sardor.xiva.dukono.R;
import sardor.xiva.dukono.databinding.DialogStockReasonBinding;
import sardor.xiva.dukono.data.db.StockMovement;
import sardor.xiva.dukono.util.Fmt;
import sardor.xiva.dukono.util.Ui;

/** Umumiy dialoglar. */
public final class Dialogs {

    public interface ReasonCallback {
        void onReason(String reason, String note);
    }

    private Dialogs() {}

    public static void alert(Context context, @StringRes int title, CharSequence message) {
        new MaterialAlertDialogBuilder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    public static void confirm(Context context, @StringRes int title, CharSequence message,
                               @StringRes int positive, Runnable onOk) {
        new MaterialAlertDialogBuilder(context)
                .setTitle(title)
                .setMessage(message)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(positive, (d, which) -> onOk.run())
                .show();
    }

    /**
     * Qoldiq o'zgarishi sababini so'raydi. "Boshqa" tanlansa izoh majburiy.
     */
    public static void stockReason(Activity activity, double oldBalance, double newBalance,
                                   boolean allowFraction, String unitName, ReasonCallback callback) {
        DialogStockReasonBinding b = DialogStockReasonBinding.inflate(activity.getLayoutInflater());
        double delta = newBalance - oldBalance;
        String deltaText = activity.getString(R.string.stock_delta_format,
                Fmt.signedQty(delta, allowFraction),
                Fmt.qty(Math.abs(delta), allowFraction) + " " + unitName,
                Fmt.qty(newBalance, allowFraction) + " " + unitName);
        b.deltaText.setText(deltaText);

        com.google.android.material.dialog.MaterialAlertDialogBuilder builder =
                new MaterialAlertDialogBuilder(activity)
                        .setTitle(R.string.stock_reason_title)
                        .setView(b.getRoot())
                        .setNegativeButton(R.string.cancel, null)
                        .setPositiveButton(R.string.save, null);

        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.setOnShowListener(d -> dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    b.noteLayout.setError(null);
                    String reason;
                    int checked = b.reasonGroup.getCheckedRadioButtonId();
                    if (checked == R.id.radioIn) reason = StockMovement.REASON_IN;
                    else if (checked == R.id.radioOut) reason = StockMovement.REASON_OUT;
                    else if (checked == R.id.radioAdjust) reason = StockMovement.REASON_ADJUST;
                    else if (checked == R.id.radioDefect) reason = StockMovement.REASON_DEFECT;
                    else if (checked == R.id.radioOther) reason = StockMovement.REASON_OTHER;
                    else {
                        Toast.makeText(activity, R.string.reason_required, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String note = text(b.noteInput.getText());
                    if (StockMovement.REASON_OTHER.equals(reason) && note.isEmpty()) {
                        b.noteLayout.setError(activity.getString(R.string.note_required));
                        return;
                    }
                    callback.onReason(reason, note.isEmpty() ? null : note);
                    dialog.dismiss();
                }));
        dialog.show();
    }

    private static String text(CharSequence s) {
        return s == null ? "" : s.toString().trim();
    }
}
