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

package com.cray.software.justreminder.utils;

import android.content.Context;

import com.cray.software.justreminder.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Some utils for time counting.
 */
public class TimeUtil {

    public static final SimpleDateFormat format24 = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
    public static final SimpleDateFormat format12 = new SimpleDateFormat("dd MMM yyyy, K:mm a", Locale.getDefault());
    public static final SimpleDateFormat fullDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault());
    public static final SimpleDateFormat fullDateTime24 = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm", Locale.getDefault());
    public static final SimpleDateFormat fullDateTime12 = new SimpleDateFormat("EEE, dd MMM yyyy K:mm a", Locale.getDefault());
    public static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
    public static final SimpleDateFormat time24 = new SimpleDateFormat("HH:mm", Locale.getDefault());
    public static final SimpleDateFormat time12 = new SimpleDateFormat("K:mm a", Locale.getDefault());
    public static final SimpleDateFormat simpleDate = new SimpleDateFormat("d MMMM", Locale.getDefault());

    public TimeUtil(){}

    /**
     * Get date and time string from date.
     * @param date date to convert.
     * @param is24 24H time format flag.
     * @return Date string
     */
    public static String getFullDateTime(long date, boolean is24){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date);
        if (is24) return fullDateTime24.format(calendar.getTime());
        else return fullDateTime12.format(calendar.getTime());
    }

    /**
     * Get date and time string from date.
     * @param date date to convert.
     * @return Date string
     */
    public static String getSimpleDate(long date){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date);
        return simpleDate.format(calendar.getTime());
    }

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

    /**
     * Get formatted ages.
     * @param mContext application context.
     * @param date date
     * @return String
     */
    public static String getAgeFormatted(Context mContext, String date) {
        int years = getYears(date);
        StringBuilder result = new StringBuilder();
        String lang = Locale.getDefault().getLanguage().toLowerCase();
        if (lang.startsWith("uk") || lang.startsWith("ru")) {
            long last = years;
            while (last > 10) {
                last -= 10;
            }
            if (last == 1 && years != 11) {
                result.append(String.format(mContext.getString(R.string.x_year), years));
            } else if (last < 5 && (years < 12 || years > 14)) {
                result.append(String.format(mContext.getString(R.string.x_yearzz), years));
            } else {
                result.append(String.format(mContext.getString(R.string.x_years), years));
            }
        } else {
            if (years < 2)
                result.append(String.format(mContext.getString(R.string.x_year), years));
            else
                result.append(String.format(mContext.getString(R.string.x_years), years));
        }
        return result.toString();
    }

    /**
     * Get formatted ages.
     * @param mContext application context.
     * @param date date
     * @return String
     */
    public static String getAgeFormatted(Context mContext, int date) {
        int years = getAge(date);
        StringBuilder result = new StringBuilder();
        String lang = Locale.getDefault().toString().toLowerCase();
        if (lang.startsWith("uk") || lang.startsWith("ru")) {
            long last = years;
            while (last > 10) {
                last -= 10;
            }
            if (last == 1 && years != 11) {
                result.append(String.format(mContext.getString(R.string.x_year), years));
            } else if (last < 5 && (years < 12 || years > 14)) {
                result.append(String.format(mContext.getString(R.string.x_yearzz), years));
            } else {
                result.append(String.format(mContext.getString(R.string.x_years), years));
            }
        } else {
            if (years < 2)
                result.append(String.format(mContext.getString(R.string.x_year), years));
            else
                result.append(String.format(mContext.getString(R.string.x_years), years));
        }
        return result.toString();
    }
}
