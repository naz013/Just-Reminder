package com.cray.software.justreminder;

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
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import com.cray.software.justreminder.adapters.CalendarPagerAdapter;
import com.cray.software.justreminder.constants.Configs;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.datas.EventsDataProvider;
import com.cray.software.justreminder.datas.models.EventsItem;
import com.cray.software.justreminder.datas.models.EventsPagerItem;
import com.cray.software.justreminder.dialogs.ActionPickerDialog;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.Recognizer;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.utils.SuperUtil;
import com.cray.software.justreminder.utils.ViewUtils;
import com.cray.software.justreminder.views.CircularProgress;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class CalendarActivity extends AppCompatActivity {

    private SharedPrefs sPrefs;
    private long dateMills;
    private Button currentEvent;
    private TextView title;
    private ViewPager pager;
    private FrameLayout calendarLayout;
    private CircularProgress progress;
    private ArrayList<EventsPagerItem> pagerData = new ArrayList<>();
    private int lastPosition = -1;

    public static final int VOICE_RECOGNITION_REQUEST_CODE = 109;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ColorSetter cSetter = new ColorSetter(CalendarActivity.this);
        setTheme(cSetter.getStyle());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ViewUtils.getColor(this, cSetter.colorPrimaryDark()));
        }
        setContentView(R.layout.calender_layout);
        setRequestedOrientation(cSetter.getRequestOrientation());

        sPrefs = new SharedPrefs(CalendarActivity.this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        title = (TextView) findViewById(R.id.title);
        title.setText(getString(R.string.calendar));

        ImageButton voiceButton = (ImageButton) findViewById(R.id.voiceButton);
        voiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SuperUtil.startVoiceRecognitionActivity(CalendarActivity.this, VOICE_RECOGNITION_REQUEST_CODE);
            }
        });

        currentEvent = (Button) findViewById(R.id.currentEvent);
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        int day = cal.get(Calendar.DAY_OF_MONTH);
        currentEvent.setText(String.valueOf(day));
        currentEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(System.currentTimeMillis());
                showEvents(cal.getTime());
                sPrefs.saveInt(Prefs.LAST_CALENDAR_VIEW, 0);
            }
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
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CalendarActivity.this, ActionPickerDialog.class)
                        .putExtra("date", dateMills));
            }
        });

        if (dateMills != 0){
            cal.setTimeInMillis(dateMills);
            showEvents(cal.getTime());
            sPrefs.saveInt(Prefs.LAST_CALENDAR_VIEW, 0);
        } else {
            cal.setTimeInMillis(System.currentTimeMillis());
            showEvents(cal.getTime());
            sPrefs.saveInt(Prefs.LAST_CALENDAR_VIEW, 0);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
            ArrayList matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

            new Recognizer(this).parseResults(matches, false);
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

        sPrefs = new SharedPrefs(getApplicationContext());
        int hour = sPrefs.loadInt(Prefs.BIRTHDAY_REMINDER_HOUR);
        int minute = sPrefs.loadInt(Prefs.BIRTHDAY_REMINDER_MINUTE);
        boolean isFeature = sPrefs.loadBoolean(Prefs.CALENDAR_FEATURE_TASKS);
        boolean isRemindersEnabled = sPrefs.loadBoolean(Prefs.REMINDERS_IN_CALENDAR);

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

            ArrayList<EventsItem> datas =
                    provider.getMatches(mDay, mMonth, mYear);

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
                int day = pagerData.get(i).getDay();
                int month = pagerData.get(i).getMonth();
                int year = pagerData.get(i).getYear();
                currentEvent.setText(SuperUtil.appendString(String.valueOf(day), "/", String.valueOf(month + 1), "/", String.valueOf(year)));
                ArrayList<EventsItem> data = pagerData.get(i).getDatas();
                if (data.size() > 0) dateMills = data.get(0).getDate();
                lastPosition = i;
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

        pager.setCurrentItem(lastPosition != -1 ? lastPosition : targetPosition);

        int i = pager.getCurrentItem();
        int day = pagerData.get(i).getDay();
        int month = pagerData.get(i).getMonth();
        int year = pagerData.get(i).getYear();
        currentEvent.setText(SuperUtil.appendString(String.valueOf(day), "/", String.valueOf(month + 1), "/", String.valueOf(year)));
        ArrayList<EventsItem> data = pagerData.get(i).getDatas();
        if (data.size() > 0) dateMills = data.get(0).getDate();

        currentEvent.setClickable(false);
        title.setText(R.string.events);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sPrefs = new SharedPrefs(CalendarActivity.this);
        Calendar calendar = Calendar.getInstance();
        if (dateMills != 0){
            calendar.setTimeInMillis(dateMills);
            showEvents(calendar.getTime());
            sPrefs.saveInt(Prefs.LAST_CALENDAR_VIEW, 0);
        } else {
            calendar.setTimeInMillis(System.currentTimeMillis());
            showEvents(calendar.getTime());
            sPrefs.saveInt(Prefs.LAST_CALENDAR_VIEW, 0);
        }
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
