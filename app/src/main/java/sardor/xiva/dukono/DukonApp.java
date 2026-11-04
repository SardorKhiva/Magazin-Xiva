package sardor.xiva.dukono;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import sardor.xiva.dukono.data.Repo;
import sardor.xiva.dukono.util.AppLock;
import sardor.xiva.dukono.util.AppPrefs;

public class DukonApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AppPrefs.init(this);
        Repo.get(this);
        applyLanguage(AppPrefs.getLang());
        applyTheme(AppPrefs.getTheme());
        AppLock.init(this);
    }

    public static void applyLanguage(String lang) {
        String tag = AppPrefs.LANG_CYR.equals(lang) ? "uz-Cyrl" : "uz-Latn";
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag));
    }

    public static void applyTheme(int theme) {
        int mode;
        if (theme == AppPrefs.THEME_LIGHT) {
            mode = AppCompatDelegate.MODE_NIGHT_NO;
        } else if (theme == AppPrefs.THEME_DARK) {
            mode = AppCompatDelegate.MODE_NIGHT_YES;
        } else {
            mode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        }
        AppCompatDelegate.setDefaultNightMode(mode);
    }
}
