package com.cray.software.justreminder.reminder;

import android.content.Context;
import android.database.Cursor;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.helpers.Interval;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.TimeCount;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.interfaces.Prefs;

import java.util.ArrayList;
import java.util.List;

public class ReminderDataProvider {
    private Cursor c, old;
    private List<ReminderItem> data;
    private Context mContext;
    private Interval mInterval;
    private ReminderItem mLastRemovedData;
    private int mLastRemovedPosition = -1;

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

    public List<ReminderItem> getData(){
        return data;
    }

    public int getCount(){
        return data != null ? data.size() : 0;
    }

    public boolean hasActive(){
        boolean res = false;
        for (ReminderItem item : data){
            if (item.getCompleted() == 0) {
                res = true;
                break;
            }
        }
        return res;
    }

    public void deselectItems(){
        for (ReminderItem item : data){
            item.setSelected(false);
        }
    }

    public boolean isActive(int position){
        return data != null && data.get(position).getCompleted() == 0;
    }

    public int getPosition(ReminderItem item){
        int res = -1;
        if (data.size() > 0) {
            for (int i = 0; i < data.size(); i++){
                ReminderItem item1 = data.get(i);
                if (item.getId() == item1.getId()) {
                    res = i;
                    break;
                }
            }
        }
        return res;
    }

    public int removeItem(ReminderItem item){
        int res = 0;
        if (data.size() > 0) {
            for (int i = 0; i < data.size(); i++){
                ReminderItem item1 = data.get(i);
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

        final ReminderItem item = data.remove(from);

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

    public ArrayList<ReminderItem> getSelected(){
        ArrayList<ReminderItem> list = new ArrayList<>();
        for (ReminderItem item : data){
            if (item.getSelected()) list.add(item);
        }
        return list;
    }

    public ReminderItem getItem(int index) {
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
                            repeat = mContext.getString(R.string.interval_zero);
                        }
                    }
                } else {
                    due = TimeCount.getNextWeekdayTime(hour, minute, weekdays, delay);

                    if (weekdays.length() == 7) {
                        repeat = ReminderUtils.getRepeatString(mContext, weekdays);
                    } else {
                        repeat = mContext.getString(R.string.interval_zero);
                    }
                }

                data.add(new ReminderItem(title, type, repeat, categoryColor, uuID, isDone, due, id,
                        new double[]{lat, lon}, number, archived));
            } while (c.moveToNext());
        }
    }

    public void sort(){
        SharedPrefs prefs = new SharedPrefs(mContext);
        String orderPrefs = prefs.loadPrefs(Prefs.LIST_ORDER);
        ArrayList<ReminderItem> list = new ArrayList<>();
        list.clear();
        if (orderPrefs.matches(Constants.ORDER_DATE_A_Z)){
            for (ReminderItem item : data){
                long due = item.getDue();
                if (list.size() > 0){
                    int pos = 0;
                    for (int i = 0; i < list.size(); i++){
                        ReminderItem tmpItem = list.get(i);
                        if (due > tmpItem.getDue()) pos = i + 1;
                        else pos = i;
                    }
                    list.add(pos, item);
                } else {
                    list.add(0, item);
                }
            }
        } else if (orderPrefs.matches(Constants.ORDER_DATE_Z_A)){
            for (ReminderItem item : data){
                long due = item.getDue();
                if (list.size() > 0){
                    int pos = 0;
                    for (int i = 0; i < list.size(); i++){
                        ReminderItem tmpItem = list.get(i);
                        if (due < tmpItem.getDue()) pos = i + 1;
                        else pos = i;
                    }
                    list.add(pos, item);
                } else {
                    list.add(0, item);
                }
            }
        } else if (orderPrefs.matches(Constants.ORDER_DATE_WITHOUT_DISABLED_A_Z)){
            for (ReminderItem item : data){
                long due = item.getDue();
                int enabled = item.getCompleted();
                if (list.size() > 0){
                    int pos = 0;
                    for (int i = 0; i < list.size(); i++){
                        ReminderItem tmpItem = list.get(i);
                        if (enabled > tmpItem.getCompleted()){
                            pos = i + 1;
                        }
                        if (enabled == tmpItem.getCompleted()){
                            if (due > tmpItem.getDue()) pos = i + 1;
                            else pos = i;
                        }
                        if (enabled < tmpItem.getCompleted()){
                            pos = i;
                        }
                    }
                    list.add(pos, item);
                } else {
                    list.add(0, item);
                }
            }
        } else if (orderPrefs.matches(Constants.ORDER_DATE_WITHOUT_DISABLED_Z_A)){
            for (ReminderItem item : data){
                long due = item.getDue();
                int enabled = item.getCompleted();
                if (list.size() > 0){
                    int pos = 0;
                    for (int i = 0; i < list.size(); i++){
                        ReminderItem tmpItem = list.get(i);
                        if (enabled > tmpItem.getCompleted()){
                            pos = i + 1;
                        }
                        if (enabled == tmpItem.getCompleted()){
                            if (due < tmpItem.getDue()) pos = i + 1;
                            else pos = i;
                        }
                        if (enabled < tmpItem.getCompleted()){
                            pos = i;
                        }
                    }
                    list.add(pos, item);
                } else {
                    list.add(0, item);
                }
            }
        }
        data.clear();
        this.data = list;
    }

    public class ReminderItem {
        private String title, type, repeat, uuId, number;
        private int completed, archived, catColor;
        private long due, id;
        private double[] place;
        private boolean mPinnedToSwipeLeft, selected;

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
            this.selected = false;
        }

        public boolean getSelected(){
            return selected;
        }

        public void setSelected(boolean selected){
            this.selected = selected;
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
