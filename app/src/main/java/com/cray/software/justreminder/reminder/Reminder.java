package com.cray.software.justreminder.reminder;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.ReminderManager;
import com.cray.software.justreminder.async.BackupTask;
import com.cray.software.justreminder.async.DeleteReminderFiles;
import com.cray.software.justreminder.async.DisableAsync;
import com.cray.software.justreminder.cloud.GTasksHelper;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.helpers.CalendarManager;
import com.cray.software.justreminder.helpers.Messages;
import com.cray.software.justreminder.helpers.Notifier;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.helpers.TimeCount;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.services.AlarmReceiver;
import com.cray.software.justreminder.services.CheckPosition;
import com.cray.software.justreminder.services.DelayReceiver;
import com.cray.software.justreminder.services.GeolocationService;
import com.cray.software.justreminder.services.MonthDayReceiver;
import com.cray.software.justreminder.services.PositionDelayReceiver;
import com.cray.software.justreminder.services.RepeatNotificationReceiver;
import com.cray.software.justreminder.services.WeekDayReceiver;
import com.cray.software.justreminder.utils.LocationUtil;
import com.cray.software.justreminder.widgets.utils.UpdatesHelper;
import com.cray.software.justreminder.fragments.NavigationDrawerFragment;

import java.util.Calendar;

/**
 * Helper class for interaction with reminders.
 */
public class Reminder {

    private String title, type, uuId, number, weekdays, melody, categoryId, exclusion;
    private int day, month, year, hour, minute, seconds, repCode, export,
            radius, color, code, vibration, voice, notificationRepeat, wake, unlock, auto;
    private long id, repMinute, due, count, limit;
    private double[] place;

    public Reminder(long id, String title, String type, String weekdays, String melody, String categoryId,
                    String uuId, double[] place, String number, int day, int month, int year,
                    int hour, int minute, int seconds, int repCode, int export, int radius,
                    int color, int code, long repMinute, long due, long count, int vibration, int voice,
                    int notificationRepeat, int wake, int unlock, int auto, long limit, String exclusion){
        this.id = id;
        this.title = title;
        this.type = type;
        this.weekdays = weekdays;
        this.melody = melody;
        this.categoryId = categoryId;
        this.uuId = uuId;
        this.place = place;
        this.number = number;
        this.day = day;
        this.month = month;
        this.year = year;
        this.hour = hour;
        this.minute = minute;
        this.seconds = seconds;
        this.repCode = repCode;
        this.export = export;
        this.radius = radius;
        this.color = color;
        this.code = code;
        this.repMinute = repMinute;
        this.due = due;
        this.count = count;
        this.vibration = vibration;
        this.voice = voice;
        this.notificationRepeat = notificationRepeat;
        this.wake = wake;
        this.unlock = unlock;
        this.auto = auto;
        this.limit = limit;
        this.exclusion = exclusion;
    }

    /**
     * Add next event to calendars.
     * @param id reminder identifier.
     * @param context application context.
     */
    public static void generateToCalendar(long id, Context context){
        DataBase db = new DataBase(context);
        db.open();
        Cursor c = db.getReminder(id);
        if (c != null && c.moveToFirst()){
            String text = "";
            String type = "";
            String weekdays = "";
            int hour = 0;
            int minute = 0;
            int day = 0;
            int exp = 0;
            Cursor t = db.getReminder(id);
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
            boolean isCalendar = sPrefs.loadBoolean(Prefs.EXPORT_TO_CALENDAR);
            boolean isStock = sPrefs.loadBoolean(Prefs.EXPORT_TO_STOCK);
            if (type.startsWith(Constants.TYPE_MONTHDAY))
                nextDate = TimeCount.getNextMonthDayTime(hour, minute, day, 0);

            if ((isCalendar || isStock) && exp == 1) {
                ReminderUtils.exportToCalendar(context, text, nextDate, id, isCalendar, isStock);
            }
        }
        if (c != null) c.close();
        db.close();
    }

    /**
     * Make backup files for all data.
     * @param context application context.
     */
    public static void backup(Context context){
        if (new SharedPrefs(context).loadBoolean(Prefs.AUTO_BACKUP)){
            new BackupTask(context).execute();
        }
    }

