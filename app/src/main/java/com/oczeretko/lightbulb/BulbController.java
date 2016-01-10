package com.oczeretko.lightbulb;

import android.content.*;

import java.util.*;

public class BulbController {

    public enum Status {
        Disconnected,
        Connecting,
        Connected
    }

    public interface StatusChangedListener {
        void onStatusChanged(Status newStatus);
    }

    private final static int MAX_LEVEL = 100;
    private final static int MIN_LEVEL = 1;
    private final static int MAX_VALUE = 0xFF;
    private final static int MIN_VALUE = 0x25;
    private final static byte[] COMMAND_INIT1 = {33, 0, 0, 0, 0, 0, 0, 0, 0};
    private final static byte[] COMMAND_INIT2 = {21, 0, 0, 0, 0, 0, 0, 0, 0};
    private final static int COMMAND_INDEX_LIGHT_LEVEL = 4;
    private final static byte[] COMMAND_TEMPLATE = {20, 0, 0, 0, 0 /* light level */, 0, 0, 0, 0};
    private final static byte[] COMMAND_ON = commandForLightLevel(MAX_LEVEL);
    private final static byte[] COMMAND_OFF = commandForLightLevel(0);

    private final Context context;
    private final BulbBluetoothConnectionListener connectionListener = new BulbBluetoothConnectionListener();
    private BulbBluetoothConnection connection;
    private final Queue<byte[]> commands = new LinkedList<>();
    private boolean isIdle;
    private final Set<StatusChangedListener> listeners = new HashSet<>();
    private Status status;

    public BulbController(Context context) {
        this.context = context;
        status = Status.Disconnected;
    }

    public void close() {
        isIdle = false;
        setStatus(Status.Disconnected);
        commands.clear();
        if (connection != null) {
            connection.close();
            connection = null;
        }
    }

    public void addListener(StatusChangedListener listener) {
        this.listeners.add(listener);
    }

    public void removeListener(StatusChangedListener listener) {
        this.listeners.remove(listener);
    }

    private void setStatus(Status newStatus) {
        status = newStatus;
        ArrayList<StatusChangedListener> listenersLocal = new ArrayList<>(listeners);
        for (StatusChangedListener listener : listenersLocal) {
            listener.onStatusChanged(status);
        }
    }

    public Status getStatus() {
        return status;
    }

    public void turnOn() {
        queueCommand(COMMAND_ON);
    }

    public void turnOff() {
        queueCommand(COMMAND_OFF);
    }

    public void setLevel(int lightLevel) {
        lightLevel = Math.min(Math.max(lightLevel, MIN_LEVEL), MAX_LEVEL);
        byte[] command = commandForLightLevel(lightLevel);
        queueCommand(command);
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
            setStatus(Status.Connecting);
            commands.offer(COMMAND_INIT1);
            commands.offer(COMMAND_INIT2);
        }
    }

    private void executeIfReady() {
        if (isIdle && !commands.isEmpty()) {
            connection.sendCommand(commands.poll());
        }
    }

    private static byte[] commandForLightLevel(int lightLevel) {
        double fraction = ((double)lightLevel) / MAX_LEVEL;
        int commandValue = (int)(fraction * (MAX_VALUE - MIN_VALUE)) + MIN_VALUE;
        byte[] command = Arrays.copyOf(COMMAND_TEMPLATE, COMMAND_TEMPLATE.length);
        command[COMMAND_INDEX_LIGHT_LEVEL] = (byte)commandValue;
        return command;
    }

    private class BulbBluetoothConnectionListener implements BulbBluetoothConnection.Listener {
        @Override
        public void onBulbConnected() {
            isIdle = true;
            setStatus(Status.Connected);
            executeIfReady();
        }

        @Override
        public void onBulbDisconnected() {
            close();
        }

        @Override
        public void onBulbCommandSent() {
            isIdle = true;
            executeIfReady();
        }
    }
}
