/*
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

package com.cray.software.justreminder.utils;

import android.os.Environment;

import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.helpers.SyncHelper;

import java.io.File;

public class MemoryUtil {

    public static File getRDir() {
        if (SyncHelper.isSdPresent()) {
            File sdPath = Environment.getExternalStorageDirectory();
            return new File(sdPath.toString() + "/JustReminder/" + Constants.DIR_SD);
        } else return null;
    }

    public static File getGroupsDir() {
        if (SyncHelper.isSdPresent()) {
            File sdPath = Environment.getExternalStorageDirectory();
            return new File(sdPath.toString() + "/JustReminder/" + Constants.DIR_GROUP_SD);
        } else return null;
    }

    public static File getBDir() {
        if (SyncHelper.isSdPresent()) {
            File sdPath = Environment.getExternalStorageDirectory();
            return new File(sdPath.toString() + "/JustReminder/" + Constants.DIR_BIRTHDAY_SD);
        } else return null;
    }

    public static File getNDir() {
        if (SyncHelper.isSdPresent()) {
            File sdPath = Environment.getExternalStorageDirectory();
            return new File(sdPath.toString() + "/JustReminder/" + Constants.DIR_NOTES_SD);
        } else return null;
    }

    public static File getDRDir() {
        if (SyncHelper.isSdPresent()) {
            File sdPath = Environment.getExternalStorageDirectory();
            return new File(sdPath.toString() + "/JustReminder/" + Constants.DIR_SD_DBX_TMP);
        } else return null;
    }

    public static File getDGroupsDir() {
        if (SyncHelper.isSdPresent()) {
            File sdPath = Environment.getExternalStorageDirectory();
            return new File(sdPath.toString() + "/JustReminder/" + Constants.DIR_GROUP_SD_DBX_TMP);
        } else return null;
    }

    public static File getDBDir() {
        if (SyncHelper.isSdPresent()) {
            File sdPath = Environment.getExternalStorageDirectory();
            return new File(sdPath.toString() + "/JustReminder/" + Constants.DIR_BIRTHDAY_SD_DBX_TMP);
        } else return null;
    }

    public static File getDNDir() {
        if (SyncHelper.isSdPresent()) {
            File sdPath = Environment.getExternalStorageDirectory();
            return new File(sdPath.toString() + "/JustReminder/" + Constants.DIR_NOTES_SD_DBX_TMP);
        } else return null;
    }

    public static File getGRDir() {
        if (SyncHelper.isSdPresent()) {
            File sdPath = Environment.getExternalStorageDirectory();
            return new File(sdPath.toString() + "/JustReminder/" + Constants.DIR_SD_GDRIVE_TMP);
        } else return null;
    }

    public static File getGGroupsDir() {
        if (SyncHelper.isSdPresent()) {
            File sdPath = Environment.getExternalStorageDirectory();
            return new File(sdPath.toString() + "/JustReminder/" + Constants.DIR_GROUP_SD_GDRIVE_TMP);
        } else return null;
    }

    public static File getGBDir() {
        if (SyncHelper.isSdPresent()) {
            File sdPath = Environment.getExternalStorageDirectory();
            return new File(sdPath.toString() + "/JustReminder/" + Constants.DIR_BIRTHDAY_SD_GDRIVE_TMP);
        } else return null;
    }

    public static File getGNDir() {
        if (SyncHelper.isSdPresent()) {
            File sdPath = Environment.getExternalStorageDirectory();
            return new File(sdPath.toString() + "/JustReminder/" + Constants.DIR_NOTES_SD_GDRIVE_TMP);
        } else return null;
    }
}
