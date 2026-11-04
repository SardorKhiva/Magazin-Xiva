package sardor.xiva.dukono.data.db;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "categories", indices = @Index(value = "name", unique = true))
public class Category {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    @ColumnInfo(collate = ColumnInfo.NOCASE)
    public String name = "";

    @NonNull
    @ColumnInfo(name = "name_search")
    public String nameSearch = "";
}
