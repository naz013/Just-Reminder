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

package com.cray.software.justreminder.services;

import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;

import com.cray.software.justreminder.databases.NextBase;

public class TaskButlerService extends IntentService {

    public TaskButlerService() {
        super("TaskButlerService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        AlarmReceiver alarm = new AlarmReceiver();
        NextBase db = new NextBase(getApplicationContext());
        db.open();
        Cursor c = db.getReminders();
        if (c != null && c.moveToFirst()){
            do {
                long rowId = c.getLong(c.getColumnIndex(NextBase._ID));
                int isDone = c.getInt(c.getColumnIndex(NextBase.DB_STATUS));
                if (isDone != 1) {
                    alarm.enableReminder(getApplicationContext(), rowId);
                }
            } while (c.moveToNext());
        }
        if (c != null) c.close();
        db.close();
        stopSelf();
    }
}