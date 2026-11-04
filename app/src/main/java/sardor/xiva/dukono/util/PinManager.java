package sardor.xiva.dukono.util;

import android.util.Base64;

import java.security.MessageDigest;
import java.security.SecureRandom;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * PIN xeshlash va tekshirish. 4 xonali PIN uchun bu jiddiy kriptografik himoya emas —
 * faqat ko'z oldidan himoya. 5 xato = 30 soniya bloklash.
 */
public final class PinManager {

    private static final int ITERATIONS = 10000;
    private static final int KEY_BITS = 256;
    private static final int SALT_BYTES = 16;
    private static final int MAX_FAILS = 5;
    private static final long LOCK_MS = 30000L;

    private PinManager() {}

    public static boolean hasPin() {
        return !AppPrefs.getPinHash().isEmpty() && !AppPrefs.getPinSalt().isEmpty();
    }

    public static void setPin(String pin) {
        try {
            byte[] salt = new byte[SALT_BYTES];
            new SecureRandom().nextBytes(salt);
            String hash = hash(pin, salt);
            AppPrefs.setPin(hash, Base64.encodeToString(salt, Base64.NO_WRAP));
        } catch (Exception e) {
            throw new IllegalStateException("PIN saqlanmadi", e);
        }
    }

    public static boolean isLocked() {
        long until = AppPrefs.getLockUntil();
        if (until <= 0) return false;
        if (System.currentTimeMillis() >= until) {
            AppPrefs.resetPinAttempts();
            return false;
        }
        return true;
    }

    public static int remainingLockSeconds() {
        long until = AppPrefs.getLockUntil();
        long left = until - System.currentTimeMillis();
        if (left <= 0) return 0;
        return (int) Math.ceil(left / 1000.0);
    }

    /** PIN to'g'ri bo'lsa true; xato bo'lsa urinishlar soni oshadi. */
    public static boolean verify(String pin) {
        if (isLocked()) return false;
        try {
            byte[] salt = Base64.decode(AppPrefs.getPinSalt(), Base64.NO_WRAP);
            String actual = hash(pin, salt);
            byte[] a = actual.getBytes();
            byte[] b = AppPrefs.getPinHash().getBytes();
            if (MessageDigest.isEqual(a, b)) {
                AppPrefs.resetPinAttempts();
                return true;
            }
        } catch (Exception ignored) {
            // xato hisoblanadi
        }
        int fails = AppPrefs.getFailCount() + 1;
        if (fails >= MAX_FAILS) {
            AppPrefs.setFailCount(0);
            AppPrefs.setLockUntil(System.currentTimeMillis() + LOCK_MS);
        } else {
            AppPrefs.setFailCount(fails);
        }
        return false;
    }

    private static String hash(String pin, byte[] salt) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(pin.toCharArray(), salt, ITERATIONS, KEY_BITS);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        byte[] key = factory.generateSecret(spec).getEncoded();
        spec.clearPassword();
        return Base64.encodeToString(key, Base64.NO_WRAP);
    }
}
