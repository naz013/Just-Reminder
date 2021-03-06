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
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.TimePicker;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.interfaces.ActionCallbacks;
import com.cray.software.justreminder.reminder.json.JExport;
import com.cray.software.justreminder.reminder.json.JsonModel;
import com.cray.software.justreminder.roboto_views.RoboCheckBox;
import com.cray.software.justreminder.roboto_views.RoboRadioButton;
import com.cray.software.justreminder.roboto_views.RoboTextView;
import com.cray.software.justreminder.utils.TimeUtil;
import com.cray.software.justreminder.utils.ViewUtils;
import com.cray.software.justreminder.views.ActionView;
import com.cray.software.justreminder.views.DateTimeView;

import java.util.Calendar;

public class MonthFragment extends BaseFragment implements
        CompoundButton.OnCheckedChangeListener, DatePickerDialog.OnDateSetListener,
        TimePickerDialog.OnTimeSetListener {

    private ActionView.OnActionListener mCallbacks;
    private DateTimeView.OnSelectListener mListener;
    private ActionCallbacks mActionCallbacks;

    private RoboTextView monthDayField;
    private RoboRadioButton dayCheck, lastCheck;
    private ActionView actionView;
    private RoboTextView timeField;

    public boolean isLast() {
        return lastCheck.isChecked();
    }

    public void setNumber(String num){
        number = num;
        actionView.setNumber(number);
    }

    /**
     * Click listener for date fields.
     */
    public View.OnClickListener dateClick = v -> new DatePickerDialog(getActivity(), MonthFragment.this, mYear, mMonth, mDay).show();

    /**
     * Click listener for time fields.
     */
    public View.OnClickListener timeClick = v -> new TimePickerDialog(getActivity(), MonthFragment.this, mHour, mMinute,
            SharedPrefs.getInstance(getActivity()).getBoolean(Prefs.IS_24_TIME_FORMAT)).show();

    public static MonthFragment newInstance(JsonModel item, boolean isDark, boolean hasCalendar,
                                            boolean hasStock, boolean hasTasks) {
        MonthFragment fragment = new MonthFragment();
        Bundle args = new Bundle();
        args.putBoolean(THEME, isDark);
        args.putBoolean(CALENDAR, hasCalendar);
        args.putBoolean(STOCK, hasStock);
        args.putBoolean(TASKS, hasTasks);
        fragment.setItem(item);
        fragment.setArguments(args);
        return fragment;
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

    public MonthFragment() {
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
        View view = inflater.inflate(R.layout.reminder_day_of_month_layout, container, false);

        monthDayField = (RoboTextView) view.findViewById(R.id.monthDayField);
        monthDayField.setOnClickListener(dateClick);

        RoboCheckBox dateExport = (RoboCheckBox) view.findViewById(R.id.dateExport);
        if (hasCalendar || hasStock) dateExport.setVisibility(View.VISIBLE);

        RoboCheckBox dateTaskExport = (RoboCheckBox) view.findViewById(R.id.dateTaskExport);
        if (hasTasks) dateTaskExport.setVisibility(View.VISIBLE);
        dateExport.setOnCheckedChangeListener(this);
        dateTaskExport.setOnCheckedChangeListener(this);

        timeField = (RoboTextView) view.findViewById(R.id.timeField);
        timeField.setOnClickListener(timeClick);
        timeField.setText(TimeUtil.getTime(updateTime(System.currentTimeMillis(), false),
                SharedPrefs.getInstance(getActivity()).getBoolean(Prefs.IS_24_TIME_FORMAT)));

        dayCheck = (RoboRadioButton) view.findViewById(R.id.dayCheck);
        dayCheck.setChecked(true);
        lastCheck = (RoboRadioButton) view.findViewById(R.id.lastCheck);
        dayCheck.setOnCheckedChangeListener(this);
        lastCheck.setOnCheckedChangeListener(this);

        actionView = (ActionView) view.findViewById(R.id.actionView);
        actionView.setListener(mCallbacks);
        actionView.setActivity(getActivity());

        if (item != null) {
            JExport jExport = item.getExport();
            int exp = jExport.getCalendar();
            int expTasks = jExport.getgTasks();
            String type = item.getType();
            long eventTime = item.getEventTime();
            number = item.getAction().getTarget();
            if (exp == 1) dateExport.setChecked(true);
            if (expTasks == Constants.SYNC_GTASKS_ONLY) {
                dateTaskExport.setChecked(true);
            }
            timeField.setText(TimeUtil.getTime(updateTime(eventTime, true),
                    SharedPrefs.getInstance(getActivity()).getBoolean(Prefs.IS_24_TIME_FORMAT)));
            if (type.matches(Constants.TYPE_MONTHDAY)){
                actionView.setAction(false);
                dayCheck.setChecked(true);
            } else if (type.matches(Constants.TYPE_MONTHDAY_LAST)) {
                actionView.setAction(false);
                lastCheck.setChecked(true);
            } else {
                actionView.setAction(true);
                actionView.setNumber(number);
                if (type.matches(Constants.TYPE_MONTHDAY_CALL_LAST) ||
                        type.matches(Constants.TYPE_MONTHDAY_MESSAGE_LAST)){
                    lastCheck.setChecked(true);
                } else {
                    dayCheck.setChecked(true);
                }
                if (type.matches(Constants.TYPE_MONTHDAY_CALL)){
                    actionView.setType(ActionView.TYPE_CALL);
                } else {
                    actionView.setType(ActionView.TYPE_MESSAGE);
                }
            }
        }
        Calendar calendar = Calendar.getInstance();
        mDay = calendar.get(Calendar.DAY_OF_MONTH);
        String dayStr;
        if (mDay > 28) mDay = 28;
        if (mDay < 10) dayStr = "0" + mDay;
        else dayStr = String.valueOf(mDay);
        monthDayField.setText(dayStr);
        if (mListener != null) {
            mListener.onDateSelect(System.currentTimeMillis(), mDay, calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.YEAR));
        }
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallbacks = (ActionView.OnActionListener) activity;
            mListener = (DateTimeView.OnSelectListener) activity;
            mActionCallbacks = (ActionCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement listeners.");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
        mListener = null;
        mActionCallbacks = null;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.dateExport:
                isCalendar = isChecked;
                break;
            case R.id.dateTaskExport:
                isTasks = isChecked;
                break;
            case R.id.dayCheck:
                if (dayCheck.isChecked()) {
                    lastCheck.setChecked(false);
                    ViewUtils.expand(monthDayField);
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(System.currentTimeMillis());
                    mDay = calendar.get(Calendar.DAY_OF_MONTH);
                    if (mDay > 28) mDay = 1;
                }
                break;
            case R.id.lastCheck:
                if (lastCheck.isChecked()) {
                    dayCheck.setChecked(false);
                    ViewUtils.collapse(monthDayField);
                    mDay = 0;
                }
                break;
        }
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        mDay = dayOfMonth;
        mMonth = monthOfYear;
        mYear = year;
        final Calendar cal = Calendar.getInstance();
        cal.set(year, monthOfYear, dayOfMonth);
        if (mDay > 28 && mActionCallbacks != null) {
            mActionCallbacks.showSnackbar(R.string.max_day_supported);
        }
        String dayStr;
        if (mDay > 28) mDay = 28;
        if (mDay < 10) dayStr = "0" + mDay;
        else dayStr = String.valueOf(mDay);
        monthDayField.setText(dayStr);
        if (mListener != null) {
            mListener.onDateSelect(cal.getTimeInMillis(), mDay, monthOfYear, year);
        }
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        mHour = hourOfDay;
        mMinute = minute;
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, hourOfDay);
        c.set(Calendar.MINUTE, minute);
        String formattedTime = TimeUtil.getTime(c.getTime(),
                SharedPrefs.getInstance(getActivity()).getBoolean(Prefs.IS_24_TIME_FORMAT));
        timeField.setText(formattedTime);
        if (mListener != null) {
            mListener.onTimeSelect(c.getTimeInMillis(), hourOfDay, minute);
        }
    }
}
