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
import com.cray.software.justreminder.helpers.Utils;
import com.cray.software.justreminder.interfaces.Constants;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class FileCursorAdapter extends CursorAdapter implements Filterable {

    TextView fileName, lastModified, task, type, number, date, time, repeat;
    LayoutInflater inflater;
    private Cursor c;
    Context cContext;
    Interval interval;
    SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm");
    SimpleDateFormat format12 = new SimpleDateFormat("dd-MM-yyyy K:mm a");

    @SuppressWarnings("deprecation")
    public FileCursorAdapter(Context context, Cursor c) {
        super(context, c);
        this.cContext = context;
        inflater = LayoutInflater.from(context);
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
        interval = new Interval(cContext);

        if (convertView == null) {
            inflater = (LayoutInflater) cContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item_file, null);
        }

        SharedPrefs sPrefs = new SharedPrefs(cContext);

        fileName = (TextView) convertView.findViewById(R.id.fileName);
        lastModified = (TextView) convertView.findViewById(R.id.lastModified);
        task = (TextView) convertView.findViewById(R.id.task);
        type = (TextView) convertView.findViewById(R.id.type);
        number = (TextView) convertView.findViewById(R.id.number);
        date = (TextView) convertView.findViewById(R.id.date);
        time = (TextView) convertView.findViewById(R.id.time);
        repeat = (TextView) convertView.findViewById(R.id.repeat);

        ColorSetter cs = new ColorSetter(cContext);
        CardView card = (CardView) convertView.findViewById(R.id.card);
        card.setCardBackgroundColor(cs.getCardStyle());

        String title;
        String fileNameS;
        String typeS;
        String numberS;
        String weekdays;
        int hour;
        int minute;
        int day;
        int month;
        int year;
        int repCode;
        long repTime;
        long lastModifiedS;
        double lat;
        double longi;
        repCode = c.getInt(c.getColumnIndex(Constants.COLUMN_REPEAT));
        repTime = c.getLong(c.getColumnIndex(Constants.COLUMN_REMIND_TIME));
        lat = c.getDouble(c.getColumnIndex(Constants.COLUMN_LATITUDE));
        longi = c.getDouble(c.getColumnIndex(Constants.COLUMN_LONGITUDE));
        day = c.getInt(c.getColumnIndex(Constants.COLUMN_DAY));
        month = c.getInt(c.getColumnIndex(Constants.COLUMN_MONTH));
        year = c.getInt(c.getColumnIndex(Constants.COLUMN_YEAR));
        hour = c.getInt(c.getColumnIndex(Constants.COLUMN_HOUR));
        minute = c.getInt(c.getColumnIndex(Constants.COLUMN_MINUTE));
        numberS = c.getString(c.getColumnIndex(Constants.COLUMN_NUMBER));
        title = c.getString(c.getColumnIndex(Constants.COLUMN_TEXT));
        fileNameS = c.getString(c.getColumnIndex(Constants.FilesConstants.COLUMN_FILE_NAME));
        lastModifiedS = c.getLong(c.getColumnIndex(Constants.FilesConstants.COLUMN_FILE_LAST_EDIT));
        typeS = c.getString(c.getColumnIndex(Constants.COLUMN_TYPE));
        weekdays = c.getString(c.getColumnIndex(Constants.COLUMN_WEEKDAYS));

        int pos = fileNameS.lastIndexOf(".");
        String fileN = fileNameS.substring(0, pos);
        fileName.setText(fileN);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(lastModifiedS);
        Date date1 = calendar.getTime();

        String dateS;
        if (new SharedPrefs(cContext).loadBoolean(Constants.APP_UI_PREFERENCES_IS_24_TIME_FORMAT)){
            dateS = format.format(date1);
        } else {
            dateS = format12.format(date1);
        }
        lastModified.setText(dateS);
        task.setText(title);
        number.setText("");
        repeat.setText("");

        if (typeS.matches(Constants.TYPE_REMINDER)){
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, day);
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);

            String formattedTime;
            if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_IS_24_TIME_FORMAT)){
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                formattedTime = sdf.format(calendar.getTime());
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("K:mm a");
                formattedTime = sdf.format(calendar.getTime());
            }

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
            String formattedDate = dateFormat.format(calendar.getTime());
            date.setText(formattedDate);
            time.setText(formattedTime);
            repeat.setText(interval.getInterval(repCode));

            type.setText(cContext.getString(R.string.reminder_type));
        } else if (typeS.matches(Constants.TYPE_TIME)){
            time.setText("");
            date.setText(Utils.generateAfterString(repTime));

            repeat.setText(interval.getTimeInterval(repCode));

            type.setText(cContext.getString(R.string.reminder_type));
        } else if (typeS.matches(Constants.TYPE_WEEKDAY)){
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            String formattedTime;
            if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_IS_24_TIME_FORMAT)){
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                formattedTime = sdf.format(calendar.getTime());
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("K:mm a");
                formattedTime = sdf.format(calendar.getTime());
            }

            time.setText(formattedTime);

            if (weekdays.length() == 7) {
                date.setText(getRepeatString(weekdays));
            }

            type.setText(cContext.getString(R.string.reminder_type));
        } else if (typeS.matches(Constants.TYPE_LOCATION)){
            date.setText(String.valueOf(lat));
            time.setText(String.valueOf(longi));

            type.setText(cContext.getString(R.string.reminder_type));
        } else if (typeS.matches(Constants.TYPE_MESSAGE) || typeS.matches(Constants.TYPE_CALL)){
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, day);
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);

            String formattedTime;
            if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_IS_24_TIME_FORMAT)){
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                formattedTime = sdf.format(calendar.getTime());
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("K:mm a");
                formattedTime = sdf.format(calendar.getTime());
            }

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
            String formattedDate = dateFormat.format(calendar.getTime());

            date.setText(formattedDate);
            time.setText(formattedTime);

            number.setText(numberS);

            repeat.setText(interval.getInterval(repCode));
        } else if (typeS.matches(Constants.TYPE_WEEKDAY_CALL) || typeS.matches(Constants.TYPE_WEEKDAY_MESSAGE)){
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            String formattedTime;
            if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_IS_24_TIME_FORMAT)){
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                formattedTime = sdf.format(calendar.getTime());
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("K:mm a");
                formattedTime = sdf.format(calendar.getTime());
            }

            time.setText(formattedTime);

            if (weekdays.length() == 7) {
                date.setText(getRepeatString(weekdays));
            }

            number.setText(numberS);
        } else if (typeS.matches(Constants.TYPE_LOCATION_MESSAGE) || typeS.matches(Constants.TYPE_LOCATION_CALL)){
            date.setText(String.valueOf(lat));
            time.setText(String.valueOf(longi));

            number.setText(numberS);
        } else if (typeS.startsWith(Constants.TYPE_SKYPE)){
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, day);
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);

            String formattedTime;
            if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_IS_24_TIME_FORMAT)){
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                formattedTime = sdf.format(calendar.getTime());
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("K:mm a");
                formattedTime = sdf.format(calendar.getTime());
            }

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
            String formattedDate = dateFormat.format(calendar.getTime());

            date.setText(formattedDate);
            time.setText(formattedTime);

            repeat.setText(interval.getInterval(repCode));
            if (typeS.matches(Constants.TYPE_SKYPE)){
                type.setText(cContext.getString(R.string.skype_call_type_title));
            } else if (typeS.matches(Constants.TYPE_SKYPE_VIDEO)){
                type.setText(cContext.getString(R.string.skype_video_type_title));
            } else if (typeS.matches(Constants.TYPE_SKYPE_CHAT)){
                type.setText(cContext.getString(R.string.skype_chat_type_title));
            }
            number.setText(numberS);
        } else if (typeS.matches(Constants.TYPE_APPLICATION)){
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, day);
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);

            String formattedTime;
            if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_IS_24_TIME_FORMAT)){
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                formattedTime = sdf.format(calendar.getTime());
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("K:mm a");
                formattedTime = sdf.format(calendar.getTime());
            }

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
            String formattedDate = dateFormat.format(calendar.getTime());

            date.setText(formattedDate);
            time.setText(formattedTime);

            repeat.setText(interval.getInterval(repCode));
            type.setText(cContext.getString(R.string.reminder_type_application));
            number.setText(numberS);
        } else if (typeS.matches(Constants.TYPE_APPLICATION_BROWSER)){
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, day);
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);

            String formattedTime;
            if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_IS_24_TIME_FORMAT)){
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                formattedTime = sdf.format(calendar.getTime());
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("K:mm a");
                formattedTime = sdf.format(calendar.getTime());
            }

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
            String formattedDate = dateFormat.format(calendar.getTime());

            date.setText(formattedDate);
            time.setText(formattedTime);

            repeat.setText(interval.getInterval(repCode));
            type.setText(cContext.getString(R.string.reminder_type_open_link));
            number.setText(numberS);
        }

        if (typeS.endsWith(Constants.TYPE_MESSAGE)){
            type.setText(cContext.getString(R.string.reminder_send_message));
        } else if (typeS.endsWith(Constants.TYPE_CALL)){
            type.setText(cContext.getString(R.string.reminder_make_call));
        }

        return convertView;
    }

    private String getRepeatString(String repCode){
        String res;
        StringBuilder sb = new StringBuilder();
        if (Character.toString(repCode.charAt(0)).matches(Constants.DAY_CHECKED)){
            sb.append(cContext.getString(R.string.weekday_monday));
            sb.append(",");
        }
        if (Character.toString(repCode.charAt(1)).matches(Constants.DAY_CHECKED)){
            sb.append(cContext.getString(R.string.weekday_tuesday));
            sb.append(",");
        }
        if (Character.toString(repCode.charAt(2)).matches(Constants.DAY_CHECKED)){
            sb.append(cContext.getString(R.string.weekday_wednesday));
            sb.append(",");
        }
        if (Character.toString(repCode.charAt(3)).matches(Constants.DAY_CHECKED)){
            sb.append(cContext.getString(R.string.weekday_thursday));
            sb.append(",");
        }
        if (Character.toString(repCode.charAt(4)).matches(Constants.DAY_CHECKED)){
            sb.append(cContext.getString(R.string.weekday_friday));
            sb.append(",");
        }
        if (Character.toString(repCode.charAt(5)).matches(Constants.DAY_CHECKED)){
            sb.append(cContext.getString(R.string.weekday_saturday));
            sb.append(",");
        }
        if (Character.toString(repCode.charAt(6)).matches(Constants.DAY_CHECKED)){
            sb.append(cContext.getString(R.string.weekday_sunday));
        }
        if (repCode.matches(Constants.ALL_CHECKED)){
            res = cContext.getString(R.string.interval_day);
        } else res = sb.toString();
        return res;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

    }
}