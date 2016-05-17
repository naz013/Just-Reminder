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

public class TaskList implements Parcelable {
    private String title;
    private String listId;
    private long id;
    private int color;

    public TaskList(String title, long id, String listId,  int color){
        this.title = title;
        this.id = id;
        this.listId = listId;
        this.color = color;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
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

    public TaskList(Parcel in) {
        super();
        readFromParcel(in);
    }

    public static final Creator<TaskList> CREATOR = new Creator<TaskList>() {
        public TaskList createFromParcel(Parcel in) {
            return new TaskList(in);
        }

        public TaskList[] newArray(int size) {

            return new TaskList[size];
        }

    };

    public void readFromParcel(Parcel in) {
        title = in.readString();
        listId = in.readString();
        id = in.readLong();
        color = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(listId);
        dest.writeLong(id);
        dest.writeInt(color);
    }
}
