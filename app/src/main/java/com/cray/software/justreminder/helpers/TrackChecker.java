package com.cray.software.justreminder.helpers;

import android.content.Context;

import com.cray.software.justreminder.interfaces.Constants;

public class TrackChecker {
    Context tContext;
    SharedPrefs sPrefs;

    public TrackChecker(Context context){
        this.tContext = context;
    }

    public void saveTrackNumber(int id){
        sPrefs = new SharedPrefs(tContext);
        sPrefs.saveInt(Constants.APP_UI_PREFERENCES_CHECKED_LAYOUT, id);
    }

    public int getTrackedId(){
        sPrefs = new SharedPrefs(tContext);
        return sPrefs.loadInt(Constants.APP_UI_PREFERENCES_CHECKED_LAYOUT);
    }
}
