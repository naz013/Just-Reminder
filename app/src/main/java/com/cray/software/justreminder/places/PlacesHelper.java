package com.cray.software.justreminder.places;

import android.content.Context;

import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.databases.NextBase;

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
public class PlacesHelper {

    private static PlacesHelper groupHelper;
    private Context mContext;

    private PlacesHelper(Context context) {
        this.mContext = context;
    }

    public static PlacesHelper getInstance(Context context) {
        if (groupHelper == null) {
            groupHelper = new PlacesHelper(context);
        }
        return groupHelper;
    }

    public PlaceItem getPlace(long id) {
        DataBase db = new DataBase(mContext);
        db.open();
        PlaceItem item = db.getPlace(id);
        db.close();
        return item;
    }

    public PlaceItem getPlace(String name) {
        DataBase db = new DataBase(mContext);
        db.open();
        PlaceItem item = db.getPlace(name);
        db.close();
        return item;
    }

    public boolean deletePlace(long id){
        DataBase db = new DataBase(mContext);
        db.open();
        boolean isDeleted = db.deletePlace(id);
        db.close();
        return isDeleted;
    }

    public long savePlace(PlaceItem placeItem) {
        DataBase db = new DataBase(mContext);
        db.open();
        long id = db.savePlace(placeItem);
        db.close();
        return id;
    }

    public List<PlaceItem> getAll() {
        DataBase db = new DataBase(mContext);
        db.open();
        List<PlaceItem> list = db.queryPlaces();
        db.close();
        return list;
    }

    public List<PlaceItem> getAllReminders() {
        NextBase db = new NextBase(mContext);
        db.open();
        List<PlaceItem> list = db.queryAllLocations();
        db.close();
        return list;
    }
}
