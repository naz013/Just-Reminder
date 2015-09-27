package com.cray.software.justreminder.reminder;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.widget.CheckBox;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.async.TaskAsync;
import com.cray.software.justreminder.cloud.GTasksHelper;
import com.cray.software.justreminder.databases.TasksData;
import com.cray.software.justreminder.helpers.CalendarManager;
import com.cray.software.justreminder.helpers.TimeCount;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.interfaces.TasksConstants;

import java.util.ArrayList;
import java.util.Calendar;

public class ReminderUtils {

    Context context;

    public ReminderUtils(){}

    public ReminderUtils(Context context){
        this.context = context;
    }

    public static void exportToCalendar(Context context, String summary, long startTime, long id,
                                        boolean calendar, boolean stock){
        if (calendar){
            new CalendarManager(context).addEvent(summary, startTime, id);
        }
        if (stock){
            new CalendarManager(context).addEventToStock(summary, startTime);
        }
    }

    public static void exportToTasks(Context context, String summary, long startTime, long mId){
        long localId = new TasksData(context).addTask(summary, null, 0, false, startTime,
                null, null, context.getString(R.string.string_task_from_just_reminder),
                null, null, null, 0, mId, null, GTasksHelper.TASKS_NEED_ACTION, false);
        new TaskAsync(context, summary, null, null, TasksConstants.INSERT_TASK, startTime,
                context.getString(R.string.string_task_from_just_reminder), localId).execute();
    }

    public static boolean isSdPresent() {
        return Environment.getExternalStorageState() != null;
    }

    public static boolean isSkypeClientInstalled(Context context) {
        PackageManager myPackageMgr = context.getPackageManager();
        try {
            myPackageMgr.getPackageInfo("com.skype.raider", PackageManager.GET_ACTIVITIES);
        }
        catch (PackageManager.NameNotFoundException e) {
            return (false);
        }
        return (true);
    }

    public static int getSyncCode(CheckBox tasks){
        if (tasks.isChecked()) return Constants.SYNC_GTASKS_ONLY;
        else return Constants.SYNC_NO;
    }

    public static ArrayList<Integer> getRepeatArray(String weekdays){
        ArrayList<Integer> res = new ArrayList<>();
        if (Character.toString(weekdays.charAt(6)).matches(Constants.DAY_CHECKED))res.add(1);
        else res.add(0);
        if (Character.toString(weekdays.charAt(0)).matches(Constants.DAY_CHECKED))res.add(1);
        else res.add(0);
        if (Character.toString(weekdays.charAt(1)).matches(Constants.DAY_CHECKED))res.add(1);
        else res.add(0);
        if (Character.toString(weekdays.charAt(2)).matches(Constants.DAY_CHECKED))res.add(1);
        else res.add(0);
        if (Character.toString(weekdays.charAt(3)).matches(Constants.DAY_CHECKED))res.add(1);
        else res.add(0);
        if (Character.toString(weekdays.charAt(4)).matches(Constants.DAY_CHECKED))res.add(1);
        else res.add(0);
        if (Character.toString(weekdays.charAt(5)).matches(Constants.DAY_CHECKED))res.add(1);
        else res.add(0);
        return res;
    }

    public static long getWeekTime(int hour, int minute, String weekdays){
        return TimeCount.getNextWeekdayTime(hour, minute, weekdays, 0);
    }

    public static long getMonthTime(int hour, int minute, int day){
        return TimeCount.getNextMonthDayTime(hour, minute, day, 0);
    }

