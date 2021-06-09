package com.favepc.reader.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

/**
 * Created by Bruce_Chiang on 2017/3/10.
 */

public class BluetoothService  extends Service {

    public static final String BLE_ACTION_SERVICE_START = "BLE_ACTION_SERVICE_START";
    public static final String BLE_ACTION_SERVICE_STOP = "BLE_ACTION_SERVICE_STOP";
    public static final String BLE_ACTION_CONNECT = "BLE_ACTION_CONNECT";
    public static final String BLE_ACTION_SEND_DATA	= "BLE_ACTION_SEND_DATA";
    public static final String BLE_ACTION_RECEIVE_DATA	= "BLE_ACTION_RECEIVE_DATA";
    public static final String BLE_ACTION_DISCONNECT = "BLE_ACTION_DISCONNECT";
    public static final String BLE_ACTION_GATT_CONNECTED = "BLE_ACTION_GATT_CONNECTED";
    public static final String BLE_ACTION_GATT_DISCONNECTED = "BLE_ACTION_GATT_DISCONNECTED";
    public static final String BLE_ACTION_CHANGE_INTERFACE = "BLE_ACTION_CHANGE_INTERFACE";

    public static final String INTERFACE_BLE = "INTERFACE_BLE";
    public static final String DEVICE_ADDRESS = "DEVICE_ADDRESS";
    public static final String BYTES_DATA = "BYTES_DATA";
    public static final String STRING_DATA = "STRING_DATA";

    public static final int REQUEST_ENABLE_BLUETOOTH = 1;

