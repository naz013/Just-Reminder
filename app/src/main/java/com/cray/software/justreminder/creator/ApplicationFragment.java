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
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.constants.Configs;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.reminder.json.JExport;
import com.cray.software.justreminder.reminder.json.JsonModel;
import com.cray.software.justreminder.roboto_views.RoboCheckBox;
import com.cray.software.justreminder.roboto_views.RoboEditText;
import com.cray.software.justreminder.roboto_views.RoboRadioButton;
import com.cray.software.justreminder.roboto_views.RoboTextView;
import com.cray.software.justreminder.utils.ViewUtils;
import com.cray.software.justreminder.views.DateTimeView;
import com.cray.software.justreminder.views.RepeatView;

public class ApplicationFragment extends BaseFragment implements
        CompoundButton.OnCheckedChangeListener {

    private DateTimeView.OnSelectListener mCallbacks;
    private RepeatView.OnRepeatListener mRepeatCallbacks;
    private RoboEditText phoneNumber;
    private RelativeLayout applicationLayout;
    private RoboTextView applicationName;
    private RepeatView repeatView;

    private String type;

    private Activity mContext;

    public static ApplicationFragment newInstance(JsonModel item, boolean isDark, boolean hasCalendar,
                                                  boolean hasStock, boolean hasTasks) {
        ApplicationFragment fragment = new ApplicationFragment();
        Bundle args = new Bundle();
        args.putBoolean(THEME, isDark);
        args.putBoolean(CALENDAR, hasCalendar);
        args.putBoolean(STOCK, hasStock);
        args.putBoolean(TASKS, hasTasks);
        fragment.setItem(item);
        fragment.setArguments(args);
        return fragment;
    }

    public ApplicationFragment() {
    }

    public void setApplication(String title){
        applicationName.setText(title);
    }

    public String getType() {
        return type;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(false);
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.reminder_application_layout, container, false);

        applicationLayout = (RelativeLayout) view.findViewById(R.id.applicationLayout);
        applicationLayout.setVisibility(View.VISIBLE);
        applicationName = (RoboTextView) view.findViewById(R.id.applicationName);

        type = Constants.TYPE_APPLICATION;

        ImageButton pickApplication = (ImageButton) view.findViewById(R.id.pickApplication);
        pickApplication.setOnClickListener(appClick);

        if (isDark) pickApplication.setImageResource(R.drawable.ic_launch_white_24dp);
        else pickApplication.setImageResource(R.drawable.ic_launch_black_24dp);

        RoboRadioButton application = (RoboRadioButton) view.findViewById(R.id.application);
        application.setChecked(true);
        RoboRadioButton browser = (RoboRadioButton) view.findViewById(R.id.browser);
        application.setOnCheckedChangeListener((compoundButton, b) -> {
            if (!b) {
                ViewUtils.collapse(applicationLayout);
                ViewUtils.expand(phoneNumber);
                type = Constants.TYPE_APPLICATION_BROWSER;
            } else {
                ViewUtils.collapse(phoneNumber);
                ViewUtils.expand(applicationLayout);
                type = Constants.TYPE_APPLICATION;
            }
        });

        phoneNumber = (RoboEditText) view.findViewById(R.id.phoneNumber);
        phoneNumber.setVisibility(View.GONE);
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
            type = item.getType();
            eventTime = item.getEventTime();

            if (exp == 1) dateExport.setChecked(true);
            if (expTasks == Constants.SYNC_GTASKS_ONLY)
                dateTaskExport.setChecked(true);

            if (type.matches(Constants.TYPE_APPLICATION)) {
                application.setChecked(true);
                PackageManager packageManager = mContext.getPackageManager();
                ApplicationInfo applicationInfo = null;
                try {
                    applicationInfo = packageManager.getApplicationInfo(number, 0);
                } catch (final PackageManager.NameNotFoundException ignored) {
                    ignored.printStackTrace();
                }

                final String name = (String) ((applicationInfo != null) ?
                        packageManager.getApplicationLabel(applicationInfo) : "???");
                applicationName.setText(name);

            }
            if (type.matches(Constants.TYPE_APPLICATION_BROWSER)) {
                browser.setChecked(true);
                phoneNumber.setText(number);
            }

            phoneNumber.setText(number);
            dateView.setDateTime(updateCalendar(eventTime, true));
            repeatView.setProgress(item.getRecurrence().getRepeat());
        }
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (mContext == null) {
            mContext = activity;
        }
        try {
            if (mCallbacks == null) mCallbacks = (DateTimeView.OnSelectListener) activity;
            if (mRepeatCallbacks == null) mRepeatCallbacks = (RepeatView.OnRepeatListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement listeners.");
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (mContext == null) {
            mContext = (Activity) context;
        }
        try {
            if (mCallbacks == null) mCallbacks = (DateTimeView.OnSelectListener) context;
            if (mRepeatCallbacks == null) mRepeatCallbacks = (RepeatView.OnRepeatListener) context;
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
