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
public class JsonShopping {

    /**
     * JSON keys.
     */
    private static final String SUMMARY = "summary";
    private static final String STATUS = "status_";

    private int status;
    private String summary;

    /**
     * JSON object.
     */
    private JSONObject jsonObject;

    public JsonShopping(JSONObject jsonObject){
        this.jsonObject = jsonObject;
        parse(jsonObject);
    }

    public JsonShopping(String object){
        try {
            jsonObject = new JSONObject(object);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        parse(jsonObject);
    }

    public JsonShopping(){
        jsonObject = new JSONObject();
    }

    private void parse(JSONObject jsonObject) {
        if (jsonObject.has(SUMMARY)) {
            try {
                summary = jsonObject.getString(SUMMARY);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if (jsonObject.has(STATUS)){
            try {
                status = jsonObject.getInt(STATUS);
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

    public void setStatus(int status) {
        this.status = status;
        try {
            jsonObject.put(STATUS, status);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setSummary(String summary) {
        this.summary = summary;
        try {
            jsonObject.put(SUMMARY, summary);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public int getStatus() {
        return status;
    }

    public String getSummary() {
        return summary;
    }
}
