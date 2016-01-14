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
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.databases.NextBase;
import com.cray.software.justreminder.fragments.NavigationDrawerFragment;
import com.cray.software.justreminder.helpers.CalendarManager;
import com.cray.software.justreminder.helpers.Messages;
import com.cray.software.justreminder.helpers.Notifier;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.helpers.TimeCount;
import com.cray.software.justreminder.json.JsonExport;
import com.cray.software.justreminder.json.JsonParser;
import com.cray.software.justreminder.services.AlarmReceiver;
import com.cray.software.justreminder.services.CheckPosition;
import com.cray.software.justreminder.services.DelayReceiver;
import com.cray.software.justreminder.services.GeolocationService;
import com.cray.software.justreminder.services.PositionDelayReceiver;
import com.cray.software.justreminder.services.RepeatNotificationReceiver;
import com.cray.software.justreminder.utils.LocationUtil;
import com.cray.software.justreminder.widgets.utils.UpdatesHelper;

/**
 * Helper class for interaction with reminders.
 */
public class Reminder {

    public Reminder(){
    }

    /**
     * Add next event to calendars.
     * @param id reminder identifier.
     * @param context application context.
     */
    public static void generateToCalendar(long id, Context context){
        NextBase db = new NextBase(context);
        db.open();
        Cursor c = db.getReminder(id);
        if (c != null && c.moveToFirst()){
            String summary = c.getString(c.getColumnIndex(NextBase.SUMMARY));
            long due = c.getLong(c.getColumnIndex(NextBase.EVENT_TIME));
            String json = c.getString(c.getColumnIndex(NextBase.JSON));
            JsonExport jsonExport = new JsonParser(json).getExport();
            int exp = jsonExport.getCalendar();
            SharedPrefs sPrefs = new SharedPrefs(context);
            boolean isCalendar = sPrefs.loadBoolean(Prefs.EXPORT_TO_CALENDAR);
            boolean isStock = sPrefs.loadBoolean(Prefs.EXPORT_TO_STOCK);
            if ((isCalendar || isStock) && exp == 1) {
                ReminderUtils.exportToCalendar(context, summary, due, id, isCalendar, isStock);
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
        NextBase db = new NextBase(context);
        db.open();
        Cursor c = db.getReminder(id);
        String type = null;
        String json = null;
        int isDone = 0;
        long eventTime = 0;
        if (c != null && c.moveToFirst()) {
            type = c.getString(c.getColumnIndex(NextBase.TYPE));
            json = c.getString(c.getColumnIndex(NextBase.JSON));
            isDone = c.getInt(c.getColumnIndex(NextBase.DB_STATUS));
            eventTime = c.getLong(c.getColumnIndex(NextBase.EVENT_TIME));
        }
        if (c != null) c.close();
        boolean res;
        if (isDone == 0){
            disableReminder(id, context);
            res = true;
        } else {
            if (type.startsWith(Constants.TYPE_LOCATION) ||
                    type.startsWith(Constants.TYPE_LOCATION_OUT)) {
                if (!LocationUtil.checkLocationEnable(context)){
                    LocationUtil.showLocationAlert(context);
                    res = false;
                } else {
                    db.setUnDone(id, json);
                    if (eventTime == 0) {
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
                if (new TimeCount(context).isNext(eventTime)) {
                    db.setUnDone(id, json);
                    new AlarmReceiver().enableReminder(context, id);
                    res = true;
                } else {
                    res = false;
                    if (callbacks != null) callbacks.showSnackbar(R.string.edit_reminder_toast);
                    else Messages.toast(context, R.string.edit_reminder_toast);
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
        NextBase db = new NextBase(context);
        SharedPrefs sPrefs = new SharedPrefs(context);
        if (!db.isOpen()) db.open();
        Cursor c = db.getReminder(id);
        if (c != null && c.moveToFirst()){
            String summary = c.getString(c.getColumnIndex(NextBase.SUMMARY));
            String type = c.getString(c.getColumnIndex(NextBase.TYPE));
            String categoryId = c.getString(c.getColumnIndex(NextBase.CATEGORY));
            String json = c.getString(c.getColumnIndex(NextBase.JSON));

            JsonExport jsonExport = new JsonParser(json).getExport();
            int exp = jsonExport.getCalendar();
            int code = jsonExport.getgTasks();

            String uuID = SyncHelper.generateID();
            long idN = db.insertReminder(summary, type, time, uuID, categoryId, json);

            if (type.startsWith(Constants.TYPE_LOCATION) ||
                    type.startsWith(Constants.TYPE_LOCATION_OUT)){
                if (time > 0){
                    new PositionDelayReceiver().setDelay(context, idN);
                } else {
                    context.startService(new Intent(context, GeolocationService.class)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                }
            } else {
                boolean isCalendar = sPrefs.loadBoolean(Prefs.EXPORT_TO_CALENDAR);
                boolean isStock = sPrefs.loadBoolean(Prefs.EXPORT_TO_STOCK);

                if (exp == 1 && isCalendar || isStock)
                    ReminderUtils.exportToCalendar(context, summary, time, idN, isCalendar, isStock);
                if (new GTasksHelper(context).isLinked() && code == Constants.SYNC_GTASKS_ONLY){
                    ReminderUtils.exportToTasks(context, summary, time, idN);
                }
                new AlarmReceiver().enableReminder(context, idN);
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
        NextBase db = new NextBase(context);
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
     * @param callbacks Callbacks for messages.
     */
    public static void moveToTrash(long id, Context context, NavigationDrawerFragment.NavigationDrawerCallbacks callbacks){
        NextBase db = new NextBase(context);
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
        NextBase db = new NextBase(context);
        if (!db.isOpen()) db.open();
        Cursor c = db.getReminder(id);
        String uuID = null;
        if (c != null && c.moveToFirst()){
            uuID = c.getString(c.getColumnIndex(NextBase.UUID));
        }
        if (c != null) c.close();
        db.deleteReminder(id);
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
        NextBase db = new NextBase(context);
        db.open();
        Cursor c = db.getReminder(id);
        if (c != null && c.moveToFirst()){
            String json = c.getString(c.getColumnIndex(NextBase.JSON));
            db.updateCount(id, json);
        }
        if (c != null) c.close();
        db.close();
    }

    /**
     * Update reminders count.
     * @param context application context.
     * @param id reminder identifier.
     */
    public static void skipNext(Context context, long id){
        NextBase db = new NextBase(context);
        db.open();
        Cursor c = db.getReminder(id);
        if (c != null && c.moveToFirst()){
            String json = c.getString(c.getColumnIndex(NextBase.JSON));
            db.updateCount(id, json);
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
        NextBase db = new NextBase(context);
        db.open();
        db.setDelay(id, delay);
        if (addAlarm) new DelayReceiver().setAlarm(context, 1, id, delay);
        db.close();
    }

    /**
     * Update next date and time for reminder.
     * @param context application context.
     * @param id reminder identifier.
     */
    public static void updateDate(Context context, long id){
        NextBase db = new NextBase(context);
        db.open();
        //// TODO: 12.01.2016 Add function for counting next event time.
        db.close();
    }

    /**
     * Change reminder group.
     * @param context application context.
     * @param id reminder identifier.
     * @param uuId unique identifier of new reminder group.
     */
    public static void setNewGroup(Context context, long id, String uuId){
        NextBase db = new NextBase(context);
        db.open();
        db.setGroup(id, uuId);
        db.close();
    }
}
