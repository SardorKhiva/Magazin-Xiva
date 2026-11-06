package sardor.xiva.dukono.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Pul, miqdor va sanalarni formatlash/parse qilish.
 */
public final class Fmt {

    public static final long DAY_MS = 86400000L;

    /**
     * Miqdor yuqori chegarasi (Infinity va ma'nosiz qiymatlardan himoya).
     */
    public static final BigDecimal MAX_QTY = new BigDecimal("999999999.999");
    /**
     * Narx yuqori chegarasi: 1 trillion so'm.
     */
    public static final long MAX_MONEY = 1_000_000_000_000L;

    private static final char NBSP = '\u00A0';

    private Fmt() {
    }

    // ------------------------------------------------------------- pul

    /**
     * 12500 -> "12 500" (mingliklar NBSP bilan).
     */
    public static String money(long value) {
        boolean negative = value < 0;
        String grouped = groupDigits(Long.toString(Math.abs(value)));
        return negative ? "-" + grouped : grouped;
    }

    private static String groupDigits(String digits) {
        StringBuilder sb = new StringBuilder(digits.length() + digits.length() / 3);
        int len = digits.length();
        for (int i = 0; i < len; i++) {
            if (i > 0 && (len - i) % 3 == 0) sb.append(NBSP);
            sb.append(digits.charAt(i));
        }
        return sb.toString();
    }

    /**
     * Pul maydonini parse qiladi (faqat butun so'm). Qabul qilinadi:
     * "12500", "12 500", "12500.00", "12 500,00", "12.500", "12,500", "12500 so'm".
     * Kasr qismi noldan farqli ("12500.50", "1,5") yoki manfiy bo'lsa, null qaytaradi
     * (jimgina yaxlitlash o'rniga xato bildiriladi).
     * Eslatma: "1.500" mingliklar ajratgichi deb hisoblanadi (1500).
     */
    public static Long parseMoney(String raw) {
        if (raw == null) return null;
        String s = raw.trim().toLowerCase(Locale.ROOT)
                .replace(NBSP, ' ').replace(" ", "");
        s = s.replaceAll("(so['\u2018\u2019\u02BB\u02BC`]?m|sum|\u0441\u045E\u043C|\u0441\u0443\u043C|uzs)$", "");
        if (s.isEmpty()) return null;
        try {
            BigDecimal value;
            if (s.matches("\\d+")) {
                value = new BigDecimal(s);
            } else if (s.matches("\\d{1,3}([.,]\\d{3})+")) {          // 12.500 / 1,234,567
                value = new BigDecimal(s.replace(".", "").replace(",", ""));
            } else if (s.matches("\\d+[.,]\\d{1,2}")) {                // 12500.00 / 12500,5
                BigDecimal d = new BigDecimal(s.replace(',', '.')).stripTrailingZeros();
                if (d.scale() > 0) return null;                        // haqiqiy kasr bor
                value = d;
            } else {
                return null;
            }
            if (value.compareTo(BigDecimal.valueOf(MAX_MONEY)) > 0) return null;
            return value.longValueExact();
        } catch (ArithmeticException | NumberFormatException e) {
            return null;
        }
    }

    // ----------------------------------------------------------- miqdor

    /**
     * Miqdorni ko'rsatish: ortiqcha nollarsiz, 3 xonagacha, o'nlik ajratgich vergul,
     * mingliklar NBSP bilan. Birlikdagi "kasr ruxsat" belgisiga qaramaydi, shuning uchun
     * haqiqiy qiymat hech qachon yashirilmaydi (2,5 "3" bo'lib qolmaydi).
     */
    public static String qty(double value) {
        String plain = BigDecimal.valueOf(value).setScale(3, RoundingMode.HALF_UP)
                .stripTrailingZeros().toPlainString();                 // "12500.125" | "-1.5" | "100"
        boolean negative = plain.startsWith("-");
        if (negative) plain = plain.substring(1);
        int dot = plain.indexOf('.');
        String intPart = dot < 0 ? plain : plain.substring(0, dot);
        String frac = dot < 0 ? "" : plain.substring(dot + 1);
        return (negative ? "-" : "") + groupDigits(intPart) + (frac.isEmpty() ? "" : "," + frac);
    }

