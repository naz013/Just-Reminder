package com.cray.software.justreminder.groups;

import android.content.Context;

import com.cray.software.justreminder.databases.DataBase;

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

    /**
     * Change group indicator color.
     * @param context application context.
     * @param id group identifier.
     * @param code indicator color code.
     */
    public static void setNewIndicator(Context context, long id, int code){
        DataBase db = new DataBase(context);
        db.open();
        db.changeGroupColor(id, code);
        db.close();
    }

    public static String getCategoryTitle(Context context, String id){
        DataBase db = new DataBase(context);
        db.open();
        String title = db.getGroup(id).getTitle();
        db.close();
        return title;
    }

    public static String getDefaultUuId(Context context){
        DataBase db = new DataBase(context);
        db.open();
        String uuId = db.getAllGroups().get(0).getUuId();
        db.close();
        return uuId;
    }

    public static GroupItem getDefaultGroup(Context context){
        DataBase db = new DataBase(context);
        db.open();
        GroupItem groupItem = db.getAllGroups().get(0);
        db.close();
        return groupItem;
    }
}
