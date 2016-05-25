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

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.notes.DeleteNoteFilesAsync;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.notes.NotesBase;
import com.cray.software.justreminder.helpers.Messages;
import com.cray.software.justreminder.helpers.Notifier;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.helpers.Telephony;
import com.cray.software.justreminder.interfaces.NavigationCallbacks;
import com.cray.software.justreminder.app_widgets.UpdatesHelper;

import org.json.JSONException;

import java.io.File;
import java.util.Calendar;

public class NoteModel {

    private String note, uuId;
    private int color, style;
    private byte[] image;
    private long id;

    public NoteModel(String note, int color, int style, byte[] image, long id){
        this.color = color;
        this.image = image;
        this.note = note;
        this.style = style;
        this.id = id;
    }

    public NoteModel(String note, int color, int style, byte[] image, String uuId){
        this.color = color;
        this.image = image;
        this.note = note;
        this.style = style;
        this.uuId = uuId;
    }

    public String getUuId() {
        return uuId;
    }

    public String getNote(){
        return note;
    }

    public void setNote(String note){
        this.note = note;
    }

    public int getColor(){
        return color;
    }

    public void setColor(int color){
        this.color = color;
    }

    public int getStyle(){
        return style;
    }

    public void setStyle(int style){
        this.style = style;
    }

    public byte[] getImage(){
        return image;
    }

    public void setImage(byte[] image){
        this.image = image;
    }

    public long getId(){
        return id;
    }

    public void setId(long id){
        this.id = id;
    }

    /**
     * Create note file and send it to other users.
     * @param id note identifier.
     * @param context application context.
     * @return Boolean
     */
    public static boolean shareNote(long id, Context context) {
        SyncHelper sHelp = new SyncHelper(context);
        NotesBase db = new NotesBase(context);
        db.open();
        boolean res = false;
        Cursor c = db.getNote(id);
        if (c != null && c.moveToFirst()) {
            String note = c.getString(c.getColumnIndex(Constants.COLUMN_NOTE));
            SharedPrefs sPrefs = new SharedPrefs(context);
            if (sPrefs.loadBoolean(Prefs.NOTE_ENCRYPT)) {
                note = SyncHelper.decrypt(note);
            }
            Calendar calendar1 = Calendar.getInstance();
            int day = calendar1.get(Calendar.DAY_OF_MONTH);
            int month = calendar1.get(Calendar.MONTH);
            int year = calendar1.get(Calendar.YEAR);
            String date = year + "/" + month + "/" + day;

            int color = c.getInt(c.getColumnIndex(Constants.COLUMN_COLOR));
            int style = c.getInt(c.getColumnIndex(Constants.COLUMN_FONT_STYLE));
            byte[] imageByte = c.getBlob(c.getColumnIndex(Constants.COLUMN_IMAGE));
            String uuID = c.getString(c.getColumnIndex(Constants.COLUMN_UUID));
            if (uuID == null || uuID.matches("")) {
                uuID = SyncHelper.generateID();
            }

            try {
                File file = sHelp.createNote(note, date, uuID, color, imageByte, style);
                if (!file.exists() || !file.canRead()) {
                    return false;
                } else {
                    res = true;
                    Telephony.sendNote(file, context);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return res;
    }

    /**
     * Delete note from database and file from SDCard or Cloud.
     * @param id note identifier.
     * @param context application context.
     * @param callbacks callback for toast or snackbar message.
     */
    public static void deleteNote(long id, Context context, NavigationCallbacks callbacks) {
        NotesBase db = new NotesBase(context);
        db.open();
        String uuId = null;
        Cursor c = db.getNote(id);
        if (c != null && c.moveToFirst()){
            uuId = c.getString(c.getColumnIndex(Constants.COLUMN_UUID));
        }
        if (c != null) c.close();
        db.deleteNote(id);
        db.close();
        new DeleteNoteFilesAsync(context).execute(uuId);
        new UpdatesHelper(context).updateNotesWidget();
        new Notifier(context).discardStatusNotification(id);
        if (callbacks != null) callbacks.showSnackbar(context.getString(R.string.note_deleted));
        else Messages.toast(context, R.string.note_deleted);
    }

    /**
     * Change note color.
     * @param context application context.
     * @param id note identifier.
     * @param newColor new color code for note.
     */
    public static void setNewColor(Context context, long id, int newColor){
        NotesBase db = new NotesBase(context);
        db.open();
        db.updateNoteColor(id, newColor);
        db.close();
    }
}
