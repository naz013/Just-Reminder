package com.cray.software.justreminder.widgets.utils;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.modules.Module;

/**
 * Copyright 2015 Nazar Suhovich
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class WidgetUtils {

    public static int getColor(int code){
        int color = 0;
        switch (code) {
            case 0:
                color = R.color.whitePrimary;
                break;
            case 1:
                color = R.color.redPrimary;
                break;
            case 2:
                color = R.color.purplePrimary;
                break;
            case 3:
                color = R.color.greenLightPrimary;
                break;
            case 4:
                color = R.color.greenPrimary;
                break;
            case 5:
                color = R.color.blueLightPrimary;
                break;
            case 6:
                color = R.color.bluePrimary;
                break;
            case 7:
                color = R.color.yellowPrimary;
                break;
            case 8:
                color = R.color.orangePrimary;
                break;
            case 9:
                color = R.color.cyanPrimary;
                break;
            case 10:
                color = R.color.pinkPrimary;
                break;
            case 11:
                color = R.color.tealPrimary;
                break;
            case 12:
                color = R.color.amberPrimary;
                break;
            case 13:
                color = android.R.color.transparent;
                break;
            default:
                if (Module.isPro()){
                    switch (code){
                        case 14:
                            color = R.color.purpleDeepPrimary;
                            break;
                        case 15:
                            color = R.color.orangeDeepPrimary;
                            break;
                        case 16:
                            color = R.color.limePrimary;
                            break;
                        case 17:
                            color = R.color.indigoPrimary;
                            break;
                    }
                } else color = R.color.bluePrimary;
                break;
        }
        return color;
    }

    public static int getDrawable(int code){
        int color = 0;
        switch (code) {
            case 0:
                color = R.drawable.rectangle_stroke_red;
                break;
            case 1:
                color = R.drawable.rectangle_stroke_purple;
                break;
            case 2:
                color = R.drawable.rectangle_stroke_light_green;
                break;
            case 3:
                color = R.drawable.rectangle_stroke_green;
                break;
            case 4:
                color = R.drawable.rectangle_stroke_light_blue;
                break;
            case 5:
                color = R.drawable.rectangle_stroke_blue;
                break;
            case 6:
                color = R.drawable.rectangle_stroke_yellow;
                break;
            case 7:
                color = R.drawable.rectangle_stroke_orange;
                break;
            case 8:
                color = R.drawable.rectangle_stroke_cyan;
                break;
            case 9:
                color = R.drawable.rectangle_stroke;
                break;
            case 10:
                color = R.drawable.rectangle_stroke_teal;
                break;
            case 11:
                color = R.drawable.rectangle_stroke_amber;
                break;
            case 12:
                color = R.drawable.rectangle_stroke_transparent;
                break;
            default:
                if (Module.isPro()){
                    switch (code){
                        case 13:
                            color = R.drawable.rectangle_stroke_deep_purple;
                            break;
                        case 14:
                            color = R.drawable.rectangle_stroke_deep_orange;
                            break;
                        case 15:
                            color = R.drawable.rectangle_stroke_lime;
                            break;
                        case 16:
                            color = R.drawable.rectangle_stroke_indigo;
                            break;
                    }
                } else color = R.drawable.rectangle_stroke_blue;
                break;
        }
        return color;
    }
}