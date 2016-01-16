package com.cray.software.justreminder.helpers;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.Prefs;
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
            if (isBetween(diff, 0, MINUTE * 5)) {
                color = getDrawable(R.drawable.drawable_red);
            } else if (isBetween(diff, MINUTE * 5, HOUR)) {
                color = getDrawable(R.drawable.drawable_yellow);
            } else if (isBetween(diff, HOUR, HALF_DAY)) {
                color = getDrawable(R.drawable.drawable_green);
            } else if (isBetween(diff, HALF_DAY, DAY)) {
                color = getDrawable(R.drawable.drawable_blue);
            } else if ((diff > DAY)) {
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

    public long generateStartEvent(String type, int dayOfMonth, int month, int year, int hour,
                                  int minute, int seconds, ArrayList<Integer> weekdays, long after) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, dayOfMonth, hour, minute, seconds);
        if (type.startsWith(Constants.TYPE_WEEKDAY)){
            return getNextWeekdayTime(calendar.getTimeInMillis(), weekdays, 0);
        } else if (type.startsWith(Constants.TYPE_MONTHDAY)){
            if (type.endsWith("_last"))
                return getLastMonthDayTime(calendar.getTimeInMillis());
            else
                return getNextMonthDayTime(dayOfMonth, calendar.getTimeInMillis());
        } else {
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
        if (startTime > System.currentTimeMillis()) return startTime;
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

    public long getNextWeekdayTime(long startTime, ArrayList<Integer> weekdays, int delay) {
        Calendar cc = Calendar.getInstance();
        cc.setTimeInMillis(startTime);
        int hourOfDay = cc.get(Calendar.HOUR_OF_DAY);
        int minuteOfHour = cc.get(Calendar.MINUTE);
        return getNextWeekdayTime(hourOfDay, minuteOfHour, weekdays, delay);
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
        long min = (difference - (DAY * days) - (HOUR * hours)) / (MINUTE);
        long sec = (difference - (DAY * days) - (HOUR * hours) - (MINUTE * min)) / (SECOND);
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
     * @param hourOfDay hour.
     * @param minuteOfHour minute.
     * @param weekdays weekday string.
     * @param delay delay for reminder.
     * @return Due time in milliseconds.
     */
    public static long getNextWeekdayTime(int hourOfDay, int minuteOfHour,
                                          ArrayList<Integer> weekdays, int delay){
        long currTime = System.currentTimeMillis();
        Calendar cc = Calendar.getInstance();
        cc.setTimeInMillis(currTime);
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

            newDbTime = mTime + (delta * DAY);
        }
        return newDbTime + (delay * MINUTE);
    }

    public boolean isCurrent(long startTime) {
        Calendar cc = Calendar.getInstance();
        cc.setTimeInMillis(System.currentTimeMillis());
        long currentTome = cc.getTimeInMillis();
        return startTime < currentTome;
    }

    /**
     * Check if number is between two values.
     * @param value target.
     * @param min min number.
     * @param max max number.
     * @return boolean
     */
    private boolean isBetween(long value, long min, long max){
        return ((value > min) && (value < max));
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
        cc.set(Calendar.HOUR_OF_DAY, hourOfDay);
        cc.set(Calendar.MINUTE, minuteOfHour);
        long mTime = getNextMonthDayTime(dayOfMonth, cc.getTimeInMillis());
        return mTime + (delay * MINUTE);
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
     * Get next due time for next last day of month.
     * @param hourOfDay hour.
     * @param minuteOfHour minute.
     * @param delay delay for reminder.
     * @return Due time in milliseconds.
     */
    public static long getLastMonthDayTime(int hourOfDay, int minuteOfHour, int delay){
        Calendar cc = Calendar.getInstance();
        cc.setTimeInMillis(System.currentTimeMillis());
        cc.set(Calendar.HOUR_OF_DAY, hourOfDay);
        cc.set(Calendar.MINUTE, minuteOfHour);
        long mTime = getLastMonthDayTime(cc.getTimeInMillis());
        return mTime + (delay * MINUTE);
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
