package com.cray.software.justreminder.reminder;

import android.content.Context;

import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.datas.models.ShoppingList;
import com.cray.software.justreminder.datas.ShoppingListDataProvider;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.services.AlarmReceiver;

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
        return new ShoppingListDataProvider(mContext, remId, ShoppingList.ACTIVE);
    }

    @Override
    public long save(Reminder item) {
        long id = super.save(item);
        startAlarm(id);
        return id;
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
                db.updateShopItem(item.getId(), item.getTitle(), item.isChecked());
            } else {
                db.addShopItem(item.getTitle(), item.getUuId(), remId, item.isChecked(), item.getTime());
            }
        }
        if (removedList != null) {
            for (ShoppingList list : removedList) {
                if (list.getId() != 0) {
                    db.updateShopItemStatus(list.getId(), ShoppingList.DELETED);
                }
            }
        }
        db.close();
    }

    @Override
    public void save(long id, Reminder item) {
        super.save(id, item);
        startAlarm(id);
    }

    private void startAlarm(long id) {
        new AlarmReceiver().setAlarm(mContext, id);
    }
}
