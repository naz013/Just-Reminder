package com.cray.software.justreminder.async;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.widget.Toast;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.helpers.Contacts;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.constants.Constants;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class CheckBirthdaysAsync extends AsyncTask<Void, Void, Integer> {

    private Context mContext;
    private final SimpleDateFormat[] birthdayFormats = {
            new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()),
            new SimpleDateFormat("yyyyMMdd", Locale.getDefault()),
            new SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()),
            new SimpleDateFormat("yy.MM.dd", Locale.getDefault()),
            new SimpleDateFormat("yy/MM/dd", Locale.getDefault()),
    };

    private boolean showDialog = false;
    private ProgressDialog pd;

    public CheckBirthdaysAsync(Context context){
        this.mContext = context;
    }

    public CheckBirthdaysAsync(Context context, boolean showDialog){
        this.mContext = context;
        this.showDialog = showDialog;
        if (showDialog){
            pd = new ProgressDialog(context);
            pd.setCancelable(true);
            pd.setMessage(context.getString(R.string.checking_new_birthdays_title));
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (showDialog) pd.show();
    }

    @Override
    protected Integer doInBackground(Void... params) {
        ContentResolver cr = mContext.getContentResolver();
        DataBase db = new DataBase(mContext);
        db.open();
        int i = 0;
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
            Contacts mContacts = new Contacts(mContext);
            Cursor cursor = db.getBirthdays();
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
                    String number = Contacts.getNumber(name, mContext);
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
                                    i = i + 1;
                                    db.addBirthday(name, id, birthday, day, month, number,
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
        db.close();
        return i;
    }

    @Override
    protected void onPostExecute(Integer files) {
        if (showDialog) {
            try {
                if ((pd != null) && pd.isShowing()) {
                    pd.dismiss();
                }
            } catch (final Exception e) {
                // Handle or log or ignore
            }
            if (files > 0) {
                Toast.makeText(mContext,
                        mContext.getString(R.string.found_word) + " " + files + " " + mContext.getString(R.string.birthdays_word),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(mContext,
                        mContext.getString(R.string.nothing_found),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}
