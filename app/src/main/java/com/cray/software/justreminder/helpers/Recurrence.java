/*
 * Copyright 2015 Nazar Suhovich
 *  <p/>
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  <p/>
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  <p/>
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.cray.software.justreminder.helpers;

import com.cray.software.justreminder.reminder.json.JExclusion;
import com.cray.software.justreminder.utils.TimeUtil;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class Recurrence {

    private JExclusion recurrence;

    public Recurrence(String jsonObject){
        recurrence = new JExclusion(jsonObject);
    }

    /**
     * Check if current event time is out of disabled range.
     * @return boolean
     */
    public boolean isRange(){
        boolean res = false;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        int mHour = calendar.get(Calendar.HOUR_OF_DAY);
        List<Integer> list = recurrence.getHours();
        if (list != null){
            return list.contains(mHour);
        }
        long eventTime = calendar.getTimeInMillis();
        String fromHour = recurrence.getFromHour();
        String toHour = recurrence.getToHour();
        if (fromHour != null && toHour != null){
            Date fromDate = TimeUtil.getDate(fromHour);
            Date toDate = TimeUtil.getDate(toHour);
            if (fromDate != null && toDate != null){
                calendar.setTime(fromDate);
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);
                calendar.setTimeInMillis(System.currentTimeMillis());
                calendar.set(Calendar.HOUR_OF_DAY, hour);
                calendar.set(Calendar.MINUTE, minute);
                long start = calendar.getTimeInMillis();
                calendar.setTime(toDate);
                hour = calendar.get(Calendar.HOUR_OF_DAY);
                minute = calendar.get(Calendar.MINUTE);
                calendar.setTimeInMillis(System.currentTimeMillis());
                calendar.set(Calendar.HOUR_OF_DAY, hour);
                calendar.set(Calendar.MINUTE, minute);
                long end = calendar.getTimeInMillis();
                if (start > end) {
                    res = eventTime >= start || eventTime < end;
                } else {
                    res = eventTime >= start && eventTime <= end;
                }
            }
        }
        return res;
    }
}
