package com.cray.software.justreminder;

import android.app.AlarmManager;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
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
import com.cray.software.justreminder.dialogs.AddBirthday;
import com.cray.software.justreminder.dialogs.BirthdaysList;
import com.cray.software.justreminder.dialogs.QuickAddReminder;
import com.cray.software.justreminder.fragments.StartFragment;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.TimeCount;
import com.cray.software.justreminder.interfaces.CalendarData;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.interfaces.Intervals;
import com.cray.software.justreminder.interfaces.PagerItem;
import com.cray.software.justreminder.views.CircularProgress;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class CalendarActivity extends AppCompatActivity {

    ColorSetter cSetter;
    CaldroidFragment calendarView;
    DataBase db;
    SharedPrefs sPrefs;
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
    Toolbar toolbar;
    boolean isCalendar;
    FloatingActionsMenu mainMenu;
    FloatingActionButton addReminder, addBirthday;
    long dateMills;
    ImageButton monthView;
    Button currentEvent;
    TextView title;
    ViewPager pager;
    FrameLayout calendarLayout;
    CircularProgress progress;

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

        monthView = (ImageButton) findViewById(R.id.monthView);
        monthView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMonth();
                if (isCalendar) {
                    if (calendarView != null) {
                        calendarView.refreshView();
                        calendarView.clearSelectedDates();
                    }
                    sPrefs = new SharedPrefs(CalendarActivity.this);
                    if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_REMINDERS_IN_CALENDAR)) {
                        loadReminders();
                    }

                    loadEvents();
                    sPrefs.saveInt(Constants.APP_UI_PREFERENCES_LAST_CALENDAR_VIEW, 1);
                }
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
            if (sPrefs.loadInt(Constants.APP_UI_PREFERENCES_LAST_CALENDAR_VIEW) == 1) {
                showMonth();
                sPrefs.saveInt(Constants.APP_UI_PREFERENCES_LAST_CALENDAR_VIEW, 1);
            } else {
                cal.setTimeInMillis(System.currentTimeMillis());
                showEvents(cal.getTime());
                sPrefs.saveInt(Constants.APP_UI_PREFERENCES_LAST_CALENDAR_VIEW, 0);
            }
        }
    }

    private void showMonth(){
        calendarView = new CaldroidFragment();
        Bundle args = new Bundle();
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        args.putInt(CaldroidFragment.MONTH, cal.get(Calendar.MONTH) + 1);
        args.putInt(CaldroidFragment.YEAR, cal.get(Calendar.YEAR));
        sPrefs = new SharedPrefs(CalendarActivity.this);
        if (sPrefs.loadInt(Constants.APP_UI_PREFERENCES_START_DAY) == 0) {
            args.putInt(CaldroidFragment.START_DAY_OF_WEEK, CaldroidFragment.SUNDAY);
        } else {
            args.putInt(CaldroidFragment.START_DAY_OF_WEEK, CaldroidFragment.MONDAY);
        }
        args.putBoolean(CaldroidFragment.SIX_WEEKS_IN_CALENDAR, true);
        calendarView.setArguments(args);
        calendarView.setMinDate(null);
        calendarView.setMaxDate(null);

        currentEvent.setText(String.valueOf(cal.get(Calendar.DAY_OF_MONTH)));
        currentEvent.setClickable(true);
        monthView.setVisibility(View.GONE);
        title.setText(getString(R.string.calendar_fragment));

        calendarLayout.setVisibility(View.VISIBLE);
        pager.setVisibility(View.GONE);

        FragmentTransaction t = getSupportFragmentManager().beginTransaction();
        t.replace(R.id.calendarView, calendarView);
        t.addToBackStack("calendar");
        t.commit();

        isCalendar = true;

        final CaldroidListener listener = new CaldroidListener() {

            @Override
            public void onSelectDate(Date date, View view) {
                showEvents(date);
                sPrefs.saveInt(Constants.APP_UI_PREFERENCES_LAST_CALENDAR_VIEW, 0);
            }

            @Override
            public void onChangeMonth(int month, int year) {
            }

            @Override
            public void onLongClickDate(Date date, View view) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);
                calendar.setTime(date);
                calendar.set(Calendar.HOUR_OF_DAY, hour);
                calendar.set(Calendar.MINUTE, minute);
                long dateMills = calendar.getTimeInMillis();
                startActivity(new Intent(CalendarActivity.this, QuickAddReminder.class)
                        .putExtra("date", dateMills));
            }

            @Override
            public void onCaldroidViewCreated() {
            }

        };

        calendarView.setCaldroidListener(listener);
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
        TimeCount mCount = new TimeCount(CalendarActivity.this);

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
                        int afterTime = s.getInt(s.getColumnIndex(Constants.COLUMN_REMIND_TIME));
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
                            long time = mCount.getEventTime(myYear, myMonth, myDay, myHour, myMinute, 0,
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
                                } while (days < 31);
                            }
                        } else if (mType.startsWith(Constants.TYPE_WEEKDAY) && isDone == 0) {
                            long time = mCount.getNextWeekdayTime(myHour, myMinute, weekdays, 0);
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
                                ArrayList<Integer> list = getRepeatArray(weekdays);
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
                                } while (days < 31);
                            }
                        }
                    } while (s.moveToNext());
                }
                if (s != null) s.close();
            }
            if (db != null) db.close();

            if (currentDay == targetDay && currentMonth == targetMonth && currentYear == targetYear){
                targetPosition = position;
                pagerData.add(new PagerItem(datas, position, 1, currentDay));
            } else {
                pagerData.add(new PagerItem(datas, position, 0, currentDay));
            }

            position++;
            calendar.setTimeInMillis(calendar.getTimeInMillis() + AlarmManager.INTERVAL_DAY);
        } while (position < 60);

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
                currentEvent.setText(String.valueOf(pagerData.get(i).getDay()));
                ArrayList<CalendarData> data = pagerData.get(i).getDatas();
                if (data.size() > 0) dateMills = data.get(0).getDate();
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

        pager.setCurrentItem(targetPosition);

        currentEvent.setClickable(false);
        monthView.setVisibility(View.VISIBLE);
        title.setText(getString(R.string.birthdays_dialog_title));

        isCalendar = false;
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
        if (isCalendar) {
            if (calendarView != null) {
                calendarView.refreshView();
                calendarView.clearSelectedDates();
            }
            sPrefs = new SharedPrefs(CalendarActivity.this);
            if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_REMINDERS_IN_CALENDAR)) {
                loadReminders();
            }

            loadEvents();
            sPrefs.saveInt(Constants.APP_UI_PREFERENCES_LAST_CALENDAR_VIEW, 1);
        }
    }

    private void loadReminders() {
        db = new DataBase(CalendarActivity.this);
        if (!db.isOpen()) db.open();
        sPrefs = new SharedPrefs(CalendarActivity.this);
        boolean isFeature = sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_CALENDAR_FEATURE_TASKS);
        TimeCount mCount = new TimeCount(CalendarActivity.this);
        ArrayList<Date> dates = new ArrayList<>();
        dates.clear();
        Cursor c = db.queryGroup();
        if (c != null && c.moveToFirst()){
            do {
                int myHour = c.getInt(c.getColumnIndex(Constants.COLUMN_HOUR));
                int myMinute = c.getInt(c.getColumnIndex(Constants.COLUMN_MINUTE));
                int myDay = c.getInt(c.getColumnIndex(Constants.COLUMN_DAY));
                int myMonth = c.getInt(c.getColumnIndex(Constants.COLUMN_MONTH));
                int myYear = c.getInt(c.getColumnIndex(Constants.COLUMN_YEAR));
                int repCode = c.getInt(c.getColumnIndex(Constants.COLUMN_REPEAT));
                int remCount = c.getInt(c.getColumnIndex(Constants.COLUMN_REMINDERS_COUNT));
                int afterTime = c.getInt(c.getColumnIndex(Constants.COLUMN_REMIND_TIME));
                String type = c.getString(c.getColumnIndex(Constants.COLUMN_TYPE));
                String weekdays = c.getString(c.getColumnIndex(Constants.COLUMN_WEEKDAYS));
                int isDone = c.getInt(c.getColumnIndex(Constants.COLUMN_IS_DONE));
                if ((type.startsWith(Constants.TYPE_SKYPE) ||
                        type.matches(Constants.TYPE_CALL) ||
                        type.startsWith(Constants.TYPE_APPLICATION) ||
                        type.matches(Constants.TYPE_MESSAGE) ||
                        type.matches(Constants.TYPE_REMINDER) ||
                        type.matches(Constants.TYPE_TIME)) && isDone == 0) {
                    long time = mCount.getEventTime(myYear, myMonth, myDay, myHour, myMinute, 0,
                            afterTime, repCode, remCount, 0);
                    Calendar calendar = Calendar.getInstance();
                    if (time > 0) {
                        calendar.setTimeInMillis(time);
                        int day = calendar.get(Calendar.DAY_OF_MONTH);
                        int month = calendar.get(Calendar.MONTH);
                        Date bdDate = getDate(0, month, day);
                        dates.add(bdDate);
                        int days = 0;
                        if (!type.matches(Constants.TYPE_TIME) && isFeature && repCode > 0){
                            do {
                                calendar.setTimeInMillis(time + (repCode * Intervals.MILLS_INTERVAL_DAY));
                                time = calendar.getTimeInMillis();
                                days = days + repCode;
                                day = calendar.get(Calendar.DAY_OF_MONTH);
                                month = calendar.get(Calendar.MONTH);
                                bdDate = getDate(0, month, day);
                                dates.add(bdDate);
                            } while (days < 61);
                        }
                    }
                } else if (type.startsWith(Constants.TYPE_WEEKDAY) && isDone == 0){
                    long time = mCount.getNextWeekdayTime(myHour, myMinute, weekdays, 0);
                    Calendar calendar = Calendar.getInstance();
                    if (time > 0) {
                        calendar.setTimeInMillis(time);
                        int day = calendar.get(Calendar.DAY_OF_MONTH);
                        int month = calendar.get(Calendar.MONTH);
                        Date bdDate = getDate(0, month, day);
                        dates.add(bdDate);
                    }
                    int days = 0;
                    if (isFeature){
                        ArrayList<Integer> list = getRepeatArray(weekdays);
                        do {
                            calendar.setTimeInMillis(time + Intervals.MILLS_INTERVAL_DAY);
                            time = calendar.getTimeInMillis();
                            int weekDay = calendar.get(Calendar.DAY_OF_WEEK);
                            days = days + 1;
                            if (list.get(weekDay - 1) == 1){
                                int day = calendar.get(Calendar.DAY_OF_MONTH);
                                int month = calendar.get(Calendar.MONTH);
                                Date bdDate = getDate(0, month, day);
                                dates.add(bdDate);
                            }
                        } while (days < 61);
                    }
                }
            } while (c.moveToNext());
        }
        if (c != null) {
            c.close();
        }
        if (db != null) db.close();

        for (int i = 0; i < dates.size(); i++) {
            if (calendarView != null) {
                calendarView.setBackgroundResourceForDate(cSetter.colorReminderCalendar(), dates.get(i));
            }
        }
    }

    private ArrayList<Integer> getRepeatArray(String weekdays){
        ArrayList<Integer> res = new ArrayList<>();
        if (Character.toString(weekdays.charAt(6)).matches(Constants.DAY_CHECKED))res.add(1);
        else res.add(0);
        if (Character.toString(weekdays.charAt(0)).matches(Constants.DAY_CHECKED))res.add(1);
        else res.add(0);
        if (Character.toString(weekdays.charAt(1)).matches(Constants.DAY_CHECKED))res.add(1);
        else res.add(0);
        if (Character.toString(weekdays.charAt(2)).matches(Constants.DAY_CHECKED))res.add(1);
        else res.add(0);
        if (Character.toString(weekdays.charAt(3)).matches(Constants.DAY_CHECKED))res.add(1);
        else res.add(0);
        if (Character.toString(weekdays.charAt(4)).matches(Constants.DAY_CHECKED))res.add(1);
        else res.add(0);
        if (Character.toString(weekdays.charAt(5)).matches(Constants.DAY_CHECKED))res.add(1);
        else res.add(0);
        return res;
    }

    private void loadEvents(){
        db = new DataBase(CalendarActivity.this);
        if (!db.isOpen()) db.open();
        ArrayList<Date> dates = new ArrayList<>();
        Cursor c = db.queryEvents();
        if (c != null && c.moveToFirst()){
            do {
                String birthday = c.getString(c.getColumnIndex(Constants.ContactConstants.COLUMN_CONTACT_BIRTHDAY));
                Date date = null;
                try {
                    date = format.parse(birthday);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                Calendar calendar = Calendar.getInstance();
                if (date != null) {
                    try {
                        calendar.setTime(date);
                    } catch (NullPointerException e){
                        e.printStackTrace();
                    }
                    int day = calendar.get(Calendar.DAY_OF_MONTH);
                    int month = calendar.get(Calendar.MONTH);
                    Date bdDate = getDate(0, month, day);
                    Date prevDate = getDate(0 - 1, month, day);
                    Date nextDate = getDate(1, month, day);
                    Date nextTwoDate = getDate(2, month, day);
                    dates.add(bdDate);
                    dates.add(prevDate);
                    dates.add(nextDate);
                    dates.add(nextTwoDate);
                }
            } while (c.moveToNext());
        }
        if (c != null) {
            c.close();
        }
        if (db != null) db.close();

        for (int i = 0; i < dates.size(); i++) {
            if (calendarView != null) {
                calendarView.setBackgroundResourceForDate(cSetter.colorBirthdayCalendar(), dates.get(i));
            }
        }
    }

    public static Date getDate(int index, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);

        Calendar cal1 = Calendar.getInstance();
        cal1.set(Calendar.YEAR, year + index);
        cal1.set(Calendar.MONTH, month);
        cal1.set(Calendar.DAY_OF_MONTH, day);
        cal1.set(Calendar.HOUR_OF_DAY, 0);
        cal1.set(Calendar.MINUTE, 0);
        cal1.set(Calendar.SECOND, 0);
        cal1.set(Calendar.MILLISECOND, 0);
        return cal1.getTime();
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
