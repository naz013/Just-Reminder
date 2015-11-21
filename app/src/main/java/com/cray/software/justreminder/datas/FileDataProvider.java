package com.cray.software.justreminder.datas;

import android.content.Context;
import android.database.Cursor;

import com.cray.software.justreminder.databases.FilesDataBase;
import com.cray.software.justreminder.datas.models.FileModel;
import com.cray.software.justreminder.helpers.Interval;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.reminder.ReminderUtils;
import com.cray.software.justreminder.utils.TimeUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class FileDataProvider {
    private List<FileModel> data;
    private Context mContext;
    private FileModel mLastRemovedData;
    private int mLastRemovedPosition = -1;
    private String where;

    public FileDataProvider(Context mContext, String where){
        data = new ArrayList<>();
        this.mContext = mContext;
        this.where = where;
        load();
    }

    public String getWhere() {
        return where;
    }

    public List<FileModel> getData(){
        return data;
    }

    public int getCount(){
        return data != null ? data.size() : 0;
    }

    public int getPosition(FileModel item){
        int res = -1;
        if (data.size() > 0) {
            for (int i = 0; i < data.size(); i++){
                FileModel item1 = data.get(i);
                if (item.getId() == item1.getId()) {
                    res = i;
                    break;
                }
            }
        }
        return res;
    }

    public int removeItem(FileModel item){
        int res = 0;
        if (data.size() > 0) {
            for (int i = 0; i < data.size(); i++){
                FileModel item1 = data.get(i);
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

        final FileModel item = data.remove(from);

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

    public FileModel getItem(int index) {
        if (index < 0 || index >= getCount()) {
            return null;
        }

        return data.get(index);
    }

    public void load() {
        data.clear();
        FilesDataBase db = new FilesDataBase(mContext);
        db.open();
        Cursor c = db.getFile(where);
        boolean is24 = new SharedPrefs(mContext).loadBoolean(Prefs.IS_24_TIME_FORMAT);
        Interval interval = new Interval(mContext);
        if (c != null && c.moveToFirst()) {
            do {
                String title = c.getString(c.getColumnIndex(Constants.COLUMN_TEXT));
                String fileNameS = c.getString(c.getColumnIndex(Constants.FilesConstants.COLUMN_FILE_NAME));
                String typeS = c.getString(c.getColumnIndex(Constants.COLUMN_TYPE));
                String numberS = c.getString(c.getColumnIndex(Constants.COLUMN_NUMBER));
                String weekdays = c.getString(c.getColumnIndex(Constants.COLUMN_WEEKDAYS));
                int hour = c.getInt(c.getColumnIndex(Constants.COLUMN_HOUR));
                int minute = c.getInt(c.getColumnIndex(Constants.COLUMN_MINUTE));
                int day = c.getInt(c.getColumnIndex(Constants.COLUMN_DAY));
                int month = c.getInt(c.getColumnIndex(Constants.COLUMN_MONTH));
                int year = c.getInt(c.getColumnIndex(Constants.COLUMN_YEAR));
                int repCode = c.getInt(c.getColumnIndex(Constants.COLUMN_REPEAT));
                long repTime = c.getLong(c.getColumnIndex(Constants.COLUMN_REMIND_TIME));
                long lastModifiedS = c.getLong(c.getColumnIndex(Constants.FilesConstants.COLUMN_FILE_LAST_EDIT));
                long id = c.getLong(c.getColumnIndex(Constants.COLUMN_ID));
                double lat = c.getDouble(c.getColumnIndex(Constants.COLUMN_LATITUDE));
                double longi = c.getDouble(c.getColumnIndex(Constants.COLUMN_LONGITUDE));

                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(lastModifiedS);
                Date date1 = calendar.getTime();

                String repeat = "";
                String time = null;
                String date = null;
                String number = "";
                if (numberS != null) number = numberS;

                if (typeS.matches(Constants.TYPE_REMINDER) || typeS.matches(Constants.TYPE_MESSAGE)
                        || typeS.matches(Constants.TYPE_CALL) || typeS.startsWith(Constants.TYPE_SKYPE)
                        || typeS.matches(Constants.TYPE_APPLICATION) || typeS.matches(Constants.TYPE_APPLICATION_BROWSER)){
                    calendar.setTimeInMillis(System.currentTimeMillis());
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, day);
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.HOUR_OF_DAY, hour);
                    calendar.set(Calendar.MINUTE, minute);
                    Date mTime = calendar.getTime();
                    date = TimeUtil.dateFormat.format(mTime);
                    time = TimeUtil.getTime(mTime, is24);
                    repeat = interval.getInterval(repCode);
                } else if (typeS.matches(Constants.TYPE_TIME)){
                    time = "";
                    date = TimeUtil.generateAfterString(repTime);
                    repeat = interval.getTimeInterval(repCode);
                } else if (typeS.startsWith(Constants.TYPE_WEEKDAY)){
                    calendar.set(Calendar.HOUR_OF_DAY, hour);
                    calendar.set(Calendar.MINUTE, minute);
                    time = TimeUtil.getTime(calendar.getTime(), is24);
                    if (weekdays.length() == 7) {
                        date = ReminderUtils.getRepeatString(mContext, weekdays);
                    }
                } else if (typeS.startsWith(Constants.TYPE_LOCATION) || typeS.startsWith(Constants.TYPE_LOCATION_OUT)){
                    date = String.format("%.5f", lat);
                    time = String.format("%.5f", longi);
                }
                data.add(new FileModel(title, fileNameS, ReminderUtils.getTypeString(mContext, typeS),
                        TimeUtil.getDateTime(date1, is24), time, date, repeat, number, id));
            }
            while (c.moveToNext());
        }
        if (c != null) c.close();
        db.close();
    }
}
