package com.cray.software.justreminder.reminder;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Typeface;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.widget.CardView;
import android.support.v7.widget.SwitchCompat;
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
import com.cray.software.justreminder.helpers.TimeCount;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.interfaces.SyncListener;
import com.cray.software.justreminder.utils.AssetsUtil;
import com.cray.software.justreminder.utils.ReminderUtils;
import com.cray.software.justreminder.utils.TimeUtil;
import com.cray.software.justreminder.utils.Utils;

import java.util.Calendar;
import java.util.Date;

public class CustomCursorAdapter extends CursorAdapter implements Filterable {

    LayoutInflater inflater;
    private Cursor c;
    Context context;
    DataBase DB;
    TimeCount mCount;
    Interval mInterval;
    SyncListener mListener;
    Typeface typeface;
    ColorSetter cs;
    SharedPrefs prefs;

    @SuppressWarnings("deprecation")
    public CustomCursorAdapter(Context context, Cursor c, SyncListener clickListener) {
        super(context, c);
        this.context = context;
        inflater = LayoutInflater.from(context);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.c = c;
        this.mListener = clickListener;
        DB = new DataBase(context);
        mInterval = new Interval(context);
        typeface = AssetsUtil.getLightTypeface(context);
        mCount = new TimeCount(context);
        cs = new ColorSetter(context);
        prefs = new SharedPrefs(context);
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
    public View getView(final int position, View convertView, ViewGroup parent) {
        c.moveToPosition(position);
        DB.open();
        String title = c.getString(c.getColumnIndex(Constants.COLUMN_TEXT));
        String type = c.getString(c.getColumnIndex(Constants.COLUMN_TYPE));
        String number = c.getString(c.getColumnIndex(Constants.COLUMN_NUMBER));
        String weekdays = c.getString(c.getColumnIndex(Constants.COLUMN_WEEKDAYS));
        String categoryId = c.getString(c.getColumnIndex(Constants.COLUMN_CATEGORY));
        int hour = c.getInt(c.getColumnIndex(Constants.COLUMN_HOUR));
        int minute = c.getInt(c.getColumnIndex(Constants.COLUMN_MINUTE));
        int seconds = c.getInt(c.getColumnIndex(Constants.COLUMN_SECONDS));
        int day = c.getInt(c.getColumnIndex(Constants.COLUMN_DAY));
        int month = c.getInt(c.getColumnIndex(Constants.COLUMN_MONTH));
        int year = c.getInt(c.getColumnIndex(Constants.COLUMN_YEAR));
        int repCode = c.getInt(c.getColumnIndex(Constants.COLUMN_REPEAT));
        long repTime = c.getLong(c.getColumnIndex(Constants.COLUMN_REMIND_TIME));
        int isDone = c.getInt(c.getColumnIndex(Constants.COLUMN_IS_DONE));
        int archived = c.getInt(c.getColumnIndex(Constants.COLUMN_ARCHIVED));
        double lat = c.getDouble(c.getColumnIndex(Constants.COLUMN_LATITUDE));
        double lon = c.getDouble(c.getColumnIndex(Constants.COLUMN_LONGITUDE));
        int repCount = c.getInt(c.getColumnIndex(Constants.COLUMN_REMINDERS_COUNT));
        int delay = c.getInt(c.getColumnIndex(Constants.COLUMN_DELAY));

        Cursor cf = DB.getCategory(categoryId);
        int categoryColor = 0;
        if (cf != null && cf.moveToFirst()) {
            categoryColor = cf.getInt(cf.getColumnIndex(Constants.COLUMN_COLOR));
        }
        if (cf != null) cf.close();

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item_card, null);
        }

