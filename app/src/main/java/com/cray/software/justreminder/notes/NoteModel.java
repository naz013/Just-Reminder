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

package com.cray.software.justreminder.notes;

public class NoteModel {

    private String note, uuId;
    private int color, style;
    private byte[] image;
    private long id;

    public NoteModel(String note, int color, int style, byte[] image, long id){
        this.color = color;
        this.image = image;
        this.note = note;
        this.style = style;
        this.id = id;
    }

    public NoteModel(String note, int color, int style, byte[] image, String uuId){
        this.color = color;
        this.image = image;
        this.note = note;
        this.style = style;
        this.uuId = uuId;
    }

    public String getUuId() {
        return uuId;
    }

    public String getNote(){
        return note;
    }

    public void setNote(String note){
        this.note = note;
    }

    public int getColor(){
        return color;
    }

    public void setColor(int color){
        this.color = color;
    }

    public int getStyle(){
        return style;
    }

    public void setStyle(int style){
        this.style = style;
    }

    public byte[] getImage(){
        return image;
    }

    public void setImage(byte[] image){
        this.image = image;
    }

    public long getId(){
        return id;
    }

    public void setId(long id){
        this.id = id;
    }
}
