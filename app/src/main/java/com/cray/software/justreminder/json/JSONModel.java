package com.cray.software.justreminder.json;

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
public class JsonModel {

    private List<JsonPlace> places;
    private List<JsonShopping> shoppings;
    private JsonExclusion exclusion;
    private JsonLed led;
    private JsonMelody melody;
    private JsonRecurrence recurrence;
    private JsonAction action;
    private JsonExport export;
    private JsonPlace place;
    private List<String> tags;
    private String summary;
    private String type;
    private String category;
    private String uuId;
    private long eventTime;
    private long count;
    private int vibrate;
    private int notificationRepeat;
    private int voice;
    private int awake;
    private int unlock;

    public JsonModel() {

    }

    public JsonPlace getPlace() {
        return place;
    }

    public void setPlace(JsonPlace place) {
        this.place = place;
    }

    public JsonExport getExport() {
        return export;
    }

    public String getType() {
        return type;
    }

    public String getSummary() {
        return summary;
    }

    public JsonAction getAction() {
        return action;
    }

    public JsonExclusion getExclusion() {
        return exclusion;
    }

    public JsonLed getLed() {
        return led;
    }

    public JsonMelody getMelody() {
        return melody;
    }

    public JsonRecurrence getRecurrence() {
        return recurrence;
    }

    public List<JsonPlace> getPlaces() {
        return places;
    }

    public List<JsonShopping> getShoppings() {
        return shoppings;
    }

    public int getAwake() {
        return awake;
    }

    public int getNotificationRepeat() {
        return notificationRepeat;
    }

    public int getVibrate() {
        return vibrate;
    }

    public int getUnlock() {
        return unlock;
    }

    public int getVoice() {
        return voice;
    }

    public List<String> getTags() {
        return tags;
    }

    public long getCount() {
        return count;
    }

    public long getEventTime() {
        return eventTime;
    }

    public String getCategory() {
        return category;
    }

    public String getUuId() {
        return uuId;
    }

    public void setExclusion(JsonExclusion exclusion) {
        this.exclusion = exclusion;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public void setAction(JsonAction action) {
        this.action = action;
    }

    public void setLed(JsonLed led) {
        this.led = led;
    }

    public void setMelody(JsonMelody melody) {
        this.melody = melody;
    }

    public void setPlaces(List<JsonPlace> places) {
        this.places = places;
    }

    public void setRecurrence(JsonRecurrence recurrence) {
        this.recurrence = recurrence;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setShoppings(List<JsonShopping> shoppings) {
        this.shoppings = shoppings;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public void setEventTime(long eventTime) {
        this.eventTime = eventTime;
    }

    public void setNotificationRepeat(int notificationRepeat) {
        this.notificationRepeat = notificationRepeat;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public void setAwake(int awake) {
        this.awake = awake;
    }

    public void setUnlock(int unlock) {
        this.unlock = unlock;
    }

    public void setUuId(String uuId) {
        this.uuId = uuId;
    }

    public void setVibrate(int vibrate) {
        this.vibrate = vibrate;
    }

    public void setVoice(int voice) {
        this.voice = voice;
    }

    public void setExport(JsonExport export) {
        this.export = export;
    }
}
