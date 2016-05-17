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

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.cray.software.justreminder.databases.NextBase;
import com.cray.software.justreminder.modules.Module;
import com.cray.software.justreminder.utils.SuperUtil;

public class PositionDelayReceiver extends WakefulBroadcastReceiver {

    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!SuperUtil.isServiceRunning(context, GeolocationService.class)) {
            context.startService(new Intent(context, GeolocationService.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }
    }

    public void setDelay(Context context, long id) {
        NextBase db = new NextBase(context);
        db.open();
        Cursor c = db.getReminder(id);

        Integer i = (int) (long) id;
        long startTime = 0;
        if (c != null && c.moveToNext()) {
            startTime = c.getLong(c.getColumnIndex(NextBase.EVENT_TIME));
        }
        if (c != null) c.close();
        db.close();

        Intent intent = new Intent(context, PositionDelayReceiver.class);
        alarmIntent = PendingIntent.getBroadcast(context, i, intent, 0);
        alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (Module.isMarshmallow()) alarmMgr.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, startTime, alarmIntent);
        else alarmMgr.set(AlarmManager.RTC_WAKEUP, startTime, alarmIntent);
    }

    public void cancelDelay(Context context, long id) {
        Integer i = (int) (long) id;
        Intent intent = new Intent(context, PositionDelayReceiver.class);
        alarmIntent = PendingIntent.getBroadcast(context, i, intent, 0);
        alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmMgr!= null) {
            alarmMgr.cancel(alarmIntent);
        }
    }
}