package com.cray.software.justreminder.datas;

public class ListItems {
    private String title;
    private String status;
    private String taskId;
    private String listId;
    private String note;
    private long date;
    private long id;

    public ListItems(String title, long id, String status, String taskId, long date, String listId, String note){
        this.title = title;
        this.id = id;
        this.status = status;
        this.date = date;
        this.taskId = taskId;
        this.listId = listId;
        this.note = note;
    }

    public String getNote(){
        return note;
    }

    public void setNote(String note){
        this.note = note;
    }

    public String getListId(){
        return listId;
    }

    public void setListId(String listId){
        this.listId = listId;
    }

    public long getId(){
        return id;
    }

    public void setId(long id){
        this.id = id;
    }

    public String getTitle(){
        return title;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public String getStatus(){
        return status;
    }

    public void setStatus(String status){
        this.status = status;
    }

    public String getTaskId(){
        return taskId;
    }

    public void setTaskId(String taskId){
        this.taskId = taskId;
    }

    public long getDate(){
        return date;
    }

    public void setDate(long date){
        this.date = date;
    }
}
