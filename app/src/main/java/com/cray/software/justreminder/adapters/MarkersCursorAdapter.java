package com.cray.software.justreminder.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filterable;
import android.widget.TextView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.interfaces.Constants;

public class MarkersCursorAdapter extends CursorAdapter implements Filterable {

    LayoutInflater inflater;
    private Cursor c;
    Context context;

    @SuppressWarnings("deprecation")
    public MarkersCursorAdapter(Context context, Cursor c) {
        super(context, c);
        this.context = context;
        inflater = LayoutInflater.from(context);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.c = c;
        c.moveToFirst();
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return inflater.inflate(R.layout.list_item_current, null);
    }

    @Override
    public long getItemId(int position) {
        Cursor cursor = getCursor();
        cursor.moveToPosition(position);
        return cursor.getLong(cursor.getColumnIndex("_id"));
    }

    @Override
    public Object getItem(int position) {
        return super.getItem(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        c.moveToPosition(position);
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item_geo, null);
        }

        TextView taskTitle = (TextView) convertView.findViewById(R.id.taskText);
        TextView latitude = (TextView) convertView.findViewById(R.id.latitude);
        TextView longitude = (TextView) convertView.findViewById(R.id.longitude);

        String title = c.getString(c.getColumnIndex(Constants.COLUMN_TEXT));

        double lat = c.getDouble(c.getColumnIndex(Constants.COLUMN_LATITUDE));
        double longi = c.getDouble(c.getColumnIndex(Constants.COLUMN_LONGITUDE));

        taskTitle.setText(title);
        latitude.setText(String.valueOf(lat));
        longitude.setText(String.valueOf(longi));

        return convertView;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

    }
}