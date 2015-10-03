package com.cray.software.justreminder.fragments;

import android.content.DialogInterface;
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
import android.widget.Toast;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.dialogs.CloudDrives;
import com.cray.software.justreminder.dialogs.utils.SelectCalendar;
import com.cray.software.justreminder.helpers.CalendarManager;
import com.cray.software.justreminder.helpers.Dialog;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.interfaces.Prefs;
import com.cray.software.justreminder.services.AutoSyncAlarm;

import java.util.ArrayList;

public class ExportSettingsFragment extends Fragment implements View.OnClickListener, DialogInterface.OnDismissListener {

    private RelativeLayout exportTasks;
    private CheckBox exportToCalendarCheck, autoBackupCheck, exportToStockCheck, exportTasksCheck,
            syncSettingsCheck;
    private TextView eventDuration;
    private TextView selectCalendar;
    private TextView syncInterval;
    private SharedPrefs sPrefs;
    private ActionBar ab;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView =  inflater.inflate(R.layout.export_settings_layout, container, false);

        ab = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if (ab != null){
            ab.setTitle(R.string.export_settings_block);
        }

        RelativeLayout exportToCalendar = (RelativeLayout) rootView.findViewById(R.id.exportToCalendar);
        exportToCalendar.setOnClickListener(this);

        exportToCalendarCheck = (CheckBox) rootView.findViewById(R.id.exportToCalendarCheck);
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        exportToCalendarCheck.setChecked(sPrefs.loadBoolean(Prefs.EXPORT_TO_CALENDAR));

        eventDuration = (TextView) rootView.findViewById(R.id.eventDuration);
        eventDuration.setOnClickListener(this);

        selectCalendar = (TextView) rootView.findViewById(R.id.selectCalendar);
        selectCalendar.setOnClickListener(this);

        TextView clouds = (TextView) rootView.findViewById(R.id.clouds);
        clouds.setOnClickListener(this);

        TextView clean = (TextView) rootView.findViewById(R.id.clean);
        clean.setOnClickListener(this);

        syncInterval = (TextView) rootView.findViewById(R.id.syncInterval);
        syncInterval.setOnClickListener(this);

        RelativeLayout autoBackup = (RelativeLayout) rootView.findViewById(R.id.autoBackup);
        autoBackup.setOnClickListener(this);

        autoBackupCheck = (CheckBox) rootView.findViewById(R.id.autoBackupCheck);
        autoBackupCheck.setChecked(sPrefs.loadBoolean(Prefs.AUTO_BACKUP));

        RelativeLayout exportToStock = (RelativeLayout) rootView.findViewById(R.id.exportToStock);
        exportToStock.setOnClickListener(this);

        exportToStockCheck = (CheckBox) rootView.findViewById(R.id.exportToStockCheck);
        exportToStockCheck.setChecked(sPrefs.loadBoolean(Prefs.EXPORT_TO_STOCK));

        RelativeLayout syncSettings = (RelativeLayout) rootView.findViewById(R.id.syncSettings);
        syncSettings.setOnClickListener(this);

        syncSettingsCheck = (CheckBox) rootView.findViewById(R.id.syncSettingsCheck);
        syncSettingsCheck.setChecked(sPrefs.loadBoolean(Prefs.EXPORT_SETTINGS));

        checkEnabling();
        checkBackup();

        return rootView;
    }

    private void prefsChange (){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        if (syncSettingsCheck.isChecked()){
            sPrefs.saveBoolean(Prefs.EXPORT_SETTINGS, false);
            syncSettingsCheck.setChecked(false);
        } else {
            sPrefs.saveBoolean(Prefs.EXPORT_SETTINGS, true);
            syncSettingsCheck.setChecked(true);
        }
    }

    private void stockChange (){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        if (exportToStockCheck.isChecked()){
            sPrefs.saveBoolean(Prefs.EXPORT_TO_STOCK, false);
            exportToStockCheck.setChecked(false);
        } else {
            sPrefs.saveBoolean(Prefs.EXPORT_TO_STOCK, true);
            exportToStockCheck.setChecked(true);
        }
    }

    private void autoBackupChange (){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        if (autoBackupCheck.isChecked()){
            sPrefs.saveBoolean(Prefs.AUTO_BACKUP, false);
            autoBackupCheck.setChecked(false);
            new AutoSyncAlarm().cancelAlarm(getActivity());
        } else {
            sPrefs.saveBoolean(Prefs.AUTO_BACKUP, true);
            autoBackupCheck.setChecked(true);
            new AutoSyncAlarm().setAlarm(getActivity());
        }
        checkBackup();
    }

    private void checkBackup(){
        if (autoBackupCheck.isChecked())syncInterval.setEnabled(true);
        else syncInterval.setEnabled(false);
    }

    private void checkEnabling(){
        if (exportToCalendarCheck.isChecked()){
            eventDuration.setEnabled(true);
            selectCalendar.setEnabled(true);
        } else {
            eventDuration.setEnabled(false);
            selectCalendar.setEnabled(false);
        }
    }

    private void exportToCalendarChange (){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        if (exportToCalendarCheck.isChecked()){
            sPrefs.saveBoolean(Prefs.EXPORT_TO_CALENDAR, false);
            exportToCalendarCheck.setChecked(false);
            eventDuration.setEnabled(false);
            selectCalendar.setEnabled(false);
            checkEnabling();
        } else {
            ArrayList<String> i = new CalendarManager(getActivity()).getCalendars();
            if (i != null && i.size() > 0) {
                sPrefs.saveBoolean(Prefs.EXPORT_TO_CALENDAR, true);
                exportToCalendarCheck.setChecked(true);
                eventDuration.setEnabled(true);
                selectCalendar.setEnabled(true);
                checkEnabling();
                getActivity().getApplicationContext().startActivity(
                        new Intent(getActivity().getApplicationContext(), SelectCalendar.class)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            } else {
                Toast.makeText(getActivity(),
                        getActivity().getString(R.string.no_google_calendars_found), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        ab = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if (ab != null){
            ab.setTitle(R.string.action_settings);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.exportToStock:
                stockChange();
                break;
            case R.id.exportToCalendar:
                exportToCalendarChange();
                break;
            case R.id.eventDuration:
                Dialog.dialogWithSeek(getActivity(), 120, Prefs.EVENT_DURATION, getString(R.string.event_duration_title), this);
                break;
            case R.id.selectCalendar:
                getActivity().getApplicationContext().startActivity(
                        new Intent(getActivity().getApplicationContext(), SelectCalendar.class)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                break;
            case R.id.autoBackup:
                autoBackupChange();
                break;
            case R.id.clouds:
                getActivity().getApplicationContext().startActivity(
                        new Intent(getActivity().getApplicationContext(), CloudDrives.class)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                break;
            case R.id.clean:
                Dialog.cleanFolders(getActivity());
                break;
            case R.id.syncSettings:
                prefsChange();
                break;
            case R.id.syncInterval:
                Dialog.syncInterval(getActivity());
                break;
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {

    }
}
