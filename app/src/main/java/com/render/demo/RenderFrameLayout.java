package com.render.demo;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.example.mi.view.GlTextureView;

/**
 * Created by chalilayang on 20-8-11 下午9:41.
 **/
public class RenderFrameLayout extends FrameLayout {
    private static final String TAG = "RenderFrameLayout";
    private GlTextureView mRenderView;
    private ViewRenderer mViewRenderer;
    GLProgressBar glProgressBar;

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
        glProgressBar = new GLProgressBar(context);
        glProgressBar.setViewRenderer(mViewRenderer);
        addView(glProgressBar);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        Log.i(TAG, "dispatchDraw: ");
        super.dispatchDraw(canvas);
    }
}
