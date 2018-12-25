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
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
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
        implements ValueAnimator.AnimatorUpdateListener,
        Animator.AnimatorListener, SurfaceFrameAnimation.FrameAnimationListener {

    private static final String TAG = "WirelessRapidChargeView";

    private static final int DISMISS_DURATION = 200;
    private static final int SWITCH_DURATION = 500;
    private static final String DRAWABLE = "drawable";
    private static final String RES_ID_NORMAL_PRE = "wireless_normal_charge_";
    private static final String RES_ID_RAPID_PRE = "wireless_rapid_charge_";
    private static final String RES_ID_RAPID_CHARGE = "rapid_charge_icon";
    private static final String RES_ID_NORMAL_CHARGE = "normal_charge_icon";
    private static final String RES_ID_SUPER_RAPID_CHARGE = "super_rapid_icon";
    private static final String RES_ID_CAR_MODE_CHARGE = "charge_mode_car";
    private static final int ENTER_ANIMATION = 800;
    public static final int ANIMATION_DURATION = 10000;
    private static final int FRAME_INTERVAL = 32;
    private static final int FRAME_COUNT_NORMAL = 36;
    private static final int FRAME_COUNT_RAPID = 24;

    private static final float CHARGE_NUMBER_SCALE_SMALL = 0.85f;
    private static final int CHARGE_NUMBER_TRANSLATE_SMALL = -40;
    private static final int CHARGE_TIP_TRANSLATE_SMALL = -50;

    public static final int NORMAL = 0;
    public static final int RAPID = 1;
    public static final int SUPER_RAPID = 2;
    @android.support.annotation.IntDef({NORMAL, RAPID, SUPER_RAPID})
    @Retention(RetentionPolicy.SOURCE)
    public @interface CHARGE_SPEED {}
    public @CHARGE_SPEED int mChargeSpeed;

    private RelativeLayout contentContainer;
    private PercentCountView percentCountView;
    private TextView stateTipNormal;
    private GTChargeAniView gtChargeAniView;
    private ImageView carModeIcon;
    private ImageView rapidIcon;
    private ImageView normalIcon;
    private FrameAnimationView circleView;
    private FrameAnimationView circleRapidView;

    private ValueAnimator zoomAnimator;
    private AnimatorSet dismissAnimatorSet;
    private AnimatorSet circleSwitchAnimator;
    private AnimatorSet contentSwitchAnimator;
    private TimeInterpolator cubicInterpolator = new CubicEaseOutInterpolator();
    private TimeInterpolator quartOutInterpolator = new QuartEaseOutInterpolator();

    private WindowManager windowManager;
    private Handler handler = new Handler();

    private boolean isRapidCharge = false;
    private boolean isSuperRapidCharge = false;
    private boolean isScreenOn;
    private boolean carMode;
    private boolean mStartingDismissWirelessAlphaAnim;

    private int resIDSuperRapid;
    private int resIDRapid;

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
        isRapidCharge = false;
        carMode = false;
        resIDRapid = getResources().getIdentifier(
                RES_ID_RAPID_CHARGE, DRAWABLE, context.getPackageName());
        resIDSuperRapid = getResources().getIdentifier(
                RES_ID_SUPER_RAPID_CHARGE, DRAWABLE, context.getPackageName());
        setBackgroundColor(Color.argb(0.85f, 0f,0f,0f));
        hideSystemUI();
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        LayoutParams flp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        flp.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;

        contentContainer = new RelativeLayout(context);
        View centerAnchorView = new TextView(context);
        RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, (int) (7f * getResources().getDisplayMetrics().density));
        rlp.addRule(RelativeLayout.CENTER_IN_PARENT);
        centerAnchorView.setId(View.generateViewId());
        contentContainer.addView(centerAnchorView, rlp);

        int bottomMargin = -50;
        rlp = new RelativeLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        rlp.addRule(RelativeLayout.CENTER_HORIZONTAL);
        rlp.addRule(RelativeLayout.ABOVE, centerAnchorView.getId());
        percentCountView = new PercentCountView(context);
        rlp.bottomMargin = bottomMargin;
        contentContainer.addView(percentCountView, rlp);

        stateTipNormal = new TextView(context);
        stateTipNormal.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12.54f);
        stateTipNormal.setIncludeFontPadding(false);
        stateTipNormal.setTextColor(Color.parseColor("#8CFFFFFF"));
        stateTipNormal.setGravity(Gravity.CENTER);
        stateTipNormal.setText(R.string.rapid_charge_mode_tip);
        rlp = new RelativeLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        rlp.addRule(RelativeLayout.CENTER_HORIZONTAL);
        rlp.addRule(RelativeLayout.BELOW, centerAnchorView.getId());
        rlp.topMargin = -bottomMargin;
        contentContainer.addView(stateTipNormal, rlp);

        gtChargeAniView = new GTChargeAniView(context);
        rlp = new RelativeLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        rlp.addRule(RelativeLayout.CENTER_HORIZONTAL);
        rlp.addRule(RelativeLayout.BELOW, centerAnchorView.getId());
        rlp.topMargin = -bottomMargin;
        contentContainer.addView(gtChargeAniView, rlp);

        normalIcon = new ImageView(context);
        normalIcon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        int redId = getResources().getIdentifier(
                RES_ID_NORMAL_CHARGE, DRAWABLE, context.getPackageName());
        normalIcon.setImageResource(redId);
        rlp = new RelativeLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        rlp.addRule(RelativeLayout.CENTER_IN_PARENT);
        int paddingTop = (int) (stateTipNormal.getTextSize() * 7);
        normalIcon.setPadding(0, paddingTop, 0, 0);
        contentContainer.addView(normalIcon, rlp);

        rapidIcon = new ImageView(context);
        rapidIcon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        rapidIcon.setImageResource(resIDRapid);
        rlp = new RelativeLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        rlp.addRule(RelativeLayout.CENTER_IN_PARENT);
        paddingTop = (int) (stateTipNormal.getTextSize() * 7);
        rapidIcon.setPadding(0, paddingTop, 0, 0);
        contentContainer.addView(rapidIcon, rlp);

        carModeIcon = new ImageView(context);
        carModeIcon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        redId = getResources().getIdentifier(
                RES_ID_CAR_MODE_CHARGE, DRAWABLE, context.getPackageName());
        carModeIcon.setImageResource(redId);
        rlp = new RelativeLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        rlp.addRule(RelativeLayout.CENTER_IN_PARENT);
        paddingTop = (int) (stateTipNormal.getTextSize() * 7);
        carModeIcon.setPadding(0, paddingTop, 0, 0);
        contentContainer.addView(carModeIcon, rlp);

        flp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        addView(contentContainer, flp);

        flp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        flp.gravity = Gravity.CENTER;
        circleView = new FrameAnimationView(
                context, FRAME_COUNT_NORMAL, FRAME_INTERVAL, RES_ID_NORMAL_PRE, ANIMATION_DURATION);
        circleView.setAnimationCallback(this);
        addView(circleView, flp);

        flp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        flp.gravity = Gravity.CENTER;
        circleRapidView = new FrameAnimationView(
                context, FRAME_COUNT_RAPID, FRAME_INTERVAL, RES_ID_RAPID_PRE, ANIMATION_DURATION);
        addView(circleRapidView, flp);
    }

    /**
     *
     * @param rapid 普通快冲 18W
     * @param superRapid 超级快冲 27W
     */
    public void setChargeState(
            final boolean rapid, final boolean superRapid, final boolean isCarMode) {
        Log.d(TAG, "setChargeState() called with: rapid = " + rapid + " " + isSuperRapidCharge);
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
        setChargeState(chargeState, isCarMode);
    }

    public void setChargeState(final int speed, final boolean isCarMode) {
        Log.d(TAG, "setChargeState() called with: speed = [" + speed + "], isCarMode = [" + isCarMode + "]");
        final boolean speedChanged = speed != mChargeSpeed;
        final boolean carModeChanged = isCarMode != carMode;
        mChargeSpeed = speed;
        isRapidCharge = speed == RAPID;
        isSuperRapidCharge = speed == SUPER_RAPID;
        carMode = isCarMode;
        post(new Runnable() {
            @Override
            public void run() {
                if (speedChanged) {
                    startSwitchAnimation();
                    startContentSwitchAnimation();
                } else if (carModeChanged) {
                    startContentSwitchAnimation();
                }
            }
        });
    }

    private void startSwitchAnimation() {
        if (!isAttachedToWindow()) {
            return;
        }
        Log.i(TAG, "startSwitchAnimation: " + isRapidCharge
                + " " + circleRapidView.getAlpha() + " " + circleView.getAlpha());
        if (circleSwitchAnimator != null) {
            circleSwitchAnimator.cancel();
        }
        final boolean rapid = isRapidCharge || isSuperRapidCharge;
        PropertyValuesHolder alphaProperty = PropertyValuesHolder.ofFloat(
                ALPHA,circleRapidView.getAlpha(), rapid ? 1 : 0);
        final ObjectAnimator circleRapidAnimator = ObjectAnimator.ofPropertyValuesHolder(
                circleRapidView, alphaProperty).setDuration(SWITCH_DURATION);
        alphaProperty = PropertyValuesHolder.ofFloat(
                ALPHA,circleView.getAlpha(), rapid ? 0 : 1);
        final ObjectAnimator circleAnimator = ObjectAnimator.ofPropertyValuesHolder(
                circleView, alphaProperty).setDuration(SWITCH_DURATION);
        circleSwitchAnimator = new AnimatorSet();
        circleSwitchAnimator.playTogether(circleRapidAnimator, circleAnimator);
        circleSwitchAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                circleView.startAnimation();
                circleRapidView.startAnimation();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (rapid) {
                    circleView.stopAnimation();
                } else {
                    circleRapidView.stopAnimation();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        circleSwitchAnimator.start();
    }

    private void startContentSwitchAnimation() {
        if (!isAttachedToWindow()) {
            return;
        }
        if (carMode) {
            animateToShowCarIcon();
        } else if (isSuperRapidCharge) {
            animateToShowSuperRapidIcon();
        } else if (isRapidCharge) {
            animateToShowRapidIcon();
        } else {
            animateToHideIcon();
        }
    }

    private void animateToShowCarIcon() {
        Log.i(TAG, "animateToShowCarIcon: ");
        if (contentSwitchAnimator != null) {
            contentSwitchAnimator.cancel();
        }
        PropertyValuesHolder scaleXProperty = PropertyValuesHolder.ofFloat(
                SCALE_X, percentCountView.getScaleX(), CHARGE_NUMBER_SCALE_SMALL);
        PropertyValuesHolder scaleYProperty = PropertyValuesHolder.ofFloat(
                SCALE_Y, percentCountView.getScaleY(), CHARGE_NUMBER_SCALE_SMALL);
        PropertyValuesHolder translationYProperty = PropertyValuesHolder.ofFloat(
                TRANSLATION_Y, percentCountView.getTranslationY(), CHARGE_NUMBER_TRANSLATE_SMALL);
        final ObjectAnimator numberAnimator = ObjectAnimator.ofPropertyValuesHolder(
                percentCountView, scaleXProperty, scaleYProperty, translationYProperty).setDuration(SWITCH_DURATION);
        numberAnimator.setInterpolator(cubicInterpolator);

        translationYProperty = PropertyValuesHolder.ofFloat(
                TRANSLATION_Y, stateTipNormal.getTranslationY(), CHARGE_TIP_TRANSLATE_SMALL);
        PropertyValuesHolder alphaProperty = PropertyValuesHolder.ofFloat(
                ALPHA, stateTipNormal.getAlpha(), isRapidCharge ? 1 : 0);
        final ObjectAnimator tipAnimator = ObjectAnimator.ofPropertyValuesHolder(
                stateTipNormal, alphaProperty, translationYProperty).setDuration(SWITCH_DURATION);
        tipAnimator.setInterpolator(cubicInterpolator);

        ObjectAnimator gtChargeAnimator = null;
        translationYProperty = PropertyValuesHolder.ofFloat(
                TRANSLATION_Y, gtChargeAniView.getTranslationY(), CHARGE_TIP_TRANSLATE_SMALL);
        alphaProperty = PropertyValuesHolder.ofFloat(
                ALPHA, gtChargeAniView.getAlpha(), isSuperRapidCharge ? 1 : 0);
        gtChargeAnimator = ObjectAnimator.ofPropertyValuesHolder(
                gtChargeAniView, alphaProperty, translationYProperty).setDuration(SWITCH_DURATION);
        gtChargeAnimator.setInterpolator(cubicInterpolator);
        gtChargeAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                gtChargeAniView.setVisibility(GONE);
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if (isSuperRapidCharge) {
                    gtChargeAniView.setViewInitState();
                    gtChargeAniView.setVisibility(VISIBLE);
                    gtChargeAniView.animationToShow();
                }
            }

            @Override
            public void onAnimationCancel(Animator animator) {
                gtChargeAniView.setVisibility(GONE);
            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });

        scaleXProperty = PropertyValuesHolder.ofFloat(
                SCALE_X, rapidIcon.getScaleX(), 0);
        scaleYProperty = PropertyValuesHolder.ofFloat(
                SCALE_Y, rapidIcon.getScaleY(), 0);
        final ObjectAnimator rapidIconAnimator = ObjectAnimator.ofPropertyValuesHolder(
                rapidIcon, scaleXProperty, scaleYProperty).setDuration(SWITCH_DURATION);
        rapidIconAnimator.setInterpolator(cubicInterpolator);

        scaleXProperty = PropertyValuesHolder.ofFloat(
                SCALE_X, normalIcon.getScaleX(), 0);
        scaleYProperty = PropertyValuesHolder.ofFloat(
                SCALE_Y, normalIcon.getScaleY(), 0);
        final ObjectAnimator normalIconAnimator = ObjectAnimator.ofPropertyValuesHolder(
                normalIcon, scaleXProperty, scaleYProperty).setDuration(SWITCH_DURATION);

        PropertyValuesHolder scaleXCarProperty = PropertyValuesHolder.ofFloat(
                SCALE_X, carModeIcon.getScaleX(), 1);
        PropertyValuesHolder scaleYCarProperty = PropertyValuesHolder.ofFloat(
                SCALE_Y, carModeIcon.getScaleY(), 1);
        final ObjectAnimator carIconAnimator = ObjectAnimator.ofPropertyValuesHolder(
                carModeIcon, scaleXCarProperty, scaleYCarProperty).setDuration(SWITCH_DURATION);
        carIconAnimator.setInterpolator(cubicInterpolator);

        contentSwitchAnimator = new AnimatorSet();
        if (rapidIcon.getScaleX() > 0.0f) {
            contentSwitchAnimator.playTogether(numberAnimator, tipAnimator, rapidIconAnimator, normalIconAnimator);
            contentSwitchAnimator.play(carIconAnimator).after(numberAnimator);
        } if (normalIcon.getScaleX() > 0.0f) {
            contentSwitchAnimator.playTogether(numberAnimator, tipAnimator, rapidIconAnimator, normalIconAnimator);
            contentSwitchAnimator.play(carIconAnimator).after(numberAnimator);
        } else {
            contentSwitchAnimator.playTogether(numberAnimator, tipAnimator);
            contentSwitchAnimator.play(carIconAnimator)
                    .with(rapidIconAnimator).with(normalIconAnimator).after(numberAnimator);
        }
        contentSwitchAnimator.play(gtChargeAnimator).with(numberAnimator);
        contentSwitchAnimator.start();
    }

    private void animateToShowRapidIcon() {
        Log.i(TAG, "animateToShowRapidIcon: ");
        if (contentSwitchAnimator != null) {
            contentSwitchAnimator.cancel();
        }
        PropertyValuesHolder scaleXProperty = PropertyValuesHolder.ofFloat(
                SCALE_X, percentCountView.getScaleX(), CHARGE_NUMBER_SCALE_SMALL);
        PropertyValuesHolder scaleYProperty = PropertyValuesHolder.ofFloat(
                SCALE_Y, percentCountView.getScaleY(), CHARGE_NUMBER_SCALE_SMALL);
        PropertyValuesHolder translationYProperty = PropertyValuesHolder.ofFloat(
                TRANSLATION_Y, percentCountView.getTranslationY(), CHARGE_NUMBER_TRANSLATE_SMALL);
        final ObjectAnimator numberAnimator = ObjectAnimator.ofPropertyValuesHolder(
                percentCountView, scaleXProperty, scaleYProperty, translationYProperty).setDuration(SWITCH_DURATION);
        numberAnimator.setInterpolator(cubicInterpolator);

        translationYProperty = PropertyValuesHolder.ofFloat(
                TRANSLATION_Y, stateTipNormal.getTranslationY(), CHARGE_TIP_TRANSLATE_SMALL);
        PropertyValuesHolder alphaProperty = PropertyValuesHolder.ofFloat(
                ALPHA, stateTipNormal.getAlpha(), 1);
        final ObjectAnimator tipAnimator = ObjectAnimator.ofPropertyValuesHolder(
                stateTipNormal, alphaProperty, translationYProperty).setDuration(SWITCH_DURATION);
        tipAnimator.setInterpolator(cubicInterpolator);

        ObjectAnimator gtChargeAnimator;
        translationYProperty = PropertyValuesHolder.ofFloat(
                TRANSLATION_Y, gtChargeAniView.getTranslationY(), CHARGE_TIP_TRANSLATE_SMALL);
        alphaProperty = PropertyValuesHolder.ofFloat(ALPHA, gtChargeAniView.getAlpha(), 0);
        gtChargeAnimator = ObjectAnimator.ofPropertyValuesHolder(
                gtChargeAniView, alphaProperty, translationYProperty).setDuration(SWITCH_DURATION);
        gtChargeAnimator.setInterpolator(cubicInterpolator);

        scaleXProperty = PropertyValuesHolder.ofFloat(SCALE_X, rapidIcon.getScaleX(), 1);
        scaleYProperty = PropertyValuesHolder.ofFloat(SCALE_Y, rapidIcon.getScaleY(), 1);
        final ObjectAnimator rapidIconAnimator = ObjectAnimator.ofPropertyValuesHolder(
                rapidIcon, scaleXProperty, scaleYProperty).setDuration(SWITCH_DURATION);
        rapidIconAnimator.setInterpolator(cubicInterpolator);
        rapidIconAnimator.setInterpolator(new OvershootInterpolator(3));
        rapidIconAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                rapidIcon.setImageResource(resIDRapid);
            }

            @Override
            public void onAnimationEnd(Animator animation) {

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        scaleXProperty = PropertyValuesHolder.ofFloat(
                SCALE_X, normalIcon.getScaleX(), 0);
        scaleYProperty = PropertyValuesHolder.ofFloat(
                SCALE_Y, normalIcon.getScaleY(), 0);
        final ObjectAnimator normalIconAnimator = ObjectAnimator.ofPropertyValuesHolder(
                normalIcon, scaleXProperty, scaleYProperty).setDuration(SWITCH_DURATION);

        PropertyValuesHolder scaleXCarProperty = PropertyValuesHolder.ofFloat(
                SCALE_X, carModeIcon.getScaleX(), 0);
        PropertyValuesHolder scaleYCarProperty = PropertyValuesHolder.ofFloat(
                SCALE_Y, carModeIcon.getScaleY(), 0);
        final ObjectAnimator carIconAnimator = ObjectAnimator.ofPropertyValuesHolder(
                carModeIcon, scaleXCarProperty, scaleYCarProperty).setDuration(SWITCH_DURATION);
        carIconAnimator.setInterpolator(cubicInterpolator);

        contentSwitchAnimator = new AnimatorSet();
        if (carModeIcon.getScaleX() > 0.0f) {
            contentSwitchAnimator.playTogether(numberAnimator, tipAnimator, carIconAnimator, normalIconAnimator);
            contentSwitchAnimator.play(rapidIconAnimator).after(numberAnimator);
        } if (normalIcon.getScaleX() > 0.0f) {
            contentSwitchAnimator.playTogether(numberAnimator, tipAnimator, carIconAnimator, rapidIconAnimator);
            contentSwitchAnimator.play(rapidIconAnimator).after(numberAnimator);
        } else {
            contentSwitchAnimator.playTogether(numberAnimator, tipAnimator);
            contentSwitchAnimator.play(carIconAnimator)
                    .with(rapidIconAnimator).with(normalIconAnimator).after(numberAnimator);
        }
        contentSwitchAnimator.play(gtChargeAnimator).with(numberAnimator);
        contentSwitchAnimator.start();
    }

    private void animateToShowSuperRapidIcon() {
        Log.i(TAG, "animateToShowSuperRapidIcon: ");
        if (contentSwitchAnimator != null) {
            contentSwitchAnimator.cancel();
        }
        PropertyValuesHolder scaleXProperty = PropertyValuesHolder.ofFloat(
                SCALE_X, percentCountView.getScaleX(), CHARGE_NUMBER_SCALE_SMALL);
        PropertyValuesHolder scaleYProperty = PropertyValuesHolder.ofFloat(
                SCALE_Y, percentCountView.getScaleY(), CHARGE_NUMBER_SCALE_SMALL);
        PropertyValuesHolder translationYProperty = PropertyValuesHolder.ofFloat(
                TRANSLATION_Y, percentCountView.getTranslationY(), CHARGE_NUMBER_TRANSLATE_SMALL);
        final ObjectAnimator numberAnimator = ObjectAnimator.ofPropertyValuesHolder(
                percentCountView, scaleXProperty, scaleYProperty, translationYProperty).setDuration(SWITCH_DURATION);
        numberAnimator.setInterpolator(cubicInterpolator);

        translationYProperty = PropertyValuesHolder.ofFloat(
                TRANSLATION_Y, stateTipNormal.getTranslationY(), CHARGE_TIP_TRANSLATE_SMALL);
        PropertyValuesHolder alphaProperty = PropertyValuesHolder.ofFloat(
                ALPHA, stateTipNormal.getAlpha(), 0);
        final ObjectAnimator tipAnimator = ObjectAnimator.ofPropertyValuesHolder(
                stateTipNormal, alphaProperty, translationYProperty).setDuration(SWITCH_DURATION);
        tipAnimator.setInterpolator(cubicInterpolator);

        ObjectAnimator gtChargeAnimator;
        translationYProperty = PropertyValuesHolder.ofFloat(
                TRANSLATION_Y, gtChargeAniView.getTranslationY(), CHARGE_TIP_TRANSLATE_SMALL);
        alphaProperty = PropertyValuesHolder.ofFloat(
                ALPHA, gtChargeAniView.getAlpha(), 1);
        gtChargeAnimator = ObjectAnimator.ofPropertyValuesHolder(
                gtChargeAniView, alphaProperty, translationYProperty).setDuration(SWITCH_DURATION);
        gtChargeAnimator.setInterpolator(cubicInterpolator);
        gtChargeAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                gtChargeAniView.setVisibility(GONE);
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                gtChargeAniView.setViewInitState();
                gtChargeAniView.setVisibility(VISIBLE);
                gtChargeAniView.animationToShow();
            }

            @Override
            public void onAnimationCancel(Animator animator) {
                gtChargeAniView.setVisibility(GONE);
            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });

        scaleXProperty = PropertyValuesHolder.ofFloat(
                SCALE_X, rapidIcon.getScaleX(), 1);
        scaleYProperty = PropertyValuesHolder.ofFloat(
                SCALE_Y, rapidIcon.getScaleY(), 1);
        final ObjectAnimator rapidIconAnimator = ObjectAnimator.ofPropertyValuesHolder(
                rapidIcon, scaleXProperty, scaleYProperty).setDuration(SWITCH_DURATION);
        rapidIconAnimator.setInterpolator(cubicInterpolator);
        rapidIconAnimator.setInterpolator(new OvershootInterpolator(3));
        rapidIconAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                rapidIcon.setImageResource(resIDSuperRapid);
            }

            @Override
            public void onAnimationEnd(Animator animation) {

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        scaleXProperty = PropertyValuesHolder.ofFloat(
                SCALE_X, normalIcon.getScaleX(), 0);
        scaleYProperty = PropertyValuesHolder.ofFloat(
                SCALE_Y, normalIcon.getScaleY(), 0);
        final ObjectAnimator normalIconAnimator = ObjectAnimator.ofPropertyValuesHolder(
                normalIcon, scaleXProperty, scaleYProperty).setDuration(SWITCH_DURATION);

        PropertyValuesHolder scaleXCarProperty = PropertyValuesHolder.ofFloat(
                SCALE_X, carModeIcon.getScaleX(), 0);
        PropertyValuesHolder scaleYCarProperty = PropertyValuesHolder.ofFloat(
                SCALE_Y, carModeIcon.getScaleY(), 0);
        final ObjectAnimator carIconAnimator = ObjectAnimator.ofPropertyValuesHolder(
                carModeIcon, scaleXCarProperty, scaleYCarProperty).setDuration(SWITCH_DURATION);
        carIconAnimator.setInterpolator(cubicInterpolator);

        contentSwitchAnimator = new AnimatorSet();
        if (carModeIcon.getScaleX() > 0.0f) {
            contentSwitchAnimator.playTogether(numberAnimator, tipAnimator, carIconAnimator, normalIconAnimator);
            contentSwitchAnimator.play(rapidIconAnimator).after(numberAnimator);
        } if (normalIcon.getScaleX() > 0.0f) {
            contentSwitchAnimator.playTogether(numberAnimator, tipAnimator, carIconAnimator, normalIconAnimator);
            contentSwitchAnimator.play(rapidIconAnimator).after(numberAnimator);
        } else {
            contentSwitchAnimator.playTogether(numberAnimator, tipAnimator);
            contentSwitchAnimator.play(carIconAnimator)
                    .with(rapidIconAnimator).with(normalIconAnimator).after(numberAnimator);
        }
        contentSwitchAnimator.play(gtChargeAnimator).with(numberAnimator);
        contentSwitchAnimator.start();
    }

    private void animateToHideIcon() {
        Log.i(TAG, "animateToHideIcon: carMode " + carMode + " isRapidCharge " + isRapidCharge);
        if (contentSwitchAnimator != null) {
            contentSwitchAnimator.cancel();
        }
        PropertyValuesHolder scaleXProperty = PropertyValuesHolder.ofFloat(
                SCALE_X, percentCountView.getScaleX(), 1);
        PropertyValuesHolder scaleYProperty = PropertyValuesHolder.ofFloat(
                SCALE_Y, percentCountView.getScaleY(), 1);
        PropertyValuesHolder translationYProperty = PropertyValuesHolder.ofFloat(
                TRANSLATION_Y, percentCountView.getTranslationY(), 0);
        final ObjectAnimator numberAnimator = ObjectAnimator.ofPropertyValuesHolder(
                percentCountView, scaleXProperty, scaleYProperty, translationYProperty).setDuration(SWITCH_DURATION);

        translationYProperty = PropertyValuesHolder.ofFloat(
                TRANSLATION_Y, stateTipNormal.getTranslationY(), 0);
        PropertyValuesHolder alphaProperty = PropertyValuesHolder.ofFloat(
                ALPHA, stateTipNormal.getAlpha(), 0);
        final ObjectAnimator tipAnimator = ObjectAnimator.ofPropertyValuesHolder(
                stateTipNormal, alphaProperty, translationYProperty).setDuration(SWITCH_DURATION);

        translationYProperty = PropertyValuesHolder.ofFloat(
                TRANSLATION_Y, gtChargeAniView.getTranslationY(), 0);
        alphaProperty = PropertyValuesHolder.ofFloat(
                ALPHA, gtChargeAniView.getAlpha(), 0);
        final ObjectAnimator gtChargeAnimator = ObjectAnimator.ofPropertyValuesHolder(
                gtChargeAniView, alphaProperty, translationYProperty).setDuration(SWITCH_DURATION);

        PropertyValuesHolder scaleXCarProperty = PropertyValuesHolder.ofFloat(
                SCALE_X, carModeIcon.getScaleX(), 0);
        PropertyValuesHolder scaleYCarProperty = PropertyValuesHolder.ofFloat(
                SCALE_Y, carModeIcon.getScaleY(), 0);
        final ObjectAnimator carIconAnimator = ObjectAnimator.ofPropertyValuesHolder(
                carModeIcon, scaleXCarProperty, scaleYCarProperty).setDuration(SWITCH_DURATION);

        scaleXProperty = PropertyValuesHolder.ofFloat(
                SCALE_X, rapidIcon.getScaleX(), 0);
        scaleYProperty = PropertyValuesHolder.ofFloat(
                SCALE_Y, rapidIcon.getScaleY(), 0);
        final ObjectAnimator rapidIconAnimator = ObjectAnimator.ofPropertyValuesHolder(
                rapidIcon, scaleXProperty, scaleYProperty).setDuration(SWITCH_DURATION);

        scaleXProperty = PropertyValuesHolder.ofFloat(
                SCALE_X, normalIcon.getScaleX(), 1);
        scaleYProperty = PropertyValuesHolder.ofFloat(
                SCALE_Y, normalIcon.getScaleY(), 1);
        final ObjectAnimator normalIconAnimator = ObjectAnimator.ofPropertyValuesHolder(
                normalIcon, scaleXProperty, scaleYProperty).setDuration(SWITCH_DURATION);

        contentSwitchAnimator = new AnimatorSet();
        contentSwitchAnimator.setInterpolator(cubicInterpolator);
        contentSwitchAnimator.playTogether(numberAnimator, tipAnimator, gtChargeAnimator);
        contentSwitchAnimator.play(carIconAnimator)
                .with(rapidIconAnimator).with(normalIconAnimator).with(numberAnimator);
        contentSwitchAnimator.start();
    }

    public void setScreenOn(boolean screenOn) {
        Log.i(TAG, "setScreenOn: " + screenOn);
        this.isScreenOn = screenOn;
    }

    public void setProgress(float progress) {
        Log.d(TAG, "setProgress() called with: progress = [" + progress + "]");
        percentCountView.setProgress(progress);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        handler.removeCallbacksAndMessages(null);
    }

    private void initAnimator() {
        zoomAnimator = ValueAnimator.ofInt(0, 1);
        zoomAnimator.setInterpolator(quartOutInterpolator);
        zoomAnimator.setDuration(ENTER_ANIMATION);
        zoomAnimator.addListener(this);
        zoomAnimator.addUpdateListener(this);
    }

    public void zoomLarge() {
        Log.i(TAG, "zoomLarge: ");
        handler.removeCallbacks(mDismissRunnable);
        if (dismissAnimatorSet != null && mStartingDismissWirelessAlphaAnim) {
            dismissAnimatorSet.cancel();
        }
        mStartingDismissWirelessAlphaAnim = false;
        if (!isAttachedToWindow()) {
            addToWindow("zoomLarge: ");
            return;
        }
        if (zoomAnimator == null) {
            initAnimator();
        }
        hideSystemUI();
        setVisibility(VISIBLE);
        setAlpha(1.0f);
        setViewState();
        if (zoomAnimator.isStarted()) {
            zoomAnimator.cancel();
        }
        zoomAnimator.start();
        if (isRapidCharge || isSuperRapidCharge) {
            circleRapidView.startAnimation();
        } else {
            circleView.startAnimation();
        }
    }

    private void setViewState() {
        circleView.setScaleX(0);
        circleView.setScaleY(0);
        circleRapidView.setScaleX(0);
        circleRapidView.setScaleY(0);

        if (isSuperRapidCharge) {
            circleView.setAlpha(0f);
            circleRapidView.setAlpha(1f);
            stateTipNormal.setAlpha(0f);
            gtChargeAniView.setViewInitState();
            gtChargeAniView.setVisibility(VISIBLE);
            gtChargeAniView.animationToShow();
            rapidIcon.setImageResource(resIDSuperRapid);
        } else if (isRapidCharge) {
            circleView.setAlpha(0f);
            circleRapidView.setAlpha(1f);
            stateTipNormal.setAlpha(1f);
            gtChargeAniView.setViewInitState();
            gtChargeAniView.setVisibility(GONE);
            rapidIcon.setImageResource(resIDRapid);
        } else {
            circleView.setAlpha(1f);
            circleRapidView.setAlpha(0f);
            stateTipNormal.setAlpha(0f);
            gtChargeAniView.setViewInitState();
            gtChargeAniView.setVisibility(GONE);
        }
        boolean needShowIcon = isSuperRapidCharge || isRapidCharge || carMode;
        if (needShowIcon) {
            stateTipNormal.setTranslationY(CHARGE_TIP_TRANSLATE_SMALL);
            gtChargeAniView.setTranslationY(CHARGE_TIP_TRANSLATE_SMALL);
            if (carMode) {
                rapidIcon.setScaleX(0);
                rapidIcon.setScaleY(0);
                carModeIcon.setScaleX(1);
                carModeIcon.setScaleY(1);
            } else {
                rapidIcon.setScaleX(1);
                rapidIcon.setScaleY(1);
                carModeIcon.setScaleX(0);
                carModeIcon.setScaleY(0);
            }
            normalIcon.setScaleX(0f);
            normalIcon.setScaleY(0f);
            percentCountView.setScaleX(CHARGE_NUMBER_SCALE_SMALL);
            percentCountView.setScaleY(CHARGE_NUMBER_SCALE_SMALL);
            percentCountView.setTranslationY(CHARGE_NUMBER_TRANSLATE_SMALL);
        } else {
            stateTipNormal.setTranslationY(0);
            gtChargeAniView.setTranslationY(0);

            rapidIcon.setScaleX(0);
            rapidIcon.setScaleY(0);
            carModeIcon.setScaleX(0);
            carModeIcon.setScaleY(0);

            normalIcon.setScaleX(1.0f);
            normalIcon.setScaleY(1.0f);
            percentCountView.setScaleX(1);
            percentCountView.setScaleY(1);
            percentCountView.setTranslationY(0);
        }
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        setAlpha(animation.getAnimatedFraction());
        contentContainer.setScaleX(animation.getAnimatedFraction());
        contentContainer.setScaleY(animation.getAnimatedFraction());
        circleView.setScaleX(animation.getAnimatedFraction());
        circleView.setScaleY(animation.getAnimatedFraction());
        circleRapidView.setScaleX(animation.getAnimatedFraction());
        circleRapidView.setScaleY(animation.getAnimatedFraction());
        if (isRapidCharge || isSuperRapidCharge) {
            circleRapidView.setAlpha(animation.getAnimatedFraction());
            circleView.setAlpha(0);
        } else {
            circleView.setAlpha(animation.getAnimatedFraction());
            circleRapidView.setAlpha(0);
        }
        Log.i(TAG, "onZoomLargeAnimationUpdate: "
                + circleView.getAlpha() + " " + circleRapidView.getAlpha());
    }

    @Override
    public void onStart() {

    }

    @Override
    public void onInterrupt() {

    }

    @Override
    public void onFinish() {

    }

    @Override
    public void onAnimationStart(Animator animation) {
        Log.i(TAG, "onZoomLargeAnimationStart: ");
        if (animationListener != null) {
            animationListener.onRapidAnimationStart(ChargeUtils.WIRELESS);
        }
        Log.i(TAG, "onZoomLargeAnimationStart: post timeoutDismissJob");
        handler.removeCallbacks(timeoutDismissJob);
        handler.postDelayed(timeoutDismissJob, ANIMATION_DURATION - DISMISS_DURATION);
    }

    @Override
    public void onAnimationEnd(Animator animation) {
        Log.i(TAG, "onZoomLargeAnimationEnd: ");
    }

    @Override
    public void onAnimationCancel(Animator animation) {

    }

    @Override
    public void onAnimationRepeat(Animator animation) {

    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        zoomLarge();
        Log.i(TAG, "onAttachedToWindow: ");
    }

    public void addToWindow(String reason) {
        if (isAttachedToWindow()) {
            return;
        }
        try {
            Log.i(TAG, "addToWindow: reason " + reason);
            this.setAlpha(0);
            windowManager.addView(this, getWindowParam());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void removeFromWindow(String reason) {
        if (!isAttachedToWindow()) {
            return;
        }
        try {
            Log.i(TAG, "removeFromWindow: reason " + reason);
            windowManager.removeView(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startDismiss(String reason) {
        if (mStartingDismissWirelessAlphaAnim) {
            return;
        }
        if (zoomAnimator != null) {
            zoomAnimator.cancel();
        }
        Log.i(TAG, "startDismiss: reason: " + reason);
        handler.removeCallbacks(timeoutDismissJob);
        handler.removeCallbacks(mDismissRunnable);
        PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat(ALPHA,getAlpha(), 0);

        final ObjectAnimator alphaAnimator = ObjectAnimator.ofPropertyValuesHolder(
                this, alpha).setDuration(DISMISS_DURATION);

        PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat(SCALE_X,contentContainer.getScaleX(), 0);
        PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat(SCALE_Y,contentContainer.getScaleY(), 0);
        final ObjectAnimator contentContainerAnimator
                = ObjectAnimator.ofPropertyValuesHolder(
                contentContainer, scaleX, scaleY).setDuration(DISMISS_DURATION);

        alpha = PropertyValuesHolder.ofFloat(ALPHA,circleView.getAlpha(), 0);
        scaleX = PropertyValuesHolder.ofFloat(SCALE_X,circleView.getScaleX(), 0);
        scaleY = PropertyValuesHolder.ofFloat(SCALE_Y,circleView.getScaleY(), 0);
        final ObjectAnimator circleViewAlphaAnimator
                = ObjectAnimator.ofPropertyValuesHolder(
                circleView, alpha, scaleX, scaleY).setDuration(DISMISS_DURATION);

        alpha = PropertyValuesHolder.ofFloat(ALPHA,circleRapidView.getAlpha(), 0);
        scaleX = PropertyValuesHolder.ofFloat(SCALE_X,circleRapidView.getScaleX(), 0);
        scaleY = PropertyValuesHolder.ofFloat(SCALE_Y,circleRapidView.getScaleY(), 0);
        final ObjectAnimator circleRapidViewAlphaAnimator
                = ObjectAnimator.ofPropertyValuesHolder(
                circleRapidView, alpha, scaleX, scaleY).setDuration(DISMISS_DURATION);

        dismissAnimatorSet = new AnimatorSet();
        dismissAnimatorSet.setInterpolator(quartOutInterpolator);
        dismissAnimatorSet.playTogether(
                alphaAnimator,
                contentContainerAnimator,
                circleViewAlphaAnimator,
                circleRapidViewAlphaAnimator);
        Animator.AnimatorListener animatorListener = new Animator.AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {
                Log.i(TAG, "onDismissAnimationStart: ");
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                Log.i(TAG, "onDismissAnimationEnd: ");
                mStartingDismissWirelessAlphaAnim = false;
                circleView.stopAnimation();
                circleRapidView.stopAnimation();
                if (animationListener != null) {
                    animationListener.onRapidAnimationEnd(ChargeUtils.WIRELESS);
                }
                handler.post(mDismissRunnable);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                Log.i(TAG, "onDismissAnimationCancel: ");
                mStartingDismissWirelessAlphaAnim = false;
                circleView.stopAnimation();
                circleRapidView.stopAnimation();
                if (animationListener != null) {
                    animationListener.onRapidAnimationEnd(ChargeUtils.WIRELESS);
                }
                handler.removeCallbacks(mDismissRunnable);
            }
        };
        dismissAnimatorSet.addListener(animatorListener);
        mStartingDismissWirelessAlphaAnim = true;
        dismissAnimatorSet.start();
    }

    private final Runnable mDismissRunnable = new Runnable() {
        @Override
        public void run() {
            Log.i(TAG, "run: mDismissRunnable isScreenOn " + isScreenOn);
            removeFromWindow("dismiss");
            if (animationListener != null) {
                animationListener.onRapidAnimationDismiss(ChargeUtils.WIRELESS);
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
                        WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM,
                PixelFormat.TRANSLUCENT);
        lp.windowAnimations = 0;
        return lp;
    }

    private IRapidAnimationListener animationListener;
    public void setRapidAnimationListener(IRapidAnimationListener listener) {
        animationListener = listener;
    }

    private void hideSystemUI() {
        int uiFlags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        uiFlags |= 0x00001000;
        setSystemUiVisibility(uiFlags);
    }

    private Runnable timeoutDismissJob = new Runnable() {
        @Override
        public void run() {
            startDismiss(DISMISS_FOR_TIMEOUT);
        }
    };
    private static final String DISMISS_FOR_TIMEOUT = "dismiss_for_timeout";

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
    public class QuartEaseOutInterpolator implements TimeInterpolator {
        public float getInterpolation(float t) {
            float value = t - 1;
            return -(value * value * value * value - 1);
        }
    }
}
