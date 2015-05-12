package com.cray.software.justreminder.modules;

import com.cray.software.justreminder.BuildConfig;

public class ManageModule {
    public boolean isPro(){
        return BuildConfig.IS_PRO;
    }
}
