package com.oczeretko.lightbulb;

import android.content.*;

public class BulbController implements BulbBluetoothConnection.Listener {

    private static byte[] VALUE_INIT1 = {33, 0, 0, 0, 0, 0, 0, 0, 0};
    private static byte[] VALUE_INIT2 = {21, 0, 0, 0, 0, 0, 0, 0, 0};
    private static byte[] VALUE_ON = {20, 0, 0, 0, -1, 0, 0, 0, 0};
    private static byte[] VALUE_OFF = {20, 0, 0, 0, 37, 0, 0, 0, 0};

    private final BulbBluetoothConnection connection;

    public BulbController(Context context) {
        connection = new BulbBluetoothConnection(context, this);
    }

    public void initialize() {
        connection.open();
    }

    public void close() {
        connection.close();
    }

    @Override
    public void onBulbConnected() {
    }

    @Override
    public void onBulbCommandSent() {
    }
}
