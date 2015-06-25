package com.cray.software.justreminder.async;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;

import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.helpers.TimeCount;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.services.CheckPosition;
import com.cray.software.justreminder.services.GeolocationService;

public class DisableAsync extends AsyncTask<Void, Void, Void> {

    Context mContext;
    DataBase mData;

    public DisableAsync(Context context){
        this.mContext = context;
    }

    @Override
    protected Void doInBackground(Void... params) {
        mData = new DataBase(mContext);
        mData.open();
        if (mData.getCount() != 0) {
            Cursor c = mData.queryGroup();
            if (!getInt(c)){
                mContext.stopService(new Intent(mContext, GeolocationService.class));
                mContext.stopService(new Intent(mContext, CheckPosition.class));
            }
        } else {
            mContext.stopService(new Intent(mContext, GeolocationService.class));
            mContext.stopService(new Intent(mContext, CheckPosition.class));
        }
        return null;
    }

    private boolean getInt(Cursor c){
        boolean x = false;
        TimeCount tc = new TimeCount(mContext);
        if (c != null && c.moveToFirst()) {
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
                        if (isShown != 1 && isDone != 1) {
                            x = true;
                        }
                    } else {
                        if (tc.isCurrent(year, month, day, hour, minute, 0)) {
                            if (isShown != 1 && isDone != 1) {
                                x = true;
                            }
                        }
                    }
                }
            } while (c.moveToNext());
        }
        if (c != null) c.close();
        return x;
    }
}
