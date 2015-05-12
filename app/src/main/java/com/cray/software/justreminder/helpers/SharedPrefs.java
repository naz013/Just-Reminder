package com.cray.software.justreminder.helpers;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefs {
    SharedPreferences prefs;
    Context pContext;
    public static final String APP_UI_PREFERENCES = "ui_settings";
    public static final String APP_CHANGES_PREFERENCES = "changes_settings";
    public static final String APP_PREFERENCES = "system_messages";
    private static int MODE = Context.MODE_PRIVATE;
    public SharedPrefs(Context context){
        this.pContext = context;
    }

    public void savePrefs(String stringToSave, String value){
        prefs = pContext.getSharedPreferences(APP_UI_PREFERENCES, MODE);
        SharedPreferences.Editor uiEd = prefs.edit();
        uiEd.putString(stringToSave, value);
        uiEd.commit();
    }

    public void saveInt(String stringToSave, int value){
        prefs = pContext.getSharedPreferences(APP_UI_PREFERENCES, MODE);
        SharedPreferences.Editor uiEd = prefs.edit();
        uiEd.putInt(stringToSave, value);
        uiEd.commit();
    }

    public int loadInt(String stringToLoad){
        prefs = pContext.getSharedPreferences(APP_UI_PREFERENCES, MODE);
        int x;
        try {
            x = prefs.getInt(stringToLoad, 0);
        } catch (ClassCastException e) {
            x = Integer.parseInt(prefs.getString(stringToLoad, "0"));
        }
        return x;
    }

    public void saveLong(String stringToSave, long value){
        prefs = pContext.getSharedPreferences(APP_UI_PREFERENCES, MODE);
        SharedPreferences.Editor uiEd = prefs.edit();
        uiEd.putLong(stringToSave, value);
        uiEd.commit();
    }

    public long loadLong(String stringToLoad){
        prefs = pContext.getSharedPreferences(APP_UI_PREFERENCES, MODE);
        long x;
        try {
            x = prefs.getLong(stringToLoad, 1000);
        } catch (ClassCastException e) {
            x = Long.parseLong(prefs.getString(stringToLoad, "1000"));
        }
        return x;
    }

    public String loadPrefs(String stringToLoad){
        String res;
        try {
            prefs = pContext.getSharedPreferences(APP_UI_PREFERENCES, MODE);
            res = prefs.getString(stringToLoad, "");
        } catch (NullPointerException e) {
            e.printStackTrace();
            res = "";
        }
        return res;
    }

    public boolean isString(String checkString){
        prefs = pContext.getSharedPreferences(APP_UI_PREFERENCES, MODE);
        return prefs.contains(checkString);
    }

    public void saveBoolean(String stringToSave, boolean value){
        prefs = pContext.getSharedPreferences(APP_UI_PREFERENCES, MODE);
        SharedPreferences.Editor uiEd = prefs.edit();
        uiEd.putBoolean(stringToSave, value);
        uiEd.commit();
    }

    public boolean loadBoolean(String stringToLoad){
        prefs = pContext.getSharedPreferences(APP_UI_PREFERENCES, MODE);
        boolean res;
        try {
            res = prefs.getBoolean(stringToLoad, false);
        } catch (ClassCastException e){
            res = Boolean.parseBoolean(prefs.getString(stringToLoad, "false"));
        }
        return res;
    }

    public void saveVersionBoolean(String stringToSave){
        prefs = pContext.getSharedPreferences(APP_CHANGES_PREFERENCES, MODE);
        SharedPreferences.Editor uiEd = prefs.edit();
        uiEd.putBoolean(stringToSave, true);
        uiEd.commit();
    }

    public boolean loadVersionBoolean(String stringToLoad){
        prefs = pContext.getSharedPreferences(APP_CHANGES_PREFERENCES, MODE);
        boolean res;
        try {
            res = prefs.getBoolean(stringToLoad, false);
        } catch (ClassCastException e){
            res = Boolean.parseBoolean(prefs.getString(stringToLoad, "false"));
        }
        return res;
    }

    public void saveSystemBoolean(String key, boolean value){
        prefs = pContext.getSharedPreferences(APP_PREFERENCES, MODE);
        SharedPreferences.Editor uiEd = prefs.edit();
        uiEd.putBoolean(key, value);
        uiEd.commit();
    }
    public boolean loadSystemBoolean(String key){
        prefs = pContext.getSharedPreferences(APP_PREFERENCES, MODE);
        return prefs.getBoolean(key, false);
    }

    public boolean isSystemKey(String key){
        prefs = pContext.getSharedPreferences(APP_PREFERENCES, MODE);
        return prefs.contains(key);
    }
}