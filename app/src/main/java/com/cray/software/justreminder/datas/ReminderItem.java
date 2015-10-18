package com.cray.software.justreminder.datas;

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
public class ReminderItem {
    private String title, type, repeat, uuId, number;
    private int completed, archived, catColor, viewType;
    private long due, id;
    private double[] place;
    private boolean mPinnedToSwipeLeft, selected;

    public ReminderItem(String title, String type, String repeat, int catColor, String uuId,
                        int completed, long due, long id, double[] place, String number, int archived, int viewType){
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
        this.selected = false;
    }

    public int getViewType() {
        return viewType;
    }

    public void setViewType(int viewType) {
        this.viewType = viewType;
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

    public void setArchived(int archived){
        this.archived = archived;
    }

    public int getCompleted(){
        return completed;
    }

    public void setCompleted(int completed){
        this.completed = completed;
    }

    public double[] getPlace(){
        return place;
    }

    public void  setPlace(double[] place){
        this.place = place;
    }

    public long getDue(){
        return due;
    }

    public void setDue(long due){
        this.due = due;
    }

    public long getId(){
        return id;
    }

    public void setId(long id){
        this.id = id;
    }

    public String getTitle(){
        return title;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public String getType(){
        return type;
    }

    public void setType(String type){
        this.type = type;
    }

    public String getRepeat(){
        return repeat;
    }

    public void setRepeat(String repeat){
        this.repeat = repeat;
    }

    public int getCatColor(){
        return catColor;
    }

    public void setCatColor(int catColor){
        this.catColor = catColor;
    }

    public String getUuId(){
        return uuId;
    }

    public void setUuId(String uuId){
        this.uuId = uuId;
    }

    public String getNumber(){
        return number;
    }

    public void setNumber(String number){
        this.number = number;
    }

    public boolean isPinnedToSwipeLeft() {
        return mPinnedToSwipeLeft;
    }

    public void setPinnedToSwipeLeft(boolean pinedToSwipeLeft) {
        mPinnedToSwipeLeft = pinedToSwipeLeft;
    }
}
