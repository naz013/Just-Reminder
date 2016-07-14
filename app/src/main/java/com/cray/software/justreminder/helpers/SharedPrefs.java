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

package com.cray.software.justreminder.helpers;

import android.content.Context;
import android.content.SharedPreferences;

import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.utils.MemoryUtil;

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
    private static SharedPrefs sharedPrefs;

    private SharedPrefs() {
    }

    public static SharedPrefs getInstance(Context context) {
        if (sharedPrefs == null) {
            sharedPrefs = new SharedPrefs(context);
        }
        return sharedPrefs;
    }

    private SharedPrefs(Context context){
        prefs = context.getSharedPreferences("ui_settings", Context.MODE_PRIVATE);
    }

    /**
     * Save String preference.
     * @param stringToSave key.
     * @param value value.
     */
    public void putString(String stringToSave, String value){
        prefs.edit().putString(stringToSave, value).apply();
    }

    /**
     * Save Integer preference.
     * @param stringToSave key.
     * @param value value.
     */
    public void putInt(String stringToSave, int value){
        prefs.edit().putInt(stringToSave, value).apply();
    }

    /**
     * Get Integer preference.
     * @param stringToLoad key.
     * @return Integer
     */
    public int getInt(String stringToLoad){
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
    public void putLong(String stringToSave, long value){
        prefs.edit().putLong(stringToSave, value).apply();
    }

    /**
     * Get Long preference.
     * @param stringToLoad key.
     * @return Long
     */
    public long getLong(String stringToLoad){
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
     * @return String
     */
    public String getString(String stringToLoad){
        String res;
        try {
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
     * @return Boolean
     */
    public boolean hasKey(String checkString){
        return prefs.contains(checkString);
    }

    /**
     * Save Boolean preference.
     * @param stringToSave key.
     * @param value value.
     */
    public void putBoolean(String stringToSave, boolean value){
        prefs.edit().putBoolean(stringToSave, value).apply();
    }

    /**
     * Get Boolean preference.
     * @param stringToLoad key.
     * @return Boolean
     */
    public boolean getBoolean(String stringToLoad){
        boolean res;
        try {
            res = prefs.getBoolean(stringToLoad, false);
        } catch (ClassCastException e){
            res = Boolean.parseBoolean(prefs.getString(stringToLoad, "false"));
        }
        return res;
    }

    public void saveVersionBoolean(String stringToSave){
        prefs.edit().putBoolean(stringToSave, true).apply();
    }

    public boolean getVersion(String stringToLoad){
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
        File dir = MemoryUtil.getPrefsDir();
        if (dir != null) {
            File prefsFile = new File(dir + "/prefs.xml");
            if (prefsFile.exists()) prefsFile.delete();
            ObjectOutputStream output = null;
            try {
                output = new ObjectOutputStream(new FileOutputStream(prefsFile));
                Map<String, ?> list = prefs.getAll();
                if (list.containsKey(Prefs.CONTACTS_IMPORT_DIALOG)) list.remove(Prefs.CONTACTS_IMPORT_DIALOG);
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
        File dir = MemoryUtil.getPrefsDir();
        if (dir == null) return;

        File prefsFile = new File(dir + "/prefs.xml");
        if (prefsFile.exists()) {
            ObjectInputStream input = null;
            try {
                input = new ObjectInputStream(new FileInputStream(prefsFile));
                SharedPreferences.Editor prefEdit = prefs.edit();
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
                prefEdit.apply();
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