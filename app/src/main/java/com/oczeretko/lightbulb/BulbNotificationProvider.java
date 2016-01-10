package com.oczeretko.lightbulb;

import android.app.*;
import android.content.*;
import android.support.v7.app.*;

public class BulbNotificationProvider {

    private final Context context;

    public BulbNotificationProvider(Context context) {
        this.context = context.getApplicationContext();
    }

    public Notification getNotification(boolean isOn) {
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        return new NotificationCompat.Builder(context)
                   .setOngoing(true)
                   .setOnlyAlertOnce(true)
                   .setColor(context.getResources().getColor(R.color.colorPrimary))
                   .setSmallIcon(R.mipmap.ic_launcher)
                   .setContentTitle(context.getString(R.string.notification_title))
                   .setContentText(context.getString(R.string.notification_description_connecting))
                   .setContentIntent(pendingIntent)
                   .addAction(isOn ? getActionTurnOff() : getActionTurnOn())
                   .addAction(getActionDisconnect())
                   .build();
    }

    private NotificationCompat.Action getActionTurnOff() {
        return getAction(LightBulbService.getIntentTurnOff(context),
                         R.drawable.ic_brightness_low_black_24dp,
                         R.string.notification_action_off);
    }

    private NotificationCompat.Action getActionTurnOn() {
        return getAction(LightBulbService.getIntentTurnOn(context),
                         R.drawable.ic_brightness_high_black_24dp,
                         R.string.notification_action_on);
    }

    private NotificationCompat.Action getActionDisconnect() {
        return getAction(LightBulbService.getIntentDisconnect(context),
                         R.drawable.ic_close_black_24dp,
                         R.string.notification_action_disconnect);
    }

    private NotificationCompat.Action getAction(Intent intent, int iconId, int stringId) {
        String string = context.getString(stringId);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return new NotificationCompat.Action.Builder(iconId, string, pendingIntent).build();
    }
}
