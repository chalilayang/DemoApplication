package com.example.mi.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import static com.example.mi.view.SurfaceFrameAnimation.MODE_ONCE;

public class ChargeDotView
        extends SurfaceView implements SurfaceHolder.Callback, SurfaceFrameAnimation.FrameAnimationListener {
    private static final String TAG = "ChargeDotView";
    private SurfaceFrameAnimation frameAnimation;
    private static final String RES_ID_PRE = "rapid_charge_dot_";
    private static final int FRAME_COUNT = 81;
    private static final int FRAME_INTERVAL = 40;
    private static final int ANIMATION_DURATION = FRAME_INTERVAL * FRAME_COUNT;
    private final int[] resArray = new int[FRAME_COUNT];
    private int drawableWidth;
    private int drawableHeight;
    private boolean pendingStartAnimation;
    private boolean surfaceReady;
    public ChargeDotView(Context context) {
        this(context, null);
    }

    public ChargeDotView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ChargeDotView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        pendingStartAnimation = false;
        surfaceReady = false;
        frameAnimation = new SurfaceFrameAnimation(this, this);
        frameAnimation.setSupportInBitmap(true);
        frameAnimation.setMode(MODE_ONCE);
        frameAnimation.setFrameInterval(FRAME_INTERVAL);
        String packageName = context.getPackageName();
        for (int index = 0; index < FRAME_COUNT; index ++) {
            int redId = getResources().getIdentifier(
                    RES_ID_PRE + index, "drawable", packageName);
            resArray[index] = redId;
        }
        Drawable drawable = getResources().getDrawable(resArray[0]);
        drawableWidth = drawable.getIntrinsicWidth();
        drawableHeight = drawable.getIntrinsicHeight();
        Log.i(TAG, "init: " + drawableWidth + " " + drawableHeight);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
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
        surfaceReady = false;
    }

    @Override
    public void onStart() {

    }

    @Override
    public void onInterrupt() {

    }

    @Override
    public void onFinish() {

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(
                MeasureSpec.makeMeasureSpec(drawableWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(drawableHeight, MeasureSpec.EXACTLY));
    }

    public void startAnimation() {
        if (surfaceReady) {
            frameAnimation.startAnimation(resArray, this);
            pendingStartAnimation = false;
        } else {
            pendingStartAnimation = true;
        }
    }
}
