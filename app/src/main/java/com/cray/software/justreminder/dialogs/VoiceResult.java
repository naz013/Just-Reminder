package com.cray.software.justreminder.dialogs;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.constants.Configs;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.datas.models.ReminderModel;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.Contacts;
import com.cray.software.justreminder.helpers.Notifier;
import com.cray.software.justreminder.helpers.Recurrence;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.TimeCount;
import com.cray.software.justreminder.modules.Module;
import com.cray.software.justreminder.reminder.Reminder;
import com.cray.software.justreminder.reminder.ReminderDataProvider;
import com.cray.software.justreminder.reminder.ReminderUtils;
import com.cray.software.justreminder.utils.IntervalUtil;
import com.cray.software.justreminder.utils.TimeUtil;
import com.cray.software.justreminder.utils.ViewUtils;

public class VoiceResult extends Activity {

    private ColorSetter cs = new ColorSetter(VoiceResult.this);
    private long id;

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

        TextView leftTime, taskTitle, taskDate, reminder_type, reminder_phone,
                repeatInterval, reminder_contact_name;
        SwitchCompat check;
        ImageView taskIcon, leftTimeIcon;
        CardView itemCard;

        RelativeLayout reminderContainer;

        reminderContainer = (RelativeLayout) findViewById(R.id.reminderContainer);
        leftTime = (TextView) findViewById(R.id.remainingTime);
        check = (SwitchCompat) findViewById(R.id.itemCheck);
        check.setVisibility(View.VISIBLE);
        taskIcon = (ImageView) findViewById(R.id.taskIcon);
        taskDate = (TextView) findViewById(R.id.taskDate);
        taskDate.setText("");
        reminder_type = (TextView) findViewById(R.id.reminder_type);
        reminder_type.setText("");
        reminder_phone = (TextView) findViewById(R.id.reminder_phone);
        reminder_phone.setText("");
        repeatInterval = (TextView) findViewById(R.id.repeatInterval);
        repeatInterval.setText("");
        reminder_contact_name = (TextView) findViewById(R.id.reminder_contact_name);
        reminder_contact_name.setText("");
        leftTimeIcon = (ImageView) findViewById(R.id.leftTime);
        leftTimeIcon.setVisibility(View.VISIBLE);

        taskTitle = (TextView) findViewById(R.id.taskText);
        taskTitle.setText("");
        itemCard = (CardView) findViewById(R.id.itemCard);
        itemCard.setCardBackgroundColor(cs.getCardStyle());
        if (Module.isLollipop()) {
            itemCard.setCardElevation(Configs.CARD_ELEVATION);
        }

        SharedPrefs prefs = new SharedPrefs(VoiceResult.this);
        boolean mDark = prefs.loadBoolean(Prefs.USE_DARK_THEME);
        boolean is24 = prefs.loadBoolean(Prefs.IS_24_TIME_FORMAT);
        repeatInterval.setBackgroundResource(mDark ? R.drawable.round_view_white : R.drawable.round_view_black);

        ReminderModel model = ReminderDataProvider.getItem(this, id);
        String title = model.getTitle();
        String type = model.getType();
        String number = model.getNumber();
        long due = model.getDue();
        double lat = model.getPlace()[0];
        double lon = model.getPlace()[1];
        int isDone = model.getCompleted();
        String repeat = IntervalUtil.getInterval(this, model.getRepeat());
        String exclusion = model.getExclusion();
        int archived = model.getArchived();
        int categoryColor = model.getCatColor();

        reminderContainer.setVisibility(View.VISIBLE);

        TimeCount mCount = new TimeCount(this);

        taskTitle.setText("");
        reminder_contact_name.setText("");
        taskDate.setText("");
        reminder_type.setText("");
        reminder_phone.setText("");
        repeatInterval.setText("");

        taskIcon.setImageDrawable(ViewUtils.getDrawable(this, cs.getCategoryIndicator(categoryColor)));
        taskTitle.setText(title);
        reminder_type.setText(ReminderUtils.getTypeString(this, type));

