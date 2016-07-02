/*
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

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.CallLog;

import java.util.ArrayList;
import java.util.List;

public class CallsAsync extends AsyncTask<Void, Void, Void> {

    private List<CallsData> mList;
    private Context mContext;
    private CallsLogListener mListener;

    public CallsAsync(Context context, CallsLogListener listener) {
        this.mContext = context;
        this.mListener = listener;
    }

    @Override
    protected Void doInBackground(Void... params) {
        Cursor c = mContext.getContentResolver().query(CallLog.Calls.CONTENT_URI, null, null, null, null);
        mList = new ArrayList<>();
        mList.clear();
        if (c != null) {
            int number = c.getColumnIndex(CallLog.Calls.NUMBER);
            int type = c.getColumnIndex(CallLog.Calls.TYPE);
            int date = c.getColumnIndex(CallLog.Calls.DATE);
            int nameIndex = c.getColumnIndex(CallLog.Calls.CACHED_NAME);
            while (c.moveToNext()) {
                String phoneNumber = c.getString(number);
                String callType = c.getString(type);
                String callDate = c.getString(date);
                String name = c.getString(nameIndex);
                int id = Contacts.getIdFromNumber(phoneNumber, mContext);
                String photo = null;
                if (id != 0) {
                    Uri uri = Contacts.getPhoto(id);
                    if (uri != null) {
                        photo = uri.toString();
                    }
                }

                CallsData data = new CallsData(name, phoneNumber, photo, Long.valueOf(callDate), id, Integer.parseInt(callType));
                int pos = getPosition(data.getDate());
                if (pos == -1) mList.add(data);
                else mList.add(pos, data);
            }
            c.close();
        }
        return null;
    }

    private int getPosition(long date) {
        if (mList.size() == 0) return 0;
        int position = -1;
        for (CallsData data : mList) {
            if (date > data.getDate()) {
                position = mList.indexOf(data);
                break;
            }
        }
        return position;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if (mListener != null) {
            mListener.onLoaded(mList);
        }
    }
}