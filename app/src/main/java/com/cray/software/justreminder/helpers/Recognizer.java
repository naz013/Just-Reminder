package com.cray.software.justreminder.helpers;

import android.content.Context;
import android.util.Log;

import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.utils.RecognizerUtils;

import java.util.ArrayList;

public class Recognizer {

    private Context mContext;

    public Recognizer(Context context){
        this.mContext = context;
    }

    public void parseResults(ArrayList matches, boolean isWidget){
        SharedPrefs prefs = new SharedPrefs(mContext);
        String language = prefs.loadPrefs(Prefs.VOICE_LANGUAGE);
        for (Object key : matches){
            String keyStr = key.toString().toLowerCase().trim();
            Log.d(Constants.LOG_TAG, keyStr);
            if (RecognizerUtils.isNumberContains(keyStr)) keyStr = RecognizerUtils.convertToNumbered(keyStr);

            if (language.matches(Constants.LANGUAGE_EN)){
                if (parseEnglish(keyStr, isWidget)) break;
            } else if (language.matches(Constants.LANGUAGE_RU)){
                if (parseRussian(keyStr, isWidget)) break;
            } else if (language.matches(Constants.LANGUAGE_UK)){
                if (parseUkrainian(keyStr, isWidget)) break;
            }
        }
    }

    private boolean parseEnglish(String keyStr, boolean isWidget) {
        return false;
    }

    private boolean parseRussian(String keyStr, boolean isWidget) {
        return false;
    }

    private boolean parseUkrainian(String keyStr, boolean isWidget) {
        return false;
    }
}