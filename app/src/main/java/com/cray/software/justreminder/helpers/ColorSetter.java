package com.cray.software.justreminder.helpers;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.modules.Module;
import com.cray.software.justreminder.utils.ViewUtils;

public class ColorSetter {

    private Context mContext;
    private SharedPrefs sPrefs;

    public ColorSetter(Context context){
        this.mContext = context;
    }

    /**
     * Method to get typeface by style code;
     * @param style code of style
     * @return typeface
     */
    public Typeface getTypeface(int style){
        Typeface typeface;
        if (style == 0) {
            typeface = Typeface.createFromAsset(mContext.getAssets(), "fonts/Roboto-Black.ttf");
        } else if (style == 1) {
            typeface = Typeface.createFromAsset(mContext.getAssets(), "fonts/Roboto-BlackItalic.ttf");
        } else if (style == 2) {
            typeface = Typeface.createFromAsset(mContext.getAssets(), "fonts/Roboto-Bold.ttf");
        } else if (style == 3) {
            typeface = Typeface.createFromAsset(mContext.getAssets(), "fonts/Roboto-BoldItalic.ttf");
        } else if (style == 4) {
            typeface = Typeface.createFromAsset(mContext.getAssets(), "fonts/Roboto-Italic.ttf");
        } else if (style == 5) {
            typeface = Typeface.createFromAsset(mContext.getAssets(), "fonts/Roboto-Light.ttf");
        } else if (style == 6) {
            typeface = Typeface.createFromAsset(mContext.getAssets(), "fonts/Roboto-LightItalic.ttf");
        } else if (style == 7) {
            typeface = Typeface.createFromAsset(mContext.getAssets(), "fonts/Roboto-Medium.ttf");
        } else if (style == 8) {
            typeface = Typeface.createFromAsset(mContext.getAssets(), "fonts/Roboto-MediumItalic.ttf");
        } else if (style == 9) {
            typeface = Typeface.createFromAsset(mContext.getAssets(), "fonts/Roboto-Regular.ttf");
        } else if (style == 10) {
            typeface = Typeface.createFromAsset(mContext.getAssets(), "fonts/Roboto-Thin.ttf");
        } else if (style == 11) {
            typeface = Typeface.createFromAsset(mContext.getAssets(), "fonts/Roboto-ThinItalic.ttf");
        } else {
            typeface = Typeface.createFromAsset(mContext.getAssets(), "fonts/Roboto-Light.ttf");
        }
        return typeface;
    }

    /**
     * Get color from resource.
     * @param color resource.
     * @return Color
     */
    private int getColor(int color){
        return ViewUtils.getColor(mContext, color);
    }

    /**
     * Get current theme primary color.
     * @return Color
     */
    public int colorPrimary(){
        sPrefs = new SharedPrefs(mContext);
        String loadedColor = sPrefs.loadPrefs(Prefs.THEME);
        int color;
        switch (loadedColor) {
            case "1":
                color = getColor(R.color.redPrimary);
                break;
            case "2":
                color = getColor(R.color.purplePrimary);
                break;
            case "3":
                color = getColor(R.color.greenLightPrimary);
                break;
            case "4":
                color = getColor(R.color.greenPrimary);
                break;
            case "5":
                color = getColor(R.color.blueLightPrimary);
                break;
            case "6":
                color = getColor(R.color.bluePrimary);
                break;
            case "7":
                color = getColor(R.color.yellowPrimary);
                break;
            case "8":
                color = getColor(R.color.orangePrimary);
                break;
            case "9":
                color = getColor(R.color.cyanPrimary);
                break;
            case "10":
                color = getColor(R.color.pinkPrimary);
                break;
            case "11":
                color = getColor(R.color.tealPrimary);
                break;
            case "12":
                color = getColor(R.color.amberPrimary);
                break;
            default:
                if (Module.isPro()){
                    switch (loadedColor) {
                        case "13":
                            color = getColor(R.color.purpleDeepPrimary);
                            break;
                        case "14":
                            color = getColor(R.color.orangeDeepPrimary);
                            break;
                        case "15":
                            color = getColor(R.color.limePrimary);
                            break;
                        case "16":
                            color = getColor(R.color.indigoPrimary);
                            break;
                        default:
                            color = getColor(R.color.bluePrimary);
                            break;
                    }
                } else color = getColor(R.color.bluePrimary);
                break;
        }
        return color;
    }

    /**
     * Get current theme accent color.
     * @return Color
     */
    public int colorAccent(){
        String string = new SharedPrefs(mContext).loadPrefs(Prefs.THEME);
        int code;
        try {
            code = Integer.parseInt(string) - 1;
        } catch (Exception e){
            code = 5;
            e.printStackTrace();
        }
        return colorAccent(code);
    }

