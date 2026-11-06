# Texnik Topshiriq (SPECIFICATION) — Magazin Xiva (Dukon)

## 1. Loyihaning Maqsadi
"Magazin Xiva" (Dukon) — chakana savdo do'konlari uchun tovarlar hisobini yuritish, qoldiqlar, narxlar va yaroqlilik muddatlarini nazorat qilish, shuningdek, ma'lumotlarni oflayn rejimda havfsiz saqlash va CSV formatida zaxiralash uchun mo'ljallangan mobil Android ilovasi.

---

## 2. Hozirgi va Reliz Qilingan Funksiyalar (Version 0.1.0-mvp / `feature` branch)

### 2.1. Xavfsizlik va Kirish Nazorati
- **Dinamik PIN-kod**: Har kuni avtomatik ravishda `Joriy Yil + Joriy Kun` (masalan: 2026 + 6 = 2032) formulasi bo'yicha hisoblanuvchi va doimiy zaxira/tiklash kaliti sifatida ishlovchi himoya tizimi.
- **Statik PIN-kod**: Foydalanuvchi tomonidan o'rnatiladigan 4 xonali shaxsiy PIN-kod (PBKDF2 bilan xeshlangan). 5 marta xato kiritilsa 30 soniyaga bloklanadi.
- **Avtoqulf (Auto-lock)**: Ilova fonda turganda belgilangan vaqt o'tgach (Darhol, 1 daqiqa, 5 daqiqa) avtomatik qulflanish.

### 2.2. Qidiruv va Bosh Ekran
- Real-vaqt rejimida tovar nomi yoki kodi bo'yicha qidirish.
- **Lotin va Kirill alifbolari neytralligi**: Qidiruv matni lotincha yoki kirillcha kiritilishidan qat'i nazar tovarlarni teng topish (`UzText`).
- Oxirgi 20 ta qidiruv tarixi chip guruhlari shaklida.
- Jami tovarlar soni ko'rsatkichi.

### 2.3. Barcha Tovarlar Ro'yxati
- Barcha tovarlarning alifbo bo'yicha tartiblangan to'liq ro'yxati va lokal filtratsiyasi.
- Har bir tovar uchun kichik rasm preview (52x52dp), nomi, kodi, narxi, qoldig'i, o'lchov birligi.
- **Kam qoldiq chegarasi**: Qoldiq kamaysa ro'yxat qatorining foni rang bilan ajratilishi.
- **Yaroqlilik muddati statusi**: Muddati o'tgan yoki tugashiga oz qolgan tovarlarni maxsus nishon (chip) bilan ko'rsatish.

### 2.4. Tovar Tafsilotlari va Harakatlar Tarixi
- Tovarning moslashuvchan rasmi (ekran enining 70% o'lchamida).
- Tovarga tegishli to'liq atributlar (Narxi, qoldig'i, o'lchov birligi, kategoriya, ishlab chiqaruvchi, davlat, yaroqlilik muddati, izoh).
- **Qoldiq harakatlari tarixi (Stock Movements)**: Har bir tovar bo'yicha kirim, chiqim, tuzatish, brak va boshlang'ich qoldiq o'zgarishlari xronologiyasi.

### 2.5. Tovar Qo'shish va Tahrirlash
- Tovar nomi, unikal shtrix/tovar kodi tekshiruvi.
- O'lchov birligi va kasr ruxsatiga mos ravishda miqdor va narx kiritish.
- Qoldiq o'zgarganda majburiy sabab kodi va izoh so'rash dialogi.
- **Rasm biriktirish**: Galeriyadan rasm tanlash, rasmni avtomatik **ekran enining 70% hajmigacha siqish** (JPEG 80% sifat) va ilova papkasida saqlash/o'chirish.
- MaterialDatePicker orqali yaroqlilik muddatini tanlash.

### 2.6. Ma'lumotnomalar (References)
- O'lchov birliklarini yaratish, tahrirlash, kasr ruxsatini sozlash va o'chirish (ishlatilayotgan bo'lsa taqiqlash).
- Kategoriyalarni boshqarish va o'chirish.

### 2.7. CSV Eksport va Import
- Barcha tovarlar ma'lumotlarini UTF-8 BOM shaklida CSV faylga eksport qilish (Excel bilan to'liq mos).
- CSV faylidan tovarlarni import qilib bazaga yuklash (birlik va kategoriyalarni avtomatik biriktirish yoki yangi yaratish).

### 2.8. Sozlamalar
- Tilni o'zgartirish (O'zbekcha lotin / O'zbekcha kirill).
- Mavzu sozlamasi (Tizim, Kunduzgi, Tungi).
- Kam qoldiq chegarasi va muddat ogohlantirish kunlarini belgilash.
- PIN-kodni o'zgartirish.

---

## 3. Ishlab Chiqilayotgan va Yaqin Rivojlanish Bosqichlari (Roadmap)

> [!NOTE]
> Barcha yangi imkoniyatlar bosqichma-bosqich (step-by-step), har bir funksiya alohida kiritilib, sinovdan o'tkazilib reliz qilinadi. Birdaniga katta o'zgarishlar qilinmaydi.

### 1-bosqich: Kamera Orqali Rasmga Olish (Camera Integration)
- Dasturning o'zida kamera orqali tovar rasmini olish (Camera capture).
- Olingan rasmni avtomatik ekran enining 70% o'lchamida siqib, tovarga biriktirish.

### 2-bosqich: Sotuv va Kirim-Chiqim (POS / Sales Tracking)
- Sotilgan tovarlarni real vaqt rejimida hisobga olish (Kassa / Sotuv bo'limi).
- Sotuv amalga oshganda tovar qoldig'ining avtomatik kamayishi va harakatlar tarixiga yozilishi.

### 3-bosqich: Hisobotlar (Analytics & Reports)
- Kunlik, haftalik va oylik sotuv hamda moliyaviy hisobotlar.

### 4-bosqich: PDF Hisobotlar (PDF Export)
- Qarzlar va foydalar bo'yicha hisobotlarni PDF formatida shakllantirish va saqlash.

### Uzoq muddatli (Server sinxronizatsiyasidan keyingi) bosqich:
- Backend/Server sinxronizatsiyasi.
- Ko'p foydalanuvchili kirish va rollar (Admin, Sotuvchi).

---

## 4. Nofunksional Talablar
1. **Oflayn Ishlash**: Ilova ishlashi uchun Internet talab etilmaydi (100% Local database).
2. **Optimizatsiya va Resurslar**: Rasmlar ekran enining 70%idan oshmagan holda siqiladi; xotirada og'ir va katta fayllar saqlanmaydi.
3. **Sezgirlik (Performance)**: Ma'lumotlar bazasi va fayl amallari faqat fon oqimida (`AppExecutors.io`) bajariladi, UI ipi bloklanmaydi.
4. **Xavfsizlik**: Shaxsiy PIN-kod PBKDF2WithHmacSHA1 alogoritmi (10,000 iteratsiya) bilan xeshlangan.

---

## 5. Foydalanuvchi Rollari
- **Yagona Rol (Do'kon egasi / Sotuvchi)**: Hozirgi bosqichda ilovaning barcha funksiyalaridan foydalanish huquqiga ega yagona foydalanuvchi rejimi.
