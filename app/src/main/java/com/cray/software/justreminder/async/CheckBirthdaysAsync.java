package com.cray.software.justreminder.async;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.ContactsContract;

import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.helpers.Contacts;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.interfaces.Constants;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class CheckBirthdaysAsync extends AsyncTask<Void, Void, Void> {

    Context mContext;
    DataBase db;
    Contacts mContacts;
    public final SimpleDateFormat[] birthdayFormats = {
            new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()),
            new SimpleDateFormat("yyyyMMdd", Locale.getDefault()),
            new SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()),
            new SimpleDateFormat("yy.MM.dd", Locale.getDefault()),
            new SimpleDateFormat("yy/MM/dd", Locale.getDefault()),
    };

    public CheckBirthdaysAsync(Context context){
        this.mContext = context;
    }

    @Override
    protected Void doInBackground(Void... params) {
        ContentResolver cr = mContext.getContentResolver();
        db = new DataBase(mContext);
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
            mContacts = new Contacts(mContext);
            Cursor cursor = db.getEvents();
            ArrayList<Integer> types = new ArrayList<>();
            if (cursor != null && cursor.moveToFirst()){
                do{
                    int tp = cursor.getInt(cursor.getColumnIndex(Constants.ContactConstants.COLUMN_CONTACT_ID));
                    types.add(tp);
                } while (cursor.moveToNext());
            }
            Cursor birthdayCur = cr.query(ContactsContract.Data.CONTENT_URI, columns, where, selectionArgs, sortOrder);
            if (birthdayCur != null && birthdayCur.getCount() > 0) {
                while (birthdayCur.moveToNext()) {
                    Date date;
                    String birthday = birthdayCur.getString(birthdayCur.getColumnIndex(ContactsContract.CommonDataKinds.Event.START_DATE));
                    String name = birthdayCur.getString(birthdayCur.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
                    int id = birthdayCur.getInt(birthdayCur.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
                    String number = Contacts.get_Number(name, mContext);
                    String email = mContacts.getMail(id);
                    Calendar calendar = Calendar.getInstance();
                    for (SimpleDateFormat f : birthdayFormats) {
                        try {
                            date = f.parse(birthday);
                            if (date != null) {
                                calendar.setTime(date);
                                int day = calendar.get(Calendar.DAY_OF_MONTH);
                                int month = calendar.get(Calendar.MONTH);
                                if (!types.contains(id)) {
                                    db.insertEvent(name, id, birthday, day, month, number,
                                            SyncHelper.generateID());
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            if (birthdayCur != null) birthdayCur.close();
        }

        cur.close();
        return null;
    }
}
