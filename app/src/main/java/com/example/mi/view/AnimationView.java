package com.example.mi.view;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Choreographer;
import android.view.TextureView;
import android.view.WindowManager;

import static com.example.mi.view.WirelessChargeCircleDrawer.WIRELESS_CIRCLE_RES_ARRAY;

public class AnimationView extends TextureView implements TextureView.SurfaceTextureListener {
    private static final int BASE_SCREEN_WIDTH = 1080;
    private int mViewWidth;
    private int mViewHeight;

    private WindowManager mWindowManager;
    private Point mScreenSize;
    private int mDrawableWidth;
    private int mDrawableHeight;

    private boolean mSurfaceAvailable;
    private boolean mPendingStartAnimation;
    private volatile boolean mAnimationRunning;
    private WirelessChargeCircleDrawer mCircleDrawer;

    public AnimationView(Context context) {
        this(context, null);
    }

    public AnimationView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AnimationView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        setOpaque(false);
        mSurfaceAvailable = false;
        mAnimationRunning = false;
        setSurfaceTextureListener(this);

        Drawable drawable = getResources().getDrawable(WIRELESS_CIRCLE_RES_ARRAY[0]);
        mDrawableWidth = drawable.getIntrinsicWidth();
        mDrawableHeight = drawable.getIntrinsicHeight();

        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        mScreenSize = new Point();
        mWindowManager.getDefaultDisplay().getRealSize(mScreenSize);
        updateSizeForScreenSizeChange();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        checkScreenSize();
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        checkScreenSize();
    }

    private void checkScreenSize() {
        Point point = new Point();
        mWindowManager.getDefaultDisplay().getRealSize(point);
        if (!mScreenSize.equals(point.x, point.y)) {
            mScreenSize.set(point.x, point.y);
            updateSizeForScreenSizeChange();
            requestLayout();
        }
    }

    private void updateSizeForScreenSizeChange() {
        int screenWidth = Math.min(mScreenSize.x, mScreenSize.y);
        float rateWidth = screenWidth * 1.0f / BASE_SCREEN_WIDTH;
        mViewWidth = (int) (rateWidth * mDrawableWidth);
        mViewHeight = (int) (rateWidth * mDrawableHeight);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(mViewWidth, mViewHeight);
    }

    public void startAnimation() {
        if (mAnimationRunning) {
            return;
        }
        if (mSurfaceAvailable) {
            mAnimationRunning = true;
            mCircleDrawer = new WirelessChargeCircleDrawer(getContext());
            mCircleDrawer.startAnimation();
            Choreographer.getInstance().postFrameCallback(mFrameCallback);
        } else {
            mPendingStartAnimation = true;
        }
    }

    public void stopAnimation() {
        mAnimationRunning = false;
        mPendingStartAnimation = false;
        Choreographer.getInstance().removeFrameCallback(mFrameCallback);
        if (mCircleDrawer != null) {
            mCircleDrawer.release();
            mCircleDrawer = null;
        }
    }

    private Choreographer.FrameCallback mFrameCallback = new Choreographer.FrameCallback() {
        @Override
        public void doFrame(long frameTimeNanos) {
            if (mAnimationRunning) {
                dispatchDraw(frameTimeNanos);
                Choreographer.getInstance().postFrameCallback(this);
            }
        }
    };

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        mSurfaceAvailable = true;
        if (mPendingStartAnimation) {
            startAnimation();
            mPendingStartAnimation = false;
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {}

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {}

    private void dispatchDraw(long frameTime) {
        if (mSurfaceAvailable && mCircleDrawer != null) {
            mCircleDrawer.onAnimationDraw(this, frameTime);
        }
    }

    private void unlockCanvasAndPostSafely(Canvas canvas) {
        if (mSurfaceAvailable) {
            unlockCanvasAndPost(canvas);
        }
    }

    public interface AnimationDrawer {
        void onAnimationDraw(TextureView textureView, long frameTime);
        default void release() {}
        default void setAnimationListener(AnimationStateListener listener) {}
        interface AnimationStateListener {
            default void onAnimationStart() {}
            default void onAnimationEnd() {}
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopAnimation();
    }
}
