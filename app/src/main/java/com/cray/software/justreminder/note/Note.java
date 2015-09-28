package com.cray.software.justreminder.note;

import android.content.Context;
import android.database.Cursor;

import com.cray.software.justreminder.helpers.Notifier;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.interfaces.Prefs;
import com.cray.software.justreminder.reminder.Telephony;
import com.cray.software.justreminder.widgets.UpdatesHelper;

import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

public class Note {

    private String note;
    private int color, style;
    private byte[] image;
    private long id;

    public Note(){}

    public Note(String note, int color, int style, byte[] image, long id){
        this.color = color;
        this.image = image;
        this.note = note;
        this.style = style;
        this.id = id;
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

    public static boolean shareNote(long id, Context context) {
        SyncHelper sHelp = new SyncHelper(context);
        NotesBase base = new NotesBase(context);
        base.open();
        boolean res = false;
        Cursor c = base.getNote(id);
        if (c != null && c.moveToFirst()) {
            String note = c.getString(c.getColumnIndex(Constants.COLUMN_NOTE));
            SharedPrefs sPrefs = new SharedPrefs(context);
            if (sPrefs.loadBoolean(Prefs.NOTE_ENCRYPT)) {
                note = sHelp.decrypt(note);
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
                    Telephony.sendMail(file, context);
                }
            } catch (JSONException | IOException e) {
                e.printStackTrace();
            }
        }
        return res;
    }

    public static void deleteNote(long id, Context context) {
        NotesBase DB = new NotesBase(context);
        DB.open();
        String uuId = null;
        Cursor c = DB.getNote(id);
        if (c != null && c.moveToFirst()){
            uuId = c.getString(c.getColumnIndex(Constants.COLUMN_UUID));
        }
        if (c != null) c.close();
        DB.deleteNote(id);
        DB.close();
        new DeleteNoteFilesAsync(context).execute(uuId);
        new UpdatesHelper(context).updateNotesWidget();
        new Notifier(context).discardStatusNotification(id);
    }
}
