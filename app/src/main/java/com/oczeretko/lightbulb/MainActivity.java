package com.oczeretko.lightbulb;

import android.os.*;
import android.support.v7.app.*;
import android.view.*;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void turn25(View view) {
        startService(LightBulbService.getIntentSetLevel(this, 25));
    }

    public void turn50(View view) {
        startService(LightBulbService.getIntentSetLevel(this, 50));
    }

    public void turn75(View view) {
        startService(LightBulbService.getIntentSetLevel(this, 75));
    }

    public void turn100(View view) {
        startService(LightBulbService.getIntentSetLevel(this, 100));
    }

    public void turnOn(View view) {
        startService(LightBulbService.getIntentTurnOn(this));
    }

    public void turnOff(View view) {
        startService(LightBulbService.getIntentTurnOff(this));
    }

    public void disconnect(View view) {
        startService(LightBulbService.getIntentDisconnect(this));
    }
}
