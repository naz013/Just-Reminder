package com.cray.software.justreminder.reminder;

import android.app.AlarmManager;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.cray.software.justreminder.constants.Configs;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.databases.NextBase;
import com.cray.software.justreminder.datas.models.ReminderModel;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.TimeCount;
import com.cray.software.justreminder.json.JModel;
import com.cray.software.justreminder.json.JParser;
import com.cray.software.justreminder.json.JRecurrence;
import com.cray.software.justreminder.utils.TimeUtil;
import com.hexrain.flextcal.Events;
import com.hexrain.flextcal.FlextHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import hirondelle.date4j.DateTime;

public class ReminderDataProvider {

    public static final int VIEW_REMINDER = 15666;
    public static final int VIEW_SHOPPING_LIST = 15667;

    private ArrayList<ReminderModel> data;
    private Context mContext;
    private boolean isArchive = false;
    private boolean isReminder = false;
    private boolean isFeature = false;
    private String categoryId = null;
    private long time = 0;

    private HashMap<DateTime, Events> map = new HashMap<>();

    public ReminderDataProvider(Context mContext, boolean isReminder, boolean isFeature){
        this.mContext = mContext;
        this.isReminder = isReminder;
        this.isFeature = isFeature;
        map = new HashMap<>();
    }

    public ReminderDataProvider(Context mContext, boolean isArchive, String categoryId){
        data = new ArrayList<>();
        this.mContext = mContext;
        this.isArchive = isArchive;
        this.categoryId = categoryId;
        load();
    }

    public ReminderDataProvider(Context mContext, long time){
        data = new ArrayList<>();
        this.mContext = mContext;
        this.isArchive = false;
        this.categoryId = null;
        this.time = time;
        load();
    }

    public ArrayList<ReminderModel> getData(){
        return data;
    }

    public int getCount(){
        return data != null ? data.size() : 0;
    }

    public int getPosition(ReminderModel item){
        int res = -1;
        if (data.size() > 0) {
            for (int i = 0; i < data.size(); i++){
                ReminderModel item1 = data.get(i);
                if (item.getId() == item1.getId()) {
                    res = i;
                    break;
                }
            }
        }
        return res;
    }

    public ReminderModel getItem(int index) {
        if (index < 0 || index >= getCount()) {
            return null;
        }

        return data.get(index);
    }

    public void load(){
        data.clear();
        NextBase db = new NextBase(mContext);
        db.open();
        Map<String, Integer> map = getCategories(mContext);
        Cursor c = isArchive ? db.getArchivedReminders() : db.getReminders();
        if (categoryId != null) c = db.getReminders(categoryId);
        if (time > 0) c = db.getReminders(time);
        if (c != null && c.moveToNext()){
            do {
                String json = c.getString(c.getColumnIndex(NextBase.JSON));
                String type = c.getString(c.getColumnIndex(NextBase.TYPE));
                String categoryId = c.getString(c.getColumnIndex(NextBase.CATEGORY));
                int archived = c.getInt(c.getColumnIndex(NextBase.DB_LIST));
                int completed = c.getInt(c.getColumnIndex(NextBase.DB_STATUS));
                long id = c.getLong(c.getColumnIndex(NextBase._ID));

                int viewType = VIEW_REMINDER;
                if (type.matches(Constants.TYPE_SHOPPING_LIST)) viewType = VIEW_SHOPPING_LIST;

                int catColor = 0;
                if (map.containsKey(categoryId)) catColor = map.get(categoryId);

                //Log.d(Constants.LOG_TAG, "Json ---- " + json);
                JModel jModel = new JParser(json).parse();
                data.add(new ReminderModel(id, jModel, catColor, archived, completed, viewType));
            } while (c.moveToNext());
        }
        if (c != null) c.close();
        db.close();
    }

    private void setEvent(long eventTime, String summary, int color) {
        DateTime key = FlextHelper.convertToDateTime(eventTime);
        if (map.containsKey(key)) {
            Events events = map.get(key);
            events.addEvent(summary, color);
            map.put(key, events);
        } else {
            Events events = new Events(summary, color);
            map.put(key, events);
        }
    }

