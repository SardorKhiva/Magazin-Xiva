package sardor.xiva.dukono.data.db;

import androidx.room.ColumnInfo;

/** Qidiruv natijasining bir qatori (faqat ro'yxatda ko'rsatiladigan maydonlar). */
public class ProductListItem {
    public long id;
    public String name;
    public String code;
    public long price;
    public double balance;

    @ColumnInfo(name = "initial_qty")
    public double initialQty;

    @ColumnInfo(name = "unit_name")
    public String unitName;

    @ColumnInfo(name = "allow_fraction")
    public boolean allowFraction;

    @ColumnInfo(name = "expiry_date")
    public Long expiryDate;
}
