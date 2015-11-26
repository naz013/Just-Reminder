package com.cray.software.justreminder.helpers;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import com.cray.software.justreminder.modules.Module;

/**
 * Copyright 2015 Nazar Suhovich
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Helper class for checking app permissions on Android 6.0 Marshmallow and above.
 */
@TargetApi(Build.VERSION_CODES.M)
public class Permissions {

    private Context mContext;

    /**
     * Permission constants.
     */
    public static final String READ_CONTACTS = Manifest.permission.READ_CONTACTS;
    public static final String GET_ACCOUNTS = Manifest.permission.GET_ACCOUNTS;

    public static final String READ_CALENDAR = Manifest.permission.READ_CALENDAR;
    public static final String WRITE_CALENDAR = Manifest.permission.WRITE_CALENDAR;

    public static final String WRITE_EXTERNAL = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    public static final String READ_EXTERNAL = Manifest.permission.READ_EXTERNAL_STORAGE;

    public static final String ACCESS_FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    public static final String ACCESS_COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;

    public static final String READ_PHONE_STATE = Manifest.permission.READ_PHONE_STATE;
    public static final String CALL_PHONE = Manifest.permission.CALL_PHONE;

    public static final String SEND_SMS = Manifest.permission.SEND_SMS;
    
    public static final String MANAGE_DOCUMENTS = Manifest.permission.MANAGE_DOCUMENTS;

    public Permissions(Context context){
        this.mContext = context;
    }

    /**
     * Check if permission is allowed on Android 6.0 and above.
     * @param permission permission constant.
     * @return boolean
     */
    public boolean checkPermission(String permission) {
        if (!Module.isMarshmallow()) return true;
        return mContext.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Ask user for a permission.
     * @param activity activity.
     * @param permission permission constant.
     * @param requestCode request code.
     */
    public void requestPermission(Activity activity, String[] permission, int requestCode){
        activity.requestPermissions(permission, requestCode);
    }

    /**
     * Show info about permission.
     * @param activity activity.
     * @param permission permission constant.
     */
    public void showInfo(Activity activity, String permission){
        activity.shouldShowRequestPermissionRationale(permission);
    }
}
