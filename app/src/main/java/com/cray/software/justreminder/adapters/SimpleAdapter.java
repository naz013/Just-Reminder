package com.cray.software.justreminder.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.interfaces.Constants;

public class SimpleAdapter extends CursorAdapter {

    LayoutInflater inflater;
    private Cursor c;
    Context cContext;

    public SimpleAdapter(Context context, Cursor c) {
        super(context, c);
        this.cContext = context;
        inflater = LayoutInflater.from(context);
        this.c = c;
        c.moveToFirst();
    }

    @Override
    public int getCount() {
        return c.getCount();
    }

    @Override
    public Object getItem(int position) {
        return super.getItem(position);
    }

    @Override
    public long getItemId(int position) {
        Cursor cursor = getCursor();
        cursor.moveToPosition(position);
        return cursor.getLong(cursor.getColumnIndex("_id"));
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return inflater.inflate(R.layout.list_item_category_card, null);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        c.moveToPosition(position);
        if (convertView == null) {
            inflater = (LayoutInflater) cContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item_category_card, null);
        }

        String text = c.getString(c.getColumnIndex(Constants.COLUMN_TEXT));
        int color = c.getInt(c.getColumnIndex(Constants.COLUMN_COLOR));

        TextView textView = (TextView) convertView.findViewById(R.id.textView);
        textView.setText(text);

        ColorSetter cs = new ColorSetter(cContext);

        CardView card = (CardView) convertView.findViewById(R.id.card);
        card.setCardBackgroundColor(cs.getCardStyle());

        View indicator = convertView.findViewById(R.id.indicator);
        indicator.setBackgroundDrawable(cContext.getResources().getDrawable(cs.getCategoryIndicator(color)));

        return convertView;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

    }
}
