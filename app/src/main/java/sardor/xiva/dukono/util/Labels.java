package sardor.xiva.dukono.util;

import androidx.annotation.StringRes;

import sardor.xiva.dukono.R;
import sardor.xiva.dukono.data.db.StockMovement;

/** Harakat sabablarining matn resurslari. */
public final class Labels {

    private Labels() {}

    @StringRes
    public static int reason(String code) {
        if (code == null) return R.string.reason_other;
        switch (code) {
            case StockMovement.REASON_INITIAL: return R.string.reason_initial;
            case StockMovement.REASON_IN: return R.string.reason_in;
            case StockMovement.REASON_OUT: return R.string.reason_out;
            case StockMovement.REASON_ADJUST: return R.string.reason_adjust;
            case StockMovement.REASON_DEFECT: return R.string.reason_defect;
            default: return R.string.reason_other;
        }
    }
}
