package com.cray.software.justreminder.helpers;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.interfaces.Prefs;
import com.cray.software.justreminder.modules.Module;
import com.cray.software.justreminder.utils.Utils;

public class ColorSetter {

    Context cContext;
    SharedPrefs sPrefs;
    Typeface typeface;

    public ColorSetter(Context context){
        this.cContext = context;
    }

    public Typeface getTypeface(int style){
        if (style == 0) {
            typeface = Typeface.createFromAsset(cContext.getAssets(), "fonts/Roboto-Black.ttf");
        } else if (style == 1) {
            typeface = Typeface.createFromAsset(cContext.getAssets(), "fonts/Roboto-BlackItalic.ttf");
        } else if (style == 2) {
            typeface = Typeface.createFromAsset(cContext.getAssets(), "fonts/Roboto-Bold.ttf");
        } else if (style == 3) {
            typeface = Typeface.createFromAsset(cContext.getAssets(), "fonts/Roboto-BoldItalic.ttf");
        } else if (style == 4) {
            typeface = Typeface.createFromAsset(cContext.getAssets(), "fonts/Roboto-Italic.ttf");
        } else if (style == 5) {
            typeface = Typeface.createFromAsset(cContext.getAssets(), "fonts/Roboto-Light.ttf");
        } else if (style == 6) {
            typeface = Typeface.createFromAsset(cContext.getAssets(), "fonts/Roboto-LightItalic.ttf");
        } else if (style == 7) {
            typeface = Typeface.createFromAsset(cContext.getAssets(), "fonts/Roboto-Medium.ttf");
        } else if (style == 8) {
            typeface = Typeface.createFromAsset(cContext.getAssets(), "fonts/Roboto-MediumItalic.ttf");
        } else if (style == 9) {
            typeface = Typeface.createFromAsset(cContext.getAssets(), "fonts/Roboto-Regular.ttf");
        } else if (style == 10) {
            typeface = Typeface.createFromAsset(cContext.getAssets(), "fonts/Roboto-Thin.ttf");
        } else if (style == 11) {
            typeface = Typeface.createFromAsset(cContext.getAssets(), "fonts/Roboto-ThinItalic.ttf");
        } else {
            typeface = Typeface.createFromAsset(cContext.getAssets(), "fonts/Roboto-Light.ttf");
        }
        return typeface;
    }

    private int getColor(int color){
        return cContext.getResources().getColor(color);
    }

