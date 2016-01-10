package com.oczeretko.lightbulb;

import android.app.*;
import android.content.*;
import android.os.*;
import android.util.*;

import com.oczeretko.lightbulb.BulbController.*;

public class LightBulbService extends Service implements StatusChangedListener {

    private final static String TAG = "LightBulbService";

    private final static String ACTION_ON = "LightBulbService.ON";
    private final static String ACTION_OFF = "LightBulbService.OFF";
    private final static String ACTION_DISCONNECT = "LightBulbService.DISCONNECT";
    private final static String ACTION_LEVEL = "LightBulbService.LEVEL";
    private final static String EXTRA_LEVEL_VALUE = "LightBulbService.LEVEL_VALUE";
    private final static String ACTION_LEVEL_ANIMATE = "LightBulbService.LEVEL_ANIMATE";
    private final static String EXTRA_LEVEL_ANIMATE_START_VALUE = "LightBulbService.LEVEL_VALUE_START";
    private final static String EXTRA_LEVEL_ANIMATE_END_VALUE = "LightBulbService.LEVEL_VALUE_END";
    private final static String EXTRA_LEVEL_ANIMATE_TIME = "LightBulbService.TIME";

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

    public static Intent getIntentAnimateLevel(Context context, int startLevel, int endLevel, long timeMillis) {
        Intent intent = getIntentWithAction(context, ACTION_LEVEL_ANIMATE);
        intent.putExtra(EXTRA_LEVEL_ANIMATE_START_VALUE, startLevel);
        intent.putExtra(EXTRA_LEVEL_ANIMATE_END_VALUE, endLevel);
        intent.putExtra(EXTRA_LEVEL_ANIMATE_TIME, timeMillis);
        return intent;
    }

    private static Intent getIntentWithAction(Context context, String intentAction) {
        Intent intent = new Intent(context, LightBulbService.class);
        intent.setAction(intentAction);
        return intent;
    }

    private BulbController controller;
    private BulbNotificationProvider notificationProvider;
    private int timeToLiveMillis;
    private boolean isBulbOn;

    private final Handler stopSelfHandler = new Handler(msg -> {
        Log.d(TAG, "Handler - stopping service.");
        if (controller != null) {
            controller.setListener(null);
            controller.close();
        }
        stopForeground(true);
        return true;
    });

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        controller = new BulbController(this);
        notificationProvider = new BulbNotificationProvider(this);
        timeToLiveMillis = getResources().getInteger(R.integer.service_lightbulb_ttlinmillis);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null || intent.getAction() == null) {
            return START_NOT_STICKY;
        }

        Log.d(TAG, "Received intent " + intent.getAction());
        stopSelfHandler.sendEmptyMessageDelayed(0, timeToLiveMillis);

        switch (intent.getAction()) {
            case ACTION_ON:
                isBulbOn = true;
                showNotification(controller.getStatus());
                controller.setListener(this);
                controller.turnOn();
                break;
            case ACTION_OFF:
                isBulbOn = false;
                showNotification(controller.getStatus());
                controller.setListener(this);
                controller.turnOff();
                break;
            case ACTION_LEVEL:
                isBulbOn = true;
                int value = intent.getIntExtra(EXTRA_LEVEL_VALUE, 0);
                showNotification(controller.getStatus());
                controller.setListener(this);
                controller.setLevel(value);
                break;
            case ACTION_LEVEL_ANIMATE:
                isBulbOn = true;
                int valueStart = intent.getIntExtra(EXTRA_LEVEL_ANIMATE_START_VALUE, 0);
                int valueEnd = intent.getIntExtra(EXTRA_LEVEL_ANIMATE_END_VALUE, 100);
                long time = intent.getLongExtra(EXTRA_LEVEL_ANIMATE_TIME, 1000);
                showNotification(controller.getStatus());
                controller.setListener(this);
                controller.animateLevel(valueStart, valueEnd, time);
                break;
            case ACTION_DISCONNECT:
                controller.setListener(null);
                controller.close();
                stopSelfHandler.removeCallbacksAndMessages(null);
                stopForeground(true);
                break;
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        stopForeground(true);
        stopSelfHandler.removeCallbacksAndMessages(null);
        controller.setListener(null);
        controller.close();
        controller = null;
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not a bound service");
    }

    @Override
    public void onStatusChanged(Status newStatus) {
        showNotification(newStatus);
    }

    private void showNotification(Status newStatus) {
        Notification notification = notificationProvider.getNotification(newStatus, isBulbOn);
        startForeground(R.string.id_notification, notification);
    }
}
