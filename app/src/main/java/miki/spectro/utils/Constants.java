package miki.spectro.utils;

import android.Manifest;
import android.content.pm.PackageManager;

public class Constants {
    public static final String SERVICE          = "0000FFE0-0000-1000-8000-00805F9B34FB";
    public static final String CHARACTERISTIC   = "0000FFE1-0000-1000-8000-00805F9B34FB";
    public static final String TIME             = "50";
    public static final String WRITE_EXTERNAL   = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    public static final String READ_EXTERNAL    = Manifest.permission.READ_EXTERNAL_STORAGE;
    public static final String ACCESS_COARSE    = Manifest.permission.ACCESS_COARSE_LOCATION;
    public static final String BLUETOOTH        = Manifest.permission.BLUETOOTH;
    public static final String BLUETOOTH_ADMIN  = Manifest.permission.BLUETOOTH_ADMIN;
    public static final String ACCESS_FINE      = Manifest.permission.ACCESS_FINE_LOCATION;
    public static final int GRANTED             = PackageManager.PERMISSION_GRANTED;
}
