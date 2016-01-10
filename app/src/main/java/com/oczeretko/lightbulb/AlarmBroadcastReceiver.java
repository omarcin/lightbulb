package com.oczeretko.lightbulb;

import android.content.*;

public class AlarmBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int lightStart = context.getResources().getInteger(R.integer.broadcast_alarm_light_level_start);
        int lightEnd = context.getResources().getInteger(R.integer.broadcast_alarm_light_level_end);
        int animationTime = context.getResources().getInteger(R.integer.broadcast_alarm_animation_time);
        context.startService(LightBulbService.getIntentAnimateLevel(context, lightStart, lightEnd, animationTime));
    }
}
