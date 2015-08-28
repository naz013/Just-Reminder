package com.cray.software.justreminder.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.dialogs.CalendarStyle;
import com.cray.software.justreminder.dialogs.utils.EventsImport;
import com.cray.software.justreminder.dialogs.utils.FirstDay;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.interfaces.Prefs;
import com.cray.software.justreminder.widgets.UpdatesHelper;

public class CalendarSettingsFragment extends Fragment implements View.OnClickListener {

    SharedPrefs sPrefs;
    TextView startDay, text1, text2, eventsImport;
    RelativeLayout themeColor, selectedColor, reminderColor, reminderInCalendar, featureReminders;
    View themeColorSwitcher, selectedColorSwitcher, reminderColorSwitcher;
    CheckBox reminderInCalendarCheck, featureRemindersCheck;
    ActionBar ab;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView =  inflater.inflate(R.layout.calendar_settings_fragment, container, false);

        ab = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if (ab != null){
            ab.setTitle(R.string.calendar_fragment);
        }

        startDay = (TextView) rootView.findViewById(R.id.startDay);
        eventsImport = (TextView) rootView.findViewById(R.id.eventsImport);
        startDay.setOnClickListener(this);
        eventsImport.setOnClickListener(this);

        themeColor = (RelativeLayout) rootView.findViewById(R.id.themeColor);
        themeColorSwitcher = rootView.findViewById(R.id.themeColorSwitcher);

        currentColor();
        themeColor.setOnClickListener(this);

        selectedColor = (RelativeLayout) rootView.findViewById(R.id.selectedColor);
        selectedColorSwitcher = rootView.findViewById(R.id.selectedColorSwitcher);

        birthdayColor();
        selectedColor.setOnClickListener(this);

        sPrefs = new SharedPrefs(getActivity());

        reminderInCalendar = (RelativeLayout) rootView.findViewById(R.id.reminderInCalendar);
        reminderInCalendar.setOnClickListener(this);

        reminderInCalendarCheck = (CheckBox) rootView.findViewById(R.id.reminderInCalendarCheck);
        reminderInCalendarCheck.setChecked(sPrefs.loadBoolean(Prefs.REMINDERS_IN_CALENDAR));

        reminderColor = (RelativeLayout) rootView.findViewById(R.id.reminderColor);
        reminderColor.setOnClickListener(this);
        reminderColorSwitcher = rootView.findViewById(R.id.reminderColorSwitcher);
        text1 = (TextView) rootView.findViewById(R.id.text1);
        text2 = (TextView) rootView.findViewById(R.id.text2);

        featureReminders = (RelativeLayout) rootView.findViewById(R.id.featureReminders);
        featureReminders.setOnClickListener(this);

        featureRemindersCheck = (CheckBox) rootView.findViewById(R.id.featureRemindersCheck);
        featureRemindersCheck.setChecked(sPrefs.loadBoolean(Prefs.CALENDAR_FEATURE_TASKS));

        reminderColor();
        checkEnabling();

