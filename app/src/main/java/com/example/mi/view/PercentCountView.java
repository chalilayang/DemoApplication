package com.example.mi.view;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.mi.view.NumberDrawView;

import java.lang.ref.WeakReference;
import java.text.DecimalFormat;

public class PercentCountView extends LinearLayout {
    private static final String TAG = "PercentCountView";
    private static final float DEFAULT_SPEED = 100.0f / 60000;
    private static final int BASE_SCREEN_WIDTH = 1080;
    private static final float LARGE_TEXT_SIZE_PX = 188;
    private static final float SMALL_TEXT_SIZE_PX = 60;
    private NumberDrawView mIntegerTv;
    private NumberDrawView mFractionTv;
    private float mCurrentProgress;
    private int mCurrentFloatProgress;
    private WindowManager mWindowManager;
    private Point mScreenSize;
    private float mLargeTextSizePx;
    private float mSmallTextSizePx;
    private float mFloatSpeed;
    private UpdateHandler mUpdateHandler;
    private long mLastUpdateTime;

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
        mUpdateHandler = new UpdateHandler(this);
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        mScreenSize = new Point();
        mWindowManager.getDefaultDisplay().getRealSize(mScreenSize);
        updateSizeForScreenSizeChange();

//        Typeface fontTypeFace
//                = Typeface.createFromAsset(context.getAssets(), "fonts/Mitype2018-35.otf");
        LayoutParams llp = new LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        setOrientation(LinearLayout.HORIZONTAL);
        setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        mIntegerTv = new NumberDrawView(context);
        mIntegerTv.setTextSize(TypedValue.COMPLEX_UNIT_PX, mLargeTextSizePx);
        mIntegerTv.setTextColor(Color.parseColor("#FFFFFF"));
//        if (fontTypeFace != null) {
//            mIntegerTv.setTypeface(fontTypeFace);
//        }
        mIntegerTv.setText("0");
        addView(mIntegerTv, llp);

        mFractionTv = new NumberDrawView(context);
        mFractionTv.setTextSize(TypedValue.COMPLEX_UNIT_PX, mSmallTextSizePx);
        mFractionTv.setTextColor(Color.parseColor("#FFFFFF"));
//        fontTypeFace
//                = Typeface.createFromAsset(context.getAssets(), "fonts/Mitype2018-50.otf");
//        if (fontTypeFace != null) {
//            mFractionTv.setTypeface(fontTypeFace);
//        }
        mFractionTv.setText(".00");
        addView(mFractionTv, llp);

        NumberDrawView signalTv = new NumberDrawView(context);
        signalTv.setTextSize(TypedValue.COMPLEX_UNIT_PX, mSmallTextSizePx);
        signalTv.setTextColor(Color.parseColor("#FFFFFF"));
//        fontTypeFace
//                = Typeface.createFromAsset(context.getAssets(), "fonts/Mitype2018-50.otf");
//        if (fontTypeFace != null) {
//            signalTv.setTypeface(fontTypeFace);
//        }
        signalTv.setText("%");
        addView(signalTv, llp);

