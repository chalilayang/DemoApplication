package com.example.mi.view;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.TimeInterpolator;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.example.mi.demoapplication.R;

public class GTChargeAniView extends RelativeLayout {
    private static final int BASE_SCREEN_WIDTH = 1080;
    private static final int ENTER_DURATION = 300;
    private static final int TAIL_DISMISS_DURATION = 100;
    private WindowManager mWindowManager;
    private Point mScreenSize;

    private Drawable mChargeIconDrawable;
    private Drawable mTurboIconDrawable;
    private Drawable mTurboTailIconDrawable;

    private int mChargeIconWidth;
    private int mChargeIconHeight;
    private int mTurboIconWidth;
    private int mTurboIconHeight;
    private int mTailIconWidth;
    private int mTailIconHeight;

    private ImageView mChargeIcon;
    private ImageView mTurboIcon;
    private ImageView mTailIcon;
    private int mTranslation;
    private AnimatorSet animatorSet;
    private Interpolator cubicEaseOutInterpolator = new QuartEaseOutInterpolator();
    public GTChargeAniView(Context context) {
        this(context, null);
    }

    public GTChargeAniView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GTChargeAniView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        mChargeIconDrawable = context.getDrawable(R.drawable.charge_icon);
        mTurboIconDrawable = context.getDrawable(R.drawable.turbo_charge_icon);
        mTurboTailIconDrawable = context.getDrawable(R.drawable.gt_charge_tail);
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        mScreenSize = new Point();
        mWindowManager.getDefaultDisplay().getRealSize(mScreenSize);
        updateSizeForScreenSizeChange();

        mChargeIcon = new ImageView(context);
        mChargeIcon.setImageDrawable(mChargeIconDrawable);
        mChargeIcon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        LayoutParams rlp = new LayoutParams(mChargeIconWidth, mChargeIconHeight);
        rlp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        addView(mChargeIcon, rlp);

        mTailIcon = new ImageView(context);
        mTailIcon.setId(View.generateViewId());
        mTailIcon.setImageDrawable(mTurboTailIconDrawable);
        mTailIcon.setPivotX(mTailIconWidth);
        mTailIcon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        rlp = new LayoutParams(mTailIconWidth, mTailIconHeight);
        rlp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        addView(mTailIcon, rlp);

        mTurboIcon = new ImageView(context);
        mTurboIcon.setImageDrawable(mTurboIconDrawable);
        mTurboIcon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        rlp = new LayoutParams(mTurboIconWidth, mTurboIconHeight);
        rlp.addRule(RelativeLayout.RIGHT_OF, mTailIcon.getId());
        rlp.leftMargin = -mTurboIconWidth / 15;
        addView(mTurboIcon, rlp);

        mTranslation = mTailIconWidth;
    }

    public void setViewInitState() {
        mChargeIcon.setAlpha(0f);
        mTailIcon.setAlpha(1.0f);
        mTurboIcon.setAlpha(1.0f);
        mTailIcon.setScaleX(1);
        mTailIcon.setTranslationX(-mTranslation);
        mTurboIcon.setTranslationX(-mTranslation);
    }

    public void animationToShow() {
        if (animatorSet != null) {
            animatorSet.cancel();
        }
        setViewInitState();
        PropertyValuesHolder alphaProperty = PropertyValuesHolder.ofFloat(
                ALPHA, 0, 1);
        PropertyValuesHolder alphaReverseProperty = PropertyValuesHolder.ofFloat(
                ALPHA, 1, 0);
        PropertyValuesHolder scaleReverseProperty = PropertyValuesHolder.ofFloat(
                SCALE_X, 1, 0);
        final ObjectAnimator chargeIconAnimator = ObjectAnimator.ofPropertyValuesHolder(
                mChargeIcon, alphaProperty).setDuration(ENTER_DURATION);
        chargeIconAnimator.setInterpolator(cubicEaseOutInterpolator);

        PropertyValuesHolder translationProperty = PropertyValuesHolder.ofFloat(
                TRANSLATION_X, -mTranslation, 0);
        final ObjectAnimator GTIconMoveInAnimator = ObjectAnimator.ofPropertyValuesHolder(
                mTurboIcon, translationProperty).setDuration(ENTER_DURATION);
        GTIconMoveInAnimator.setInterpolator(cubicEaseOutInterpolator);
        final ObjectAnimator GTTailIconMoveInAnimator = ObjectAnimator.ofPropertyValuesHolder(
                mTailIcon, translationProperty).setDuration(ENTER_DURATION);
        GTTailIconMoveInAnimator.setInterpolator(cubicEaseOutInterpolator);

        final ObjectAnimator GTTailIconFadeOutAnimator = ObjectAnimator.ofPropertyValuesHolder(
                mTailIcon, alphaReverseProperty, scaleReverseProperty).setDuration(TAIL_DISMISS_DURATION);
        GTTailIconFadeOutAnimator.setInterpolator(cubicEaseOutInterpolator);

        animatorSet = new AnimatorSet();
        animatorSet.playTogether(chargeIconAnimator, GTIconMoveInAnimator, GTTailIconMoveInAnimator);
        animatorSet.play(GTTailIconFadeOutAnimator).after(GTTailIconMoveInAnimator);
        animatorSet.start();
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Point point = new Point();
        mWindowManager.getDefaultDisplay().getRealSize(point);
        if (!mScreenSize.equals(point.x, point.y)) {
            mScreenSize.set(point.x, point.y);
            updateSizeForScreenSizeChange();
            updateLayoutParamForScreenSizeChange();
            requestLayout();
        }
    }

    private void updateSizeForScreenSizeChange() {
        int screenWidth = mScreenSize.x;
        float rateWidth = screenWidth * 1.0f / BASE_SCREEN_WIDTH;

        if (mChargeIconDrawable != null) {
            mChargeIconWidth = (int) (rateWidth * mChargeIconDrawable.getIntrinsicWidth());
            mChargeIconHeight = (int) (rateWidth * mChargeIconDrawable.getIntrinsicHeight());
        }

        if (mTurboIconDrawable != null) {
            mTurboIconWidth = (int) (rateWidth * mTurboIconDrawable.getIntrinsicWidth());
            mTurboIconHeight = (int) (rateWidth * mTurboIconDrawable.getIntrinsicHeight());
        }

        if (mTurboTailIconDrawable != null) {
            mTailIconWidth = (int) (rateWidth * mTurboTailIconDrawable.getIntrinsicWidth());
            mTailIconHeight = (int) (rateWidth * mTurboTailIconDrawable.getIntrinsicHeight());
        }

        mTranslation = mTailIconWidth;
    }

    private void updateLayoutParamForScreenSizeChange() {
        LayoutParams rlp = (RelativeLayout.LayoutParams) mChargeIcon.getLayoutParams();
        rlp.width = mChargeIconWidth;
        rlp.height = mChargeIconHeight;

        rlp = (RelativeLayout.LayoutParams) mTailIcon.getLayoutParams();
        rlp.width = mTailIconWidth;
        rlp.height = mTailIconHeight;
        mTailIcon.setPivotX(mTailIconWidth);

        rlp = (RelativeLayout.LayoutParams) mTurboIcon.getLayoutParams();
        rlp.width = mTurboIconWidth;
        rlp.height = mTurboIconHeight;
        rlp.leftMargin = -mTurboIconWidth / 15;
    }

    /**
     * 缓动插值器class
     */
    public class QuartEaseOutInterpolator implements Interpolator {
        public float getInterpolation(float t) {
            float value = t - 1;
            return -(value * value * value * value - 1);
        }
    }
}
