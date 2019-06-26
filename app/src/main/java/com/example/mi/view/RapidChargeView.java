package com.example.mi.view;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.mi.demoapplication.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class RapidChargeView extends FrameLayout
        implements ValueAnimator.AnimatorUpdateListener,
        Animator.AnimatorListener {

    private static final String TAG = "RapidChargeView";
    public static final int ANIMATION_DURATION = 10 * 1000;
    public static final int ENTER_ANIMATION = 800;
    private static final int DISMISS_DURATION = 200;
    private static final int LIGHT_BOTTOM_DURATION = 1000;
    private static final int SWITCH_DURATION = 500;

    private static final int PIVOT_X = 100;
    private static final float CHARGE_NUMBER_SCALE_SMALL = 0.85f;
    private static final int CHARGE_NUMBER_TRANSLATE_SMALL = -70;
    private static final int CHARGE_NUMBER_TRANSLATE_INIT = -10;
    private static final int CHARGE_TIP_TRANSLATE_SMALL = -50;
    private static final int INNER_CIRCLE_SIZE = 662; // 旋转的彩色内圈长宽
    private static final int INNER_PARTICAL_CIRCLE_SIZE = 612; // 旋转的彩色内圈长宽

    private ViewGroup mContentContainer;
    private PercentCountView mPercentCountView;
    private TextView mStateTip;
    private GTChargeAniView mGtChargeAniView;
    private ImageView mRapidIcon;
    private ImageView mSuperRapidIcon;

    private SketchView mCircleImage;
    private ImageView mBottomLightImage;
    private ImageView mCircleView;
    private ImageView mParticleCircleView;
    private FireworksView mDotView;

    private AnimatorSet mEnterAnimatorSet;
    private AnimatorSet mDismissAnimatorSet;

    private WindowManager mWindowManager;
    private Handler mHandler = new Handler();

    private boolean mIsScreenOn;

    private boolean mStartingDismissWirelessAlphaAnim;

    private AnimatorSet mContentSwitchAnimator;
    private Interpolator mCubicInterpolator = new LinearInterpolator();
    private Interpolator mQuartOutInterpolator = new LinearInterpolator();

    public static final int NORMAL = 0;
    public static final int RAPID = 1;
    public static final int SUPER_RAPID = 2;
    @android.support.annotation.IntDef({NORMAL, RAPID, SUPER_RAPID})
    @Retention(RetentionPolicy.SOURCE)
    public @interface CHARGE_SPEED {}
    private @CHARGE_SPEED int mChargeState;

    private boolean mWindowShouldAdd; // 用于避免addToWindow后迅速removeWindow失败的情况

    public RapidChargeView(Context context) {
        this(context, null);
    }

    public RapidChargeView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RapidChargeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        mChargeState = NORMAL;
        setLayoutDirection(LAYOUT_DIRECTION_LTR);
        int resIdRapidIcon = R.drawable.rapid_charge_icon;
        int resIdSuperRapidIcon = R.drawable.super_rapid_icon;
        setBackgroundColor(Color.parseColor("#D9000000"));
        hideSystemUI();
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        //外轮廓
        LayoutParams flp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        flp.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
        mCircleImage = new SketchView(context);
        addView(mCircleImage, flp);

        //从下至上的竖条动画
        flp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        flp.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
        mDotView = new FireworksView(context);
        addView(mDotView, flp);

        mContentContainer = new RelativeLayout(context);
        View centerAnchorView = new TextView(context);
        RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, (int) (6f * getResources().getDisplayMetrics().density));
        rlp.addRule(RelativeLayout.CENTER_IN_PARENT);
        centerAnchorView.setId(View.generateViewId());
        mContentContainer.addView(centerAnchorView, rlp);

        // 电量百分比文字
        int bottomMargin = -50;
        rlp = new RelativeLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        rlp.addRule(RelativeLayout.CENTER_IN_PARENT);
        mPercentCountView = new PercentCountView(context);
        mPercentCountView.setTranslationY(CHARGE_NUMBER_TRANSLATE_INIT);
        mContentContainer.addView(mPercentCountView, rlp);

        // 快速充电文案
        mStateTip = new TextView(context);
        mStateTip.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12.54f);
        mStateTip.setIncludeFontPadding(false);
        mStateTip.setTextColor(Color.parseColor("#8CFFFFFF"));
        mStateTip.setGravity(Gravity.CENTER);
        mStateTip.setText(getResources().getString(R.string.rapid_charge_mode_tip));
        rlp = new RelativeLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        rlp.addRule(RelativeLayout.CENTER_HORIZONTAL);
        rlp.addRule(RelativeLayout.BELOW, centerAnchorView.getId());
        rlp.topMargin = -bottomMargin + 20;
        mContentContainer.addView(mStateTip, rlp);

        // turbo charge动画
        mGtChargeAniView = new GTChargeAniView(context);
        rlp = new RelativeLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        rlp.addRule(RelativeLayout.CENTER_HORIZONTAL);
        rlp.addRule(RelativeLayout.BELOW, centerAnchorView.getId());
        rlp.topMargin = -bottomMargin + 20;
        mGtChargeAniView.setVisibility(GONE);
        mGtChargeAniView.setViewInitState();
        mContentContainer.addView(mGtChargeAniView, rlp);

        // 快充闪电
        mRapidIcon = new ImageView(context);
        mRapidIcon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        mRapidIcon.setImageResource(resIdRapidIcon);
        rlp = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        rlp.addRule(RelativeLayout.CENTER_IN_PARENT);
        int paddingTop = 275;
        mRapidIcon.setPadding(0, paddingTop, 0, 0);
        mRapidIcon.setPivotX(PIVOT_X);
        mContentContainer.addView(mRapidIcon, rlp);

        // 超级快充闪电
        mSuperRapidIcon = new ImageView(context);
        mSuperRapidIcon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        mSuperRapidIcon.setImageResource(resIdSuperRapidIcon);
        rlp = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        rlp.addRule(RelativeLayout.CENTER_IN_PARENT);
        mSuperRapidIcon.setPadding(0, paddingTop, 0, 0);
        mSuperRapidIcon.setPivotX(PIVOT_X);
        mContentContainer.addView(mSuperRapidIcon, rlp);

        flp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        addView(mContentContainer, flp);

        // 旋转的彩色内圈
        mCircleView = new ImageView(context);
        mCircleView.setScaleType(ImageView.ScaleType.FIT_XY);
        Drawable drawable = context.getDrawable(R.drawable.wired_rotate_circle);
        if (drawable != null) {
            drawable.setBounds(0, 0, INNER_CIRCLE_SIZE, INNER_CIRCLE_SIZE);
        }
        mCircleView.setImageDrawable(drawable);
        flp = new LayoutParams(INNER_CIRCLE_SIZE, INNER_CIRCLE_SIZE);
        flp.gravity = Gravity.CENTER;
        mCircleView.setLayoutParams(flp);
        addView(mCircleView);
        ObjectAnimator rotateAnimator = ObjectAnimator.ofFloat(mCircleView, View.ROTATION, 0, 360);
        rotateAnimator.setInterpolator(new LinearInterpolator());
        rotateAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Log.i(TAG, "rotateAnimator onAnimationUpdate: " + animation.getAnimatedValue());
            }
        });
        rotateAnimator.setRepeatCount(ValueAnimator.INFINITE);
        rotateAnimator.setDuration(6000);
