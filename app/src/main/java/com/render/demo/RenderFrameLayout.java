package com.render.demo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.example.mi.view.GlTextureView;

/**
 * Created by chalilayang on 20-8-11 下午9:41.
 **/
public class RenderFrameLayout extends FrameLayout {
    private static final String TAG = "RenderFrameLayout";
    private GlTextureView mRenderView;
    private ViewRenderer mViewRenderer;

    public RenderFrameLayout(Context context) {
        this(context, null);
    }

    public RenderFrameLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RenderFrameLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setWillNotDraw(false);
        mViewRenderer = new ViewRenderer(context);
        mRenderView = new GlTextureView(context);
        mRenderView.setEGLContextClientVersion(2);
        mRenderView.setRenderer(mViewRenderer);
        addView(mRenderView, new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        ProgressBar glProgressBar = new ProgressBar(context);
        addView(glProgressBar);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
    }

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        if (child.equals(mRenderView)) {
            return super.drawChild(canvas, child, drawingTime);
        } else {
            if (mViewRenderer.isAvailable()) {
                Canvas surfaceCanvas = null;
                boolean invalid = false;
                try {
                    surfaceCanvas = mViewRenderer.lockCanvas(true);
                    surfaceCanvas.drawColor(Color.BLUE);
                    invalid = super.drawChild(surfaceCanvas, child, drawingTime);
                } catch (Surface.OutOfResourcesException e) {
                    e.printStackTrace();
                } finally {
                    if (surfaceCanvas != null) {
                        mViewRenderer.unlockCanvasAndPost(surfaceCanvas);
                    }
                }
                Log.i(TAG, "drawChild: " + invalid);
            }
            invalidate();
            return false;
        }
    }
}
