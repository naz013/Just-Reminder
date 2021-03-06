/*
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

package com.cray.software.justreminder.reminder.json;

import org.json.JSONException;
import org.json.JSONObject;

public class JExport {

    /**
     * JSON keys.
     */
    private static final String GTASKS = "g_tasks";
    private static final String CALENDAR = "to_calendar";
    private static final String CALENDAR_ID = "calendar_id";

    private int gTasks, calendar;
    private String calendarId;

    /**
     * JSON object.
     */
    private JSONObject jsonObject;

    /**
     * Get current JSON object.
     * @return JSON object string
     */
    @Override
    public String toString(){
        return "JExport->Calendar: " + calendar +
                "->GTasks: " + gTasks +
                "->CalendarId: " + calendarId;
    }

    public JExport(JSONObject jsonObject){
        if (jsonObject != null) {
            this.jsonObject = jsonObject;
            parse(jsonObject);
        }
    }

    public JExport(String object){
        if (object != null) {
            try {
                jsonObject = new JSONObject(object);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            parse(jsonObject);
        }
    }

    public JExport(){
        jsonObject = new JSONObject();
        setGtasks(0);
        setCalendar(0);
        setCalendarId(null);
    }

    public JExport(int gTasks, int calendar, String calendarId){
        jsonObject = new JSONObject();
        setGtasks(gTasks);
        setCalendar(calendar);
        setCalendarId(calendarId);
    }

    private void parse(JSONObject jsonObject) {
        if (jsonObject.has(CALENDAR_ID)) {
            try {
                calendarId = jsonObject.getString(CALENDAR_ID);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if (jsonObject.has(GTASKS)){
            try {
                gTasks = jsonObject.getInt(GTASKS);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if (jsonObject.has(CALENDAR)){
            try {
                calendar = jsonObject.getInt(CALENDAR);
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
     * Set current JSON object
     * @param jsonObject JSON object
     */
    public JExport setJsonObject(JSONObject jsonObject) {
        this.jsonObject = jsonObject;
        return this;
    }

    public JExport setCalendar(int calendar) {
        this.calendar = calendar;
        try {
            jsonObject.put(CALENDAR, calendar);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return this;
    }

    public JExport setGtasks(int gTasks) {
        this.gTasks = gTasks;
        try {
            jsonObject.put(GTASKS, gTasks);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return this;
    }

    public JExport setCalendarId(String calendarId) {
        this.calendarId = calendarId;
        try {
            jsonObject.put(CALENDAR_ID, calendarId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return this;
    }

    public String getCalendarId() {
        return calendarId;
    }

    public int getCalendar() {
        return calendar;
    }

    public int getgTasks() {
        return gTasks;
    }
}
