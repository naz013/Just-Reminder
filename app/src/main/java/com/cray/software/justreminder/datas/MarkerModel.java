package com.cray.software.justreminder.datas;

import com.google.android.gms.maps.model.LatLng;

/**
 * Copyright 2015 Nazar Suhovich
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
public class MarkerModel {
    private String title;
    private LatLng position;
    private int icon;
    private long id;

    public MarkerModel(String title, long id){
        this.title = title;
        this.id = id;
    }

    public MarkerModel(String title, LatLng position, int icon, long id){
        this.position = position;
        this.title = title;
        this.icon = icon;
        this.id = id;
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
