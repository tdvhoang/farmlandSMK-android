/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package hdv.ble.tdx.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.squareup.otto.Bus;

import java.util.UUID;

import javax.inject.Inject;

import hdv.ble.tdx.BLEApplication;
import hdv.ble.tdx.R;
import hdv.ble.tdx.data.BusEvent;
import hdv.ble.tdx.util.CommonUtils;
import hdv.ble.tdx.util.EventPosterHelper;
import hdv.ble.tdx.util.NotificationHelper;

/**
 * Service for managing connection and data communication with a GATT server hosted on a
 * given Bluetooth LE device.
 */
public class BluetoothLeService extends Service {
    private final static String TAG = "nna";
    private static final String UUIDNOTIFY = "6e400003-b5a3-f393-e0a9-e50e24dcca9e";
    public static final UUID CCCD = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    public static final UUID FIRMWARE_REVISON_UUID = UUID.fromString("00002a26-0000-1000-8000-00805f9b34fb");
    public static final UUID DIS_UUID = UUID.fromString("0000180a-0000-1000-8000-00805f9b34fb");
    public static final UUID RX_SERVICE_UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
    public static final UUID RX_CHAR_UUID = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e");
    public static final UUID TX_CHAR_UUID = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e");

    public static final int STATE_DISCONNECTED = 0;
    public static final int STATE_CONNECTING = 1;
    public static final int STATE_CONNECTED = 2;
    public static final int STATE_DISCONVERED = 3;

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress, mAddress;
    private BluetoothGatt mBluetoothGatt;
    private int mConnectionState = STATE_DISCONNECTED;
    private final IBinder mBinder = new LocalBinder();

    @Inject
    Bus mEventBus;
    @Inject
    EventPosterHelper eventPosterHelper;
    private Handler mHandler = new Handler();


    @Override
    public void onCreate() {
        super.onCreate();
        BLEApplication.get(this).getComponent().inject(this);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mEventBus.unregister(this);
    }

    public class LocalBinder extends Binder {
        public BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }

    private void startForeGround(){
        NotificationHelper notificationHelper = new NotificationHelper(this);
        startForeground(NotificationHelper.BLE_SERVICE_NOTIFICATION_ID, notificationHelper.getNotify(""));

    }

