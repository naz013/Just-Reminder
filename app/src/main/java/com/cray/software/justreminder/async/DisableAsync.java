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

package com.cray.software.justreminder.async;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;

import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.databases.NextBase;
import com.cray.software.justreminder.helpers.TimeCount;
import com.cray.software.justreminder.services.GeolocationService;
import com.cray.software.justreminder.utils.LocationUtil;

public class DisableAsync extends AsyncTask<Void, Void, Void> {

    private Context mContext;

    public DisableAsync(Context context){
        this.mContext = context;
    }

    @Override
    protected Void doInBackground(Void... params) {
        NextBase db = new NextBase(mContext);
        db.open();
        Cursor c = db.getReminders();
        if (c != null && c.moveToFirst()){
            boolean res = false;
            do {
                String tp = c.getString(c.getColumnIndex(NextBase.TYPE));
                if (tp.contains(Constants.TYPE_LOCATION)) {
                    long startTime = c.getLong(c.getColumnIndex(NextBase.EVENT_TIME));
                    int isShown = c.getInt(c.getColumnIndex(NextBase.REMINDER_STATUS));
                    int isDone = c.getInt(c.getColumnIndex(NextBase.DB_STATUS));
                    if (startTime == 0) {
                        if (isDone != 1){
                            if (isShown != LocationUtil.SHOWN) res = true;
                        }
                    } else {
                        TimeCount tc = new TimeCount(mContext);
                        if (tc.isCurrent(startTime)) {
                            if (isDone != 1){
                                if (isShown != LocationUtil.SHOWN) res = true;
                            }
                        }
                    }
                }
            } while (c.moveToNext());
            if (!res) {
                mContext.stopService(new Intent(mContext, GeolocationService.class));
            }
        } else {
            mContext.stopService(new Intent(mContext, GeolocationService.class));
        }
        if (c != null) c.close();
        db.close();
        return null;
    }
}
