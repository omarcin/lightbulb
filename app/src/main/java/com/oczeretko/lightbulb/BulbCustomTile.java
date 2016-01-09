package com.oczeretko.lightbulb;

import android.app.*;
import android.content.*;

import cyanogenmod.app.*;

public class BulbCustomTile {

    private final Context context;

    public BulbCustomTile(Context context) {
        this.context = context;
    }

    public void publishTurnOnTile() {
        Intent intent = LightBulbService.getIntentTurnOn(context);
        publishTile(intent, R.string.tile_label_on);
    }

    public void publishTurnOffTile() {
        Intent intent = LightBulbService.getIntentTurnOff(context);
        publishTile(intent, R.string.tile_label_off);
    }

    private void publishTile(Intent intent, int labelStringId) {
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        CustomTile customTile = new CustomTile.Builder(context)
                                    .setOnClickIntent(pendingIntent)
                                    .setContentDescription(R.string.tile_description)
                                    .setLabel(labelStringId)
                                    .shouldCollapsePanel(false)
                                    .setIcon(R.mipmap.ic_launcher)
                                    .build();

        CMStatusBarManager.getInstance(context).publishTile(R.string.id_tile, customTile);
    }
}
