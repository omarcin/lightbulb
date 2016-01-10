package com.oczeretko.lightbulb;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.format.*;

public class AlarmBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(LightBulbService.getIntentAnimateLevel(context, 1, 100, 3 * DateUtils.MINUTE_IN_MILLIS));
    }
}
