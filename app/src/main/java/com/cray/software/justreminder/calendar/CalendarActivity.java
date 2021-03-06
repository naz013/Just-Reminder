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

package com.cray.software.justreminder.calendar;

import android.app.AlarmManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.constants.Configs;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.dialogs.ActionPickerDialog;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.Recognize;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.roboto_views.RoboButton;
import com.cray.software.justreminder.roboto_views.RoboTextView;
import com.cray.software.justreminder.utils.SuperUtil;
import com.cray.software.justreminder.utils.ViewUtils;
import com.cray.software.justreminder.views.CircularProgress;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CalendarActivity extends AppCompatActivity {

    private long dateMills;
    private RoboButton currentEvent;
    private RoboTextView title;
    private ViewPager pager;
    private FrameLayout calendarLayout;
    private CircularProgress progress;
    private ArrayList<EventsPagerItem> pagerData = new ArrayList<>();
    private int lastPosition = -1;

    public static final int VOICE_RECOGNITION_REQUEST_CODE = 109;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ColorSetter cSetter = ColorSetter.getInstance(CalendarActivity.this);
        setTheme(cSetter.getStyle());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ViewUtils.getColor(this, cSetter.colorPrimaryDark()));
        }
        setContentView(R.layout.calender_layout);
        setRequestedOrientation(cSetter.getRequestOrientation());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        title = (RoboTextView) findViewById(R.id.title);
        title.setText(getString(R.string.calendar));

        ImageButton voiceButton = (ImageButton) findViewById(R.id.voiceButton);
        voiceButton.setOnClickListener(view -> SuperUtil.startVoiceRecognitionActivity(CalendarActivity.this,
                VOICE_RECOGNITION_REQUEST_CODE, false));

        currentEvent = (RoboButton) findViewById(R.id.currentEvent);
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        int day = cal.get(Calendar.DAY_OF_MONTH);
        currentEvent.setText(String.valueOf(day));
        currentEvent.setOnClickListener(v -> {
            Calendar cal1 = Calendar.getInstance();
            cal1.setTimeInMillis(System.currentTimeMillis());
            showEvents(cal1.getTime());
            SharedPrefs.getInstance(this).putInt(Prefs.LAST_CALENDAR_VIEW, 0);
        });

        pager = (ViewPager) findViewById(R.id.pager);
        pager.setVisibility(View.GONE);

        calendarLayout = (FrameLayout) findViewById(R.id.calendarView);
        calendarLayout.setVisibility(View.GONE);

        progress = (CircularProgress) findViewById(R.id.progress);

        Intent intent = getIntent();
        dateMills = intent.getLongExtra("date", 0);

        findViewById(R.id.windowBackground).setBackgroundColor(cSetter.getBackgroundStyle());

        FloatingActionButton mFab = (FloatingActionButton) findViewById(R.id.fab);
        if (dateMills == 0) dateMills = System.currentTimeMillis();
        mFab.setOnClickListener(v -> startActivity(new Intent(CalendarActivity.this, ActionPickerDialog.class)
                .putExtra("date", dateMills)));

        if (dateMills != 0){
            cal.setTimeInMillis(dateMills);
            showEvents(cal.getTime());
            SharedPrefs.getInstance(this).putInt(Prefs.LAST_CALENDAR_VIEW, 0);
        } else {
            cal.setTimeInMillis(System.currentTimeMillis());
            showEvents(cal.getTime());
            SharedPrefs.getInstance(this).putInt(Prefs.LAST_CALENDAR_VIEW, 0);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
            ArrayList matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

            new Recognize(this).parseResults(matches, false);
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void showEvents(Date date) {
        progress.setVisibility(View.VISIBLE);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int targetDay = calendar.get(Calendar.DAY_OF_MONTH);
        int targetMonth = calendar.get(Calendar.MONTH);
        int targetYear = calendar.get(Calendar.YEAR);
        calendar.setTimeInMillis(System.currentTimeMillis());
        int hour = SharedPrefs.getInstance(this).getInt(Prefs.BIRTHDAY_REMINDER_HOUR);
        int minute = SharedPrefs.getInstance(this).getInt(Prefs.BIRTHDAY_REMINDER_MINUTE);
        boolean isFeature = SharedPrefs.getInstance(this).getBoolean(Prefs.CALENDAR_FEATURE_TASKS);
        boolean isRemindersEnabled = SharedPrefs.getInstance(this).getBoolean(Prefs.REMINDERS_IN_CALENDAR);

        EventsDataProvider provider = new EventsDataProvider(this);
        provider.setBirthdays(true);
        provider.setTime(hour, minute);
        if (isRemindersEnabled) {
            provider.setReminders(true);
            provider.setFeature(isFeature);
        }
        provider.fillArray();

        pagerData.clear();

        int position = 0;
        int targetPosition = -1;
        do {
            int mDay = calendar.get(Calendar.DAY_OF_MONTH);
            int mMonth = calendar.get(Calendar.MONTH);
            int mYear = calendar.get(Calendar.YEAR);
            List<EventsItem> datas = provider.getMatches(mDay, mMonth, mYear);
            if (mDay == targetDay && mMonth == targetMonth && mYear == targetYear){
                targetPosition = position;
                pagerData.add(new EventsPagerItem(datas, position, 1, mDay, mMonth, mYear));
            } else {
                pagerData.add(new EventsPagerItem(datas, position, 0, mDay, mMonth, mYear));
            }

            position++;
            calendar.setTimeInMillis(calendar.getTimeInMillis() + AlarmManager.INTERVAL_DAY);
        } while (position < Configs.MAX_DAYS_COUNT);

        calendarLayout.setVisibility(View.GONE);
        pager.setVisibility(View.VISIBLE);

        progress.setVisibility(View.GONE);
        CalendarPagerAdapter pagerAdapter = new CalendarPagerAdapter(getSupportFragmentManager(), pagerData);
        pager.setAdapter(pagerAdapter);
        pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {

            }

            @Override
            public void onPageSelected(int i) {
                updateLastTime(i);
                lastPosition = i;
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

        pager.setCurrentItem(lastPosition != -1 ? lastPosition : targetPosition);
        updateLastTime(pager.getCurrentItem());
        currentEvent.setClickable(false);
        title.setText(R.string.events);
    }

    private void updateLastTime(int i) {
        EventsPagerItem item = pagerData.get(i);
        if (item != null) {
            int day = item.getDay();
            int month = item.getMonth();
            int year = item.getYear();
            currentEvent.setText(SuperUtil.appendString(String.valueOf(day), "/", String.valueOf(month + 1), "/", String.valueOf(year)));
            Calendar calendar = Calendar.getInstance();
            calendar.set(item.getYear(), item.getMonth(), item.getDay());
            dateMills = calendar.getTimeInMillis();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Calendar calendar = Calendar.getInstance();
        if (dateMills != 0){
            calendar.setTimeInMillis(dateMills);
        } else {
            calendar.setTimeInMillis(System.currentTimeMillis());
        }
        showEvents(calendar.getTime());
        SharedPrefs.getInstance(this).putInt(Prefs.LAST_CALENDAR_VIEW, 0);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
