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
public class Recurrance {

    public static final String FROM_HOUR = "from_hour";
    public static final String FROM_MINUTE = "from_minute";
    public static final String TO_HOUR = "to_minute";
    public static final String TO_MINUTE = "to_minute";
    public static final String HOURS = "hours";

    private JSONObject jsonObject;

    public Recurrance(JSONObject jsonObject){
        this.jsonObject = jsonObject;
    }

    public Recurrance(String object){
        try {
            jsonObject = new JSONObject(object);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public Recurrance (){
        jsonObject = new JSONObject();
    }

    public JSONObject getJsonObject() {
        return jsonObject;
    }

    public void setJsonObject(JSONObject jsonObject) {
        this.jsonObject = jsonObject;
    }

    public void addExclusionFrom(int fromHour, int fromMinute){
        try {
            jsonObject.put(FROM_HOUR, fromHour);
            jsonObject.put(FROM_MINUTE, fromMinute);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void addExclusionTo(int toHour, int toMinute){
        try {
            jsonObject.put(TO_HOUR, toHour);
            jsonObject.put(TO_MINUTE, toMinute);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void addExclusion(int[] hours){
        JSONArray jsonArray = new JSONArray();
        for (int hour : hours) jsonArray.put(hour);
        try {
            jsonObject.put(HOURS, jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public int getFromHour(){
        if (jsonObject.has(FROM_HOUR)){
            try {
                return jsonObject.getInt(FROM_HOUR);
            } catch (JSONException e) {
                e.printStackTrace();
                return -1;
            }
        } else return -1;
    }

    public int getFromMinute(){
        if (jsonObject.has(FROM_MINUTE)){
            try {
                return jsonObject.getInt(FROM_MINUTE);
            } catch (JSONException e) {
                e.printStackTrace();
                return -1;
            }
        } else return -1;
    }

    public int getToHour(){
        if (jsonObject.has(TO_HOUR)){
            try {
                return jsonObject.getInt(TO_HOUR);
            } catch (JSONException e) {
                e.printStackTrace();
                return -1;
            }
        } else return -1;
    }

    public int getToMinute(){
        if (jsonObject.has(TO_MINUTE)){
            try {
                return jsonObject.getInt(TO_MINUTE);
            } catch (JSONException e) {
                e.printStackTrace();
                return -1;
            }
        } else return -1;
    }

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
