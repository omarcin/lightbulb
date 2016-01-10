package com.oczeretko.lightbulb;

import android.app.*;
import android.content.*;
import android.support.v4.app.*;

public class BulbNotificationProvider {

    private final Context context;

    public BulbNotificationProvider(Context context) {
        this.context = context.getApplicationContext();
    }

    public Notification getNotification(BulbController.Status status, boolean isBulbOn) {
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                                                 .setOngoing(true)
                                                 .setOnlyAlertOnce(true)
                                                 .setSmallIcon(R.drawable.ic_wb_incandescent_black_24dp)
                                                 .setContentTitle(context.getString(R.string.notification_title))
                                                 .setContentIntent(pendingIntent)
                                                 .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        switch (status) {
            case Disconnected:
                builder.setContentText(context.getString(R.string.notification_description_error))
                       .setColor(context.getResources().getColor(R.color.colorAccent))
                       .addAction(getActionTurnOn())
                       .setOngoing(false);
                break;
            case Connected:
                builder.setContentText(context.getString(R.string.notification_description_connected))
                       .setColor(context.getResources().getColor(R.color.colorPrimary))
                       .addAction(isBulbOn ? getActionTurnOff() : getActionTurnOn())
                       .addAction(getActionDisconnect());
                break;
            case Connecting:
                builder.setContentText(context.getString(R.string.notification_description_connecting))
                       .setColor(context.getResources().getColor(R.color.grey))
                       .addAction(getActionDisconnect());
                break;
        }


        return builder.build();
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