//        rotateAnimator.start();

        // 旋转的彩色内圈上面的粒子浮层
        mParticleCircleView = new ImageView(context);
        mParticleCircleView.setScaleType(ImageView.ScaleType.FIT_XY);
        drawable = context.getDrawable(R.drawable.particle_circle);
        if (drawable != null) {
            drawable.setBounds(0, 0, INNER_PARTICAL_CIRCLE_SIZE, INNER_PARTICAL_CIRCLE_SIZE);
        }
        mParticleCircleView.setImageDrawable(drawable);
        flp = new LayoutParams(INNER_PARTICAL_CIRCLE_SIZE, INNER_PARTICAL_CIRCLE_SIZE);
        flp.gravity = Gravity.CENTER;
        mParticleCircleView.setLayoutParams(flp);
        addView(mParticleCircleView);
        rotateAnimator = ObjectAnimator.ofFloat(mParticleCircleView, View.ROTATION, 0, 360);
        rotateAnimator.setInterpolator(new LinearInterpolator());
        rotateAnimator.setRepeatCount(ValueAnimator.INFINITE);
        rotateAnimator.setDuration(1000);
//        rotateAnimator.start();

        // 底部光晕
        flp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        flp.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
        mBottomLightImage = new ImageView(context);
        mBottomLightImage.setImageResource(R.drawable.normal_charge_bottom_light);
        addView(mBottomLightImage, flp);

        setComponentTransparent(true);
    }

    public void setChargeState(@CHARGE_SPEED int state) {
        Log.i(TAG, "setChargeState: " + state);
        if (state != mChargeState) {
            mChargeState = state;
            post(this::startContentSwitchAnimation);
        }
    }

    public void setChargeState(final boolean rapid, final boolean superRapid) {
        Log.d(TAG, "setChargeState() called with: rapid = " + rapid);
        int chargeState;
        if (superRapid) {
            chargeState = SUPER_RAPID;
        } else {
            if (rapid) {
                chargeState = RAPID;
            } else {
                chargeState = NORMAL;
            }
        }
        setChargeState(chargeState);
    }

    private void startContentSwitchAnimation() {
        Log.i(TAG, "startContentSwitchAnimation: ");
        switch (mChargeState) {
            case NORMAL:
                switchToNormal();
                break;
            case RAPID:
                switchToRapid();
                break;
            case SUPER_RAPID:
                switchToSuperRapid();
                break;
            default:
                break;
        }
    }

    private void switchToNormal() {
        animateToHideIcon();
    }

    private void switchToRapid() {
        animateToShowRapidIcon();
    }

    private void switchToSuperRapid() {
        animateToShowSuperRapidIcon();
    }

    private void animateToHideIcon() {
        Log.i(TAG, "animateToHideIcon: ");
        if (mContentSwitchAnimator != null) {
            mContentSwitchAnimator.cancel();
        }
        PropertyValuesHolder scaleXProperty = PropertyValuesHolder.ofFloat(
                SCALE_X, mPercentCountView.getScaleX(), 1);
        PropertyValuesHolder scaleYProperty = PropertyValuesHolder.ofFloat(
                SCALE_Y, mPercentCountView.getScaleY(), 1);
        PropertyValuesHolder translationYProperty = PropertyValuesHolder.ofFloat(
                TRANSLATION_Y, mPercentCountView.getTranslationY(), CHARGE_NUMBER_TRANSLATE_INIT);
        final ObjectAnimator numberAnimator = ObjectAnimator.ofPropertyValuesHolder(
                mPercentCountView, scaleXProperty, scaleYProperty, translationYProperty).setDuration(SWITCH_DURATION);

        translationYProperty = PropertyValuesHolder.ofFloat(
                TRANSLATION_Y, mStateTip.getTranslationY(), 0);
        PropertyValuesHolder alphaProperty = PropertyValuesHolder.ofFloat(
                ALPHA, mStateTip.getAlpha(), 0);
        final ObjectAnimator tipAnimator = ObjectAnimator.ofPropertyValuesHolder(
                mStateTip, alphaProperty, translationYProperty).setDuration(SWITCH_DURATION);

        translationYProperty = PropertyValuesHolder.ofFloat(
                TRANSLATION_Y, mGtChargeAniView.getTranslationY(), 0);
        alphaProperty = PropertyValuesHolder.ofFloat(ALPHA, mGtChargeAniView.getAlpha(), 0);
        final ObjectAnimator gtTipRapidAnimator = ObjectAnimator.ofPropertyValuesHolder(
                mGtChargeAniView, alphaProperty, translationYProperty).setDuration(SWITCH_DURATION);

        PropertyValuesHolder scaleXCarProperty = PropertyValuesHolder.ofFloat(
                SCALE_X, mRapidIcon.getScaleX(), 0);
        PropertyValuesHolder scaleYCarProperty = PropertyValuesHolder.ofFloat(
                SCALE_Y, mRapidIcon.getScaleY(), 0);
        alphaProperty = PropertyValuesHolder.ofFloat(
                ALPHA, mRapidIcon.getAlpha(), 0);
        final ObjectAnimator rapidIconAnimator = ObjectAnimator.ofPropertyValuesHolder(
                mRapidIcon, scaleXCarProperty, scaleYCarProperty, alphaProperty).setDuration(SWITCH_DURATION);

        scaleXProperty = PropertyValuesHolder.ofFloat(
                SCALE_X, mSuperRapidIcon.getScaleX(), 0);
        scaleYProperty = PropertyValuesHolder.ofFloat(
                SCALE_Y, mSuperRapidIcon.getScaleY(), 0);
        alphaProperty = PropertyValuesHolder.ofFloat(
                ALPHA, mSuperRapidIcon.getAlpha(), 0);
        final ObjectAnimator superRapidIconAnimator = ObjectAnimator.ofPropertyValuesHolder(
                mSuperRapidIcon, scaleXProperty, scaleYProperty , alphaProperty).setDuration(SWITCH_DURATION);

        mContentSwitchAnimator = new AnimatorSet();
        mContentSwitchAnimator.setInterpolator(mCubicInterpolator);
        mContentSwitchAnimator.playTogether(
                numberAnimator, tipAnimator, gtTipRapidAnimator, rapidIconAnimator, superRapidIconAnimator);
        mContentSwitchAnimator.start();
    }

    private void animateToShowRapidIcon() {
        Log.i(TAG, "animateToShowRapidIcon: ");
        if (mContentSwitchAnimator != null) {
            mContentSwitchAnimator.cancel();
        }
        PropertyValuesHolder scaleXProperty = PropertyValuesHolder.ofFloat(
                SCALE_X, mPercentCountView.getScaleX(), CHARGE_NUMBER_SCALE_SMALL);
        PropertyValuesHolder scaleYProperty = PropertyValuesHolder.ofFloat(
                SCALE_Y, mPercentCountView.getScaleY(), CHARGE_NUMBER_SCALE_SMALL);
        PropertyValuesHolder translationYProperty = PropertyValuesHolder.ofFloat(
                TRANSLATION_Y, mPercentCountView.getTranslationY(), CHARGE_NUMBER_TRANSLATE_SMALL);
        final ObjectAnimator numberAnimator = ObjectAnimator.ofPropertyValuesHolder(
                mPercentCountView, scaleXProperty, scaleYProperty, translationYProperty).setDuration(SWITCH_DURATION);
        numberAnimator.setInterpolator(mCubicInterpolator);

        translationYProperty = PropertyValuesHolder.ofFloat(
                TRANSLATION_Y, mStateTip.getTranslationY(), CHARGE_TIP_TRANSLATE_SMALL);
        PropertyValuesHolder alphaProperty = PropertyValuesHolder.ofFloat(
                ALPHA, mStateTip.getAlpha(), 1);
        final ObjectAnimator tipAnimator = ObjectAnimator.ofPropertyValuesHolder(
                mStateTip, alphaProperty, translationYProperty).setDuration(SWITCH_DURATION);
        tipAnimator.setInterpolator(mCubicInterpolator);

        translationYProperty = PropertyValuesHolder.ofFloat(
                TRANSLATION_Y, mGtChargeAniView.getTranslationY(), CHARGE_TIP_TRANSLATE_SMALL);
        alphaProperty = PropertyValuesHolder.ofFloat(ALPHA, mGtChargeAniView.getAlpha(), 0);
        final ObjectAnimator gtTipAnimator = ObjectAnimator.ofPropertyValuesHolder(
                mGtChargeAniView, alphaProperty, translationYProperty).setDuration(SWITCH_DURATION / 2);
        gtTipAnimator.setInterpolator(mCubicInterpolator);

        scaleXProperty = PropertyValuesHolder.ofFloat(
                SCALE_X, mRapidIcon.getScaleX(), 1);
        scaleYProperty = PropertyValuesHolder.ofFloat(
                SCALE_Y, mRapidIcon.getScaleY(), 1);
        alphaProperty = PropertyValuesHolder.ofFloat(
                ALPHA, mRapidIcon.getAlpha(), 1);
        final ObjectAnimator rapidIconAnimator = ObjectAnimator.ofPropertyValuesHolder(
                mRapidIcon, scaleXProperty, scaleYProperty, alphaProperty).setDuration(SWITCH_DURATION);
        rapidIconAnimator.setInterpolator(mCubicInterpolator);

        PropertyValuesHolder scaleXCarProperty = PropertyValuesHolder.ofFloat(
                SCALE_X, mSuperRapidIcon.getScaleX(), 0);
        PropertyValuesHolder scaleYCarProperty = PropertyValuesHolder.ofFloat(
                SCALE_Y, mSuperRapidIcon.getScaleY(), 0);
        alphaProperty = PropertyValuesHolder.ofFloat(
                ALPHA, mSuperRapidIcon.getAlpha(), 0);
        final ObjectAnimator superRapidIconAnimator = ObjectAnimator.ofPropertyValuesHolder(
                mSuperRapidIcon, scaleXCarProperty, scaleYCarProperty, alphaProperty).setDuration(SWITCH_DURATION);
        superRapidIconAnimator.setInterpolator(mCubicInterpolator);
        rapidIconAnimator.setInterpolator(new OvershootInterpolator(3));

        mContentSwitchAnimator = new AnimatorSet();
        mContentSwitchAnimator.playTogether(
                numberAnimator, tipAnimator, gtTipAnimator, rapidIconAnimator, superRapidIconAnimator);
        mContentSwitchAnimator.start();
    }

    private void animateToShowSuperRapidIcon() {
        Log.i(TAG, "animateToShowSuperRapidIcon: ");
        if (mContentSwitchAnimator != null) {
            mContentSwitchAnimator.cancel();
        }
        PropertyValuesHolder scaleXProperty = PropertyValuesHolder.ofFloat(
                SCALE_X, mPercentCountView.getScaleX(), CHARGE_NUMBER_SCALE_SMALL);
        PropertyValuesHolder scaleYProperty = PropertyValuesHolder.ofFloat(
                SCALE_Y, mPercentCountView.getScaleY(), CHARGE_NUMBER_SCALE_SMALL);
        PropertyValuesHolder translationYProperty = PropertyValuesHolder.ofFloat(
                TRANSLATION_Y, mPercentCountView.getTranslationY(), CHARGE_NUMBER_TRANSLATE_SMALL);
        final ObjectAnimator numberAnimator = ObjectAnimator.ofPropertyValuesHolder(
                mPercentCountView, scaleXProperty, scaleYProperty, translationYProperty).setDuration(SWITCH_DURATION);
        numberAnimator.setInterpolator(mCubicInterpolator);

        translationYProperty = PropertyValuesHolder.ofFloat(
                TRANSLATION_Y, mStateTip.getTranslationY(), CHARGE_TIP_TRANSLATE_SMALL);
        PropertyValuesHolder alphaProperty = PropertyValuesHolder.ofFloat(
                ALPHA, mStateTip.getAlpha(), 0);
        final ObjectAnimator tipAnimator = ObjectAnimator.ofPropertyValuesHolder(
                mStateTip, alphaProperty, translationYProperty).setDuration(SWITCH_DURATION);
        tipAnimator.setInterpolator(mCubicInterpolator);

        translationYProperty = PropertyValuesHolder.ofFloat(
                TRANSLATION_Y, mGtChargeAniView.getTranslationY(), CHARGE_TIP_TRANSLATE_SMALL);
        alphaProperty = PropertyValuesHolder.ofFloat(ALPHA, mGtChargeAniView.getAlpha(), 1);
        final ObjectAnimator gtTipAnimator = ObjectAnimator.ofPropertyValuesHolder(
                mGtChargeAniView, alphaProperty, translationYProperty).setDuration(SWITCH_DURATION / 2);
        gtTipAnimator.setInterpolator(mCubicInterpolator);
        gtTipAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                mGtChargeAniView.setVisibility(GONE);
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                mGtChargeAniView.setViewInitState();
                mGtChargeAniView.setVisibility(VISIBLE);
                mGtChargeAniView.animationToShow();
            }

            @Override
            public void onAnimationCancel(Animator animator) {
                mGtChargeAniView.setVisibility(GONE);
            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });

        scaleXProperty = PropertyValuesHolder.ofFloat(
                SCALE_X, mRapidIcon.getScaleX(), 0);
        scaleYProperty = PropertyValuesHolder.ofFloat(
                SCALE_Y, mRapidIcon.getScaleY(), 0);
        alphaProperty = PropertyValuesHolder.ofFloat(
                ALPHA, mRapidIcon.getAlpha(), 0);
        final ObjectAnimator rapidIconAnimator = ObjectAnimator.ofPropertyValuesHolder(
                mRapidIcon, scaleXProperty, scaleYProperty, alphaProperty).setDuration(SWITCH_DURATION);
        rapidIconAnimator.setInterpolator(mCubicInterpolator);

        PropertyValuesHolder scaleXCarProperty = PropertyValuesHolder.ofFloat(
                SCALE_X, mSuperRapidIcon.getScaleX(), 1);
        PropertyValuesHolder scaleYCarProperty = PropertyValuesHolder.ofFloat(
                SCALE_Y, mSuperRapidIcon.getScaleY(), 1);
        alphaProperty = PropertyValuesHolder.ofFloat(
                ALPHA, mSuperRapidIcon.getAlpha(), 1);
        final ObjectAnimator superRapidIconAnimator = ObjectAnimator.ofPropertyValuesHolder(
                mSuperRapidIcon, scaleXCarProperty, scaleYCarProperty, alphaProperty).setDuration(SWITCH_DURATION);
        superRapidIconAnimator.setInterpolator(mCubicInterpolator);
        superRapidIconAnimator.setInterpolator(new OvershootInterpolator(3));

        mContentSwitchAnimator = new AnimatorSet();
        mContentSwitchAnimator.playTogether(
                numberAnimator, tipAnimator, gtTipAnimator, rapidIconAnimator, superRapidIconAnimator);
        mContentSwitchAnimator.start();
    }

    public void setScreenOn(boolean screenOn) {
        Log.d(TAG, "setScreenOn() called with: screenOn = [" + screenOn + "]");
        this.mIsScreenOn = screenOn;
    }

    public void setProgress(float progress) {
        Log.d(TAG, "setProgress() called with: progress = [" + progress + "]");
        mPercentCountView.setProgress(progress);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!mWindowShouldAdd) {
            removeFromWindow("!mWindowShouldAdd");
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        Log.i(TAG, "onDetachedFromWindow: ");
        super.onDetachedFromWindow();
        if (mWindowShouldAdd) {
            addToWindow("mWindowShouldAdd");
        }
        mHandler.removeCallbacksAndMessages(null);
    }

    public void zoomLarge() {
        Log.i(TAG, "zoomLarge: ");
        mHandler.removeCallbacks(mDismissRunnable);
        if (mDismissAnimatorSet != null && mStartingDismissWirelessAlphaAnim) {
            mDismissAnimatorSet.cancel();
        }
        mStartingDismissWirelessAlphaAnim = false;
        addToWindow("zoomLarge: ");
        hideSystemUI();
        setComponentTransparent(false);
        setViewState();
        setVisibility(VISIBLE);

        if (mEnterAnimatorSet == null) {
            initAnimator();
        }
        if (mEnterAnimatorSet.isStarted()) {
            mEnterAnimatorSet.cancel();
        }
        mEnterAnimatorSet.start();

        // 防止isAttachedToWindow未及时更新导致Window无法接收事件
        post(() -> disableTouch(false));
    }

    private void initAnimator() {
        ValueAnimator mZoomAnimator = ValueAnimator.ofInt(0, 1);
        mZoomAnimator.setInterpolator(mQuartOutInterpolator);
        mZoomAnimator.setDuration(ENTER_ANIMATION);
        mZoomAnimator.addListener(this);
        mZoomAnimator.addUpdateListener(this);

        PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat(ALPHA, 1f, 0f);
        ObjectAnimator mLightFadeOutAnimator = ObjectAnimator.ofPropertyValuesHolder(
                mBottomLightImage, alpha).setDuration(LIGHT_BOTTOM_DURATION);

        mEnterAnimatorSet = new AnimatorSet();
        mEnterAnimatorSet.play(mLightFadeOutAnimator).after(mZoomAnimator);
    }

    private void setViewState() {
        mCircleView.setAlpha(0.0f);
        mCircleView.setScaleX(0);
        mCircleView.setScaleY(0);
        mParticleCircleView.setAlpha(0.0f);
        mParticleCircleView.setScaleX(0);
        mParticleCircleView.setScaleY(0);
        mDotView.setAlpha(0.0f);
        mCircleImage.setAlpha(0.0f);
        mBottomLightImage.setAlpha(0.0f);
        switch (mChargeState) {
            case NORMAL:
                mPercentCountView.setScaleX(1.0f);
                mPercentCountView.setScaleY(1.0f);
                mPercentCountView.setTranslationY(CHARGE_NUMBER_TRANSLATE_INIT);
                mStateTip.setAlpha(0.0f);
                mStateTip.setTranslationY(0);
                mGtChargeAniView.setViewInitState();
                mGtChargeAniView.setVisibility(GONE);
                mRapidIcon.setScaleY(0.0f);
                mRapidIcon.setScaleX(0.0f);
                mRapidIcon.setAlpha(0.0f);
                mSuperRapidIcon.setScaleY(0.0f);
                mSuperRapidIcon.setScaleX(0.0f);
                mSuperRapidIcon.setAlpha(0.0f);
                break;
            case RAPID:
                mPercentCountView.setScaleX(CHARGE_NUMBER_SCALE_SMALL);
                mPercentCountView.setScaleY(CHARGE_NUMBER_SCALE_SMALL);
                mPercentCountView.setTranslationY(CHARGE_NUMBER_TRANSLATE_SMALL);
                mStateTip.setAlpha(1.0f);
                mStateTip.setTranslationY(CHARGE_TIP_TRANSLATE_SMALL);
                mGtChargeAniView.setViewInitState();
                mGtChargeAniView.setVisibility(GONE);
                mRapidIcon.setScaleY(1.0f);
                mRapidIcon.setScaleX(1.0f);
                mRapidIcon.setAlpha(1.0f);
                mSuperRapidIcon.setScaleY(0.0f);
                mSuperRapidIcon.setScaleX(0.0f);
                mSuperRapidIcon.setAlpha(0.0f);
                break;
            case SUPER_RAPID:
                mPercentCountView.setScaleX(CHARGE_NUMBER_SCALE_SMALL);
                mPercentCountView.setScaleY(CHARGE_NUMBER_SCALE_SMALL);
                mPercentCountView.setTranslationY(CHARGE_NUMBER_TRANSLATE_SMALL);
                mStateTip.setAlpha(0.0f);
                mStateTip.setTranslationY(CHARGE_TIP_TRANSLATE_SMALL);
                mGtChargeAniView.setViewInitState();
                mGtChargeAniView.setVisibility(VISIBLE);
                mGtChargeAniView.animationToShow();
                mRapidIcon.setScaleY(0.0f);
                mRapidIcon.setScaleX(0.0f);
                mRapidIcon.setAlpha(0.0f);
                mSuperRapidIcon.setScaleY(1.0f);
                mSuperRapidIcon.setScaleX(1.0f);
                mSuperRapidIcon.setAlpha(1.0f);
                break;
            default:
                break;
        }
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        float fraction = animation.getAnimatedFraction();
        mContentContainer.setScaleX(fraction);
        mContentContainer.setScaleY(fraction);
        mContentContainer.setAlpha(fraction);

        mCircleView.setScaleX(fraction);
        mCircleView.setScaleY(fraction);
        mCircleView.setAlpha(fraction);

        mParticleCircleView.setScaleX(fraction);
        mParticleCircleView.setScaleY(fraction);
        mParticleCircleView.setAlpha(fraction);

        mDotView.setAlpha(fraction);
        mCircleImage.setAlpha(fraction);
        mBottomLightImage.setAlpha(fraction);
    }

    @Override
    public void onAnimationStart(Animator animation) {
        Log.i(TAG, "onZoomLargeAnimationStart: ");
        if (animationListener != null) {
            animationListener.onRapidAnimationStart(ChargeUtils.NORMAL);
        }
        mHandler.removeCallbacks(timeoutDismissJob);
        mHandler.postDelayed(timeoutDismissJob, ANIMATION_DURATION - DISMISS_DURATION);
    }

    @Override
    public void onAnimationEnd(Animator animation) {}

    @Override
    public void onAnimationCancel(Animator animation) {}

    @Override
    public void onAnimationRepeat(Animator animation) {}

    public void addToWindow(String reason) {
        mWindowShouldAdd = true;
        if (isAttachedToWindow() || getParent() != null) {
            return;
        }
        try {
            Log.i(TAG, "addToWindow: " + reason);
            this.setAlpha(0);
            mWindowManager.addView(this, getWindowParam());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void removeFromWindow(String reason) {
        mWindowShouldAdd = false;
        if (!isAttachedToWindow()) {
            return;
        }
        try {
            Log.i(TAG, "removeFromWindow: " + reason);
            mWindowManager.removeView(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startDismiss(String reason) {
        disableTouch(true);
        if (mStartingDismissWirelessAlphaAnim) {
            return;
        }
        if (mEnterAnimatorSet != null) {
            mEnterAnimatorSet.cancel();
        }
        Log.i(TAG, "startDismiss: reason: " + reason);
        mHandler.removeCallbacks(timeoutDismissJob);
        mHandler.removeCallbacks(mDismissRunnable);
        PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat(ALPHA, getAlpha(), 0);
        final ObjectAnimator bgAnimator = ObjectAnimator.ofPropertyValuesHolder(
                this, alpha).setDuration(DISMISS_DURATION);

        PropertyValuesHolder alphaContentContainer
                = PropertyValuesHolder.ofFloat(ALPHA, mContentContainer.getAlpha(), 0);
        PropertyValuesHolder scaleX
                = PropertyValuesHolder.ofFloat(SCALE_X, mContentContainer.getScaleX(), 0);
        PropertyValuesHolder scaleY
                = PropertyValuesHolder.ofFloat(SCALE_Y, mContentContainer.getScaleY(), 0);
        final ObjectAnimator contentContainerAnimator = ObjectAnimator.ofPropertyValuesHolder(
                mContentContainer, alphaContentContainer, scaleX, scaleY).setDuration(DISMISS_DURATION);

        PropertyValuesHolder alphaImage
                = PropertyValuesHolder.ofFloat(ALPHA, mCircleImage.getAlpha(), 0);
        final ObjectAnimator imageAnimator = ObjectAnimator.ofPropertyValuesHolder(
                mCircleImage, alphaImage).setDuration(DISMISS_DURATION);

        PropertyValuesHolder alphaCircle
                = PropertyValuesHolder.ofFloat(ALPHA, mCircleView.getAlpha(), 0);
        scaleX = PropertyValuesHolder.ofFloat(SCALE_X, mCircleView.getScaleX(), 0);
        scaleY = PropertyValuesHolder.ofFloat(SCALE_Y, mCircleView.getScaleY(), 0);
        final ObjectAnimator circleAnimator
                = ObjectAnimator.ofPropertyValuesHolder(
                mCircleView, alphaCircle, scaleX, scaleY).setDuration(DISMISS_DURATION);

        alphaCircle
                = PropertyValuesHolder.ofFloat(ALPHA, mParticleCircleView.getAlpha(), 0);
        scaleX = PropertyValuesHolder.ofFloat(SCALE_X, mParticleCircleView.getScaleX(), 0);
        scaleY = PropertyValuesHolder.ofFloat(SCALE_Y, mParticleCircleView.getScaleY(), 0);
        final ObjectAnimator circleParticleAnimator
                = ObjectAnimator.ofPropertyValuesHolder(
                mParticleCircleView, alphaCircle, scaleX, scaleY).setDuration(DISMISS_DURATION);

        PropertyValuesHolder alphaDot
                = PropertyValuesHolder.ofFloat(ALPHA, mDotView.getAlpha(), 0);
        final ObjectAnimator dotAnimator = ObjectAnimator.ofPropertyValuesHolder(
                mDotView, alphaDot).setDuration(DISMISS_DURATION);

        PropertyValuesHolder alphaBottomLight
                = PropertyValuesHolder.ofFloat(ALPHA, mBottomLightImage.getAlpha(), 0);
        final ObjectAnimator bottomLightAnimator = ObjectAnimator.ofPropertyValuesHolder(
                mBottomLightImage, alphaBottomLight).setDuration(DISMISS_DURATION);

        mDismissAnimatorSet = new AnimatorSet();
        mDismissAnimatorSet.setInterpolator(mQuartOutInterpolator);
        mDismissAnimatorSet.playTogether(
                contentContainerAnimator, imageAnimator, circleAnimator, circleParticleAnimator, dotAnimator, bottomLightAnimator);
        if (!DISMISS_FOR_TIMEOUT.equals(reason)) {
            mDismissAnimatorSet.play(bgAnimator).with(contentContainerAnimator);
        }
        Animator.AnimatorListener animatorListener = new Animator.AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {
                Log.i(TAG, "onDismissAnimationStart: ");
            }

            @Override
            public void onAnimationRepeat(Animator animation) {}

            @Override
            public void onAnimationEnd(Animator animation) {
                Log.i(TAG, "onDismissAnimationEnd: ");
                mStartingDismissWirelessAlphaAnim = false;
                if (animationListener != null) {
                    animationListener.onRapidAnimationEnd(ChargeUtils.NORMAL);
                }
                mHandler.post(mDismissRunnable);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                Log.i(TAG, "onDismissAnimationCancel: ");
                mStartingDismissWirelessAlphaAnim = false;
                if (animationListener != null) {
                    animationListener.onRapidAnimationEnd(ChargeUtils.NORMAL);
                }
                mHandler.removeCallbacks(mDismissRunnable);
            }
        };
        mDismissAnimatorSet.addListener(animatorListener);
        mStartingDismissWirelessAlphaAnim = true;
        mDismissAnimatorSet.start();
    }

    private final Runnable mDismissRunnable = new Runnable() {
        @Override
        public void run() {
            Log.i(TAG, "run: mDismissRunnable mIsScreenOn " + mIsScreenOn);
            setComponentTransparent(true); // 设置INVISIBLE或者GONE,则在灭屏状态下启动动画会闪锁屏界面，所以通过设置透明度来实现
            disableTouch(true);
            if (mIsScreenOn) {
                removeFromWindow("dismiss");
            }
            if (animationListener != null) {
                animationListener.onRapidAnimationDismiss(ChargeUtils.NORMAL);
            }
        }
    };

    public static WindowManager.LayoutParams getWindowParam() {
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION,
                WindowManager.LayoutParams.FLAG_FULLSCREEN |
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                        WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR |
                        WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED |
                        WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS |
                        WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM |
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                PixelFormat.TRANSLUCENT);
        lp.windowAnimations = 0;
        lp.setTitle("rapid_charge");
        return lp;
    }

    private void setComponentTransparent(boolean transparent) {
        if (transparent) {
            setAlpha(0.0f);
            mCircleView.setAlpha(0.0f);
            mParticleCircleView.setAlpha(0.0f);
            mDotView.setAlpha(0.0f);
            mCircleImage.setAlpha(0.0f);
            mContentContainer.setAlpha(0.0f);
            mBottomLightImage.setAlpha(0.0f);
        } else {
            setAlpha(1.0f);
            mCircleView.setAlpha(1.0f);
            mParticleCircleView.setAlpha(1.0f);
            mDotView.setAlpha(1.0f);
            mCircleImage.setAlpha(1.0f);
            mContentContainer.setAlpha(1.0f);
            mBottomLightImage.setAlpha(1.0f);
        }
    }

    private IRapidAnimationListener animationListener;
    public void setRapidAnimationListener(IRapidAnimationListener listener) {
        animationListener = listener;
    }

    private void hideSystemUI() {
        int uiFlags = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        setSystemUiVisibility(uiFlags);
    }

    private void disableTouch(boolean disableTouch) {
        if (!isAttachedToWindow()) {
            return;
        }
        WindowManager.LayoutParams windowLayoutParameters
                = (WindowManager.LayoutParams) getLayoutParams();
        if (disableTouch) {
            windowLayoutParameters.flags
                    = windowLayoutParameters.flags | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        } else {
            windowLayoutParameters.flags
                    = windowLayoutParameters.flags & (~WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
        mWindowManager.updateViewLayout(this, windowLayoutParameters);
    }
    private static final String DISMISS_FOR_TIMEOUT = "dismiss_for_timeout";
    private Runnable timeoutDismissJob = () -> startDismiss(DISMISS_FOR_TIMEOUT);
}
