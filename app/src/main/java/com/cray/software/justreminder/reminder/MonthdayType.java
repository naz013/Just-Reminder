package com.cray.software.justreminder.reminder;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.services.MonthDayReceiver;

public class MonthdayType extends ReminderType {

    Context context;

    public MonthdayType(Context context) {
        super(context);
        this.context = context;
        setType(Constants.TYPE_MONTHDAY);
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
        new MonthDayReceiver().setAlarm(context, id);
    }
}
