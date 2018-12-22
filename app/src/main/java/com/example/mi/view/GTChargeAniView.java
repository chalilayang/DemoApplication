package com.example.mi.view;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.TimeInterpolator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class GTChargeAniView extends RelativeLayout {
    private static final int SWITCH_DURATION = 800;
    private ImageView chargeIcon;
    private ImageView GTIcon;
    private ImageView GTTailIcon;
    private int mTranslation;
    private AnimatorSet animatorSet;
    private CubicEaseOutInterpolator cubicEaseOutInterpolator = new CubicEaseOutInterpolator();
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
        setBackgroundColor(Color.BLACK);
        int resIdChargeIcon = context.getResources().getIdentifier(
                "charge_icon", "drawable", context.getPackageName());
        int resIdGTIcon = context.getResources().getIdentifier(
                "gt_charge_icon", "drawable", context.getPackageName());
        int resIdGTTailIcon = context.getResources().getIdentifier(
                "gt_charge_tail", "drawable", context.getPackageName());

        chargeIcon = new ImageView(context);
        chargeIcon.setImageResource(resIdChargeIcon);
        chargeIcon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rlp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        addView(chargeIcon, rlp);

        GTTailIcon = new ImageView(context);
        GTTailIcon.setId(View.generateViewId());
        GTTailIcon.setImageResource(resIdGTTailIcon);
        GTTailIcon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        rlp = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rlp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        addView(GTTailIcon, rlp);

        Drawable drawable = context.getDrawable(resIdGTIcon);
        int width = 0;
        if (drawable != null) {
            width = drawable.getIntrinsicWidth();
        }
        GTIcon = new ImageView(context);
        GTIcon.setImageResource(resIdGTIcon);
        GTIcon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        rlp = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rlp.addRule(RelativeLayout.RIGHT_OF, GTTailIcon.getId());
        rlp.leftMargin = -width / 5;
        addView(GTIcon, rlp);

        drawable = context.getDrawable(resIdGTTailIcon);
        if (drawable != null) {
            mTranslation = drawable.getIntrinsicWidth();
        }
    }

    private void setViewInitState() {
        chargeIcon.setAlpha(0f);
        GTTailIcon.setAlpha(1.0f);
        GTIcon.setAlpha(1.0f);
        GTTailIcon.setTranslationX(-mTranslation);
        GTIcon.setTranslationX(-mTranslation);
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
        final ObjectAnimator chargeIconAnimator = ObjectAnimator.ofPropertyValuesHolder(
                chargeIcon, alphaProperty).setDuration(SWITCH_DURATION);
        chargeIconAnimator.setInterpolator(cubicEaseOutInterpolator);

        PropertyValuesHolder translationProperty = PropertyValuesHolder.ofFloat(
                TRANSLATION_X, -mTranslation, 0);
        final ObjectAnimator GTIconMoveInAnimator = ObjectAnimator.ofPropertyValuesHolder(
                GTIcon, translationProperty).setDuration(SWITCH_DURATION);
        GTIconMoveInAnimator.setInterpolator(cubicEaseOutInterpolator);
        final ObjectAnimator GTTailIconMoveInAnimator = ObjectAnimator.ofPropertyValuesHolder(
                GTTailIcon, translationProperty).setDuration(SWITCH_DURATION);
        GTTailIconMoveInAnimator.setInterpolator(cubicEaseOutInterpolator);

        final ObjectAnimator GTTailIconFadeOutAnimator = ObjectAnimator.ofPropertyValuesHolder(
                GTTailIcon, alphaReverseProperty).setDuration(SWITCH_DURATION);
        GTTailIconMoveInAnimator.setInterpolator(cubicEaseOutInterpolator);

        animatorSet = new AnimatorSet();
        animatorSet.playTogether(chargeIconAnimator, GTIconMoveInAnimator, GTTailIconMoveInAnimator);
        animatorSet.play(GTTailIconFadeOutAnimator).after(GTTailIconMoveInAnimator);
        animatorSet.start();
    }

    /**
     * 缓动插值器class
     */
    public class CubicEaseOutInterpolator implements TimeInterpolator {
        public float getInterpolation(float t) {
            float value = t - 1;
            return value * value * value + 1;
        }
    }

    /**
     * 缓动插值器class
     */
    public class QuartEaseOutInterpolator implements TimeInterpolator {
        public float getInterpolation(float t) {
            float value = t - 1;
            return -(value * value * value * value - 1);
        }
    }
}
