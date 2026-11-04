package sardor.xiva.dukono.ui;

// Kerakli Android va Java kutubxonalarini import qilamiz
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.List;

import sardor.xiva.dukono.R;
import sardor.xiva.dukono.data.Repo;
import sardor.xiva.dukono.data.db.ProductListItem;
import sardor.xiva.dukono.databinding.ActivityProductListBinding;
import sardor.xiva.dukono.util.AppExecutors;
import sardor.xiva.dukono.util.AppPrefs;
import sardor.xiva.dukono.util.SimpleWatcher;
import sardor.xiva.dukono.util.UzText;

/**
 * Tovarlar ro'yxati oynasi (Activity).
 * Bu oynada barcha tovarlar ko'rsatiladi, qidiriladi, tahrirlanadi va o'chiriladi.
 */
public class ProductListActivity extends AppCompatActivity implements ProductAdapter.Listener {

    // XML maket elementlariga to'g'ridan-to mezon bilan bog'lanish uchun ViewBinding
    private ActivityProductListBinding b;

    // RecyclerView ro'yxat ma'lumotlarini chizib beruvchi adapter
    private ProductAdapter adapter;

    // Bazadan yuklab olingan barcha tovarlar ro'yxati keshi
    private final List<ProductListItem> allProducts = new ArrayList<>();

    // Boshqa ekrandan (tahrirlash yoki tafsilotlar) qaytganda natijani tutib olish uchun launcher
    private ActivityResultLauncher<Intent> activityLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Oynaning bazaviy yaratilish sikli metodini chaqiramiz
        super.onCreate(savedInstanceState);

        // Layout XML faylini binar ko'rinishdan obyektga o'giramiz (Inflate qilamiz)
        b = ActivityProductListBinding.inflate(getLayoutInflater());

        // Ekran ko'rinishi sifatida ViewBinding ildiz elementini o'rnatamiz
        setContentView(b.getRoot());

        // Toolbar dagi orqaga qaytish tugmasi bosilganda oynani yopamiz
        b.toolbar.setNavigationOnClickListener(v -> finish());

