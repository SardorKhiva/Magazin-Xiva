package sardor.xiva.dukono.data.db;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(
    entities = {Product.class, Unit.class, Category.class, StockMovement.class, SearchHistory.class},
    version = 1,
    exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    public abstract ProductDao productDao();

    public abstract UnitDao unitDao();

    public abstract CategoryDao categoryDao();

    public abstract StockMovementDao stockMovementDao();

    public abstract SearchHistoryDao searchHistoryDao();

    public static AppDatabase get(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "dukon.db")
                            .addCallback(SEED)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    /** Birinchi yaratishda standart o'lchov birliklari. */
    private static final Callback SEED = new Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            db.execSQL("INSERT INTO units (name, allow_fraction) VALUES ('dona', 0)");
            db.execSQL("INSERT INTO units (name, allow_fraction) VALUES ('kg', 1)");
            db.execSQL("INSERT INTO units (name, allow_fraction) VALUES ('g', 1)");
            db.execSQL("INSERT INTO units (name, allow_fraction) VALUES ('litr', 1)");
            db.execSQL("INSERT INTO units (name, allow_fraction) VALUES ('metr', 1)");
            db.execSQL("INSERT INTO units (name, allow_fraction) VALUES ('quti', 0)");
            db.execSQL("INSERT INTO units (name, allow_fraction) VALUES ('qadoq', 0)");
        }
    };
}
