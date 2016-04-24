package com.cray.software.justreminder.app_widgets.calendar;

import android.content.Intent;
import android.widget.RemoteViewsService;

public class CalendarMonthService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new CalendarMonthFactory(getApplicationContext(), intent);
    }
}
