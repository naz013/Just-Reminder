/*
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

package com.cray.software.justreminder.datas.models;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class PlaceModel {
    private String name, id, icon, address;
    private LatLng position;
    private int selected;
    private ArrayList<String> types;

    public PlaceModel() {
        selected = 0;
    }

    public PlaceModel(String name, String id, String icon, String address,
                      LatLng position, ArrayList<String> types){
        this.name = name;
        this.id = id;
        this.icon = icon;
        this.address = address;
        this.position = position;
        this.types = types;
        selected = 0;
    }

    public ArrayList<String> getTypes() {
        return types;
    }

    public void setTypes(ArrayList<String> types) {
        this.types = types;
    }

    public void setSelected(int selected) {
        this.selected = selected;
    }

    public int getSelected() {
        return selected;
    }

    public LatLng getPosition() {
        return position;
    }

    public String getAddress() {
        return address;
    }

    public String getIcon() {
        return icon;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPosition(LatLng position) {
        this.position = position;
    }
}
