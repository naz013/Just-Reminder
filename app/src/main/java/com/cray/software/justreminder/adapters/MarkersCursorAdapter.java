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
import com.cray.software.justreminder.helpers.ColorSetter;

import java.util.ArrayList;

public class MarkersCursorAdapter extends BaseAdapter implements Filterable {

    private LayoutInflater inflater;
    private ArrayList<MarkerItem> data;
    private ColorSetter colorSetter;

    @SuppressWarnings("deprecation")
    public MarkersCursorAdapter(Context context, ArrayList<MarkerItem> data) {
        this.data = data;
        inflater = LayoutInflater.from(context);
        colorSetter = new ColorSetter(context);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public long getItemId(int position) {
        return data.get(position).getId();
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
        markerIcon.setImageResource(colorSetter.getMarkerStyle(item.getIcon()));

        return convertView;
    }

    @Override
    public Filter getFilter() {
        return null;
    }
}