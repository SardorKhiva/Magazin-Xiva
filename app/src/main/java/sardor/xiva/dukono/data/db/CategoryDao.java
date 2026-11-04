package sardor.xiva.dukono.data.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface CategoryDao {

    @Insert
    long insert(Category category);

    @Update
    void update(Category category);

    @Query("DELETE FROM categories WHERE id = :id")
    void deleteById(long id);

    @Query("SELECT * FROM categories WHERE id = :id")
    Category getById(long id);

    @Query("SELECT * FROM categories ORDER BY name COLLATE NOCASE")
    List<Category> all();

    @Query("SELECT c.id AS id, c.name AS name, 0 AS allow_fraction, " +
            "(SELECT COUNT(*) FROM products p WHERE p.category_id = c.id) AS usage_count " +
            "FROM categories c ORDER BY c.name COLLATE NOCASE")
    List<RefItem> allWithUsage();

    @Query("SELECT COUNT(*) FROM products WHERE category_id = :id")
    int countUsing(long id);

    @Query("SELECT COUNT(*) FROM categories WHERE name = :name AND id != :excludeId")
    int countName(String name, long excludeId);
}
