package com.cray.software.justreminder.reminder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.services.AlarmReceiver;

public class DateType extends ReminderType {

    Context context;
    SharedPrefs sPrefs;

    public DateType(Context context) {
        super(context);
        this.context = context;
        sPrefs = new SharedPrefs(context);
    }

    public View inflateView(ViewGroup container, LayoutInflater inflater, String type) {
        int layout = 0;
        if (type.matches(Constants.TYPE_CALL)) layout = R.layout.call_layout;
        else if (type.startsWith(Constants.TYPE_APPLICATION)) layout = R.layout.application_layout;
        else if (type.startsWith(Constants.TYPE_SKYPE)) layout = R.layout.skype_layout;
        else if (type.matches(Constants.TYPE_MESSAGE)) layout = R.layout.message_layout;
        else layout = R.layout.date_layout;
        return inflateView(layout, container, inflater);
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
