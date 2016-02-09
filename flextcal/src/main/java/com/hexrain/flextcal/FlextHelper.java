package com.hexrain.flextcal;

import android.content.Context;
import android.os.Build;
import android.support.annotation.ColorRes;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import hirondelle.date4j.DateTime;

/**
 * Copyright 2015 Nazar Suhovich
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
public class FlextHelper {

    public static SimpleDateFormat yyyyMMddFormat = new SimpleDateFormat(
            "yyyy-MM-dd", Locale.ENGLISH);

    public FlextHelper(){
    }

    /**
     * Get color from resource.
     * @param context application context.
     * @param res resource identifier
     * @return Color
     */
    public static int getColor(Context context, @ColorRes int res){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            return context.getResources().getColor(res, null);
        else return context.getResources().getColor(res);
    }

    /**
     * Retrieve all the dates for a given calendar month Include previous month,
     * current month and next month.
     *
     * @param month month
     * @param year year
     * @param startDayOfWeek
     *            : calendar can start from customized date instead of Sunday
     * @return list of datetime objects
     */
    public static ArrayList<DateTime> getFullWeeks(int month, int year,
                                                   int startDayOfWeek) {
        ArrayList<DateTime> datetimeList = new ArrayList<>();

        DateTime firstDateOfMonth = new DateTime(year, month, 1, 0, 0, 0, 0);
        DateTime lastDateOfMonth = firstDateOfMonth.plusDays(firstDateOfMonth
                .getNumDaysInMonth() - 1);

        // Add dates of first week from previous month
        int weekdayOfFirstDate = firstDateOfMonth.getWeekDay();

        // If weekdayOfFirstDate smaller than startDayOfWeek
        // For e.g: weekdayFirstDate is Monday, startDayOfWeek is Tuesday
        // increase the weekday of FirstDate because it's in the future
        if (weekdayOfFirstDate < startDayOfWeek) {
            weekdayOfFirstDate += 7;
        }

        while (weekdayOfFirstDate > 0) {
            DateTime dateTime = firstDateOfMonth.minusDays(weekdayOfFirstDate
                    - startDayOfWeek);
            if (!dateTime.lt(firstDateOfMonth)) {
                break;
            }

            datetimeList.add(dateTime);
            weekdayOfFirstDate--;
        }

        // Add dates of current month
        for (int i = 0; i < lastDateOfMonth.getDay(); i++) {
            datetimeList.add(firstDateOfMonth.plusDays(i));
        }

        // Add dates of last week from next month
        int endDayOfWeek = startDayOfWeek - 1;

        if (endDayOfWeek == 0) {
            endDayOfWeek = 7;
        }

        if (lastDateOfMonth.getWeekDay() != endDayOfWeek) {
            int i = 1;
            while (true) {
                DateTime nextDay = lastDateOfMonth.plusDays(i);
                datetimeList.add(nextDay);
                i++;
                if (nextDay.getWeekDay() == endDayOfWeek) {
                    break;
                }
            }
        }

        // Add more weeks to fill remaining rows
        int size = datetimeList.size();
        int row = size / 7;
        int numOfDays = (6 - row) * 7;
        DateTime lastDateTime = datetimeList.get(size - 1);
        for (int i = 1; i <= numOfDays; i++) {
            DateTime nextDateTime = lastDateTime.plusDays(i);
            datetimeList.add(nextDateTime);
        }

        return datetimeList;
    }

    /**
     * Get the DateTime from Date, with hour and min is 0
     *
     * @param date date
     * @return date time object
     */

    public static DateTime convertDateToDateTime(Date date) {
        // Get year, javaMonth, date
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.setTime(date);

        int year = calendar.get(Calendar.YEAR);
        int javaMonth = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DATE);

        // javaMonth start at 0. Need to plus 1 to get datetimeMonth
        return new DateTime(year, javaMonth + 1, day, 0, 0, 0, 0);
    }

    public static Date convertDateTimeToDate(DateTime dateTime) {
        int year = dateTime.getYear();
        int datetimeMonth = dateTime.getMonth();
        int day = dateTime.getDay();

        Calendar calendar = Calendar.getInstance();
        calendar.clear();

        // datetimeMonth start at 1. Need to minus 1 to get javaMonth
        calendar.set(year, datetimeMonth - 1, day);

        return calendar.getTime();
    }

    /**
     * Get the Date from String with custom format. Default format is yyyy-MM-dd
     *
     * @param dateString date in string
     * @param dateFormat date formatter string
     * @return date
     * @throws ParseException
     */
    public static Date getDateFromString(String dateString, String dateFormat)
            throws ParseException {
        SimpleDateFormat formatter;
        if (dateFormat == null) {
            formatter = yyyyMMddFormat;
        } else {
            formatter = new SimpleDateFormat(dateFormat, Locale.ENGLISH);
        }

        return formatter.parse(dateString);
    }

    /**
     * Get the DateTime from String with custom format. Default format is
     * yyyy-MM-dd
     *
     * @param dateString date in string
     * @param dateFormat date formatter string
     * @return datetime
     */
    public static DateTime getDateTimeFromString(String dateString,
                                                 String dateFormat) {
        Date date;
        try {
            date = getDateFromString(dateString, dateFormat);
            return convertDateToDateTime(date);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public static DateTime convertToDateTime(long eventTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.setTimeInMillis(eventTime);
        int year = calendar.get(Calendar.YEAR);
        int javaMonth = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DATE);

        // javaMonth start at 0. Need to plus 1 to get datetimeMonth
        return new DateTime(year, javaMonth + 1, day, 0, 0, 0, 0);
    }

    public static boolean is21() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }
}
