package sardor.xiva.dukono.util;

// Kerakli Android va Java kutubxonalarini import qilamiz
import android.content.Context;
import android.net.Uri;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import sardor.xiva.dukono.data.Repo;
import sardor.xiva.dukono.data.db.Category;
import sardor.xiva.dukono.data.db.Product;
import sardor.xiva.dukono.data.db.ProductListItem;
import sardor.xiva.dukono.data.db.StockMovement;
import sardor.xiva.dukono.data.db.Unit;

/**
 * Tovarlar ma'lumotlarini CSV faylga eksport va CSV faylidan import qiluvchi yordamchi sinf.
 * Excel va boshqa dasturlar bilan to'liq mos kelish uchun UTF-8 BOM ishlatiladi.
 */
public final class CsvHelper {

    // Konstruktorni shaxsiy qilamiz (Util sinfining nusxasi olinmasligi uchun)
    private CsvHelper() {}

    /**
     * Bazadagi barcha tovarlarni berilgan Uri fayliga CSV formatida eksport qiladi.
     *
     * @param context App konteksti
     * @param uri     Saqlanadigan CSV faylining Uri manzili
     * @return Eksport qilingan tovarlar soni
     * @throws Exception Faylga yozish jarayonidagi xatoliklar
     */
    public static int exportProductsToCsv(Context context, Uri uri) throws Exception {
        // Bazadan barcha tovarlar ro'yxatini olamiz
        List<ProductListItem> items = Repo.get().getAllProducts();

        // Barcha o'lchov birliklari va kategoriyalarni keshlaymiz
        List<Unit> units = Repo.get().allUnits();
        List<Category> categories = Repo.get().allCategories();

        // ID bo'yicha nomlarni tezkor topish uchun Map tayyorlaymiz
        Map<Long, String> unitMap = new HashMap<>();
        for (Unit u : units) unitMap.put(u.id, u.name);

        Map<Long, String> categoryMap = new HashMap<>();
        for (Category c : categories) categoryMap.put(c.id, c.name);

        // ContentResolver orqali faylga yozish oqimini (OutputStream) ochamiz
        try (OutputStream os = context.getContentResolver().openOutputStream(uri);
             OutputStreamWriter writer = new OutputStreamWriter(os, StandardCharsets.UTF_8)) {

            // Excel O'zbek (Lotin va Kirill) harflarini to'g'ri tanishi uchun UTF-8 BOM belgisini yozamiz
            writer.write("\uFEFF");

            // CSV sarlavha (Header) qatorini yozamiz
            writer.write("Nomi,Kodi,Narxi,O'lchov birligi,Qoldig'i,Boshlang'ich miqdor,Kategoriya,Ishlab chiqaruvchi,Davlat,Yaroqlilik muddati,Izoh\n");

            int count = 0;
            for (ProductListItem item : items) {
                // Tovar to'liq obyektini bazadan olamiz (barcha qo'shimcha maydonlari uchun)
                Product p = Repo.get().getProduct(item.id);
                if (p == null) continue;

                // O'lchov birligi va kategoriya nomlarini aniqlaymiz
                String unitName = item.unitName != null ? item.unitName : unitMap.get(p.unitId);
                String categoryName = p.categoryId != null ? categoryMap.get(p.categoryId) : "";

                // Yaroqlilik muddatini matn shakliga o'giramiz
                String expiryStr = p.expiryDate != null ? Fmt.date(p.expiryDate) : "";

                // Har bir tovar maydonlarini CSV qatoriga aylantiramiz
                StringBuilder line = new StringBuilder();
                line.append(escapeCsv(p.name)).append(",");
                line.append(escapeCsv(p.code)).append(",");
                line.append(p.price).append(",");
                line.append(escapeCsv(unitName)).append(",");
                line.append(p.balance).append(",");
                line.append(p.initialQty).append(",");
                line.append(escapeCsv(categoryName)).append(",");
                line.append(escapeCsv(p.manufacturer)).append(",");
                line.append(escapeCsv(p.country)).append(",");
                line.append(escapeCsv(expiryStr)).append(",");
                line.append(escapeCsv(p.note)).append("\n");

                // Qatorni faylga yozamiz
                writer.write(line.toString());
                count++;
            }
            // Buferni bo'shatib faylga saqlaymiz
            writer.flush();
            return count;
        }
    }

