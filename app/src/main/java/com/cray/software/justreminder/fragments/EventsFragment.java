package com.cray.software.justreminder.fragments;

import android.app.Activity;
import android.app.AlarmManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.adapters.CalendarPagerAdapter;
import com.cray.software.justreminder.datas.EventsDataProvider;
import com.cray.software.justreminder.datas.models.EventsItem;
import com.cray.software.justreminder.datas.models.EventsPagerItem;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.constants.Configs;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.ScreenManager;
import com.cray.software.justreminder.interfaces.NavigationCallbacks;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class EventsFragment extends Fragment {

    private long dateMills;
    private ViewPager pager;
    private ArrayList<EventsPagerItem> pagerData = new ArrayList<>();
    private int lastPosition = -1;
    private String dayString;

    private NavigationCallbacks mCallbacks;

    public static EventsFragment newInstance(long date, int lastPosition) {
        EventsFragment pageFragment = new EventsFragment();
        Bundle arguments = new Bundle();
        arguments.putLong("date", date);
        arguments.putInt("pos", lastPosition);
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
        lastPosition = intent.getInt("pos", -1);
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
                    mCallbacks.onItemSelected(ScreenManager.VOICE_RECOGNIZER);
                }
                return true;
            case R.id.action_month:
                if (mCallbacks != null){
                    mCallbacks.onItemSelected(ScreenManager.ACTION_CALENDAR);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_calendar, container, false);

        SharedPrefs sPrefs = new SharedPrefs(getActivity());
        pager = (ViewPager) rootView.findViewById(R.id.pager);

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());

        if (dateMills != 0) {
            cal.setTimeInMillis(dateMills);
        }
        updateMenuTitles(cal.get(Calendar.DAY_OF_MONTH) + "/" + (cal.get(Calendar.MONTH) + 1) +
                "/" + cal.get(Calendar.YEAR));
        sPrefs.saveInt(Prefs.LAST_CALENDAR_VIEW, 0);
        loadData();
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallbacks = (NavigationCallbacks) activity;
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

    private void updateMenuTitles(String title) {
        dayString = title;
        getActivity().invalidateOptionsMenu();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (new SharedPrefs(getActivity()).loadBoolean(Prefs.REMINDER_CHANGED)) {
            loadData();
        }
    }

    private void loadData() {
        new SharedPrefs(getActivity()).saveBoolean(Prefs.REMINDER_CHANGED, false);
        Calendar calendar = Calendar.getInstance();
        if (dateMills != 0){
            calendar.setTimeInMillis(dateMills);
            showEvents(calendar.getTime());
        } else {
            calendar.setTimeInMillis(System.currentTimeMillis());
            showEvents(calendar.getTime());
        }
    }

    private void showEvents(Date date) {
        pagerData.clear();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int targetDay = calendar.get(Calendar.DAY_OF_MONTH);
        int targetMonth = calendar.get(Calendar.MONTH);
        int targetYear = calendar.get(Calendar.YEAR);

        calendar.setTimeInMillis(System.currentTimeMillis());

        SharedPrefs sPrefs = new SharedPrefs(getActivity());
        int hour = sPrefs.loadInt(Prefs.BIRTHDAY_REMINDER_HOUR);
        int minute = sPrefs.loadInt(Prefs.BIRTHDAY_REMINDER_MINUTE);
        boolean isFeature = sPrefs.loadBoolean(Prefs.CALENDAR_FEATURE_TASKS);
        boolean isRemindersEnabled = sPrefs.loadBoolean(Prefs.REMINDERS_IN_CALENDAR);

        EventsDataProvider provider = new EventsDataProvider(getActivity());
        provider.setBirthdays(true);
        provider.setTime(hour, minute);
        provider.setReminders(isRemindersEnabled);
        provider.setFeature(isFeature);
        provider.fillArray();

        int position = 0;
        int targetPosition = -1;
        while (position < Configs.MAX_DAYS_COUNT) {
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
        }
        final CalendarPagerAdapter pagerAdapter =
                new CalendarPagerAdapter(getChildFragmentManager(), pagerData);
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
                updateMenuTitles(day + "/" + (month + 1) + "/" + year);
                Calendar calendar1 = Calendar.getInstance();
                calendar1.set(Calendar.DAY_OF_MONTH, day);
                calendar1.set(Calendar.MONTH, month);
                calendar1.set(Calendar.YEAR, year);
                dateMills = calendar1.getTimeInMillis();
                lastPosition = i;
                if (mCallbacks != null) {
                    mCallbacks.onDateChanged(dateMills, i);
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

        pager.setCurrentItem(lastPosition != -1 ? lastPosition : targetPosition);
    }
}
