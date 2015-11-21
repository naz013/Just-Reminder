package com.cray.software.justreminder.reminder;

import android.content.Context;
import android.database.Cursor;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.datas.models.ReminderModel;
import com.cray.software.justreminder.helpers.Interval;
import com.cray.software.justreminder.helpers.TimeCount;
import com.cray.software.justreminder.constants.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReminderDataProvider {
    private Cursor c, old;
    private List<ReminderModel> data;
    private Context mContext;
    private Interval mInterval;
    private ReminderModel mLastRemovedData;
    private int mLastRemovedPosition = -1;
    public static final int VIEW_REMINDER = 15666;
    public static final int VIEW_SHOPPING_LIST = 15667;

    public ReminderDataProvider(Context mContext){
        data = new ArrayList<>();
        this.mContext = mContext;
        mInterval = new Interval(mContext);
    }

    public ReminderDataProvider(Context mContext, Cursor c){
        data = new ArrayList<>();
        mInterval = new Interval(mContext);
        this.mContext = mContext;
        this.c = c;
        load();
    }

    public void setCursor(Cursor c){
        this.c = c;
        load();
    }

    public List<ReminderModel> getData(){
        return data;
    }

    public int getCount(){
        return data != null ? data.size() : 0;
    }

    public boolean hasActive(){
        boolean res = false;
        for (ReminderModel item : data){
            if (item.getCompleted() == 0) {
                res = true;
                break;
            }
        }
        return res;
    }

    public void deselectItems(){
        for (ReminderModel item : data){
            item.setSelected(false);
        }
    }

    public boolean isActive(int position){
        return data != null && data.get(position).getCompleted() == 0;
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

    public int removeItem(ReminderModel item){
        int res = 0;
        if (data.size() > 0) {
            for (int i = 0; i < data.size(); i++){
                ReminderModel item1 = data.get(i);
                if (item.getId() == item1.getId()) {
                    data.remove(i);
                    res = i;
                    break;
                }
            }
        }
        return res;
    }

    public void removeItem(int position){
        mLastRemovedData = data.remove(position);
        mLastRemovedPosition = position;
    }

    public void moveItem(int from, int to){
        if (to < 0 || to >= getCount()) {
            throw new IndexOutOfBoundsException("index = " + to);
        }

        if (from == to) {
            return;
        }

        final ReminderModel item = data.remove(from);

        data.add(to, item);
        mLastRemovedPosition = -1;
    }

    public int undoLastRemoval() {
        if (mLastRemovedData != null) {
            int insertedPosition;
            if (mLastRemovedPosition >= 0 && mLastRemovedPosition < data.size()) {
                insertedPosition = mLastRemovedPosition;
            } else {
                insertedPosition = data.size();
            }

            data.add(insertedPosition, mLastRemovedData);

            mLastRemovedData = null;
            mLastRemovedPosition = -1;

            return insertedPosition;
        } else {
            return -1;
        }
    }

    public ArrayList<ReminderModel> getSelected(){
        ArrayList<ReminderModel> list = new ArrayList<>();
        for (ReminderModel item : data){
            if (item.getSelected()) list.add(item);
        }
        return list;
    }

    public ReminderModel getItem(int index) {
        if (index < 0 || index >= getCount()) {
            return null;
        }

        return data.get(index);
    }

    public void setSelected(int position, boolean select){
        if (data.size() > position) data.get(position).setSelected(select);
    }

    public void load(){
        data.clear();
        DataBase db = new DataBase(mContext);
        db.open();
        Map<String, Integer> map = getCategories(mContext);
        if (c != null && c.moveToNext()){
            do {
                String title = c.getString(c.getColumnIndex(Constants.COLUMN_TEXT));
                String type = c.getString(c.getColumnIndex(Constants.COLUMN_TYPE));
                String number = c.getString(c.getColumnIndex(Constants.COLUMN_NUMBER));
                String weekdays = c.getString(c.getColumnIndex(Constants.COLUMN_WEEKDAYS));
                String categoryId = c.getString(c.getColumnIndex(Constants.COLUMN_CATEGORY));
                String uuID = c.getString(c.getColumnIndex(Constants.COLUMN_TECH_VAR));
                String exclusion = c.getString(c.getColumnIndex(Constants.COLUMN_EXTRA_3));
                String melody = c.getString(c.getColumnIndex(Constants.COLUMN_CUSTOM_MELODY));
                int hour = c.getInt(c.getColumnIndex(Constants.COLUMN_HOUR));
                int minute = c.getInt(c.getColumnIndex(Constants.COLUMN_MINUTE));
                int seconds = c.getInt(c.getColumnIndex(Constants.COLUMN_SECONDS));
                int day = c.getInt(c.getColumnIndex(Constants.COLUMN_DAY));
                int month = c.getInt(c.getColumnIndex(Constants.COLUMN_MONTH));
                int year = c.getInt(c.getColumnIndex(Constants.COLUMN_YEAR));
                int repCode = c.getInt(c.getColumnIndex(Constants.COLUMN_REPEAT));
                long repTime = c.getLong(c.getColumnIndex(Constants.COLUMN_REMIND_TIME));
                long id = c.getLong(c.getColumnIndex(Constants.COLUMN_ID));
                int isDone = c.getInt(c.getColumnIndex(Constants.COLUMN_IS_DONE));
                int archived = c.getInt(c.getColumnIndex(Constants.COLUMN_ARCHIVED));
                double lat = c.getDouble(c.getColumnIndex(Constants.COLUMN_LATITUDE));
                double lon = c.getDouble(c.getColumnIndex(Constants.COLUMN_LONGITUDE));
                long repCount = c.getInt(c.getColumnIndex(Constants.COLUMN_REMINDERS_COUNT));
                int delay = c.getInt(c.getColumnIndex(Constants.COLUMN_DELAY));
                int radius = c.getInt(c.getColumnIndex(Constants.COLUMN_CUSTOM_RADIUS));

                String repeat = null;
                long due = 0;

                if (type.startsWith(Constants.TYPE_MONTHDAY)){
                    due = TimeCount.getNextMonthDayTime(hour, minute, day, delay);
                } else if (!type.startsWith(Constants.TYPE_WEEKDAY)) {
                    due = TimeCount.getEventTime(year, month, day, hour, minute, seconds, repTime,
                            repCode, repCount, delay);

                    if (type.matches(Constants.TYPE_CALL) || type.matches(Constants.TYPE_MESSAGE) ||
                            type.matches(Constants.TYPE_REMINDER) || type.startsWith(Constants.TYPE_SKYPE) ||
                            type.startsWith(Constants.TYPE_APPLICATION)) {
                        repeat = mInterval.getInterval(repCode);
                    } else if (type.matches(Constants.TYPE_TIME)) {
                        repeat = mInterval.getTimeInterval(repCode);
                    } else {
                        if (!type.startsWith(Constants.TYPE_LOCATION) &&
                                !type.startsWith(Constants.TYPE_LOCATION_OUT)){
                            repeat = mContext.getString(R.string.interval_zero);
                        }
                    }
                } else if (!type.matches(Constants.TYPE_SHOPPING_LIST)){
                    due = TimeCount.getNextWeekdayTime(hour, minute, weekdays, delay);

                    if (weekdays.length() == 7) {
                        repeat = ReminderUtils.getRepeatString(mContext, weekdays);
                    } else {
                        repeat = mContext.getString(R.string.interval_zero);
                    }
                }

                int viewType = VIEW_REMINDER;
                if (type.matches(Constants.TYPE_SHOPPING_LIST)) viewType = VIEW_SHOPPING_LIST;

                int catColor = 0;
                if (map.containsKey(categoryId)) catColor = map.get(categoryId);

                data.add(new ReminderModel(title, type, repeat, catColor, uuID, isDone, due, id,
                        new double[]{lat, lon}, number, archived, viewType, categoryId, exclusion,
                        radius, melody));
            } while (c.moveToNext());
        }
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
        DataBase db = new DataBase(mContext);
        db.open();
        Map<String, Integer> map = getCategories(mContext);
        Cursor c = db.getReminder(id);
        if (c != null && c.moveToNext()){
            String title = c.getString(c.getColumnIndex(Constants.COLUMN_TEXT));
            String type = c.getString(c.getColumnIndex(Constants.COLUMN_TYPE));
            String number = c.getString(c.getColumnIndex(Constants.COLUMN_NUMBER));
            String weekdays = c.getString(c.getColumnIndex(Constants.COLUMN_WEEKDAYS));
            String categoryId = c.getString(c.getColumnIndex(Constants.COLUMN_CATEGORY));
            String uuID = c.getString(c.getColumnIndex(Constants.COLUMN_TECH_VAR));
            String exclusion = c.getString(c.getColumnIndex(Constants.COLUMN_EXTRA_3));
            String melody = c.getString(c.getColumnIndex(Constants.COLUMN_CUSTOM_MELODY));
            int hour = c.getInt(c.getColumnIndex(Constants.COLUMN_HOUR));
            int minute = c.getInt(c.getColumnIndex(Constants.COLUMN_MINUTE));
            int seconds = c.getInt(c.getColumnIndex(Constants.COLUMN_SECONDS));
            int day = c.getInt(c.getColumnIndex(Constants.COLUMN_DAY));
            int month = c.getInt(c.getColumnIndex(Constants.COLUMN_MONTH));
            int year = c.getInt(c.getColumnIndex(Constants.COLUMN_YEAR));
            int repCode = c.getInt(c.getColumnIndex(Constants.COLUMN_REPEAT));
            long repTime = c.getLong(c.getColumnIndex(Constants.COLUMN_REMIND_TIME));
            int isDone = c.getInt(c.getColumnIndex(Constants.COLUMN_IS_DONE));
            int archived = c.getInt(c.getColumnIndex(Constants.COLUMN_ARCHIVED));
            double lat = c.getDouble(c.getColumnIndex(Constants.COLUMN_LATITUDE));
            double lon = c.getDouble(c.getColumnIndex(Constants.COLUMN_LONGITUDE));
            long repCount = c.getInt(c.getColumnIndex(Constants.COLUMN_REMINDERS_COUNT));
            int delay = c.getInt(c.getColumnIndex(Constants.COLUMN_DELAY));
            int radius = c.getInt(c.getColumnIndex(Constants.COLUMN_CUSTOM_RADIUS));

            String repeat = null;
            long due = 0;

            if (type.startsWith(Constants.TYPE_MONTHDAY)){
                due = TimeCount.getNextMonthDayTime(hour, minute, day, delay);
            } else if (!type.startsWith(Constants.TYPE_WEEKDAY)) {
                due = TimeCount.getEventTime(year, month, day, hour, minute, seconds, repTime,
                        repCode, repCount, delay);

                if (type.matches(Constants.TYPE_CALL) || type.matches(Constants.TYPE_MESSAGE) ||
                        type.matches(Constants.TYPE_REMINDER) || type.startsWith(Constants.TYPE_SKYPE) ||
                        type.startsWith(Constants.TYPE_APPLICATION)) {
                    repeat = new Interval(mContext).getInterval(repCode);
                } else if (type.matches(Constants.TYPE_TIME)) {
                    repeat = new Interval(mContext).getTimeInterval(repCode);
                } else {
                    if (!type.startsWith(Constants.TYPE_LOCATION) &&
                            !type.startsWith(Constants.TYPE_LOCATION_OUT)){
                        repeat = mContext.getString(R.string.interval_zero);
                    }
                }
            } else if (!type.matches(Constants.TYPE_SHOPPING_LIST)){
                due = TimeCount.getNextWeekdayTime(hour, minute, weekdays, delay);

                if (weekdays.length() == 7) {
                    repeat = ReminderUtils.getRepeatString(mContext, weekdays);
                } else {
                    repeat = mContext.getString(R.string.interval_zero);
                }
            } else {
                due = TimeCount.getEventTime(year, month, day, hour, minute, seconds, repTime,
                        repCode, repCount, delay);
            }

            int viewType = VIEW_REMINDER;
            if (type.matches(Constants.TYPE_SHOPPING_LIST)) viewType = VIEW_SHOPPING_LIST;

            int catColor = 0;
            if (map.containsKey(categoryId)) catColor = map.get(categoryId);

            item = new ReminderModel(title, type, repeat, catColor, uuID, isDone, due, id,
                    new double[]{lat, lon}, number, archived, viewType, categoryId, exclusion,
                    radius, melody);
        }
        return item;
    }
}
