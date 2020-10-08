package com.example.mi.demoapplication;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.renderscript.Allocation;
import android.renderscript.RenderScript;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.example.mi.ScriptC_flip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_render_script);
        ButterKnife.bind(this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                Bitmap srcBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.music);
                Bitmap flipBitmap = flipBitmap(srcBitmap);
                image1.setImageBitmap(flipBitmap);
            }
        });

        mRenderScript = RenderScript.create(getApplicationContext());
        mScriptCFlip = new ScriptC_flip(mRenderScript);
    }

    private Allocation mInAllocation;
    private Allocation mOutAllocation;

    public Bitmap flipBitmap(Bitmap bitmap) {
        Bitmap outBitmap = Bitmap.createBitmap(bitmap);
        if (mInAllocation == null) {
            mInAllocation = Allocation.createFromBitmap(mRenderScript, bitmap);
        } else {
            mInAllocation.copyFrom(bitmap);
        }
        if (mOutAllocation == null) {
            mOutAllocation = Allocation.createFromBitmap(mRenderScript, outBitmap);
        } else {
            mOutAllocation.copyFrom(bitmap);
        }

        mScriptCFlip.set_gIn(mInAllocation);
        mScriptCFlip.set_gOut(mOutAllocation);
        mScriptCFlip.set_imageHeight(bitmap.getHeight());
        mScriptCFlip.set_imageWidth(bitmap.getWidth());
//        mScriptCFlip.invoke_flip_setup(mInAllocation, mOutAllocation);
        mScriptCFlip.forEach_flip(mInAllocation, mOutAllocation);
        mOutAllocation.copyTo(outBitmap);

        Log.i(TAG, "flipBitmap: " + bitmap.getAllocationByteCount());
        return outBitmap;
    }
}