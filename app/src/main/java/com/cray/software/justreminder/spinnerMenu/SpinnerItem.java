package com.cray.software.justreminder.spinnerMenu;

public class SpinnerItem {

    private String title;
    private int icon;

    public SpinnerItem(String title, int icon){
        this.title = title;
        this.icon = icon;
    }

    public String getTitle(){
        return this.title;
    }

    public int getIcon(){
        return this.icon;
    }
}