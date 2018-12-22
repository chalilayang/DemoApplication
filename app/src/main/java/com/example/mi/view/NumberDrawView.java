package com.example.mi.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

public class NumberDrawView extends View {
    private static final String TAG = "TextDrawView";
    private TextPaint linePaint;
    private Paint.FontMetrics fontMetrics;
    private String content = "1234567890";
    public NumberDrawView(Context context) {
        this(context, null);
    }

    public NumberDrawView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NumberDrawView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        linePaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(Color.WHITE);
        Typeface typeface
                = Typeface.createFromAsset(context.getAssets(), "fonts/Mitype2018-20.otf");
        if (typeface != null) {
            linePaint.setTypeface(typeface);
        }
        fontMetrics = linePaint.getFontMetrics();
    }

    public void setTextSize(int unit, float size) {
        Context c = getContext();
        Resources r;
        if (c == null) {
            r = Resources.getSystem();
        } else {
            r = c.getResources();
        }
        float textSize = TypedValue.applyDimension(unit, size, r.getDisplayMetrics());
        if (textSize != linePaint.getTextSize()) {
            linePaint.setTextSize(textSize);
            fontMetrics = linePaint.getFontMetrics();
            requestLayout();
        }
    }

    public void setTypeface(Typeface typeFace) {
        if (typeFace != null) {
            linePaint.setTypeface(typeFace);
            requestLayout();
        }
    }

    public void setTextColor(int color) {
        linePaint.setColor(color);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawText(content, 0, Math.abs(fontMetrics.top), linePaint);
    }

    public void setText(String text) {
        if (TextUtils.isEmpty(text)) {
            content = "";
            return;
        }
        if (text.equals(content)) {
            return;
        }
        content = text;
        requestLayout();
    }

    private int getStringLength() {
        if (TextUtils.isEmpty(content)) {
            return 0;
        }
        return (int) (linePaint.measureText(content) + 0.5f);
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = getStringLength();
        int height = (int) (Math.abs(fontMetrics.top) + 1f);
        setMeasuredDimension(width, height);
    }
}
