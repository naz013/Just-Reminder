package com.cray.software.justreminder.reminder;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.services.CheckPosition;
import com.cray.software.justreminder.services.GeolocationService;
import com.cray.software.justreminder.services.PositionDelayReceiver;

public class LocationType extends ReminderType {

    Context context;

    public LocationType(Context context, String type) {
        super(context);
        this.context = context;
        setType(type);
    }

    @Override
    public long save(DataItem item) {
        long id = super.save(item);
        startTracking(id, item);
        return id;
    }

    @Override
    public void save(long id, DataItem item) {
        super.save(id, item);
        startTracking(id, item);
    }

    private void startTracking(long id, DataItem item) {
        if (item.getDue() > 0) {
            new PositionDelayReceiver().setDelay(context, id);
        } else {
            context.startService(new Intent(context, GeolocationService.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            context.startService(new Intent(context, CheckPosition.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }
    }
}
