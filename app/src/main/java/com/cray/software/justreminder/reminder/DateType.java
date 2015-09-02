package com.cray.software.justreminder.reminder;

import android.content.Context;
import android.util.Log;

import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.services.AlarmReceiver;

public class DateType extends ReminderType {

    Context context;

    public DateType(Context context, String type) {
        super(context);
        this.context = context;
        setType(type);
    }

    @Override
    public long save(DataItem item) {
        long id = super.save(item);
        startAlarm(id);
        return id;
    }

    @Override
    public void save(long id, DataItem item) {
        super.save(id, item);
        startAlarm(id);
    }

    private void startAlarm(long id) {
        new AlarmReceiver().setAlarm(context, id);
    }
}
