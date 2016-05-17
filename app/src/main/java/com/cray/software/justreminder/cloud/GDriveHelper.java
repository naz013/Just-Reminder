/**
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

package com.cray.software.justreminder.cloud;

import android.content.Context;
import android.os.Environment;

import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.FileConfig;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.datas.models.UserModel;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.utils.MemoryUtil;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.About;
import com.google.api.services.drive.model.FileList;

import org.json.JSONException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Google Drive API helper class.
 */
public class GDriveHelper {

    private Context mContext;
    private SharedPrefs prefs;

    private final HttpTransport mTransport = AndroidHttp.newCompatibleTransport();
    private final JsonFactory mJsonFactory = GsonFactory.getDefaultInstance();
    private Drive driveService;
    private static final String APPLICATION_NAME = "Reminder/5.0";

    public GDriveHelper(Context context){
        this.mContext = context;
    }

    /**
     * Authorization method.
     */
    public void authorize(){
        prefs = new SharedPrefs(mContext);
        GoogleAccountCredential m_credential = GoogleAccountCredential.usingOAuth2(mContext, Collections.singleton(DriveScopes.DRIVE));
        m_credential.setSelectedAccountName(SyncHelper.decrypt(prefs.loadPrefs(Prefs.DRIVE_USER)));
        driveService = new Drive.Builder(
                mTransport, mJsonFactory, m_credential).setApplicationName(APPLICATION_NAME)
                .build();
    }

    /**
     * Check if user has already login to Google Drive from this application.
     * @return return true if user was already logged.
     */
    public boolean isLinked(){
        prefs = new SharedPrefs(mContext);
        return SyncHelper.decrypt(prefs.loadPrefs(Prefs.DRIVE_USER)).matches(".*@.*");
    }

    /**
     * Logout from Drive on this application.
     */
    public void unlink(){
        prefs = new SharedPrefs(mContext);
        prefs.savePrefs(Prefs.DRIVE_USER, Constants.DRIVE_USER_NONE);
    }

    /**
     * Get information about user.
     * @return user info object
     */
    public UserModel getData() {
        if (isLinked()) {
            authorize();
            try {
                About about = driveService.about().get().setFields("user, storageQuota").execute();
                About.StorageQuota quota = about.getStorageQuota();
                return new UserModel(about.getUser().getDisplayName(), quota.getLimit(),
                        quota.getUsage(), countFiles(), about.getUser().getPhotoLink());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Count all backup files stored on Google Drive.
     * @return number of files in local folder.
     */
    public int countFiles(){
        int count = 0;
        File dir = MemoryUtil.getGRDir();
        if (dir != null && dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null) count += files.length;
        }
        dir = MemoryUtil.getGNDir();
        if (dir != null && dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null) count += files.length;
        }
        dir = MemoryUtil.getGBDir();
        if (dir != null && dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null) count += files.length;
        }
        dir = MemoryUtil.getGGroupsDir();
        if (dir != null && dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null) count += files.length;
        }

