package com.cray.software.justreminder.reminder;

import android.content.Context;

import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.services.MonthDayReceiver;

public class MonthdayType extends Type {

    private Context mContext;

    public MonthdayType(Context context) {
        super(context);
        this.mContext = context;
        setType(Constants.TYPE_MONTHDAY);
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
        new MonthDayReceiver().setAlarm(mContext, id);
    }
}
