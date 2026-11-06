package sardor.xiva.dukono.data.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.RawQuery;
import androidx.room.Update;
import androidx.sqlite.db.SupportSQLiteQuery;

import java.util.List;

@Dao
public interface ProductDao {

    @Insert
    long insert(Product product);

    @Update
    void update(Product product);

    @Query("DELETE FROM products WHERE id = :id")
    void deleteById(long id);

    @Query("SELECT * FROM products WHERE id = :id")
    Product getById(long id);

    @Query("SELECT COUNT(*) FROM products")
    int countAll();

    @Query("SELECT COUNT(*) FROM products WHERE code = :code AND id != :excludeId")
    int countCode(String code, long excludeId);

    @Query("SELECT COUNT(*) FROM products WHERE name_sort = :nameSort AND id != :excludeId")
    int countNameSort(String nameSort, long excludeId);

    @RawQuery
    List<ProductListItem> search(SupportSQLiteQuery query);

    @Query("SELECT p.id AS id, p.name AS name, p.code AS code, p.price AS price, " +
           "p.balance AS balance, p.initial_qty AS initial_qty, u.name AS unit_name, " +
           "u.allow_fraction AS allow_fraction, p.expiry_date AS expiry_date, p.image_path AS image_path " +
           "FROM products p " +
           "JOIN units u ON u.id = p.unit_id " +
           "ORDER BY p.name_sort, p.id")
    List<ProductListItem> getAll();

    @Query("UPDATE products SET image_path = NULL")
    void clearAllImagePaths();
}
