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

import android.content.Context;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.cloud.GoogleTasks;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.constants.TasksConstants;
import com.cray.software.justreminder.google_tasks.TaskAsync;
import com.cray.software.justreminder.google_tasks.TaskItem;
import com.cray.software.justreminder.google_tasks.TasksHelper;
import com.cray.software.justreminder.helpers.CalendarManager;
import com.cray.software.justreminder.helpers.SharedPrefs;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ReminderUtils {

    /**
     * Add new event from reminder to Calendar.
     * @param context application context.
     * @param summary event summary.
     * @param startTime event start time in milliseconds.
     * @param id reminder identifier.
     * @param calendar flag for exporting to Google Calendar.
     * @param stock flag for exporting to stock Android calendar.
     */
    public static void exportToCalendar(Context context, String summary, long startTime, long id,
                                        boolean calendar, boolean stock){
        if (calendar){
            new CalendarManager(context).addEvent(summary, startTime, id);
        }
        if (stock){
            new CalendarManager(context).addEventToStock(summary, startTime);
        }
    }

    /**
     * Add new task to Google Tasks from reminder.
     * @param context application context.
     * @param summary task summary.
     * @param startTime task start time in milliseconds.
     * @param mId reminder identifier.
     */
    public static void exportToTasks(Context context, String summary, long startTime, long mId){
        TaskItem item = new TaskItem();
        item.setTitle(summary);
        item.setStatus(GoogleTasks.TASKS_NEED_ACTION);
        item.setDueDate(startTime);
        item.setReminderId(mId);
        item.setNotes(context.getString(R.string.from_reminder));
        long localId = TasksHelper.getInstance(context).saveTask(item);
        new TaskAsync(context, summary, null, null, TasksConstants.INSERT_TASK, startTime,
                context.getString(R.string.from_reminder), localId, null).execute();
    }

    /**
     * Get days array for weekday reminder type.
     * @param weekdays weekdays.
     * @return selected weekdays array.
     */
    public static ArrayList<Integer> getRepeatArray(String weekdays){
        ArrayList<Integer> res = new ArrayList<>();
        if (Character.toString(weekdays.charAt(6)).matches(Constants.DAY_CHECK)) res.add(1);
        else res.add(0);
        if (Character.toString(weekdays.charAt(0)).matches(Constants.DAY_CHECK)) res.add(1);
        else res.add(0);
        if (Character.toString(weekdays.charAt(1)).matches(Constants.DAY_CHECK)) res.add(1);
        else res.add(0);
        if (Character.toString(weekdays.charAt(2)).matches(Constants.DAY_CHECK)) res.add(1);
        else res.add(0);
        if (Character.toString(weekdays.charAt(3)).matches(Constants.DAY_CHECK)) res.add(1);
        else res.add(0);
        if (Character.toString(weekdays.charAt(4)).matches(Constants.DAY_CHECK)) res.add(1);
        else res.add(0);
        if (Character.toString(weekdays.charAt(5)).matches(Constants.DAY_CHECK)) res.add(1);
        else res.add(0);
        return res;
    }

    /**
     * Get time in milliseconds for date and timer reminder type.
     * @param day day.
     * @param month month.
     * @param year year.
     * @param hour hour.
     * @param minute MINUTE.
     * @param after time for timer.
     * @return time in mills.
     */
    public static long getTime(int day, int month, int year, int hour, int minute, long after){
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, hour, minute, 0);
        return calendar.getTimeInMillis() + after;
    }

    /**
     * Generate human readable weekdays string for weekday reminder type.
     * @param context application context.
     * @param repCode system weekdays string.
     * @return get selected weekdays string.
     */
    public static String getRepeatString(Context context, List<Integer> repCode){
        StringBuilder sb = new StringBuilder();
        int first = SharedPrefs.getInstance(context).getInt(Prefs.START_DAY);
        if (first == 0) {
            if (repCode.get(0) == Constants.DAY_CHECKED) {
                sb.append(" ");
                sb.append(context.getString(R.string.sun));
            }
        }
        if (repCode.get(1) == Constants.DAY_CHECKED) {
            sb.append(" ");
            sb.append(context.getString(R.string.mon));
        }
        if (repCode.get(2) == Constants.DAY_CHECKED) {
            sb.append(" ");
            sb.append(context.getString(R.string.tue));
        }
        if (repCode.get(3) == Constants.DAY_CHECKED) {
            sb.append(" ");
            sb.append(context.getString(R.string.wed));
        }
        if (repCode.get(4) == Constants.DAY_CHECKED) {
            sb.append(" ");
            sb.append(context.getString(R.string.thu));
        }
        if (repCode.get(5) == Constants.DAY_CHECKED) {
            sb.append(" ");
            sb.append(context.getString(R.string.fri));
        }
        if (repCode.get(6) == Constants.DAY_CHECKED) {
            sb.append(" ");
            sb.append(context.getString(R.string.sat));
        }
        if (first == 1) {
            if (repCode.get(0) == Constants.DAY_CHECKED) {
                sb.append(" ");
                sb.append(context.getString(R.string.sun));
            }
        }

        if (isAllChecked(repCode)){
            return context.getString(R.string.everyday);
        } else return sb.toString();
    }

    public static boolean isAllChecked(List<Integer> repCode) {
        boolean is = true;
        for (int i : repCode) {
            if (i == Constants.DAY_UNCHECKED) {
                is = false;
                break;
            }
        }
        return is;
    }

    /**
     * Generate human readable string for reminder type.
     * @param context application context.
     * @param type reminder type.
     * @return reminder type.
     */
    public static String getTypeString(Context context, String type){
        String res;
        if (type.startsWith(Constants.TYPE_MONTHDAY_CALL) || type.matches(Constants.TYPE_WEEKDAY_CALL) ||
                type.matches(Constants.TYPE_CALL) || type.matches(Constants.TYPE_LOCATION_CALL) ||
                type.matches(Constants.TYPE_LOCATION_OUT_CALL)){
            String init = context.getString(R.string.make_call);
            res = init + " (" + getType(context, type) + ")";
        } else if (type.startsWith(Constants.TYPE_MONTHDAY_MESSAGE) || type.matches(Constants.TYPE_WEEKDAY_MESSAGE) ||
                type.matches(Constants.TYPE_MESSAGE) || type.matches(Constants.TYPE_LOCATION_MESSAGE) ||
                type.matches(Constants.TYPE_LOCATION_OUT_MESSAGE)){
            String init = context.getString(R.string.message);
            res = init + " (" + getType(context, type) + ")";
        } else if (type.matches(Constants.TYPE_SKYPE)){
            String init = context.getString(R.string.skype_call);
            res = init + " (" + getType(context, type) + ")";
        } else if (type.matches(Constants.TYPE_SKYPE_CHAT)){
            String init = context.getString(R.string.skype_chat);
            res = init + " (" + getType(context, type) + ")";
        } else if (type.matches(Constants.TYPE_SKYPE_VIDEO)){
            String init = context.getString(R.string.video_call);
            res = init + " (" + getType(context, type) + ")";
        } else if (type.matches(Constants.TYPE_APPLICATION)){
            String init = context.getString(R.string.application);
            res = init + " (" + getType(context, type) + ")";
        } else if (type.matches(Constants.TYPE_APPLICATION_BROWSER)){
            String init = context.getString(R.string.open_link);
            res = init + " (" + getType(context, type) + ")";
        } else if (type.matches(Constants.TYPE_SHOPPING_LIST)){
            res = context.getString(R.string.shopping_list);
        } else if (type.matches(Constants.TYPE_MAIL)){
            res = context.getString(R.string.e_mail);
        } else {
            String init = context.getString(R.string.reminder);
            res = init + " (" + getType(context, type) + ")";
        }
        return res;
    }

    /**
     * Get human readable string for reminder type.
     * @param context application context.
     * @param type reminder type.
     * @return reminder type.
     */
    public static String getType(Context context, String type){
        String res;
        if (type.startsWith(Constants.TYPE_MONTHDAY)){
            res = context.getString(R.string.day_of_month);
        } else if (type.startsWith(Constants.TYPE_WEEKDAY)){
            res = context.getString(R.string.alarm);
        } else if (type.startsWith(Constants.TYPE_LOCATION)){
            res = context.getString(R.string.location);
        } else if (type.startsWith(Constants.TYPE_LOCATION_OUT)){
            res = context.getString(R.string.place_out);
        } else if (type.matches(Constants.TYPE_TIME)){
            res = context.getString(R.string.timer);
        } else if (type.matches(Constants.TYPE_PLACES)){
            res = context.getString(R.string.places);
        } else {
            res = context.getString(R.string.by_date);
        }
        return res;
    }
}