    /**
     * Eski chaqiruvlar buzilmasligi uchun; endi {@link #qty(double)} ga teng.
     */
    @Deprecated
    public static String qty(double value, boolean allowFraction) {
        return qty(value);
    }

    /**
     * Ishorali miqdor (harakatlar tarixi): +2 / -1,5.
     */
    public static String signedQty(double value) {
        String s = qty(Math.abs(value));
        return (value > 0 ? "+" : value < 0 ? "-" : "") + s;
    }

    @Deprecated
    public static String signedQty(double value, boolean allowFraction) {
        return signedQty(value);
    }

    /**
     * Kiritish maydoni uchun: mingliklarsiz, vergul bilan ("12500,5").
     */
    public static String qtyInput(double value) {
        return BigDecimal.valueOf(value).setScale(3, RoundingMode.HALF_UP)
                .stripTrailingZeros().toPlainString().replace('.', ',');
    }

    /**
     * Miqdorni parse qiladi. Vergul nuqtaga almashtiriladi, bo'sh joylar olib tashlanadi.
     * Noto'g'ri, manfiy yoki {@link #MAX_QTY} dan katta bo'lsa null.
     */
    public static Double parseQty(String raw) {
        if (raw == null) return null;
        String s = raw.trim().replace(NBSP, ' ').replace(" ", "").replace(',', '.');
        if (s.isEmpty()) return null;
        try {
            BigDecimal bd = new BigDecimal(s);
            if (bd.signum() < 0 || bd.compareTo(MAX_QTY) > 0) return null;
            return bd.doubleValue();
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static double round3(double value) {
        return BigDecimal.valueOf(value).setScale(3, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * float'ni o'nlik ko'rinishi bo'yicha double'ga o'tkazadi: 2.3f -> 2.3 (2.299999952... emas).
     */
    public static double fromFloat(float value) {
        return Double.parseDouble(Float.toString(value));
    }

    /**
     * Qoldiq chegaradan kam yoki teng bo'lsa true (3 xonagacha yaxlitlab solishtiradi).
     */
    public static boolean isLowStock(double balance, double limit) {
        return round3(balance) <= round3(limit);
    }

    // ------------------------------------------------------------ sanalar

    /**
     * UTC epoch kunni "dd.MM.yyyy" ko'rinishida qaytaradi.
     */
    public static String date(long epochDay) {
        SimpleDateFormat f = new SimpleDateFormat("dd.MM.yyyy", Locale.ROOT);
        f.setTimeZone(TimeZone.getTimeZone("UTC"));
        return f.format(new Date(epochDay * DAY_MS));
    }

    public static String dateTime(long millis) {
        SimpleDateFormat f = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.ROOT);
        return f.format(new Date(millis));
    }

    /**
     * Bugungi MAHALLIY sana, yaroqlilik muddati saqlanadigan formatda (UTC yarim tundagi epoch kun).
     * Eski variant System.currentTimeMillis()/DAY_MS edi: UTC+5 da 00:00-05:00 oralig'ida kecha chiqardi.
     */
    public static long todayEpochDay() {
        return localEpochDay(System.currentTimeMillis(), TimeZone.getDefault());
    }

    static long localEpochDay(long nowMillis, TimeZone zone) {
        Calendar local = Calendar.getInstance(zone);
        local.setTimeInMillis(nowMillis);
        Calendar utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        utc.clear();
        utc.set(local.get(Calendar.YEAR), local.get(Calendar.MONTH), local.get(Calendar.DAY_OF_MONTH));
        return utc.getTimeInMillis() / DAY_MS;
    }

    public static long daysLeft(long expiryEpochDay) {
        return expiryEpochDay - todayEpochDay();
    }
}