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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.cray.software.justreminder.async.DisableAsync;
import com.cray.software.justreminder.helpers.Notifier;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.constants.Prefs;

public class JustBootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, TaskButlerService.class));
        if (SharedPrefs.getInstance(context).getBoolean(Prefs.BIRTHDAY_REMINDER)){
            new BirthdayAlarm().setAlarm(context);
        }
        if (SharedPrefs.getInstance(context).getBoolean(Prefs.STATUS_BAR_NOTIFICATION)){
            new Notifier(context).showPermanent();
        }
        if (SharedPrefs.getInstance(context).getBoolean(Prefs.AUTO_CHECK_BIRTHDAYS)){
            new BirthdayCheckAlarm().setAlarm(context);
        }
        if (SharedPrefs.getInstance(context).getBoolean(Prefs.AUTO_CHECK_FOR_EVENTS)){
            new EventsCheckAlarm().setAlarm(context);
        }
        if (SharedPrefs.getInstance(context).getBoolean(Prefs.AUTO_BACKUP)){
            new AutoSyncAlarm().setAlarm(context);
        }
        if (SharedPrefs.getInstance(context).getBoolean(Prefs.BIRTHDAY_PERMANENT)){
            new BirthdayPermanentAlarm().setAlarm(context);
            new Notifier(context).showBirthdayPermanent();
        }
        if (SharedPrefs.getInstance(context).getBoolean(Prefs.WEAR_SERVICE)) {
            context.startService(new Intent(context, WearService.class));
        }

        new DisableAsync(context).execute();
    }
}