        // ActivityResultLauncher yaratamiz: tahrirlash yoki ko'rishdan qaytilganda ro'yxatni yangilash uchun
        activityLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    // Agar biror ma'lumot o'zgargan bo'lsa (RESULT_OK), ma'lumotlarni qayta yuklaymiz
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        loadProducts();
                    }
                });

        // ProductAdapter obyektini ushbu klass tinglovchi (listener) sifatida yaratamiz
        adapter = new ProductAdapter(this);

        // RecyclerView uchun vertikal chiziqli tartib o'rnatamiz
        b.list.setLayoutManager(new LinearLayoutManager(this));

        // RecyclerView ga adapterni biriktiramiz
        b.list.setAdapter(adapter);

        // Animatsiyalar harakatini bekor qilamiz (tezkor va silliq yangilanish uchun)
        b.list.setItemAnimator(null);

        // Qidiruv inputiga matn o'zgarganda ishlaydigan watcher (tinglovchi) qo'shamiz
        b.searchInput.addTextChangedListener(new SimpleWatcher() {
            @Override
            protected void onChanged(String text) {
                // Qidiruv matni o'zgarganda ro'yxatni filterlaymiz
                filterList(text);
            }
        });

        // Yangi tovar qo'shish suzuvchi (FAB) tugmasi bosilganda tahrirlash oynasini ochamiz
        b.fab.setOnClickListener(v -> openEdit(0, null, null));

        // Bo'sh holatdagi "Yangi tovar qo'shish" tugmasi bosilganda tahrirlash oynasini ochamiz
        b.addFromEmpty.setOnClickListener(v -> openEdit(0, currentQuery(), null));

        // Dastlabki ma'lumotlarni bazadan yuklaymiz
        loadProducts();
    }

    @Override
    protected void onResume() {
        // Ekran faollashganda bazaviy onResume chaqiruvi
        super.onResume();

        // Sozlamalardagi kam qoldiq va yaroqlilik kunlarini adapterga o'rnatamiz
        adapter.setConfig(AppPrefs.getLowStockLimit(), AppPrefs.getExpiryWarningDays());

        // Ma'lumotlarni qayta yangilaymiz
        loadProducts();
    }

    /**
     * Hozirgi qidiruv maydonidagi matnni qaytaruvchi yordamchi metod.
     */
    private String currentQuery() {
        // Input matni null bo'lsa bo'sh satr, aks holda matnni olamiz
        return b.searchInput.getText() == null ? "" : b.searchInput.getText().toString();
    }

    /**
     * Bazadan barcha tovarlarni fon oqimida (IO thread) yuklash va UI da ko'rsatish.
     */
    private void loadProducts() {
        // Bazaga murajaat qilish fon oqimida bajariladi
        AppExecutors.io(() -> {
            // Repo orqali barcha tovarlarni bazadan olamiz
            List<ProductListItem> items = Repo.get().getAllProducts();

            // Natijani asosiy (UI) oqimda ekranga chiqaramiz
            AppExecutors.main(() -> {
                // Agar oyna yopilgan yoki yo'q qilingan bo'lsa ishni to'xtatamiz
                if (isFinishing() || isDestroyed()) return;

                // Keshlangan ro'yxatni tozalaymiz
                allProducts.clear();

                // Yangi yuklangan tovarlarni keshga qo'shamiz
                allProducts.addAll(items);

                // Joriy qidiruv bo'yicha filterlab ro'yxatni chizamiz
                filterList(currentQuery());
            });
        });
    }

    /**
     * Kiritilgan matn bo'yicha tovarlar ro'yxatini lokal filterlash.
     */
    private void filterList(String query) {
        // Kiritilgan matnni lotin/kirill va kichik harflarga normallashtiramiz
        String normalized = UzText.normalize(query);

        // Filterlangan tovarlar uchun vaqtinchalik ro'yxat
        List<ProductListItem> filtered = new ArrayList<>();

        // Agar qidiruv satri bo'sh bo'lsa, barcha tovarlarni ko'rsatamiz
        if (normalized.isEmpty()) {
            filtered.addAll(allProducts);
        } else {
            // Aks holda har bir tovar nomi va kodini mosligini tekshiramiz
            String[] words = normalized.split(" ");
            for (ProductListItem item : allProducts) {
                // Tovar nomi va kodini normallashtirib birlashtiramiz
                String target = UzText.normalize((item.name == null ? "" : item.name) + " " + (item.code == null ? "" : item.code));

                // Barcha so'zlar mos kelishini tekshiramiz (AND sharti)
                boolean matches = true;
                for (String word : words) {
                    if (word.isEmpty()) continue;
                    if (!target.contains(word)) {
                        matches = false;
                        break;
                    }
                }
                // Agar mos kelsa filterlangan ro'yxatga qo'shamiz
                if (matches) {
                    filtered.add(item);
                }
            }
        }

        // Filterlangan natijani adapterga taqdim etamiz
        adapter.submitList(filtered);

        // Jami tovarlar sonini tekst shaklida ko'rsatamiz
        b.totalText.setText(getString(R.string.total_count, filtered.size()));

        // Agar ro'yxat bo'sh bo'lsa bo'sh holat panelini ko'rsatamiz, aks holda ro'yxatni ko'rsatamiz
        boolean empty = filtered.isEmpty();
        b.list.setVisibility(empty ? View.GONE : View.VISIBLE);
        b.emptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
    }

    /**
     * Tovar qo'shish yoki tahrirlash oynasini ochuvchi yordamchi metod.
     */
    private void openEdit(long id, String prefillName, String prefillCode) {
        // ProductEditActivity ga intent yaratamiz
        Intent intent = new Intent(this, ProductEditActivity.class);

        // Agar ID mavjud bo'lsa intent ga qo'shamiz
        if (id > 0) intent.putExtra(ProductEditActivity.EXTRA_ID, id);

        // Dastlabki nom yoki kod mavjud bo'lsa intent ga qo'shamiz
        if (prefillName != null) intent.putExtra(ProductEditActivity.EXTRA_NAME, prefillName);
        if (prefillCode != null) intent.putExtra(ProductEditActivity.EXTRA_CODE, prefillCode);

        // Natijani kutuvchi launcher orqali tahrirlash oynasini ochamiz
        activityLauncher.launch(intent);
    }

    /**
     * Adapterdagi tovar bosilganda ishlaydigan metod (ProductAdapter.Listener).
     * Tovarning batafsil ma'lumotlar oynasini ochadi.
     */
    @Override
    public void onOpen(ProductListItem item) {
        // ProductDetailActivity ga o'tish uchun intent yaratamiz
        Intent intent = new Intent(this, ProductDetailActivity.class);

        // Tanlangan tovar ID sini berib yuboramiz
        intent.putExtra(ProductDetailActivity.EXTRA_ID, item.id);

        // Natijani kutuvchi launcher orqali ko'rish oynasini ochamiz
        activityLauncher.launch(intent);
    }

    /**
     * Adapterdagi tahrirlash tugmasi bosilganda ishlaydigan metod (ProductAdapter.Listener).
     * Tovarni tahrirlash oynasini ochadi.
     */
    @Override
    public void onEdit(ProductListItem item) {
        // Tanlangan tovar ID si bilan tahrirlash oynasini ochamiz
        openEdit(item.id, null, null);
    }

    /**
     * Tovarni o'chirishni tasdiqlash va bazadan o'chirish dialogini ko'rsatish.
     */
    public void confirmDelete(ProductListItem item) {
        // Agar tovar obyekti yo'q bo'lsa ishni to'xtatamiz
        if (item == null) return;

        // O'chirishni tasdiqlash uchun ogohlantiruvchi dialog oynasini chiqaramiz
        Dialogs.confirm(this, R.string.delete,
                getString(R.string.delete_confirm_message, item.name),
                R.string.delete,
                // Tasdiqlanganda fon oqimida bazadan o'chiramiz
                () -> AppExecutors.io(() -> {
                    // Repo orqali tovar id sini bazadan o'chiramiz
                    Repo.get().deleteProduct(item.id);

                    // Natijani UI oqimida bildiramiz va ro'yxatni yangilaymiz
                    AppExecutors.main(() -> {
                        // Xabar chiqaramiz
                        Toast.makeText(this, R.string.deleted, Toast.LENGTH_SHORT).show();

                        // Tovarlar ro'yxatini qayta yuklaymiz
                        loadProducts();
                    });
                }));
    }
}
