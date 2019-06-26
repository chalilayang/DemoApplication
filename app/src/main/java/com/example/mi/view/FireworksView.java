package com.example.mi.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Choreographer;
import android.view.View;
import android.view.WindowManager;

import com.example.mi.FireworksManager;
import com.example.mi.demoapplication.R;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO: document your custom view class.
 */
public class FireworksView extends View {

    private static final String TAG = "FireworksView";
    private static final int TRACK_PAINT_WIDTH = 4;
    private static final int TRACK_WIDTH = 122;
    private static final float FRACTION = 0.12f;
    private static final float FRACTION_FIRE_DISMISS = 0.3f;
    private static final int FIRE_WIDTH = 15;
    private static final int FIRE_HEIGHT = 345;
    private static final int OUTER_TRACK_START_COLOR = Color.parseColor("#002F3A81");
    private static final int OUTER_TRACK_END_COLOR = Color.parseColor("#ff210672");
    private static final int OUTER_TRACK_MIDDLE_COLOR = Color.parseColor("#B42F3A81");

    private static final float SPEED_MOVE = 878.0f / 600;

    private final int mViewWidth = TRACK_WIDTH;
    private int mViewHeight;
    private Paint mTrackPaint;
    private long mLastTime;

    private Drawable mFireDrawable;
    private FireworksManager mFireworksManager;
    private List<PointF> mFireList;
    public FireworksView(Context context) {
        this(context, null);
    }

    public FireworksView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FireworksView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (manager != null) {
            Point point = new Point();
            manager.getDefaultDisplay().getRealSize(point);
            int screenHeight = point.y;
            mViewHeight = (int) (878.0f * screenHeight / 2340);
        } else {
            mViewHeight = 878;
        }

        mTrackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTrackPaint.setStyle(Paint.Style.STROKE);
        mTrackPaint.setStrokeWidth(TRACK_PAINT_WIDTH);
        LinearGradient mLinearGradient = new LinearGradient(0, 0, 0, mViewHeight,
                new int[] {OUTER_TRACK_START_COLOR, OUTER_TRACK_MIDDLE_COLOR, OUTER_TRACK_END_COLOR},
                new float[] {0, FRACTION, 1 }, Shader.TileMode.CLAMP);
        mTrackPaint.setShader(mLinearGradient);

        mFireworksManager = new FireworksManager(mViewHeight, SPEED_MOVE);
        mFireList = new ArrayList<>();
        mFireDrawable = context.getDrawable(R.drawable.fire_light);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawTrack(canvas);
        drawFireworks(canvas);
    }

    private void drawTrack(Canvas canvas) {
        float step = TRACK_WIDTH / 6.0f;
        for (int index = 1; index <= 5; index ++) {
            float startX = step * index;
            canvas.drawLine(startX, 0, startX, mViewHeight, mTrackPaint);
        }
    }

    private void drawFireworks(Canvas canvas) {
        if (mFireList == null) {
            return;
        }
        float step = TRACK_WIDTH / 6.0f;
        for (PointF pointF : mFireList) {
            float startX = (pointF.x + 1) * step;
            float startY = pointF.y;
            int left = (int) (startX - FIRE_WIDTH / 2);
            int top = (int) startY;
            int right = left + FIRE_WIDTH;
            int bottom = top + FIRE_HEIGHT;
            int alpha = evaluateAlpha(top, mViewHeight);
            mFireDrawable.setAlpha(alpha);
            mFireDrawable.setBounds(left, top, right, bottom);
            mFireDrawable.draw(canvas);
        }
    }

    private int evaluateAlpha(int top, int height) {
        float dismissPoint = height * FRACTION_FIRE_DISMISS;
        if (top > dismissPoint) {
            return 255;
        } else {
            return (int) (top * 255 / dismissPoint);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(mViewWidth, mViewHeight);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        Choreographer.getInstance().postFrameCallback(mFrameCallback);
        post(mFireRunnable);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeCallbacks(mFireRunnable);
        Choreographer.getInstance().removeFrameCallback(mFrameCallback);
    }

    private Choreographer.FrameCallback mFrameCallback = new Choreographer.FrameCallback() {
        @Override
        public void doFrame(long frameTimeNanos) {
            long elapseTime = (frameTimeNanos - mLastTime) / 1000000;
            mLastTime = frameTimeNanos;
            if (mFireworksManager != null) {
                mFireworksManager.freshPositions(mFireList, elapseTime);
                invalidate();
            }
            Choreographer.getInstance().postFrameCallback(this);
        }
    };

    private Runnable mFireRunnable = new Runnable() {
        @Override
        public void run() {
            if (mFireworksManager != null) {
                mFireworksManager.fire();
            }
            postDelayed(this, 120);
        }
    };
}
