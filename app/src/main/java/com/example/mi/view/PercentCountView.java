package com.example.mi.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.DecimalFormat;

public class PercentCountView extends LinearLayout {
    private NumberDrawView integerTv;
    private NumberDrawView fractionTv;
    private float currentProgress;
    private DecimalFormat decimalFormat;
    public PercentCountView(Context context) {
        this(context, null);
    }

    public PercentCountView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PercentCountView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        LayoutParams llp = new LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        currentProgress = 0f;
        decimalFormat = new DecimalFormat(".00");
        setOrientation(LinearLayout.HORIZONTAL);
        setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        integerTv = new NumberDrawView(context);
        integerTv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 57.8f);
        integerTv.setTextColor(Color.parseColor("#FFFFFF"));
        integerTv.setTypeface(
                Typeface.createFromAsset(context.getAssets(), "fonts/Mitype2018-20.otf")
        );
        addView(integerTv, llp);

        fractionTv = new NumberDrawView(context);
        fractionTv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 22.39f);
        fractionTv.setTextColor(Color.parseColor("#FFFFFF"));
        fractionTv.setTypeface(
                Typeface.createFromAsset(context.getAssets(), "fonts/Mitype2018-20.otf")
        );
        addView(fractionTv, llp);
        setProgress(0);
    }

    public void setProgress(float progress) {
        if (progress < 0.0f || progress > 100.0f) {
            return;
        }
        currentProgress = progress;
        int integerValue = (int) currentProgress;
//        float fractionValue = (int)((currentProgress - integerValue) * 100f) / 100f;
        integerTv.setText(String.valueOf(integerValue));
        fractionTv.setText("%");
    }
}
