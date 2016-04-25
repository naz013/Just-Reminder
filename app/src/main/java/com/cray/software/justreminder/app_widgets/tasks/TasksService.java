package com.cray.software.justreminder.app_widgets.tasks;

import android.content.Intent;
import android.widget.RemoteViewsService;

import com.cray.software.justreminder.app_widgets.tasks.TasksFactory;

public class TasksService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new TasksFactory(getApplicationContext(), intent);
    }
}
