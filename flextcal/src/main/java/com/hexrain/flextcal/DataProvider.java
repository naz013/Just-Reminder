package com.hexrain.flextcal;

import java.util.ArrayList;
import java.util.HashMap;

import hirondelle.date4j.DateTime;

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
public class DataProvider {
    private ArrayList<FlextData> list;

    public DataProvider(){
        list = new ArrayList<>();
    }

    public DataProvider(ArrayList<FlextData> list){
        this.list = list;
    }

    public void swapData(ArrayList<FlextData> list){
        this.list.clear();
        this.list = list;
    }

    public ArrayList<FlextData> getList(){
        return list;
    }

    public HashMap<DateTime, String> getReminderTasks(){
        HashMap<DateTime, String> map = new HashMap<>();
        for (FlextData item : list){
            DateTime key = item.getKey();
            if (item.hasEvent1() == 1 && !map.containsKey(key)){
                map.put(key, item.getTask());
            }
        }
        return map;
    }

    public HashMap<DateTime, String> getBirthTasks(){
        HashMap<DateTime, String> map = new HashMap<>();
        for (FlextData item : list){
            DateTime key = item.getKey();
            if (item.hasEvent1() == 2 && !map.containsKey(key)){
                map.put(key, item.getTask());
            }
        }
        return map;
    }

    public HashMap<DateTime, Integer> getReminderRes(){
        HashMap<DateTime, Integer> map = new HashMap<>();
        for (FlextData item : list){
            DateTime key = item.getKey();
            if (item.hasEvent1() == 1 && !map.containsKey(key)){
                map.put(key, item.getResource());
            }
        }
        return map;
    }

    public HashMap<DateTime, Integer> getBirthRes(){
        HashMap<DateTime, Integer> map = new HashMap<>();
        for (FlextData item : list){
            DateTime key = item.getKey();
            if (item.hasEvent1() == 2 && !map.containsKey(key)){
                map.put(key, item.getResource());
            }
        }
        return map;
    }

    /*public ArrayList<FlextData> getList(DateTime key){
        ArrayList<FlextData> datas = new ArrayList<>();
        for (FlextData item : list){
            if (item.getKey() == key.getMilliseconds(TimeZone.getDefault())) datas.add(item);
        }
        return datas;
    }

    public FlextData getItem(DateTime key, int event){
        FlextData datas = null;
        for (FlextData item : list){
            if (event != 0) {
                if (item.getKey() == key.getMilliseconds(TimeZone.getDefault()) && event == item.hasEvent1()) {
                    datas = item;
                    break;
                }
            } else {
                if (item.getKey() == key.getMilliseconds(TimeZone.getDefault())) {
                    datas = item;
                    break;
                }
            }
        }
        return datas;
    }

    public FlextData[] getItem(DateTime key){
        FlextData event1 = null;
        FlextData event2 = null;
        boolean has1 = false;
        boolean has2 = false;
        for (FlextData item : list){
            long itemKey = item.getKey();
            if (itemKey == key.getMilliseconds(TimeZone.getDefault())) {
                if (!has1 || !has2) {
                    int i = item.hasEvent1();
                    if (i == 1) {
                        has1 = true;
                        event1 = item;
                    }
                    if (i == 2) {
                        has2 = true;
                        event2 = item;
                    }
                } else break;
            }
        }
        return new FlextData[]{event1, event2};
    }*/
}
