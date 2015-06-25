package com.cray.software.justreminder.utils;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Utils {
    public static final SimpleDateFormat format24 = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
    public static final SimpleDateFormat format12 = new SimpleDateFormat("dd MMM yyyy, K:mm a", Locale.getDefault());
    public static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
    public static final SimpleDateFormat time24 = new SimpleDateFormat("HH:mm", Locale.getDefault());
    public static final SimpleDateFormat time12 = new SimpleDateFormat("K:mm a", Locale.getDefault());

    public static Typeface getThinTypeface(Context context){
        return Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Thin.ttf");
    }

    public static Typeface getThinTypeface(Context context, boolean italic){
        if (italic) return Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-ThinItalic.ttf");
        else return Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Thin.ttf");
    }

    public static Typeface getLightTypeface(Context context){
        return Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Light.ttf");
    }

    public static Typeface getLightTypeface(Context context, boolean italic){
        if (italic) return Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-LightItalic.ttf");
        else return Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Light.ttf");
    }

    public static Typeface getMediumTypeface(Context context){
        return Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Medium.ttf");
    }

    public static Typeface getMediumTypeface(Context context, boolean italic){
        if (italic) return Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-MediumItalic.ttf");
        else return Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Medium.ttf");
    }

    public static Drawable getDrawable (Context context, int resource){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            return context.getResources().getDrawable(resource, null);
        } else {
            return context.getResources().getDrawable(resource);
        }
    }

    public static String getDateTime(Date date, boolean is24){
        if (is24) return format24.format(date);
        else return format12.format(date);
    }

    public static String getTime(Date date, boolean is24){
        if (is24) return time24.format(date);
        else return time12.format(date);
    }

    public static String generateAfterString(long time){
        long s = 1000;
        long m = s * 60;
        long h = m * 60;
        long hours = (time / h);
        long minutes = ((time - hours * h) / (m));
        long seconds = ((time - (hours * h) - (minutes * m)) / (s));
        String hourStr;
        if (hours < 10) hourStr = "0" + hours;
        else hourStr = String.valueOf(hours);
        String minuteStr;
        if (minutes < 10) minuteStr = "0" + minutes;
        else minuteStr = String.valueOf(minutes);
        String secondStr;
        if (seconds < 10) secondStr = "0" + seconds;
        else secondStr = String.valueOf(seconds);
        return hourStr + minuteStr + secondStr;
    }
}
