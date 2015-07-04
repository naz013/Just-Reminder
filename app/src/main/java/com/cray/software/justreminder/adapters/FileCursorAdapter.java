package com.cray.software.justreminder.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filterable;
import android.widget.TextView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.Interval;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.utils.ReminderUtils;
import com.cray.software.justreminder.utils.TimeUtil;
import com.cray.software.justreminder.utils.Utils;

import java.util.Calendar;
import java.util.Date;

public class FileCursorAdapter extends CursorAdapter implements Filterable {
    LayoutInflater inflater;
    private Cursor c;
    Context context;
    Interval interval;
    SharedPrefs sPrefs;
    ColorSetter cs;

    @SuppressWarnings("deprecation")
    public FileCursorAdapter(Context context, Cursor c) {
        super(context, c);
        this.context = context;
        interval = new Interval(context);
        sPrefs = new SharedPrefs(context);
        cs = new ColorSetter(context);

        inflater = LayoutInflater.from(context);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.c = c;
        c.moveToFirst();
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return inflater.inflate(R.layout.list_item_file, null);
    }

    @Override
    public long getItemId(int position) {
        Cursor cursor = getCursor();
        cursor.moveToPosition(position);
        return cursor.getLong(cursor.getColumnIndex("_id"));
    }

    @Override
    public Object getItem(int position) {
        return super.getItem(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        c.moveToPosition(position);

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item_file, null);
        }

        TextView fileName = (TextView) convertView.findViewById(R.id.fileName);
        TextView lastModified = (TextView) convertView.findViewById(R.id.lastModified);
        TextView task = (TextView) convertView.findViewById(R.id.task);
        TextView type = (TextView) convertView.findViewById(R.id.type);
        TextView number = (TextView) convertView.findViewById(R.id.number);
        TextView date = (TextView) convertView.findViewById(R.id.date);
        TextView time = (TextView) convertView.findViewById(R.id.time);
        TextView repeat = (TextView) convertView.findViewById(R.id.repeat);
        CardView card = (CardView) convertView.findViewById(R.id.card);
        card.setCardBackgroundColor(cs.getCardStyle());

        String title = c.getString(c.getColumnIndex(Constants.COLUMN_TEXT));
        String fileNameS = c.getString(c.getColumnIndex(Constants.FilesConstants.COLUMN_FILE_NAME));
        String typeS = c.getString(c.getColumnIndex(Constants.COLUMN_TYPE));
        String numberS = c.getString(c.getColumnIndex(Constants.COLUMN_NUMBER));
        String weekdays = c.getString(c.getColumnIndex(Constants.COLUMN_WEEKDAYS));
        int hour = c.getInt(c.getColumnIndex(Constants.COLUMN_HOUR));
        int minute = c.getInt(c.getColumnIndex(Constants.COLUMN_MINUTE));
        int day = c.getInt(c.getColumnIndex(Constants.COLUMN_DAY));
        int month = c.getInt(c.getColumnIndex(Constants.COLUMN_MONTH));
        int year = c.getInt(c.getColumnIndex(Constants.COLUMN_YEAR));
        int repCode = c.getInt(c.getColumnIndex(Constants.COLUMN_REPEAT));
        long repTime = c.getLong(c.getColumnIndex(Constants.COLUMN_REMIND_TIME));
        long lastModifiedS = c.getLong(c.getColumnIndex(Constants.FilesConstants.COLUMN_FILE_LAST_EDIT));
        double lat = c.getDouble(c.getColumnIndex(Constants.COLUMN_LATITUDE));
        double longi = c.getDouble(c.getColumnIndex(Constants.COLUMN_LONGITUDE));

        int pos = fileNameS.lastIndexOf(".");
        String fileN = fileNameS.substring(0, pos);
        fileName.setText(fileN);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(lastModifiedS);
        Date date1 = calendar.getTime();

