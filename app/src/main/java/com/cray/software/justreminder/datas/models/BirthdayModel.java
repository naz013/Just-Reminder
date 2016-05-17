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

public class BirthdayModel {
    private String name, age, date, number;
    private int conId;

    public BirthdayModel(String name, String age, String date){
        this.name = name;
        this.age = age;
        this.date = date;
    }

    public BirthdayModel(String name, int conId, String date, String number){
        this.name = name;
        this.conId = conId;
        this.number = number;
        this.date = date;
    }

    public int getConId() {
        return conId;
    }

    public String getNumber() {
        return number;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
