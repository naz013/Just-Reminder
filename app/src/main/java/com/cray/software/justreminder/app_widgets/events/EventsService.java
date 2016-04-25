package com.cray.software.justreminder.app_widgets.events;

import android.content.Intent;
import android.widget.RemoteViewsService;

public class EventsService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new EventsFactory(getApplicationContext(), intent);
    }
}
