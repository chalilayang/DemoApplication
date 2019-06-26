package com.example.mi;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class BroadcastTestService extends Service {
    public BroadcastTestService() {
    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }
}
