package com.render.demo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.example.mi.view.GlTextureView;

/**
 * Created by chalilayang on 20-8-11 下午9:41.
 **/
public class RenderFrameLayout extends FrameLayout {
    private final GlTextureView mRenderView;
    private final ViewRenderer mViewRenderer;

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
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        int count = getChildCount();
        if (count >= 2) {
            throw new RuntimeException("only one child view");
        }
        super.addView(child, index, params);
    }

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        if (child.equals(mRenderView)) {
            return super.drawChild(canvas, child, drawingTime);
        } else {
            if (mViewRenderer.isAvailable()) {
                Canvas surfaceCanvas = null;
                try {
                    surfaceCanvas = mViewRenderer.lockCanvas(true);
                    surfaceCanvas.drawColor(Color.BLUE);
                    super.drawChild(surfaceCanvas, child, drawingTime);
                } catch (Surface.OutOfResourcesException e) {
                    e.printStackTrace();
                } finally {
                    if (surfaceCanvas != null) {
                        mViewRenderer.unlockCanvasAndPost(surfaceCanvas);
                    }
                }
            }
            invalidate();
            return true;
        }
    }
}
