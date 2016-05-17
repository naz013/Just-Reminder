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

package com.cray.software.justreminder.contacts;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;

import com.cray.software.justreminder.R;

import java.util.ArrayList;
import java.util.List;

public class ContactsAsync extends AsyncTask<Void, Void, Void> {

    private ProgressDialog mDialog;
    private List<ContactData> mList;

    private Context mContext;
    private LoadListener mListener;

    public ContactsAsync(Context context, LoadListener listener) {
        this.mContext = context;
        this.mListener = listener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mDialog = ProgressDialog.show(mContext, null, mContext.getString(R.string.please_wait), true);
    }

    @Override
    protected Void doInBackground(Void... params) {
        Cursor cursor = mContext.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, ContactsContract.Contacts.DISPLAY_NAME + " ASC");
        mList = new ArrayList<>();
        mList.clear();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                String hasPhone = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup._ID));
                Uri uri = Contacts.getPhoto(id);
                String photo = null;
                if (uri != null) {
                    photo = uri.toString();
                }
                if (hasPhone.equalsIgnoreCase("1"))
                    hasPhone = "true";
                else
                    hasPhone = "false";
                if (name != null) {
                    if (Boolean.parseBoolean(hasPhone)) {
                        ContactData data = new ContactData(name, photo, id);
                        int pos = getPosition(name);
                        if (pos == -1) mList.add(data);
                        else mList.add(getPosition(name), data);
                    }
                }
            }
            cursor.close();
        }
        return null;
    }

    private int getPosition(String name) {
        if (mList.size() == 0) return 0;
        int position = -1;
        for (ContactData data : mList) {
            int comp = name.compareTo(data.getName());
            if (comp <= 0) {
                position = mList.indexOf(data);
                break;
            }
        }
        return position;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if (mDialog != null && mDialog.isShowing()) mDialog.dismiss();

        if (mListener != null) {
            mListener.onLoaded(mList);
        }
    }
}
