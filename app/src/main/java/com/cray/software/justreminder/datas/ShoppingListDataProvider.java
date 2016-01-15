package com.cray.software.justreminder.datas;

import android.content.Context;
import android.database.Cursor;

import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.databases.NextBase;
import com.cray.software.justreminder.datas.models.ShoppingList;
import com.cray.software.justreminder.json.JsonParser;
import com.cray.software.justreminder.json.JsonShopping;

import java.util.ArrayList;
import java.util.List;

public class ShoppingListDataProvider {
    private ArrayList<ShoppingList> data;
    private Context mContext;
    private long remId;
    private int flag;

    public ShoppingListDataProvider(Context mContext){
        data = new ArrayList<>();
        this.mContext = mContext;
    }

    public ShoppingListDataProvider(Context mContext, long remId, int flag){
        this.mContext = mContext;
        this.remId = remId;
        this.flag = flag;
        load();
    }

    public ShoppingListDataProvider(List<JsonShopping> data){
        this.data = new ArrayList<>();
        loadFromList(data);
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public int getFlag() {
        return flag;
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

    public void removeItem(int position){
        data.remove(position);
    }

    public ShoppingList getItem(int index) {
        if (index < 0 || index >= getCount()) {
            return null;
        }

        return data.get(index);
    }

    public static ArrayList<ShoppingList> load(Context mContext, long remId) {
        ArrayList<ShoppingList> data = new ArrayList<>();
        data.clear();
        DataBase db = new DataBase(mContext);
        db.open();
        Cursor c = db.getShopItemsActive(remId);
        if (c != null && c.moveToFirst()){
            do {
                String title = c.getString(c.getColumnIndex(Constants.COLUMN_TEXT));
                String uuId = c.getString(c.getColumnIndex(Constants.COLUMN_TECH_VAR));
                int checked = c.getInt(c.getColumnIndex(Constants.COLUMN_ARCHIVED));
                long time = c.getLong(c.getColumnIndex(Constants.COLUMN_DATE_TIME));
                int status = c.getInt(c.getColumnIndex(Constants.COLUMN_EXTRA_1));
                data.add(new ShoppingList(title, checked, uuId, status, time));
            } while (c.moveToNext());
        }
        if (c != null) c.close();
        db.close();
        return data;
    }

    public void loadFromList(List<JsonShopping> jsonShoppings) {
        data = new ArrayList<>();
        data.clear();
        for (JsonShopping item : jsonShoppings) {
            data.add(new ShoppingList(item.getSummary(), item.getStatus(),
                    item.getUuId(), item.getDeleted(), item.getDateTime()));
        }
    }

    public void load() {
        data = new ArrayList<>();
        data.clear();
        NextBase db = new NextBase(mContext);
        db.open();
        Cursor c = db.getReminder(remId);
        if (c != null && c.moveToFirst()){
            do {
                String json = c.getString(c.getColumnIndex(NextBase.JSON));
                List<JsonShopping> jsonShoppings = new JsonParser(json).getShoppings();
                for (JsonShopping item : jsonShoppings) {
                    data.add(new ShoppingList(item.getSummary(), item.getStatus(),
                            item.getUuId(), item.getDeleted(), item.getDateTime()));
                }
            } while (c.moveToNext());
        }
        if (c != null) c.close();
        db.close();
    }
}
