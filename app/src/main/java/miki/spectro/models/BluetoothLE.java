package miki.spectro.models;

import android.bluetooth.BluetoothDevice;

public class BluetoothLE {

    private String name;
    private String macAddress;
    private BluetoothDevice device;

    public BluetoothLE(String name, String macAddress, BluetoothDevice device) {
        this.name       = name;
        this.macAddress = macAddress;
        this.device     = device;
    }

    @Override
    public String toString() {
        return name + " MAC: " + macAddress;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public BluetoothDevice getDevice() {
        return device;
    }

    public void setDevice(BluetoothDevice device) {
        this.device = device;
    }
}
