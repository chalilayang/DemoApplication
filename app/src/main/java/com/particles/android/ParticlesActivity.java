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
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

import com.example.mi.demoapplication.R;
import com.example.mi.view.GlTextureView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ParticlesActivity extends Activity {
    //    @BindView(R.id.gl_surface_view)
    GLSurfaceView glSurfaceView;
    //    @BindView(R.id.gl_texture_view)
    GlTextureView glTextureView;
    private boolean rendererSet = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.gl_layout);
        ButterKnife.bind(this);

        glSurfaceView = findViewById(R.id.gl_surface_view);
        glTextureView = findViewById(R.id.gl_texture_view);

        if (glSurfaceView != null) {
            final ParticlesRenderer particlesRenderer = new ParticlesRenderer(this);
            glSurfaceView.setEGLContextClientVersion(2);
            glSurfaceView.setZOrderOnTop(true);
            glSurfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
            glSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 0, 0);
            glSurfaceView.setRenderer(particlesRenderer);
        }

        if (glTextureView != null) {
            final ParticlesRenderer particlesRenderer2 = new ParticlesRenderer(this);
            glTextureView.setEGLContextClientVersion(2);
            glTextureView.setEGLConfigChooser(8, 8, 8, 8, 8, 0);
            glTextureView.setRenderer(particlesRenderer2);
        }

        rendererSet = true;
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (rendererSet) {
            if (glSurfaceView != null) {
                glSurfaceView.onPause();
            }
            if (glTextureView != null) {
                glTextureView.onPause();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (rendererSet) {
            if (glSurfaceView != null) {
                glSurfaceView.onResume();
            }
            if (glTextureView != null) {
                glTextureView.onResume();
            }
        }
    }
}