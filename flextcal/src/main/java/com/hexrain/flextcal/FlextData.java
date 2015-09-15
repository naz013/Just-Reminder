package com.hexrain.flextcal;

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
public class FlextData {

    private String task, birth;

    public FlextData(){

    }

    public FlextData(String task, String birth){
        this.task = task;
        this.birth = birth;
    }

    public String getBirth(){
        return birth;
    }

    public void setBirth(String birth){
        this.birth = birth;
    }

    public String getTask(){
        return task;
    }

    public void setTask(String task){
        this.task = task;
    }
}
