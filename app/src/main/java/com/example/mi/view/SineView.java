package com.example.mi.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Interpolator;

import com.example.mi.demoapplication.R;

import java.util.ArrayList;
import java.util.List;

import static android.animation.ValueAnimator.INFINITE;

public class SineView extends View {
    private static final double PI_ANGLE = 2 * Math.PI;
    private static final int LINE_COUNT = 8;
    private static final int LINE_WIDTH = 4;
    private static final int MAX_LINE_MARGIN = 9;
    private static final int MAX_AMP = (MAX_LINE_MARGIN / 2) * (LINE_COUNT);
    private Paint mPaint;
    private List<LineData> mLineList;
    private Drawable mShaderDrawable;
    private LinearGradient mLinearGradient;

    public SineView(Context context) {
        this(context, null);
    }

    public SineView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SineView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(LINE_WIDTH);
        mLineList = new ArrayList<>(LINE_COUNT);
        for (int index = 0; index < LINE_COUNT; index ++) {
            mLineList.add(new LineData(new Path(), new Path(), 0));
        }
        mShaderDrawable = context.getDrawable(R.drawable.slide_blue_shader);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int height = getHeight();
        int width = getWidth();
        canvas.save();
        mShaderDrawable.setBounds((int) ((1 - mProgress * 0.6) * width), 0, width, height);
        mShaderDrawable.draw(canvas);
        canvas.rotate(180, width / 2, height / 2);
        mShaderDrawable.draw(canvas);
        canvas.restore();
        if (mLinearGradient == null) {
            mLinearGradient = new LinearGradient(0, 0, 0, height,
                    new int[]{Color.parseColor("#153E9E"), Color.WHITE, Color.parseColor("#153E9E")}, null,
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

        float rate = mProgress;
        float amplitude = MAX_AMP * rate;
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

        for (int i = 0, count = mLineList.size(); i < count; i ++) {
            LineData data = mLineList.get(i);
            mPaint.setAlpha(getAlphaByAmp(data.mAmp));
            data.mLeftPath.lineTo(0, height);
            canvas.drawPath(data.mLeftPath, mPaint);
            data.mRightPath.lineTo(width, height);
            canvas.drawPath(data.mRightPath, mPaint);
        }
    }

    private float mProgress = 1;

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        startAnimation();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        animator.cancel();
    }

    ValueAnimator animator;
    public void startAnimation() {
        animator = ValueAnimator.ofFloat(0.0f, 1.0f);
        animator.setDuration(1200);
        animator.setInterpolator(new MyIn());
        animator.setRepeatCount(INFINITE);
        animator.setRepeatMode(ValueAnimator.RESTART);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mProgress = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        animator.start();
    }

    public class MyIn implements Interpolator {
        public float rate = 0.3f;
        public float getInterpolation(float input) {
            if (input < rate) {
                return (1.0f - (1.0f - input / rate) * (1.0f - input / rate));
            } else {
                float temp = 1 - rate;
                return (1 - input) / temp;
            }
        }
    }

    class LineData {
        public final Path mLeftPath;
        public final Path mRightPath;
        public float mAmp;
        public LineData(Path leftPath, Path rightPath, int amp) {
            mLeftPath = leftPath;
            mRightPath = rightPath;
            mAmp = amp;
        }
    }

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
}
