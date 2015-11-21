package com.cray.software.justreminder.datas.models;

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
public class ReminderModel {
    private final String title, type, repeat, uuId, number, groupId, exclusion, melody;
    private final int completed, archived, catColor, viewType, radius;
    private final long due, id;
    private final double[] place;
    private boolean selected;

    public ReminderModel(String title, String type, String repeat, int catColor, String uuId,
                         int completed, long due, long id, double[] place, String number, int archived,
                         int viewType, String groupId, String exclusion, int radius, String melody){
        this.catColor = catColor;
        this.viewType = viewType;
        this.title = title;
        this.type = type;
        this.due = due;
        this.id = id;
        this.completed = completed;
        this.uuId = uuId;
        this.place = place;
        this.repeat = repeat;
        this.number = number;
        this.archived = archived;
        this.groupId = groupId;
        this.exclusion = exclusion;
        this.selected = false;
        this.radius = radius;
        this.melody = melody;
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

    public boolean getSelected(){
        return selected;
    }

    public void setSelected(boolean selected){
        this.selected = selected;
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

    public String getRepeat(){
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