        return count;
    }

    /**
     * Upload all reminder backup files stored on SD Card.
     * @throws IOException
     */
    public void saveReminderToDrive() throws IOException {
        if (isLinked()) {
            authorize();
            String folderId = getFolderId();
            if (folderId == null){
                com.google.api.services.drive.model.File destFolder = createFolder();
                folderId = destFolder.getId();
            }

            File sdPath = Environment.getExternalStorageDirectory();
            File sdPathDr = new File(sdPath.toString() + "/JustReminder/" + Constants.DIR_SD);
            File[] files = sdPathDr.listFiles();
            if (files != null) {
                for (File file : files) {
                    com.google.api.services.drive.model.File fileMetadata = new com.google.api.services.drive.model.File();
                    fileMetadata.setName(file.getName());
                    fileMetadata.setDescription("Reminder Backup");
                    fileMetadata.setParents(Collections.singletonList(folderId));
                    FileContent mediaContent = new FileContent("text/plain", file);
                    deleteReminder(file.getName());
                    driveService.files().create(fileMetadata, mediaContent)
                            .setFields("id")
                            .execute();
                }
            }
        }
    }

    /**
     * Upload all note backup files stored on SD Card.
     * @throws IOException
     */
    public void saveNoteToDrive() throws IOException {
        if (isLinked()) {
            authorize();
            String folderId = getFolderId();
            if (folderId == null){
                com.google.api.services.drive.model.File destFolder = createFolder();
                folderId = destFolder.getId();
            }

            File sdPath = Environment.getExternalStorageDirectory();
            File sdPathDr = new File(sdPath.toString() + "/JustReminder/" + Constants.DIR_NOTES_SD);
            File[] files = sdPathDr.listFiles();
            if (files != null) {
                for (File file : files) {
                    com.google.api.services.drive.model.File fileMetadata = new com.google.api.services.drive.model.File();
                    fileMetadata.setName(file.getName());
                    fileMetadata.setDescription("Note Backup");
                    fileMetadata.setParents(Collections.singletonList(folderId));
                    FileContent mediaContent = new FileContent("text/plain", file);
                    deleteNote(file.getName());
                    driveService.files().create(fileMetadata, mediaContent)
                            .setFields("id")
                            .execute();
                }
            }
        }
    }

    /**
     * Upload all group backup files stored on SD Card.
     * @throws IOException
     */
    public void saveGroupToDrive() throws IOException {
        if (isLinked()) {
            authorize();
            String folderId = getFolderId();
            if (folderId == null){
                com.google.api.services.drive.model.File destFolder = createFolder();
                folderId = destFolder.getId();
            }

            File sdPath = Environment.getExternalStorageDirectory();
            File sdPathDr = new File(sdPath.toString() + "/JustReminder/" + Constants.DIR_GROUP_SD);
            File[] files = sdPathDr.listFiles();
            if (files != null) {
                for (File file : files) {
                    com.google.api.services.drive.model.File fileMetadata = new com.google.api.services.drive.model.File();
                    fileMetadata.setName(file.getName());
                    fileMetadata.setDescription("Group Backup");
                    fileMetadata.setParents(Collections.singletonList(folderId));
                    FileContent mediaContent = new FileContent("text/plain", file);
                    deleteGroup(file.getName());
                    driveService.files().create(fileMetadata, mediaContent)
                            .setFields("id")
                            .execute();
                }
            }
        }
    }

    /**
     * Upload all birthday backup files stored on SD Card.
     * @throws IOException
     */
    public void saveBirthToDrive() throws IOException {
        if (isLinked()) {
            authorize();
            String folderId = getFolderId();
            if (folderId == null){
                com.google.api.services.drive.model.File destFolder = createFolder();
                folderId = destFolder.getId();
            }

            File sdPath = Environment.getExternalStorageDirectory();
            File sdPathDr = new File(sdPath.toString() + "/JustReminder/" + Constants.DIR_BIRTHDAY_SD);
            File[] files = sdPathDr.listFiles();
            if (files != null) {
                for (File file : files) {
                    com.google.api.services.drive.model.File fileMetadata = new com.google.api.services.drive.model.File();
                    fileMetadata.setName(file.getName());
                    fileMetadata.setDescription("Birthday Backup");
                    fileMetadata.setParents(Collections.singletonList(folderId));
                    FileContent mediaContent = new FileContent("text/plain", file);
                    deleteBirthday(file.getName());
                    driveService.files().create(fileMetadata, mediaContent)
                            .setFields("id")
                            .execute();
                }
            }
        }
    }

    /**
     * Download on SD Card all reminder backup files stored on Google Drive.
     * @throws IOException
     */
    public void downloadReminder() throws IOException {
        if (isLinked()) {
            authorize();
            File sdPath = Environment.getExternalStorageDirectory();
            File sdPathDr = new File(sdPath.toString() + "/JustReminder/" + Constants.DIR_SD_GDRIVE_TMP);
            //deleteFolders();
            Drive.Files.List request;
            try {
                request = driveService.files().list().setQ("mimeType = 'application/json'"); // .setQ("mimeType=\"text/plain\"");
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            do {
                FileList files;
                try {
                    files = request.execute();
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
                ArrayList<com.google.api.services.drive.model.File> fileList = (ArrayList<com.google.api.services.drive.model.File>) files.getFiles();
                for (com.google.api.services.drive.model.File f : fileList) {
                    String title = f.getName();
                    if (!sdPathDr.exists() && !sdPathDr.mkdirs()) {
                        throw new IOException("Unable to create parent directory");
                    }

                    if (title.endsWith(FileConfig.FILE_NAME_REMINDER)) {
                        File file = new File(sdPathDr, title);
                        if (!file.exists()) {
                            try {
                                file.createNewFile();
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                        }
                        OutputStream out = new FileOutputStream(file);
                        driveService.files().get(f.getId()).executeMediaAndDownloadTo(out);
                        try {
                            new SyncHelper(mContext).reminderFromJson(file.toString());
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
                request.setPageToken(files.getNextPageToken());
            } while (request.getPageToken() != null && request.getPageToken().length() >= 0);
        }
    }

    /**
     * Download on SD Card all note backup files stored on Google Drive.
     * @throws IOException
     */
    public void downloadNote() throws IOException {
        if (isLinked()) {
            authorize();
            File sdPath = Environment.getExternalStorageDirectory();
            File sdPathDr = new File(sdPath.toString() + "/JustReminder/" + Constants.DIR_NOTES_SD_GDRIVE_TMP);
            //deleteFolders();
            Drive.Files.List request;
            try {
                request = driveService.files().list().setQ("mimeType = 'text/plain'"); // .setQ("mimeType=\"text/plain\"");
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            do {
                FileList files;
                try {
                    files = request.execute();
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
                ArrayList<com.google.api.services.drive.model.File> fileList = (ArrayList<com.google.api.services.drive.model.File>) files.getFiles();
                for (com.google.api.services.drive.model.File f : fileList) {
                    String title = f.getName();
                    if (!sdPathDr.exists() && !sdPathDr.mkdirs()) {
                        throw new IOException("Unable to create parent directory");
                    }

                    if (title.endsWith(FileConfig.FILE_NAME_NOTE)) {
                        File file = new File(sdPathDr, title);
                        if (!file.exists()) {
                            try {
                                file.createNewFile(); //otherwise dropbox client will fail silently
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                        }
                        OutputStream out = new FileOutputStream(file);
                        driveService.files().get(f.getId()).executeMediaAndDownloadTo(out);
                        try {
                            new SyncHelper(mContext).noteFromJson(file.toString(), title);
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
                request.setPageToken(files.getNextPageToken());
            } while (request.getPageToken() != null && request.getPageToken().length() >= 0);
        }
    }

    /**
     * Download on SD Card all group backup files stored on Google Drive.
     * @throws IOException
     */
    public void downloadGroup() throws IOException {
        if (isLinked()) {
            authorize();
            File sdPath = Environment.getExternalStorageDirectory();
            File sdPathDr = new File(sdPath.toString() + "/JustReminder/" + Constants.DIR_GROUP_SD_GDRIVE_TMP);
            //deleteFolders();
            Drive.Files.List request;
            try {
                request = driveService.files().list().setQ("mimeType = 'text/plain'"); // .setQ("mimeType=\"text/plain\"");
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            do {
                FileList files;
                try {
                    files = request.execute();
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
                ArrayList<com.google.api.services.drive.model.File> fileList = (ArrayList<com.google.api.services.drive.model.File>) files.getFiles();
                for (com.google.api.services.drive.model.File f : fileList) {
                    String title = f.getName();
                    if (!sdPathDr.exists() && !sdPathDr.mkdirs()) {
                        throw new IOException("Unable to create parent directory");
                    }

                    if (title.endsWith(FileConfig.FILE_NAME_GROUP)) {
                        File file = new File(sdPathDr, title);
                        if (!file.exists()) {
                            try {
                                file.createNewFile();
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                        }
                        OutputStream out = new FileOutputStream(file);
                        driveService.files().get(f.getId()).executeMediaAndDownloadTo(out);
                        try {
                            new SyncHelper(mContext).groupFromJson(file);
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
                request.setPageToken(files.getNextPageToken());
            } while (request.getPageToken() != null && request.getPageToken().length() >= 0);
        }
    }

    /**
     * Download on SD Card all birthday backup files stored on Google Drive.
     * @throws IOException
     */
    public void downloadBirthday(boolean deleteFile) throws IOException {
        if (isLinked()) {
            authorize();
            File sdPath = Environment.getExternalStorageDirectory();
            File sdPathDr = new File(sdPath.toString() + "/JustReminder/" + Constants.DIR_BIRTHDAY_SD_GDRIVE_TMP);

            Drive.Files.List request;
            try {
                request = driveService.files().list().setQ("mimeType = 'text/plain'"); // .setQ("mimeType=\"text/plain\"");
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            do {
                FileList files;
                try {
                    files = request.execute();
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
                ArrayList<com.google.api.services.drive.model.File> fileList = (ArrayList<com.google.api.services.drive.model.File>) files.getFiles();
                for (com.google.api.services.drive.model.File f : fileList) {
                    String title = f.getName();
                    if (!sdPathDr.exists() && !sdPathDr.mkdirs()) {
                        throw new IOException("Unable to create parent directory");
                    }

                    if (title.endsWith(FileConfig.FILE_NAME_BIRTHDAY)) {
                        File file = new File(sdPathDr, title);
                        if (!file.exists()) {
                            try {
                                file.createNewFile();
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                        }
                        OutputStream out = new FileOutputStream(file);
                        driveService.files().get(f.getId()).executeMediaAndDownloadTo(out);

                        if (deleteFile) deleteBirthday(title);
                        try {
                            new SyncHelper(mContext).birthdayFromJson(file);
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
                request.setPageToken(files.getNextPageToken());
            } while (request.getPageToken() != null && request.getPageToken().length() >= 0);
        }
    }

    /**
     * Delete reminder backup file from Google Drive by file name.
     * @param title file name.
     */
    public void deleteReminder(String title){
        String[] strs = title.split(".");
        if (strs.length != 0) {
            title = strs[0];
        }
        if (isLinked()) {
            authorize();
            Drive.Files.List request = null;
            try {
                request = driveService.files().list().setQ("mimeType = 'application/json'");
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (request != null) {
                do {
                    FileList files;
                    try {
                        files = request.execute();
                    } catch (IOException e) {
                        e.printStackTrace();
                        break;
                    }
                    ArrayList<com.google.api.services.drive.model.File> fileList = (ArrayList<com.google.api.services.drive.model.File>) files.getFiles();
                    for (com.google.api.services.drive.model.File f : fileList) {
                        String fileTitle = f.getName();
                        if (fileTitle.endsWith(FileConfig.FILE_NAME_REMINDER) && fileTitle.contains(title)) {
                            try {
                                driveService.files().delete(f.getId()).execute();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    request.setPageToken(files.getNextPageToken());
                } while (request.getPageToken() != null && request.getPageToken().length() >= 0);
            }
        }
    }

    /**
     * Delete note backup file from Google Drive by file name.
     * @param title file name.
     */
    public void deleteNote (String title){
        String[] strs = title.split(".");
        if (strs.length != 0) {
            title = strs[0];
        }
        if (isLinked()) {
            authorize();
            Drive.Files.List request = null;
            try {
                request = driveService.files().list().setQ("mimeType = 'text/plain'");
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (request != null) {
                do {
                    FileList files;
                    try {
                        files = request.execute();
                    } catch (IOException e) {
                        e.printStackTrace();
                        break;
                    }
                    ArrayList<com.google.api.services.drive.model.File> fileList = (ArrayList<com.google.api.services.drive.model.File>) files.getFiles();
                    for (com.google.api.services.drive.model.File f : fileList) {
                        String fileTitle = f.getName();
                        if (fileTitle.endsWith(FileConfig.FILE_NAME_NOTE) && fileTitle.contains(title)) {
                            try {
                                driveService.files().delete(f.getId()).execute();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    request.setPageToken(files.getNextPageToken());
                } while (request.getPageToken() != null && request.getPageToken().length() >= 0);
            }
        }
    }

    /**
     * Delete group backup file from Google Drive by file name.
     * @param title file name.
     */
    public void deleteGroup (String title){
        String[] strs = title.split(".");
        if (strs.length != 0) {
            title = strs[0];
        }
        if (isLinked()) {
            authorize();
            Drive.Files.List request = null;
            try {
                request = driveService.files().list().setQ("mimeType = 'text/plain'");
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (request != null) {
                do {
                    FileList files;
                    try {
                        files = request.execute();
                    } catch (IOException e) {
                        e.printStackTrace();
                        break;
                    }
                    ArrayList<com.google.api.services.drive.model.File> fileList = (ArrayList<com.google.api.services.drive.model.File>) files.getFiles();
                    for (com.google.api.services.drive.model.File f : fileList) {
                        String fileTitle = f.getName();
                        if (fileTitle.endsWith(FileConfig.FILE_NAME_GROUP) && fileTitle.contains(title)) {
                            try {
                                driveService.files().delete(f.getId()).execute();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    request.setPageToken(files.getNextPageToken());
                } while (request.getPageToken() != null && request.getPageToken().length() >= 0);
            }
        }
    }

    /**
     * Delete birthday backup file from Google Drive by file name.
     * @param title file name.
     */
    public void deleteBirthday(String title){
        String[] strs = title.split(".");
        if (strs.length != 0) {
            title = strs[0];
        }
        if (isLinked()) {
            authorize();
            Drive.Files.List request = null;
            try {
                request = driveService.files().list().setQ("mimeType = 'text/plain'");
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (request != null) {
                do {
                    FileList files;
                    try {
                        files = request.execute();
                    } catch (IOException e) {
                        e.printStackTrace();
                        break;
                    }
                    ArrayList<com.google.api.services.drive.model.File> fileList = (ArrayList<com.google.api.services.drive.model.File>) files.getFiles();
                    for (com.google.api.services.drive.model.File f : fileList) {
                        String fileTitle = f.getName();
                        if (fileTitle.endsWith(FileConfig.FILE_NAME_BIRTHDAY) && fileTitle.contains(title)) {
                            try {
                                driveService.files().delete(f.getId()).execute();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    request.setPageToken(files.getNextPageToken());
                } while (request.getPageToken() != null && request.getPageToken().length() >= 0);
            }
        }
    }

    public void deleteFile(String fileName) {
        if (fileName.endsWith(FileConfig.FILE_NAME_REMINDER))
            deleteReminder(fileName);
        else if (fileName.endsWith(FileConfig.FILE_NAME_NOTE))
            deleteNote(fileName);
        else if (fileName.endsWith(FileConfig.FILE_NAME_GROUP))
            deleteGroup(fileName);
        else if (fileName.endsWith(FileConfig.FILE_NAME_BIRTHDAY))
            deleteBirthday(fileName);
    }

    /**
     * Delete application folder from Google Drive.
     */
    public void clean(){
        if (isLinked()) {
            authorize();
            Drive.Files.List requestF = null;
            try {
                requestF = driveService.files().list().setQ("mimeType = 'application/vnd.google-apps.folder'");
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (requestF != null) {
                do {
                    FileList files;
                    try {
                        files = requestF.execute();
                    } catch (IOException e) {
                        e.printStackTrace();
                        break;
                    }
                    ArrayList<com.google.api.services.drive.model.File> fileList = (ArrayList<com.google.api.services.drive.model.File>) files.getFiles();
                    for (com.google.api.services.drive.model.File f : fileList) {
                        String fileMIME = f.getMimeType();

                        if (fileMIME.matches("application/vnd.google-apps.folder") && f.getName().matches("Just Reminder")) {
                            try {
                                driveService.files().delete(f.getId()).execute();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            break;
                        }
                    }
                    requestF.setPageToken(files.getNextPageToken());
                } while (requestF.getPageToken() != null && requestF.getPageToken().length() >= 0);
            }
        }
    }

    /**
     * Get application folder identifier on Google Drive.
     * @return Drive folder identifier.
     */
    private String getFolderId(){
        String id = null;
        Drive.Files.List request = null;
        try {
            request = driveService.files().list().setQ("mimeType = 'application/vnd.google-apps.folder'");
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (request != null) {
            do {
                FileList files = null;
                try {
                    files = request.execute();
                } catch (IOException e) {
                    e.printStackTrace();
                    id = null;
                }
                if (files != null) {
                    ArrayList<com.google.api.services.drive.model.File> fileList =
                            (ArrayList<com.google.api.services.drive.model.File>) files.getFiles();
                    for (com.google.api.services.drive.model.File f : fileList) {
                        String fileMIME = f.getMimeType();

                        if (fileMIME.trim().matches("application/vnd.google-apps.folder") &&
                                f.getName().contains("Just Reminder")) {
                            id = f.getId();
                        }
                    }
                    request.setPageToken(files.getNextPageToken());
                } else id = null;
            } while (request.getPageToken() != null && request.getPageToken().length() >= 0);
        } else id = null;
        return id;
    }

    /**
     * Create application folder on Google Drive.
     * @return Drive folder
     * @throws IOException
     */
    private com.google.api.services.drive.model.File createFolder() throws IOException {
        com.google.api.services.drive.model.File folder = new com.google.api.services.drive.model.File();
        folder.setName("Just Reminder");
        folder.setMimeType("application/vnd.google-apps.folder");
        Drive.Files.Create folderInsert = null;
        try {
            folderInsert = driveService.files().create(folder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return folderInsert != null ? folderInsert.execute() : null;
    }
}
