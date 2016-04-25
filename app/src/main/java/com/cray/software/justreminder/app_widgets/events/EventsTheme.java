package com.cray.software.justreminder.app_widgets.events;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;

import com.cray.software.justreminder.R;

import java.util.ArrayList;

public class EventsTheme implements Parcelable {

    private int headerColor;
    private int backgroundColor;
    private int titleColor;
    private int plusIcon;
    private int settingsIcon;
    private int voiceIcon;
    private int itemTextColor;
    private int itemBackgroud;
    private int checkboxColor;
    private String title;
    private int windowColor;
    private int windowTextColor;

    private EventsTheme() {}

    public EventsTheme(@ColorRes int headerColor, @ColorRes int backgroundColor, @ColorInt int titleColor,
                       @DrawableRes int plusIcon, @DrawableRes int settingsIcon, @DrawableRes int voiceIcon,
                       @ColorInt int itemTextColor, @ColorRes int itemBackground, int checkboxColor,
                       String title, @ColorRes int windowColor, @ColorInt int windowTextColor) {
        this.headerColor = headerColor;
        this.backgroundColor = backgroundColor;
        this.titleColor = titleColor;
        this.plusIcon = plusIcon;
        this.settingsIcon = settingsIcon;
        this.title = title;
        this.windowColor = windowColor;
        this.windowTextColor = windowTextColor;
        this.voiceIcon = voiceIcon;
        this.itemTextColor = itemTextColor;
        this.itemBackgroud = itemBackground;
        this.checkboxColor = checkboxColor;
    }

    @DrawableRes
    public int getVoiceIcon() {
        return voiceIcon;
    }

    public int getCheckboxColor() {
        return checkboxColor;
    }

    @ColorRes
    public int getItemBackground() {
        return itemBackgroud;
    }

    @ColorInt
    public int getItemTextColor() {
        return itemTextColor;
    }

    @ColorInt
    public int getWindowTextColor() {
        return windowTextColor;
    }

    @ColorRes
    public int getWindowColor() {
        return windowColor;
    }

    @ColorInt
    public int getTitleColor() {
        return titleColor;
    }

    @DrawableRes
    public int getSettingsIcon() {
        return settingsIcon;
    }

    @ColorRes
    public int getBackgroundColor() {
        return backgroundColor;
    }

    @ColorRes
    public int getHeaderColor() {
        return headerColor;
    }

    @DrawableRes
    public int getPlusIcon() {
        return plusIcon;
    }

    public String getTitle(){
        return title;
    }

    private static int getResColor(Context ctx, int res){
        return ctx.getResources().getColor(res);
    }

