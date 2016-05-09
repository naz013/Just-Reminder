package com.cray.software.justreminder.utils;

import android.content.Context;
import android.graphics.Typeface;

public class AssetsUtil {
    public AssetsUtil(){
    }

    public static Typeface getTypeface(Context context, int code){
        Typeface typeface;
        if (code == 0) {
            typeface = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Black.ttf");
        } else if (code == 1) {
            typeface = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-BlackItalic.ttf");
        } else if (code == 2) {
            typeface = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Bold.ttf");
        } else if (code == 3) {
            typeface = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-BoldItalic.ttf");
        } else if (code == 4) {
            typeface = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Italic.ttf");
        } else if (code == 5) {
            typeface = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Light.ttf");
        } else if (code == 6) {
            typeface = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-LightItalic.ttf");
        } else if (code == 7) {
            typeface = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Medium.ttf");
        } else if (code == 8) {
            typeface = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-MediumItalic.ttf");
        } else if (code == 9) {
            typeface = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Regular.ttf");
        } else if (code == 10) {
            typeface = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Thin.ttf");
        } else if (code == 11) {
            typeface = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-ThinItalic.ttf");
        } else {
            typeface = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Regular.ttf");
        }
        return typeface;
    }
}
