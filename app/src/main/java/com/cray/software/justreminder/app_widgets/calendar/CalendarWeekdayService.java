package com.cray.software.justreminder.app_widgets.calendar;

import android.content.Intent;
import android.widget.RemoteViewsService;

public class CalendarWeekdayService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new CalendarWeekdayFactory(getApplicationContext(), intent);
    }
}
