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

package com.cray.software.justreminder.reminder.json;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class JRecurrence {

    /**
     * JSON keys.
     */
    private static final String REPEAT = "repeat";
    private static final String AFTER = "after";
    private static final String WEEKDAYS = "weekdays";
    private static final String MONTHDAY = "month_day";
    private static final String LIMIT = "limit";

    private int monthday;
    private long repeat, limit, after;
    private List<Integer> weekdays = new ArrayList<>();

    /**
     * Get current JSON object.
     * @return JSON object string
     */
    @Override
    public String toString(){
        return "JRecurrence->Month day: " + monthday +
                "->Repeat: " + repeat +
                "->Limit: " + limit +
                "->After: " + after +
                "->Days: " + Arrays.asList(weekdays);
    }

    /**
     * JSON object.
     */
    private JSONObject jsonObject;

    public JRecurrence(JSONObject jsonObject){
        if (jsonObject != null) {
            this.jsonObject = jsonObject;
            parse(jsonObject);
        }
    }

    public JRecurrence(String object){
        if (object != null) {
            try {
                jsonObject = new JSONObject(object);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            parse(jsonObject);
        }
    }

    public JRecurrence(){
        jsonObject = new JSONObject();
        setMonthday(0);
        setRepeat(0);
        setLimit(-1);
        setWeekdays(null);
        setAfter(0);
    }

    public JRecurrence(int monthday, long repeat, long limit, List<Integer> weekdays, long after){
        jsonObject = new JSONObject();
        setMonthday(monthday);
        setRepeat(repeat);
        setLimit(limit);
        setWeekdays(weekdays);
        setAfter(after);
    }

    private void parse(JSONObject jsonObject) {
        if (jsonObject.has(REPEAT)) {
            try {
                repeat = jsonObject.getLong(REPEAT);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if (jsonObject.has(AFTER)) {
            try {
                after = jsonObject.getLong(AFTER);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if (jsonObject.has(LIMIT)) {
            try {
                limit = jsonObject.getLong(LIMIT);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if (jsonObject.has(MONTHDAY)){
            try {
                monthday = jsonObject.getInt(MONTHDAY);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if (jsonObject.has(WEEKDAYS)){
            Type collectionType = new TypeToken<List<Integer>>() {}.getType();
            try {
                weekdays = new Gson().fromJson(jsonObject.get(WEEKDAYS).toString(), collectionType);
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

    public void setLimit(long limit) {
        this.limit = limit;
        try {
            jsonObject.put(LIMIT, limit);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setMonthday(int monthday) {
        this.monthday = monthday;
        try {
            jsonObject.put(MONTHDAY, monthday);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setRepeat(long repeat) {
        this.repeat = repeat;
        try {
            jsonObject.put(REPEAT, repeat);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setAfter(long after) {
        this.after = after;
        try {
            jsonObject.put(AFTER, after);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setWeekdays(List<Integer> weekdays) {
        if (weekdays != null) {
            this.weekdays = weekdays;
            JSONArray jsonArray = new JSONArray();
            for (int day : weekdays) jsonArray.put(day);
            try {
                jsonObject.put(WEEKDAYS, jsonArray);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public long getAfter() {
        return after;
    }

    public List<Integer> getWeekdays() {
        return weekdays;
    }

    public int getMonthday() {
        return monthday;
    }

    public long getLimit() {
        return limit;
    }

    public long getRepeat() {
        return repeat;
    }
}
