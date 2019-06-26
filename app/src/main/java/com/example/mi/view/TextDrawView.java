package com.example.mi.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

public class TextDrawView extends TextView {
    private Paint mLinePaint;
    public TextDrawView(Context context) {
        this(context, null);
    }

    public TextDrawView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TextDrawView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLinePaint.setStrokeWidth(10);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        TextPaint paint = getPaint();
        if (paint != null) {
            Paint.FontMetrics metrics = paint.getFontMetrics();
            Log.i(TAG, "onDraw: "
                    + metrics.ascent + " " + metrics.bottom + " " + metrics.descent + " " + metrics.leading + " " + metrics.top);
            mLinePaint.setColor(Color.RED);
            canvas.drawLine(0, metrics.ascent, getWidth(), metrics.ascent, mLinePaint);
            mLinePaint.setColor(Color.YELLOW);
            canvas.drawLine(0, metrics.bottom, getWidth(), metrics.bottom, mLinePaint);
            mLinePaint.setColor(Color.BLUE);
            canvas.drawLine(0, metrics.descent, getWidth(), metrics.descent, mLinePaint);
            mLinePaint.setColor(Color.GREEN);
            canvas.drawLine(0, metrics.leading, getWidth(), metrics.leading, mLinePaint);
            mLinePaint.setColor(Color.LTGRAY);
            canvas.drawLine(0, metrics.top, getWidth(), metrics.top, mLinePaint);
        }
    }

    private static final String TAG = "TextDrawView";
}
