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
