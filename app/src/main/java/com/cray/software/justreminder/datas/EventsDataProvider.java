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

package com.cray.software.justreminder.datas;

import android.app.AlarmManager;
import android.content.Context;

import com.cray.software.justreminder.birthdays.BirthdayHelper;
import com.cray.software.justreminder.birthdays.BirthdayItem;
import com.cray.software.justreminder.constants.Configs;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.datas.models.EventsItem;
import com.cray.software.justreminder.enums.EventType;
import com.cray.software.justreminder.groups.GroupHelper;
import com.cray.software.justreminder.groups.GroupItem;
import com.cray.software.justreminder.helpers.TimeCount;
import com.cray.software.justreminder.reminder.ReminderHelper;
import com.cray.software.justreminder.reminder.ReminderItem;
import com.cray.software.justreminder.reminder.json.JRecurrence;
import com.cray.software.justreminder.reminder.json.JsonModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class EventsDataProvider {

    private ArrayList<EventsItem> data = new ArrayList<>();
    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private int hour, minute;
    private boolean isFeature;
    private boolean isBirthdays;
    private boolean isReminders;
    private Context mContext;

    public EventsDataProvider(Context mContext){
        this.mContext = mContext;
        data = new ArrayList<>();
    }

    public void setBirthdays(boolean isBirthdays){
        this.isBirthdays = isBirthdays;
    }

    public void setReminders(boolean isReminders){
        this.isReminders = isReminders;
    }

    public void setTime(int hour, int minute){
        this.hour = hour;
        this.minute = minute;
    }

    public void setFeature(boolean isFeature){
        this.isFeature = isFeature;
    }

    public ArrayList<EventsItem> getData(){
        return data;
    }

    public EventsItem getItem(int position){
        return data.get(position);
    }

    public ArrayList<EventsItem> getMatches(int day, int month, int year){
        ArrayList<EventsItem> res = new ArrayList<>();
        for (EventsItem item : data){
            int mDay = item.getDay();
            int mMonth = item.getMonth();
            int mYear = item.getYear();
            EventType type = item.getInn();
            if (type == EventType.birthday && mDay == day && mMonth == month){
                res.add(item);
            } else {
                if (mDay == day && mMonth == month && mYear == year) res.add(item);
            }
        }
        return res;
    }

    public void fillArray(){
        if (isBirthdays) loadBirthdays();
        if (isReminders) loadReminders();
    }

    public void loadBirthdays(){
        List<BirthdayItem> list = BirthdayHelper.getInstance(mContext).getAll();
        for (BirthdayItem item : list) {
            String name = item.getName();
            long id = item.getId();
            String number = item.getNumber();
            Date date = null;
            try {
                date = format.parse(item.getDate());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (date != null) {
                Calendar calendar1 = Calendar.getInstance();
                calendar1.setTime(date);
                int bDay = calendar1.get(Calendar.DAY_OF_MONTH);
                int bMonth = calendar1.get(Calendar.MONTH);
                int bYear = calendar1.get(Calendar.YEAR);
                calendar1.setTimeInMillis(System.currentTimeMillis());
                calendar1.set(Calendar.MONTH, bMonth);
                calendar1.set(Calendar.DAY_OF_MONTH, bDay);
                calendar1.set(Calendar.HOUR_OF_DAY, hour);
                calendar1.set(Calendar.MINUTE, minute);
                data.add(new EventsItem("birthday", name, number, id, calendar1.getTimeInMillis(),
                        bDay, bMonth, bYear, EventType.birthday, 0));
            }
        }
    }

    public void loadReminders(){
        List<GroupItem> allGroups = GroupHelper.getInstance(mContext).getAll();
        Map<String, Integer> map = new HashMap<>();
        for (GroupItem item : allGroups) {
            map.put(item.getUuId(), item.getColor());
        }
        List<ReminderItem> reminders = ReminderHelper.getInstance(mContext).getRemindersEnabled();
        for (ReminderItem item : reminders) {
            String mType = item.getType();
            String summary = item.getSummary();
            String category = item.getGroupUuId();
            long eventTime = item.getDateTime();
            long id = item.getId();
            int color = 0;
            if (map.containsKey(category)) color = map.get(category);
            if (!mType.contains(Constants.TYPE_LOCATION)) {
                JsonModel jsonModel = item.getModel();
                JRecurrence jRecurrence = jsonModel.getRecurrence();
                long repeatTime = jRecurrence.getRepeat();
                long limit = jRecurrence.getLimit();
                long count = jsonModel.getCount();
                int myDay = jRecurrence.getMonthday();
                boolean isLimited = limit > 0;
                String number = jsonModel.getAction().getTarget();

                Calendar calendar1 = Calendar.getInstance();
                calendar1.setTimeInMillis(eventTime);
                int mDay = calendar1.get(Calendar.DAY_OF_MONTH);
                int mMonth = calendar1.get(Calendar.MONTH);
                int mYear = calendar1.get(Calendar.YEAR);
                if (eventTime > 0) {
                    if (number == null) number = "0";
                    data.add(new EventsItem("reminder", summary, number, id, eventTime, mDay,
                            mMonth, mYear, EventType.reminder, color));
                }
                if (isFeature) {
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
                                mDay = calendar1.get(Calendar.DAY_OF_MONTH);
                                mMonth = calendar1.get(Calendar.MONTH);
                                mYear = calendar1.get(Calendar.YEAR);
                                days++;
                                if (number == null) number = "0";
                                data.add(new EventsItem("reminder", summary, number, id, eventTime, mDay,
                                        mMonth, mYear, EventType.reminder, color));
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
                            mDay = calendar1.get(Calendar.DAY_OF_MONTH);
                            mMonth = calendar1.get(Calendar.MONTH);
                            mYear = calendar1.get(Calendar.YEAR);
                            if (eventTime > 0) {
                                days++;
                                if (number == null) number = "0";
                                data.add(new EventsItem("reminder", summary, number, id, eventTime, mDay,
                                        mMonth, mYear, EventType.reminder, color));
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
                            mDay = calendar1.get(Calendar.DAY_OF_MONTH);
                            mMonth = calendar1.get(Calendar.MONTH);
                            mYear = calendar1.get(Calendar.YEAR);
                            if (eventTime > 0) {
                                days++;
                                if (number == null) number = "0";
                                data.add(new EventsItem("reminder", summary, number, id, eventTime, mDay,
                                        mMonth, mYear, EventType.reminder, color));
                            }
                        } while (days < max);

                    }
                }
            }
        }
    }
}
