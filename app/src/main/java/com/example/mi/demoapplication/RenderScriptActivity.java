package com.example.mi.demoapplication;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Rect;
import android.os.Bundle;
import android.renderscript.Allocation;
import android.renderscript.RenderScript;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.example.mi.ScriptC_flip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.io.InputStream;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import butterknife.BindView;
import butterknife.ButterKnife;

public class RenderScriptActivity extends AppCompatActivity {

    private static final String TAG = "RenderScriptActivity";
    @BindView(R.id.image1)
    ImageView image1;
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
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                Bitmap flipBitmap = processBitmap2();
                image1.setImageBitmap(flipBitmap);
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
            Rect rect = new Rect(0, 0, width, height / 4);
            BitmapFactory.Options regionOptions = new BitmapFactory.Options();
            regionOptions.inPreferredConfig = Bitmap.Config.RGB_565;
            Bitmap result1 = decoder.decodeRegion(rect, regionOptions);
            rect.offset(0, 100);
            Bitmap result2 = decoder.decodeRegion(rect, regionOptions);
            result[0] = result1;
            result[1] = result2;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}