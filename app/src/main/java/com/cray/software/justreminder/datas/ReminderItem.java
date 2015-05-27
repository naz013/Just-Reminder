package com.cray.software.justreminder.datas;

public class ReminderItem {
    String title, type, repeat, category, uuId, number;
    int completed, archived;
    long due, id;
    double[] place;
    private boolean mPinnedToSwipeLeft;

    public ReminderItem(String title, String type, String repeat, String category, String uuId,
                        int completed, long due, long id, double[] place, String number){
        this.category = category;
        this.title = title;
        this.type = type;
        this.due = due;
        this.id = id;
        this.completed = completed;
        this.uuId = uuId;
        this.place = place;
        this.repeat = repeat;
        this.number = number;
    }

    public int getArchived(){
        return archived;
    }

    public void setArchived(int archived){
        this.archived = archived;
    }

    public int getCompleted(){
        return completed;
    }

    public void setCompleted(int completed){
        this.completed = completed;
    }

    public double[] getPlace(){
        return place;
    }

    public void  setPlace(double[] place){
        this.place = place;
    }

    public long getDue(){
        return due;
    }

    public void setDue(long due){
        this.due = due;
    }

    public long getId(){
        return id;
    }

    public void setId(long id){
        this.id = id;
    }

    public String getTitle(){
        return title;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public String getType(){
        return type;
    }

    public void setType(String type){
        this.type = type;
    }

    public String getRepeat(){
        return repeat;
    }

    public void setRepeat(String repeat){
        this.repeat = repeat;
    }

    public String getCategory(){
        return category;
    }

    public void setCategory(String category){
        this.category = category;
    }

    public String getUuId(){
        return uuId;
    }

    public void setUuId(String uuId){
        this.uuId = uuId;
    }

    public String getNumber(){
        return number;
    }

    public void setNumber(String number){
        this.number = number;
    }

    public boolean isPinnedToSwipeLeft() {
        return mPinnedToSwipeLeft;
    }

    public void setPinnedToSwipeLeft(boolean pinedToSwipeLeft) {
        mPinnedToSwipeLeft = pinedToSwipeLeft;
    }
}
