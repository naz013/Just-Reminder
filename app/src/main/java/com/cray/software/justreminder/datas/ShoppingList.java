package com.cray.software.justreminder.datas;

import com.cray.software.justreminder.helpers.SyncHelper;

/**
 * Copyright 2015 Nazar Suhovich
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
public class ShoppingList {

    private String title;
    private int position;
    private boolean isChecked;
    private String uuId;
    private long remId, dateTime, id;

    public ShoppingList(String title, long dateTime){
        this.uuId = SyncHelper.generateID();
        this.title = title;
        this.dateTime = dateTime;
        this.isChecked = false;
    }

    public ShoppingList(long id, String title, boolean isChecked, String uuId, long remId){
        this.uuId = uuId;
        this.title = title;
        this.isChecked = isChecked;
        this.remId = remId;
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getDateTime() {
        return dateTime;
    }

    public void setDateTime(long dateTime) {
        this.dateTime = dateTime;
    }

    public long getRemId() {
        return remId;
    }

    public void setRemId(long remId) {
        this.remId = remId;
    }

    public String getUuId() {
        return uuId;
    }

    public void setUuId(String uuId) {
        this.uuId = uuId;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setIsChecked(boolean isChecked) {
        this.isChecked = isChecked;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