        return rootView;
    }

    private void featureChange (){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        if (featureRemindersCheck.isChecked()){
            sPrefs.saveBoolean(Prefs.CALENDAR_FEATURE_TASKS, false);
            featureRemindersCheck.setChecked(false);
        } else {
            sPrefs.saveBoolean(Prefs.CALENDAR_FEATURE_TASKS, true);
            featureRemindersCheck.setChecked(true);
        }

        new UpdatesHelper(getActivity()).updateCalendarWidget();
    }

    private void remindersChange (){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        if (reminderInCalendarCheck.isChecked()){
            sPrefs.saveBoolean(Prefs.REMINDERS_IN_CALENDAR, false);
            reminderInCalendarCheck.setChecked(false);
        } else {
            sPrefs.saveBoolean(Prefs.REMINDERS_IN_CALENDAR, true);
            reminderInCalendarCheck.setChecked(true);
        }
        checkEnabling();
        new UpdatesHelper(getActivity()).updateCalendarWidget();
    }

    private void checkEnabling(){
        if (reminderInCalendarCheck.isChecked()){
            reminderColor.setEnabled(true);
            reminderColorSwitcher.setEnabled(true);
            text1.setEnabled(true);
            text2.setEnabled(true);
        } else {
            reminderColor.setEnabled(false);
            reminderColorSwitcher.setEnabled(false);
            text1.setEnabled(false);
            text2.setEnabled(false);
        }
    }

    @Override
    public void onResume() {
        currentColor();
        birthdayColor();
        reminderColor();
        super.onResume();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        ab = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if (ab != null){
            ab.setTitle(R.string.action_settings);
        }
    }

    private void reminderColor(){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        String loadedColor = sPrefs.loadPrefs(Prefs.REMINDERS_COLOR);
        switch (loadedColor) {
            case "1":
                reminderColorSwitcher.setBackgroundResource(R.drawable.drawable_red);
                break;
            case "2":
                reminderColorSwitcher.setBackgroundResource(R.drawable.drawable_violet);
                break;
            case "3":
                reminderColorSwitcher.setBackgroundResource(R.drawable.drawable_green_light);
                break;
            case "4":
                reminderColorSwitcher.setBackgroundResource(R.drawable.drawable_green);
                break;
            case "5":
                reminderColorSwitcher.setBackgroundResource(R.drawable.drawable_blue_light);
                break;
            case "6":
                reminderColorSwitcher.setBackgroundResource(R.drawable.drawable_blue);
                break;
            case "7":
                reminderColorSwitcher.setBackgroundResource(R.drawable.drawable_yellow);
                break;
            case "8":
                reminderColorSwitcher.setBackgroundResource(R.drawable.drawable_orange);
                break;
            case "9":
                reminderColorSwitcher.setBackgroundResource(R.drawable.drawable_grey);
                break;
            case "10":
                reminderColorSwitcher.setBackgroundResource(R.drawable.drawable_pink);
                break;
            case "11":
                reminderColorSwitcher.setBackgroundResource(R.drawable.drawable_teal);
                break;
            case "12":
                reminderColorSwitcher.setBackgroundResource(R.drawable.drawable_brown);
                break;
            default:
                reminderColorSwitcher.setBackgroundResource(R.drawable.drawable_blue);
                break;
        }
    }

    private void currentColor(){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        String loadedColor = sPrefs.loadPrefs(Prefs.CURRENT_COLOR);
        switch (loadedColor) {
            case "1":
                themeColorSwitcher.setBackgroundResource(R.drawable.drawable_red);
                break;
            case "2":
                themeColorSwitcher.setBackgroundResource(R.drawable.drawable_violet);
                break;
            case "3":
                themeColorSwitcher.setBackgroundResource(R.drawable.drawable_green_light);
                break;
            case "4":
                themeColorSwitcher.setBackgroundResource(R.drawable.drawable_green);
                break;
            case "5":
                themeColorSwitcher.setBackgroundResource(R.drawable.drawable_blue_light);
                break;
            case "6":
                themeColorSwitcher.setBackgroundResource(R.drawable.drawable_blue);
                break;
            case "7":
                themeColorSwitcher.setBackgroundResource(R.drawable.drawable_yellow);
                break;
            case "8":
                themeColorSwitcher.setBackgroundResource(R.drawable.drawable_orange);
                break;
            case "9":
                themeColorSwitcher.setBackgroundResource(R.drawable.drawable_grey);
                break;
            case "10":
                themeColorSwitcher.setBackgroundResource(R.drawable.drawable_pink);
                break;
            case "11":
                themeColorSwitcher.setBackgroundResource(R.drawable.drawable_teal);
                break;
            case "12":
                themeColorSwitcher.setBackgroundResource(R.drawable.drawable_brown);
                break;
            default:
                themeColorSwitcher.setBackgroundResource(R.drawable.drawable_blue);
                break;
        }
    }

    private void birthdayColor(){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        String loadedColor = sPrefs.loadPrefs(Prefs.BIRTHDAY_COLOR);
        switch (loadedColor) {
            case "1":
                selectedColorSwitcher.setBackgroundResource(R.drawable.drawable_red);
                break;
            case "2":
                selectedColorSwitcher.setBackgroundResource(R.drawable.drawable_violet);
                break;
            case "3":
                selectedColorSwitcher.setBackgroundResource(R.drawable.drawable_green_light);
                break;
            case "4":
                selectedColorSwitcher.setBackgroundResource(R.drawable.drawable_green);
                break;
            case "5":
                selectedColorSwitcher.setBackgroundResource(R.drawable.drawable_blue_light);
                break;
            case "6":
                selectedColorSwitcher.setBackgroundResource(R.drawable.drawable_blue);
                break;
            case "7":
                selectedColorSwitcher.setBackgroundResource(R.drawable.drawable_yellow);
                break;
            case "8":
                selectedColorSwitcher.setBackgroundResource(R.drawable.drawable_orange);
                break;
            case "9":
                selectedColorSwitcher.setBackgroundResource(R.drawable.drawable_grey);
                break;
            case "10":
                selectedColorSwitcher.setBackgroundResource(R.drawable.drawable_pink);
                break;
            case "11":
                selectedColorSwitcher.setBackgroundResource(R.drawable.drawable_teal);
                break;
            case "12":
                selectedColorSwitcher.setBackgroundResource(R.drawable.drawable_brown);
                break;
            default:
                selectedColorSwitcher.setBackgroundResource(R.drawable.drawable_blue);
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.themeColor:
                Intent i = new Intent(getActivity().getApplicationContext(), CalendarStyle.class);
                i.putExtra("type", 1);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getActivity().startActivity(i);
                break;
            case R.id.selectedColor:
                Intent ii = new Intent(getActivity().getApplicationContext(), CalendarStyle.class);
                ii.putExtra("type", 2);
                ii.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getActivity().startActivity(ii);
                break;
            case R.id.reminderColor:
                Intent iz = new Intent(getActivity().getApplicationContext(), CalendarStyle.class);
                iz.putExtra("type", 3);
                iz.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getActivity().startActivity(iz);
                break;
            case R.id.startDay:
                getActivity().getApplicationContext()
                        .startActivity(new Intent(getActivity().getApplicationContext(), FirstDay.class)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                break;
            case R.id.reminderInCalendar:
                remindersChange();
                break;
            case R.id.featureReminders:
                featureChange();
                break;
            case R.id.eventsImport:
                getActivity().getApplicationContext()
                        .startActivity(new Intent(getActivity().getApplicationContext(), EventsImport.class)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                break;
        }
    }
}
