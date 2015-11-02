package com.cray.software.justreminder.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.async.CheckBirthdaysAsync;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.dialogs.utils.BirthdayImport;
import com.cray.software.justreminder.helpers.Dialogues;
import com.cray.software.justreminder.helpers.Notifier;
import com.cray.software.justreminder.helpers.Permissions;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.interfaces.Prefs;
import com.cray.software.justreminder.modules.Module;
import com.cray.software.justreminder.services.BirthdayAlarm;
import com.cray.software.justreminder.services.BirthdayCheckAlarm;
import com.cray.software.justreminder.services.BirthdayPermanentAlarm;
import com.cray.software.justreminder.services.SetBirthdays;
import com.cray.software.justreminder.utils.TimeUtil;
import com.cray.software.justreminder.widgets.UpdatesHelper;

import java.util.Calendar;

public class BirthdaysSettingsFragment extends Fragment implements View.OnClickListener, DialogInterface.OnDismissListener {

    private ActionBar ab;
    private SharedPrefs sPrefs;
    private TextView daysToText;
    private TextView contactsScan;
    private TextView reminderTimeText;
    private TextView text3;
    private TextView text4;
    private RelativeLayout autoScan;
    private SwitchCompat contactsSwitch;
    private CheckBox autoScanCheck, widgetShowCheck, backupBirthCheck, birthReminderCheck,
            birthdayPermanentCheck;
    private DataBase db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView =  inflater.inflate(R.layout.birthday_settings_layout, container, false);

        ab = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if (ab != null){
            ab.setTitle(R.string.birthday_settings);
        }

        sPrefs = new SharedPrefs(getActivity().getApplicationContext());

        RelativeLayout useContacts = (RelativeLayout) rootView.findViewById(R.id.useContacts);
        useContacts.setOnClickListener(this);

        contactsSwitch = (SwitchCompat) rootView.findViewById(R.id.contactsSwitch);
        contactsSwitch.setChecked(sPrefs.loadBoolean(Prefs.CONTACT_BIRTHDAYS));

        RelativeLayout daysTo = (RelativeLayout) rootView.findViewById(R.id.daysTo);
        daysTo.setOnClickListener(this);

        daysToText = (TextView) rootView.findViewById(R.id.daysToText);

        RelativeLayout reminderTime = (RelativeLayout) rootView.findViewById(R.id.reminderTime);
        reminderTime.setOnClickListener(this);

        reminderTimeText = (TextView) rootView.findViewById(R.id.reminderTimeText);

        contactsScan = (TextView) rootView.findViewById(R.id.contactsScan);
        contactsScan.setOnClickListener(this);

        TextView birthImport = (TextView) rootView.findViewById(R.id.birthImport);
        birthImport.setOnClickListener(this);
        birthImport.setVisibility(View.GONE);

        RelativeLayout birthReminder = (RelativeLayout) rootView.findViewById(R.id.birthReminder);
        birthReminder.setOnClickListener(this);

        birthReminderCheck = (CheckBox) rootView.findViewById(R.id.birthReminderCheck);
        birthReminderCheck.setChecked(sPrefs.loadBoolean(Prefs.BIRTHDAY_REMINDER));

        autoScan = (RelativeLayout) rootView.findViewById(R.id.autoScan);
        autoScan.setOnClickListener(this);

        autoScanCheck = (CheckBox) rootView.findViewById(R.id.autoScanCheck);
        autoScanCheck.setChecked(sPrefs.loadBoolean(Prefs.AUTO_CHECK_BIRTHDAYS));

        RelativeLayout backupBirth = (RelativeLayout) rootView.findViewById(R.id.backupBirth);
        backupBirth.setOnClickListener(this);

        backupBirthCheck = (CheckBox) rootView.findViewById(R.id.backupBirthCheck);
        backupBirthCheck.setChecked(sPrefs.loadBoolean(Prefs.SYNC_BIRTHDAYS));

        text3 = (TextView) rootView.findViewById(R.id.text3);
        text4 = (TextView) rootView.findViewById(R.id.text4);

