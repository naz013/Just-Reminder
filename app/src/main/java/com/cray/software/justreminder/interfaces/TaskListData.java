package com.cray.software.justreminder.interfaces;

public class TaskListData {
    private String title;
    private long id;
    private String listId;
    private int color;

    public TaskListData(String title, long id, String listId, int color){
        this.title = title;
        this.id = id;
        this.listId = listId;
        this.color = color;
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
