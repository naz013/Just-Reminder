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

import android.os.Parcel;
import android.os.Parcelable;

public class NoteItem implements Parcelable {

    private String note;
    private String uuId;
    private String date;
    private int color;
    private int style;
    private byte[] image;
    private long id;
    private long linkId;

    public NoteItem() {}

    public NoteItem(String note, String uuId, String date, int color, int style, byte[] image, long id, long linkId) {
        this.note = note;
        this.uuId = uuId;
        this.date = date;
        this.color = color;
        this.style = style;
        this.image = image;
        this.id = id;
        this.linkId = linkId;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getUuId() {
        return uuId;
    }

    public void setUuId(String uuId) {
        this.uuId = uuId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getStyle() {
        return style;
    }

    public void setStyle(int style) {
        this.style = style;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getLinkId() {
        return linkId;
    }

    public void setLinkId(long linkId) {
        this.linkId = linkId;
    }

    public NoteItem(Parcel in) {
        super();
        readFromParcel(in);
    }

    public static final Creator<NoteItem> CREATOR = new Creator<NoteItem>() {
        public NoteItem createFromParcel(Parcel in) {
            return new NoteItem(in);
        }

        public NoteItem[] newArray(int size) {
            return new NoteItem[size];
        }
    };

    public void readFromParcel(Parcel in) {
        note = in.readString();
        uuId = in.readString();
        date = in.readString();
        id = in.readLong();
        linkId = in.readLong();
        color = in.readInt();
        style = in.readInt();
        in.readByteArray(image);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(note);
        dest.writeString(uuId);
        dest.writeString(date);
        dest.writeLong(id);
        dest.writeLong(linkId);
        dest.writeInt(color);
        dest.writeInt(style);
        dest.writeByteArray(image);
    }
}
