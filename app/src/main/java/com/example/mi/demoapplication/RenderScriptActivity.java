package com.example.mi.demoapplication;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.miui.screenshot.BitmapUtils;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.ByteBuffer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import butterknife.BindView;
import butterknife.ButterKnife;

public class RenderScriptActivity extends AppCompatActivity {

    private static final String TAG = "RenderScriptActivity";
    @BindView(R.id.image1)
    ImageView image1;
    @BindView(R.id.image2)
    ImageView image2;
    @BindView(R.id.image3)
    ImageView image3;
    @BindView(R.id.image4)
    ImageView image4;
    private Bitmap[] mBitmaps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_render_script);
        ButterKnife.bind(this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mBitmaps = decodeBitmap();
        image1.setImageBitmap(mBitmaps[0]);
        image2.setImageBitmap(mBitmaps[1]);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            long start = SystemClock.elapsedRealtime();
            int value = BitmapUtils.nativeCompareBitmapWithSimilarity(mBitmaps[0], mBitmaps[1], 0.01f);
            double similarity = BitmapUtils.nativeGetSimilarity(
                    mBitmaps[0], value, mBitmaps[1], 0, mBitmaps[1].getHeight() - value, 0.01f);
            Log.i(TAG, "onClick: value " + value + " similarity " + similarity + " " + mBitmaps[0].getHeight()
                    + " cost " + (SystemClock.elapsedRealtime() - start));
            Rect rect = new Rect();
            rect.left = 0;
            rect.top = value;
            rect.right = mBitmaps[0].getWidth() - 1;
            rect.bottom = mBitmaps[0].getHeight();
            if (rect.height() > 0) {
                image1.setImageBitmap(drawRectOnBitmap(mBitmaps[0], rect));
                Bitmap bitmapa = createBitmapRegion(mBitmaps[0], rect);
                image3.setImageBitmap(bitmapa);
                rect.top = 0;
                rect.bottom = mBitmaps[0].getHeight() - value;
                image2.setImageBitmap(drawRectOnBitmap(mBitmaps[1], rect));
                Bitmap bitmapb = createBitmapRegion(mBitmaps[1], rect);
                image4.setImageBitmap(bitmapb);
            }

//                ViewGroup.MarginLayoutParams layoutParams
//                        = (ViewGroup.MarginLayoutParams) image2.getLayoutParams();
//                layoutParams.topMargin --;
//                image2.setLayoutParams(layoutParams);
//                Log.i(TAG, "onClick: " + layoutParams.topMargin + " " + image2.getHeight() + " " + mBitmaps[1].getHeight());
//                image2.setAlpha(0.6f);
//
//                image3.setImageBitmap(dddBitmap(layoutParams.topMargin));

//                long start = SystemClock.elapsedRealtime();
//                double min = Double.MAX_VALUE;
//                int minIndex = -1;
//                for (int index = 0; index < 2188; index ++) {
//                    double sim = BitmapUtils.nativeGetSimilarity(
//                            mBitmaps[0], index, mBitmaps[1], 0, 2188 - index, 0.01f);
//                    if (sim < min) {
//                        minIndex = index;
//                        min = sim;
//                    }
//                    Log.i(TAG, "onClick: Similarity " + index + " " + new BigDecimal(sim+""));
//                }
//                Log.i(TAG, "onClick: Similarity minIndex " + minIndex + " cost " + (SystemClock.elapsedRealtime() - start));
        });
        fab = findViewById(R.id.fab2);
        fab.setOnClickListener(view -> {
//            ViewGroup.MarginLayoutParams layoutParams
//                    = (ViewGroup.MarginLayoutParams) image2.getLayoutParams();
//            layoutParams.topMargin ++;
//            image2.setLayoutParams(layoutParams);
//            Log.i(TAG, "onClick: " + layoutParams.topMargin + " " + image2.getHeight() + " " + mBitmaps[1].getHeight());
//            image2.setAlpha(0.6f);
//            image3.setImageBitmap(dddBitmap(layoutParams.topMargin));
        });
    }

    public Bitmap dddBitmap(int bottom) {
        bottom = Math.abs(bottom);
        Rect rect = new Rect(
                0,
                mBitmaps[0].getHeight() - 1 - bottom,
                mBitmaps[0].getWidth() - 1, mBitmaps[0].getHeight() - 1);
        int[] p1 = createBitmapPixels(mBitmaps[0], rect);
        rect.offset(0, -rect.top);
        int[] p2 = createBitmapPixels(mBitmaps[1], rect);
        long sum = 0;
        for (int i = 0, count = p1.length; i < count; i ++) {
            int d = p1[i] - p2[i];
            p1[i] = d;
            if (d != 0) {
                sum ++;
            }
        }
        Log.i(TAG, "dddBitmap: sum " + (sum * 1.0f / p1.length));
        return Bitmap.createBitmap(p1, rect.width(), rect.height(), mBitmaps[0].getConfig());
    }

    public Bitmap[] decodeRegion() {
        Bitmap[] result = new Bitmap[2];
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(getResources(), R.drawable.screenshot, options);
        int width = options.outWidth;
        int height = options.outHeight;
        try {
            InputStream inputStream = getResources().openRawResource(R.drawable.screenshot);
            BitmapRegionDecoder decoder = BitmapRegionDecoder.newInstance(inputStream,false);
            Rect rect = new Rect(0, 0, width, height / 3);
            BitmapFactory.Options regionOptions = new BitmapFactory.Options();
            Bitmap result1 = decoder.decodeRegion(rect, regionOptions);
            rect.offset(0, 300);
            Bitmap result2 = decoder.decodeRegion(rect, regionOptions);
            result[0] = result1;
            result[1] = result2;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public Bitmap[] decodeBitmap() {
        Bitmap[] result = new Bitmap[2];
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(getResources(), R.drawable.r5, options);
        int width = options.outWidth;
        int height = options.outHeight;
        try {
            InputStream inputStream = getResources().openRawResource(R.drawable.r1);
            BitmapRegionDecoder decoder = BitmapRegionDecoder.newInstance(inputStream,false);
            Rect rect = new Rect(0, 0, width, height);
            BitmapFactory.Options regionOptions = new BitmapFactory.Options();
            Bitmap result1 = decoder.decodeRegion(rect, regionOptions);

            rect.offset(0, 0);
            inputStream = getResources().openRawResource(R.drawable.r2);
            decoder = BitmapRegionDecoder.newInstance(inputStream,false);
            Bitmap result2 = decoder.decodeRegion(rect, regionOptions);

            result[0] = result1;
            result[1] = result2;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public boolean compare(int[] d1, int[] d2) {
        for (int index = 0, count = d1.length; index < count; index ++) {
            if (d1[index] != d2[index]) {
                return false;
            }
        }
        return true;
    }

    public int[] createBitmapPixels(Bitmap bitmap, Rect region) {
        int width = region.width();
        int height = region.height();
        int[] pix = new int[width * height];
        bitmap.getPixels(pix, 0, width, region.left, region.top, width, height);
        return pix;
    }

    public Bitmap createBitmapRegion(Bitmap bitmap, Rect region) {
        int[] pix = createBitmapPixels(bitmap, region);
        bitmap.getPixels(pix, 0, region.width(), region.left, region.top, region.width(), region.height());
        return Bitmap.createBitmap(
                pix, 0, region.width(), region.width(), region.height(), bitmap.getConfig());
    }

    public int compareBitmap(Bitmap bitmapPre, Bitmap bitmapBack) {
        long start = System.currentTimeMillis();
        int height = bitmapPre.getHeight();
        int startOffset = -1;
        if (bitmapPre.sameAs(bitmapBack)) {
            Log.i(TAG, "compareBitmap: sameAs");
            return 0;
        }
        for (int lineBack = height - 1; lineBack >= 0; lineBack --) {
            int linePre = height - lineBack - 1;
            if (compareBitmapRange(
                    bitmapPre, linePre, height - 1, bitmapBack, 0, lineBack)) {
                Log.i(TAG, "compareBitmap: lineBack " + lineBack);
                startOffset = lineBack;
                break;
            }
        }
        Log.i(TAG, "compareBitmap: cost " + (System.currentTimeMillis() - start));
        return height - startOffset - 1;
    }

//    public boolean compareBitmapRange(
//            Bitmap bitmapPre, int lineTopPre, int lineBottomPre,
//            Bitmap bitmapBack, int lineTopBack, int lineBottomBack) {
//        if (lineBottomPre - lineTopPre != lineBottomBack - lineTopBack) {
//            return false;
//        }
//        Log.i(TAG, "compareBitmapRange: linePre "
//                + lineTopPre + "--" + lineBottomPre + "  lineBack " + lineTopBack + " --" + lineBottomBack);
//        int count = lineBottomPre - lineTopPre;
//        for(int index = 0; index <= count; index ++) {
//            if (!compareBitmapLine(
//                    bitmapPre, lineTopPre + index, bitmapBack, lineTopBack + index)) {
//                Log.i(TAG, "compareBitmapRange: " + (lineTopPre + index) + " " + (lineTopBack + index) + "  false");
//                return false;
//            }
//        }
//        return true;
//    }

    public boolean compareBitmapRange(
            Bitmap bitmapPre, int lineTopPre, int lineBottomPre,
            Bitmap bitmapBack, int lineTopBack, int lineBottomBack) {
        if (lineBottomPre - lineTopPre != lineBottomBack - lineTopBack) {
            return false;
        }
        Log.i(TAG, "compareBitmapRange: linePre "
                + lineTopPre + "--" + lineBottomPre + "  lineBack " + lineTopBack + " --" + lineBottomBack);
        int count = lineBottomPre - lineTopPre;
        for(int index = 0; index <= count; index ++) {
            if (!compareBitmapLine(bitmapPre, lineTopPre + index, bitmapBack, lineTopBack + index)) {
                Log.i(TAG, "compareBitmapRange:"+ " "+ (lineTopPre + index) + " " + (lineTopBack + index) + "  false");
                return false;
            }
        }
        return true;
    }

    private boolean compareBitmap2(Bitmap bitmap1, Bitmap bitmap2) {
        int height = bitmap1.getHeight();
        for (int line2 = height - 1; line2 >= 0; line2 --) {
            if (!compareBitmapLine(bitmap1, height - 1, bitmap2, line2)) {
                return false;
            }
        }
        return true;
    }

    public String getPixelString(Bitmap bitmap) {
        StringBuilder sb = new StringBuilder();
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        if (pixel1 == null) {
            pixel1 = new int[width];
        }
        for (int index = 0; index < height; index ++) {
//            bitmap.getPixels(pixel1, 0, width, 0, index, width, 1);
//            for (int value : pixel1) {
//                sb.append(Color.red(value)).append("  ");
//            }
            for (int x = 0; x < width; x ++) {
                sb.append(bitmap.getPixel(x, index)).append("  ");
            }
            sb.append("\n");
            Log.i(TAG, "getPixelString: " + sb.length());
        }
        return sb.toString();
    }

    private static void saveAsFileWriter(String content, String path) {
        FileWriter fwriter = null;
        try {
            // true表示不覆盖原来的内容，而是加到文件的后面。若要覆盖原来的内容，直接省略这个参数就好
            fwriter = new FileWriter(path);
            fwriter.write(content);
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

    int[] pixel1;
    int[] pixel2;
    private boolean compareBitmapLine(Bitmap bitmap1, int line1, Bitmap bitmap2, int line2) {
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
        int sum = 0;
        for (int index = 0; index < width; index = index + 3) {
            if ((pixel1[index] - pixel2[index]) != 0) {
                sum ++;
                if (sum > 80) {
                    result = false;
                    break;
                }
            }
        }
        return result;
    }

    static {
        System.loadLibrary("native-lib");
    }
    public native int[] imgToGray(int[] pixels, int width, int height);
    public native int compareByte(byte[] pixels1, byte[] pixels2, int width, int height);
    public native int nativeCompareBitmap(Bitmap bitmap, Bitmap bitmap2);

    private ByteBuffer byteBuffer1;
    private ByteBuffer byteBuffer2;
    public boolean compareBitmapNative(Bitmap bitmap1, Bitmap bitmap2) {
        if (bitmap1 == null || bitmap2 == null) {
            return false;
        }
        int height1 = bitmap1.getHeight();
        int height2 = bitmap2.getHeight();
        if (height1 != height2) {
            return false;
        }
        int rowBytes1 = bitmap1.getRowBytes();
        int rowBytes2 = bitmap2.getRowBytes();
        if (rowBytes1 != rowBytes2) {
            return false;
        }
        int bytesCount = rowBytes1 * height1;
//        ByteBuffer byteBuffer1 = ByteBuffer.allocateDirect(bytesCount);
        bitmap1.copyPixelsToBuffer(byteBuffer1);
        byte[] byteArray1 = byteBuffer1.array();
//        byteBuffer1 = ByteBuffer.allocateDirect(bytesCount);
        bitmap2.copyPixelsToBuffer(byteBuffer2);
        byte[] byteArray2 = byteBuffer2.array();
        long start = System.currentTimeMillis();
        boolean result = compareByte(byteArray1, byteArray2, rowBytes1, height1) == 1;
        Log.i(TAG, "compareBitmap: cost " + (System.currentTimeMillis() - start) + " " + result);
        return result;
    }

    public static Bitmap transViewToBitmap(View view) {
        long start = System.currentTimeMillis();
        int w = view.getMeasuredWidth();
        int h = view.getMeasuredHeight();
        Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);
        canvas.drawColor(Color.TRANSPARENT);
        view.layout(0, 0, w, h);
        view.draw(canvas);
        Log.i(TAG, "transViewToBitmap: " + (System.currentTimeMillis() - start));
        return bmp;
    }

    public Bitmap drawRectOnBitmap(Bitmap bitmap, Rect rect) {
        Log.i(TAG, "drawRectOnBitmap: " + rect.height());
        rect.top = Math.max(0, rect.top);
        rect.bottom = Math.min(bitmap.getHeight(), rect.bottom);
        Bitmap result = bitmap.copy(bitmap.getConfig(), true);
        Canvas canvas = new Canvas(result);
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setAlpha(100);
        canvas.drawRect(rect, paint);
        return result;
    }
}