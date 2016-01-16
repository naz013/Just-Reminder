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
import com.cray.software.justreminder.helpers.CalendarManager;
import com.cray.software.justreminder.helpers.Messages;
import com.cray.software.justreminder.helpers.Notifier;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.helpers.TimeCount;
import com.cray.software.justreminder.interfaces.ActionCallbacks;
import com.cray.software.justreminder.json.JsonExport;
import com.cray.software.justreminder.json.JsonModel;
import com.cray.software.justreminder.json.JsonParser;
import com.cray.software.justreminder.json.JsonRecurrence;
import com.cray.software.justreminder.services.AlarmReceiver;
import com.cray.software.justreminder.services.DelayReceiver;
import com.cray.software.justreminder.services.GeolocationService;
import com.cray.software.justreminder.services.PositionDelayReceiver;
import com.cray.software.justreminder.services.RepeatNotificationReceiver;
import com.cray.software.justreminder.utils.LocationUtil;
import com.cray.software.justreminder.utils.SuperUtil;
import com.cray.software.justreminder.widgets.utils.UpdatesHelper;

import java.util.ArrayList;

/**
 * Helper class for interaction with reminders.
 */
public class Reminder {

    public Reminder(){
    }

    public static ArrayList<String> getUuIds(Context context) {
        NextBase db = new NextBase(context);
        db.open();
        ArrayList<String> list = new ArrayList<>();
        Cursor c = db.queryAllReminders();
        if (c != null && c.moveToFirst()) {
            do {
                list.add(c.getString(c.getColumnIndex(NextBase.UUID)));
            } while (c.moveToNext());
        }
        if (c != null) c.close();
        db.close();
        return list;
    }

    public static boolean isUuId(Context context, String uuId) {
        NextBase db = new NextBase(context);
        db.open();
        ArrayList<String> list = new ArrayList<>();
        Cursor c = db.queryAllReminders();
        if (c != null && c.moveToFirst()) {
            do {
                list.add(c.getString(c.getColumnIndex(NextBase.UUID)));
            } while (c.moveToNext());
        }
        if (c != null) c.close();
        db.close();
        return list.contains(uuId);
    }

    public static void update(Context context, long id) {
        NextBase db = new NextBase(context);
        db.open();
        Cursor c = db.getReminder(id);
        if (c != null && c.moveToFirst()){
            String json = c.getString(c.getColumnIndex(NextBase.JSON));
            String summary = c.getString(c.getColumnIndex(NextBase.SUMMARY));
            String type = c.getString(c.getColumnIndex(NextBase.TYPE));
            int delay = c.getInt(c.getColumnIndex(NextBase.DELAY));
            JsonModel jsonModel = new JsonParser(json).parse();
            JsonRecurrence jsonRecurrence = jsonModel.getRecurrence();
            long repeat = jsonRecurrence.getRepeat();
            long limit = jsonRecurrence.getLimit();
            long count = jsonModel.getCount() + 1;
            long eventTime = new TimeCount(context).generateDateTime(type,
                    jsonRecurrence.getMonthday(), jsonModel.getStartTime(),
                    repeat, jsonRecurrence.getWeekdays(), count, delay);
            jsonModel.setEventTime(eventTime);
            if ((repeat == 0 || (limit > 0 && (limit - count - 1 == 0)))  &&
                    !type.startsWith(Constants.TYPE_WEEKDAY) &&
                    !type.contains(Constants.TYPE_MONTHDAY)){
                disableReminder(id, context);
            } else {
                jsonModel.setCount(count);
                db.updateReminderTime(id, eventTime);
                db.setJson(id, new JsonParser().toJsonString(jsonModel));
                int exp = jsonModel.getExport().getCalendar();
                SharedPrefs sPrefs = new SharedPrefs(context);
                boolean isCalendar = sPrefs.loadBoolean(Prefs.EXPORT_TO_CALENDAR);
                boolean isStock = sPrefs.loadBoolean(Prefs.EXPORT_TO_STOCK);
                if ((isCalendar || isStock) && exp == 1) {
                    ReminderUtils.exportToCalendar(context, summary, eventTime, id, isCalendar, isStock);
                }
            }
        }
        if (c != null) c.close();
        db.close();
        backup(context);
    }

