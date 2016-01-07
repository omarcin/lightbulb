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

    public void connect(View view) {
        controller.initialize();
    }

    public void disconnect(View view) {
        controller.close();
    }
}
