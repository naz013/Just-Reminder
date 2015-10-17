package com.cray.software.justreminder.helpers;

import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;

import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.databases.FilesDataBase;
import com.cray.software.justreminder.databases.NotesBase;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.interfaces.Prefs;
import com.cray.software.justreminder.reminder.DateType;
import com.cray.software.justreminder.reminder.LocationType;
import com.cray.software.justreminder.reminder.MonthdayType;
import com.cray.software.justreminder.reminder.Reminder;
import com.cray.software.justreminder.reminder.TimerType;
import com.cray.software.justreminder.reminder.WeekdayType;
import com.cray.software.justreminder.services.AlarmReceiver;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Helper class for creating backup files on SD Card.
 */
public class SyncHelper {

    private Context mContext;
    private DataBase DB;
    private NotesBase db;
    private AlarmReceiver alarm = new AlarmReceiver();
    private WeekDayReceiver weekDayReceiver = new WeekDayReceiver();

    public SyncHelper(Context context){
        this.mContext = context;
    }

    public SyncHelper(){
    }

    /**
     * Creates backup files on SD Card for all groups.
     * @throws JSONException
     * @throws IOException
     */
    public void groupToJson() throws JSONException, IOException {
        DataBase dataBase = new DataBase(mContext);
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

    /**
     * Creates backup files on SD Card for all birthdays.
     * @throws JSONException
     * @throws IOException
     */
    public void birthdayToJson() throws JSONException, IOException {
        DataBase dataBase = new DataBase(mContext);
        dataBase.open();
        Cursor c = dataBase.getBirthdays();
        if (c != null && c.moveToFirst()){
            do {
                String title = c.getString(c.getColumnIndex(Constants.ContactConstants.COLUMN_CONTACT_NAME));
                String date = c.getString(c.getColumnIndex(Constants.ContactConstants.COLUMN_CONTACT_BIRTHDAY));
                String number = c.getString(c.getColumnIndex(Constants.ContactConstants.COLUMN_CONTACT_NUMBER));
                String uuID = c.getString(c.getColumnIndex(Constants.ContactConstants.COLUMN_CONTACT_UUID));
                String mail = c.getString(c.getColumnIndex(Constants.ContactConstants.COLUMN_CONTACT_MAIL));
                int conId = c.getInt(c.getColumnIndex(Constants.ContactConstants.COLUMN_CONTACT_ID));
                JSONObject jObjectData = new JSONObject();
                jObjectData.put(encrypt(Constants.ContactConstants.COLUMN_CONTACT_NAME),
                        title != null ? encrypt(title) : encrypt(" "));
                jObjectData.put(encrypt(Constants.ContactConstants.COLUMN_CONTACT_BIRTHDAY), encrypt(date));
                jObjectData.put(encrypt(Constants.ContactConstants.COLUMN_CONTACT_NUMBER),
                        number != null ? encrypt(number) : encrypt(" "));
                jObjectData.put(encrypt(Constants.ContactConstants.COLUMN_CONTACT_UUID),
                        uuID != null ? encrypt(uuID) : encrypt(generateID()));
                jObjectData.put(encrypt(Constants.ContactConstants.COLUMN_CONTACT_MAIL),
                        mail != null ? encrypt(mail) : encrypt(" "));
                jObjectData.put(encrypt(Constants.ContactConstants.COLUMN_CONTACT_ID),
                        encrypt(String.valueOf(conId)));

                if (isSdPresent()) {
                    File sdPath = Environment.getExternalStorageDirectory();
                    File sdPathDr = new File(sdPath.toString() + "/JustReminder/" + Constants.DIR_BIRTHDAY_SD);
                    if (!sdPathDr.exists()) {
                        sdPathDr.mkdirs();
                    }
                    String exportFileName = uuID + Constants.FILE_NAME_BIRTHDAY;

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

    /**
     * Creates backup files on SD Card for all reminders.
     * @throws JSONException
     * @throws IOException
     */
    public void reminderToJson() throws JSONException, IOException {
        DB = new DataBase(mContext);
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
                int voice  = c.getInt(c.getColumnIndex(Constants.COLUMN_VOICE));
                int vibration  = c.getInt(c.getColumnIndex(Constants.COLUMN_VIBRATION));
                int auto  = c.getInt(c.getColumnIndex(Constants.COLUMN_AUTO_ACTION));
                int unlock  = c.getInt(c.getColumnIndex(Constants.COLUMN_UNLOCK_DEVICE));
                int wake  = c.getInt(c.getColumnIndex(Constants.COLUMN_WAKE_SCREEN));
                int notificationRepeat  = c.getInt(c.getColumnIndex(Constants.COLUMN_NOTIFICATION_REPEAT));
                long repMinute  = c.getLong(c.getColumnIndex(Constants.COLUMN_REMIND_TIME));
                long count = c.getLong(c.getColumnIndex(Constants.COLUMN_REMINDERS_COUNT));
                long radius = c.getLong(c.getColumnIndex(Constants.COLUMN_CUSTOM_RADIUS));
                long limit = c.getLong(c.getColumnIndex(Constants.COLUMN_REPEAT_LIMIT));
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
                    jObjectData.put(Constants.COLUMN_VOICE, voice);
                    jObjectData.put(Constants.COLUMN_VIBRATION, vibration);
                    jObjectData.put(Constants.COLUMN_AUTO_ACTION, auto);
                    jObjectData.put(Constants.COLUMN_WAKE_SCREEN, wake);
                    jObjectData.put(Constants.COLUMN_UNLOCK_DEVICE, unlock);
                    jObjectData.put(Constants.COLUMN_REPEAT_LIMIT, limit);
                    jObjectData.put(Constants.COLUMN_NOTIFICATION_REPEAT, notificationRepeat);

                    if (isSdPresent()) {
                        File sdPath = Environment.getExternalStorageDirectory();
                        File sdPathDr = new File(sdPath.toString() + "/JustReminder/" + Constants.DIR_SD);
                        if (!sdPathDr.exists()) {
                            sdPathDr.mkdirs();
                        }
                        String exportFileName = uuID + Constants.FILE_NAME_REMINDER;

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

    /**
     * Creates note file on SD Card.
     * @param note note content.
     * @param date date of note creating.
     * @param uuID unique note identifier.
     * @param color note color.
     * @param image image attached to note.
     * @param style typeface style.
     * @return Note file
     * @throws JSONException
     * @throws IOException
     */
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
        if (new SharedPrefs(mContext).loadBoolean(Prefs.NOTE_ENCRYPT)){
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

    /**
     * Creates backup files on SD Card for all notes.
     * @throws JSONException
     * @throws IOException
     */
    public void noteToJson() throws JSONException, IOException {
        db = new NotesBase(mContext);
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
                if (new SharedPrefs(mContext).loadBoolean(Prefs.NOTE_ENCRYPT)){
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

    /**
     * Restore note from JSON file to application.
     * @param file file path.
     * @param fileNameR file name.
     * @throws IOException
     * @throws JSONException
     */
    public void noteFromJson(String file, String fileNameR) throws IOException, JSONException {
        if (isSdPresent()){
            db = new NotesBase(mContext);
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
                    reminderObject(jsonObj);
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
                                noteObject(jsonObj);
                            }
                        }
                    }
                }
                db.close();
            }
        }
    }

    /**
     * Insert note to database.
     * @param jsonObj object excluded from file.
     * @throws JSONException
     */
    private void noteObject(JSONObject jsonObj) throws JSONException {
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
        SharedPrefs prefs = new SharedPrefs(mContext);
        if (!prefs.loadBoolean(Prefs.NOTE_ENCRYPT)){
            note = decrypt(note);
        }
        long linkId = jsonObj.getLong(Constants.COLUMN_LINK_ID);

        db = new NotesBase(mContext);
        db.open();
        long id = db.saveNote(note, date, color, uuID, image, style);
        db.linkToReminder(id, linkId);
    }

    /**
     * Get note content from file or JSON object.
     * @param file note file.
     * @param object JSON object.
     * @return
     */
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

    /**
     * Get note color from file or JSON object.
     * @param file note file.
     * @param object JSON object.
     * @return
     */
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

    /**
     * Get font style from file or JSON object.
     * @param file note file.
     * @param object JSON object.
     * @return
     */
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

    /**
     * Get flag is note is encrypted from file or object.
     * @param file note file.
     * @param object JSON object.
     * @return
     */
    @Deprecated
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

    /**
     * Get attached to note image from file or JSON object.
     * @param file note file.
     * @param object JSON object.
     * @return
     */
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

    /**
     * Get note content and unique identifier from JSON object.
     * @param jsonObj JSON object.
     * @return
     * @throws JSONException
     */
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

    /**
     * Get note color from JSON object.
     * @param jsonObj JSON object.
     * @return
     * @throws JSONException
     */
    private int getNoteColor(JSONObject jsonObj) throws JSONException {
        return jsonObj.getInt(Constants.COLUMN_COLOR);
    }

    /**
     * Get note font style from JSON object.
     * @param jsonObj JSON object.
     * @return
     * @throws JSONException
     */
    private int getNoteFontStyle(JSONObject jsonObj) throws JSONException {
        int style = 5;
        if (!jsonObj.isNull(Constants.COLUMN_FONT_STYLE)) {
            style = jsonObj.getInt(Constants.COLUMN_FONT_STYLE);
        }
        return style;
    }

    /**
     * Get note encrypt flag from JSON object.
     * @param jsonObj JSON object.
     * @return
     * @throws JSONException
     */
    @Deprecated
    private int getNoteEncrypt(JSONObject jsonObj) throws JSONException {
        return jsonObj.getInt(Constants.COLUMN_ENCRYPTED);
    }

    /**
     * Get note attached image from JSON object.
     * @param jsonObj JSON object.
     * @return
     * @throws JSONException
     */
    private byte[] getNoteImage(JSONObject jsonObj) throws JSONException {
        byte[] image = null;
        if (!jsonObj.isNull(Constants.COLUMN_IMAGE)) {
            image = Base64.decode(jsonObj.getString(Constants.COLUMN_IMAGE), Base64.DEFAULT);
        }
        return image;
    }

    /**
     * Restore reminder from JSON file to database.
     * Application restore reminders only with actual date.
     * @param file reminder file path.
     * @param fileNameR file name.
     * @throws IOException
     * @throws JSONException
     */
    public void reminderFromJson(String file, String fileNameR) throws IOException, JSONException {
        if (isSdPresent()){
            DB = new DataBase(mContext);
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
                    reminderObject(jsonObj);
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
                            reminderObject(jsonObj);
                        }
                    }
                }
                DB.close();
            }
        }
    }

    /**
     * Insert reminder to database from JSON object.
     * @param jsonObj JSON object.
     * @throws JSONException
     */
    private void reminderObject(JSONObject jsonObj) throws JSONException {
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

        int vibration = -1;
        if (jsonObj.has(Constants.COLUMN_VIBRATION)) jsonObj.getInt(Constants.COLUMN_VIBRATION);
        int voice = -1;
        if (jsonObj.has(Constants.COLUMN_VOICE)) jsonObj.getInt(Constants.COLUMN_VOICE);
        int wake = -1;
        if (jsonObj.has(Constants.COLUMN_WAKE_SCREEN)) jsonObj.getInt(Constants.COLUMN_WAKE_SCREEN);
        int unlock = -1;
        if (jsonObj.has(Constants.COLUMN_UNLOCK_DEVICE)) jsonObj.getInt(Constants.COLUMN_UNLOCK_DEVICE);
        int notificationRepeat = -1;
        if (jsonObj.has(Constants.COLUMN_NOTIFICATION_REPEAT)) jsonObj.getInt(Constants.COLUMN_NOTIFICATION_REPEAT);
        int auto = -1;
        if (jsonObj.has(Constants.COLUMN_AUTO_ACTION)) jsonObj.getInt(Constants.COLUMN_AUTO_ACTION);
        long limit = -1;
        if (jsonObj.has(Constants.COLUMN_REPEAT_LIMIT)) jsonObj.getInt(Constants.COLUMN_REPEAT_LIMIT);

        DB = new DataBase(mContext);
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
        TimeCount timeCount = new TimeCount(mContext);
        long id;
        Integer i = (int) (long) count;
        List<String> namesPass = new ArrayList<>();
        Cursor e = DB.queryAllReminders();
        while (e.moveToNext()) {
            for (e.moveToFirst(); !e.isAfterLast(); e.moveToNext()) {
                namesPass.add(e.getString(e.getColumnIndex(Constants.COLUMN_TECH_VAR)));
            }
        }
        e.close();
        if (type != null && !namesPass.contains(type)) {
            if (type.startsWith(Constants.TYPE_WEEKDAY)) {
                if (!namesPass.contains(uuID)) {
                    long due = TimeCount.getNextWeekdayTime(hour, minute, weekdays, 0);
                    new WeekdayType(mContext).save(new Reminder(text, type, weekdays, melody, categoryId,
                            uuID, new double[]{latitude, longitude}, number, day, month, year, hour,
                            minute, seconds, repeatCode, 0, radius, 0, 0, repMinute, due, vibration, voice,
                            notificationRepeat, wake, unlock, auto, limit));
                }
            } else if (type.startsWith(Constants.TYPE_MONTHDAY)) {
                long due = TimeCount.getNextMonthDayTime(hour, minute, day, 0);
                new MonthdayType(mContext).save(new Reminder(text, type, weekdays, melody, categoryId,
                        uuID, new double[]{latitude, longitude}, number, day, month, year, hour,
                        minute, seconds, repeatCode, 0, radius, 0, 0, repMinute, due, vibration, voice,
                        notificationRepeat, wake, unlock, auto, limit));
            } else {
                if (timeCount.isNext(year, month, day, hour, minute, seconds, repMinute, repeatCode, i)) {
                    if (type.matches(Constants.TYPE_TIME)) {
                        long due = TimeCount.getEventTime(year, month, day, hour, minute, seconds, repMinute, repeatCode, count, 0);
                        new TimerType(mContext).save(new Reminder(text, type, weekdays, melody, categoryId,
                                uuID, new double[]{latitude, longitude}, number, day, month, year, hour,
                                minute, seconds, repeatCode, 0, radius, 0, 0, repMinute, due, vibration, voice,
                                notificationRepeat, wake, unlock, auto, limit));
                    } else if (type.startsWith(Constants.TYPE_LOCATION) || type.startsWith(Constants.TYPE_LOCATION_OUT)){
                        long due = TimeCount.getEventTime(year, month, day, hour, minute, seconds, repMinute, repeatCode, count, 0);
                        new LocationType(mContext, type).save(new Reminder(text, type, weekdays, melody, categoryId,
                                uuID, new double[]{latitude, longitude}, number, day, month, year, hour,
                                minute, seconds, repeatCode, 0, radius, 0, 0, repMinute, due, vibration, voice,
                                notificationRepeat, wake, unlock, auto, limit));
                    } else {
                        long due = TimeCount.getEventTime(year, month, day, hour, minute, seconds, repMinute, repeatCode, count, 0);
                        new DateType(mContext, type).save(new Reminder(text, type, weekdays, melody, categoryId,
                                uuID, new double[]{latitude, longitude}, number, day, month, year, hour,
                                minute, seconds, repeatCode, 0, radius, 0, 0, repMinute, due, vibration, voice,
                                notificationRepeat, wake, unlock, auto, limit));
                    }
                }
            }
        }
    }

    /**
     * Restore group from JSON file to application.
     * @param file group file path.
     * @param fileNameR file name.
     * @throws IOException
     * @throws JSONException
     */
    public void groupFromJson(String file, String fileNameR) throws IOException, JSONException {
        if (isSdPresent()){
            DB = new DataBase(mContext);
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
                    groupObject(jsonObj);
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
                            groupObject(jsonObj);
                        }
                    }
                }
                DB.close();
            }
        }
    }

    /**
     * Insert group from JSON object to database.
     * @param jsonObj JSON object.
     * @throws JSONException
     */
    private void groupObject(JSONObject jsonObj) throws JSONException {
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
        DB = new DataBase(mContext);
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

    /**
     * Restore birthday from JSON file to application.
     * @param file birthday file path.
     * @param fileNameR file name.
     * @throws IOException
     * @throws JSONException
     */
    public void birthdayFromJson(String file, String fileNameR) throws IOException, JSONException {
        if (isSdPresent()){
            DB = new DataBase(mContext);
            DB.open();
            List<String> namesPass = new ArrayList<>();
            Cursor e = DB.getBirthdays();
            while (e.moveToNext()) {
                for (e.moveToFirst(); !e.isAfterLast(); e.moveToNext()) {
                    namesPass.add(e.getString(e.getColumnIndex(Constants.ContactConstants.COLUMN_CONTACT_UUID)));
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
                    birthdayObject(jsonObj);
                }
            } else {
                File sdPath = Environment.getExternalStorageDirectory();
                File sdPathDr = new File(sdPath.toString() + "/JustReminder/" + Constants.DIR_BIRTHDAY_SD);
                File[] files = sdPathDr.listFiles();
                if (files != null && files.length > 0) {
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
                            birthdayObject(jsonObj);
                        }
                    }
                }
                DB.close();
            }
        }
    }

    /**
     * SimpleDateFormat variable for date parsing.
     */
    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    /**
     * Insert birthdays from JSON object to database.
     * @param jsonObj JSON object.
     * @throws JSONException
     */
    private void birthdayObject(JSONObject jsonObj) throws JSONException {
        String name = null;
        String key = encrypt(Constants.ContactConstants.COLUMN_CONTACT_NAME);
        if (!jsonObj.isNull(key)) {
            name = decrypt(jsonObj.getString(key));
        }
        String date = null;
        key = encrypt(Constants.ContactConstants.COLUMN_CONTACT_BIRTHDAY);
        if (!jsonObj.isNull(key)) {
            date = decrypt(jsonObj.getString(key));
        }
        String number = null;
        key = encrypt(Constants.ContactConstants.COLUMN_CONTACT_NUMBER);
        if (!jsonObj.isNull(key)) {
            number = decrypt(jsonObj.getString(key));
        }
        String mail = null;
        key = encrypt(Constants.ContactConstants.COLUMN_CONTACT_MAIL);
        if (!jsonObj.isNull(key)) {
            mail = decrypt(jsonObj.getString(key));
        }
        String uuID = null;
        key = encrypt(Constants.ContactConstants.COLUMN_CONTACT_UUID);
        if (!jsonObj.isNull(key)) {
            uuID = decrypt(jsonObj.getString(key));
        }
        String id = null;
        key = encrypt(Constants.ContactConstants.COLUMN_CONTACT_ID);
        if (!jsonObj.isNull(key)) {
            id = decrypt(jsonObj.getString(key));
        }
        int conId = 0;
        if (id != null) {
            conId = Integer.parseInt(id);
        }
        int day = 0;
        int month = 0;
        try {
            Date d = format.parse(date);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(d);
            day = calendar.get(Calendar.DAY_OF_MONTH);
            month = calendar.get(Calendar.MONTH);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        DB = new DataBase(mContext);
        DB.open();
        Cursor cf = DB.getBirthdays();
        if (cf != null && cf.moveToFirst()) {
            List<String> namesPass = new ArrayList<>();
            List<String> numbers = new ArrayList<>();
            while (cf.moveToNext()) {
                for (cf.moveToFirst(); !cf.isAfterLast(); cf.moveToNext()) {
                    namesPass.add(cf.getString(cf.getColumnIndex(Constants.ContactConstants.COLUMN_CONTACT_NAME)));
                    numbers.add(cf.getString(cf.getColumnIndex(Constants.ContactConstants.COLUMN_CONTACT_NUMBER)));
                }
            }
            if (!namesPass.contains(name) && !numbers.contains(number)) {
                DB.addBirthday(name, conId, date, day, month, number, uuID);
            }
        } else {
            DB.addBirthday(name, conId, date, day, month, number, uuID);
        }
        DB.close();
    }

    /**
     * Scan application folders on SD Card for a JSON files.
     * @throws IOException
     * @throws JSONException
     */
    public void findJson() throws IOException, JSONException {
        if (isSdPresent()){
            FilesDataBase fdb = new FilesDataBase(mContext);
            fdb.open();
            Cursor e = fdb.getFiles();
            if (e != null && e.moveToFirst()){
                do{
                    long rowId = e.getLong(e.getColumnIndex(Constants.COLUMN_ID));
                    if (rowId != 0 && fdb != null) {
                        fdb.deleteFile(rowId);
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
                    try {
                        FileInputStream stream = new FileInputStream(fileLoc);
                        BufferedReader reader = new BufferedReader(
                                new InputStreamReader(stream, "UTF-8")
                        );
                        int n;
                        while ((n = reader.read(buffer)) != -1) {
                            writer.write(buffer, 0, n);
                        }
                    } catch (FileNotFoundException s){
                        throw s;
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

    /**
     * Insert found JSON file to files database.
     * @param jsonObj JSON object.
     * @param fileName file name.
     * @param fileType type of file.
     * @param fileLocation path to file.
     * @param lastEdit file last edit time in milliseconds.
     * @throws JSONException
     */
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

        int vibration = -1;
        if (jsonObj.has(Constants.COLUMN_VIBRATION)) jsonObj.getInt(Constants.COLUMN_VIBRATION);
        int voice = -1;
        if (jsonObj.has(Constants.COLUMN_VOICE)) jsonObj.getInt(Constants.COLUMN_VOICE);
        int wake = -1;
        if (jsonObj.has(Constants.COLUMN_WAKE_SCREEN)) jsonObj.getInt(Constants.COLUMN_WAKE_SCREEN);
        int unlock = -1;
        if (jsonObj.has(Constants.COLUMN_UNLOCK_DEVICE)) jsonObj.getInt(Constants.COLUMN_UNLOCK_DEVICE);
        int notificationRepeat = -1;
        if (jsonObj.has(Constants.COLUMN_NOTIFICATION_REPEAT)) jsonObj.getInt(Constants.COLUMN_NOTIFICATION_REPEAT);
        int auto = -1;
        if (jsonObj.has(Constants.COLUMN_AUTO_ACTION)) jsonObj.getInt(Constants.COLUMN_AUTO_ACTION);
        long limit = -1;
        if (jsonObj.has(Constants.COLUMN_REPEAT_LIMIT)) jsonObj.getInt(Constants.COLUMN_REPEAT_LIMIT);

        String uuID = null;
        if (!jsonObj.isNull(Constants.COLUMN_TECH_VAR)) {
            uuID = jsonObj.getString(Constants.COLUMN_TECH_VAR);
        }
        FilesDataBase fdb = new FilesDataBase(mContext);
        fdb.open();
        if (repMinute < 1000) repMinute = repMinute * TimeCount.minute;
        long id = fdb.insertFile(fileName, fileType, fileLocation, lastEdit, text, type, day, month, year, hour, minute, seconds, number,
                repeatCode, repMinute, count, latitude, longitude, uuID, weekdays);
        fdb.updateFileExtra(id, vibration, voice, notificationRepeat, wake, unlock, auto, limit);
        fdb.close();
    }

    /**
     * Generate unique identifier.
     * @return
     */
    public static String generateID(){
        return UUID.randomUUID().toString();
    }

    /**
     * Check if device has SD Card.
     * @return
     */
    public static boolean isSdPresent() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    /**
     * Check for internet connection.
     * @param context application context.
     * @return
     */
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

    /**
     * Decrypt string to human readable format.
     * @param string string to decrypt.
     * @return
     */
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

    /**
     * Encrypt string.
     * @param string string to encrypt.
     * @return
     */
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
