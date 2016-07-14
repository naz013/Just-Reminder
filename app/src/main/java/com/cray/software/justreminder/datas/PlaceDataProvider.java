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

package com.cray.software.justreminder.datas;

import android.content.Context;
import android.database.Cursor;

import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.databases.NextBase;
import com.cray.software.justreminder.datas.models.MarkerModel;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.json.JParser;
import com.cray.software.justreminder.json.JPlace;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class PlaceDataProvider {
    private List<MarkerModel> data;
    private Context mContext;

    public PlaceDataProvider(Context mContext, boolean list){
        data = new ArrayList<>();
        this.mContext = mContext;
        if (list) {
            loadPlaces();
        } else {
            loadReminders();
        }
    }

    public List<MarkerModel> getData(){
        return data;
    }

    public int getCount(){
        return data != null ? data.size() : 0;
    }

    public int getPosition(MarkerModel item){
        int res = -1;
        if (data.size() > 0) {
            for (int i = 0; i < data.size(); i++){
                MarkerModel item1 = data.get(i);
                if (item.getId() == item1.getId()) {
                    res = i;
                    break;
                }
            }
        }
        return res;
    }

    public MarkerModel getItem(int index) {
        if (index < 0 || index >= getCount()) {
            return null;
        }

        return data.get(index);
    }

    private void loadReminders() {
        data.clear();
        NextBase db = new NextBase(mContext);
        int mRadius = SharedPrefs.getInstance(mContext).getInt(Prefs.LOCATION_RADIUS);
        db.open();
        Cursor c = db.queryAllLocations();
        if (c != null && c.moveToNext()) {
            do {
                String text = c.getString(c.getColumnIndex(NextBase.SUMMARY));
                long id = c.getLong(c.getColumnIndex(NextBase._ID));
                String json = c.getString(c.getColumnIndex(NextBase.JSON));
                int isDone = c.getInt(c.getColumnIndex(NextBase.DB_STATUS));
                int isArch = c.getInt(c.getColumnIndex(NextBase.DB_LIST));
                if (isArch == 0 && isDone == 0) {
                    JPlace jPlace = new JParser(json).getPlace();
                    double latitude = jPlace.getLatitude();
                    double longitude = jPlace.getLongitude();
                    int style = jPlace.getMarker();
                    int radius = jPlace.getRadius();
                    if (radius == -1) {
                        radius = mRadius;
                    }
                    data.add(new MarkerModel(text, new LatLng(latitude, longitude), style, id, radius));
                }
            } while (c.moveToNext());
        }
        if (c != null) {
            c.close();
        }
        db.close();
    }

    public void loadPlaces() {
        data.clear();
        DataBase db = new DataBase(mContext);
        db.open();
        Cursor c = db.queryPlaces();
        if (c != null && c.moveToNext()) {
            do {
                String text = c.getString(c.getColumnIndex(Constants.LocationConstants.COLUMN_LOCATION_NAME));
                long id = c.getLong(c.getColumnIndex(Constants.LocationConstants.COLUMN_ID));
                data.add(new MarkerModel(text, id));
            } while (c.moveToNext());
        }
        if (c != null) {
            c.close();
        }
        db.close();
    }

    public void load() {
        data.clear();
        DataBase db = new DataBase(mContext);
        db.open();
        Cursor c = db.queryPlaces();
        if (c != null && c.moveToNext()) {
            do {
                String text = c.getString(c.getColumnIndex(Constants.LocationConstants.COLUMN_LOCATION_NAME));
                long id = c.getLong(c.getColumnIndex(Constants.LocationConstants.COLUMN_ID));
                data.add(new MarkerModel(text, id));
            } while (c.moveToNext());
        }
        if (c != null) c.close();
        db.close();
    }
}
