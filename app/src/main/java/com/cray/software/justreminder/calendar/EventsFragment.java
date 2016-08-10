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

import android.app.Activity;
import android.app.AlarmManager;
import android.content.Context;
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
import com.cray.software.justreminder.ScreenManager;
import com.cray.software.justreminder.constants.Configs;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.interfaces.NavigationCallbacks;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class EventsFragment extends Fragment {

    private long dateMills;
    private ViewPager pager;
    private ArrayList<EventsPagerItem> pagerData = new ArrayList<>();
    private String dayString;

    private NavigationCallbacks mCallbacks;
    private Activity mContext;

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
        rootView.findViewById(R.id.wrapper).setBackgroundColor(new ColorSetter(mContext).getBackgroundStyle());
        pager = (ViewPager) rootView.findViewById(R.id.pager);
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        if (dateMills != 0) {
            cal.setTimeInMillis(dateMills);
        }
        updateMenuTitles(cal.get(Calendar.DAY_OF_MONTH) + "/" + (cal.get(Calendar.MONTH) + 1) +
                "/" + cal.get(Calendar.YEAR));
        SharedPrefs.getInstance(mContext).putInt(Prefs.LAST_CALENDAR_VIEW, 0);
        loadData();
        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (mContext == null) {
            mContext = (Activity) context;
        }
        if (mCallbacks == null) {
            try {
                mCallbacks = (NavigationCallbacks) context;
            } catch (ClassCastException e) {
                throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
            }
        }
        ((ScreenManager) context).onSectionAttached(ScreenManager.FRAGMENT_EVENTS);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (mContext == null) {
            mContext = activity;
        }
        if (mCallbacks == null) {
            try {
                mCallbacks = (NavigationCallbacks) activity;
            } catch (ClassCastException e) {
                throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
            }
        }
        ((ScreenManager) activity).onSectionAttached(ScreenManager.FRAGMENT_EVENTS);
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
        if (SharedPrefs.getInstance(mContext).getBoolean(Prefs.REMINDER_CHANGED)) {
            loadData();
        }
    }

    private void loadData() {
        SharedPrefs.getInstance(mContext).putBoolean(Prefs.REMINDER_CHANGED, false);
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

        SharedPrefs sPrefs = SharedPrefs.getInstance(mContext);
        int hour = sPrefs.getInt(Prefs.BIRTHDAY_REMINDER_HOUR);
        int minute = sPrefs.getInt(Prefs.BIRTHDAY_REMINDER_MINUTE);
        boolean isFeature = sPrefs.getBoolean(Prefs.CALENDAR_FEATURE_TASKS);
        boolean isRemindersEnabled = sPrefs.getBoolean(Prefs.REMINDERS_IN_CALENDAR);

        EventsDataProvider provider = new EventsDataProvider(mContext);
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
            List<EventsItem> datas = provider.getMatches(mDay, mMonth, mYear);
            if (mDay == targetDay && mMonth == targetMonth && mYear == targetYear){
                targetPosition = position;
                pagerData.add(new EventsPagerItem(datas, position, 1, mDay, mMonth, mYear));
            } else {
                pagerData.add(new EventsPagerItem(datas, position, 0, mDay, mMonth, mYear));
            }
            position++;
            calendar.setTimeInMillis(calendar.getTimeInMillis() + AlarmManager.INTERVAL_DAY);
        }
        final CalendarPagerAdapter pagerAdapter = new CalendarPagerAdapter(getFragmentManager(), pagerData);
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
                if (mCallbacks != null) {
                    mCallbacks.onDateChanged(dateMills, i);
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

        pager.setCurrentItem(targetPosition);
    }
}
