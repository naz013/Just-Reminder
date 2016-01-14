package com.cray.software.justreminder.datas;

import android.content.Context;
import android.database.Cursor;

import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.datas.models.MarkerModel;

import java.util.ArrayList;
import java.util.List;

public class PlaceDataProvider {
    private List<MarkerModel> data;
    private Context mContext;

    public PlaceDataProvider(Context mContext){
        data = new ArrayList<>();
        this.mContext = mContext;
        load();
    }

    public List<MarkerModel> getData(){
        return data;
    }

    public int getCount(){
        return data != null ? data.size() : 0;
    }

    public int getPosition(MarkerModel item){
        int res = -1;
        if (data.size() > 0) {
            for (int i = 0; i < data.size(); i++){
                MarkerModel item1 = data.get(i);
                if (item.getId() == item1.getId()) {
                    res = i;
                    break;
                }
            }
        }
        return res;
    }

    public MarkerModel getItem(int index) {
        if (index < 0 || index >= getCount()) {
            return null;
        }

        return data.get(index);
    }

    public void load() {
        data.clear();
        DataBase db = new DataBase(mContext);
        db.open();
        Cursor c = db.queryPlaces();
        if (c != null && c.moveToNext()) {
            do {
                String text = c.getString(c.getColumnIndex(Constants.LocationConstants.COLUMN_LOCATION_NAME));
                long id = c.getLong(c.getColumnIndex(Constants.LocationConstants.COLUMN_ID));
                data.add(new MarkerModel(text, id));
            } while (c.moveToNext());
        }
        if (c != null) c.close();
        db.close();
    }
}
