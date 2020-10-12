package com.example.mi;

import android.graphics.Bitmap;

/**
 * Created by chalilayang on 20-10-12 下午2:08.
 **/
public class BitmapCompare {
    private static final String TAG = "BitmapCompare";
    private int[] mPixelsPre;
    private int[] mPixelsBack;
    private int mWidth;
    private int mHeight;

    public Bitmap cropBitmap(Bitmap bitmap) {
        final int width = bitmap.getWidth();
        final int height = bitmap.getHeight();
        final int length = width * height;
        if (mPixelsPre == null) {
            mPixelsPre = new int[length];
            bitmap.getPixels(mPixelsPre, 0, width, 0, 0, width, height);
            return bitmap;
        }
        if (mPixelsPre.length < length) {
            throw new RuntimeException("mPixelsPre.length < length");
        }
        if (mPixelsBack == null || mPixelsBack.length < length) {
            mPixelsBack = new int[length];
        }
        bitmap.getPixels(mPixelsBack, 0, width, 0, 0, width, height);
        mWidth = width;
        mHeight = height;
        int position = searchCropPosition();
        if (position <= 0 || position >= height) {
            return null;
        }
        System.arraycopy(mPixelsBack, 0, mPixelsPre, 0, length);
        return Bitmap.createBitmap(bitmap, 0, height - position - 1, width, position);
    }

    private int searchCropPosition() {
        final int height = mHeight;
        int startOffset = -1;
        for (int line2 = height - 1; line2 >= 0; line2 --) {
            int line1Top = height - line2 - 1;
            if (compareLine(height - 1, line2) && compareLine(line1Top, 0)) {
                startOffset = line2;
                break;
            }
        }
        return height - startOffset - 1;
    }

    private boolean compareLine(int linePre, int lineBack) {
        boolean result = true;
        int startIndexPre = linePre * mWidth;
        int startIndexBack = lineBack * mWidth;
        for (int index = 0; index < mWidth; index = index + 10) {
            if ((mPixelsPre[startIndexPre + index] - mPixelsBack[startIndexBack + index]) != 0) {
                result = false;
                break;
            }
        }
        return result;
    }
}
