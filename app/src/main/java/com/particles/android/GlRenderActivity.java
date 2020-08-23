package com.particles.android;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.Display;
import android.widget.FrameLayout;

import com.example.mi.demoapplication.R;
import com.example.mi.view.GlTextureView;
import com.render.demo.GLProgressBar;
import com.render.demo.ViewRenderer;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

public class GlRenderActivity extends AppCompatActivity {

    private FrameLayout root;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gl_render);
        root = findViewById(R.id.root);
        addView();
    }

    private void addView() {
        Display mDisplay = getWindowManager().getDefaultDisplay();
        GLProgressBar glProgressBar = new GLProgressBar(this);

        ViewRenderer renderer = new ViewRenderer(getApplicationContext(), glProgressBar, mDisplay);
        GlTextureView glSurfaceView = new GlTextureView(getApplicationContext());
        glSurfaceView.setEGLContextClientVersion(2);
        glSurfaceView.setRenderer(renderer);
        glSurfaceView.setLayoutParams(new FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
        root.addView(glSurfaceView);
        root.addView(glProgressBar);
    }
}