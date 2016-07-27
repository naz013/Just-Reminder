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
package com.cray.software.justreminder.reminder;

import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.reminder.json.JAction;
import com.cray.software.justreminder.reminder.json.JMelody;
import com.cray.software.justreminder.reminder.json.JsonModel;
import com.cray.software.justreminder.reminder.json.JPlace;
import com.cray.software.justreminder.reminder.json.JRecurrence;
import com.cray.software.justreminder.reminder.json.JShopping;

import java.util.ArrayList;

public class ReminderModel {
    private String title, type, uuId, number, groupId, exclusion, melody;
    private int completed, archived, catColor, viewType, radius, marker, totalPlaces;
    private long due, id, repeat;
    private double[] place;
    private ArrayList<JShopping> shoppings;
    private ArrayList<Integer> weekdays;
    private ArrayList<JPlace> places;

    public ReminderModel(long id, JsonModel jsonModel, int catColor, int archived, int completed,
                         int viewType) {
        this.id = id;
        this.catColor = catColor;
        this.archived = archived;
        this.completed = completed;
        this.viewType = viewType;

        this.groupId = jsonModel.getCategory();
        this.title = jsonModel.getSummary();
        this.type = jsonModel.getType();
        this.uuId = jsonModel.getUuId();
        this.exclusion = jsonModel.getExclusion().toString();
        this.due = jsonModel.getEventTime();

        JMelody jMelody = jsonModel.getMelody();
        this.melody = jMelody.getMelodyPath();

        JAction jAction = jsonModel.getAction();
        this.number = jAction.getTarget();

        JPlace jPlace = jsonModel.getPlace();
        this.radius = jPlace.getRadius();
        this.place = new double[]{jPlace.getLatitude(), jPlace.getLongitude()};
        this.marker = jPlace.getMarker();

        if (type.matches(Constants.TYPE_PLACES)) {
            places = jsonModel.getPlaces();
            if (places != null && places.size() > 0) {
                totalPlaces = places.size();
                JPlace place = places.get(0);
                this.radius = place.getRadius();
                this.place = new double[]{place.getLatitude(), place.getLongitude()};
                this.marker = place.getMarker();
            }
        }

        JRecurrence jRecurrence = jsonModel.getRecurrence();
        repeat = jRecurrence.getRepeat();
        weekdays = jRecurrence.getWeekdays();

        this.shoppings = jsonModel.getShoppings();
    }

    public ArrayList<JPlace> getPlaces() {
        return places;
    }

    public int getTotalPlaces() {
        return totalPlaces;
    }

    public ArrayList<Integer> getWeekdays() {
        return weekdays;
    }

    public int getMarker() {
        return marker;
    }

    public ArrayList<JShopping> getShoppings() {
        return shoppings;
    }

    public String getMelody() {
        return melody;
    }

    public int getRadius() {
        return radius;
    }

    public String getExclusion() {
        return exclusion;
    }

    public String getGroupId() {
        return groupId;
    }

    public int getViewType() {
        return viewType;
    }

    public int getArchived(){
        return archived;
    }

    public int getCompleted(){
        return completed;
    }

    public double[] getPlace(){
        return place;
    }

    public long getDue(){
        return due;
    }

    public long getId(){
        return id;
    }

    public String getTitle(){
        return title;
    }

    public String getType(){
        return type;
    }

    public long getRepeat(){
        return repeat;
    }

    public int getCatColor(){
        return catColor;
    }

    public String getUuId(){
        return uuId;
    }

    public String getNumber(){
        return number;
    }
}
