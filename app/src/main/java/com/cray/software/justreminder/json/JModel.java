package com.cray.software.justreminder.json;

import java.util.ArrayList;
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
public class JModel {

    private List<JPlace> places;
    private ArrayList<JShopping> shoppings;
    private JExclusion exclusion;
    private JLed led;
    private JMelody melody;
    private JRecurrence recurrence;
    private JAction action;
    private JExport export;
    private JPlace place;
    private List<String> tags;
    private String summary;
    private String type;
    private String category;
    private String uuId;
    private long eventTime;
    private long startTime;
    private long count;
    private int vibrate;
    private int notificationRepeat;
    private int voice;
    private int awake;
    private int unlock;

    public JModel() {
    }

    public JModel(String summary, String type, String category, String uuId,
                  long eventTime, long startTime, JRecurrence jRecurrence,
                  JAction jAction, JExport jExport) {
        this.summary = summary;
        this.type = type;
        this.category = category;
        this.uuId = uuId;
        this.eventTime = eventTime;
        this.startTime = startTime;
        this.count = 0;
        this.vibrate = -1;
        this.notificationRepeat = -1;
        this.voice = -1;
        this.awake = -1;
        this.unlock = -1;
        this.exclusion = null;
        this.led = null;
        this.melody = null;
        this.recurrence = jRecurrence;
        this.action = jAction;
        this.export = jExport;
        this.place = null;
        this.tags = null;
        this.places = null;
        this.shoppings = null;
    }

    public JModel(String summary, String type, String category, String uuId,
                  long eventTime, long startTime, long count, int vibrate,
                  int notificationRepeat, int voice, int awake, int unlock,
                  JExclusion jExclusion, JLed jLed, JMelody jMelody,
                  JRecurrence jRecurrence, JAction jAction, JExport jExport,
                  JPlace jPlace, List<String> tags, List<JPlace> places,
                  ArrayList<JShopping> shoppings) {
        this.summary = summary;
        this.type = type;
        this.category = category;
        this.uuId = uuId;
        this.eventTime = eventTime;
        this.startTime = startTime;
        this.count = count;
        this.vibrate = vibrate;
        this.notificationRepeat = notificationRepeat;
        this.voice = voice;
        this.awake = awake;
        this.unlock = unlock;
        this.exclusion = jExclusion;
        this.led = jLed;
        this.melody = jMelody;
        this.recurrence = jRecurrence;
        this.action = jAction;
        this.export = jExport;
        this.place = jPlace;
        this.tags = tags;
        this.places = places;
        this.shoppings = shoppings;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public JPlace getPlace() {
        return place;
    }

    public void setPlace(JPlace place) {
        this.place = place;
    }

    public JExport getExport() {
        return export;
    }

    public String getType() {
        return type;
    }

    public String getSummary() {
        return summary;
    }

    public JAction getAction() {
        return action;
    }

    public JExclusion getExclusion() {
        return exclusion;
    }

    public JLed getLed() {
        return led;
    }

    public JMelody getMelody() {
        return melody;
    }

    public JRecurrence getRecurrence() {
        return recurrence;
    }

    public List<JPlace> getPlaces() {
        return places;
    }

    public ArrayList<JShopping> getShoppings() {
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

    public void setExclusion(JExclusion exclusion) {
        this.exclusion = exclusion;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public void setAction(JAction action) {
        this.action = action;
    }

    public void setLed(JLed led) {
        this.led = led;
    }

    public void setMelody(JMelody melody) {
        this.melody = melody;
    }

    public void setPlaces(List<JPlace> places) {
        this.places = places;
    }

    public void setRecurrence(JRecurrence recurrence) {
        this.recurrence = recurrence;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setShoppings(ArrayList<JShopping> shoppings) {
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

    public void setExport(JExport export) {
        this.export = export;
    }

    public void setShopping(JShopping shopping) {
        if (shoppings != null) shoppings.add(shopping);
        else {
            shoppings = new ArrayList<>();
            shoppings.add(shopping);
        }
    }
}
