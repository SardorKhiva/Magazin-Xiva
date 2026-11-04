package sardor.xiva.dukono.util;

import java.util.Locale;

/**
 * O'zbekcha matn bilan ishlash: lotin va kirill alifbolarini bitta shaklga keltirish (qidiruv uchun)
 * hamda kirill alifbosi bo'yicha saralash kalitini hosil qilish.
 */
public final class UzText {

    private UzText() {}

    /** Kirill alifbosi tartibi (o'zbek: ... ъ, э, ю, я, ў, қ, ғ, ҳ). */
    private static final String CYR = "абвгдеёжзийклмнопрстуфхцчшщъыьэюяўқғҳ";

    /** Har bir kirill harfning qidiruv uchun lotincha shakli (CYR bilan bir xil tartibda). */
    private static final String[] LAT = {
            "a", "b", "v", "g", "d", "e", "yo", "j", "z", "i", "y", "k", "l", "m", "n", "o", "p", "r", "s", "t",
            "u", "f", "h", "s", "ch", "sh", "sh", "", "i", "", "e", "yu", "ya", "o", "k", "g", "h"
    };

    private static boolean isApostrophe(char c) {
        return c == '\'' || c == '\u2018' || c == '\u2019' || c == '\u02BB' || c == '\u02BC'
                || c == '`' || c == '\u00B4' || c == '\u2032';
    }

    /** Ortiqcha bo'sh joylarni olib tashlaydi, chetlarini kesadi. */
    public static String collapse(CharSequence s) {
        if (s == null) return "";
        return s.toString().trim().replaceAll("\\s+", " ");
    }

    /**
     * Qidiruv normalizatsiyasi: kichik harf, kirill -> lotin, o'/g' belgilari, q/k, x/h, w/v,
     * ye/e, ts/s farqlari yo'qotiladi. Ma'lumot ham, so'rov ham shu funksiyadan o'tadi.
     */
    public static String normalize(CharSequence s) {
        String t = collapse(s).toLowerCase(Locale.ROOT);
        StringBuilder sb = new StringBuilder(t.length() + 8);
        for (int i = 0; i < t.length(); i++) {
            char c = t.charAt(i);
            if (isApostrophe(c)) continue;
            int idx = CYR.indexOf(c);
            if (idx >= 0) {
                sb.append(LAT[idx]);
                continue;
            }
            switch (c) {
                case 'q': sb.append('k'); break;
                case 'x': sb.append('h'); break;
                case 'w': sb.append('v'); break;
                default: sb.append(c);
            }
        }
        return sb.toString().replace("ye", "e").replace("ts", "s");
    }

    private static char latToCyr(char c) {
        switch (c) {
            case 'a': return 'а';
            case 'b': return 'б';
            case 'd': return 'д';
            case 'f': return 'ф';
            case 'h': return 'ҳ';
            case 'i': return 'и';
            case 'j': return 'ж';
            case 'k': return 'к';
            case 'l': return 'л';
            case 'm': return 'м';
            case 'n': return 'н';
            case 'p': return 'п';
            case 'q': return 'қ';
            case 'r': return 'р';
            case 'u': return 'у';
            case 'v': return 'в';
            case 'w': return 'в';
            case 'x': return 'х';
            case 'z': return 'з';
            default: return 0;
        }
    }

    /** Lotincha matnni kirillga o'giradi (saralash uchun). Kirillcha harflar o'zgarishsiz qoladi. */
    public static String toCyrillic(CharSequence s) {
        String t = collapse(s).toLowerCase(Locale.ROOT);
        int n = t.length();
        StringBuilder sb = new StringBuilder(n + 4);
        for (int i = 0; i < n; i++) {
            char c = t.charAt(i);
            char nx = i + 1 < n ? t.charAt(i + 1) : 0;
            if (isApostrophe(c)) {
                sb.append('ъ');
                continue;
            }
            switch (c) {
                case 'o':
                    if (isApostrophe(nx)) { sb.append('ў'); i++; } else sb.append('о');
                    break;
                case 'g':
                    if (isApostrophe(nx)) { sb.append('ғ'); i++; } else sb.append('г');
                    break;
                case 's':
                    if (nx == 'h') { sb.append('ш'); i++; } else sb.append('с');
                    break;
                case 'c':
                    if (nx == 'h') { sb.append('ч'); i++; } else sb.append('к');
                    break;
                case 't':
                    if (nx == 's') { sb.append('ц'); i++; } else sb.append('т');
                    break;
                case 'y':
                    if (nx == 'o') { sb.append('ё'); i++; }
                    else if (nx == 'u') { sb.append('ю'); i++; }
                    else if (nx == 'a') { sb.append('я'); i++; }
                    else if (nx == 'e') { sb.append('е'); i++; }
                    else sb.append('й');
                    break;
                case 'e': {
                    boolean wordStart = i == 0 || !Character.isLetter(t.charAt(i - 1));
                    sb.append(wordStart ? 'э' : 'е');
                    break;
                }
                default: {
                    char m = latToCyr(c);
                    sb.append(m != 0 ? m : c);
                }
            }
        }
        return sb.toString();
    }

    /**
     * Saralash kaliti: nom kirillga o'giriladi va har bir harf alifbodagi o'rniga qarab
     * monoton ketma-ketlikka almashtiriladi (SQLite BINARY tartibi bilan to'g'ri ishlaydi).
     * Raqamlar va belgilar harflardan oldin keladi. Shu kalit takroriy nomni aniqlash uchun ham ishlatiladi.
     */
    public static String sortKey(CharSequence s) {
        String cyr = toCyrillic(s);
        StringBuilder sb = new StringBuilder(cyr.length());
        for (int i = 0; i < cyr.length(); i++) {
            char ch = cyr.charAt(i);
            int idx = CYR.indexOf(ch);
            sb.append(idx >= 0 ? (char) (0x100 + idx) : ch);
        }
        return sb.toString();
    }

    /** SQL LIKE uchun maxsus belgilarni ekranlaydi (ESCAPE '\'). */
    public static String escapeLike(String s) {
        return s.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

    static int cyrLengthForTest() { return CYR.length(); }
    static int latLengthForTest() { return LAT.length; }
}
