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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.dialogs.TemplatesList;
import com.cray.software.justreminder.dialogs.utils.ContactGroups;
import com.cray.software.justreminder.helpers.Dialogues;
import com.cray.software.justreminder.helpers.Permissions;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.interfaces.Prefs;

public class ExtraSettingsFragment extends Fragment implements View.OnClickListener, DialogInterface.OnDismissListener {

    private SharedPrefs sPrefs;
    private ActionBar ab;
    private LinearLayout missedTime;
    private CheckBox missedCheck, quickSMSCheck, followReminderCheck;
    private TextView textMissed2, textMissed3, templates, contactGroups;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.settings_extra, container, false);

        ab = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if (ab != null){
            ab.setTitle(R.string.extra_settings_fragment);
        }
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());

        RelativeLayout missed = (RelativeLayout) rootView.findViewById(R.id.missed);
        missed.setOnClickListener(this);

        missedCheck = (CheckBox) rootView.findViewById(R.id.missedCheck);
        missedCheck.setChecked(sPrefs.loadBoolean(Prefs.MISSED_CALL_REMINDER));

        missedTime = (LinearLayout) rootView.findViewById(R.id.missedTime);
        missedTime.setOnClickListener(this);
        textMissed2 = (TextView) rootView.findViewById(R.id.textMissed2);
        textMissed3 = (TextView) rootView.findViewById(R.id.textMissed3);

        templates = (TextView) rootView.findViewById(R.id.templates);
        templates.setOnClickListener(this);

        contactGroups = (TextView) rootView.findViewById(R.id.contactGroups);
        contactGroups.setOnClickListener(this);

        RelativeLayout quickSMS = (RelativeLayout) rootView.findViewById(R.id.quickSMS);
        quickSMS.setOnClickListener(this);

        quickSMSCheck = (CheckBox) rootView.findViewById(R.id.quickSMSCheck);
        quickSMSCheck.setChecked(sPrefs.loadBoolean(Prefs.QUICK_SMS));

        RelativeLayout followReminder = (RelativeLayout) rootView.findViewById(R.id.followReminder);
        followReminder.setOnClickListener(this);

        followReminderCheck = (CheckBox) rootView.findViewById(R.id.followReminderCheck);
        followReminderCheck.setChecked(sPrefs.loadBoolean(Prefs.FOLLOW_REMINDER));

        checkMissedEnabling();
        checkQuickEnabling();
        checkFollow();

        return rootView;
    }

    private void checkMissedEnabling(){
        if (missedCheck.isChecked()){
            missedTime.setEnabled(true);
            textMissed2.setEnabled(true);
            textMissed3.setEnabled(true);
        } else {
            missedTime.setEnabled(false);
            textMissed2.setEnabled(false);
            textMissed3.setEnabled(false);
        }
    }

    private void missedChange (){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        if (missedCheck.isChecked()){
            sPrefs.saveBoolean(Prefs.MISSED_CALL_REMINDER, false);
            missedCheck.setChecked(false);
        } else {
            sPrefs.saveBoolean(Prefs.MISSED_CALL_REMINDER, true);
            missedCheck.setChecked(true);
        }
        checkMissedEnabling();
    }

    private void checkQuickEnabling(){
        if (quickSMSCheck.isChecked()){
            templates.setEnabled(true);
        } else {
            templates.setEnabled(false);
        }
    }

    private void checkFollow(){
        if (followReminderCheck.isChecked()){
            contactGroups.setEnabled(true);
        } else {
            contactGroups.setEnabled(false);
        }
    }

    private void quickChange (){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        if (quickSMSCheck.isChecked()){
            sPrefs.saveBoolean(Prefs.QUICK_SMS, false);
            quickSMSCheck.setChecked(false);
        } else {
            sPrefs.saveBoolean(Prefs.QUICK_SMS, true);
            quickSMSCheck.setChecked(true);
        }
        checkQuickEnabling();
    }

    private void followChange (){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        if (followReminderCheck.isChecked()){
            sPrefs.saveBoolean(Prefs.FOLLOW_REMINDER, false);
            followReminderCheck.setChecked(false);
        } else {
            sPrefs.saveBoolean(Prefs.FOLLOW_REMINDER, true);
            followReminderCheck.setChecked(true);
        }
        checkFollow();
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
            case R.id.missed:
                if (new Permissions(getActivity()).checkPermission(Permissions.READ_PHONE_STATE)) {
                    missedChange();
                } else {
                    new Permissions(getActivity())
                            .requestPermission(getActivity(),
                                    new String[]{Permissions.READ_PHONE_STATE}, 107);
                }
                break;
            case R.id.missedTime:
                Dialogues.dialogWithSeek(getActivity(), 60, Prefs.MISSED_CALL_TIME, getString(R.string.repeat_interval_dialog_title), this);
                break;
            case R.id.quickSMS:
                if (new Permissions(getActivity()).checkPermission(Permissions.READ_PHONE_STATE)) {
                    quickChange();
                } else {
                    new Permissions(getActivity())
                            .requestPermission(getActivity(),
                                    new String[]{Permissions.READ_PHONE_STATE}, 108);
                }
                break;
            case R.id.templates:
                getActivity().getApplicationContext()
                        .startActivity(new Intent(getActivity().getApplicationContext(), TemplatesList.class)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                break;
            case R.id.followReminder:
                if (new Permissions(getActivity()).checkPermission(Permissions.READ_PHONE_STATE)) {
                    followChange();
                } else {
                    new Permissions(getActivity())
                            .requestPermission(getActivity(),
                                    new String[]{Permissions.READ_PHONE_STATE}, 109);
                }
                break;
            case R.id.contactGroups:
                getActivity().getApplicationContext()
                        .startActivity(new Intent(getActivity().getApplicationContext(), ContactGroups.class)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                break;
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {

    }
}
