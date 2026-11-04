package sardor.xiva.dukono.util;

import android.content.Context;
import android.content.SharedPreferences;

/** Sozlamalar ("dukon_prefs"). */
public final class AppPrefs {

    private static final String FILE = "dukon_prefs";

    public static final String LANG_LAT = "lat";
    public static final String LANG_CYR = "cyr";

    public static final int THEME_SYSTEM = 0;
    public static final int THEME_LIGHT = 1;
    public static final int THEME_DARK = 2;

    public static final long LOCK_IMMEDIATE = 0L;
    public static final long LOCK_1_MIN = 60000L;
    public static final long LOCK_5_MIN = 300000L;

    private static final String K_LANG = "lang";
    private static final String K_THEME = "theme";
    private static final String K_LOW_STOCK = "low_stock";
    private static final String K_EXPIRY_DAYS = "expiry_days";
    private static final String K_AUTO_LOCK = "auto_lock";
    private static final String K_PIN_HASH = "pin_hash";
    private static final String K_PIN_SALT = "pin_salt";
    private static final String K_FAIL_COUNT = "fail_count";
    private static final String K_LOCK_UNTIL = "lock_until";

    private static SharedPreferences sp;

    private AppPrefs() {}

    public static void init(Context context) {
        sp = context.getApplicationContext().getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    private static SharedPreferences p() {
        if (sp == null) throw new IllegalStateException("AppPrefs.init() chaqirilmagan");
        return sp;
    }

    public static String getLang() {
        return p().getString(K_LANG, LANG_LAT);
    }

    public static void setLang(String lang) {
        p().edit().putString(K_LANG, lang).apply();
    }

    public static int getTheme() {
        return p().getInt(K_THEME, THEME_SYSTEM);
    }

    public static void setTheme(int theme) {
        p().edit().putInt(K_THEME, theme).apply();
    }

    public static float getLowStockLimit() {
        return p().getFloat(K_LOW_STOCK, 5f);
    }

    public static void setLowStockLimit(float value) {
        p().edit().putFloat(K_LOW_STOCK, value).apply();
    }

    public static int getExpiryWarningDays() {
        return p().getInt(K_EXPIRY_DAYS, 30);
    }

    public static void setExpiryWarningDays(int days) {
        p().edit().putInt(K_EXPIRY_DAYS, days).apply();
    }

    public static long getAutoLockMs() {
        return p().getLong(K_AUTO_LOCK, LOCK_1_MIN);
    }

    public static void setAutoLockMs(long ms) {
        p().edit().putLong(K_AUTO_LOCK, ms).apply();
    }

    // ------------------------------------------------------------------- PIN

    public static String getPinHash() {
        return p().getString(K_PIN_HASH, "");
    }

    public static String getPinSalt() {
        return p().getString(K_PIN_SALT, "");
    }

    public static void setPin(String hash, String salt) {
        p().edit()
                .putString(K_PIN_HASH, hash)
                .putString(K_PIN_SALT, salt)
                .putInt(K_FAIL_COUNT, 0)
                .putLong(K_LOCK_UNTIL, 0)
                .apply();
    }

    public static int getFailCount() {
        return p().getInt(K_FAIL_COUNT, 0);
    }

    public static void setFailCount(int count) {
        p().edit().putInt(K_FAIL_COUNT, count).apply();
    }

    public static long getLockUntil() {
        return p().getLong(K_LOCK_UNTIL, 0);
    }

    public static void setLockUntil(long millis) {
        p().edit().putLong(K_LOCK_UNTIL, millis).apply();
    }

    public static void resetPinAttempts() {
        p().edit().putInt(K_FAIL_COUNT, 0).putLong(K_LOCK_UNTIL, 0).apply();
    }
}
