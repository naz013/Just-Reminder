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

package com.cray.software.justreminder.calls;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.modules.Module;

public class MissedCallAlarm extends WakefulBroadcastReceiver {

    private AlarmManager alarmMgr;

    @Override
    public void onReceive(Context context, Intent intent) {
        long id = intent.getLongExtra(Constants.ITEM_ID_INTENT, 0);
        String number = intent.getStringExtra("number");
        long time = intent.getLongExtra("time", 0);
        Intent service = new Intent(context, MissedCallAlarm.class);
        context.startService(service);
        Intent resultIntent = new Intent(context, MissedCallDialog.class);
        resultIntent.putExtra(Constants.ITEM_ID_INTENT, id);
        resultIntent.putExtra("number", number);
        resultIntent.putExtra("time", time);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(resultIntent);
    }

    public void setAlarm(Context context, long id, String number, long timeStamp) {
        Intent intent = new Intent(context, MissedCallAlarm.class);
        intent.putExtra("number", number);
        intent.putExtra("time", timeStamp);
        intent.putExtra(Constants.ITEM_ID_INTENT, id);
        int time = SharedPrefs.getInstance(context).getInt(Prefs.MISSED_CALL_TIME);
        long mills = 1000 * 60;
        Integer i = (int) (long) id;
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, i, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (Module.isMarshmallow()) alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + (time * mills), time * mills, alarmIntent);
        else alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + (time * mills), time * mills, alarmIntent);
    }

    public void cancelAlarm(Context context, long id) {
        Integer i = (int) (long) id;
        Intent intent = new Intent(context, MissedCallAlarm.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, i, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmMgr!= null) {
            alarmMgr.cancel(alarmIntent);
        }
    }
}