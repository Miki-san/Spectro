package miki.spectro.utils;

import android.app.Activity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class Permissions {
    public static boolean isReadyForScan(Activity act) {
        return ContextCompat.checkSelfPermission(act, Constants.ACCESS_COARSE) == Constants.GRANTED
                && ContextCompat.checkSelfPermission(act, Constants.BLUETOOTH) == Constants.GRANTED
                && ContextCompat.checkSelfPermission(act, Constants.BLUETOOTH_ADMIN) == Constants.GRANTED
                && Functions.getStatusGps(act);
    }

    public static boolean permissionGranted(Activity act){
        return ContextCompat.checkSelfPermission(act, Constants.WRITE_EXTERNAL) == Constants.GRANTED
                && ContextCompat.checkSelfPermission(act, Constants.READ_EXTERNAL) == Constants.GRANTED
                && ContextCompat.checkSelfPermission(act, Constants.ACCESS_COARSE) == Constants.GRANTED
                && ContextCompat.checkSelfPermission(act, Constants.BLUETOOTH) == Constants.GRANTED
                && ContextCompat.checkSelfPermission(act, Constants.BLUETOOTH_ADMIN) == Constants.GRANTED
                && ContextCompat.checkSelfPermission(act, Constants.ACCESS_FINE) == Constants.GRANTED;
    }

    public static void requestPermission(Activity act){
        ActivityCompat.requestPermissions(act, new String[]{
                Constants.WRITE_EXTERNAL,
                Constants.READ_EXTERNAL,
                Constants.ACCESS_COARSE,
                Constants.BLUETOOTH,
                Constants.BLUETOOTH_ADMIN,
                Constants.ACCESS_FINE
        }, 1);
    }
}
