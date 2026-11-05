package sardor.xiva.dukono.data;

import android.content.Context;

import androidx.sqlite.db.SimpleSQLiteQuery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import sardor.xiva.dukono.data.db.AppDatabase;
import sardor.xiva.dukono.data.db.Category;
import sardor.xiva.dukono.data.db.Product;
import sardor.xiva.dukono.data.db.ProductListItem;
import sardor.xiva.dukono.data.db.RefItem;
import sardor.xiva.dukono.data.db.SearchHistory;
import sardor.xiva.dukono.data.db.StockMovement;
import sardor.xiva.dukono.data.db.Unit;
import sardor.xiva.dukono.util.UzText;

/**
 * Ma'lumotlarga yagona kirish nuqtasi. Barcha metodlar fon oqimida chaqirilishi shart
 * (Room asosiy oqimda ishlamaydi).
 */
public final class Repo {

    private static volatile Repo INSTANCE;

    private final AppDatabase db;

    private Repo(Context context) {
        db = AppDatabase.get(context);
    }

    public static Repo get(Context context) {
        if (INSTANCE == null) {
            synchronized (Repo.class) {
                if (INSTANCE == null) {
                    INSTANCE = new Repo(context.getApplicationContext());
                }
            }
        }
        return INSTANCE;
    }

    public static Repo get() {
        Repo r = INSTANCE;
        if (r == null) {
            throw new IllegalStateException("Repo.init() chaqirilmagan");
        }
        return r;
    }

    // ---------------------------------------------------------------- qidiruv

    /**
     * Matn bo'yicha qidiruv. Har bir so'z alohida AND sharti; lotin/kirill farqi yo'q.
     * Kod aniq mos kelsa, o'sha tovar ro'yxat boshida turadi.
     */
    public List<ProductListItem> search(String rawQuery) {
        String normalized = UzText.normalize(rawQuery);
        if (normalized.isEmpty()) return Collections.emptyList();

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT p.id AS id, p.name AS name, p.code AS code, p.price AS price, ")
                .append("p.balance AS balance, p.initial_qty AS initial_qty, u.name AS unit_name, ")
                .append("u.allow_fraction AS allow_fraction, p.expiry_date AS expiry_date, p.image_path AS image_path ")
                .append("FROM products p ")
                .append("JOIN units u ON u.id = p.unit_id ")
                .append("LEFT JOIN categories c ON c.id = p.category_id ")
                .append("WHERE ");

        List<Object> args = new ArrayList<>();
        String[] words = normalized.split(" ");
        for (String word : words) {
            if (word.isEmpty()) continue;
            if (!args.isEmpty()) sql.append(" AND ");
            sql.append("(p.search_text || ' ' || IFNULL(c.name_search, '')) LIKE ? ESCAPE '\\'");
            args.add("%" + UzText.escapeLike(word) + "%");
        }
        sql.append(" ORDER BY CASE WHEN p.code = ? THEN 0 ELSE 1 END, p.name_sort, p.id");
        args.add(rawQuery == null ? "" : rawQuery.trim());

        return db.productDao().search(new SimpleSQLiteQuery(sql.toString(), args.toArray()));
    }

    public List<ProductListItem> getAllProducts() {
        return db.productDao().getAll();
    }

    public int countProducts() {
        return db.productDao().countAll();
    }

    public Product getProduct(long id) {
        return db.productDao().getById(id);
    }

    public int countCode(String code, long excludeId) {
        return db.productDao().countCode(code, excludeId);
    }

    public int countSameName(String nameSort, long excludeId) {
        return db.productDao().countNameSort(nameSort, excludeId);
    }

