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
public class FileModel {
    private String title, fileName, type, lastModified, time, date, repeat, number;
    private long id;

    public FileModel(String title, String fileName, String type, String lastModified, String time,
                     String date, String repeat, String number, long id){
        this.title = title;
        this.fileName = fileName;
        this.type = type;
        this.lastModified = lastModified;
        this.time = time;
        this.date = date;
        this.repeat = repeat;
        this.number = number;
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public String getDate() {
        return date;
    }

    public String getFileName() {
        return fileName;
    }

    public String getLastModified() {
        return lastModified;
    }

    public String getNumber() {
        return number;
    }

    public String getRepeat() {
        return repeat;
    }

    public String getTime() {
        return time;
    }

    public String getTitle() {
        return title;
    }

    public String getType() {
        return type;
    }
}
