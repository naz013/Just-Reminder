package com.cray.software.justreminder.cloud;

import android.content.Context;
import android.os.Environment;

import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.interfaces.Prefs;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.media.MediaHttpDownloader;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.ParentReference;

import org.json.JSONException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;

public class GDriveHelper {

    Context ctx;
    SharedPrefs prefs;

    private final HttpTransport m_transport = AndroidHttp.newCompatibleTransport();
    private final JsonFactory m_jsonFactory = GsonFactory.getDefaultInstance();
    private GoogleAccountCredential m_credential;
    private com.google.api.services.drive.Drive m_client;
    private static final String APPLICATION_NAME = "Just Reminder/2.3.4";

    public GDriveHelper(Context context){
        this.ctx = context;
    }

    public void authorize(){
        prefs = new SharedPrefs(ctx);
        m_credential = GoogleAccountCredential.usingOAuth2(ctx, Collections.singleton(DriveScopes.DRIVE));
        m_credential.setSelectedAccountName(new SyncHelper(ctx).decrypt(prefs.loadPrefs(Prefs.DRIVE_USER)));
        m_client = new com.google.api.services.drive.Drive.Builder(
                m_transport, m_jsonFactory, m_credential).setApplicationName(APPLICATION_NAME)
                .build();
    }

    public boolean isLinked(){
        prefs = new SharedPrefs(ctx);
        return new SyncHelper(ctx).decrypt(prefs.loadPrefs(Prefs.DRIVE_USER)).matches(".*@.*");
    }

    public void unlink(){
        prefs = new SharedPrefs(ctx);
        prefs.savePrefs(Prefs.DRIVE_USER, Constants.DRIVE_USER_NONE);
    }

