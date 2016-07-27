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
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Base64;
import android.util.Log;

import com.cray.software.justreminder.birthdays.BirthdayHelper;
import com.cray.software.justreminder.birthdays.BirthdayItem;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.FileConfig;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.reminder.NextBase;
import com.cray.software.justreminder.groups.GroupHelper;
import com.cray.software.justreminder.groups.GroupItem;
import com.cray.software.justreminder.reminder.json.JsonModel;
import com.cray.software.justreminder.reminder.json.JParser;
import com.cray.software.justreminder.reminder.json.JRecurrence;
import com.cray.software.justreminder.reminder.json.JShopping;
import com.cray.software.justreminder.notes.NoteHelper;
import com.cray.software.justreminder.notes.NoteItem;
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

    /**
     * SimpleDateFormat variable for date parsing.
     */
    private static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

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
        List<GroupItem> groups = GroupHelper.getInstance(mContext).getAll();
        if (groups != null && groups.size() > 0){
            for (GroupItem item : groups) {
                JSONObject jObjectData = new JSONObject();
                jObjectData.put(Constants.COLUMN_COLOR, item.getColor());
                jObjectData.put(Constants.COLUMN_TEXT, item.getTitle());
                jObjectData.put(Constants.COLUMN_DATE_TIME, item.getDateTime());
                jObjectData.put(Constants.COLUMN_TECH_VAR, item.getUuId());

                File dir = MemoryUtil.getGroupsDir();
                if (dir != null) {
                    String exportFileName = item.getUuId() + FileConfig.FILE_NAME_GROUP;

                    File file = new File(dir, exportFileName);
                    try {
                        writeFile(file, jObjectData.toString());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * Creates backup files on SD Card for all birthdays.
     * @throws JSONException
     */
    public void birthdayToJson() throws JSONException {
        List<BirthdayItem> list = BirthdayHelper.getInstance(mContext).getAll();
        for (BirthdayItem item : list) {
            JSONObject jObjectData = new JSONObject();
            String name = item.getName();
            if (name != null) name = encrypt(name);
            else name = encrypt(" ");
            String number = item.getNumber();
            jObjectData.put(encrypt(Constants.Contacts.COLUMN_NAME), name);
            jObjectData.put(encrypt(Constants.Contacts.COLUMN_BIRTHDATE), encrypt(item.getDate()));
            jObjectData.put(encrypt(Constants.Contacts.COLUMN_NUMBER), number != null ? encrypt(number) : encrypt(" "));
            jObjectData.put(encrypt(Constants.Contacts.COLUMN_CONTACT_ID), encrypt(String.valueOf(item.getUuId())));

            File dir = MemoryUtil.getBDir();
            if (dir != null) {
                String exportFileName = item.getUuId() + FileConfig.FILE_NAME_BIRTHDAY;
                File file = new File(dir, exportFileName);
                try {
                    writeFile(file, jObjectData.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else Log.i("reminder-info", "Couldn't find external storage!");
        }
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
     * @return Note file
     * @throws JSONException
     */
    public File createNote(NoteItem item) throws JSONException {
        JSONObject jObjectData = new JSONObject();
        jObjectData.put(Constants.COLUMN_COLOR, item.getStyle());
        jObjectData.put(Constants.COLUMN_FONT_STYLE, item.getColor());
        jObjectData.put(Constants.COLUMN_DATE, item.getDate());
        jObjectData.put(Constants.COLUMN_UUID, item.getUuId());
        jObjectData.put(Constants.COLUMN_NOTE, encrypt(item.getNote()));
        byte[] image = item.getImage();
        if (image != null) {
            jObjectData.put(Constants.COLUMN_IMAGE, Base64.encodeToString(image, Base64.DEFAULT));
        } else {
            jObjectData.put(Constants.COLUMN_IMAGE, null);
        }
        if (SharedPrefs.getInstance(mContext).getBoolean(Prefs.NOTE_ENCRYPT)){
            jObjectData.put(Constants.COLUMN_ENCRYPTED, 1);
        } else {
            jObjectData.put(Constants.COLUMN_ENCRYPTED, 0);
        }
        File file = null;
        File dir = MemoryUtil.getMailDir();
        if (dir != null) {
            String exportFileName = item.getUuId() + FileConfig.FILE_NAME_NOTE;
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
        List<NoteItem> list = NoteHelper.getInstance(mContext).getAll();
        for (NoteItem item : list) {
            JSONObject jObjectData = new JSONObject();
            jObjectData.put(Constants.COLUMN_COLOR, item.getColor());
            jObjectData.put(Constants.COLUMN_FONT_STYLE, item.getStyle());
            jObjectData.put(Constants.COLUMN_DATE, item.getDate());
            jObjectData.put(Constants.COLUMN_UUID, item.getUuId());
            jObjectData.put(Constants.COLUMN_LINK_ID, item.getLinkId());
            if (item.getImage() != null) {
                jObjectData.put(Constants.COLUMN_IMAGE, Base64.encodeToString(item.getImage(), Base64.DEFAULT));
            } else jObjectData.put(Constants.COLUMN_IMAGE, item.getImage());
            if (SharedPrefs.getInstance(mContext).getBoolean(Prefs.NOTE_ENCRYPT)){
                jObjectData.put(Constants.COLUMN_ENCRYPTED, 1);
                jObjectData.put(Constants.COLUMN_NOTE, item.getNote());
            } else {
                jObjectData.put(Constants.COLUMN_ENCRYPTED, 0);
                jObjectData.put(Constants.COLUMN_NOTE, encrypt(item.getNote()));
            }

            File dir = MemoryUtil.getNDir();
            if (dir != null) {
                String exportFileName = item.getUuId() + FileConfig.FILE_NAME_NOTE;

                File file = new File(dir, exportFileName);
                try {
                    writeFile(file, jObjectData.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else Log.i("reminder-info", "Couldn't find external storage!");
        }
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
            List<String> allUuId = NoteHelper.getInstance(mContext).getAllUuId();
            if (file != null){
                int pos = fileNameR.lastIndexOf(".");
                String fileNameS = fileNameR.substring(0, pos);
                if (!allUuId.contains(fileNameS)) {
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
                        if (!allUuId.contains(fileNameS)) {
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
        if (!SharedPrefs.getInstance(mContext).getBoolean(Prefs.NOTE_ENCRYPT)){
            note = decrypt(note);
        }
        long linkId = jsonObj.getLong(Constants.COLUMN_LINK_ID);
        NoteItem item = new NoteItem(note, uuID, date, color, style, image, 0, linkId);
        NoteHelper.getInstance(mContext).saveNote(item);
    }

    /**
     * Get note object from json.
     * @param filePath path to file.
     * @param json json object.
     * @return note object
     * @throws JSONException
     */
    public static NoteItem getNote(String filePath, String json) throws JSONException {
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
                String date = null;
                if (!jsonObj.isNull(Constants.COLUMN_DATE)) {
                    date = jsonObj.getString(Constants.COLUMN_DATE);
                }
                byte[] image = null;
                if (!jsonObj.isNull(Constants.COLUMN_IMAGE)) {
                    image = Base64.decode(jsonObj.getString(Constants.COLUMN_IMAGE), Base64.DEFAULT);
                }
                long linkId = jsonObj.getLong(Constants.COLUMN_LINK_ID);
                return new NoteItem(note, uuID, date, color, style, image, 0, linkId);
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
            String date = null;
            if (!jsonObj.isNull(Constants.COLUMN_DATE)) {
                date = jsonObj.getString(Constants.COLUMN_DATE);
            }
            byte[] image = null;
            if (!jsonObj.isNull(Constants.COLUMN_IMAGE)) {
                image = Base64.decode(jsonObj.getString(Constants.COLUMN_IMAGE), Base64.DEFAULT);
            }
            long linkId = jsonObj.getLong(Constants.COLUMN_LINK_ID);
            return new NoteItem(note, uuID, date, color, style, image, 0, linkId);
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
        if (dir != null && dir.exists()) {
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
        JsonModel jsonModel = new JParser(jsonObj).parse();
        if (jsonModel == null) return;
        String uuID = jsonModel.getUuId();
        String type = jsonModel.getType();
        if (uuID != null && !Reminder.isUuId(mContext, uuID) && type != null) {
            if (type.contains(Constants.TYPE_LOCATION)){
                new LocationType(mContext, type).save(jsonModel);
            } else {
                if (type.startsWith(Constants.TYPE_WEEKDAY) ||
                        type.startsWith(Constants.TYPE_MONTHDAY)) {
                    JRecurrence jr = jsonModel.getRecurrence();
                    long time = new TimeCount(mContext).generateDateTime(type, jr.getMonthday(),
                            jsonModel.getEventTime(), jr.getRepeat(), jr.getWeekdays(), 0, 0);
                    jsonModel.setEventTime(time);
                    jsonModel.setStartTime(time);
                    new DateType(mContext, type).save(jsonModel);
                } else {
                    if (jsonModel.getEventTime() > System.currentTimeMillis()) {
                        new DateType(mContext, type).save(jsonModel);
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
                    JsonModel model = parser.parse();
                    ArrayList<String> uuIds = parser.getShoppingKeys();
                    List<JShopping> shoppings = jsonModel.getShoppings();
                    if (shoppings != null) {
                        for (JShopping item : shoppings) {
                            if (!uuIds.contains(item.getUuId())) {
                                model.setShopping(item);
                            }
                        }
                    }
                    new DateType(mContext, type).save(jsonModel);
                } else {
                    new DateType(mContext, type).save(jsonModel);
                }
                if (c != null) c.close();
                db.close();
            }
        }
    }

    /**
     * Restore group from JSON file to application.
     * @param file group file path.
     * @throws JSONException
     */
    public void groupFromJson(File file) throws JSONException {
        List<String> uuIdList = new ArrayList<>();
        for (GroupItem groupItem : GroupHelper.getInstance(mContext).getAll()) {
            uuIdList.add(groupItem.getUuId());
        }

        if (file != null){
            String fileNameR = file.getName();
            int pos = fileNameR.lastIndexOf(".");
            String fileNameS = fileNameR.substring(0, pos);
            if (!uuIdList.contains(fileNameS)) {
                String jsonText = readFile(file.toString());
                JSONObject jsonObj = new JSONObject(jsonText);
                groupObject(jsonObj);
            }
        } else {
            File dir = MemoryUtil.getGroupsDir();
            File[] files = dir.listFiles();
            if (files != null){
                for (File file1 : files) {
                    String fileName = file1.getName();
                    int pos = fileName.lastIndexOf(".");
                    String fileLoc = dir + "/" + fileName;
                    String fileNameS = fileName.substring(0, pos);
                    if (!uuIdList.contains(fileNameS)) {
                        String jsonText = readFile(fileLoc);
                        JSONObject jsonObj = new JSONObject(jsonText);
                        groupObject(jsonObj);
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
        GroupHelper helper = GroupHelper.getInstance(mContext);
        List<GroupItem> itemList = helper.getAll();
        if (itemList.size() > 0) {
            List<String> uuIdsList = new ArrayList<>();
            List<String> titles = new ArrayList<>();
            for (GroupItem item : itemList) {
                uuIdsList.add(item.getUuId());
                titles.add(item.getTitle());
            }
            if (!uuIdsList.contains(uuID) && !titles.contains(title)) {
                helper.saveGroup(new GroupItem(title, uuID, color, 0, date));
            }
        } else {
            helper.saveGroup(new GroupItem(title, uuID, color, 0, date));
        }
    }

    /**
     * Get group object from file.
     * @param filePath path to file.
     * @return group object
     * @throws JSONException
     */
    public static GroupItem getGroup(String filePath) throws JSONException {
        if (filePath != null) {
            if (MemoryUtil.isSdPresent()) {
                String jsonText = readFile(filePath);
                JSONObject jsonObj = new JSONObject(jsonText);
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

                return new GroupItem(title, uuID, color, 0, date);
            } else return null;
        } else return null;
    }

    /**
     * Restore birthday from JSON file to application.
     * @param file birthday file path.
     * @throws JSONException
     */
    public void birthdayFromJson(File file) throws JSONException {
        List<String> namesPass = BirthdayHelper.getInstance(mContext).getNames();
        if (file != null){
            String fileNameR = file.getName();
            int pos = fileNameR.lastIndexOf(".");
            String fileNameS = fileNameR.substring(0, pos);
            if (!namesPass.contains(fileNameS)) {
                String jsonText = readFile(file.toString());
                JSONObject jsonObj = new JSONObject(jsonText);
                birthdayObject(jsonObj);
            }
        } else {
            File dir = MemoryUtil.getBDir();
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

    /**
     * Insert birthdays from JSON object to database.
     * @param jsonObj JSON object.
     * @throws JSONException
     */
    private void birthdayObject(JSONObject jsonObj) throws JSONException {
        String name = null;
        String key = encrypt(Constants.Contacts.COLUMN_NAME);
        if (!jsonObj.isNull(key)) {
            name = decrypt(jsonObj.getString(key));
        }
        String date = null;
        key = encrypt(Constants.Contacts.COLUMN_BIRTHDATE);
        if (!jsonObj.isNull(key)) {
            date = decrypt(jsonObj.getString(key));
        }
        String number = null;
        key = encrypt(Constants.Contacts.COLUMN_NUMBER);
        if (!jsonObj.isNull(key)) {
            number = decrypt(jsonObj.getString(key));
        }
        String uuId = null;
        key = encrypt(Constants.Contacts.COLUMN_UUID);
        if (!jsonObj.isNull(key)) {
            uuId = decrypt(jsonObj.getString(key));
        }
        String id = null;
        key = encrypt(Constants.Contacts.COLUMN_CONTACT_ID);
        if (!jsonObj.isNull(key)) {
            id = decrypt(jsonObj.getString(key));
        }
        int contactId = 0;
        if (id != null) {
            contactId = Integer.parseInt(id);
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
        List<String> names = BirthdayHelper.getInstance(mContext).getNames();
        List<String> numbers = BirthdayHelper.getInstance(mContext).getNumbers();
        if (!names.contains(name) && !numbers.contains(number)) {
            BirthdayItem item = new BirthdayItem(0, name, date, number, uuId, null, contactId, day, month);
            BirthdayHelper.getInstance(mContext).saveBirthday(item);
        }
    }

    /**
     * Get birthday object from file.
     * @param file path to file.
     * @return birthday object
     * @throws JSONException
     */
    public static BirthdayItem getBirthday(String file) {
        if (MemoryUtil.isSdPresent()){
            if (file != null) {
                try {
                    String jsonText = readFile(file);
                    JSONObject jsonObj = new JSONObject(jsonText);
                    String name = null;
                    String key = encrypt(Constants.Contacts.COLUMN_NAME);
                    if (!jsonObj.isNull(key)) {
                        name = decrypt(jsonObj.getString(key));
                    }
                    String date = null;
                    key = encrypt(Constants.Contacts.COLUMN_BIRTHDATE);
                    if (!jsonObj.isNull(key)) {
                        date = decrypt(jsonObj.getString(key));
                    }
                    String number = null;
                    key = encrypt(Constants.Contacts.COLUMN_NUMBER);
                    if (!jsonObj.isNull(key)) {
                        number = decrypt(jsonObj.getString(key));
                    }
                    String id = null;
                    key = encrypt(Constants.Contacts.COLUMN_CONTACT_ID);
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
                    return new BirthdayItem(0, name, date, number, null, null, conId, day, month);
                } catch (JSONException e) {
                    return null;
                }
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
