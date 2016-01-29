package com.cray.software.justreminder.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.speech.RecognizerIntent;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.activities.ContactsList;
import com.cray.software.justreminder.activities.SelectApplication;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.helpers.Messages;
import com.cray.software.justreminder.helpers.SharedPrefs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Copyright 2015 Nazar Suhovich
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
            stringBuilder.append(string);
        }
        return stringBuilder.toString();
    }

    /**
     * Load list of installed application and show chooser activity.
     * @param activity context activity.
     * @param requestCode result request code.
     */
    public static void selectApplication(final Activity activity, final int requestCode){
        class Async extends AsyncTask<Void, Void, Void>{

            private ProgressDialog pd;
            private ArrayList<String> contacts;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                pd = ProgressDialog.show(activity, null, activity.getString(R.string.please_wait), true);
            }

            @Override
            protected Void doInBackground(Void... params) {
                contacts = new ArrayList<>();
                contacts.clear();
                final PackageManager pm = activity.getPackageManager();
                List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

                for (ApplicationInfo packageInfo : packages) {
                    contacts.add(packageInfo.packageName);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                if (pd != null && pd.isShowing()) pd.dismiss();
                Intent i = new Intent(activity, SelectApplication.class);
                i.putStringArrayListExtra(Constants.SELECTED_CONTACT_ARRAY, contacts);
                activity.startActivityForResult(i, requestCode);
            }
        }

        new Async().execute();
    }

    /**
     * Load list of contacts and show chooser activity.
     * @param activity context activity.
     * @param requestCode result request code.
     */
    public static void selectContact(final Activity activity, final int requestCode){
        class Async extends AsyncTask<Void, Void, Void>{

            private ProgressDialog pd;
            private ArrayList<String> contacts;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                pd = ProgressDialog.show(activity, null, activity.getString(R.string.please_wait), true);
            }

            @Override
            protected Void doInBackground(Void... params) {
                Cursor cursor = activity.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI,
                        null, null, null, ContactsContract.Contacts.DISPLAY_NAME + " ASC");
                contacts = new ArrayList<>();
                contacts.clear();
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                        String hasPhone = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

                        if (hasPhone.equalsIgnoreCase("1"))
                            hasPhone = "true";
                        else
                            hasPhone = "false";
                        if (name != null) {
                            if (Boolean.parseBoolean(hasPhone)) {
                                contacts.add(name);
                            }
                        }
                    }
                    cursor.close();
                }
                try {
                    Collections.sort(contacts, new Comparator<String>() {
                        @Override
                        public int compare(String e1, String e2) {
                            return e1.compareToIgnoreCase(e2);
                        }
                    });
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                if (pd != null && pd.isShowing()) pd.dismiss();
                Intent i = new Intent(activity, ContactsList.class);
                i.putStringArrayListExtra(Constants.SELECTED_CONTACT_ARRAY, contacts);
                activity.startActivityForResult(i, requestCode);
            }
        }

        new Async().execute();
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
    public static void startVoiceRecognitionActivity(Activity activity, int requestCode) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        SharedPrefs sPrefs = new SharedPrefs(activity);
        if (!sPrefs.loadBoolean(Prefs.AUTO_LANGUAGE)) {
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, sPrefs.loadPrefs(Prefs.VOICE_LANGUAGE));
        } else intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
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
        Intent myIntent = new Intent(Intent.ACTION_VIEW, marketUri);
        myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(myIntent);
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