    public int countFiles(){
        authorize();
        int i = 0;
        Drive.Files.List request = null;
        try {
            request = m_client.files().list().setQ("mimeType = 'application/json'");
        } catch (IOException e) {
            e.printStackTrace();
        }
        do {
            FileList files;
            try {
                files = request.execute();
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
            ArrayList<com.google.api.services.drive.model.File> fileList = (ArrayList<com.google.api.services.drive.model.File>) files.getItems();
            for (com.google.api.services.drive.model.File f : fileList) {
                String fileTitle = f.getTitle();

                if (fileTitle.trim().endsWith(Constants.FILE_NAME_REMINDER)) {
                    i += 1;
                }
            }
            request.setPageToken(files.getNextPageToken());
        } while (request.getPageToken() != null && request.getPageToken().length() >= 0);
        return i;
    }

    public void saveFileToDrive() throws IOException {
        if (isLinked()) {
            prefs = new SharedPrefs(ctx);
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
                    fileMetadata.setTitle(file.getName());
                    fileMetadata.setDescription("Reminder Backup");
                    fileMetadata.setParents(Collections.singletonList(new ParentReference().setId(folderId)));

                    FileContent mediaContent = new FileContent("application/json", file);

                    deleteFile(file.getName());

                    com.google.api.services.drive.Drive.Files.Insert insert = m_client.files().insert(fileMetadata, mediaContent);
                    MediaHttpUploader uploader = insert.getMediaHttpUploader();
                    uploader.setDirectUploadEnabled(true);
                    insert.execute();
                }
            }
        }
    }

    public void saveNoteToDrive() throws IOException {
        if (isLinked()) {
            prefs = new SharedPrefs(ctx);
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
                    fileMetadata.setTitle(file.getName());
                    fileMetadata.setDescription("Note Backup");
                    fileMetadata.setParents(Collections.singletonList(new ParentReference().setId(folderId)));

                    FileContent mediaContent = new FileContent("text/plain", file);

                    deleteNote(file.getName());

                    com.google.api.services.drive.Drive.Files.Insert insert = m_client.files().insert(fileMetadata, mediaContent);
                    MediaHttpUploader uploader = insert.getMediaHttpUploader();
                    uploader.setDirectUploadEnabled(true);
                    insert.execute();
                }
            }
        }
    }

    public void saveGroupToDrive() throws IOException {
        if (isLinked()) {
            prefs = new SharedPrefs(ctx);
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
                    fileMetadata.setTitle(file.getName());
                    fileMetadata.setDescription("Group Backup");
                    fileMetadata.setParents(Collections.singletonList(new ParentReference().setId(folderId)));

                    FileContent mediaContent = new FileContent("text/plain", file);

                    deleteGroup(file.getName());

                    com.google.api.services.drive.Drive.Files.Insert insert = m_client.files().insert(fileMetadata, mediaContent);
                    MediaHttpUploader uploader = insert.getMediaHttpUploader();
                    uploader.setDirectUploadEnabled(true);
                    insert.execute();
                }
            }
        }
    }

    public void saveBirthToDrive() throws IOException {
        if (isLinked()) {
            prefs = new SharedPrefs(ctx);
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
                    fileMetadata.setTitle(file.getName());
                    fileMetadata.setDescription("Birthday Backup");
                    fileMetadata.setParents(Collections.singletonList(new ParentReference().setId(folderId)));

                    FileContent mediaContent = new FileContent("text/plain", file);

                    deleteGroup(file.getName());

                    com.google.api.services.drive.Drive.Files.Insert insert = m_client.files().insert(fileMetadata, mediaContent);
                    MediaHttpUploader uploader = insert.getMediaHttpUploader();
                    uploader.setDirectUploadEnabled(true);
                    insert.execute();
                }
            }
        }
    }

    public void loadFileFromDrive() throws IOException {
        if (isLinked()) {
            prefs = new SharedPrefs(ctx);
            authorize();
            File sdPath = Environment.getExternalStorageDirectory();
            File sdPathDr = new File(sdPath.toString() + "/JustReminder/" + Constants.DIR_SD_GDRIVE_TMP);
            //deleteFolders();
            Drive.Files.List request;
            try {
                request = m_client.files().list().setQ("mimeType = 'application/json'"); // .setQ("mimeType=\"text/plain\"");
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
                ArrayList<com.google.api.services.drive.model.File> fileList = (ArrayList<com.google.api.services.drive.model.File>) files.getItems();
                for (com.google.api.services.drive.model.File f : fileList) {
                    String title = f.getTitle();
                    if (!sdPathDr.exists() && !sdPathDr.mkdirs()) {
                        throw new IOException("Unable to create parent directory");
                    }

                    if (title.endsWith(Constants.FILE_NAME_REMINDER)) {
                        File file = new File(sdPathDr, title);
                        if (!file.exists()) {
                            try {
                                file.createNewFile(); //otherwise dropbox client will fail silently
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                        }
                        OutputStream out = new FileOutputStream(file);

                        MediaHttpDownloader downloader =
                                new MediaHttpDownloader(m_transport, m_client.getRequestFactory().getInitializer());
                        downloader.setDirectDownloadEnabled(true);
                        downloader.download(new GenericUrl(f.getDownloadUrl()), out);
                        try {
                            new SyncHelper(ctx).importReminderFromJSON(file.toString(), title);
                        } catch (IOException | JSONException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
                request.setPageToken(files.getNextPageToken());
            } while (request.getPageToken() != null && request.getPageToken().length() >= 0);
        }
    }

    public void loadNoteFromDrive() throws IOException {
        if (isLinked()) {
            prefs = new SharedPrefs(ctx);
            authorize();
            File sdPath = Environment.getExternalStorageDirectory();
            File sdPathDr = new File(sdPath.toString() + "/JustReminder/" + Constants.DIR_NOTES_SD_GDRIVE_TMP);
            //deleteFolders();
            Drive.Files.List request;
            try {
                request = m_client.files().list().setQ("mimeType = 'text/plain'"); // .setQ("mimeType=\"text/plain\"");
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
                ArrayList<com.google.api.services.drive.model.File> fileList = (ArrayList<com.google.api.services.drive.model.File>) files.getItems();
                for (com.google.api.services.drive.model.File f : fileList) {
                    String title = f.getTitle();
                    if (!sdPathDr.exists() && !sdPathDr.mkdirs()) {
                        throw new IOException("Unable to create parent directory");
                    }

                    if (title.endsWith(Constants.FILE_NAME_NOTE)) {
                        File file = new File(sdPathDr, title);
                        if (!file.exists()) {
                            try {
                                file.createNewFile(); //otherwise dropbox client will fail silently
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                        }
                        OutputStream out = new FileOutputStream(file);

                        MediaHttpDownloader downloader =
                                new MediaHttpDownloader(m_transport, m_client.getRequestFactory().getInitializer());
                        downloader.setDirectDownloadEnabled(true);
                        downloader.download(new GenericUrl(f.getDownloadUrl()), out);
                        try {
                            new SyncHelper(ctx).importNotes(file.toString(), title);
                        } catch (IOException | JSONException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
                request.setPageToken(files.getNextPageToken());
            } while (request.getPageToken() != null && request.getPageToken().length() >= 0);
        }
    }

    public void loadGroupsFromDrive() throws IOException {
        if (isLinked()) {
            prefs = new SharedPrefs(ctx);
            authorize();
            File sdPath = Environment.getExternalStorageDirectory();
            File sdPathDr = new File(sdPath.toString() + "/JustReminder/" + Constants.DIR_GROUP_SD_GDRIVE_TMP);
            //deleteFolders();
            Drive.Files.List request;
            try {
                request = m_client.files().list().setQ("mimeType = 'text/plain'"); // .setQ("mimeType=\"text/plain\"");
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
                ArrayList<com.google.api.services.drive.model.File> fileList = (ArrayList<com.google.api.services.drive.model.File>) files.getItems();
                for (com.google.api.services.drive.model.File f : fileList) {
                    String title = f.getTitle();
                    if (!sdPathDr.exists() && !sdPathDr.mkdirs()) {
                        throw new IOException("Unable to create parent directory");
                    }

                    if (title.endsWith(Constants.FILE_NAME_GROUP)) {
                        File file = new File(sdPathDr, title);
                        if (!file.exists()) {
                            try {
                                file.createNewFile();
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                        }
                        OutputStream out = new FileOutputStream(file);

                        MediaHttpDownloader downloader =
                                new MediaHttpDownloader(m_transport, m_client.getRequestFactory().getInitializer());
                        downloader.setDirectDownloadEnabled(true);
                        downloader.download(new GenericUrl(f.getDownloadUrl()), out);
                        try {
                            new SyncHelper(ctx).importGroup(file.toString(), title);
                        } catch (IOException | JSONException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
                request.setPageToken(files.getNextPageToken());
            } while (request.getPageToken() != null && request.getPageToken().length() >= 0);
        }
    }

    public void loadBirthFromDrive() throws IOException {
        if (isLinked()) {
            prefs = new SharedPrefs(ctx);
            authorize();
            File sdPath = Environment.getExternalStorageDirectory();
            File sdPathDr = new File(sdPath.toString() + "/JustReminder/" + Constants.DIR_BIRTHDAY_SD_GDRIVE_TMP);
            //deleteFolders();
            Drive.Files.List request;
            try {
                request = m_client.files().list().setQ("mimeType = 'text/plain'"); // .setQ("mimeType=\"text/plain\"");
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
                ArrayList<com.google.api.services.drive.model.File> fileList = (ArrayList<com.google.api.services.drive.model.File>) files.getItems();
                for (com.google.api.services.drive.model.File f : fileList) {
                    String title = f.getTitle();
                    if (!sdPathDr.exists() && !sdPathDr.mkdirs()) {
                        throw new IOException("Unable to create parent directory");
                    }

                    if (title.endsWith(Constants.FILE_NAME_BIRTHDAY)) {
                        File file = new File(sdPathDr, title);
                        if (!file.exists()) {
                            try {
                                file.createNewFile();
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                        }
                        OutputStream out = new FileOutputStream(file);

                        MediaHttpDownloader downloader =
                                new MediaHttpDownloader(m_transport, m_client.getRequestFactory().getInitializer());
                        downloader.setDirectDownloadEnabled(true);
                        downloader.download(new GenericUrl(f.getDownloadUrl()), out);
                        try {
                            new SyncHelper(ctx).importBirthday(file.toString(), title);
                        } catch (IOException | JSONException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
                request.setPageToken(files.getNextPageToken());
            } while (request.getPageToken() != null && request.getPageToken().length() >= 0);
        }
    }

    public void deleteFile (String title){
        if (isLinked()) {
            prefs = new SharedPrefs(ctx);
            authorize();
            Drive.Files.List request = null;
            try {
                request = m_client.files().list().setQ("mimeType = 'application/json'");
            } catch (IOException e) {
                e.printStackTrace();
            }
            do {
                FileList files;
                try {
                    files = request.execute();
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
                ArrayList<com.google.api.services.drive.model.File> fileList = (ArrayList<com.google.api.services.drive.model.File>) files.getItems();
                for (com.google.api.services.drive.model.File f : fileList) {
                    String fileTitle = f.getTitle();

                    if (fileTitle.endsWith(Constants.FILE_NAME_REMINDER) && fileTitle.contains(title)) {
                        try {
                            m_client.files().delete(f.getId()).execute();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                request.setPageToken(files.getNextPageToken());
            } while (request.getPageToken() != null && request.getPageToken().length() >= 0);
        }
    }

    public void deleteNote (String title){
        if (isLinked()) {
            prefs = new SharedPrefs(ctx);
            authorize();
            Drive.Files.List request = null;
            try {
                request = m_client.files().list().setQ("mimeType = 'text/plain'");
            } catch (IOException e) {
                e.printStackTrace();
            }
            do {
                FileList files;
                try {
                    files = request.execute();
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
                ArrayList<com.google.api.services.drive.model.File> fileList = (ArrayList<com.google.api.services.drive.model.File>) files.getItems();
                for (com.google.api.services.drive.model.File f : fileList) {
                    String fileTitle = f.getTitle();

                    if (fileTitle.endsWith(Constants.FILE_NAME_NOTE) && fileTitle.contains(title)) {
                        try {
                            m_client.files().delete(f.getId()).execute();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                request.setPageToken(files.getNextPageToken());
            } while (request.getPageToken() != null && request.getPageToken().length() >= 0);
        }
    }

    public void deleteGroup (String title){
        if (isLinked()) {
            prefs = new SharedPrefs(ctx);
            authorize();
            Drive.Files.List request = null;
            try {
                request = m_client.files().list().setQ("mimeType = 'text/plain'");
            } catch (IOException e) {
                e.printStackTrace();
            }
            do {
                FileList files;
                try {
                    files = request.execute();
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
                ArrayList<com.google.api.services.drive.model.File> fileList = (ArrayList<com.google.api.services.drive.model.File>) files.getItems();
                for (com.google.api.services.drive.model.File f : fileList) {
                    String fileTitle = f.getTitle();

                    if (fileTitle.endsWith(Constants.FILE_NAME_GROUP) && fileTitle.contains(title)) {
                        try {
                            m_client.files().delete(f.getId()).execute();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                request.setPageToken(files.getNextPageToken());
            } while (request.getPageToken() != null && request.getPageToken().length() >= 0);
        }
    }

    public void deleteBirth (String title){
        if (isLinked()) {
            prefs = new SharedPrefs(ctx);
            authorize();
            Drive.Files.List request = null;
            try {
                request = m_client.files().list().setQ("mimeType = 'text/plain'");
            } catch (IOException e) {
                e.printStackTrace();
            }
            do {
                FileList files;
                try {
                    files = request.execute();
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
                ArrayList<com.google.api.services.drive.model.File> fileList = (ArrayList<com.google.api.services.drive.model.File>) files.getItems();
                for (com.google.api.services.drive.model.File f : fileList) {
                    String fileTitle = f.getTitle();

                    if (fileTitle.endsWith(Constants.FILE_NAME_BIRTHDAY) && fileTitle.contains(title)) {
                        try {
                            m_client.files().delete(f.getId()).execute();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                request.setPageToken(files.getNextPageToken());
            } while (request.getPageToken() != null && request.getPageToken().length() >= 0);
        }
    }

    public void clean(){
        if (isLinked()) {
            prefs = new SharedPrefs(ctx);
            authorize();

            Drive.Files.List requestF = null;
            try {
                requestF = m_client.files().list().setQ("mimeType = 'application/vnd.google-apps.folder'");
            } catch (IOException e) {
                e.printStackTrace();
            }
            do {
                FileList files;
                try {
                    files = requestF.execute();
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
                ArrayList<com.google.api.services.drive.model.File> fileList = (ArrayList<com.google.api.services.drive.model.File>) files.getItems();
                for (com.google.api.services.drive.model.File f : fileList) {
                    String fileMIME = f.getMimeType();

                    if (fileMIME.matches("application/vnd.google-apps.folder") && f.getTitle().matches("Just Reminder")) {
                        try {
                            m_client.files().delete(f.getId()).execute();
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

    private String getFolderId(){
        String id = null;
        Drive.Files.List request = null;
        try {
            request = m_client.files().list().setQ("mimeType = 'application/vnd.google-apps.folder'");
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
                            (ArrayList<com.google.api.services.drive.model.File>) files.getItems();
                    for (com.google.api.services.drive.model.File f : fileList) {
                        String fileMIME = f.getMimeType();

                        if (fileMIME.trim().matches("application/vnd.google-apps.folder") &&
                                f.getTitle().contains("Just Reminder")) {
                            id = f.getId();
                        }
                    }
                    request.setPageToken(files.getNextPageToken());
                } else id = null;
            } while (request.getPageToken() != null && request.getPageToken().length() >= 0);
        } else id = null;
        return id;
    }

    private com.google.api.services.drive.model.File createFolder() throws IOException {
        com.google.api.services.drive.model.File folder = new com.google.api.services.drive.model.File();
        folder.setTitle("Just Reminder");
        folder.setMimeType("application/vnd.google-apps.folder");
        com.google.api.services.drive.Drive.Files.Insert folderInsert = null;
        try {
            folderInsert = m_client.files().insert(folder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return folderInsert.execute();
    }
}
