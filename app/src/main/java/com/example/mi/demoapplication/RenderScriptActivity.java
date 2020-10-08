package com.example.mi.demoapplication;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.renderscript.Allocation;
import android.renderscript.RenderScript;
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

    public Bitmap flipBitmap(Bitmap bitmap) {
        Bitmap outBitmap = Bitmap.createBitmap(bitmap);
        Allocation inAllocation = Allocation.createFromBitmap(mRenderScript, bitmap);
        Allocation outAllocation = Allocation.createFromBitmap(mRenderScript, outBitmap);

        mScriptCFlip.invoke_flip_setup(inAllocation, outAllocation, 0);
        mScriptCFlip.forEach_flip(inAllocation, outAllocation);
        outAllocation.copyTo(outBitmap);

        inAllocation.destroy();
        outAllocation.destroy();
        return outBitmap;
    }
}