    @Override
    public IBinder onBind(Intent intent) {
        startForeGround();
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        close();
        return super.onUnbind(intent);
    }
    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.d(TAG, "onConnectionStateChange " + status);
            if(status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    mConnectionState = STATE_CONNECTED;
                    BusEvent.Connect connected = new BusEvent.Connect();
                    connected.state = STATE_CONNECTED;
                    eventPosterHelper.postEventSafely(connected);
                    mBluetoothGatt.discoverServices();

                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    BusEvent.Connect connected = new BusEvent.Connect();
                    connected.state = STATE_DISCONNECTED;
                    eventPosterHelper.postEventSafely(connected);

                    mConnectionState = STATE_DISCONNECTED;
                    mBluetoothGatt.close();
                    mBluetoothGatt = null;
                }
            }else{
                mConnectionState = STATE_DISCONNECTED;
            }

            if(mConnectionState == STATE_DISCONNECTED) {
                connect(mAddress);
            }

            //start timer to check search ble
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                enableTXNotification();
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        BusEvent.Connect connected = new BusEvent.Connect();
                        connected.state = STATE_DISCONVERED;
                        eventPosterHelper.postEventSafely(connected);

                    }
                }, 1000);
            }
        }
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            Log.d(TAG, "onCharacteristicWrite status" + CommonUtils.convertByteToString(characteristic.getValue()));
            if(status == BluetoothGatt.GATT_SUCCESS){

            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            Log.d(TAG, "onCharacteristicRead ");
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {

            BusEvent.ReceiveData event = new BusEvent.ReceiveData();

            event.values = characteristic.getValue();
            eventPosterHelper.postEventSafely(event);
        }
    };




    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }



    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     *
     * @return Return true if the connection is initiated successfully. The connection result
     *         is reported asynchronously through the
     *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     *         callback.
     */
    public boolean connect(String address) {
        mAddress = address;
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

//        mBluetoothDeviceAddress = null;
        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null
                && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            if (mBluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;

                BusEvent.Connect connected = new BusEvent.Connect();
                connected.state = STATE_CONNECTING;
                eventPosterHelper.postEventSafely(connected);
                return true;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "IkyDevice not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        Log.d(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;

        BusEvent.Connect connected = new BusEvent.Connect();
        connected.state = STATE_CONNECTING;
        eventPosterHelper.postEventSafely(connected);
        return true;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
        mBluetoothDeviceAddress = null;
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }

        BusEvent.Connect connected = new BusEvent.Connect();
        connected.state = STATE_DISCONNECTED;
        eventPosterHelper.postEventSafely(connected);

        mBluetoothGatt.close();
        mAddress = null;
        mBluetoothGatt = null;
    }

    public void enableTXNotification()
    {
    	/*
    	if (mBluetoothGatt == null) {
    		showMessage("mBluetoothGatt null" + mBluetoothGatt);
    		broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
    		return;
    	}
    		*/
        BluetoothGattService RxService = mBluetoothGatt.getService(RX_SERVICE_UUID);
        BluetoothGattCharacteristic TxChar = RxService.getCharacteristic(TX_CHAR_UUID);
        if (TxChar == null) {
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(TxChar,true);

        BluetoothGattDescriptor descriptor = TxChar.getDescriptor(CCCD);
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        mBluetoothGatt.writeDescriptor(descriptor);

    }

    public void writeCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }

        mBluetoothGatt.writeCharacteristic(characteristic);
    }
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }


    public void readStatus(){
        String stringuuid = "6e400003-b5a3-f393-e0a9-e50e24dcca9e";
        if(mBluetoothGatt == null){
            BusEvent.ShowError showError = new BusEvent.ShowError();
            showError.message = getString(R.string.cannot_connect_to_device);
            eventPosterHelper.postEventSafely(showError);

            return;
        }

        for(BluetoothGattService service: mBluetoothGatt.getServices()){
            for(BluetoothGattCharacteristic characteristic:service.getCharacteristics()){
                if(characteristic.getUuid().toString().equals(stringuuid)){
                    readCharacteristic(characteristic);
                    return;
                }
            }

        }

        BusEvent.ShowError showError = new BusEvent.ShowError();
        showError.message = getString(R.string.cannot_connect_to_device);
        eventPosterHelper.postEventSafely(showError);
    }

    public void writeRXCharacteristic(byte[] value)
    {


        BluetoothGattService RxService = mBluetoothGatt.getService(RX_SERVICE_UUID);
        if (RxService == null) {
            BusEvent.ShowError showError = new BusEvent.ShowError();
            showError.message = getString(R.string.cannot_connect_to_device);
            eventPosterHelper.postEventSafely(showError);
            return;
        }
        BluetoothGattCharacteristic RxChar = RxService.getCharacteristic(RX_CHAR_UUID);
        if (RxChar == null) {
            BusEvent.ShowError showError = new BusEvent.ShowError();
            showError.message = getString(R.string.cannot_connect_to_device);
            eventPosterHelper.postEventSafely(showError);
            return;
        }
        RxChar.setValue(value);
        boolean status = mBluetoothGatt.writeCharacteristic(RxChar);

        BusEvent.SendData sendData = new BusEvent.SendData();
        sendData.msg = value;
        eventPosterHelper.postEventSafely(sendData);
        Log.d(TAG, "write TXchar - status=" + status);

    }

    public void enableNotify(boolean value){
        if(mBluetoothGatt == null){
            BusEvent.ShowError showError = new BusEvent.ShowError();
            showError.message = getString(R.string.cannot_connect_to_device);
            eventPosterHelper.postEventSafely(showError);

            return;
        }

        for(BluetoothGattService service: mBluetoothGatt.getServices()){
            for(BluetoothGattCharacteristic characteristic:service.getCharacteristics()){
                if(characteristic.getUuid().toString().equals(UUIDNOTIFY)){

                    mBluetoothGatt.setCharacteristicNotification(characteristic,value);

                    BluetoothGattDescriptor descriptor = characteristic.getDescriptors().get(0);
                    if(value) {
                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    }else{
                        descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);

                    }

                    mBluetoothGatt.writeDescriptor(descriptor);

                    return;
                }
            }

        }

        BusEvent.ShowError showError = new BusEvent.ShowError();
        showError.message = getString(R.string.cannot_connect_to_device);
        eventPosterHelper.postEventSafely(showError);

    }

}
