package com.cray.software.justreminder.fragments;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.provider.ContactsContract;
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
import android.widget.Toast;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.dialogs.utils.BirthdayImport;
import com.cray.software.justreminder.helpers.Contacts;
import com.cray.software.justreminder.helpers.Dialog;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.interfaces.Prefs;
import com.cray.software.justreminder.modules.Module;
import com.cray.software.justreminder.services.BirthdayAlarm;
import com.cray.software.justreminder.services.BirthdayCheckAlarm;
import com.cray.software.justreminder.services.SetBirthdays;
import com.cray.software.justreminder.utils.TimeUtil;
import com.cray.software.justreminder.widgets.UpdatesHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

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
    private CheckBox autoScanCheck, widgetShowCheck, backupBirthCheck, birthReminderCheck;
    private DataBase db;
    private BirthdayAlarm alarmReceiver = new BirthdayAlarm();

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
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        if (sPrefs.isString(Prefs.BIRTHDAY_REMINDER_HOUR)
                && sPrefs.isString(Prefs.BIRTHDAY_REMINDER_MINUTE)){
            int myHour = sPrefs.loadInt(Prefs.BIRTHDAY_REMINDER_HOUR);
            int myMinute = sPrefs.loadInt(Prefs.BIRTHDAY_REMINDER_MINUTE);
            Calendar calendar = Calendar.getInstance();
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
                alarmReceiver.cancelAlarm(getActivity(), 0);
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
            case R.id.birthImport:
                getActivity().getApplicationContext()
                        .startActivity(new Intent(getActivity().getApplicationContext(), BirthdayImport.class)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                break;
            case R.id.contactsScan:
                new checkBirthdays(getActivity()).execute();
                break;
            case R.id.daysTo:
                Dialog.dialogWithSeek(getActivity(), 5, Prefs.DAYS_TO_BIRTHDAY, getString(R.string.days_to_dialog_title), this);
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

    class checkBirthdays extends AsyncTask<Void, Void, Integer>{

        ProgressDialog pd;
        Context tContext;
        DataBase db;
        Contacts cc;
        //SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        public final SimpleDateFormat[] birthdayFormats = {
                new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()),
                new SimpleDateFormat("yyyyMMdd", Locale.getDefault()),
                new SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()),
                new SimpleDateFormat("yy.MM.dd", Locale.getDefault()),
                new SimpleDateFormat("yy/MM/dd", Locale.getDefault()),
        };

        public checkBirthdays(Context context){
            this.tContext = context;
            pd = new ProgressDialog(context);
            pd.setCancelable(true);
            pd.setMessage(context.getString(R.string.checking_new_birthdays_title));
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd.show();
        }

        @Override
        protected Integer doInBackground(Void... params) {
            int i = 0;
            ContentResolver cr = tContext.getContentResolver();
            db = new DataBase(tContext);
            db.open();
            String[] projection = new String[] { ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME};

            Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, projection, null, null,
                    ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC");

            while (cur.moveToNext()) {
                String contactId = cur.getString(cur.getColumnIndex(ContactsContract.Data._ID));


                String columns[] = {
                        ContactsContract.CommonDataKinds.Event.START_DATE,
                        ContactsContract.CommonDataKinds.Event.TYPE,
                        ContactsContract.CommonDataKinds.Event.MIMETYPE,
                        ContactsContract.PhoneLookup.DISPLAY_NAME,
                        ContactsContract.Contacts._ID,
                };

                String where = ContactsContract.CommonDataKinds.Event.TYPE + "=" + ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY +
                        " and " + ContactsContract.CommonDataKinds.Event.MIMETYPE + " = '" + ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE +
                        "' and "                  + ContactsContract.Data.CONTACT_ID + " = " + contactId;

                String[] selectionArgs = null;
                String sortOrder = ContactsContract.Contacts.DISPLAY_NAME;
                cc = new Contacts(tContext);
                Cursor cursor = db.getBirthdays();
                ArrayList<Integer> types = new ArrayList<>();
                if (cursor != null && cursor.moveToFirst()){
                    do{
                        int tp = cursor.getInt(cursor.getColumnIndex(Constants.ContactConstants.COLUMN_CONTACT_ID));
                        types.add(tp);
                    } while (cursor.moveToNext());
                }
                Cursor birthdayCur = cr.query(ContactsContract.Data.CONTENT_URI, columns, where, selectionArgs, sortOrder);
                if (birthdayCur.getCount() > 0) {
                    while (birthdayCur.moveToNext()) {
                        Date date;
                        String birthday = birthdayCur.getString(birthdayCur.getColumnIndex(ContactsContract.CommonDataKinds.Event.START_DATE));
                        String name = birthdayCur.getString(birthdayCur.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
                        int id = birthdayCur.getInt(birthdayCur.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
                        String number = Contacts.getNumber(name, tContext);
                        String email = cc.getMail(id);
                        Calendar calendar = Calendar.getInstance();
                        for (SimpleDateFormat f : birthdayFormats) {
                            try {
                                date = f.parse(birthday);
                                if (date != null) {
                                    calendar.setTime(date);
                                    int day = calendar.get(Calendar.DAY_OF_MONTH);
                                    int month = calendar.get(Calendar.MONTH);
                                    if (!types.contains(id)) {
                                        i = i + 1;
                                        db.addBirthday(name, id, birthday, day, month, number,
                                                SyncHelper.generateID());
                                    }
                                }
                            } catch (Exception e) {

                            }
                        }
                    }
                }
                birthdayCur.close();
            }

            cur.close();
            return i;
        }

        @Override
        protected void onPostExecute(Integer files) {
            try {
                if ((pd != null) && pd.isShowing()) {
                    pd.dismiss();
                }
            } catch (final Exception e) {
                // Handle or log or ignore
            }
            if (files > 0){
                Toast.makeText(tContext,
                        tContext.getString(R.string.found_word) + " " + files + " " + tContext.getString(R.string.birthdays_word),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(tContext,
                        tContext.getString(R.string.nothing_found),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}
