package com.example.mi.screenshot;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.FrameLayout;

/**
 * Created by chalilayang on 20-11-13 下午5:35.
 **/
public class FrameContainer2 extends FrameLayout {
    private static final String TAG = "FrameContainer2";
    public FrameContainer2(Context context) {
        this(context, null);
    }

    public FrameContainer2(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FrameContainer2(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        Log.i(TAG, "dispatchTouchEvent: " + ev.getAction() + " " + ev.getRawX() + " " + ev.getRawY());
        return super.dispatchTouchEvent(ev);
    }
}
