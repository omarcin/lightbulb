package com.oczeretko.lightbulb;

import android.content.*;
import android.widget.*;

import java.util.*;

public class BulbController implements BulbBluetoothConnection.Listener {

    private static byte[] VALUE_INIT1 = {33, 0, 0, 0, 0, 0, 0, 0, 0};
    private static byte[] VALUE_INIT2 = {21, 0, 0, 0, 0, 0, 0, 0, 0};
    private static byte[] VALUE_ON = {20, 0, 0, 0, -1, 0, 0, 0, 0};
    private static byte[] VALUE_OFF = {20, 0, 0, 0, 37, 0, 0, 0, 0};

    private final Context context;
    private BulbBluetoothConnection connection;
    private final Queue<byte[]> commands = new LinkedList<>();
    private boolean isIdle;

    public BulbController(Context context) {
        this.context = context;
    }

    public void close() {
        if (connection != null) {
            connection.close();
            connection = null;
        }
    }

    public void turnOn() {
        queueCommand(VALUE_ON);
    }

    public void turnOff() {
        queueCommand(VALUE_OFF);
    }

    private void queueCommand(byte[] command) {
        ensureConnection();
        commands.offer(command);
        executeIfReady();
    }

    private void ensureConnection() {
        if (connection == null) {
            isIdle = false;
            connection = new BulbBluetoothConnection(context, this);
            connection.open();
            commands.offer(VALUE_INIT1);
            commands.offer(VALUE_INIT2);
        }
    }

    private void executeIfReady() {
        if (isIdle && !commands.isEmpty()) {
            connection.sendCommand(commands.poll());
        }
    }

    @Override
    public void onBulbConnected() {
        Toast.makeText(context, "CONNECTED", Toast.LENGTH_SHORT).show();
        isIdle = true;
        executeIfReady();
    }

    @Override
    public void onBulbDisconnected() {
        Toast.makeText(context, "DISCONNECTED", Toast.LENGTH_SHORT).show();
        // TODO
        close();
    }

    @Override
    public void onBulbCommandSent() {
        isIdle = true;
        executeIfReady();
    }
}
