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

package com.cray.software.justreminder.notes;

import android.content.Context;
import android.database.Cursor;

import com.cray.software.justreminder.constants.Constants;

import java.util.ArrayList;
import java.util.List;

public class NoteDataProvider {
    private List<NoteModel> data;
    private Context mContext;

    public NoteDataProvider(Context mContext){
        data = new ArrayList<>();
        this.mContext = mContext;
        load();
    }

    public List<NoteModel> getData(){
        return data;
    }

    public int getCount(){
        return data != null ? data.size() : 0;
    }

    public int getPosition(NoteModel item){
        int res = -1;
        if (data.size() > 0) {
            for (int i = 0; i < data.size(); i++){
                NoteModel item1 = data.get(i);
                if (item.getId() == item1.getId()) {
                    res = i;
                    break;
                }
            }
        }
        return res;
    }

    public NoteModel getItem(int index) {
        if (index < 0 || index >= getCount()) {
            return null;
        }

        return data.get(index);
    }

    public void load() {
        data.clear();
        NotesBase db = new NotesBase(mContext);
        db.open();
        Cursor c = db.getNotes();
        if (c != null && c.moveToNext()) {
            do {
                String note = c.getString(c.getColumnIndex(Constants.COLUMN_NOTE));
                int color = c.getInt(c.getColumnIndex(Constants.COLUMN_COLOR));
                int style = c.getInt(c.getColumnIndex(Constants.COLUMN_FONT_STYLE));
                byte[] image = c.getBlob(c.getColumnIndex(Constants.COLUMN_IMAGE));
                long id = c.getLong(c.getColumnIndex(Constants.COLUMN_ID));
                data.add(new NoteModel(note, color, style, image, id));
            } while (c.moveToNext());
        }
        if (c != null) c.close();
        db.close();
    }
}
