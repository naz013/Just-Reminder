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
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageButton;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.constants.Configs;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.reminder.json.JExport;
import com.cray.software.justreminder.reminder.json.JsonModel;
import com.cray.software.justreminder.roboto_views.RoboCheckBox;
import com.cray.software.justreminder.roboto_views.RoboEditText;
import com.cray.software.justreminder.utils.ViewUtils;
import com.cray.software.justreminder.views.DateTimeView;
import com.cray.software.justreminder.views.RepeatView;

public class CallFragment extends BaseFragment implements
        CompoundButton.OnCheckedChangeListener {

    private DateTimeView.OnSelectListener mCallbacks;
    private RepeatView.OnRepeatListener mRepeatCallbacks;
    private RoboEditText phoneNumber;
    private RepeatView repeatView;

    public static CallFragment newInstance(JsonModel item, boolean isDark, boolean hasCalendar,
                                           boolean hasStock, boolean hasTasks) {
        CallFragment fragment = new CallFragment();
        Bundle args = new Bundle();
        args.putBoolean(THEME, isDark);
        args.putBoolean(CALENDAR, hasCalendar);
        args.putBoolean(STOCK, hasStock);
        args.putBoolean(TASKS, hasTasks);
        fragment.setItem(item);
        fragment.setArguments(args);
        return fragment;
    }

    public CallFragment() {
    }

    public void setNumber(String num){
        number = num;
        phoneNumber.setText(number);
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
        View view = inflater.inflate(R.layout.reminder_call_layout, container, false);

        ImageButton addNumberButton = (ImageButton) view.findViewById(R.id.addNumberButton);
        addNumberButton.setOnClickListener(contactClick);
        ViewUtils.setImage(addNumberButton, isDark);

        phoneNumber = (RoboEditText) view.findViewById(R.id.phoneNumber);
        phoneNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                number = s.toString();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        repeatView = (RepeatView) view.findViewById(R.id.repeatView);
        repeatView.setListener(mRepeatCallbacks);
        repeatView.setMax(Configs.REPEAT_SEEKBAR_MAX);

        DateTimeView dateView = (DateTimeView) view.findViewById(R.id.dateView);
        dateView.setListener(new DateTimeView.OnSelectListener() {
            @Override
            public void onDateSelect(long mills, int day, int month, int year) {
                mYear = year;
                mMonth = month;
                mDay = day;
                if (mCallbacks != null) {
                    mCallbacks.onDateSelect(mills, day, month, year);
                }
                if (repeatView != null) repeatView.setDateTime(mYear, mMonth, mDay, mHour, mMinute);
            }

            @Override
            public void onTimeSelect(long mills, int hour, int minute) {
                mHour = hour;
                mMinute = minute;
                if (mCallbacks != null) {
                    mCallbacks.onTimeSelect(mills, hour, minute);
                }
                if (repeatView != null) repeatView.setDateTime(mYear, mMonth, mDay, mHour, mMinute);
            }
        });
        eventTime = System.currentTimeMillis();
        dateView.setDateTime(updateCalendar(eventTime, false));

        RoboCheckBox dateExport = (RoboCheckBox) view.findViewById(R.id.dateExport);
        if (hasCalendar || hasStock) dateExport.setVisibility(View.VISIBLE);

        RoboCheckBox dateTaskExport = (RoboCheckBox) view.findViewById(R.id.dateTaskExport);
        if (hasTasks) dateTaskExport.setVisibility(View.VISIBLE);
        dateExport.setOnCheckedChangeListener(this);
        dateTaskExport.setOnCheckedChangeListener(this);
        if (item != null) {
            JExport jExport = item.getExport();
            int exp = jExport.getCalendar();
            int expTasks = jExport.getgTasks();
            number = item.getAction().getTarget();
            eventTime = item.getEventTime();

            if (exp == 1) dateExport.setChecked(true);
            if (expTasks == Constants.SYNC_GTASKS_ONLY)
                dateTaskExport.setChecked(true);

            phoneNumber.setText(number);
            dateView.setDateTime(updateCalendar(item.getEventTime(), true));
            repeatView.setProgress(item.getRecurrence().getRepeat());
        }
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallbacks = (DateTimeView.OnSelectListener) activity;
            mRepeatCallbacks = (RepeatView.OnRepeatListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement listeners.");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
        mRepeatCallbacks = null;
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
        }
    }
}
