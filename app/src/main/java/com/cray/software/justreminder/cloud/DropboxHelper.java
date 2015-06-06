package com.cray.software.justreminder.cloud;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.interfaces.Constants;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.android.AuthActivity;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.exception.DropboxUnlinkedException;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;

import org.json.JSONException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class DropboxHelper {

    Context ctx;
    String dbxFolder = "JustReminder/";
    String dbxNoteFolder = "Notes/";
    String dbxGroupFolder = "Groups/";
    private DropboxAPI<AndroidAuthSession> mDBApi;
    DropboxAPI.DropboxFileInfo info;
    DropboxAPI.Entry newEntry;

    public static final String APP_KEY = "4zi1d414h0v8sxe";
    public static final String APP_SECRET = "aopehxo80oq8g5o";
    final static private String ACCOUNT_PREFS_NAME = "prefs";
    final static private String ACCESS_KEY_NAME = "ACCESS_KEY";
    final static private String ACCESS_SECRET_NAME = "ACCESS_SECRET";

    private static final boolean USE_OAUTH1 = false;

    public DropboxHelper(Context context){
        this.ctx = context;
    }

    public void startSession(){
        AndroidAuthSession session = buildSession();
        mDBApi = new DropboxAPI<AndroidAuthSession>(session);
        checkAppKeySetup();
    }

    public boolean isLinked() {
        return mDBApi != null && mDBApi.getSession().isLinked();
    }

    public String userName(){
        DropboxAPI.Account account = null;
        try {
            account = mDBApi.accountInfo();
        } catch (DropboxException e) {
            e.printStackTrace();
        }
        return account != null ? account.displayName : null;
    }

    public long userQuota(){
        DropboxAPI.Account account = null;
        try {
            account = mDBApi.accountInfo();
        } catch (DropboxException e) {
            e.printStackTrace();
        }
        return account != null ? account.quota : 0;
    }

    public long userQuotaNormal(){
        DropboxAPI.Account account = null;
        try {
            account = mDBApi.accountInfo();
        } catch (DropboxException e) {
            e.printStackTrace();
        }
        return account != null ? account.quotaNormal : 0;
    }

    public long userQuotaShared(){
        DropboxAPI.Account account = null;
        try {
            account = mDBApi.accountInfo();
        } catch (DropboxException e) {
            e.printStackTrace();
        }
        return account != null ? account.quotaShared : 0;
    }

    public boolean checkLink(){
        boolean isLogged = false;
        startSession();
        AndroidAuthSession session = mDBApi.getSession();
        if (session.authenticationSuccessful()) {
            try {
                session.finishAuthentication();
                storeAuth(session);
                isLogged = true;

            } catch (IllegalStateException e) {
                showToast("Couldn't authenticate with Dropbox:" + e.getLocalizedMessage());
                //Log.d(Constants.LOG_TAG, "Error authenticating", e);
            }
        }
        return isLogged;
    }

    public boolean startLink(){
        boolean isLinkSuccessful;
        if (USE_OAUTH1) {
            mDBApi.getSession().startAuthentication(ctx);
            isLinkSuccessful = mDBApi.getSession().isLinked();
        } else {
            mDBApi.getSession().startOAuth2Authentication(ctx);
            isLinkSuccessful = mDBApi.getSession().isLinked();
        }
        return isLinkSuccessful;
    }

    public boolean unlink(){
        boolean is = false;
        if (logOut()) {
            is = true;
        }
        return is;
    }

    public void checkAppKeySetup() {
        if (APP_KEY.startsWith("CHANGE") ||
                APP_SECRET.startsWith("CHANGE")) {
            showToast("You must apply for an app key and secret from developers.dropbox.com, and add them to the DBRoulette ap before trying it.");
            ((Activity)ctx).finish();
            return;
        }
        Intent testIntent = new Intent(Intent.ACTION_VIEW);
        String scheme = "db-" + APP_KEY;
        String uri = scheme + "://" + AuthActivity.AUTH_VERSION + "/test";
        testIntent.setData(Uri.parse(uri));
        PackageManager pm = ctx.getPackageManager();
        if (0 == pm.queryIntentActivities(testIntent, 0).size()) {
            showToast("URL scheme in your app's " +
                    "manifest is not set up correctly. You should have a " +
                    "com.dropbox.client2.android.AuthActivity with the " +
                    "scheme: " + scheme);
            ((Activity)ctx).finish();
        }
    }

    public void showToast(String msg) {
        Toast toast = Toast.makeText(ctx, msg, Toast.LENGTH_LONG);
        toast.show();
    }

    public void loadAuth(AndroidAuthSession session) {
        SharedPreferences prefs = ctx.getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        String key = prefs.getString(ACCESS_KEY_NAME, null);
        String secret = prefs.getString(ACCESS_SECRET_NAME, null);
        if (key == null || secret == null || key.length() == 0 || secret.length() == 0) return;

        if (key.equals("oauth2:")) {
            session.setOAuth2AccessToken(secret);
        } else {
            session.setAccessTokenPair(new AccessTokenPair(key, secret));
        }
    }

    public void storeAuth(AndroidAuthSession session) {
        String oauth2AccessToken = session.getOAuth2AccessToken();
        if (oauth2AccessToken != null) {
            SharedPreferences prefs = ctx.getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
            SharedPreferences.Editor edit = prefs.edit();
            edit.putString(ACCESS_KEY_NAME, "oauth2:");
            edit.putString(ACCESS_SECRET_NAME, oauth2AccessToken);
            edit.commit();
            return;
        }
        AccessTokenPair oauth1AccessToken = session.getAccessTokenPair();
        if (oauth1AccessToken != null) {
            SharedPreferences prefs = ctx.getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
            SharedPreferences.Editor edit = prefs.edit();
            edit.putString(ACCESS_KEY_NAME, oauth1AccessToken.key);
            edit.putString(ACCESS_SECRET_NAME, oauth1AccessToken.secret);
            edit.commit();
        }
    }

    public AndroidAuthSession buildSession() {
        AppKeyPair appKeyPair = new AppKeyPair(APP_KEY, APP_SECRET);

        AndroidAuthSession session = new AndroidAuthSession(appKeyPair);
        loadAuth(session);
        return session;
    }

    private boolean logOut() {
        mDBApi.getSession().unlink();
        clearKeys();
        return true;
    }

    private void clearKeys() {
        SharedPreferences prefs = ctx.getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        SharedPreferences.Editor edit = prefs.edit();
        edit.clear();
        edit.commit();
    }

    public void uploadToCloud(final String fileName) {
        startSession();
        if (isLinked()) {
            if (fileName != null) {
                File sdPath = Environment.getExternalStorageDirectory();
                File sdPathDr = new File(sdPath.toString() + "/JustReminder/" + Constants.DIR_SD);
                String fileLoc = sdPathDr.toString();
                File tmpFile = new File(fileLoc, fileName);
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(tmpFile);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                try {
                    newEntry = mDBApi.putFileOverwrite(dbxFolder + fileName, fis, tmpFile.length(), null);
                } catch (DropboxUnlinkedException e) {
                    Log.e("DbLog", "User has unlinked.");
                } catch (DropboxException e) {
                    Log.e("DbLog", "Something went wrong while uploading.");
                }
            } else {
                File sdPath = Environment.getExternalStorageDirectory();
                File sdPathDr = new File(sdPath.toString() + "/JustReminder/" + Constants.DIR_SD);
                File[] files = sdPathDr.listFiles();
                String fileLoc = sdPathDr.toString();
                if (files != null) {
                    for (File file : files) {
                        String fileLoopName = file.getName();
                        File tmpFile = new File(fileLoc, fileLoopName);
                        FileInputStream fis = null;
                        try {
                            fis = new FileInputStream(tmpFile);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        try {
                            newEntry = mDBApi.putFileOverwrite(dbxFolder + fileLoopName, fis, tmpFile.length(), null);
                        } catch (DropboxUnlinkedException e) {
                            Log.e("DbLog", "User has unlinked.");
                        } catch (DropboxException e) {
                            Log.e("DbLog", "Something went wrong while uploading.");
                        }
                    }
                }
            }
        }
    }

    public void uploadNoteToCloud() {
        startSession();
        if (isLinked()) {
            File sdPath = Environment.getExternalStorageDirectory();
            File sdPathDr = new File(sdPath.toString() + "/JustReminder/" + Constants.DIR_NOTES_SD);
            File[] files = sdPathDr.listFiles();
            String fileLoc = sdPathDr.toString();
            if (files != null) {
                for (File file : files) {
                    String fileLoopName = file.getName();
                    File tmpFile = new File(fileLoc, fileLoopName);
                    FileInputStream fis = null;
                    try {
                        fis = new FileInputStream(tmpFile);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    try {
                        newEntry = mDBApi.putFileOverwrite(dbxNoteFolder + fileLoopName, fis, tmpFile.length(), null);
                    } catch (DropboxUnlinkedException e) {
                        Log.e("DbLog", "User has unlinked.");
                    } catch (DropboxException e) {
                        Log.e("DbLog", "Something went wrong while uploading.");
                    }
                }
            }
        }
    }

    public void uploadGroupToCloud() {
        startSession();
        if (isLinked()) {
            File sdPath = Environment.getExternalStorageDirectory();
            File sdPathDr = new File(sdPath.toString() + "/JustReminder/" + Constants.DIR_GROUP_SD);
            File[] files = sdPathDr.listFiles();
            String fileLoc = sdPathDr.toString();
            if (files != null) {
                for (File file : files) {
                    String fileLoopName = file.getName();
                    File tmpFile = new File(fileLoc, fileLoopName);
                    FileInputStream fis = null;
                    try {
                        fis = new FileInputStream(tmpFile);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    try {
                        newEntry = mDBApi.putFileOverwrite(dbxGroupFolder + fileLoopName, fis, tmpFile.length(), null);
                    } catch (DropboxUnlinkedException e) {
                        Log.e("DbLog", "User has unlinked.");
                    } catch (DropboxException e) {
                        Log.e("DbLog", "Something went wrong while uploading.");
                    }
                }
            }
        }
    }

    public void deleteFile(String name){
        try {
            mDBApi.delete(dbxFolder + name + ".json");
        } catch (DropboxException e) {
            e.printStackTrace();
        }
    }

    public void deleteNote(String name){
        startSession();
        if (isLinked()) {
            try {
                mDBApi.delete(dbxNoteFolder + name + ".note");
            } catch (DropboxException e) {
                e.printStackTrace();
            }
        }
    }

    public void deleteGroup(String name){
        startSession();
        if (isLinked()) {
            try {
                mDBApi.delete(dbxGroupFolder + name + ".rgroup");
            } catch (DropboxException e) {
                e.printStackTrace();
            }
        }
    }

    public void cleanFolder(){
        startSession();
        if (isLinked()) {
            try {
                mDBApi.delete(dbxNoteFolder);
            } catch (DropboxException e) {
                e.printStackTrace();
            }
            try {
                mDBApi.delete(dbxGroupFolder);
            } catch (DropboxException e) {
                e.printStackTrace();
            }
            try {
                mDBApi.delete(dbxFolder);
            } catch (DropboxException e) {
                e.printStackTrace();
            }
        }
    }

    public void downloadFromCloud() {
        startSession();
        if (isLinked()) {
            try {
                newEntry = mDBApi.metadata("/" + dbxFolder, 1000, null, true, null);
            } catch (DropboxException e) {
                e.printStackTrace();
            }
            if (newEntry != null) {
                for (DropboxAPI.Entry e : newEntry.contents) {
                    if (!e.isDeleted) {
                        String fileName = e.fileName();
                        File sdPath = Environment.getExternalStorageDirectory();
                        File file = new File(sdPath.toString() + "/JustReminder/" + Constants.DIR_SD_DBX_TMP);
                        if (!file.exists()) {
                            file.mkdirs();
                        }
                        File localFile = new File(file + "/" + fileName);
                        if (!localFile.exists()) {
                            try {
                                localFile.createNewFile(); //otherwise dropbox client will fail silently
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                        }
                        FileOutputStream outputStream = null;
                        try {
                            outputStream = new FileOutputStream(localFile);
                        } catch (FileNotFoundException e1) {
                            e1.printStackTrace();
                        }
                        try {
                            info = mDBApi.getFile("/" + dbxFolder + fileName, null, outputStream, null);
                        } catch (DropboxException e1) {
                            e1.printStackTrace();
                        }
                        //restore tmp files after downloading
                        try {
                            new SyncHelper(ctx).importReminderFromJSON(localFile.toString(), fileName);
                        } catch (IOException | JSONException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    public void downloadNoteFromCloud() {
        startSession();
        if (isLinked()) {
            try {
                newEntry = mDBApi.metadata("/" + dbxNoteFolder, 1000, null, true, null);
            } catch (DropboxException e) {
                e.printStackTrace();
            }
            if (newEntry != null) {
                for (DropboxAPI.Entry e : newEntry.contents) {
                    if (!e.isDeleted) {
                        String fileName = e.fileName();
                        File sdPath = Environment.getExternalStorageDirectory();
                        File file = new File(sdPath.toString() + "/JustReminder/" + Constants.DIR_NOTES_SD_DBX_TMP);
                        if (!file.exists()) {
                            file.mkdirs();
                        }
                        File localFile = new File(file + "/" + fileName);
                        if (!localFile.exists()) {
                            try {
                                localFile.createNewFile(); //otherwise dropbox client will fail silently
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                        }
                        FileOutputStream outputStream = null;
                        try {
                            outputStream = new FileOutputStream(localFile);
                        } catch (FileNotFoundException e1) {
                            e1.printStackTrace();
                        }
                        try {
                            info = mDBApi.getFile("/" + dbxNoteFolder + fileName, null, outputStream, null);
                        } catch (DropboxException e1) {
                            e1.printStackTrace();
                        }
                        //restore tmp files after downloading
                        try {
                            new SyncHelper(ctx).importNotes(localFile.toString(), fileName);
                        } catch (IOException | JSONException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    public void downloadGroupFromCloud() {
        startSession();
        if (isLinked()) {
            try {
                newEntry = mDBApi.metadata("/" + dbxGroupFolder, 1000, null, true, null);
            } catch (DropboxException e) {
                e.printStackTrace();
            }
            if (newEntry != null) {
                for (DropboxAPI.Entry e : newEntry.contents) {
                    if (!e.isDeleted) {
                        String fileName = e.fileName();
                        File sdPath = Environment.getExternalStorageDirectory();
                        File file = new File(sdPath.toString() + "/JustReminder/" + Constants.DIR_GROUP_SD_DBX_TMP);
                        if (!file.exists()) {
                            file.mkdirs();
                        }
                        File localFile = new File(file + "/" + fileName);
                        if (!localFile.exists()) {
                            try {
                                localFile.createNewFile(); //otherwise dropbox client will fail silently
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                        }
                        FileOutputStream outputStream = null;
                        try {
                            outputStream = new FileOutputStream(localFile);
                        } catch (FileNotFoundException e1) {
                            e1.printStackTrace();
                        }
                        try {
                            info = mDBApi.getFile("/" + dbxGroupFolder + fileName, null, outputStream, null);
                        } catch (DropboxException e1) {
                            e1.printStackTrace();
                        }
                        //restore tmp files after downloading
                        try {
                            new SyncHelper(ctx).importGroup(localFile.toString(), fileName);
                        } catch (IOException | JSONException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    public int countFiles() {
        int res = 0;
        startSession();
        if (isLinked()) {
            try {
                newEntry = mDBApi.metadata("/" + dbxFolder, 1000, null, true, null);
            } catch (DropboxException e) {
                e.printStackTrace();
            }
            if (newEntry != null) {
                for (DropboxAPI.Entry e : newEntry.contents) {
                    if (!e.isDeleted) {
                        String fileName = e.fileName();
                        if (fileName.endsWith(Constants.FILE_NAME)) {
                            res += 1;
                        }
                    }
                }
            }
        }
        return res;
    }
}
