package com.cray.software.justreminder.reminder;

import android.content.Context;
import android.content.Intent;

import com.cray.software.justreminder.services.CheckPosition;
import com.cray.software.justreminder.services.GeolocationService;
import com.cray.software.justreminder.services.PositionDelayReceiver;

public class LocationType extends Type {

    private Context mContext;

    public LocationType(Context context, String type) {
        super(context);
        this.mContext = context;
        setType(type);
    }

    @Override
    public long save(Reminder item) {
        long id = super.save(item);
        startTracking(id, item);
        return id;
    }

    @Override
    public void save(long id, Reminder item) {
        super.save(id, item);
        startTracking(id, item);
    }

    private void startTracking(long id, Reminder item) {
        if (item.getDue() > 0) {
            new PositionDelayReceiver().setDelay(mContext, id);
        } else {
            mContext.startService(new Intent(mContext, GeolocationService.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            mContext.startService(new Intent(mContext, CheckPosition.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }
    }
}
