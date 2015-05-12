package com.cray.software.justreminder.adapters;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.Contacts;
import com.cray.software.justreminder.helpers.Interval;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.interfaces.Constants;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ArchiveCursorAdapter extends CursorAdapter implements Filterable {

    TextView taskTitle, taskDate, viewTime, reminder_type, reminder_phone, repeatInterval, reminder_contact_name;
    LayoutInflater inflater;
    CheckBox itemCheck;
    private Cursor c;
    ImageView leftTime;
    ImageView taskIcon;
    Context cContext;
    DataBase DB;
    Contacts contacts;
    Interval interval;
    CardView card;

    @SuppressWarnings("deprecation")
    public ArchiveCursorAdapter(Context context, Cursor c) {
        super(context, c);
        this.cContext = context;
        inflater = LayoutInflater.from(context);
        this.c = c;
        c.moveToFirst();
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return inflater.inflate(R.layout.list_item_card, null);
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
        DB = new DataBase(cContext);
        contacts = new Contacts(cContext);
        interval = new Interval(cContext);
        DB.open();
        if (convertView == null) {
            inflater = (LayoutInflater) cContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item_card, null);
        }
        taskIcon = (ImageView) convertView.findViewById(R.id.taskIcon);
        taskTitle = (TextView) convertView.findViewById(R.id.taskText);
        taskDate = (TextView) convertView.findViewById(R.id.taskDate);
        viewTime = (TextView) convertView.findViewById(R.id.taskTime);
        reminder_type = (TextView) convertView.findViewById(R.id.reminder_type);
        reminder_phone = (TextView) convertView.findViewById(R.id.reminder_phone);
        repeatInterval = (TextView) convertView.findViewById(R.id.repeatInterval);
        reminder_contact_name = (TextView) convertView.findViewById(R.id.reminder_contact_name);
        itemCheck = (CheckBox) convertView.findViewById(R.id.itemCheck);
        itemCheck.setVisibility(View.GONE);
        leftTime = (ImageView) convertView.findViewById(R.id.leftTime);
        leftTime.setVisibility(View.GONE);

        ColorSetter cs = new ColorSetter(cContext);
        card = (CardView) convertView.findViewById(R.id.card);
        card.setCardBackgroundColor(cs.getCardStyle());

        SharedPrefs prefs = new SharedPrefs(cContext);
        boolean mDark = prefs.loadBoolean(Constants.APP_UI_PREFERENCES_USE_DARK_THEME);
        if (mDark){
            repeatInterval.setBackgroundDrawable(cContext.getResources().getDrawable(R.drawable.round_view_white));
        } else {
            repeatInterval.setBackgroundDrawable(cContext.getResources().getDrawable(R.drawable.round_view_black));
        }

        convertView.findViewById(R.id.remainingTime).setVisibility(View.GONE);

        String title = c.getString(c.getColumnIndex(Constants.COLUMN_TEXT));
        String type = c.getString(c.getColumnIndex(Constants.COLUMN_TYPE));
        String number = c.getString(c.getColumnIndex(Constants.COLUMN_NUMBER));
        String weekdays = c.getString(c.getColumnIndex(Constants.COLUMN_WEEKDAYS));
        int hour = c.getInt(c.getColumnIndex(Constants.COLUMN_HOUR));
        int minute = c.getInt(c.getColumnIndex(Constants.COLUMN_MINUTE));
        int day = c.getInt(c.getColumnIndex(Constants.COLUMN_DAY));
        int month = c.getInt(c.getColumnIndex(Constants.COLUMN_MONTH));
        int year = c.getInt(c.getColumnIndex(Constants.COLUMN_YEAR));
        int repCode = c.getInt(c.getColumnIndex(Constants.COLUMN_REPEAT));
        int repTime = c.getInt(c.getColumnIndex(Constants.COLUMN_REMIND_TIME));
        double lat = c.getDouble(c.getColumnIndex(Constants.COLUMN_LATITUDE));
        double longi = c.getDouble(c.getColumnIndex(Constants.COLUMN_LONGITUDE));
        String categoryId = c.getString(c.getColumnIndex(Constants.COLUMN_CATEGORY));

        Cursor cf = DB.getCategory(categoryId);
        int categoryColor = 0;
        if (cf != null && cf.moveToFirst()) {
            categoryColor = cf.getInt(cf.getColumnIndex(Constants.COLUMN_COLOR));
        }
        if (cf != null) cf.close();

        if (!type.startsWith(Constants.TYPE_WEEKDAY)) {
            if (type.matches(Constants.TYPE_CALL) || type.matches(Constants.TYPE_LOCATION_CALL)) {
                reminder_phone.setText(number);
                reminder_type.setText(cContext.getString(R.string.reminder_make_call));
                String name = contacts.getContactNameFromNumber(number, mContext);
                if (name != null) reminder_contact_name.setText(name);
                else reminder_contact_name.setText("");
            } else if (type.matches(Constants.TYPE_REMINDER) || type.matches(Constants.TYPE_TIME)) {
                reminder_type.setText(cContext.getString(R.string.reminder_type));
            } else if (type.matches(Constants.TYPE_LOCATION)) {
                reminder_type.setText(cContext.getString(R.string.reminder_type));
            } else if (type.matches(Constants.TYPE_MESSAGE) || type.matches(Constants.TYPE_LOCATION_MESSAGE)) {
                reminder_phone.setText(number);
                reminder_type.setText(cContext.getString(R.string.reminder_send_message));
                String name = contacts.getContactNameFromNumber(number, mContext);
                if (name != null) reminder_contact_name.setText(name);
                else reminder_contact_name.setText("");
            } else if (type.startsWith(Constants.TYPE_SKYPE)){
                reminder_phone.setText(number);
                if (type.matches(Constants.TYPE_SKYPE)){
                    reminder_type.setText(cContext.getString(R.string.skype_call_type_title));
                } else if (type.matches(Constants.TYPE_SKYPE_VIDEO)){
                    reminder_type.setText(cContext.getString(R.string.skype_video_type_title));
                } else if (type.matches(Constants.TYPE_SKYPE_CHAT)){
                    reminder_type.setText(cContext.getString(R.string.skype_chat_type_title));
                }
                reminder_contact_name.setText(number);
            } else if (type.matches(Constants.TYPE_APPLICATION)){
                PackageManager packageManager = cContext.getPackageManager();
                ApplicationInfo applicationInfo = null;
                try {
                    applicationInfo = packageManager.getApplicationInfo(number, 0);
                } catch (final PackageManager.NameNotFoundException ignored) {}
                final String name = (String)((applicationInfo != null) ?
                        packageManager.getApplicationLabel(applicationInfo) : "???");
                reminder_phone.setText(number);
                reminder_type.setText(cContext.getString(R.string.reminder_type_application));
                reminder_contact_name.setText(name);
            } else if (type.matches(Constants.TYPE_APPLICATION_BROWSER)){
                reminder_phone.setText(number);
                reminder_type.setText(cContext.getString(R.string.reminder_type_open_link));
                reminder_contact_name.setText(number);
            }

            if (type.matches(Constants.TYPE_CALL) || type.matches(Constants.TYPE_MESSAGE) || type.matches(Constants.TYPE_REMINDER)){
                repeatInterval.setText(interval.getInterval(repCode));
            } else if (type.matches(Constants.TYPE_TIME)) {
                repeatInterval.setText(interval.getTimeInterval(repCode));
            } else {
                repeatInterval.setText(cContext.getString(R.string.interval_zero));
            }

            taskTitle.setText(title);
            taskIcon.setImageDrawable(cContext.getResources().getDrawable(cs.getCategoryIndicator(categoryColor)));

            if (repTime != 0){
                viewTime.setText("");
                if (repTime == 1){
                    taskDate.setText(cContext.getString(R.string.after_one_minute));
                } else if (repTime == 2){
                    taskDate.setText(cContext.getString(R.string.after_two_minutes));
                } else if (repTime == 5){
                    taskDate.setText(cContext.getString(R.string.after_five_minutes));
                } else if (repTime == 10){
                    taskDate.setText(cContext.getString(R.string.after_ten_minutes));
                } else if (repTime == 15){
                    taskDate.setText(cContext.getString(R.string.after_fifteen_minutes));
                } else if (repTime == 30){
                    taskDate.setText(cContext.getString(R.string.after_half_hour));
                } else if (repTime == 60){
                    taskDate.setText(cContext.getString(R.string.after_one_hour));
                } else if (repTime == 120){
                    taskDate.setText(cContext.getString(R.string.after_two_hours));
                } else if (repTime == 300){
                    taskDate.setText(cContext.getString(R.string.after_five_hours));
                }
            } else if (lat != 0.0 || longi != 0.0){
                taskDate.setText(String.format("%.5f", lat));
                viewTime.setText(String.format("%.5f", longi));
            } else {
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, hour);
                calendar.set(Calendar.MINUTE, minute);
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, day);

                String formattedTime;
                if (new SharedPrefs(mContext).loadBoolean(Constants.APP_UI_PREFERENCES_IS_24_TIME_FORMAT)){
                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                    formattedTime = sdf.format(calendar.getTime());
                } else {
                    SimpleDateFormat sdf = new SimpleDateFormat("K:mm a");
                    formattedTime = sdf.format(calendar.getTime());
                }

                SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
                String date = dateFormat.format(calendar.getTime());

                taskDate.setText(date);
                viewTime.setText(formattedTime);
            }
            DB.close();
        } else {
            taskTitle.setText(title);

            taskIcon.setImageDrawable(cContext.getResources().getDrawable(cs.getCategoryIndicator(categoryColor)));

            if (type.matches(Constants.TYPE_WEEKDAY_CALL)) {
                reminder_phone.setText(number);
                reminder_type.setText(cContext.getString(R.string.reminder_make_call));
                String name = contacts.getContactNameFromNumber(number, mContext);
                if (name != null) reminder_contact_name.setText(name);
                else reminder_contact_name.setText("");
            } else if (type.matches(Constants.TYPE_WEEKDAY_MESSAGE)) {
                reminder_phone.setText(number);
                reminder_type.setText(cContext.getString(R.string.reminder_send_message));
                String name = contacts.getContactNameFromNumber(number, mContext);
                if (name != null) reminder_contact_name.setText(name);
                else reminder_contact_name.setText("");
            } else if (type.matches(Constants.TYPE_WEEKDAY)) {
                reminder_type.setText(cContext.getString(R.string.reminder_type));
            }

            leftTime.setVisibility(View.GONE);

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            String formattedTime;
            if (new SharedPrefs(mContext).loadBoolean(Constants.APP_UI_PREFERENCES_IS_24_TIME_FORMAT)){
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                formattedTime = sdf.format(calendar.getTime());
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("K:mm a");
                formattedTime = sdf.format(calendar.getTime());
            }

            if (weekdays.length() == 7) {
                taskDate.setText(getRepeatString(weekdays));
            }
            viewTime.setText(formattedTime);

            DB.close();
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