package com.example.mi.view;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Choreographer;
import android.view.View;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AnimationView extends View {
    private static final String TAG = "AnimationView";
    private volatile boolean mAnimationRunning;
    private volatile long mFrameTime;
    private final List<AnimationDrawer> mDrawerList = new ArrayList<>();
    public AnimationView(Context context) {
        this(context, null);
    }

    public AnimationView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AnimationView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        mAnimationRunning = false;
    }

    private void startAnimation() {
        if (mAnimationRunning) {
            return;
        }
        mAnimationRunning = true;
        Choreographer.getInstance().postFrameCallback(frameCallback);
    }

    public void stopAnimation() {
        mAnimationRunning = false;
        Choreographer.getInstance().removeFrameCallback(frameCallback);
    }

    private Choreographer.FrameCallback frameCallback = new Choreographer.FrameCallback() {
        @Override
        public void doFrame(long frameTimeNanos) {
            dispatchDraw(frameTimeNanos);
            if (mAnimationRunning) {
                Choreographer.getInstance().postFrameCallback(this);
            }
        }
    };

    private void dispatchDraw(long frameTime) {
        if (!mDrawerList.isEmpty()) {
            mFrameTime = frameTime;
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Iterator<AnimationDrawer> iterator = mDrawerList.listIterator();
        while(iterator.hasNext()) {
            AnimationDrawer item = iterator.next();
            if (!item.onAnimationDraw(canvas, mFrameTime)) {
                item.release();
                iterator.remove();
            }
        }
        if (mDrawerList.isEmpty()) {
            stopAnimation();
        }
    }

    public boolean hasDrawer() {
        return !mDrawerList.isEmpty();
    }

    public void addAnimationDrawer(AnimationDrawer drawer) {
        if (drawer != null) {
            for (AnimationDrawer item : mDrawerList) {
                if (drawer.equals(item)) {
                    Log.e(TAG, "addAnimationDrawer: duplicate");
                    return;
                }
            }
            mDrawerList.add(drawer);
            if (!mAnimationRunning) {
                startAnimation();
            }
        }
    }

    public void removeAnimationDrawer(AnimationDrawer drawer) {
        if (drawer != null) {
            Iterator<AnimationDrawer> iterator = mDrawerList.listIterator();
            while(iterator.hasNext()) {
                AnimationDrawer item = iterator.next();
                if (drawer.equals(item)) {
                    item.release();
                    iterator.remove();
                }
            }
            if (mDrawerList.isEmpty()) {
                stopAnimation();
            }
        }
    }

    public interface AnimationDrawer {
        default void setRepeatMode(boolean repeate) {}
        boolean onAnimationDraw(Canvas canvas, long frameTime);
        default void release() {};
        default void setAnimationListener(AnimationStateListener listener) {}
        interface AnimationStateListener {
            default void onAnimationStart() {}
            default void onAnimationEnd() {}
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!mDrawerList.isEmpty()) {
            startAnimation();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        clearDrawer();
        stopAnimation();
    }

    public void clearDrawer() {
        if (!mDrawerList.isEmpty()) {
            Iterator<AnimationDrawer> iterator = mDrawerList.listIterator();
            while (iterator.hasNext()) {
                AnimationDrawer item = iterator.next();
                item.release();
                iterator.remove();
            }
        }
    }
}
