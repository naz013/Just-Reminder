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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageButton;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.json.JExclusion;
import com.cray.software.justreminder.json.JExport;
import com.cray.software.justreminder.json.JModel;
import com.cray.software.justreminder.json.JRecurrence;
import com.cray.software.justreminder.roboto_views.RoboButton;
import com.cray.software.justreminder.roboto_views.RoboCheckBox;
import com.cray.software.justreminder.roboto_views.RoboTextView;
import com.cray.software.justreminder.utils.TimeUtil;
import com.cray.software.justreminder.views.RepeatView;

public class TimerFragment extends BaseFragment implements
        CompoundButton.OnCheckedChangeListener, View.OnClickListener {

    private RepeatView.OnRepeatListener mRepeatCallbacks;

    private RoboTextView hoursView, minutesView, secondsView, selectExclusion;
    private ImageButton deleteButton, exclusionClear;
    private String timeString = "000000";
    private String exclusion;

    public String getTimeString() {
        return timeString;
    }

    /**
     * Set up exclusion for reminder.
     * @param jsonObject json object string.
     */
    public void setExclusion(String jsonObject) {
        if (jsonObject != null) {
            JExclusion recurrence = new JExclusion(jsonObject);
            if (recurrence.getHours() != null) {
                selectExclusion.setText(String.format(getString(R.string.excluded_hours_x), recurrence.getHours().toString()));
                exclusionClear.setVisibility(View.VISIBLE);
            } else {
                String fromHour = recurrence.getFromHour();
                String toHour = recurrence.getToHour();
                if (fromHour != null && toHour != null) {
                    selectExclusion.setText(String.format(getString(R.string.excluded_time_from_x_to_x), fromHour, toHour));
                    exclusionClear.setVisibility(View.VISIBLE);
                }
            }
        } else {
            exclusion = null;
            selectExclusion.setText(getString(R.string.exclusion));
            exclusionClear.setVisibility(View.INVISIBLE);
        }
    }

    public static TimerFragment newInstance(JModel item, boolean isDark, boolean hasCalendar,
                                                  boolean hasStock, boolean hasTasks) {
        TimerFragment fragment = new TimerFragment();
        Bundle args = new Bundle();
        args.putBoolean(THEME, isDark);
        args.putBoolean(CALENDAR, hasCalendar);
        args.putBoolean(STOCK, hasStock);
        args.putBoolean(TASKS, hasTasks);
        fragment.setItem(item);
        fragment.setArguments(args);
        return fragment;
    }

    public TimerFragment() {
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
        View view = inflater.inflate(R.layout.reminder_time_layout, container, false);

        hoursView = (RoboTextView) view.findViewById(R.id.hoursView);
        minutesView = (RoboTextView) view.findViewById(R.id.minutesView);
        secondsView = (RoboTextView) view.findViewById(R.id.secondsView);
        selectExclusion = (RoboTextView) view.findViewById(R.id.selectExclusion);
        selectExclusion.setOnClickListener(exclusionClick);

        deleteButton = (ImageButton) view.findViewById(R.id.deleteButton);
        exclusionClear = (ImageButton) view.findViewById(R.id.exclusionClear);
        exclusionClear.setVisibility(View.INVISIBLE);
        if (isDark) {
            deleteButton.setImageResource(R.drawable.ic_backspace_white_24dp);
            exclusionClear.setImageResource(R.drawable.ic_clear_white_24dp);
        } else {
            deleteButton.setImageResource(R.drawable.ic_backspace_black_24dp);
            exclusionClear.setImageResource(R.drawable.ic_clear_black_24dp);
        }
        deleteButton.setOnClickListener(v -> {
            timeString = timeString.substring(0, timeString.length() - 1);
            timeString = "0" + timeString;
            updateTimeView();
        });
        deleteButton.setOnLongClickListener(v -> {
            timeString = "000000";
            updateTimeView();
            return true;
        });
        exclusionClear.setOnClickListener(v -> {
            if (exclusion != null){
                exclusion = null;
                selectExclusion.setText(getString(R.string.exclusion));
                exclusionClear.setVisibility(View.INVISIBLE);
            }
        });

        updateTimeView();

        RoboButton b1 = (RoboButton) view.findViewById(R.id.b1);
        RoboButton b2 = (RoboButton) view.findViewById(R.id.b2);
        RoboButton b3 = (RoboButton) view.findViewById(R.id.b3);
        RoboButton b4 = (RoboButton) view.findViewById(R.id.b4);
        RoboButton b5 = (RoboButton) view.findViewById(R.id.b5);
        RoboButton b6 = (RoboButton) view.findViewById(R.id.b6);
        RoboButton b7 = (RoboButton) view.findViewById(R.id.b7);
        RoboButton b8 = (RoboButton) view.findViewById(R.id.b8);
        RoboButton b9 = (RoboButton) view.findViewById(R.id.b9);
        RoboButton b0 = (RoboButton) view.findViewById(R.id.b0);
        if (b1 != null) {
            b1.setId(Integer.valueOf(101));
            b2.setId(Integer.valueOf(102));
            b3.setId(Integer.valueOf(103));
            b4.setId(Integer.valueOf(104));
            b5.setId(Integer.valueOf(105));
            b6.setId(Integer.valueOf(106));
            b7.setId(Integer.valueOf(107));
            b8.setId(Integer.valueOf(108));
            b9.setId(Integer.valueOf(109));
            b0.setId(Integer.valueOf(100));
            b1.setOnClickListener(this);
            b2.setOnClickListener(this);
            b3.setOnClickListener(this);
            b4.setOnClickListener(this);
            b5.setOnClickListener(this);
            b6.setOnClickListener(this);
            b7.setOnClickListener(this);
            b8.setOnClickListener(this);
            b9.setOnClickListener(this);
            b0.setOnClickListener(this);
        }

        RoboCheckBox dateExport = (RoboCheckBox) view.findViewById(R.id.dateExport);
        if (hasCalendar || hasStock) dateExport.setVisibility(View.VISIBLE);

        RoboCheckBox dateTaskExport = (RoboCheckBox) view.findViewById(R.id.dateTaskExport);
        if (hasTasks) dateTaskExport.setVisibility(View.VISIBLE);
        dateExport.setOnCheckedChangeListener(this);
        dateTaskExport.setOnCheckedChangeListener(this);

        RepeatView repeatView = (RepeatView) view.findViewById(R.id.repeatView);
        repeatView.setListener(mRepeatCallbacks);
        repeatView.setMax(120);

        if (item != null) {
            JExport jExport = item.getExport();
            int exp = jExport.getCalendar();
            int expTasks = jExport.getgTasks();

            JRecurrence jRecurrence = item.getRecurrence();
            long repeat = jRecurrence.getRepeat();
            long afterTime = jRecurrence.getAfter();
            exclusion = item.getExclusion().toString();

            timeString = TimeUtil.generateAfterString(afterTime);
            updateTimeView();
            setExclusion(exclusion);

            if (exp == 1) dateExport.setChecked(true);
            if (expTasks == Constants.SYNC_GTASKS_ONLY)
                dateTaskExport.setChecked(true);

            repeatView.setProgress(repeat);
        }
        return view;
    }

    /**
     * Set time in time view fields.
     */
    private void updateTimeView() {
        if (timeString.matches("000000")) deleteButton.setEnabled(false);
        else deleteButton.setEnabled(true);
        if (timeString.length() == 6){
            String hours = timeString.substring(0, 2);
            String minutes = timeString.substring(2, 4);
            String seconds = timeString.substring(4, 6);
            hoursView.setText(hours);
            minutesView.setText(minutes);
            secondsView.setText(seconds);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mRepeatCallbacks = (RepeatView.OnRepeatListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement listeners.");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
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

    @Override
    public void onClick(View v) {
        int ids = v.getId();
        if (ids >= 100 && ids < 110){
            String charS = String.valueOf(timeString.charAt(0));
            if (charS.matches("0")){
                timeString = timeString.substring(1, timeString.length());
                timeString = timeString + String.valueOf(ids - 100);
                updateTimeView();
            }
        }
    }
}
