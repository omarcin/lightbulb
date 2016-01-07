package com.oczeretko.lightbulb;

import android.app.*;
import android.content.*;
import android.os.*;

public class LightBulbService extends Service {

    private final static String ACTION_ON = "LightBulbService.ON";
    private final static String ACTION_OFF = "LightBulbService.OFF";
    private final static String ACTION_DISCONNECT = "LightBulbService.DISCONNECT";
    private final static String ACTION_LEVEL = "LightBulbService.LEVEL";
    private final static String EXTRA_LEVEL_VALUE = "LightBulbService.LEVEL_VALUE";

    public static Intent getIntentTurnOn(Context context) {
        return getIntentWithAction(context, ACTION_ON);
    }

    public static Intent getIntentTurnOff(Context context) {
        return getIntentWithAction(context, ACTION_OFF);
    }

    public static Intent getIntentDisconnect(Context context) {
        return getIntentWithAction(context, ACTION_DISCONNECT);
    }

    public static Intent getIntentSetLevel(Context context, int lightLevel) {
        Intent intent = getIntentWithAction(context, ACTION_LEVEL);
        intent.putExtra(EXTRA_LEVEL_VALUE, lightLevel);
        return intent;
    }

    private static Intent getIntentWithAction(Context context, String intentAction) {
        Intent intent = new Intent(context, LightBulbService.class);
        intent.setAction(intentAction);
        return intent;
    }

    private final Handler stopSelfHandler = new Handler(msg -> {
        stopSelf();
        return true;
    });

    private BulbController controller;
    private int timeToLiveMillis;

    @Override
    public void onCreate() {
        super.onCreate();
        controller = new BulbController(this);
        timeToLiveMillis = getResources().getInteger(R.integer.service_lightbulb_ttlinmillis);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            return START_NOT_STICKY;
        }

        stopSelfHandler.sendEmptyMessageDelayed(0, timeToLiveMillis);

        switch (intent.getAction()) {
            case ACTION_ON:
                controller.turnOn();
                break;
            case ACTION_OFF:
                controller.turnOff();
                break;
            case ACTION_LEVEL:
                int value = intent.getIntExtra(EXTRA_LEVEL_VALUE, 0);
                controller.setLevel(value);
                break;
            case ACTION_DISCONNECT:
                controller.close();
                stopSelfHandler.removeCallbacksAndMessages(null);
                stopSelf();
                break;
        }

        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopSelfHandler.removeCallbacksAndMessages(null);
        controller.close();
        controller = null;
    }
}
