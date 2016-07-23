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

package com.cray.software.justreminder.birthdays;

import android.app.AlarmManager;
import android.content.Context;

import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.helpers.SharedPrefs;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class Birthday {

    private static SimpleDateFormat birthFormat = new SimpleDateFormat("dd MM", Locale.getDefault());

    public static ArrayList<Long> getTodayBirthdays(Context context){
        ArrayList<Long> list = new ArrayList<>();
        int mDays = SharedPrefs.getInstance(context).getInt(Prefs.DAYS_TO_BIRTHDAY);
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        int mYear = cal.get(Calendar.YEAR);
        String mDate = birthFormat.format(cal.getTime());
        List<BirthdayItem> birthdayItemList = BirthdayHelper.getInstance(context).getAll();
        for (BirthdayItem item : birthdayItemList) {
            String year = item.getShowedYear();
            String birthValue = getBirthdayValue(item.getMonth(), item.getDay(), mDays);
            if (year != null) {
                if (birthValue.equals(mDate) && !year.matches(String.valueOf(mYear))) {
                    list.add(item.getId());
                }
            } else {
                if (birthValue.equals(mDate)) {
                    list.add(item.getId());
                }
            }
        }
        return list;
    }

    public static String getBirthdayValue(int month, int day, int daysBefore) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.setTimeInMillis(calendar.getTimeInMillis() - (AlarmManager.INTERVAL_DAY * daysBefore));
        return birthFormat.format(calendar.getTime());
    }
}
