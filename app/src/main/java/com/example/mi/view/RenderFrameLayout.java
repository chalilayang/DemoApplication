package com.example.mi.view;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.render.demo.ViewRenderer;

/**
 * Created by chalilayang on 20-8-11 下午9:41.
 **/
public class RenderFrameLayout extends FrameLayout {
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
        setWillNotDraw(true);
//        mViewRenderer = new ViewRenderer(context, )
        mRenderView = new GlTextureView(context, attrs, defStyle);
        super.addView(mRenderView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        super.addView(child, index, params);
    }

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        if (mRenderView != child) {
//            Canvas canvas1 = mSurface.lockCanvas(null);
//            super.drawChild(canvas1, child, drawingTime);
//            mSurface.unlockCanvasAndPost(canvas1);
            return true;
        } else {
            return super.drawChild(canvas, child, drawingTime);
        }
    }
}
