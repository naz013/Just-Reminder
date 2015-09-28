package com.cray.software.justreminder.reminder;

import android.content.Context;

import com.cray.software.justreminder.services.WeekDayReceiver;

public class WeekdayType extends Type {

    private Context mContext;

    public WeekdayType(Context context) {
        super(context);
        this.mContext = context;
    }

    @Override
    public long save(Reminder item) {
        long id = super.save(item);
        startAlarm(id);
        exportToServices(item, id);
        return id;
    }

    private void startAlarm(long id) {
        new WeekDayReceiver().setAlarm(mContext, id);
    }

    @Override
    public void save(long id, Reminder item) {
        super.save(id, item);
        startAlarm(id);
        exportToServices(item, id);
    }
}
