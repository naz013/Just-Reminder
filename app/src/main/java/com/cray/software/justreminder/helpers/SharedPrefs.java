package com.cray.software.justreminder.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;

import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.interfaces.Prefs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;

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

    public void savePrefsBackup(){
        if (new SyncHelper(pContext).isSdPresent()){
            File sdPath = Environment.getExternalStorageDirectory();
            File sdPathDr = new File(sdPath.toString() + "/JustReminder/" + Constants.DIR_PREFS);
            if (!sdPathDr.exists()){
                sdPathDr.mkdirs();
            }
            File prefs = new File(sdPathDr + "/prefs.xml");
            if (prefs.exists()){
                prefs.delete();
            }
            ObjectOutputStream output = null;
            try {
                output = new ObjectOutputStream(new FileOutputStream(prefs));
                SharedPreferences pref =
                        pContext.getSharedPreferences(APP_UI_PREFERENCES, Context.MODE_PRIVATE);
                Map<String, ?> list = pref.getAll();
                list.remove(Prefs.DRIVE_USER);
                list.remove(Prefs.CONTACTS_IMPORT_DIALOG);
                output.writeObject(list);
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                try {
                    if (output != null) {
                        output.flush();
                        output.close();
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public void loadPrefsFromFile(){
        File sdPath = Environment.getExternalStorageDirectory();
        File sdPathDr = new File(sdPath.toString() + "/JustReminder/" + Constants.DIR_PREFS);
        File prefs = new File(sdPathDr + "/prefs.xml");
        if (prefs.exists()) {
            ObjectInputStream input = null;
            try {
                input = new ObjectInputStream(new FileInputStream(prefs));
                SharedPreferences.Editor prefEdit = pContext.getSharedPreferences(APP_UI_PREFERENCES, Context.MODE_PRIVATE).edit();
                prefEdit.clear();
                Map<String, ?> entries = (Map<String, ?>) input.readObject();
                for (Map.Entry<String, ?> entry : entries.entrySet()) {
                    Object v = entry.getValue();
                    String key = entry.getKey();

                    if (v instanceof Boolean)
                        prefEdit.putBoolean(key, (Boolean) v);
                    else if (v instanceof Float)
                        prefEdit.putFloat(key, (Float) v);
                    else if (v instanceof Integer)
                        prefEdit.putInt(key, (Integer) v);
                    else if (v instanceof Long)
                        prefEdit.putLong(key, (Long) v);
                    else if (v instanceof String)
                        prefEdit.putString(key, ((String) v));
                }
                prefEdit.commit();
            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (input != null) {
                        input.close();
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}