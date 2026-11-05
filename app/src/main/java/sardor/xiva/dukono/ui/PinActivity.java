package sardor.xiva.dukono.ui;

// Kerakli Android va Java kutubxonalarini import qilamiz
import android.content.Intent;
import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

import sardor.xiva.dukono.R;
import sardor.xiva.dukono.databinding.ActivityPinBinding;
import sardor.xiva.dukono.util.AppLock;
import sardor.xiva.dukono.util.Ui;

/**
 * Dinamik PIN-kod bilan dasturni himoyalash oynasi (PinActivity).
 * Ilova ishga tushirilganda eng birinchi ochiluvchi xavfsizlik aktivligi.
 * Har kuni avtomatik dinamik ravishda PIN-kod yangilanib boradi: (Joriy Yil + Joriy Kun).
 */
public class PinActivity extends AppCompatActivity {

    // ViewBinding obyekti: XML elementlar bilan xavfsiz va to'g'ridan-to'g'ri ishlash uchun
    private ActivityPinBinding b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Otasi bo'lgan AppCompatActivity dagi yaratilish metodini chaqiramiz
        super.onCreate(savedInstanceState);

        // Layout XML faylini binar obyekt shakliga o'giramiz (Inflate)
        b = ActivityPinBinding.inflate(getLayoutInflater());

        // Oynaning asosiy ko'rinishi sifatida ViewBinding ildiz elementini o'rnatamiz
        setContentView(b.getRoot());

        // Orqaga qaytish tugmasi (Back Button) bosilganda dasturni to'liq yopish xatti-harakati
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // PinActivity ni chetlab o'tmaslik uchun butun ilovani yopamiz
                finishAffinity();
            }
        });

        // "Kirish" tugmasi bosilganda PIN-kodni tekshirish funksiyasini chaqiramiz
        b.submitButton.setOnClickListener(v -> checkPin());

        // Klaviatura yuborish (IME Done/Search) tugmasi bosilganda ham PIN-kodni tekshiramiz
        b.pinInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_GO) {
                checkPin();
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        // Ekran qayta faollashganda otasi metodini chaqiramiz
        super.onResume();

        // Foydalanuvchiga qulaylik yaratish uchun klaviaturani avtomatik ravishda ko'rsatamiz
        b.pinInput.postDelayed(() -> {
            if (!isFinishing() && !isDestroyed()) {
                Ui.showKeyboard(b.pinInput);
            }
        }, 250);
    }

    /**
     * Bugungi kunga mos keladigan dinamik PIN-kodni hisoblovchi metod.
     * Formula: Joriy Yil + Joriy Kun (Calendar.getInstance() yordamida).
     * Masalan: 2026 + 5 = 2031
     *
     * @return Bugungi kunga mos keluvchi dinamik PIN-kod matni
     */
    private String getExpectedDynamicPin() {
        // Kalendar obyektining joriy vaqt nusxasini olamiz
        Calendar calendar = Calendar.getInstance();

        // Joriy yilni olamiz (masalan: 2026)
        int currentYear = calendar.get(Calendar.YEAR);

        // Joriy oyning kunini olamiz (masalan: 5)
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);

        // Dinamik PIN-kodni hisoblaymiz: Joriy Yil + Joriy Kun
        int dynamicPinValue = currentYear + currentDay;

        // Hisoblangan raqamli qiymatni matn (String) shakliga o'girib qaytaramiz
        return String.valueOf(dynamicPinValue);
    }

    /**
     * Foydalanuvchi kiritgan PIN-kodni hisoblangan dinamik PIN-kod bilan solishtirib tekshirish.
     */
    private void checkPin() {
        // Input EditText maydonidan foydalanuvchi kiritgan matnni olamiz
        String inputPin = b.pinInput.getText() == null ? "" : b.pinInput.getText().toString().trim();

        // Agar kiritish maydoni bo'sh bo'lsa, ogohlantirish xabarini chiqaramiz
        if (inputPin.isEmpty()) {
            Toast.makeText(this, R.string.dynamic_pin_empty, Toast.LENGTH_SHORT).show();
            return;
        }

        // Foydalanuvchi kiritgan PIN-kod haqiqiy dinamik PIN-kod yoki saqlangan PIN-kodga mosligini tekshiramiz
        if (sardor.xiva.dukono.util.PinManager.verify(inputPin)) {
            // PIN-kod to'g'ri bo'lsa, dasturni qulfdan chiqaramiz
            AppLock.unlock();

            // Asosiy ekranga (MainActivity) o'tish uchun intent yaratamiz
            Intent intent = new Intent(this, MainActivity.class);

            // Intent orqali MainActivity ni ochamiz
            startActivity(intent);

            // PinActivity oynasini yopamiz (orqaga qaytib bo'lmasligi uchun)
            finish();
        } else {
            // PIN-kod noto'g'ri bo'lsa, xatolik bildirishnomasini (Toast) chiqaramiz
            Toast.makeText(this, R.string.dynamic_pin_wrong, Toast.LENGTH_SHORT).show();

            // Noto'g'ri kiritilgan input maydonini tozalaymiz
            b.pinInput.setText("");
        }
    }
}