    /**
     * Tovarni saqlaydi (yangi yoki tahrirlangan). Qoldiq o'zgargan bo'lsa harakat yoziladi.
     *
     * @param reason tahrirlashda qoldiq o'zgargandagi sabab kodi
     * @param note   sabab izohi (ixtiyoriy)
     */
    public long saveProduct(Product product, boolean isNew, String reason, String note) {
        final long[] resultId = new long[1];
        db.runInTransaction(() -> {
            long now = System.currentTimeMillis();
            if (isNew) {
                product.createdAt = now;
                product.updatedAt = now;
                resultId[0] = db.productDao().insert(product);
                product.id = resultId[0];
                if (Math.abs(product.balance) > 0.0000001) {
                    addMovement(product.id, product.balance,
                            StockMovement.REASON_INITIAL, null, now);
                }
            } else {
                Product old = db.productDao().getById(product.id);
                product.createdAt = old != null ? old.createdAt : now;
                product.updatedAt = now;
                db.productDao().update(product);
                if (old != null) {
                    double delta = product.balance - old.balance;
                    if (Math.abs(delta) > 0.0000001) {
                        addMovement(product.id, delta,
                                reason != null ? reason : StockMovement.REASON_OTHER, note, now);
                    }
                }
                resultId[0] = product.id;
            }
        });
        return resultId[0];
    }

    public void deleteProduct(long id) {
        db.productDao().deleteById(id);
    }

    private void addMovement(long productId, double delta, String reason, String note, long movedAt) {
        StockMovement m = new StockMovement();
        m.productId = productId;
        m.delta = delta;
        m.reason = reason != null ? reason : StockMovement.REASON_OTHER;
        m.note = note;
        m.movedAt = movedAt;
        db.stockMovementDao().insert(m);
    }

    public List<StockMovement> recentMovements(long productId, int limit) {
        return db.stockMovementDao().recentForProduct(productId, limit);
    }

    // -------------------------------------------------------------- birliklar

    public List<Unit> allUnits() {
        return db.unitDao().all();
    }

    public Unit getUnit(long id) {
        return db.unitDao().getById(id);
    }

    public List<RefItem> unitsWithUsage() {
        return db.unitDao().allWithUsage();
    }

    public int countUnitName(String name, long excludeId) {
        return db.unitDao().countName(name, excludeId);
    }

    public int countProductsUsingUnit(long unitId) {
        return db.unitDao().countUsing(unitId);
    }

    public long saveUnit(Unit unit) {
        if (unit.id == 0) return db.unitDao().insert(unit);
        db.unitDao().update(unit);
        return unit.id;
    }

    public void deleteUnit(long id) {
        db.unitDao().deleteById(id);
    }

    // ---------------------------------------------------------- kategoriyalar

    public List<Category> allCategories() {
        return db.categoryDao().all();
    }

    public Category getCategory(long id) {
        return db.categoryDao().getById(id);
    }

    public List<RefItem> categoriesWithUsage() {
        return db.categoryDao().allWithUsage();
    }

    public int countCategoryName(String name, long excludeId) {
        return db.categoryDao().countName(name, excludeId);
    }

    public int countProductsInCategory(long categoryId) {
        return db.categoryDao().countUsing(categoryId);
    }

    public long saveCategory(Category category) {
        category.nameSearch = UzText.normalize(category.name);
        if (category.id == 0) return db.categoryDao().insert(category);
        db.categoryDao().update(category);
        return category.id;
    }

    public void deleteCategory(long id) {
        db.categoryDao().deleteById(id);
    }

    // ------------------------------------------------------- qidiruv tarixi

    public void addHistory(String query) {
        String text = UzText.collapse(query);
        if (text.isEmpty()) return;
        String key = UzText.normalize(text);
        if (key.isEmpty()) return;

        SearchHistory h = new SearchHistory();
        h.queryText = text;
        h.queryKey = key;
        h.usedAt = System.currentTimeMillis();

        db.runInTransaction(() -> {
            db.searchHistoryDao().deleteByKey(key);
            db.searchHistoryDao().insert(h);
            db.searchHistoryDao().trim(20);
        });
    }

    public List<SearchHistory> recentHistory(int limit) {
        return db.searchHistoryDao().recent(limit);
    }

    public void clearHistory() {
        db.searchHistoryDao().clear();
    }
}
