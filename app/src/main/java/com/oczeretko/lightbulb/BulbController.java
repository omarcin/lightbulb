package com.oczeretko.lightbulb;

import android.content.*;
import android.os.*;

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
    private final Handler commandsHandler;
    private boolean isIdle;
    private StatusChangedListener listener;
    private Status status;
    private final int animationSteps;

    public BulbController(Context context) {
        this.context = context;
        commandsHandler = new Handler(context.getMainLooper(), this::handleCommandMessage);
        status = Status.Disconnected;
        animationSteps = context.getResources().getInteger(R.integer.controller_bulb_animation_steps);
    }

    private boolean handleCommandMessage(Message message) {
        int level = message.what;
        queueCommand(commandForLightLevel(level));
        return true;
    }

    public void close() {
        isIdle = false;
        setStatus(Status.Disconnected);
        commandsHandler.removeCallbacksAndMessages(null);
        commands.clear();
        if (connection != null) {
            connection.close();
            connection = null;
        }
    }

    public void setListener(StatusChangedListener listener) {
        this.listener = listener;
    }

    private void setStatus(Status newStatus) {
        status = newStatus;
        if (listener != null) {
            listener.onStatusChanged(status);
        }
    }

    public Status getStatus() {
        return status;
    }

    public void turnOn() {
        commandsHandler.removeCallbacksAndMessages(null);
        queueCommand(COMMAND_ON);
    }

    public void turnOff() {
        commandsHandler.removeCallbacksAndMessages(null);
        queueCommand(COMMAND_OFF);
    }

    public void setLevel(int lightLevel) {
        lightLevel = Math.min(Math.max(lightLevel, MIN_LEVEL), MAX_LEVEL);
        byte[] command = commandForLightLevel(lightLevel);
        commandsHandler.removeCallbacksAndMessages(null);
        queueCommand(command);
    }

    public void animateLevel(int valueStart, int valueEnd, long time) {
        for (int i = 1; i <= animationSteps; i++) {
            int value = (int)((double)i * (valueEnd - valueStart) / animationSteps + valueStart);
            long delay = (long)((double)(i - 1) / animationSteps * time);
            commandsHandler.sendEmptyMessageDelayed(value, delay);
        }
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
