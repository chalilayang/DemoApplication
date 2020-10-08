package com.example.mi.demoapplication;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.mi.view.GTChargeAniView;
import com.example.mi.view.TextDrawView;
import com.particles.android.GlRenderActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.edit_query)
    EditText editQuery;
    @BindView(R.id.button)
    Button button;
    @BindView(R.id.button2)
    Button button2;
    @BindView(R.id.button3)
    Button button3;
    @BindView(R.id.button4)
    Button button4;
    @BindView(R.id.gt_charge_view)
    GTChargeAniView gtChargeAniView;
    @BindView(R.id.text_draw_view)
    TextDrawView mTextDrawView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        button3.setBackground(getRoundCornerDrawable());
    }

    @OnClick({R.id.button, R.id.button2, R.id.button3, R.id.button4})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.button:
            case R.id.button2:
                break;
            case R.id.button3:
                startActivity(new Intent(this, GlRenderActivity.class));
                break;
            case R.id.button4:
                startActivity(new Intent(this, RenderScriptActivity.class));
                break;
        }
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
    }
}
