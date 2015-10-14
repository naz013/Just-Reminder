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
    public enum Type{
        EDITABLE,
        READY,
        NEW
    }

    private String title;
    private int position;
    private boolean isChecked;
    private String uuId;
    private long id;
    private Type type;

    public ShoppingList(){
        this.type = Type.NEW;
        this.uuId = SyncHelper.generateID();
        this.title = "Add item";
    }

    public ShoppingList(Type type){
        this.type = type;
        this.uuId = SyncHelper.generateID();
        this.title = "";
    }

    public ShoppingList(String title, boolean isChecked, String uuId, long id, Type type){
        this.uuId = uuId;
        this.title = title;
        this.isChecked = isChecked;
        this.id = id;
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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