    /**
     * Make backup files for all data.
     * @param context application context.
     */
    private static void backup(Context context){
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
    public static boolean toggle(long id, Context context, ActionCallbacks callbacks){
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
            if (type.contains(Constants.TYPE_LOCATION)) {
                if (!LocationUtil.checkLocationEnable(context)){
                    db.close();
                    LocationUtil.showLocationAlert(context, callbacks);
                    return false;
                } else {
                    db.setUnDone(id);
                    if (eventTime <= 0) {
                        if (!SuperUtil.isServiceRunning(context, GeolocationService.class)) {
                            context.startService(new Intent(context, GeolocationService.class)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                        }
                    } else {
                        new PositionDelayReceiver().setDelay(context, id);
                    }
                    res = true;
                }
            } else if(type.matches(Constants.TYPE_TIME)) {
                db.setUnDone(id);
                JsonModel jsonModel = new JsonParser(json).parse();
                long newTime = System.currentTimeMillis() + jsonModel.getRecurrence().getAfter();
                jsonModel.setEventTime(newTime);
                jsonModel.setStartTime(newTime);
                jsonModel.setCount(0);
                db.updateReminderTime(id, newTime);
                db.setJson(id, new JsonParser().toJsonString(jsonModel));
                new AlarmReceiver().enableReminder(context, id);
                res = true;
            } else if (type.contains(Constants.TYPE_MONTHDAY) ||
                    type.contains(Constants.TYPE_WEEKDAY)) {
                db.setUnDone(id);
                JsonModel jsonModel = new JsonParser(json).parse();
                JsonRecurrence jsonRecurrence = jsonModel.getRecurrence();
                long nextTime = new TimeCount(context).generateDateTime(type,
                        jsonRecurrence.getMonthday(), System.currentTimeMillis(),
                        0, jsonRecurrence.getWeekdays(), 0, 0);
                db.updateReminderTime(id, nextTime);
                jsonModel.setEventTime(nextTime);
                db.setJson(id, new JsonParser().toJsonString(jsonModel));
                new AlarmReceiver().enableReminder(context, id);
                res = true;
            } else {
                if (new TimeCount(context).isNext(eventTime)) {
                    db.setUnDone(id);
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
    public static void copy(long id, long time, Context context, ActionCallbacks callbacks) {
        NextBase db = new NextBase(context);
        SharedPrefs sPrefs = new SharedPrefs(context);
        if (!db.isOpen()) db.open();
        Cursor c = db.getReminder(id);
        if (c != null && c.moveToFirst()){
            String summary = c.getString(c.getColumnIndex(NextBase.SUMMARY));
            String type = c.getString(c.getColumnIndex(NextBase.TYPE));
            String categoryId = c.getString(c.getColumnIndex(NextBase.CATEGORY));
            String json = c.getString(c.getColumnIndex(NextBase.JSON));

            JsonParser jsonParser = new JsonParser(json);
            JsonModel jsonModel = jsonParser.parse();
            JsonExport jsonExport = jsonModel.getExport();
            int exp = jsonExport.getCalendar();
            int code = jsonExport.getgTasks();
            jsonModel.setEventTime(time);
            jsonModel.setStartTime(time);
            jsonParser.toJsonString(jsonModel);

            String uuID = SyncHelper.generateID();
            long idN = db.insertReminder(summary, type, time, uuID, categoryId, jsonParser.toJsonString());

            if (type.contains(Constants.TYPE_LOCATION)){
                if (time > 0){
                    new PositionDelayReceiver().setDelay(context, idN);
                } else {
                    if (!SuperUtil.isServiceRunning(context, GeolocationService.class)) {
                        context.startService(new Intent(context, GeolocationService.class)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                    }
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
    public static void moveToTrash(long id, Context context, ActionCallbacks callbacks){
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
    public static void skipNext(Context context, long id){
        NextBase db = new NextBase(context);
        db.open();
        Cursor c = db.getReminder(id);
        if (c != null && c.moveToFirst()){
            String json = c.getString(c.getColumnIndex(NextBase.JSON));
            JsonParser parser = new JsonParser(json);
            long count = parser.getCount();
            parser.setCount(count + 1);
            db.updateCount(id, parser.toJsonString());
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
        update(context, id);
        if (addAlarm) new DelayReceiver().setAlarm(context, id, delay);
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
