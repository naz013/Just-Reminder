package com.cray.software.justreminder.helpers;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.utils.TimeUtil;
import com.cray.software.justreminder.utils.ViewUtils;

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
     * Get drawable indicator based on time parameters.
     * @param weekdays reminder weekdays.
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
     * @return Drawable
     */
    public Drawable getDifference(String weekdays, int year, int month, int dayOfMonth, int hourOfDay,
                                  int minuteOfHour, int seconds, long inTime, int repeatCode,
                                  long remCount, int delay){
        Drawable color;
        if (year == 0 && month == 0 && dayOfMonth == 0 && hourOfDay == 0 && minuteOfHour == 0) {
            color = getDrawable(R.color.material_divider);
        } else {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(System.currentTimeMillis());
            long currTime = cal.getTimeInMillis();

            long newDbTime;
            if (weekdays != null && !weekdays.matches("")){
                newDbTime = getNextWeekdayTime(hourOfDay, minuteOfHour, weekdays, delay);
            } else {
                newDbTime = getEventTime(year, month, dayOfMonth, hourOfDay, minuteOfHour, seconds,
                        inTime, repeatCode, remCount, delay);
            }
            long diff = newDbTime - currTime;
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

    /**
     * Generate new due time for reminder.
     * @param id reminder identifier.
     * @return Due time
     */
    public long generateDateTime(long id){
        DataBase db = new DataBase(mContext);
        int hourOfDay = 0;
        int minuteOfHour = 0;
        int seconds = 0;
        int dayOfMonth = 0;
        int monthOfYear = 0;
        int year = 0;
        int repCode = 0;
        int delay = 0;
        long repTime = 0;
        long repCount = 0;
        String type = null;
        String weekdays = null;
        Cursor c = db.getReminder(id);
        if (c != null && c.moveToFirst()) {
            repCode = c.getInt(c.getColumnIndex(Constants.COLUMN_REPEAT));
            repCount = c.getLong(c.getColumnIndex(Constants.COLUMN_REMINDERS_COUNT));
            repTime = c.getLong(c.getColumnIndex(Constants.COLUMN_REMIND_TIME));
            dayOfMonth = c.getInt(c.getColumnIndex(Constants.COLUMN_DAY));
            monthOfYear = c.getInt(c.getColumnIndex(Constants.COLUMN_MONTH));
            year = c.getInt(c.getColumnIndex(Constants.COLUMN_YEAR));
            hourOfDay = c.getInt(c.getColumnIndex(Constants.COLUMN_HOUR));
            minuteOfHour = c.getInt(c.getColumnIndex(Constants.COLUMN_MINUTE));
            seconds = c.getInt(c.getColumnIndex(Constants.COLUMN_SECONDS));
            delay = c.getInt(c.getColumnIndex(Constants.COLUMN_DELAY));
            type = c.getString(c.getColumnIndex(Constants.COLUMN_TYPE));
            weekdays = c.getString(c.getColumnIndex(Constants.COLUMN_WEEKDAYS));
        }
        long dateTime;
        if (year == 0 && monthOfYear == 0 && dayOfMonth == 0 && hourOfDay == 0 && minuteOfHour == 0) {
            dateTime = 0;
        } else {
            if (type.startsWith(Constants.TYPE_WEEKDAY)){
                dateTime = getNextWeekdayTime(hourOfDay, minuteOfHour, weekdays, delay);
            } else if (type.startsWith(Constants.TYPE_MONTHDAY)){
                if (type.endsWith("_last")){
                    dateTime = getLastMonthDayTime(hourOfDay, minuteOfHour, delay);
                } else {
                    dateTime = getNextMonthDayTime(hourOfDay, minuteOfHour, dayOfMonth, delay);
                }
            } else {
                dateTime = getEventTime(year, monthOfYear, dayOfMonth, hourOfDay, minuteOfHour,
                        seconds, repTime, repCode, repCount, delay);
            }
        }
        return dateTime;
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
     * @return
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
     * Check if time is actual.
     * @param year year.
     * @param month month.
     * @param dayOfMonth day.
     * @param hourOfDay hour.
     * @param minuteOfHour minute.
     * @param seconds seconds.
     * @param inTime timer reminder time.
     * @param repeatCode reminder repeat code.
     * @param remCount number of reminder repeats.
     * @return
     */
    public boolean isNext(int year, int month, int dayOfMonth, int hourOfDay, int minuteOfHour, int seconds,
                          long inTime, int repeatCode, long remCount) {
        boolean nextDate = false;
        if (year == 0 && month == 0 && dayOfMonth == 0 && hourOfDay == 0 && minuteOfHour == 0) {
            nextDate = true;
        } else {
            Calendar cc = Calendar.getInstance();
            cc.setTimeInMillis(System.currentTimeMillis());
            long currentTome = cc.getTimeInMillis();
            long newDbTime = getEventTime(year, month, dayOfMonth, hourOfDay, minuteOfHour, seconds,
                    inTime, repeatCode, remCount, 0);
            if (newDbTime > currentTome) {
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
    public static long getNextWeekdayTime(int hourOfDay, int minuteOfHour, String weekdays, int delay){
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
                if (Character.toString(weekdays.charAt(i)).matches(Constants.DAY_CHECKED)){
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
     * @return
     */
    private boolean isBetween(long value, long min, long max){
        return((value > min) && (value < max));
    }

    /**
     * Check if current days of week is selected for weekday reminder.
     * @param repeat weekdays string.
     * @return
     */
    public static boolean isDay(String repeat){
        boolean res = false;
        Calendar calendar = Calendar.getInstance();
        int weekDay = calendar.get(Calendar.DAY_OF_WEEK);
        if (weekDay == Calendar.MONDAY) {
            res = Character.toString(repeat.charAt(0)).matches(Constants.DAY_CHECKED);
        }
        if (weekDay == Calendar.TUESDAY){
            res = Character.toString(repeat.charAt(1)).matches(Constants.DAY_CHECKED);
        }
        if (weekDay == Calendar.WEDNESDAY){
            res = Character.toString(repeat.charAt(2)).matches(Constants.DAY_CHECKED);
        }
        if (weekDay == Calendar.THURSDAY){
            res = Character.toString(repeat.charAt(3)).matches(Constants.DAY_CHECKED);
        }
        if (weekDay == Calendar.FRIDAY){
            res = Character.toString(repeat.charAt(4)).matches(Constants.DAY_CHECKED);
        }
        if (weekDay == Calendar.SATURDAY){
            res = Character.toString(repeat.charAt(5)).matches(Constants.DAY_CHECKED);
        }
        if (weekDay == Calendar.SUNDAY){
            res = Character.toString(repeat.charAt(6)).matches(Constants.DAY_CHECKED);
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
     * @return
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
     * @return
     */
    public static boolean isLastDay(){
        Calendar cc = Calendar.getInstance();
        cc.setTimeInMillis(System.currentTimeMillis());
        return cc.get(Calendar.DAY_OF_MONTH) == cc.getActualMaximum(Calendar.DAY_OF_MONTH);
    }
}
