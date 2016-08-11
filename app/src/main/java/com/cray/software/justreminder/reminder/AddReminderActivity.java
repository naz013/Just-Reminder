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

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.cloud.GTasksHelper;
import com.cray.software.justreminder.constants.Configs;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.groups.GroupHelper;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.helpers.TimeCount;
import com.cray.software.justreminder.modules.Module;
import com.cray.software.justreminder.reminder.json.JExport;
import com.cray.software.justreminder.reminder.json.JRecurrence;
import com.cray.software.justreminder.reminder.json.JsonModel;
import com.cray.software.justreminder.roboto_views.RoboCheckBox;
import com.cray.software.justreminder.roboto_views.RoboEditText;
import com.cray.software.justreminder.utils.ViewUtils;
import com.cray.software.justreminder.views.DateTimeView;
import com.cray.software.justreminder.views.RepeatView;

import java.util.Calendar;

public class AddReminderActivity extends AppCompatActivity {

    private RoboEditText task_text;
    private RoboCheckBox taskExport;
    private RepeatView repeatView;

    private int mHour = 0;
    private int mMinute = 0;
    private int mYear = 0;
    private int mMonth = 0;
    private int mDay = 1;
    private long mRepeat;

    private GTasksHelper gtx = new GTasksHelper(AddReminderActivity.this);

    private DateTimeView.OnSelectListener mDateTimeCallback = new DateTimeView.OnSelectListener() {
        @Override
        public void onDateSelect(long mills, int day, int month, int year) {
            mYear = year;
            mMonth = month;
            mDay = day;
            if (repeatView != null) repeatView.setDateTime(mYear, mMonth, mDay, mHour, mMinute);
        }

        @Override
        public void onTimeSelect(long mills, int hour, int minute) {
            mHour = hour;
            mMinute = minute;
            if (repeatView != null) repeatView.setDateTime(mYear, mMonth, mDay, mHour, mMinute);
        }
    };
    private RepeatView.OnRepeatListener mRepeatCallback = new RepeatView.OnRepeatListener() {
        @Override
        public void onProgress(int progress) {
            mRepeat = progress * TimeCount.DAY;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ColorSetter cs = ColorSetter.getInstance(AddReminderActivity.this);
        setTheme(cs.getStyle());
        if (Module.isLollipop()) {
            getWindow().setStatusBarColor(ViewUtils.getColor(this, cs.colorPrimaryDark()));
        }
        Intent i = getIntent();
        long receivedDate = i.getLongExtra("date", 0);
        setContentView(R.layout.quick_add_reminder_layout);
        setRequestedOrientation(cs.getRequestOrientation());
        initActionBar();
        findViewById(R.id.windowBackground).setBackgroundColor(cs.getBackgroundStyle());
        task_text = (RoboEditText) findViewById(R.id.task_text);
        taskExport = (RoboCheckBox) findViewById(R.id.taskExport);
        if (gtx.isLinked()) {
            taskExport.setVisibility(View.VISIBLE);
        }

        Calendar c = Calendar.getInstance();
        if (receivedDate != 0) {
            c.setTimeInMillis(receivedDate);
        } else {
            c.setTimeInMillis(System.currentTimeMillis());
        }
        mHour = c.get(Calendar.HOUR_OF_DAY);
        mMinute = c.get(Calendar.MINUTE);
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);

        repeatView = (RepeatView) findViewById(R.id.repeatView);
        repeatView.setListener(mRepeatCallback);
        repeatView.setMax(Configs.REPEAT_SEEKBAR_MAX);

        DateTimeView dateView = (DateTimeView) findViewById(R.id.dateView);
        dateView.setListener(mDateTimeCallback);
        dateView.setDateTime(updateCalendar(c.getTimeInMillis(), false));
    }

    protected long updateCalendar(long millis, boolean deny) {
        final Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(millis);
        if (mYear > 0 && !deny) cal.set(mYear, mMonth, mDay, mHour, mMinute);
        else {
            mYear = cal.get(Calendar.YEAR);
            mMonth = cal.get(Calendar.MONTH);
            mDay = cal.get(Calendar.DAY_OF_MONTH);
            mHour = cal.get(Calendar.HOUR_OF_DAY);
            mMinute = cal.get(Calendar.MINUTE);
        }
        return cal.getTimeInMillis();
    }

    private void initActionBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
    }

    private void saveDateTask() {
        String text = task_text.getText().toString().trim();
        if (text.matches("")) {
            task_text.setError(getString(R.string.must_be_not_empty));
            return;
        }
        String type = Constants.TYPE_REMINDER;
        String categoryId = GroupHelper.getInstance(this).getDefaultUuId();
        long startTime = ReminderUtils.getTime(mDay, mMonth, mYear, mHour, mMinute, 0);
        boolean isCalendar = SharedPrefs.getInstance(this).getBoolean(Prefs.EXPORT_TO_CALENDAR);
        boolean isStock = SharedPrefs.getInstance(this).getBoolean(Prefs.EXPORT_TO_STOCK);
        boolean isTasks = gtx.isLinked() && taskExport.isChecked();
        int isCal = isCalendar || isStock ? 1 : 0;
        JExport jExport = new JExport(isTasks ? 1 : 0, isCal, null);
        JRecurrence jRecurrence = new JRecurrence(0, mRepeat, -1, null, 0);
        JsonModel jsonModel = new JsonModel(text, type, categoryId,
                SyncHelper.generateID(), startTime, startTime, jRecurrence, null, jExport);
        long remId = new DateType(AddReminderActivity.this, Constants.TYPE_REMINDER).save(new ReminderItem(jsonModel));
        if (isCalendar || isStock) {
            ReminderUtils.exportToCalendar(this, text, startTime, remId, isCalendar, isStock);
        }
        if (isTasks) {
            ReminderUtils.exportToTasks(this, text, startTime, remId);
        }
        SharedPrefs.getInstance(this).putBoolean(Prefs.REMINDER_CHANGED, true);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.save_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                saveDateTask();
                return true;
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
