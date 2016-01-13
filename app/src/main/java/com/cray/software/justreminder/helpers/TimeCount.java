package com.cray.software.justreminder.helpers;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.databases.NextBase;
import com.cray.software.justreminder.json.JsonModel;
import com.cray.software.justreminder.json.JsonParser;
import com.cray.software.justreminder.json.JsonRecurrence;
import com.cray.software.justreminder.utils.TimeUtil;
import com.cray.software.justreminder.utils.ViewUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Helper class for working with date and time.
 */
public class TimeCount {

    /**
     * Millisecond constants.
     */
    public final static long minute = 60 * 1000;
    public final static long hour = minute  * 60;
    public final static long halfDay = hour * 12;
    public final static long day = halfDay * 2;

    private Context mContext;

    public TimeCount(Context context){
        this.mContext = context;
    }

    /**
     * Get drawable from resource.
     * @param resource resource.
     * @return Drawable
     */
    private Drawable getDrawable(int resource){
        return ViewUtils.getDrawable(mContext, resource);
    }

    /**
     * Get drawable indicator based on time difference.
     * @param time target time.
     * @return Drawable
     */
    public Drawable getDifference(long time){
        Drawable color;
        if (time == 0) {
            color = getDrawable(R.color.material_divider);
        } else {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(System.currentTimeMillis());
            long currTime = cal.getTimeInMillis();

            long diff = time - currTime;
            if (isBetween(diff, 0, minute * 5)) {
                color = getDrawable(R.drawable.drawable_red);
            } else if (isBetween(diff, minute * 5, hour)) {
                color = getDrawable(R.drawable.drawable_yellow);
            } else if (isBetween(diff, hour, halfDay)) {
                color = getDrawable(R.drawable.drawable_green);
            } else if (isBetween(diff, halfDay, day)) {
                color = getDrawable(R.drawable.drawable_blue);
            } else if ((diff > day)) {
                color = getDrawable(R.drawable.drawable_indigo);
            } else {
                color = getDrawable(R.drawable.drawable_grey);
            }
        }
        return color;
    }

    /**
     * Get next date and time for milliseconds.
     * @param timeLong time in milliseconds.
     * @return [0 - date] [1 - time]
     */
    public String[] getNextDateTime(long timeLong){
        String date;
        String time;
        if (timeLong == 0) {
            date = null;
            time = null;
        } else {
            Calendar cl = Calendar.getInstance();
            cl.setTimeInMillis(timeLong);
            Date mTime = cl.getTime();
            date = TimeUtil.dateFormat.format(mTime);
            time = TimeUtil.getTime(mTime,
                    new SharedPrefs(mContext).loadBoolean(Prefs.IS_24_TIME_FORMAT));
        }
        return new String[]{date, time};
    }

    /**
     * Get next date and time for time parameters.
     * @param year year.
     * @param month month.
     * @param dayOfMonth day.
     * @param hourOfDay hour.
     * @param minuteOfHour minute.
     * @param seconds seconds.
     * @param inTime timer reminder time.
     * @param repeatCode reminder repeat code.
     * @param remCount number of reminder repeats.
     * @param delay delay for reminder.
     * @return [0 - date] [1 - time]
     */
    public String[] getNextDateTime(int year, int month, int dayOfMonth, int hourOfDay,
                                    int minuteOfHour, int seconds, long inTime, int repeatCode,
                                    long remCount, int delay){
        String date;
        String time;
        if (year == 0 && month == 0 && dayOfMonth == 0 && hourOfDay == 0 && minuteOfHour == 0) {
            date = null;
            time = null;
        } else {
            long newDbTime = getEventTime(year, month, dayOfMonth, hourOfDay, minuteOfHour, seconds,
                    inTime, repeatCode, remCount, delay);
            Calendar cl = Calendar.getInstance();
            cl.setTimeInMillis(newDbTime);
            Date mTime = cl.getTime();
            date = TimeUtil.dateFormat.format(mTime);
            time = TimeUtil.getTime(mTime,
                    new SharedPrefs(mContext).loadBoolean(Prefs.IS_24_TIME_FORMAT));
        }
        return new String[]{date, time};
    }