        TextView leftTime = (TextView) convertView.findViewById(R.id.remainingTime);
        leftTime.setTypeface(typeface);
        SwitchCompat check = (SwitchCompat) convertView.findViewById(R.id.itemCheck);
        check.setFocusable(false);
        check.setFocusableInTouchMode(false);
        check.setVisibility(View.VISIBLE);
        ImageView taskIcon = (ImageView) convertView.findViewById(R.id.taskIcon);
        TextView taskTitle = (TextView) convertView.findViewById(R.id.taskText);
        taskTitle.setTypeface(typeface);
        taskTitle.setText("");
        TextView taskDate = (TextView) convertView.findViewById(R.id.taskDate);
        taskDate.setTypeface(typeface);
        taskDate.setText("");
        TextView viewTime = (TextView) convertView.findViewById(R.id.taskTime);
        viewTime.setTypeface(typeface);
        viewTime.setText("");
        TextView reminder_type = (TextView) convertView.findViewById(R.id.reminder_type);
        reminder_type.setTypeface(typeface);
        reminder_type.setText("");
        TextView reminder_phone = (TextView) convertView.findViewById(R.id.reminder_phone);
        reminder_phone.setTypeface(typeface);
        reminder_phone.setText("");
        TextView repeatInterval = (TextView) convertView.findViewById(R.id.repeatInterval);
        repeatInterval.setTypeface(typeface);
        repeatInterval.setText("");
        TextView reminder_contact_name = (TextView) convertView.findViewById(R.id.reminder_contact_name);
        reminder_contact_name.setTypeface(typeface);
        reminder_contact_name.setText("");
        ImageView leftTimeIcon = (ImageView) convertView.findViewById(R.id.leftTime);
        leftTimeIcon.setVisibility(View.VISIBLE);

        CardView card = (CardView) convertView.findViewById(R.id.card);
        card.setCardBackgroundColor(cs.getCardStyle());

        if (isDone == 1){
            check.setChecked(false);
        } else {
            check.setChecked(true);
        }

        reminder_type.setText(ReminderUtils.getTypeString(context, type));

        boolean is24 = prefs.loadBoolean(Constants.APP_UI_PREFERENCES_IS_24_TIME_FORMAT);