    public HashMap<DateTime, Events> getEvents() {
        ColorSetter cs = new ColorSetter(mContext);
        int bColor = cs.getColor(cs.colorBirthdayCalendar());

        if (isReminder) {
            int rColor = cs.getColor(cs.colorReminderCalendar());
            NextBase db = new NextBase(mContext);
            db.open();
            Cursor c = db.getActiveReminders();
            if (c != null && c.moveToNext()){
                Log.d(Constants.LOG_TAG, "Count " + c.getCount());
                do {
                    String json = c.getString(c.getColumnIndex(NextBase.JSON));
                    String mType = c.getString(c.getColumnIndex(NextBase.TYPE));
                    String summary = c.getString(c.getColumnIndex(NextBase.SUMMARY));
                    long eventTime = c.getLong(c.getColumnIndex(NextBase.EVENT_TIME));
                    if (!mType.contains(Constants.TYPE_LOCATION)) {
                        JModel jModel = new JParser(json).parse();
                        JRecurrence jRecurrence = jModel.getRecurrence();
                        long repeatTime = jRecurrence.getRepeat();
                        long limit = jRecurrence.getLimit();
                        long count = jModel.getCount();
                        int myDay = jRecurrence.getMonthday();
                        boolean isLimited = limit > 0;

                        if (eventTime > 0) {
                            setEvent(eventTime, summary, rColor);
                        } else continue;

                        if (isFeature) {
                            Calendar calendar1 = Calendar.getInstance();
                            calendar1.setTimeInMillis(eventTime);
                            if (mType.startsWith(Constants.TYPE_WEEKDAY)) {
                                long days = 0;
                                long max = Configs.MAX_DAYS_COUNT;
                                if (isLimited) max = limit - count;
                                ArrayList<Integer> list = jRecurrence.getWeekdays();
                                do {
                                    calendar1.setTimeInMillis(calendar1.getTimeInMillis() +
                                            AlarmManager.INTERVAL_DAY);
                                    eventTime = calendar1.getTimeInMillis();
                                    int weekDay = calendar1.get(Calendar.DAY_OF_WEEK);
                                    if (list.get(weekDay - 1) == 1 && eventTime > 0) {
                                        days++;
                                        setEvent(eventTime, summary, rColor);
                                    }
                                } while (days < max);
                            } else if (mType.startsWith(Constants.TYPE_MONTHDAY)) {
                                long days = 0;
                                long max = Configs.MAX_DAYS_COUNT;
                                if (isLimited) max = limit - count;
                                do {
                                    eventTime = TimeCount.getNextMonthDayTime(myDay,
                                            calendar1.getTimeInMillis() + TimeCount.DAY);
                                    calendar1.setTimeInMillis(eventTime);
                                    if (eventTime > 0) {
                                        days++;
                                        setEvent(eventTime, summary, rColor);
                                    }
                                } while (days < max);
                            } else {
                                if (repeatTime == 0) continue;
                                long days = 0;
                                long max = Configs.MAX_DAYS_COUNT;
                                if (isLimited) max = limit - count;
                                do {
                                    calendar1.setTimeInMillis(calendar1.getTimeInMillis() + repeatTime);
                                    eventTime = calendar1.getTimeInMillis();
                                    days++;
                                    setEvent(eventTime, summary, rColor);
                                } while (days < max);

                            }
                        }
                    }
                } while (c.moveToNext());
            }
            if (c != null) c.close();
            db.close();
        }

        DataBase db = new DataBase(mContext);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        db.open();
        Cursor c = db.getBirthdays();
        if (c != null && c.moveToFirst()){
            Log.d(Constants.LOG_TAG, "Count BD" + c.getCount());
            do {
                String birthday = c.getString(c.getColumnIndex(Constants.ContactConstants.COLUMN_CONTACT_BIRTHDAY));
                String name = c.getString(c.getColumnIndex(Constants.ContactConstants.COLUMN_CONTACT_NAME));
                Date date = null;
                try {
                    date = format.parse(birthday);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());
                int year = calendar.get(Calendar.YEAR);
                if (date != null) {
                    try {
                        calendar.setTime(date);
                    } catch (NullPointerException e){
                        e.printStackTrace();
                    }
                    int i = -1;
                    while (i < 2) {
                        calendar.set(Calendar.YEAR, year + i);
                        setEvent(calendar.getTimeInMillis(), name, bColor);
                        i++;
                    }
                }
            } while (c.moveToNext());
        }
        if (c != null) c.close();
        db.close();

        return map;
    }

