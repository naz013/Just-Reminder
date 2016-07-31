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

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.app_widgets.UpdatesHelper;
import com.cray.software.justreminder.async.BackupTask;
import com.cray.software.justreminder.cloud.GTasksHelper;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.datas.models.ShoppingList;
import com.cray.software.justreminder.helpers.CalendarManager;
import com.cray.software.justreminder.helpers.Messages;
import com.cray.software.justreminder.helpers.Notifier;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.helpers.TimeCount;
import com.cray.software.justreminder.interfaces.ActionCallbacks;
import com.cray.software.justreminder.reminder.json.JExport;
import com.cray.software.justreminder.reminder.json.JParser;
import com.cray.software.justreminder.reminder.json.JRecurrence;
import com.cray.software.justreminder.reminder.json.JShopping;
import com.cray.software.justreminder.reminder.json.JsonModel;
import com.cray.software.justreminder.services.AlarmReceiver;
import com.cray.software.justreminder.services.DelayReceiver;
import com.cray.software.justreminder.services.GeolocationService;
import com.cray.software.justreminder.services.PositionDelayReceiver;
import com.cray.software.justreminder.services.RepeatNotificationReceiver;
import com.cray.software.justreminder.utils.LocationUtil;
import com.cray.software.justreminder.utils.SuperUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Helper class for interaction with reminders.
 */
public class Reminder {

    public Reminder(){
    }

    /**
     * Check if reminder unique identifier already present in database.
     * @param context application context.
     * @param uuId unique identifier.
     * @return Boolean
     */
    public static boolean isUuId(Context context, String uuId) {
        ArrayList<String> list = new ArrayList<>();
        for (ReminderItem item : ReminderHelper.getInstance(context).getAll()) {
            list.add(item.getUuId());
        }
        return list.contains(uuId);
    }

