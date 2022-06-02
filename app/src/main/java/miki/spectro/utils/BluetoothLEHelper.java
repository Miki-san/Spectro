package miki.spectro.utils;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;
import miki.spectro.interfaces.BleCallback;
import miki.spectro.models.BluetoothLE;

public class BluetoothLEHelper{

    public Activity act;

    public final ArrayList<BluetoothLE> aDevices     = new ArrayList<>();

    public BluetoothGatt    mBluetoothGatt;
    public BluetoothAdapter mBluetoothAdapter;
    public BluetoothGattCharacteristic mCharacteristic;
    public BleCallback bleCallback;

    public static final int STATE_DISCONNECTED = 0;
    public static final int STATE_CONNECTED    = 1;
    public int              mConnectionState   = STATE_DISCONNECTED;

    public static long SCAN_PERIOD             = 2500;
    public static boolean mScanning            = false;
    public static String FILTER_SERVICE        = "";
    public int packetSize;
    public byte[][] packets;
    public int packetIteration;

    public BluetoothLEHelper(Activity _act){
        if(Functions.isBleSupported(_act)) {
            act = _act;
            BluetoothManager bluetoothManager = (BluetoothManager) act.getSystemService(Context.BLUETOOTH_SERVICE);
            if (bluetoothManager != null) {
                mBluetoothAdapter = bluetoothManager.getAdapter();
            }
            mBluetoothAdapter.enable();
        }
    }