        boolean is24 = sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_IS_24_TIME_FORMAT);

        lastModified.setText(TimeUtil.getDateTime(date1, is24));
        task.setText(title);
        number.setText("");
        repeat.setText("");

        if (typeS.matches(Constants.TYPE_REMINDER)){
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, day);
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);

            Date mTime = calendar.getTime();
            date.setText(TimeUtil.dateFormat.format(mTime));
            time.setText(TimeUtil.getTime(mTime, is24));
            repeat.setText(interval.getInterval(repCode));

            type.setText(context.getString(R.string.reminder_type));
        } else if (typeS.matches(Constants.TYPE_TIME)){
            time.setText("");
            date.setText(TimeUtil.generateAfterString(repTime));

            repeat.setText(interval.getTimeInterval(repCode));

            type.setText(context.getString(R.string.reminder_type));
        } else if (typeS.matches(Constants.TYPE_WEEKDAY)){
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);

            time.setText(TimeUtil.getTime(calendar.getTime(), is24));

            if (weekdays.length() == 7) {
                date.setText(ReminderUtils.getRepeatString(context, weekdays));
            }

            type.setText(context.getString(R.string.reminder_type));
        } else if (typeS.matches(Constants.TYPE_LOCATION) || typeS.matches(Constants.TYPE_LOCATION_OUT)){
            date.setText(String.valueOf(lat));
            time.setText(String.valueOf(longi));

            type.setText(context.getString(R.string.reminder_type));
        } else if (typeS.matches(Constants.TYPE_MESSAGE) || typeS.matches(Constants.TYPE_CALL)){
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, day);
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);

            Date mTime = calendar.getTime();
            date.setText(TimeUtil.dateFormat.format(mTime));
            time.setText(TimeUtil.getTime(mTime, is24));

            number.setText(numberS);

            repeat.setText(interval.getInterval(repCode));
        } else if (typeS.matches(Constants.TYPE_WEEKDAY_CALL) || typeS.matches(Constants.TYPE_WEEKDAY_MESSAGE)){
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);

            time.setText(TimeUtil.getTime(calendar.getTime(), is24));

            if (weekdays.length() == 7) {
                date.setText(ReminderUtils.getRepeatString(context, weekdays));
            }

            number.setText(numberS);
        } else if (typeS.matches(Constants.TYPE_LOCATION_MESSAGE) || typeS.matches(Constants.TYPE_LOCATION_CALL) ||
                typeS.matches(Constants.TYPE_LOCATION_OUT_MESSAGE) || typeS.matches(Constants.TYPE_LOCATION_OUT_CALL)){
            date.setText(String.valueOf(lat));
            time.setText(String.valueOf(longi));

            number.setText(numberS);
        } else if (typeS.startsWith(Constants.TYPE_SKYPE)){
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, day);
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);

            Date mTime = calendar.getTime();
            date.setText(TimeUtil.dateFormat.format(mTime));
            time.setText(TimeUtil.getTime(mTime, is24));
            repeat.setText(interval.getInterval(repCode));

            repeat.setText(interval.getInterval(repCode));
            if (typeS.matches(Constants.TYPE_SKYPE)){
                type.setText(context.getString(R.string.skype_call_type_title));
            } else if (typeS.matches(Constants.TYPE_SKYPE_VIDEO)){
                type.setText(context.getString(R.string.skype_video_type_title));
            } else if (typeS.matches(Constants.TYPE_SKYPE_CHAT)){
                type.setText(context.getString(R.string.skype_chat_type_title));
            }
            number.setText(numberS);
        } else if (typeS.matches(Constants.TYPE_APPLICATION)){
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, day);
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);

            Date mTime = calendar.getTime();
            date.setText(TimeUtil.dateFormat.format(mTime));
            time.setText(TimeUtil.getTime(mTime, is24));
            repeat.setText(interval.getInterval(repCode));

            repeat.setText(interval.getInterval(repCode));
            type.setText(context.getString(R.string.reminder_type_application));
            number.setText(numberS);
        } else if (typeS.matches(Constants.TYPE_APPLICATION_BROWSER)){
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, day);
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);

            Date mTime = calendar.getTime();
            date.setText(TimeUtil.dateFormat.format(mTime));
            time.setText(TimeUtil.getTime(mTime, is24));
            repeat.setText(interval.getInterval(repCode));

            repeat.setText(interval.getInterval(repCode));
            type.setText(context.getString(R.string.reminder_type_open_link));
            number.setText(numberS);
        }

        if (typeS.endsWith(Constants.TYPE_MESSAGE)){
            type.setText(context.getString(R.string.reminder_send_message));
        } else if (typeS.endsWith(Constants.TYPE_CALL)){
            type.setText(context.getString(R.string.reminder_make_call));
        }

        return convertView;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

    }
}