package com.cray.software.justreminder.datas;

public class WidgetItem {
    private int day, month, year;
    private boolean hasReminders, hasBirthdays;

    public WidgetItem(int day, int month, int year, boolean hasReminders, boolean hasBirthdays){
        this.day = day;
        this.month = month;
        this.year = year;
        this.hasReminders = hasReminders;
        this.hasBirthdays = hasBirthdays;
    }

    public boolean isHasBirthdays(){
        return hasBirthdays;
    }

    public void setHasBirthdays(boolean hasBirthdays){
        this.hasBirthdays = hasBirthdays;
    }

    public boolean isHasReminders(){
        return hasReminders;
    }

    public void setHasReminders(boolean hasReminders){
        this.hasReminders = hasReminders;
    }

    public int getYear(){
        return year;
    }

    public void setYear(int year){
        this.year = year;
    }

    public int getMonth(){
        return month;
    }

    public void setMonth(int month){
        this.month = month;
    }

    public int getDay(){
        return day;
    }

    public void setDay(int day){
        this.day = day;
    }
}
