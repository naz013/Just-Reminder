package com.cray.software.justreminder.app_widgets.services;

import android.content.Intent;
import android.widget.RemoteViewsService;

public class CurrentTaskService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new CurrentTaskFactory(getApplicationContext(), intent);
    }
}