    /**
     * Toggle reminder status.
     * @param id reminder identifier.
     * @param context application context.
     * @return boolean
     */
    public static boolean toggle(long id, Context context, NavigationDrawerFragment.NavigationDrawerCallbacks callbacks){
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
            disableReminder(id, context);
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
                if (!LocationUtil.checkLocationEnable(context)){
                    LocationUtil.showLocationAlert(context);
                    res = false;
                } else {
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
                }
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
                            .isNext(year, month, day, hour, minute, seconds, repTime, repCode, repCount)) {
                        db.setUnDone(id);
                        db.updateReminderDateTime(id);
                        new AlarmReceiver().setAlarm(context, id);
                        res = true;
                    } else {
                        res = false;
                        if (callbacks != null) callbacks.showSnackbar(R.string.edit_reminder_toast);
                        else Messages.toast(context, R.string.edit_reminder_toast);
                    }
                }
            }
        }
        db.close();
        new Notifier(context).recreatePermanent();
        new UpdatesHelper(context).updateWidget();
        return res;
    }

    /**
     * Create copy of reminder.
     * @param id reminder identifier.
     * @param time due time for copy.
     * @param context application context.
     */
    public static void copy(long id, long time, Context context, NavigationDrawerFragment.NavigationDrawerCallbacks callbacks) {
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
                        code, categoryId, null);
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
                boolean isCalendar = sPrefs.loadBoolean(Prefs.EXPORT_TO_CALENDAR);
                boolean isStock = sPrefs.loadBoolean(Prefs.EXPORT_TO_STOCK);
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
        if (callbacks != null) callbacks.showSnackbar(R.string.string_reminder_created);
        else Messages.toast(context, R.string.string_reminder_created);
    }

    /**
     * Disable reminder.
     * @param id reminder identifier.
     * @param context application context.
     */
    public static void disableReminder(long id, Context context){
        DataBase db = new DataBase(context);
        if (!db.isOpen()) db.open();
        db.setDone(id);
        db.close();
        disable(context, id);
    }

    /**
     * Disable all available reminder notifications.
     * @param context application context.
     * @param id reminder identifier.
     */
    private static void disable(Context context, long id) {
        NotificationManager mNotifyMgr =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Integer i = (int) (long) id;
        mNotifyMgr.cancel(i);
        new AlarmReceiver().cancelAlarm(context, i);
        new WeekDayReceiver().cancelAlarm(context, i);
        new MonthDayReceiver().cancelAlarm(context, i);
        new DelayReceiver().cancelAlarm(context, id);
        new RepeatNotificationReceiver().cancelAlarm(context, i);
        new PositionDelayReceiver().cancelDelay(context, i);
        new UpdatesHelper(context).updateWidget();
        new Notifier(context).recreatePermanent();
        new DisableAsync(context).execute();
    }

    /**
     * Move reminder to trash.
     * @param id reminder identifier.
     * @param context application context.
     */
    public static void moveToTrash(long id, Context context, NavigationDrawerFragment.NavigationDrawerCallbacks callbacks){
        DataBase db = new DataBase(context);
        if (!db.isOpen()) db.open();
        db.toArchive(id);
        db.close();
        disable(context, id);
        if (callbacks != null) callbacks.showSnackbar(R.string.archived_result_message);
        else Messages.toast(context, R.string.archived_result_message);
    }

    /**
     * Edit reminder.
     * @param id reminder identifier.
     * @param context application context.
     */
    public static void edit(long id, Context context){
        disable(context, id);
        Intent intentId = new Intent(context, ReminderManager.class);
        intentId.putExtra(Constants.EDIT_ID, id);
        context.startActivity(intentId);
    }

    /**
     * Delete reminder from application.
     * @param id reminder identifier.
     * @param context application context.
     */
    public static void delete(long id, Context context) {
        DataBase db = new DataBase(context);
        if (!db.isOpen()) db.open();
        Cursor c = db.getReminder(id);
        String uuID = null;
        if (c != null && c.moveToFirst()){
            uuID = c.getString(c.getColumnIndex(Constants.COLUMN_TECH_VAR));
        }
        if (c != null) c.close();
        Reminder reminder = new Type(context).getItem(id);
        db.deleteReminder(id);
        if (reminder.getType().matches(Constants.TYPE_SHOPPING_LIST)){
            db.deleteShopItems(id);
        }
        db.close();
        new CalendarManager(context).deleteEvents(id);
        new DeleteReminderFiles(context, uuID).execute();
        disable(context, id);
    }

    /**
     * Update reminders count.
     * @param context application context.
     * @param id reminder identifier.
     */
    public static void updateCount(Context context, long id){
        DataBase db = new DataBase(context);
        db.open();
        Cursor c = db.getReminder(id);
        if (c != null && c.moveToFirst()){
            int repCode = c.getInt(c.getColumnIndex(Constants.COLUMN_REPEAT));
            if (repCode > 0) {
                db.updateReminderCount(id, (c.getLong(c.getColumnIndex(Constants.COLUMN_REMINDERS_COUNT)) + 1));
            }
        }
        if (c != null) c.close();
        db.close();
    }

    /**
     * Set delay for reminder.
     * @param context application context.
     * @param id reminder identifier.
     * @param delay delay for reminder (integer).
     * @param addAlarm flag for enabling delayed reminder.
     */
    public static void setDelay(Context context, long id, int delay, boolean addAlarm){
        DataBase db = new DataBase(context);
        db.open();
        db.setDelay(id, delay);
        db.updateReminderDateTime(id);
        if (addAlarm) new DelayReceiver().setAlarm(context, 1, id, delay);
        db.close();
    }

    /**
     * Update next date and time for reminder.
     * @param context application context.
     * @param id reminder identifier.
     */
    public static void updateDate(Context context, long id){
        DataBase db = new DataBase(context);
        db.open();
        db.updateReminderDateTime(id);
        db.close();
    }

    /**
     * Change reminder group.
     * @param context application context.
     * @param id reminder identifier.
     * @param uuId unique identifier of new reminder group.
     */
    public static void setNewGroup(Context context, long id, String uuId){
        DataBase db = new DataBase(context);
        db.open();
        db.updateReminderGroup(id, uuId);
        db.close();
    }

    public String getExclusion() {
        return exclusion;
    }

    public void setExclusion(String exclusion) {
        this.exclusion = exclusion;
    }

    public long getLimit() {
        return limit;
    }

    public void setLimit(long limit) {
        this.limit = limit;
    }

    public int getAuto() {
        return auto;
    }

    public void setAuto(int auto) {
        this.auto = auto;
    }

    public int getNotificationRepeat() {
        return notificationRepeat;
    }

    public void setNotificationRepeat(int notificationRepeat) {
        this.notificationRepeat = notificationRepeat;
    }

    public int getUnlock() {
        return unlock;
    }

    public void setUnlock(int unlock) {
        this.unlock = unlock;
    }

    public int getVibration() {
        return vibration;
    }

    public void setVibration(int vibration) {
        this.vibration = vibration;
    }

    public int getVoice() {
        return voice;
    }

    public void setVoice(int voice) {
        this.voice = voice;
    }

    public void setWake(int wake) {
        this.wake = wake;
    }

    public int getWake() {
        return wake;
    }

    public String getWeekdays(){
        return weekdays;
    }

    public void setWeekdays(String weekdays){
        this.weekdays = weekdays;
    }

    public String getMelody(){
        return melody;
    }

    public void setMelody(String melody){
        this.melody = melody;
    }

    public String getCategoryId(){
        return categoryId;
    }

    public void setCategoryId(String categoryId){
        this.categoryId = categoryId;
    }

    public int getDay(){
        return day;
    }

    public void setDay(int day){
        this.day = day;
    }

    public int getMonth(){
        return month;
    }

    public void setMonth(int month){
        this.month = month;
    }

    public int getYear(){
        return year;
    }

    public void setYear(int year){
        this.year = year;
    }

    public int getHour(){
        return hour;
    }

    public void setHour(int hour){
        this.hour = hour;
    }

    public int getMinute(){
        return minute;
    }

    public int getSeconds(){
        return seconds;
    }

    public int getRepCode(){
        return repCode;
    }

    public int getExport(){
        return export;
    }

    public void setSeconds(int seconds){
        this.seconds = seconds;
    }

    public void setRepCode(int repCode){
        this.repCode = repCode;
    }

    public void setExport(int export){
        this.export = export;
    }

    public void setRepMinute(int repMinute){
        this.repMinute = repMinute;
    }

    public int getRadius(){
        return radius;
    }

    public void setRadius(int radius){
        this.radius = radius;
    }

    public int getColor(){
        return color;
    }

    public void setColor(int color){
        this.color = color;
    }

    public int getCode(){
        return code;
    }

    public void setCode(int code){
        this.code = code;
    }

    public long getRepMinute(){
        return repMinute;
    }

    public void setMinute(int minute){
        this.minute = minute;
    }

    public long getDue(){
        return due;
    }

    public void setDue(long due){
        this.due = due;
    }

    public double[] getPlace(){
        return place;
    }

    public void  setPlace(double[] place){
        this.place = place;
    }

    public long getId(){
        return id;
    }

    public void setId(long id){
        this.id = id;
    }

    public String getTitle(){
        return title;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public String getType(){
        return type;
    }

    public void setType(String type){
        this.type = type;
    }

    public String getUuId(){
        return uuId;
    }

    public void setUuId(String uuId){
        this.uuId = uuId;
    }

    public String getNumber(){
        return number;
    }

    public void setNumber(String number){
        this.number = number;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public void setRepMinute(long repMinute) {
        this.repMinute = repMinute;
    }
}
