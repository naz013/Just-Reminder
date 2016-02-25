package com.cray.software.justreminder.datas;

import android.app.AlarmManager;
import android.content.Context;
import android.database.Cursor;

import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.helpers.SharedPrefs;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

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
public class Birthday {

    private static SimpleDateFormat birthFormat = new SimpleDateFormat("dd MM", Locale.getDefault());

    public static ArrayList<Long> getTodayBirthdays(Context context){
        ArrayList<Long> list = new ArrayList<>();
        SharedPrefs sharedPrefs = new SharedPrefs(context);
        int mDays = sharedPrefs.loadInt(Prefs.DAYS_TO_BIRTHDAY);
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        int mYear = cal.get(Calendar.YEAR);
        String mDate = birthFormat.format(cal.getTime());
        DataBase db = new DataBase(context);
        db.open();
        Cursor c = db.getBirthdays();
        if (c != null && c.moveToFirst()){
            do {
                long id = c.getLong(c.getColumnIndex(Constants.ContactConstants.COLUMN_ID));
                int month = c.getInt(c.getColumnIndex(Constants.ContactConstants.COLUMN_CONTACT_MONTH));
                int day = c.getInt(c.getColumnIndex(Constants.ContactConstants.COLUMN_CONTACT_DAY));
                String year = c.getString(c.getColumnIndex(Constants.ContactConstants.COLUMN_CONTACT_VAR));
                String birthValue = getBirthdayValue(month, day, mDays);
                if (year != null) {
                    if (birthValue.equals(mDate) && !year.matches(String.valueOf(mYear))) {
                        list.add(id);
                    }
                } else {
                    if (birthValue.equals(mDate)) {
                        list.add(id);
                    }
                }
            } while (c.moveToNext());
        }
        if (c != null) c.close();
        db.close();
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
