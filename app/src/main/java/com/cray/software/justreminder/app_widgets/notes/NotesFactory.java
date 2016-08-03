/**
 * Copyright 2015 Nazar Suhovich
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cray.software.justreminder.app_widgets.notes;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.notes.NoteHelper;
import com.cray.software.justreminder.notes.NoteItem;

import java.util.ArrayList;
import java.util.List;

public class NotesFactory implements RemoteViewsService.RemoteViewsFactory {

    private List<NoteItem> notes;
    private Context mContext;

    NotesFactory(Context ctx, Intent intent) {
        mContext = ctx;
        int widgetID = intent.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    @Override
    public void onCreate() {
        notes = new ArrayList<>();
    }

    @Override
    public void onDataSetChanged() {
        notes.clear();
        List<NoteItem> list = NoteHelper.getInstance(mContext).getAll();
        notes.addAll(list);
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        return notes.size();
    }

    @Override
    public RemoteViews getViewAt(int i) {
        RemoteViews rView = new RemoteViews(mContext.getPackageName(), R.layout.list_item_note_widget);
        ColorSetter cs = new ColorSetter(mContext);
        NoteItem note = notes.get(i);
        rView.setInt(R.id.noteBackground, "setBackgroundColor", cs.getNoteLightColor(note.getColor()));
        byte[] byteImage = note.getImage();
        if (byteImage != null){
            Bitmap photo = BitmapFactory.decodeByteArray(byteImage, 0, byteImage.length);
            if (photo != null){
                rView.setImageViewBitmap(R.id.noteImage, photo);
                rView.setViewVisibility(R.id.noteImage, View.VISIBLE);
            } else rView.setViewVisibility(R.id.noteImage, View.GONE);
        } else rView.setViewVisibility(R.id.noteImage, View.GONE);
        String title = note.getNote();
        if (SharedPrefs.getInstance(mContext).getBoolean(Prefs.NOTE_ENCRYPT)){
                title = SyncHelper.decrypt(title);
        }
        rView.setTextViewText(R.id.note, title);
        Intent fillInIntent = new Intent();
        fillInIntent.putExtra(Constants.EDIT_ID, note.getId());
        rView.setOnClickFillInIntent(R.id.note, fillInIntent);
        rView.setOnClickFillInIntent(R.id.noteImage, fillInIntent);
        rView.setOnClickFillInIntent(R.id.noteBackground, fillInIntent);
        return rView;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}