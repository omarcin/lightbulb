package com.oczeretko.lightbulb;

import android.app.*;
import android.content.*;
import android.os.*;

public class LightBulbService extends Service {

    private BulbController controller;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // TODO

        stopSelf();
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (controller != null) {
            controller.close();
            controller = null;
        }
    }
}
