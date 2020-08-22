/***
 * Excerpted from "OpenGL ES for Android",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material, 
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose. 
 * Visit http://www.pragmaticprogrammer.com/titles/kbogla for more book information.
 ***/
package com.particles.android;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import com.example.mi.demoapplication.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ParticlesActivity extends Activity {
    @BindView(R.id.gl_surface_view)
    GLSurfaceView glSurfaceView;
    private boolean rendererSet = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.gl_layout);
        ButterKnife.bind(this);
        final ParticlesRenderer particlesRenderer = new ParticlesRenderer(this);
        glSurfaceView.setEGLContextClientVersion(2);
        // Assign our renderer.
        glSurfaceView.setZOrderOnTop(true);
        glSurfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        glSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 0, 0);
        glSurfaceView.setRenderer(particlesRenderer);
        rendererSet = true;
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (rendererSet) {
            glSurfaceView.onPause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (rendererSet) {
            glSurfaceView.onResume();
        }
    }
}