package com.cray.software.justreminder.fragments;


import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.Permissions;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.interfaces.Prefs;
import com.cray.software.justreminder.widgets.UpdatesHelper;

public class CalendarSettingsFragment extends Fragment implements View.OnClickListener {

    SharedPrefs sPrefs;
    TextView startDay, text1, text2, eventsImport;
    RelativeLayout themeColor, selectedColor, reminderColor, reminderInCalendar, featureReminders,
            bgImage;
    View themeColorSwitcher, selectedColorSwitcher, reminderColorSwitcher;
    CheckBox reminderInCalendarCheck, featureRemindersCheck, bgImageCheck;
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
        text2 = (TextView) rootView.findViewById(R.id.textView2);

        featureReminders = (RelativeLayout) rootView.findViewById(R.id.featureReminders);
        featureReminders.setOnClickListener(this);

        featureRemindersCheck = (CheckBox) rootView.findViewById(R.id.featureRemindersCheck);
        featureRemindersCheck.setChecked(sPrefs.loadBoolean(Prefs.CALENDAR_FEATURE_TASKS));

        bgImage = (RelativeLayout) rootView.findViewById(R.id.bgImage);
        bgImage.setOnClickListener(this);

        bgImageCheck = (CheckBox) rootView.findViewById(R.id.bgImageCheck);
        bgImageCheck.setChecked(sPrefs.loadBoolean(Prefs.CALENDAR_IMAGE));

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

    private void imageCheck (){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        if (bgImageCheck.isChecked()){
            sPrefs.saveBoolean(Prefs.CALENDAR_IMAGE, false);
            bgImageCheck.setChecked(false);
        } else {
            sPrefs.saveBoolean(Prefs.CALENDAR_IMAGE, true);
            bgImageCheck.setChecked(true);
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
        reminderColorSwitcher.setBackgroundResource(new ColorSetter(getActivity()).getIndicator(loadedColor));
    }

    private void currentColor(){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        String loadedColor = sPrefs.loadPrefs(Prefs.CURRENT_COLOR);
        themeColorSwitcher.setBackgroundResource(new ColorSetter(getActivity()).getIndicator(loadedColor));
    }

    private void birthdayColor(){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        String loadedColor = sPrefs.loadPrefs(Prefs.BIRTHDAY_COLOR);
        selectedColorSwitcher.setBackgroundResource(new ColorSetter(getActivity()).getIndicator(loadedColor));
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
            case R.id.bgImage:
                imageCheck();
                break;
            case R.id.eventsImport:
                importEvents();
                break;
        }
    }

    private void importEvents() {
        Permissions permissions = new Permissions(getActivity());
        if (permissions.checkPermission(Permissions.READ_CALENDAR)) {
            getActivity().getApplicationContext()
                    .startActivity(new Intent(getActivity().getApplicationContext(), EventsImport.class)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        } else {
            permissions.requestPermission(getActivity(), new String[]{Permissions.READ_CALENDAR,
                    Permissions.WRITE_CALENDAR}, 101);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 101:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    getActivity().getApplicationContext()
                            .startActivity(new Intent(getActivity().getApplicationContext(), EventsImport.class)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                } else {
                    new Permissions(getActivity()).showInfo(getActivity(), Permissions.READ_CALENDAR);
                }
                break;
        }
    }
}
