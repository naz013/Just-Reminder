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

/**
 * Helper class for working with SharedPreferences.
 */
public class SharedPrefs {
    private SharedPreferences prefs;
    private Context mContext;
    public static final String APP_UI_PREFERENCES = "ui_settings";
    public static final String APP_CHANGES_PREFERENCES = "changes_settings";
    public static final String APP_PREFERENCES = "system_messages";
    private static int MODE = Context.MODE_PRIVATE;
    public SharedPrefs(Context context){
        this.mContext = context;
    }

    /**
     * Save String preference.
     * @param stringToSave key.
     * @param value value.
     */
    public void savePrefs(String stringToSave, String value){
        prefs = mContext.getSharedPreferences(APP_UI_PREFERENCES, MODE);
        SharedPreferences.Editor uiEd = prefs.edit();
        uiEd.putString(stringToSave, value);
        uiEd.commit();
    }

    /**
     * Save Integer preference.
     * @param stringToSave key.
     * @param value value.
     */
    public void saveInt(String stringToSave, int value){
        prefs = mContext.getSharedPreferences(APP_UI_PREFERENCES, MODE);
        SharedPreferences.Editor uiEd = prefs.edit();
        uiEd.putInt(stringToSave, value);
        uiEd.commit();
    }

    /**
     * Get Integer preference.
     * @param stringToLoad key.
     * @return
     */
    public int loadInt(String stringToLoad){
        prefs = mContext.getSharedPreferences(APP_UI_PREFERENCES, MODE);
        int x;
        try {
            x = prefs.getInt(stringToLoad, 0);
        } catch (ClassCastException e) {
            x = Integer.parseInt(prefs.getString(stringToLoad, "0"));
        }
        return x;
    }

    /**
     * Save Long preference.
     * @param stringToSave key.
     * @param value value.
     */
    public void saveLong(String stringToSave, long value){
        prefs = mContext.getSharedPreferences(APP_UI_PREFERENCES, MODE);
        SharedPreferences.Editor uiEd = prefs.edit();
        uiEd.putLong(stringToSave, value);
        uiEd.commit();
    }

    /**
     * Get Long preference.
     * @param stringToLoad key.
     * @return
     */
    public long loadLong(String stringToLoad){
        prefs = mContext.getSharedPreferences(APP_UI_PREFERENCES, MODE);
        long x;
        try {
            x = prefs.getLong(stringToLoad, 1000);
        } catch (ClassCastException e) {
            x = Long.parseLong(prefs.getString(stringToLoad, "1000"));
        }
        return x;
    }

    /**
     * Get String preference.
     * @param stringToLoad key.
     * @return
     */
    public String loadPrefs(String stringToLoad){
        String res;
        try {
            prefs = mContext.getSharedPreferences(APP_UI_PREFERENCES, MODE);
            res = prefs.getString(stringToLoad, "");
        } catch (NullPointerException e) {
            e.printStackTrace();
            res = "";
        }
        return res;
    }

    /**
     * Check if preference exist.
     * @param checkString key.
     * @return
     */
    public boolean isString(String checkString){
        prefs = mContext.getSharedPreferences(APP_UI_PREFERENCES, MODE);
        return prefs.contains(checkString);
    }

    /**
     * Save Boolean preference.
     * @param stringToSave key.
     * @param value value.
     */
    public void saveBoolean(String stringToSave, boolean value){
        prefs = mContext.getSharedPreferences(APP_UI_PREFERENCES, MODE);
        SharedPreferences.Editor uiEd = prefs.edit();
        uiEd.putBoolean(stringToSave, value);
        uiEd.commit();
    }

    /**
     * Get Boolean preference.
     * @param stringToLoad key.
     * @return
     */
    public boolean loadBoolean(String stringToLoad){
        prefs = mContext.getSharedPreferences(APP_UI_PREFERENCES, MODE);
        boolean res;
        try {
            res = prefs.getBoolean(stringToLoad, false);
        } catch (ClassCastException e){
            res = Boolean.parseBoolean(prefs.getString(stringToLoad, "false"));
        }
        return res;
    }

    public void saveVersionBoolean(String stringToSave){
        prefs = mContext.getSharedPreferences(APP_CHANGES_PREFERENCES, MODE);
        SharedPreferences.Editor uiEd = prefs.edit();
        uiEd.putBoolean(stringToSave, true);
        uiEd.commit();
    }

    public boolean loadVersionBoolean(String stringToLoad){
        prefs = mContext.getSharedPreferences(APP_CHANGES_PREFERENCES, MODE);
        boolean res;
        try {
            res = prefs.getBoolean(stringToLoad, false);
        } catch (ClassCastException e){
            res = Boolean.parseBoolean(prefs.getString(stringToLoad, "false"));
        }
        return res;
    }

    /**
     * Save copy of preferences on SD Card.
     */
    public void savePrefsBackup(){
        if (SyncHelper.isSdPresent()){
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
                        mContext.getSharedPreferences(APP_UI_PREFERENCES, Context.MODE_PRIVATE);
                Map<String, ?> list = pref.getAll();
                if (list.containsKey(Prefs.CONTACTS_IMPORT_DIALOG)) list.remove(Prefs.CONTACTS_IMPORT_DIALOG);
                //if (list.containsKey(Prefs.DRIVE_USER)) list.remove(Prefs.DRIVE_USER);
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

    /**
     * Get preferences from backup file on SD Card.
     */
    public void loadPrefsFromFile(){
        File sdPath = Environment.getExternalStorageDirectory();
        File sdPathDr = new File(sdPath.toString() + "/JustReminder/" + Constants.DIR_PREFS);
        File prefs = new File(sdPathDr + "/prefs.xml");
        if (prefs.exists()) {
            ObjectInputStream input = null;
            try {
                input = new ObjectInputStream(new FileInputStream(prefs));
                SharedPreferences.Editor prefEdit = mContext.getSharedPreferences(APP_UI_PREFERENCES, Context.MODE_PRIVATE).edit();
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