    public static ArrayList<EventsTheme> getThemes(Context context) {
        ArrayList<EventsTheme> list = new ArrayList<>();
        list.clear();
        list.add(new EventsTheme(R.color.indigoPrimary, R.color.material_grey,
                getResColor(context, R.color.whitePrimary), R.drawable.ic_add_white_24dp,
                R.drawable.ic_settings_white_24dp, R.drawable.ic_mic_white_24dp,
                getResColor(context, R.color.blackPrimary), R.color.whitePrimary, 0, "Indigo", R.color.whitePrimary,
                getResColor(context, R.color.blackPrimary)));

        list.add(new EventsTheme(R.color.tealPrimaryDark, R.color.material_grey,
                getResColor(context, R.color.whitePrimary), R.drawable.ic_add_white_24dp,
                R.drawable.ic_settings_white_24dp, R.drawable.ic_mic_white_24dp,
                getResColor(context, R.color.blackPrimary), R.color.whitePrimary, 0, "Teal", R.color.whitePrimary,
                getResColor(context, R.color.blackPrimary)));

        list.add(new EventsTheme(R.color.limePrimaryDark, R.color.material_grey,
                getResColor(context, R.color.whitePrimary), R.drawable.ic_add_white_24dp,
                R.drawable.ic_settings_white_24dp, R.drawable.ic_mic_white_24dp,
                getResColor(context, R.color.blackPrimary), R.color.whitePrimary, 0, "Lime", R.color.whitePrimary,
                getResColor(context, R.color.blackPrimary)));

        list.add(new EventsTheme(R.color.bluePrimaryDark, R.color.material_grey,
                getResColor(context, R.color.whitePrimary), R.drawable.ic_add_white_24dp,
                R.drawable.ic_settings_white_24dp, R.drawable.ic_mic_white_24dp,
                getResColor(context, R.color.blackPrimary), R.color.whitePrimary, 0, "Blue", R.color.whitePrimary,
                getResColor(context, R.color.blackPrimary)));

        list.add(new EventsTheme(R.color.material_grey, R.color.material_divider,
                getResColor(context, R.color.whitePrimary), R.drawable.ic_add_white_24dp,
                R.drawable.ic_settings_white_24dp, R.drawable.ic_mic_white_24dp,
                getResColor(context, R.color.blackPrimary), R.color.whitePrimary, 0, "Gray", R.color.whitePrimary,
                getResColor(context, R.color.blackPrimary)));

        list.add(new EventsTheme(R.color.greenPrimaryDark, R.color.material_grey,
                getResColor(context, R.color.whitePrimary), R.drawable.ic_add_white_24dp,
                R.drawable.ic_settings_white_24dp, R.drawable.ic_mic_white_24dp,
                getResColor(context, R.color.blackPrimary), R.color.whitePrimary, 0, "Green", R.color.whitePrimary,
                getResColor(context, R.color.blackPrimary)));

        list.add(new EventsTheme(R.color.blackPrimary, R.color.blackPrimary,
                getResColor(context, R.color.whitePrimary), R.drawable.ic_add_white_24dp,
                R.drawable.ic_settings_white_24dp, R.drawable.ic_mic_white_24dp,
                getResColor(context, R.color.whitePrimary), R.color.blackPrimary, 1, "Dark", R.color.whitePrimary,
                getResColor(context, R.color.blackPrimary)));

        list.add(new EventsTheme(R.color.whitePrimary, R.color.whitePrimary,
                getResColor(context, R.color.blackPrimary), R.drawable.ic_add_black_24dp,
                R.drawable.ic_settings_black_24dp, R.drawable.ic_mic_black_24dp,
                getResColor(context, R.color.blackPrimary), R.color.whitePrimary, 0, "White", R.color.material_grey,
                getResColor(context, R.color.whitePrimary)));

        list.add(new EventsTheme(R.color.orangePrimaryDark, R.color.material_grey,
                getResColor(context, R.color.whitePrimary), R.drawable.ic_add_white_24dp,
                R.drawable.ic_settings_white_24dp, R.drawable.ic_mic_white_24dp,
                getResColor(context, R.color.blackPrimary), R.color.whitePrimary, 0, "Orange", R.color.whitePrimary,
                getResColor(context, R.color.blackPrimary)));

        list.add(new EventsTheme(R.color.redPrimaryDark, R.color.material_grey,
                getResColor(context, R.color.whitePrimary), R.drawable.ic_add_white_24dp,
                R.drawable.ic_settings_white_24dp, R.drawable.ic_mic_white_24dp,
                getResColor(context, R.color.blackPrimary), R.color.whitePrimary, 0, "Red", R.color.whitePrimary,
                getResColor(context, R.color.blackPrimary)));

        list.add(new EventsTheme(R.color.material_grey_dialog, R.color.orangeAccent,
                getResColor(context, R.color.whitePrimary), R.drawable.ic_add_white_24dp,
                R.drawable.ic_settings_white_24dp, R.drawable.ic_mic_white_24dp,
                getResColor(context, R.color.blackPrimary), R.color.material_divider, 0, "Simple Orange", R.color.whitePrimary,
                getResColor(context, R.color.blackPrimary)));

        list.add(new EventsTheme(R.color.simple_transparent_header_color, R.color.simple_transparent_header_color,
                getResColor(context, R.color.whitePrimary), R.drawable.ic_add_white_24dp,
                R.drawable.ic_settings_white_24dp, R.drawable.ic_mic_white_24dp,
                getResColor(context, R.color.whitePrimary), R.color.simple_transparent_header_color, 1, "Transparent Light", R.color.material_grey,
                getResColor(context, R.color.whitePrimary)));

        list.add(new EventsTheme(R.color.simple_transparent_header_color, R.color.simple_transparent_header_color,
                getResColor(context, R.color.blackPrimary), R.drawable.ic_add_black_24dp,
                R.drawable.ic_settings_black_24dp, R.drawable.ic_mic_black_24dp,
                getResColor(context, R.color.blackPrimary), R.color.simple_transparent_header_color, 0, "Transparent Dark", R.color.whitePrimary,
                getResColor(context, R.color.blackPrimary)));

        list.add(new EventsTheme(R.color.pinkAccent, R.color.material_grey,
                getResColor(context, R.color.whitePrimary), R.drawable.ic_add_white_24dp,
                R.drawable.ic_settings_white_24dp, R.drawable.ic_mic_white_24dp,
                getResColor(context, R.color.blackPrimary), R.color.whitePrimary, 1, "Simple Pink", R.color.whitePrimary,
                getResColor(context, R.color.blackPrimary)));
        return list;
    }

    public EventsTheme(Parcel in) {
        super();
        readFromParcel(in);
    }

    public final Creator<EventsTheme> CREATOR = new Creator<EventsTheme>() {
        public EventsTheme createFromParcel(Parcel in) {
            return new EventsTheme(in);
        }

        public EventsTheme[] newArray(int size) {

            return new EventsTheme[size];
        }

    };

    public void readFromParcel(Parcel in) {
        title = in.readString();
        backgroundColor = in.readInt();
        headerColor = in.readInt();
        titleColor = in.readInt();
        plusIcon = in.readInt();
        settingsIcon = in.readInt();
        voiceIcon = in.readInt();
        windowColor = in.readInt();
        windowTextColor = in.readInt();
        itemBackgroud = in.readInt();
        itemTextColor = in.readInt();
        checkboxColor = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeInt(headerColor);
        dest.writeInt(titleColor);
        dest.writeInt(plusIcon);
        dest.writeInt(backgroundColor);
        dest.writeInt(settingsIcon);
        dest.writeInt(voiceIcon);
        dest.writeInt(windowColor);
        dest.writeInt(windowTextColor);
        dest.writeInt(itemBackgroud);
        dest.writeInt(itemTextColor);
        dest.writeInt(checkboxColor);
    }
}
