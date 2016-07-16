package com.cray.software.justreminder.groups;

import android.content.Context;

import com.cray.software.justreminder.databases.DataBase;

import java.util.List;

/**
 * Copyright 2016 Nazar Suhovich
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class GroupHelper {

    private static GroupHelper groupHelper;
    private Context mContext;

    private GroupHelper(Context context) {
        this.mContext = context;
    }

    public static GroupHelper getInstance(Context context) {
        if (groupHelper == null) {
            groupHelper = new GroupHelper(context);
        }
        return groupHelper;
    }

    /**
     * Change group indicator color.
     * @param id group identifier.
     * @param code indicator color code.
     */
    public void setNewIndicator(long id, int code){
        DataBase db = new DataBase(mContext);
        db.open();
        db.changeGroupColor(id, code);
        db.close();
    }

    public String getCategoryTitle(String id){
        DataBase db = new DataBase(mContext);
        db.open();
        String title = db.getGroup(id).getTitle();
        db.close();
        return title;
    }

    public String getDefaultUuId(){
        DataBase db = new DataBase(mContext);
        db.open();
        String uuId = db.getAllGroups().get(0).getUuId();
        db.close();
        return uuId;
    }

    public GroupItem getDefaultGroup(){
        DataBase db = new DataBase(mContext);
        db.open();
        GroupItem groupItem = db.getAllGroups().get(0);
        db.close();
        return groupItem;
    }

    public GroupItem getGroup(long id) {
        DataBase db = new DataBase(mContext);
        db.open();
        GroupItem item = db.getGroup(id);
        db.close();
        return item;
    }

    public GroupItem getGroup(String uuId) {
        DataBase db = new DataBase(mContext);
        db.open();
        GroupItem item = db.getGroup(uuId);
        db.close();
        return item;
    }

    public boolean deleteGroup(long id){
        DataBase db = new DataBase(mContext);
        db.open();
        boolean isDeleted = db.deleteGroup(id);
        db.close();
        return isDeleted;
    }

    public long saveGroup(GroupItem groupItem) {
        DataBase db = new DataBase(mContext);
        db.open();
        long id = db.setGroup(groupItem);
        db.close();
        return id;
    }

    public List<GroupItem> getAll() {
        DataBase db = new DataBase(mContext);
        db.open();
        List<GroupItem> list = db.getAllGroups();
        db.close();
        return list;
    }
}
