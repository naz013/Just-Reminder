/**
 * Copyright 2016 Nazar Suhovich
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cray.software.justreminder.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.speech.RecognizerIntent;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.contacts.ContactsActivity;
import com.cray.software.justreminder.apps.ApplicationActivity;
import com.cray.software.justreminder.constants.Language;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.helpers.Messages;
import com.cray.software.justreminder.helpers.SharedPrefs;

public class SuperUtil {

    /**
     * Check if service is already running.
     * @param context application context.
     * @param serviceClass service class.
     * @return boolean
     */
    public static boolean isServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Concatenate many string to single.
     * @param strings string to concatenate.
     * @return concatenated string
     */
    public static String appendString(String... strings){
        StringBuilder stringBuilder = new StringBuilder();
        for (String string : strings){
            if (string != null) {
                stringBuilder.append(string);
            }
        }
        return stringBuilder.toString();
    }

    /**
     * Load list of installed application and show chooser activity.
     * @param activity context activity.
     * @param requestCode result request code.
     */
    public static void selectApplication(final Activity activity, final int requestCode){
        activity.startActivityForResult(new Intent(activity, ApplicationActivity.class), requestCode);
    }

    /**
     * Load list of contacts and show chooser activity.
     * @param activity context activity.
     * @param requestCode result request code.
     */
    public static void selectContact(final Activity activity, final int requestCode){
        activity.startActivityForResult(new Intent(activity, ContactsActivity.class), requestCode);
    }

    /**
     * Get time for timer.
     * @param timeString human readable time string.
     * @return time in milliseconds.
     */
    public static long getAfterTime(String timeString) {
        if (timeString.length() == 6 && !timeString.matches("000000")){
            String hours = timeString.substring(0, 2);
            String minutes = timeString.substring(2, 4);
            String seconds = timeString.substring(4, 6);
            int hour = Integer.parseInt(hours);
            int minute = Integer.parseInt(minutes);
            int sec = Integer.parseInt(seconds);
            long s = 1000;
            long m = s * 60;
            long h = m * 60;
            return (hour * h) + (minute * m) + (sec * s);
        } else return 0;
    }

    /**
     * Start voice listener for recognition.
     * @param activity activity.
     * @param requestCode result request code.
     */
    public static void startVoiceRecognitionActivity(Activity activity, int requestCode, boolean free) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        SharedPrefs sPrefs = new SharedPrefs(activity);
        if (free) intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        else intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Language.getLanguage(sPrefs.loadInt(Prefs.VOICE_LOCALE)));
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, activity.getString(R.string.say_something));
        try {
            activity.startActivityForResult(intent, requestCode);
        } catch (ActivityNotFoundException e){
            Messages.toast(activity, activity.getString(R.string.no_recognizer_found));
        }
    }

    /**
     * Check if application installed on device.
     * @param context Application context.
     * @param packageName package name.
     * @return boolean
     */
    public static boolean isAppInstalled(Context context, String packageName) {
        PackageManager pm = context.getPackageManager();
        boolean installed;
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            installed = false;
        }
        return installed;
    }

    /**
     * Open Google Play market for Skype client installation.
     * @param context application context.
     */
    public static void installSkype(Context context) {
        Uri marketUri = Uri.parse("market://details?id=com.skype.raider");
        Intent intent = new Intent(Intent.ACTION_VIEW, marketUri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Check if Skype client installed on device.
     * @param context application context.
     * @return Boolean
     */
    public static boolean isSkypeClientInstalled(Context context) {
        PackageManager myPackageMgr = context.getPackageManager();
        try {
            myPackageMgr.getPackageInfo("com.skype.raider", PackageManager.GET_ACTIVITIES);
        }
        catch (PackageManager.NameNotFoundException e) {
            return (false);
        }
        return (true);
    }
}
