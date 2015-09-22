package com.cray.software.justreminder.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.datas.MarkerItem;

import java.util.ArrayList;

public class MarkersCursorAdapter extends BaseAdapter implements Filterable {

    LayoutInflater inflater;
    Context context;
    private ArrayList<MarkerItem> data;

    @SuppressWarnings("deprecation")
    public MarkersCursorAdapter(Context context, ArrayList<MarkerItem> data) {
        this.context = context;
        this.data = data;
        inflater = LayoutInflater.from(context);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item_geo, null);
        }

        TextView taskTitle = (TextView) convertView.findViewById(R.id.taskText);
        TextView latitude = (TextView) convertView.findViewById(R.id.latitude);
        TextView longitude = (TextView) convertView.findViewById(R.id.longitude);
        ImageView markerIcon = (ImageView) convertView.findViewById(R.id.markerIcon);

        MarkerItem item = data.get(position);

        String title = item.getTitle();

        double lat = item.getPosition().latitude;
        double longi = item.getPosition().longitude;

        taskTitle.setText(title);
        latitude.setText(String.format("%.5f", lat));
        longitude.setText(String.format("%.5f", longi));
        markerIcon.setImageResource(item.getIcon());

        return convertView;
    }

    @Override
    public Filter getFilter() {
        return null;
    }
}