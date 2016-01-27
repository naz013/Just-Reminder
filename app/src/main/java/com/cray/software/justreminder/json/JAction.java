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

package com.cray.software.justreminder.json;

import org.json.JSONException;
import org.json.JSONObject;

public class JAction {

    /**
     * JSON keys.
     */
    private static final String TYPE = "_type";
    private static final String SUBJECT = "subject";
    private static final String TARGET = "target";
    private static final String AUTO = "_auto";

    private String type, target, subject;
    private int auto;

    /**
     * JSON object.
     */
    private JSONObject jsonObject;

    public JAction(JSONObject jsonObject){
        if (jsonObject != null) {
            this.jsonObject = jsonObject;
            parse(jsonObject);
        }
    }

    public JAction(String object){
        if (object != null) {
            try {
                jsonObject = new JSONObject(object);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            parse(jsonObject);
        }
    }

    public JAction(){
        jsonObject = new JSONObject();
        setAuto(-1);
        setTarget(null);
        setType(null);
        setSubject(null);
    }

    public JAction(String type, String target, int auto, String subject){
        jsonObject = new JSONObject();
        setAuto(auto);
        setTarget(target);
        setType(type);
        setSubject(subject);
    }

    private void parse(JSONObject jsonObject) {
        if (jsonObject.has(TYPE)) {
            try {
                type = jsonObject.getString(TYPE);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if (jsonObject.has(TARGET)){
            try {
                target = jsonObject.getString(TARGET);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if (jsonObject.has(SUBJECT)){
            try {
                subject = jsonObject.getString(SUBJECT);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if (jsonObject.has(AUTO)){
            try {
                auto = jsonObject.getInt(AUTO);
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
    @Override
    public String toString(){
        if (jsonObject != null) return jsonObject.toString();
        else return null;
    }

    /**
     * Set current JSON object
     * @param jsonObject JSON object
     */
    public void setJsonObject(JSONObject jsonObject) {
        this.jsonObject = jsonObject;
    }

    public void setTarget(String target) {
        this.target = target;
        try {
            jsonObject.put(TARGET, target);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setSubject(String subject) {
        this.subject = subject;
        try {
            jsonObject.put(SUBJECT, subject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setType(String type) {
        this.type = type;
        try {
            jsonObject.put(TYPE, type);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setAuto(int auto) {
        this.auto = auto;
        try {
            jsonObject.put(AUTO, auto);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getSubject() {
        return subject;
    }

    public String getType() {
        return type;
    }

    public String getTarget() {
        return target;
    }

    public int getAuto() {
        return auto;
    }
}
