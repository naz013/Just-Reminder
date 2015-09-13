package com.cray.software.justreminder;

import android.app.AlarmManager;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.cray.software.justreminder.adapters.CalendarPagerAdapter;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.datas.EventsDataProvider;
import com.cray.software.justreminder.datas.PagerItem;
import com.cray.software.justreminder.dialogs.AddBirthday;
import com.cray.software.justreminder.dialogs.BirthdaysList;
import com.cray.software.justreminder.dialogs.QuickAddReminder;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.Recognizer;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.interfaces.Configs;
import com.cray.software.justreminder.interfaces.Prefs;
import com.cray.software.justreminder.views.CircularProgress;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class CalendarActivity extends AppCompatActivity {

    ColorSetter cSetter;
    DataBase db;
    SharedPrefs sPrefs;
    Toolbar toolbar;
    FloatingActionsMenu mainMenu;
    FloatingActionButton addReminder, addBirthday;
    long dateMills;
    ImageButton voiceButton;
    Button currentEvent;
    TextView title;
    ViewPager pager;
    FrameLayout calendarLayout;
    CircularProgress progress;

    public static final int VOICE_RECOGNITION_REQUEST_CODE = 109;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cSetter = new ColorSetter(CalendarActivity.this);
        setTheme(cSetter.getStyle());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(cSetter.colorStatus());
        }
        setContentView(R.layout.calender_layout);
        setRequestedOrientation(cSetter.getRequestOrientation());

        sPrefs = new SharedPrefs(CalendarActivity.this);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        title = (TextView) findViewById(R.id.title);
        title.setText(getString(R.string.calendar_fragment));

        voiceButton = (ImageButton) findViewById(R.id.voiceButton);
        voiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startVoiceRecognitionActivity();
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

        mainMenu = (FloatingActionsMenu) findViewById(R.id.mainMenu);

        addBirthday = new FloatingActionButton(getBaseContext());
        addBirthday.setTitle(getString(R.string.new_birthday));
        addBirthday.setSize(FloatingActionButton.SIZE_MINI);
        addBirthday.setIcon(R.drawable.ic_cake_grey600_24dp);
        addBirthday.setColorNormal(getResources().getColor(R.color.colorWhite));
        addBirthday.setColorPressed(getResources().getColor(R.color.grey_light));
        addBirthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mainMenu.isExpanded()) mainMenu.collapse();

                if (!sPrefs.loadBoolean(Prefs.BIRTHDAY_REMINDER))
                    Toast.makeText(CalendarActivity.this, getString(R.string.calendar_birthday_info), Toast.LENGTH_LONG).show();
                else {
                    addBirthday();
                }
            }
        });

        addReminder = new FloatingActionButton(getBaseContext());
        addReminder.setTitle(getString(R.string.new_reminder));
        addReminder.setSize(FloatingActionButton.SIZE_NORMAL);
        addReminder.setIcon(R.drawable.ic_alarm_add_grey600_24dp);
        addReminder.setColorNormal(getResources().getColor(R.color.colorWhite));
        addReminder.setColorPressed(getResources().getColor(R.color.grey_light));
        addReminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mainMenu.isExpanded()) mainMenu.collapse();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(CalendarActivity.this, QuickAddReminder.class)
                                .putExtra("date", dateMills));
                    }
                }, 150);
            }
        });

        mainMenu.addButton(addBirthday);
        mainMenu.addButton(addReminder);

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

    public void startVoiceRecognitionActivity() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        sPrefs = new SharedPrefs(this);
        if (!sPrefs.loadBoolean(Prefs.AUTO_LANGUAGE)) {
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, sPrefs.loadPrefs(Prefs.VOICE_LANGUAGE));
        } else intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.voice_say_something));
        try {
            startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
        } catch (ActivityNotFoundException e){
            Toast t = Toast.makeText(getApplicationContext(),
                    getString(R.string.recognizer_not_found_error_message),
                    Toast.LENGTH_SHORT);
            t.show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
            ArrayList matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

            new Recognizer(this).selectTask(matches, false);
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void addBirthday() {
        sPrefs = new SharedPrefs(CalendarActivity.this);
        if (!sPrefs.loadBoolean(Prefs.BIRTHDAY_REMINDER)) {
            Toast.makeText(CalendarActivity.this, getString(R.string.calendar_birthday_info),
                    Toast.LENGTH_LONG).show();
        } else startActivity(new Intent(CalendarActivity.this, AddBirthday.class));
    }

    ArrayList<PagerItem> pagerData = new ArrayList<>();

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

        db = new DataBase(CalendarActivity.this);
        if (!db.isOpen()) db.open();

        EventsDataProvider provider = new EventsDataProvider();
        Cursor c = db.getBirthdays();
        provider.setBirthdays(c);
        provider.setTime(hour, minute);
        if (isRemindersEnabled) {
            Cursor s = db.getActiveReminders();
            provider.setReminders(s);
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

            ArrayList<EventsDataProvider.EventsItem> datas =
                    provider.getMatches(mDay, mMonth, mYear);

            if (mDay == targetDay && mMonth == targetMonth && mYear == targetYear){
                targetPosition = position;
                pagerData.add(new PagerItem(datas, position, 1, mDay, mMonth, mYear));
            } else {
                pagerData.add(new PagerItem(datas, position, 0, mDay, mMonth, mYear));
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
                currentEvent.setText(day + "/" + (month + 1) + "/" + year);
                ArrayList<EventsDataProvider.EventsItem> data = pagerData.get(i).getDatas();
                if (data.size() > 0) dateMills = data.get(0).getDate();
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

        pager.setCurrentItem(targetPosition);

        int i = pager.getCurrentItem();
        int day = pagerData.get(i).getDay();
        int month = pagerData.get(i).getMonth();
        int year = pagerData.get(i).getYear();
        currentEvent.setText(day + "/" + (month + 1) + "/" + year);
        ArrayList<EventsDataProvider.EventsItem> data = pagerData.get(i).getDatas();
        if (data.size() > 0) dateMills = data.get(0).getDate();

        currentEvent.setClickable(false);
        title.setText(getString(R.string.birthdays_dialog_title));
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
