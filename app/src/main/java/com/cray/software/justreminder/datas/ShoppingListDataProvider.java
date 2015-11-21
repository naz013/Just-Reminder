package com.cray.software.justreminder.datas;

import android.content.Context;
import android.database.Cursor;

import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.datas.models.ShoppingList;

import java.util.ArrayList;

public class ShoppingListDataProvider {
    private ArrayList<ShoppingList> data;
    private ArrayList<ShoppingList> initList;
    private Context mContext;
    private ShoppingList mLastRemovedData;
    private int mLastRemovedPosition = -1;
    private long remId;
    private int flag;

    public ShoppingListDataProvider(Context mContext){
        data = new ArrayList<>();
        initList = new ArrayList<>();
        this.mContext = mContext;
    }

    public ShoppingListDataProvider(Context mContext, long remId, int flag){
        this.mContext = mContext;
        initList = new ArrayList<>();
        this.remId = remId;
        this.flag = flag;
        load();
    }

    public ShoppingListDataProvider(ArrayList<ShoppingList> data){
        this.data = new ArrayList<>();
        initList = new ArrayList<>();
        this.data.addAll(data);
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public int getFlag() {
        return flag;
    }

    public ArrayList<ShoppingList> getRemovedItems(){
        long start = System.currentTimeMillis();
        ArrayList<ShoppingList> lists = new ArrayList<>();
        for (ShoppingList item : initList){
            long id = item.getId();
            if (id != 0) {
                lists.add(item);
            }
        }
        return lists;
    }

    public void clear(){
        if (data != null) data.clear();
    }

    public int addItem(ShoppingList item){
        int size = data.size();
        data.add(size, item);
        return size;
    }

    public ArrayList<ShoppingList> getData(){
        return data;
    }

    public int getCount(){
        return data != null ? data.size() : 0;
    }

    public int getPosition(ShoppingList item){
        int res = -1;
        if (data.size() > 0) {
            for (int i = 0; i < data.size(); i++){
                ShoppingList item1 = data.get(i);
                if (item.getUuId().matches(item1.getUuId())) {
                    res = i;
                    break;
                }
            }
        }
        return res;
    }

    public int removeItem(ShoppingList item){
        int res = 0;
        if (data.size() > 0) {
            for (int i = 0; i < data.size(); i++){
                ShoppingList item1 = data.get(i);
                if (item.getUuId().matches(item1.getUuId())) {
                    data.remove(i);
                    initList.add(item1);
                    res = i;
                    break;
                }
            }
        }
        return res;
    }

    public void removeItem(int position){
        initList.add(data.get(position));
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

        final ShoppingList item = data.remove(from);

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

    public ShoppingList getItem(int index) {
        if (index < 0 || index >= getCount()) {
            return null;
        }

        return data.get(index);
    }

    public void load() {
        data = new ArrayList<>();
        data.clear();
        DataBase db = new DataBase(mContext);
        db.open();
        Cursor c = db.getShopItems(remId);
        if (flag == ShoppingList.ACTIVE) c = db.getShopItemsActive(remId);
        if (c != null && c.moveToFirst()){
            do {
                long id = c.getLong(c.getColumnIndex(Constants.COLUMN_ID));
                String title = c.getString(c.getColumnIndex(Constants.COLUMN_TEXT));
                String uuId = c.getString(c.getColumnIndex(Constants.COLUMN_TECH_VAR));
                int checked = c.getInt(c.getColumnIndex(Constants.COLUMN_ARCHIVED));
                long time = c.getLong(c.getColumnIndex(Constants.COLUMN_DATE_TIME));
                int status = c.getInt(c.getColumnIndex(Constants.COLUMN_EXTRA_1));
                data.add(new ShoppingList(id, title, checked, uuId, remId, status, time));
            } while (c.moveToNext());
        }
        if (c != null) c.close();
        db.close();
    }
}
