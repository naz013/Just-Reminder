/*
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

package com.cray.software.justreminder.creator;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.ToggleButton;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.json.JExport;
import com.cray.software.justreminder.json.JModel;
import com.cray.software.justreminder.utils.AssetsUtil;
import com.cray.software.justreminder.utils.IntervalUtil;
import com.cray.software.justreminder.utils.TimeUtil;
import com.cray.software.justreminder.views.ActionView;
import com.cray.software.justreminder.views.DateTimeView;

import java.util.ArrayList;
import java.util.Calendar;

public class WeekFragment extends BaseFragment implements
        CompoundButton.OnCheckedChangeListener, TimePickerDialog.OnTimeSetListener {

    private ActionView.OnActionListener mCallbacks;
    private DateTimeView.OnSelectListener mListener;

    /**
     * Weekday reminder type variables.
     */
    private ToggleButton mondayCheck, tuesdayCheck, wednesdayCheck, thursdayCheck,
            fridayCheck, saturdayCheck, sundayCheck;
    private ActionView actionView;
    private TextView timeField;

    public ArrayList<Integer> getDays() {
        return IntervalUtil.getWeekRepeat(mondayCheck.isChecked(),
                tuesdayCheck.isChecked(), wednesdayCheck.isChecked(),
                thursdayCheck.isChecked(), fridayCheck.isChecked(),
                saturdayCheck.isChecked(), sundayCheck.isChecked());
    }

    public void setNumber(String number){
        actionView.setNumber(number);
    }
    /**
     * Click listener for time fields.
     */
    public View.OnClickListener timeClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            new TimePickerDialog(getActivity(), WeekFragment.this, myHour, myMinute,
                    new SharedPrefs(getActivity()).loadBoolean(Prefs.IS_24_TIME_FORMAT)).show();
        }
    };

    public static WeekFragment newInstance(JModel item, boolean isDark, boolean hasCalendar,
                                                  boolean hasStock, boolean hasTasks) {
        WeekFragment fragment = new WeekFragment();
        Bundle args = new Bundle();
        args.putBoolean(THEME, isDark);
        args.putBoolean(CALENDAR, hasCalendar);
        args.putBoolean(STOCK, hasStock);
        args.putBoolean(TASKS, hasTasks);
        fragment.setItem(item);
        fragment.setArguments(args);
        return fragment;
    }

    public WeekFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            hasCalendar = args.getBoolean(CALENDAR);
            hasStock = args.getBoolean(STOCK);
            hasTasks = args.getBoolean(TASKS);
            isDark = args.getBoolean(THEME);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Indicate that this fragment would like to influence the set of actions in the action bar.
        setHasOptionsMenu(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.reminder_weekdays_layout, container, false);

        CheckBox dateExport = (CheckBox) view.findViewById(R.id.dateExport);
        if (hasCalendar || hasStock)
            dateExport.setVisibility(View.VISIBLE);

        CheckBox dateTaskExport = (CheckBox) view.findViewById(R.id.dateTaskExport);
        if (hasTasks) dateTaskExport.setVisibility(View.VISIBLE);
        dateExport.setOnCheckedChangeListener(this);

        timeField = (TextView) view.findViewById(R.id.timeField);
        timeField.setOnClickListener(timeClick);
        timeField.setText(TimeUtil.getTime(updateTime(System.currentTimeMillis(), false),
                new SharedPrefs(getActivity()).loadBoolean(Prefs.IS_24_TIME_FORMAT)));
        timeField.setTypeface(AssetsUtil.getMediumTypeface(getActivity()));

        ColorSetter cs = new ColorSetter(getActivity());
        mondayCheck = (ToggleButton) view.findViewById(R.id.mondayCheck);
        tuesdayCheck = (ToggleButton) view.findViewById(R.id.tuesdayCheck);
        wednesdayCheck = (ToggleButton) view.findViewById(R.id.wednesdayCheck);
        thursdayCheck = (ToggleButton) view.findViewById(R.id.thursdayCheck);
        fridayCheck = (ToggleButton) view.findViewById(R.id.fridayCheck);
        saturdayCheck = (ToggleButton) view.findViewById(R.id.saturdayCheck);
        sundayCheck = (ToggleButton) view.findViewById(R.id.sundayCheck);
        mondayCheck.setBackgroundDrawable(cs.toggleDrawable());
        tuesdayCheck.setBackgroundDrawable(cs.toggleDrawable());
        wednesdayCheck.setBackgroundDrawable(cs.toggleDrawable());
        thursdayCheck.setBackgroundDrawable(cs.toggleDrawable());
        fridayCheck.setBackgroundDrawable(cs.toggleDrawable());
        saturdayCheck.setBackgroundDrawable(cs.toggleDrawable());
        sundayCheck.setBackgroundDrawable(cs.toggleDrawable());

        actionView = (ActionView) view.findViewById(R.id.actionView);
        actionView.setListener(mCallbacks);
        actionView.setActivity(getActivity());

        if (item != null) {
            JExport jExport = item.getExport();
            int exp = jExport.getCalendar();
            int expTasks = jExport.getgTasks();
            String type = item.getType();
            long eventTime = item.getEventTime();
            ArrayList<Integer> weekdays = item.getRecurrence().getWeekdays();

            if (exp == 1) dateExport.setChecked(true);
            if (expTasks == Constants.SYNC_GTASKS_ONLY)
                dateTaskExport.setChecked(true);

            timeField.setText(TimeUtil.getTime(updateTime(eventTime, true),
                    new SharedPrefs(getActivity()).loadBoolean(Prefs.IS_24_TIME_FORMAT)));

            setCheckForDays(weekdays);

            if (type.matches(Constants.TYPE_WEEKDAY))
                actionView.setAction(false);
            else {
                actionView.setAction(true);
                actionView.setNumber(number);
                if (type.matches(Constants.TYPE_WEEKDAY_CALL))
                    actionView.setType(ActionView.TYPE_CALL);
                else actionView.setType(ActionView.TYPE_MESSAGE);
            }
        }
        return view;
    }

    /**
     * Check days toggle buttons depends on weekday string.
     * @param weekdays weekday string.
     */
    private void setCheckForDays(ArrayList<Integer> weekdays){
        if (weekdays.get(0) == Constants.DAY_CHECKED)
            sundayCheck.setChecked(true);
        else sundayCheck.setChecked(false);

        if (weekdays.get(1) == Constants.DAY_CHECKED)
            mondayCheck.setChecked(true);
        else mondayCheck.setChecked(false);

        if (weekdays.get(2) == Constants.DAY_CHECKED)
            tuesdayCheck.setChecked(true);
        else tuesdayCheck.setChecked(false);

        if (weekdays.get(3) == Constants.DAY_CHECKED)
            wednesdayCheck.setChecked(true);
        else wednesdayCheck.setChecked(false);

        if (weekdays.get(4) == Constants.DAY_CHECKED)
            thursdayCheck.setChecked(true);
        else thursdayCheck.setChecked(false);

        if (weekdays.get(5) == Constants.DAY_CHECKED)
            fridayCheck.setChecked(true);
        else fridayCheck.setChecked(false);

        if (weekdays.get(6) == Constants.DAY_CHECKED)
            saturdayCheck.setChecked(true);
        else saturdayCheck.setChecked(false);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallbacks = (ActionView.OnActionListener) activity;
            mListener = (DateTimeView.OnSelectListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement listeners.");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.dateExport:
                super.isCalendar = isChecked;
                break;
            case R.id.dateTaskExport:
                super.isTasks = isChecked;
                break;
        }
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        myHour = hourOfDay;
        myMinute = minute;

        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, hourOfDay);
        c.set(Calendar.MINUTE, minute);

        String formattedTime = TimeUtil.getTime(c.getTime(),
                new SharedPrefs(getActivity()).loadBoolean(Prefs.IS_24_TIME_FORMAT));

        timeField.setText(formattedTime);

        if (mListener != null) {
            mListener.onTimeSelect(c.getTimeInMillis(), hourOfDay, minute);
        }
    }
}
