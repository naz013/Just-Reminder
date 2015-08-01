package com.cray.software.justreminder.reminder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.services.WeekDayReceiver;

public class WeekdayType extends ReminderType {

    Context context;
    SharedPrefs sPrefs;

    public WeekdayType(Context context) {
        super(context);
        this.context = context;
        sPrefs = new SharedPrefs(context);
    }

    public View inflateView(ViewGroup container, LayoutInflater inflater) {
        return inflateView(R.layout.weekdays_reminder_layout, container, inflater);

    }

    @Override
    public long save(DataItem item) {
        long id = super.save(item);
        new WeekDayReceiver().setAlarm(context, id);
        return id;
    }

    @Override
    public void save(long id, DataItem item) {
        super.save(id, item);
        new WeekDayReceiver().setAlarm(context, id);
    }
}
