package com.cray.software.justreminder.widgets.services;

import android.content.Intent;
import android.widget.RemoteViewsService;

import com.cray.software.justreminder.widgets.factories.CurrentNotesFactory;

public class CurrentNotesService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new CurrentNotesFactory(getApplicationContext(), intent);
    }
}
