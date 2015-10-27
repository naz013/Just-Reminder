package com.cray.software.justreminder.helpers;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.interfaces.Prefs;
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
     * @return
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
    public int colorSetter(){
        sPrefs = new SharedPrefs(mContext);
        String loadedColor = sPrefs.loadPrefs(Prefs.THEME);
        int color;
        switch (loadedColor) {
            case "1":
                color = getColor(R.color.colorRed);
                break;
            case "2":
                color = getColor(R.color.colorViolet);
                break;
            case "3":
                color = getColor(R.color.colorLightCreen);
                break;
            case "4":
                color = getColor(R.color.colorGreen);
                break;
            case "5":
                color = getColor(R.color.colorLightBlue);
                break;
            case "6":
                color = getColor(R.color.colorBlue);
                break;
            case "7":
                color = getColor(R.color.colorYellow);
                break;
            case "8":
                color = getColor(R.color.colorOrange);
                break;
            case "9":
                color = getColor(R.color.colorGrey);
                break;
            case "10":
                color = getColor(R.color.colorPink);
                break;
            case "11":
                color = getColor(R.color.colorSand);
                break;
            case "12":
                color = getColor(R.color.colorBrown);
                break;
            default:
                if (Module.isPro()){
                    switch (loadedColor) {
                        case "13":
                            color = getColor(R.color.colorDeepPurple);
                            break;
                        case "14":
                            color = getColor(R.color.colorDeepOrange);
                            break;
                        case "15":
                            color = getColor(R.color.colorLime);
                            break;
                        case "16":
                            color = getColor(R.color.colorIndigo);
                            break;
                        default:
                            color = getColor(R.color.colorBlue);
                            break;
                    }
                } else color = getColor(R.color.colorBlue);
                break;
        }
        return color;
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
                color = R.color.colorRed;
                break;
            case "2":
                color = R.color.colorViolet;
                break;
            case "3":
                color = R.color.colorLightCreen;
                break;
            case "4":
                color = R.color.colorGreen;
                break;
            case "5":
                color = R.color.colorLightBlue;
                break;
            case "6":
                color = R.color.colorBlue;
                break;
            case "7":
                color = R.color.colorYellow;
                break;
            case "8":
                color = R.color.colorOrange;
                break;
            case "9":
                color = R.color.colorGrey;
                break;
            case "10":
                color = R.color.colorPink;
                break;
            case "11":
                color = R.color.colorSand;
                break;
            case "12":
                color = R.color.colorBrown;
                break;
            default:
                if (Module.isPro()){
                    switch (loadedColor) {
                        case "13":
                            color = R.color.colorDeepPurple;
                            break;
                        case "14":
                            color = R.color.colorDeepOrange;
                            break;
                        case "15":
                            color = R.color.colorLime;
                            break;
                        case "16":
                            color = R.color.colorIndigo;
                            break;
                        default:
                            color = R.color.colorGreen;
                            break;
                    }
                } else {
                    color = R.color.colorGreen;
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
                color = R.color.colorRed;
                break;
            case "2":
                color = R.color.colorViolet;
                break;
            case "3":
                color = R.color.colorLightCreen;
                break;
            case "4":
                color = R.color.colorGreen;
                break;
            case "5":
                color = R.color.colorLightBlue;
                break;
            case "6":
                color = R.color.colorBlue;
                break;
            case "7":
                color = R.color.colorYellow;
                break;
            case "8":
                color = R.color.colorOrange;
                break;
            case "9":
                color = R.color.colorGrey;
                break;
            case "10":
                color = R.color.colorPink;
                break;
            case "11":
                color = R.color.colorSand;
                break;
            case "12":
                color = R.color.colorBrown;
                break;
            default:
                if (Module.isPro()){
                    switch (loadedColor) {
                        case "13":
                            color = R.color.colorDeepPurple;
                            break;
                        case "14":
                            color = R.color.colorDeepOrange;
                            break;
                        case "15":
                            color = R.color.colorLime;
                            break;
                        case "16":
                            color = R.color.colorIndigo;
                            break;
                        default:
                            color = R.color.colorGreen;
                            break;
                    }
                } else {
                    color = R.color.colorGreen;
                }
                break;
        }
        return color;
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
                color = R.color.colorRed;
                break;
            case "2":
                color = R.color.colorViolet;
                break;
            case "3":
                color = R.color.colorLightCreen;
                break;
            case "4":
                color = R.color.colorGreen;
                break;
            case "5":
                color = R.color.colorLightBlue;
                break;
            case "6":
                color = R.color.colorBlue;
                break;
            case "7":
                color = R.color.colorYellow;
                break;
            case "8":
                color = R.color.colorOrange;
                break;
            case "9":
                color = R.color.colorGrey;
                break;
            case "10":
                color = R.color.colorPink;
                break;
            case "11":
                color = R.color.colorSand;
                break;
            case "12":
                color = R.color.colorBrown;
                break;
            default:
                if (Module.isPro()){
                    switch (loadedColor) {
                        case "13":
                            color = R.color.colorDeepPurple;
                            break;
                        case "14":
                            color = R.color.colorDeepOrange;
                            break;
                        case "15":
                            color = R.color.colorLime;
                            break;
                        case "16":
                            color = R.color.colorIndigo;
                            break;
                        default:
                            color = R.color.colorGreen;
                            break;
                    }
                } else {
                    color = R.color.colorGreen;
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
                drawable = R.drawable.drawable_violet;
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
                drawable = R.drawable.drawable_grey;
                break;
            case "10":
                drawable = R.drawable.drawable_pink;
                break;
            case "11":
                drawable = R.drawable.drawable_teal;
                break;
            case "12":
                drawable = R.drawable.drawable_brown;
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
                color = getDrawable(R.drawable.toggle_violet);
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
                color = getDrawable(R.drawable.toggle_grey);
                break;
            case "10":
                color = getDrawable(R.drawable.toggle_pink);
                break;
            case "11":
                color = getDrawable(R.drawable.toggle_sand);
                break;
            case "12":
                color = getDrawable(R.drawable.toggle_brown);
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
    public int colorStatus(){
        sPrefs = new SharedPrefs(mContext);
        String loadedColor = sPrefs.loadPrefs(Prefs.THEME);
        int color;
        switch (loadedColor) {
            case "1":
                color = getColor(R.color.colorRedDark);
                break;
            case "2":
                color = getColor(R.color.colorVioletDark);
                break;
            case "3":
                color = getColor(R.color.colorLightCreenDark);
                break;
            case "4":
                color = getColor(R.color.colorGreenDark);
                break;
            case "5":
                color = getColor(R.color.colorLightBlueDark);
                break;
            case "6":
                color = getColor(R.color.colorBlueDark);
                break;
            case "7":
                color = getColor(R.color.colorYellowDark);
                break;
            case "8":
                color = getColor(R.color.colorOrangeDark);
                break;
            case "9":
                color = getColor(R.color.colorGreyDark);
                break;
            case "10":
                color = getColor(R.color.colorPinkDark);
                break;
            case "11":
                color = getColor(R.color.colorSandDark);
                break;
            case "12":
                color = getColor(R.color.colorBrownDark);
                break;
            default:
                if (Module.isPro()){
                    switch (loadedColor) {
                        case "13":
                            color = getColor(R.color.colorDeepPurpleDark);
                            break;
                        case "14":
                            color = getColor(R.color.colorDeepOrangeDark);
                            break;
                        case "15":
                            color = getColor(R.color.colorLimeDark);
                            break;
                        case "16":
                            color = getColor(R.color.colorIndigoDark);
                            break;
                        default:
                            color = getColor(R.color.colorBlueDark);
                            break;
                    }
                } else color = getColor(R.color.colorBlueDark);
                break;
        }
        return color;
    }

    /**
     * Get current theme light color.
     * @return Color
     */
    public int colorChooser(){
        sPrefs = new SharedPrefs(mContext);
        String loadedColor = sPrefs.loadPrefs(Prefs.THEME);
        int color;
        switch (loadedColor) {
            case "1":
                color = getColor(R.color.colorRedLight);
                break;
            case "2":
                color = getColor(R.color.colorVioletLight);
                break;
            case "3":
                color = getColor(R.color.colorLightCreenLight);
                break;
            case "4":
                color = getColor(R.color.colorGreenLight);
                break;
            case "5":
                color = getColor(R.color.colorLightBlueLight);
                break;
            case "6":
                color = getColor(R.color.colorBlueLight);
                break;
            case "7":
                color = getColor(R.color.colorYellowLight);
                break;
            case "8":
                color = getColor(R.color.colorOrangeLight);
                break;
            case "9":
                color = getColor(R.color.colorGreyLight);
                break;
            case "10":
                color = getColor(R.color.colorPinkLight);
                break;
            case "11":
                color = getColor(R.color.colorSandLight);
                break;
            case "12":
                color = getColor(R.color.colorBrownLight);
                break;
            default:
                if (Module.isPro()){
                    switch (loadedColor) {
                        case "13":
                            color = getColor(R.color.colorDeepPurpleLight);
                            break;
                        case "14":
                            color = getColor(R.color.colorDeepOrangeLight);
                            break;
                        case "15":
                            color = getColor(R.color.colorLimeLight);
                            break;
                        case "16":
                            color = getColor(R.color.colorIndigoLight);
                            break;
                        default:
                            color = getColor(R.color.colorBlueLight);
                            break;
                    }
                } else color = getColor(R.color.colorBlueLight);
                break;
        }
        return color;
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
                    id = R.style.HomeDark_Violet;
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
                    id = R.style.HomeDark_Grey;
                    break;
                case "10":
                    id = R.style.HomeDark_Pink;
                    break;
                case "11":
                    id = R.style.HomeDark_Sand;
                    break;
                case "12":
                    id = R.style.HomeDark_Brown;
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
                    id = R.style.HomeWhite_Violet;
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
                    id = R.style.HomeWhite_Grey;
                    break;
                case "10":
                    id = R.style.HomeWhite_Pink;
                    break;
                case "11":
                    id = R.style.HomeWhite_Sand;
                    break;
                case "12":
                    id = R.style.HomeWhite_Brown;
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
     * Get style for spinner based on current theme.
     * @return Color
     */
    public int getSpinnerStyle(){
        int color;
        sPrefs = new SharedPrefs(mContext);
        if (sPrefs.loadBoolean(Prefs.USE_DARK_THEME)) {
            color = getColor(R.color.grey_dark_x);
        } else color = getColor(R.color.colorWhite);
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
                    id = R.style.HomeDarkDialog_Violet;
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
                    id = R.style.HomeDarkDialog_Grey;
                    break;
                case "10":
                    id = R.style.HomeDarkDialog_Pink;
                    break;
                case "11":
                    id = R.style.HomeDarkDialog_Sand;
                    break;
                case "12":
                    id = R.style.HomeDarkDialog_Brown;
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
                    id = R.style.HomeWhiteDialog_Violet;
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
                    id = R.style.HomeWhiteDialog_Grey;
                    break;
                case "10":
                    id = R.style.HomeWhiteDialog_Pink;
                    break;
                case "11":
                    id = R.style.HomeWhiteDialog_Sand;
                    break;
                case "12":
                    id = R.style.HomeWhiteDialog_Brown;
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
            id = getColor(R.color.grey_dark_x);
        } else id = getColor(R.color.colorGreyDark);
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
        } else color = getColor(R.color.colorWhite);
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
     * @return
     */
    public int[] getMarkerRadiusStyle(){
        int fillColor;
        int strokeColor;
        if (Module.isPro()) {
            sPrefs = new SharedPrefs(mContext);
            int color = sPrefs.loadInt(Prefs.MARKER_STYLE);
            if (color == 1) {
                fillColor = R.color.colorRedTr;
                strokeColor = R.color.colorRedDark;
            } else if (color == 2) {
                fillColor = R.color.colorGreenTr;
                strokeColor = R.color.colorGreenDark;
            } else if (color == 3) {
                fillColor = R.color.colorBlueTr;
                strokeColor = R.color.colorBlueDark;
            } else if (color == 4) {
                fillColor = R.color.colorYellowTr;
                strokeColor = R.color.colorYellowDark;
            } else if (color == 5) {
                fillColor = R.color.colorLightCreenTr;
                strokeColor = R.color.colorLightCreenDark;
            } else if (color == 6) {
                fillColor = R.color.colorLightBlueTr;
                strokeColor = R.color.colorLightBlueDark;
            } else if (color == 7) {
                fillColor = R.color.colorGreyTr;
                strokeColor = R.color.colorGreyDark;
            } else if (color == 8) {
                fillColor = R.color.colorVioletTr;
                strokeColor = R.color.colorVioletDark;
            } else if (color == 9) {
                fillColor = R.color.colorBrownTr;
                strokeColor = R.color.colorBrownDark;
            } else if (color == 10) {
                fillColor = R.color.colorOrangeTr;
                strokeColor = R.color.colorOrangeDark;
            } else if (color == 11) {
                fillColor = R.color.colorPinkTr;
                strokeColor = R.color.colorPinkDark;
            } else if (color == 12) {
                fillColor = R.color.colorSandTr;
                strokeColor = R.color.colorSandDark;
            } else if (color == 13) {
                fillColor = R.color.colorDeepPurpleTr;
                strokeColor = R.color.colorDeepPurpleDark;
            } else if (color == 14) {
                fillColor = R.color.colorDeepOrangeTr;
                strokeColor = R.color.colorDeepOrangeDark;
            } else if (color == 15) {
                fillColor = R.color.colorIndigoTr;
                strokeColor = R.color.colorIndigoDark;
            } else if (color == 16) {
                fillColor = R.color.colorLimeTr;
                strokeColor = R.color.colorLimeDark;
            } else {
                fillColor = R.color.colorBlueTr;
                strokeColor = R.color.colorBlueDark;
            }
        } else {
            fillColor = R.color.colorBlueTr;
            strokeColor = R.color.colorBlueDark;
        }
        return new int[]{fillColor, strokeColor};
    }

    /**
     * Get fill amd stroke color by marker color, for drawing circle around marker on Google Map.
     * @param color marker color.
     * @return
     */
    public int[] getMarkerRadiusStyle(int color){
        int fillColor;
        int strokeColor;
        if (color == 1) {
            fillColor = R.color.colorRedTr;
            strokeColor = R.color.colorRedDark;
        } else if (color == 2) {
            fillColor = R.color.colorGreenTr;
            strokeColor = R.color.colorGreenDark;
        } else if (color == 3) {
            fillColor = R.color.colorBlueTr;
            strokeColor = R.color.colorBlueDark;
        } else if (color == 4) {
            fillColor = R.color.colorYellowTr;
            strokeColor = R.color.colorYellowDark;
        } else if (color == 5) {
            fillColor = R.color.colorLightCreenTr;
            strokeColor = R.color.colorLightCreenDark;
        } else if (color == 6) {
            fillColor = R.color.colorLightBlueTr;
            strokeColor = R.color.colorLightBlueDark;
        } else if (color == 7) {
            fillColor = R.color.colorGreyTr;
            strokeColor = R.color.colorGreyDark;
        } else if (color == 8) {
            fillColor = R.color.colorVioletTr;
            strokeColor = R.color.colorVioletDark;
        } else if (color == 9) {
            fillColor = R.color.colorBrownTr;
            strokeColor = R.color.colorBrownDark;
        } else if (color == 10) {
            fillColor = R.color.colorOrangeTr;
            strokeColor = R.color.colorOrangeDark;
        } else if (color == 11) {
            fillColor = R.color.colorPinkTr;
            strokeColor = R.color.colorPinkDark;
        } else if (color == 12) {
            fillColor = R.color.colorSandTr;
            strokeColor = R.color.colorSandDark;
        } else if (color == 13) {
            fillColor = R.color.colorDeepPurpleTr;
            strokeColor = R.color.colorDeepPurpleDark;
        } else if (color == 14) {
            fillColor = R.color.colorDeepOrangeTr;
            strokeColor = R.color.colorDeepOrangeDark;
        } else if (color == 15) {
            fillColor = R.color.colorIndigoTr;
            strokeColor = R.color.colorIndigoDark;
        } else if (color == 16) {
            fillColor = R.color.colorLimeTr;
            strokeColor = R.color.colorLimeDark;
        } else {
            fillColor = R.color.colorBlueTr;
            strokeColor = R.color.colorBlueDark;
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
                color = R.drawable.circle_violet;
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
                color = R.drawable.circle_grey;
                break;
            case 9:
                color = R.drawable.circle_pink;
                break;
            case 10:
                color = R.drawable.circle_teal;
                break;
            case 11:
                color = R.drawable.circle_brown;
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
                color = R.color.colorRed;
                break;
            case 1:
                color = R.color.colorViolet;
                break;
            case 2:
                color = R.color.colorGreen;
                break;
            case 3:
                color = R.color.colorLightCreen;
                break;
            case 4:
                color = R.color.colorBlue;
                break;
            case 5:
                color = R.color.colorLightBlue;
                break;
            case 6:
                color = R.color.colorYellow;
                break;
            case 7:
                color = R.color.colorOrange;
                break;
            case 8:
                color = R.color.colorGrey;
                break;
            case 9:
                color = R.color.colorPink;
                break;
            case 10:
                color = R.color.colorSand;
                break;
            case 11:
                color = R.color.colorBrown;
                break;
            default:
                if (Module.isPro()){
                    switch (code){
                        case 12:
                            color = R.color.colorDeepPurple;
                            break;
                        case 13:
                            color = R.color.colorDeepOrange;
                            break;
                        case 14:
                            color = R.color.colorLime;
                            break;
                        case 15:
                            color = R.color.colorIndigo;
                            break;
                        default:
                            color = R.color.colorBlue;
                            break;
                    }
                } else color = R.color.colorBlue;
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
                color = R.color.colorRed;
                break;
            case 1:
                color = R.color.colorViolet;
                break;
            case 2:
                color = R.color.colorGreen;
                break;
            case 3:
                color = R.color.colorLightCreen;
                break;
            case 4:
                color = R.color.colorBlue;
                break;
            case 5:
                color = R.color.colorLightBlue;
                break;
            case 6:
                color = R.color.colorYellow;
                break;
            case 7:
                color = R.color.colorOrange;
                break;
            case 8:
                color = R.color.colorGrey;
                break;
            case 9:
                color = R.color.colorPink;
                break;
            case 10:
                color = R.color.colorSand;
                break;
            case 11:
                color = R.color.colorBrown;
                break;
            default:
                if (Module.isPro()){
                    switch (code){
                        case 12:
                            color = R.color.colorDeepPurple;
                            break;
                        case 13:
                            color = R.color.colorDeepOrange;
                            break;
                        case 14:
                            color = R.color.colorLime;
                            break;
                        case 15:
                            color = R.color.colorIndigo;
                            break;
                        default:
                            color = R.color.colorBlue;
                            break;
                    }
                } else color = R.color.colorBlue;
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
                color = R.color.colorRedDark;
                break;
            case 1:
                color = R.color.colorVioletDark;
                break;
            case 2:
                color = R.color.colorGreenDark;
                break;
            case 3:
                color = R.color.colorLightCreenDark;
                break;
            case 4:
                color = R.color.colorBlueDark;
                break;
            case 5:
                color = R.color.colorLightBlueDark;
                break;
            case 6:
                color = R.color.colorYellowDark;
                break;
            case 7:
                color = R.color.colorOrangeDark;
                break;
            case 8:
                color = R.color.colorGreyDark;
                break;
            case 9:
                color = R.color.colorPinkDark;
                break;
            case 10:
                color = R.color.colorSandDark;
                break;
            case 11:
                color = R.color.colorBrownDark;
                break;
            default:
                if (Module.isPro()){
                    switch (code){
                        case 12:
                            color = R.color.colorDeepPurpleDark;
                            break;
                        case 13:
                            color = R.color.colorDeepOrangeDark;
                            break;
                        case 14:
                            color = R.color.colorLimeDark;
                            break;
                        case 15:
                            color = R.color.colorIndigoDark;
                            break;
                        default:
                            color = R.color.colorBlueDark;
                            break;
                    }
                } else color = R.color.colorBlueDark;
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
                color = R.color.colorRedLight;
                break;
            case 1:
                color = R.color.colorVioletLight;
                break;
            case 2:
                color = R.color.colorGreenLight;
                break;
            case 3:
                color = R.color.colorLightCreenLight;
                break;
            case 4:
                color = R.color.colorBlueLight;
                break;
            case 5:
                color = R.color.colorLightBlueLight;
                break;
            case 6:
                color = R.color.colorYellowLight;
                break;
            case 7:
                color = R.color.colorOrangeLight;
                break;
            case 8:
                color = R.color.colorGreyLight;
                break;
            case 9:
                color = R.color.colorPinkLight;
                break;
            case 10:
                color = R.color.colorSandLight;
                break;
            case 11:
                color = R.color.colorBrownLight;
                break;
            default:
                if (Module.isPro()){
                    switch (code){
                        case 12:
                            color = R.color.colorDeepPurpleLight;
                            break;
                        case 13:
                            color = R.color.colorDeepOrangeLight;
                            break;
                        case 14:
                            color = R.color.colorLimeLight;
                            break;
                        case 15:
                            color = R.color.colorIndigoLight;
                            break;
                        default:
                            color = R.color.colorBlueLight;
                            break;
                    }
                } else color = R.color.colorBlueLight;
                break;
        }
        return getColor(color);
    }
}
