package com.cray.software.justreminder.helpers;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;

import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.databases.FilesDataBase;
import com.cray.software.justreminder.note.NotesBase;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.services.AlarmReceiver;
import com.cray.software.justreminder.services.GeolocationService;
import com.cray.software.justreminder.services.MonthDayReceiver;
import com.cray.software.justreminder.services.WeekDayReceiver;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SyncHelper {
    Context sContext;
    DataBase DB;
    NotesBase db;
    AlarmReceiver alarm = new AlarmReceiver();
    WeekDayReceiver weekDayReceiver = new WeekDayReceiver();
    TimeCount timeCount;

    public SyncHelper(Context context){
        this.sContext = context;
    }

    public SyncHelper(){

    }

    public void exportGroups() throws JSONException, IOException {
        DataBase dataBase = new DataBase(sContext);
        dataBase.open();
        Cursor c = dataBase.queryCategories();
        if (c != null && c.moveToFirst()){
            do {
                String title = c.getString(c.getColumnIndex(Constants.COLUMN_TEXT));
                int color  = c.getInt(c.getColumnIndex(Constants.COLUMN_COLOR));
                long date = c.getLong(c.getColumnIndex(Constants.COLUMN_DATE_TIME));
                String uuID = c.getString(c.getColumnIndex(Constants.COLUMN_TECH_VAR));
                JSONObject jObjectData = new JSONObject();
                jObjectData.put(Constants.COLUMN_COLOR, color);
                jObjectData.put(Constants.COLUMN_TEXT, title);
                jObjectData.put(Constants.COLUMN_DATE_TIME, date);
                jObjectData.put(Constants.COLUMN_TECH_VAR, uuID);

                if (isSdPresent()) {
                    File sdPath = Environment.getExternalStorageDirectory();
                    File sdPathDr = new File(sdPath.toString() + "/JustReminder/" + Constants.DIR_GROUP_SD);
                    if (!sdPathDr.exists()) {
                        sdPathDr.mkdirs();
                    }
                    String exportFileName = uuID + Constants.FILE_NAME_GROUP;

                    File file = new File(sdPathDr, exportFileName);
                    if (file.exists()) {
                        file.delete();
                    }
                    FileWriter  fw = new FileWriter(file);
                    fw.write(jObjectData.toString());
                    fw.close();
                } else Log.i("reminder-info", "Couldn't find external storage!");
            } while (c.moveToNext());
        }
        if (c != null) c.close();
        dataBase.close();
    }

    public void exportReminderToJSON() throws JSONException, IOException {
        DB = new DataBase(sContext);
        DB.open();
        Cursor c = DB.queryGroup();
        if (c != null && c.moveToFirst()){
            do {
                long id = c.getLong(c.getColumnIndex(Constants.COLUMN_ID));
                String text = c.getString(c.getColumnIndex(Constants.COLUMN_TEXT));
                String type = c.getString(c.getColumnIndex(Constants.COLUMN_TYPE));
                String weekdays = c.getString(c.getColumnIndex(Constants.COLUMN_WEEKDAYS));
                int day  = c.getInt(c.getColumnIndex(Constants.COLUMN_DAY));
                int month  = c.getInt(c.getColumnIndex(Constants.COLUMN_MONTH));
                int year  = c.getInt(c.getColumnIndex(Constants.COLUMN_YEAR));
                int hour  = c.getInt(c.getColumnIndex(Constants.COLUMN_HOUR));
                int minute  = c.getInt(c.getColumnIndex(Constants.COLUMN_MINUTE));
                int seconds  = c.getInt(c.getColumnIndex(Constants.COLUMN_SECONDS));
                int isDone  = c.getInt(c.getColumnIndex(Constants.COLUMN_IS_DONE));
                String number = c.getString(c.getColumnIndex(Constants.COLUMN_NUMBER));
                int repeatCode  = c.getInt(c.getColumnIndex(Constants.COLUMN_REPEAT));
                long repMinute  = c.getLong(c.getColumnIndex(Constants.COLUMN_REMIND_TIME));
                long count = c.getLong(c.getColumnIndex(Constants.COLUMN_REMINDERS_COUNT));
                long radius = c.getLong(c.getColumnIndex(Constants.COLUMN_CUSTOM_RADIUS));
                double latitude = c.getDouble(c.getColumnIndex(Constants.COLUMN_LATITUDE));
                double longitude = c.getDouble(c.getColumnIndex(Constants.COLUMN_LONGITUDE));
                String uuID = c.getString(c.getColumnIndex(Constants.COLUMN_TECH_VAR));
                String categoryId = c.getString(c.getColumnIndex(Constants.COLUMN_CATEGORY));
                String melody = c.getString(c.getColumnIndex(Constants.COLUMN_CUSTOM_MELODY));

                if (uuID == null) {
                    String uID = generateID();
                    DB.setUniqueId(id, uID);
                    uuID = uID;
                }

                if (isDone == 0) {
                    JSONObject jObjectData = new JSONObject();
                    jObjectData.put(Constants.COLUMN_TEXT, text);
                    jObjectData.put(Constants.COLUMN_TYPE, type);
                    jObjectData.put(Constants.COLUMN_DAY, day);
                    jObjectData.put(Constants.COLUMN_MONTH, month);
                    jObjectData.put(Constants.COLUMN_YEAR, year);
                    jObjectData.put(Constants.COLUMN_HOUR, hour);
                    jObjectData.put(Constants.COLUMN_MINUTE, minute);
                    jObjectData.put(Constants.COLUMN_SECONDS, seconds);
                    jObjectData.put(Constants.COLUMN_NUMBER, number);
                    jObjectData.put(Constants.COLUMN_REPEAT, repeatCode);
                    jObjectData.put(Constants.COLUMN_REMIND_TIME, repMinute);
                    jObjectData.put(Constants.COLUMN_REMINDERS_COUNT, count);
                    jObjectData.put(Constants.COLUMN_LATITUDE, latitude);
                    jObjectData.put(Constants.COLUMN_LONGITUDE, longitude);
                    jObjectData.put(Constants.COLUMN_TECH_VAR, uuID);
                    jObjectData.put(Constants.COLUMN_WEEKDAYS, weekdays);
                    jObjectData.put(Constants.COLUMN_CATEGORY, categoryId);
                    jObjectData.put(Constants.COLUMN_CUSTOM_MELODY, melody);
                    jObjectData.put(Constants.COLUMN_CUSTOM_RADIUS, radius);

                    if (isSdPresent()) {
                        File sdPath = Environment.getExternalStorageDirectory();
                        File sdPathDr = new File(sdPath.toString() + "/JustReminder/" + Constants.DIR_SD);
                        if (!sdPathDr.exists()) {
                            sdPathDr.mkdirs();
                        }
                        String exportFileName = uuID + Constants.FILE_NAME;

                        File file = new File(sdPathDr, exportFileName);
                        if (file.exists()) {
                            file.delete();
                        }
                        FileWriter fw = new FileWriter(file);
                        fw.write(jObjectData.toString());
                        fw.close();
                    } else Log.i("reminder-info", "Couldn't find external storage!");
                }
            } while (c.moveToNext());
        }
        if (c != null) c.close();
        DB.close();
    }

    public File createNote(String note, String date, String uuID, int color, byte[] image,
                           int style) throws JSONException, IOException {
        JSONObject jObjectData = new JSONObject();
        jObjectData.put(Constants.COLUMN_COLOR, style);
        jObjectData.put(Constants.COLUMN_FONT_STYLE, color);
        jObjectData.put(Constants.COLUMN_DATE, date);
        jObjectData.put(Constants.COLUMN_UUID, uuID);
        jObjectData.put(Constants.COLUMN_NOTE, encrypt(note));
        if (image != null) {
            jObjectData.put(Constants.COLUMN_IMAGE, Base64.encodeToString(image, Base64.DEFAULT));
        } else jObjectData.put(Constants.COLUMN_IMAGE, image);
        if (new SharedPrefs(sContext).loadBoolean(Constants.APP_UI_PREFERENCES_NOTE_ENCRYPT)){
            jObjectData.put(Constants.COLUMN_ENCRYPTED, 1);
        } else {
            jObjectData.put(Constants.COLUMN_ENCRYPTED, 0);
        }
        File file = null;
        if (isSdPresent()) {
            File sdPath = Environment.getExternalStorageDirectory();
            File sdPathDr = new File(sdPath.toString() + "/JustReminder/" + Constants.DIR_MAIL_SD);
            if (!sdPathDr.exists()) {
                sdPathDr.mkdirs();
            }
            String exportFileName = uuID + Constants.FILE_NAME_NOTE;

            file = new File(sdPathDr, exportFileName);
            if (file.exists()) {
                file.delete();
            }
            FileWriter  fw = new FileWriter(file);
            fw.write(jObjectData.toString());
            fw.close();
        } else Log.i("reminder-info", "Couldn't find external storage!");
        return file;
    }

    public void exportNotes() throws JSONException, IOException {
        db = new NotesBase(sContext);
        db.open();
        Cursor c = db.getNotes();
        if (c != null && c.moveToFirst()){
            do {
                String note = c.getString(c.getColumnIndex(Constants.COLUMN_NOTE));
                int color  = c.getInt(c.getColumnIndex(Constants.COLUMN_COLOR));
                int style  = c.getInt(c.getColumnIndex(Constants.COLUMN_FONT_STYLE));
                String date = c.getString(c.getColumnIndex(Constants.COLUMN_DATE));
                String uuID = c.getString(c.getColumnIndex(Constants.COLUMN_UUID));
                byte[] image = c.getBlob(c.getColumnIndex(Constants.COLUMN_IMAGE));
                long linkId = c.getLong(c.getColumnIndex(Constants.COLUMN_LINK_ID));
                JSONObject jObjectData = new JSONObject();
                jObjectData.put(Constants.COLUMN_COLOR, color);
                jObjectData.put(Constants.COLUMN_FONT_STYLE, style);
                jObjectData.put(Constants.COLUMN_DATE, date);
                jObjectData.put(Constants.COLUMN_UUID, uuID);
                jObjectData.put(Constants.COLUMN_LINK_ID, linkId);
                if (image != null) {
                    jObjectData.put(Constants.COLUMN_IMAGE, Base64.encodeToString(image, Base64.DEFAULT));
                } else jObjectData.put(Constants.COLUMN_IMAGE, image);
                if (new SharedPrefs(sContext).loadBoolean(Constants.APP_UI_PREFERENCES_NOTE_ENCRYPT)){
                    jObjectData.put(Constants.COLUMN_ENCRYPTED, 1);
                    jObjectData.put(Constants.COLUMN_NOTE, note);
                } else {
                    jObjectData.put(Constants.COLUMN_ENCRYPTED, 0);
                    jObjectData.put(Constants.COLUMN_NOTE, encrypt(note));
                }

                if (isSdPresent()) {
                    File sdPath = Environment.getExternalStorageDirectory();
                    File sdPathDr = new File(sdPath.toString() + "/JustReminder/" + Constants.DIR_NOTES_SD);
                    if (!sdPathDr.exists()) {
                        sdPathDr.mkdirs();
                    }
                    String exportFileName = uuID + Constants.FILE_NAME_NOTE;

                    File file = new File(sdPathDr, exportFileName);
                    if (file.exists()) {
                        file.delete();
                    }
                    FileWriter  fw = new FileWriter(file);
                    fw.write(jObjectData.toString());
                    fw.close();
                } else Log.i("reminder-info", "Couldn't find external storage!");
            } while (c.moveToNext());
        }
        if (c != null) c.close();
        db.close();
    }

    public void importNotes(String file, String fileNameR) throws IOException, JSONException {
        if (isSdPresent()){
            db = new NotesBase(sContext);
            db.open();
            List<String> namesPass = new ArrayList<>();
            Cursor e = db.getNotes();
            while (e.moveToNext()) {
                for (e.moveToFirst(); !e.isAfterLast(); e.moveToNext()) {
                    namesPass.add(e.getString(e.getColumnIndex(Constants.COLUMN_UUID)));
                }
            }
            if (e != null) e.close();
            if (file != null){
                int pos = fileNameR.lastIndexOf(".");
                String fileNameS = fileNameR.substring(0, pos);
                if (!namesPass.contains(fileNameS)) {
                    FileInputStream stream = new FileInputStream(file);
                    Writer writer = new StringWriter();
                    char[] buffer = new char[1024];
                    try {
                        BufferedReader reader = new BufferedReader(
                                new InputStreamReader(stream, "UTF-8")
                        );
                        int n;
                        while ((n = reader.read(buffer)) != -1) {
                            writer.write(buffer, 0, n);
                        }
                    } finally {
                        stream.close();
                    }
                    String jsonText = writer.toString();
                    JSONObject jsonObj = new JSONObject(jsonText);
                    importObject(jsonObj);
                }
            } else {
                File sdPath = Environment.getExternalStorageDirectory();
                File sdPathDr = new File(sdPath.toString() + "/JustReminder/" + Constants.DIR_NOTES_SD);
                File[] files = sdPathDr.listFiles();
                if (files != null) {
                    int f = files.length;
                    if (f > 0) {
                        for (File file1 : files) {
                            String fileName = file1.getName();
                            int pos = fileName.lastIndexOf(".");
                            String fileLoc = sdPathDr + "/" + fileName;
                            String fileNameS = fileName.substring(0, pos);
                            if (!namesPass.contains(fileNameS)) {
                                FileInputStream stream = new FileInputStream(fileLoc);
                                Writer writer = new StringWriter();
                                char[] buffer = new char[1024];
                                try {
                                    BufferedReader reader = new BufferedReader(
                                            new InputStreamReader(stream, "UTF-8")
                                    );
                                    int n;
                                    while ((n = reader.read(buffer)) != -1) {
                                        writer.write(buffer, 0, n);
                                    }
                                } finally {
                                    stream.close();
                                }
                                String jsonText = writer.toString();
                                JSONObject jsonObj = new JSONObject(jsonText);
                                importNoteObject(jsonObj);
                            }
                        }
                    }
                }
                db.close();
            }
        }
    }

    private void importNoteObject(JSONObject jsonObj) throws JSONException {
        String note = null;
        if (!jsonObj.isNull(Constants.COLUMN_NOTE)) {
            note = jsonObj.getString(Constants.COLUMN_NOTE);
        }
        String date = null;
        if (!jsonObj.isNull(Constants.COLUMN_DATE)) {
            date = jsonObj.getString(Constants.COLUMN_DATE);
        }
        String uuID = null;
        if (!jsonObj.isNull(Constants.COLUMN_UUID)) {
            uuID = jsonObj.getString(Constants.COLUMN_UUID);
        }
        int color = jsonObj.getInt(Constants.COLUMN_COLOR);
        int style = 5;
        if (!jsonObj.isNull(Constants.COLUMN_FONT_STYLE)) {
            style = jsonObj.getInt(Constants.COLUMN_FONT_STYLE);
        }
        int encrypt = jsonObj.getInt(Constants.COLUMN_ENCRYPTED);
        byte[] image = null;
        if (!jsonObj.isNull(Constants.COLUMN_IMAGE)) {
            image = Base64.decode(jsonObj.getString(Constants.COLUMN_IMAGE), Base64.DEFAULT);
        }
        SharedPrefs prefs = new SharedPrefs(sContext);
        if (!prefs.loadBoolean(Constants.APP_UI_PREFERENCES_NOTE_ENCRYPT)){
            note = decrypt(note);
        }
        long linkId = jsonObj.getLong(Constants.COLUMN_LINK_ID);

        db = new NotesBase(sContext);
        db.open();
        long id = db.saveNote(note, date, color, uuID, image, style);
        db.linkToReminder(id, linkId);
    }

    public ArrayList<String> getNote(File file, JSONObject object) {
        ArrayList<String> data = new ArrayList<>();
        data.clear();
        if (object != null){
            try {
                data = getNoteString(object);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            if (isSdPresent()) {
                if (file != null) {
                    FileInputStream stream = null;
                    try {
                        stream = new FileInputStream(file.toString());
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    Writer writer = new StringWriter();
                    char[] buffer = new char[1024];
                    try {
                        BufferedReader reader = new BufferedReader(
                                new InputStreamReader(stream, "UTF-8")
                        );
                        int n;
                        while ((n = reader.read(buffer)) != -1) {
                            writer.write(buffer, 0, n);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            if (stream != null) {
                                stream.close();
                            }
                        } catch (IOException | NullPointerException e) {
                            e.printStackTrace();
                        }
                    }
                    String jsonText = writer.toString();
                    JSONObject jsonObj = null;
                    try {
                        jsonObj = new JSONObject(jsonText);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    try {
                        data = getNoteString(jsonObj);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return data;
    }

    public int getColor(File file, JSONObject object){
        int data = 0;
        if (object != null){
            try {
                data = getNoteColor(object);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            if (isSdPresent()) {
                if (file != null) {
                    FileInputStream stream = null;
                    try {
                        stream = new FileInputStream(file.toString());
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    Writer writer = new StringWriter();
                    char[] buffer = new char[1024];
                    try {
                        BufferedReader reader = new BufferedReader(
                                new InputStreamReader(stream, "UTF-8")
                        );
                        int n;
                        while ((n = reader.read(buffer)) != -1) {
                            writer.write(buffer, 0, n);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            if (stream != null) {
                                stream.close();
                            }
                        } catch (IOException | NullPointerException e) {
                            e.printStackTrace();
                        }
                    }
                    String jsonText = writer.toString();
                    JSONObject jsonObj = null;
                    try {
                        jsonObj = new JSONObject(jsonText);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    try {
                        data = getNoteColor(jsonObj);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return data;
    }

    public int getFontStyle(File file, JSONObject object){
        int data = 5;
        if (object != null){
            try {
                data = getNoteFontStyle(object);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            if (isSdPresent()) {
                if (file != null) {
                    FileInputStream stream = null;
                    try {
                        stream = new FileInputStream(file.toString());
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    Writer writer = new StringWriter();
                    char[] buffer = new char[1024];
                    try {
                        BufferedReader reader = new BufferedReader(
                                new InputStreamReader(stream, "UTF-8")
                        );
                        int n;
                        while ((n = reader.read(buffer)) != -1) {
                            writer.write(buffer, 0, n);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            if (stream != null) {
                                stream.close();
                            }
                        } catch (IOException | NullPointerException e) {
                            e.printStackTrace();
                        }
                    }
                    String jsonText = writer.toString();
                    JSONObject jsonObj = null;
                    try {
                        jsonObj = new JSONObject(jsonText);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    try {
                        data = getNoteFontStyle(jsonObj);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return data;
    }

    public int getEncrypt(File file, JSONObject object){
        int data = 0;
        if (object != null){
            try {
                data = getNoteEncrypt(object);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            if (isSdPresent()) {
                if (file != null) {
                    FileInputStream stream = null;
                    try {
                        stream = new FileInputStream(file.toString());
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    Writer writer = new StringWriter();
                    char[] buffer = new char[1024];
                    try {
                        BufferedReader reader = new BufferedReader(
                                new InputStreamReader(stream, "UTF-8")
                        );
                        int n;
                        while ((n = reader.read(buffer)) != -1) {
                            writer.write(buffer, 0, n);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            if (stream != null) {
                                stream.close();
                            }
                        } catch (IOException | NullPointerException e) {
                            e.printStackTrace();
                        }
                    }
                    String jsonText = writer.toString();
                    JSONObject jsonObj = null;
                    try {
                        jsonObj = new JSONObject(jsonText);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    try {
                        data = getNoteEncrypt(jsonObj);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return data;
    }

    public byte[] getImage(File file, JSONObject object){
        byte[] data = null;
        if (object != null){
            try {
                data = getNoteImage(object);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            if (isSdPresent()) {
                if (file != null) {
                    FileInputStream stream = null;
                    try {
                        stream = new FileInputStream(file.toString());
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    Writer writer = new StringWriter();
                    char[] buffer = new char[1024];
                    try {
                        BufferedReader reader = new BufferedReader(
                                new InputStreamReader(stream, "UTF-8")
                        );
                        int n;
                        while ((n = reader.read(buffer)) != -1) {
                            writer.write(buffer, 0, n);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            if (stream != null) {
                                stream.close();
                            }
                        } catch (IOException | NullPointerException e) {
                            e.printStackTrace();
                        }
                    }
                    String jsonText = writer.toString();
                    JSONObject jsonObj = null;
                    try {
                        jsonObj = new JSONObject(jsonText);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    try {
                        data = getNoteImage(jsonObj);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return data;
    }

    private ArrayList<String> getNoteString(JSONObject jsonObj) throws JSONException {
        ArrayList<String> data = new ArrayList<>();
        data.clear();
        String note;
        if (!jsonObj.isNull(Constants.COLUMN_NOTE)) {
            note = jsonObj.getString(Constants.COLUMN_NOTE);
            data.add(note);
        }

        String uuID;
        if (!jsonObj.isNull(Constants.COLUMN_UUID)) {
            uuID = jsonObj.getString(Constants.COLUMN_UUID);
            data.add(uuID);
        }
        return data;
    }

    private int getNoteColor(JSONObject jsonObj) throws JSONException {
        return jsonObj.getInt(Constants.COLUMN_COLOR);
    }

    private int getNoteFontStyle(JSONObject jsonObj) throws JSONException {
        int style = 5;
        if (!jsonObj.isNull(Constants.COLUMN_FONT_STYLE)) {
            style = jsonObj.getInt(Constants.COLUMN_FONT_STYLE);
        }
        return style;
    }

    private int getNoteEncrypt(JSONObject jsonObj) throws JSONException {
        return jsonObj.getInt(Constants.COLUMN_ENCRYPTED);
    }

    private byte[] getNoteImage(JSONObject jsonObj) throws JSONException {
        byte[] image = null;
        if (!jsonObj.isNull(Constants.COLUMN_IMAGE)) {
            image = Base64.decode(jsonObj.getString(Constants.COLUMN_IMAGE), Base64.DEFAULT);
        }
        return image;
    }

    public void importReminderFromJSON(String file, String fileNameR) throws IOException, JSONException {
        if (isSdPresent()){
            DB = new DataBase(sContext);
            DB.open();
            List<String> namesPass = new ArrayList<>();
            Cursor e = DB.queryGroup();
            while (e.moveToNext()) {
                for (e.moveToFirst(); !e.isAfterLast(); e.moveToNext()) {
                    namesPass.add(e.getString(e.getColumnIndex(Constants.COLUMN_TECH_VAR)));
                }
            }
            e.close();
            if (file != null){
                int pos = fileNameR.lastIndexOf(".");
                String fileNameS = fileNameR.substring(0, pos);
                if (!namesPass.contains(fileNameS)) {
                    FileInputStream stream = new FileInputStream(file);
                    Writer writer = new StringWriter();
                    char[] buffer = new char[1024];
                    try {
                        BufferedReader reader = new BufferedReader(
                                new InputStreamReader(stream, "UTF-8")
                        );
                        int n;
                        while ((n = reader.read(buffer)) != -1) {
                            writer.write(buffer, 0, n);
                        }
                    } finally {
                        stream.close();
                    }
                    String jsonText = writer.toString();
                    JSONObject jsonObj = new JSONObject(jsonText);
                    importObject(jsonObj);
                }
            } else {
                File sdPath = Environment.getExternalStorageDirectory();
                File sdPathDr = new File(sdPath.toString() + "/JustReminder/" + Constants.DIR_SD);
                File[] files = sdPathDr.listFiles();
                int f = files.length;
                if (f > 0) {
                    for (File file1 : files) {
                        String fileName = file1.getName();
                        int pos = fileName.lastIndexOf(".");
                        String fileLoc = sdPathDr + "/" + fileName;
                        String fileNameS = fileName.substring(0, pos);
                        if (!namesPass.contains(fileNameS)) {
                            FileInputStream stream = new FileInputStream(fileLoc);
                            Writer writer = new StringWriter();
                            char[] buffer = new char[1024];
                            try {
                                BufferedReader reader = new BufferedReader(
                                        new InputStreamReader(stream, "UTF-8")
                                );
                                int n;
                                while ((n = reader.read(buffer)) != -1) {
                                    writer.write(buffer, 0, n);
                                }
                            } finally {
                                stream.close();
                            }
                            String jsonText = writer.toString();
                            JSONObject jsonObj = new JSONObject(jsonText);
                            importObject(jsonObj);
                        }
                    }
                }
                DB.close();
            }
        }
    }

    private void importObject(JSONObject jsonObj) throws JSONException {
        String text = null;
        if (!jsonObj.isNull(Constants.COLUMN_TEXT)) {
            text = jsonObj.getString(Constants.COLUMN_TEXT);
        }
        String type = null;
        if (!jsonObj.isNull(Constants.COLUMN_TYPE)) {
            type = jsonObj.getString(Constants.COLUMN_TYPE);
        }
        String weekdays = null;
        if (!jsonObj.isNull(Constants.COLUMN_WEEKDAYS)) {
            weekdays = jsonObj.getString(Constants.COLUMN_WEEKDAYS);
        }
        String categoryId = null;
        if (!jsonObj.isNull(Constants.COLUMN_CATEGORY)) {
            categoryId = jsonObj.getString(Constants.COLUMN_CATEGORY);
        }
        String melody = null;
        if (!jsonObj.isNull(Constants.COLUMN_CUSTOM_MELODY)) {
            melody = jsonObj.getString(Constants.COLUMN_CUSTOM_MELODY);
        }
        int day = jsonObj.getInt(Constants.COLUMN_DAY);
        int month = jsonObj.getInt(Constants.COLUMN_MONTH);
        int year = jsonObj.getInt(Constants.COLUMN_YEAR);
        int hour = jsonObj.getInt(Constants.COLUMN_HOUR);
        int minute = jsonObj.getInt(Constants.COLUMN_MINUTE);
        int seconds = jsonObj.getInt(Constants.COLUMN_SECONDS);
        int radius = jsonObj.getInt(Constants.COLUMN_CUSTOM_RADIUS);
        if (radius == 0) radius = -1;
        String number = null;
        if (!jsonObj.isNull(Constants.COLUMN_NUMBER)) {
            number = jsonObj.getString(Constants.COLUMN_NUMBER);
        }
        int repeatCode = jsonObj.getInt(Constants.COLUMN_REPEAT);
        long repMinute = jsonObj.getLong(Constants.COLUMN_REMIND_TIME);
        long count = jsonObj.getLong(Constants.COLUMN_REMINDERS_COUNT);
        double latitude = jsonObj.getDouble(Constants.COLUMN_LATITUDE);
        double longitude = jsonObj.getDouble(Constants.COLUMN_LONGITUDE);
        String uuID = null;
        if (!jsonObj.isNull(Constants.COLUMN_TECH_VAR)) {
            uuID = jsonObj.getString(Constants.COLUMN_TECH_VAR);
        }
        if (repMinute < 1000) repMinute = repMinute * TimeCount.minute;
        DB = new DataBase(sContext);
        DB.open();
        if (categoryId == null) {
            Cursor cf = DB.queryCategories();
            if (cf != null && cf.moveToFirst()) {
                categoryId = cf.getString(cf.getColumnIndex(Constants.COLUMN_TECH_VAR));
            }
            if (cf != null) cf.close();
        } else {
            Cursor cf = DB.getCategory(categoryId);
            if (cf == null || cf.getCount() == 0) {
                cf = DB.queryCategories();
                if (cf != null && cf.moveToFirst()) {
                    categoryId = cf.getString(cf.getColumnIndex(Constants.COLUMN_TECH_VAR));
                }
            }
            if (cf != null) cf.close();
        }
        timeCount = new TimeCount(sContext);
        long id;
        Integer i = (int) (long) count;
        if (type != null) {
            if (type.startsWith(Constants.TYPE_WEEKDAY)) {
                if (DB.getCount() == 0) {
                    id = DB.insertReminder(text, type, day, month, year, hour, minute, seconds, number,
                            repeatCode, repMinute, count, latitude, longitude, uuID, weekdays, 0, melody,
                            radius, 0, 0, categoryId);
                    DB.updateReminderDateTime(id);
                    weekDayReceiver.setAlarm(sContext, id);
                } else {
                    List<String> namesPass = new ArrayList<>();
                    Cursor e = DB.queryAllReminders();
                    while (e.moveToNext()) {
                        for (e.moveToFirst(); !e.isAfterLast(); e.moveToNext()) {
                            namesPass.add(e.getString(e.getColumnIndex(Constants.COLUMN_TECH_VAR)));
                        }
                    }
                    e.close();
                    if (!namesPass.contains(uuID)) {
                        id = DB.insertReminder(text, type, day, month, year, hour, minute, seconds, number,
                                repeatCode, repMinute, count, latitude, longitude, uuID, weekdays, 0,
                                melody, radius, 0, 0, categoryId);
                        DB.updateReminderDateTime(id);
                        weekDayReceiver.setAlarm(sContext, id);
                    }
                }
            } else if (type.startsWith(Constants.TYPE_MONTHDAY)) {
                if (DB.getCount() == 0) {
                    id = DB.insertReminder(text, type, day, month, year, hour, minute, seconds, number,
                            repeatCode, repMinute, count, latitude, longitude, uuID, weekdays, 0, melody,
                            radius, 0, 0, categoryId);
                    DB.updateReminderDateTime(id);
                    new MonthDayReceiver().setAlarm(sContext, id);
                } else {
                    List<String> namesPass = new ArrayList<>();
                    Cursor e = DB.queryAllReminders();
                    while (e.moveToNext()) {
                        for (e.moveToFirst(); !e.isAfterLast(); e.moveToNext()) {
                            namesPass.add(e.getString(e.getColumnIndex(Constants.COLUMN_TECH_VAR)));
                        }
                    }
                    e.close();
                    if (!namesPass.contains(uuID)) {
                        id = DB.insertReminder(text, type, day, month, year, hour, minute, seconds, number,
                                repeatCode, repMinute, count, latitude, longitude, uuID, weekdays, 0,
                                melody, radius, 0, 0, categoryId);
                        DB.updateReminderDateTime(id);
                        new MonthDayReceiver().setAlarm(sContext, id);
                    }
                }
            } else {
                if (timeCount.getNextDate(year, month, day, hour, minute, seconds, repMinute, repeatCode, i)) {
                    if (DB.getCount() == 0) {
                        id = DB.insertReminder(text, type, day, month, year, hour, minute, seconds, number,
                                repeatCode, repMinute, count, latitude, longitude, uuID, weekdays, 0,
                                melody, radius, 0, 0, categoryId);
                        DB.updateReminderDateTime(id);
                        if (type.startsWith(Constants.TYPE_LOCATION) ||
                                type.startsWith(Constants.TYPE_LOCATION_OUT)) {
                            sContext.startService(new Intent(sContext, GeolocationService.class)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                        } else {
                            alarm.setAlarm(sContext, id);
                        }
                    } else {
                        List<String> namesPass = new ArrayList<>();
                        Cursor e = DB.queryAllReminders();
                        while (e.moveToNext()) {
                            for (e.moveToFirst(); !e.isAfterLast(); e.moveToNext()) {
                                namesPass.add(e.getString(e.getColumnIndex(Constants.COLUMN_TECH_VAR)));
                            }
                        }
                        e.close();
                        if (!namesPass.contains(uuID)) {
                            id = DB.insertReminder(text, type, day, month, year, hour, minute, seconds, number,
                                    repeatCode, repMinute, count, latitude, longitude, uuID, weekdays, 0,
                                    melody, radius, 0, 0, categoryId);
                            DB.updateReminderDateTime(id);
                            if (type.startsWith(Constants.TYPE_LOCATION) ||
                                    type.startsWith(Constants.TYPE_LOCATION_OUT)) {
                                sContext.startService(new Intent(sContext, GeolocationService.class)
                                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                            } else {
                                alarm.setAlarm(sContext, id);
                            }
                        }
                    }
                }
            }
        }
    }

    public void importGroup(String file, String fileNameR) throws IOException, JSONException {
        if (isSdPresent()){
            DB = new DataBase(sContext);
            DB.open();
            List<String> namesPass = new ArrayList<>();
            Cursor e = DB.queryCategories();
            while (e.moveToNext()) {
                for (e.moveToFirst(); !e.isAfterLast(); e.moveToNext()) {
                    namesPass.add(e.getString(e.getColumnIndex(Constants.COLUMN_TECH_VAR)));
                }
            }
            e.close();
            if (file != null){
                int pos = fileNameR.lastIndexOf(".");
                String fileNameS = fileNameR.substring(0, pos);
                if (!namesPass.contains(fileNameS)) {
                    FileInputStream stream = new FileInputStream(file);
                    Writer writer = new StringWriter();
                    char[] buffer = new char[1024];
                    try {
                        BufferedReader reader = new BufferedReader(
                                new InputStreamReader(stream, "UTF-8")
                        );
                        int n;
                        while ((n = reader.read(buffer)) != -1) {
                            writer.write(buffer, 0, n);
                        }
                    } finally {
                        stream.close();
                    }
                    String jsonText = writer.toString();
                    JSONObject jsonObj = new JSONObject(jsonText);
                    importGroupObject(jsonObj);
                }
            } else {
                File sdPath = Environment.getExternalStorageDirectory();
                File sdPathDr = new File(sdPath.toString() + "/JustReminder/" + Constants.DIR_GROUP_SD);
                File[] files = sdPathDr.listFiles();
                int f = files.length;
                if (f > 0) {
                    for (File file1 : files) {
                        String fileName = file1.getName();
                        int pos = fileName.lastIndexOf(".");
                        String fileLoc = sdPathDr + "/" + fileName;
                        String fileNameS = fileName.substring(0, pos);
                        if (!namesPass.contains(fileNameS)) {
                            FileInputStream stream = new FileInputStream(fileLoc);
                            Writer writer = new StringWriter();
                            char[] buffer = new char[1024];
                            try {
                                BufferedReader reader = new BufferedReader(
                                        new InputStreamReader(stream, "UTF-8")
                                );
                                int n;
                                while ((n = reader.read(buffer)) != -1) {
                                    writer.write(buffer, 0, n);
                                }
                            } finally {
                                stream.close();
                            }
                            String jsonText = writer.toString();
                            JSONObject jsonObj = new JSONObject(jsonText);
                            importGroupObject(jsonObj);
                        }
                    }
                }
                DB.close();
            }
        }
    }

    private void importGroupObject(JSONObject jsonObj) throws JSONException {
        String title = null;
        if (!jsonObj.isNull(Constants.COLUMN_TEXT)) {
            title = jsonObj.getString(Constants.COLUMN_TEXT);
        }
        int color = jsonObj.getInt(Constants.COLUMN_COLOR);
        long date = jsonObj.getLong(Constants.COLUMN_DATE_TIME);
        String uuID = null;
        if (!jsonObj.isNull(Constants.COLUMN_TECH_VAR)) {
            uuID = jsonObj.getString(Constants.COLUMN_TECH_VAR);
        }
        DB = new DataBase(sContext);
        DB.open();
        Cursor cf = DB.queryCategories();
        if (cf != null && cf.moveToFirst()) {
            List<String> namesPass = new ArrayList<>();
            List<String> titles = new ArrayList<>();
            while (cf.moveToNext()) {
                for (cf.moveToFirst(); !cf.isAfterLast(); cf.moveToNext()) {
                    namesPass.add(cf.getString(cf.getColumnIndex(Constants.COLUMN_TECH_VAR)));
                    titles.add(cf.getString(cf.getColumnIndex(Constants.COLUMN_TEXT)));
                }
            }
            if (!namesPass.contains(uuID) && !titles.contains(title)) {
                DB.addCategory(title, date, uuID, color);
            }
        } else {
            DB.addCategory(title, date, uuID, color);
        }
        if (cf != null) cf.close();
    }

    public void scanFoldersForJSON() throws IOException, JSONException {
        if (isSdPresent()){
            FilesDataBase fdb = new FilesDataBase(sContext);
            fdb.open();
            Cursor e = fdb.queryGroup();
            if (e != null && e.moveToFirst()){
                do{
                    long rowId = e.getLong(e.getColumnIndex(Constants.COLUMN_ID));
                    if (rowId != 0 && fdb != null) {
                        fdb.deleteTask(rowId);
                    }
                }while (e.moveToNext());
            }
            if (e != null) e.close();
            File sdPath = Environment.getExternalStorageDirectory();
            if (sdPath == null) sdPath = Environment.getDataDirectory();
            File sdPathDr = new File(sdPath.toString() + "/JustReminder/" + Constants.DIR_SD);
            if (!sdPathDr.exists()) sdPathDr.mkdirs();
            File[] files = sdPathDr.listFiles();
            int f = files.length;
            if (f > 0) {
                for (File file1 : files) {
                    String fileName = file1.getName();
                    long lastEdit = file1.lastModified();
                    String fileLoc = sdPathDr + "/" + fileName;
                    Writer writer = new StringWriter();
                    char[] buffer = new char[1024];
                    try (FileInputStream stream = new FileInputStream(fileLoc)) {
                        BufferedReader reader = new BufferedReader(
                                new InputStreamReader(stream, "UTF-8")
                        );
                        int n;
                        while ((n = reader.read(buffer)) != -1) {
                            writer.write(buffer, 0, n);
                        }
                    }
                    String jsonText = writer.toString();
                    JSONObject jsonObj = new JSONObject(jsonText);
                    saveFiles(jsonObj, fileName, Constants.FilesConstants.FILE_TYPE_LOCAL, fileLoc, lastEdit);
                }
            }
            // scanning Dropbox tmp folder
            File sdPathDrop = new File(sdPath.toString() + "/JustReminder/" + Constants.DIR_SD_DBX_TMP);
            if (!sdPathDrop.exists()) sdPathDrop.mkdirs();
            File[] dFiles = sdPathDrop.listFiles();
            int d = dFiles.length;
            if (d > 0) {
                for (File file1 : dFiles) {
                    String fileName = file1.getName();
                    long lastEdit = file1.lastModified();
                    String fileLoc = sdPathDrop + "/" + fileName;
                    FileInputStream stream = new FileInputStream(fileLoc);
                    Writer writer = new StringWriter();
                    char[] buffer = new char[1024];
                    try {
                        BufferedReader reader = new BufferedReader(
                                new InputStreamReader(stream, "UTF-8")
                        );
                        int n;
                        while ((n = reader.read(buffer)) != -1) {
                            writer.write(buffer, 0, n);
                        }
                    } finally {
                        stream.close();
                    }
                    String jsonText = writer.toString();
                    JSONObject jsonObj = new JSONObject(jsonText);
                    saveFiles(jsonObj, fileName, Constants.FilesConstants.FILE_TYPE_DROPBOX, fileLoc, lastEdit);
                }
            }
            // scanning Google Drive tmp folder
            File gFolder = new File(sdPath.toString() + "/JustReminder/" + Constants.DIR_SD_GDRIVE_TMP);
            if (!gFolder.exists()) gFolder.mkdirs();
            File[] gFiles = gFolder.listFiles();
            int g = gFiles.length;
            if (g > 0) {
                for (File file1 : gFiles) {
                    String fileName = file1.getName();
                    long lastEdit = file1.lastModified();
                    String fileLoc = gFolder + "/" + fileName;
                    FileInputStream stream = new FileInputStream(fileLoc);
                    Writer writer = new StringWriter();
                    char[] buffer = new char[1024];
                    try {
                        BufferedReader reader = new BufferedReader(
                                new InputStreamReader(stream, "UTF-8")
                        );
                        int n;
                        while ((n = reader.read(buffer)) != -1) {
                            writer.write(buffer, 0, n);
                        }
                    } finally {
                        stream.close();
                    }
                    String jsonText = writer.toString();
                    JSONObject jsonObj = new JSONObject(jsonText);
                    saveFiles(jsonObj, fileName, Constants.FilesConstants.FILE_TYPE_GDRIVE, fileLoc, lastEdit);
                }
            }
        }
    }

    private void saveFiles(JSONObject jsonObj, String fileName, String fileType, String fileLocation, long lastEdit) throws JSONException {
        String text = null;
        if (!jsonObj.isNull(Constants.COLUMN_TEXT)) {
            text = jsonObj.getString(Constants.COLUMN_TEXT);
        }
        String type = null;
        if (!jsonObj.isNull(Constants.COLUMN_TYPE)) {
            type = jsonObj.getString(Constants.COLUMN_TYPE);
        }
        String weekdays = null;
        if (!jsonObj.isNull(Constants.COLUMN_WEEKDAYS)) {
            weekdays = jsonObj.getString(Constants.COLUMN_WEEKDAYS);
        }

        int day = 0;
        int month = 0;
        int year = 0;
        int hour = 0;
        int minute = 0;
        int seconds = 0;
        try {
            day = jsonObj.getInt(Constants.COLUMN_DAY);
        } catch (JSONException e){
            e.printStackTrace();
        }
        try {
            month = jsonObj.getInt(Constants.COLUMN_MONTH);
        } catch (JSONException e){
            e.printStackTrace();
        }
        try {
            year = jsonObj.getInt(Constants.COLUMN_YEAR);
        } catch (JSONException e){
            e.printStackTrace();
        }
        try {
            hour = jsonObj.getInt(Constants.COLUMN_HOUR);
        } catch (JSONException e){
            e.printStackTrace();
        }
        try {
            minute = jsonObj.getInt(Constants.COLUMN_MINUTE);
        } catch (JSONException e){
            e.printStackTrace();
        }
        try {
            seconds = jsonObj.getInt(Constants.COLUMN_SECONDS);
        } catch (JSONException e){
            e.printStackTrace();
        }

        String number = null;
        if (!jsonObj.isNull(Constants.COLUMN_NUMBER)) {
            number = jsonObj.getString(Constants.COLUMN_NUMBER);
        }
        int repeatCode = jsonObj.getInt(Constants.COLUMN_REPEAT);
        long repMinute = jsonObj.getLong(Constants.COLUMN_REMIND_TIME);
        long count = jsonObj.getLong(Constants.COLUMN_REMINDERS_COUNT);
        double latitude = jsonObj.getDouble(Constants.COLUMN_LATITUDE);
        double longitude = jsonObj.getDouble(Constants.COLUMN_LONGITUDE);
        String uuID = null;
        if (!jsonObj.isNull(Constants.COLUMN_TECH_VAR)) {
            uuID = jsonObj.getString(Constants.COLUMN_TECH_VAR);
        }
        FilesDataBase fdb = new FilesDataBase(sContext);
        fdb.open();
        if (repMinute < 1000) repMinute = repMinute * TimeCount.minute;
        fdb.insertTask(fileName, fileType, fileLocation, lastEdit, text, type, day, month, year, hour, minute, seconds, number,
                repeatCode, repMinute, count, latitude, longitude, uuID, weekdays);
    }

    public static String generateID(){
        return UUID.randomUUID().toString();
    }

    public boolean isSdPresent() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    public static boolean isConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager)context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnected()) {
            try {
                URL url = new URL("http://www.google.com/");
                HttpURLConnection urlc = (HttpURLConnection)url.openConnection();
                urlc.setRequestProperty("User-Agent", "test");
                urlc.setRequestProperty("Connection", "close");
                urlc.setConnectTimeout(1000); // mTimeout is in seconds
                urlc.connect();
                if (urlc.getResponseCode() == 200) {
                    return true;
                } else {
                    return false;
                }
            } catch (IOException e) {
                Log.i("warning", "Error checking internet connection");
                return false;
            }
        }
        return false;
    }

    public String decrypt(String string){
        String result = "";
        byte[] byte_string = Base64.decode(string, Base64.DEFAULT);
        try {
            result = new String(byte_string, "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
        return result;
    }

    public String encrypt(String string){
        byte[] string_byted = null;
        try {
            string_byted = string.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return Base64.encodeToString(string_byted, Base64.DEFAULT).trim();
    }
}