    /**
     * CSV fayldan tovarlar ma'lumotlarini o'qib bazaga saqlaydi (Import qiladi).
     *
     * @param context App konteksti
     * @param uri     Tanlangan CSV fayl Uri manzili
     * @return Muvaffaqiyatli import qilingan tovarlar soni
     * @throws Exception Fayldan o'qish va parse qilishdagi xatoliklar
     */
    public static int importProductsFromCsv(Context context, Uri uri) throws Exception {
        // Bazada mavjud birlik va kategoriyalarni keshlaymiz
        List<Unit> existingUnits = Repo.get().allUnits();
        List<Category> existingCategories = Repo.get().allCategories();

        Map<String, Unit> unitByName = new HashMap<>();
        for (Unit u : existingUnits) {
            unitByName.put(u.name.toLowerCase(Locale.ROOT).trim(), u);
        }

        Map<String, Category> categoryByName = new HashMap<>();
        for (Category c : existingCategories) {
            categoryByName.put(c.name.toLowerCase(Locale.ROOT).trim(), c);
        }

        int importedCount = 0;

        // ContentResolver orqali faylni o'qish oqimini (InputStream) ochamiz
        try (InputStream is = context.getContentResolver().openInputStream(uri);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {

            String line;
            boolean isHeader = true;

            // Qatorma-qator o'qiymiz
            while ((line = reader.readLine()) != null) {
                // UTF-8 BOM belgisini tozalaymiz (agar birinchi satrda bo'lsa)
                if (isHeader && line.startsWith("\uFEFF")) {
                    line = line.substring(1);
                }

                // Bo'sh satrlarni o'tkazib yuboramiz
                if (line.trim().isEmpty()) continue;

                // Qatorni CSV elementlariga ajratamiz
                List<String> tokens = parseCsvLine(line);
                if (tokens.isEmpty()) continue;

                // Birinchi sarlavha (Header) qatorini o'tkazib yuboramiz
                if (isHeader) {
                    isHeader = false;
                    String firstToken = tokens.get(0).toLowerCase(Locale.ROOT);
                    if (firstToken.contains("nom") || firstToken.contains("name")) {
                        continue;
                    }
                }

                // Tovar nomini olamiz
                String name = getSafeToken(tokens, 0);
                if (name.isEmpty()) continue; // Nom kiritilishi shart

                // Tovar kodini olamiz (yo'q bo'lsa avtomatik hosil qilamiz)
                String code = getSafeToken(tokens, 1);
                if (code.isEmpty()) {
                    code = "CSV" + System.currentTimeMillis() + (int)(Math.random() * 100);
                }

                // Narxini parse qilamiz
                Long priceObj = Fmt.parseMoney(getSafeToken(tokens, 2));
                long price = priceObj != null ? priceObj : 0L;

                // O'lchov birligi nomini olamiz
                String unitName = getSafeToken(tokens, 3);
                if (unitName.isEmpty()) unitName = "dona";

                // Qoldiq va boshlang'ich miqdorni parse qilamiz
                Double balanceObj = Fmt.parseQty(getSafeToken(tokens, 4));
                double balance = balanceObj != null ? balanceObj : 0.0;

                Double initQtyObj = Fmt.parseQty(getSafeToken(tokens, 5));
                double initialQty = initQtyObj != null ? initQtyObj : balance;

                // Qolgan maydonlarni olamiz
                String categoryName = getSafeToken(tokens, 6);
                String manufacturer = getSafeToken(tokens, 7);
                String country = getSafeToken(tokens, 8);
                String expiryStr = getSafeToken(tokens, 9);
                String note = getSafeToken(tokens, 10);

                // O'lchov birligini aniqlaymiz yoki bazaga yangi qo'shamiz
                Unit unit = unitByName.get(unitName.toLowerCase(Locale.ROOT).trim());
                if (unit == null) {
                    unit = new Unit();
                    unit.name = unitName.trim();
                    unit.allowFraction = true;
                    unit.id = Repo.get().saveUnit(unit);
                    unitByName.put(unit.name.toLowerCase(Locale.ROOT).trim(), unit);
                }

                // Kategoriyani aniqlaymiz yoki bazaga yangi qo'shamiz
                Long categoryId = null;
                if (!categoryName.isEmpty()) {
                    Category cat = categoryByName.get(categoryName.toLowerCase(Locale.ROOT).trim());
                    if (cat == null) {
                        cat = new Category();
                        cat.name = categoryName.trim();
                        cat.id = Repo.get().saveCategory(cat);
                        categoryByName.put(cat.name.toLowerCase(Locale.ROOT).trim(), cat);
                    }
                    categoryId = cat.id;
                }

                // Yaroqlilik muddatini UTC kun shakliga o'giramiz
                Long expiryEpochDay = parseDateToEpochDay(expiryStr);

                // Yangi tovar obyektini tayyorlaymiz
                String nameSort = UzText.sortKey(name);
                String searchText = UzText.normalize(name + " " + code + " " + manufacturer + " " + country + " " + note);

                Product p = new Product();
                p.name = name;
                p.nameSort = nameSort;
                p.code = code;
                p.price = price;
                p.unitId = unit.id;
                p.initialQty = initialQty;
                p.balance = balance;
                p.categoryId = categoryId;
                p.manufacturer = manufacturer.isEmpty() ? null : manufacturer;
                p.country = country.isEmpty() ? null : country;
                p.expiryDate = expiryEpochDay;
                p.note = note.isEmpty() ? null : note;
                p.searchText = searchText;

                // Kod bo'yicha bazada takrorlanish yo'qligini tekshiramiz
                int codeDup = Repo.get().countCode(code, 0);
                if (codeDup > 0) {
                    // Kod takrorlansa kodinga tasodifiy qo'shimcha ulaymiz
                    p.code = code + "_" + ((int)(Math.random() * 899 + 100));
                }

                // Tovarni bazaga saqlaymiz
                Repo.get().saveProduct(p, true, StockMovement.REASON_INITIAL, "CSV import");
                importedCount++;
            }
        }
        return importedCount;
    }

    /**
     * List dan xavfsiz element olish yordamchi metodi.
     */
    private static String getSafeToken(List<String> tokens, int index) {
        if (index < tokens.size()) {
            String val = tokens.get(index);
            return val != null ? val.trim() : "";
        }
        return "";
    }

    /**
     * CSV formatida belgilarni muhofaza qilish (escape) yordamchi metodi.
     */
    private static String escapeCsv(String input) {
        if (input == null) return "";
        if (input.contains(",") || input.contains("\"") || input.contains("\n") || input.contains("\r")) {
            return "\"" + input.replace("\"", "\"\"") + "\"";
        }
        return input;
    }

    /**
     * CSV satrini tirnoqlar va ajratgichlarni e'tiborga olib parse qilish.
     */
    private static List<String> parseCsvLine(String line) {
        List<String> tokens = new ArrayList<>();
        if (line == null) return tokens;

        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        char delimiter = line.contains(";") && !line.contains(",") ? ';' : ',';

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    sb.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == delimiter && !inQuotes) {
                tokens.add(sb.toString().trim());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        tokens.add(sb.toString().trim());
        return tokens;
    }

    /**
     * Matnli sanani (dd.MM.yyyy / yyyy-MM-dd) epoch day ga o'tkazish.
     */
    private static Long parseDateToEpochDay(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return null;
        String s = dateStr.trim();
        String[] formats = {"dd.MM.yyyy", "yyyy-MM-dd", "dd/MM/yyyy"};
        for (String fmt : formats) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(fmt, Locale.ROOT);
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date d = sdf.parse(s);
                if (d != null) {
                    return d.getTime() / Fmt.DAY_MS;
                }
            } catch (Exception ignored) {}
        }
        return null;
    }
}
