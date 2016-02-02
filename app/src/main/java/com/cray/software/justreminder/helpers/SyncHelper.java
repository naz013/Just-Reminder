package com.cray.software.justreminder.helpers;

import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Base64;
import android.util.Log;

import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.FileConfig;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.databases.NextBase;
import com.cray.software.justreminder.databases.NotesBase;
import com.cray.software.justreminder.datas.models.BirthdayModel;
import com.cray.software.justreminder.datas.models.CategoryModel;
import com.cray.software.justreminder.datas.models.NoteModel;
import com.cray.software.justreminder.json.JModel;
import com.cray.software.justreminder.json.JParser;
import com.cray.software.justreminder.json.JRecurrence;
import com.cray.software.justreminder.json.JShopping;
import com.cray.software.justreminder.reminder.DateType;
import com.cray.software.justreminder.reminder.LocationType;
import com.cray.software.justreminder.reminder.Reminder;
import com.cray.software.justreminder.utils.MemoryUtil;

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

    public SyncHelper(Context context){
        this.mContext = context;
    }

    public SyncHelper(){
    }

    /**
     * Write data to selected file.
     * @param file file to write.
     * @param data data for writing.
     * @throws IOException
     */
    private void writeFile(File file, String data) throws IOException {
        if (file.exists()) {
            file.delete();
        }
        FileWriter fw = new FileWriter(file);
        fw.write(data);
        fw.close();
    }

    /**
     * Creates backup files on SD Card for all groups.
     * @throws JSONException
     */
    public void groupToJson() throws JSONException {
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

                File dir = MemoryUtil.getGroupsDir();
                if (dir != null) {
                    String exportFileName = uuID + FileConfig.FILE_NAME_GROUP;

                    File file = new File(dir, exportFileName);
                    try {
                        writeFile(file, jObjectData.toString());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } while (c.moveToNext());
        }
        if (c != null) c.close();
        dataBase.close();
    }

    /**
     * Creates backup files on SD Card for all birthdays.
     * @throws JSONException
     */
    public void birthdayToJson() throws JSONException {
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
                if (title != null) title = encrypt(title);
                else title = encrypt(" ");
                jObjectData.put(encrypt(Constants.ContactConstants.COLUMN_CONTACT_NAME), title);
                jObjectData.put(encrypt(Constants.ContactConstants.COLUMN_CONTACT_BIRTHDAY), encrypt(date));
                jObjectData.put(encrypt(Constants.ContactConstants.COLUMN_CONTACT_NUMBER),
                        number != null ? encrypt(number) : encrypt(" "));
                jObjectData.put(encrypt(Constants.ContactConstants.COLUMN_CONTACT_MAIL),
                        mail != null ? encrypt(mail) : encrypt(" "));
                jObjectData.put(encrypt(Constants.ContactConstants.COLUMN_CONTACT_ID),
                        encrypt(String.valueOf(conId)));

                File dir = MemoryUtil.getBDir();
                if (dir != null) {
                    String exportFileName = uuID + FileConfig.FILE_NAME_BIRTHDAY;

                    File file = new File(dir, exportFileName);
                    try {
                        writeFile(file, jObjectData.toString());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else Log.i("reminder-info", "Couldn't find external storage!");
            } while (c.moveToNext());
        }
        if (c != null) c.close();
        dataBase.close();
    }

    /**
     * Creates backup files on SD Card for all reminders.
     * @throws JSONException
     */
    public void reminderToJson() throws JSONException {
        NextBase db = new NextBase(mContext);
        db.open();
        Cursor c = db.getReminders();
        if (c != null && c.moveToFirst()){
            do {
                String json = c.getString(c.getColumnIndex(NextBase.JSON));
                String uuID = c.getString(c.getColumnIndex(NextBase.UUID));
                int isDone = c.getInt(c.getColumnIndex(NextBase.DB_STATUS));
                int isArchived = c.getInt(c.getColumnIndex(NextBase.DB_LIST));
                if (isDone == 0 && isArchived == 0) {
                    File dir = MemoryUtil.getRDir();
                    if (dir != null) {
                        String exportFileName = uuID + FileConfig.FILE_NAME_REMINDER;

                        File file = new File(dir, exportFileName);
                        try {
                            writeFile(file, json);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else Log.i("reminder-info", "Couldn't find external storage!");
                }
            } while (c.moveToNext());
        }
        if (c != null) c.close();
        db.close();
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
     */
    public File createNote(String note, String date, String uuID, int color, byte[] image,
                           int style) throws JSONException {
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
        File dir = MemoryUtil.getMailDir();
        if (dir != null) {
            String exportFileName = uuID + FileConfig.FILE_NAME_NOTE;

            file = new File(dir, exportFileName);
            try {
                writeFile(file, jObjectData.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else Log.i("reminder-info", "Couldn't find external storage!");
        return file;
    }

    /**
     * Creates backup files on SD Card for all notes.
     * @throws JSONException
     */
    public void noteToJson() throws JSONException {
        NotesBase db = new NotesBase(mContext);
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

                File dir = MemoryUtil.getNDir();
                if (dir != null) {
                    String exportFileName = uuID + FileConfig.FILE_NAME_NOTE;

                    File file = new File(dir, exportFileName);
                    try {
                        writeFile(file, jObjectData.toString());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
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
     * @throws JSONException
     */
    public void noteFromJson(String file, String fileNameR) throws JSONException {
        File dir = MemoryUtil.getNDir();
        if (dir != null) {
            NotesBase db = new NotesBase(mContext);
            db.open();
            List<String> namesPass = new ArrayList<>();
            Cursor e = db.getNotes();
            if (e != null) {
                while (e.moveToNext()) {
                    for (e.moveToFirst(); !e.isAfterLast(); e.moveToNext()) {
                        namesPass.add(e.getString(e.getColumnIndex(Constants.COLUMN_UUID)));
                    }
                }
                e.close();
            }
            db.close();

            if (file != null){
                int pos = fileNameR.lastIndexOf(".");
                String fileNameS = fileNameR.substring(0, pos);
                if (!namesPass.contains(fileNameS)) {
                    String jsonText = readFile(file);
                    JSONObject jsonObj = new JSONObject(jsonText);
                    noteObject(jsonObj);
                }
            } else {
                File[] files = dir.listFiles();
                if (files != null) {
                    for (File file1 : files) {
                        String fileName = file1.getName();
                        int pos = fileName.lastIndexOf(".");
                        String fileLoc = dir + "/" + fileName;
                        String fileNameS = fileName.substring(0, pos);
                        if (!namesPass.contains(fileNameS)) {
                            String jsonText = readFile(fileLoc);
                            JSONObject jsonObj = new JSONObject(jsonText);
                            noteObject(jsonObj);
                        }
                    }
                }
            }
        }
    }

    /**
     * Read file content to string.
     * @param path path to file.
     * @return String content.
     */
    public static String readFile(String path) {
        FileInputStream stream = null;
        try {
            stream = new FileInputStream(path);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if (stream != null) {
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
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Log.d(Constants.LOG_TAG, "Read file ----" + writer.toString() + "----");
            return writer.toString();
        }
        return null;
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
        byte[] image = null;
        if (!jsonObj.isNull(Constants.COLUMN_IMAGE)) {
            image = Base64.decode(jsonObj.getString(Constants.COLUMN_IMAGE), Base64.DEFAULT);
        }
        SharedPrefs prefs = new SharedPrefs(mContext);
        if (!prefs.loadBoolean(Prefs.NOTE_ENCRYPT)){
            note = decrypt(note);
        }
        long linkId = jsonObj.getLong(Constants.COLUMN_LINK_ID);

        NotesBase db = new NotesBase(mContext);
        db.open();
        long id = db.saveNote(note, date, color, uuID, image, style);
        db.linkToReminder(id, linkId);
        db.close();
    }

    /**
     * Get note object from json.
     * @param filePath path to file.
     * @param json json object.
     * @return note object
     * @throws JSONException
     */
    public static NoteModel getNote(String filePath, String json) throws JSONException {
        if (filePath != null) {
            if (MemoryUtil.isSdPresent()){
                String jsonText = readFile(filePath);
                JSONObject jsonObj = new JSONObject(jsonText);
                String note = null;
                if (!jsonObj.isNull(Constants.COLUMN_NOTE)) {
                    note = jsonObj.getString(Constants.COLUMN_NOTE);
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
                byte[] image = null;
                if (!jsonObj.isNull(Constants.COLUMN_IMAGE)) {
                    image = Base64.decode(jsonObj.getString(Constants.COLUMN_IMAGE), Base64.DEFAULT);
                }

                return new NoteModel(note, color, style, image, uuID);
            } else return null;
        } else {
            JSONObject jsonObj = new JSONObject(json);
            String note = null;
            if (!jsonObj.isNull(Constants.COLUMN_NOTE)) {
                note = jsonObj.getString(Constants.COLUMN_NOTE);
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
            byte[] image = null;
            if (!jsonObj.isNull(Constants.COLUMN_IMAGE)) {
                image = Base64.decode(jsonObj.getString(Constants.COLUMN_IMAGE), Base64.DEFAULT);
            }

            return new NoteModel(note, color, style, image, uuID);
        }
    }

    /**
     * Get note color from file or JSON object.
     * @param file note file.
     * @param object JSON object.
     * @return Note color code
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
            if (MemoryUtil.isSdPresent()) {
                if (file != null) {
                    String jsonText = readFile(file.toString());
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
     * Get note color from JSON object.
     * @param jsonObj JSON object.
     * @return Note color code
     * @throws JSONException
     */
    private int getNoteColor(JSONObject jsonObj) throws JSONException {
        return jsonObj.getInt(Constants.COLUMN_COLOR);
    }

    /**
     * Restore reminder from JSON file to database.
     * Application restore reminders only with actual date.
     * @param file reminder file path.
     * @throws JSONException
     */
    public void reminderFromJson(String file) throws JSONException {
        File dir = MemoryUtil.getRDir();
        if (dir != null) {
            if (file != null){
                String jsonText = readFile(file);
                JSONObject jsonObj = new JSONObject(jsonText);
                reminderObject(jsonObj);
            } else {
                File[] files = dir.listFiles();
                if (files != null) {
                    for (File file1 : files) {
                        String fileName = file1.getName();
                        String fileLoc = dir + "/" + fileName;
                        String jsonText = readFile(fileLoc);
                        JSONObject jsonObj = new JSONObject(jsonText);
                        reminderObject(jsonObj);
                    }
                }
            }
        }
    }

    /**
     * Insert reminder to database from JSON object.
     * @param jsonObj JSON object.
     * @throws JSONException
     */
    private void reminderObject(JSONObject jsonObj) throws JSONException {
        JModel jModel = new JParser(jsonObj).parse();
        String uuID = jModel.getUuId();
        String type = jModel.getType();
        if (!Reminder.isUuId(mContext, uuID) && type != null) {
            if (type.contains(Constants.TYPE_LOCATION)){
                new LocationType(mContext, type).save(jModel);
            } else {
                if (type.startsWith(Constants.TYPE_WEEKDAY) ||
                        type.startsWith(Constants.TYPE_MONTHDAY)) {
                    JRecurrence jr = jModel.getRecurrence();
                    long time = new TimeCount(mContext).generateDateTime(type, jr.getMonthday(),
                            jModel.getEventTime(), jr.getRepeat(), jr.getWeekdays(), 0, 0);
                    jModel.setEventTime(time);
                    jModel.setStartTime(time);
                    new DateType(mContext, type).save(jModel);
                } else {
                    if (jModel.getEventTime() > System.currentTimeMillis()) {
                        new DateType(mContext, type).save(jModel);
                    }
                }
            }
        } else {
            if (type != null && type.matches(Constants.TYPE_SHOPPING_LIST)) {
                NextBase db = new NextBase(mContext);
                db.open();
                Cursor c = db.getReminder(uuID);
                if (c != null && c.moveToFirst()) {
                    String json = c.getString(c.getColumnIndex(NextBase.JSON));
                    JParser parser = new JParser(json);
                    JModel model = parser.parse();
                    ArrayList<String> uuIds = parser.getShoppingKeys();
                    List<JShopping> shoppings = jModel.getShoppings();
                    if (shoppings != null) {
                        for (JShopping item : shoppings) {
                            if (!uuIds.contains(item.getUuId())) {
                                model.setShopping(item);
                            }
                        }
                    }
                    new DateType(mContext, type).save(jModel);
                } else {
                    new DateType(mContext, type).save(jModel);
                }
                if (c != null) c.close();
                db.close();
            }
        }
    }

    /**
     * Restore group from JSON file to application.
     * @param file group file path.
     * @param fileNameR file name.
     * @throws JSONException
     */
    public void groupFromJson(String file, String fileNameR) throws JSONException {
        File dir = MemoryUtil.getGroupsDir();
        if (dir != null) {
            DataBase db = new DataBase(mContext);
            db.open();
            List<String> namesPass = new ArrayList<>();
            Cursor e = db.queryCategories();
            while (e.moveToNext()) {
                for (e.moveToFirst(); !e.isAfterLast(); e.moveToNext()) {
                    namesPass.add(e.getString(e.getColumnIndex(Constants.COLUMN_TECH_VAR)));
                }
            }
            e.close();
            db.close();

            if (file != null){
                int pos = fileNameR.lastIndexOf(".");
                String fileNameS = fileNameR.substring(0, pos);
                if (!namesPass.contains(fileNameS)) {
                    String jsonText = readFile(file);
                    JSONObject jsonObj = new JSONObject(jsonText);
                    groupObject(jsonObj);
                }
            } else {
                File[] files = dir.listFiles();
                if (files != null){
                    for (File file1 : files) {
                        String fileName = file1.getName();
                        int pos = fileName.lastIndexOf(".");
                        String fileLoc = dir + "/" + fileName;
                        String fileNameS = fileName.substring(0, pos);
                        if (!namesPass.contains(fileNameS)) {
                            String jsonText = readFile(fileLoc);
                            JSONObject jsonObj = new JSONObject(jsonText);
                            groupObject(jsonObj);
                        }
                    }
                }
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
        DataBase db = new DataBase(mContext);
        db.open();
        Cursor cf = db.queryCategories();
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
                db.addCategory(title, date, uuID, color);
            }
        } else {
            db.addCategory(title, date, uuID, color);
        }
        if (cf != null) cf.close();
        db.close();
    }

    /**
     * Get group object from file.
     * @param filePath path to file.
     * @return group object
     * @throws JSONException
     */
    public static CategoryModel getGroup(String filePath) throws JSONException {
        if (filePath != null) {
            if (MemoryUtil.isSdPresent()) {
                String jsonText = readFile(filePath);
                JSONObject jsonObj = new JSONObject(jsonText);
                String title = null;
                if (!jsonObj.isNull(Constants.COLUMN_TEXT)) {
                    title = jsonObj.getString(Constants.COLUMN_TEXT);
                }
                int color = jsonObj.getInt(Constants.COLUMN_COLOR);
                //long date = jsonObj.getLong(Constants.COLUMN_DATE_TIME);
                String uuID = null;
                if (!jsonObj.isNull(Constants.COLUMN_TECH_VAR)) {
                    uuID = jsonObj.getString(Constants.COLUMN_TECH_VAR);
                }

                return new CategoryModel(title, uuID, color);
            } else return null;
        } else return null;
    }

    /**
     * Restore birthday from JSON file to application.
     * @param file birthday file path.
     * @param fileNameR file name.
     * @throws JSONException
     */
    public void birthdayFromJson(String file, String fileNameR) throws JSONException {
        File dir = MemoryUtil.getBDir();
        if (dir != null) {
            DataBase db = new DataBase(mContext);
            db.open();
            List<String> namesPass = new ArrayList<>();
            Cursor e = db.getBirthdays();
            while (e.moveToNext()) {
                for (e.moveToFirst(); !e.isAfterLast(); e.moveToNext()) {
                    namesPass.add(e.getString(e.getColumnIndex(Constants.ContactConstants.COLUMN_CONTACT_UUID)));
                }
            }
            e.close();
            db.close();

            if (file != null){
                int pos = fileNameR.lastIndexOf(".");
                String fileNameS = fileNameR.substring(0, pos);
                if (!namesPass.contains(fileNameS)) {
                    String jsonText = readFile(file);
                    JSONObject jsonObj = new JSONObject(jsonText);
                    birthdayObject(jsonObj);
                }
            } else {
                File[] files = dir.listFiles();
                if (files != null && files.length > 0) {
                    for (File file1 : files) {
                        String fileName = file1.getName();
                        int pos = fileName.lastIndexOf(".");
                        String fileLoc = dir + "/" + fileName;
                        String fileNameS = fileName.substring(0, pos);
                        if (!namesPass.contains(fileNameS)) {
                            String jsonText = readFile(fileLoc);
                            JSONObject jsonObj = new JSONObject(jsonText);
                            birthdayObject(jsonObj);
                        }
                    }
                }
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
        /*String mail = null;
        key = encrypt(Constants.ContactConstants.COLUMN_CONTACT_MAIL);
        if (!jsonObj.isNull(key)) {
            mail = decrypt(jsonObj.getString(key));
        }*/
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
        DataBase db = new DataBase(mContext);
        db.open();
        Cursor cf = db.getBirthdays();
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
                db.addBirthday(name, conId, date, day, month, number, uuID);
            }
        } else {
            db.addBirthday(name, conId, date, day, month, number, uuID);
        }
        db.close();
    }

    /**
     * Get birthday object from file.
     * @param file path to file.
     * @return birthday object
     * @throws JSONException
     */
    public static BirthdayModel getBirthday(String file) throws JSONException {
        if (MemoryUtil.isSdPresent()){
            if (file != null) {
                String jsonText = readFile(file);
                JSONObject jsonObj = new JSONObject(jsonText);
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
                String id = null;
                key = encrypt(Constants.ContactConstants.COLUMN_CONTACT_ID);
                if (!jsonObj.isNull(key)) {
                    id = decrypt(jsonObj.getString(key));
                }
                int conId = 0;
                if (id != null) {
                    conId = Integer.parseInt(id);
                }

                return new BirthdayModel(name, conId, date, number);
            } else return null;
        } else return null;
    }

    /**
     * Generate unique identifier.
     * @return New generated Unique identifier
     */
    public static String generateID(){
        return UUID.randomUUID().toString();
    }

    /**
     * Check for internet connection.
     * @param context application context.
     * @return Boolean
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
                return urlc.getResponseCode() == 200;
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
     * @return Decrypted string
     */
    public static String decrypt(String string){
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
     * @return Encrypted string
     */
    public static String encrypt(String string){
        byte[] string_byted = null;
        try {
            string_byted = string.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return Base64.encodeToString(string_byted, Base64.DEFAULT).trim();
    }
}
