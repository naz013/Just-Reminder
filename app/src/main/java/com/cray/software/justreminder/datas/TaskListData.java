package com.cray.software.justreminder.datas;

import java.util.ArrayList;

public class TaskListData {
    private String title;
    private long id;
    private String listId;
    private int color;
    ArrayList<ListItems> mData;
    int position, current;

    public TaskListData(String title, long id, String listId, int color, ArrayList<ListItems> mData,
                        int position, int current){
        this.title = title;
        this.id = id;
        this.listId = listId;
        this.color = color;
        this.mData = mData;
        this.current = current;
        this.position = position;
    }

    public TaskListData(String title, long id, String listId, int color){
        this.title = title;
        this.id = id;
        this.listId = listId;
        this.color = color;
    }

    public int getPosition(){
        return position;
    }

    public void setPosition(int position){
        this.position = position;
    }

    public int getCurrent(){
        return current;
    }

    public void setCurrent(int current){
        this.current = current;
    }

    public ArrayList<ListItems> getmData(){
        return mData;
    }

    public void setmData(ArrayList<ListItems> mData){
        this.mData = mData;
    }

    public int getColor(){
        return color;
    }

    public void setColor(int color){
        this.color = color;
    }

    public String getTitle(){
        return title;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public long getId(){
        return id;
    }

    public void setId(long id){
        this.id = id;
    }

    public String getListId(){
        return listId;
    }

    public void setListId(String listId){
        this.listId = listId;
    }
}
