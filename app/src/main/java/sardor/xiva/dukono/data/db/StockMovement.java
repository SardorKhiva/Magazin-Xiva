package sardor.xiva.dukono.data.db;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "stock_movements",
    foreignKeys = @ForeignKey(
        entity = Product.class,
        parentColumns = "id",
        childColumns = "product_id",
        onDelete = ForeignKey.CASCADE
    ),
    indices = @Index(value = "product_id")
)
public class StockMovement {
    public static final String REASON_INITIAL = "initial";
    public static final String REASON_IN = "in";
    public static final String REASON_OUT = "out";
    public static final String REASON_ADJUST = "adjust";
    public static final String REASON_DEFECT = "defect";
    public static final String REASON_OTHER = "other";

    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "product_id")
    public long productId;

    public double delta;

    @NonNull
    public String reason = REASON_OTHER;

    public String note;

    @ColumnInfo(name = "moved_at")
    public long movedAt;
}
