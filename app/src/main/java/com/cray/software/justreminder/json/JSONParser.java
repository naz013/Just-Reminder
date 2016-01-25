package com.cray.software.justreminder.json;

import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.datas.models.ShoppingList;
import com.cray.software.justreminder.helpers.TimeCount;
import com.cray.software.justreminder.reminder.ReminderUtils;
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
    public static final String EVENT_TIME = "event_time";
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
    public static final String PLACE = "place";
    public static final String UUID = "uuid";
    public static final String TYPE = "reminder_type";
    public static final String SHOPPING = "shopping";


    public static final String VERSION = "1.0";
    public static final String VERSION_KEY = "v_key";

    private JSONObject jsonObject;

    public JsonParser() {
        jsonObject = new JSONObject();
        setVersion();
    }

    public JsonParser(String jsonObject) {
        try {
            this.jsonObject = new JSONObject(jsonObject);
            setVersion();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public JsonParser(JSONObject jsonObject) {
        if (jsonObject != null) {
            this.jsonObject = jsonObject;
            setVersion();
        }
    }

    private void setVersion() {
        if (jsonObject != null) {
            try {
                jsonObject.put(VERSION_KEY, VERSION);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public JsonModel parse(){
        if (jsonObject.has(Constants.COLUMN_TEXT)) {
            try {
                return modelFromOld();
            } catch (JSONException e) {
                return null;
            }
        } else {
            long start = System.currentTimeMillis();
            JsonModel model = new JsonModel();
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
            model.setStartTime(getStartTime());
            model.setExclusion(getExclusion());
            model.setRecurrence(getRecurrence());
            model.setShoppings(getShoppings());
            model.setLed(getLed());
            model.setPlace(getPlace());
            //Log.d("------TIME------", "Parsing time - " + (System.currentTimeMillis() - start));
            return model;
        }
    }

    private JsonModel modelFromOld() throws JSONException {
        long start = System.currentTimeMillis();
        String text = null;
        if (!jsonObject.isNull(Constants.COLUMN_TEXT)) {
            text = jsonObject.getString(Constants.COLUMN_TEXT);
        }
        String type = null;
        if (!jsonObject.isNull(Constants.COLUMN_TYPE)) {
            type = jsonObject.getString(Constants.COLUMN_TYPE);
        }
        String weekdays = null;
        if (!jsonObject.isNull(Constants.COLUMN_WEEKDAYS)) {
            weekdays = jsonObject.getString(Constants.COLUMN_WEEKDAYS);
        }
        String categoryId = null;
        if (!jsonObject.isNull(Constants.COLUMN_CATEGORY)) {
            categoryId = jsonObject.getString(Constants.COLUMN_CATEGORY);
        }
        String melody = null;
        if (!jsonObject.isNull(Constants.COLUMN_CUSTOM_MELODY)) {
            melody = jsonObject.getString(Constants.COLUMN_CUSTOM_MELODY);
        }
        int day = jsonObject.getInt(Constants.COLUMN_DAY);
        int month = jsonObject.getInt(Constants.COLUMN_MONTH);
        int year = jsonObject.getInt(Constants.COLUMN_YEAR);
        int hour = jsonObject.getInt(Constants.COLUMN_HOUR);
        int minute = jsonObject.getInt(Constants.COLUMN_MINUTE);
        int radius = jsonObject.getInt(Constants.COLUMN_CUSTOM_RADIUS);
        if (radius == 0) radius = -1;
        String number = null;
        if (!jsonObject.isNull(Constants.COLUMN_NUMBER)) {
            number = jsonObject.getString(Constants.COLUMN_NUMBER);
        }
        int repeatCode = jsonObject.getInt(Constants.COLUMN_REPEAT);
        long repMinute = jsonObject.getLong(Constants.COLUMN_REMIND_TIME);
        long count = jsonObject.getLong(Constants.COLUMN_REMINDERS_COUNT);
        double latitude = jsonObject.getDouble(Constants.COLUMN_LATITUDE);
        double longitude = jsonObject.getDouble(Constants.COLUMN_LONGITUDE);
        String uuID = null;
        if (!jsonObject.isNull(Constants.COLUMN_TECH_VAR)) {
            uuID = jsonObject.getString(Constants.COLUMN_TECH_VAR);
        }
        if (repMinute < 1000) repMinute = repMinute * TimeCount.MINUTE;

        int vibration = -1;
        if (jsonObject.has(Constants.COLUMN_VIBRATION))
            vibration = jsonObject.getInt(Constants.COLUMN_VIBRATION);
        int voice = -1;
        if (jsonObject.has(Constants.COLUMN_VOICE))
            voice = jsonObject.getInt(Constants.COLUMN_VOICE);
        int wake = -1;
        if (jsonObject.has(Constants.COLUMN_WAKE_SCREEN))
            wake = jsonObject.getInt(Constants.COLUMN_WAKE_SCREEN);
        int unlock = -1;
        if (jsonObject.has(Constants.COLUMN_UNLOCK_DEVICE))
            unlock = jsonObject.getInt(Constants.COLUMN_UNLOCK_DEVICE);
        int notificationRepeat = -1;
        if (jsonObject.has(Constants.COLUMN_NOTIFICATION_REPEAT))
            notificationRepeat = jsonObject.getInt(Constants.COLUMN_NOTIFICATION_REPEAT);
        int auto = -1;
        if (jsonObject.has(Constants.COLUMN_AUTO_ACTION))
            auto = jsonObject.getInt(Constants.COLUMN_AUTO_ACTION);
        long limit = -1;
        if (jsonObject.has(Constants.COLUMN_REPEAT_LIMIT))
            limit = jsonObject.getInt(Constants.COLUMN_REPEAT_LIMIT);
        String exclusion = null;
        if (jsonObject.has(Constants.COLUMN_EXTRA_3))
            exclusion = jsonObject.getString(Constants.COLUMN_EXTRA_3);

        long due = ReminderUtils.getTime(day, month, year, hour, minute, 0);

        JsonModel jsonModel = new JsonModel();
        jsonModel.setCategory(categoryId);
        jsonModel.setCount(count);
        jsonModel.setAwake(wake);
        jsonModel.setUnlock(unlock);
        jsonModel.setNotificationRepeat(notificationRepeat);
        jsonModel.setVibrate(vibration);
        jsonModel.setNotificationRepeat(voice);
        jsonModel.setSummary(text);
        jsonModel.setType(type);
        jsonModel.setEventTime(due);
        jsonModel.setStartTime(due);
        jsonModel.setUuId(uuID);

        JsonAction jsonAction = new JsonAction(type, number, auto, null);
        jsonModel.setAction(jsonAction);

        JsonExport jsonExport = new JsonExport(0, 0, null);
        jsonModel.setExport(jsonExport);

        JsonMelody jsonMelody = new JsonMelody(melody, -1);
        jsonModel.setMelody(jsonMelody);

        JsonLed jsonLed = new JsonLed(-1, 0);
        jsonModel.setLed(jsonLed);

        JsonPlace jsonPlace = new JsonPlace(latitude, longitude, radius, -1);
        jsonModel.setPlace(jsonPlace);

        JsonExclusion jsonExclusion = new JsonExclusion(exclusion);
        jsonModel.setExclusion(jsonExclusion);

        JsonRecurrence jsonRecurrence = new JsonRecurrence();
        if (weekdays != null) {
            ArrayList<Integer> list = new ArrayList<>();
            for (char c1 : weekdays.toCharArray()) {
                list.add(String.valueOf(c1).matches(Constants.DAY_CHECK) ? 1 : 0);
            }
            jsonRecurrence.setWeekdays(list);
        }
        jsonRecurrence.setLimit(limit);
        jsonRecurrence.setMonthday(day);
        jsonRecurrence.setRepeat(repeatCode * TimeCount.DAY);
        jsonRecurrence.setAfter(repMinute);
        jsonModel.setRecurrence(jsonRecurrence);

        if (jsonObject.has("shopping_list")) {
            ArrayList<JsonShopping> list = new ArrayList<>();
            JSONObject listObject = jsonObject.getJSONObject("shopping_list");
            ArrayList<ShoppingList> arrayList = new ArrayList<>();
            Iterator<?> keys = listObject.keys();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                JSONObject item = (JSONObject) listObject.get(key);
                if (item != null) {
                    String title = item.getString(Constants.COLUMN_TEXT);
                    String uuId = item.getString(Constants.COLUMN_TECH_VAR);
                    long time = item.getInt(Constants.COLUMN_DATE_TIME);
                    int status = 1;
                    if (item.has(Constants.COLUMN_EXTRA_1)) status = item.getInt(Constants.COLUMN_EXTRA_1);
                    int checked = item.getInt(Constants.COLUMN_ARCHIVED);
                    arrayList.add(new ShoppingList(title, checked, uuId, status, time));
                }
            }

            for (ShoppingList item : arrayList){
                JsonShopping jsonShopping = new JsonShopping(item.getTitle(),
                        item.getIsChecked(), item.getUuId(), item.getTime(), item.getStatus());
                list.add(jsonShopping);
            }
            jsonModel.setShoppings(list);
        }
        //Log.d("------TIME_F------", "Parsing time - " + (System.currentTimeMillis() - start));
        return jsonModel;
    }

    public String toJsonString(JsonModel model) {
        setUuid(model.getUuId());
        setSummary(model.getSummary());
        setType(model.getType());
        setCategory(model.getCategory());
        setEventTime(model.getEventTime());
        setStartTime(model.getStartTime());
        setCount(model.getCount());
        setVibration(model.getVibrate());
        setNotificationRepeat(model.getNotificationRepeat());
        setVoiceNotification(model.getVoice());
        setAwakeScreen(model.getAwake());
        setUnlockScreen(model.getUnlock());
        setExport(model.getExport());
        setPlace(model.getPlace());
        setPlaces(model.getPlaces());
        setTags(model.getTags());
        setRecurrence(model.getRecurrence());
        setMelody(model.getMelody());
        setExclusion(model.getExclusion());
        setLed(model.getLed());
        setAction(model.getAction());
        setShopping(model.getShoppings());
        return toJsonString();
    }

    public void setStartTime(long startDate) {
        try {
            jsonObject.put(START_DATE, startDate);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setEventTime(long startDate) {
        try {
            jsonObject.put(EVENT_TIME, startDate);
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
        if (tags != null) {
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
        try {
            jsonObject.put(PLACE, jsonPlace.getJsonObject());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setPlaces(List<JsonPlace> list) {
        if (list != null) {
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
    }

    public void setShopping(List<JsonShopping> list) {
        if (list != null) {
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
    }

    public ArrayList<String> getShoppingKeys() {
        ArrayList<String> list = new ArrayList<>();
        if (jsonObject.has(SHOPPING)) {
            try {
                JSONObject object = jsonObject.getJSONObject(SHOPPING);
                Iterator<String> keys = object.keys();
                while (keys.hasNext()) {
                    list.add(keys.next());
                }
                return list;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public ArrayList<JsonShopping> getShoppings() {
        if (jsonObject.has(SHOPPING)) {
            try {
                ArrayList<JsonShopping> places = new ArrayList<>();
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
        return new JsonRecurrence();
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
        return new JsonExport();
    }

    public JsonPlace getPlace() {
        if (jsonObject.has(PLACE)) {
            try {
                JSONObject object = jsonObject.getJSONObject(PLACE);
                return new JsonPlace(object);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return new JsonPlace();
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
        return new JsonExclusion();
    }

    public JsonLed getLed() {
        if (jsonObject.has(LED)) {
            try {
                JSONObject object = jsonObject.getJSONObject(LED);
                return new JsonLed(object);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return new JsonLed();
    }

    public long getEventTime() {
        if (jsonObject.has(EVENT_TIME)) {
            try {
                return jsonObject.getLong(EVENT_TIME);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    public long getStartTime() {
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
        return new JsonMelody();
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
        return new JsonAction();
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

    public String toJsonString() {
        if (jsonObject != null) return jsonObject.toString();
        else return null;
    }
}
