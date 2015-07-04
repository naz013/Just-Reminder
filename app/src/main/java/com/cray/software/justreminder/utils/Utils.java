package com.cray.software.justreminder.utils;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.interfaces.Constants;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Utils {

    public static Drawable getDrawable (Context context, int resource){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            return context.getResources().getDrawable(resource, null);
        } else {
            return context.getResources().getDrawable(resource);
        }
    }

    public static int getIcon(String typePrefs){
        int icon;
        if (typePrefs.matches(Constants.TYPE_CALL) ||
                typePrefs.matches(Constants.TYPE_LOCATION_CALL) ||
                typePrefs.matches(Constants.TYPE_LOCATION_OUT_CALL)) {
            icon = R.drawable.ic_call_white_24dp;
        } else if (typePrefs.matches(Constants.TYPE_MESSAGE) ||
                typePrefs.matches(Constants.TYPE_LOCATION_MESSAGE) ||
                typePrefs.matches(Constants.TYPE_LOCATION_OUT_MESSAGE)) {
            icon = R.drawable.ic_message_white_24dp;
        } else if (typePrefs.matches(Constants.TYPE_LOCATION) ||
                typePrefs.matches(Constants.TYPE_LOCATION_OUT)) {
            icon = R.drawable.ic_navigation_white_24dp;
        } else if (typePrefs.matches(Constants.TYPE_TIME)) {
            icon = R.drawable.ic_access_time_white_24dp;
        } else if (typePrefs.startsWith(Constants.TYPE_SKYPE)) {
            icon = R.drawable.skype_icon_white;
        } else if (typePrefs.matches(Constants.TYPE_APPLICATION)) {
            icon = R.drawable.ic_launch_white_24dp;
        } else if (typePrefs.matches(Constants.TYPE_APPLICATION_BROWSER)) {
            icon = R.drawable.ic_public_white_24dp;
        } else {
            icon = R.drawable.ic_event_white_24dp;
        }
        return icon;
    }
}
