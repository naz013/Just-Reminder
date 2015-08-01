package com.cray.software.justreminder.reminder;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.widget.Toast;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.ReminderManager;
import com.cray.software.justreminder.async.BackupTask;
import com.cray.software.justreminder.async.DeleteReminderFiles;
import com.cray.software.justreminder.async.DisableAsync;
import com.cray.software.justreminder.cloud.GTasksHelper;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.helpers.CalendarManager;
import com.cray.software.justreminder.helpers.Notifier;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.helpers.TimeCount;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.services.AlarmReceiver;
import com.cray.software.justreminder.services.CheckPosition;
import com.cray.software.justreminder.services.DelayReceiver;
import com.cray.software.justreminder.services.GeolocationService;
import com.cray.software.justreminder.services.MonthDayReceiver;
import com.cray.software.justreminder.services.PositionDelayReceiver;
import com.cray.software.justreminder.services.RepeatNotificationReceiver;
import com.cray.software.justreminder.services.WeekDayReceiver;
import com.cray.software.justreminder.utils.ReminderUtils;
import com.cray.software.justreminder.widgets.UpdatesHelper;

import java.util.Calendar;

public class Reminder {
    public Reminder(){
    }

    public static void generate(long id, Context context){
        DataBase DB = new DataBase(context);
        DB.open();
        Cursor c = DB.getReminder(id);
        if (c != null && c.moveToFirst()){
            String text = "";
            String type = "";
            String weekdays = "";
            int hour = 0;
            int minute = 0;
            int day = 0;
            int exp = 0;
            Cursor t = DB.getReminder(id);
            if (t != null && t.moveToNext()) {
                text = t.getString(t.getColumnIndex(Constants.COLUMN_TEXT));
                type = t.getString(t.getColumnIndex(Constants.COLUMN_TYPE));
                weekdays = t.getString(t.getColumnIndex(Constants.COLUMN_WEEKDAYS));
                hour = t.getInt(t.getColumnIndex(Constants.COLUMN_HOUR));
                day = t.getInt(t.getColumnIndex(Constants.COLUMN_DAY));
                minute = t.getInt(t.getColumnIndex(Constants.COLUMN_MINUTE));
                exp = t.getInt(t.getColumnIndex(Constants.COLUMN_EXPORT_TO_CALENDAR));
            }
            long nextDate = TimeCount.getNextWeekdayTime(hour, minute, weekdays, 0);
            SharedPrefs sPrefs = new SharedPrefs(context);
            boolean isCalendar = sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_EXPORT_TO_CALENDAR);
            boolean isStock = sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_EXPORT_TO_STOCK);
            if (type.startsWith(Constants.TYPE_MONTHDAY))
                nextDate = TimeCount.getNextMonthDayTime(hour, minute, day, 0);

