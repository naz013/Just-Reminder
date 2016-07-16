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

package com.cray.software.justreminder.groups;

import android.content.Context;

import com.cray.software.justreminder.databases.DataBase;

import java.util.ArrayList;
import java.util.List;

public class CategoryDataProvider {
    private List<GroupItem> data;
    private Context mContext;

    public CategoryDataProvider(Context mContext){
        data = new ArrayList<>();
        this.mContext = mContext;
        load();
    }

    public List<GroupItem> getData(){
        return data;
    }

    public int getCount(){
        return data != null ? data.size() : 0;
    }

    public int getPosition(GroupItem item){
        int res = -1;
        if (data.size() > 0) {
            for (int i = 0; i < data.size(); i++){
                GroupItem item1 = data.get(i);
                if (item.getUuId().matches(item1.getUuId())) {
                    res = i;
                    break;
                }
            }
        }
        return res;
    }

    public int getPosition(String uuId){
        int res = -1;
        if (data.size() > 0 && uuId != null) {
            for (int i = 0; i < data.size(); i++){
                GroupItem item = data.get(i);
                if (item.getUuId().matches(uuId)) {
                    res = i;
                    break;
                }
            }
        } else res = 0;
        return res;
    }

    public GroupItem getItem(int index) {
        if (index < 0 || index >= getCount()) {
            return null;
        }
        return data.get(index);
    }

    public void load() {
        data.clear();
        DataBase db = new DataBase(mContext);
        db.open();
        data = db.getAllGroups();
        db.close();
    }
}
