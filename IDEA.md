# Dukon: Android magazin tovarlari hisobi (IDEA / HANDOFF)

Bu hujjat loyihani boshqa AI agentga (Hermes) topshirish uchun yozilgan. Ichida: loyiha nima haqida, kelishilgan barcha qarorlar, nima tayyor, nima qolgan va aniq reja bor. Foydalanuvchi o'zbek tilida (lotin) gaplashadi, javoblarni shunday bering. Kod identifikatorlari inglizcha.

## 1. What's this project about?

Magazin egasi uchun oflayn Android dasturi: tovarlarni (nomi, kodi, narxi, adadi, qoldig'i va boshqalar) tez qidirish, ko'rish, qo'shish, tahrirlash, o'chirish.

Asosiy g'oya: dastur ochilishi bilan qidiruv oynasi avtomatik ochiladi, klaviatura chiqadi, har bir harfda natija real vaqtda yangilanadi. Har natija qatorida ruchka (✏️) bor, bosilsa tahrirlash oynasi ochiladi. Qatorning o'zi bosilsa tovarning to'liq ko'rinishi (faqat o'qish) ochiladi.

Muhim: lotin va kirill harflari aralash qidirilsa ham topilishi kerak ("sut" va "сут" bir xil natija beradi).

## 2. Texnik asos

| | |
|---|---|
| Paket nomi | `sardor.xiva.dukono` |
| Dastur nomi | "Dukon" (kirillda "Дукон", ishchi nom) |
| Til / UI | Java, XML, Material 3 (Material Components), ViewBinding |
| Baza | SQLite, Room 2.6.1 (Java, annotationProcessor) |
| minSdk / targetSdk / compileSdk | 24 / 34 / 34 |
| Yo'nalish | Faqat portrait, faqat telefon |
| Ruxsatlar | Hech qanday. Internet yo'q. `allowBackup=false` |
| Tarqatish | Faqat APK |
| Foydalanuvchi | Bitta, rollar yo'q |
| Mavzu | Kunduzgi / tungi / tizim (`AppCompatDelegate.setDefaultNightMode`) |
| Til | O'zbek lotin (`values/`) va o'zbek kirill (`values-b+uz+Cyrl/`), `AppCompatDelegate.setApplicationLocales` bilan almashadi ("uz-Latn" / "uz-Cyrl") |
| Skrinshot himoyasi | Kerak emas (FLAG_SECURE ishlatilmaydi) |
| Qatlamlar | UI -> ViewModel (faqat qidiruv uchun) -> Repo -> DAO -> Room |

Kutubxonalar: appcompat 1.7.0, material 1.12.0, recyclerview 1.3.2, lifecycle-viewmodel/livedata 2.6.2, room 2.6.1, junit 4.13.2 (test). AGP 8.5.2, Gradle 8.7, JDK 17.

## 3. Bosqichlar

1. **1-bosqich (MVP):** PIN, qidiruv, tovar qo'shish/ko'rish/tahrirlash/o'chirish, o'lchov birliklari va kategoriyalar, sozlamalar, qoldiq harakatlari (sabab).
2. **2-bosqich:** Excel (.xlsx) import/eksport, qo'lda va avtomatik kunlik backup, backup'dan tiklash, "PIN'ni unutdim" tiklash oqimi.
3. **3-bosqich (ixtiyoriy):** kamera orqali shtrix-kod skaner (internetsiz), tovar rasmi.
4. **Kelajak:** savdo, chek, kirim-chiqim hujjatlari, hisobotlar. Shuning uchun `stock_movements` jadvali hozirdan bor.

## 4. Joriy holat (muhim)

**Tayyor:**
- `settings.gradle`, `build.gradle`, `gradle.properties`, `.gitignore`
- Gradle wrapper (`gradlew`, `gradlew.bat`, `gradle-wrapper.jar`, `gradle-wrapper.properties`)
- `app/build.gradle`, `app/proguard-rules.pro`
- `app/src/main/AndroidManifest.xml` (barcha activity e'lon qilingan, ular hali yozilmagan)
- `app/src/main/java/sardor/xiva/dukono/util/UzText.java`: yozilgan, lekin **SINALMAGAN** (sandboxda `javac` yo'q edi). Birinchi vazifa: lokalda kompilyatsiya qilib, 10-bo'limdagi test holatlarini o'tkazish.

**Yozilmagan (hammasi shu ro'yxatdagi):** `DukonApp`, `data/db/*` (entity, DAO, AppDatabase), `data/Repo`, qolgan `util/*`, barcha `ui/*`, barcha `res/*` (layout, values, drawable, menu, mipmap), JUnit test. Hali Android Studio'da hech qachon kompilyatsiya qilinmagan.

## 5. Paketlar va fayllar rejasi

```
sardor.xiva.dukono
├─ DukonApp.java              Application: AppPrefs.init, tema, til, AppLock ni ro'yxatdan o'tkazish
├─ data/
│  ├─ Repo.java               yagona kirish nuqtasi (singleton), faqat fon oqimida chaqiriladi
│  └─ db/
│     ├─ AppDatabase.java     version 1, exportSchema=false, fayl "dukon.db", boshlang'ich birliklar (seed)
│     ├─ Product, Unit, Category, StockMovement, SearchHistory   (entity)
│     ├─ ProductListItem, RefItem                                (POJO natijalar)
│     └─ ProductDao, UnitDao, CategoryDao, StockMovementDao, SearchHistoryDao
├─ util/
│  ├─ UzText.java             normalizatsiya, lotin->kirill, sortKey, escapeLike  (YOZILGAN)
│  ├─ Fmt.java                pul/miqdor/sana formatlash va parse
│  ├─ AppPrefs.java           SharedPreferences ("dukon_prefs")
│  ├─ PinManager.java         PBKDF2 xesh, tekshirish, 5 xato = 30 soniya bloklash
│  ├─ AppLock.java            ActivityLifecycleCallbacks: avtoqulf
│  ├─ AppExecutors.java       io() va main() yordamchilari
│  ├─ SimpleWatcher.java      TextWatcher qisqartmasi
│  └─ Ui.java                 text(TextView), hideKeyboard
└─ ui/
   ├─ LockActivity            PIN o'rnatish / kirish
   ├─ MainActivity            qidiruv (bosh ekran)
   ├─ SearchViewModel         natijalar, tarix, jami son
   ├─ ProductAdapter          ListAdapter + DiffUtil, qator ranglari
   ├─ ProductDetailActivity   faqat o'qish + harakatlar tarixi + Tahrirlash/O'chirish
   ├─ ProductEditActivity     qo'shish/tahrirlash
   ├─ SettingsActivity
   ├─ ReferenceActivity       birliklar va kategoriyalar (TYPE_UNIT / TYPE_CATEGORY)
   ├─ RefAdapter, Dialogs (confirm yordamchisi), Labels (sabab nomlari)
```

Resurslar: layoutlar `activity_lock, activity_main, item_product, item_kv, activity_product_detail, activity_product_edit, dialog_stock_reason, dialog_ref_edit, dialog_change_pin, activity_settings, activity_reference, item_ref`; `menu_edit` (o'chirish ikonkasi); vektor ikonkalar `ic_search, ic_edit, ic_add, ic_settings, ic_delete, ic_arrow_back, ic_close`; `bg_chip`; adaptive launcher ikonka (+ eski API uchun PNG).

ViewBinding ishlatiladi. Layout id'lari camelCase (`searchInput`, `fab`, `list`, `toolbar` ...).

## 6. Ma'lumotlar modeli

- **products:** id, name, name_sort, code (UNIQUE, NOCASE), price (long, so'm), unit_id (FK RESTRICT), initial_qty (double), balance (double), category_id (FK SET NULL, `Long` nullable), manufacturer, country, expiry_date (`Long` nullable = epoch kun, UTC), note, image_path, search_text, created_at, updated_at. Indekslar: code (unique), name_sort, unit_id, category_id.
- **units:** id, name (UNIQUE NOCASE), allow_fraction. Boshlang'ich: dona(0), kg(1), g(1), litr(1), metr(1), quti(0), qadoq(0).
- **categories:** id, name (UNIQUE NOCASE), name_search (normalizatsiyalangan).
- **stock_movements:** id, product_id (FK CASCADE), delta (double, +/-), reason, note, moved_at. Sabab kodlari: `initial, in, out, adjust, defect, other`.
- **search_history:** id, query_text, query_key (normalizatsiyalangan), used_at. Oxirgi 20 tasi saqlanadi.
- **Sozlamalar (SharedPreferences):** til (`lat`/`cyr`), mavzu (0/1/2), kam qoldiq chegarasi (double, boshlang'ich 5), muddat ogohlantirish kunlari (boshlang'ich 30), avtoqulf (0 / 60000 / 300000 ms, boshlang'ich 1 daqiqa), PIN xeshi + salt, xato urinishlar soni, bloklanish vaqti.
- `expiry_date` ni `java.time` bilan emas, `Calendar` bilan hisoblang (minSdk 24). MaterialDatePicker UTC yarim tun millisekundini qaytaradi, `/ 86400000L` = epoch kun.

## 7. Funksional talablar (kelishilgan)

### 7.1. PIN
- Birinchi ishga tushishda 4 xonali PIN o'rnatiladi (ikki marta kiritib tasdiqlash). Keyin har kirishda so'raladi.
- PIN xeshlanadi (PBKDF2WithHmacSHA1, tasodifiy salt, 10000 iteratsiya).
- 5 marta xato = 30 soniya kutish.
- Avtoqulf: fondan qaytganda darhol / 1 daqiqa / 5 daqiqa (sozlamada). Konfiguratsiya o'zgarishida (recreate) qulflanmasligi kerak (`isChangingConfigurations()` + ~800 ms imtiyoz).
- PIN'ni unutganda: backup fayldan tiklash (2-bosqich). PIN backup faylga kirmaydi. 1-bosqichda "unutdim" tugmasi yo'q.

### 7.2. Qidiruv (bosh ekran)
- PIN'dan keyin avtomatik ochiladi, kursor qidiruv maydonida, klaviatura chiqadi (faqat unlock'dan keyin, `onResume` da, ~250 ms kechikish bilan).
- Har harfda real vaqtda (debounce 200 ms, fon oqimida). Bo'sh so'rov = qidiruv tarixi paneli (chip'lar) + "Jami: N ta tovar". Bo'sh so'rovda butun ro'yxat ko'rsatilmaydi (taklif, kerak bo'lsa o'zgartirish mumkin).
- Qidiruv maydonlari: nomi, kodi, kategoriya nomi, ishlab chiqaruvchi, davlat, izoh.
- Mantiq: so'zning istalgan joyidan (LIKE `%x%`). Bir nechta so'z = AND, tartibi ahamiyatsiz. Lotin/kirill aralash. Natijalar soni cheklanmaydi.
- Tartib: nom bo'yicha alifbo, **hammasi kirill tartibida** (`name_sort`). Lekin kod so'rovga aniq mos kelsa, o'sha tovar eng tepada (`ORDER BY CASE WHEN p.code = ? THEN 0 ELSE 1 END, p.name_sort, p.id`).
- Ro'yxatda nom qanday yozilgan bo'lsa shunday ko'rinadi (alifbo o'zgartirilmaydi).
- Qator: nomi, kodi, narxi (minglik ajratgich: `12 500 so'm`, NBSP bilan), adadi, qoldig'i + birlik, ruchka tugmasi. Kategoriya ro'yxatda ko'rinmaydi (faqat to'liq ko'rinishda).
- Ranglar:
  - qoldiq <= chegara: qizg'ish fon; qoldiq <= 0: kuchliroq qizil fon
  - muddat yaqin (<= N kun): **och sariq** chip + qator chetidagi chiziq
  - muddat o'tgan: **to'q sariq** chip + chiziq
  - qoldiq rangi fon, muddat rangi chip/chiziq, shuning uchun ikkalasi bir vaqtda ko'rinadi
  - ranglar `values/colors.xml` va `values-night/colors.xml` da (kunduzgi/tungi)
- Qidiruv maydonida X (tozalash). Natija topilmasa: "Tovar topilmadi" + "Yangi tovar qo'shish" tugmasi (so'rov faqat raqam bo'lsa kodga, bo'lmasa nomga oldindan to'ldiriladi). Suzuvchi (+) tugma ham bor.
- Tarixga yozish: klaviaturadagi "qidirish" tugmasi yoki natijani ochganda. Tarixni tozalash tugmasi bor.

### 7.3. Tovarning to'liq ko'rinishi
Barcha maydonlar (bo'shlari "—"), muddat qolgan kun bilan, qoldiq harakatlari (oxirgi ~30 ta: sana, sabab, +/- miqdor), tugmalar: Tahrirlash, O'chirish (tasdiq dialogi). Tahrirlashdan RESULT_OK qaytsa, bu ekran ham yopiladi (shunda foydalanuvchi qidiruvga qaytadi).

### 7.4. Tahrirlash / qo'shish
| Maydon | Qoida |
|---|---|
| Nomi | Majburiy. Takroriy bo'lsa faqat ogohlantirish (saqlash mumkin). Takror `name_sort` orqali aniqlanadi (lotin = kirill, katta-kichik farqsiz) |
| Kodi | Majburiy, foydalanuvchi o'zi kiritadi, **unikal** (saqlashga to'sqinlik qiladi), harf va raqam mumkin |
| Narxi | Butun son so'm, >= 0 |
| O'lchov birligi | Ro'yxatdan, majburiy |
| Adadi | >= 0, yangi tovarda kiritiladi; qoldiqdan mustaqil |
| Qoldig'i | >= 0, yangi tovarda kiritiladi |
| Kategoriya | Ixtiyoriy ("— Kategoriyasiz —" bor) |
| Ishlab chiqaruvchi, Davlat, Izoh | Ixtiyoriy |
| Yaroqlilik muddati | Ixtiyoriy sana (MaterialDatePicker, tozalash ikonkasi) |
| Rasm | 3-bosqich |

- Kasr faqat birlikda `allow_fraction=1` bo'lsa ruxsat (dona uchun yo'q). Kiritishda vergul nuqtaga almashtiriladi. Saqlashda 3 xonagacha yaxlitlanadi.
- **Qoldiq o'zgarsa** "Sabab" dialogi majburiy: kirim / chiqim / tuzatish / brak / boshqa (+ izoh; "boshqa" uchun izoh majburiy). `stock_movements` ga `delta = yangi - eski` yoziladi. Yangi tovarda boshlang'ich qoldiq `initial` sababi bilan yoziladi (0 bo'lmasa).
- Saqlamasdan chiqishga urinsa (orqaga, bekor qilish, toolbar strelkasi): "O'zgarishlar saqlanmadi" dialogi (Saqlash / Saqlamasdan chiqish / Qolish). O'zgarish borligi formaning "imzosi"ni boshlang'ich imzo bilan solishtirib aniqlanadi.
- O'chirish tahrirlash toolbar'ida ham bor (faqat mavjud tovarda), tasdiq bilan. Tahrirlash tarixi (kim/nima) yuritilmaydi.
- Saqlangandan keyin qidiruvga qaytiladi, qidiruv matni saqlanadi, ro'yxat `onResume` da yangilanadi.
- `onSaveInstanceState` da: tanlangan birlik/kategoriya id, muddat, boshlang'ich imzo.

### 7.5. Spravochniklar
- **Birliklar:** qo'shish, tahrirlash (nomi + "kasr ruxsat"), o'chirish. Ishlatilayotgan birlik o'chirilmaydi (xabar chiqadi).
- **Kategoriyalar:** qo'shish, tahrirlash, o'chirish. O'chirishda tovarlar "kategoriyasiz" bo'ladi, buning haqida ogohlantirish chiqadi.
- Ro'yxat qatorida: nom, kichik matn (kasr/butun, nechta tovarda ishlatilgani).

### 7.6. Sozlamalar
Til (lotin/kirill), mavzu, kam qoldiq chegarasi, muddat ogohlantirish kunlari, avtoqulf, PIN o'zgartirish (joriy + yangi + takror), birliklar va kategoriyalar. Natijalar `onResume` da ro'yxatga qo'llanadi (`adapter.setConfig`).

### 7.7. 2-bosqich (keyin)
- **Excel:** import (.xlsx, SAF orqali, ustunlarni moslash, oldindan ko'rish, xatolar hisoboti, mavjud kod uchun "o'tkazib yuborish/yangilash") va eksport (barcha yoki filtrlangan natija). Ma'lumotlar avval qo'lda kiritiladi, shuning uchun "Shablon yuklab olish" tugmasi. Apache POI Androidda og'ir, yengil kutubxona yoki o'zimizning minimal xlsx o'qish/yozish kodi.
- **Backup:** WorkManager bilan har kuni avtomatik (papka bir marta SAF orqali tanlanadi, doimiy ruxsat saqlanadi), oxirgi 7 nusxa, fayl `dukon_backup_YYYY-MM-DD.db`, qo'lda "hozir saqlash", sozlamada oxirgi muvaffaqiyatli sana va 3 kundan ortiq o'tsa ogohlantirish. Tiklash: tasdiq + versiya tekshiruvi. Eslatma: SAF fayl tanlagichi ochilganda avtoqulf "darhol" bo'lsa ilova fonga o'tgandek bo'ladi, shuni hisobga oling.
- **PIN'ni unutdim:** ma'lumotlarni o'chirib, tanlangan backup'dan tiklash + yangi PIN (qattiq ogohlantirish bilan).

## 8. Muhim dizayn qarorlari (nima uchun shunday)

### 8.1. Qidiruv normalizatsiyasi (`UzText.normalize`)
Ma'lumot ham, so'rov ham bitta kanonik **soddalashtirilgan lotin** shaklga keltiriladi: kichik harf, kirill harflari lotinga o'giriladi, apostroflar (' ʻ ʼ ‘ ’ ` ´ ′) olib tashlanadi, `q->k`, `x->h`, `w->v`, `ye->e`, `ts->s`; kirillda `ў->o, қ->k, ғ->g, ҳ->h, х->h, ц->s, ч->ch, ш->sh, щ->sh, ъ ь->"", ы->i, э->e, ё->yo, ю->yu, я->ya, й->y, ж->j`. Shuning uchun "O'zbekiston", "Ozbekiston" va "Ўзбекистон" bir xil bo'ladi. Qisman yozishda ham ishlashi uchun (masalan "s" -> "sh..." ni topadi) kanonik shakl lotin tanlangan.

`products.search_text = normalize(name + code + manufacturer + country + note)`. Kategoriya nomi `categories.name_search` da, so'rovda `(p.search_text || ' ' || IFNULL(c.name_search,'')) LIKE ? ESCAPE '\'` (har so'z uchun bitta shart, AND). Shunda kategoriya nomi o'zgarganda tovarlarni qayta indekslash shart emas.

### 8.2. Saralash (`UzText.sortKey`)
Nom avval kirillga o'giriladi (`toCyrillic`: o'/g' -> ў/ғ, sh, ch, yo/yu/ya/ye, ts, so'z boshidagi e -> э, apostrof -> ъ), keyin har kirill harf alifbodagi o'rniga qarab `(char)(0x100 + indeks)` ga almashtiriladi. Sabab: SQLite BINARY tartibida Ё, Ў, Қ, Ғ, Ҳ noto'g'ri joyga tushardi; bu kalit bilan Ё Е'dan keyin, Ў Қ Ғ Ҳ oxirida turadi (o'zbek kirill alifbosi tartibi). Raqam va belgilar harflardan oldin keladi. Shu kalit takroriy nomni aniqlashda ham ishlatiladi. Alifbo: `абвгдеёжзийклмнопрстуфхцчшщъыьэюяўқғҳ`.

### 8.3. Avtoqulf (`AppLock`)
`Application.ActivityLifecycleCallbacks`: ko'rinadigan activity'lar sonini sanaydi; 0 ga tushganda vaqt yoziladi, qaytganda (`timeout + ~800 ms`) o'tgan bo'lsa `unlocked=false`. `unlocked=false` bo'lsa, `onActivityStarted` da `LockActivity` ochiladi va shu paytda `android.R.id.content` `INVISIBLE` qilinadi (kontent miltillab ko'rinmasligi uchun), `onActivityResumed` da `unlocked` bo'lsa `VISIBLE` qaytariladi. `isChangingConfigurations()` holatida vaqt yozilmaydi. LockActivity'da orqaga = `finishAffinity()`. `unlocked` faqat xotirada (static), process o'lsa qayta so'raladi.

### 8.4. Til va mavzu
`DukonApp.onCreate` da `AppCompatDelegate.setApplicationLocales(...)` va `setDefaultNightMode(...)` chaqiriladi, sozlamalarda o'zgarganda ham shu metodlar (activity'lar o'zi recreate bo'ladi). Lotin = `values/` (default), kirill = `values-b+uz+Cyrl/`. Ikkala `strings.xml` kalitlari bir xil bo'lishi shart. Tavsiya: bitta Python skriptdan generatsiya qilish (apostrof `\'`, qo'shtirnoq `\"`, `&amp;`, ko'p argumentli satrlar `%1$s`). Birlik va kategoriya nomlari bazada foydalanuvchi kiritgan alifboda qoladi (seed lotinda).

### 8.5. Fon oqimi
Room asosiy oqimda ishlamaydi. Qidiruv uchun bitta oqimli executor (`SearchViewModel`) + `AtomicInteger` avlod hisoblagichi (eskirgan natijani tashlab yuborish). Boshqa chaqiruvlar `AppExecutors.io(...)` va natija `AppExecutors.main(...)` da. UI'da natija `SearchResult(query, items)` ko'rinishida keladi, joriy matn bilan mos kelmasa e'tiborga olinmaydi.

### 8.6. Boshqa
- Pul: `long` so'm; miqdor: `double`, ko'rsatishda `BigDecimal` bilan 3 xonagacha, ortiqcha nollarsiz, o'nlik ajratgich vergul, minglik NBSP.
- ListAdapter + DiffUtil, `setItemAnimator(null)`, 10 000+ qatorda ham tez bo'lishi kerak. Maqsad: qidiruv <= ~200-300 ms.
- Chegara/rang o'zgarsa `notifyDataSetChanged` (DiffUtil mazmunni o'zgarmagan deb hisoblaydi).
- Dialoglar: `MaterialAlertDialogBuilder`; tekshiruvdan o'tmasa dialog yopilmasligi uchun musbat tugma `setOnShowListener` ichida qayta bog'lanadi.
- Faza 1'da alohida EditViewModel yo'q (soddalik uchun), mantiq Activity + Repo da.

## 9. Ish tartibi (Hermes uchun tavsiya)

1. `UzText.java` ni lokalda kompilyatsiya qilib, 10-bo'lim testlarini o'tkazing, kerak bo'lsa tuzating.
2. `res/values` (strings lotin + kirill, colors, themes) va drawable'larni yozing.
3. `data/db` entity + DAO + `AppDatabase`, keyin `Repo`.
4. `util/*`, `DukonApp`, `AppLock`, `LockActivity`.
5. `MainActivity` + `SearchViewModel` + `ProductAdapter` + layoutlar.
6. `ProductDetailActivity`, `ProductEditActivity`.
7. `SettingsActivity`, `ReferenceActivity`.
8. Android Studio'da sync + build, xatolarni tuzatish; emulyatorda (Android 7 va 14) sinash.
9. Faqat shundan keyin 2-bosqichga o'tish.

Har bosqichdan keyin foydalanuvchiga qisqa hisobot bering (nima tayyor, nima sinalgan).

## 10. UzText test holatlari (kutilgan natija)

Quyidagilar `normalize()` dan keyin tengma-teng bo'lishi kerak:
`Сут = sut`, `Ўзбекистон = O'zbekiston = Ozbekiston`, `Қаймоқ = Qaymoq`, `Шакар = Shakar`, `Чой = Choy`, `Ёғ = Yog'`, `Янги = yangi`, `Етти = Yetti`, `Цирк = Sirk = Tsirk`, `Ҳаво = Xavo = Havo`, `Вино = Wino`, `МАЪЛУМОТ = ma'lumot`, `Нон 500г = non 500g`.

`toCyrillic()` natijalari: `shakar->шакар`, `o'rik->ўрик`, `yog'->ёғ`, `ma'no->маъно`, `choy->чой`, `eshik->эшик`, `yetti->етти`, `tsirk->цирк`, `qalam->қалам`, `hovli->ҳовли`, `xamir->хамир`, `Sut 1 litr->сут 1 литр`.

`sortKey()`: `sortKey("Sut") == sortKey("Сут")`; `sortKey("  Sut   1 ") == sortKey("сут 1")`; tartib (har biri keyingisidan kichik): Анор, Bodom, Вино, Gilam, Елим2, Ёғоч, Non, Olma, Sut, Shakar, Яшил, O'rik, Qalam, Hovli; `sortKey("1 kg") < sortKey("Anor")`. `escapeLike("50%_x\\")` = `50\%\_x\\`. `LAT` va `CYR` uzunliklari teng (37).

## 11. Foydalanuvchi qabul qilgan taklif qarorlar

Dastur nomi "Dukon"; PIN 5 xato = 30 soniya; bo'sh qidiruvda tarix paneli; kategoriya ro'yxatda ko'rinmaydi; kod aniq mos kelsa tepada; muddat ogohlantirish chegarasi 30 kun (sozlanadi); backup oxirgi 7 nusxa; matnlar ikki alifboda. Foydalanuvchi bularni o'zgartirishni xohlasa, aytadi.

## 12. Xavflar va ehtiyot bo'ladigan joylar

- Kod hali Android Studio'da kompilyatsiya qilinmagan. Versiyalar mos kelmasa (AGP/Gradle/compileSdk), `app/build.gradle` va `build.gradle` dagi raqamlarni yangilang.
- Per-app locale (`uz-Cyrl`) Android 7 emulyatorida ham tekshirilsin (resurs `b+uz+Cyrl` mosligi).
- Room'da `RawQuery` POJO maydonlari ustun nomlari bilan to'liq mos bo'lishi kerak (alias'lar: `initial_qty`, `expiry_date`, `unit_name`).
- `AutoCompleteTextView` dropdown'ida `setText(text, false)` ishlating (filtr ishga tushmasin).
- TextInputLayout "clear_text" ikonkasi fokussiz maydonda ko'rinmaydi, muddat maydonida `custom` end icon ishlating.
- PIN 4 raqam bo'lgani uchun xesh faqat tasodifiy ko'z oldidan himoya, jiddiy kriptografik himoya emas. Buni foydalanuvchiga halol ayting.
