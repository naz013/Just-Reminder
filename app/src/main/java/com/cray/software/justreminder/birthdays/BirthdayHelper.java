package com.cray.software.justreminder.birthdays;

import android.content.Context;

import com.cray.software.justreminder.databases.DataBase;

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
public class BirthdayHelper {

    private static BirthdayHelper groupHelper;
    private Context mContext;

    private BirthdayHelper(Context context) {
        this.mContext = context;
    }

    public static BirthdayHelper getInstance(Context context) {
        if (groupHelper == null) {
            groupHelper = new BirthdayHelper(context);
        }
        return groupHelper;
    }

    public BirthdayItem getBirthday(long id) {
        DataBase db = new DataBase(mContext);
        db.open();
        BirthdayItem item = db.getBirthday(id);
        db.close();
        return item;
    }

    public void setShown(long id, String year) {
        DataBase db = new DataBase(mContext);
        db.open();
        db.setShown(id, year);
        db.close();
    }

    public void setUuid(long id, String uuId) {
        DataBase db = new DataBase(mContext);
        db.open();
        db.updateOtherInformationEvent(id, uuId);
        db.close();
    }

    public List<BirthdayItem> getBirthdays(int day, int month) {
        DataBase db = new DataBase(mContext);
        db.open();
        List<BirthdayItem> list = db.getBirthdays(day, month);
        db.close();
        return list;
    }

    public boolean deleteBirthday(long id){
        DataBase db = new DataBase(mContext);
        db.open();
        boolean isDeleted = db.deleteBirthday(id);
        db.close();
        return isDeleted;
    }

    public long saveBirthday(BirthdayItem item) {
        if (item == null) return 0;
        DataBase db = new DataBase(mContext);
        db.open();
        long id = db.saveBirthday(item);
        db.close();
        return id;
    }

    public List<String> getNames() {
        List<String> list = new ArrayList<>();
        for (BirthdayItem item : getAll()) {
            list.add(item.getName());
        }
        return list;
    }

    public List<String> getNumbers() {
        List<String> list = new ArrayList<>();
        for (BirthdayItem item : getAll()) {
            if (item.getNumber() != null) list.add(item.getNumber());
        }
        return list;
    }

    public List<Integer> getContacts() {
        List<Integer> list = new ArrayList<>();
        for (BirthdayItem item : getAll()) {
            if (item.getContactId() != 0) list.add(item.getContactId());
        }
        return list;
    }

    public List<BirthdayItem> getAll() {
        DataBase db = new DataBase(mContext);
        db.open();
        List<BirthdayItem> list = db.getBirthdays();
        db.close();
        return list;
    }
}