    public static HashMap<DateTime, String> getBirthdays(Context context) {
        DataBase db = new DataBase(context);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        db.open();
        HashMap<DateTime, String> map = new HashMap<>();
        Cursor c = db.getBirthdays();
        if (c != null && c.moveToFirst()){
            do {
                String birthday = c.getString(c.getColumnIndex(Constants.ContactConstants.COLUMN_CONTACT_BIRTHDAY));
                String name = c.getString(c.getColumnIndex(Constants.ContactConstants.COLUMN_CONTACT_NAME));
                Date date = null;
                try {
                    date = format.parse(birthday);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());
                int year = calendar.get(Calendar.YEAR);
                if (date != null) {
                    try {
                        calendar.setTime(date);
                    } catch (NullPointerException e){
                        e.printStackTrace();
                    }
                    int day = calendar.get(Calendar.DAY_OF_MONTH);
                    int month = calendar.get(Calendar.MONTH);
                    Date bdDate = TimeUtil.getDate(year, month, day);
                    Date prevDate = TimeUtil.getDate(year - 1, month, day);
                    Date nextDate = TimeUtil.getDate(year + 1, month, day);
                    Date nextTwoDate = TimeUtil.getDate(year + 2, month, day);
                    map.put(FlextHelper.convertDateToDateTime(bdDate), name);
                    map.put(FlextHelper.convertDateToDateTime(prevDate), name);
                    map.put(FlextHelper.convertDateToDateTime(nextDate), name);
                    map.put(FlextHelper.convertDateToDateTime(nextTwoDate), name);
                }
            } while (c.moveToNext());
        }
        if (c != null) c.close();
        db.close();
        return map;
    }

    public static Map<String, Integer> getCategories(Context context) {
        Map<String, Integer> map = new HashMap<>();
        DataBase db = new DataBase(context);
        db.open();
        Cursor cf = db.queryCategories();
        if (cf != null && cf.moveToFirst()){
            do {
                String uuid = cf.getString(cf.getColumnIndex(Constants.COLUMN_TECH_VAR));
                int color = cf.getInt(cf.getColumnIndex(Constants.COLUMN_COLOR));
                map.put(uuid, color);
            } while (cf.moveToNext());
        }
        if (cf != null) cf.close();
        db.close();
        return map;
    }

    public static ReminderModel getItem(Context mContext, long id){
        ReminderModel item = null;
        NextBase db = new NextBase(mContext);
        db.open();
        Map<String, Integer> map = getCategories(mContext);
        Cursor c = db.getReminder(id);
        if (c != null && c.moveToNext()){
            String json = c.getString(c.getColumnIndex(NextBase.JSON));
            String type = c.getString(c.getColumnIndex(NextBase.TYPE));
            String summary = c.getString(c.getColumnIndex(NextBase.SUMMARY));
            String categoryId = c.getString(c.getColumnIndex(NextBase.CATEGORY));
            int archived = c.getInt(c.getColumnIndex(NextBase.DB_LIST));
            int completed = c.getInt(c.getColumnIndex(NextBase.DB_STATUS));

            int viewType = VIEW_REMINDER;
            if (type.matches(Constants.TYPE_SHOPPING_LIST)) viewType = VIEW_SHOPPING_LIST;

            int catColor = 0;
            if (map.containsKey(categoryId)) catColor = map.get(categoryId);

            JModel jModel = new JParser(json).parse();
            item = new ReminderModel(id, jModel, catColor, archived, completed, viewType);
        }
        if (c != null) c.close();
        db.close();
        return item;
    }
}
