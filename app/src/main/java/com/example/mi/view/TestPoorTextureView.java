package com.example.mi.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;
import android.view.TextureView;

public class TestPoorTextureView extends TextureView implements TextureView.SurfaceTextureListener {
    private Paint mPaint;
    private int mOffsetY;
    public TestPoorTextureView(Context context) {
        this(context, null);
    }

    public TestPoorTextureView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TestPoorTextureView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.BLUE);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
    }

    public void setColor(int color) {
        mPaint.setColor(color);
        removeCallbacks(drawRunnable);
        post(drawRunnable);
    }

    public void setmOffsetY(int offsetY) {
        mOffsetY = offsetY;
        removeCallbacks(drawRunnable);
        post(drawRunnable);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        removeCallbacks(drawRunnable);
        post(drawRunnable);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        removeCallbacks(drawRunnable);
        post(drawRunnable);
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }

    private Runnable drawRunnable = new Runnable() {
        @Override
        public void run() {
            Canvas canvas = lockCanvas();
            if (canvas != null) {
                try {
                    int width = canvas.getWidth();
                    int height = canvas.getHeight();
                    canvas.drawRect(0,  mOffsetY, width, height/2 + mOffsetY, mPaint);
                } finally {
                    unlockCanvasAndPost(canvas);
                }
            }
        }
    };
}
