# 🛒 Magazin Xiva (Dukon) — Android Tovar Hisobi va Boshqaruv Dasturi

[![Android Platform](https://img.shields.io/badge/Platform-Android%207.0%2B%20%28API%2024%2B%29-green.svg)](https://developer.android.com)
[![Language](https://img.shields.io/badge/Language-Java%2011-orange.svg)](https://www.oracle.com/java/)
[![Architecture](https://img.shields.io/badge/Architecture-MVVM%20%2B%20Repository-blue.svg)](https://developer.android.com/topic/architecture)
[![Database](https://img.shields.io/badge/Database-Room%20SQLite-brightgreen.svg)](https://developer.android.com/training/data-storage/room)
[![UI](https://img.shields.io/badge/Design-Material%203-purple.svg)](https://m3.material.io)
[![Offline](https://img.shields.io/badge/Network-100%25%20Offline-success.svg)](#)

**"Magazin Xiva" (Dukon)** — chakana savdo do'konlari uchun tovarlar hisobini yuritish, narxlarni belgilash, qoldiqlar harakatini kuzatish, yaroqlilik muddatlarini nazorat qilish hamda ma'lumotlarni 100% oflayn rejimida xavfsiz boshqarish uchun mo'ljallangan zamonaviy Android ilovasi.

---

## ✨ Asosiy Imkoniyatlar (Key Features)

### 🔒 1. Xavfsizlik va Kirish Nazorati
- **Dinamik PIN-kod**: Har kuni avtomatik ravishda `Joriy Yil + Joriy Kun` (masalan: $2026 + 6 = 2032$) formulasi bo'yicha dinamik tarzda o'zgaruvchi va har doim zaxira/tiklash kaliti bo'lib xizmat qiluvchi xavfsizlik tizimi.
- **Statik PIN-kod**: Foydalanuvchi o'rnatadigan 4 xonali shaxsiy PIN-kod (PBKDF2 kriptografik xeshi bilan saqlanadi). 5 marta xato kiritilsa 30 soniyaga avtomatik bloklanadi.
- **Avtoqulf (Auto-lock)**: Ilova fonda turganda belgilangan vaqt (Darhol, 1 daqiqa, 5 daqiqa) o'tgach avtomatik qulflanish.

### 🔍 2. Qidiruv va Bosh Ekran
- Tovar nomi yoki kodi bo'yicha real-vaqt rejimida tezkor qidiruv.
- **Lotin va Kirill alifbolari neytralligi**: Qidiruv matni lotincha yoki kirillcha kiritilishidan qat'i nazar tovarlarni teng darajada aniq topish (`UzText`).
- Oxirgi qidiruvlar tarixi (chip guruhlari, tozalash imkoniyati bilan).
- Jami tovarlar soni ko'rsatkichi.

### 📦 3. Tovarlar Ro'yxati va Optimizatsiyalangan Rasmlar
- Barcha tovarlarning alifbo bo'yicha tartiblangan to'liq ro'yxati hamda tezkor lokal filtratsiyasi.
- **Kamera va Galeriya Integratsiyasi**: Dasturning o'zida kamera orqali tovar rasmini rasmga olish yoki galeriyadan tanlash.
- **Rasm Optimizatsiyasi**: Rasmlar avtomatik ravishda **ekran enining 70% o'lchamigacha** va **maksimal 500 KB** hajmdan oshmagan holda JPEG formatida siqiladi.
- **Fayllar Joylashuvi (`Pictures/Dukono/`)**: Rasmlar ochiq papkada saqlanadi — foydalanuvchi Galeriya yoki telefonning "Fayllar" dasturidan bemalol ko'rishi yoki o'chirishi mumkin.
- **Kam Qoldiq va Muddat Nazorati**: Kam qolgan tovarlar rangi ajratiladi hamda yaroqlilik muddati tugashiga oz qolgan tovarlar maxsus chip bilan ko'rsatiladi.

### 📊 4. Tovar Tafsilotlari va Qoldiq Harakatlari Tarixi
- Tovarning moslashuvchan rasmi (ekran enining 70% o'lchamida), narxi, qoldig'i, o'lchov birligi, kategoriyasi, ishlab chiqaruvchi, davlat va izohlar.
- **Stock Movement History**: Har bir tovar bo'yicha barcha qoldiq o'zgarishlari (Kirim, Chiqim, Tuzatish, Yaroqsiz, Boshlang'ich) xronologiyasi va rangli ko'rsatgichlari.

### 📁 5. CSV Eksport va Import
- **Eksport**: Barcha tovarlar ma'lumotlarini UTF-8 BOM shaklida CSV faylga saqlash (Microsoft Excel va Google Sheets bilan 100% mos).
- **Import**: CSV faylidan tovarlarni import qilib bazaga yuklash (o'lchov birliklari va kategoriyalarni avtomatik biriktirish/yaratish).

### ⚙️ 6. Sozlamalar va Ma'lumotnomalar
- O'zbek tili sozlamasi: **Lotin / Kirill**.
- Mavzu: **Tizim / Kunduzgi / Tungi**.
- O'lchov birliklari (kasr ruxsati sozlamasi bilan) va Kategoriyalarni boshqarish.
- Kam qoldiq chegarasi hamda yaroqlilik kunlarini sozlash.
- **Barcha Rasmlarni O'chirish**: Tovarlar ma'lumotlariga (nomi, narxi, qoldig'i va h.k.) tegmagan holda, faqat rasm fayllari va keshlarini tozalash tugmasi.

---

## 🛠 Texnologiyalar va Arxitektura (Tech Stack & Architecture)

- **Architecture Pattern**: MVVM / Repository Pattern
- **Programming Language**: Java 11
- **UI Components**: Material Design 3, ViewBinding, AndroidX AppCompat
- **Database**: Room SQLite Persistence Library (v2.6.1)
- **Asynchronous Execution**: `AppExecutors` (Custom SingleThread IO Executor & Main Thread Handler)
- **Security**: PBKDF2WithHmacSHA1 Hashing, Custom Dynamic PIN algorithm
- **Storage Access**: Storage Access Framework (SAF) & Activity Result Contracts (`CreateDocument`, `OpenDocument`, `GetContent`, `TakePicture`)

---

## 📁 Papkalar Tuzilishi (Project Structure)

```
Magazin Xiva/
├── app/
│   ├── src/main/
│   │   ├── java/sardor/xiva/dukono/
│   │   │   ├── DukonApp.java            # Application sinfi
│   │   │   ├── data/                    # Room Database, DAOs & Repo
│   │   │   │   ├── Repo.java            # Central Repository
│   │   │   │   └── db/                  # AppDatabase, Product, Category, Unit, StockMovement...
│   │   │   ├── ui/                      # ViewBinding Activity va Adapterlar
│   │   │   │   ├── MainActivity.java    # Bosh ekran & Qidiruv
│   │   │   │   ├── PinActivity.java     # Dinamik PIN ekrani
│   │   │   │   ├── ProductListActivity.java # Tovarlar ro'yxati
│   │   │   │   ├── ProductDetailActivity.java # Tovar tafsilotlari
│   │   │   │   ├── ProductEditActivity.java   # Tovar tahrirlash/Kamera
│   │   │   │   ├── ReferenceActivity.java     # Birlik/Kategoriyalar
│   │   │   │   └── SettingsActivity.java      # Sozlamalar/CSV/Tozalash
│   │   │   └── util/                    # Utility sinflari
│   │   │       ├── ImageUtil.java       # Rasm siqish (70% en, max 500KB)
│   │   │       ├── CsvHelper.java       # CSV Export/Import (UTF-8 BOM)
│   │   │       ├── PinManager.java      # PBKDF2 va Dinamik PIN
│   │   │       ├── AppLock.java         # Avtoqulf lifecycle
│   │   │       ├── UzText.java          # Lotin/Kirill transliteratsiyasi va qidiruv
│   │   │       └── Fmt.java             # Pul/Sana/Miqdor formatlash
│   │   └── res/                         # XML Layoutlar, Menyu, Vektorlar va Resurslar
│   └── build.gradle                     # App Gradle sozlamalari
├── SPECIFICATION.md                     # Texnik Topshiriq hujjati
├── ARCHITECTURE.md                      # Arxitektura hujjati
├── build.gradle                         # Root Gradle sozlamalari
└── README.md                            # GitHub hujjati
```

---

## 🚀 Ishga Tushirish (How to Build & Run)

1. **Loyiha kodini nusxalash (Clone)**:
   ```bash
   git clone https://github.com/username/magazin-xiva.git
   cd magazin-xiva
   ```

2. **Android Studio da ochish**:
   - Android Studio (Ladybug / Koala / 2024.1+ / 2026+) dasturini oching.
   - `Open an existing project` bosib, ushbu papkani tanlang.

3. **Gradle va Proyektni yig'ish**:
   - Gradle avtomatik ravishda barcha bog'liqliklarni yuklab oladi.
   - Proyektni bino qilish uchun terminalda:
     ```bash
     ./gradlew assembleDebug
     ```

4. **Qurilmada yuritish**:
   - USB orqali ulangan Android qurilmada yoki Emulator da **Run ('app')** (Shift + F10) tugmasini bosing.

---

## 📄 Litsenziya (License)

Ushbu loyiha shaxsiy va tijorat maqsadda foydalanish uchun ochiq ishlab chiqilmoqda.

---

Designed with ❤️ for **Magazin Xiva**.
