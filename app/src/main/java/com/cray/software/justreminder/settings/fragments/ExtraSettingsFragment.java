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

import android.content.DialogInterface;
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
import com.cray.software.justreminder.activities.TemplatesList;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.helpers.Dialogues;
import com.cray.software.justreminder.helpers.Permissions;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.views.PrefsView;

public class ExtraSettingsFragment extends Fragment implements 
        View.OnClickListener, DialogInterface.OnDismissListener {

    private SharedPrefs sPrefs;
    private ActionBar ab;
    private TextView templates;
    
    private PrefsView missedPrefs, missedTimePrefs, quickSMSPrefs, followReminderPrefs;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.settings_extra, container, false);

        ab = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if (ab != null){
            ab.setTitle(R.string.additional);
        }
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());

        missedPrefs = (PrefsView) rootView.findViewById(R.id.missedPrefs);
        missedPrefs.setChecked(sPrefs.loadBoolean(Prefs.MISSED_CALL_REMINDER));
        missedPrefs.setOnClickListener(this);

        missedTimePrefs = (PrefsView) rootView.findViewById(R.id.missedTimePrefs);
        missedTimePrefs.setOnClickListener(this);

        quickSMSPrefs = (PrefsView) rootView.findViewById(R.id.quickSMSPrefs);
        quickSMSPrefs.setChecked(sPrefs.loadBoolean(Prefs.QUICK_SMS));
        quickSMSPrefs.setOnClickListener(this);

        followReminderPrefs = (PrefsView) rootView.findViewById(R.id.followReminderPrefs);
        followReminderPrefs.setChecked(sPrefs.loadBoolean(Prefs.FOLLOW_REMINDER));
        followReminderPrefs.setOnClickListener(this);

        templates = (TextView) rootView.findViewById(R.id.templates);
        templates.setOnClickListener(this);

        checkMissedEnabling();
        checkQuickEnabling();

        return rootView;
    }

    private void checkMissedEnabling(){
        if (missedPrefs.isChecked()){
            missedTimePrefs.setEnabled(true);
        } else {
            missedTimePrefs.setEnabled(false);
        }
    }

    private void missedChange (){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        if (missedPrefs.isChecked()){
            sPrefs.saveBoolean(Prefs.MISSED_CALL_REMINDER, false);
            missedPrefs.setChecked(false);
        } else {
            sPrefs.saveBoolean(Prefs.MISSED_CALL_REMINDER, true);
            missedPrefs.setChecked(true);
        }
        checkMissedEnabling();
    }

    private void checkQuickEnabling(){
        if (quickSMSPrefs.isChecked()){
            templates.setEnabled(true);
        } else {
            templates.setEnabled(false);
        }
    }

    private void quickChange (){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        if (quickSMSPrefs.isChecked()){
            sPrefs.saveBoolean(Prefs.QUICK_SMS, false);
            quickSMSPrefs.setChecked(false);
        } else {
            sPrefs.saveBoolean(Prefs.QUICK_SMS, true);
            quickSMSPrefs.setChecked(true);
        }
        checkQuickEnabling();
    }

    private void followChange (){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        if (followReminderPrefs.isChecked()){
            sPrefs.saveBoolean(Prefs.FOLLOW_REMINDER, false);
            followReminderPrefs.setChecked(false);
        } else {
            sPrefs.saveBoolean(Prefs.FOLLOW_REMINDER, true);
            followReminderPrefs.setChecked(true);
        }
    }

    @Override
    public void onResume() {
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.missedPrefs:
                if (Permissions.checkPermission(getActivity(), Permissions.READ_PHONE_STATE)) {
                    missedChange();
                } else {
                    Permissions.requestPermission(getActivity(), 107, Permissions.READ_PHONE_STATE);
                }
                break;
            case R.id.missedTimePrefs:
                Dialogues.dialogWithSeek(getActivity(), 60, Prefs.MISSED_CALL_TIME, getString(R.string.interval), this);
                break;
            case R.id.quickSMSPrefs:
                if (Permissions.checkPermission(getActivity(), Permissions.READ_PHONE_STATE)) {
                    quickChange();
                } else {
                    Permissions.requestPermission(getActivity(), 108, Permissions.READ_PHONE_STATE);
                }
                break;
            case R.id.templates:
                getActivity().getApplicationContext()
                        .startActivity(new Intent(getActivity().getApplicationContext(), TemplatesList.class)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                break;
            case R.id.followReminderPrefs:
                if (Permissions.checkPermission(getActivity(), Permissions.READ_PHONE_STATE)) {
                    followChange();
                } else {
                    Permissions.requestPermission(getActivity(), 109, Permissions.READ_PHONE_STATE);
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 107:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    missedChange();
                }
                break;
            case 108:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    quickChange();
                }
                break;
            case 109:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    followChange();
                }
                break;
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {

    }
}
