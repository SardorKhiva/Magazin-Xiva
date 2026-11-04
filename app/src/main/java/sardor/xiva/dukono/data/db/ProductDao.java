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
}
