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

package com.cray.software.justreminder.reminder;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.helpers.TimeCount;
import com.cray.software.justreminder.services.GeolocationService;
import com.cray.software.justreminder.utils.LocationUtil;

import java.util.List;

public class DisableAsync extends AsyncTask<Void, Void, Void> {

    private Context mContext;

    public DisableAsync(Context context){
        this.mContext = context;
    }

    @Override
    protected Void doInBackground(Void... params) {
        boolean res = false;
        List<ReminderItem> list = ReminderHelper.getInstance(mContext).getRemindersActive();
        if (list.size() == 0) {
            mContext.stopService(new Intent(mContext, GeolocationService.class));
        }
        for (ReminderItem item : list) {
            if (item.getType().contains(Constants.TYPE_LOCATION)) {
                long startTime = item.getDateTime();
                int isShown = item.getLocation();
                int isDone = item.getStatus();
                if (startTime == 0) {
                    if (isDone != 1){
                        if (isShown != LocationUtil.SHOWN) res = true;
                    }
                } else {
                    TimeCount tc = TimeCount.getInstance(mContext);
                    if (tc.isCurrent(startTime)) {
                        if (isDone != 1){
                            if (isShown != LocationUtil.SHOWN) res = true;
                        }
                    }
                }
            }
        }
        if (!res) {
            mContext.stopService(new Intent(mContext, GeolocationService.class));
        }
        return null;
    }
}