    /**
     * Get accent color by code.
     * @return Color
     */
    public int colorAccent(int code){
        int color;
        boolean isDark = new SharedPrefs(mContext).loadBoolean(Prefs.USE_DARK_THEME);
        if (isDark) {
            switch (code) {
                case 0:
                    color = R.color.indigoAccent;
                    break;
                case 1:
                    color = R.color.amberAccent;
                    break;
                case 2:
                    color = R.color.pinkAccent;
                    break;
                case 3:
                    color = R.color.purpleAccent;
                    break;
                case 4:
                    color = R.color.yellowAccent;
                    break;
                case 5:
                    color = R.color.redAccent;
                    break;
                case 6:
                    color = R.color.redAccent;
                    break;
                case 7:
                    color = R.color.greenAccent;
                    break;
                case 8:
                    color = R.color.purpleDeepAccent;
                    break;
                case 9:
                    color = R.color.blueLightAccent;
                    break;
                case 10:
                    color = R.color.pinkAccent;
                    break;
                case 11:
                    color = R.color.blueAccent;
                    break;
                default:
                    if (Module.isPro()) {
                        switch (code) {
                            case 12:
                                color = R.color.greenAccent;
                                break;
                            case 13:
                                color = R.color.purpleAccent;
                                break;
                            case 14:
                                color = R.color.redAccent;
                                break;
                            case 15:
                                color = R.color.pinkAccent;
                                break;
                            default:
                                color = R.color.redAccent;
                                break;
                        }
                    } else color = R.color.redAccent;
                    break;
            }
        } else {
            switch (code) {
                case 0:
                    color = R.color.indigoAccent;
                    break;
                case 1:
                    color = R.color.amberAccent;
                    break;
                case 2:
                    color = R.color.purpleDeepAccent;
                    break;
                case 3:
                    color = R.color.cyanAccent;
                    break;
                case 4:
                    color = R.color.pinkAccent;
                    break;
                case 5:
                    color = R.color.yellowAccent;
                    break;
                case 6:
                    color = R.color.cyanAccent;
                    break;
                case 7:
                    color = R.color.pinkAccent;
                    break;
                case 8:
                    color = R.color.redAccent;
                    break;
                case 9:
                    color = R.color.cyanAccent;
                    break;
                case 10:
                    color = R.color.redAccent;
                    break;
                case 11:
                    color = R.color.indigoAccent;
                    break;
                default:
                    if (Module.isPro()) {
                        switch (code) {
                            case 12:
                                color = R.color.greenLightAccent;
                                break;
                            case 13:
                                color = R.color.purpleDeepAccent;
                                break;
                            case 14:
                                color = R.color.purpleAccent;
                                break;
                            case 15:
                                color = R.color.pinkAccent;
                                break;
                            default:
                                color = R.color.yellowAccent;
                                break;
                        }
                    } else color = R.color.yellowAccent;
                    break;
            }
        }
        return getColor(color);
    }

    /**
     * Get theme for application based on user choice.
     * @return Theme resource
     */
    public int getStyle(){
        int id;
        sPrefs = new SharedPrefs(mContext);
        String loadedColor = sPrefs.loadPrefs(Prefs.THEME);
        boolean isDark = sPrefs.loadBoolean(Prefs.USE_DARK_THEME);
        if (isDark) {
            switch (loadedColor) {
                case "1":
                    id = R.style.HomeDark_Red;
                    break;
                case "2":
                    id = R.style.HomeDark_Purple;
                    break;
                case "3":
                    id = R.style.HomeDark_LightGreen;
                    break;
                case "4":
                    id = R.style.HomeDark_Green;
                    break;
                case "5":
                    id = R.style.HomeDark_LightBlue;
                    break;
                case "6":
                    id = R.style.HomeDark_Blue;
                    break;
                case "7":
                    id = R.style.HomeDark_Yellow;
                    break;
                case "8":
                    id = R.style.HomeDark_Orange;
                    break;
                case "9":
                    id = R.style.HomeDark_Cyan;
                    break;
                case "10":
                    id = R.style.HomeDark_Pink;
                    break;
                case "11":
                    id = R.style.HomeDark_Teal;
                    break;
                case "12":
                    id = R.style.HomeDark_Amber;
                    break;
                default:
                    if (Module.isPro()) {
                        switch (loadedColor) {
                            case "13":
                                id = R.style.HomeDark_DeepPurple;
                                break;
                            case "14":
                                id = R.style.HomeDark_DeepOrange;
                                break;
                            case "15":
                                id = R.style.HomeDark_Lime;
                                break;
                            case "16":
                                id = R.style.HomeDark_Indigo;
                                break;
                            default:
                                id = R.style.HomeDark_Blue;
                                break;
                        }
                    } else id = R.style.HomeDark_Blue;
                    break;
            }
        } else {
            switch (loadedColor) {
                case "1":
                    id = R.style.HomeWhite_Red;
                    break;
                case "2":
                    id = R.style.HomeWhite_Purple;
                    break;
                case "3":
                    id = R.style.HomeWhite_LightGreen;
                    break;
                case "4":
                    id = R.style.HomeWhite_Green;
                    break;
                case "5":
                    id = R.style.HomeWhite_LightBlue;
                    break;
                case "6":
                    id = R.style.HomeWhite_Blue;
                    break;
                case "7":
                    id = R.style.HomeWhite_Yellow;
                    break;
                case "8":
                    id = R.style.HomeWhite_Orange;
                    break;
                case "9":
                    id = R.style.HomeWhite_Cyan;
                    break;
                case "10":
                    id = R.style.HomeWhite_Pink;
                    break;
                case "11":
                    id = R.style.HomeWhite_Teal;
                    break;
                case "12":
                    id = R.style.HomeWhite_Amber;
                    break;
                default:
                    if (Module.isPro()) {
                        switch (loadedColor) {
                            case "13":
                                id = R.style.HomeWhite_DeepPurple;
                                break;
                            case "14":
                                id = R.style.HomeWhite_DeepOrange;
                                break;
                            case "15":
                                id = R.style.HomeWhite_Lime;
                                break;
                            case "16":
                                id = R.style.HomeWhite_Indigo;
                                break;
                            default:
                                id = R.style.HomeWhite_Blue;
                                break;
                        }
                    } else id = R.style.HomeWhite_Blue;
                    break;
            }
        }
        return id;
    }

