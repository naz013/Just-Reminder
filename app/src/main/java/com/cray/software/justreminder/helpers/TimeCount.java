package com.cray.software.justreminder.helpers;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Build;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.interfaces.Constants;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class TimeCount {

    public final static long minute = 60 * 1000;
    public final static long hour = minute  * 60;
    public final static long halfDay = hour * 12;
    public final static long day = halfDay * 2;
    Context mContext;

    public TimeCount(Context context){
        this.mContext = context;
    }

    private Drawable getColor(int color){
        Drawable res;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            res = mContext.getResources().getDrawable(color, null);
        } else {
            res = mContext.getResources().getDrawable(color);
        }
        return res;
    }

    public Drawable getDifference(long time){
        Drawable color;
        if (time == 0) {
            color = getColor(R.color.colorSemiTrGrayDark);
        } else {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(System.currentTimeMillis());
            long currTime = cal.getTimeInMillis();

            long diff = time - currTime;
            if (isBetween(diff, 0, minute * 5)) {
                color = getColor(R.drawable.drawable_red);
            } else if (isBetween(diff, minute * 5, hour)) {
                color = getColor(R.drawable.drawable_yellow);
            } else if (isBetween(diff, hour, halfDay)) {
                color = getColor(R.drawable.drawable_green);
            } else if (isBetween(diff, halfDay, day)) {
                color = getColor(R.drawable.drawable_blue);
            } else if ((diff > day)) {
                color = getColor(R.drawable.drawable_indigo);
            } else {
                color = getColor(R.drawable.drawable_grey);
            }
        }
        return color;
    }

    public Drawable getDifference(String weekdays, int year, int month, int dayOfMonth, int hourOfDay,
                                  int minuteOfHour, int seconds, long inTime, int repeatCode,
                                  int remCount, int delay){
        Drawable color;
        if (year == 0 && month == 0 && dayOfMonth == 0 && hourOfDay == 0 && minuteOfHour == 0) {
            color = getColor(R.color.colorSemiTrGrayDark);
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
                color = getColor(R.drawable.drawable_red);
            } else if (isBetween(diff, minute * 5, hour)) {
                color = getColor(R.drawable.drawable_yellow);
            } else if (isBetween(diff, hour, halfDay)) {
                color = getColor(R.drawable.drawable_green);
            } else if (isBetween(diff, halfDay, day)) {
                color = getColor(R.drawable.drawable_blue);
            } else if ((diff > day)) {
                color = getColor(R.drawable.drawable_indigo);
            } else {
                color = getColor(R.drawable.drawable_grey);
            }
        }
        return color;
    }

    public String[] getNextDateTime(long timeLong){
        String date;
        String time;
        if (timeLong == 0) {
            date = null;
            time = null;
        } else {
            Calendar cl = Calendar.getInstance();
            cl.setTimeInMillis(timeLong);
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
            date = dateFormat.format(cl.getTime());

            String formattedTime;
            if (new SharedPrefs(mContext).loadBoolean(Constants.APP_UI_PREFERENCES_IS_24_TIME_FORMAT)){
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                formattedTime = sdf.format(cl.getTime());
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("K:mm:ss a");
                formattedTime = sdf.format(cl.getTime());
            }
            time = formattedTime;
        }
        return new String[]{date, time};
    }

    public String[] getNextDateTime(int year, int month, int dayOfMonth, int hourOfDay,
                                    int minuteOfHour, int seconds, long inTime, int repeatCode,
                                    int remCount, int delay){
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
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
            date = dateFormat.format(cl.getTime());

            String formattedTime;
            if (new SharedPrefs(mContext).loadBoolean(Constants.APP_UI_PREFERENCES_IS_24_TIME_FORMAT)){
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                formattedTime = sdf.format(cl.getTime());
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("K:mm:ss a");
                formattedTime = sdf.format(cl.getTime());
            }
            time = formattedTime;
        }
        return new String[]{date, time};
    }

    public long generateDateTime(long id){
        DataBase db = new DataBase(mContext);
        int hourOfDay = 0;
        int minuteOfHour = 0;
        int seconds = 0;
        int dayOfMonth = 0;
        int monthOfYear = 0;
        int year = 0;
        int repCode = 0;
        long repTime = 0;
        int repCount = 0;
        String type = null;
        String weekdays = null;
        Cursor c = db.getTask(id);
        if (c != null && c.moveToFirst()) {
            repCode = c.getInt(c.getColumnIndex(Constants.COLUMN_REPEAT));
            repCount = c.getInt(c.getColumnIndex(Constants.COLUMN_REMINDERS_COUNT));
            repTime = c.getLong(c.getColumnIndex(Constants.COLUMN_REMIND_TIME));
            dayOfMonth = c.getInt(c.getColumnIndex(Constants.COLUMN_DAY));
            monthOfYear = c.getInt(c.getColumnIndex(Constants.COLUMN_MONTH));
            year = c.getInt(c.getColumnIndex(Constants.COLUMN_YEAR));
            hourOfDay = c.getInt(c.getColumnIndex(Constants.COLUMN_HOUR));
            minuteOfHour = c.getInt(c.getColumnIndex(Constants.COLUMN_MINUTE));
            seconds = c.getInt(c.getColumnIndex(Constants.COLUMN_SECONDS));
            type = c.getString(c.getColumnIndex(Constants.COLUMN_TYPE));
            weekdays = c.getString(c.getColumnIndex(Constants.COLUMN_WEEKDAYS));
        }
        long dateTime;
        if (year == 0 && monthOfYear == 0 && dayOfMonth == 0 && hourOfDay == 0 && minuteOfHour == 0) {
            dateTime = 0;
        } else {
            long newDbTime;
            if (type.startsWith(Constants.TYPE_WEEKDAY)){
                newDbTime = getNextWeekdayTime(hourOfDay, minuteOfHour, weekdays, 0);
            } else if (type.startsWith(Constants.TYPE_MONTHDAY)){
                if (type.endsWith("_last")){
                    newDbTime = getLastMonthDayTime(hourOfDay, minuteOfHour, 0);
                } else {
                    newDbTime = getNextMonthDayTime(hourOfDay, minuteOfHour, dayOfMonth, 0);
                }
            } else {
                newDbTime = getEventTime(year, monthOfYear, dayOfMonth, hourOfDay, minuteOfHour,
                        seconds, repTime, repCode, repCount, 0);
            }
            dateTime = newDbTime;
        }
        return dateTime;
    }

    public long getEventTime(int year, int month, int dayOfMonth, int hourOfDay, int minuteOfHour,
                             int seconds, long inTime, int repeatCode, int remCount, int delay){
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
                result = days + " " + mContext.getString(R.string.remaining_days) +
                        "," + " " + hours + " " + mContext.getString(R.string.remaining_hours) + " " +
                        mContext.getString(R.string.remaining_and) + " " + min + " " +
                        mContext.getString(R.string.remaining_minutes);
            } else {
                result = days + " " + mContext.getString(R.string.remaining_day) +
                        "," + " " + hours + " " + mContext.getString(R.string.remaining_hours) + " " +
                        mContext.getString(R.string.remaining_and) + " " + min + " " +
                        mContext.getString(R.string.remaining_minutes);
            }
        } else if (days == 0 && hours > 0){
            if (hours > 1) {
                result = hours + " " + mContext.getString(R.string.remaining_hours) + " " +
                        mContext.getString(R.string.remaining_and) + " " + min + " " +
                        mContext.getString(R.string.remaining_minutes);
            } else {
                result = hours + " " + mContext.getString(R.string.remaining_hour) + " " +
                        mContext.getString(R.string.remaining_and) + " " + min + " " +
                        mContext.getString(R.string.remaining_minutes);
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

    public boolean getNextDate(int year, int month, int dayOfMonth, int hourOfDay, int minuteOfHour, int seconds,
                               long inTime, int repeatCode, int remCount) {
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

    public long getNextWeekdayTime(int hourOfDay, int minuteOfHour, String weekdays, int delay){
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

    private boolean isBetween(long value, long min, long max){
        return((value > min) && (value < max));
    }

    public boolean isDay(String repeat){
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

    public long getNextMonthDayTime(int hourOfDay, int minuteOfHour, int dayOfMonth, int delay){
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

    public long getNextMonthDayTime(int dayOfMonth, long fromTime, int multi){
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

    private long getLastMonthDayTime(long fromTime, int multi) {
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

    public long getLastMonthDayTime(int hourOfDay, int minuteOfHour, int delay){
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

    public boolean isDay(int dayOfMonth){
        if (dayOfMonth == 0){
            return isLastDay();
        }
        Calendar cc = Calendar.getInstance();
        cc.setTimeInMillis(System.currentTimeMillis());
        if (cc.get(Calendar.DAY_OF_MONTH) == dayOfMonth) return true;
        else return false;
    }

    public boolean isLastDay(){
        Calendar cc = Calendar.getInstance();
        cc.setTimeInMillis(System.currentTimeMillis());
        if (cc.get(Calendar.DAY_OF_MONTH) == cc.getActualMaximum(Calendar.DAY_OF_MONTH)) return true;
        else return false;
    }
}
