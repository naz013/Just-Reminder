package com.cray.software.justreminder.datas;

import android.content.Context;
import android.database.Cursor;

import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.interfaces.Constants;

import java.util.ArrayList;
import java.util.List;

public class CategoryDataProvider {
    private List<CategoryModel> data;
    private Context mContext;
    private CategoryModel mLastRemovedData;
    private int mLastRemovedPosition = -1;

    public CategoryDataProvider(Context mContext){
        data = new ArrayList<>();
        this.mContext = mContext;
        load();
    }

    public List<CategoryModel> getData(){
        return data;
    }

    public int getCount(){
        return data != null ? data.size() : 0;
    }

    public int getPosition(CategoryModel item){
        int res = -1;
        if (data.size() > 0) {
            for (int i = 0; i < data.size(); i++){
                CategoryModel item1 = data.get(i);
                if (item.getUuID().matches(item1.getUuID())) {
                    res = i;
                    break;
                }
            }
        }
        return res;
    }

    public int getPosition(String uuId){
        int res = -1;
        if (data.size() > 0 && uuId != null) {
            for (int i = 0; i < data.size(); i++){
                CategoryModel item = data.get(i);
                if (item.getUuID().matches(uuId)) {
                    res = i;
                    break;
                }
            }
        } else res = 0;
        return res;
    }

    public int removeItem(CategoryModel item){
        int res = 0;
        if (data.size() > 0) {
            for (int i = 0; i < data.size(); i++){
                CategoryModel item1 = data.get(i);
                if (item.getUuID().matches(item1.getUuID())) {
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

        final CategoryModel item = data.remove(from);

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

    public CategoryModel getItem(int index) {
        if (index < 0 || index >= getCount()) {
            return null;
        }

        return data.get(index);
    }

    public void load() {
        data.clear();
        DataBase db = new DataBase(mContext);
        db.open();
        Cursor c = db.queryCategories();
        if (c != null && c.moveToNext()) {
            do {
                String text = c.getString(c.getColumnIndex(Constants.COLUMN_TEXT));
                String uuId = c.getString(c.getColumnIndex(Constants.COLUMN_TECH_VAR));
                int color = c.getInt(c.getColumnIndex(Constants.COLUMN_COLOR));
                long id = c.getLong(c.getColumnIndex(Constants.COLUMN_ID));

                data.add(new CategoryModel(text, uuId, color, id));
            } while (c.moveToNext());
        }
        if (c != null) c.close();
        db.close();
    }
}
