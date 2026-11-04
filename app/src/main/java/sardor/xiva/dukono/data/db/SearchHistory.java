package sardor.xiva.dukono.data.db;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "search_history")
public class SearchHistory {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    @ColumnInfo(name = "query_text")
    public String queryText = "";

    @NonNull
    @ColumnInfo(name = "query_key")
    public String queryKey = "";

    @ColumnInfo(name = "used_at")
    public long usedAt;
}
