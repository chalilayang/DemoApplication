package com.example.mi.demoapplication;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.WindowManager;

import com.example.mi.view.AnimationView;
import com.example.mi.view.notification.WaveLineDrawer;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TextDrawActivity extends AppCompatActivity {

    @BindView(R.id.animation_view)
    AnimationView animationView;

    @BindView(R.id.icon_group)
    View mIconGroup;

    private Handler mHandler = new Handler(Looper.getMainLooper());

    private int mType = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        getWindow().setAttributes(lp);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.BLACK);
        setContentView(R.layout.activity_text_draw);
        ButterKnife.bind(this);
        mType = getIntent().getIntExtra("type", 1);
        onViewClicked();
    }

    @OnClick(R.id.animation_view)
    public void onViewClicked() {
        start();
    }

    private void startAnimation(boolean repeat, boolean useCustomInterpolator2) {
        animationView.clearDrawer();
        WaveLineDrawer waveLineDrawer = new WaveLineDrawer(this.getApplicationContext());
        waveLineDrawer.setRepeatMode(repeat);
        if (useCustomInterpolator2) {
            waveLineDrawer.useCustomInterpolator2();
        }
        animationView.addAnimationDrawer(waveLineDrawer);
    }

    private Runnable task = new Runnable() {
        @Override
        public void run() {
            startAnimation(true, false);
            mHandler.postDelayed(this, 60000);
        }
    };

    private Runnable task2 = new Runnable() {
        @Override
        public void run() {
            startAnimation(false, true);
            mHandler.removeCallbacksAndMessages(task3);
            mHandler.postDelayed(task3, 10000);
            mHandler.postDelayed(this, 60000);
        }
    };

    private Runnable task3 = new Runnable() {
        @Override
        public void run() {
            animationView.stopAnimation();
            animationView.invalidate();
        }
    };

    private ViewPropertyAnimator viewPropertyAnimator;
    private void start() {
        mHandler.removeCallbacks(task);
        if (mType == 1) {
            startAnimation(false, false);
            mHandler.postDelayed(task, 60000);
        } else if (mType == 2) {
            startAnimation(false, true);
            mHandler.removeCallbacksAndMessages(task3);
            mHandler.postDelayed(task3, 10000);
            mHandler.postDelayed(task2, 60000);
        } else if (mType == 3) {
            startAnimation(false, false);
            if (viewPropertyAnimator != null) {
                viewPropertyAnimator.cancel();
            }
            mIconGroup.setVisibility(View.VISIBLE);
            mIconGroup.setAlpha(1.0f);
            viewPropertyAnimator = mIconGroup.animate();
            viewPropertyAnimator.alpha(0.5f).setDuration(300).setStartDelay(2000).start();
        }
    }
}
