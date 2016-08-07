package com.hexrain.flextcal;

import java.util.ArrayList;

/**
 * Copyright 2015 Nazar Suhovich
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
public class Events {

    private ArrayList<Event> events;
    int mPosition = 0;

    public Events() {
        events = new ArrayList<>();
    }

    public Events(Event event) {
        events = new ArrayList<>();
        events.add(event);
    }

    public Events(String task, int color, Type type) {
        Event event = new Event(task, color, type);
        if (events != null) {
            events.add(event);
        } else {
            events = new ArrayList<>();
            events.add(event);
        }
    }

    public int addEvent(String task, int color, Type type) {
        Event event = new Event(task, color, type);
        if (events != null) {
            events.add(event);
        } else {
            events = new ArrayList<>();
            events.add(event);
        }
        return events.indexOf(event);
    }

    public Event getNext() {
        if (events != null && mPosition < events.size()) {
            Event event = events.get(mPosition);
            mPosition++;
            return event;
        } else return null;
    }

    public Event getLast() {
        if (events != null) {
            return events.get(events.size() - 1);
        } else return null;
    }

    public boolean hasNext() {
        return events != null && mPosition < events.size();
    }

    public int count() {
        return events.size();
    }

    public enum Type {
        REMINDER,
        BIRTHDAY
    }

    public class Event {
        private String task;
        private int color;
        private Type type;

        Event(String task, int color, Type type) {
            this.task = task;
            this.color = color;
            this.type = type;
        }

        public Type getType() {
            return type;
        }

        public void setType(Type type) {
            this.type = type;
        }

        public int getColor() {
            return color;
        }

        public void setColor(int color) {
            this.color = color;
        }

        public String getTask(){
            return task;
        }

        public void setTask(String task){
            this.task = task;
        }
    }
}
