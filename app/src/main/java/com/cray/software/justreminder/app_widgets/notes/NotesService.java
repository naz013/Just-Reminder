package com.cray.software.justreminder.app_widgets.notes;

import android.content.Intent;
import android.widget.RemoteViewsService;

public class NotesService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new NotesFactory(getApplicationContext(), intent);
    }
}
