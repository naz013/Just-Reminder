package com.cray.software.justreminder.datas.models;

import android.content.Context;
import android.database.Cursor;

import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.constants.Constants;

import java.util.HashMap;

/**
 * Copyright 2015 Nazar Suhovich
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
public class ShoppingList {

    private String title;
    private int position;

    /**
     * Task mark flag. 1 - completed, 0 - active.
     */
    private int isChecked;
    private String uuId;
    private long remId, id, time;
    private int status;

    public static final int DELETED = -1;
    public static final int ACTIVE = 1;

    public ShoppingList(String title){
        this.uuId = SyncHelper.generateID();
        this.title = title;
        this.status = ACTIVE;
        this.isChecked = 0;
        this.time = System.currentTimeMillis();
    }

    public ShoppingList(long id, String title, int isChecked, String uuId, long remId, int status, long time){
        this.uuId = uuId;
        this.title = title;
        this.isChecked = isChecked;
        this.remId = remId;
        this.id = id;
        this.status = status;
        this.time = time;
    }

    /**
     * Mark task in shopping list done/undone.
     * @param context application context.
     * @param id task identifier.
     * @param checked task status.
     */
    public static void switchItem(Context context, long id, boolean checked){
        DataBase db = new DataBase(context);
        db.open();
        db.updateShopItem(id, checked ? 1 : 0);
        db.close();
    }

    /**
     * Hide task from shopping list.
     * @param context application context.
     * @param id task identifier.
     */
    public static void hideItem(Context context, long id){
        DataBase db = new DataBase(context);
        db.open();
        db.updateShopItemStatus(id, DELETED);
        db.close();
    }

    /**
     * Show task in shopping list.
     * @param context application context.
     * @param id task identifier.
     */
    public static void showItem(Context context, long id){
        DataBase db = new DataBase(context);
        db.open();
        db.updateShopItemStatus(id, ACTIVE);
        db.close();
    }

    /**
     * Remove task from database.
     * @param context application context.
     * @param id task identifier.
     */
    public static void removeItem(Context context, long id){
        DataBase db = new DataBase(context);
        db.open();
        db.deleteShopItem(id);
        db.close();
    }

    /**
     * Get all tasks unique identifiers.
     * @param context application context.
     * @param remId reminder identifier.
     * @return Map with unique identifier as key and database identifier as value
     */
    public static HashMap<String, Long> getUuIds(Context context, long remId){
        DataBase db = new DataBase(context);
        db.open();
        HashMap<String, Long> ids = new HashMap<>();
        Cursor c = db.getShopItems(remId);
        if (c != null && c.moveToFirst()){
            do {
                String uuId = c.getString(c.getColumnIndex(Constants.COLUMN_TECH_VAR));
                long id = c.getLong(c.getColumnIndex(Constants.COLUMN_ID));
                ids.put(uuId, id);
            } while (c.moveToNext());
        }
        if (c != null) c.close();
        db.close();
        return ids;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public int getIsChecked() {
        return isChecked;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getRemId() {
        return remId;
    }

    public void setRemId(long remId) {
        this.remId = remId;
    }

    public String getUuId() {
        return uuId;
    }

    public void setUuId(String uuId) {
        this.uuId = uuId;
    }

    public int isChecked() {
        return isChecked;
    }

    public void setIsChecked(int isChecked) {
        this.isChecked = isChecked;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
