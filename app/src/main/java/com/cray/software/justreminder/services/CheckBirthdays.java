package com.cray.software.justreminder.services;

import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;
import android.os.Looper;

import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.dialogs.ShowBirthday;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.interfaces.Prefs;

import java.util.Calendar;

public class CheckBirthdays extends IntentService{

    private static final int minuteMills = 1000 * 60;
    private static final int hourMills = minuteMills * 60;
    private static final int dayMills = hourMills * 24;

    public CheckBirthdays() {
        super("CheckBirthdaysAsync");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                SharedPrefs sharedPrefs = new SharedPrefs(getApplicationContext());
                int mDays = sharedPrefs.loadInt(Prefs.DAYS_TO_BIRTHDAY);
                int mHour = sharedPrefs.loadInt(Prefs.BIRTHDAY_REMINDER_HOUR);
                int mMinute= sharedPrefs.loadInt(Prefs.BIRTHDAY_REMINDER_MINUTE);
                long currentTime = getCurrentDate(mDays, mHour, mMinute);
                DataBase db = new DataBase(getApplicationContext());
                db.open();
                Cursor c = db.getBirthdays();
                if (c != null && c.moveToFirst()){
                    do {
                        Calendar cal = Calendar.getInstance();
                        cal.getTimeInMillis();
                        int mYear = cal.get(Calendar.YEAR);
                        long id = c.getLong(c.getColumnIndex(Constants.ContactConstants.COLUMN_ID));
                        int month = c.getInt(c.getColumnIndex(Constants.ContactConstants.COLUMN_CONTACT_MONTH));
                        int day = c.getInt(c.getColumnIndex(Constants.ContactConstants.COLUMN_CONTACT_DAY));
                        String year = c.getString(c.getColumnIndex(Constants.ContactConstants.COLUMN_CONTACT_VAR));
                        long birthValue = getBirthdayValue(month, day, mHour, mMinute, mDays);
                        if (year != null) {
                            if (birthValue == currentTime && (!year.matches(String.valueOf(mYear)))) {
                                Intent resultIntent = new Intent(getApplicationContext(), ShowBirthday.class);
                                resultIntent.putExtra("id", id);
                                resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                                getApplicationContext().startActivity(resultIntent);
                            }
                        } else {
                            if (birthValue == currentTime) {
                                Intent resultIntent = new Intent(getApplicationContext(), ShowBirthday.class);
                                resultIntent.putExtra("id", id);
                                resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                                getApplicationContext().startActivity(resultIntent);
                            }
                        }
                        stopSelf();
                    } while (c.moveToNext());
                } stopSelf();
                if (c != null) c.close();
            }
        }).start();
    }

    private long getCurrentDate(int plusDays, int hour, int minute){
        long time;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        time = calendar.getTimeInMillis() + (plusDays * dayMills);
        return time;
    }

    private long getBirthdayValue(int month, int day, int hour, int minute, int daysBefore) {
        long time;
        Calendar cal = Calendar.getInstance();
        cal.getTimeInMillis();
        int year = cal.get(Calendar.YEAR);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        if (month == 0) {
            if (daysBefore == 1) {
                if (day == 1){
                    calendar.set(Calendar.YEAR, year + 1);
                }
            } else if (daysBefore == 2){
                if (day < 3){
                    calendar.set(Calendar.YEAR, year + 1);
                }
            } else if (daysBefore == 3){
                if (day < 4){
                    calendar.set(Calendar.YEAR, year + 1);
                }
            } else if (daysBefore == 4){
                if (day < 5){
                    calendar.set(Calendar.YEAR, year + 1);
                }
            } else if (daysBefore == 5){
                if (day < 6){
                    calendar.set(Calendar.YEAR, year + 1);
                }
            }
        }
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        time = calendar.getTimeInMillis();
        return time;
    }
}
