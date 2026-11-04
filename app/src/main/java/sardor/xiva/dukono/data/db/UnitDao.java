package sardor.xiva.dukono.data.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface UnitDao {

    @Insert
    long insert(Unit unit);

    @Update
    void update(Unit unit);

    @Query("DELETE FROM units WHERE id = :id")
    void deleteById(long id);

    @Query("SELECT * FROM units WHERE id = :id")
    Unit getById(long id);

    @Query("SELECT * FROM units ORDER BY name COLLATE NOCASE")
    List<Unit> all();

    @Query("SELECT u.id AS id, u.name AS name, u.allow_fraction AS allow_fraction, " +
            "(SELECT COUNT(*) FROM products p WHERE p.unit_id = u.id) AS usage_count " +
            "FROM units u ORDER BY u.name COLLATE NOCASE")
    List<RefItem> allWithUsage();

    @Query("SELECT COUNT(*) FROM products WHERE unit_id = :id")
    int countUsing(long id);

    @Query("SELECT COUNT(*) FROM units WHERE name = :name AND id != :excludeId")
    int countName(String name, long excludeId);

    @Query("SELECT COUNT(*) FROM units")
    int countAll();
}