            if (isCalendar || isStock && exp == 1) {
                ReminderUtils.exportToCalendar(context, text, nextDate, id, isCalendar, isStock);
            }
        }
        if (c != null) c.close();
        DB.close();
    }

    public static void backup(Context context){
        if (new SharedPrefs(context).loadBoolean(Constants.APP_UI_PREFERENCES_AUTO_BACKUP)){
            new BackupTask(context).execute();
        }
    }

    public static boolean toggle(long id, Context context){
        DataBase db = new DataBase(context);
        db.open();
        Cursor c = db.getReminder(id);
        String type = null;
        int hour = 0, minute = 0, seconds = 0, day = 0, month = 0, year = 0, repCode = 0,
                repCount = 0, isDone = 0;
        long repTime = 0;
        if (c != null && c.moveToFirst()) {
            type = c.getString(c.getColumnIndex(Constants.COLUMN_TYPE));
            hour = c.getInt(c.getColumnIndex(Constants.COLUMN_HOUR));
            minute = c.getInt(c.getColumnIndex(Constants.COLUMN_MINUTE));
            seconds = c.getInt(c.getColumnIndex(Constants.COLUMN_SECONDS));
            day = c.getInt(c.getColumnIndex(Constants.COLUMN_DAY));
            month = c.getInt(c.getColumnIndex(Constants.COLUMN_MONTH));
            year = c.getInt(c.getColumnIndex(Constants.COLUMN_YEAR));
            repCode = c.getInt(c.getColumnIndex(Constants.COLUMN_REPEAT));
            repTime = c.getLong(c.getColumnIndex(Constants.COLUMN_REMIND_TIME));
            repCount = c.getInt(c.getColumnIndex(Constants.COLUMN_REMINDERS_COUNT));
            isDone = c.getInt(c.getColumnIndex(Constants.COLUMN_IS_DONE));
        }
        if (c != null) c.close();
        boolean res;
        if (isDone == 0){
            db.setDone(id);
            Integer i = (int) (long) id;
            new AlarmReceiver().cancelAlarm(context, i);
            new WeekDayReceiver().cancelAlarm(context, i);
            new MonthDayReceiver().cancelAlarm(context, i);
            new DelayReceiver().cancelAlarm(context, id);
            new RepeatNotificationReceiver().cancelAlarm(context, i);
            new PositionDelayReceiver().cancelDelay(context, i);
            NotificationManager mNotifyMgr =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotifyMgr.cancel(i);
            new DisableAsync(context).execute();
            res = true;
        } else {
            if (type.startsWith(Constants.TYPE_WEEKDAY)) {
                db.setUnDone(id);
                db.updateReminderDateTime(id);
                new WeekDayReceiver().setAlarm(context, id);
                res = true;
            } else if (type.startsWith(Constants.TYPE_MONTHDAY)) {
                db.setUnDone(id);
                db.updateReminderDateTime(id);
                new MonthDayReceiver().setAlarm(context, id);
                res = true;
            } else if (type.startsWith(Constants.TYPE_LOCATION) ||
                    type.startsWith(Constants.TYPE_LOCATION_OUT)) {
                db.setUnDone(id);
                db.updateReminderDateTime(id);
                if (year == 0 && month == 0 && day == 0 && hour == 0 && minute == 0) {
                    context.startService(new Intent(context, GeolocationService.class)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                    context.startService(new Intent(context, CheckPosition.class)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                } else {
                    new PositionDelayReceiver().setDelay(context, id);
                }
                res = true;
            } else {
                if (type.matches(Constants.TYPE_TIME)){
                    final Calendar calendar1 = Calendar.getInstance();
                    int myYear = calendar1.get(Calendar.YEAR);
                    int myMonth = calendar1.get(Calendar.MONTH);
                    int myDay = calendar1.get(Calendar.DAY_OF_MONTH);
                    int myHour = calendar1.get(Calendar.HOUR_OF_DAY);
                    int myMinute = calendar1.get(Calendar.MINUTE);
                    int mySeconds = calendar1.get(Calendar.SECOND);
                    db.updateReminderStartTime(id, myDay, myMonth, myYear, myHour, myMinute, mySeconds);
                    db.updateReminderDateTime(id);
                    new AlarmReceiver().setAlarm(context, id);
                    res = true;
                } else {
                    if (new TimeCount(context)
                            .getNextDate(year, month, day, hour, minute, seconds, repTime, repCode, repCount)) {
                        db.setUnDone(id);
                        db.updateReminderDateTime(id);
                        new AlarmReceiver().setAlarm(context, id);
                        res = true;
                    } else {
                        res = false;
                        Toast.makeText(context, context.getString(R.string.edit_reminder_toast),
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
        db.close();
        new Notifier(context).recreatePermanent();
        new UpdatesHelper(context).updateWidget();
        return res;
    }

    public static void copy(long id, long time, Context context) {
        DataBase db = new DataBase(context);
        SharedPrefs sPrefs = new SharedPrefs(context);
        if (!db.isOpen()) db.open();
        Cursor c = db.getReminder(id);
        if (c != null && c.moveToFirst()){
            String text = c.getString(c.getColumnIndex(Constants.COLUMN_TEXT));
            String type = c.getString(c.getColumnIndex(Constants.COLUMN_TYPE));
            String number = c.getString(c.getColumnIndex(Constants.COLUMN_NUMBER));
            String melody = c.getString(c.getColumnIndex(Constants.COLUMN_CUSTOM_MELODY));
            String categoryId = c.getString(c.getColumnIndex(Constants.COLUMN_CATEGORY));
            String weekdays = c.getString(c.getColumnIndex(Constants.COLUMN_WEEKDAYS));
            int myHour;
            int myMinute;
            int myDay = c.getInt(c.getColumnIndex(Constants.COLUMN_DAY));
            int myMonth = c.getInt(c.getColumnIndex(Constants.COLUMN_MONTH));
            int myYear = c.getInt(c.getColumnIndex(Constants.COLUMN_YEAR));
            int repCode = c.getInt(c.getColumnIndex(Constants.COLUMN_REPEAT));
            int exp = c.getInt(c.getColumnIndex(Constants.COLUMN_EXPORT_TO_CALENDAR));
            int radius = c.getInt(c.getColumnIndex(Constants.COLUMN_CUSTOM_RADIUS));
            int ledColor = c.getInt(c.getColumnIndex(Constants.COLUMN_LED_COLOR));
            int code = c.getInt(c.getColumnIndex(Constants.COLUMN_SYNC_CODE));
            double latitude = c.getDouble(c.getColumnIndex(Constants.COLUMN_LATITUDE));
            double longitude = c.getDouble(c.getColumnIndex(Constants.COLUMN_LONGITUDE));
            if (!type.matches(Constants.TYPE_TIME)) {
                String uuID = SyncHelper.generateID();
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(time);
                myHour = calendar.get(Calendar.HOUR_OF_DAY);
                myMinute = calendar.get(Calendar.MINUTE);
                long idN = db.insertReminder(text, type, myDay, myMonth, myYear, myHour, myMinute, 0,
                        number, repCode, 0, 0, latitude, longitude, uuID, weekdays, exp, melody, radius, ledColor,
                        code, categoryId);
                db.updateReminderDateTime(idN);
                if (type.startsWith(Constants.TYPE_LOCATION) ||
                        type.startsWith(Constants.TYPE_LOCATION_OUT)){
                    if (myHour > 0 && myMinute > 0){
                        new PositionDelayReceiver().setDelay(context, idN);
                    } else {
                        context.startService(new Intent(context, GeolocationService.class)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                    }
                }
                boolean isCalendar = sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_EXPORT_TO_CALENDAR);
                boolean isStock = sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_EXPORT_TO_STOCK);
                if (type.startsWith(Constants.TYPE_APPLICATION) || type.matches(Constants.TYPE_CALL) ||
                        type.matches(Constants.TYPE_MESSAGE) || type.matches(Constants.TYPE_REMINDER) ||
                        type.startsWith(Constants.TYPE_SKYPE)){
                    long startTime = ReminderUtils.getTime(myDay, myMonth, myYear, myHour, myMinute, 0);
                    if (exp == 1 && isCalendar || isStock)
                        ReminderUtils.exportToCalendar(context, text, startTime, idN, isCalendar, isStock);
                    if (new GTasksHelper(context).isLinked() && code == Constants.SYNC_GTASKS_ONLY ||
                            code == Constants.SYNC_ALL){
                        ReminderUtils.exportToTasks(context, text, startTime, idN);
                    }
                    new AlarmReceiver().setAlarm(context, idN);
                }
                if (type.startsWith(Constants.TYPE_WEEKDAY)){
                    long startTime = ReminderUtils.getWeekTime(myHour, myMinute, weekdays);
                    if (exp == 1 && isCalendar || isStock)
                        ReminderUtils.exportToCalendar(context, text, startTime, idN, isCalendar, isStock);
                    if (new GTasksHelper(context).isLinked() && code == Constants.SYNC_GTASKS_ONLY ||
                            code == Constants.SYNC_ALL){
                        ReminderUtils.exportToTasks(context, text, startTime, idN);
                    }
                    new WeekDayReceiver().setAlarm(context, idN);
                }
                if (type.startsWith(Constants.TYPE_MONTHDAY)){
                    long startTime = ReminderUtils.getMonthTime(myHour, myMinute, myDay);
                    if (exp == 1 && isCalendar || isStock)
                        ReminderUtils.exportToCalendar(context, text, startTime, idN, isCalendar, isStock);
                    if (new GTasksHelper(context).isLinked() && code == Constants.SYNC_GTASKS_ONLY ||
                            code == Constants.SYNC_ALL){
                        ReminderUtils.exportToTasks(context, text, startTime, idN);
                    }
                    new MonthDayReceiver().setAlarm(context, idN);
                }
            }
        }
        if (c != null) c.close();
        db.close();
        new UpdatesHelper(context).updateWidget();
        new Notifier(context).recreatePermanent();
        Toast.makeText(context, context.getString(R.string.string_reminder_created),
                Toast.LENGTH_SHORT).show();
    }

    public static void disable(long id, Context context){
        DataBase DB = new DataBase(context);
        if (!DB.isOpen()) DB.open();
        NotificationManager mNotifyMgr =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Integer i = (int) (long) id;
        mNotifyMgr.cancel(i);
        DB.setDone(id);
        DB.close();
        new UpdatesHelper(context).updateWidget();
        new Notifier(context).recreatePermanent();
        Toast.makeText(context, context.getString(R.string.archived_result_message),
                Toast.LENGTH_SHORT).show();
        new DisableAsync(context).execute();
    }

    public static void makeArchive(long id, Context context){
        DataBase DB = new DataBase(context);
        if (!DB.isOpen()) DB.open();
        NotificationManager mNotifyMgr =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Integer i = (int) (long) id;
        mNotifyMgr.cancel(i);
        DB.toArchive(id);
        DB.close();
        new AlarmReceiver().cancelAlarm(context, id);
        new WeekDayReceiver().cancelAlarm(context, id);
        new DelayReceiver().cancelAlarm(context, id);
        new PositionDelayReceiver().cancelDelay(context, id);
        new UpdatesHelper(context).updateWidget();
        new Notifier(context).recreatePermanent();
        Toast.makeText(context, context.getString(R.string.archived_result_message),
                Toast.LENGTH_SHORT).show();
        new DisableAsync(context).execute();
    }

    public static void edit(long id, Context context){
        Intent intentId = new Intent(context, ReminderManager.class);
        intentId.putExtra(Constants.EDIT_ID, id);
        new AlarmReceiver().cancelAlarm(context, id);
        new WeekDayReceiver().cancelAlarm(context, id);
        new MonthDayReceiver().cancelAlarm(context, id);
        new DelayReceiver().cancelAlarm(context, id);
        new PositionDelayReceiver().cancelDelay(context, id);
        context.startActivity(intentId);
        new DisableAsync(context).execute();
    }

    public static void delete(long id, Context context) {
        DataBase db = new DataBase(context);
        if (!db.isOpen()) db.open();
        NotificationManager mNotifyMgr =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Integer i = (int) (long) id;
        mNotifyMgr.cancel(i);
        Cursor c = db.getReminder(id);
        String uuID = null;
        if (c != null && c.moveToFirst()){
            uuID = c.getString(c.getColumnIndex(Constants.COLUMN_TECH_VAR));
        }
        if (c != null) c.close();
        db.deleteReminder(id);
        db.close();
        new CalendarManager(context).deleteEvents(id);
        new DeleteReminderFiles(context, uuID).execute();
        new UpdatesHelper(context).updateWidget();
        new Notifier(context).recreatePermanent();
        new DisableAsync(context).execute();
    }
}
