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

import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.datas.CalendarData;
import com.cray.software.justreminder.datas.PagerItem;
import com.cray.software.justreminder.dialogs.AddBirthday;
import com.cray.software.justreminder.dialogs.BirthdaysList;
import com.cray.software.justreminder.dialogs.QuickAddReminder;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.Recognizer;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.TimeCount;
import com.cray.software.justreminder.interfaces.Configs;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.interfaces.Intervals;
import com.cray.software.justreminder.utils.ReminderUtils;
import com.cray.software.justreminder.views.CircularProgress;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class CalendarActivity extends AppCompatActivity {

    ColorSetter cSetter;
    DataBase db;
    SharedPrefs sPrefs;
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
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
                sPrefs.saveInt(Constants.APP_UI_PREFERENCES_LAST_CALENDAR_VIEW, 0);
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

                if (!sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_USE_CONTACTS))
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
            sPrefs.saveInt(Constants.APP_UI_PREFERENCES_LAST_CALENDAR_VIEW, 0);
        } else {
            cal.setTimeInMillis(System.currentTimeMillis());
            showEvents(cal.getTime());
            sPrefs.saveInt(Constants.APP_UI_PREFERENCES_LAST_CALENDAR_VIEW, 0);
        }
    }

    public void startVoiceRecognitionActivity() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        sPrefs = new SharedPrefs(this);
        if (!sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_AUTO_LANGUAGE)) {
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, sPrefs.loadPrefs(Constants.APP_UI_PREFERENCES_VOICE_LANGUAGE));
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
        if (!sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_USE_CONTACTS)) Toast.makeText(CalendarActivity.this, getString(R.string.calendar_birthday_info), Toast.LENGTH_LONG).show();
        else startActivity(new Intent(CalendarActivity.this, AddBirthday.class));
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
        int currentDay;
        int currentMonth;
        int currentYear;

        sPrefs = new SharedPrefs(getApplicationContext());
        int hour = sPrefs.loadInt(Constants.APP_UI_PREFERENCES_BIRTHDAY_REMINDER_HOUR);
        int minute = sPrefs.loadInt(Constants.APP_UI_PREFERENCES_BIRTHDAY_REMINDER_MINUTE);
        boolean isFeature = sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_CALENDAR_FEATURE_TASKS);
        boolean isRemindersEnabled = sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_REMINDERS_IN_CALENDAR);

        db = new DataBase(CalendarActivity.this);
        if (!db.isOpen()) db.open();

        pagerData.clear();

        int position = 0;
        int targetPosition = -1;
        do {
            ArrayList<CalendarData> datas = new ArrayList<>();
            datas.clear();
            currentDay = calendar.get(Calendar.DAY_OF_MONTH);
            currentMonth = calendar.get(Calendar.MONTH);
            currentYear = calendar.get(Calendar.YEAR);
            Cursor c = db.getEvents(currentDay, currentMonth);
            if (c != null && c.moveToFirst()){
                do {
                    String birthday = c.getString(c.getColumnIndex(Constants.ContactConstants.COLUMN_CONTACT_BIRTHDAY));
                    String name = c.getString(c.getColumnIndex(Constants.ContactConstants.COLUMN_CONTACT_NAME));
                    long id = c.getLong(c.getColumnIndex(Constants.ContactConstants.COLUMN_ID));
                    String number = c.getString(c.getColumnIndex(Constants.ContactConstants.COLUMN_CONTACT_NUMBER));
                    Date date1 = null;
                    try {
                        date1 = format.parse(birthday);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    if (date1 != null) {
                        Calendar calendar1 = Calendar.getInstance();
                        calendar1.setTime(date1);
                        int bDay = calendar1.get(Calendar.DAY_OF_MONTH);
                        int bMonth = calendar1.get(Calendar.MONTH);
                        calendar1.setTimeInMillis(System.currentTimeMillis());
                        calendar1.set(Calendar.MONTH, bMonth);
                        calendar1.set(Calendar.DAY_OF_MONTH, bDay);
                        calendar1.set(Calendar.HOUR_OF_DAY, hour);
                        calendar1.set(Calendar.MINUTE, minute);
                        if (bDay == currentDay && currentMonth == bMonth) {
                            datas.add(new CalendarData("birthday", name, number, id, calendar1.getTimeInMillis()));
                        }
                    }
                } while (c.moveToNext());
            }
            if (c != null) c.close();

            if (isRemindersEnabled) {
                Cursor s = db.queryGroup();
                if (s != null && s.moveToFirst()) {
                    do {
                        int myHour = s.getInt(s.getColumnIndex(Constants.COLUMN_HOUR));
                        int myMinute = s.getInt(s.getColumnIndex(Constants.COLUMN_MINUTE));
                        int myDay = s.getInt(s.getColumnIndex(Constants.COLUMN_DAY));
                        int myMonth = s.getInt(s.getColumnIndex(Constants.COLUMN_MONTH));
                        int myYear = s.getInt(s.getColumnIndex(Constants.COLUMN_YEAR));
                        int repCode = s.getInt(s.getColumnIndex(Constants.COLUMN_REPEAT));
                        int remCount = s.getInt(s.getColumnIndex(Constants.COLUMN_REMINDERS_COUNT));
                        long afterTime = s.getLong(s.getColumnIndex(Constants.COLUMN_REMIND_TIME));
                        String mType = s.getString(s.getColumnIndex(Constants.COLUMN_TYPE));
                        String name = s.getString(s.getColumnIndex(Constants.COLUMN_TEXT));
                        String number = s.getString(s.getColumnIndex(Constants.COLUMN_NUMBER));
                        String weekdays = s.getString(s.getColumnIndex(Constants.COLUMN_WEEKDAYS));
                        int isDone = s.getInt(s.getColumnIndex(Constants.COLUMN_IS_DONE));
                        long id = s.getLong(s.getColumnIndex(Constants.COLUMN_ID));
                        if ((mType.startsWith(Constants.TYPE_SKYPE) ||
                                mType.matches(Constants.TYPE_CALL) ||
                                mType.startsWith(Constants.TYPE_APPLICATION) ||
                                mType.matches(Constants.TYPE_MESSAGE) ||
                                mType.matches(Constants.TYPE_REMINDER) ||
                                mType.matches(Constants.TYPE_TIME)) && isDone == 0) {
                            long time = TimeCount.getEventTime(myYear, myMonth, myDay, myHour, myMinute, 0,
                                    afterTime, repCode, remCount, 0);
                            Calendar calendar1 = Calendar.getInstance();
                            calendar1.setTimeInMillis(time);
                            int mDay = calendar1.get(Calendar.DAY_OF_MONTH);
                            int mMonth = calendar1.get(Calendar.MONTH);
                            int mYear = calendar1.get(Calendar.YEAR);
                            if (time > 0 && mDay == currentDay && mMonth == currentMonth && mYear == currentYear) {
                                if (number == null) number = "0";
                                datas.add(new CalendarData("reminder", name, number, id, time));
                            }
                            if (!mType.matches(Constants.TYPE_TIME) && isFeature && repCode > 0) {
                                int days = 0;
                                do {
                                    calendar1.setTimeInMillis(time + (repCode * Intervals.MILLS_INTERVAL_DAY));
                                    time = calendar1.getTimeInMillis();
                                    mDay = calendar1.get(Calendar.DAY_OF_MONTH);
                                    mMonth = calendar1.get(Calendar.MONTH);
                                    mYear = calendar1.get(Calendar.YEAR);
                                    days = days + repCode;
                                    if (time > 0 && mDay == currentDay && mMonth == currentMonth && mYear == currentYear) {
                                        if (number == null) number = "0";
                                        datas.add(new CalendarData("reminder", name, number, id, time));
                                    }
                                } while (days < Configs.MAX_DAYS_COUNT);
                            }
                        } else if (mType.startsWith(Constants.TYPE_WEEKDAY) && isDone == 0) {
                            long time = TimeCount.getNextWeekdayTime(myHour, myMinute, weekdays, 0);
                            Calendar calendar1 = Calendar.getInstance();
                            calendar1.setTimeInMillis(time);
                            int mDay = calendar1.get(Calendar.DAY_OF_MONTH);
                            int mMonth = calendar1.get(Calendar.MONTH);
                            int mYear = calendar1.get(Calendar.YEAR);
                            if (time > 0 && mDay == currentDay && mMonth == currentMonth && mYear == currentYear) {
                                if (number == null) number = "0";
                                datas.add(new CalendarData("reminder", name, number, id, time));
                            }
                            int days = 0;
                            if (isFeature) {
                                ArrayList<Integer> list = ReminderUtils.getRepeatArray(weekdays);
                                do {
                                    calendar1.setTimeInMillis(time + Intervals.MILLS_INTERVAL_DAY);
                                    time = calendar1.getTimeInMillis();
                                    int weekDay = calendar1.get(Calendar.DAY_OF_WEEK);
                                    days = days + 1;
                                    if (list.get(weekDay - 1) == 1) {
                                        int sDay = calendar1.get(Calendar.DAY_OF_MONTH);
                                        int sMonth = calendar1.get(Calendar.MONTH);
                                        int sYear = calendar1.get(Calendar.YEAR);
                                        if (time > 0 && sDay == currentDay && sMonth == currentMonth && sYear == currentYear) {
                                            if (number == null) number = "0";
                                            datas.add(new CalendarData("reminder", name, number, id, time));
                                        }
                                    }
                                } while (days < Configs.MAX_DAYS_COUNT);
                            }
                        } else if (mType.startsWith(Constants.TYPE_MONTHDAY) && isDone == 0){
                            long time = TimeCount.getNextMonthDayTime(myHour, myMinute, myDay, 0);
                            Calendar calendar1 = Calendar.getInstance();
                            if (time > 0) {
                                calendar1.setTimeInMillis(time);
                                int mDay = calendar1.get(Calendar.DAY_OF_MONTH);
                                int mMonth = calendar1.get(Calendar.MONTH);
                                int mYear = calendar1.get(Calendar.YEAR);
                                if (time > 0 && mDay == currentDay && mMonth == currentMonth && mYear == currentYear) {
                                    if (number == null) number = "0";
                                    datas.add(new CalendarData("reminder", name, number, id, time));
                                }
                            }
                            int days = 1;
                            if (isFeature){
                                do {
                                    time = TimeCount.getNextMonthDayTime(myDay, calendar1.getTimeInMillis(), days);
                                    days = days + 1;
                                    calendar1.setTimeInMillis(time);
                                    int mDay = calendar1.get(Calendar.DAY_OF_MONTH);
                                    int mMonth = calendar1.get(Calendar.MONTH);
                                    int mYear = calendar1.get(Calendar.YEAR);
                                    if (time > 0 && mDay == currentDay && mMonth == currentMonth && mYear == currentYear) {
                                        if (number == null) number = "0";
                                        datas.add(new CalendarData("reminder", name, number, id, time));
                                    }
                                } while (days < Configs.MAX_MONTH_COUNT);
                            }
                        }
                    } while (s.moveToNext());
                }
                if (s != null) s.close();
            }
            if (db != null) db.close();

            if (currentDay == targetDay && currentMonth == targetMonth && currentYear == targetYear){
                targetPosition = position;
                pagerData.add(new PagerItem(datas, position, 1, currentDay, currentMonth, currentYear));
            } else {
                pagerData.add(new PagerItem(datas, position, 0, currentDay, currentMonth, currentYear));
            }

            position++;
            calendar.setTimeInMillis(calendar.getTimeInMillis() + AlarmManager.INTERVAL_DAY);
        } while (position < Configs.MAX_DAYS_COUNT);

        calendarLayout.setVisibility(View.GONE);
        pager.setVisibility(View.VISIBLE);

        progress.setVisibility(View.GONE);
        MyFragmentPagerAdapter pagerAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager(), pagerData);
        pager.setAdapter(pagerAdapter);
        pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {

            }

            @Override
            public void onPageSelected(int i) {
                int day = pagerData.get(i).getDay();
                int month = pagerData.get(i).getMonth();
                currentEvent.setText(day + "/" + (month + 1));
                ArrayList<CalendarData> data = pagerData.get(i).getDatas();
                if (data.size() > 0) dateMills = data.get(0).getDate();
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

        pager.setCurrentItem(targetPosition);

        currentEvent.setClickable(false);
        title.setText(getString(R.string.birthdays_dialog_title));
    }

    private class MyFragmentPagerAdapter extends FragmentPagerAdapter {

        ArrayList<PagerItem> datas;

        public MyFragmentPagerAdapter(FragmentManager fm, ArrayList<PagerItem> datas) {
            super(fm);
            this.datas = datas;
        }

        @Override
        public Fragment getItem(int position) {
            return BirthdaysList.newInstance(position, datas.get(position).getDatas());
        }

        @Override
        public int getCount() {
            return datas.size();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        sPrefs = new SharedPrefs(CalendarActivity.this);
        Calendar calendar = Calendar.getInstance();
        if (dateMills != 0){
            calendar.setTimeInMillis(dateMills);
            showEvents(calendar.getTime());
            sPrefs.saveInt(Constants.APP_UI_PREFERENCES_LAST_CALENDAR_VIEW, 0);
        } else {
            calendar.setTimeInMillis(System.currentTimeMillis());
            showEvents(calendar.getTime());
            sPrefs.saveInt(Constants.APP_UI_PREFERENCES_LAST_CALENDAR_VIEW, 0);
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
