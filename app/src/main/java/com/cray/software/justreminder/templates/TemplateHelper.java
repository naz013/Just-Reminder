package com.cray.software.justreminder.templates;

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
public class TemplateHelper {

    private static TemplateHelper groupHelper;
    private Context mContext;

    private TemplateHelper(Context context) {
        this.mContext = context;
    }

    public static TemplateHelper getInstance(Context context) {
        if (groupHelper == null) {
            groupHelper = new TemplateHelper(context);
        }
        return groupHelper;
    }

    public TemplateItem getTemplate(long id) {
        DataBase db = new DataBase(mContext);
        db.open();
        TemplateItem item = db.getTemplate(id);
        db.close();
        return item;
    }

    public boolean deleteTemplate(long id){
        DataBase db = new DataBase(mContext);
        db.open();
        boolean isDeleted = db.deleteTemplate(id);
        db.close();
        return isDeleted;
    }

    public long saveTemplate(TemplateItem templateItem) {
        DataBase db = new DataBase(mContext);
        db.open();
        long id = db.saveTemplate(templateItem);
        db.close();
        return id;
    }

    public List<TemplateItem> getAll() {
        DataBase db = new DataBase(mContext);
        db.open();
        List<TemplateItem> list = db.queryTemplates();
        db.close();
        return list;
    }
}