        if (type.startsWith(Constants.TYPE_MONTHDAY)) {
            if (type.startsWith(Constants.TYPE_MONTHDAY_CALL)) {
                reminder_phone.setText(number);
                String name = Contacts.getNameFromNumber(number, this);
                if (name != null) {
                    reminder_contact_name.setText(name);
                } else {
                    reminder_contact_name.setText("");
                }
            } else if (type.startsWith(Constants.TYPE_MONTHDAY_MESSAGE)) {
                reminder_phone.setText(number);
                String name = Contacts.getNameFromNumber(number, this);
                if (name != null) {
                    reminder_contact_name.setText(name);
                } else {
                    reminder_contact_name.setText("");
                }
            }

            leftTimeIcon.setImageDrawable(mCount.
                    getDifference(due));
            repeatInterval.setVisibility(View.GONE);
            taskDate.setText(TimeUtil.getFullDateTime(due, is24));

            if (isDone == 0) {
                leftTime.setText(mCount.getRemaining(due));
            }
        } else if (type.startsWith(Constants.TYPE_WEEKDAY)) {
            if (type.matches(Constants.TYPE_WEEKDAY_CALL)) {
                reminder_phone.setText(number);
                String name = Contacts.getNameFromNumber(number, this);
                if (name != null) {
                    reminder_contact_name.setText(name);
                } else {
                    reminder_contact_name.setText("");
                }
            } else if (type.matches(Constants.TYPE_WEEKDAY_MESSAGE)) {
                reminder_phone.setText(number);
                String name = Contacts.getNameFromNumber(number, this);
                if (name != null) {
                    reminder_contact_name.setText(name);
                } else {
                    reminder_contact_name.setText("");
                }
            }

            leftTimeIcon.setImageDrawable(mCount.
                    getDifference(due));
            repeatInterval.setVisibility(View.GONE);
            if (isDone == 0) {
                leftTime.setText(mCount.getRemaining(due));
            }
            taskDate.setText(TimeUtil.getFullDateTime(due, is24));
        } else {
            if (type.matches(Constants.TYPE_CALL) || type.matches(Constants.TYPE_LOCATION_CALL) ||
                    type.matches(Constants.TYPE_LOCATION_OUT_CALL)) {
                reminder_phone.setText(number);
                String name = Contacts.getNameFromNumber(number, this);
                if (name != null) {
                    reminder_contact_name.setText(name);
                } else {
                    reminder_contact_name.setText("");
                }
            } else if (type.matches(Constants.TYPE_MESSAGE) || type.matches(Constants.TYPE_LOCATION_MESSAGE) ||
                    type.matches(Constants.TYPE_LOCATION_OUT_MESSAGE)) {
                reminder_phone.setText(number);
                String name = Contacts.getNameFromNumber(number, this);
                if (name != null) {
                    reminder_contact_name.setText(name);
                } else {
                    reminder_contact_name.setText("");
                }
            } else if (type.startsWith(Constants.TYPE_SKYPE)) {
                reminder_phone.setText(number);
                reminder_contact_name.setText(number);
            } else if (type.matches(Constants.TYPE_APPLICATION)) {
                PackageManager packageManager = getPackageManager();
                ApplicationInfo applicationInfo = null;
                try {
                    applicationInfo = packageManager.getApplicationInfo(number, 0);
                } catch (final PackageManager.NameNotFoundException ignored) {
                }
                final String name = (String) ((applicationInfo != null) ?
                        packageManager.getApplicationLabel(applicationInfo) : "???");
                reminder_phone.setText(number);
                reminder_contact_name.setText(name);
            } else if (type.matches(Constants.TYPE_APPLICATION_BROWSER)) {
                reminder_phone.setText(number);
                reminder_contact_name.setText(number);
            }

            if (type.matches(Constants.TYPE_CALL) || type.matches(Constants.TYPE_MESSAGE) ||
                    type.matches(Constants.TYPE_REMINDER) || type.startsWith(Constants.TYPE_SKYPE) ||
                    type.startsWith(Constants.TYPE_APPLICATION)) {
                leftTimeIcon.setImageDrawable(mCount.
                        getDifference(due));
                repeatInterval.setText(repeat);
            } else if (type.matches(Constants.TYPE_TIME)) {
                leftTimeIcon.setImageDrawable(mCount.
                        getDifference(due));
                repeatInterval.setText(repeat);
            } else {
                if (type.startsWith(Constants.TYPE_LOCATION) || type.startsWith(Constants.TYPE_LOCATION_OUT)) {
                    leftTimeIcon.setVisibility(View.GONE);
                    repeatInterval.setVisibility(View.GONE);
                } else {
                    leftTimeIcon.setVisibility(View.GONE);
                    repeatInterval.setText("0");
                }
            }

            if (lat != 0.0 || lon != 0.0) {
                taskDate.setText(String.format("%.5f", lat) + "\n" + String.format("%.5f", lon));
                leftTime.setVisibility(View.GONE);
            } else {
                if (isDone == 0) {
                    leftTime.setText(mCount.getRemaining(due));
                }
                taskDate.setText(TimeUtil.getFullDateTime(due, is24));
            }
        }

        if (type.matches(Constants.TYPE_TIME) && archived == 0){
            if (exclusion != null){
                if (new Recurrence(exclusion).isRange()){
                    leftTimeIcon.setVisibility(View.GONE);
                    taskDate.setText(R.string.paused);
                } else {
                    leftTimeIcon.setVisibility(View.VISIBLE);
                }
            }
        }
    }
}