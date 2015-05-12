package com.cray.software.justreminder.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
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
import com.cray.software.justreminder.cloud.DropboxHelper;
import com.cray.software.justreminder.cloud.GDriveHelper;
import com.cray.software.justreminder.dialogs.CloudDrives;
import com.cray.software.justreminder.dialogs.utils.EventDuration;
import com.cray.software.justreminder.dialogs.utils.SelectCalendar;
import com.cray.software.justreminder.helpers.CalendarManager;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.interfaces.Constants;

import java.io.File;
import java.util.ArrayList;

public class ExportSettingsFragment extends Fragment implements View.OnClickListener {

    RelativeLayout exportToCalendar, autoBackup, exportToStock, exportTasks;
    CheckBox exportToCalendarCheck, autoBackupCheck, exportToStockCheck, exportTasksCheck;
    TextView eventDuration, selectCalendar, clouds, clean;
    SharedPrefs sPrefs;
    ActionBar ab;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView =  inflater.inflate(R.layout.export_settings_layout, container, false);

        ab = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if (ab != null){
            ab.setTitle(R.string.export_settings_block);
        }

        exportToCalendar = (RelativeLayout) rootView.findViewById(R.id.exportToCalendar);
        exportToCalendar.setOnClickListener(this);

        exportToCalendarCheck = (CheckBox) rootView.findViewById(R.id.exportToCalendarCheck);
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        exportToCalendarCheck.setChecked(sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_EXPORT_TO_CALENDAR));

        eventDuration = (TextView) rootView.findViewById(R.id.eventDuration);
        eventDuration.setOnClickListener(this);

        selectCalendar = (TextView) rootView.findViewById(R.id.selectCalendar);
        selectCalendar.setOnClickListener(this);

        clouds = (TextView) rootView.findViewById(R.id.clouds);
        clouds.setOnClickListener(this);

        clean = (TextView) rootView.findViewById(R.id.clean);
        clean.setOnClickListener(this);

        autoBackup = (RelativeLayout) rootView.findViewById(R.id.autoBackup);
        autoBackup.setOnClickListener(this);

        autoBackupCheck = (CheckBox) rootView.findViewById(R.id.autoBackupCheck);
        autoBackupCheck.setChecked(sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_AUTO_BACKUP));

        exportToStock = (RelativeLayout) rootView.findViewById(R.id.exportToStock);
        exportToStock.setOnClickListener(this);

        exportToStockCheck = (CheckBox) rootView.findViewById(R.id.exportToStockCheck);
        exportToStockCheck.setChecked(sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_EXPORT_TO_STOCK));

        checkEnabling();

        return rootView;
    }

    private void stockChange (){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        if (exportToStockCheck.isChecked()){
            sPrefs.saveBoolean(Constants.APP_UI_PREFERENCES_EXPORT_TO_STOCK, false);
            exportToStockCheck.setChecked(false);
        } else {
            sPrefs.saveBoolean(Constants.APP_UI_PREFERENCES_EXPORT_TO_STOCK, true);
            exportToStockCheck.setChecked(true);
        }
    }

    private void autoBackupChange (){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        if (autoBackupCheck.isChecked()){
            sPrefs.saveBoolean(Constants.APP_UI_PREFERENCES_AUTO_BACKUP, false);
            autoBackupCheck.setChecked(false);
        } else {
            sPrefs.saveBoolean(Constants.APP_UI_PREFERENCES_AUTO_BACKUP, true);
            autoBackupCheck.setChecked(true);
        }
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
            sPrefs.saveBoolean(Constants.APP_UI_PREFERENCES_EXPORT_TO_CALENDAR, false);
            exportToCalendarCheck.setChecked(false);
            eventDuration.setEnabled(false);
            selectCalendar.setEnabled(false);
            checkEnabling();
        } else {
            ArrayList<String> i = new CalendarManager(getActivity()).getCalendars();
            if (i != null && i.size() > 0) {
                sPrefs.saveBoolean(Constants.APP_UI_PREFERENCES_EXPORT_TO_CALENDAR, true);
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
                getActivity().getApplicationContext().startActivity(
                        new Intent(getActivity().getApplicationContext(), EventDuration.class)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
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
                cleanFolders();
                break;
        }
    }

    public void DeleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                DeleteRecursive(child);

        fileOrDirectory.delete();
    }

    private void cleanFolders() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(true);
        builder.setTitle(getActivity().getString(R.string.settings_clean_title));
        builder.setMessage(getActivity().getString(R.string.clean_dialog_message));
        builder.setNeutralButton(getActivity().getString(R.string.clean_dialog_button_local), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SyncHelper syncHelper = new SyncHelper(getActivity());
                if (syncHelper.isSdPresent()){
                    File sdPath = Environment.getExternalStorageDirectory();
                    File sdPathDr = new File(sdPath.getAbsolutePath() + "/JustReminder/");
                    DeleteRecursive(sdPathDr);
                }
            }
        });
        builder.setNegativeButton(getActivity().getString(R.string.button_close), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setPositiveButton(getActivity().getString(R.string.clean_dialog_button_full), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                SyncHelper syncHelper = new SyncHelper(getActivity());
                if (syncHelper.isSdPresent()){
                    File sdPath = Environment.getExternalStorageDirectory();
                    File sdPathDr = new File(sdPath.getAbsolutePath() + "/JustReminder/");
                    DeleteRecursive(sdPathDr);
                }
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        GDriveHelper gdx = new GDriveHelper(getActivity());
                        DropboxHelper dbx = new DropboxHelper(getActivity());
                        if (SyncHelper.isConnected(getActivity())){
                            gdx.clean();
                            dbx.cleanFolder();
                        }
                    }
                }).start();

            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
