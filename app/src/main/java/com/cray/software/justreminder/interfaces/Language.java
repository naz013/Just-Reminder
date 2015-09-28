package com.cray.software.justreminder.interfaces;

import android.content.Context;

import com.cray.software.justreminder.helpers.SharedPrefs;

import java.util.Locale;

public class Language {
    public static final String ENGLISH = "en";
    public static final String FRENCH = "fr";
    public static final String GERMAN = "de";
    public static final String ITALIAN = "it";
    public static final String JAPANESE = "ja";
    public static final String KOREAN = "ko";
    public static final String POLISH = "pl";
    public static final String RUSSIAN = "ru";
    public static final String SPANISH = "es";

    /**
     * Get locale for tts.
     * @param context application context.
     * @param birth flag for birthdays.
     * @return Locale
     */
    public Locale getLocale(Context context, boolean birth){
        Locale res = null;
        SharedPrefs sPrefs = new SharedPrefs(context);
        switch (sPrefs.loadPrefs(birth ? Prefs.BIRTHDAY_TTS_LOCALE : Prefs.TTS_LOCALE)){
            case ENGLISH:
                res = Locale.ENGLISH;
                break;
            case FRENCH:
                res = Locale.FRENCH;
                break;
            case GERMAN:
                res = Locale.GERMAN;
                break;
            case JAPANESE:
                res = Locale.JAPANESE;
                break;
            case ITALIAN:
                res = Locale.ITALIAN;
                break;
            case KOREAN:
                res = Locale.KOREAN;
                break;
            case POLISH:
                res = new Locale("pl", "");
                break;
            case RUSSIAN:
                res = new Locale("ru", "");
                break;
            case SPANISH:
                res = new Locale("es", "");
                break;
        }
        return res;
    }
}
