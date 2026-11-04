package sardor.xiva.dukono.data.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface SearchHistoryDao {

    @Insert
    long insert(SearchHistory history);

    @Query("SELECT * FROM search_history ORDER BY used_at DESC, id DESC LIMIT :limit")
    List<SearchHistory> recent(int limit);

    @Query("DELETE FROM search_history WHERE query_key = :key")
    void deleteByKey(String key);

    @Query("DELETE FROM search_history WHERE id IN (" +
            "SELECT id FROM search_history ORDER BY used_at DESC, id DESC LIMIT -1 OFFSET :keep)")
    void trim(int keep);

    @Query("DELETE FROM search_history")
    void clear();
}
