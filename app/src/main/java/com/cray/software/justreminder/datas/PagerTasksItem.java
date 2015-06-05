package com.cray.software.justreminder.datas;

import java.util.ArrayList;

public class PagerTasksItem {
    int position, current;
    ArrayList<TaskListData> datas;

    public PagerTasksItem(ArrayList<TaskListData> datas, int position, int current){
        this.datas = datas;
        this.current = current;
        this.position = position;
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

    public ArrayList<TaskListData> getDatas(){
        return datas;
    }

    public void setDatas(ArrayList<TaskListData> datas){
        this.datas = datas;
    }
}
