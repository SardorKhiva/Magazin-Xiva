package sardor.xiva.dukono.util;

// Kerakli Android va Java kutubxonalarini import qilamiz
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * Tovarlar rasmlarini qayta ishlash va saqlash uchun utilit sinfi.
 * Rasmlar hajmi oshib ketmasligi uchun ularni ekran enining 70% o'lchamigacha ixchamlashtiradi (scale & compress).
 */
public final class ImageUtil {

    // Konstruktorni shaxsiy qilamiz (Utility sinfidan obyekt olinmasligi uchun)
    private ImageUtil() {}

    /**
     * Berilgan Uri manzilidagi rasmni ekran enining 70% o'lchamida ixchamlab saqlaydi.
     *
     * @param context App konteksti
     * @param imageUri Galeriyadan tanlangan rasm Uri si
     * @return Saqlangan ichki rasm faylining absolyut yo'li (path), xatolik bo'lsa null
     */
    public static String saveProductImage(Context context, Uri imageUri) {
        if (context == null || imageUri == null) return null;

        try {
            // Ekran o'lchamlarini olamiz
            DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();

            // Ekran enining 70% o'lchamini hisoblaymiz (masalan 1080px bo'lsa ~756px)
            int targetSize = (int) (displayMetrics.widthPixels * 0.70);
            if (targetSize < 200) targetSize = 400; // Minimal o'lcham chegarasi

            // 1. Dastlab rasmning asl o'lchamlarini (kenglik va balandlik) o'qiymiz (xotiraga to'liq yuklamasdan)
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;

            try (InputStream is = context.getContentResolver().openInputStream(imageUri)) {
                BitmapFactory.decodeStream(is, null, options);
            }

            int srcWidth = options.outWidth;
            int srcHeight = options.outHeight;

            if (srcWidth <= 0 || srcHeight <= 0) return null;

            // 2. inSampleSize ni hisoblaymiz (xotirada rasm hajmini kamaytirish uchun)
            int inSampleSize = 1;
            int maxDim = Math.max(srcWidth, srcHeight);
            while (maxDim / (inSampleSize * 2) >= targetSize) {
                inSampleSize *= 2;
            }

            // 3. Endi rasmni xotiraga haqiqiy ixchamlashgan holda dekod qilamiz
            options.inJustDecodeBounds = false;
            options.inSampleSize = inSampleSize;

            Bitmap bitmap;
            try (InputStream is = context.getContentResolver().openInputStream(imageUri)) {
                bitmap = BitmapFactory.decodeStream(is, null, options);
            }

            if (bitmap == null) return null;

            // 4. Rasmni aniq targetSize (ekran enining 70%) o'lchamigacha proporsional masshtablaymiz
            int currentMax = Math.max(bitmap.getWidth(), bitmap.getHeight());
            if (currentMax > targetSize) {
                float scale = (float) targetSize / currentMax;
                int newW = Math.round(bitmap.getWidth() * scale);
                int newH = Math.round(bitmap.getHeight() * scale);
                Bitmap scaled = Bitmap.createScaledBitmap(bitmap, newW, newH, true);
                if (scaled != bitmap) {
                    bitmap.recycle();
                    bitmap = scaled;
                }
            }

            // 5. Saqlash uchun ilovaning ichki papkasida "product_images" papkasini yaratamiz
            File dir = new File(context.getFilesDir(), "product_images");
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // Noyob fayl nomi hosil qilamiz
            String fileName = "img_" + System.currentTimeMillis() + ".jpg";
            File outputFile = new File(dir, fileName);

            // 6. Rasmni JPEG formatida (80% sifat bilan) siqib faylga yozamiz
            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos);
                fos.flush();
            }

            // Bitmap xotirasini bo'shatamiz
            bitmap.recycle();

            // Saqlangan fayl yo'lini qaytaramiz
            return outputFile.getAbsolutePath();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Berilgan yo'ldagi rasmni ImageView ga yuklaydi.
     *
     * @param imageView Rasm ko'rsatiladigan ImageView
     * @param imagePath Rasm faylining yo'li
     */
    public static void loadProductImage(ImageView imageView, String imagePath) {
        if (imageView == null) return;

        if (imagePath == null || imagePath.trim().isEmpty()) {
            imageView.setImageDrawable(null);
            return;
        }

        File file = new File(imagePath);
        if (!file.exists()) {
            imageView.setImageDrawable(null);
            return;
        }

        try {
            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            imageView.setImageBitmap(bitmap);
        } catch (Exception e) {
            imageView.setImageDrawable(null);
        }
    }

    /**
     * Rasm faylini o'chirish yordamchi metodi.
     */
    public static void deleteImageFile(String imagePath) {
        if (imagePath == null || imagePath.trim().isEmpty()) return;
        try {
            File file = new File(imagePath);
            if (file.exists()) {
                file.delete();
            }
        } catch (Exception ignored) {}
    }
}
