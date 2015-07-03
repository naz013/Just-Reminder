package com.cray.software.justreminder.datas;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.view.View;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.helpers.Interval;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.TimeCount;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.utils.ReminderUtils;
import com.cray.software.justreminder.utils.Utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class ReminderDataProvider {
    Cursor c;
    ArrayList<ReminderItem> data;
    Context context;
    Interval mInterval;

    public ReminderDataProvider(Context context){
        data = new ArrayList<>();
        this.context = context;
        mInterval = new Interval(context);
    }

    public void setCursor(Cursor c){
        this.c = c;
    }

    public ArrayList<ReminderItem> getData(){
        return data;
    }

    public void removeItem(int position){
        if (data.size() > position) data.remove(position);
    }

    public void load(){
        DataBase db = new DataBase(context);
        db.open();
        if (c != null && c.moveToNext()){
            do {
                String title = c.getString(c.getColumnIndex(Constants.COLUMN_TEXT));
                String type = c.getString(c.getColumnIndex(Constants.COLUMN_TYPE));
                String number = c.getString(c.getColumnIndex(Constants.COLUMN_NUMBER));
                String weekdays = c.getString(c.getColumnIndex(Constants.COLUMN_WEEKDAYS));
                String categoryId = c.getString(c.getColumnIndex(Constants.COLUMN_CATEGORY));
                String uuID = c.getString(c.getColumnIndex(Constants.COLUMN_TECH_VAR));
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
                int repCount = c.getInt(c.getColumnIndex(Constants.COLUMN_REMINDERS_COUNT));
                int delay = c.getInt(c.getColumnIndex(Constants.COLUMN_DELAY));

                Cursor cf = db.getCategory(categoryId);
                int categoryColor = 0;
                if (cf != null && cf.moveToFirst()) {
                    categoryColor = cf.getInt(cf.getColumnIndex(Constants.COLUMN_COLOR));
                }
                if (cf != null) cf.close();

                String repeat = null;
                long due;

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
                            repeat = context.getString(R.string.interval_zero);
                        }
                    }
                } else {
                    due = TimeCount.getNextWeekdayTime(hour, minute, weekdays, delay);

                    if (weekdays.length() == 7) {
                        repeat = ReminderUtils.getRepeatString(context, weekdays);
                    }
                }

                data.add(new ReminderItem(title, ReminderUtils.getTypeString(context, type), repeat,
                        categoryColor, uuID, isDone, due, id, new double[]{lat, lon}, number, archived));
            } while (c.moveToNext());
        }
    }

    public class ReminderItem {
        String title, type, repeat, uuId, number;
        int completed, archived, catColor;
        long due, id;
        double[] place;
        private boolean mPinnedToSwipeLeft;

        public ReminderItem(String title, String type, String repeat, int catColor, String uuId,
                            int completed, long due, long id, double[] place, String number, int archived){
            this.catColor = catColor;
            this.title = title;
            this.type = type;
            this.due = due;
            this.id = id;
            this.completed = completed;
            this.uuId = uuId;
            this.place = place;
            this.repeat = repeat;
            this.number = number;
            this.archived = archived;
        }

        public int getArchived(){
            return archived;
        }

        public void setArchived(int archived){
            this.archived = archived;
        }

        public int getCompleted(){
            return completed;
        }

        public void setCompleted(int completed){
            this.completed = completed;
        }

        public double[] getPlace(){
            return place;
        }

        public void  setPlace(double[] place){
            this.place = place;
        }

        public long getDue(){
            return due;
        }

        public void setDue(long due){
            this.due = due;
        }

        public long getId(){
            return id;
        }

        public void setId(long id){
            this.id = id;
        }

        public String getTitle(){
            return title;
        }

        public void setTitle(String title){
            this.title = title;
        }

        public String getType(){
            return type;
        }

        public void setType(String type){
            this.type = type;
        }

        public String getRepeat(){
            return repeat;
        }

        public void setRepeat(String repeat){
            this.repeat = repeat;
        }

        public int getCatColor(){
            return catColor;
        }

        public void setCatColor(int catColor){
            this.catColor = catColor;
        }

        public String getUuId(){
            return uuId;
        }

        public void setUuId(String uuId){
            this.uuId = uuId;
        }

        public String getNumber(){
            return number;
        }

        public void setNumber(String number){
            this.number = number;
        }

        public boolean isPinnedToSwipeLeft() {
            return mPinnedToSwipeLeft;
        }

        public void setPinnedToSwipeLeft(boolean pinedToSwipeLeft) {
            mPinnedToSwipeLeft = pinedToSwipeLeft;
        }
    }

}
