package com.cray.software.justreminder.settings.fragments;


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
import android.widget.TextView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.activities.CalendarStyle;
import com.cray.software.justreminder.activities.EventsImport;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.Dialogues;
import com.cray.software.justreminder.helpers.Permissions;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.views.PrefsView;
import com.cray.software.justreminder.widgets.utils.UpdatesHelper;

public class CalendarSettingsFragment extends Fragment implements View.OnClickListener {

    private SharedPrefs sPrefs;
    private ActionBar ab;
    
    private PrefsView todayColorPrefs, birthdayColorPrefs, reminderInCalendarPrefs,
            reminderColorPrefs, featureRemindersPrefs, bgImagePrefs;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView =  inflater.inflate(R.layout.settings_calendar, container, false);

        ab = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if (ab != null){
            ab.setTitle(R.string.calendar);
        }

        sPrefs = new SharedPrefs(getActivity());

        TextView startDay = (TextView) rootView.findViewById(R.id.startDay);
        TextView eventsImport = (TextView) rootView.findViewById(R.id.eventsImport);
        startDay.setOnClickListener(this);
        eventsImport.setOnClickListener(this);

        todayColorPrefs = (PrefsView) rootView.findViewById(R.id.themeColorPrefs);
        todayColorPrefs.setOnClickListener(this);

        birthdayColorPrefs = (PrefsView) rootView.findViewById(R.id.selectedColorPrefs);
        birthdayColorPrefs.setOnClickListener(this);

        reminderInCalendarPrefs = (PrefsView) rootView.findViewById(R.id.reminderInCalendarPrefs);
        reminderInCalendarPrefs.setOnClickListener(this);
        reminderInCalendarPrefs.setChecked(sPrefs.loadBoolean(Prefs.REMINDERS_IN_CALENDAR));

        reminderColorPrefs = (PrefsView) rootView.findViewById(R.id.reminderColorPrefs);
        reminderColorPrefs.setOnClickListener(this);

        featureRemindersPrefs = (PrefsView) rootView.findViewById(R.id.featureRemindersPrefs);
        featureRemindersPrefs.setOnClickListener(this);
        featureRemindersPrefs.setChecked(sPrefs.loadBoolean(Prefs.CALENDAR_FEATURE_TASKS));

        bgImagePrefs = (PrefsView) rootView.findViewById(R.id.bgImagePrefs);
        bgImagePrefs.setOnClickListener(this);
        bgImagePrefs.setChecked(sPrefs.loadBoolean(Prefs.CALENDAR_IMAGE));

        currentColor();
        birthdayColor();
        reminderColor();
        checkEnabling();

        return rootView;
    }

    private void featureChange (){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        if (featureRemindersPrefs.isChecked()){
            sPrefs.saveBoolean(Prefs.CALENDAR_FEATURE_TASKS, false);
            featureRemindersPrefs.setChecked(false);
        } else {
            sPrefs.saveBoolean(Prefs.CALENDAR_FEATURE_TASKS, true);
            featureRemindersPrefs.setChecked(true);
        }

        new UpdatesHelper(getActivity()).updateCalendarWidget();
    }

    private void imageCheck (){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        if (bgImagePrefs.isChecked()){
            sPrefs.saveBoolean(Prefs.CALENDAR_IMAGE, false);
            bgImagePrefs.setChecked(false);
        } else {
            sPrefs.saveBoolean(Prefs.CALENDAR_IMAGE, true);
            bgImagePrefs.setChecked(true);
        }

        new UpdatesHelper(getActivity()).updateCalendarWidget();
    }

    private void remindersChange (){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        if (reminderInCalendarPrefs.isChecked()){
            sPrefs.saveBoolean(Prefs.REMINDERS_IN_CALENDAR, false);
            reminderInCalendarPrefs.setChecked(false);
        } else {
            sPrefs.saveBoolean(Prefs.REMINDERS_IN_CALENDAR, true);
            reminderInCalendarPrefs.setChecked(true);
        }
        checkEnabling();
        new UpdatesHelper(getActivity()).updateCalendarWidget();
    }

    private void checkEnabling(){
        if (reminderInCalendarPrefs.isChecked()){
            reminderColorPrefs.setEnabled(true);
        } else {
            reminderColorPrefs.setEnabled(false);
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
        reminderColorPrefs.setViewResource(new ColorSetter(getActivity())
                .getIndicator(new SharedPrefs(getActivity()).loadInt(Prefs.REMINDER_COLOR)));
    }

    private void currentColor(){
        todayColorPrefs.setViewResource(new ColorSetter(getActivity())
                .getIndicator(new SharedPrefs(getActivity()).loadInt(Prefs.TODAY_COLOR)));
    }

    private void birthdayColor(){
        birthdayColorPrefs.setViewResource(new ColorSetter(getActivity())
                .getIndicator(new SharedPrefs(getActivity()).loadInt(Prefs.BIRTH_COLOR)));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.themeColorPrefs:
                Intent i = new Intent(getActivity().getApplicationContext(), CalendarStyle.class);
                i.putExtra("type", 1);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getActivity().startActivity(i);
                break;
            case R.id.selectedColorPrefs:
                Intent ii = new Intent(getActivity().getApplicationContext(), CalendarStyle.class);
                ii.putExtra("type", 2);
                ii.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getActivity().startActivity(ii);
                break;
            case R.id.reminderColorPrefs:
                Intent iz = new Intent(getActivity().getApplicationContext(), CalendarStyle.class);
                iz.putExtra("type", 3);
                iz.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getActivity().startActivity(iz);
                break;
            case R.id.startDay:
                Dialogues.firstDay(getActivity());
                break;
            case R.id.reminderInCalendarPrefs:
                remindersChange();
                break;
            case R.id.featureRemindersPrefs:
                featureChange();
                break;
            case R.id.bgImagePrefs:
                imageCheck();
                break;
            case R.id.eventsImport:
                importEvents();
                break;
        }
    }

    private void importEvents() {
        if (Permissions.checkPermission(getActivity(), Permissions.READ_CALENDAR,
                Permissions.WRITE_CALENDAR)) {
            getActivity().getApplicationContext()
                    .startActivity(new Intent(getActivity().getApplicationContext(), EventsImport.class)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        } else {
            Permissions.requestPermission(getActivity(), 101, Permissions.READ_CALENDAR,
                    Permissions.WRITE_CALENDAR);
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
                    Permissions.showInfo(getActivity(), Permissions.READ_CALENDAR);
                }
                break;
        }
    }
}
