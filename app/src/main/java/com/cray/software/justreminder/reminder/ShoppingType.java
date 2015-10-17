package com.cray.software.justreminder.reminder;

import android.content.Context;
import android.util.Log;

import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.datas.ShoppingList;
import com.cray.software.justreminder.datas.ShoppingListDataProvider;
import com.cray.software.justreminder.interfaces.Constants;

import java.util.ArrayList;

public class ShoppingType extends Type {

    private Context mContext;

    public ShoppingType(Context context) {
        super(context);
        this.mContext = context;
        setType(Constants.TYPE_SHOPPING_LIST);
    }

    /**
     * Get reminder tasks provider.
     * @param remId reminder identifier.
     * @return ShoppingListDataProvider
     */
    public ShoppingListDataProvider getProvider(long remId){
        return new ShoppingListDataProvider(mContext, remId);
    }

    @Override
    public long save(Reminder item) {
        return super.save(item);
    }

    /**
     * Save tasks for selected reminder.
     * @param remId reminder identifier.
     * @param newList list of tasks that will be added to database.
     * @param removedList list of tasks that will be deleted from database.
     */
    public void saveShopList(long remId, ArrayList<ShoppingList> newList, ArrayList<ShoppingList> removedList){
        DataBase db = new DataBase(mContext);
        db.open();
        for (ShoppingList item : newList){
            if (item.getId() != 0){
                db.updateShopItem(item.getId(), item.getTitle(), item.getDateTime());
            } else {
                db.addShopItem(item.getTitle(), item.getDateTime(), item.getUuId(), remId);
            }
            Log.d(Constants.LOG_TAG, "Saved with rem id " + remId);
        }
        if (removedList != null) {
            for (ShoppingList list : removedList) {
                if (list.getId() != 0) {
                    db.deleteShopItem(list.getId());
                }
                Log.d(Constants.LOG_TAG, "Removed item id " + list.getId());
            }
        }
        db.close();
    }

    @Override
    public void save(long id, Reminder item) {
        super.save(id, item);
    }
}
