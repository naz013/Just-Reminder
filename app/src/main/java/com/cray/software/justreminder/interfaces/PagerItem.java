package com.cray.software.justreminder.interfaces;

import java.util.ArrayList;

public class PagerItem {
    int position, current;
    ArrayList<CalendarData> datas;
    int day;

    public PagerItem(ArrayList<CalendarData> datas, int position, int current, int day){
        this.datas = datas;
        this.current = current;
        this.position = position;
        this.day = day;
    }

    public int getDay(){
        return day;
    }

    public void setDay(int day){
        this.day = day;
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

    public ArrayList<CalendarData> getDatas(){
        return datas;
    }

    public void setDatas(ArrayList<CalendarData> datas){
        this.datas = datas;
    }
}
