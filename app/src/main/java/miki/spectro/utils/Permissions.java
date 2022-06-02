package miki.spectro.utils;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

public class Permissions {
    public static boolean checkPermissionStatus(Activity act, String permission) {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && act.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
    }
}
