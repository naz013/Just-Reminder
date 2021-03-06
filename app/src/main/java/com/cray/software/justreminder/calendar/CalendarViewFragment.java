package com.cray.software.justreminder.calendar;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.StartActivity;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.dialogs.ActionPickerDialog;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.reminder.ReminderDataProvider;
import com.cray.software.justreminder.theme.MonthImage;
import com.hexrain.flextcal.FlextCal;
import com.hexrain.flextcal.FlextListener;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

public class CalendarViewFragment extends Fragment {

    private static final String TAG = "CalendarViewFragment";
    private Activity mContext;
    private DateCallback mCallback;
    private boolean isImage;

    public CalendarViewFragment() {
    }

    public static CalendarViewFragment newInstance() {
        return new CalendarViewFragment();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar_view, container, false);
        isImage = SharedPrefs.getInstance(mContext).getBoolean(Prefs.CALENDAR_IMAGE);
        return view;
    }

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);
        if (mContext == null) {
            mContext = activity;
        }
        if (mCallback == null) {
            try {
                mCallback = (DateCallback) activity;
            } catch (ClassCastException e) {
                throw new ClassCastException("Activity must implement DateCallback.");
            }
        }
        ((StartActivity) activity).onSectionAttached(StartActivity.ACTION_CALENDAR);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (mContext == null) {
            mContext = (Activity) context;
        }
        if (mCallback == null) {
            try {
                mCallback = (DateCallback) context;
            } catch (ClassCastException e) {
                throw new ClassCastException("Activity must implement DateCallback.");
            }
        }
        ((StartActivity) context).onSectionAttached(StartActivity.ACTION_CALENDAR);
    }

    @Override
    public void onResume() {
        super.onResume();
        showCalendar();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void showCalendar() {
        ColorSetter cSetter = ColorSetter.getInstance(mContext);
        FlextCal calendarView = new FlextCal();
        Bundle args = new Bundle();
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        args.putInt(FlextCal.MONTH, cal.get(Calendar.MONTH) + 1);
        args.putInt(FlextCal.YEAR, cal.get(Calendar.YEAR));
        if (SharedPrefs.getInstance(mContext).getInt(Prefs.START_DAY) == 0) {
            args.putInt(FlextCal.START_DAY_OF_WEEK, FlextCal.SUNDAY);
        } else {
            args.putInt(FlextCal.START_DAY_OF_WEEK, FlextCal.MONDAY);
        }
        args.putBoolean(FlextCal.DARK_THEME, cSetter.isDark());
        args.putBoolean(FlextCal.ENABLE_IMAGES, SharedPrefs.getInstance(mContext).getBoolean(Prefs.CALENDAR_IMAGE));
        MonthImage monthImage = (MonthImage) SharedPrefs.getInstance(mContext).getObject(Prefs.CALENDAR_IMAGES, MonthImage.class);
        Log.d(TAG, "showCalendar: " + Arrays.toString(monthImage.getPhotos()));
        args.putLongArray(FlextCal.MONTH_IMAGES, monthImage.getPhotos());
        calendarView.setArguments(args);
        calendarView.setBackgroundForToday(cSetter.getColor(cSetter.colorCurrentCalendar()));
        replace(calendarView, StartActivity.ACTION_CALENDAR);
        final FlextListener listener = new FlextListener() {

            @Override
            public void onClickDate(Date date, View view) {
                if (mCallback != null) mCallback.dateSelect(date);
                ((StartActivity) mContext).onItemSelected(StartActivity.FRAGMENT_EVENTS);
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
                startActivity(new Intent(mContext, ActionPickerDialog.class).putExtra("date", dateMills));
            }

            @Override
            public void onMonthChanged(int month, int year) {

            }

            @Override
            public void onCaldroidViewCreated() {
            }

            @Override
            public void onMonthSelected(int month) {

            }
        };
        calendarView.setCaldroidListener(listener);
        calendarView.refreshView();
        boolean isReminder = SharedPrefs.getInstance(mContext).getBoolean(Prefs.REMINDERS_IN_CALENDAR);
        boolean isFeature = SharedPrefs.getInstance(mContext).getBoolean(Prefs.CALENDAR_FEATURE_TASKS);
        calendarView.setEvents(new ReminderDataProvider(mContext, isReminder, isFeature).getEvents());
        replace(calendarView, StartActivity.ACTION_CALENDAR);
        SharedPrefs.getInstance(mContext).putInt(Prefs.LAST_CALENDAR_VIEW, 1);
        getActivity().invalidateOptionsMenu();
    }

    private void replace(Fragment fragment, String tag) {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.container, fragment, tag);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.addToBackStack(null);
        ft.commit();
        SharedPrefs.getInstance(mContext).putString(Prefs.LAST_FRAGMENT, tag);
    }
}
