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

package com.cray.software.justreminder.helpers;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.StyleRes;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.modules.Module;
import com.cray.software.justreminder.utils.ViewUtils;

import java.util.Calendar;

public class ColorSetter {

    public static final int NUM_OF_MARKERS = 16;

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
    @ColorInt
    public int getColor(@ColorRes int color){
        return ViewUtils.getColor(mContext, color);
    }

    /**
     * Get current theme primary color.
     * @return Color resource
     */
    @ColorRes
    public int colorPrimary(){
        return colorPrimary(new SharedPrefs(mContext).loadInt(Prefs.APP_THEME));
    }

    /**
     * Get current theme accent color.
     * @return Color resource
     */
    @ColorRes
    public int colorAccent(){
        return colorAccent(new SharedPrefs(mContext).loadInt(Prefs.APP_THEME));
    }

    /**
     * Get accent color by code.
     * @return Color resource
     */
    @ColorRes
    public int colorAccent(int code){
        int color;
        if (isDark()) {
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
        return color;
    }

    /**
     * <p>
     *     Check what color palette use for application.
     * </p>
     * @return boolean
     */
    public boolean isDark() {
        sPrefs = new SharedPrefs(mContext);
        boolean isDark = sPrefs.loadBoolean(Prefs.USE_DARK_THEME);
        boolean isDayNight = sPrefs.loadBoolean(Prefs.DAY_NIGHT);
        if (isDayNight) {
            Calendar calendar = Calendar.getInstance();
            long mTime = System.currentTimeMillis();
            calendar.setTimeInMillis(mTime);
            calendar.set(Calendar.HOUR_OF_DAY, 8);
            calendar.set(Calendar.MINUTE, 0);
            long min = calendar.getTimeInMillis();
            calendar.set(Calendar.HOUR_OF_DAY, 19);
            long max = calendar.getTimeInMillis();
            return !(mTime >= min && mTime <= max);
        }
        return isDark;
    }

    /**
     * Get theme for application based on user choice.
     * @return Theme resource
     */
    @StyleRes
    public int getStyle(){
        int id;
        sPrefs = new SharedPrefs(mContext);
        int loadedColor = sPrefs.loadInt(Prefs.APP_THEME);
        if (isDark()) {
            switch (loadedColor) {
                case 0:
                    id = R.style.HomeDark_Red;
                    break;
                case 1:
                    id = R.style.HomeDark_Purple;
                    break;
                case 2:
                    id = R.style.HomeDark_LightGreen;
                    break;
                case 3:
                    id = R.style.HomeDark_Green;
                    break;
                case 4:
                    id = R.style.HomeDark_LightBlue;
                    break;
                case 5:
                    id = R.style.HomeDark_Blue;
                    break;
                case 6:
                    id = R.style.HomeDark_Yellow;
                    break;
                case 7:
                    id = R.style.HomeDark_Orange;
                    break;
                case 8:
                    id = R.style.HomeDark_Cyan;
                    break;
                case 9:
                    id = R.style.HomeDark_Pink;
                    break;
                case 10:
                    id = R.style.HomeDark_Teal;
                    break;
                case 11:
                    id = R.style.HomeDark_Amber;
                    break;
                default:
                    if (Module.isPro()) {
                        switch (loadedColor) {
                            case 12:
                                id = R.style.HomeDark_DeepPurple;
                                break;
                            case 13:
                                id = R.style.HomeDark_DeepOrange;
                                break;
                            case 14:
                                id = R.style.HomeDark_Lime;
                                break;
                            case 15:
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
                case 0:
                    id = R.style.HomeWhite_Red;
                    break;
                case 1:
                    id = R.style.HomeWhite_Purple;
                    break;
                case 2:
                    id = R.style.HomeWhite_LightGreen;
                    break;
                case 3:
                    id = R.style.HomeWhite_Green;
                    break;
                case 4:
                    id = R.style.HomeWhite_LightBlue;
                    break;
                case 5:
                    id = R.style.HomeWhite_Blue;
                    break;
                case 6:
                    id = R.style.HomeWhite_Yellow;
                    break;
                case 7:
                    id = R.style.HomeWhite_Orange;
                    break;
                case 8:
                    id = R.style.HomeWhite_Cyan;
                    break;
                case 9:
                    id = R.style.HomeWhite_Pink;
                    break;
                case 10:
                    id = R.style.HomeWhite_Teal;
                    break;
                case 11:
                    id = R.style.HomeWhite_Amber;
                    break;
                default:
                    if (Module.isPro()) {
                        switch (loadedColor) {
                            case 12:
                                id = R.style.HomeWhite_DeepPurple;
                                break;
                            case 13:
                                id = R.style.HomeWhite_DeepOrange;
                                break;
                            case 14:
                                id = R.style.HomeWhite_Lime;
                                break;
                            case 15:
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
    @ColorRes
    public int colorBirthdayCalendar(){
        return colorPrimary(new SharedPrefs(mContext).loadInt(Prefs.BIRTH_COLOR));
    }

    /**
     * Get color resource by code.
     * @param code code.
     * @return Color resource
     */
    @ColorRes
    public int colorPrimary(int code) {
        int color;
        switch (code) {
            case 0:
                color = R.color.redPrimary;
                break;
            case 1:
                color = R.color.purplePrimary;
                break;
            case 2:
                color = R.color.greenLightPrimary;
                break;
            case 3:
                color = R.color.greenPrimary;
                break;
            case 4:
                color = R.color.blueLightPrimary;
                break;
            case 5:
                color = R.color.bluePrimary;
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
                    switch (code) {
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
                            color = R.color.cyanPrimary;
                            break;
                    }
                } else {
                    color = R.color.cyanPrimary;
                }
                break;
        }
        return color;
    }

    /**
     * Get reminders color for calendar.
     * @return Color Resource
     */
    @ColorRes
    public int colorReminderCalendar(){
        return colorPrimary(new SharedPrefs(mContext).loadInt(Prefs.REMINDER_COLOR));
    }

    /**
     * Get current day color for calendar.
     * @return Color Resource
     */
    @ColorRes
    public int colorCurrentCalendar(){
        return colorPrimary(new SharedPrefs(mContext).loadInt(Prefs.TODAY_COLOR));
    }

    /**
     * Get color indicator by identifier.
     * @param color color identifier.
     * @return Drawable resource
     */
    @DrawableRes
    public int getIndicator(int color){
        int drawable;
        switch (color) {
            case 0:
                drawable = R.drawable.drawable_red;
                break;
            case 1:
                drawable = R.drawable.drawable_purple;
                break;
            case 2:
                drawable = R.drawable.drawable_green_light;
                break;
            case 3:
                drawable = R.drawable.drawable_green;
                break;
            case 4:
                drawable = R.drawable.drawable_blue_light;
                break;
            case 5:
                drawable = R.drawable.drawable_blue;
                break;
            case 6:
                drawable = R.drawable.drawable_yellow;
                break;
            case 7:
                drawable = R.drawable.drawable_orange;
                break;
            case 8:
                drawable = R.drawable.drawable_cyan;
                break;
            case 9:
                drawable = R.drawable.drawable_pink;
                break;
            case 10:
                drawable = R.drawable.drawable_teal;
                break;
            case 11:
                drawable = R.drawable.drawable_amber;
                break;
            case 12:
                drawable = R.drawable.drawable_deep_purple;
                break;
            case 13:
                drawable = R.drawable.drawable_deep_orange;
                break;
            case 14:
                drawable = R.drawable.drawable_lime;
                break;
            case 15:
                drawable = R.drawable.drawable_indigo;
                break;
            default:
                drawable = R.drawable.drawable_cyan;
                break;
        }
        return drawable;
    }

    /**
     * Get drawable from resource.
     * @param i resource.
     * @return Drawable
     */
    private Drawable getDrawable(@DrawableRes int i){
        return ViewUtils.getDrawable(mContext, i);
    }

    /**
     * Get style for ToggleButton.
     * @return Drawable
     */
    public Drawable toggleDrawable(){
        int loadedColor = new SharedPrefs(mContext).loadInt(Prefs.APP_THEME);
        int color;
        switch (loadedColor) {
            case 0:
                color = R.drawable.toggle_red;
                break;
            case 1:
                color = R.drawable.toggle_purple;
                break;
            case 2:
                color = R.drawable.toggle_green_light;
                break;
            case 3:
                color = R.drawable.toggle_green;
                break;
            case 4:
                color = R.drawable.toggle_blue_light;
                break;
            case 5:
                color = R.drawable.toggle_blue;
                break;
            case 6:
                color = R.drawable.toggle_yellow;
                break;
            case 7:
                color = R.drawable.toggle_orange;
                break;
            case 8:
                color = R.drawable.toggle_cyan;
                break;
            case 9:
                color = R.drawable.toggle_pink;
                break;
            case 10:
                color = R.drawable.toggle_teal;
                break;
            case 11:
                color = R.drawable.toggle_amber;
                break;
            default:
                if (Module.isPro()){
                    switch (loadedColor) {
                        case 12:
                            color = R.drawable.toggle_deep_purple;
                            break;
                        case 13:
                            color = R.drawable.toggle_deep_orange;
                            break;
                        case 14:
                            color = R.drawable.toggle_lime;
                            break;
                        case 15:
                            color = R.drawable.toggle_indigo;
                            break;
                        default:
                            color = R.drawable.toggle_cyan;
                            break;
                    }
                } else color = R.drawable.toggle_cyan;
                break;
        }
        return getDrawable(color);
    }

    /**
     * Get color primary dark by code.
     * @param code code
     * @return Color resource
     */
    @ColorRes
    public int colorPrimaryDark(int code) {
        int color;
        switch (code) {
            case 0:
                color = R.color.redPrimaryDark;
                break;
            case 1:
                color = R.color.purplePrimaryDark;
                break;
            case 2:
                color = R.color.greenLightPrimaryDark;
                break;
            case 3:
                color = R.color.greenPrimaryDark;
                break;
            case 4:
                color = R.color.blueLightPrimaryDark;
                break;
            case 5:
                color = R.color.bluePrimaryDark;
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
                    switch (code) {
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
                            color = R.color.cyanPrimaryDark;
                            break;
                    }
                } else color = R.color.cyanPrimaryDark;
                break;
        }
        return color;
    }

    /**
     * Get status bar color based on current application theme.
     * @return Color resource
     */
    @ColorRes
    public int colorPrimaryDark(){
        int loadedColor = new SharedPrefs(mContext).loadInt(Prefs.APP_THEME);
        return colorPrimaryDark(loadedColor);
    }

    /**
     * Get style for spinner based on current theme.
     * @return Color
     */
    @ColorInt
    public int getSpinnerStyle(){
        int color;
        sPrefs = new SharedPrefs(mContext);
        if (isDark()) {
            color = R.color.material_grey;
        } else color = R.color.whitePrimary;
        return getColor(color);
    }

    /**
     * Get theme for dialog styled activity based on current application theme.
     * @return Theme resource
     */
    @StyleRes
    public int getDialogStyle(){
        int id;
        sPrefs = new SharedPrefs(mContext);
        int loadedColor = sPrefs.loadInt(Prefs.APP_THEME);
        if (isDark()) {
            switch (loadedColor) {
                case 0:
                    id = R.style.HomeDarkDialog_Red;
                    break;
                case 1:
                    id = R.style.HomeDarkDialog_Purple;
                    break;
                case 2:
                    id = R.style.HomeDarkDialog_LightGreen;
                    break;
                case 3:
                    id = R.style.HomeDarkDialog_Green;
                    break;
                case 4:
                    id = R.style.HomeDarkDialog_LightBlue;
                    break;
                case 5:
                    id = R.style.HomeDarkDialog_Blue;
                    break;
                case 6:
                    id = R.style.HomeDarkDialog_Yellow;
                    break;
                case 7:
                    id = R.style.HomeDarkDialog_Orange;
                    break;
                case 8:
                    id = R.style.HomeDarkDialog_Cyan;
                    break;
                case 9:
                    id = R.style.HomeDarkDialog_Pink;
                    break;
                case 10:
                    id = R.style.HomeDarkDialog_Teal;
                    break;
                case 11:
                    id = R.style.HomeDarkDialog_Amber;
                    break;
                default:
                    if (Module.isPro()) {
                        switch (loadedColor) {
                            case 12:
                                id = R.style.HomeDarkDialog_DeepPurple;
                                break;
                            case 13:
                                id = R.style.HomeDarkDialog_DeepOrange;
                                break;
                            case 14:
                                id = R.style.HomeDarkDialog_Lime;
                                break;
                            case 15:
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
                case 0:
                    id = R.style.HomeWhiteDialog_Red;
                    break;
                case 1:
                    id = R.style.HomeWhiteDialog_Purple;
                    break;
                case 2:
                    id = R.style.HomeWhiteDialog_LightGreen;
                    break;
                case 3:
                    id = R.style.HomeWhiteDialog_Green;
                    break;
                case 4:
                    id = R.style.HomeWhiteDialog_LightBlue;
                    break;
                case 5:
                    id = R.style.HomeWhiteDialog_Blue;
                    break;
                case 6:
                    id = R.style.HomeWhiteDialog_Yellow;
                    break;
                case 7:
                    id = R.style.HomeWhiteDialog_Orange;
                    break;
                case 8:
                    id = R.style.HomeWhiteDialog_Cyan;
                    break;
                case 9:
                    id = R.style.HomeWhiteDialog_Pink;
                    break;
                case 10:
                    id = R.style.HomeWhiteDialog_Teal;
                    break;
                case 11:
                    id = R.style.HomeWhiteDialog_Amber;
                    break;
                default:
                    if (Module.isPro()) {
                        switch (loadedColor) {
                            case 12:
                                id = R.style.HomeWhiteDialog_DeepPurple;
                                break;
                            case 13:
                                id = R.style.HomeWhiteDialog_DeepOrange;
                                break;
                            case 14:
                                id = R.style.HomeWhiteDialog_Lime;
                                break;
                            case 15:
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
    @StyleRes
    public int getFullscreenStyle(){
        int id;
        sPrefs = new SharedPrefs(mContext);
        if (isDark()) {
            id = R.style.HomeDarkFullscreen;
        } else id = R.style.HomeWhiteFullscreen;
        return id;
    }

    /**
     * Get theme for translucent activities.
     * @return Theme resource
     */
    @StyleRes
    public int getTransparentStyle(){
        int id;
        sPrefs = new SharedPrefs(mContext);
        if (isDark()) {
            id = R.style.HomeDarkTranslucent;
        } else id = R.style.HomeWhiteTranslucent;
        return id;
    }

    /**
     * Get window background color based on current theme.
     * @return Color
     */
    @ColorInt
    public int getBackgroundStyle(){
        int id;
        sPrefs = new SharedPrefs(mContext);
        if (isDark()) {
            id = getColor(R.color.material_grey);
        } else id = getColor(R.color.material_white);
        return id;
    }

    /**
     * Get status bar color for reminder window based on current theme.
     * @return Color
     */
    @ColorInt
    public int getStatusBarStyle(){
        sPrefs = new SharedPrefs(mContext);
        if (isDark()) {
            return getColor(R.color.material_grey);
        } else return getColor(colorPrimaryDark());
    }

    /**
     * Get background color for CardView based on current theme.
     * @return Color
     */
    @ColorInt
    public int getCardStyle(){
        int color;
        sPrefs = new SharedPrefs(mContext);
        if (isDark()) {
            color = getColor(R.color.grey_x);
        } else color = getColor(R.color.whitePrimary);
        return color;
    }

    /**
     * Get card-like background drawable based on current theme.
     * @return Drawable resource
     */
    @DrawableRes
    public int getCardDrawableStyle(){
        int color;
        sPrefs = new SharedPrefs(mContext);
        if (isDark()) {
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
    @ColorRes
    public int[] getMarkerRadiusStyle(){
        int color = new SharedPrefs(mContext).loadInt(Prefs.MARKER_STYLE);
        return getMarkerRadiusStyle(color);
    }

    /**
     * Get fill amd stroke color by marker color, for drawing circle around marker on Google Map.
     * @param color marker color.
     * @return  color resources array
     */
    @ColorRes
    public int[] getMarkerRadiusStyle(int color){
        int fillColor;
        int strokeColor;
        switch (color) {
            case 0:
                fillColor = R.color.red50;
                strokeColor = R.color.redPrimaryDark;
                break;
            case 1:
                fillColor = R.color.purple50;
                strokeColor = R.color.purplePrimaryDark;
                break;
            case 2:
                fillColor = R.color.greenLight50;
                strokeColor = R.color.greenLightPrimaryDark;
                break;
            case 3:
                fillColor = R.color.green50;
                strokeColor = R.color.greenPrimaryDark;
                break;
            case 4:
                fillColor = R.color.blueLight50;
                strokeColor = R.color.blueLightPrimaryDark;
                break;
            case 5:
                fillColor = R.color.blue50;
                strokeColor = R.color.bluePrimaryDark;
                break;
            case 6:
                fillColor = R.color.yellow50;
                strokeColor = R.color.yellowPrimaryDark;
                break;
            case 7:
                fillColor = R.color.orange50;
                strokeColor = R.color.orangePrimaryDark;
                break;
            case 8:
                fillColor = R.color.cyan50;
                strokeColor = R.color.cyanPrimaryDark;
                break;
            case 9:
                fillColor = R.color.pink50;
                strokeColor = R.color.pinkPrimaryDark;
                break;
            case 10:
                fillColor = R.color.teal50;
                strokeColor = R.color.tealPrimaryDark;
                break;
            case 11:
                fillColor = R.color.amber50;
                strokeColor = R.color.amberPrimaryDark;
                break;
            case 12:
                fillColor = R.color.purpleDeep50;
                strokeColor = R.color.purpleDeepPrimaryDark;
                break;
            case 13:
                fillColor = R.color.orangeDeep50;
                strokeColor = R.color.orangeDeepPrimaryDark;
                break;
            case 14:
                fillColor = R.color.indigo50;
                strokeColor = R.color.indigoPrimaryDark;
                break;
            case 15:
                fillColor = R.color.lime50;
                strokeColor = R.color.limePrimaryDark;
                break;
            default:
                fillColor = R.color.blue50;
                strokeColor = R.color.bluePrimaryDark;
                break;
        }
        return new int[]{fillColor, strokeColor};
    }

    /**
     * Get marker icon, based on user settings.
     * @return Drawable resource
     */
    @DrawableRes
    public int getMarkerStyle(){
        if (Module.isPro()) {
            int loaded = new SharedPrefs(mContext).loadInt(Prefs.MARKER_STYLE);
            return getMarkerStyle(loaded);
        } else {
            return R.drawable.marker_blue;
        }
    }

    /**
     * Get marker icon by code.
     * @param code code of marker icon.
     * @return Drawable resource
     */
    @DrawableRes
    public int getMarkerStyle(int code){
        int color;
        switch (code) {
            case 0:
                color = R.drawable.marker_red;
                break;
            case 1:
                color = R.drawable.marker_violet;
                break;
            case 2:
                color = R.drawable.marker_green_light;
                break;
            case 3:
                color = R.drawable.marker_green;
                break;
            case 4:
                color = R.drawable.marker_blue_light;
                break;
            case 5:
                color = R.drawable.marker_blue;
                break;
            case 6:
                color = R.drawable.marker_yellow;
                break;
            case 7:
                color = R.drawable.marker_orange;
                break;
            case 8:
                color = R.drawable.marker_cyan;
                break;
            case 9:
                color = R.drawable.marker_pink;
                break;
            case 10:
                color = R.drawable.marker_teal;
                break;
            case 11:
                color = R.drawable.marker_amber;
                break;
            case 12:
                color = R.drawable.marker_deep_purple;
                break;
            case 13:
                color = R.drawable.marker_deep_orange;
                break;
            case 14:
                color = R.drawable.marker_indigo;
                break;
            case 15:
                color = R.drawable.marker_lime;
                break;
            default:
                color = R.drawable.marker_blue;
                break;
        }
        return color;
    }

    /**
     * Get reminder group indicator by code.
     * @param code indicator code.
     * @return Drawable resource
     */
    @DrawableRes
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
                color = R.drawable.circle_green_light;
                break;
            case 3:
                color = R.drawable.circle_green;
                break;
            case 4:
                color = R.drawable.circle_blue_light;
                break;
            case 5:
                color = R.drawable.circle_blue;
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
    @ColorRes
    public int getCategoryColor(int code){
        return colorPrimary(code);
    }

    /**
     * Get note primary color by code.
     * @param code color code.
     * @return Color resource
     */
    @ColorInt
    public int getNoteColor(int code){
        return getColor(colorPrimary(code));
    }

    /**
     * Get note dark color by color code.
     * @param code color code.
     * @return Color
     */
    @ColorInt
    public int getNoteDarkColor(int code){
        return getColor(colorPrimaryDark(code));
    }

    /**
     * Get note light color by color code.
     * @param code color code.
     * @return Color
     */
    @ColorInt
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
