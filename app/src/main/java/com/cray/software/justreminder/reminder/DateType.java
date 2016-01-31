package com.cray.software.justreminder.reminder;

import android.content.Context;
import android.support.v4.app.Fragment;

import com.cray.software.justreminder.json.JModel;
import com.cray.software.justreminder.services.AlarmReceiver;

public class DateType extends Type {

    private Context mContext;

    public DateType(Context context, String type, Fragment fragment) {
        super(context);
        this.mContext = context;
        setType(type);
        inflateView(fragment);
    }

    @Override
    public long save(JModel item) {
        long id = super.save(item);
        startAlarm(id);
        exportToServices(item, id);
        return id;
    }

    @Override
    public void save(long id, JModel item) {
        super.save(id, item);
        startAlarm(id);
        exportToServices(item, id);
    }

    private void startAlarm(long id) {
        new AlarmReceiver().enableReminder(mContext, id);
    }
}
