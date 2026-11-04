package sardor.xiva.dukono.data.db;

import androidx.room.ColumnInfo;

/** Birlik yoki kategoriya ro'yxati qatori (ishlatilish soni bilan). */
public class RefItem {
    public long id;
    public String name;

    @ColumnInfo(name = "allow_fraction")
    public boolean allowFraction;

    @ColumnInfo(name = "usage_count")
    public int usageCount;
}
