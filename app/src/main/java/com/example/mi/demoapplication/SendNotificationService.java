package com.example.mi.demoapplication;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;

public class SendNotificationService extends Service {
    public Handler mHandler = new Handler(Looper.getMainLooper());
    private int mCount = 0;
    private AlarmManager mAlarmManager;
    public SendNotificationService() {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        mRunnable.run();
        return START_STICKY;
    }

    public Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            mCount ++;
            sendNotification(mCount);
            mAlarmManager.setExact(AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis() + 5000, "SendNotificationService", onAlarmListener, mHandler);
        }
    };

    public AlarmManager.OnAlarmListener onAlarmListener = new AlarmManager.OnAlarmListener() {
        @Override
        public void onAlarm() {
            mRunnable.run();
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void sendNotification(int count) {
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Notification.Builder builder;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {//8.0系统之上
            NotificationChannel channel = new NotificationChannel(
                    String.valueOf(1212), "chanel_name", NotificationManager.IMPORTANCE_HIGH);
            channel.enableVibration(true);
            channel.enableLights(true);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            channel.canShowBadge();
            manager.createNotificationChannel(channel);
            builder = new Notification.Builder(this, String.valueOf(1212));
        } else {
            builder = new Notification.Builder(this);
        }
        Bundle bundle = new Bundle();
        bundle.putBoolean("travel_display", true);
        bundle.putBoolean("miui.keptOnKeyguard", true);
//        bundle.putString("miui.aodNotificationTip", "ddddddddddddddddddddd");
        Notification notification = builder
                .setExtras(bundle)
                .setCategory(Notification.CATEGORY_MESSAGE)  //通知的类型
                .setTicker("这是状态栏标题")
                .setContentTitle("这是通知标题这是通知标题这是通知标题这是通知标题")
                .setContentText(String.format(getString(R.string.charge_speed_and_level), count))
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.charge_icon)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_ALL | Notification.DEFAULT_SOUND)
                .build();
        manager.notify(1, notification);
        Log.i(TAG, "showAnimate pkg: sendNotification: " +count);
    }

    private static final String TAG = "SendNotificationService";
//    public void sendNotification() {
//        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//        Notification.Builder builder;
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {//8.0系统之上
//            NotificationChannel channel = new NotificationChannel(
//                    String.valueOf(1212), "chanel_name", NotificationManager.IMPORTANCE_HIGH);
//            manager.createNotificationChannel(channel);
//            builder = new Notification.Builder(this, String.valueOf(1212));
//        } else {
//            builder = new Notification.Builder(this);
//        }
//        Notification notification = builder
//                .setContentTitle("这是通知标题")
//                .setContentText(String.format(getString(R.string.charge_speed_and_level), 90))
//                .setWhen(System.currentTimeMillis()).setAutoCancel(true)
//                .setTimeoutAfter(1000)
//                .setSmallIcon(R.drawable.ic_battery_20_black_24dp)
//                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
//                .build();
//        manager.notify(1, notification);
//        countHandler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                manager.cancel(1);
//            }
//        }, 1000);
//    }
}
