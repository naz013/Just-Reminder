package com.cray.software.justreminder.datas;

import android.content.Context;
import android.database.Cursor;

import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.interfaces.Constants;

import java.util.ArrayList;
import java.util.List;

public class PlaceDataProvider {
    private List<Marker> data;
    private Context mContext;
    private Marker mLastRemovedData;
    private int mLastRemovedPosition = -1;

    public PlaceDataProvider(Context mContext){
        data = new ArrayList<>();
        this.mContext = mContext;
        load();
    }

    public List<Marker> getData(){
        return data;
    }

    public int getCount(){
        return data != null ? data.size() : 0;
    }

    public int getPosition(Marker item){
        int res = -1;
        if (data.size() > 0) {
            for (int i = 0; i < data.size(); i++){
                Marker item1 = data.get(i);
                if (item.getId() == item1.getId()) {
                    res = i;
                    break;
                }
            }
        }
        return res;
    }

    public int removeItem(Marker item){
        int res = 0;
        if (data.size() > 0) {
            for (int i = 0; i < data.size(); i++){
                Marker item1 = data.get(i);
                if (item.getId() == item1.getId()) {
                    data.remove(i);
                    res = i;
                    break;
                }
            }
        }
        return res;
    }

    public void removeItem(int position){
        mLastRemovedData = data.remove(position);
        mLastRemovedPosition = position;
    }

    public void moveItem(int from, int to){
        if (to < 0 || to >= getCount()) {
            throw new IndexOutOfBoundsException("index = " + to);
        }

        if (from == to) {
            return;
        }

        final Marker item = data.remove(from);

        data.add(to, item);
        mLastRemovedPosition = -1;
    }

    public int undoLastRemoval() {
        if (mLastRemovedData != null) {
            int insertedPosition;
            if (mLastRemovedPosition >= 0 && mLastRemovedPosition < data.size()) {
                insertedPosition = mLastRemovedPosition;
            } else {
                insertedPosition = data.size();
            }

            data.add(insertedPosition, mLastRemovedData);

            mLastRemovedData = null;
            mLastRemovedPosition = -1;

            return insertedPosition;
        } else {
            return -1;
        }
    }

    public Marker getItem(int index) {
        if (index < 0 || index >= getCount()) {
            throw new IndexOutOfBoundsException("index = " + index);
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
                data.add(new Marker(text, id));
            } while (c.moveToNext());
        }
        if (c != null) c.close();
        db.close();
    }
}
