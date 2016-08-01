/**
 * Copyright 2015 Nazar Suhovich
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
package com.cray.software.justreminder.reminder;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.cray.software.justreminder.services.AlarmReceiver;

public class DateType extends Type {

    private static final String TAG = "DateType";
    private Context mContext;

    public DateType(Context context, String type, Fragment fragment) {
        super(context);
        this.mContext = context;
        setType(type);
        inflateView(fragment);
    }

    public DateType(Context context, String type) {
        super(context);
        this.mContext = context;
        setType(type);
    }

    @Override
    public long save(ReminderItem item) {
        long id = super.save(item);
        startAlarm(id);
        exportToServices(item, id);
        return id;
    }

    private void startAlarm(long id) {
        new AlarmReceiver().enableReminder(mContext, id);
    }
}
