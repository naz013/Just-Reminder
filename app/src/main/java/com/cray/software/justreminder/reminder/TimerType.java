package com.cray.software.justreminder.reminder;

import android.content.Context;

import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.services.AlarmReceiver;

public class TimerType extends Type {

    private Context mContext;

    public TimerType(Context context) {
        super(context);
        this.mContext = context;
        setType(Constants.TYPE_TIME);
    }

    @Override
    public long save(Reminder item) {
        long id = super.save(item);
        startAlarm(id);
        exportToServices(item, id);
        return id;
    }

    private void startAlarm(long id) {
        new AlarmReceiver().setAlarm(mContext, id);
    }

    @Override
    public void save(long id, Reminder item) {
        super.save(id, item);
        startAlarm(id);
        exportToServices(item, id);
    }
}
