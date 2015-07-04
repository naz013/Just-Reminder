package com.cray.software.justreminder.utils;

import android.content.Context;
import android.graphics.Typeface;

public class AssetsUtil {
    public AssetsUtil(){
    }

    public static Typeface getThinTypeface(Context context){
        return Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Thin.ttf");
    }

    public static Typeface getThinTypeface(Context context, boolean italic){
        if (italic) return Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-ThinItalic.ttf");
        else return Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Thin.ttf");
    }

    public static Typeface getLightTypeface(Context context){
        return Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Light.ttf");
    }

    public static Typeface getLightTypeface(Context context, boolean italic){
        if (italic) return Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-LightItalic.ttf");
        else return Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Light.ttf");
    }

    public static Typeface getMediumTypeface(Context context){
        return Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Medium.ttf");
    }

    public static Typeface getMediumTypeface(Context context, boolean italic){
        if (italic) return Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-MediumItalic.ttf");
        else return Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Medium.ttf");
    }
}
