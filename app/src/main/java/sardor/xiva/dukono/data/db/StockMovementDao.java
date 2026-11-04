package sardor.xiva.dukono.data.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface StockMovementDao {

    @Insert
    long insert(StockMovement movement);

    @Query("SELECT * FROM stock_movements WHERE product_id = :productId ORDER BY moved_at DESC, id DESC LIMIT :limit")
    List<StockMovement> recentForProduct(long productId, int limit);
}
