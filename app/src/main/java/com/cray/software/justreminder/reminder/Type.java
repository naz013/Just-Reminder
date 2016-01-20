package com.cray.software.justreminder.reminder;

import android.content.Context;
import android.database.Cursor;

import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.databases.NextBase;
import com.cray.software.justreminder.helpers.Notifier;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.json.JsonExport;
import com.cray.software.justreminder.json.JsonModel;
import com.cray.software.justreminder.json.JsonParser;
import com.cray.software.justreminder.widgets.utils.UpdatesHelper;

public class Type {

    private Context mContext;
    private int view;
    private String type;

    public Type(Context context){
        this.mContext = context;
        this.type = "";
    }

    /**
     * Inflate layout file for reminder.
     * @param view layout resource identifier.
     */
    public void inflateView(int view){
        this.view = view;
    }

    /**
     * Get reminder object.
     * @param id reminder identifier.
     * @return reminder object
     */
    public JsonModel getItem(long id){
        NextBase db = new NextBase(mContext);
        db.open();
        Cursor c = db.getReminder(id);
        if (c != null && c.moveToFirst()){
            String json = c.getString(c.getColumnIndex(NextBase.JSON));
            c.close();
            db.close();
            return new JsonParser(json).parse();
        }

        if (c != null) c.close();
        db.close();
        return null;
    }

    /**
     * Get reminder object.
     * @param uuId reminder unique identifier.
     * @return reminder object
     */
    public JsonModel getItem(String uuId){
        NextBase db = new NextBase(mContext);
        db.open();
        Cursor c = db.getReminder(uuId);
        if (c != null && c.moveToFirst()){
            String json = c.getString(c.getColumnIndex(NextBase.JSON));
            c.close();
            db.close();
            return new JsonParser(json).parse();
        }
        if (c != null) c.close();
        db.close();
        return null;
    }

    /**
     * Get reminder layout resource identifier.
     * @return reminder layout id
     */
    public int getView(){
        return view;
    }

    /**
     * Set reminder type.
     * @param type reminder type.
     */
    public void setType(String type){
        this.type = type;
    }

    /**
     * Get reminder type.
     * @return reminder type
     */
    public String getType(){
        return type;
    }

    /**
     * Save new reminder to database.
     * @param item reminder object.
     * @return reminder identifier
     */
    public long save(JsonModel item){
        NextBase db = new NextBase(mContext);
        db.open();
        JsonParser jsonParser = new JsonParser();
        jsonParser.toJsonString(item);
        long id = db.insertReminder(item.getSummary(), item.getType(), item.getEventTime(), item.getUuId(),
                item.getCategory(), jsonParser.toJsonString());
        db.close();
        updateViews();
        return id;
    }

    /**
     * Update reminder in database.
     * @param id reminder identifier.
     * @param item reminder object.
     */
    public void save(long id, JsonModel item){
        NextBase db = new NextBase(mContext);
        db.open();
        JsonParser jsonParser = new JsonParser();
        jsonParser.toJsonString(item);
        db.updateReminder(id, item.getSummary(), item.getType(), item.getEventTime(), item.getUuId(),
                item.getCategory(), jsonParser.toJsonString());
        db.close();
        updateViews();
    }

    /**
     * Add reminder to Google, Stock Calendar and/or Google Tasks.
     * @param item reminder object.
     * @param id reminder identifier.
     */
    protected void exportToServices(JsonModel item, long id){
        long due = item.getEventTime();
        if (due > 0) {
            SharedPrefs prefs = new SharedPrefs(mContext);
            boolean stock = prefs.loadBoolean(Prefs.EXPORT_TO_STOCK);
            boolean calendar = prefs.loadBoolean(Prefs.EXPORT_TO_CALENDAR);
            JsonExport jsonExport = item.getExport();
            if (jsonExport.getCalendar() == 1) ReminderUtils.exportToCalendar(mContext,
                    item.getSummary(), due, id, calendar, stock);
            if (jsonExport.getgTasks() == Constants.SYNC_GTASKS_ONLY)
                ReminderUtils.exportToTasks(mContext, item.getSummary(), due, id);
        }
    }

    /**
     * Update all application widgets and permanent notification in Status Bar.
     */
    private void updateViews(){
        new Notifier(mContext).recreatePermanent();
        new UpdatesHelper(mContext).updateWidget();
    }
}
