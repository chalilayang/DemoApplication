package com.example.mi.view.notification;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.view.animation.Interpolator;

import com.example.mi.demoapplication.R;
import com.example.mi.view.AnimationView;

public class RedWaveDrawer implements AnimationView.AnimationDrawer {
    public static final long DURATION = 3100;
    private Paint mPaint;
    private Drawable mShaderDrawable;
    private long mLastFrameTime;
    private Interpolator mInterpolator;
    private int mDrawableWidth;
    private boolean mRepeat = false;

    public RedWaveDrawer(Context context) {
        mLastFrameTime = -1;
        mInterpolator = new CustomInterpolator();
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.STROKE);
        mShaderDrawable = context.getDrawable(R.drawable.slide_red_shader);
        mDrawableWidth = mShaderDrawable.getIntrinsicWidth();
    }

    @Override
    public void setRepeatMode(boolean repeat) {
        mRepeat = repeat;
    }

    @Override
    public boolean onAnimationDraw(Canvas canvas, long frameTime) {
        float progress;
        if (mLastFrameTime  == -1) {
            mLastFrameTime = frameTime;
            progress = 0.0f;
        } else {
            float elapse = (frameTime - mLastFrameTime) / 1000000.0f;
            progress = elapse * 1.0f / DURATION;
        }
        progress = Math.max(0, Math.min(1, progress));
        drawWaveLine(canvas, mInterpolator.getInterpolation(progress));
        if (progress >= 1.0f && mRepeat) {
            mLastFrameTime = -1;
        }
        return mRepeat || progress < 1.0f;
    }

    protected void drawWaveLine(Canvas canvas, float progress) {
        int height = canvas.getHeight();
        int width = canvas.getWidth();
        canvas.save();
        float top = (0.2f - progress) * height / 2.0f;
        float bottom = height - top;
        float right = (0.2f + progress) * mDrawableWidth;
        mShaderDrawable.setBounds(0, (int) top, (int) right, (int) bottom);
        int alpha = (int) (255 * progress);
        mShaderDrawable.setAlpha(alpha);
        mShaderDrawable.draw(canvas);
        canvas.translate(width, 0);
        canvas.scale(-1, 1);
        mShaderDrawable.draw(canvas);
        canvas.restore();
    }

    @Override
    public void release() {}

    @Override
    public void setAnimationListener(AnimationStateListener listener) {}

    public class CustomInterpolator implements Interpolator {
        private float rate = 0.03f;
        public float getInterpolation(float input) {
            float result;
            float value;
            if (input < rate) {
                value = input / rate;
                result = -((value -= 1) * value * value * value - 1);
            } else {
                value = (input - rate) / (1 - rate);
                result = 1+((value -= 1) * value * value * value - 1);
            }
            result = Math.max(0, Math.min(1, result));
            return result;
        }
    }
}
