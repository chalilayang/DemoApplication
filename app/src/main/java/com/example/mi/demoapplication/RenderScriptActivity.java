package com.example.mi.demoapplication;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.renderscript.Allocation;
import android.renderscript.RenderScript;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import com.example.mi.ScriptC_flip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.InputStream;
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
    private RenderScript mRenderScript;
    private ScriptC_flip mScriptCFlip;
    private Bitmap[] mBitmaps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_render_script);
        ButterKnife.bind(this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mBitmaps = decodeRegion();
        image1.setImageBitmap(mBitmaps[0]);
        image3.setImageBitmap(mBitmaps[1]);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int w = mBitmaps[0].getWidth();
                int h = mBitmaps[0].getHeight();
                int[] pixel = new int[w * h];
                mBitmaps[0].getPixels(pixel, 0, w, 0, 0, w, h);
                Bitmap bitmap2 = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
                byteBuffer1 = ByteBuffer.allocateDirect(mBitmaps[0].getByteCount());
                byteBuffer2 = ByteBuffer.allocateDirect(mBitmaps[1].getByteCount());
                long start = System.currentTimeMillis();
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//                Bitmap flipBitmap = processBitmap3();
//                image1.setImageBitmap(flipBitmap);
//                compareBitmap(mBitmaps[0], mBitmaps[0]);
//                new ComputeTask(getApplicationContext()).execute();
//                BitmapCompare bitmapCompare = new BitmapCompare();
//                bitmapCompare.cropBitmap(mBitmaps[0]);

//                Bitmap bitmap = bitmapCompare.cropBitmap(mBitmaps[1]);
//                image2.setImageBitmap(bitmap);
//                boolean same = compareBitmap();


                boolean same = compareBitmapNative(mBitmaps[0], mBitmaps[1]);
//                boolean same = mBitmaps[0].sameAs(mBitmaps[1]);
                Log.i(TAG, "onClick: " + (System.currentTimeMillis() - start) + " " + same);
                transViewToBitmap(getWindow().getDecorView());
            }
        });

        mRenderScript = RenderScript.create(getApplicationContext());
        mScriptCFlip = new ScriptC_flip(mRenderScript);
    }

    private Allocation mInAllocation;
    private Allocation mOutAllocation;
    private Allocation mExtraAllocation;

    public Bitmap flipBitmap() {
        if (mInAllocation == null) {
            mInAllocation = Allocation.createFromBitmapResource(mRenderScript, getResources(), R.drawable.music);
        }

        int width = mInAllocation.getType().getX();
        int height = mInAllocation.getType().getY();
        Bitmap outBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        if (mOutAllocation == null) {
            mOutAllocation = Allocation.createTyped(
                    mRenderScript, mInAllocation.getType(), Allocation.USAGE_SCRIPT);
        }
        if (mExtraAllocation == null) {
            mExtraAllocation = Allocation.createFromBitmapResource(mRenderScript, getResources(), R.drawable.wechat2);
        }

        mScriptCFlip.set_gIn(mExtraAllocation);
        mScriptCFlip.set_imageHeight(height);
        mScriptCFlip.set_imageWidth(width);
        mScriptCFlip.forEach_addPixel(mInAllocation, mOutAllocation);
        mOutAllocation.copyTo(outBitmap);
        return outBitmap;
    }

    public Bitmap processBitmap() {
        if (mInAllocation == null) {
            mInAllocation = Allocation.createFromBitmapResource(mRenderScript, getResources(), R.drawable.wechat2);
        }
        Allocation outputAllocation = Allocation.createTyped(
                mRenderScript, mInAllocation.getType(), Allocation.USAGE_SCRIPT);
        mScriptCFlip.invoke_process(mInAllocation, outputAllocation);
        int width = outputAllocation.getType().getX();
        int height = outputAllocation.getType().getY();
        Bitmap outBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        outputAllocation.copyTo(outBitmap);
        return outBitmap;
    }

    public Bitmap processBitmap2() {
        long start = System.currentTimeMillis();
        if (mInAllocation == null) {
            mInAllocation = Allocation.createFromBitmap(mRenderScript, mBitmaps[0]);
        }
        if (mExtraAllocation == null) {
            mExtraAllocation = Allocation.createFromBitmap(mRenderScript, mBitmaps[1]);
        }
        Allocation outputAllocation = Allocation.createTyped(
                mRenderScript, mInAllocation.getType(), Allocation.USAGE_SCRIPT);
        int width = mInAllocation.getType().getX();
        int height = mInAllocation.getType().getY();
        mScriptCFlip.invoke_process2(mInAllocation, mExtraAllocation, outputAllocation);
        Bitmap outBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        outputAllocation.copyTo(outBitmap);
        Log.i(TAG, "processBitmap2: cost " + (System.currentTimeMillis() - start));
        return outBitmap;
    }

    public Bitmap processBitmap3() {
        long start = System.currentTimeMillis();
        if (mInAllocation == null) {
            mInAllocation = Allocation.createFromBitmap(mRenderScript, mBitmaps[0]);
        }
        if (mExtraAllocation == null) {
            mExtraAllocation = Allocation.createFromBitmap(mRenderScript, mBitmaps[1]);
        }
        Allocation outputAllocation = Allocation.createTyped(
                mRenderScript, mInAllocation.getType(), Allocation.USAGE_SCRIPT);
        int width = mInAllocation.getType().getX();
        int height = mInAllocation.getType().getY();
        mScriptCFlip.invoke_process3(mInAllocation, mExtraAllocation, outputAllocation);
        Bitmap outBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        outputAllocation.copyTo(outBitmap);
        Log.i(TAG, "processBitmap3: cost " + (System.currentTimeMillis() - start));
        return outBitmap;
    }

    public boolean compareBitmap() {
//        long start = System.currentTimeMillis();
//        if (mInAllocation == null) {
//            mInAllocation = Allocation.createFromBitmap(mRenderScript, mBitmaps[0]);
//        }
//        if (mExtraAllocation == null) {
//            mExtraAllocation = Allocation.createFromBitmap(mRenderScript, mBitmaps[1]);
//        }
//        Type.Builder typeBuilder = new Type.Builder(mRenderScript, Element.I32(mRenderScript));
//        typeBuilder.setX(1);
//        typeBuilder.setY(1);
//        Allocation resultAlloc = Allocation.createTyped(
//                mRenderScript, typeBuilder.create(), Allocation.USAGE_SCRIPT);
//        mScriptCFlip.set_resultAlloc(resultAlloc);
//        mScriptCFlip.invoke_process4(mInAllocation, mExtraAllocation);
//        int[] result = new int[1];
//        resultAlloc.copyTo(result);
//        Log.i(TAG, "processBitmap3: cost " + (System.currentTimeMillis() - start) + "  " + result[0]);
//        return result[0] == 0;
        Bitmap bitmap1 = Bitmap.createBitmap(1080, 2010, Bitmap.Config.ARGB_8888);
        Bitmap bitmap2 = Bitmap.createBitmap(1080, 2010, Bitmap.Config.ARGB_8888);
//        return bitmap1.sameAs(bitmap2);
        return compareBitmap2(bitmap1, bitmap2);
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
            rect.offset(0, 0);
            Bitmap result2 = decoder.decodeRegion(rect, regionOptions);
            result[0] = result1;
            result[1] = result2;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private void compareBitmap(Bitmap bitmap1, Bitmap bitmap2) {
        long start = System.currentTimeMillis();
        int height = bitmap1.getHeight();
        int startOffset = -1;
        for (int line2 = height - 1; line2 >= 0; line2 --) {
            int line1Top = height - line2 - 1;
            if (compareBitmapLine(bitmap1, height - 1, bitmap2, line2)
                    && compareBitmapLine(bitmap1, line1Top, bitmap2, 0)) {
                startOffset = line2;
                break;
            }
        }
        int result = height - startOffset - 1;
        Log.i(TAG, "compareBitmap: cost " + (System.currentTimeMillis() - start) + " " + result);
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
        for (int index = 0; index < width; index ++) {
            if ((pixel1[index]^pixel2[index]) != 0) {
                result = false;
                break;
            }
        }
        return result;
    }

    static {
        System.loadLibrary("native-lib");
    }
    public native int[] imgToGray(int[] pixels, int width, int height);
    public native int compareByte(byte[] pixels1, byte[] pixels2, int width, int height);

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
}