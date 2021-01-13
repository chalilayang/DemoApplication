package com.miui.screenshot;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Random;

/**
 * Created by chalilayang on 20-12-30 下午4:17.
 **/
public class BitmapUtils {
    private static final String TAG = "BitmapUtils";
    public static Bitmap cropBitmap(Bitmap bitmap, Rect rect) {
        if (bitmap == null) {
            return null;
        }
        if (rect == null) {
            return bitmap;
        }
        Log.i(TAG, "cropBitmap: " + bitmap.getHeight() + " " + rect);
        return Bitmap.createBitmap(bitmap, rect.left, rect.top, rect.width(), rect.height());
    }

    public static Bitmap cropBitmapFromTop(Bitmap bitmap, int cropHeightFromTop) {
        if (bitmap == null || cropHeightFromTop <= 1 || bitmap.getHeight() <= cropHeightFromTop) {
            return null;
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), cropHeightFromTop);
    }

    public static Bitmap cropBitmapFromBottom(Bitmap bitmap, int cropHeightFromBottom) {
        if (bitmap == null || cropHeightFromBottom <= 0 || bitmap.getHeight() <= cropHeightFromBottom) {
            return null;
        }
        return Bitmap.createBitmap(bitmap,
                0, bitmap.getHeight() - cropHeightFromBottom - 1, bitmap.getWidth(), cropHeightFromBottom);
    }

    public static int compareBitmap(Bitmap bitmapPre, Bitmap bitmapBack) {
        Log.i(TAG, "compareBitmap: start");
        long start = System.currentTimeMillis();
        int height = bitmapPre.getHeight();
        int startOffset = -1;
        for (int lineBack = height - 1; lineBack >= 0; lineBack --) {
            int linePre = height - lineBack - 1;
//            if (compareBitmapRange(
//                    bitmapPre, linePre, height - 1, bitmapBack, 0, lineBack)) {
//                startOffset = lineBack;
//                break;
//            }
            if (nativeCompareBitmapRange(
                    bitmapPre, linePre, height - 1, bitmapBack, 0, lineBack, 2) == 1) {
                startOffset = lineBack;
                break;
            }
        }
        Log.i(TAG, "compareBitmap: cost " + (System.currentTimeMillis() - start));
        return height - startOffset - 1;
    }

    public static boolean compareBitmapRange(
            Bitmap bitmapPre, int lineTopPre, int lineBottomPre,
            Bitmap bitmapBack, int lineTopBack, int lineBottomBack) {
        if (lineBottomPre - lineTopPre != lineBottomBack - lineTopBack) {
            return false;
        }
        int count = lineBottomPre - lineTopPre;
        for(int index = 0; index <= count; index = index + 2) {
            if (!compareBitmapLine(
                    bitmapPre, lineTopPre + index, bitmapBack, lineTopBack + index)) {
                return false;
            }
        }
        return true;
    }

    static int[] pixel1;
    static int[] pixel2;
    private static boolean compareBitmapLine(Bitmap bitmap1, int line1, Bitmap bitmap2, int line2) {
        boolean result = true;
        int width = bitmap1.getWidth();
        if (pixel1 == null) {
            pixel1 = new int[width];
        }
        if (pixel2 == null) {
            pixel2 = new int[width];
        }
        bitmap1.getPixels(pixel1, 0, width, 0, line1, width, 1);
        bitmap2.getPixels(pixel2, 0, width, 0, line2, width, 1);
        for (int index = 0; index < width; index ++) {
            if ((pixel1[index]^pixel2[index]) != 0) {
                result = false;
                break;
            }
        }
        return result;
    }

    public static void saveBitmapList(List<Bitmap> bitmapList, String tail) {
        File mScreenshotDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM), "ScreenshotsTest");
        if (!mScreenshotDir.exists()) {
            mScreenshotDir.mkdirs();
        }
        int count = new Random(System.currentTimeMillis()).nextInt(100);
        for (Bitmap bitmap : bitmapList) {
            saveBitmap(bitmap, mScreenshotDir.getAbsoluteFile() + File.separator + (count++) + "_" + tail + ".png");
        }
    }

    public static boolean saveBitmap(Bitmap bitmap, String path) {
        boolean success = false;
        OutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(path);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            success = true;
        } catch (Exception e) {
            Log.e(TAG, "saveBitmapToUri: ", e);
            e.printStackTrace();
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.flush();
                    outputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return success;
    }

    public static void saveBitmapPixel(Bitmap bitmap, String name) {
        File mScreenshotDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM), "ScreenshotsTest");
        if (!mScreenshotDir.exists()) {
            mScreenshotDir.mkdirs();
        }
        FileWriter fwriter = null;
        try {
            // true表示不覆盖原来的内容，而是加到文件的后面。若要覆盖原来的内容，直接省略这个参数就好
            fwriter = new FileWriter(mScreenshotDir.getAbsolutePath() + File.separator + name);
            fwriter.write(getPixelString(bitmap));
            Log.i(TAG, "saveBitmapPixel: " + name);
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                fwriter.flush();
                fwriter.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static String getPixelString(Bitmap bitmap) {
        StringBuilder sb = new StringBuilder();
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        if (pixel1 == null) {
            pixel1 = new int[width];
        }
        for (int index = 0; index < height; index ++) {
            for (int x = 0; x < width; x ++) {
                sb.append(bitmap.getPixel(x, index)).append("  ");
            }
            sb.append("\r\n");
        }
        return sb.toString();
    }

    static {
        System.loadLibrary("bitmap-compare");
    }
    public static native int[] imgToGray(int[] pixels, int width, int height);
    public static native int compareByte(byte[] pixels1, byte[] pixels2, int width, int height);
    public static native int getBitmapStride(Bitmap bitmap);
    public static native double nativeGetSimilarity(
            Bitmap bitmapPre, int startYPre, Bitmap bitmapBack, int startYBack, int height, double threshold);
    public static native int nativeCompareBitmap(Bitmap bitmap, Bitmap bitmap2, float threshold);
    public static native int nativeCompareBitmapRange(Bitmap bitmapPre, int lineTopPre, int lineBottomPre,
                                                      Bitmap bitmapBack, int lineTopBack, int lineBottomBack, int step);
}
