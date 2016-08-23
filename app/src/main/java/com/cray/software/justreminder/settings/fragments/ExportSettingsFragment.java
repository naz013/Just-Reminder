/**
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

package com.cray.software.justreminder.settings.fragments;

import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.activities.CloudDrivesActivity;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.helpers.CalendarHelper;
import com.cray.software.justreminder.helpers.Dialogues;
import com.cray.software.justreminder.helpers.Permissions;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.services.AutoSyncAlarm;
import com.cray.software.justreminder.views.PrefsView;

import java.util.ArrayList;

public class ExportSettingsFragment extends Fragment implements View.OnClickListener, DialogInterface.OnDismissListener {
    
    private TextView eventDuration;
    private TextView selectCalendar;
    private TextView syncInterval;
    private SharedPrefs sPrefs;
    private ActionBar ab;
    private PrefsView exportToCalendarPrefs, exportToStockPrefs, autoBackupPrefs, 
            syncSettingsPrefs;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView =  inflater.inflate(R.layout.settings_export, container, false);
        ab = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (ab != null){
            ab.setTitle(R.string.export_and_sync);
        }
        sPrefs = SharedPrefs.getInstance(getActivity());

        exportToCalendarPrefs = (PrefsView) rootView.findViewById(R.id.exportToCalendarPrefs);
        exportToCalendarPrefs.setChecked(sPrefs.getBoolean(Prefs.EXPORT_TO_CALENDAR));
        exportToCalendarPrefs.setOnClickListener(this);

        exportToStockPrefs = (PrefsView) rootView.findViewById(R.id.exportToStockPrefs);
        exportToStockPrefs.setChecked(sPrefs.getBoolean(Prefs.EXPORT_TO_STOCK));
        exportToStockPrefs.setOnClickListener(this);

        autoBackupPrefs = (PrefsView) rootView.findViewById(R.id.autoBackupPrefs);
        autoBackupPrefs.setChecked(sPrefs.getBoolean(Prefs.AUTO_BACKUP));
        autoBackupPrefs.setOnClickListener(this);

        syncSettingsPrefs = (PrefsView) rootView.findViewById(R.id.syncSettingsPrefs);
        syncSettingsPrefs.setChecked(sPrefs.getBoolean(Prefs.EXPORT_SETTINGS));
        syncSettingsPrefs.setOnClickListener(this);

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

        checkEnabling();
        checkBackup();

        return rootView;
    }

    private void prefsChange (){
        if (syncSettingsPrefs.isChecked()){
            sPrefs.putBoolean(Prefs.EXPORT_SETTINGS, false);
            syncSettingsPrefs.setChecked(false);
        } else {
            sPrefs.putBoolean(Prefs.EXPORT_SETTINGS, true);
            syncSettingsPrefs.setChecked(true);
        }
    }

    private void stockChange (){
        if (exportToStockPrefs.isChecked()){
            sPrefs.putBoolean(Prefs.EXPORT_TO_STOCK, false);
            exportToStockPrefs.setChecked(false);
        } else {
            sPrefs.putBoolean(Prefs.EXPORT_TO_STOCK, true);
            exportToStockPrefs.setChecked(true);
        }
    }

    private void autoBackupChange (){
        if (autoBackupPrefs.isChecked()){
            sPrefs.putBoolean(Prefs.AUTO_BACKUP, false);
            autoBackupPrefs.setChecked(false);
            new AutoSyncAlarm().cancelAlarm(getActivity());
        } else {
            sPrefs.putBoolean(Prefs.AUTO_BACKUP, true);
            autoBackupPrefs.setChecked(true);
            new AutoSyncAlarm().setAlarm(getActivity());
        }
        checkBackup();
    }

    private void checkBackup(){
        if (autoBackupPrefs.isChecked())syncInterval.setEnabled(true);
        else syncInterval.setEnabled(false);
    }

    private void checkEnabling(){
        if (exportToCalendarPrefs.isChecked()){
            eventDuration.setEnabled(true);
            selectCalendar.setEnabled(true);
        } else {
            eventDuration.setEnabled(false);
            selectCalendar.setEnabled(false);
        }
    }

    private void exportToCalendarChange (){
        if (exportToCalendarPrefs.isChecked()){
            sPrefs.putBoolean(Prefs.EXPORT_TO_CALENDAR, false);
            exportToCalendarPrefs.setChecked(false);
            eventDuration.setEnabled(false);
            selectCalendar.setEnabled(false);
            checkEnabling();
        } else {
            if (Permissions.checkPermission(getActivity(), Permissions.READ_CALENDAR)) {
                loadCalendars();
            } else {
                Permissions.requestPermission(getActivity(), 101, Permissions.READ_CALENDAR);
            }
        }
    }

    private void loadCalendars() {
        ArrayList<String> i = new CalendarHelper(getActivity()).getCalendars();
        if (i != null && i.size() > 0) {
            sPrefs.putBoolean(Prefs.EXPORT_TO_CALENDAR, true);
            exportToCalendarPrefs.setChecked(true);
            eventDuration.setEnabled(true);
            selectCalendar.setEnabled(true);
            checkEnabling();
            ArrayList<CalendarHelper.CalendarItem> list = new CalendarHelper(getActivity()).getCalendarsList();
            Dialogues.selectCalendar(getActivity(), list);
        } else {
            Toast.makeText(getActivity(),
                    getActivity().getString(R.string.no_calendars_found), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        ab = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (ab != null){
            ab.setTitle(R.string.action_settings);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.exportToStockPrefs:
                stockChange();
                break;
            case R.id.exportToCalendarPrefs:
                exportToCalendarChange();
                break;
            case R.id.eventDuration:
                Dialogues.dialogWithSeek(getActivity(), 120, Prefs.EVENT_DURATION, getString(R.string.event_duration), this);
                break;
            case R.id.selectCalendar:
                ArrayList<CalendarHelper.CalendarItem> list = new CalendarHelper(getActivity()).getCalendarsList();
                Dialogues.selectCalendar(getActivity(), list);
                break;
            case R.id.autoBackupPrefs:
                autoBackupChange();
                break;
            case R.id.clouds:
                getActivity().getApplicationContext().startActivity(
                        new Intent(getActivity().getApplicationContext(), CloudDrivesActivity.class)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                break;
            case R.id.clean:
                Dialogues.cleanFolders(getActivity());
                break;
            case R.id.syncSettingsPrefs:
                prefsChange();
                break;
            case R.id.syncInterval:
                Dialogues.selectInterval(getActivity(), Prefs.AUTO_BACKUP_INTERVAL, R.string.interval);
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 101:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    loadCalendars();
                }
                break;
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {

    }
}
