package com.cray.software.justreminder.reminder;

public class DataItem {
    private String title, type, uuId, number, weekdays, melody, categoryId;
    private int day, month, year, hour, minute, seconds, repCode, export,
            radius, color, code;
    private long id, repMinute, due;
    private double[] place;

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

    public String getMelody(){
        return melody;
    }

    public String getCategoryId(){
        return categoryId;
    }

    public int getDay(){return day;}
    public int getMonth(){return month;}
    public int getYear(){return year;}
    public int getHour(){return hour;}
    public int getMinute(){return minute;}
    public int getSeconds(){return seconds;}
    public int getRepCode(){return repCode;}
    public int getExport(){return export;}
    public int getRadius(){return radius;}
    public int getColor(){return color;}
    public int getCode(){return code;}

    public long getRepMinute(){return repMinute;}
    public long getDue(){return due;}

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
