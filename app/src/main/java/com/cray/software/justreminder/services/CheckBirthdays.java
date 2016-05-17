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

import com.cray.software.justreminder.datas.Birthday;
import com.cray.software.justreminder.activities.ShowBirthday;

import java.util.ArrayList;

public class CheckBirthdays extends IntentService{

    public CheckBirthdays() {
        super("CheckBirthdaysAsync");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        ArrayList<Long> list = Birthday.getTodayBirthdays(getApplicationContext());
        if (list != null && list.size() > 0) {
            for (long id : list){
                Intent resultIntent = new Intent(getApplicationContext(), ShowBirthday.class);
                resultIntent.putExtra("id", id);
                resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                getApplicationContext().startActivity(resultIntent);
            }
        }
        stopSelf();
    }
}
