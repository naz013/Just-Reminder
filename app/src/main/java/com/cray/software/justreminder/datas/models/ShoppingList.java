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

import com.cray.software.justreminder.helpers.SyncHelper;

public class ShoppingList {

    private String title;
    private int position;

    /**
     * Task mark flag. 1 - completed, 0 - active.
     */
    private int isChecked;
    private String uuId;
    private long time;
    private int status;

    public static final int DELETED = -1;
    public static final int ACTIVE = 1;

    public ShoppingList(String title){
        this.uuId = SyncHelper.generateID();
        this.title = title;
        this.status = ACTIVE;
        this.isChecked = 0;
        this.time = System.currentTimeMillis();
    }

    public ShoppingList(String title, int isChecked, String uuId, int status, long time){
        this.uuId = uuId;
        this.title = title;
        this.isChecked = isChecked;
        this.status = status;
        this.time = time;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public int getIsChecked() {
        return isChecked;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getUuId() {
        return uuId;
    }

    public void setUuId(String uuId) {
        this.uuId = uuId;
    }

    public int isChecked() {
        return isChecked;
    }

    public void setIsChecked(int isChecked) {
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
