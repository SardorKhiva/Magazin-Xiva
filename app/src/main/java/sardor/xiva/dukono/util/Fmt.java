package sardor.xiva.dukono.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/** Pul, miqdor va sanalarni formatlash/parse qilish. */
public final class Fmt {

    public static final long DAY_MS = 86400000L;

    private Fmt() {}

    /** 12500 -> "12 500" (mingliklar NBSP bilan). */
    public static String money(long value) {
        boolean negative = value < 0;
        String digits = Long.toString(Math.abs(value));
        StringBuilder sb = new StringBuilder(digits.length() + digits.length() / 3);
        int counter = 0;
        for (int i = digits.length() - 1; i >= 0; i--) {
            sb.append(digits.charAt(i));
            counter++;
            if (counter % 3 == 0 && i > 0) sb.append('\u00A0');
        }
        if (negative) sb.append('-');
        return sb.reverse().toString();
    }

    /** Miqdorni ko'rsatish: kasr birlikda 3 xonagacha, butun birlikda butun songa yaxlitlanadi. */
    public static String qty(double value, boolean allowFraction) {
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(allowFraction ? 3 : 0, RoundingMode.HALF_UP);
        String s = bd.stripTrailingZeros().toPlainString();
        return s.replace('.', ',');
    }

    /** Ishorali miqdor (harakatlar tarixi uchun): +2 / -1,5. */
    public static String signedQty(double value, boolean allowFraction) {
        String s = qty(Math.abs(value), allowFraction);
        return (value > 0 ? "+" : value < 0 ? "-" : "") + s;
    }

    /**
     * Miqdorni parse qiladi. Vergul nuqtaga almashtiriladi. Noto'g'ri yoki manfiy bo'lsa null.
     */
    public static Double parseQty(String raw) {
        if (raw == null) return null;
        String s = raw.trim().replace('\u00A0', ' ').replace(" ", "").replace(',', '.');
        if (s.isEmpty()) return null;
        try {
            BigDecimal bd = new BigDecimal(s);
            if (bd.signum() < 0) return null;
            return bd.doubleValue();
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** Pul maydonini parse qiladi: bo'sh joy va ajratgichlar olib tashlanadi. */
    public static Long parseMoney(String raw) {
        if (raw == null) return null;
        String s = raw.trim()
                .replace('\u00A0', ' ')
                .replace(" ", "")
                .replace("\u2019", "")
                .replace("'", "")
                .replace("so'm", "")
                .replace("so\u2018m", "");
        if (s.isEmpty()) return null;
        if (!s.matches("\\d+")) return null;
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** UTC epoch kunni "dd.MM.yyyy" ko'rinishida qaytaradi. */
    public static String date(long epochDay) {
        SimpleDateFormat f = new SimpleDateFormat("dd.MM.yyyy", Locale.ROOT);
        f.setTimeZone(TimeZone.getTimeZone("UTC"));
        return f.format(new Date(epochDay * DAY_MS));
    }

    public static String dateTime(long millis) {
        SimpleDateFormat f = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.ROOT);
        return f.format(new Date(millis));
    }

    /** Bugungi UTC kun raqami (epoch day). */
    public static long todayEpochDay() {
        return System.currentTimeMillis() / DAY_MS;
    }

    public static long daysLeft(long expiryEpochDay) {
        return expiryEpochDay - todayEpochDay();
    }
}
