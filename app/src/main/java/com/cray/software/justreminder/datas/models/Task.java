/**
 * Copyright 2016 Nazar Suhovich
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cray.software.justreminder.datas.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Task implements Parcelable {
    private String title;
    private String status;
    private String taskId;
    private String listId;
    private String note;
    private long date;
    private long id;
    private int color;

    public Task(String title, long id, String status, String taskId, long date, String listId,
                String note, int color){
        this.title = title;
        this.id = id;
        this.status = status;
        this.date = date;
        this.taskId = taskId;
        this.listId = listId;
        this.note = note;
        this.color = color;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
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

    public Task(Parcel in) {
        super();
        readFromParcel(in);
    }

    public static final Creator<Task> CREATOR = new Creator<Task>() {
        public Task createFromParcel(Parcel in) {
            return new Task(in);
        }

        public Task[] newArray(int size) {

            return new Task[size];
        }

    };

    public void readFromParcel(Parcel in) {
        title = in.readString();
        status = in.readString();
        note = in.readString();
        listId = in.readString();
        taskId = in.readString();
        id = in.readLong();
        date = in.readLong();
        color = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(status);
        dest.writeString(listId);
        dest.writeString(note);
        dest.writeString(taskId);
        dest.writeLong(id);
        dest.writeLong(date);
        dest.writeInt(color);
    }
}