        if (Module.isPro()){
            RelativeLayout birthdayNotifContainer = (RelativeLayout) rootView.findViewById(R.id.birthdayNotifContainer);
            birthdayNotifContainer.setVisibility(View.VISIBLE);

            TextView birthdayNotification = (TextView) rootView.findViewById(R.id.birthdayNotification);
            birthdayNotification.setOnClickListener(this);
        }

        RelativeLayout widgetShow = (RelativeLayout) rootView.findViewById(R.id.widgetShow);
        widgetShow.setOnClickListener(this);

        widgetShowCheck = (CheckBox) rootView.findViewById(R.id.widgetShowCheck);
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        widgetShowCheck.setChecked(sPrefs.loadBoolean(Prefs.WIDGET_BIRTHDAYS));

        RelativeLayout birthdayPermanent = (RelativeLayout) rootView.findViewById(R.id.birthdayPermanent);
        birthdayPermanent.setOnClickListener(this);

        birthdayPermanentCheck = (CheckBox) rootView.findViewById(R.id.birthdayPermanentCheck);
        birthdayPermanentCheck.setChecked(sPrefs.loadBoolean(Prefs.BIRTHDAY_PERMANENT));

        checkEnabling();
        return rootView;
    }

    private void showDays(){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        int days;
        if (sPrefs.isString(Prefs.DAYS_TO_BIRTHDAY)) {
            days = sPrefs.loadInt(Prefs.DAYS_TO_BIRTHDAY);
        } else days = 0;
        daysToText.setText(String.valueOf(days));
    }

    private void showTime(){
        sPrefs = new SharedPrefs(getActivity());
        if (sPrefs.isString(Prefs.BIRTHDAY_REMINDER_HOUR)
                && sPrefs.isString(Prefs.BIRTHDAY_REMINDER_MINUTE)){
            int myHour = sPrefs.loadInt(Prefs.BIRTHDAY_REMINDER_HOUR);
            int myMinute = sPrefs.loadInt(Prefs.BIRTHDAY_REMINDER_MINUTE);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.HOUR_OF_DAY, myHour);
            calendar.set(Calendar.MINUTE, myMinute);
            reminderTimeText.setText(TimeUtil.getTime(calendar.getTime(),
                    sPrefs.loadBoolean(Prefs.IS_24_TIME_FORMAT)));
        }
    }

    private void checkEnabling(){
        if (contactsSwitch.isChecked()){
            contactsScan.setEnabled(true);
            autoScan.setEnabled(true);
            autoScanCheck.setEnabled(true);
            text3.setEnabled(true);
            text4.setEnabled(true);
        } else {
            contactsScan.setEnabled(false);
            autoScan.setEnabled(false);
            autoScanCheck.setEnabled(false);
            text3.setEnabled(false);
            text4.setEnabled(false);
        }
    }

    private void autoScanCheck (){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        BirthdayCheckAlarm alarm = new BirthdayCheckAlarm();
        if (autoScanCheck.isChecked()){
            sPrefs.saveBoolean(Prefs.AUTO_CHECK_BIRTHDAYS, false);
            autoScanCheck.setChecked(false);
            alarm.cancelAlarm(getActivity());
        } else {
            sPrefs.saveBoolean(Prefs.AUTO_CHECK_BIRTHDAYS, true);
            autoScanCheck.setChecked(true);
            alarm.setAlarm(getActivity());
        }
    }

    private void widgetCheck (){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        if (widgetShowCheck.isChecked()){
            sPrefs.saveBoolean(Prefs.WIDGET_BIRTHDAYS, false);
            widgetShowCheck.setChecked(false);
            UpdatesHelper helper = new UpdatesHelper(getActivity());
            helper.updateWidget();
        } else {
            sPrefs.saveBoolean(Prefs.WIDGET_BIRTHDAYS, true);
            widgetShowCheck.setChecked(true);
            UpdatesHelper helper = new UpdatesHelper(getActivity());
            helper.updateWidget();
        }
    }

    private void setBackupBirthCheck (){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        if (backupBirthCheck.isChecked()){
            sPrefs.saveBoolean(Prefs.SYNC_BIRTHDAYS, false);
            backupBirthCheck.setChecked(false);
        } else {
            sPrefs.saveBoolean(Prefs.SYNC_BIRTHDAYS, true);
            backupBirthCheck.setChecked(true);
        }
    }

    private void setBirthdayPermanentCheck(){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        if (birthdayPermanentCheck.isChecked()){
            sPrefs.saveBoolean(Prefs.BIRTHDAY_PERMANENT, false);
            birthdayPermanentCheck.setChecked(false);
            new Notifier(getActivity()).hideBirthdayPermanent();
            new BirthdayPermanentAlarm().cancelAlarm(getActivity());
        } else {
            sPrefs.saveBoolean(Prefs.BIRTHDAY_PERMANENT, true);
            birthdayPermanentCheck.setChecked(true);
            new Notifier(getActivity()).showBirthdayPermanent();
            new BirthdayPermanentAlarm().setAlarm(getActivity());
        }
    }

    private void setContactsSwitch (){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        if (contactsSwitch.isChecked()){
            sPrefs.saveBoolean(Prefs.CONTACT_BIRTHDAYS, false);
            contactsSwitch.setChecked(false);
            checkEnabling();
        } else {
            sPrefs.saveBoolean(Prefs.CONTACT_BIRTHDAYS, true);
            contactsSwitch.setChecked(true);
            checkEnabling();
        }
    }

    private void cleanBirthdays(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                db = new DataBase(getActivity());
                db.open();
                if (db.getCountBirthdays() > 0){
                    Cursor c = db.getBirthdays();
                    if (c != null && c.moveToFirst()){
                        do {
                            long id = c.getLong(c.getColumnIndex(Constants.ContactConstants.COLUMN_ID));
                            db.deleteBirthday(id);
                        } while (c.moveToNext());
                        c.close();
                    }
                }
                db.close();
            }
        }).start();
    }

    @Override
    public void onResume() {
        super.onResume();
        showDays();
        showTime();
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
        switch (v.getId()){
            case R.id.reminderTime:
                new BirthdayAlarm().cancelAlarm(getActivity());
                getActivity().stopService(new Intent(getActivity(), SetBirthdays.class));
                DialogFragment timePickerFragment = new TimePickerFragment();
                timePickerFragment.show(getActivity().getSupportFragmentManager(), "timePicker");
                break;
            case R.id.useContacts:
                setContactsSwitch();
                break;
            case R.id.autoScan:
                autoScanCheck();
                break;
            case R.id.backupBirth:
                setBackupBirthCheck();
                break;
            case R.id.widgetShow:
                widgetCheck();
                break;
            case R.id.birthReminder:
                birthCheck();
                break;
            case R.id.birthdayPermanent:
                setBirthdayPermanentCheck();
                break;
            case R.id.birthImport:
                getActivity().getApplicationContext()
                        .startActivity(new Intent(getActivity().getApplicationContext(), BirthdayImport.class)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                break;
            case R.id.contactsScan:
                if (new Permissions(getActivity()).checkPermission(Permissions.READ_CONTACTS)) {
                    new CheckBirthdaysAsync(getActivity(), true).execute();
                } else {
                    new Permissions(getActivity())
                            .requestPermission(getActivity(),
                                    new String[]{Permissions.READ_CONTACTS}, 106);
                }
                break;
            case R.id.daysTo:
                Dialogues.dialogWithSeek(getActivity(), 5, Prefs.DAYS_TO_BIRTHDAY, getString(R.string.days_to_dialog_title), this);
                break;
            case R.id.birthdayNotification:
                BirthdayNotificationSettingsFragment newFragment = new BirthdayNotificationSettingsFragment();
                Bundle args = new Bundle();
                newFragment.setArguments(args);
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, newFragment);
                transaction.addToBackStack("birth_notif");
                transaction.commit();
                break;
        }
    }

    private void birthCheck() {
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        if (birthReminderCheck.isChecked()){
            sPrefs.saveBoolean(Prefs.BIRTHDAY_REMINDER, false);
            birthReminderCheck.setChecked(false);
            cleanBirthdays();
        } else {
            sPrefs.saveBoolean(Prefs.BIRTHDAY_REMINDER, true);
            birthReminderCheck.setChecked(true);
            getActivity().startService(new Intent(getActivity(), SetBirthdays.class));
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        showDays();
        showTime();
    }
}
