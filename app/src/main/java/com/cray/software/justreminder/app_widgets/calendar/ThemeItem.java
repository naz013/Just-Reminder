package com.cray.software.justreminder.app_widgets.calendar;

import android.os.Parcel;
import android.os.Parcelable;

public class ThemeItem implements Parcelable {
    private int itemTextColor, widgetBgColor, headerColor, borderColor, titleColor, rowColor;
    private int leftArrow, rightArrow, iconPlus, iconVoice, iconSettings;
    private int currentMark, birthdayMark, reminderMark;
    private String title;
    private int windowColor;
    private int windowTextColor;

    private ThemeItem() {}

    public ThemeItem(int itemTextColor, int widgetBgColor, int headerColor, int borderColor,
                     int titleColor, int rowColor, int leftArrow, int rightArrow, int iconPlus, int iconVoice,
                     int iconSettings, String title, int currentMark, int birthdayMark, int reminderMark,
                     int windowColor, int windowTextColor){
        this.itemTextColor = itemTextColor;
        this.widgetBgColor = widgetBgColor;
        this.headerColor = headerColor;
        this.borderColor = borderColor;
        this.titleColor = titleColor;
        this.leftArrow = leftArrow;
        this.rightArrow = rightArrow;
        this.iconPlus = iconPlus;
        this.iconVoice = iconVoice;
        this.iconSettings = iconSettings;
        this.rowColor = rowColor;
        this.title = title;
        this.currentMark = currentMark;
        this.birthdayMark = birthdayMark;
        this.reminderMark = reminderMark;
        this.windowColor = windowColor;
        this.windowTextColor = windowTextColor;
    }

    public int getCurrentMark(){
        return currentMark;
    }

    public int getBirthdayMark(){
        return birthdayMark;
    }

    public int getReminderMark(){
        return reminderMark;
    }

    public void setItemTextColor(int itemTextColor){
        this.itemTextColor = itemTextColor;
    }

    public int getItemTextColor(){
        return itemTextColor;
    }

    public int getRowColor(){
        return rowColor;
    }

    public int getWidgetBgColor(){
        return widgetBgColor;
    }

    public int getHeaderColor(){
        return headerColor;
    }

    public int getBorderColor(){
        return borderColor;
    }

    public int getTitleColor(){
        return titleColor;
    }

    public int getLeftArrow(){
        return leftArrow;
    }

    public int getRightArrow(){
        return rightArrow;
    }

    public int getIconPlus(){
        return iconPlus;
    }

    public int getIconVoice(){
        return iconVoice;
    }

    public int getIconSettings(){
        return iconSettings;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public int getWindowColor() {
        return windowColor;
    }

    public int getWindowTextColor() {
        return windowTextColor;
    }

    public String getTitle(){
        return title;
    }

    public ThemeItem(Parcel in) {
        super();
        readFromParcel(in);
    }

    public final Creator<ThemeItem> CREATOR = new Creator<ThemeItem>() {
        public ThemeItem createFromParcel(Parcel in) {
            return new ThemeItem(in);
        }

        public ThemeItem[] newArray(int size) {

            return new ThemeItem[size];
        }

    };

    public void readFromParcel(Parcel in) {
        title = in.readString();
        itemTextColor = in.readInt();
        widgetBgColor = in.readInt();
        rowColor = in.readInt();
        borderColor = in.readInt();
        headerColor = in.readInt();
        titleColor = in.readInt();
        leftArrow = in.readInt();
        rightArrow = in.readInt();
        iconPlus = in.readInt();
        iconSettings = in.readInt();
        iconVoice = in.readInt();
        windowColor = in.readInt();
        windowTextColor = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeInt(itemTextColor);
        dest.writeInt(widgetBgColor);
        dest.writeInt(rowColor);
        dest.writeInt(borderColor);
        dest.writeInt(headerColor);
        dest.writeInt(titleColor);
        dest.writeInt(leftArrow);
        dest.writeInt(rightArrow);
        dest.writeInt(iconPlus);
        dest.writeInt(iconVoice);
        dest.writeInt(iconSettings);
        dest.writeInt(windowColor);
        dest.writeInt(windowTextColor);
    }
}
