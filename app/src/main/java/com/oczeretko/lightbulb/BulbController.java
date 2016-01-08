package com.oczeretko.lightbulb;

import android.content.*;

import java.util.*;

public class BulbController {

    private final static int MAX_LEVEL = 100;
    private final static int MIN_LEVEL = 1;

    private final static int MAX_VALUE = 0xFF;
    private final static int MIN_VALUE = 0x25;

    private final static byte[] COMMAND_INIT1 = {33, 0, 0, 0, 0, 0, 0, 0, 0};
    private final static byte[] COMMAND_INIT2 = {21, 0, 0, 0, 0, 0, 0, 0, 0};

    private final static int COMMAND_INDEX_LIGHT_LEVEL = 4;
    private final static byte[] COMMAND_TEMPLATE = {20, 0, 0, 0, 0 /* light level */, 0, 0, 0, 0};
    private final static byte[] COMMAND_ON = commandForValue(MAX_VALUE);
    private final static byte[] COMMAND_OFF = commandForValue(MIN_VALUE);

    private final Context context;
    private final BulbBluetoothConnectionListener connectionListener = new BulbBluetoothConnectionListener();
    private BulbBluetoothConnection connection;
    private final Queue<byte[]> commands = new LinkedList<>();
    private boolean isIdle;

    public BulbController(Context context) {
        this.context = context;
    }

    public void close() {
        commands.clear();
        if (connection != null) {
            connection.close();
            connection = null;
        }
    }

    public void turnOn() {
        queueCommand(COMMAND_ON);
    }

    public void turnOff() {
        queueCommand(COMMAND_OFF);
    }

    private void queueCommand(byte[] command) {
        ensureConnection();
        commands.offer(command);
        executeIfReady();
    }

    private void ensureConnection() {
        if (connection == null) {
            isIdle = false;
            connection = new BulbBluetoothConnection(context, connectionListener);
            connection.open();
            commands.offer(COMMAND_INIT1);
            commands.offer(COMMAND_INIT2);
        }
    }

    private void executeIfReady() {
        if (isIdle && !commands.isEmpty()) {
            connection.sendCommand(commands.poll());
        }
    }

    public void setLevel(int value) {
        value = Math.min(Math.max(value, MIN_LEVEL), MAX_LEVEL);
        byte[] command = commandForValue(value);
        queueCommand(command);
    }

    private static byte[] commandForValue(int value) {
        double fraction = ((double)value) / MAX_VALUE;
        int commandValue = (int)(fraction * (MAX_VALUE - MIN_VALUE)) + MIN_VALUE;
        byte[] command = Arrays.copyOf(COMMAND_TEMPLATE, COMMAND_TEMPLATE.length);
        command[COMMAND_INDEX_LIGHT_LEVEL] = (byte)commandValue;
        return command;
    }

    private class BulbBluetoothConnectionListener implements BulbBluetoothConnection.Listener {
        @Override
        public void onBulbConnected() {
            isIdle = true;
            executeIfReady();
        }

        @Override
        public void onBulbDisconnected() {
            // TODO
            close();
        }

        @Override
        public void onBulbCommandSent() {
            isIdle = true;
            executeIfReady();
        }
    }
}
