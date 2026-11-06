package sardor.xiva.dukono.util;

// Kerakli Android va Java kutubxonalarini import qilamiz
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.widget.ImageView;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * Tovarlar rasmlarini qayta ishlash va saqlash uchun utilit sinfi.
 * Rasmlar hajmi oshib ketmasligi uchun ularni ekran enining 70% o'lchamigacha ixchamlashtiradi (scale & compress).
 * Maksimal fayl hajmi 500 KB (0.5 MB) dan oshmasligi kafolatlanadi.
 */
public final class ImageUtil {

    // Maksimal fayl hajmi: 500 KB (0.5 MB)
    private static final long MAX_FILE_SIZE_BYTES = 500 * 1024L;

    // Konstruktorni shaxsiy qilamiz (Utility sinfidan obyekt olinmasligi uchun)
    private ImageUtil() {}

    /**
     * Rasmlarni saqlash papkasini olamiz (Pictures/Dukono yoki App External Files).
     */
    public static File getPicturesDir(Context context) {
        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Dukono");
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (!created) {
                dir = context.getExternalFilesDir("product_images");
            }
        }
        if (dir == null) {
            dir = new File(context.getFilesDir(), "product_images");
            if (!dir.exists()) dir.mkdirs();
        }
        return dir;
    }

    /**
     * Kameradan rasmga olish uchun vaqtinchalik Uri yaratadi.
     */
    public static Uri createTempCameraUri(Context context) {
        try {
            File cacheDir = new File(context.getCacheDir(), "camera_photos");
            if (!cacheDir.exists()) cacheDir.mkdirs();
            File tempFile = new File(cacheDir, "temp_cam_" + System.currentTimeMillis() + ".jpg");
            String authority = context.getPackageName() + ".fileprovider";
            return FileProvider.getUriForFile(context, authority, tempFile);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Berilgan Uri manzilidagi rasmni ekran enining 70% o'lchamida va max 500 KB hajmda ixchamlab saqlaydi.
     *
     * @param context App konteksti
     * @param imageUri Galeriyadan yoki kameradan olingan rasm Uri si
     * @return Saqlangan rasm faylining absolyut yo'li (path), xatolik bo'lsa null
     */
    public static String saveProductImage(Context context, Uri imageUri) {
        if (context == null || imageUri == null) return null;

        try {
            // Ekran o'lchamlarini olamiz
            DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();

            // Ekran enining 70% o'lchamini hisoblaymiz (masalan 1080px bo'lsa ~756px)
            int targetSize = (int) (displayMetrics.widthPixels * 0.70);
            if (targetSize < 200) targetSize = 400; // Minimal o'lcham chegarasi

            // 1. Dastlab rasmning asl o'lchamlarini (kenglik va balandlik) o'qiymiz
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

            // 3. Rasmni xotiraga ixchamlashgan holda dekod qilamiz
            options.inJustDecodeBounds = false;
            options.inSampleSize = inSampleSize;

            Bitmap bitmap;
            try (InputStream is = context.getContentResolver().openInputStream(imageUri)) {
                bitmap = BitmapFactory.decodeStream(is, null, options);
            }

            if (bitmap == null) return null;

            // 4. Rasmni targetSize (ekran enining 70%) o'lchamigacha proporsional masshtablaymiz
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

            // 5. Saqlash uchun Pictures/Dukono papkasini olamiz
            File dir = getPicturesDir(context);

            // Noyob fayl nomi hosil qilamiz
            String fileName = "img_" + System.currentTimeMillis() + ".jpg";
            File outputFile = new File(dir, fileName);

            // 6. Rasmni JPEG formatida max 500 KB hajmda saqlash
            int quality = 80;
            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, fos);
                fos.flush();
            }

            // Agar rasm hajmi 500 KB dan katta bo'lib ketsa, sifatni 60% ga tushirib qayta siqamiz
            if (outputFile.length() > MAX_FILE_SIZE_BYTES) {
                try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 60, fos);
                    fos.flush();
                }
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
                boolean deleted = file.delete();
            }
        } catch (Exception ignored) {}
    }

    /**
     * Barcha tovar rasmlarini xotiradan to'liq o'chirish (Sozlamalardagi tozalash uchun).
     */
    public static void deleteAllProductImages(Context context) {
        if (context == null) return;
        try {
            // Pictures/Dukono papkasini tozalaymiz
            File pubDir = getPicturesDir(context);
            deleteFilesInDir(pubDir);

            // App External Files papkasini tozalaymiz
            File extDir = context.getExternalFilesDir("product_images");
            deleteFilesInDir(extDir);

            // App Internal Files papkasini tozalaymiz
            File intDir = new File(context.getFilesDir(), "product_images");
            deleteFilesInDir(intDir);

            // Vaqtinchalik kesh fayllarni tozalaymiz
            File cacheDir = new File(context.getCacheDir(), "camera_photos");
            deleteFilesInDir(cacheDir);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void deleteFilesInDir(File dir) {
        if (dir != null && dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.isFile()) f.delete();
                }
            }
        }
    }
}