    public long generateStartEvent(String type, int dayOfMonth, int month, int year, int hour,
                                  int minute, int seconds, ArrayList<Integer> weekdays) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        if (type.startsWith(Constants.TYPE_WEEKDAY)){
            return getNextWeekdayTime(calendar.getTimeInMillis(), weekdays, 0);
        } else if (type.startsWith(Constants.TYPE_MONTHDAY)){
            if (type.endsWith("_last")){
                return getLastMonthDayTime(calendar.getTimeInMillis(), 0);
            } else {
                return getNextMonthDayTime(dayOfMonth, calendar.getTimeInMillis(), 0);
            }
        } else {
            calendar.set(year, month, dayOfMonth, hour, minute, seconds);
            return calendar.getTimeInMillis();
        }
    }

    /**
     * Generate new due time for reminder.
     * @param id reminder identifier.
     * @return Due time
     */
    public long generateDateTime(long id){
        NextBase db = new NextBase(mContext);
        long startTime = 0;
        int delay = 0;
        int dayOfMonth = 0;
        long repTime = 0;
        String type = null;
        ArrayList<Integer> weekdays = null;
        Cursor c = db.getReminder(id);
        if (c != null && c.moveToFirst()) {
            startTime = c.getLong(c.getColumnIndex(NextBase.START_TIME));
            delay = c.getInt(c.getColumnIndex(NextBase.DELAY));
            type = c.getString(c.getColumnIndex(NextBase.TYPE));
            String json = c.getString(c.getColumnIndex(NextBase.JSON));
            JsonModel jsonModel = new JsonModel();
            new JsonParser(json).parse(jsonModel);
            JsonRecurrence jsonRecurrence = jsonModel.getRecurrence();

            repTime = jsonRecurrence.getRepeat();
            dayOfMonth = jsonRecurrence.getMonthday();
            weekdays = jsonRecurrence.getWeekdays();
        }
        long dateTime;
        if (startTime == 0) {
            dateTime = 0;
        } else {
            if (type.startsWith(Constants.TYPE_WEEKDAY)){
                dateTime = getNextWeekdayTime(startTime, weekdays, delay);
            } else if (type.startsWith(Constants.TYPE_MONTHDAY)){
                if (type.endsWith("_last")){
                    dateTime = getLastMonthDayTime(startTime, delay);
                } else {
                    dateTime = getNextMonthDayTime(dayOfMonth, startTime, delay);
                }
            } else {
                dateTime = startTime + repTime + (delay * 1000 * 60);
            }
        }
        return dateTime;
    }

    private long getNextWeekdayTime(long startTime, ArrayList<Integer> weekdays, int delay) {
        long date = 0;
        Calendar cc = Calendar.getInstance();
        cc.setTimeInMillis(System.currentTimeMillis());
        long currTime = cc.getTimeInMillis();
        int mDay = cc.get(Calendar.DAY_OF_WEEK);
        cc.setTimeInMillis(startTime);
        long mTime = cc.getTimeInMillis();
        boolean isZeroSupport = false;
        if (mTime > currTime || delay > 0) isZeroSupport = true;
        if (weekdays != null) {
            int charDay;
            int delta = 7;
            for (int i = 0; i < 7; i++){
                if (weekdays.get(i) == Constants.DAY_CHECKED){
                    if (i == 6) charDay = 1;
                    else charDay = i + 2;
                    int mDelta = charDay - mDay;
                    if (mDelta > 0) {
                        if (mDelta < delta) {
                            delta = mDelta;
                        }
                    } else if (mDelta < 0){
                        mDelta = 7 + mDelta;
                        if (mDelta < delta) {
                            delta = mDelta;
                        }
                    } else if (mDelta == 0 && isZeroSupport){
                        delta = mDelta;
                    }
                }
            }

            date = mTime + (delta * day);
        }
        if (delay > 0) date = date + (delay * 1000 * 60);
        return date;
    }

    /**
     * Count due time in milliseconds for time parameters.
     * @param year year.
     * @param month month.
     * @param dayOfMonth day.
     * @param hourOfDay hour.
     * @param minuteOfHour minute.
     * @param seconds seconds.
     * @param inTime timer reminder time.
     * @param repeatCode reminder repeat code.
     * @param remCount number of reminder repeats.
     * @param delay delay for reminder.
     * @return Due time in milliseconds.
     */
    public static long getEventTime(int year, int month, int dayOfMonth, int hourOfDay, int minuteOfHour,
                             int seconds, long inTime, int repeatCode, long remCount, int delay){
        long date;
        if (year == 0 && month == 0 && dayOfMonth == 0 && hourOfDay == 0 && minuteOfHour == 0) {
            date = 0;
        } else {
            Calendar cc = Calendar.getInstance();
            cc.setTimeInMillis(System.currentTimeMillis());
            if (inTime > 0) cc.set(year, month, dayOfMonth, hourOfDay, minuteOfHour, seconds);
            else {
                cc.set(year, month, dayOfMonth, hourOfDay, minuteOfHour, seconds);
                cc.set(Calendar.MILLISECOND, 0);
            }
            long dbTime = cc.getTimeInMillis();
            long newDbTime;
            if (inTime > 0){
                if (repeatCode > 0){
                    if (remCount > 0){
                        newDbTime = dbTime + inTime + (remCount * repeatCode * minute);
                    } else {
                        newDbTime = dbTime + inTime;
                    }
                } else {
                    newDbTime = dbTime + inTime;
                }
            } else {
                if (repeatCode > 0){
                    if (remCount > 0){
                        newDbTime = dbTime + (repeatCode * day * remCount);
                    } else {
                        newDbTime = dbTime;
                    }
                } else {
                    newDbTime = dbTime;
                }
            }
            if (delay > 0) newDbTime = newDbTime + delay * 60 * 1000;
            date = newDbTime;
        }
        return date;
    }

    /**
     * Get remaining title for reminder.
     * @param eventTime due time in milliseconds.
     * @return Remaining String
     */
    public String getRemaining(long eventTime){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        long difference = eventTime - calendar.getTimeInMillis();
        long secondMills = 1000;
        long minuteMills = secondMills * 60;
        long hourMills = minuteMills * 60;
        long dayMills = hourMills * 24;
        long days = (difference / (dayMills));
        long hours = ((difference - (dayMills * days)) / (hourMills));
        long min = (difference - (dayMills * days) - (hourMills * hours)) / (minuteMills);
        long sec = (difference - (dayMills * days) - (hourMills * hours) - (minuteMills * min)) / (secondMills);
        hours = (hours < 0 ? -hours : hours);
        String result;
        if (days > 5){
            result = days + " " + mContext.getString(R.string.remaining_days);
        } else if (days > 0 && days <= 5){
            if (days > 1) {
                result = days + " " + mContext.getString(R.string.remaining_days) + "," + " " + hours
                        + " " + (hours > 1 ? mContext.getString(R.string.remaining_hours) :
                        mContext.getString(R.string.remaining_hour)) + " " +
                        mContext.getString(R.string.remaining_and) + " " + min + " " +
                        (min > 1 ? mContext.getString(R.string.remaining_minutes) : mContext.getString(R.string.remaining_minute));
            } else {
                result = days + " " + mContext.getString(R.string.remaining_day) +
                        "," + " " + hours + " " +(hours > 1 ? mContext.getString(R.string.remaining_hours) :
                        mContext.getString(R.string.remaining_hour)) + " " +
                        mContext.getString(R.string.remaining_and) + " " + min + " " +
                        (min > 1 ? mContext.getString(R.string.remaining_minutes) : mContext.getString(R.string.remaining_minute));
            }
        } else if (days == 0 && hours > 0){
            if (hours > 1) {
                result = hours + " " + mContext.getString(R.string.remaining_hours) + " " +
                        mContext.getString(R.string.remaining_and) + " " + min + " " +
                        (min > 1 ? mContext.getString(R.string.remaining_minutes) : mContext.getString(R.string.remaining_minute));
            } else {
                result = hours + " " + mContext.getString(R.string.remaining_hour) + " " +
                        mContext.getString(R.string.remaining_and) + " " + min + " " +
                        (min > 1 ? mContext.getString(R.string.remaining_minutes) : mContext.getString(R.string.remaining_minute));
            }
        } else {
            if (min >= 1) {
                if (min == 1){
                    result = min + " " + mContext.getString(R.string.remaining_minute) + " " +
                            mContext.getString(R.string.remaining_and) +
                            " " + sec + " " + mContext.getString(R.string.remaining_seconds);
                } else {
                    result = min + " " + mContext.getString(R.string.remaining_minutes);
                }
            } else {
                result = mContext.getString(R.string.remaining_less_minute);
            }
        }
        return result;
    }

    /**
     * Check if time is actual.
     * @param due time in milliseconds.
     * @return boolean
     */
    public boolean isNext(long due) {
        boolean nextDate = false;
        if (due == 0) {
            nextDate = true;
        } else {
            Calendar cc = Calendar.getInstance();
            cc.setTimeInMillis(System.currentTimeMillis());
            long currentTome = cc.getTimeInMillis();
            if (due > currentTome) {
                nextDate = true;
            }
        }
        return nextDate;
    }

    /**
     * Count next due time for weekday reminder type.
     * @param hourOfDay hour.
     * @param minuteOfHour minute.
     * @param weekdays weekday string.
     * @param delay delay for reminder.
     * @return Due time in milliseconds.
     */
    public static long getNextWeekdayTime(int hourOfDay, int minuteOfHour, ArrayList<Integer> weekdays, int delay){
        long date;
        Calendar cc = Calendar.getInstance();
        cc.setTimeInMillis(System.currentTimeMillis());
        long currTime = cc.getTimeInMillis();
        int mDay = cc.get(Calendar.DAY_OF_WEEK);
        cc.set(Calendar.HOUR_OF_DAY, hourOfDay);
        cc.set(Calendar.MINUTE, minuteOfHour);
        cc.set(Calendar.SECOND, 0);
        cc.set(Calendar.MILLISECOND, 0);
        long mTime = cc.getTimeInMillis();
        boolean isZeroSupport = false;
        if (mTime > currTime || delay > 0) isZeroSupport = true;
        long newDbTime = 0;
        if (weekdays != null) {
            int charDay;
            int delta = 7;
            for (int i = 0; i < 7; i++){
                if (weekdays.get(i) == Constants.DAY_CHECKED){
                    if (i == 6) charDay = 1;
                    else charDay = i + 2;
                    int mDelta = charDay - mDay;
                    if (mDelta > 0) {
                        if (mDelta < delta) {
                            delta = mDelta;
                        }
                    } else if (mDelta < 0){
                        mDelta = 7 + mDelta;
                        if (mDelta < delta) {
                            delta = mDelta;
                        }
                    } else if (mDelta == 0 && isZeroSupport){
                        delta = mDelta;
                    }
                }
            }

            newDbTime = mTime + (delta * day);
        }
        if (delay > 0) newDbTime = newDbTime + delay * 1000 * 60;
        date = newDbTime;
        return date;
    }

    public boolean isCurrent(long startTime) {
        boolean res = false;
        Calendar cc = Calendar.getInstance();
        cc.setTimeInMillis(System.currentTimeMillis());
        long currentTome = cc.getTimeInMillis();
        if (startTime < currentTome) {
            res = true;
        }
        return res;
    }

    public boolean isCurrent(int year, int month, int dayOfMonth, int hourOfDay, int minuteOfHour, int seconds) {
        boolean res = false;
        Calendar cc = Calendar.getInstance();
        cc.setTimeInMillis(System.currentTimeMillis());
        long currentTome = cc.getTimeInMillis();
        Calendar db = Calendar.getInstance();
        db.set(Calendar.DATE, dayOfMonth);
        db.set(Calendar.MONTH, month);
        db.set(Calendar.YEAR, year);
        db.set(Calendar.HOUR_OF_DAY, hourOfDay);
        db.set(Calendar.MINUTE, minuteOfHour);
        db.set(Calendar.SECOND, seconds);
        db.set(Calendar.MILLISECOND, 0);
        long dbTime = db.getTimeInMillis();
        if (dbTime < currentTome) {
            res = true;
        }
        return res;
    }

    /**
     * Check if number is between two values.
     * @param value target.
     * @param min min number.
     * @param max max number.
     * @return boolean
     */
    private boolean isBetween(long value, long min, long max){
        return((value > min) && (value < max));
    }

    /**
     * Check if current days of week is selected for weekday reminder.
     * @param repeat weekdays string.
     * @return boolean
     */
    public static boolean isDay(ArrayList<Integer> repeat){
        boolean res = false;
        Calendar calendar = Calendar.getInstance();
        int weekDay = calendar.get(Calendar.DAY_OF_WEEK);
        if (weekDay == Calendar.MONDAY) {
            res = repeat.get(0) == Constants.DAY_CHECKED;
        }
        if (weekDay == Calendar.TUESDAY){
            res = repeat.get(1) == Constants.DAY_CHECKED;
        }
        if (weekDay == Calendar.WEDNESDAY){
            res = repeat.get(2) == Constants.DAY_CHECKED;
        }
        if (weekDay == Calendar.THURSDAY){
            res = repeat.get(3) == Constants.DAY_CHECKED;
        }
        if (weekDay == Calendar.FRIDAY){
            res = repeat.get(4) == Constants.DAY_CHECKED;
        }
        if (weekDay == Calendar.SATURDAY){
            res = repeat.get(5) == Constants.DAY_CHECKED;
        }
        if (weekDay == Calendar.SUNDAY){
            res = repeat.get(6) == Constants.DAY_CHECKED;
        }
        return res;
    }

    /**
     * Get next due time for MonthDay reminder type.
     * @param hourOfDay hour.
     * @param minuteOfHour minute.
     * @param dayOfMonth day.
     * @param delay delay for reminder.
     * @return Due time in milliseconds.
     */
    public static long getNextMonthDayTime(int hourOfDay, int minuteOfHour, int dayOfMonth, int delay){
        if (dayOfMonth == 0){
            return getLastMonthDayTime(hourOfDay, minuteOfHour, delay);
        }
        Calendar cc = Calendar.getInstance();
        cc.setTimeInMillis(System.currentTimeMillis());
        long currTime = cc.getTimeInMillis();
        cc.set(Calendar.HOUR_OF_DAY, hourOfDay);
        cc.set(Calendar.MINUTE, minuteOfHour);
        cc.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        cc.set(Calendar.SECOND, 0);
        cc.set(Calendar.MILLISECOND, 0);
        long mTime = cc.getTimeInMillis();
        boolean isZeroSupport = false;
        if (mTime > currTime) isZeroSupport = true;
        long newDbTime;
        if (isZeroSupport){
            newDbTime = mTime + (delay * 1000 * 60);
        } else {
            cc.set(Calendar.MONTH, cc.get(Calendar.MONTH) + 1);
            newDbTime = cc.getTimeInMillis() + (delay * 1000 * 60);
        }
        return newDbTime;
    }

    /**
     * Get next due time for MonthDay reminder type starts from selected date and time.
     * @param dayOfMonth day.
     * @param fromTime start time.
     * @param multi step for next due.
     * @return Due time in milliseconds.
     */
    public static long getNextMonthDayTime(int dayOfMonth, long fromTime, int multi){
        if (dayOfMonth == 0){
            return getLastMonthDayTime(fromTime, multi);
        }
        Calendar cc = Calendar.getInstance();
        cc.setTimeInMillis(fromTime);
        cc.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        cc.set(Calendar.MONTH, cc.get(Calendar.MONTH) + multi);
        cc.set(Calendar.SECOND, 0);
        cc.set(Calendar.MILLISECOND, 0);
        return cc.getTimeInMillis();
    }

    /**
     * Get next due time for next last day of month.
     * @param fromTime start time.
     * @param multi step for calculating.
     * @return Due time in milliseconds.
     */
    public static long getLastMonthDayTime(long fromTime, int multi) {
        Calendar cc = Calendar.getInstance();
        cc.setTimeInMillis(fromTime);
        int month = cc.get(Calendar.MONTH);
        cc.set(Calendar.DAY_OF_MONTH, 15);
        cc.set(Calendar.MONTH, month + multi);
        int lastDay = cc.getActualMaximum(Calendar.DAY_OF_MONTH);
        cc.set(Calendar.DAY_OF_MONTH, lastDay);
        cc.set(Calendar.SECOND, 0);
        cc.set(Calendar.MILLISECOND, 0);
        return cc.getTimeInMillis();
    }

    /**
     * Get next due time for next last day of month.
     * @param hourOfDay hour.
     * @param minuteOfHour minute.
     * @param delay delay for reminder.
     * @return Due time in milliseconds.
     */
    public static long getLastMonthDayTime(int hourOfDay, int minuteOfHour, int delay){
        Calendar cc = Calendar.getInstance();
        cc.setTimeInMillis(System.currentTimeMillis());
        int lastDay = cc.getActualMaximum(Calendar.DAY_OF_MONTH);
        long currTime = cc.getTimeInMillis();
        cc.set(Calendar.HOUR_OF_DAY, hourOfDay);
        cc.set(Calendar.MINUTE, minuteOfHour);
        cc.set(Calendar.DAY_OF_MONTH, lastDay);
        cc.set(Calendar.SECOND, 0);
        cc.set(Calendar.MILLISECOND, 0);
        long mTime = cc.getTimeInMillis();
        boolean isZeroSupport = false;
        if (mTime > currTime) isZeroSupport = true;
        long newDbTime;
        if (isZeroSupport){
            newDbTime = mTime + (delay * 1000 * 60);
        } else {
            cc.set(Calendar.MONTH, cc.get(Calendar.MONTH) + 1);
            newDbTime = cc.getTimeInMillis() + (delay * 1000 * 60);
        }
        return newDbTime;
    }

    /**
     * Check if current day is same as is in reminder.
     * @param dayOfMonth day.
     * @return boolean
     */
    public static boolean isDay(int dayOfMonth){
        if (dayOfMonth == 0){
            return isLastDay();
        }
        Calendar cc = Calendar.getInstance();
        cc.setTimeInMillis(System.currentTimeMillis());
        return cc.get(Calendar.DAY_OF_MONTH) == dayOfMonth;
    }

    /**
     * Check if current day is the last day in this month.
     * @return boolean
     */
    public static boolean isLastDay(){
        Calendar cc = Calendar.getInstance();
        cc.setTimeInMillis(System.currentTimeMillis());
        return cc.get(Calendar.DAY_OF_MONTH) == cc.getActualMaximum(Calendar.DAY_OF_MONTH);
    }
}
