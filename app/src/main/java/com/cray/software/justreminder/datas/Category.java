package com.cray.software.justreminder.datas;

/**
 * Reminder category item constructor.
 */
public class Category {
    private String title;
    private String uuID;
    private int color;
    private long id;
    private boolean mPinnedToSwipeLeft;

    public Category(String title, String uuID){
        this.uuID = uuID;
        this.title = title;
    }

    public Category(String title, String uuID, int color, long id){
        this.uuID = uuID;
        this.title = title;
        this.color = color;
        this.id = id;
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

    public boolean isPinnedToSwipeLeft() {
        return mPinnedToSwipeLeft;
    }

    public void setPinnedToSwipeLeft(boolean pinedToSwipeLeft) {
        mPinnedToSwipeLeft = pinedToSwipeLeft;
    }
}
