package com.example.mi.view.notification;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.view.animation.Interpolator;

import com.example.mi.demoapplication.R;
import com.example.mi.view.AnimationView;

import java.util.ArrayList;
import java.util.List;

public class WaveLineDrawer implements AnimationView.AnimationDrawer {
    public static final long DURATION = 1200;
    public static final long DURATION_2 = 1560;
    public static final int REPEAT_COUNT = 5;
    private static final double PI_ANGLE = 2 * Math.PI;
    private static final int LINE_COUNT = 8;
    private static final int LINE_WIDTH = 4;
    private static final int MAX_LINE_MARGIN = 9;
    private static final int MAX_AMP = (MAX_LINE_MARGIN / 2) * (LINE_COUNT);
    private Paint mPaint;
    private List<LineData> mLineList;
    private Drawable mShaderDrawable;
    private LinearGradient mLinearGradient;
    private long mLastFrameTime;
    private Interpolator mInterpolator;
    private boolean mRepeat = false;
    private int mRepeatCount;

    private long mDuration = DURATION;
    private boolean mUseExtraInterpltor = false;

    public WaveLineDrawer(Context context) {
        mLastFrameTime = -1;
        mInterpolator = new CustomInterpolator();
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(LINE_WIDTH);
        mLineList = new ArrayList<>(LINE_COUNT);
        for (int index = 0; index < LINE_COUNT; index ++) {
            mLineList.add(new LineData(new Path(), new Path(), 0));
        }
        mShaderDrawable = context.getDrawable(R.drawable.slide_blue_shader);
    }

    public void useCustomInterpolator2() {
        mUseExtraInterpltor = true;
        mDuration = DURATION_2;
        mInterpolator = new CustomInterpolator2();
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
            progress = elapse * 1.0f / mDuration;
        }
        progress = Math.max(0, Math.min(1, progress));
        drawWaveLine(canvas, mInterpolator.getInterpolation(progress));
        if (progress >= 1.0f && mRepeat && mRepeatCount < REPEAT_COUNT) {
            mRepeatCount ++;
            mLastFrameTime = -1;
        }
        return (mRepeat && mRepeatCount < REPEAT_COUNT) || progress < 1.0f;
    }

    protected void drawWaveLine(Canvas canvas, float progress) {
        int height = canvas.getHeight();
        int width = canvas.getWidth();

        canvas.save();
        mShaderDrawable.setBounds((int) ((1 - progress * 0.6) * width), 0, width, height);
        mShaderDrawable.draw(canvas);
        canvas.rotate(180, width / 2.0f, height / 2.0f);
        mShaderDrawable.draw(canvas);
        canvas.restore();

        if (mLinearGradient == null) {
            mLinearGradient = new LinearGradient(0, 0, 0, height,
                    new int[]{Color.parseColor("#153E9E"),
                            Color.WHITE, Color.parseColor("#153E9E")}, null,
                    Shader.TileMode.REPEAT);
            mPaint.setShader(mLinearGradient);
        }
        int startY = 0;
        for (LineData lineData : mLineList) {
            lineData.mLeftPath.reset();
            lineData.mLeftPath.moveTo(0, 0);
            lineData.mLeftPath.lineTo(0, startY);
            lineData.mRightPath.reset();
            lineData.mRightPath.moveTo(width, 0);
            lineData.mRightPath.lineTo(width, startY);
        }

        float amplitude = MAX_AMP * progress;
        int index = 0;
        int distance = height - 2 * startY;
        while (index <= height - 2 * startY) {
            float angle = (float) (index * PI_ANGLE / distance);
            float sineAngle = 1 - (float) (Math.cos(angle));
            for (int i = 0, count = mLineList.size(); i < count; i ++) {
                float amplitudeTemp = amplitude - i * (MAX_LINE_MARGIN / 2.0f);
                amplitudeTemp = Math.max(0, amplitudeTemp);
                float endX = sineAngle * amplitudeTemp;
                mLineList.get(i).mAmp = amplitudeTemp;
                mLineList.get(i).mLeftPath.lineTo(endX, index + startY);
                mLineList.get(i).mRightPath.lineTo(width - endX, index + startY);
            }
            index++;
        }

        for (int i = 0, count = mLineList.size(); i < count; i++) {
            LineData data = mLineList.get(i);
            if (progress <= 0.0f) {
                mPaint.setAlpha(0);
            } else {
                mPaint.setAlpha(getAlphaByAmp(data.mAmp));
            }
            data.mLeftPath.lineTo(0, height);
            canvas.drawPath(data.mLeftPath, mPaint);
            data.mRightPath.lineTo(width, height);
            canvas.drawPath(data.mRightPath, mPaint);
        }
    }

    @Override
    public void release() {
        mLineList.clear();
    }

    @Override
    public void setAnimationListener(AnimationStateListener listener) {}

    public int getAlphaByAmp(float amp) {
        float endAmp = MAX_LINE_MARGIN / 2.0f * 5;
        int middleAlpha = 50;
        if (amp > endAmp) {
            float zeroAmp = MAX_LINE_MARGIN / 2.0f * 6;
            float rate = (zeroAmp - amp) * 1.0f / (zeroAmp - endAmp);
            rate = Math.max(0, rate);
            return (int) (rate * middleAlpha);
        } else {
            float endAlpha = middleAlpha;
            float rate = amp / endAmp;
            int result = (int) (255 - (255 - endAlpha) * rate);
            return Math.min(255, Math.max(result, 0));
        }
    }

    public class CustomInterpolator implements Interpolator {
        public float rate = 0.3f;
        public float getInterpolation(float input) {
            float result;
            if (input < rate) {
                result = (1.0f - (1.0f - input / rate) * (1.0f - input / rate));
            } else {
                float temp = 1 - rate;
                result = (1 - input) / temp;
            }
            result = Math.max(0, Math.min(1, result));
            return result;
        }
    }

    public class CustomInterpolator2 implements Interpolator {
        public float rate = 0.23f;
        public float rate2 = 0.77f;
        public float getInterpolation(float input) {
            float result;
            if (input < rate) {
                result = (1.0f - (1.0f - input / rate) * (1.0f - input / rate));
            } else if (input < rate2){
                float temp = 1 - rate;
                result = (1 - input) / temp;
            } else {
                float inputTemp = (input - rate2) * rate /(1 - rate2);
                result = (1.0f - (1.0f - inputTemp / rate) * (1.0f - inputTemp / rate));
            }
            result = Math.max(0, Math.min(1, result));
            return result;
        }
    }

    static class LineData {
        public final Path mLeftPath;
        public final Path mRightPath;
        public float mAmp;
        public LineData(Path leftPath, Path rightPath, int amp) {
            mLeftPath = leftPath;
            mRightPath = rightPath;
            mAmp = amp;
        }
    }
}
