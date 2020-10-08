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
                Bitmap flipBitmap = flipBitmap();
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
                    mRenderScript, mInAllocation.getType(),Allocation.USAGE_SCRIPT);
        }
        if (mExtraAllocation == null) {
            mExtraAllocation = Allocation.createFromBitmapResource(mRenderScript, getResources(), R.drawable.wechat2);
        }

        mScriptCFlip.set_gIn(mExtraAllocation);
        mScriptCFlip.set_imageHeight(height);
        mScriptCFlip.set_imageWidth(width);
        mScriptCFlip.forEach_flip(mInAllocation, mOutAllocation);
        mOutAllocation.copyTo(outBitmap);
        return outBitmap;
    }
}