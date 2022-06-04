package miki.spectro.utils;

import android.app.Activity;
import android.os.Handler;
import android.util.Log;
import java.util.Objects;
import miki.spectro.interfaces.BleCallback;

public class SimpleConnection {
    public BluetoothLEHelper ble;
    public Activity activity;
    public String deviceName;
    public BleCallback bleCallback;

    public SimpleConnection(Activity activity, String deviceName, BleCallback bleCallback) {
        this.activity = activity;
        this.deviceName = deviceName;
        this.bleCallback = bleCallback;
        ble = new BluetoothLEHelper(this.activity);
        scanCollars();
    }

    public void scanCollars(){
        if(!ble.isScanning()) {
            Handler mHandler = new Handler();
            ble.scanLeDevice(true);
            mHandler.postDelayed(this::connectDevice,ble.getScanPeriod());
        }
    }

    public void connectDevice(){
        if(ble.getListDevices().size() > 0){
            for (int i=0; i < ble.getListDevices().size(); i++) {
                if(Objects.equals(ble.getListDevices().get(i).getName(), this.deviceName)){
                    Log.i("TAG", "SimpleConnection: Connection...");
                    ble.connect(ble.getListDevices().get(i).getDevice(), bleCallback);
                }
            }
        }
    }
}