    /**
     * Get birthdays color for calendar.
     * @return Color Resource
     */
    public int colorBirthdayCalendar(){
        sPrefs = new SharedPrefs(mContext);
        String loadedColor = sPrefs.loadPrefs(Prefs.BIRTHDAY_COLOR);
        int color;
        switch (loadedColor) {
            case "1":
                color = R.color.redPrimary;
                break;
            case "2":
                color = R.color.purplePrimary;
                break;
            case "3":
                color = R.color.greenLightPrimary;
                break;
            case "4":
                color = R.color.greenPrimary;
                break;
            case "5":
                color = R.color.blueLightPrimary;
                break;
            case "6":
                color = R.color.bluePrimary;
                break;
            case "7":
                color = R.color.yellowPrimary;
                break;
            case "8":
                color = R.color.orangePrimary;
                break;
            case "9":
                color = R.color.cyanPrimary;
                break;
            case "10":
                color = R.color.pinkPrimary;
                break;
            case "11":
                color = R.color.tealPrimary;
                break;
            case "12":
                color = R.color.amberPrimary;
                break;
            default:
                if (Module.isPro()){
                    switch (loadedColor) {
                        case "13":
                            color = R.color.purpleDeepPrimary;
                            break;
                        case "14":
                            color = R.color.orangeDeepPrimary;
                            break;
                        case "15":
                            color = R.color.limePrimary;
                            break;
                        case "16":
                            color = R.color.indigoPrimary;
                            break;
                        default:
                            color = R.color.greenPrimary;
                            break;
                    }
                } else {
                    color = R.color.greenPrimary;
                }
                break;
        }
        return color;
    }

    /**
     * Get reminders color for calendar.
     * @return Color Resource
     */
    public int colorReminderCalendar(){
        sPrefs = new SharedPrefs(mContext);
        String loadedColor = sPrefs.loadPrefs(Prefs.REMINDERS_COLOR);
        int color;
        switch (loadedColor) {
            case "1":
                color = R.color.redPrimary;
                break;
            case "2":
                color = R.color.purplePrimary;
                break;
            case "3":
                color = R.color.greenLightPrimary;
                break;
            case "4":
                color = R.color.greenPrimary;
                break;
            case "5":
                color = R.color.blueLightPrimary;
                break;
            case "6":
                color = R.color.bluePrimary;
                break;
            case "7":
                color = R.color.yellowPrimary;
                break;
            case "8":
                color = R.color.orangePrimary;
                break;
            case "9":
                color = R.color.cyanPrimary;
                break;
            case "10":
                color = R.color.pinkPrimary;
                break;
            case "11":
                color = R.color.tealPrimary;
                break;
            case "12":
                color = R.color.amberPrimary;
                break;
            default:
                if (Module.isPro()){
                    switch (loadedColor) {
                        case "13":
                            color = R.color.purpleDeepPrimary;
                            break;
                        case "14":
                            color = R.color.orangeDeepPrimary;
                            break;
                        case "15":
                            color = R.color.limePrimary;
                            break;
                        case "16":
                            color = R.color.indigoPrimary;
                            break;
                        default:
                            color = R.color.greenPrimary;
                            break;
                    }
                } else {
                    color = R.color.greenPrimary;
                }
                break;
        }
        return color;
    }

    /**
     * Get RGB color from color resource.
     * @return Color Resource
     */
    public String getRGB(int color){
        String rgb;
        switch (color) {
            case R.color.redPrimary:
                rgb = "F44336";
                break;
            case R.color.purplePrimary:
                rgb = "9C27B0";
                break;
            case R.color.greenLightPrimary:
                rgb = "8BC34A";
                break;
            case R.color.greenPrimary:
                rgb = "4CAF50";
                break;
            case R.color.blueLightPrimary:
                rgb = "03A9F4";
                break;
            case R.color.bluePrimary:
                rgb = "2196F3";
                break;
            case R.color.yellowPrimary:
                rgb = "FFEB3B";
                break;
            case R.color.orangePrimary:
                rgb = "FF9800";
                break;
            case R.color.cyanPrimary:
                rgb = "00BCD4";
                break;
            case R.color.pinkPrimary:
                rgb = "E91E63";
                break;
            case R.color.tealPrimary:
                rgb = "009688";
                break;
            case R.color.amberPrimary:
                rgb = "FFC107";
                break;
            default:
                if (Module.isPro()){
                    switch (color) {
                        case R.color.purpleDeepPrimary:
                            rgb = "673AB7";
                            break;
                        case R.color.orangeDeepPrimary:
                            rgb = "FF5722";
                            break;
                        case R.color.limePrimary:
                            rgb = "CDDC39";
                            break;
                        case R.color.indigoPrimary:
                            rgb = "3F51B5";
                            break;
                        default:
                            rgb = "4CAF50";
                            break;
                    }
                } else {
                    rgb = "4CAF50";
                }
                break;
        }
        return rgb;
    }

    /**
     * Get current day color for calendar.
     * @return Color Resource
     */
    public int colorCurrentCalendar(){
        sPrefs = new SharedPrefs(mContext);
        String loadedColor = sPrefs.loadPrefs(Prefs.CURRENT_COLOR);
        int color;
        switch (loadedColor) {
            case "1":
                color = R.color.redPrimary;
                break;
            case "2":
                color = R.color.purplePrimary;
                break;
            case "3":
                color = R.color.greenLightPrimary;
                break;
            case "4":
                color = R.color.greenPrimary;
                break;
            case "5":
                color = R.color.blueLightPrimary;
                break;
            case "6":
                color = R.color.bluePrimary;
                break;
            case "7":
                color = R.color.yellowPrimary;
                break;
            case "8":
                color = R.color.orangePrimary;
                break;
            case "9":
                color = R.color.cyanPrimary;
                break;
            case "10":
                color = R.color.pinkPrimary;
                break;
            case "11":
                color = R.color.tealPrimary;
                break;
            case "12":
                color = R.color.amberPrimary;
                break;
            default:
                if (Module.isPro()){
                    switch (loadedColor) {
                        case "13":
                            color = R.color.purpleDeepPrimary;
                            break;
                        case "14":
                            color = R.color.orangeDeepPrimary;
                            break;
                        case "15":
                            color = R.color.limePrimary;
                            break;
                        case "16":
                            color = R.color.indigoPrimary;
                            break;
                        default:
                            color = R.color.greenPrimary;
                            break;
                    }
                } else {
                    color = R.color.greenPrimary;
                }
                break;
        }
        return color;
    }

