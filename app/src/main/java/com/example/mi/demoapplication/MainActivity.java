package com.example.mi.demoapplication;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.example.mi.ApkInfos;
import com.example.mi.DemoReceiver;
import com.example.mi.view.GTChargeAniView;
import com.example.mi.view.RapidChargeView;
import com.example.mi.view.TextDrawView;
import com.example.mi.view.WirelessRapidChargeView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "DemoApplicationMainActivity";
    RapidChargeView rapidChargeView;
    WirelessRapidChargeView countView;

    @BindView(R.id.edit_query)
    EditText editQuery;
    @BindView(R.id.button)
    Button button;
    @BindView(R.id.button2)
    Button button2;
    @BindView(R.id.button3)
    Button button3;
    @BindView(R.id.gt_charge_view)
    GTChargeAniView gtChargeAniView;
    @BindView(R.id.text_draw_view)
    TextDrawView mTextDrawView;
    @BindView(R.id.app_icon)
    ImageView mAppIcon;
    private int count;
    private Handler countHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            count++;
            if (count > 100) {
                count = 0;
            }
            countView.setProgress(78);
            rapidChargeView.setProgress(78);
            this.sendEmptyMessageDelayed(0, 100);
        }
    };

    private boolean rapid = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        countView = new WirelessRapidChargeView(this);
        countView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wirelessChargeState -= 1;
                if (wirelessChargeState > 2) {
                    wirelessChargeState = 0;
                } else if (wirelessChargeState < 0) {
                    wirelessChargeState = 2;
                }
            }
        });
        rapidChargeView = new RapidChargeView(this);
        rapidChargeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (chargeState) {
                    case RapidChargeView.NORMAL:
                        chargeState = RapidChargeView.SUPER_RAPID;
                        break;
                    case RapidChargeView.RAPID:
                        chargeState = RapidChargeView.SUPER_RAPID;
                        break;
                    case RapidChargeView.SUPER_RAPID:
                        chargeState = RapidChargeView.NORMAL;
                        break;
                }
                rapidChargeView.setChargeState(chargeState);
//                rapidChargeView.startDismiss("onClick");
            }
        });
//        countHandler.sendEmptyMessage(0);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        intentFilter.addAction(Intent.ACTION_USER_PRESENT);
        intentFilter.addAction(Intent.ACTION_USER_UNLOCKED);
        intentFilter.addAction(Intent.ACTION_TIME_TICK);
        intentFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY + 1);
        registerReceiver(demoReceiver, intentFilter);

        final ValueAnimator animator = ValueAnimator.ofInt(0, 100);
        animator.setDuration(500);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Log.i(TAG, "onAnimationUpdate: " + animation.getAnimatedValue());
            }
        });
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                Log.i(TAG, "onAnimationStart: ");
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                Log.i(TAG, "onAnimationEnd: ");
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                Log.i(TAG, "onAnimationCancel: ");
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                Log.i(TAG, "onAnimationRepeat: ");
            }
        });
//        animator.start();
        button3.setBackground(getRoundCornerDrawable());
        startService(new Intent(this, SendNotificationService.class));
        ApkInfos apkInfos = new ApkInfos(getApplicationContext());
        List<String> ddd = apkInfos.getAllInstalledApkInfo();
        for (String name :ddd) {
            Log.i(TAG, "onCreate: " + name);
        }
        mAppIcon.setImageDrawable(apkInfos.ddd(
                getApplicationContext(), "com.android.contacts",
                "com.android.contacts.activities.TwelveKeyDialer"));
    }

    /**
     * 获取图标 bitmap
     * @param context
     */
    public static synchronized Bitmap getBitmap(Context context) {
        PackageManager packageManager = null;
        ApplicationInfo applicationInfo = null;
        try {
            packageManager = context.getApplicationContext().getPackageManager();
            applicationInfo = packageManager.getApplicationInfo("com.tencent.mm", 0);
        } catch (PackageManager.NameNotFoundException e) {
            applicationInfo = null;
        }
        Drawable d = packageManager.getApplicationIcon(applicationInfo); //xxx根据自己的情况获取drawable
        BitmapDrawable bd = (BitmapDrawable) d;
        Bitmap bm = bd.getBitmap();
        return bm;
    }

    private DemoReceiver demoReceiver = new DemoReceiver();
    private @RapidChargeView.CHARGE_SPEED int chargeState = RapidChargeView.NORMAL;
    private int wirelessChargeState = 0;
    @OnClick({R.id.button, R.id.button2, R.id.button3})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.button:
//                if (!countView.isAttachedToWindow()) {
//                    countView.addToWindow("ddd");
//                } else {
//                    countView.zoomLarge(true);
//                }
                break;
            case R.id.button2:
//                startActivity(new Intent(this, TextDrawActivity.class));
//                if (!rapidChargeView.isAttachedToWindow()) {
//                    rapidChargeView.addToWindow("eee");
//                }
//                rapidChargeView.zoomLarge();
                break;
            case R.id.button3:
                startActivity(new Intent(this, CrateBitmapActivity.class));
//                gtChargeAniView.animationToShow();
//                sendNotification();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        countHandler.removeCallbacksAndMessages(null);
        unregisterReceiver(demoReceiver);
    }

    public Drawable getRoundCornerDrawable() {
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(Color.WHITE);
        gd.setCornerRadius(10 * getResources().getDisplayMetrics().density);
        return gd;
    }
}
