package com.cray.software.justreminder.dialogs;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.Contacts;
import com.cray.software.justreminder.helpers.Interval;
import com.cray.software.justreminder.helpers.Notifier;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.TimeCount;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.interfaces.Prefs;
import com.cray.software.justreminder.reminder.Reminder;
import com.cray.software.justreminder.utils.AssetsUtil;
import com.cray.software.justreminder.utils.ReminderUtils;
import com.cray.software.justreminder.utils.Utils;

public class VoiceResult extends Activity {

    ColorSetter cs = new ColorSetter(VoiceResult.this);
    DataBase DB;
    long id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTheme(cs.getDialogStyle());
        setContentView(R.layout.voice_dialog_layout);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        Intent intent = getIntent();
        id = intent.getLongExtra("ids", 0);

        TextView buttonEdit = (TextView) findViewById(R.id.buttonEdit);
        buttonEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Reminder.edit(id, VoiceResult.this);
                finish();
            }
        });

        findViewById(R.id.buttonClose).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Notifier(VoiceResult.this).recreatePermanent();
                finish();
            }
        });

        DB = new DataBase(VoiceResult.this);
        Interval interval = new Interval(VoiceResult.this);
        DB.open();

        ImageView taskIcon = (ImageView) findViewById(R.id.taskIcon);
        Typeface typeface = AssetsUtil.getLightTypeface(this);
        TextView taskTitle = (TextView) findViewById(R.id.taskText);
        taskTitle.setTypeface(typeface);
        TextView taskDate = (TextView) findViewById(R.id.taskDate);
        taskDate.setTypeface(typeface);
        TextView viewTime = (TextView) findViewById(R.id.taskTime);
        viewTime.setTypeface(typeface);
        TextView reminder_type = (TextView) findViewById(R.id.reminder_type);
        reminder_type.setTypeface(typeface);
        TextView reminder_phone = (TextView) findViewById(R.id.reminder_phone);
        reminder_phone.setTypeface(typeface);
        TextView repeatInterval = (TextView) findViewById(R.id.repeatInterval);
        repeatInterval.setTypeface(typeface);
        TextView reminder_contact_name = (TextView) findViewById(R.id.reminder_contact_name);
        reminder_contact_name.setTypeface(typeface);
        ImageView leftTime = (ImageView) findViewById(R.id.leftTime);
        leftTime.setVisibility(View.VISIBLE);

        SharedPrefs prefs = new SharedPrefs(VoiceResult.this);
        boolean mDark = prefs.loadBoolean(Prefs.USE_DARK_THEME);
        repeatInterval.setBackgroundResource(mDark ? R.drawable.round_view_white : R.drawable.round_view_black);

        String title = null;
        String type = null;
        String number = null;
        String weekdays = null;
        int hour = 0;
        int minute = 0;
        int seconds = 0;
        int day = 0;
        int month = 0;
        int year = 0;
        int repCode = 0;
        long repTime = 0;
        double lat = 0.0;
        double longi = 0.0;
        int repCount = 0;
        int delay = 0;
        String categoryId = null;
        Cursor c = DB.getReminder(id);
        if (c != null && c.moveToFirst()) {
            repCode = c.getInt(c.getColumnIndex(Constants.COLUMN_REPEAT));
            repCount = c.getInt(c.getColumnIndex(Constants.COLUMN_REMINDERS_COUNT));
            repTime = c.getLong(c.getColumnIndex(Constants.COLUMN_REMIND_TIME));
            lat = c.getDouble(c.getColumnIndex(Constants.COLUMN_LATITUDE));
            longi = c.getDouble(c.getColumnIndex(Constants.COLUMN_LONGITUDE));
            day = c.getInt(c.getColumnIndex(Constants.COLUMN_DAY));
            month = c.getInt(c.getColumnIndex(Constants.COLUMN_MONTH));
            year = c.getInt(c.getColumnIndex(Constants.COLUMN_YEAR));
            hour = c.getInt(c.getColumnIndex(Constants.COLUMN_HOUR));
            minute = c.getInt(c.getColumnIndex(Constants.COLUMN_MINUTE));
            delay = c.getInt(c.getColumnIndex(Constants.COLUMN_DELAY));
            seconds = c.getInt(c.getColumnIndex(Constants.COLUMN_SECONDS));
            number = c.getString(c.getColumnIndex(Constants.COLUMN_NUMBER));
            title = c.getString(c.getColumnIndex(Constants.COLUMN_TEXT));
            type = c.getString(c.getColumnIndex(Constants.COLUMN_TYPE));
            weekdays = c.getString(c.getColumnIndex(Constants.COLUMN_WEEKDAYS));
            categoryId = c.getString(c.getColumnIndex(Constants.COLUMN_CATEGORY));
        }

        TimeCount mCount = new TimeCount(VoiceResult.this);

        Cursor cf = DB.getCategory(categoryId);
        int categoryColor = 0;
        if (cf != null && cf.moveToFirst()) {
            categoryColor = cf.getInt(cf.getColumnIndex(Constants.COLUMN_COLOR));
        }
        if (cf != null) cf.close();

        taskIcon.setImageDrawable(Utils.getDrawable(this, cs.getCategoryIndicator(categoryColor)));

        if (!type.startsWith(Constants.TYPE_WEEKDAY)) {
            if (type.matches(Constants.TYPE_CALL) || type.matches(Constants.TYPE_LOCATION_CALL)) {
                reminder_phone.setText(number);
                reminder_type.setText(getString(R.string.reminder_make_call));
                String contactName = Contacts.getContactNameFromNumber(number, VoiceResult.this);
                reminder_contact_name.setText(contactName);
            } else if (type.matches(Constants.TYPE_REMINDER) || type.matches(Constants.TYPE_TIME)) {
                reminder_type.setText(getString(R.string.reminder_type));
            } else if (type.matches(Constants.TYPE_LOCATION)) {
                reminder_type.setText(getString(R.string.reminder_type));
            } else if (type.matches(Constants.TYPE_MESSAGE) || type.matches(Constants.TYPE_LOCATION_MESSAGE)) {
                reminder_phone.setText(number);
                reminder_type.setText(getString(R.string.reminder_send_message));
                String contactName = Contacts.getContactNameFromNumber(number, VoiceResult.this);
                reminder_contact_name.setText(contactName);
            } else if (type.startsWith(Constants.TYPE_SKYPE)) {
                reminder_phone.setText(number);
                if (type.matches(Constants.TYPE_SKYPE)) {
                    reminder_type.setText(getString(R.string.skype_call_type_title));
                } else if (type.matches(Constants.TYPE_SKYPE_VIDEO)) {
                    reminder_type.setText(getString(R.string.skype_video_type_title));
                } else if (type.matches(Constants.TYPE_SKYPE_CHAT)) {
                    reminder_type.setText(getString(R.string.skype_chat_type_title));
                }
                reminder_contact_name.setText(number);
            } else if (type.matches(Constants.TYPE_APPLICATION)) {
                PackageManager packageManager = getPackageManager();
                ApplicationInfo applicationInfo = null;
                try {
                    applicationInfo = packageManager.getApplicationInfo(number, 0);
                } catch (final PackageManager.NameNotFoundException ignored) {
                }
                final String name = (String) ((applicationInfo != null) ? packageManager.getApplicationLabel(applicationInfo) : "???");
                reminder_phone.setText(number);
                reminder_type.setText(getString(R.string.reminder_type_application));
                reminder_contact_name.setText(name);
            } else if (type.matches(Constants.TYPE_APPLICATION_BROWSER)) {
                reminder_phone.setText(number);
                reminder_type.setText(getString(R.string.reminder_type_open_link));
                reminder_contact_name.setText(number);
            }

            if (type.matches(Constants.TYPE_CALL) || type.matches(Constants.TYPE_MESSAGE) ||
                    type.matches(Constants.TYPE_REMINDER) || type.startsWith(Constants.TYPE_SKYPE) ||
                    type.startsWith(Constants.TYPE_APPLICATION)) {
                leftTime.setImageDrawable(mCount.getDifference(null, year, month, day, hour, minute,
                        seconds, repTime, repCode, repCount, delay));
                repeatInterval.setText(interval.getInterval(repCode));
            } else if (type.matches(Constants.TYPE_TIME)) {
                leftTime.setImageDrawable(mCount.getDifference(null, year, month, day, hour, minute,
                        seconds, repTime, repCode, repCount, delay));
                repeatInterval.setText(interval.getTimeInterval(repCode));
            } else {
                leftTime.setVisibility(View.GONE);
                repeatInterval.setText(getString(R.string.interval_zero));
            }

            taskTitle.setText(title);

            String[] dT = mCount.getNextDateTime(year, month, day, hour, minute, seconds, repTime,
                    repCode, repCount, delay);
            if (lat != 0.0 || longi != 0.0) {
                taskDate.setText(String.format("%.5f", lat));
                viewTime.setText(String.format("%.5f", longi));
            } else {
                taskDate.setText(dT[0]);
                viewTime.setText(dT[1]);
            }
            DB.close();
        } else {
            taskTitle.setText(title);

            if (type.matches(Constants.TYPE_WEEKDAY_CALL)) {
                reminder_phone.setText(number);
                reminder_type.setText(getString(R.string.reminder_make_call));
                String contactName = Contacts.getContactNameFromNumber(number, VoiceResult.this);
                reminder_contact_name.setText(contactName);
            } else if (type.matches(Constants.TYPE_WEEKDAY_MESSAGE)) {
                reminder_phone.setText(number);
                reminder_type.setText(getString(R.string.reminder_send_message));
                String contactName = Contacts.getContactNameFromNumber(number, VoiceResult.this);
                reminder_contact_name.setText(contactName);
            } else if (type.matches(Constants.TYPE_WEEKDAY)) {
                reminder_type.setText(getString(R.string.reminder_type));
            }

            leftTime.setImageDrawable(mCount.
                    getDifference(weekdays, year, month, day, hour, minute, seconds, repTime,
                            repCode, repCount, delay));
            repeatInterval.setVisibility(View.GONE);

            String minuteStr;
            if (minute < 10) {
                minuteStr = "0" + minute;
            } else minuteStr = String.valueOf(minute);
            String hourStr;
            if (hour < 10){
                hourStr = "0" + hour;
            } else hourStr = String.valueOf(hour);

            if (weekdays != null && weekdays.length() == 7) {
                taskDate.setText(ReminderUtils.getRepeatString(this, weekdays));
            }
            viewTime.setText(hourStr + ":" + minuteStr);

            DB.close();
        }
    }
}