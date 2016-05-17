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
import com.cray.software.justreminder.json.JAction;
import com.cray.software.justreminder.json.JMelody;
import com.cray.software.justreminder.json.JModel;
import com.cray.software.justreminder.json.JPlace;
import com.cray.software.justreminder.json.JRecurrence;
import com.cray.software.justreminder.json.JShopping;

import java.util.ArrayList;

public class ReminderModel {
    private String title, type, uuId, number, groupId, exclusion, melody;
    private int completed, archived, catColor, viewType, radius, marker, totalPlaces;
    private long due, id, repeat;
    private double[] place;
    private ArrayList<JShopping> shoppings;
    private ArrayList<Integer> weekdays;

    public ReminderModel(long id, JModel jModel, int catColor, int archived, int completed,
                         int viewType) {
        this.id = id;
        this.catColor = catColor;
        this.archived = archived;
        this.completed = completed;
        this.viewType = viewType;

        this.groupId = jModel.getCategory();
        this.title = jModel.getSummary();
        this.type = jModel.getType();
        this.uuId = jModel.getUuId();
        this.exclusion = jModel.getExclusion().toString();
        this.due = jModel.getEventTime();

        JMelody jMelody = jModel.getMelody();
        this.melody = jMelody.getMelodyPath();

        JAction jAction = jModel.getAction();
        this.number = jAction.getTarget();

        JPlace jPlace = jModel.getPlace();
        this.radius = jPlace.getRadius();
        this.place = new double[]{jPlace.getLatitude(), jPlace.getLongitude()};
        this.marker = jPlace.getMarker();

        if (type.matches(Constants.TYPE_PLACES)) {
            ArrayList<JPlace> list = jModel.getPlaces();
            if (list != null && list.size() > 0) {
                totalPlaces = list.size();
                JPlace place = list.get(0);
                this.radius = place.getRadius();
                this.place = new double[]{place.getLatitude(), place.getLongitude()};
                this.marker = place.getMarker();
            }
        }

        JRecurrence jRecurrence = jModel.getRecurrence();
        repeat = jRecurrence.getRepeat();
        weekdays = jRecurrence.getWeekdays();

        this.shoppings = jModel.getShoppings();
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