        mFloatSpeed = DEFAULT_SPEED; // 默认速度：每1秒小数跳动增加0.01
        mCurrentProgress = -1;
        mCurrentFloatProgress = -1;
        mLastUpdateTime = -1;
        setProgress(0);
    }

    public void setProgress(float progress) {
        if (progress < 0.0f || progress > 100.0f) {
            return;
        }
        if (mCurrentProgress == progress) {
            return;
        }
        float increased = progress - mCurrentProgress;
        if (mLastUpdateTime > 0) {
            long elapse = System.currentTimeMillis() - mLastUpdateTime;
            if (elapse > 0 && mCurrentProgress > 0 && increased > 0) {
                mFloatSpeed = increased * 100 / elapse;
            } else {
                mFloatSpeed = DEFAULT_SPEED;
            }
            Log.i(TAG, "setUpdateTime: " + (1 / mFloatSpeed));
        }
        Log.i(TAG, "setProgress: " + progress);
        mLastUpdateTime = System.currentTimeMillis();
        mCurrentProgress = progress;
        mIntegerTv.setText(String.valueOf((int) mCurrentProgress));
        if (mCurrentFloatProgress >= 0) {
            mCurrentFloatProgress = 0;
        }
        updateState();
    }

    public void setFloatProgress(int floatNum) {
        if (floatNum == mCurrentFloatProgress) {
            return;
        }
        Log.i(TAG, "setFloatProgress: " + + floatNum);
        mCurrentFloatProgress = floatNum;
        updateState();
    }

    public void setFloatSpeed(float speed) {
        Log.i(TAG, "setFloatSpeed: " + speed);
        if (speed > 0f && speed < 1f) {
            mFloatSpeed = speed;
        }
        updateState();
    }

    private void updateState() {
        if (mIntegerTv != null && mCurrentProgress > 0) {
            mIntegerTv.setText(String.valueOf((int) mCurrentProgress));
        }
        if (mCurrentFloatProgress < 0) {
            mFractionTv.setVisibility(GONE);
        } else {
            if (mCurrentProgress >= 100) {
                mUpdateHandler.removeMessages(UpdateHandler.MSG_INCREASE);
                mCurrentFloatProgress = 0;
                mFractionTv.setText(".00");
                mFractionTv.setVisibility(GONE);
            } else {
                startFloatAnimation();
            }
        }
    }

    private void startFloatAnimation() {
        if (isAttachedToWindow()) {
            mUpdateHandler.removeMessages(UpdateHandler.MSG_INCREASE);
            Message msg = mUpdateHandler.obtainMessage(
                    UpdateHandler.MSG_INCREASE, mCurrentFloatProgress, 0);
            mUpdateHandler.sendMessage(msg);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        checkScreenSize();
        updateState();
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        checkScreenSize();
    }

    private void checkScreenSize() {
        Point point = new Point();
        mWindowManager.getDefaultDisplay().getRealSize(point);
        if (!mScreenSize.equals(point.x, point.y)) {
            mScreenSize.set(point.x, point.y);
            updateSizeForScreenSizeChange();
            mIntegerTv.setTextSize(TypedValue.COMPLEX_UNIT_PX, mLargeTextSizePx);
            mFractionTv.setTextSize(TypedValue.COMPLEX_UNIT_PX, mSmallTextSizePx);
            requestLayout();
        }
    }

    private void updateSizeForScreenSizeChange() {
        int screenWidth = mScreenSize.x;
        float rateWidth = screenWidth * 1.0f / BASE_SCREEN_WIDTH;
        mLargeTextSizePx = (int) (LARGE_TEXT_SIZE_PX * rateWidth);
        mSmallTextSizePx = (int) (SMALL_TEXT_SIZE_PX * rateWidth);
    }

    private static class UpdateHandler extends Handler {
        private static final int MSG_INCREASE = 1212;
        private WeakReference<PercentCountView> mRef;
        public UpdateHandler(PercentCountView view) {
            if (view != null) {
                mRef = new WeakReference<>(view);
            }
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mRef == null || mRef.get() == null) {
                return;
            }
            switch (msg.what) {
                case MSG_INCREASE:
                    Log.i(TAG, "handleMessage: " + mRef.get().mCurrentFloatProgress + " ");
                    if (mRef.get().mCurrentProgress >= 100) {
                        removeMessages(UpdateHandler.MSG_INCREASE);
                        mRef.get().mCurrentFloatProgress = 0;
                        mRef.get().mFractionTv.setText(".00");
                        mRef.get().mFractionTv.setVisibility(GONE);
                    } else {
                        int value = msg.arg1;
                        mRef.get().mCurrentFloatProgress = value;
                        String valueStr = String.valueOf(value);
                        if (value >= 0 && value <= 9) {
                            valueStr = "0" + valueStr;
                        }
                        mRef.get().mFractionTv.setText("." + valueStr);
                        if (mRef.get().mFractionTv.getVisibility() != VISIBLE) {
                            mRef.get().mFractionTv.setVisibility(VISIBLE);
                        }
                        value ++;
                        if (value <= 99) {
                            Message newMsg = obtainMessage(MSG_INCREASE, value, 0);
                            float speed = mRef.get().mFloatSpeed;
                            if (speed > 0 && speed < 1) {
                                long timeDelay = (long) (1.0f / speed);
                                sendMessageDelayed(newMsg, timeDelay);
                            }
                        } else {
                            // 小数部分递增到最大值自动停止更新
                            mRef.get().mCurrentFloatProgress = 99;
                            mRef.get().mFractionTv.setText(".99");
                            removeMessages(UpdateHandler.MSG_INCREASE);
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    }
}
