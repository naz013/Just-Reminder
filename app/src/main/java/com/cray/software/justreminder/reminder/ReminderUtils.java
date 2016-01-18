package com.cray.software.justreminder.reminder;

import android.content.Context;
import android.widget.CheckBox;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.async.TaskAsync;
import com.cray.software.justreminder.cloud.GTasksHelper;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.TasksConstants;
import com.cray.software.justreminder.databases.TasksData;
import com.cray.software.justreminder.helpers.CalendarManager;
import com.cray.software.justreminder.helpers.TimeCount;

import java.util.ArrayList;
import java.util.Calendar;

public class ReminderUtils {

    public ReminderUtils(){}

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
        TasksData data = new TasksData(context);
        data.open();
        long localId = data.addTask(summary, null, 0, false, startTime,
                null, null, context.getString(R.string.from_reminder), null, null, null, 0,
                mId, null, GTasksHelper.TASKS_NEED_ACTION, false);
        data.close();

        new TaskAsync(context, summary, null, null, TasksConstants.INSERT_TASK, startTime,
                context.getString(R.string.from_reminder), localId).execute();
    }

    /**
     * Generate sync code for reminder.
     * @param tasks Checkbox.
     * @return Sync code.
     */
    public static int getSyncCode(CheckBox tasks){
        if (tasks.isChecked()) return Constants.SYNC_GTASKS_ONLY;
        else return Constants.SYNC_NO;
    }

    /**
     * Get days array for weekday reminder type.
     * @param weekdays weekdays.
     * @return selected weekdays array.
     */
    public static ArrayList<Integer> getRepeatArray(String weekdays){
        ArrayList<Integer> res = new ArrayList<>();
        if (Character.toString(weekdays.charAt(6)).matches(Constants.DAY_CHECK))res.add(1);
        else res.add(0);
        if (Character.toString(weekdays.charAt(0)).matches(Constants.DAY_CHECK))res.add(1);
        else res.add(0);
        if (Character.toString(weekdays.charAt(1)).matches(Constants.DAY_CHECK))res.add(1);
        else res.add(0);
        if (Character.toString(weekdays.charAt(2)).matches(Constants.DAY_CHECK))res.add(1);
        else res.add(0);
        if (Character.toString(weekdays.charAt(3)).matches(Constants.DAY_CHECK))res.add(1);
        else res.add(0);
        if (Character.toString(weekdays.charAt(4)).matches(Constants.DAY_CHECK))res.add(1);
        else res.add(0);
        if (Character.toString(weekdays.charAt(5)).matches(Constants.DAY_CHECK))res.add(1);
        else res.add(0);
        return res;
    }

    /**
     * Get time in milliseconds for weekday reminder type.
     * @param hour hour.
     * @param minute MINUTE.
     * @param weekdays weekdays string.
     * @return time in mills.
     */
    public static long getWeekTime(int hour, int minute, ArrayList<Integer> weekdays){
        return TimeCount.getNextWeekdayTime(hour, minute, weekdays, 0);
    }

    /**
     * Get time in milliseconds for MonthDay reminder type.
     * @param hour hour.
     * @param minute MINUTE.
     * @param day day (if 0 get time for last day in month).
     * @return time in mills.
     */
    public static long getMonthTime(int hour, int minute, int day){
        return TimeCount.getNextMonthDayTime(hour, minute, day, 0);
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
    public static String getRepeatString(Context context, ArrayList<Integer> repCode){
        String res;
        StringBuilder sb = new StringBuilder();
        if (repCode.get(0) == Constants.DAY_CHECKED){
            sb.append(context.getString(R.string.mon));
            sb.append(",");
        }
        if (repCode.get(1) == Constants.DAY_CHECKED){
            sb.append(context.getString(R.string.tue));
            sb.append(",");
        }
        if (repCode.get(2) == Constants.DAY_CHECKED){
            sb.append(context.getString(R.string.wed));
            sb.append(",");
        }
        if (repCode.get(3) == Constants.DAY_CHECKED){
            sb.append(context.getString(R.string.thu));
            sb.append(",");
        }
        if (repCode.get(4) == Constants.DAY_CHECKED){
            sb.append(context.getString(R.string.fri));
            sb.append(",");
        }
        if (repCode.get(5) == Constants.DAY_CHECKED){
            sb.append(context.getString(R.string.sat));
            sb.append(",");
        }
        if (repCode.get(6) == Constants.DAY_CHECKED){
            sb.append(context.getString(R.string.sun));
        }
        if (isAllChecked(repCode)){
            res = context.getString(R.string.everyday);
        } else res = sb.toString();
        return res;
    }

    public static boolean isAllChecked(ArrayList<Integer> repCode) {
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
        } else {
            res = context.getString(R.string.by_date);
        }
        return res;
    }
}
