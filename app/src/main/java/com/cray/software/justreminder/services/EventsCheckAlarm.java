package com.cray.software.justreminder.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;

import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.helpers.CalendarManager;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.Prefs;

import org.dmfs.rfc5545.recur.InvalidRecurrenceRuleException;
import org.dmfs.rfc5545.recur.RecurrenceRule;

import java.util.ArrayList;
import java.util.Calendar;

public class EventsCheckAlarm extends BroadcastReceiver {

    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent service = new Intent(context, EventsCheckAlarm.class);
        context.startService(service);
        new CheckEventsAsync(context).execute();
    }

    public void setAlarm(Context context){
        Intent intent1 = new Intent(context, EventsCheckAlarm.class);
        alarmIntent = PendingIntent.getBroadcast(context, 1111, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        long currTime = calendar.getTimeInMillis();
        SharedPrefs prefs = new SharedPrefs(context);
        int interval = prefs.loadInt(Prefs.AUTO_CHECK_FOR_EVENTS_INTERVAL);
        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_HOUR * interval, alarmIntent);
    }

    public void cancelAlarm(Context context) {
        Integer i = (int) (long) 1100;
        Intent intent = new Intent(context, EventsCheckAlarm.class);
        alarmIntent = PendingIntent.getBroadcast(context, i, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmMgr!= null) {
            alarmMgr.cancel(alarmIntent);
        }
    }

    private class CheckEventsAsync extends AsyncTask<Void, Void, Void>{
        Context context;

        public CheckEventsAsync(Context context) {
            this.context = context;
        }

        @Override
        protected Void doInBackground(Void... params) {
            CalendarManager cm = new CalendarManager(context);
            SharedPrefs prefs = new SharedPrefs(context);
            String calID = prefs.loadPrefs(Prefs.EVENTS_CALENDAR);
            ArrayList<CalendarManager.EventItem> eventItems = cm.getEvents(calID);
            if (eventItems != null && eventItems.size() > 0){
                DataBase DB = new DataBase(context);
                DB.open();
                Cursor c = DB.getCalendarEvents();
                ArrayList<Long> ids = new ArrayList<>();
                if (c != null && c.moveToFirst()){
                    do {
                        long eventId = c.getLong(c.getColumnIndex(Constants.COLUMN_EVENT_ID));
                        ids.add(eventId);
                    } while (c.moveToNext());
                }
                for (CalendarManager.EventItem item : eventItems){
                    long itemId = item.getId();
                    if (!ids.contains(itemId)) {
                        String rrule = item.getRrule();
                        int repeat = 0;
                        if (rrule != null && !rrule.matches("")) {
                            try {
                                RecurrenceRule rule = new RecurrenceRule(rrule);
                                int interval = rule.getInterval();
                                RecurrenceRule.Freq freq = rule.getFreq();
                                if (freq == RecurrenceRule.Freq.HOURLY || freq == RecurrenceRule.Freq.MINUTELY || freq == RecurrenceRule.Freq.SECONDLY) {
                                } else {
                                    if (freq == RecurrenceRule.Freq.WEEKLY) repeat = interval * 7;
                                    else if (freq == RecurrenceRule.Freq.MONTHLY) repeat = interval * 30;
                                    else if (freq == RecurrenceRule.Freq.YEARLY) repeat = interval * 365;
                                    else repeat = interval;
                                }
                            } catch (InvalidRecurrenceRuleException e) {
                                e.printStackTrace();
                            }
                        }
                        String text = item.getTitle();
                        String type = Constants.TYPE_REMINDER;

                        String uuID = SyncHelper.generateID();
                        Cursor cf = DB.queryCategories();
                        String categoryId = null;
                        if (cf != null && cf.moveToFirst()) {
                            categoryId = cf.getString(cf.getColumnIndex(Constants.COLUMN_TECH_VAR));
                        }
                        if (cf != null) cf.close();

                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(item.getDtStart());
                        int day = calendar.get(Calendar.DAY_OF_MONTH);
                        int month = calendar.get(Calendar.MONTH);
                        int year = calendar.get(Calendar.YEAR);
                        int hour = calendar.get(Calendar.HOUR_OF_DAY);
                        int minute = calendar.get(Calendar.MINUTE);

                        long id = DB.insertReminder(text, type, day, month, year, hour,
                                minute, 0, null, repeat, 0, 0, 0, 0, uuID, null, 1, null, 0, 0,
                                0, categoryId, null);
                        DB.updateReminderDateTime(id);
                        DB.addCalendarEvent(null, id, item.getId());
                        new AlarmReceiver().setAlarm(context, id);
                    }
                }
                DB.close();
            }
            return null;
        }
    }
}