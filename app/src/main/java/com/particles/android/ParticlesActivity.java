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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.example.mi.demoapplication.R;
import com.example.mi.view.GlTextureView;

import butterknife.ButterKnife;

import static android.opengl.GLSurfaceView.RENDERMODE_CONTINUOUSLY;

public class ParticlesActivity extends Activity {
    private static final String TAG = "ParticlesActivity";
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
            final TextureRenderer particlesRenderer2 = new TextureRenderer(this);
            glTextureView.setEGLContextClientVersion(2);
            glTextureView.setEGLConfigChooser(8, 8, 8, 8, 0, 0);
            glTextureView.setRenderer(particlesRenderer2);
            glTextureView.setRenderMode(RENDERMODE_CONTINUOUSLY);
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setColor(Color.BLUE);
            paint.setStrokeWidth(10f);
            new Handler().postDelayed(() -> {
                Canvas canvas = particlesRenderer2.lockCanvas(null);
                Rect rect = canvas.getClipBounds();
                Log.i(TAG, "lockCanvas: " + rect.toString() + " " + canvas.getWidth());
                Drawable bitmap = getDrawable(R.drawable.rapid_charge_circle_75);
                bitmap.setBounds(rect);
                bitmap.draw(canvas);
                canvas.drawRect(0, 0, 50, 50, paint);
                particlesRenderer2.unlockCanvasAndPost(canvas);
            }, 1000);
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