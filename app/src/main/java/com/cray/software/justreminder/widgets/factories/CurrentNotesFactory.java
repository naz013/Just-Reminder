package com.cray.software.justreminder.widgets.factories;

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
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.Prefs;

import java.util.ArrayList;

public class CurrentNotesFactory implements RemoteViewsService.RemoteViewsFactory {

    private ArrayList<Note> notes;
    private Context mContext;

    CurrentNotesFactory(Context ctx, Intent intent) {
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
        NotesBase db = new NotesBase(mContext);
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

                notes.add(new Note(title, col, id, img));
            } while (c.moveToNext());
        }
        if(c != null) c.close();
        db.close();
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
        RemoteViews rView = new RemoteViews(mContext.getPackageName(),
                R.layout.list_item_note_widget);
        ColorSetter cs = new ColorSetter(mContext);
        Note note = notes.get(i);
        rView.setInt(R.id.noteBackground, "setBackgroundColor", cs.getNoteLightColor(note.getColor()));
        byte[] byteImage = note.getImage();
        if (byteImage != null){
            Bitmap photo = BitmapFactory.decodeByteArray(byteImage, 0, byteImage.length);
            if (photo != null){
                rView.setImageViewBitmap(R.id.noteImage, photo);
                rView.setViewVisibility(R.id.noteImage, View.VISIBLE);
            } else rView.setViewVisibility(R.id.noteImage, View.GONE);
        } else rView.setViewVisibility(R.id.noteImage, View.GONE);

        SharedPrefs prefs = new SharedPrefs(mContext);
        String title = note.getText();
        if (prefs.loadBoolean(Prefs.NOTE_ENCRYPT)){
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

    public class Note{
        private String text;
        private int color;
        private long id;
        private byte[] image;

        public Note(String text, int color, long id, byte[] image){
            this.text = text;
            this.color = color;
            this.id = id;
            this.image = image;
        }

        public byte[] getImage() {
            return image;
        }

        public void setImage(byte[] image) {
            this.image = image;
        }

        public int getColor() {
            return color;
        }

        public void setColor(int color) {
            this.color = color;
        }

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }
}