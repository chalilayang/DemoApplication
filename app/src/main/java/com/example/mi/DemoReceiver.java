package com.example.mi;

import android.app.KeyguardManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class DemoReceiver extends BroadcastReceiver {

    private static final String TAG = "DemoReceiver";

    KeyguardManager keyguardManager;

    @Override
    public void onReceive(Context context, Intent intent) {
        long start = System.currentTimeMillis();
        keyguardManager = (KeyguardManager) context.getSystemService(Service.KEYGUARD_SERVICE);
        boolean lockScreen = keyguardManager.isKeyguardLocked();
        Log.i(TAG, "onReceive: lockScreen " + lockScreen + "  " + (System.currentTimeMillis() - start));
    }
}
