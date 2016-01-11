package com.cray.software.justreminder.json;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
public class JsonParser {

    /**
     * JSON keys.
     */
    public static final String VOICE_NOTIFICATION = "voice_notification";
    public static final String AWAKE_SCREEN = "awake_screen";
    public static final String UNLOCK_SCREEN = "unlock_screen";
    public static final String EXCLUSION = "exclusion";
    public static final String RECURRENCE = "recurrence";
    public static final String START_DATE = "event_start";
    public static final String EXPORT = "export";
    public static final String COUNT = "count";
    public static final String ACTION = "action";
    public static final String SUMMARY = "summary";
    public static final String MELODY = "melody";
    public static final String VIBRATION = "vibration";
    public static final String CATEGORY = "category";
    public static final String NOTIFICATION_REPEAT = "notification_repeat";
    public static final String LED = "led";
    public static final String TAGS = "tags";
    public static final String PLACES = "places";
    public static final String UUID = "uuid";
    public static final String TYPE = "reminder_type";
    public static final String SHOPPING = "shopping";

    private JSONObject jsonObject;

    public JsonParser() {
        jsonObject = new JSONObject();
    }

    public JsonParser(String jsonObject) {
        try {
            this.jsonObject = new JSONObject(jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void parse(JsonModel model){
        model.setAction(getAction());
        model.setExport(getExport());
        model.setSummary(getSummary());
        model.setTags(getTags());
        model.setNotificationRepeat(getNotificationRepeat());
        model.setVoice(getVoice());
        model.setUnlock(getUnlock());
        model.setPlaces(getPlaces());
        model.setMelody(getMelody());
        model.setUuId(getUuid());
        model.setAwake(getAwake());
        model.setVibrate(getVibrate());
        model.setType(getType());
        model.setCategory(getCategory());
        model.setCount(getCount());
        model.setEventTime(getEventTime());
        model.setExclusion(getExclusion());
        model.setRecurrence(getRecurrence());
        model.setShoppings(getShoppings());
    }

    public void toJson(JsonModel model) {
        try {
            jsonObject.put(UUID, model.getUuId());
            jsonObject.put(SUMMARY, model.getSummary());
            jsonObject.put(TYPE, model.getType());
            jsonObject.put(START_DATE, model.getEventTime());
            jsonObject.put(COUNT, model.getCount());
            jsonObject.put(VIBRATION, model.getVibrate());
            jsonObject.put(NOTIFICATION_REPEAT, model.getNotificationRepeat());
            jsonObject.put(VOICE_NOTIFICATION, model.getVoice());
            jsonObject.put(AWAKE_SCREEN, model.getAwake());
            jsonObject.put(UNLOCK_SCREEN, model.getUnlock());
            jsonObject.put(CATEGORY, model.getCategory());
            if (model.getExport() != null) {
                jsonObject.put(EXPORT, model.getExport().getJsonObject());
            }

            if (model.getTags() != null && model.getTags().size() > 0) {
                JSONArray array = new JSONArray();
                for (String tag : model.getTags()) {
                    array.put(tag);
                }
                jsonObject.put(TAGS, array);
            }
            if (model.getRecurrence() != null) {
                jsonObject.put(RECURRENCE, model.getRecurrence().getJsonObject());
            }
            if (model.getMelody() != null) {
                jsonObject.put(MELODY, model.getMelody().getJsonObject());
            }
            if (model.getExclusion() != null) {
                jsonObject.put(EXCLUSION, model.getExclusion().getJsonObject());
            }
            if (model.getLed() != null) {
                jsonObject.put(LED, model.getLed().getJsonObject());
            }
            if (model.getAction() != null) {
                jsonObject.put(ACTION, model.getAction().getJsonObject());
            }
            if (model.getPlaces() != null && model.getPlaces().size() > 0) {
                JSONArray array = new JSONArray();
                for (JsonPlace place : model.getPlaces()) {
                    array.put(place.getJsonObject());
                }
                jsonObject.put(PLACES, array);
            }
            if (model.getShoppings() != null && model.getShoppings().size() > 0) {
                JSONArray array = new JSONArray();
                for (JsonShopping shopping : model.getShoppings()) {
                    array.put(shopping.getJsonObject());
                }
                jsonObject.put(SHOPPING, array);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setStartDate(long startDate) {
        try {
            jsonObject.put(START_DATE, startDate);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setUnlockScreen(int unlockScreen) {
        try {
            jsonObject.put(UNLOCK_SCREEN, unlockScreen);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setAwakeScreen(int awakeScreen) {
        try {
            jsonObject.put(AWAKE_SCREEN, awakeScreen);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setVoiceNotification(int voiceNotification) {
        try {
            jsonObject.put(VOICE_NOTIFICATION, voiceNotification);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setNotificationRepeat(int notificationRepeat) {
        try {
            jsonObject.put(NOTIFICATION_REPEAT, notificationRepeat);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setVibration(int vibration) {
        try {
            jsonObject.put(VIBRATION, vibration);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setType(String type) {
        try {
            jsonObject.put(TYPE, type);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setSummary(String summary) {
        try {
            jsonObject.put(SUMMARY, summary);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setCategory(String category) {
        try {
            jsonObject.put(CATEGORY, category);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setUuid(String uuid) {
        try {
            jsonObject.put(UUID, uuid);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setExport(JsonExport export) {
        try {
            jsonObject.put(EXPORT, export.getJsonObject());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setTags(List<String> tags) {
        JSONArray array = new JSONArray();
        for (String tag : tags) {
            array.put(tag);
        }
        try {
            jsonObject.put(TAGS, array);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setRecurrence(JsonRecurrence recurrence) {
        try {
            jsonObject.put(RECURRENCE, recurrence.getJsonObject());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setMelody(JsonMelody melody) {
        try {
            jsonObject.put(MELODY, melody.getJsonObject());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setExclusion(JsonExclusion exclusion) {
        try {
            jsonObject.put(EXCLUSION, exclusion.getJsonObject());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setLed(JsonLed led) {
        try {
            jsonObject.put(LED, led.getJsonObject());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setAction(JsonAction action) {
        try {
            jsonObject.put(ACTION, action.getJsonObject());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setPlace(JsonPlace jsonPlace) {
        JSONArray array = new JSONArray();
        array.put(jsonPlace);
        try {
            jsonObject.put(PLACES, array);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setPlaces(List<JsonPlace> list) {
        JSONArray array = new JSONArray();
        for (JsonPlace place : list) {
            array.put(place.getJsonObject());
        }
        try {
            jsonObject.put(PLACES, array);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setShopping(List<JsonShopping> list) {
        JSONObject array = new JSONObject();
        try {
            for (JsonShopping shopping : list) {
                array.put(shopping.getUuId(), shopping.getJsonObject());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            jsonObject.put(SHOPPING, array);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public List<JsonShopping> getShoppings() {
        if (jsonObject.has(SHOPPING)) {
            try {
                List<JsonShopping> places = new ArrayList<>();
                JSONObject object = jsonObject.getJSONObject(SHOPPING);
                Iterator<String> keys = object.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    places.add(new JsonShopping(object.getJSONObject(key)));
                }
                return places;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public JsonRecurrence getRecurrence() {
        if (jsonObject.has(RECURRENCE)) {
            try {
                JSONObject object = jsonObject.getJSONObject(RECURRENCE);
                return new JsonRecurrence(object);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public JsonExport getExport() {
        if (jsonObject.has(EXPORT)) {
            try {
                JSONObject object = jsonObject.getJSONObject(EXPORT);
                return new JsonExport(object);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public JsonExclusion getExclusion() {
        if (jsonObject.has(EXCLUSION)) {
            try {
                JSONObject object = jsonObject.getJSONObject(EXCLUSION);
                return new JsonExclusion(object);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public long getEventTime() {
        if (jsonObject.has(START_DATE)) {
            try {
                return jsonObject.getLong(START_DATE);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    public void setCount(long count) {
        if (jsonObject != null) {
            try {
                jsonObject.put(COUNT, count);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public long getCount() {
        if (jsonObject.has(COUNT)) {
            try {
                return jsonObject.getLong(COUNT);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    public String getCategory() {
        if (jsonObject.has(CATEGORY)) {
            try {
                return jsonObject.getString(CATEGORY);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public String getType() {
        if (jsonObject.has(TYPE)) {
            try {
                return jsonObject.getString(TYPE);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public int getVibrate() {
        if (jsonObject.has(VIBRATION)) {
            try {
                return jsonObject.getInt(VIBRATION);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return -1;
    }

    public int getAwake() {
        if (jsonObject.has(AWAKE_SCREEN)) {
            try {
                return jsonObject.getInt(AWAKE_SCREEN);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return -1;
    }

    public List<JsonPlace> getPlaces() {
        if (jsonObject.has(PLACES)) {
            try {
                List<JsonPlace> places = new ArrayList<>();
                JSONObject object = jsonObject.getJSONObject(PLACES);
                Iterator<String> keys = object.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    places.add(new JsonPlace(object.getJSONObject(key)));
                }
                return places;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public int getVoice() {
        if (jsonObject.has(VOICE_NOTIFICATION)) {
            try {
                return jsonObject.getInt(VOICE_NOTIFICATION);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return -1;
    }

    public int getUnlock() {
        if (jsonObject.has(UNLOCK_SCREEN)) {
            try {
                return jsonObject.getInt(UNLOCK_SCREEN);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return -1;
    }

    public String getUuid() {
        if (jsonObject.has(UUID)) {
            try {
                return jsonObject.getString(UUID);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public JsonMelody getMelody() {
        if (jsonObject.has(MELODY)) {
            try {
                JSONObject object = jsonObject.getJSONObject(MELODY);
                return new JsonMelody(object);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public int getNotificationRepeat() {
        if (jsonObject.has(NOTIFICATION_REPEAT)) {
            try {
                return jsonObject.getInt(NOTIFICATION_REPEAT);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return -1;
    }

    public JsonAction getAction() {
        if (jsonObject.has(ACTION)) {
            try {
                JSONObject object = jsonObject.getJSONObject(ACTION);
                return new JsonAction(object);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public List<String> getTags() {
        if (jsonObject.has(TAGS)){
            Type collectionType = new TypeToken<List<String>>() {}.getType();
            try {
                return new Gson().fromJson(jsonObject.get(TAGS).toString(), collectionType);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public String getSummary() {
        if (jsonObject.has(SUMMARY)) {
            try {
                return jsonObject.getString(SUMMARY);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public String getJSON() {
        return jsonObject.toString();
    }
}
