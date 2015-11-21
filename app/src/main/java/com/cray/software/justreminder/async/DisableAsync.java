package com.cray.software.justreminder.async;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;

import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.helpers.TimeCount;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.services.CheckPosition;
import com.cray.software.justreminder.services.GeolocationService;
import com.cray.software.justreminder.utils.LocationUtil;

public class DisableAsync extends AsyncTask<Void, Void, Void> {

    private Context mContext;

    public DisableAsync(Context context){
        this.mContext = context;
    }

    @Override
    protected Void doInBackground(Void... params) {
        DataBase db = new DataBase(mContext);
        db.open();
        Cursor c = db.queryGroup();
        if (c != null && c.moveToFirst()){
            boolean res = false;
            do {
                String tp = c.getString(c.getColumnIndex(Constants.COLUMN_TYPE));
                if (tp.startsWith(Constants.TYPE_LOCATION) || tp.startsWith(Constants.TYPE_LOCATION_OUT)) {
                    int year = c.getInt(c.getColumnIndex(Constants.COLUMN_YEAR));
                    int month = c.getInt(c.getColumnIndex(Constants.COLUMN_MONTH));
                    int day = c.getInt(c.getColumnIndex(Constants.COLUMN_DAY));
                    int hour = c.getInt(c.getColumnIndex(Constants.COLUMN_HOUR));
                    int minute = c.getInt(c.getColumnIndex(Constants.COLUMN_MINUTE));
                    int isShown = c.getInt(c.getColumnIndex(Constants.COLUMN_REMINDERS_COUNT));
                    int isDone = c.getInt(c.getColumnIndex(Constants.COLUMN_IS_DONE));
                    if (year == 0 && month == 0 && day == 0 && hour == 0 && minute == 0) {
                        if (isDone != 1){
                            if (isShown != LocationUtil.SHOWN) res = true;
                        }
                    } else {
                        TimeCount tc = new TimeCount(mContext);
                        if (tc.isCurrent(year, month, day, hour, minute, 0)) {
                            if (isDone != 1){
                                if (isShown != LocationUtil.SHOWN) res = true;
                            }
                        }
                    }
                }
            } while (c.moveToNext());
            if (!res) {
                mContext.stopService(new Intent(mContext, GeolocationService.class));
                mContext.stopService(new Intent(mContext, CheckPosition.class));
            } else {
                mContext.startService(new Intent(mContext, GeolocationService.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            }
        } else {
            mContext.stopService(new Intent(mContext, GeolocationService.class));
            mContext.stopService(new Intent(mContext, CheckPosition.class));
        }
        if (c != null) c.close();
        db.close();
        return null;
    }
}
