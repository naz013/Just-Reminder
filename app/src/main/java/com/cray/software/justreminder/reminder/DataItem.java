package com.cray.software.justreminder.reminder;

public class DataItem {
    private String title, type, uuId, number, weekdays, melody, categoryId;
    private int day, month, year, hour, minute, seconds, repCode, export,
            radius, color, code;
    private long id, repMinute, due;
    private double[] place;

    public DataItem(){

    }

    public DataItem(String title, String type, String weekdays, String melody, String categoryId,
                    String uuId, double[] place, String number, int day, int month, int year,
                    int hour, int minute, int seconds, int repCode, int export, int radius,
                    int color, int code, long repMinute, long due){
        this.title = title;
        this.type = type;
        this.weekdays = weekdays;
        this.melody = melody;
        this.categoryId = categoryId;
        this.uuId = uuId;
        this.place = place;
        this.number = number;
        this.day = day;
        this.month = month;
        this.year = year;
        this.hour = hour;
        this.minute = minute;
        this.seconds = seconds;
        this.repCode = repCode;
        this.export = export;
        this.radius = radius;
        this.color = color;
        this.code = code;
        this.repMinute = repMinute;
        this.due = due;
    }

    public String getWeekdays(){
        return weekdays;
    }

    public void setWeekdays(String weekdays){
        this.weekdays = weekdays;
    }

    public String getMelody(){
        return melody;
    }

    public void setMelody(String melody){
        this.melody = melody;
    }

    public String getCategoryId(){
        return categoryId;
    }

    public void setCategoryId(String categoryId){
        this.categoryId = categoryId;
    }

    public int getDay(){
        return day;
    }

    public void setDay(int day){
        this.day = day;
    }

    public int getMonth(){
        return month;
    }

    public void setMonth(int month){
        this.month = month;
    }

    public int getYear(){
        return year;
    }

    public void setYear(int year){
        this.year = year;
    }

    public int getHour(){
        return hour;
    }

    public void setHour(int hour){
        this.hour = hour;
    }

    public int getMinute(){
        return minute;
    }

    public int getSeconds(){
        return seconds;
    }

    public int getRepCode(){
        return repCode;
    }

    public int getExport(){
        return export;
    }

    public void setSeconds(int seconds){
        this.seconds = seconds;
    }

    public void setRepCode(int repCode){
        this.repCode = repCode;
    }

    public void setExport(int export){
        this.export = export;
    }

    public void setRepMinute(int repMinute){
        this.repMinute = repMinute;
    }

    public int getRadius(){
        return radius;
    }

    public void setRadius(int radius){
        this.radius = radius;
    }

    public int getColor(){
        return color;
    }

    public void setColor(int color){
        this.color = color;
    }

    public int getCode(){
        return code;
    }

    public void setCode(int code){
        this.code = code;
    }

    public long getRepMinute(){
        return repMinute;
    }

    public void setMinute(int minute){
        this.minute = minute;
    }

    public long getDue(){
        return due;
    }

    public void setDue(long due){
        this.due = due;
    }

    public double[] getPlace(){
        return place;
    }

    public void  setPlace(double[] place){
        this.place = place;
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

    public String getType(){
        return type;
    }

    public void setType(String type){
        this.type = type;
    }

    public String getUuId(){
        return uuId;
    }

    public void setUuId(String uuId){
        this.uuId = uuId;
    }

    public String getNumber(){
        return number;
    }

    public void setNumber(String number){
        this.number = number;
    }
}
