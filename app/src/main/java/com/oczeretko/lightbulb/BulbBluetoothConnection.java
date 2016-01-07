package com.oczeretko.lightbulb;

import android.bluetooth.*;
import android.bluetooth.le.*;
import android.content.*;
import android.os.*;
import android.text.format.*;

import java.util.*;

public class BulbBluetoothConnection {

    public interface Listener {
        void onBulbConnected();

        void onBulbCommandSent();
    }

    private static final long SCAN_TIME = 30 * DateUtils.SECOND_IN_MILLIS;
    private static final UUID SERVICE_ID = UUID.fromString("0000fe02-0000-1000-8000-00805f9b34fb");
    private static final UUID CHARACTERISTIC_ID = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner leScanner;
    private BluetoothGatt bulbGatt;
    private boolean wasBluetoothDisabled;

    private final Handler handler = new Handler();
    private final BluetoothScanCallback scanCallback = new BluetoothScanCallback();
    private BluetoothStateChangedReceiver stateChangedReceiver;
    private final Context context;
    private final Listener listener;

    public BulbBluetoothConnection(Context context, Listener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void open() {
        stateChangedReceiver = new BluetoothStateChangedReceiver();
        context.registerReceiver(stateChangedReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        BluetoothManager bluetoothManager = (BluetoothManager)context.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter.getState() != BluetoothAdapter.STATE_ON) {
            wasBluetoothDisabled = true;
            bluetoothAdapter.enable();
        } else {
            onBluetoothEnabled();
        }
    }

    public void close() {
        handler.removeCallbacksAndMessages(null);
        if (stateChangedReceiver != null) {
            context.unregisterReceiver(stateChangedReceiver);
            stateChangedReceiver = null;
        }
        if (bulbGatt != null) {
            bulbGatt.disconnect();
            bulbGatt = null;
        }
        if (leScanner != null) {
            leScanner.stopScan(scanCallback);
            leScanner = null;
        }
        if (wasBluetoothDisabled) {
            bluetoothAdapter.disable();
        }
    }

    private void onBluetoothEnabled() {
        leScanner = bluetoothAdapter.getBluetoothLeScanner();
        ScanFilter scanFilter = new ScanFilter.Builder().setServiceUuid(new ParcelUuid(SERVICE_ID)).build();
        ScanSettings scanSettings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_BALANCED).build();
        leScanner.startScan(Arrays.asList(scanFilter), scanSettings, scanCallback);
        handler.postDelayed(() -> leScanner.stopScan(scanCallback), SCAN_TIME);
    }

    private void onBluetoothDisabled() {
        // TODO
    }

    private void onBluetoothDeviceFound(BluetoothDevice device) {
        handler.removeCallbacksAndMessages(null);
        leScanner.stopScan(scanCallback);
        leScanner = null;
        bulbGatt = device.connectGatt(context, false, new BluetoothGattCallback() {
            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                listener.onBulbCommandSent();
            }

            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                if (newState == BluetoothGatt.STATE_CONNECTED) {
                    bulbGatt.discoverServices();
                } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                    // TODO
                }
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                onBluetoothDeviceReady();
            }
        });
    }

    public void sendCommand(byte[] command) {
        BluetoothGattService service = bulbGatt.getService(SERVICE_ID);
        BluetoothGattCharacteristic characteristic = service.getCharacteristic(CHARACTERISTIC_ID);
        characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        characteristic.setValue(command);
        bulbGatt.writeCharacteristic(characteristic);
    }

    private void onBluetoothDeviceReady() {
        listener.onBulbConnected();
    }

    private class BluetoothStateChangedReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)) {
                case BluetoothAdapter.STATE_ON:
                    onBluetoothEnabled();
                    break;
                default:
                    onBluetoothDisabled();
                    break;
            }
        }
    }

    private class BluetoothScanCallback extends ScanCallback {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            onBluetoothDeviceFound(result.getDevice());
        }
    }
}
