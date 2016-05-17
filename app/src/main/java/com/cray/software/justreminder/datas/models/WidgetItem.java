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

package com.cray.software.justreminder.datas.models;

public class WidgetItem {
    private int day, month, year;
    private boolean hasReminders, hasBirthdays;

    public WidgetItem(int day, int month, int year, boolean hasReminders, boolean hasBirthdays){
        this.day = day;
        this.month = month;
        this.year = year;
        this.hasReminders = hasReminders;
        this.hasBirthdays = hasBirthdays;
    }

    public boolean isHasBirthdays(){
        return hasBirthdays;
    }

    public void setHasBirthdays(boolean hasBirthdays){
        this.hasBirthdays = hasBirthdays;
    }

    public boolean isHasReminders(){
        return hasReminders;
    }

    public void setHasReminders(boolean hasReminders){
        this.hasReminders = hasReminders;
    }

    public int getYear(){
        return year;
    }

    public void setYear(int year){
        this.year = year;
    }

    public int getMonth(){
        return month;
    }

    public void setMonth(int month){
        this.month = month;
    }

    public int getDay(){
        return day;
    }

    public void setDay(int day){
        this.day = day;
    }
}
