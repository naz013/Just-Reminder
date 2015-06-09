package com.hexrain.design.fragments;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.datas.CalendarData;
import com.cray.software.justreminder.datas.PagerItem;
import com.cray.software.justreminder.dialogs.BirthdaysList;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.TimeCount;
import com.cray.software.justreminder.interfaces.Configs;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.views.CircularProgress;
import com.hexrain.design.NavigationDrawerFragment;
import com.hexrain.design.ScreenManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class EventsFragment extends Fragment {

    ColorSetter cSetter;
    DataBase db;
    SharedPrefs sPrefs;
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
    long dateMills;
    ViewPager pager;
    CircularProgress progress;

    private NavigationDrawerFragment.NavigationDrawerCallbacks mCallbacks;

    public static EventsFragment newInstance(long date) {
        EventsFragment pageFragment = new EventsFragment();
        Bundle arguments = new Bundle();
        arguments.putLong("date", date);
        pageFragment.setArguments(arguments);
        return pageFragment;
    }

    public EventsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle intent = getArguments();
        dateMills = intent.getLong("date", 0);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Indicate that this fragment would like to influence the set of actions in the action bar.
        setHasOptionsMenu(true);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.calendar_menu, menu);
        menu.findItem(R.id.action_month).setVisible(true);
        menu.findItem(R.id.action_day).setTitle(dayString);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_voice:
                if (mCallbacks != null){
                    mCallbacks.onNavigationDrawerItemSelected(ScreenManager.VOICE_RECOGNIZER);
                }
                return true;
            case R.id.action_month:
                if (mCallbacks != null){
                    mCallbacks.onNavigationDrawerItemSelected(ScreenManager.ACTION_CALENDAR);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_calendar, container, false);

        cSetter = new ColorSetter(getActivity());
        sPrefs = new SharedPrefs(getActivity());

        pager = (ViewPager) rootView.findViewById(R.id.pager);

        progress = (CircularProgress) rootView.findViewById(R.id.progress);

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());

        if (dateMills != 0) cal.setTimeInMillis(dateMills);
        showEvents(cal.getTime());
        updateMenuTitles(String.valueOf(cal.get(Calendar.DAY_OF_MONTH)));
        sPrefs.saveInt(Constants.APP_UI_PREFERENCES_LAST_CALENDAR_VIEW, 0);
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallbacks = (NavigationDrawerFragment.NavigationDrawerCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
        }
        ((ScreenManager)activity).onSectionAttached(ScreenManager.FRAGMENT_EVENTS);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    String dayString;

    private void updateMenuTitles(String title) {
        dayString = title;
        getActivity().invalidateOptionsMenu();
    }

    @Override
    public void onResume() {
        super.onResume();
        sPrefs = new SharedPrefs(getActivity());
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

    ArrayList<PagerItem> pagerData = new ArrayList<>();
    ProgressDialog dialog;

    private void showEvents(Date date) {
        progress.setVisibility(View.VISIBLE);
        dialog = new ProgressDialog(getActivity(), ProgressDialog.STYLE_SPINNER);
        dialog.setMax(100);
        dialog.setMessage(getActivity().getString(R.string.string_generating_events));
        dialog.setCancelable(false);
        dialog.setIndeterminate(false);
        dialog.show();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int targetDay = calendar.get(Calendar.DAY_OF_MONTH);
        int targetMonth = calendar.get(Calendar.MONTH);
        int targetYear = calendar.get(Calendar.YEAR);

        calendar.setTimeInMillis(System.currentTimeMillis());
        int currentDay;
        int currentMonth;
        int currentYear;

        sPrefs = new SharedPrefs(getActivity());
        int hour = sPrefs.loadInt(Constants.APP_UI_PREFERENCES_BIRTHDAY_REMINDER_HOUR);
        int minute = sPrefs.loadInt(Constants.APP_UI_PREFERENCES_BIRTHDAY_REMINDER_MINUTE);
        boolean isFeature = sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_CALENDAR_FEATURE_TASKS);
        boolean isRemindersEnabled = sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_REMINDERS_IN_CALENDAR);
        TimeCount mCount = new TimeCount(getActivity());

        db = new DataBase(getActivity());
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
                        long afterTime = s.getInt(s.getColumnIndex(Constants.COLUMN_REMIND_TIME));
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
                                    calendar1.setTimeInMillis(calendar1.getTimeInMillis() + (repCode *
                                            AlarmManager.INTERVAL_DAY));
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
                                    calendar1.setTimeInMillis(calendar1.getTimeInMillis() + AlarmManager.INTERVAL_DAY);
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
                            long time = mCount.getNextMonthDayTime(myHour, myMinute, myDay, 0);
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
                                    time = mCount.getNextMonthDayTime(myDay, calendar1.getTimeInMillis(), days);
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

        progress.setVisibility(View.GONE);
        if (dialog != null && dialog.isShowing()) dialog.dismiss();
        final MyFragmentPagerAdapter pagerAdapter =
                new MyFragmentPagerAdapter(getChildFragmentManager(), pagerData);
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
                updateMenuTitles(String.valueOf(day));
                Calendar calendar1 = Calendar.getInstance();
                calendar1.set(Calendar.DAY_OF_MONTH, day);
                calendar1.set(Calendar.MONTH, month);
                calendar1.set(Calendar.YEAR, year);
                dateMills = calendar1.getTimeInMillis();
                if (mCallbacks != null) mCallbacks.onDateChanged(dateMills);
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

        pager.setCurrentItem(targetPosition);
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
}
