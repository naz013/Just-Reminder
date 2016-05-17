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

import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.constants.Constants;

/**
 * Reminder category item constructor.
 */
public class CategoryModel {
    private String title;
    private String uuID;
    private int color;
    private long id;

    public CategoryModel(String title, String uuID){
        this.uuID = uuID;
        this.title = title;
    }

    public CategoryModel(String title, String uuID, int color, long id){
        this.uuID = uuID;
        this.title = title;
        this.color = color;
        this.id = id;
    }

    public CategoryModel(String title, String uuID, int color){
        this.uuID = uuID;
        this.title = title;
        this.color = color;
    }

    /**
     * Change group indicator color.
     * @param context application context.
     * @param id group identifier.
     * @param code indicator color code.
     */
    public static void setNewIndicator(Context context, long id, int code){
        DataBase db = new DataBase(context);
        db.open();
        db.updateCategoryColor(id, code);
        db.close();
    }

    public static String getCategoryTitle(Context context, String id){
        DataBase db = new DataBase(context);
        db.open();
        Cursor c = db.getCategory(id);
        String title = null;
        if (c != null && c.moveToFirst()){
            title = c.getString(c.getColumnIndex(Constants.COLUMN_TEXT));
        }
        if (c != null) c.close();
        db.close();
        return title;
    }

    public static String getDefault(Context context){
        DataBase db = new DataBase(context);
        db.open();
        Cursor c = db.queryCategories();
        String uuId = null;
        if (c != null && c.moveToFirst()){
            uuId = c.getString(c.getColumnIndex(Constants.COLUMN_TECH_VAR));
        }
        if (c != null) c.close();
        db.close();
        return uuId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle(){
        return title;
    }

    public String getUuID(){
        return uuID;
    }

    public int getColor(){
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public void setUuID(String uuID){
        this.uuID = uuID;
    }
}
