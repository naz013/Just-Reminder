package com.cray.software.justreminder.reminder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.services.AlarmReceiver;

public class TimerType extends ReminderType {

    Context context;
    SharedPrefs sPrefs;

    public TimerType(Context context) {
        super(context);
        this.context = context;
        sPrefs = new SharedPrefs(context);
    }

    public View inflateView(ViewGroup container, LayoutInflater inflater) {
        return inflateView(R.layout.time_layout, container, inflater);
    }

    @Override
    public long save(DataItem item) {
        long id = super.save(item);
        new AlarmReceiver().setAlarm(context, id);
        return id;
    }

    @Override
    public void save(long id, DataItem item) {
        super.save(id, item);
        new AlarmReceiver().setAlarm(context, id);
    }
}