    /**
     * Get color indicator by identifier.
     * @param color color identifier.
     * @return Drawable identifier
     */
    public int getIndicator(String color){
        int drawable;
        switch (color) {
            case "1":
                drawable = R.drawable.drawable_red;
                break;
            case "2":
                drawable = R.drawable.drawable_purple;
                break;
            case "3":
                drawable = R.drawable.drawable_green_light;
                break;
            case "4":
                drawable = R.drawable.drawable_green;
                break;
            case "5":
                drawable = R.drawable.drawable_blue_light;
                break;
            case "6":
                drawable = R.drawable.drawable_blue;
                break;
            case "7":
                drawable = R.drawable.drawable_yellow;
                break;
            case "8":
                drawable = R.drawable.drawable_orange;
                break;
            case "9":
                drawable = R.drawable.drawable_cyan;
                break;
            case "10":
                drawable = R.drawable.drawable_pink;
                break;
            case "11":
                drawable = R.drawable.drawable_teal;
                break;
            case "12":
                drawable = R.drawable.drawable_amber;
                break;
            case "13":
                drawable = R.drawable.drawable_deep_purple;
                break;
            case "14":
                drawable = R.drawable.drawable_deep_orange;
                break;
            case "15":
                drawable = R.drawable.drawable_lime;
                break;
            case "16":
                drawable = R.drawable.drawable_indigo;
                break;
            default:
                drawable = R.drawable.drawable_blue;
                break;
        }
        return drawable;
    }

    /**
     * Get drawable from resource.
     * @param i resource.
     * @return Drawable
     */
    private Drawable getDrawable(int i){
        return ViewUtils.getDrawable(mContext, i);
    }

    /**
     * Get style for ToggleButton.
     * @return Drawable
     */
    public Drawable toggleDrawable(){
        sPrefs = new SharedPrefs(mContext);
        String loadedColor = sPrefs.loadPrefs(Prefs.THEME);
        Drawable color;
        switch (loadedColor) {
            case "1":
                color = getDrawable(R.drawable.toggle_red);
                break;
            case "2":
                color = getDrawable(R.drawable.toggle_purple);
                break;
            case "3":
                color = getDrawable(R.drawable.toggle_green_light);
                break;
            case "4":
                color = getDrawable(R.drawable.toggle_green);
                break;
            case "5":
                color = getDrawable(R.drawable.toggle_blue_light);
                break;
            case "6":
                color = getDrawable(R.drawable.toggle_blue);
                break;
            case "7":
                color = getDrawable(R.drawable.toggle_yellow);
                break;
            case "8":
                color = getDrawable(R.drawable.toggle_orange);
                break;
            case "9":
                color = getDrawable(R.drawable.toggle_cyan);
                break;
            case "10":
                color = getDrawable(R.drawable.toggle_pink);
                break;
            case "11":
                color = getDrawable(R.drawable.toggle_teal);
                break;
            case "12":
                color = getDrawable(R.drawable.toggle_amber);
                break;
            default:
                if (Module.isPro()){
                    switch (loadedColor) {
                        case "13":
                            color = getDrawable(R.drawable.toggle_deep_purple);
                            break;
                        case "14":
                            color = getDrawable(R.drawable.toggle_deep_orange);
                            break;
                        case "15":
                            color = getDrawable(R.drawable.toggle_lime);
                            break;
                        case "16":
                            color = getDrawable(R.drawable.toggle_indigo);
                            break;
                        default:
                            color = getDrawable(R.drawable.toggle_blue);
                            break;
                    }
                } else color = getDrawable(R.drawable.toggle_blue);
                break;
        }
        return color;
    }

    /**
     * Get status bar color based on current application theme.
     * @return Color
     */
    public int colorPrimaryDark(){
        sPrefs = new SharedPrefs(mContext);
        String loadedColor = sPrefs.loadPrefs(Prefs.THEME);
        int color;
        switch (loadedColor) {
            case "1":
                color = getColor(R.color.redPrimaryDark);
                break;
            case "2":
                color = getColor(R.color.purplePrimaryDark);
                break;
            case "3":
                color = getColor(R.color.greenLightPrimaryDark);
                break;
            case "4":
                color = getColor(R.color.greenPrimaryDark);
                break;
            case "5":
                color = getColor(R.color.blueLightPrimaryDark);
                break;
            case "6":
                color = getColor(R.color.bluePrimaryDark);
                break;
            case "7":
                color = getColor(R.color.yellowPrimaryDark);
                break;
            case "8":
                color = getColor(R.color.orangePrimaryDark);
                break;
            case "9":
                color = getColor(R.color.cyanPrimaryDark);
                break;
            case "10":
                color = getColor(R.color.pinkPrimaryDark);
                break;
            case "11":
                color = getColor(R.color.tealPrimaryDark);
                break;
            case "12":
                color = getColor(R.color.amberPrimaryDark);
                break;
            default:
                if (Module.isPro()){
                    switch (loadedColor) {
                        case "13":
                            color = getColor(R.color.purpleDeepPrimaryDark);
                            break;
                        case "14":
                            color = getColor(R.color.orangeDeepPrimaryDark);
                            break;
                        case "15":
                            color = getColor(R.color.limePrimaryDark);
                            break;
                        case "16":
                            color = getColor(R.color.indigoPrimaryDark);
                            break;
                        default:
                            color = getColor(R.color.bluePrimaryDark);
                            break;
                    }
                } else color = getColor(R.color.bluePrimaryDark);
                break;
        }
        return color;
    }

