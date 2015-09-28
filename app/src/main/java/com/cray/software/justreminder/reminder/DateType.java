package com.cray.software.justreminder.reminder;

import android.content.Context;

import com.cray.software.justreminder.services.AlarmReceiver;

public class DateType extends Type {

    private Context mContext;

    public DateType(Context context, String type) {
        super(context);
        this.mContext = context;
        setType(type);
    }

    @Override
    public long save(Reminder item) {
        long id = super.save(item);
        startAlarm(id);
        exportToServices(item, id);
        return id;
    }

    @Override
    public void save(long id, Reminder item) {
        super.save(id, item);
        startAlarm(id);
        exportToServices(item, id);
    }

    private void startAlarm(long id) {
        new AlarmReceiver().setAlarm(mContext, id);
    }
}
