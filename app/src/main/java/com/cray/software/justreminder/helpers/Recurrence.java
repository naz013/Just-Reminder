package com.cray.software.justreminder.helpers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.List;

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
public class Recurrence {

    /**
     * JSON keys.
     */
    public static final String FROM_HOUR = "from_hour";
    public static final String TO_HOUR = "to_hour";
    public static final String HOURS = "hours";

    /**
     * JSON object.
     */
    private JSONObject jsonObject;

    public Recurrence(JSONObject jsonObject){
        this.jsonObject = jsonObject;
    }

    public Recurrence(String object){
        try {
            jsonObject = new JSONObject(object);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public Recurrence(){
        jsonObject = new JSONObject();
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

    /**
     * Add range exclusion to Timer.
     * @param fromHour start time.
     * @param toHour end time.
     */
    public void addExclusion(String fromHour, String toHour){
        try {
            jsonObject.put(FROM_HOUR, fromHour);
            jsonObject.put(TO_HOUR, toHour);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Add excluded hours to Timer.
     * @param hours list of excluded hours.
     */
    public void addExclusion(List<Integer> hours){
        JSONArray jsonArray = new JSONArray();
        for (int hour : hours) jsonArray.put(hour);
        try {
            jsonObject.put(HOURS, jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get excluded range start time.
     * @return time string
     */
    public String getFromHour(){
        if (jsonObject.has(FROM_HOUR)){
            try {
                return jsonObject.getString(FROM_HOUR);
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        } else return null;
    }

    /**
     * Get excluded range end time.
     * @return time string
     */
    public String getToHour(){
        if (jsonObject.has(TO_HOUR)){
            try {
                return jsonObject.getString(TO_HOUR);
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        } else return null;
    }

    /**
     * Get list of excluded hours from Timer.
     * @return list of hours.
     */
    public List<Integer> getHours(){
        if (jsonObject.has(HOURS)){
            Type collectionType = new TypeToken<List<Integer>>() {}.getType();
            try {
                return new Gson().fromJson(jsonObject.get(HOURS).toString(), collectionType);
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        } else return null;
    }
}
