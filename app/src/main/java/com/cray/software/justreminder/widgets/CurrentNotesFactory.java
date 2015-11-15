package com.cray.software.justreminder.widgets;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.databases.NotesBase;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.interfaces.Prefs;

import java.util.ArrayList;

public class CurrentNotesFactory implements RemoteViewsService.RemoteViewsFactory {

    private ArrayList<String> note;
    private ArrayList<byte[]> images;
    private ArrayList<Integer> color;
    private ArrayList<Long> ids;
    private Context mContext;
    private NotesBase db;
    private ColorSetter cs;

    CurrentNotesFactory(Context ctx, Intent intent) {
        mContext = ctx;
        int widgetID = intent.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    @Override
    public void onCreate() {
        note = new ArrayList<>();
        images = new ArrayList<>();
        color = new ArrayList<>();
        ids = new ArrayList<>();
        db = new NotesBase(mContext);
        cs = new ColorSetter(mContext);
    }

    @Override
    public void onDataSetChanged() {
        note.clear();
        images.clear();
        color.clear();
        ids.clear();
        db = new NotesBase(mContext);
        String title;
        int col;
        long id;
        byte[] img;
        Cursor c = db.getNotes();
        if (c != null && c.moveToFirst()) {
            do {
                col = c.getInt(c.getColumnIndex(Constants.COLUMN_COLOR));
                id = c.getLong(c.getColumnIndex(Constants.COLUMN_ID));
                title = c.getString(c.getColumnIndex(Constants.COLUMN_NOTE));
                img = c.getBlob(c.getColumnIndex(Constants.COLUMN_IMAGE));

                ids.add(id);
                note.add(title);
                if (img != null) {
                    images.add(img);
                } else images.add(null);

                color.add(col);

            } while (c.moveToNext());
        }

        if(c != null) c.close();
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        return note.size();
    }

    @Override
    public RemoteViews getViewAt(int i) {
        RemoteViews rView = new RemoteViews(mContext.getPackageName(),
                R.layout.list_item_note_widget);
        cs = new ColorSetter(mContext);
        rView.setInt(R.id.noteBackground, "setBackgroundColor", cs.getNoteLightColor(color.get(i)));
        byte[] byteImage = images.get(i);
        if (byteImage != null){
            Bitmap photo = BitmapFactory.decodeByteArray(byteImage, 0, byteImage.length);
            if (photo != null){
                rView.setImageViewBitmap(R.id.imageView, photo);
            } else rView.setViewVisibility(R.id.imageView, View.GONE);
        } else rView.setViewVisibility(R.id.imageView, View.GONE);

        SharedPrefs prefs = new SharedPrefs(mContext);
        String title = note.get(i);
        if (prefs.loadBoolean(Prefs.NOTE_ENCRYPT)){
            title = SyncHelper.decrypt(title);
        }

        rView.setTextViewText(R.id.note, title);

        Intent fillInIntent = new Intent();
        fillInIntent.putExtra(Constants.EDIT_ID, ids.get(i));
        rView.setOnClickFillInIntent(R.id.note, fillInIntent);
        rView.setOnClickFillInIntent(R.id.imageView, fillInIntent);
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