package com.cray.software.justreminder.widgets.services;

import android.content.Intent;
import android.widget.RemoteViewsService;

import com.cray.software.justreminder.widgets.factories.TasksFactory;

public class TasksService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new TasksFactory(getApplicationContext(), intent);
    }
}
