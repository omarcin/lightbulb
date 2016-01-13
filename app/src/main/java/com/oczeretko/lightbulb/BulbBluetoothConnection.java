package com.oczeretko.lightbulb;

import android.bluetooth.*;
import android.bluetooth.le.*;
import android.content.*;
import android.os.*;
import android.util.*;

import java.util.*;

public class BulbBluetoothConnection {

    public interface Listener {
        void onBulbConnected();

        void onBulbDisconnected();

        void onBulbCommandSent();
    }

    private static final String TAG = "BulbConnection";
    private static final UUID SERVICE_ID = UUID.fromString("0000fe02-0000-1000-8000-00805f9b34fb");
    private static final UUID CHARACTERISTIC_ID = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner leScanner;
    private BluetoothGatt bulbGatt;
    private boolean disableBluetoothOnClose;

    private final Handler handler = new Handler();
    private final BluetoothScanCallback scanCallback = new BluetoothScanCallback();
    private BluetoothStateChangedReceiver stateChangedReceiver;
    private final Context context;
    private final Listener listener;
    private final long scanTimeMillis;

    public BulbBluetoothConnection(Context context, Listener listener) {
        this.context = context;
        this.listener = new ListenerUiThreadAdapter(listener);
        scanTimeMillis = context.getResources().getInteger(R.integer.service_lightbulb_ttlinmillis);
    }

    public void open() {
        stateChangedReceiver = new BluetoothStateChangedReceiver();
        context.registerReceiver(stateChangedReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        BluetoothManager bluetoothManager = (BluetoothManager)context.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter.getState() != BluetoothAdapter.STATE_ON) {
            disableBluetoothOnClose = true;
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
            bulbGatt.close();
            bulbGatt = null;
        }
        tryStopLeScan();
        if (disableBluetoothOnClose) {
            bluetoothAdapter.disable();
        }
    }

    private void tryStopLeScan() {
        if (leScanner != null) {
            try {
                leScanner.stopScan(scanCallback);
                leScanner = null;
            } catch (IllegalStateException ex) {
            }
        }
    }

    private void onBluetoothEnabled() {
        Log.d(TAG, "on Bluetooth enabled");
        if (leScanner == null) {
            Log.d(TAG, "Initiating ble scan.");
            leScanner = bluetoothAdapter.getBluetoothLeScanner();
            ScanFilter scanFilter = new ScanFilter.Builder().setServiceUuid(new ParcelUuid(SERVICE_ID)).build();
            ScanSettings scanSettings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_BALANCED).build();
            leScanner.startScan(Arrays.asList(scanFilter), scanSettings, scanCallback);
            handler.postDelayed(this::onScanTimeout, scanTimeMillis);
        } else {
            Log.d(TAG, "Already scanning");
        }
    }

    private void onScanTimeout() {
        Log.d(TAG, "ble scan timeout");
        tryStopLeScan();
        listener.onBulbDisconnected();
    }

    private void onBluetoothDisabled() {
        Log.d(TAG, "on Bluetooth disabled");
        disableBluetoothOnClose = false;
        listener.onBulbDisconnected();
    }

    private void onBluetoothDeviceFound(BluetoothDevice device) {
        Log.d(TAG, "Bluetooth device found");
        handler.removeCallbacksAndMessages(null);
        leScanner.stopScan(scanCallback);
        leScanner = null;
        bulbGatt = device.connectGatt(context, false, new BluetoothGattCallback() {
            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                Log.d(TAG, "GATT - characteristic write " + status);
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    listener.onBulbCommandSent();
                } else {
                    listener.onBulbDisconnected();
                }
            }

            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                Log.d(TAG, "GATT - state change " + newState);
                if (newState == BluetoothGatt.STATE_CONNECTED) {
                    bulbGatt.discoverServices();
                } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                    listener.onBulbDisconnected();
                }
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                Log.d(TAG, "GATT - Services discovered " + status);
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    onBluetoothDeviceReady();
                } else {
                    listener.onBulbDisconnected();
                }
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
        Log.d(TAG, "Device ready");
        listener.onBulbConnected();
    }

    private class BluetoothStateChangedReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)) {
                case BluetoothAdapter.STATE_ON:
                    Log.d(TAG, "Bluetooth state ON");
                    onBluetoothEnabled();
                    break;
                case BluetoothAdapter.STATE_OFF:
                case BluetoothAdapter.ERROR:
                    Log.d(TAG, "Bluetooth state OFF/ERROR");
                    onBluetoothDisabled();
                    break;
            }
        }
    }

    private class BluetoothScanCallback extends ScanCallback {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Log.d(TAG, "onScanResult " + callbackType);
            onBluetoothDeviceFound(result.getDevice());
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.d(TAG, "onScanFailed " + errorCode);
            listener.onBulbDisconnected();
        }
    }

    private class ListenerUiThreadAdapter implements Listener {

        private final Listener innerListener;

        public ListenerUiThreadAdapter(Listener listener) {
            innerListener = listener;
        }

        @Override
        public void onBulbConnected() {
            handler.post(innerListener::onBulbConnected);
        }

        @Override
        public void onBulbDisconnected() {
            handler.post(innerListener::onBulbDisconnected);
        }

        @Override
        public void onBulbCommandSent() {
            handler.post(innerListener::onBulbCommandSent);
        }
    }
}