    public void scanLeDevice(boolean enable) {
        Handler mHandler = new Handler();

        if (enable) {
            mScanning = true;

            mHandler.postDelayed(() -> {
                mScanning = false;
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
            }, SCAN_PERIOD);

            if(!FILTER_SERVICE.equals("")) {
                UUID[] filter  = new UUID[1];
                filter [0]     = UUID.fromString(FILTER_SERVICE);
                mBluetoothAdapter.startLeScan(filter, mLeScanCallback);
            }else{
                mBluetoothAdapter.startLeScan(mLeScanCallback);
            }

        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }

    private final BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
            act.runOnUiThread(() -> {
                if(aDevices.size() > 0) {

                    boolean isNewItem = true;

                    for (int i = 0; i < aDevices.size(); i++) {
                        if (aDevices.get(i).getMacAddress().equals(device.getAddress())) {
                            isNewItem = false;
                        }
                    }

                    if(isNewItem) {
                        aDevices.add(new BluetoothLE(device.getName(), device.getAddress(), rssi, device));
                    }

                }else{
                    aDevices.add(new BluetoothLE(device.getName(), device.getAddress(), rssi, device));
                }
            });
        }
    };

    public ArrayList<BluetoothLE> getListDevices(){
        return aDevices;
    }

    public void connect(BluetoothDevice device, BleCallback bleCallback){
        if (mBluetoothGatt == null && !isConnected()) {
            this.bleCallback = bleCallback;
            mBluetoothGatt = device.connectGatt(act, false, mGattCallback);
        }
    }

    public void disconnect(){
        if (mBluetoothGatt != null && isConnected()) {
            mBluetoothGatt.close();
            mBluetoothGatt = null;
        }
    }

    public boolean isReadyForScan(){

        return Permissions.checkPermissionStatus(act, Manifest.permission.BLUETOOTH)
                && Permissions.checkPermissionStatus(act, Manifest.permission.BLUETOOTH_ADMIN)
                && Permissions.checkPermissionStatus(act, Manifest.permission.ACCESS_COARSE_LOCATION) && Functions.getStatusGps(act);
    }

    public void write(byte[] data){
        mCharacteristic = mBluetoothGatt.getService(UUID.fromString(Constants.SERVICE)).getCharacteristic(UUID.fromString(Constants.CHARACTERISTIC));
        int chunksize = 20;
        packetSize = (int) Math.ceil( data.length / (double)chunksize);
        mCharacteristic.setValue(Integer.toString(packetSize).getBytes());
        mBluetoothGatt.writeCharacteristic(mCharacteristic);
        mBluetoothGatt.executeReliableWrite();

        packets = new byte[packetSize][chunksize];
        packetIteration = 0;
        int start = 0;
        for(int i = 0; i < packets.length; i++) {
            int end = start+chunksize;
            if(end>data.length){end = data.length;}
            packets[i] = Arrays.copyOfRange(data, start, end);
            start += chunksize;
        }
        Log.i("TAG", "BluetoothLEHelper: " + new String(data));
    }

    public void write(String aData){
        //mCharacteristic = mBluetoothGatt.getService(UUID.fromString(Constants.SERVICE)).getCharacteristic(UUID.fromString(Constants.CHARACTERISTIC));
        //mCharacteristic.setValue(aData);
        //mBluetoothGatt.writeCharacteristic(mCharacteristic);
        //
        //Log.i("TAG", aData);
    }

    public void read(){
        mCharacteristic = mBluetoothGatt.getService(UUID.fromString(Constants.SERVICE)).getCharacteristic(UUID.fromString(Constants.CHARACTERISTIC));
        mBluetoothGatt.readCharacteristic(mCharacteristic);
    }

    private final BluetoothGattCallback mGattCallback;
    {
        mGattCallback = new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    //act.runOnUiThread(() -> Toast.makeText(act, "Connected to GATT server.", Toast.LENGTH_SHORT).show());
                    Log.i("TAG", "BluetoothLEHelper: Attempting to start service discovery: " + mBluetoothGatt.discoverServices());
                    mConnectionState = STATE_CONNECTED;

                }else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    //act.runOnUiThread(() ->Toast.makeText(act, "Disconnected from GATT server.", Toast.LENGTH_SHORT).show());
                    mConnectionState = STATE_DISCONNECTED;
                }
                bleCallback.onBleConnectionStateChange(gatt, status, newState);
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                if (status != BluetoothGatt.GATT_SUCCESS) {
                    Log.e("TAG","BluetoothLEHelper: onServicesDiscovered received: " + status);
                }
                bleCallback.onBleServiceDiscovered(gatt, status);

            }

            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                //act.runOnUiThread(() ->Toast.makeText(act, "onCharacteristicWrite Status : " + status, Toast.LENGTH_SHORT).show());
                if(packetIteration <packetSize){
                    mCharacteristic.setValue(packets[packetIteration]);
                    mBluetoothGatt.writeCharacteristic(mCharacteristic);
                    packetIteration++;
                }else{
                    mCharacteristic = mBluetoothGatt.getService(UUID.fromString(Constants.SERVICE)).getCharacteristic(UUID.fromString(Constants.CHARACTERISTIC));
                    mBluetoothGatt.setCharacteristicNotification(mCharacteristic, true);
                }

                bleCallback.onBleWrite(gatt, characteristic, status);
            }

            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.i("TAG", "BluetoothLEHelper: " + characteristic.getStringValue(0));
                }
                bleCallback.onBleRead(gatt, characteristic, status);
            }

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                //act.runOnUiThread(() ->Toast.makeText(act, characteristic.getStringValue(0), Toast.LENGTH_SHORT).show());
                //Log.i("TAG","onCharacteristicChanged Value: " + characteristic.getStringValue(0));
                bleCallback.onBleCharacteristicChange(gatt, characteristic);
            }

        };
    }

    public boolean isConnected(){
        return mConnectionState == STATE_CONNECTED;
    }

    public boolean isScanning(){
        return mScanning;
    }

    public void setScanPeriod(int scanPeriod){
        SCAN_PERIOD = scanPeriod;
    }

    public long getScanPeriod(){
        return SCAN_PERIOD;
    }

    public void setFilterService(String filterService){
        FILTER_SERVICE = filterService;
    }

    public BluetoothGatt getGatt(){
        return mBluetoothGatt;
    }

}