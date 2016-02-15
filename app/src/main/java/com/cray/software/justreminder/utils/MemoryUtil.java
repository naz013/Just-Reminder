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

import java.io.File;
import java.util.Locale;

public class MemoryUtil {

    /**
     * Check if device has SD Card.
     * @return Boolean
     */
    public static boolean isSdPresent() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

    /**
     * Get directory with reminders backup files.
     * @return Directory
     */
    public static File getRDir() {
        return getDir(Constants.DIR_SD);
    }

    /**
     * Get directory with groups backup files.
     * @return Directory
     */
    public static File getGroupsDir() {
        return getDir(Constants.DIR_GROUP_SD);
    }

    /**
     * Get directory with birthdays backup files.
     * @return Directory
     */
    public static File getBDir() {
        return getDir(Constants.DIR_BIRTHDAY_SD);
    }

    /**
     * Get directory with notes backup files.
     * @return Directory
     */
    public static File getNDir() {
        return getDir(Constants.DIR_NOTES_SD);
    }

    /**
     * Get directory with reminders backup files from Dropbox.
     * @return Directory
     */
    public static File getDRDir() {
        return getDir(Constants.DIR_SD_DBX_TMP);
    }

    /**
     * Get directory with groups backup files from Dropbox.
     * @return Directory
     */
    public static File getDGroupsDir() {
        return getDir(Constants.DIR_GROUP_SD_DBX_TMP);
    }

    /**
     * Get directory with birthdays backup files from Dropbox.
     * @return Directory
     */
    public static File getDBDir() {
        return getDir(Constants.DIR_BIRTHDAY_SD_DBX_TMP);
    }

    /**
     * Get directory with notes backup files from Dropbox.
     * @return Directory
     */
    public static File getDNDir() {
        return getDir(Constants.DIR_NOTES_SD_DBX_TMP);
    }

    /**
     * Get directory with reminders backup files from Google Drive.
     * @return Directory
     */
    public static File getGRDir() {
        return getDir(Constants.DIR_SD_GDRIVE_TMP);
    }

    /**
     * Get directory with groups backup files from Google Drive.
     * @return Directory
     */
    public static File getGGroupsDir() {
        return getDir(Constants.DIR_GROUP_SD_GDRIVE_TMP);
    }

    /**
     * Get directory with birthdays backup files from Google Drive.
     * @return Directory
     */
    public static File getGBDir() {
        return getDir(Constants.DIR_BIRTHDAY_SD_GDRIVE_TMP);
    }

    /**
     * Get directory with notes backup files from Google Drive.
     * @return Directory
     */
    public static File getGNDir() {
        return getDir(Constants.DIR_NOTES_SD_GDRIVE_TMP);
    }

    /**
     * Get directory with notes email attachments.
     * @return Directory
     */
    public static File getMailDir() {
        return getDir(Constants.DIR_MAIL_SD);
    }

    /**
     * Get directory with settings backup file.
     * @return Directory
     */
    public static File getPrefsDir() {
        return getDir(Constants.DIR_PREFS);
    }

    /**
     * Get application parent directory on SD Card.
     * @return Directory
     */
    public static File getParent() {
        return getDir("");
    }

    /**
     * Get directory by child.
     * @param directory child directory name.
     * @return file object or null if cant access external storage.
     */
    public static File getDir(String directory) {
        if (isSdPresent()) {
            File sdPath = Environment.getExternalStorageDirectory();
            File dir = new File(sdPath.toString() + "/JustReminder/" + directory);
            if (!dir.exists()) {
                if (dir.mkdirs()) return dir;
            }
            return dir;
        } else return null;
    }

    /**
     * Get directory with images.
     * @return file object or null if cant access external storage.
     */
    public static File getImagesDir() {
        return getDir("image_cache");
    }

    public static String humanReadableByte(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) {
            return bytes + " B";
        }
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "");
        return String.format(Locale.getDefault(), "%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }
}