    /**
     * Get style for spinner based on current theme.
     * @return Color
     */
    public int getSpinnerStyle(){
        int color;
        sPrefs = new SharedPrefs(mContext);
        if (sPrefs.loadBoolean(Prefs.USE_DARK_THEME)) {
            color = getColor(R.color.material_grey);
        } else color = getColor(R.color.whitePrimary);
        return color;
    }

    /**
     * Get theme for dialog styled activity based on current application theme.
     * @return Theme resource
     */
    public int getDialogStyle(){
        int id;
        sPrefs = new SharedPrefs(mContext);
        String loadedColor = sPrefs.loadPrefs(Prefs.THEME);
        boolean isDark = sPrefs.loadBoolean(Prefs.USE_DARK_THEME);
        if (isDark) {
            switch (loadedColor) {
                case "1":
                    id = R.style.HomeDarkDialog_Red;
                    break;
                case "2":
                    id = R.style.HomeDarkDialog_Purple;
                    break;
                case "3":
                    id = R.style.HomeDarkDialog_LightGreen;
                    break;
                case "4":
                    id = R.style.HomeDarkDialog_Green;
                    break;
                case "5":
                    id = R.style.HomeDarkDialog_LightBlue;
                    break;
                case "6":
                    id = R.style.HomeDarkDialog_Blue;
                    break;
                case "7":
                    id = R.style.HomeDarkDialog_Yellow;
                    break;
                case "8":
                    id = R.style.HomeDarkDialog_Orange;
                    break;
                case "9":
                    id = R.style.HomeDarkDialog_Cyan;
                    break;
                case "10":
                    id = R.style.HomeDarkDialog_Pink;
                    break;
                case "11":
                    id = R.style.HomeDarkDialog_Teal;
                    break;
                case "12":
                    id = R.style.HomeDarkDialog_Amber;
                    break;
                default:
                    if (Module.isPro()) {
                        switch (loadedColor) {
                            case "13":
                                id = R.style.HomeDarkDialog_DeepPurple;
                                break;
                            case "14":
                                id = R.style.HomeDarkDialog_DeepOrange;
                                break;
                            case "15":
                                id = R.style.HomeDarkDialog_Lime;
                                break;
                            case "16":
                                id = R.style.HomeDarkDialog_Indigo;
                                break;
                            default:
                                id = R.style.HomeDarkDialog_Blue;
                                break;
                        }
                    } else id = R.style.HomeDarkDialog_Blue;
                    break;
            }
        } else {
            switch (loadedColor) {
                case "1":
                    id = R.style.HomeWhiteDialog_Red;
                    break;
                case "2":
                    id = R.style.HomeWhiteDialog_Purple;
                    break;
                case "3":
                    id = R.style.HomeWhiteDialog_LightGreen;
                    break;
                case "4":
                    id = R.style.HomeWhiteDialog_Green;
                    break;
                case "5":
                    id = R.style.HomeWhiteDialog_LightBlue;
                    break;
                case "6":
                    id = R.style.HomeWhiteDialog_Blue;
                    break;
                case "7":
                    id = R.style.HomeWhiteDialog_Yellow;
                    break;
                case "8":
                    id = R.style.HomeWhiteDialog_Orange;
                    break;
                case "9":
                    id = R.style.HomeWhiteDialog_Cyan;
                    break;
                case "10":
                    id = R.style.HomeWhiteDialog_Pink;
                    break;
                case "11":
                    id = R.style.HomeWhiteDialog_Teal;
                    break;
                case "12":
                    id = R.style.HomeWhiteDialog_Amber;
                    break;
                default:
                    if (Module.isPro()) {
                        switch (loadedColor) {
                            case "13":
                                id = R.style.HomeWhiteDialog_DeepPurple;
                                break;
                            case "14":
                                id = R.style.HomeWhiteDialog_DeepOrange;
                                break;
                            case "15":
                                id = R.style.HomeWhiteDialog_Lime;
                                break;
                            case "16":
                                id = R.style.HomeWhiteDialog_Indigo;
                                break;
                            default:
                                id = R.style.HomeWhiteDialog_Blue;
                                break;
                        }
                    } else id = R.style.HomeWhiteDialog_Blue;
                    break;
            }
        }
        return id;
    }

    /**
     * Get theme for fullscreen activities.
     * @return Theme resource
     */
    public int getFullscreenStyle(){
        int id;
        sPrefs = new SharedPrefs(mContext);
        if (sPrefs.loadBoolean(Prefs.USE_DARK_THEME)) {
            id = R.style.HomeDarkFullscreen;
        } else id = R.style.HomeWhiteFullscreen;
        return id;
    }

    /**
     * Get theme for translucent activities.
     * @return Theme resource
     */
    public int getTransparentStyle(){
        int id;
        sPrefs = new SharedPrefs(mContext);
        if (sPrefs.loadBoolean(Prefs.USE_DARK_THEME)) {
            id = R.style.HomeDarkTranslucent;
        } else id = R.style.HomeWhiteTranslucent;
        return id;
    }

    /**
     * Get window background color based on current theme.
     * @return Color
     */
    public int getBackgroundStyle(){
        int id;
        sPrefs = new SharedPrefs(mContext);
        if (sPrefs.loadBoolean(Prefs.USE_DARK_THEME)) {
            id = getColor(R.color.material_grey);
        } else id = getColor(R.color.material_white);
        return id;
    }

    /**
     * Get status bar color for reminder window based on current theme.
     * @return Color
     */
    public int getStatusBarStyle(){
        int id;
        sPrefs = new SharedPrefs(mContext);
        if (sPrefs.loadBoolean(Prefs.USE_DARK_THEME)) {
            id = getColor(R.color.material_grey);
        } else id = getColor(R.color.material_divider);
        return id;
    }

