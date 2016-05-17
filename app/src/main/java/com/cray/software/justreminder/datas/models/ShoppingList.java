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

package com.cray.software.justreminder.datas.models;

import android.content.Context;
import android.database.Cursor;

import com.cray.software.justreminder.databases.NextBase;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.json.JModel;
import com.cray.software.justreminder.json.JParser;
import com.cray.software.justreminder.json.JShopping;

import java.util.ArrayList;

public class ShoppingList {

    private String title;
    private int position;

    /**
     * Task mark flag. 1 - completed, 0 - active.
     */
    private int isChecked;
    private String uuId;
    private long time;
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

    public ShoppingList(String title, int isChecked, String uuId, int status, long time){
        this.uuId = uuId;
        this.title = title;
        this.isChecked = isChecked;
        this.status = status;
        this.time = time;
    }

    /**
     * Mark task in shopping list done/undone.
     * @param context application context.
     * @param id task identifier.
     * @param checked task status.
     * @param uuId shopping item unique identifier.
     */
    public static void switchItem(Context context, long id, boolean checked, String uuId){
        NextBase db = new NextBase(context);
        db.open();
        Cursor c = db.getReminder(id);
        if (c != null && c.moveToFirst()) {
            String json = c.getString(c.getColumnIndex(NextBase.JSON));
            JParser jParser = new JParser(json);
            JModel jModel = jParser.parse();
            ArrayList<JShopping> shoppings = jModel.getShoppings();
            for (JShopping shopping : shoppings) {
                if (shopping.getUuId().matches(uuId)) {
                    shopping.setStatus(checked ? 1 : 0);
                    break;
                }
            }

            jModel.setShoppings(shoppings);
            jParser.toJsonString(jModel);
            db.setJson(id, jParser.toJsonString());
        }
        if (c != null) c.close();
        db.close();
    }

    /**
     * Hide task from shopping list.
     * @param context application context.
     * @param id task identifier.
     */
    public static void hideItem(Context context, long id, String uuId){
        NextBase db = new NextBase(context);
        db.open();
        Cursor c = db.getReminder(id);
        if (c != null && c.moveToFirst()) {
            String json = c.getString(c.getColumnIndex(NextBase.JSON));
            JParser jParser = new JParser(json);
            JModel jModel = jParser.parse();
            ArrayList<JShopping> shoppings = jModel.getShoppings();
            for (JShopping shopping : shoppings) {
                if (shopping.getUuId().matches(uuId)) {
                    shopping.setDeleted(DELETED);
                    break;
                }
            }

            jModel.setShoppings(shoppings);
            jParser.toJsonString(jModel);
            db.setJson(id, jParser.toJsonString());
        }
        if (c != null) c.close();
        db.close();
    }

    /**
     * Show task in shopping list.
     * @param context application context.
     * @param id task identifier.
     */
    public static void showItem(Context context, long id, String uuId){
        NextBase db = new NextBase(context);
        db.open();
        Cursor c = db.getReminder(id);
        if (c != null && c.moveToFirst()) {
            String json = c.getString(c.getColumnIndex(NextBase.JSON));
            JParser jParser = new JParser(json);
            JModel jModel = jParser.parse();
            ArrayList<JShopping> shoppings = jModel.getShoppings();
            for (JShopping shopping : shoppings) {
                if (shopping.getUuId().matches(uuId)) {
                    shopping.setDeleted(ACTIVE);
                    break;
                }
            }

            jModel.setShoppings(shoppings);
            jParser.toJsonString(jModel);
            db.setJson(id, jParser.toJsonString());
        }
        if (c != null) c.close();
        db.close();
    }

    /**
     * Remove task from database.
     * @param context application context.
     * @param id task identifier.
     * @param uuId shop item unique identifier.
     */
    public static void removeItem(Context context, long id, String uuId){
        NextBase db = new NextBase(context);
        db.open();
        Cursor c = db.getReminder(id);
        if (c != null && c.moveToFirst()) {
            String json = c.getString(c.getColumnIndex(NextBase.JSON));
            JParser jParser = new JParser(json);
            JModel jModel = jParser.parse();
            ArrayList<JShopping> shoppings = jModel.getShoppings();
            for (int i = 0; i < shoppings.size(); i++) {
                if (shoppings.get(i).getUuId().matches(uuId)) {
                    shoppings.remove(i);
                    break;
                }
            }
            jModel.setShoppings(shoppings);
            jParser.toJsonString(jModel);
            db.setJson(id, jParser.toJsonString());
        }
        if (c != null) c.close();
        db.close();
    }

    /**
     * Get all tasks unique identifiers.
     * @param context application context.
     * @param remId reminder identifier.
     * @return Map with unique identifier as key and database identifier as value
     */
    public static ArrayList<String> getUuIds(Context context, long remId){
        NextBase db = new NextBase(context);
        db.open();
        ArrayList<String> ids = new ArrayList<>();
        Cursor c = db.getReminder(remId);
        if (c != null && c.moveToFirst()){
            String json = c.getString(c.getColumnIndex(NextBase.JSON));
            ids = new JParser(json).getShoppingKeys();
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

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
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
