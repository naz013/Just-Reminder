package com.cray.software.justreminder.note;

public class NoteItem {
    String note;
    int color, style;
    byte[] image;
    long id;

    public NoteItem(String note, int color, int style, byte[] image, long id){
        this.color = color;
        this.image = image;
        this.note = note;
        this.style = style;
        this.id = id;
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
