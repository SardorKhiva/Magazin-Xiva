package sardor.xiva.dukono.data.db;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "products",
    foreignKeys = {
        @ForeignKey(
            entity = Unit.class,
            parentColumns = "id",
            childColumns = "unit_id",
            onDelete = ForeignKey.RESTRICT
        ),
        @ForeignKey(
            entity = Category.class,
            parentColumns = "id",
            childColumns = "category_id",
            onDelete = ForeignKey.SET_NULL
        )
    },
    indices = {
        @Index(value = "code", unique = true),
        @Index(value = "name_sort"),
        @Index(value = "unit_id"),
        @Index(value = "category_id")
    }
)
public class Product {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    public String name = "";

    @NonNull
    @ColumnInfo(name = "name_sort")
    public String nameSort = "";

    @NonNull
    @ColumnInfo(collate = ColumnInfo.NOCASE)
    public String code = "";

    public long price;

    @ColumnInfo(name = "unit_id")
    public long unitId;

    @ColumnInfo(name = "initial_qty")
    public double initialQty;

    public double balance;

    @ColumnInfo(name = "category_id")
    public Long categoryId;

    public String manufacturer;
    public String country;

    @ColumnInfo(name = "expiry_date")
    public Long expiryDate;

    public String note;

    @ColumnInfo(name = "image_path")
    public String imagePath;

    @NonNull
    @ColumnInfo(name = "search_text")
    public String searchText = "";

    @ColumnInfo(name = "created_at")
    public long createdAt;

    @ColumnInfo(name = "updated_at")
    public long updatedAt;
}
