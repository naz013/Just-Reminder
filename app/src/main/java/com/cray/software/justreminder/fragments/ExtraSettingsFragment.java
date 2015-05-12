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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.dialogs.TemplatesList;
import com.cray.software.justreminder.dialogs.utils.RepeatInterval;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.interfaces.Constants;

public class ExtraSettingsFragment extends Fragment implements View.OnClickListener {

    SharedPrefs sPrefs;
    ActionBar ab;
    LinearLayout missedTime;
    RelativeLayout missed, quickSMS, followReminder;
    CheckBox missedCheck, quickSMSCheck, followReminderCheck;
    TextView textMissed2, textMissed3, templates;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.extra_settings_layout, container, false);

        ab = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if (ab != null){
            ab.setTitle(R.string.extra_settings_fragment);
        }
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());

        missed = (RelativeLayout) rootView.findViewById(R.id.missed);
        missed.setOnClickListener(this);

        missedCheck = (CheckBox) rootView.findViewById(R.id.missedCheck);
        missedCheck.setChecked(sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_MISSED_CALL_REMINDER));

        missedTime = (LinearLayout) rootView.findViewById(R.id.missedTime);
        missedTime.setOnClickListener(this);
        textMissed2 = (TextView) rootView.findViewById(R.id.textMissed2);
        textMissed3 = (TextView) rootView.findViewById(R.id.textMissed3);

        templates = (TextView) rootView.findViewById(R.id.templates);
        templates.setOnClickListener(this);

        quickSMS = (RelativeLayout) rootView.findViewById(R.id.quickSMS);
        quickSMS.setOnClickListener(this);

        quickSMSCheck = (CheckBox) rootView.findViewById(R.id.quickSMSCheck);
        quickSMSCheck.setChecked(sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_QUICK_SMS));

        followReminder = (RelativeLayout) rootView.findViewById(R.id.followReminder);
        followReminder.setOnClickListener(this);

        followReminderCheck = (CheckBox) rootView.findViewById(R.id.followReminderCheck);
        followReminderCheck.setChecked(sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_FOLLOW_REMINDER));

        checkMissedEnabling();
        checkQuickEnabling();

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
            sPrefs.saveBoolean(Constants.APP_UI_PREFERENCES_MISSED_CALL_REMINDER, false);
            missedCheck.setChecked(false);
        } else {
            sPrefs.saveBoolean(Constants.APP_UI_PREFERENCES_MISSED_CALL_REMINDER, true);
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

    private void quickChange (){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        if (quickSMSCheck.isChecked()){
            sPrefs.saveBoolean(Constants.APP_UI_PREFERENCES_QUICK_SMS, false);
            quickSMSCheck.setChecked(false);
        } else {
            sPrefs.saveBoolean(Constants.APP_UI_PREFERENCES_QUICK_SMS, true);
            quickSMSCheck.setChecked(true);
        }
        checkQuickEnabling();
    }

    private void followChange (){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        if (followReminderCheck.isChecked()){
            sPrefs.saveBoolean(Constants.APP_UI_PREFERENCES_FOLLOW_REMINDER, false);
            followReminderCheck.setChecked(false);
        } else {
            sPrefs.saveBoolean(Constants.APP_UI_PREFERENCES_FOLLOW_REMINDER, true);
            followReminderCheck.setChecked(true);
        };
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
                missedChange();
                break;
            case R.id.missedTime:
                getActivity().getApplicationContext()
                        .startActivity(new Intent(getActivity().getApplicationContext(), RepeatInterval.class)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                .putExtra(Constants.ITEM_ID_INTENT, 2));
                break;
            case R.id.quickSMS:
                quickChange();
                break;
            case R.id.templates:
                getActivity().getApplicationContext()
                        .startActivity(new Intent(getActivity().getApplicationContext(), TemplatesList.class)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                break;
            case R.id.followReminder:
                followChange();
                break;
        }
    }
}
