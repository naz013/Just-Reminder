package com.cray.software.justreminder.widgets;

import android.content.Intent;
import android.widget.RemoteViewsService;

public class CurrentNotesService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new CurrentNotesFactory(getApplicationContext(), intent);
    }
}
