package com.cray.software.justreminder.reminder;

import android.content.Context;
import android.database.Cursor;

import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.databases.NextBase;
import com.cray.software.justreminder.datas.models.ReminderModel;
import com.cray.software.justreminder.json.JsonModel;
import com.cray.software.justreminder.json.JsonParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReminderDataProvider {
    private List<ReminderModel> data;
    private Context mContext;
    private ReminderModel mLastRemovedData;
    private int mLastRemovedPosition = -1;
    public static final int VIEW_REMINDER = 15666;
    public static final int VIEW_SHOPPING_LIST = 15667;
    private boolean isArchive = false;

    public ReminderDataProvider(Context mContext){
        data = new ArrayList<>();
        this.mContext = mContext;
    }

    public ReminderDataProvider(Context mContext, boolean isArchive){
        data = new ArrayList<>();
        this.mContext = mContext;
        this.isArchive = isArchive;
        load();
    }

    public List<ReminderModel> getData(){
        return data;
    }

    public int getCount(){
        return data != null ? data.size() : 0;
    }

    public int getPosition(ReminderModel item){
        int res = -1;
        if (data.size() > 0) {
            for (int i = 0; i < data.size(); i++){
                ReminderModel item1 = data.get(i);
                if (item.getId() == item1.getId()) {
                    res = i;
                    break;
                }
            }
        }
        return res;
    }

    public int removeItem(ReminderModel item){
        int res = 0;
        if (data.size() > 0) {
            for (int i = 0; i < data.size(); i++){
                ReminderModel item1 = data.get(i);
                if (item.getId() == item1.getId()) {
                    data.remove(i);
                    res = i;
                    break;
                }
            }
        }
        return res;
    }

    public void removeItem(int position){
        mLastRemovedData = data.remove(position);
        mLastRemovedPosition = position;
    }

    public void moveItem(int from, int to){
        if (to < 0 || to >= getCount()) {
            throw new IndexOutOfBoundsException("index = " + to);
        }

        if (from == to) {
            return;
        }

        final ReminderModel item = data.remove(from);

        data.add(to, item);
        mLastRemovedPosition = -1;
    }

    public int undoLastRemoval() {
        if (mLastRemovedData != null) {
            int insertedPosition;
            if (mLastRemovedPosition >= 0 && mLastRemovedPosition < data.size()) {
                insertedPosition = mLastRemovedPosition;
            } else {
                insertedPosition = data.size();
            }

            data.add(insertedPosition, mLastRemovedData);

            mLastRemovedData = null;
            mLastRemovedPosition = -1;

            return insertedPosition;
        } else {
            return -1;
        }
    }

    public ReminderModel getItem(int index) {
        if (index < 0 || index >= getCount()) {
            return null;
        }

        return data.get(index);
    }

    public void load(){
        data.clear();
        NextBase db = new NextBase(mContext);
        db.open();
        Map<String, Integer> map = getCategories(mContext);
        Cursor c = isArchive ? db.getArchivedReminders() : db.getActiveReminders();
        if (c != null && c.moveToNext()){
            do {
                String json = c.getString(c.getColumnIndex(NextBase.JSON));
                String type = c.getString(c.getColumnIndex(NextBase.TYPE));
                String categoryId = c.getString(c.getColumnIndex(NextBase.CATEGORY));
                int archived = c.getInt(c.getColumnIndex(NextBase.DB_LIST));
                int completed = c.getInt(c.getColumnIndex(NextBase.DB_STATUS));
                long id = c.getLong(c.getColumnIndex(NextBase._ID));

                int viewType = VIEW_REMINDER;
                if (type.matches(Constants.TYPE_SHOPPING_LIST)) viewType = VIEW_SHOPPING_LIST;

                int catColor = 0;
                if (map.containsKey(categoryId)) catColor = map.get(categoryId);

                JsonModel jsonModel = new JsonParser(json).parse();
                data.add(new ReminderModel(id, jsonModel, catColor, archived, completed, viewType));
            } while (c.moveToNext());
        }
        if (c != null) c.close();
        db.close();
    }

    public static Map<String, Integer> getCategories(Context context) {
        Map<String, Integer> map = new HashMap<>();
        DataBase db = new DataBase(context);
        db.open();
        Cursor cf = db.queryCategories();
        if (cf != null && cf.moveToFirst()){
            do {
                String uuid = cf.getString(cf.getColumnIndex(Constants.COLUMN_TECH_VAR));
                int color = cf.getInt(cf.getColumnIndex(Constants.COLUMN_COLOR));
                map.put(uuid, color);
            } while (cf.moveToNext());
        }
        if (cf != null) cf.close();
        db.close();
        return map;
    }

    public static ReminderModel getItem(Context mContext, long id){
        ReminderModel item = null;
        NextBase db = new NextBase(mContext);
        db.open();
        Map<String, Integer> map = getCategories(mContext);
        Cursor c = db.getReminder(id);
        if (c != null && c.moveToNext()){
            String json = c.getString(c.getColumnIndex(NextBase.JSON));
            String type = c.getString(c.getColumnIndex(NextBase.TYPE));
            String categoryId = c.getString(c.getColumnIndex(NextBase.CATEGORY));
            int archived = c.getInt(c.getColumnIndex(NextBase.DB_LIST));
            int completed = c.getInt(c.getColumnIndex(NextBase.DB_STATUS));

            int viewType = VIEW_REMINDER;
            if (type.matches(Constants.TYPE_SHOPPING_LIST)) viewType = VIEW_SHOPPING_LIST;

            int catColor = 0;
            if (map.containsKey(categoryId)) catColor = map.get(categoryId);

            JsonModel jsonModel = new JsonParser(json).parse();
            item = new ReminderModel(id, jsonModel, catColor, archived, completed, viewType);
        }
        if (c != null) c.close();
        db.close();
        return item;
    }
}