        if (type.startsWith(Constants.TYPE_MONTHDAY)){
            taskTitle.setText(title);

            taskIcon.setImageDrawable(Utils.getDrawable(context, cs.getCategoryIndicator(categoryColor)));

            if (type.startsWith(Constants.TYPE_MONTHDAY_CALL)) {
                reminder_phone.setText(number);
                String name = Contacts.getContactNameFromNumber(number, mContext);
                if (name != null) reminder_contact_name.setText(name);
                else reminder_contact_name.setText("");
            } else if (type.startsWith(Constants.TYPE_MONTHDAY_MESSAGE)) {
                reminder_phone.setText(number);
                String name = Contacts.getContactNameFromNumber(number, mContext);
                if (name != null) reminder_contact_name.setText(name);
                else reminder_contact_name.setText("");
            }

            long time = TimeCount.getNextMonthDayTime(hour, minute, day, delay);

            leftTimeIcon.setImageDrawable(mCount.
                    getDifference(time));
            repeatInterval.setVisibility(View.GONE);

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            Date mTime = calendar.getTime();
            viewTime.setText(TimeUtil.getTime(mTime, is24));

            calendar.setTimeInMillis(time);
            taskDate.setText(TimeUtil.dateFormat.format(calendar.getTime()));
            if (isDone == 0) {
                String remaining = mCount.getRemaining(time);
                leftTime.setText(remaining);
            } else {
                leftTime.setVisibility(View.GONE);
            }
        } else if (!type.startsWith(Constants.TYPE_WEEKDAY)) {
            if (type.matches(Constants.TYPE_CALL) || type.matches(Constants.TYPE_LOCATION_CALL) ||
                    type.matches(Constants.TYPE_LOCATION_OUT_CALL)) {
                reminder_phone.setText(number);
                String name = Contacts.getContactNameFromNumber(number, mContext);
                if (name != null) reminder_contact_name.setText(name);
                else reminder_contact_name.setText("");
            } else if (type.matches(Constants.TYPE_MESSAGE) || type.matches(Constants.TYPE_LOCATION_MESSAGE) ||
                    type.matches(Constants.TYPE_LOCATION_OUT_MESSAGE)) {
                reminder_phone.setText(number);
                String name = Contacts.getContactNameFromNumber(number, mContext);
                if (name != null) reminder_contact_name.setText(name);
                else reminder_contact_name.setText("");
            } else if (type.startsWith(Constants.TYPE_SKYPE)){
                reminder_phone.setText(number);
                reminder_contact_name.setText(number);
            } else if (type.matches(Constants.TYPE_APPLICATION)){
                PackageManager packageManager = context.getPackageManager();
                ApplicationInfo applicationInfo = null;
                try {
                    applicationInfo = packageManager.getApplicationInfo(number, 0);
                } catch (final PackageManager.NameNotFoundException ignored) {}
                final String name = (String)((applicationInfo != null) ? packageManager.getApplicationLabel(applicationInfo) : "???");
                reminder_phone.setText(number);
                reminder_contact_name.setText(name);
            } else if (type.matches(Constants.TYPE_APPLICATION_BROWSER)){
                reminder_phone.setText(number);
                reminder_contact_name.setText(number);
            }

            taskIcon.setImageDrawable(Utils.getDrawable(context, cs.getCategoryIndicator(categoryColor)));

            long time = TimeCount.getEventTime(year, month, day, hour, minute, seconds, repTime,
                    repCode, repCount, delay);

            if (type.matches(Constants.TYPE_CALL) || type.matches(Constants.TYPE_MESSAGE) ||
                    type.matches(Constants.TYPE_REMINDER) || type.startsWith(Constants.TYPE_SKYPE) ||
                    type.startsWith(Constants.TYPE_APPLICATION)) {
                leftTimeIcon.setImageDrawable(mCount.
                        getDifference(time));
                repeatInterval.setText(mInterval.getInterval(repCode));
            } else if (type.matches(Constants.TYPE_TIME)) {
                leftTimeIcon.setImageDrawable(mCount.
                        getDifference(time));
                repeatInterval.setText(mInterval.getTimeInterval(repCode));
            } else {
                if (type.startsWith(Constants.TYPE_LOCATION) || type.startsWith(Constants.TYPE_LOCATION_OUT)){
                    leftTimeIcon.setVisibility(View.GONE);
                    repeatInterval.setVisibility(View.GONE);
                } else {
                    leftTimeIcon.setVisibility(View.GONE);
                    repeatInterval.setText(context.getString(R.string.interval_zero));
                }
            }

            taskTitle.setText(title);

            String[] dT = mCount.
                    getNextDateTime(time);
            if (lat != 0.0 || lon != 0.0) {
                taskDate.setText(String.format("%.5f", lat));
                viewTime.setText(String.format("%.5f", lon));
                leftTime.setVisibility(View.GONE);
            } else {
                if (isDone == 0) {
                    leftTime.setText(mCount.
                            getRemaining(time));
                } else {
                   leftTime.setVisibility(View.GONE);
                }

                taskDate.setText(dT[0]);
                viewTime.setText(dT[1]);
            }
        } else {
            taskTitle.setText(title);

            taskIcon.setImageDrawable(Utils.getDrawable(context, cs.getCategoryIndicator(categoryColor)));

            if (type.matches(Constants.TYPE_WEEKDAY_CALL)) {
                reminder_phone.setText(number);
                String name = Contacts.getContactNameFromNumber(number, mContext);
                if (name != null) reminder_contact_name.setText(name);
                else reminder_contact_name.setText("");
            } else if (type.matches(Constants.TYPE_WEEKDAY_MESSAGE)) {
                reminder_phone.setText(number);
                String name = Contacts.getContactNameFromNumber(number, mContext);
                if (name != null) reminder_contact_name.setText(name);
                else reminder_contact_name.setText("");
            }

            long time = TimeCount.getNextWeekdayTime(hour, minute, weekdays, delay);

            leftTimeIcon.setImageDrawable(mCount.
                    getDifference(time));
            repeatInterval.setVisibility(View.GONE);

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            Date mTime = calendar.getTime();
            viewTime.setText(TimeUtil.getTime(mTime, is24));

            if (weekdays.length() == 7) {
                taskDate.setText(ReminderUtils.getRepeatString(context, weekdays));
                if (isDone == 0) {
                    String remaining = mCount.getRemaining(time);
                    leftTime.setText(remaining);
                } else {
                    leftTime.setVisibility(View.GONE);
                }
            }
        }
        if (isDone == 1){
            leftTimeIcon.setImageDrawable(Utils.getDrawable(context, R.drawable.drawable_grey));
        }

        if (archived > 0) {
            check.setVisibility(View.GONE);
            leftTime.setVisibility(View.GONE);
            leftTimeIcon.setVisibility(View.GONE);
        }

        DB.close();
        return convertView;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

    }
}