    /**
     * Get background color for CardView based on current theme.
     * @return Color
     */
    public int getCardStyle(){
        int color;
        sPrefs = new SharedPrefs(mContext);
        if (sPrefs.loadBoolean(Prefs.USE_DARK_THEME)) {
            color = getColor(R.color.grey_x);
        } else color = getColor(R.color.whitePrimary);
        return color;
    }

    /**
     * Get card-like background drawable based on current theme.
     * @return Drawable resource
     */
    public int getCardDrawableStyle(){
        int color;
        sPrefs = new SharedPrefs(mContext);
        if (sPrefs.loadBoolean(Prefs.USE_DARK_THEME)) {
            color = R.drawable.card_bg_dark;
        } else color = R.drawable.card_bg;
        return color;
    }

    /**
     * Get screen orientation, depends on user settings.
     * @return Integer
     */
    public int getRequestOrientation(){
        int i;
        sPrefs = new SharedPrefs(mContext);
        String prefs = sPrefs.loadPrefs(Prefs.SCREEN);
        if (prefs.matches(Constants.SCREEN_PORTRAIT)){
            i = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        } else if (prefs.matches(Constants.SCREEN_LANDSCAPE)){
            i = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        } else if (prefs.matches(Constants.SCREEN_AUTO)){
            i = ActivityInfo.SCREEN_ORIENTATION_SENSOR;
        } else i = ActivityInfo.SCREEN_ORIENTATION_SENSOR;
        return i;
    }

    /**
     * Get fill amd stroke color for drawing circle around marker on Google Map.
     * @return color resources array
     */
    public int[] getMarkerRadiusStyle(){
        int fillColor;
        int strokeColor;
        if (Module.isPro()) {
            sPrefs = new SharedPrefs(mContext);
            int color = sPrefs.loadInt(Prefs.MARKER_STYLE);
            if (color == 1) {
                fillColor = R.color.redA100;
                strokeColor = R.color.redPrimaryDark;
            } else if (color == 2) {
                fillColor = R.color.greenA100;
                strokeColor = R.color.greenPrimaryDark;
            } else if (color == 3) {
                fillColor = R.color.blueA100;
                strokeColor = R.color.bluePrimaryDark;
            } else if (color == 4) {
                fillColor = R.color.yellowA100;
                strokeColor = R.color.yellowPrimaryDark;
            } else if (color == 5) {
                fillColor = R.color.greenLightA100;
                strokeColor = R.color.greenLightPrimaryDark;
            } else if (color == 6) {
                fillColor = R.color.blueLightA100;
                strokeColor = R.color.blueLightPrimaryDark;
            } else if (color == 7) {
                fillColor = R.color.cyanA100;
                strokeColor = R.color.cyanPrimaryDark;
            } else if (color == 8) {
                fillColor = R.color.purpleA100;
                strokeColor = R.color.purplePrimaryDark;
            } else if (color == 9) {
                fillColor = R.color.amberA100;
                strokeColor = R.color.amberPrimaryDark;
            } else if (color == 10) {
                fillColor = R.color.orangeA100;
                strokeColor = R.color.orangePrimaryDark;
            } else if (color == 11) {
                fillColor = R.color.pinkA100;
                strokeColor = R.color.pinkPrimaryDark;
            } else if (color == 12) {
                fillColor = R.color.tealA100;
                strokeColor = R.color.tealPrimaryDark;
            } else if (color == 13) {
                fillColor = R.color.purpleDeepA100;
                strokeColor = R.color.purpleDeepPrimaryDark;
            } else if (color == 14) {
                fillColor = R.color.orangeDeepA100;
                strokeColor = R.color.orangeDeepPrimaryDark;
            } else if (color == 15) {
                fillColor = R.color.indigoA100;
                strokeColor = R.color.indigoPrimaryDark;
            } else if (color == 16) {
                fillColor = R.color.limeA100;
                strokeColor = R.color.limePrimaryDark;
            } else {
                fillColor = R.color.blueA100;
                strokeColor = R.color.bluePrimaryDark;
            }
        } else {
            fillColor = R.color.blueA100;
            strokeColor = R.color.bluePrimaryDark;
        }
        return new int[]{fillColor, strokeColor};
    }

    /**
     * Get fill amd stroke color by marker color, for drawing circle around marker on Google Map.
     * @param color marker color.
     * @return  color resources array
     */
    public int[] getMarkerRadiusStyle(int color){
        int fillColor;
        int strokeColor;
        if (color == 1) {
            fillColor = R.color.redA100;
            strokeColor = R.color.redPrimaryDark;
        } else if (color == 2) {
            fillColor = R.color.greenA100;
            strokeColor = R.color.greenPrimaryDark;
        } else if (color == 3) {
            fillColor = R.color.blueA100;
            strokeColor = R.color.bluePrimaryDark;
        } else if (color == 4) {
            fillColor = R.color.yellowA100;
            strokeColor = R.color.yellowPrimaryDark;
        } else if (color == 5) {
            fillColor = R.color.greenLightA100;
            strokeColor = R.color.greenLightPrimaryDark;
        } else if (color == 6) {
            fillColor = R.color.blueLightA100;
            strokeColor = R.color.blueLightPrimaryDark;
        } else if (color == 7) {
            fillColor = R.color.cyanA100;
            strokeColor = R.color.cyanPrimaryDark;
        } else if (color == 8) {
            fillColor = R.color.purpleA100;
            strokeColor = R.color.purplePrimaryDark;
        } else if (color == 9) {
            fillColor = R.color.amberA100;
            strokeColor = R.color.amberPrimaryDark;
        } else if (color == 10) {
            fillColor = R.color.orangeA100;
            strokeColor = R.color.orangePrimaryDark;
        } else if (color == 11) {
            fillColor = R.color.pinkA100;
            strokeColor = R.color.pinkPrimaryDark;
        } else if (color == 12) {
            fillColor = R.color.tealA100;
            strokeColor = R.color.tealPrimaryDark;
        } else if (color == 13) {
            fillColor = R.color.purpleDeepA100;
            strokeColor = R.color.purpleDeepPrimaryDark;
        } else if (color == 14) {
            fillColor = R.color.orangeDeepA100;
            strokeColor = R.color.orangeDeepPrimaryDark;
        } else if (color == 15) {
            fillColor = R.color.indigoA100;
            strokeColor = R.color.indigoPrimaryDark;
        } else if (color == 16) {
            fillColor = R.color.limeA100;
            strokeColor = R.color.limePrimaryDark;
        } else {
            fillColor = R.color.blueA100;
            strokeColor = R.color.bluePrimaryDark;
        }
        return new int[]{fillColor, strokeColor};
    }

