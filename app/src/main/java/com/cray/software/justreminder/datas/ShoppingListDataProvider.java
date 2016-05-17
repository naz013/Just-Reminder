/**
 * Copyright 2016 Nazar Suhovich
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cray.software.justreminder.datas;

import android.content.Context;
import android.database.Cursor;

import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.datas.models.ShoppingList;
import com.cray.software.justreminder.json.JShopping;

import java.util.ArrayList;

public class ShoppingListDataProvider {
    private ArrayList<ShoppingList> data;
    private int flag;
    private boolean hidden;

    public ShoppingListDataProvider(){
        data = new ArrayList<>();
    }

    public ShoppingListDataProvider(ArrayList<JShopping> datas, boolean hidden){
        this.data = new ArrayList<>();
        this.hidden = hidden;
        loadFromList(datas);
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

    public void loadFromList(ArrayList<JShopping> jShoppings) {
        data = new ArrayList<>();
        data.clear();
        for (JShopping item : jShoppings) {
            if (!hidden) {
                int deleted = item.getDeleted();
                if (deleted == ShoppingList.ACTIVE) {
                    data.add(new ShoppingList(item.getSummary(), item.getStatus(),
                            item.getUuId(), deleted, item.getDateTime()));
                }
            } else {
                data.add(new ShoppingList(item.getSummary(), item.getStatus(),
                        item.getUuId(), item.getDeleted(), item.getDateTime()));
            }
        }
    }
}
