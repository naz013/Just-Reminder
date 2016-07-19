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

package com.cray.software.justreminder.places;

import com.google.android.gms.maps.model.LatLng;

public class PlaceItem {
    private String title;
    private LatLng position;
    private int icon, radius;
    private long id;

    public PlaceItem(String title, LatLng latLng, long id){
        this.title = title;
        this.id = id;
        this.position = latLng;
    }

    public PlaceItem(String title, LatLng position, int icon, long id, int radius){
        this.position = position;
        this.title = title;
        this.icon = icon;
        this.id = id;
        this.radius = radius;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setPosition(LatLng position) {
        this.position = position;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getRadius() {
        return radius;
    }

    public long getId() {
        return id;
    }

    public String getTitle(){
        return title;
    }

    public LatLng getPosition(){
        return position;
    }

    public int getIcon(){
        return icon;
    }
}