    private MsgBLEReceiver mMsgBLEReceiver;
    private BluetoothGatt mBluetoothGatt;
    private BluetoothGattCharacteristic mWriteGattCharacteristic = null, mReadGattCharacteristic = null;
    private BluetoothDevice mBluetoothDevice = null;
    private boolean mLeConnected = false;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.mMsgBLEReceiver = new MsgBLEReceiver();
        registerReceiver(mMsgBLEReceiver, new IntentFilter(BLE_ACTION_SERVICE_START));
        registerReceiver(mMsgBLEReceiver, new IntentFilter(BLE_ACTION_SERVICE_STOP));
        registerReceiver(mMsgBLEReceiver, new IntentFilter(BLE_ACTION_CONNECT));
        registerReceiver(mMsgBLEReceiver, new IntentFilter(BLE_ACTION_SEND_DATA));
        registerReceiver(mMsgBLEReceiver, new IntentFilter(BLE_ACTION_DISCONNECT));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mBluetoothGatt != null) {
            mBluetoothGatt.disconnect();
            mBluetoothGatt.close();
        }
        unregisterReceiver(mMsgBLEReceiver);
    }


    public class MsgBLEReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case BLE_ACTION_SERVICE_START:
                    break;
                case BLE_ACTION_SERVICE_STOP:
                    if(mBluetoothGatt != null) {
                        mBluetoothGatt.disconnect();
                        mBluetoothGatt.close();
                    }
                    mLeConnected = false;
                    break;
                case BLE_ACTION_CONNECT:
                    if (!mLeConnected) {
                        mBluetoothDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(intent.getExtras().getString(DEVICE_ADDRESS));
                        connectTo(mBluetoothDevice);
                    }
                    break;
                case BLE_ACTION_SEND_DATA:
                    byte[] _data = intent.getExtras().getByteArray(BYTES_DATA);
                    sendData(_data);
                    break;
                case BLE_ACTION_DISCONNECT:
                    if(mBluetoothGatt != null) mBluetoothGatt.close();
                    break;
            }
        }
    }


    private synchronized void connectTo(@NonNull BluetoothDevice device) {

        this.mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
    }

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState)
        {
            super.onConnectionStateChange(gatt, status, newState);
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                sendBroadcast(BLE_ACTION_GATT_CONNECTED, "Connected to GATT server.");
                mBluetoothGatt.discoverServices();
                mLeConnected = true;
            }
            else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                sendBroadcast(BLE_ACTION_GATT_DISCONNECTED, "Disconnected from GATT server.");
                mBluetoothGatt.disconnect();
                mBluetoothGatt.close();
                mLeConnected = false;
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            List<BluetoothGattService> _gattServices = gatt.getServices();
            BluetoothGattService _targetGattService = null;
            String _uuid;

            for (BluetoothGattService gattService : _gattServices) {
                _uuid = gattService.getUuid().toString();
                if(DefineGattAttributes.lookUpService(_uuid)) {
                    _targetGattService = gattService;
                    break;
                }
            }

            if (_targetGattService != null) {

                _uuid = _targetGattService.getUuid().toString();
                List<BluetoothGattCharacteristic> characteristic = _targetGattService.getCharacteristics();

                switch(_uuid) {
                    case DefineGattAttributes.TELINK_SPP_UUID_SERVICE:
                        mWriteGattCharacteristic = _targetGattService.getCharacteristic(DefineGattAttributes.TELINK_WRITE_UUID);
                        mReadGattCharacteristic = _targetGattService.getCharacteristic(DefineGattAttributes.TELINK_READ_UUID);
                        break;
                    case DefineGattAttributes.SPP_UUID_SERVICE:
                        mWriteGattCharacteristic = characteristic.get(3);
                        mReadGattCharacteristic = characteristic.get(0);
                        break;
                    case DefineGattAttributes.SLICON_BLE_UUID_SERVICE:
                    default:
                        mWriteGattCharacteristic = _targetGattService.getCharacteristic(DefineGattAttributes.SLICON_WRITE_UUID);
                        mReadGattCharacteristic = _targetGattService.getCharacteristic(DefineGattAttributes.SLICON_READ_UUID);
                            break;
                }
                /*if (_uuid.contains("49535343")) {
                    mWriteGattCharacteristic = characteristic.get(3);
                    mReadGattCharacteristic = characteristic.get(0);
                    //mWriteGattCharacteristic = _targetGattService.getCharacteristic(DefineGattAttributes.WRITE_UUID);
                    //mReadGattCharacteristic = _targetGattService.getCharacteristic(DefineGattAttributes.READ_UUID);
                }
                else {
                    mWriteGattCharacteristic = _targetGattService.getCharacteristic(DefineGattAttributes.TELINK_WRITE_UUID);
                    mReadGattCharacteristic = _targetGattService.getCharacteristic(DefineGattAttributes.TELINK_READ_UUID);
                }*/

                if(mReadGattCharacteristic != null) {
                    boolean _b = gatt.setCharacteristicNotification(mReadGattCharacteristic, true);

                    if(_b) {
                        List<BluetoothGattDescriptor> descriptorList = mReadGattCharacteristic.getDescriptors();
                        if(descriptorList != null && descriptorList.size() > 0) {
                            for(BluetoothGattDescriptor descriptor : descriptorList) {
                                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                                mBluetoothGatt.writeDescriptor(descriptor);
                            }
                        }
                    }
                }

            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                byte[] data = characteristic.getValue();
                if (data != null && data.length > 0 && data[0] != 0) {
                    sendBroadcast(BLE_ACTION_RECEIVE_DATA, data);
                }
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            if (mReadGattCharacteristic.getUuid().equals(characteristic.getUuid())) {
                byte[] data = characteristic.getValue();
                if (data != null && data.length > 0) {
                    sendBroadcast(BLE_ACTION_RECEIVE_DATA, data);
                }
            }
        }
    };

    private synchronized void sendData(@NonNull byte[] value) {
        if(mWriteGattCharacteristic != null && mBluetoothGatt != null && mLeConnected == true) {
            int _targetLen = 0;
            int offset=0;
            for(int len = value.length; len > 0; len -= 20) {
                if(len < 20)
                    _targetLen = len;
                else
                    _targetLen = 20;
                byte[] _targetByte = new byte[_targetLen];
                System.arraycopy(value, offset, _targetByte, 0, _targetLen);
                offset += 20;
                mWriteGattCharacteristic.setValue(_targetByte);
                mBluetoothGatt.writeCharacteristic(mWriteGattCharacteristic);
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if(mReadGattCharacteristic != null)
                //gatt.setCharacteristicNotification(mReadGattCharacteristic, true);
                mBluetoothGatt.readCharacteristic(mReadGattCharacteristic);
        }
    }

    private void sendBroadcast(String action, @NonNull byte[] data) {
        Intent i = new Intent(action);
        i.putExtra(BYTES_DATA, data);
        sendBroadcast(i);
    }

    private void sendBroadcast(String action, @NonNull String data) {
        Intent i = new Intent(action);
        i.putExtra(STRING_DATA, data);
        sendBroadcast(i);
    }
}
