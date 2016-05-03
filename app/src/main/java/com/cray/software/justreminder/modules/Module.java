package com.cray.software.justreminder.modules;

import android.os.Build;

import com.cray.software.justreminder.BuildConfig;

/**
 * Helper class for checking type of built application.
 */
public class Module {

    /**
     * Check if application has PRO license.
     * @return boolean
     */
    public static boolean isPro(){
        return BuildConfig.IS_PRO;
    }

    /**
     * Check if application is for Cloud Test.
     * @return boolean
     */
    public static boolean isCloud(){
        return BuildConfig.IS_CLOUD;
    }

    /**
     * Check if device runs on Lollipop and above.
     * @return boolean
     */
    public static boolean isLollipop(){
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    /**
     * Check if device runs on Marshmallow and above.
     * @return boolean
     */
    public static boolean isMarshmallow(){
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    /**
     * Check if device runs on Lollipop and above.
     * @return boolean
     */
    public static boolean isKitkat(){
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    }

    public static boolean isJellyMR2() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2;
    }
}
