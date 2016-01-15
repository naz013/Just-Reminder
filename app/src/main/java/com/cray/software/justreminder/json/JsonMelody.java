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
public class JsonMelody {

    /**
     * JSON keys.
     */
    private static final String VOLUME = "melody";
    private static final String PATH = "path";

    private int volume;
    private String melodyPath;

    /**
     * JSON object.
     */
    private JSONObject jsonObject;

    public JsonMelody(JSONObject jsonObject){
        if (jsonObject != null) {
            this.jsonObject = jsonObject;
            parse(jsonObject);
        }
    }

    public JsonMelody(String object){
        if (object != null) {
            try {
                jsonObject = new JSONObject(object);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            parse(jsonObject);
        }
    }

    public JsonMelody(){
        jsonObject = new JSONObject();
        setMelodyPath(null);
        setVolume(-1);
    }

    public JsonMelody(String melodyPath, int volume){
        jsonObject = new JSONObject();
        setMelodyPath(melodyPath);
        setVolume(volume);
    }

    private void parse(JSONObject jsonObject) {
        if (jsonObject.has(VOLUME)) {
            try {
                volume = jsonObject.getInt(VOLUME);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if (jsonObject.has(PATH)){
            try {
                melodyPath = jsonObject.getString(PATH);
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

    public void setMelodyPath(String melodyPath) {
        this.melodyPath = melodyPath;
        try {
            jsonObject.put(PATH, melodyPath);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setVolume(int volume) {
        this.volume = volume;
        try {
            jsonObject.put(VOLUME, volume);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public int getVolume() {
        return volume;
    }

    public String getMelodyPath() {
        return melodyPath;
    }
}
