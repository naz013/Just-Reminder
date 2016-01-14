package com.cray.software.justreminder.async;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;

import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.databases.NextBase;
import com.cray.software.justreminder.helpers.TimeCount;
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
        NextBase db = new NextBase(mContext);
        db.open();
        Cursor c = db.queryGroup();
        if (c != null && c.moveToFirst()){
            boolean res = false;
            do {
                String tp = c.getString(c.getColumnIndex(NextBase.TYPE));
                if (tp.contains(Constants.TYPE_LOCATION)) {
                    long startTime = c.getLong(c.getColumnIndex(NextBase.START_TIME));
                    int isShown = c.getInt(c.getColumnIndex(NextBase.REMINDER_STATUS));
                    int isDone = c.getInt(c.getColumnIndex(NextBase.DB_STATUS));
                    if (startTime == 0) {
                        if (isDone != 1){
                            if (isShown != LocationUtil.SHOWN) res = true;
                        }
                    } else {
                        TimeCount tc = new TimeCount(mContext);
                        if (tc.isCurrent(startTime)) {
                            if (isDone != 1){
                                if (isShown != LocationUtil.SHOWN) res = true;
                            }
                        }
                    }
                }
            } while (c.moveToNext());
            if (!res) {
                mContext.stopService(new Intent(mContext, GeolocationService.class));
            }
        } else {
            mContext.stopService(new Intent(mContext, GeolocationService.class));
        }
        if (c != null) c.close();
        db.close();
        return null;
    }
}
