package com.cray.software.justreminder.datas;

public class Category {
    private String title;
    private String uuID;

    public Category(String title, String uuID){
        this.uuID = uuID;
        this.title = title;
    }

    public String getTitle(){
        return title;
    }

    public String getUuID(){
        return uuID;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public void setUuID(String uuID){
        this.uuID = uuID;
    }
}
