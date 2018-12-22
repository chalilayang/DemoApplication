package com.example.mi.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by MQCP on 2017/4/12.
 */

public class bezierView extends View {
    private Paint mPaint;
    private Path mPath;
    private final int LENGTH = 1000;
    private final int RADIUS = 30;
    private final int START=100;

    public bezierView(Context context) {
        super(context);
    }

    public bezierView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint();
        mPaint.setStrokeWidth(3);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setAntiAlias(true);
        mPaint.setTextSize(60);
        mPaint.setColor(Color.BLACK);
        initPath(LENGTH / 2 + START, START);
        nowX=LENGTH / 2 + START;
        nowY=START;
    }

    private void initPath(float cx, float cy) {
        mPath = new Path();
        mPath.moveTo(START, START);
        mPath.quadTo(cx, cy, LENGTH, LENGTH);

        Path temp = new Path();
        temp.moveTo(LENGTH, LENGTH);
        temp.quadTo(0, 0, START, START);
        mPath.addPath(temp);
    }



    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawLine(START, START, START, LENGTH, mPaint);
        canvas.drawLine(START, LENGTH, LENGTH, LENGTH, mPaint);
        canvas.drawCircle(nowX, nowY, RADIUS, mPaint);
        canvas.drawPath(mPath, mPaint);
        canvas.drawText("控制点坐标:("+(int)nowX+","+(int)nowY+")",START,LENGTH+200,mPaint);
    }

    private float nowX;
    private float nowY;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                nowX = event.getX();
                nowY = event.getY();
                initPath(nowX, nowY);
                invalidate();
                break;
        }
        return true;
    }
}