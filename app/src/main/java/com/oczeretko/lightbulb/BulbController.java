package com.oczeretko.lightbulb;

import android.content.*;
import android.widget.*;

public class BulbController implements BulbBluetoothConnection.Listener {

    private static byte[] VALUE_INIT1 = {33, 0, 0, 0, 0, 0, 0, 0, 0};
    private static byte[] VALUE_INIT2 = {21, 0, 0, 0, 0, 0, 0, 0, 0};
    private static byte[] VALUE_ON = {20, 0, 0, 0, -1, 0, 0, 0, 0};
    private static byte[] VALUE_OFF = {20, 0, 0, 0, 37, 0, 0, 0, 0};

    private final BulbBluetoothConnection connection;
    private final Context context;

    public BulbController(Context context) {
        this.context = context;
        connection = new BulbBluetoothConnection(context, this);
    }

    public void initialize() {
        connection.open();
    }

    public void close() {
        connection.close();
    }

    public void turnOn() {
        connection.sendCommand(VALUE_ON);
    }

    public void turnOff() {
        connection.sendCommand(VALUE_OFF);
    }

    @Override
    public void onBulbConnected() {
        Toast.makeText(context, "CONNECTED", Toast.LENGTH_SHORT).show();
        connection.sendCommand(VALUE_INIT1);
        connection.sendCommand(VALUE_INIT2);
    }

    @Override
    public void onBulbDisconnected() {
        Toast.makeText(context, "DISCONNECTED", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBulbCommandSent() {
    }
}
