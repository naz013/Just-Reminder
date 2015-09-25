package com.cray.software.justreminder.reminder;

import android.content.Context;

import com.cray.software.justreminder.services.WeekDayReceiver;

public class WeekdayType extends ReminderType {

    Context context;

    public WeekdayType(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    public long save(DataItem item) {
        long id = super.save(item);
        startAlarm(id);
        exportToServices(item, id);
        return id;
    }

    private void startAlarm(long id) {
        new WeekDayReceiver().setAlarm(context, id);
    }

    @Override
    public void save(long id, DataItem item) {
        super.save(id, item);
        startAlarm(id);
        exportToServices(item, id);
    }
}
