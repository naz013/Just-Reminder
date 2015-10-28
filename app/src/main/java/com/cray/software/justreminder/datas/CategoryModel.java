package com.cray.software.justreminder.datas;

import android.content.Context;

import com.cray.software.justreminder.databases.DataBase;

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
