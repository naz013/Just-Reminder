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

package com.cray.software.justreminder.datas;

import android.content.Context;
import android.database.Cursor;

import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.datas.models.TemplateModel;

import java.util.ArrayList;
import java.util.List;

public class TemplateDataProvider {
    private List<TemplateModel> data;
    private Context mContext;

    public TemplateDataProvider(Context mContext){
        data = new ArrayList<>();
        this.mContext = mContext;
        load();
    }

    public List<TemplateModel> getData(){
        return data;
    }

    public int getCount(){
        return data != null ? data.size() : 0;
    }

    public int getPosition(TemplateModel item){
        int res = -1;
        if (data.size() > 0) {
            for (int i = 0; i < data.size(); i++){
                TemplateModel item1 = data.get(i);
                if (item.getId() == item1.getId()) {
                    res = i;
                    break;
                }
            }
        }
        return res;
    }

    public TemplateModel getItem(int index) {
        if (index < 0 || index >= getCount()) {
            return null;
        }

        return data.get(index);
    }

    public void load() {
        data.clear();
        DataBase db = new DataBase(mContext);
        db.open();
        Cursor c = db.queryTemplates();
        if (c != null && c.moveToNext()) {
            do {
                String text = c.getString(c.getColumnIndex(Constants.COLUMN_TEXT));
                long id = c.getLong(c.getColumnIndex(Constants.COLUMN_ID));
                data.add(new TemplateModel(text, id));
            } while (c.moveToNext());
        }
        if (c != null) c.close();
        db.close();
    }
}
