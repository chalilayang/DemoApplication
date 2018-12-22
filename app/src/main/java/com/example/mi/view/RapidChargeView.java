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
import android.support.annotation.IntDef;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Interpolator;
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
    private static final int FRAME_COUNT = 248;
    private static final int FRAME_INTERVAL = 40;
    public static final int ENTER_ANIMATION = 800;
    public static final int ANIMATION_DURATION = FRAME_COUNT * FRAME_INTERVAL;
    private static final int DISMISS_DURATION = 200;
    private RelativeLayout contentContainer;
    private PercentCountView percentCountView;
    private TextView stateTip;
    
    private ImageView circleImage;
    private FrameAnimationView circleView;
    private FrameAnimationView dotView;
    private ValueAnimator zoomAnimator;
    private AnimatorSet dismissAnimatorSet;

    private WindowManager windowManager;
    private Handler handler = new Handler();

    private boolean isScreenOn;

    private int resIdRapidCircle;
    private int resIdNormalCircle;

    private boolean mStartingDismissWirelessAlphaAnim;

    private TimeInterpolator quartOutInterpolator = new QuartEaseOutInterpolator();

    public static final int NORMAL = 0;
    public static final int RAPID = 1;
    public static final int SUPER_RAPID = 2;
    @IntDef({NORMAL, RAPID, SUPER_RAPID})
    @Retention(RetentionPolicy.SOURCE)
    public @interface CHARGE_STATE {}
    private @CHARGE_STATE int mChargeState;

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
        resIdRapidCircle = getResources().getIdentifier(
                "rapid_charge_circle", "drawable", context.getPackageName());
        resIdNormalCircle = getResources().getIdentifier(
                "normal_charge_circle", "drawable", context.getPackageName());
        setBackgroundColor(Color.argb(0.85f, 0f,0f,0f));
        hideSystemUI();
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        LayoutParams flp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        flp.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;

        dotView = new FrameAnimationView(
                context, FRAME_COUNT, FRAME_INTERVAL, "rapid_charge_dot_");
        addView(dotView, flp);

        contentContainer = new RelativeLayout(context);
        View centerAnchorView = new TextView(context);
        RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, (int) (6f * getResources().getDisplayMetrics().density));
        rlp.addRule(RelativeLayout.CENTER_IN_PARENT);
        centerAnchorView.setId(View.generateViewId());
        contentContainer.addView(centerAnchorView, rlp);

        rlp = new RelativeLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        rlp.addRule(RelativeLayout.CENTER_HORIZONTAL);
        rlp.addRule(RelativeLayout.ABOVE, centerAnchorView.getId());
        percentCountView = new PercentCountView(context);
        contentContainer.addView(percentCountView, rlp);

        stateTip = new TextView(context);
        stateTip.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12.54f);
        stateTip.setIncludeFontPadding(false);
        stateTip.setTextColor(Color.parseColor("#8CFFFFFF"));
        stateTip.setGravity(Gravity.CENTER);
        stateTip.setText(getResources().getString(R.string.rapid_charge_mode_tip));
        rlp = new RelativeLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        rlp.addRule(RelativeLayout.CENTER_HORIZONTAL);
        rlp.addRule(RelativeLayout.BELOW, centerAnchorView.getId());
        contentContainer.addView(stateTip, rlp);

        flp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        addView(contentContainer, flp);

        flp = new LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        flp.gravity = Gravity.CENTER;
        circleView = new FrameAnimationView(
                context, FRAME_COUNT, FRAME_INTERVAL, "rapid_charge_circle_");
        addView(circleView, flp);

        flp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        flp.gravity = Gravity.CENTER;
        circleImage = new ImageView(context);
        addView(circleImage, flp);
    }

    /**
     *
     * @param rapid 普通快冲 18W
     * @param superRapid 超级快冲 27W
     */
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
        final boolean speedChanged = chargeState != mChargeState;
        mChargeState = chargeState;
        post(new Runnable() {
            @Override
            public void run() {
                if (speedChanged) {
                    startContentSwitchAnimation();
                }
            }
        });
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
        circleImage.setImageResource(resIdNormalCircle);
    }

    private void switchToRapid() {
        circleImage.setImageResource(resIdNormalCircle);
    }

    private void switchToSuperRapid() {
        circleImage.setImageResource(resIdRapidCircle);
    }

    public void setScreenOn(boolean screenOn) {
        Log.d(TAG, "setScreenOn() called with: screenOn = [" + screenOn + "]");
        this.isScreenOn = screenOn;
    }

    public void setProgress(float progress) {
        Log.d(TAG, "setProgress() called with: progress = [" + progress + "]");
        percentCountView.setProgress(progress);
    }

    @Override
    protected void onDetachedFromWindow() {
        Log.i(TAG, "onDetachedFromWindow: ");
        super.onDetachedFromWindow();
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        Log.i(TAG, "onAttachedToWindow: ");
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
        }
        if (zoomAnimator == null) {
            initAnimator();
        }
        hideSystemUI();
        setVisibility(VISIBLE);
        setAlpha(1.0f);
        setViewState();
        if (!zoomAnimator.isStarted()) {
            zoomAnimator.cancel();
        }
        zoomAnimator.start();
        circleView.startAnimation();
        dotView.startAnimation();
    }

    private void initAnimator() {
        zoomAnimator = ValueAnimator.ofInt(0, 1);
        zoomAnimator.setInterpolator(quartOutInterpolator);
        zoomAnimator.setDuration(ENTER_ANIMATION);
        zoomAnimator.addListener(this);
        zoomAnimator.addUpdateListener(this);
    }

    private void setViewState() {
        switchToNormal();
        circleView.setAlpha(1.0f);
        circleView.setScaleX(1);
        circleView.setScaleY(1);
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        contentContainer.setScaleX(animation.getAnimatedFraction());
        contentContainer.setScaleY(animation.getAnimatedFraction());
        contentContainer.setAlpha(animation.getAnimatedFraction());

        circleImage.setScaleX(animation.getAnimatedFraction());
        circleImage.setScaleY(animation.getAnimatedFraction());
        circleImage.setAlpha(animation.getAnimatedFraction());
    }

    @Override
    public void onAnimationStart(Animator animation) {
        Log.i(TAG, "onZoomLargeAnimationStart: ");
        if (animationListener != null) {
            animationListener.onRapidAnimationStart(ChargeUtils.NORMAL);
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
        Log.i(TAG, "onZoomLargeAnimationCancel: ");
    }

    @Override
    public void onAnimationRepeat(Animator animation) {

    }

    public void addToWindow(String reason) {
        if (isAttachedToWindow()) {
            return;
        }
        try {
            Log.i(TAG, "addToWindow: " + reason);
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
            Log.i(TAG, "removeFromWindow: " + reason);
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
        PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat(ALPHA,contentContainer.getAlpha(), 0);
        PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat(SCALE_X,contentContainer.getScaleX(), 0);
        PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat(SCALE_Y,contentContainer.getScaleY(), 0);
        final ObjectAnimator alphaAnimator = ObjectAnimator.ofPropertyValuesHolder(
                contentContainer, alpha, scaleX, scaleY).setDuration(DISMISS_DURATION);

        PropertyValuesHolder alphaImage = PropertyValuesHolder.ofFloat(ALPHA,circleImage.getAlpha(), 0);
        scaleX = PropertyValuesHolder.ofFloat(SCALE_X,circleImage.getScaleX(), 0);
        scaleY = PropertyValuesHolder.ofFloat(SCALE_Y,circleImage.getScaleY(), 0);
        final ObjectAnimator alphaImageAnimator = ObjectAnimator.ofPropertyValuesHolder(
                circleImage, alphaImage, scaleX, scaleY).setDuration(DISMISS_DURATION);

        alpha = PropertyValuesHolder.ofFloat(ALPHA,circleView.getAlpha(), 0);
        scaleX = PropertyValuesHolder.ofFloat(SCALE_X,circleView.getScaleX(), 0);
        scaleY = PropertyValuesHolder.ofFloat(SCALE_Y,circleView.getScaleY(), 0);
        final ObjectAnimator circleViewAlphaAnimator
                = ObjectAnimator.ofPropertyValuesHolder(circleView, alpha, scaleX, scaleY).setDuration(DISMISS_DURATION);

        dismissAnimatorSet = new AnimatorSet();
        dismissAnimatorSet.setInterpolator(quartOutInterpolator);
        dismissAnimatorSet.playTogether(alphaAnimator, alphaImageAnimator, circleViewAlphaAnimator);
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
                dotView.stopAnimation();
                if (animationListener != null) {
                    animationListener.onRapidAnimationEnd(ChargeUtils.NORMAL);
                }
                handler.post(mDismissRunnable);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                Log.i(TAG, "onDismissAnimationCancel: ");
                mStartingDismissWirelessAlphaAnim = false;
                circleView.stopAnimation();
                dotView.stopAnimation();
                if (animationListener != null) {
                    animationListener.onRapidAnimationEnd(ChargeUtils.NORMAL);
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
            if (animationListener != null) {
                animationListener.onRapidAnimationDismiss(ChargeUtils.NORMAL);
            }
            Log.i(TAG, "run: mDismissRunnable isScreenOn " + isScreenOn);
            if (isScreenOn) {
                removeFromWindow("dismiss");
            } else {
                setVisibility(INVISIBLE);
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
