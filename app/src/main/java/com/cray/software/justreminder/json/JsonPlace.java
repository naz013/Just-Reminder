package com.cray.software.justreminder.json;

import org.json.JSONException;
import org.json.JSONObject;

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
public class JsonPlace {

    /**
     * JSON keys.
     */
    private static final String LATITUDE = "latitude";
    private static final String LONGITUDE = "longitude";
    private static final String MARKER = "marker";
    private static final String RADIUS = "radius";

    private int radius, marker;
    private double latitude, longitude;

    /**
     * JSON object.
     */
    private JSONObject jsonObject;

    public JsonPlace(JSONObject jsonObject){
        if (jsonObject != null) {
            this.jsonObject = jsonObject;
            parse(jsonObject);
        }
    }

    public JsonPlace(String object){
        if (object != null) {
            try {
                jsonObject = new JSONObject(object);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            parse(jsonObject);
        }
    }

    public JsonPlace(){
        jsonObject = new JSONObject();
    }

    public JsonPlace(double latitude, double longitude, int radius, int marker){
        jsonObject = new JSONObject();
        setLatitude(latitude);
        setLongitude(longitude);
        setRadius(radius);
        setMarker(marker);
    }

    private void parse(JSONObject jsonObject) {
        if (jsonObject.has(MARKER)) {
            try {
                marker = jsonObject.getInt(MARKER);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if (jsonObject.has(RADIUS)) {
            try {
                radius = jsonObject.getInt(RADIUS);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if (jsonObject.has(LATITUDE)){
            try {
                latitude = jsonObject.getDouble(LATITUDE);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if (jsonObject.has(LONGITUDE)){
            try {
                longitude = jsonObject.getDouble(LONGITUDE);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Get current JSON object.
     * @return JSON object
     */
    public JSONObject getJsonObject() {
        return jsonObject;
    }

    /**
     * Get current JSON object.
     * @return JSON object string
     */
    public String getJsonString(){
        return jsonObject.toString();
    }

    /**
     * Set current JSON object
     * @param jsonObject JSON object
     */
    public void setJsonObject(JSONObject jsonObject) {
        this.jsonObject = jsonObject;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
        try {
            jsonObject.put(LATITUDE, latitude);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
        try {
            jsonObject.put(LONGITUDE, longitude);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setMarker(int marker) {
        this.marker = marker;
        try {
            jsonObject.put(MARKER, marker);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setRadius(int radius) {
        this.radius = radius;
        try {
            jsonObject.put(RADIUS, radius);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public int getRadius() {
        return radius;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public int getMarker() {
        return marker;
    }
}
