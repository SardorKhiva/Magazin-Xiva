package sardor.xiva.dukono.util;

import android.text.Editable;
import android.text.TextWatcher;

/** TextWatcher qisqartmasi. */
public abstract class SimpleWatcher implements TextWatcher {
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {}

    @Override
    public final void afterTextChanged(Editable s) {
        onChanged(s == null ? "" : s.toString());
    }

    protected abstract void onChanged(String text);
}
