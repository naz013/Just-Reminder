package com.cray.software.justreminder.interfaces;

public class Item {
    String title;
    String uuID;

    public Item(String title, String uuID){
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
