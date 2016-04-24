package com.cray.software.justreminder.settings.fragments;

import android.content.DialogInterface;
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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.async.CheckBirthdaysAsync;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.fragments.helpers.TimePickerFragment;
import com.cray.software.justreminder.helpers.Dialogues;
import com.cray.software.justreminder.helpers.Notifier;
import com.cray.software.justreminder.helpers.Permissions;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.modules.Module;
import com.cray.software.justreminder.services.BirthdayAlarm;
import com.cray.software.justreminder.services.BirthdayCheckAlarm;
import com.cray.software.justreminder.services.BirthdayPermanentAlarm;
import com.cray.software.justreminder.utils.TimeUtil;
import com.cray.software.justreminder.views.PrefsView;
import com.cray.software.justreminder.app_widgets.UpdatesHelper;

import java.util.Calendar;

public class BirthdaysSettingsFragment extends Fragment implements View.OnClickListener,
        DialogInterface.OnDismissListener {

    private ActionBar ab;
    private SharedPrefs sPrefs;
    private TextView contactsScan, reminderTimeText;
    private SwitchCompat contactsSwitch;
    
    private PrefsView birthReminderPrefs, widgetShowPrefs, birthdayPermanentPrefs, 
            daysToPrefs, backupBirthPrefs, autoScanPrefs;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView =  inflater.inflate(R.layout.settings_birthdays_layout, container, false);

        ab = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if (ab != null){
            ab.setTitle(R.string.birthdays);
        }

        sPrefs = new SharedPrefs(getActivity().getApplicationContext());

        RelativeLayout useContacts = (RelativeLayout) rootView.findViewById(R.id.useContacts);
        RelativeLayout reminderTime = (RelativeLayout) rootView.findViewById(R.id.reminderTime);
        reminderTimeText = (TextView) rootView.findViewById(R.id.reminderTimeText);
        useContacts.setOnClickListener(this);
        reminderTime.setOnClickListener(this);

        contactsSwitch = (SwitchCompat) rootView.findViewById(R.id.contactsSwitch);
        contactsSwitch.setChecked(sPrefs.loadBoolean(Prefs.CONTACT_BIRTHDAYS));

        birthReminderPrefs = (PrefsView) rootView.findViewById(R.id.birthReminderPrefs);
        birthReminderPrefs.setOnClickListener(this);
        birthReminderPrefs.setChecked(sPrefs.loadBoolean(Prefs.BIRTHDAY_REMINDER));

        widgetShowPrefs = (PrefsView) rootView.findViewById(R.id.widgetShowPrefs);
        widgetShowPrefs.setOnClickListener(this);
        widgetShowPrefs.setChecked(sPrefs.loadBoolean(Prefs.WIDGET_BIRTHDAYS));

        birthdayPermanentPrefs = (PrefsView) rootView.findViewById(R.id.birthdayPermanentPrefs);
        birthdayPermanentPrefs.setOnClickListener(this);
        birthdayPermanentPrefs.setChecked(sPrefs.loadBoolean(Prefs.BIRTHDAY_PERMANENT));

        daysToPrefs = (PrefsView) rootView.findViewById(R.id.daysToPrefs);
        daysToPrefs.setOnClickListener(this);

        contactsScan = (TextView) rootView.findViewById(R.id.contactsScan);
        contactsScan.setOnClickListener(this);

        backupBirthPrefs = (PrefsView) rootView.findViewById(R.id.backupBirthPrefs);
        backupBirthPrefs.setOnClickListener(this);
        backupBirthPrefs.setChecked(sPrefs.loadBoolean(Prefs.SYNC_BIRTHDAYS));

        autoScanPrefs = (PrefsView) rootView.findViewById(R.id.autoScanPrefs);
        autoScanPrefs.setOnClickListener(this);
        autoScanPrefs.setChecked(sPrefs.loadBoolean(Prefs.AUTO_CHECK_BIRTHDAYS));

        if (Module.isPro()){
            RelativeLayout birthdayNotifContainer = (RelativeLayout) rootView.findViewById(R.id.birthdayNotifContainer);
            birthdayNotifContainer.setVisibility(View.VISIBLE);

            TextView birthdayNotification = (TextView) rootView.findViewById(R.id.birthdayNotification);
            birthdayNotification.setOnClickListener(this);
        }

        checkEnabling();
        return rootView;
    }

    private void showDays(){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        int days;
        if (sPrefs.isString(Prefs.DAYS_TO_BIRTHDAY)) {
            days = sPrefs.loadInt(Prefs.DAYS_TO_BIRTHDAY);
        } else days = 0;
        daysToPrefs.setValueText(String.valueOf(days));
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
            autoScanPrefs.setEnabled(true);
        } else {
            contactsScan.setEnabled(false);
            autoScanPrefs.setEnabled(false);
        }
    }

    private void autoScanCheck (){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        BirthdayCheckAlarm alarm = new BirthdayCheckAlarm();
        if (autoScanPrefs.isChecked()){
            sPrefs.saveBoolean(Prefs.AUTO_CHECK_BIRTHDAYS, false);
            autoScanPrefs.setChecked(false);
            alarm.cancelAlarm(getActivity());
        } else {
            sPrefs.saveBoolean(Prefs.AUTO_CHECK_BIRTHDAYS, true);
            autoScanPrefs.setChecked(true);
            alarm.setAlarm(getActivity());
        }
    }

    private void widgetCheck (){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        if (widgetShowPrefs.isChecked()){
            sPrefs.saveBoolean(Prefs.WIDGET_BIRTHDAYS, false);
            widgetShowPrefs.setChecked(false);
            UpdatesHelper helper = new UpdatesHelper(getActivity());
            helper.updateWidget();
        } else {
            sPrefs.saveBoolean(Prefs.WIDGET_BIRTHDAYS, true);
            widgetShowPrefs.setChecked(true);
            UpdatesHelper helper = new UpdatesHelper(getActivity());
            helper.updateWidget();
        }
    }

    private void setBackupBirthCheck (){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        if (backupBirthPrefs.isChecked()){
            sPrefs.saveBoolean(Prefs.SYNC_BIRTHDAYS, false);
            backupBirthPrefs.setChecked(false);
        } else {
            sPrefs.saveBoolean(Prefs.SYNC_BIRTHDAYS, true);
            backupBirthPrefs.setChecked(true);
        }
    }

    private void setBirthdayPermanentCheck(){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        if (birthdayPermanentPrefs.isChecked()){
            sPrefs.saveBoolean(Prefs.BIRTHDAY_PERMANENT, false);
            birthdayPermanentPrefs.setChecked(false);
            new Notifier(getActivity()).hideBirthdayPermanent();
            new BirthdayPermanentAlarm().cancelAlarm(getActivity());
        } else {
            sPrefs.saveBoolean(Prefs.BIRTHDAY_PERMANENT, true);
            birthdayPermanentPrefs.setChecked(true);
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
                DataBase db = new DataBase(getActivity());
                db.open();
                Cursor c = db.getBirthdays();
                if (c != null && c.moveToFirst()){
                    do {
                        long id = c.getLong(c.getColumnIndex(Constants.ContactConstants.COLUMN_ID));
                        db.deleteBirthday(id);
                    } while (c.moveToNext());
                    c.close();
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
                DialogFragment timePickerFragment = new TimePickerFragment();
                timePickerFragment.show(getActivity().getSupportFragmentManager(), "timePicker");
                break;
            case R.id.useContacts:
                setContactsSwitch();
                break;
            case R.id.autoScanPrefs:
                autoScanCheck();
                break;
            case R.id.backupBirthPrefs:
                setBackupBirthCheck();
                break;
            case R.id.widgetShowPrefs:
                widgetCheck();
                break;
            case R.id.birthReminderPrefs:
                birthCheck();
                break;
            case R.id.birthdayPermanentPrefs:
                setBirthdayPermanentCheck();
                break;
            case R.id.contactsScan:
                if (Permissions.checkPermission(getActivity(), Permissions.READ_CONTACTS)) {
                    new CheckBirthdaysAsync(getActivity(), true).execute();
                } else {
                    Permissions
                            .requestPermission(getActivity(), 106,
                                    Permissions.READ_CONTACTS);
                }
                break;
            case R.id.daysToPrefs:
                Dialogues.dialogWithSeek(getActivity(), 5, Prefs.DAYS_TO_BIRTHDAY, getString(R.string.days_to_birthday), this);
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
        if (birthReminderPrefs.isChecked()){
            sPrefs.saveBoolean(Prefs.BIRTHDAY_REMINDER, false);
            birthReminderPrefs.setChecked(false);
            cleanBirthdays();
        } else {
            sPrefs.saveBoolean(Prefs.BIRTHDAY_REMINDER, true);
            birthReminderPrefs.setChecked(true);
            new BirthdayAlarm().setAlarm(getActivity());
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        showDays();
        showTime();
    }
}
