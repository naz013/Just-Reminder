/*
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

package com.cray.software.justreminder.reminder.json;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

public class JExclusion {

    /**
     * JSON keys.
     */
    private static final String FROM_HOUR = "from_hour";
    private static final String TO_HOUR = "to_hour";
    private static final String HOURS = "hours";

    private String from, to;
    private List<Integer> hours;

    /**
     * JSON object.
     */
    private JSONObject jsonObject;

    /**
     * Get current JSON object.
     * @return JSON object string
     */
    @Override
    public String toString(){
        return "JExclusion->From: " + from +
                "->To: " + to +
                "->Hours: " + Arrays.asList(hours);
    }

    public String getJsonString() {
        if (jsonObject != null) return jsonObject.toString();
        else return null;
    }

    public JExclusion(JSONObject jsonObject){
        if (jsonObject != null) {
            this.jsonObject = jsonObject;
            parse(jsonObject);
        }
    }

    public JExclusion(String object){
        if (object != null) {
            try {
                jsonObject = new JSONObject(object);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            parse(jsonObject);
        }
    }

    public JExclusion(){
        jsonObject = new JSONObject();
        addExclusion(null, null);
        addExclusion(null);
    }

    public JExclusion(String from, String to){
        jsonObject = new JSONObject();
        addExclusion(from, to);
    }

    private void parse(JSONObject jsonObject) {
        if (jsonObject.has(FROM_HOUR)) {
            try {
                from = jsonObject.getString(FROM_HOUR);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if (jsonObject.has(TO_HOUR)){
            try {
                to = jsonObject.getString(TO_HOUR);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if (jsonObject.has(HOURS)){
            Type collectionType = new TypeToken<List<Integer>>() {}.getType();
            try {
                hours = new Gson().fromJson(jsonObject.get(HOURS).toString(), collectionType);
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
        this.from = fromHour;
        this.to = toHour;
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
        if (hours != null) {
            this.hours = hours;
            JSONArray jsonArray = new JSONArray();
            for (int hour : hours) jsonArray.put(hour);
            try {
                jsonObject.put(HOURS, jsonArray);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Get excluded range start time.
     * @return time string
     */
    public String getFromHour(){
        return from;
    }

    /**
     * Get excluded range end time.
     * @return time string
     */
    public String getToHour(){
        return to;
    }

    /**
     * Get list of excluded hours from Timer.
     * @return list of hours.
     */
    public List<Integer> getHours(){
        return hours;
    }
}