    /**
     * Get marker icon, based on user settings.
     * @return Drawable resource
     */
    public int getMarkerStyle(){
        int color;
        if (Module.isPro()) {
            sPrefs = new SharedPrefs(mContext);
            int loaded = sPrefs.loadInt(Prefs.MARKER_STYLE);
            if (loaded == 1) {
                color = R.drawable.marker_red;
            } else if (loaded == 2) {
                color = R.drawable.marker_green;
            } else if (loaded == 3) {
                color = R.drawable.marker_blue;
            } else if (loaded == 4) {
                color = R.drawable.marker_yellow;
            } else if (loaded == 5) {
                color = R.drawable.marker_green_light;
            } else if (loaded == 6) {
                color = R.drawable.marker_blue_light;
            } else if (loaded == 7) {
                color = R.drawable.marker_grey;
            } else if (loaded == 8) {
                color = R.drawable.marker_violet;
            } else if (loaded == 9) {
                color = R.drawable.marker_brown;
            } else if (loaded == 10) {
                color = R.drawable.marker_orange;
            } else if (loaded == 11) {
                color = R.drawable.marker_pink;
            } else if (loaded == 12) {
                color = R.drawable.marker_teal;
            } else if (loaded == 13) {
                color = R.drawable.marker_deep_purple;
            } else if (loaded == 14) {
                color = R.drawable.marker_deep_orange;
            } else if (loaded == 15) {
                color = R.drawable.marker_indigo;
            } else if (loaded == 16) {
                color = R.drawable.marker_lime;
            } else {
                color = R.drawable.marker_blue;
            }
        } else {
            color = R.drawable.marker_blue;
        }
        return color;
    }

    /**
     * Get marker icon by code.
     * @param code code of marker icon.
     * @return Drawable resource
     */
    public int getMarkerStyle(int code){
        int color;
        if (code == 1) {
            color = R.drawable.marker_red;
        } else if (code == 2) {
            color = R.drawable.marker_green;
        } else if (code == 3) {
            color = R.drawable.marker_blue;
        } else if (code == 4) {
            color = R.drawable.marker_yellow;
        } else if (code == 5) {
            color = R.drawable.marker_green_light;
        } else if (code == 6) {
            color = R.drawable.marker_blue_light;
        } else if (code == 7) {
            color = R.drawable.marker_grey;
        } else if (code == 8) {
            color = R.drawable.marker_violet;
        } else if (code == 9) {
            color = R.drawable.marker_brown;
        } else if (code == 10) {
            color = R.drawable.marker_orange;
        } else if (code == 11) {
            color = R.drawable.marker_pink;
        } else if (code == 12) {
            color = R.drawable.marker_teal;
        } else if (code == 13) {
            color = R.drawable.marker_deep_purple;
        } else if (code == 14) {
            color = R.drawable.marker_deep_orange;
        } else if (code == 15) {
            color = R.drawable.marker_indigo;
        } else if (code == 16) {
            color = R.drawable.marker_lime;
        } else {
            color = R.drawable.marker_blue;
        }
        return color;
    }

    /**
     * Get reminder group indicator by code.
     * @param code indicator code.
     * @return Drawable resource
     */
    public int getCategoryIndicator(int code){
        int color;
        switch (code){
            case 0:
                color = R.drawable.circle_red;
                break;
            case 1:
                color = R.drawable.circle_purple;
                break;
            case 2:
                color = R.drawable.circle_green;
                break;
            case 3:
                color = R.drawable.circle_green_light;
                break;
            case 4:
                color = R.drawable.circle_blue;
                break;
            case 5:
                color = R.drawable.circle_blue_light;
                break;
            case 6:
                color = R.drawable.circle_yellow;
                break;
            case 7:
                color = R.drawable.circle_orange;
                break;
            case 8:
                color = R.drawable.circle_cyan;
                break;
            case 9:
                color = R.drawable.circle_pink;
                break;
            case 10:
                color = R.drawable.circle_teal;
                break;
            case 11:
                color = R.drawable.circle_amber;
                break;
            default:
                if (Module.isPro()){
                    switch (code){
                        case 12:
                            color = R.drawable.circle_deep_purple;
                            break;
                        case 13:
                            color = R.drawable.circle_deep_orange;
                            break;
                        case 14:
                            color = R.drawable.circle_lime;
                            break;
                        case 15:
                            color = R.drawable.circle_indigo;
                            break;
                        default:
                            color = R.drawable.circle_blue;
                            break;
                    }
                } else color = R.drawable.circle_blue;
                break;
        }
        return color;
    }

