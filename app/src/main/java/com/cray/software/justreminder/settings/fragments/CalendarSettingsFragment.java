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
    
    private PrefsView themeColorPrefs, selectedColorPrefs, reminderInCalendarPrefs, 
            reminderColorPrefs, featureRemindersPrefs, bgImagePrefs;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView =  inflater.inflate(R.layout.settings_calendar, container, false);

        ab = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if (ab != null){
            ab.setTitle(R.string.calendar_fragment);
        }

        sPrefs = new SharedPrefs(getActivity());

        TextView startDay = (TextView) rootView.findViewById(R.id.startDay);
        TextView eventsImport = (TextView) rootView.findViewById(R.id.eventsImport);
        startDay.setOnClickListener(this);
        eventsImport.setOnClickListener(this);

        themeColorPrefs = (PrefsView) rootView.findViewById(R.id.themeColorPrefs);
        themeColorPrefs.setOnClickListener(this);

        selectedColorPrefs = (PrefsView) rootView.findViewById(R.id.selectedColorPrefs);
        selectedColorPrefs.setOnClickListener(this);

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
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        String loadedColor = sPrefs.loadPrefs(Prefs.REMINDERS_COLOR);
        reminderColorPrefs.setViewResource(new ColorSetter(getActivity()).getIndicator(loadedColor));
    }

    private void currentColor(){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        String loadedColor = sPrefs.loadPrefs(Prefs.CURRENT_COLOR);
        themeColorPrefs.setViewResource(new ColorSetter(getActivity()).getIndicator(loadedColor));
    }

    private void birthdayColor(){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        String loadedColor = sPrefs.loadPrefs(Prefs.BIRTHDAY_COLOR);
        selectedColorPrefs.setViewResource(new ColorSetter(getActivity()).getIndicator(loadedColor));
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
