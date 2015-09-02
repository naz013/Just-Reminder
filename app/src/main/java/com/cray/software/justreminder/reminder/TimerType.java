package com.cray.software.justreminder.reminder;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.services.AlarmReceiver;

public class TimerType extends ReminderType {

    Context context;

    public TimerType(Context context) {
        super(context);
        this.context = context;
        setType(Constants.TYPE_TIME);
    }

    @Override
    public long save(DataItem item) {
        long id = super.save(item);
        startAlarm(id);
        return id;
    }

    private void startAlarm(long id) {
        new AlarmReceiver().setAlarm(context, id);
    }

    @Override
    public void save(long id, DataItem item) {
        super.save(id, item);
        startAlarm(id);
    }
}
