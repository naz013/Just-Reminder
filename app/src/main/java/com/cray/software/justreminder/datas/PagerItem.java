package com.cray.software.justreminder.datas;

import java.util.ArrayList;

public class PagerItem {
    int position, current;
    ArrayList<CalendarData> datas;
    int day, month, year;

    public PagerItem(ArrayList<CalendarData> datas, int position, int current, int day){
        this.datas = datas;
        this.current = current;
        this.position = position;
        this.day = day;
    }

    public PagerItem(ArrayList<CalendarData> datas, int position, int current, int day, int month, int year){
        this.datas = datas;
        this.current = current;
        this.position = position;
        this.day = day;
        this.month = month;
        this.year = year;
    }

    public int getMonth(){
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear(){
        return year;
    }

    public void setYear(int year){
        this.year = year;
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
