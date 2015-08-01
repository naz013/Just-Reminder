package com.cray.software.justreminder.reminder;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.services.AlarmReceiver;
import com.cray.software.justreminder.services.CheckPosition;
import com.cray.software.justreminder.services.GeolocationService;
import com.cray.software.justreminder.services.PositionDelayReceiver;

public class LocationType extends ReminderType {

    Context context;
    SharedPrefs sPrefs;

    public LocationType(Context context) {
        super(context);
        this.context = context;
        sPrefs = new SharedPrefs(context);
    }

    public View inflateView(ViewGroup container, LayoutInflater inflater, String type) {
        int layout = 0;
        if (type.startsWith(Constants.TYPE_LOCATION_OUT)) layout = R.layout.location_out_reminder_layout;
        else layout = R.layout.location_reminder_layout;
        return inflateView(layout, container, inflater);
    }

    @Override
    public long save(DataItem item) {
        long id = super.save(item);
        if (item.getDue() > 0) {
            new PositionDelayReceiver().setDelay(context, id);
        } else {
            context.startService(new Intent(context, GeolocationService.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            context.startService(new Intent(context, CheckPosition.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }
        return id;
    }

    @Override
    public void save(long id, DataItem item) {
        super.save(id, item);
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
