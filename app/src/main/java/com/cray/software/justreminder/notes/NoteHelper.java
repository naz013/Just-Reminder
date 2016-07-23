package com.cray.software.justreminder.notes;

import android.content.Context;

import java.util.ArrayList;
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
public class NoteHelper {

    private static NoteHelper groupHelper;
    private Context mContext;

    private NoteHelper(Context context) {
        this.mContext = context;
    }

    public static NoteHelper getInstance(Context context) {
        if (groupHelper == null) {
            groupHelper = new NoteHelper(context);
        }
        return groupHelper;
    }

    public NoteItem getNote(long id) {
        NotesBase db = new NotesBase(mContext);
        db.open();
        NoteItem item = db.getNote(id);
        db.close();
        return item;
    }

    public NoteItem getNoteByReminder(long reminderId) {
        NotesBase db = new NotesBase(mContext);
        db.open();
        NoteItem item = db.getNoteByReminder(reminderId);
        db.close();
        return item;
    }

    public void changeColor(long id, int color) {
        NotesBase db = new NotesBase(mContext);
        db.open();
        db.updateNoteColor(id, color);
        db.close();
    }

    public void linkReminder(long id, long reminderId) {
        NotesBase db = new NotesBase(mContext);
        db.open();
        db.linkToReminder(id, reminderId);
        db.close();
    }

    public NoteItem deleteNote(long id){
        NotesBase db = new NotesBase(mContext);
        db.open();
        NoteItem item = db.getNote(id);
        db.deleteNote(id);
        db.close();
        return item;
    }

    public long saveNote(NoteItem item) {
        if (item == null) return 0;
        NotesBase db = new NotesBase(mContext);
        db.open();
        long id = db.saveNote(item);
        db.close();
        return id;
    }

    public List<NoteItem> getAll() {
        NotesBase db = new NotesBase(mContext);
        db.open();
        List<NoteItem> list = db.getNotes();
        db.close();
        return list;
    }

    public long getCount() {
        NotesBase db = new NotesBase(mContext);
        db.open();
        List<NoteItem> list = db.getNotes();
        db.close();
        return list.size();
    }

    public List<String> getAllUuId() {
        List<String> ids = new ArrayList<>();
        for (NoteItem item : getAll()) {
            ids.add(item.getUuId());
        }
        return ids;
    }
}
