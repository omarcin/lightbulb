package com.oczeretko.lightbulb;

import android.os.*;
import android.support.v7.app.*;
import android.view.*;

public class MainActivity extends AppCompatActivity {

    private BulbController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();
        controller = new BulbController(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        controller.close();
        controller = null;
    }

    public void disconnect(View view) {
        controller.close();
    }

    public void turnOn(View view) {
        controller.turnOn();
    }

    public void turnOff(View view) {
        controller.turnOff();
    }
}