    public static long getTime(int day, int month, int year, int hour, int minute, long after){
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, hour, minute);
        return calendar.getTimeInMillis() + after;
    }

    public static String getRepeatString(Context context, String repCode){
        String res;
        StringBuilder sb = new StringBuilder();
        if (Character.toString(repCode.charAt(0)).matches(Constants.DAY_CHECKED)){
            sb.append(context.getString(R.string.weekday_monday));
            sb.append(",");
        }
        if (Character.toString(repCode.charAt(1)).matches(Constants.DAY_CHECKED)){
            sb.append(context.getString(R.string.weekday_tuesday));
            sb.append(",");
        }
        if (Character.toString(repCode.charAt(2)).matches(Constants.DAY_CHECKED)){
            sb.append(context.getString(R.string.weekday_wednesday));
            sb.append(",");
        }
        if (Character.toString(repCode.charAt(3)).matches(Constants.DAY_CHECKED)){
            sb.append(context.getString(R.string.weekday_thursday));
            sb.append(",");
        }
        if (Character.toString(repCode.charAt(4)).matches(Constants.DAY_CHECKED)){
            sb.append(context.getString(R.string.weekday_friday));
            sb.append(",");
        }
        if (Character.toString(repCode.charAt(5)).matches(Constants.DAY_CHECKED)){
            sb.append(context.getString(R.string.weekday_saturday));
            sb.append(",");
        }
        if (Character.toString(repCode.charAt(6)).matches(Constants.DAY_CHECKED)){
            sb.append(context.getString(R.string.weekday_sunday));
        }
        if (repCode.matches(Constants.ALL_CHECKED)){
            res = context.getString(R.string.interval_day);
        } else res = sb.toString();
        return res;
    }

    public static String getTypeString(Context context, String type){
        String res;
        if (type.startsWith(Constants.TYPE_MONTHDAY_CALL) || type.matches(Constants.TYPE_WEEKDAY_CALL) ||
                type.matches(Constants.TYPE_CALL) || type.matches(Constants.TYPE_LOCATION_CALL) ||
                type.matches(Constants.TYPE_LOCATION_OUT_CALL)){
            String init = context.getString(R.string.reminder_make_call);
            res = init + " (" + getType(context, type) + ")";
        } else if (type.startsWith(Constants.TYPE_MONTHDAY_MESSAGE) || type.matches(Constants.TYPE_WEEKDAY_MESSAGE) ||
                type.matches(Constants.TYPE_MESSAGE) || type.matches(Constants.TYPE_LOCATION_MESSAGE) ||
                type.matches(Constants.TYPE_LOCATION_OUT_MESSAGE)){
            String init = context.getString(R.string.reminder_send_message);
            res = init + " (" + getType(context, type) + ")";
        } else if (type.matches(Constants.TYPE_SKYPE)){
            String init = context.getString(R.string.skype_call_type_title);
            res = init + " (" + getType(context, type) + ")";
        } else if (type.matches(Constants.TYPE_SKYPE_CHAT)){
            String init = context.getString(R.string.skype_chat_type_title);
            res = init + " (" + getType(context, type) + ")";
        } else if (type.matches(Constants.TYPE_SKYPE_VIDEO)){
            String init = context.getString(R.string.skype_video_type_title);
            res = init + " (" + getType(context, type) + ")";
        } else if (type.matches(Constants.TYPE_APPLICATION)){
            String init = context.getString(R.string.reminder_type_application);
            res = init + " (" + getType(context, type) + ")";
        } else if (type.matches(Constants.TYPE_APPLICATION_BROWSER)){
            String init = context.getString(R.string.reminder_type_open_link);
            res = init + " (" + getType(context, type) + ")";
        } else {
            String init = context.getString(R.string.reminder_type);
            res = init + " (" + getType(context, type) + ")";
        }
        return res;
    }

    public static String getType(Context context, String type){
        String res;
        if (type.startsWith(Constants.TYPE_MONTHDAY)){
            res = context.getString(R.string.string_by_day_of_month);
        } else if (type.startsWith(Constants.TYPE_WEEKDAY)){
            res = context.getString(R.string.by_weekdays_title);
        } else if (type.startsWith(Constants.TYPE_LOCATION)){
            res = context.getString(R.string.by_location_title);
        } else if (type.startsWith(Constants.TYPE_LOCATION_OUT)){
            res = context.getString(R.string.string_place_out);
        } else if (type.matches(Constants.TYPE_TIME)){
            res = context.getString(R.string.after_time_title);
        } else {
            res = context.getString(R.string.by_date_title);
        }
        return res;
    }
}
