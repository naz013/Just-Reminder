package com.cray.software.justreminder.reminder;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

import com.cray.software.justreminder.json.JModel;
import com.cray.software.justreminder.services.GeolocationService;
import com.cray.software.justreminder.services.PositionDelayReceiver;
import com.cray.software.justreminder.utils.SuperUtil;

public class LocationType extends Type {

    private Context mContext;

    public LocationType(Context context, String type, Fragment fragment) {
        super(context);
        this.mContext = context;
        setType(type);
        inflateView(fragment);
    }

    public LocationType(Context context, String type) {
        super(context);
        this.mContext = context;
        setType(type);
    }

    @Override
    public long save(JModel item) {
        long id = super.save(item);
        startTracking(id, item);
        return id;
    }

    @Override
    public void save(long id, JModel item) {
        super.save(id, item);
        startTracking(id, item);
    }

    private void startTracking(long id, JModel item) {
        if (item.getEventTime() > 0) {
            new PositionDelayReceiver().setDelay(mContext, id);
        } else {
            if (!SuperUtil.isServiceRunning(mContext, GeolocationService.class)) {
                mContext.startService(new Intent(mContext, GeolocationService.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            }
        }
    }
}
