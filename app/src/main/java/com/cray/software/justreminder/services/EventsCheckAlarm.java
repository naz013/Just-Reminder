package com.cray.software.justreminder.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.helpers.CalendarManager;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.json.JsonModel;
import com.cray.software.justreminder.json.JsonRecurrence;
import com.cray.software.justreminder.modules.Module;
import com.cray.software.justreminder.reminder.DateType;

import org.dmfs.rfc5545.recur.Freq;
import org.dmfs.rfc5545.recur.InvalidRecurrenceRuleException;
import org.dmfs.rfc5545.recur.RecurrenceRule;

import java.util.ArrayList;
import java.util.Calendar;

public class EventsCheckAlarm extends WakefulBroadcastReceiver {

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
        SharedPrefs prefs = new SharedPrefs(context);
        int interval = prefs.loadInt(Prefs.AUTO_CHECK_FOR_EVENTS_INTERVAL);
        if (Module.isMarshmallow()) alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_HOUR * interval, alarmIntent);
        else alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
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
                DataBase db = new DataBase(context);
                db.open();
                Cursor c = db.getCalendarEvents();
                ArrayList<Long> ids = new ArrayList<>();
                if (c != null && c.moveToFirst()){
                    do {
                        long eventId = c.getLong(c.getColumnIndex(Constants.COLUMN_EVENT_ID));
                        ids.add(eventId);
                    } while (c.moveToNext());
                }
                if (c != null) c.close();
                for (CalendarManager.EventItem item : eventItems){
                    long itemId = item.getId();
                    if (!ids.contains(itemId)) {
                        String rrule = item.getRrule();
                        int repeat = 0;
                        if (rrule != null && !rrule.matches("")) {
                            try {
                                RecurrenceRule rule = new RecurrenceRule(rrule);
                                int interval = rule.getInterval();
                                Freq freq = rule.getFreq();
                                if (freq == Freq.HOURLY || freq == Freq.MINUTELY || freq == Freq.SECONDLY) {
                                } else {
                                    if (freq == Freq.WEEKLY) repeat = interval * 7;
                                    else if (freq == Freq.MONTHLY) repeat = interval * 30;
                                    else if (freq == Freq.YEARLY) repeat = interval * 365;
                                    else repeat = interval;
                                }
                            } catch (InvalidRecurrenceRuleException e) {
                                e.printStackTrace();
                            }
                        }
                        String summary = item.getTitle();
                        String uuID = SyncHelper.generateID();
                        Cursor cf = db.queryCategories();
                        String categoryId = null;
                        if (cf != null && cf.moveToFirst()) {
                            categoryId = cf.getString(cf.getColumnIndex(Constants.COLUMN_TECH_VAR));
                        }
                        if (cf != null) cf.close();

                        long due = item.getDtStart() + (repeat * AlarmManager.INTERVAL_DAY);
                        JsonRecurrence jsonRecurrence = new JsonRecurrence(0, repeat, -1, null, 0);
                        JsonModel jsonModel = new JsonModel(summary, Constants.TYPE_REMINDER, categoryId, uuID, due,
                                due, jsonRecurrence, null, null);
                        long id = new DateType(context, Constants.TYPE_REMINDER).save(jsonModel);
                        db.addCalendarEvent(null, id, item.getId());
                    }
                }
                db.close();
            }
            return null;
        }
    }
}