    public static void update(Context context, long id) {
        ReminderItem item = ReminderHelper.getInstance(context).getReminder(id);
        if (item != null){
            String summary = item.getSummary();
            String type = item.getType();
            JsonModel model = item.getModel();
            JRecurrence jRecurrence = model.getRecurrence();
            long repeat = jRecurrence.getRepeat();
            long limit = jRecurrence.getLimit();
            long count = model.getCount() + 1;
            if ((repeat == 0 || (limit > 0 && (limit - count - 1 == 0)))  &&
                    !type.startsWith(Constants.TYPE_WEEKDAY) &&
                    !type.contains(Constants.TYPE_MONTHDAY)){
                disableReminder(id, context);
            } else {
                long eventTime = TimeCount.getInstance(context).generateDateTime(type,
                        jRecurrence.getMonthday(), model.getStartTime(),
                        repeat, jRecurrence.getWeekdays(), count, 0);
                if (type.startsWith(Constants.TYPE_MONTHDAY) || type.startsWith(Constants.TYPE_WEEKDAY)) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(eventTime);
                    int hour = calendar.get(Calendar.HOUR_OF_DAY);
                    int minute = calendar.get(Calendar.MINUTE);
                    calendar.setTimeInMillis(System.currentTimeMillis());
                    calendar.set(Calendar.HOUR_OF_DAY, hour);
                    calendar.set(Calendar.MINUTE, minute);
                    eventTime = TimeCount.getInstance(context).generateDateTime(type,
                            jRecurrence.getMonthday(), calendar.getTimeInMillis(),
                            repeat, jRecurrence.getWeekdays(), count, 0);
                }
                model.setEventTime(eventTime);
                model.setCount(count);
                item.setModel(model);
                ReminderHelper.getInstance(context).saveReminder(item);
                int exp = model.getExport().getCalendar();
                new AlarmReceiver().enableReminder(context, id);
                boolean isCalendar = SharedPrefs.getInstance(context).getBoolean(Prefs.EXPORT_TO_CALENDAR);
                boolean isStock = SharedPrefs.getInstance(context).getBoolean(Prefs.EXPORT_TO_STOCK);
                if ((isCalendar || isStock) && exp == 1) {
                    ReminderUtils.exportToCalendar(context, summary, eventTime, id, isCalendar, isStock);
                }
            }
        }
        backup(context);
    }

    /**
     * Make backup files for all data.
     * @param context application context.
     */
    private static void backup(Context context){
        if (SharedPrefs.getInstance(context).getBoolean(Prefs.AUTO_BACKUP)){
            new BackupTask(context).execute();
        }
    }

    /**
     * Toggle reminder status.
     * @param id reminder identifier.
     * @param context application context.
     * @return boolean
     */
    public static boolean toggle(final long id, final Context context, ActionCallbacks callbacks){
        ReminderItem item = ReminderHelper.getInstance(context).getReminder(id);
        boolean res = false;
        if (item != null) {
            if (item.getStatus() == 0) {
                disableReminder(id, context);
                res = true;
            } else {
                if (item.getType().contains(Constants.TYPE_LOCATION)) {
                    if (!LocationUtil.checkLocationEnable(context)) {
                        LocationUtil.showLocationAlert(context, callbacks);
                        return false;
                    } else {
                        ReminderHelper.getInstance(context).setStatus(id, false);
                        if (item.getDateTime() <= 0) {
                            if (!SuperUtil.isServiceRunning(context, GeolocationService.class)) {
                                context.startService(new Intent(context, GeolocationService.class)
                                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                            }
                        } else {
                            new PositionDelayReceiver().setDelay(context, id);
                        }
                        res = true;
                    }
                } else if (item.getType().matches(Constants.TYPE_TIME)) {
                    JsonModel model = item.getModel();
                    long newTime = System.currentTimeMillis() + model.getRecurrence().getAfter();
                    model.setEventTime(newTime);
                    model.setStartTime(newTime);
                    model.setCount(0);
                    item.setModel(model);
                    item.setDateTime(newTime);
                    item.setStatus(ReminderItem.ENABLED);
                    ReminderHelper.getInstance(context).saveReminder(item);
                    new AlarmReceiver().enableReminder(context, id);
                    res = true;
                } else if (item.getType().contains(Constants.TYPE_MONTHDAY) ||
                        item.getType().contains(Constants.TYPE_WEEKDAY)) {
                    JsonModel model = item.getModel();
                    JRecurrence jRecurrence = model.getRecurrence();
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(model.getEventTime());
                    int hour = calendar.get(Calendar.HOUR_OF_DAY);
                    int minute = calendar.get(Calendar.MINUTE);
                    calendar.setTimeInMillis(System.currentTimeMillis());
                    calendar.set(Calendar.HOUR_OF_DAY, hour);
                    calendar.set(Calendar.MINUTE, minute);
                    long nextTime = TimeCount.getInstance(context).generateDateTime(item.getType(),
                            jRecurrence.getMonthday(), calendar.getTimeInMillis(), 0,
                            jRecurrence.getWeekdays(), 0, 0);
                    model.setEventTime(nextTime);
                    item.setDateTime(nextTime);
                    item.setStatus(ReminderItem.ENABLED);
                    item.setModel(model);
                    ReminderHelper.getInstance(context).saveReminder(item);
                    new AlarmReceiver().enableReminder(context, id);
                    res = true;
                } else {
                    if (TimeCount.getInstance(context).isNext(item.getDateTime())) {
                        ReminderHelper.getInstance(context).setStatus(id, false);
                        new AlarmReceiver().enableReminder(context, id);
                        res = true;
                    } else {
                        res = false;
                        if (callbacks != null) {
                            callbacks.showSnackbar(R.string.reminder_is_outdated, R.string.edit, v -> edit(id, context));
                        } else Messages.toast(context, R.string.reminder_is_outdated);
                    }
                }
            }
        }
        new Notifier(context).recreatePermanent();
        UpdatesHelper.getInstance(context).updateWidget();
        return res;
    }

    /**
     * Create copy of reminder.
     * @param id reminder identifier.
     * @param time due time for copy.
     * @param context application context.
     */
    public static void copy(long id, long time, Context context, ActionCallbacks callbacks) {
        ReminderItem item = ReminderHelper.getInstance(context).getReminder(id);
        if (item != null){
            String summary = item.getSummary();
            String type = item.getType();
            long eventTime = item.getDateTime();
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(time);
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);
            calendar.setTimeInMillis(eventTime);
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            eventTime = calendar.getTimeInMillis();
            JsonModel jsonModel = item.getModel();
            JExport jExport = jsonModel.getExport();
            int exp = jExport.getCalendar();
            int code = jExport.getgTasks();
            jsonModel.setEventTime(eventTime);
            jsonModel.setStartTime(eventTime);
            String uuID = SyncHelper.generateID();
            jsonModel.setUuId(uuID);
            item.setDateTime(eventTime);
            item.setModel(jsonModel);
            item.setUuId(uuID);
            item.setId(0);
            if (type.contains(Constants.TYPE_LOCATION)){
                long ids = ReminderHelper.getInstance(context).saveReminder(item);
                if (eventTime > 0){
                    new PositionDelayReceiver().setDelay(context, ids);
                } else {
                    if (!SuperUtil.isServiceRunning(context, GeolocationService.class)) {
                        context.startService(new Intent(context, GeolocationService.class)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                    }
                }
            } else if (type.contains(Constants.TYPE_MONTHDAY) || type.contains(Constants.TYPE_WEEKDAY)) {
                JRecurrence jRecurrence = jsonModel.getRecurrence();
                long nextTime = TimeCount.getInstance(context).generateDateTime(type,
                        jRecurrence.getMonthday(), time, 0, jRecurrence.getWeekdays(), 0, 0);
                item.setDateTime(nextTime);
                jsonModel.setEventTime(nextTime);
                item.setModel(jsonModel);
                long ids = ReminderHelper.getInstance(context).saveReminder(item);
                new AlarmReceiver().enableReminder(context, ids);
            } else {
                long ids = ReminderHelper.getInstance(context).saveReminder(item);
                boolean isCalendar = SharedPrefs.getInstance(context).getBoolean(Prefs.EXPORT_TO_CALENDAR);
                boolean isStock = SharedPrefs.getInstance(context).getBoolean(Prefs.EXPORT_TO_STOCK);
                if (exp == 1 && isCalendar || isStock)
                    ReminderUtils.exportToCalendar(context, summary, time, ids, isCalendar, isStock);
                if (new GTasksHelper(context).isLinked() && code == Constants.SYNC_GTASKS_ONLY){
                    ReminderUtils.exportToTasks(context, summary, time, ids);
                }
                new AlarmReceiver().enableReminder(context, ids);
            }
        }
        UpdatesHelper.getInstance(context).updateWidget();
        new Notifier(context).recreatePermanent();
        if (callbacks != null) callbacks.showSnackbar(R.string.reminder_created);
        else Messages.toast(context, R.string.reminder_created);
    }

    /**
     * Disable reminder.
     * @param id reminder identifier.
     * @param context application context.
     */
    public static void disableReminder(long id, Context context){
        ReminderHelper.getInstance(context).setStatus(id, true);
        disable(context, id);
    }

    /**
     * Disable all available reminder notifications.
     * @param context application context.
     * @param id reminder identifier.
     */
    public static void disable(Context context, long id) {
        NotificationManager mNotifyMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Integer i = (int) (long) id;
        mNotifyMgr.cancel(i);
        new AlarmReceiver().cancelAlarm(context, i);
        new DelayReceiver().cancelAlarm(context, id);
        new RepeatNotificationReceiver().cancelAlarm(context, i);
        new PositionDelayReceiver().cancelDelay(context, i);
        UpdatesHelper.getInstance(context).updateWidget();
        new Notifier(context).recreatePermanent();
        new DisableAsync(context).execute();
    }

    /**
     * Move reminder to trash.
     * @param id reminder identifier.
     * @param context application context.
     */
    public static void moveToTrash(long id, Context context, ActionCallbacks callbacks){
        ReminderHelper.getInstance(context).moveToArchive(id);
        disable(context, id);
        if (callbacks != null) callbacks.showSnackbar(R.string.moved_to_trash);
    }

    /**
     * Edit reminder.
     * @param id reminder identifier.
     * @param context application context.
     */
    public static void edit(long id, Context context){
        disable(context, id);
        Intent intent = new Intent(context, ReminderManager.class);
        intent.putExtra(Constants.EDIT_ID, id);
        context.startActivity(intent);
    }

    /**
     * Delete reminder from application.
     * @param id reminder identifier.
     * @param context application context.
     */
    public static void delete(long id, Context context) {
        ReminderItem item = ReminderHelper.getInstance(context).deleteReminder(id);
        new CalendarManager(context).deleteEvents(id);
        new DeleteReminderFiles(context, item.getUuId()).execute();
        disable(context, id);
    }

    /**
     * Update reminders count.
     * @param context application context.
     * @param id reminder identifier.
     */
    public static void skipNext(Context context, long id){
        ReminderItem item = ReminderHelper.getInstance(context).getReminder(id);
        if (item != null){
            long count = item.getModel().getCount();
            item.getModel().setCount(count + 1);
            ReminderHelper.getInstance(context).saveReminder(item);
        }
    }

    /**
     * Set delay for reminder.
     * @param context application context.
     * @param id reminder identifier.
     * @param delay delay for reminder (integer).
     * @param addAlarm flag for enabling delayed reminder.
     */
    public static void setDelay(Context context, long id, int delay, boolean addAlarm){
        ReminderItem item = ReminderHelper.getInstance(context).getReminder(id);
        if (item != null){
            item.setDelay(delay);
            String summary = item.getSummary();
            String type = item.getType();
            JsonModel model = item.getModel();
            JRecurrence jRecurrence = model.getRecurrence();
            long repeat = jRecurrence.getRepeat();
            long limit = jRecurrence.getLimit();
            long count = model.getCount() + 1;
            if ((repeat == 0 || (limit > 0 && (limit - count - 1 == 0)))  &&
                    !type.startsWith(Constants.TYPE_WEEKDAY) &&
                    !type.contains(Constants.TYPE_MONTHDAY) && delay == 0){
                disableReminder(id, context);
            } else {
                long eventTime = TimeCount.getInstance(context).generateDateTime(type,
                        jRecurrence.getMonthday(), model.getStartTime(),
                        repeat, jRecurrence.getWeekdays(), count, delay);
                if (type.startsWith(Constants.TYPE_MONTHDAY) || type.startsWith(Constants.TYPE_WEEKDAY)) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(eventTime);
                    int hour = calendar.get(Calendar.HOUR_OF_DAY);
                    int minute = calendar.get(Calendar.MINUTE);
                    calendar.setTimeInMillis(System.currentTimeMillis());
                    calendar.set(Calendar.HOUR_OF_DAY, hour);
                    calendar.set(Calendar.MINUTE, minute);
                    eventTime = TimeCount.getInstance(context).generateDateTime(type,
                            jRecurrence.getMonthday(), calendar.getTimeInMillis(),
                            repeat, jRecurrence.getWeekdays(), count, delay);
                }
                model.setCount(count);
                model.setEventTime(eventTime);
                item.setDateTime(eventTime);
                item.setModel(model);
                ReminderHelper.getInstance(context).saveReminder(item);
                int exp = model.getExport().getCalendar();
                new AlarmReceiver().enableReminder(context, id);
                boolean isCalendar = SharedPrefs.getInstance(context).getBoolean(Prefs.EXPORT_TO_CALENDAR);
                boolean isStock = SharedPrefs.getInstance(context).getBoolean(Prefs.EXPORT_TO_STOCK);
                if ((isCalendar || isStock) && exp == 1) {
                    ReminderUtils.exportToCalendar(context, summary, eventTime, id, isCalendar, isStock);
                }
            }
        }
        if (addAlarm) new DelayReceiver().setAlarm(context, id, delay);
    }

    /**
     * Change reminder group.
     * @param context application context.
     * @param id reminder identifier.
     * @param uuId unique identifier of new reminder group.
     */
    public static void setNewGroup(Context context, long id, String uuId){
        ReminderHelper.getInstance(context).changeGroup(id, uuId);
    }

    /**
     * Mark task in shopping list done/undone.
     * @param context application context.
     * @param id task identifier.
     * @param checked task status.
     * @param uuId shopping item unique identifier.
     */
    public static void switchItem(Context context, long id, boolean checked, String uuId){
        ReminderItem item = ReminderHelper.getInstance(context).getReminder(id);
        if (item != null) {
            JsonModel jsonModel = item.getModel();
            List<JShopping> shoppings = jsonModel.getShoppings();
            for (JShopping shopping : shoppings) {
                if (shopping.getUuId().matches(uuId)) {
                    shopping.setStatus(checked ? 1 : 0);
                    break;
                }
            }
            jsonModel.setShoppings(shoppings);
            item.setModel(jsonModel);
            ReminderHelper.getInstance(context).saveReminder(item);
        }
    }

    /**
     * Hide task from shopping list.
     * @param context application context.
     * @param id task identifier.
     */
    public static void hideItem(Context context, long id, String uuId){
        ReminderItem item = ReminderHelper.getInstance(context).getReminder(id);
        if (item != null) {
            JsonModel jsonModel = item.getModel();
            List<JShopping> shoppings = jsonModel.getShoppings();
            for (JShopping shopping : shoppings) {
                if (shopping.getUuId().matches(uuId)) {
                    shopping.setDeleted(ShoppingList.DELETED);
                    break;
                }
            }
            jsonModel.setShoppings(shoppings);
            item.setModel(jsonModel);
            ReminderHelper.getInstance(context).saveReminder(item);
        }
    }

    /**
     * Show task in shopping list.
     * @param context application context.
     * @param id task identifier.
     */
    public static void showItem(Context context, long id, String uuId){
        ReminderItem item = ReminderHelper.getInstance(context).getReminder(id);
        if (item != null) {
            JsonModel jsonModel = item.getModel();
            List<JShopping> shoppings = jsonModel.getShoppings();
            for (JShopping shopping : shoppings) {
                if (shopping.getUuId().matches(uuId)) {
                    shopping.setDeleted(ShoppingList.ACTIVE);
                    break;
                }
            }
            jsonModel.setShoppings(shoppings);
            item.setModel(jsonModel);
            ReminderHelper.getInstance(context).saveReminder(item);
        }
    }

    /**
     * Remove task from database.
     * @param context application context.
     * @param id task identifier.
     * @param uuId shop item unique identifier.
     */
    public static void removeItem(Context context, long id, String uuId){
        ReminderItem item = ReminderHelper.getInstance(context).getReminder(id);
        if (item != null) {
            JsonModel jsonModel = item.getModel();
            List<JShopping> shoppings = jsonModel.getShoppings();
            for (int i = 0; i < shoppings.size(); i++) {
                if (shoppings.get(i).getUuId().matches(uuId)) {
                    shoppings.remove(i);
                    break;
                }
            }
            jsonModel.setShoppings(shoppings);
            item.setModel(jsonModel);
            ReminderHelper.getInstance(context).saveReminder(item);
        }
    }

    /**
     * Get all tasks unique identifiers.
     * @param context application context.
     * @return Map with unique identifier as key and database identifier as value
     */
    public static List<String> getUuIds(Context context, long id){
        List<String> ids = new ArrayList<>();
        ReminderItem item = ReminderHelper.getInstance(context).getReminder(id);
        if (item != null) {
            ids = new JParser(item.getJson()).getShoppingKeys();
        }
        return ids;
    }
}
