package com.cray.software.justreminder.reminder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.services.MonthDayReceiver;

public class MonthdayType extends ReminderType {

    Context context;
    SharedPrefs sPrefs;

    public MonthdayType(Context context) {
        super(context);
        this.context = context;
        sPrefs = new SharedPrefs(context);
    }

    public View inflateView(ViewGroup container, LayoutInflater inflater) {
        return inflateView(R.layout.reminder_day_of_month_layout, container, inflater);
    }

    @Override
    public long save(DataItem item) {
        long id = super.save(item);
        new MonthDayReceiver().setAlarm(context, id);
        return id;
    }

    @Override
    public void save(long id, DataItem item) {
        super.save(id, item);
        new MonthDayReceiver().setAlarm(context, id);
    }
}
