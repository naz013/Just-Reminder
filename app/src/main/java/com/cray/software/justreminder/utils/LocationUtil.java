package com.cray.software.justreminder.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

public class LocationUtil {
    public static final int ACTIVE = 0;
    public static final int SHOWN = 1;
    public static final int LOCKED = 2;

    public static boolean checkGooglePlayServicesAvailability(Activity a) {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(a.getApplicationContext());
        if(resultCode != ConnectionResult.SUCCESS) {
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, a, 69);
            dialog.setCancelable(false);
            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    dialog.dismiss();
                }
            });
            dialog.show();
            return false;
        } else {
            Log.d("GooglePlayServicesUtil", "Result is: " + resultCode);
            return true;
        }
    }

    public static String getAddress(double currentLat, double currentLong){
        return String.format("%.5f", currentLat) + ", " +
                String.format("%.5f", currentLong);
    }
}
