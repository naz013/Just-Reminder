/**
 * Copyright 2016 Nazar Suhovich
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

package com.cray.software.justreminder.dialogs;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.constants.Configs;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.contacts.Contacts;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.Notifier;
import com.cray.software.justreminder.helpers.Recurrence;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.TimeCount;
import com.cray.software.justreminder.modules.Module;
import com.cray.software.justreminder.reminder.Reminder;
import com.cray.software.justreminder.reminder.ReminderDataProvider;
import com.cray.software.justreminder.reminder.ReminderModel;
import com.cray.software.justreminder.reminder.ReminderUtils;
import com.cray.software.justreminder.roboto_views.RoboSwitchCompat;
import com.cray.software.justreminder.roboto_views.RoboTextView;
import com.cray.software.justreminder.utils.IntervalUtil;
import com.cray.software.justreminder.utils.TimeUtil;

public class VoiceResult extends Activity {

    private ColorSetter cs = new ColorSetter(VoiceResult.this);
    private long id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(cs.getDialogStyle());
        setContentView(R.layout.voice_dialog_layout);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        findViewById(R.id.windowBackground).setBackgroundColor(cs.getBackgroundStyle());
        Intent intent = getIntent();
        id = intent.getLongExtra("ids", 0);

        findViewById(R.id.buttonEdit).setOnClickListener(v -> {
            Reminder.edit(id, VoiceResult.this);
            finish();
        });

        findViewById(R.id.buttonClose).setOnClickListener(v -> {
            new Notifier(VoiceResult.this).recreatePermanent();
            finish();
        });

        RelativeLayout reminderContainer = (RelativeLayout) findViewById(R.id.reminderContainer);
        RoboTextView leftTime = (RoboTextView) findViewById(R.id.remainingTime);
        RoboSwitchCompat check = (RoboSwitchCompat) findViewById(R.id.itemCheck);
        check.setVisibility(View.GONE);
        RoboTextView taskDate = (RoboTextView) findViewById(R.id.taskDate);
        taskDate.setText("");
        RoboTextView reminder_type = (RoboTextView) findViewById(R.id.reminder_type);
        reminder_type.setText("");
        RoboTextView reminder_phone = (RoboTextView) findViewById(R.id.reminder_phone);
        reminder_phone.setText("");
        RoboTextView repeatInterval = (RoboTextView) findViewById(R.id.repeatInterval);
        repeatInterval.setText("");

        RoboTextView taskTitle = (RoboTextView) findViewById(R.id.taskText);
        taskTitle.setText("");
        CardView itemCard = (CardView) findViewById(R.id.itemCard);
        itemCard.setCardBackgroundColor(cs.getCardStyle());
        if (Module.isLollipop()) {
            itemCard.setCardElevation(Configs.CARD_ELEVATION);
        }

        SharedPrefs prefs = new SharedPrefs(VoiceResult.this);
        boolean is24 = prefs.loadBoolean(Prefs.IS_24_TIME_FORMAT);

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

        reminderContainer.setVisibility(View.VISIBLE);

        TimeCount mCount = new TimeCount(this);

        taskTitle.setText(title);
        reminder_type.setText(ReminderUtils.getTypeString(this, type));

        if (type.startsWith(Constants.TYPE_MONTHDAY)) {
            if (type.startsWith(Constants.TYPE_MONTHDAY_CALL)) {
                reminder_phone.setText(number);
                String name = Contacts.getNameFromNumber(number, this);
                if (name != null) {
                    reminder_phone.setText(name + "(" + number + ")");
                }
            } else if (type.startsWith(Constants.TYPE_MONTHDAY_MESSAGE)) {
                reminder_phone.setText(number);
                String name = Contacts.getNameFromNumber(number, this);
                if (name != null) {
                    reminder_phone.setText(name + "(" + number + ")");
                }
            }

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
                    reminder_phone.setText(name + "(" + number + ")");
                }
            } else if (type.matches(Constants.TYPE_WEEKDAY_MESSAGE)) {
                reminder_phone.setText(number);
                String name = Contacts.getNameFromNumber(number, this);
                if (name != null) {
                    reminder_phone.setText(name + "(" + number + ")");
                }
            }
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
                    reminder_phone.setText(name + "(" + number + ")");
                }
            } else if (type.matches(Constants.TYPE_MESSAGE) || type.matches(Constants.TYPE_LOCATION_MESSAGE) ||
                    type.matches(Constants.TYPE_LOCATION_OUT_MESSAGE)) {
                reminder_phone.setText(number);
                String name = Contacts.getNameFromNumber(number, this);
                if (name != null) {
                    reminder_phone.setText(name + "(" + number + ")");
                }
            } else if (type.startsWith(Constants.TYPE_SKYPE)) {
                reminder_phone.setText(number);
            } else if (type.matches(Constants.TYPE_APPLICATION)) {
                PackageManager packageManager = getPackageManager();
                ApplicationInfo applicationInfo = null;
                try {
                    applicationInfo = packageManager.getApplicationInfo(number, 0);
                } catch (final PackageManager.NameNotFoundException ignored) {
                }
                final String name = (String) ((applicationInfo != null) ?
                        packageManager.getApplicationLabel(applicationInfo) : "???");
                reminder_phone.setText(name + "/" + number);
            } else if (type.matches(Constants.TYPE_APPLICATION_BROWSER)) {
                reminder_phone.setText(number);
            }

            if (type.matches(Constants.TYPE_CALL) || type.matches(Constants.TYPE_MESSAGE) ||
                    type.matches(Constants.TYPE_REMINDER) || type.startsWith(Constants.TYPE_SKYPE) ||
                    type.startsWith(Constants.TYPE_APPLICATION)) {
                repeatInterval.setText(repeat);
            } else if (type.matches(Constants.TYPE_TIME)) {
                repeatInterval.setText(repeat);
            } else {
                if (type.startsWith(Constants.TYPE_LOCATION) || type.startsWith(Constants.TYPE_LOCATION_OUT)) {
                    repeatInterval.setVisibility(View.GONE);
                } else {
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
                if (new Recurrence(exclusion).isRange()) {
                    taskDate.setText(R.string.paused);
                }
            }
        }
    }
}