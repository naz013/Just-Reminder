package com.cray.software.justreminder.fragments;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
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
import com.cray.software.justreminder.dialogs.utils.DaysTo;
import com.cray.software.justreminder.helpers.Contacts;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.modules.ManageModule;
import com.cray.software.justreminder.services.BirthdayAlarm;
import com.cray.software.justreminder.services.BirthdayCheckAlarm;
import com.cray.software.justreminder.services.SetBirthdays;
import com.cray.software.justreminder.widgets.UpdatesHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class BirthdaysSettingsFragment extends Fragment implements View.OnClickListener {

    ActionBar ab;
    SharedPrefs sPrefs;
    TextView daysToText, contactsScan, reminderTimeText, text, text1, text2, text3, text4,
            birthdayNotification, text6, text7;
    RelativeLayout useContacts, daysTo, reminderTime, autoScan, birthdayNotifContainer, widgetShow;
    SwitchCompat contactsSwitch;
    CheckBox autoScanCheck, widgetShowCheck;
    DataBase db;
    BirthdayAlarm alarmReceiver = new BirthdayAlarm();
    int myHour = 0, myMinute = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView =  inflater.inflate(R.layout.birthday_settings_layout, container, false);

        ab = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if (ab != null){
            ab.setTitle(R.string.birthday_settings);
        }

        sPrefs = new SharedPrefs(getActivity().getApplicationContext());

        useContacts = (RelativeLayout) rootView.findViewById(R.id.useContacts);
        useContacts.setOnClickListener(this);

        contactsSwitch = (SwitchCompat) rootView.findViewById(R.id.contactsSwitch);
        contactsSwitch.setChecked(sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_USE_CONTACTS));

        daysTo = (RelativeLayout) rootView.findViewById(R.id.daysTo);
        daysTo.setOnClickListener(this);

        daysToText = (TextView) rootView.findViewById(R.id.daysToText);

        reminderTime = (RelativeLayout) rootView.findViewById(R.id.reminderTime);
        reminderTime.setOnClickListener(this);

        reminderTimeText = (TextView) rootView.findViewById(R.id.reminderTimeText);

        contactsScan = (TextView) rootView.findViewById(R.id.contactsScan);
        contactsScan.setOnClickListener(this);

        autoScan = (RelativeLayout) rootView.findViewById(R.id.autoScan);
        autoScan.setOnClickListener(this);

        autoScanCheck = (CheckBox) rootView.findViewById(R.id.autoScanCheck);
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        autoScanCheck.setChecked(sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_AUTO_CHECK_BIRTHDAYS));

        text = (TextView) rootView.findViewById(R.id.text);
        text1 = (TextView) rootView.findViewById(R.id.text1);
        text2 = (TextView) rootView.findViewById(R.id.text2);
        text3 = (TextView) rootView.findViewById(R.id.text3);
        text4 = (TextView) rootView.findViewById(R.id.text4);
        text6 = (TextView) rootView.findViewById(R.id.text6);
        text7 = (TextView) rootView.findViewById(R.id.text7);

        if (new ManageModule().isPro()){
            birthdayNotifContainer = (RelativeLayout) rootView.findViewById(R.id.birthdayNotifContainer);
            birthdayNotifContainer.setVisibility(View.VISIBLE);

            birthdayNotification = (TextView) rootView.findViewById(R.id.birthdayNotification);
            birthdayNotification.setOnClickListener(this);
        }

        widgetShow = (RelativeLayout) rootView.findViewById(R.id.widgetShow);
        widgetShow.setOnClickListener(this);

        widgetShowCheck = (CheckBox) rootView.findViewById(R.id.widgetShowCheck);
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        widgetShowCheck.setChecked(sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_WIDGET_BIRTHDAYS));

        checkEnabling();

        return rootView;
    }

    private void showDays(){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        int days;
        if (sPrefs.isString(Constants.APP_UI_PREFERENCES_DAYS_TO_BIRTHDAY)) {
            days = sPrefs.loadInt(Constants.APP_UI_PREFERENCES_DAYS_TO_BIRTHDAY);
        } else days = 0;
        daysToText.setText(String.valueOf(days));
    }

    private void showTime(){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        if (sPrefs.isString(Constants.APP_UI_PREFERENCES_BIRTHDAY_REMINDER_HOUR)
                && sPrefs.isString(Constants.APP_UI_PREFERENCES_BIRTHDAY_REMINDER_MINUTE)){
            myHour = sPrefs.loadInt(Constants.APP_UI_PREFERENCES_BIRTHDAY_REMINDER_HOUR);
            myMinute = sPrefs.loadInt(Constants.APP_UI_PREFERENCES_BIRTHDAY_REMINDER_MINUTE);
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, myHour);
            calendar.set(Calendar.MINUTE, myMinute);
            if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_IS_24_TIME_FORMAT)){
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                String formattedTime = sdf.format(calendar.getTime());
                reminderTimeText.setText(formattedTime);
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("K:mm a");
                String formattedTime = sdf.format(calendar.getTime());
                reminderTimeText.setText(formattedTime);
            }
        }
    }

    private void checkEnabling(){
        if (contactsSwitch.isChecked()){
            daysTo.setEnabled(true);
            contactsScan.setEnabled(true);
            reminderTime.setEnabled(true);
            autoScan.setEnabled(true);
            autoScanCheck.setEnabled(true);
            widgetShow.setEnabled(true);
            widgetShowCheck.setEnabled(true);
            text.setEnabled(true);
            text1.setEnabled(true);
            text2.setEnabled(true);
            text3.setEnabled(true);
            text4.setEnabled(true);
            text6.setEnabled(true);
            text7.setEnabled(true);
            daysToText.setEnabled(true);
            reminderTimeText.setEnabled(true);
            if (new ManageModule().isPro()) {
                birthdayNotifContainer.setEnabled(true);
                birthdayNotification.setEnabled(true);
            }
        } else {
            daysTo.setEnabled(false);
            contactsScan.setEnabled(false);
            reminderTime.setEnabled(false);
            autoScan.setEnabled(false);
            autoScanCheck.setEnabled(false);
            widgetShow.setEnabled(false);
            widgetShowCheck.setEnabled(false);
            text.setEnabled(false);
            text1.setEnabled(false);
            text2.setEnabled(false);
            text3.setEnabled(false);
            text4.setEnabled(false);
            text6.setEnabled(false);
            text7.setEnabled(false);
            daysToText.setEnabled(false);
            reminderTimeText.setEnabled(false);
            if (new ManageModule().isPro()) {
                birthdayNotifContainer.setEnabled(false);
                birthdayNotification.setEnabled(false);
            }
        }
    }

    private void autoScanCheck (){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        BirthdayCheckAlarm alarm = new BirthdayCheckAlarm();
        if (autoScanCheck.isChecked()){
            sPrefs.saveBoolean(Constants.APP_UI_PREFERENCES_AUTO_CHECK_BIRTHDAYS, false);
            autoScanCheck.setChecked(false);
            alarm.cancelAlarm(getActivity());
        } else {
            sPrefs.saveBoolean(Constants.APP_UI_PREFERENCES_AUTO_CHECK_BIRTHDAYS, true);
            autoScanCheck.setChecked(true);
            alarm.setAlarm(getActivity());
        }
    }

    private void widgetCheck (){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        if (widgetShowCheck.isChecked()){
            sPrefs.saveBoolean(Constants.APP_UI_PREFERENCES_WIDGET_BIRTHDAYS, false);
            widgetShowCheck.setChecked(false);
            UpdatesHelper helper = new UpdatesHelper(getActivity());
            helper.updateWidget();
        } else {
            sPrefs.saveBoolean(Constants.APP_UI_PREFERENCES_WIDGET_BIRTHDAYS, true);
            widgetShowCheck.setChecked(true);
            UpdatesHelper helper = new UpdatesHelper(getActivity());
            helper.updateWidget();
        }
    }

    private void setContactsSwitch (){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        if (contactsSwitch.isChecked()){
            sPrefs.saveBoolean(Constants.APP_UI_PREFERENCES_USE_CONTACTS, false);
            sPrefs.saveBoolean(Constants.APP_UI_PREFERENCES_WIDGET_BIRTHDAYS, false);
            contactsSwitch.setChecked(false);
            widgetShowCheck.setChecked(false);
            checkEnabling();
            cleanBirthdays();
        } else {
            sPrefs.saveBoolean(Constants.APP_UI_PREFERENCES_USE_CONTACTS, true);
            contactsSwitch.setChecked(true);
            checkEnabling();
            getActivity().startService(new Intent(getActivity(), SetBirthdays.class));
        }
    }

    private void cleanBirthdays(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                db = new DataBase(getActivity());
                db.open();
                if (db.getCountEvents() > 0){
                    Cursor c = db.queryEvents();
                    if (c != null && c.moveToFirst()){
                        do {
                            long id = c.getLong(c.getColumnIndex(Constants.ContactConstants.COLUMN_ID));
                            db.deleteEvent(id);
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
            case R.id.widgetShow:
                widgetCheck();
                break;
            case R.id.contactsScan:
                new checkBirthdays(getActivity()).execute();
                break;
            case R.id.daysTo:
                getActivity().getApplicationContext()
                        .startActivity(new Intent(getActivity().getApplicationContext(), DaysTo.class)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
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

    class checkBirthdays extends AsyncTask<Void, Void, Integer>{

        ProgressDialog pd;
        Context tContext;
        DataBase db;
        Contacts cc;
        //SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        public final SimpleDateFormat[] birthdayFormats = {
                new SimpleDateFormat("yyyy-MM-dd"),
                new SimpleDateFormat("yyyyMMdd"),
                new SimpleDateFormat("yyyy.MM.dd"),
                new SimpleDateFormat("yy.MM.dd"),
                new SimpleDateFormat("yy/MM/dd"),
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
                Cursor cursor = db.queryEvents();
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
                        String number = cc.get_Number(name, tContext);
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
                                        db.insertEvent(name, id, birthday, day, month, number, email);
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
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
