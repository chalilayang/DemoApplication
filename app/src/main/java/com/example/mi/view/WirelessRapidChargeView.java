package com.example.mi.view;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.mi.demoapplication.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class WirelessRapidChargeView extends FrameLayout
        implements ValueAnimator.AnimatorUpdateListener, Animator.AnimatorListener {

    private static final String TAG = "WirelessRapidChargeView";

    private static final int DISMISS_DURATION = 600;
    private static final int SWITCH_DURATION = 500;
    private static final int ENTER_ANIMATION = 800;
    public static final int ANIMATION_DURATION = 10 * 1000;

    private static final int NORMAL = 0;
    private static final int SUPER_RAPID = 2;
    @android.support.annotation.IntDef({NORMAL, SUPER_RAPID})
    @Retention(RetentionPolicy.SOURCE)
    private @interface CHARGE_SPEED {}
    private @CHARGE_SPEED int mChargeSpeed;

    private static final int BASE_SCREEN_WIDTH = 1080;
    private static final int PIVOT_X = 100;
    private static final float CHARGE_NUMBER_SCALE_SMALL = 0.85f;
    private static final int CHARGE_NUMBER_TRANSLATE_SMALL = -70;
    private static final int CHARGE_NUMBER_TRANSLATE_INIT = -10;
    private static final int CHARGE_TIP_TRANSLATE_SMALL = -50;

    private static final int SPACE_HEIGHT = (int) (2.75f * 6f);
    private static final int TIP_ICON_TOP_MARGIN = 70;
    private static final int ICON_PADDING_TOP = 275;

    private int mPivotX;
    private int mChargeNumberTranslateSmall;
    private int mChargeNumberTranslateInit;
    private int mChargeTipTranslateSmall;
    private int mSpaceHeight;
    private int mTipTopMargin;
    private int mIconPaddingTop;
    private int mNormalIconWidth;
    private int mNormalIconHeight;
    private int mSuperRapidIconWidth;
    private int mSuperRapidIconHeight;
    private int mCarIconWidth;
    private int mCarIconHeight;

    private WindowManager mWindowManager;
    private Point mScreenSize;

    private ViewGroup mContentContainer;
    private View mCenterAnchorView;
    private PercentCountView mPercentCountView;
    private GTChargeAniView mGtChargeAniView;
    private ImageView mCarModeIcon;
    private ImageView mSuperRapidIcon;
    private ImageView mNormalIcon;
    private AnimationView mCircleRapidView;

    private Drawable mNormalIconDrawable;
    private Drawable mSuperRapidIconDrawable;
    private Drawable mCarIconDrawable;

    private ValueAnimator mZoomAnimator;
    private AnimatorSet mDismissAnimatorSet;
    private AnimatorSet mContentSwitchAnimator;
    private Interpolator mCubicInterpolator = new CubicEaseOutInterpolator();
    private Interpolator mQuartOutInterpolator = new QuartEaseOutInterpolator();
    /**
     * 缓动插值器class
     */
    public class CubicEaseOutInterpolator implements Interpolator {
        public float getInterpolation(float t) {
            float value = t - 1;
            return value * value * value + 1;
        }
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
    private Handler mHandler = new Handler();

    private boolean mIsSuperRapidCharge = false;
    private boolean mIsScreenOn;
    private boolean mInitScreenOn;
    private boolean mIsCarMode;
    private boolean mStartingDismissWirelessAlphaAnim;

    private boolean mWindowShouldAdd; // 用于避免addToWindow后迅速removeWindow失败的情况

    public WirelessRapidChargeView(Context context) {
        this(context, null);
    }

    public WirelessRapidChargeView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WirelessRapidChargeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        mIsSuperRapidCharge = false;
        mIsCarMode = false;

        mNormalIconDrawable = context.getDrawable(R.drawable.charge_animation_normal_charge_icon);
        mSuperRapidIconDrawable = context.getDrawable(R.drawable.charge_animation_normal_charge_icon);
        mCarIconDrawable = context.getDrawable(R.drawable.charge_animation_car_mode_icon);

        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        mScreenSize = new Point();
        mWindowManager.getDefaultDisplay().getRealSize(mScreenSize);
        updateSizeForScreenSizeChange();

        setBackgroundColor(Color.argb((int)(255 * 0.95), 0, 0, 0));
        hideSystemUI();

        LayoutParams flp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        flp.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;

        mContentContainer = new RelativeLayout(context);
        mCenterAnchorView = new TextView(context);
        RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, mSpaceHeight);
        rlp.addRule(RelativeLayout.CENTER_IN_PARENT);
        mCenterAnchorView.setId(View.generateViewId());
        mContentContainer.addView(mCenterAnchorView, rlp);

        rlp = new RelativeLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        rlp.addRule(RelativeLayout.CENTER_IN_PARENT);
        mPercentCountView = new PercentCountView(context);
        mContentContainer.addView(mPercentCountView, rlp);

        mGtChargeAniView = new GTChargeAniView(context);
        rlp = new RelativeLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        rlp.addRule(RelativeLayout.CENTER_HORIZONTAL);
        rlp.addRule(RelativeLayout.BELOW, mCenterAnchorView.getId());
        rlp.topMargin = mTipTopMargin;
        mContentContainer.addView(mGtChargeAniView, rlp);

        mNormalIcon = new ImageView(context);
        mNormalIcon.setScaleType(ImageView.ScaleType.FIT_XY);
        mNormalIcon.setImageDrawable(mNormalIconDrawable);
        rlp = new RelativeLayout.LayoutParams(mNormalIconWidth, mNormalIconHeight + mIconPaddingTop);
        rlp.addRule(RelativeLayout.CENTER_IN_PARENT);
        mNormalIcon.setPadding(0, mIconPaddingTop, 0, 0);
        mNormalIcon.setPivotX(mPivotX);
        mContentContainer.addView(mNormalIcon, rlp);

        mSuperRapidIcon = new ImageView(context);
        mSuperRapidIcon.setScaleType(ImageView.ScaleType.FIT_XY);
        mSuperRapidIcon.setImageDrawable(mSuperRapidIconDrawable);
        rlp = new RelativeLayout.LayoutParams(mSuperRapidIconWidth, mSuperRapidIconHeight + mIconPaddingTop);
        rlp.addRule(RelativeLayout.CENTER_IN_PARENT);
        mSuperRapidIcon.setPivotX(mPivotX);
        mSuperRapidIcon.setPadding(0, mIconPaddingTop, 0, 0);
        mContentContainer.addView(mSuperRapidIcon, rlp);

        mCarModeIcon = new ImageView(context);
        mCarModeIcon.setScaleType(ImageView.ScaleType.FIT_XY);
        mCarModeIcon.setImageDrawable(mCarIconDrawable);
        rlp = new RelativeLayout.LayoutParams(mCarIconWidth, mCarIconHeight + mIconPaddingTop);
        rlp.addRule(RelativeLayout.CENTER_IN_PARENT);
        mCarModeIcon.setPivotX(mPivotX);
        mCarModeIcon.setPadding(0, mIconPaddingTop, 0, 0);
        mContentContainer.addView(mCarModeIcon, rlp);

        flp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        addView(mContentContainer, flp);

        flp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        flp.gravity = Gravity.CENTER;
        mCircleRapidView = new AnimationView(context);
        addView(mCircleRapidView, flp);
        setComponentTransparent(true);
    }

    public void setChargeState(final boolean superRapid, final boolean isCarMode) {
        int chargeState;
        if (superRapid) {
            chargeState = SUPER_RAPID;
        } else {
            chargeState = NORMAL;
        }
        setChargeState(chargeState, isCarMode);
    }

    private void setChargeState(final int speed, final boolean isCarMode) {
        final boolean speedChanged = speed != mChargeSpeed;
        final boolean carModeChanged = isCarMode != mIsCarMode;
        mChargeSpeed = speed;
        mIsSuperRapidCharge = speed == SUPER_RAPID;
        mIsCarMode = isCarMode;
        post(new Runnable() {
            @Override
            public void run() {
                if (speedChanged || carModeChanged) {
                    startContentSwitchAnimation();
                }
            }
        });
    }

    private void startContentSwitchAnimation() {
        if (!isAttachedToWindow()) {
            return;
        }
        if (mIsCarMode) {
            animateToShowCarIcon();
        } else if (mIsSuperRapidCharge) {
            animateToShowSuperRapidIcon();
        } else {
            animateToHideIcon();
        }
    }

    private void animateToShowCarIcon() {
        Log.i(TAG, "animateToShowCarIcon: ");
        if (mContentSwitchAnimator != null) {
            mContentSwitchAnimator.cancel();
        }
        PropertyValuesHolder scaleXProperty = PropertyValuesHolder.ofFloat(
                SCALE_X, mPercentCountView.getScaleX(), CHARGE_NUMBER_SCALE_SMALL);
        PropertyValuesHolder scaleYProperty = PropertyValuesHolder.ofFloat(
                SCALE_Y, mPercentCountView.getScaleY(), CHARGE_NUMBER_SCALE_SMALL);
        PropertyValuesHolder translationYProperty = PropertyValuesHolder.ofFloat(
                TRANSLATION_Y, mPercentCountView.getTranslationY(), mChargeNumberTranslateSmall);
        final ObjectAnimator numberAnimator = ObjectAnimator.ofPropertyValuesHolder(
                mPercentCountView, scaleXProperty, scaleYProperty, translationYProperty).setDuration(SWITCH_DURATION);
        numberAnimator.setInterpolator(mCubicInterpolator);

        ObjectAnimator gtChargeAnimator;
        translationYProperty = PropertyValuesHolder.ofFloat(
                TRANSLATION_Y, mGtChargeAniView.getTranslationY(), mChargeTipTranslateSmall);
        PropertyValuesHolder alphaProperty = PropertyValuesHolder.ofFloat(
                ALPHA, mGtChargeAniView.getAlpha(), mIsSuperRapidCharge ? 1 : 0);
        gtChargeAnimator = ObjectAnimator.ofPropertyValuesHolder(
                mGtChargeAniView, alphaProperty, translationYProperty).setDuration(SWITCH_DURATION / 2);
        gtChargeAnimator.setInterpolator(mCubicInterpolator);
        gtChargeAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                mGtChargeAniView.setVisibility(GONE);
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if (mIsSuperRapidCharge) {
                    mGtChargeAniView.setViewInitState();
                    mGtChargeAniView.setVisibility(VISIBLE);
                    mGtChargeAniView.animationToShow();
                }
            }

            @Override
            public void onAnimationCancel(Animator animator) {
                mGtChargeAniView.setVisibility(GONE);
            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });

        scaleXProperty = PropertyValuesHolder.ofFloat(SCALE_X, mSuperRapidIcon.getScaleX(), 0);
        scaleYProperty = PropertyValuesHolder.ofFloat(SCALE_Y, mSuperRapidIcon.getScaleY(), 0);
        alphaProperty = PropertyValuesHolder.ofFloat(ALPHA, mSuperRapidIcon.getAlpha(), -4.0f);
        final ObjectAnimator rapidIconAnimator = ObjectAnimator.ofPropertyValuesHolder(
                mSuperRapidIcon, scaleXProperty, scaleYProperty, alphaProperty).setDuration(SWITCH_DURATION);
        rapidIconAnimator.setInterpolator(mCubicInterpolator);

        scaleXProperty = PropertyValuesHolder.ofFloat(SCALE_X, mNormalIcon.getScaleX(), 0);
        scaleYProperty = PropertyValuesHolder.ofFloat(SCALE_Y, mNormalIcon.getScaleY(), 0);
        alphaProperty = PropertyValuesHolder.ofFloat(ALPHA, mNormalIcon.getAlpha(), -4.0f);
        final ObjectAnimator normalIconAnimator = ObjectAnimator.ofPropertyValuesHolder(
                mNormalIcon, scaleXProperty, scaleYProperty, alphaProperty).setDuration(SWITCH_DURATION);

        PropertyValuesHolder scaleXCarProperty = PropertyValuesHolder.ofFloat(
                SCALE_X, mCarModeIcon.getScaleX(), 1);
        PropertyValuesHolder scaleYCarProperty = PropertyValuesHolder.ofFloat(
                SCALE_Y, mCarModeIcon.getScaleY(), 1);
        alphaProperty = PropertyValuesHolder.ofFloat(ALPHA, mCarModeIcon.getAlpha(), 1);
        final ObjectAnimator carIconAnimator = ObjectAnimator.ofPropertyValuesHolder(
                mCarModeIcon, scaleXCarProperty, scaleYCarProperty, alphaProperty).setDuration(SWITCH_DURATION);
        carIconAnimator.setInterpolator(mCubicInterpolator);

        mContentSwitchAnimator = new AnimatorSet();
        mContentSwitchAnimator.playTogether(
                numberAnimator,
                carIconAnimator,
                rapidIconAnimator, normalIconAnimator, gtChargeAnimator);
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
                TRANSLATION_Y, mPercentCountView.getTranslationY(), mChargeNumberTranslateSmall);
        final ObjectAnimator numberAnimator = ObjectAnimator.ofPropertyValuesHolder(
                mPercentCountView, scaleXProperty, scaleYProperty, translationYProperty).setDuration(SWITCH_DURATION);
        numberAnimator.setInterpolator(mCubicInterpolator);

        ObjectAnimator gtChargeAnimator;
        translationYProperty = PropertyValuesHolder.ofFloat(
                TRANSLATION_Y, mGtChargeAniView.getTranslationY(), mChargeTipTranslateSmall);
        PropertyValuesHolder alphaProperty = PropertyValuesHolder.ofFloat(
                ALPHA, mGtChargeAniView.getAlpha(), 1);
        gtChargeAnimator = ObjectAnimator.ofPropertyValuesHolder(
                mGtChargeAniView, alphaProperty, translationYProperty).setDuration(SWITCH_DURATION / 2);
        gtChargeAnimator.setInterpolator(mCubicInterpolator);
        gtChargeAnimator.addListener(new Animator.AnimatorListener() {
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
                SCALE_X, mSuperRapidIcon.getScaleX(), 1);
        scaleYProperty = PropertyValuesHolder.ofFloat(
                SCALE_Y, mSuperRapidIcon.getScaleY(), 1);
        alphaProperty = PropertyValuesHolder.ofFloat(
                ALPHA, mSuperRapidIcon.getAlpha(), 1);
        final ObjectAnimator rapidIconAnimator = ObjectAnimator.ofPropertyValuesHolder(
                mSuperRapidIcon, scaleXProperty, scaleYProperty, alphaProperty).setDuration(SWITCH_DURATION);

        rapidIconAnimator.setInterpolator(mCubicInterpolator);
        rapidIconAnimator.setInterpolator(new OvershootInterpolator(3));

        scaleXProperty = PropertyValuesHolder.ofFloat(
                SCALE_X, mNormalIcon.getScaleX(), 0);
        scaleYProperty = PropertyValuesHolder.ofFloat(
                SCALE_Y, mNormalIcon.getScaleY(), 0);
        alphaProperty = PropertyValuesHolder.ofFloat(
                ALPHA, mNormalIcon.getAlpha(), -4.0f);
        final ObjectAnimator normalIconAnimator = ObjectAnimator.ofPropertyValuesHolder(
                mNormalIcon, scaleXProperty, scaleYProperty, alphaProperty).setDuration(SWITCH_DURATION);

        PropertyValuesHolder scaleXCarProperty = PropertyValuesHolder.ofFloat(
                SCALE_X, mCarModeIcon.getScaleX(), 0);
        PropertyValuesHolder scaleYCarProperty = PropertyValuesHolder.ofFloat(
                SCALE_Y, mCarModeIcon.getScaleY(), 0);
        alphaProperty = PropertyValuesHolder.ofFloat(
                ALPHA, mCarModeIcon.getAlpha(), -4.0f);
        final ObjectAnimator carIconAnimator = ObjectAnimator.ofPropertyValuesHolder(
                mCarModeIcon, scaleXCarProperty, scaleYCarProperty, alphaProperty).setDuration(SWITCH_DURATION);
        carIconAnimator.setInterpolator(mCubicInterpolator);

        mContentSwitchAnimator = new AnimatorSet();
        mContentSwitchAnimator.playTogether(
                numberAnimator,
                carIconAnimator,
                rapidIconAnimator, normalIconAnimator, gtChargeAnimator);
        mContentSwitchAnimator.start();
    }

    private void animateToHideIcon() {
        Log.i(TAG, "animateToHideIcon: mIsCarMode " + mIsCarMode + " mIsSuperRapidCharge " + mIsSuperRapidCharge);
        if (mContentSwitchAnimator != null) {
            mContentSwitchAnimator.cancel();
        }
        PropertyValuesHolder scaleXProperty = PropertyValuesHolder.ofFloat(
                SCALE_X, mPercentCountView.getScaleX(), 1);
        PropertyValuesHolder scaleYProperty = PropertyValuesHolder.ofFloat(
                SCALE_Y, mPercentCountView.getScaleY(), 1);
        PropertyValuesHolder translationYProperty = PropertyValuesHolder.ofFloat(
                TRANSLATION_Y, mPercentCountView.getTranslationY(), mChargeNumberTranslateInit);
        final ObjectAnimator numberAnimator = ObjectAnimator.ofPropertyValuesHolder(
                mPercentCountView, scaleXProperty, scaleYProperty, translationYProperty).setDuration(SWITCH_DURATION);

        translationYProperty = PropertyValuesHolder.ofFloat(
                TRANSLATION_Y, mGtChargeAniView.getTranslationY(), 0);
        PropertyValuesHolder alphaProperty = PropertyValuesHolder.ofFloat(
                ALPHA, mGtChargeAniView.getAlpha(), 0);
        final ObjectAnimator gtChargeAnimator = ObjectAnimator.ofPropertyValuesHolder(
                mGtChargeAniView, alphaProperty, translationYProperty).setDuration(SWITCH_DURATION);

        PropertyValuesHolder scaleXCarProperty = PropertyValuesHolder.ofFloat(
                SCALE_X, mCarModeIcon.getScaleX(), 0);
        PropertyValuesHolder scaleYCarProperty = PropertyValuesHolder.ofFloat(
                SCALE_Y, mCarModeIcon.getScaleY(), 0);
        alphaProperty = PropertyValuesHolder.ofFloat(
                ALPHA, mCarModeIcon.getAlpha(), -4.0f);
        final ObjectAnimator carIconAnimator = ObjectAnimator.ofPropertyValuesHolder(
                mCarModeIcon, scaleXCarProperty, scaleYCarProperty, alphaProperty).setDuration(SWITCH_DURATION);

        scaleXProperty = PropertyValuesHolder.ofFloat(
                SCALE_X, mSuperRapidIcon.getScaleX(), 0);
        scaleYProperty = PropertyValuesHolder.ofFloat(
                SCALE_Y, mSuperRapidIcon.getScaleY(), 0);
        alphaProperty = PropertyValuesHolder.ofFloat(
                ALPHA, mSuperRapidIcon.getAlpha(), -4.0f);
        final ObjectAnimator rapidIconAnimator = ObjectAnimator.ofPropertyValuesHolder(
                mSuperRapidIcon, scaleXProperty, scaleYProperty, alphaProperty).setDuration(SWITCH_DURATION);

        scaleXProperty = PropertyValuesHolder.ofFloat(
                SCALE_X, mNormalIcon.getScaleX(), 1);
        scaleYProperty = PropertyValuesHolder.ofFloat(
                SCALE_Y, mNormalIcon.getScaleY(), 1);
        alphaProperty = PropertyValuesHolder.ofFloat(
                ALPHA, mNormalIcon.getAlpha(), 1);
        final ObjectAnimator normalIconAnimator = ObjectAnimator.ofPropertyValuesHolder(
                mNormalIcon, scaleXProperty, scaleYProperty, alphaProperty).setDuration(SWITCH_DURATION);

        mContentSwitchAnimator = new AnimatorSet();
        mContentSwitchAnimator.setInterpolator(mCubicInterpolator);
        mContentSwitchAnimator.playTogether(
                numberAnimator,
                carIconAnimator,
                rapidIconAnimator, normalIconAnimator, gtChargeAnimator);
        mContentSwitchAnimator.start();
    }

    public void setScreenOn(boolean screenOn) {
        this.mIsScreenOn = screenOn;
    }

    public void setProgress(float progress) {
        mPercentCountView.setProgress(progress);
    }

    private void initAnimator() {
        mZoomAnimator = ValueAnimator.ofInt(0, 1);
        mZoomAnimator.setInterpolator(mQuartOutInterpolator);
        mZoomAnimator.setDuration(ENTER_ANIMATION);
        mZoomAnimator.addListener(this);
        mZoomAnimator.addUpdateListener(this);
    }

    public void zoomLarge(boolean screenOn) {
        Log.i(TAG, "zoomLarge: mInitScreenOn " + screenOn);
        mInitScreenOn = screenOn;
        mHandler.removeCallbacks(mDismissRunnable);
        if (mDismissAnimatorSet != null && mStartingDismissWirelessAlphaAnim) {
            mDismissAnimatorSet.cancel();
        }
        mStartingDismissWirelessAlphaAnim = false;
        addToWindow("zoomLarge: ");
        hideSystemUI();
        setComponentTransparent(false);
        setAlpha(mInitScreenOn ? 0.0f : 1.0f);
        setViewState();
        setVisibility(VISIBLE);

        if (mZoomAnimator == null) {
            initAnimator();
        }
        if (mZoomAnimator.isStarted()) {
            mZoomAnimator.cancel();
        }
        mZoomAnimator.start();
        mCircleRapidView.startAnimation();
        post(new Runnable() {
            @Override
            public void run() {
                // 防止isAttachedToWindow未及时更新导致Window无法接收事件
                disableTouch(false);
            }
        });
    }

    private void setViewState() {
        mCircleRapidView.setScaleX(0);
        mCircleRapidView.setScaleY(0);
        mCircleRapidView.setAlpha(1f);
        if (mIsSuperRapidCharge) {
            mGtChargeAniView.setViewInitState();
            mGtChargeAniView.setVisibility(VISIBLE);
            mGtChargeAniView.animationToShow();
        } else {
            mGtChargeAniView.setViewInitState();
            mGtChargeAniView.setVisibility(GONE);
        }
        boolean needShowIcon = mIsSuperRapidCharge || mIsCarMode;
        if (needShowIcon) {
            mGtChargeAniView.setTranslationY(mChargeTipTranslateSmall);
            if (mIsCarMode) {
                mSuperRapidIcon.setScaleX(0);
                mSuperRapidIcon.setScaleY(0);
                mCarModeIcon.setScaleX(1);
                mCarModeIcon.setScaleY(1);
            } else {
                mSuperRapidIcon.setScaleX(1);
                mSuperRapidIcon.setScaleY(1);
                mCarModeIcon.setScaleX(0);
                mCarModeIcon.setScaleY(0);
            }
            mNormalIcon.setScaleX(0f);
            mNormalIcon.setScaleY(0f);
            mPercentCountView.setScaleX(CHARGE_NUMBER_SCALE_SMALL);
            mPercentCountView.setScaleY(CHARGE_NUMBER_SCALE_SMALL);
            mPercentCountView.setTranslationY(mChargeNumberTranslateSmall);
        } else {
            mGtChargeAniView.setTranslationY(0);

            mSuperRapidIcon.setScaleX(0);
            mSuperRapidIcon.setScaleY(0);
            mCarModeIcon.setScaleX(0);
            mCarModeIcon.setScaleY(0);

            mNormalIcon.setScaleX(1.0f);
            mNormalIcon.setScaleY(1.0f);
            mPercentCountView.setScaleX(1);
            mPercentCountView.setScaleY(1);
            mPercentCountView.setTranslationY(mChargeNumberTranslateInit);
        }
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        float fraction = animation.getAnimatedFraction();
        setAlpha(mInitScreenOn ? fraction : 1.0f);
        mContentContainer.setScaleX(fraction);
        mContentContainer.setScaleY(fraction);
        mCircleRapidView.setScaleX(fraction);
        mCircleRapidView.setScaleY(fraction);
        mCircleRapidView.setAlpha(fraction);
    }

    @Override
    public void onAnimationStart(Animator animation) {
        if (animationListener != null) {
            animationListener.onRapidAnimationStart(ChargeUtils.WIRELESS);
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

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!mWindowShouldAdd) {
            removeFromWindow("!mWindowShouldAdd");
        } else {
            checkScreenSize();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mWindowShouldAdd) {
            addToWindow("mWindowShouldAdd");
        }
        mHandler.removeCallbacksAndMessages(null);
    }

    public void addToWindow(String reason) {
        mWindowShouldAdd = true;
        if (isAttachedToWindow() || getParent() != null) {
            return;
        }
        try {
            Log.i(TAG, "addToWindow: reason " + reason);
            setComponentTransparent(true);
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
            Log.i(TAG, "removeFromWindow: reason " + reason);
            mWindowManager.removeViewImmediate(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startDismiss(String reason) {
        disableTouch(true);
        if (mStartingDismissWirelessAlphaAnim) {
            return;
        }
        if (mZoomAnimator != null) {
            mZoomAnimator.cancel();
        }
        Log.i(TAG, "startDismiss: reason: " + reason);
        mHandler.removeCallbacks(timeoutDismissJob);
        mHandler.removeCallbacks(mDismissRunnable);

        PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat(ALPHA, getAlpha(), 0);
        final ObjectAnimator alphaAnimator = ObjectAnimator.ofPropertyValuesHolder(
                this, alpha).setDuration(DISMISS_DURATION);

        PropertyValuesHolder alphaContentContainer
                = PropertyValuesHolder.ofFloat(ALPHA, mContentContainer.getAlpha(), 0);
        PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat(SCALE_X, mContentContainer.getScaleX(), 0);
        PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat(SCALE_Y, mContentContainer.getScaleY(), 0);
        final ObjectAnimator contentContainerAnimator
                = ObjectAnimator.ofPropertyValuesHolder(
                mContentContainer, alphaContentContainer, scaleX, scaleY).setDuration(DISMISS_DURATION);

        alpha = PropertyValuesHolder.ofFloat(ALPHA, mCircleRapidView.getAlpha(), 0);
        scaleX = PropertyValuesHolder.ofFloat(SCALE_X, mCircleRapidView.getScaleX(), 0);
        scaleY = PropertyValuesHolder.ofFloat(SCALE_Y, mCircleRapidView.getScaleY(), 0);
        final ObjectAnimator circleRapidViewAlphaAnimator
                = ObjectAnimator.ofPropertyValuesHolder(
                mCircleRapidView, alpha, scaleX, scaleY).setDuration(DISMISS_DURATION);

        mDismissAnimatorSet = new AnimatorSet();
        mDismissAnimatorSet.setInterpolator(mQuartOutInterpolator);
        mDismissAnimatorSet.playTogether(contentContainerAnimator, circleRapidViewAlphaAnimator);
        if (!DISMISS_FOR_TIMEOUT.equals(reason)) {
            mDismissAnimatorSet.play(alphaAnimator).with(contentContainerAnimator);
        }
        Animator.AnimatorListener animatorListener = new Animator.AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {}

            @Override
            public void onAnimationRepeat(Animator animation) {}

            @Override
            public void onAnimationEnd(Animator animation) {
                mStartingDismissWirelessAlphaAnim = false;
                if (animationListener != null) {
                    animationListener.onRapidAnimationEnd(ChargeUtils.WIRELESS);
                }
                mHandler.post(mDismissRunnable);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mStartingDismissWirelessAlphaAnim = false;
                if (animationListener != null) {
                    animationListener.onRapidAnimationEnd(ChargeUtils.WIRELESS);
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
            mCircleRapidView.stopAnimation();
            setComponentTransparent(true); // 设置INVISIBLE或者GONE,则在灭屏状态下启动动画会闪锁屏界面，所以通过设置透明度来实现
            disableTouch(true);
            if (mIsScreenOn) {
                removeFromWindow("dismiss");
            }
            if (animationListener != null) {
                animationListener.onRapidAnimationDismiss(ChargeUtils.WIRELESS);
            }
        }
    };

    private void setComponentTransparent(boolean transparent) {
        if (transparent) {
            setAlpha(0.0f);
            mCircleRapidView.setAlpha(0.0f);
            mContentContainer.setAlpha(0.0f);
        } else {
            setAlpha(1.0f);
            mCircleRapidView.setAlpha(1.0f);
            mContentContainer.setAlpha(1.0f);
        }
    }

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
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        lp.windowAnimations = 0;
        lp.setTitle("wireless_rapid_charge");
        return lp;
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
            updateLayoutParamForScreenSizeChange();
            requestLayout();
            post(new Runnable() {
                @Override
                public void run() {
                    startContentSwitchAnimation();
                }
            });
        }
    }

    private void updateSizeForScreenSizeChange() {
        int screenWidth = Math.min(mScreenSize.x, mScreenSize.y);
        float rateWidth = screenWidth * 1.0f / BASE_SCREEN_WIDTH;
        mPivotX = (int) (rateWidth * PIVOT_X);
        mChargeNumberTranslateSmall = (int) (rateWidth * CHARGE_NUMBER_TRANSLATE_SMALL);
        mChargeNumberTranslateInit = (int) (rateWidth * CHARGE_NUMBER_TRANSLATE_INIT);
        mChargeTipTranslateSmall = (int) (rateWidth * CHARGE_TIP_TRANSLATE_SMALL);

        mSpaceHeight = (int) (rateWidth * SPACE_HEIGHT);
        mTipTopMargin = (int) (rateWidth * TIP_ICON_TOP_MARGIN);
        mIconPaddingTop = (int) (rateWidth * ICON_PADDING_TOP);

        if (mNormalIconDrawable != null) {
            mNormalIconWidth = (int) (rateWidth * mNormalIconDrawable.getIntrinsicWidth());
            mNormalIconHeight = (int) (rateWidth * mNormalIconDrawable.getIntrinsicHeight());
        }
        if (mSuperRapidIconDrawable != null) {
            mSuperRapidIconWidth = (int) (rateWidth * mSuperRapidIconDrawable.getIntrinsicWidth());
            mSuperRapidIconHeight = (int) (rateWidth * mSuperRapidIconDrawable.getIntrinsicHeight());
        }
        if (mCarIconDrawable != null) {
            mCarIconWidth = (int) (rateWidth * mCarIconDrawable.getIntrinsicWidth());
            mCarIconHeight = (int) (rateWidth * mCarIconDrawable.getIntrinsicHeight());
        }
    }

    private void updateLayoutParamForScreenSizeChange() {
        ViewGroup.LayoutParams lp = mCenterAnchorView.getLayoutParams();
        lp.height = mSpaceHeight;

        // turbo charge动画
        RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) mGtChargeAniView.getLayoutParams();
        rlp.topMargin = mTipTopMargin;

        // 普通闪电
        rlp = (RelativeLayout.LayoutParams) mNormalIcon.getLayoutParams();
        rlp.width = mNormalIconWidth;
        rlp.height = mNormalIconHeight + mIconPaddingTop;
        mNormalIcon.setPadding(0, mIconPaddingTop, 0, 0);
        mNormalIcon.setPivotX(mPivotX);

        // 超级快充闪电
        rlp = (RelativeLayout.LayoutParams) mSuperRapidIcon.getLayoutParams();
        rlp.width = mSuperRapidIconWidth;
        rlp.height = mSuperRapidIconHeight + mIconPaddingTop;
        mSuperRapidIcon.setPadding(0, mIconPaddingTop, 0, 0);
        mSuperRapidIcon.setPivotX(mPivotX);

        // 车载
        rlp = (RelativeLayout.LayoutParams) mCarModeIcon.getLayoutParams();
        rlp.width = mCarIconWidth;
        rlp.height = mCarIconHeight + mIconPaddingTop;
        mCarModeIcon.setPadding(0, mIconPaddingTop, 0, 0);
        mCarModeIcon.setPivotX(mPivotX);
    }

    private Runnable timeoutDismissJob = new Runnable() {
        @Override
        public void run() {
            startDismiss(DISMISS_FOR_TIMEOUT);
        }
    };
    private static final String DISMISS_FOR_TIMEOUT = "dismiss_for_timeout";
}
