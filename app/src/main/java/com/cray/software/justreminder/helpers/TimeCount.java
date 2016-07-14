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

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.utils.TimeUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

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
            time = TimeUtil.getTime(mTime, SharedPrefs.getInstance(mContext).getBoolean(Prefs.IS_24_TIME_FORMAT));
        }
        return new String[]{date, time};
    }

    /**
     * Generate start date time for new event.
     * @param type event type.
     * @param dayOfMonth day of month.
     * @param month month.
     * @param year year.
     * @param hour hour.
     * @param minute minute.
     * @param seconds seconds.
     * @param weekdays list of selected days.
     * @param after timer countdown time.
     * @return Date and time milliseconds.
     */
    public long generateStartEvent(String type, int dayOfMonth, int month, int year, int hour,
                                  int minute, int seconds, ArrayList<Integer> weekdays, long after) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        if (type.startsWith(Constants.TYPE_WEEKDAY)){
            return getNextWeekdayTime(calendar.getTimeInMillis(), weekdays, 0);
        } else if (type.startsWith(Constants.TYPE_MONTHDAY)){
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
                                      ArrayList<Integer> weekdays, long count, long delay){
        long dateTime;
        if (startTime == 0) {
            dateTime = 0;
        } else {
            if (type.startsWith(Constants.TYPE_WEEKDAY)){
                dateTime = getNextWeekdayTime(startTime, weekdays, delay);
            } else if (type.startsWith(Constants.TYPE_MONTHDAY)){
                dateTime = getNextMonthDayTime(dayOfMonth, startTime);
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
        String lang = Locale.getDefault().toString().toLowerCase();
        if (difference > DAY){
            if (lang.startsWith("uk") || lang.startsWith("ru")) {
                long last = days;
                while (last > 10) {
                    last -= 10;
                }
                if (last == 1 && days != 11) {
                    result.append(String.format(mContext.getString(R.string.x_day), days));
                } else if (last < 5 && (days < 12 || days > 14)) {
                    result.append(String.format(mContext.getString(R.string.x_dayzz), days));
                } else {
                    result.append(String.format(mContext.getString(R.string.x_days), days));
                }
            } else {
                if (days < 2)
                    result.append(String.format(mContext.getString(R.string.x_day), days));
                else
                    result.append(String.format(mContext.getString(R.string.x_days), days));
            }
        } else if (difference > HOUR){
            hours = (days * 24) + hours;
            if (lang.startsWith("uk") || lang.startsWith("ru")) {
                long last = hours;
                while (last > 10) {
                    last -= 10;
                }
                if (last == 1 && hours != 11) {
                    result.append(String.format(mContext.getString(R.string.x_hour), hours));
                } else if (last < 5 && (hours < 12 || hours > 14)) {
                    result.append(String.format(mContext.getString(R.string.x_hourzz), hours));
                } else {
                    result.append(String.format(mContext.getString(R.string.x_hours), hours));
                }
            } else {
                if (hours < 2)
                    result.append(String.format(mContext.getString(R.string.x_hour), hours));
                else
                    result.append(String.format(mContext.getString(R.string.x_hours), hours));
            }
        } else if (difference > MINUTE){
            minutes = (hours * 60) + minutes;
            if (lang.startsWith("uk") || lang.startsWith("ru")) {
                long last = minutes;
                while (last > 10) {
                    last -= 10;
                }
                if (last == 1 && minutes != 11) {
                    result.append(String.format(mContext.getString(R.string.x_minute), minutes));
                } else if (last < 5 && (minutes < 12 || minutes > 14)) {
                    result.append(String.format(mContext.getString(R.string.x_minutezz), minutes));
                } else {
                    result.append(String.format(mContext.getString(R.string.x_minutes), minutes));
                }
            } else {
                if (hours < 2)
                    result.append(String.format(mContext.getString(R.string.x_minute), minutes));
                else
                    result.append(String.format(mContext.getString(R.string.x_minutes), minutes));
            }
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
    public static long getNextWeekdayTime(long startTime, ArrayList<Integer> weekdays, long delay){
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
                    if (cc.getTimeInMillis() > System.currentTimeMillis()) {
                        break;
                    }
                }
                cc.setTimeInMillis(cc.getTimeInMillis() + DAY);
            }
            return cc.getTimeInMillis();
        }
    }

    /**
     * Check if date and time is actual.
     * @param startTime date and time in mills.
     * @return Boolean
     */
    public boolean isCurrent(long startTime) {
        Calendar cc = Calendar.getInstance();
        cc.setTimeInMillis(System.currentTimeMillis());
        long currentTome = cc.getTimeInMillis();
        return startTime < currentTome;
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

        if (cc.getTimeInMillis() > System.currentTimeMillis())
            return cc.getTimeInMillis();

        cc.set(Calendar.DAY_OF_MONTH, dayOfMonth + 1);
        while (cc.get(Calendar.DAY_OF_MONTH) != dayOfMonth)
            cc.setTimeInMillis(cc.getTimeInMillis() + DAY);

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
        while (fromTime < System.currentTimeMillis()) {
            int lastDay = cc.getActualMaximum(Calendar.DAY_OF_MONTH);
            cc.set(Calendar.DAY_OF_MONTH, lastDay);
            fromTime = cc.getTimeInMillis();
            if (fromTime > System.currentTimeMillis())
                break;
            cc.set(Calendar.DAY_OF_MONTH, 15);
            cc.setTimeInMillis(cc.getTimeInMillis() + (30 * DAY));
        }
        cc.set(Calendar.SECOND, 0);
        cc.set(Calendar.MILLISECOND, 0);
        return cc.getTimeInMillis();
    }

    public static boolean isWeeekDay(ArrayList<Integer> days) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        return days.get(day) == 1;
    }

    public static boolean isDayOfMonth(int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        return dayOfMonth == day;
    }
}
