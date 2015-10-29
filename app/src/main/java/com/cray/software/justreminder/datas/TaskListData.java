package com.cray.software.justreminder.datas;

import java.util.ArrayList;

public class TaskListData {
    private TaskList taskList;
    private ArrayList<Task> mData;
    private int position;

    public TaskListData(TaskList taskList, ArrayList<Task> mData,
                        int position){
        this.taskList = taskList;
        this.mData = mData;
        this.position = position;
    }

    public TaskListData(TaskList taskList){
        this.taskList = taskList;
    }

    public int getPosition(){
        return position;
    }

    public void setPosition(int position){
        this.position = position;
    }

    public ArrayList<Task> getmData(){
        return mData;
    }

    public void setmData(ArrayList<Task> mData){
        this.mData = mData;
    }

    public TaskList getTaskList() {
        return taskList;
    }

    public void setTaskList(TaskList taskList) {
        this.taskList = taskList;
    }
}
