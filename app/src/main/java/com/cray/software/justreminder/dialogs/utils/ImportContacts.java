package com.cray.software.justreminder.dialogs.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Looper;
import android.provider.ContactsContract;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.Contacts;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.services.SetBirthdays;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class ImportContacts extends Activity implements View.OnClickListener {

    TextView importButton, noImportButton;
    SharedPrefs sharedPrefs;
    ColorSetter colorSetter;
    ProgressDialog pd;
    DataBase db;
    TextView dialog_content;
    Contacts cc;
    public final SimpleDateFormat[] birthdayFormats = {
            new SimpleDateFormat("yyyy-MM-dd"),
            new SimpleDateFormat("yyyyMMdd"),
            new SimpleDateFormat("yyyy.MM.dd"),
            new SimpleDateFormat("yy.MM.dd"),
            new SimpleDateFormat("yy/MM/dd"),
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        colorSetter = new ColorSetter(ImportContacts.this);
        setTheme(colorSetter.getDialogStyle());
        setContentView(R.layout.contact_import_layout);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        importButton = (TextView) findViewById(R.id.importButton);
        importButton.setOnClickListener(this);

        noImportButton = (TextView) findViewById(R.id.noImportButton);
        noImportButton.setOnClickListener(this);

        dialog_content = (TextView) findViewById(R.id.dialog_content);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.importButton:
                sharedPrefs = new SharedPrefs(ImportContacts.this);
                sharedPrefs.saveBoolean(Constants.APP_UI_PREFERENCES_USE_CONTACTS, true);
                sharedPrefs.saveBoolean(Constants.APP_UI_PREFERENCES_CONTACTS_IMPORT_DIALOG, true);
                pd = ProgressDialog.show(ImportContacts.this, null, getString(R.string.load_contats), true);
                pickContacts(pd);
                break;
            case R.id.noImportButton:
                sharedPrefs = new SharedPrefs(ImportContacts.this);
                sharedPrefs.saveBoolean(Constants.APP_UI_PREFERENCES_CONTACTS_IMPORT_DIALOG, true);
                finish();
                break;
        }
    }

    private void pickContacts(final ProgressDialog pd) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                ContentResolver cr = getContentResolver(); //getContnetResolver()
                db = new DataBase(ImportContacts.this);
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
                    cc = new Contacts(ImportContacts.this);
                    Cursor birthdayCur = cr.query(ContactsContract.Data.CONTENT_URI, columns, where, selectionArgs, sortOrder);
                    if (birthdayCur.getCount() > 0) {
                        while (birthdayCur.moveToNext()) {
                            // fix error;
                            Date date;
                            String birthday = birthdayCur.getString(birthdayCur.getColumnIndex(ContactsContract.CommonDataKinds.Event.START_DATE));
                            String name = birthdayCur.getString(birthdayCur.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
                            int id = birthdayCur.getInt(birthdayCur.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
                            String number = cc.get_Number(name, ImportContacts.this);
                            String email = cc.getMail(id);
                            Calendar calendar = Calendar.getInstance();
                            for (SimpleDateFormat f : birthdayFormats) {
                                try {
                                    date = f.parse(birthday);
                                    if (date != null) {
                                        calendar.setTime(date);
                                        int day = calendar.get(Calendar.DAY_OF_MONTH);
                                        int month = calendar.get(Calendar.MONTH);
                                        db.insertEvent(name, id, birthday, day, month, number, email);
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
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if ((pd != null) && pd.isShowing()) {
                                pd.dismiss();
                            }
                        } catch (final Exception e) {
                            // Handle or log or ignore
                        }
                        importButton.setVisibility(View.GONE);
                        noImportButton.setText(getString(R.string.button_close));
                        dialog_content.setText(getString(R.string.import_end_title));
                        startService(new Intent(ImportContacts.this, SetBirthdays.class));
                        finish();
                    }
                });
            }
        }).start();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        sharedPrefs = new SharedPrefs(ImportContacts.this);
        sharedPrefs.saveBoolean(Constants.APP_UI_PREFERENCES_CONTACTS_IMPORT_DIALOG, true);
        finish();
    }
}
