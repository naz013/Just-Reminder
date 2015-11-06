package com.cray.software.justreminder.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Some utils for time counting.
 */
public class TimeUtil {

    public static final SimpleDateFormat format24 = new SimpleDateFormat("dd MMM yyyy, HH:mm");
    public static final SimpleDateFormat format12 = new SimpleDateFormat("dd MMM yyyy, K:mm a");
    public static final SimpleDateFormat fullDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault());
    public static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
    public static final SimpleDateFormat time24 = new SimpleDateFormat("HH:mm");
    public static final SimpleDateFormat time12 = new SimpleDateFormat("K:mm a");

    public TimeUtil(){}

    /**
     * Get date and time string from date.
     * @param date date to convert.
     * @return Date string
     */
    public static String getDate(Date date){
        return fullDateFormat.format(date);
    }

    /**
     * Get time from string.
     * @param date date string.
     * @return Date
     */
    public static Date getDate(String date){
        try {
            return time24.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get user age from birth date string.
     * @param dateOfBirth date of birth.
     * @return Integer
     */
    public static int getYears(String dateOfBirth){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        int years;
        Date date = null;
        try {
            date = format.parse(dateOfBirth);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        if (date != null) calendar.setTime(date);
        int yearOfBirth = calendar.get(Calendar.YEAR);

        Calendar calendar1 = Calendar.getInstance();
        calendar1.getTimeInMillis();
        int currentYear = calendar1.get(Calendar.YEAR);
        years = currentYear - yearOfBirth;
        return years;
    }

    /**
     * Get date and time string from date.
     * @param date date to convert.
     * @param is24 24H time format flag.
     * @return Date string
     */
    public static String getDateTime(Date date, boolean is24){
        if (is24) return format24.format(date);
        else return format12.format(date);
    }

    /**
     * Get time from date object.
     * @param date date to convert.
     * @param is24 24H time format flag.
     * @return Time string
     */
    public static String getTime(Date date, boolean is24){
        if (is24) return time24.format(date);
        else return time12.format(date);
    }

    /**
     * Get age from year of birth.
     * @param year year.
     * @return Integer
     */
    public static int getAge(int year){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        int mYear = calendar.get(Calendar.YEAR);
        return mYear - year;
    }

    /**
     * Get Date object.
     * @param year year.
     * @param month month.
     * @param day day.
     * @return Date
     */
    public static Date getDate(int year, int month, int day) {
        Calendar cal1 = Calendar.getInstance();
        cal1.set(Calendar.YEAR, year);
        cal1.set(Calendar.MONTH, month);
        cal1.set(Calendar.DAY_OF_MONTH, day);
        cal1.set(Calendar.HOUR_OF_DAY, 0);
        cal1.set(Calendar.MINUTE, 0);
        cal1.set(Calendar.SECOND, 0);
        cal1.set(Calendar.MILLISECOND, 0);
        return cal1.getTime();
    }

    /**
     * Generate human readable string from time.
     * @param time time in milliseconds.
     * @return Time string
     */
    public static String generateAfterString(long time){
        long s = 1000;
        long m = s * 60;
        long h = m * 60;
        long hours = (time / h);
        long minutes = ((time - hours * h) / (m));
        long seconds = ((time - (hours * h) - (minutes * m)) / (s));
        String hourStr;
        if (hours < 10) hourStr = "0" + hours;
        else hourStr = String.valueOf(hours);
        String minuteStr;
        if (minutes < 10) minuteStr = "0" + minutes;
        else minuteStr = String.valueOf(minutes);
        String secondStr;
        if (seconds < 10) secondStr = "0" + seconds;
        else secondStr = String.valueOf(seconds);
        return hourStr + minuteStr + secondStr;
    }
}
