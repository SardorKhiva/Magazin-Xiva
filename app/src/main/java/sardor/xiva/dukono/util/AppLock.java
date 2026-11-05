package sardor.xiva.dukono.util;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import sardor.xiva.dukono.ui.LockActivity;
import sardor.xiva.dukono.ui.PinActivity;

/**
 * Avtoqulf: ko'rinadigan activity'lar sonini sanaydi. Ilova fondan qaytganda sozlamadagi
 * vaqt o'tgan bo'lsa, LockActivity ochiladi. Konfiguratsiya o'zgarishida qulflanmaydi.
 */
public final class AppLock implements Application.ActivityLifecycleCallbacks {

    /** Konfiguratsiya o'zgarishi/tez qaytish uchun imtiyoz. */
    private static final long GRACE_MS = 800L;

    private static boolean unlocked = false;
    private static long leftAt = 0L;

    private int startedCount = 0;

    private AppLock() {}

    public static void init(Application app) {
        app.registerActivityLifecycleCallbacks(new AppLock());
    }

    public static boolean isUnlocked() {
        return unlocked;
    }

    public static void unlock() {
        unlocked = true;
        leftAt = 0L;
    }

    public static void lockNow() {
        unlocked = false;
    }

    private static void setContentVisible(Activity activity, boolean visible) {
        View content = activity.findViewById(android.R.id.content);
        if (content != null) {
            content.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
        }
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        startedCount++;

        if (!(activity instanceof LockActivity) && !(activity instanceof PinActivity)) {
            long timeout = AppPrefs.getAutoLockMs();
            long away = leftAt > 0 ? System.currentTimeMillis() - leftAt : 0;
            if (unlocked && leftAt > 0 && away >= Math.max(timeout, GRACE_MS)) {
                unlocked = false;
            }

            if (!unlocked) {
                setContentVisible(activity, false);
                Intent intent = new Intent(activity, LockActivity.class);
                activity.startActivity(intent);
            } else {
                leftAt = 0L;
                setContentVisible(activity, true);
            }
        }
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        startedCount--;
        if (startedCount < 0) startedCount = 0;
        if (startedCount == 0 && !activity.isChangingConfigurations()
                && !(activity instanceof LockActivity) && !(activity instanceof PinActivity)) {
            leftAt = System.currentTimeMillis();
        }
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        if (!(activity instanceof LockActivity) && !(activity instanceof PinActivity) && unlocked) {
            setContentVisible(activity, true);
        }
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {}

    @Override
    public void onActivityPaused(@NonNull Activity activity) {}

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {}

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {}
}
