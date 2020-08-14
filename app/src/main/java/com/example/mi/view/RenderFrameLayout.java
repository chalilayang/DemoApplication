package com.example.mi.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

/**
 * Created by chalilayang on 20-8-11 下午9:41.
 **/
public class RenderFrameLayout extends FrameLayout implements TextureView.SurfaceTextureListener {
    private ProxyRenderView mRenderView;
    private View mTargetView;
    private Surface mSurface;
    public RenderFrameLayout(Context context) {
        this(context, null);
    }

    public RenderFrameLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RenderFrameLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setWillNotDraw(true);
        mRenderView = new ProxyRenderView(context, attrs, defStyle);
        super.addView(mRenderView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mRenderView.setSurfaceTextureListener(this);
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        super.addView(child, index, params);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
    }

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        if (child instanceof TextView) {
            Canvas canvas1 = mSurface.lockCanvas(null);
            super.drawChild(canvas1, child, drawingTime);
            mSurface.unlockCanvasAndPost(canvas1);
            return true;
        } else {
            return super.drawChild(canvas, child, drawingTime);
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        mSurface = new Surface(surface);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {}

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        mSurface = null;
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {}
}