    public int colorSetter(){
        sPrefs = new SharedPrefs(cContext);
        String loadedColor = sPrefs.loadPrefs(Prefs.THEME);
        int color;
        if (Module.isPro()){
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
        } else {
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
                    color = getColor(R.color.colorBlue);
                    break;
            }
        }
        return color;
    }

    public int colorBirthdayCalendar(){
        sPrefs = new SharedPrefs(cContext);
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
                color = R.color.colorBlue;
                break;
        }
        return color;
    }

    public int colorReminderCalendar(){
        sPrefs = new SharedPrefs(cContext);
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
                color = R.color.colorGreen;
                break;
        }
        return color;
    }

    public int colorCurrentCalendar(){
        sPrefs = new SharedPrefs(cContext);
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
                color = R.color.colorGreen;
                break;
        }
        return color;
    }

    private Drawable getDrawable(int i){
        return Utils.getDrawable(cContext, i);
    }

    public Drawable toggleDrawable(){
        sPrefs = new SharedPrefs(cContext);
        String loadedColor = sPrefs.loadPrefs(Prefs.THEME);
        Drawable color;
        if (Module.isPro()){
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
        } else {
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
                    color = getDrawable(R.drawable.toggle_blue);
                    break;
            }
        }
        return color;
    }

    public int colorStatus(){
        sPrefs = new SharedPrefs(cContext);
        String loadedColor = sPrefs.loadPrefs(Prefs.THEME);
        int color;
        if (Module.isPro()){
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
        } else {
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
                    color = getColor(R.color.colorBlueDark);
                    break;
            }
        }
        return color;
    }

    public int colorChooser(){
        sPrefs = new SharedPrefs(cContext);
        String loadedColor = sPrefs.loadPrefs(Prefs.THEME);
        int color;
        if (Module.isPro()){
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
                    color = getColor(R.color.colorRedLight);
                    break;
            }
        } else {
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
                    color = getColor(R.color.colorRedLight);
                    break;
            }
        }
        return color;
    }

    public int getStyle(){
        int id;
        sPrefs = new SharedPrefs(cContext);
        String loadedColor = sPrefs.loadPrefs(Prefs.THEME);
        boolean isDark = sPrefs.loadBoolean(Prefs.USE_DARK_THEME);
        if (Module.isPro()) {
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
            }
        } else {
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
                        id = R.style.HomeDark_Blue;
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
                        id = R.style.HomeWhite_Blue;
                        break;
                }
            }
        }
        return id;
    }

    public int getNewStyle(){
        int color;
        sPrefs = new SharedPrefs(cContext);
        if (sPrefs.loadBoolean(Prefs.USE_DARK_THEME)) {
            color = R.style.HomeDarkActionbar;
        } else color = R.style.HomeWhiteActionbar;
        return color;
    }

    public int getSpinnerStyle(){
        int color;
        sPrefs = new SharedPrefs(cContext);
        if (sPrefs.loadBoolean(Prefs.USE_DARK_THEME)) {
            color = getColor(R.color.grey_dark_x);
        } else color = getColor(R.color.colorWhite);
        return color;
    }

    public int getDialogStyle(){
        int id;
        sPrefs = new SharedPrefs(cContext);
        String loadedColor = sPrefs.loadPrefs(Prefs.THEME);
        boolean isDark = sPrefs.loadBoolean(Prefs.USE_DARK_THEME);
        sPrefs = new SharedPrefs(cContext);
        if (Module.isPro()) {
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
            }
        } else {
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
                        id = R.style.HomeDarkDialog_Blue;
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
                        id = R.style.HomeWhiteDialog_Blue;
                        break;
                }
            }
        }
        return id;
    }

    public int getFullscreenStyle(){
        int id;
        sPrefs = new SharedPrefs(cContext);
        if (sPrefs.loadBoolean(Prefs.USE_DARK_THEME)) {
            id = R.style.HomeDarkFullscreen;
        } else id = R.style.HomeWhiteFullscreen;
        return id;
    }

    public int getBackgroundStyle(){
        int id;
        sPrefs = new SharedPrefs(cContext);
        if (sPrefs.loadBoolean(Prefs.USE_DARK_THEME)) {
            id = getColor(R.color.grey_dark_x);
        } else id = getColor(R.color.colorWhite);
        return id;
    }

    public int getStatusBarStyle(){
        int id;
        sPrefs = new SharedPrefs(cContext);
        if (sPrefs.loadBoolean(Prefs.USE_DARK_THEME)) {
            id = getColor(R.color.grey_dark_x);
        } else id = getColor(R.color.colorGreyDark);
        return id;
    }

    public int getCardStyle(){
        int color;
        sPrefs = new SharedPrefs(cContext);
        if (sPrefs.loadBoolean(Prefs.USE_DARK_THEME)) {
            color = getColor(R.color.grey_x);
        } else color = getColor(R.color.colorWhite);
        return color;
    }

    public int getCardDrawableStyle(){
        int color;
        sPrefs = new SharedPrefs(cContext);
        if (sPrefs.loadBoolean(Prefs.USE_DARK_THEME)) {
            color = R.drawable.card_bg_dark;
        } else color = R.drawable.card_bg;
        return color;
    }

    public int getRequestOrientation(){
        int i;
        sPrefs = new SharedPrefs(cContext);
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

    public int[] getMarkerRadiusStyle(){
        int borderColor;
        int overlayColor;
        if (Module.isPro()) {
            sPrefs = new SharedPrefs(cContext);
            int loaded = sPrefs.loadInt(Prefs.MARKER_STYLE);
            if (loaded == 1) {
                borderColor = R.color.colorRedLight;
                overlayColor = R.color.colorRedDark;
            } else if (loaded == 2) {
                borderColor = R.color.colorGreenLight;
                overlayColor = R.color.colorGreenDark;
            } else if (loaded == 3) {
                borderColor = R.color.colorBlueLight;
                overlayColor = R.color.colorBlueDark;
            } else if (loaded == 4) {
                borderColor = R.color.colorYellowLight;
                overlayColor = R.color.colorYellowDark;
            } else if (loaded == 5) {
                borderColor = R.color.colorRedLight;
                overlayColor = R.color.colorRedDark;
            } else if (loaded == 6) {
                borderColor = R.color.colorGreenLight;
                overlayColor = R.color.colorGreenDark;
            } else if (loaded == 7) {
                borderColor = R.color.colorBlueLight;
                overlayColor = R.color.colorBlueDark;
            } else if (loaded == 8) {
                borderColor = R.color.colorYellowLight;
                overlayColor = R.color.colorYellowDark;
            } else if (loaded == 9) {
                borderColor = R.color.colorRedLight;
                overlayColor = R.color.colorRedDark;
            } else if (loaded == 10) {
                borderColor = R.color.colorOrangeLight;
                overlayColor = R.color.colorOrangeDark;
            } else if (loaded == 11) {
                borderColor = R.color.colorGreenLight;
                overlayColor = R.color.colorGreenDark;
            } else if (loaded == 12) {
                borderColor = R.color.colorBlueLight;
                overlayColor = R.color.colorBlueDark;
            } else {
                borderColor = R.color.colorBlueLight;
                overlayColor = R.color.colorBlueDark;
            }
        } else {
            borderColor = R.color.colorBlueLight;
            overlayColor = R.color.colorBlueDark;
        }
        return new int[]{borderColor, overlayColor};
    }

    public int getMarkerStyle(){
        int color;
        if (Module.isPro()) {
            sPrefs = new SharedPrefs(cContext);
            int loaded = sPrefs.loadInt(Prefs.MARKER_STYLE);
            if (loaded == 1) {
                color = R.drawable.flat_red;
            } else if (loaded == 2) {
                color = R.drawable.flat_green;
            } else if (loaded == 3) {
                color = R.drawable.marker;
            } else if (loaded == 4) {
                color = R.drawable.flat_yellow;
            } else if (loaded == 5) {
                color = R.drawable.simple_red;
            } else if (loaded == 6) {
                color = R.drawable.simple_green;
            } else if (loaded == 7) {
                color = R.drawable.simple_blue;
            } else if (loaded == 8) {
                color = R.drawable.simple_yellow;
            } else if (loaded == 9) {
                color = R.drawable.round_red;
            } else if (loaded == 10) {
                color = R.drawable.round_orange;
            } else if (loaded == 11) {
                color = R.drawable.round_green;
            } else if (loaded == 12) {
                color = R.drawable.round_blue;
            } else {
                color = R.drawable.marker;
            }
        } else {
            color = R.drawable.marker;
        }
        return color;
    }

    public int getNoteColor(int position){
        int color;
        if (Module.isPro()){
            if (position == 0) {
                color = R.color.colorRed;
            } else if (position == 1) {
                color = R.color.colorViolet;
            } else if (position == 2) {
                color = R.color.colorGreen;
            } else if (position == 3) {
                color = R.color.colorLightCreen;
            } else if (position == 4) {
                color = R.color.colorBlue;
            } else if (position == 5) {
                color = R.color.colorLightBlue;
            } else if (position == 6) {
                color = R.color.colorYellow;
            } else if (position == 7) {
                color = R.color.colorOrange;
            } else if (position == 8) {
                color = R.color.colorGrey;
            } else if (position == 9) {
                color = R.color.colorPink;
            } else if (position == 10) {
                color = R.color.colorSand;
            } else if (position == 11) {
                color = R.color.colorBrown;
            } else if (position == 12) {
                color = R.color.colorDeepPurple;
            } else if (position == 13) {
                color = R.color.colorDeepOrange;
            } else if (position == 14) {
                color = R.color.colorLime;
            } else if (position == 15) {
                color = R.color.colorIndigo;
            } else {
                color = R.color.colorGrey;
            }
        } else {
            if (position == 0) {
                color = R.color.colorRed;
            } else if (position == 1) {
                color = R.color.colorViolet;
            } else if (position == 2) {
                color = R.color.colorGreen;
            } else if (position == 3) {
                color = R.color.colorLightCreen;
            } else if (position == 4) {
                color = R.color.colorBlue;
            } else if (position == 5) {
                color = R.color.colorLightBlue;
            } else if (position == 6) {
                color = R.color.colorYellow;
            } else if (position == 7) {
                color = R.color.colorOrange;
            } else if (position == 8) {
                color = R.color.colorGrey;
            } else if (position == 9) {
                color = R.color.colorPink;
            } else if (position == 10) {
                color = R.color.colorSand;
            } else if (position == 11) {
                color = R.color.colorBrown;
            } else {
                color = R.color.colorGrey;
            }
        }
        return color;
    }

    public int getCategoryIndicator(int position){
        int color;
        if (Module.isPro()){
            if (position == 0) {
                color = R.drawable.circle_red;
            } else if (position == 1) {
                color = R.drawable.circle_violet;
            } else if (position == 2) {
                color = R.drawable.circle_green;
            } else if (position == 3) {
                color = R.drawable.circle_green_light;
            } else if (position == 4) {
                color = R.drawable.circle_blue;
            } else if (position == 5) {
                color = R.drawable.circle_blue_light;
            } else if (position == 6) {
                color = R.drawable.circle_yellow;
            } else if (position == 7) {
                color = R.drawable.circle_orange;
            } else if (position == 8) {
                color = R.drawable.circle_grey;
            } else if (position == 9) {
                color = R.drawable.circle_pink;
            } else if (position == 10) {
                color = R.drawable.circle_teal;
            } else if (position == 11) {
                color = R.drawable.circle_brown;
            } else if (position == 12) {
                color = R.drawable.circle_deep_purple;
            } else if (position == 13) {
                color = R.drawable.circle_deep_orange;
            } else if (position == 14) {
                color = R.drawable.circle_lime;
            } else if (position == 15) {
                color = R.drawable.circle_indigo;
            } else {
                color = R.drawable.circle_blue;
            }
        } else {
            if (position == 0) {
                color = R.drawable.circle_red;
            } else if (position == 1) {
                color = R.drawable.circle_violet;
            } else if (position == 2) {
                color = R.drawable.circle_green;
            } else if (position == 3) {
                color = R.drawable.circle_green_light;
            } else if (position == 4) {
                color = R.drawable.circle_blue;
            } else if (position == 5) {
                color = R.drawable.circle_blue_light;
            } else if (position == 6) {
                color = R.drawable.circle_yellow;
            } else if (position == 7) {
                color = R.drawable.circle_orange;
            } else if (position == 8) {
                color = R.drawable.circle_grey;
            } else if (position == 9) {
                color = R.drawable.circle_pink;
            } else if (position == 10) {
                color = R.drawable.circle_teal;
            } else if (position == 11) {
                color = R.drawable.circle_brown;
            } else {
                color = R.drawable.circle_blue;
            }
        }
        return color;
    }

    public int getNoteDarkColor(int position){
        int color;
        if (Module.isPro()){
            if (position == R.color.colorRed) {
                color = getColor(R.color.colorRedDark);
            } else if (position == R.color.colorViolet) {
                color = getColor(R.color.colorVioletDark);
            } else if (position == R.color.colorGreen) {
                color = getColor(R.color.colorGreenDark);
            } else if (position == R.color.colorLightCreen) {
                color = getColor(R.color.colorLightCreenDark);
            } else if (position == R.color.colorBlue) {
                color = getColor(R.color.colorBlueDark);
            } else if (position == R.color.colorLightBlue) {
                color = getColor(R.color.colorLightBlueDark);
            } else if (position == R.color.colorYellow) {
                color = getColor(R.color.colorYellowDark);
            } else if (position == R.color.colorOrange) {
                color = getColor(R.color.colorOrangeDark);
            } else if (position == R.color.colorGrey) {
                color = getColor(R.color.colorGreyDark);
            } else if (position == R.color.colorPink) {
                color = getColor(R.color.colorPinkDark);
            } else if (position == R.color.colorSand) {
                color = getColor(R.color.colorSandDark);
            } else if (position == R.color.colorBrown) {
                color = getColor(R.color.colorBrownDark);
            } else if (position == R.color.colorDeepPurple) {
                color = getColor(R.color.colorDeepPurpleDark);
            } else if (position == R.color.colorDeepOrange) {
                color = getColor(R.color.colorDeepOrangeDark);
            } else if (position == R.color.colorLime) {
                color = getColor(R.color.colorLimeDark);
            } else if (position == R.color.colorIndigo) {
                color = getColor(R.color.colorIndigoDark);
            } else {
                color = getColor(R.color.colorGreyDark);
            }
        } else {
            if (position == R.color.colorRed) {
                color = getColor(R.color.colorRedDark);
            } else if (position == R.color.colorViolet) {
                color = getColor(R.color.colorVioletDark);
            } else if (position == R.color.colorGreen) {
                color = getColor(R.color.colorGreenDark);
            } else if (position == R.color.colorLightCreen) {
                color = getColor(R.color.colorLightCreenDark);
            } else if (position == R.color.colorBlue) {
                color = getColor(R.color.colorBlueDark);
            } else if (position == R.color.colorLightBlue) {
                color = getColor(R.color.colorLightBlueDark);
            } else if (position == R.color.colorYellow) {
                color = getColor(R.color.colorYellowDark);
            } else if (position == R.color.colorOrange) {
                color = getColor(R.color.colorOrangeDark);
            } else if (position == R.color.colorGrey) {
                color = getColor(R.color.colorGreyDark);
            } else if (position == R.color.colorPink) {
                color = getColor(R.color.colorPinkDark);
            } else if (position == R.color.colorSand) {
                color = getColor(R.color.colorSandDark);
            } else if (position == R.color.colorBrown) {
                color = getColor(R.color.colorBrownDark);
            } else {
                color = getColor(R.color.colorGreyDark);
            }
        }
        return color;
    }

    public int getNoteLightColor(int position){
        int color;
        if (Module.isPro()){
            if (position == R.color.colorRed) {
                color = getColor(R.color.colorRedLight);
            } else if (position == R.color.colorViolet) {
                color = getColor(R.color.colorVioletLight);
            } else if (position == R.color.colorGreen) {
                color = getColor(R.color.colorGreenLight);
            } else if (position == R.color.colorLightCreen) {
                color = getColor(R.color.colorLightCreenLight);
            } else if (position == R.color.colorBlue) {
                color = getColor(R.color.colorBlueLight);
            } else if (position == R.color.colorLightBlue) {
                color = getColor(R.color.colorLightBlueLight);
            } else if (position == R.color.colorYellow) {
                color = getColor(R.color.colorYellowLight);
            } else if (position == R.color.colorOrange) {
                color = getColor(R.color.colorOrangeLight);
            } else if (position == R.color.colorGrey) {
                color = getColor(R.color.colorGreyLight);
            } else if (position == R.color.colorPink) {
                color = getColor(R.color.colorPinkLight);
            } else if (position == R.color.colorSand) {
                color = getColor(R.color.colorSandLight);
            } else if (position == R.color.colorBrown) {
                color = getColor(R.color.colorBrownLight);
            } else if (position == R.color.colorDeepPurple) {
                color = getColor(R.color.colorDeepPurpleLight);
            } else if (position == R.color.colorDeepOrange) {
                color = getColor(R.color.colorDeepOrangeLight);
            } else if (position == R.color.colorLime) {
                color = getColor(R.color.colorLimeLight);
            } else if (position == R.color.colorIndigo) {
                color = getColor(R.color.colorIndigoLight);
            } else {
                color = getColor(R.color.colorGreyLight);
            }
        } else {
            if (position == R.color.colorRed) {
                color = getColor(R.color.colorRedLight);
            } else if (position == R.color.colorViolet) {
                color = getColor(R.color.colorVioletLight);
            } else if (position == R.color.colorGreen) {
                color = getColor(R.color.colorGreenLight);
            } else if (position == R.color.colorLightCreen) {
                color = getColor(R.color.colorLightCreenLight);
            } else if (position == R.color.colorBlue) {
                color = getColor(R.color.colorBlueLight);
            } else if (position == R.color.colorLightBlue) {
                color = getColor(R.color.colorLightBlueLight);
            } else if (position == R.color.colorYellow) {
                color = getColor(R.color.colorYellowLight);
            } else if (position == R.color.colorOrange) {
                color = getColor(R.color.colorOrangeLight);
            } else if (position == R.color.colorGrey) {
                color = getColor(R.color.colorGreyLight);
            } else if (position == R.color.colorPink) {
                color = getColor(R.color.colorPinkLight);
            } else if (position == R.color.colorSand) {
                color = getColor(R.color.colorSandLight);
            } else if (position == R.color.colorBrown) {
                color = getColor(R.color.colorBrownLight);
            } else {
                color = getColor(R.color.colorGreyLight);
            }
        }
        return color;
    }
}
