package com.cray.software.justreminder.helpers;

import android.content.Context;
import android.util.Log;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.utils.TimeUtil;

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
    public final static long SECOND = 1000;
    public final static long MINUTE = 60 * SECOND;
    public final static long HOUR = MINUTE * 60;
    public final static long HALF_DAY = HOUR * 12;
    public final static long DAY = HALF_DAY * 2;

    private Context mContext;

    public TimeCount(Context context){
        this.mContext = context;
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

    public long generateStartEvent(String type, int dayOfMonth, int month, int year, int hour,
                                  int minute, int seconds, ArrayList<Integer> weekdays, long after) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        if (type.startsWith(Constants.TYPE_WEEKDAY)){
            return getNextWeekdayTime(calendar.getTimeInMillis(), weekdays, 0);
        } else if (type.startsWith(Constants.TYPE_MONTHDAY)){
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            if (type.endsWith("_last"))
                return getLastMonthDayTime(calendar.getTimeInMillis());
            else
                return getNextMonthDayTime(dayOfMonth, calendar.getTimeInMillis());
        } else {
            calendar.set(year, month, dayOfMonth, hour, minute, seconds);
            if (type.matches(Constants.TYPE_TIME))
                return System.currentTimeMillis() + after;

            if (type.matches(Constants.TYPE_SHOPPING_LIST)) {
                if (dayOfMonth == 0) return 0;
            }

            return calendar.getTimeInMillis();
        }
    }

    /**
     * Generate new due time for reminder.
     * @param type Reminder type.
     * @param delay Snooze for reminder in minutes.
     * @return Next event time
     */
    public long generateDateTime(String type, int dayOfMonth, long startTime, long repeat,
                                      ArrayList<Integer> weekdays, long count, int delay){
        long dateTime;
        if (startTime == 0) {
            dateTime = 0;
        } else {
            if (type.startsWith(Constants.TYPE_WEEKDAY)){
                dateTime = getNextWeekdayTime(startTime, weekdays, delay);
            } else if (type.startsWith(Constants.TYPE_MONTHDAY)){
                if (type.endsWith("_last")){
                    dateTime = getLastMonthDayTime(startTime);
                } else {
                    dateTime = getNextMonthDayTime(dayOfMonth, startTime);
                }
            } else {
                dateTime = startTime + (repeat * count) + (delay * MINUTE);
            }
        }
        return dateTime;
    }

    /**
     * Get remaining title for reminder.
     * @param eventTime due time in milliseconds.
     * @return Remaining String
     */
    public String getRemaining(long eventTime){
        long difference = eventTime - System.currentTimeMillis();
        long days = (difference / (DAY));
        long hours = ((difference - (DAY * days)) / (HOUR));
        long minutes = (difference - (DAY * days) - (HOUR * hours)) / (MINUTE);
        hours = (hours < 0 ? -hours : hours);
        StringBuilder result = new StringBuilder();
        if (difference > DAY){
            result.append(String.format(mContext.getString(R.string.x_days), days));
            /*if (hours > 1) {
                result.append(mContext.getString(R.string.and));
                result.append(String.format(mContext.getString(R.string.x_hours), hours));
            }*/
        } else if (difference > HOUR){
            result.append(String.format(mContext.getString(R.string.x_hours), (days * 24) + hours));
            /*if (minutes > 1) {
                result.append(mContext.getString(R.string.and));
                result.append(String.format(mContext.getString(R.string.x_minutes), minutes));
            }*/
        } else if (difference > MINUTE){
            result.append(String.format(mContext.getString(R.string.x_minutes), (hours * 60) + minutes));
        } else if (difference > 0){
            result.append(mContext.getString(R.string.less_than_minute));
        } else {
            result.append(mContext.getString(R.string.overdue));
        }
        return result.toString();
    }

    /**
     * Check if time is actual.
     * @param due time in milliseconds.
     * @return boolean
     */
    public boolean isNext(long due) {
        if (due == 0) return true;
        else {
            Calendar cc = Calendar.getInstance();
            cc.setTimeInMillis(System.currentTimeMillis());
            long currentTome = cc.getTimeInMillis();
            return due > currentTome;
        }
    }

    /**
     * Count next due time for weekday reminder type.
     * @param startTime next event time start point.
     * @param weekdays weekday string.
     * @param delay delay for reminder.
     * @return Due time in milliseconds.
     */
    public static long getNextWeekdayTime(long startTime, ArrayList<Integer> weekdays, int delay){
        Calendar cc = Calendar.getInstance();
        cc.setTimeInMillis(startTime);
        cc.set(Calendar.SECOND, 0);
        cc.set(Calendar.MILLISECOND, 0);
        if (delay > 0) {
            return startTime + (delay * MINUTE);
        } else {
            while (true) {
                int mDay = cc.get(Calendar.DAY_OF_WEEK);
                if (weekdays.get(mDay - 1) == 1) {
                    Log.d(Constants.LOG_TAG, "Day " + mDay);
                    Log.d(Constants.LOG_TAG, "Date " + TimeUtil.getFullDateTime(cc.getTimeInMillis(), true));
                    if (cc.getTimeInMillis() > System.currentTimeMillis()) {
                        break;
                    }
                }
                cc.setTimeInMillis(cc.getTimeInMillis() + DAY);
            }

            return cc.getTimeInMillis();
        }
    }

    public boolean isCurrent(long startTime) {
        Calendar cc = Calendar.getInstance();
        cc.setTimeInMillis(System.currentTimeMillis());
        long currentTome = cc.getTimeInMillis();
        return startTime < currentTome;
    }

    /**
     * Check if current days of week is selected for weekday reminder.
     * @param repeat weekdays string.
     * @return boolean
     */
    public static boolean isDay(ArrayList<Integer> repeat){
        boolean res = false;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        int weekDay = calendar.get(Calendar.DAY_OF_WEEK);
        if (weekDay == Calendar.SUNDAY){
            res = repeat.get(0) == Constants.DAY_CHECKED;
        }
        if (weekDay == Calendar.MONDAY) {
            res = repeat.get(1) == Constants.DAY_CHECKED;
        }
        if (weekDay == Calendar.TUESDAY){
            res = repeat.get(2) == Constants.DAY_CHECKED;
        }
        if (weekDay == Calendar.WEDNESDAY){
            res = repeat.get(3) == Constants.DAY_CHECKED;
        }
        if (weekDay == Calendar.THURSDAY){
            res = repeat.get(4) == Constants.DAY_CHECKED;
        }
        if (weekDay == Calendar.FRIDAY){
            res = repeat.get(5) == Constants.DAY_CHECKED;
        }
        if (weekDay == Calendar.SATURDAY){
            res = repeat.get(6) == Constants.DAY_CHECKED;
        }
        return res;
    }

    /**
     * Get next due time for MonthDay reminder type starts from selected date and time.
     * @param dayOfMonth day.
     * @param fromTime start time.
     * @return Due time in milliseconds.
     */
    public static long getNextMonthDayTime(int dayOfMonth, long fromTime){
        if (dayOfMonth == 0){
            return getLastMonthDayTime(fromTime);
        }
        Calendar cc = Calendar.getInstance();
        cc.setTimeInMillis(fromTime);
        cc.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        if (fromTime < System.currentTimeMillis())
            cc.setTimeInMillis(cc.getTimeInMillis() + (30 * DAY));

        cc.set(Calendar.SECOND, 0);
        cc.set(Calendar.MILLISECOND, 0);
        return cc.getTimeInMillis();
    }

    /**
     * Get next due time for next last day of month.
     * @param fromTime start time.
     * @return Due time in milliseconds.
     */
    public static long getLastMonthDayTime(long fromTime) {
        Calendar cc = Calendar.getInstance();
        cc.setTimeInMillis(fromTime);
        if (fromTime < System.currentTimeMillis()) {
            cc.set(Calendar.DAY_OF_MONTH, 15);
            cc.setTimeInMillis(cc.getTimeInMillis() + (30 * DAY));
            int lastDay = cc.getActualMaximum(Calendar.DAY_OF_MONTH);
            cc.set(Calendar.DAY_OF_MONTH, lastDay);
        } else {
            int lastDay = cc.getActualMaximum(Calendar.DAY_OF_MONTH);
            cc.set(Calendar.DAY_OF_MONTH, lastDay);
        }
        cc.set(Calendar.SECOND, 0);
        cc.set(Calendar.MILLISECOND, 0);
        return cc.getTimeInMillis();
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
