/**
 * Copyright 2015 Nazar Suhovich
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
package com.cray.software.justreminder.reminder;

import android.app.AlarmManager;
import android.content.Context;
import android.util.Log;

import com.cray.software.justreminder.birthdays.BirthdayHelper;
import com.cray.software.justreminder.birthdays.BirthdayItem;
import com.cray.software.justreminder.constants.Configs;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.TimeCount;
import com.cray.software.justreminder.reminder.json.JRecurrence;
import com.cray.software.justreminder.reminder.json.JsonModel;
import com.hexrain.flextcal.Events;
import com.hexrain.flextcal.FlextHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import hirondelle.date4j.DateTime;

public class ReminderDataProvider {

    private Context mContext;
    private boolean isReminder = false;
    private boolean isFeature = false;

    private HashMap<DateTime, Events> map = new HashMap<>();

    public ReminderDataProvider(Context mContext, boolean isReminder, boolean isFeature){
        this.mContext = mContext;
        this.isReminder = isReminder;
        this.isFeature = isFeature;
        map = new HashMap<>();
    }

    private void setEvent(long eventTime, String summary, int color, Events.Type type) {
        DateTime key = FlextHelper.convertToDateTime(eventTime);
        if (map.containsKey(key)) {
            Events events = map.get(key);
            events.addEvent(summary, color, type);
            map.put(key, events);
        } else {
            Events events = new Events(summary, color, type);
            map.put(key, events);
        }
    }

    public HashMap<DateTime, Events> getEvents() {
        ColorSetter cs = new ColorSetter(mContext);
        int bColor = cs.getColor(cs.colorBirthdayCalendar());

        if (isReminder) {
            int rColor = cs.getColor(cs.colorReminderCalendar());
            List<ReminderItem> reminders = ReminderHelper.getInstance(mContext).getRemindersEnabled();
            for (ReminderItem item : reminders) {
                String mType = item.getType();
                String summary = item.getSummary();
                long eventTime = item.getDateTime();
                if (!mType.contains(Constants.TYPE_LOCATION)) {
                    JsonModel jsonModel = item.getModel();
                    JRecurrence jRecurrence = jsonModel.getRecurrence();
                    long repeatTime = jRecurrence.getRepeat();
                    long limit = jRecurrence.getLimit();
                    long count = jsonModel.getCount();
                    int myDay = jRecurrence.getMonthday();
                    boolean isLimited = limit > 0;

                    if (eventTime > 0) {
                        setEvent(eventTime, summary, rColor, Events.Type.REMINDER);
                    } else continue;

                    if (isFeature) {
                        Calendar calendar1 = Calendar.getInstance();
                        calendar1.setTimeInMillis(eventTime);
                        if (mType.startsWith(Constants.TYPE_WEEKDAY)) {
                            long days = 0;
                            long max = Configs.MAX_DAYS_COUNT;
                            if (isLimited) max = limit - count;
                            List<Integer> list = jRecurrence.getWeekdays();
                            do {
                                calendar1.setTimeInMillis(calendar1.getTimeInMillis() +
                                        AlarmManager.INTERVAL_DAY);
                                eventTime = calendar1.getTimeInMillis();
                                int weekDay = calendar1.get(Calendar.DAY_OF_WEEK);
                                if (list.get(weekDay - 1) == 1 && eventTime > 0) {
                                    days++;
                                    setEvent(eventTime, summary, rColor, Events.Type.REMINDER);
                                }
                            } while (days < max);
                        } else if (mType.startsWith(Constants.TYPE_MONTHDAY)) {
                            long days = 0;
                            long max = Configs.MAX_DAYS_COUNT;
                            if (isLimited) max = limit - count;
                            do {
                                eventTime = TimeCount.getNextMonthDayTime(myDay,
                                        calendar1.getTimeInMillis() + TimeCount.DAY);
                                calendar1.setTimeInMillis(eventTime);
                                if (eventTime > 0) {
                                    days++;
                                    setEvent(eventTime, summary, rColor, Events.Type.REMINDER);
                                }
                            } while (days < max);
                        } else {
                            if (repeatTime == 0) continue;
                            long days = 0;
                            long max = Configs.MAX_DAYS_COUNT;
                            if (isLimited) max = limit - count;
                            do {
                                calendar1.setTimeInMillis(calendar1.getTimeInMillis() + repeatTime);
                                eventTime = calendar1.getTimeInMillis();
                                days++;
                                setEvent(eventTime, summary, rColor, Events.Type.REMINDER);
                            } while (days < max);

                        }
                    }
                }
            }
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        List<BirthdayItem> list = BirthdayHelper.getInstance(mContext).getAll();
        Log.d(Constants.LOG_TAG, "Count BD" + list.size());
        for (BirthdayItem item : list) {
            Date date = null;
            try {
                date = format.parse(item.getDate());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            int year = calendar.get(Calendar.YEAR);
            if (date != null) {
                try {
                    calendar.setTime(date);
                } catch (NullPointerException e){
                    e.printStackTrace();
                }
                int i = -1;
                while (i < 2) {
                    calendar.set(Calendar.YEAR, year + i);
                    setEvent(calendar.getTimeInMillis(), item.getName(), bColor, Events.Type.BIRTHDAY);
                    i++;
                }
            }
        }
        return map;
    }
}