    /**
     * Get reminder group color by code.
     * @param code indicator code.
     * @return Color resource
     */
    public int getCategoryColor(int code){
        int color;
        switch (code){
            case 0:
                color = R.color.redPrimary;
                break;
            case 1:
                color = R.color.purplePrimary;
                break;
            case 2:
                color = R.color.greenPrimary;
                break;
            case 3:
                color = R.color.greenLightPrimary;
                break;
            case 4:
                color = R.color.bluePrimary;
                break;
            case 5:
                color = R.color.blueLightPrimary;
                break;
            case 6:
                color = R.color.yellowPrimary;
                break;
            case 7:
                color = R.color.orangePrimary;
                break;
            case 8:
                color = R.color.cyanPrimary;
                break;
            case 9:
                color = R.color.pinkPrimary;
                break;
            case 10:
                color = R.color.tealPrimary;
                break;
            case 11:
                color = R.color.amberPrimary;
                break;
            default:
                if (Module.isPro()){
                    switch (code){
                        case 12:
                            color = R.color.purpleDeepPrimary;
                            break;
                        case 13:
                            color = R.color.orangeDeepPrimary;
                            break;
                        case 14:
                            color = R.color.limePrimary;
                            break;
                        case 15:
                            color = R.color.indigoPrimary;
                            break;
                        default:
                            color = R.color.bluePrimary;
                            break;
                    }
                } else color = R.color.bluePrimary;
                break;
        }
        return color;
    }

    /**
     * Get note primary color by code.
     * @param code color code.
     * @return Color resource
     */
    public int getNoteColor(int code){
        int color;
        switch (code){
            case 0:
                color = R.color.redPrimary;
                break;
            case 1:
                color = R.color.purplePrimary;
                break;
            case 2:
                color = R.color.greenPrimary;
                break;
            case 3:
                color = R.color.greenLightPrimary;
                break;
            case 4:
                color = R.color.bluePrimary;
                break;
            case 5:
                color = R.color.blueLightPrimary;
                break;
            case 6:
                color = R.color.yellowPrimary;
                break;
            case 7:
                color = R.color.orangePrimary;
                break;
            case 8:
                color = R.color.cyanPrimary;
                break;
            case 9:
                color = R.color.pinkPrimary;
                break;
            case 10:
                color = R.color.tealPrimary;
                break;
            case 11:
                color = R.color.amberPrimary;
                break;
            default:
                if (Module.isPro()){
                    switch (code){
                        case 12:
                            color = R.color.purpleDeepPrimary;
                            break;
                        case 13:
                            color = R.color.orangeDeepPrimary;
                            break;
                        case 14:
                            color = R.color.limePrimary;
                            break;
                        case 15:
                            color = R.color.indigoPrimary;
                            break;
                        default:
                            color = R.color.bluePrimary;
                            break;
                    }
                } else color = R.color.bluePrimary;
                break;
        }
        return getColor(color);
    }

    /**
     * Get note dark color by color code.
     * @param code color code.
     * @return Color
     */
    public int getNoteDarkColor(int code){
        int color;
        switch (code){
            case 0:
                color = R.color.redPrimaryDark;
                break;
            case 1:
                color = R.color.purplePrimaryDark;
                break;
            case 2:
                color = R.color.greenPrimaryDark;
                break;
            case 3:
                color = R.color.greenLightPrimaryDark;
                break;
            case 4:
                color = R.color.bluePrimaryDark;
                break;
            case 5:
                color = R.color.blueLightPrimaryDark;
                break;
            case 6:
                color = R.color.yellowPrimaryDark;
                break;
            case 7:
                color = R.color.orangePrimaryDark;
                break;
            case 8:
                color = R.color.cyanPrimaryDark;
                break;
            case 9:
                color = R.color.pinkPrimaryDark;
                break;
            case 10:
                color = R.color.tealPrimaryDark;
                break;
            case 11:
                color = R.color.amberPrimaryDark;
                break;
            default:
                if (Module.isPro()){
                    switch (code){
                        case 12:
                            color = R.color.purpleDeepPrimaryDark;
                            break;
                        case 13:
                            color = R.color.orangeDeepPrimaryDark;
                            break;
                        case 14:
                            color = R.color.limePrimaryDark;
                            break;
                        case 15:
                            color = R.color.indigoPrimaryDark;
                            break;
                        default:
                            color = R.color.bluePrimaryDark;
                            break;
                    }
                } else color = R.color.bluePrimaryDark;
                break;
        }
        return getColor(color);
    }

    /**
     * Get note light color by color code.
     * @param code color code.
     * @return Color
     */
    public int getNoteLightColor(int code){
        int color;
        switch (code){
            case 0:
                color = R.color.redA100;
                break;
            case 1:
                color = R.color.purpleA100;
                break;
            case 2:
                color = R.color.greenA100;
                break;
            case 3:
                color = R.color.greenLightA100;
                break;
            case 4:
                color = R.color.blueA100;
                break;
            case 5:
                color = R.color.blueLightA100;
                break;
            case 6:
                color = R.color.yellowA100;
                break;
            case 7:
                color = R.color.orangeA100;
                break;
            case 8:
                color = R.color.cyanA100;
                break;
            case 9:
                color = R.color.pinkA100;
                break;
            case 10:
                color = R.color.tealA100;
                break;
            case 11:
                color = R.color.amberA100;
                break;
            default:
                if (Module.isPro()){
                    switch (code){
                        case 12:
                            color = R.color.purpleDeepA100;
                            break;
                        case 13:
                            color = R.color.orangeDeepA100;
                            break;
                        case 14:
                            color = R.color.limeA100;
                            break;
                        case 15:
                            color = R.color.indigoA100;
                            break;
                        default:
                            color = R.color.blueA100;
                            break;
                    }
                } else color = R.color.blueA100;
                break;
        }
        return getColor(color);
    }
}
