package com.cray.software.justreminder.reminder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.databases.DataBase;

public class ReminderType {

    private Context context;

    public ReminderType(Context context){
        this.context = context;
    }

    public View inflateView(int layout, ViewGroup container, LayoutInflater inflater){
        return inflater.inflate(layout, container, false);
    }

    public long save(DataItem item){
        DataBase db = new DataBase(context);
        db.open();
        long id = db.insertReminder(item.getTitle(), item.getType(), item.getDay(), item.getMonth(),
                item.getYear(), item.getHour(), item.getMinute(), item.getSeconds(),
                item.getNumber(), item.getRepCode(), item.getRepMinute(), 0, item.getPlace()[0],
                item.getPlace()[1], item.getUuId(), item.getWeekdays(), item.getExport(),
                item.getMelody(), item.getRadius(), item.getColor(), item.getCode(),
                item.getCategoryId());
        db.updateReminderDateTime(id);
        db.close();
        return id;
    }

    public void save(long id, DataItem item){
        DataBase db = new DataBase(context);
        db.open();
        db.updateReminder(id, item.getTitle(), item.getType(), item.getDay(), item.getMonth(),
                item.getYear(), item.getHour(), item.getMinute(), item.getSeconds(),
                item.getNumber(), item.getRepCode(), item.getRepMinute(), 0, item.getPlace()[0],
                item.getPlace()[1], item.getWeekdays(), item.getExport(), item.getMelody(),
                item.getRadius(), item.getColor(), item.getCode(), item.getCategoryId());
        db.updateReminderDateTime(id);
        db.close();
    }
}
