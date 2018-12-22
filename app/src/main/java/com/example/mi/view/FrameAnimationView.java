package com.example.mi.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import static com.example.mi.view.SurfaceFrameAnimation.MODE_ONCE;
import static com.example.mi.view.SurfaceFrameAnimation.RES_DRAW_TRANSPARENT;

public class FrameAnimationView
        extends SurfaceView implements SurfaceHolder.Callback, SurfaceFrameAnimation.FrameAnimationListener {
    private static final String TAG = "FrameAnimationView";
    private SurfaceFrameAnimation frameAnimation;
    private String mResNamePre;
    private int mFrameCount;
    private int mFrameInterval;
    private int[] resArray;
    private int drawableWidth;
    private int drawableHeight;
    private boolean pendingStartAnimation;
    private boolean surfaceReady;
    public FrameAnimationView(Context context, int frameCount, int interval, String preName) {
        this(context, frameCount, interval, preName, frameCount * interval);
    }

    public FrameAnimationView(Context context, int frameCount, int interval, String preName, int duration) {
        super(context);
        Log.d(TAG, "FrameAnimationView() called with: context = [" + context
                + "], frameCount = [" + frameCount
                + "], interval = [" + interval + "], preName = ["
                + preName + "], duration = [" + duration + "]");
        mFrameCount = frameCount;
        mFrameInterval = interval;
        mResNamePre = preName;
        int size = duration / mFrameInterval;
        resArray = new int[size];
        init(context);
    }

    private void init(Context context) {
        pendingStartAnimation = false;
        surfaceReady = false;
        frameAnimation = new SurfaceFrameAnimation(this, this);
        frameAnimation.setSupportInBitmap(true);
        frameAnimation.setMode(MODE_ONCE);
        frameAnimation.setFrameInterval(mFrameInterval);
        String packageName = context.getPackageName();
        for (int index = 0; index < resArray.length; index ++) {
            int id = index % mFrameCount;
            int redId = getResources().getIdentifier(
                    mResNamePre + id, "drawable", packageName);
            resArray[index] = redId;
        }
        Drawable drawable = getResources().getDrawable(resArray[0]);
        drawableWidth = drawable.getIntrinsicWidth();
        drawableHeight = drawable.getIntrinsicHeight();
        Log.i(TAG, "init: " + drawableWidth + " " + drawableHeight);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.i(TAG, "surfaceCreated: ");
        surfaceReady = true;
        if (pendingStartAnimation) {
            startAnimation();
            pendingStartAnimation = false;
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.i(TAG, "surfaceDestroyed: ");
        surfaceReady = false;
    }

    @Override
    public void onStart() {
        if (callback != null) {
            callback.onStart();
        }
    }

    @Override
    public void onInterrupt() {
        if (callback != null) {
            callback.onInterrupt();
        }
    }

    @Override
    public void onFinish() {
        if (callback != null) {
            callback.onFinish();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(drawableWidth, drawableHeight);
    }

    public void startAnimation() {
        if (isRunning()) {
            return;
        }
        if (surfaceReady) {
            frameAnimation.startAnimation(
                    resArray, 0, RES_DRAW_TRANSPARENT, 0, this, alphaDismissDraw);
            pendingStartAnimation = false;
        } else {
            pendingStartAnimation = true;
        }
    }

    public void stopAnimation() {
        frameAnimation.stopAnimation();
    }

    public boolean isRunning() {
        return frameAnimation.isAnimating();
    }

    private SurfaceFrameAnimation.FrameAnimationListener callback;
    public void setAnimationCallback(SurfaceFrameAnimation.FrameAnimationListener listener) {
        callback = listener;
    }

    @Override
    public void setAlpha(float alpha) {
        super.setAlpha(alpha);
        frameAnimation.setAlpha(alpha);
    }

    private AlphaDismissDraw alphaDismissDraw = new AlphaDismissDraw();
    private class AlphaDismissDraw implements SurfaceFrameAnimation.CustomerDrawBitmap {

        private final Paint paint = new Paint();
        @Override
        public void drawBitmap(Canvas canvas, Bitmap bitmap, Matrix matrix) {
            paint.setAlpha((int)(255 * getAlpha()));
            Log.i(TAG, "drawBitmap: " + getAlpha());
            canvas.drawBitmap(bitmap, matrix, paint);
        }
    }
}
