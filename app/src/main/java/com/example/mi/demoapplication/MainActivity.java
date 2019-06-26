package com.example.mi.demoapplication;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.example.mi.DemoReceiver;
import com.example.mi.view.GTChargeAniView;
import com.example.mi.view.RapidChargeView;
import com.example.mi.view.TextDrawView;
import com.example.mi.view.WirelessRapidChargeView;

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
    }

    private DemoReceiver demoReceiver = new DemoReceiver();
    private @RapidChargeView.CHARGE_SPEED int chargeState = RapidChargeView.NORMAL;
    private int wirelessChargeState = 0;
    @OnClick({R.id.button, R.id.button2, R.id.button3})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.button:
                if (!countView.isAttachedToWindow()) {
                    countView.addToWindow("ddd");
                } else {
                    countView.zoomLarge(true);
                }
                break;
            case R.id.button2:
//                startActivity(new Intent(this, TextDrawActivity.class));
                if (!rapidChargeView.isAttachedToWindow()) {
                    rapidChargeView.addToWindow("eee");
                }
                rapidChargeView.zoomLarge();
                break;
            case R.id.button3:
//                startActivity(new Intent(this, ParticalActivity.class));
//                gtChargeAniView.animationToShow();
//                sendNotification();
                showInvalidChargerDialog();
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

    public void sendNotification() {
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Notification.Builder builder;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {//8.0系统之上
            NotificationChannel channel = new NotificationChannel(
                    String.valueOf(1212), "chanel_name", NotificationManager.IMPORTANCE_HIGH);
            manager.createNotificationChannel(channel);
            builder = new Notification.Builder(this, String.valueOf(1212));
        } else {
            builder = new Notification.Builder(this);
        }
        Notification notification = builder
                .setContentTitle("这是通知标题")
                .setContentText(String.format(getString(R.string.charge_speed_and_level), 90))
                .setWhen(System.currentTimeMillis()).setAutoCancel(true)
                .setTimeoutAfter(1000)
                .setSmallIcon(R.drawable.ic_battery_20_black_24dp)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .build();
        manager.notify(1, notification);
        countHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                manager.cancel(1);
            }
        }, 1000);
    }
}
