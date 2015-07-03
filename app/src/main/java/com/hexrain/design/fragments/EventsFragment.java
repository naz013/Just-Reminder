package com.hexrain.design.fragments;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.ProgressDialog;
import android.database.Cursor;
import android.os.AsyncTask;
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
import com.cray.software.justreminder.datas.EventsDataProvider;
import com.cray.software.justreminder.datas.PagerItem;
import com.cray.software.justreminder.dialogs.BirthdaysList;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.interfaces.Configs;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.views.CircularProgress;
import com.hexrain.design.NavigationDrawerFragment;
import com.hexrain.design.ScreenManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ExecutionException;

public class EventsFragment extends Fragment {

    ColorSetter cSetter;
    SharedPrefs sPrefs;
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
        updateMenuTitles(cal.get(Calendar.DAY_OF_MONTH) + "/" + (cal.get(Calendar.MONTH) + 1) +
                "/" + cal.get(Calendar.YEAR));
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
    int targetPosition = -1;

    private void showEvents(Date date) {
        pagerData.clear();
        ProgressDialog dialog = new ProgressDialog(getActivity(), ProgressDialog.STYLE_SPINNER);
        dialog.setMessage(getActivity().getString(R.string.string_generating_events));
        dialog.setCancelable(false);
        dialog.setIndeterminate(true);
        dialog.show();
        try {
            pagerData = new LoadAsync(dialog).execute(date).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } finally {
            if (dialog != null && dialog.isShowing()) dialog.dismiss();
        }
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
                updateMenuTitles(day + "/" + (month + 1) + "/" + year);
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

    private class LoadAsync extends AsyncTask<Date, Void, ArrayList<PagerItem>>{

        ProgressDialog dialog;

        public LoadAsync(ProgressDialog dialog) {
            this.dialog = dialog;
        }

        @Override
        protected ArrayList<PagerItem> doInBackground(Date... params) {
            Date date = params[0];
            ArrayList<PagerItem> data = new ArrayList<>();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            int targetDay = calendar.get(Calendar.DAY_OF_MONTH);
            int targetMonth = calendar.get(Calendar.MONTH);
            int targetYear = calendar.get(Calendar.YEAR);

            calendar.setTimeInMillis(System.currentTimeMillis());
            int currentDay;
            int currentMonth;
            int currentYear;

            SharedPrefs sPrefs = new SharedPrefs(getActivity());
            int hour = sPrefs.loadInt(Constants.APP_UI_PREFERENCES_BIRTHDAY_REMINDER_HOUR);
            int minute = sPrefs.loadInt(Constants.APP_UI_PREFERENCES_BIRTHDAY_REMINDER_MINUTE);
            boolean isFeature = sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_CALENDAR_FEATURE_TASKS);
            boolean isRemindersEnabled = sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_REMINDERS_IN_CALENDAR);

            DataBase db = new DataBase(getActivity());
            if (!db.isOpen()) db.open();

            EventsDataProvider provider = new EventsDataProvider();
            Cursor c = db.getEvents();
            provider.setBirthdays(c);
            provider.setTime(hour, minute);
            if (isRemindersEnabled) {
                Cursor s = db.getActiveReminders();
                provider.setReminders(s);
                provider.setFeature(isFeature);
            }
            provider.fillArray();

            //long start = provider.getStartTime();
            //calendar.setTimeInMillis(start);

            int position = 0;
            do {
                currentDay = calendar.get(Calendar.DAY_OF_MONTH);
                currentMonth = calendar.get(Calendar.MONTH);
                currentYear = calendar.get(Calendar.YEAR);

                ArrayList<EventsDataProvider.EventsItem> datas =
                        provider.getMatches(currentDay, currentMonth, currentYear);

                if (currentDay == targetDay && currentMonth == targetMonth && currentYear == targetYear){
                    targetPosition = position;
                    data.add(new PagerItem(datas, position, 1, currentDay, currentMonth, currentYear));
                } else {
                    data.add(new PagerItem(datas, position, 0, currentDay, currentMonth, currentYear));
                }

                position++;
                calendar.setTimeInMillis(calendar.getTimeInMillis() + AlarmManager.INTERVAL_DAY);
            } while (position < Configs.MAX_DAYS_COUNT);
            return data;
        }

        @Override
        protected void onPostExecute(ArrayList<PagerItem> pagerItems) {
            super.onPostExecute(pagerItems);
            if (dialog != null && dialog.isShowing()) dialog.dismiss();
        }
    }
}
