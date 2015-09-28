package com.cray.software.justreminder.cloud;

import android.content.Context;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.SyncHelper;

public class BoxHelper
        //implements BoxAuthentication.AuthListener
{

    Context mContext;
    //BoxSession session;

    static final String USER = "user_bbb";
    static final String FOLDER = "Just_Reminder";
    private boolean isAuth = false;

    public BoxHelper (Context context){
        this.mContext = context;
        isAuth = false;
        /*BoxConfig.CLIENT_ID = "5hu6faodwsrjndvddvsfxf8r5kujhtkp";
        BoxConfig.CLIENT_SECRET = "MPQBkQcytLGEcZirThiR4LLab0VPevzl";
        BoxConfig.REDIRECT_URL = "https://craysoftware.wordpress.com/";*/
        if (isLogged()) authWithId();
    }

    public void auth(){
        /*session = new BoxSession(mContext);
        session.setSessionAuthListener(this);
        session.authenticate();*/
    }

    public void authMultiple(){
        /*session = new BoxSession(mContext, null);
        session.setSessionAuthListener(this);
        session.authenticate();*/
    }

    public void authWithId(){
        /*String user = new SharedPrefs(mContext).loadPrefs(USER);
        if (user != null) user = new SyncHelper(mContext).decrypt(user);
        session = new BoxSession(mContext, user);
        session.setSessionAuthListener(this);
        session.authenticate();*/
    }

    /*private void saveCredentials(BoxAuthentication.BoxAuthenticationInfo info){
        if (session != null){
            SharedPrefs prefs = new SharedPrefs(mContext);
            String userID = info.getUser().getId();
            Log.d(Constants.LOG_TAG, "User is " + userID);
            prefs.savePrefs(USER, new SyncHelper(mContext).encrypt(userID));
        }
    }*/

    public boolean isLogged(){
        String user = new SharedPrefs(mContext).loadPrefs(USER);
        if (user != null) user = new SyncHelper(mContext).decrypt(user);
        return user != null && !user.matches("");
    }

    public void disconnect(){
        //if (session != null) session.logout();
    }

    public void logout(){
        SharedPrefs prefs = new SharedPrefs(mContext);
        prefs.savePrefs(USER, null);
        disconnect();
        new Thread(new Runnable() {
            @Override
            public void run() {
                //BoxAuthentication.getInstance().logoutAllUsers(mContext);
            }
        });
    }

    public void uploadReminder() {
        //upload(Constants.DIR_SD);
    }

    public void uploadNote() {
        //upload(Constants.DIR_NOTES_SD);
    }

    public void uploadGroup() {
        //upload(Constants.DIR_GROUP_SD);
    }

    public void uploadBirthday() {
        //upload(Constants.DIR_BIRTHDAY_SD);
    }

    /*private void upload(String path){
        if (isLogged()) {
            if (!isAuth) authWithId();
            Log.d(Constants.LOG_TAG, "Auth complete");
            File sdPath = Environment.getExternalStorageDirectory();
            File sdPathDr = new File(sdPath.toString() + "/JustReminder/" + path);
            File[] files = sdPathDr.listFiles();
            if (files != null) {
                Log.d(Constants.LOG_TAG, "Found files " + files.length);
                for (File file : files) {
                    String fileName = file.getName();
                    deleteReminder(getCoreName(fileName));
                    Log.d(Constants.LOG_TAG, "Send file " + fileName);
                    BoxApiFile fileApi = new BoxApiFile(session);
                    BoxFile uploadedFile = null;
                    try {
                        uploadedFile = fileApi.getUploadRequest(file, getFolderId())
                                .setFileName(fileName)
                                .send();
                    } catch (BoxException e) {
                        e.printStackTrace();
                    }
                    Log.d(Constants.LOG_TAG, "File sent " +
                            (uploadedFile != null ? uploadedFile.getId() : ""));

                }
            }
        }
    }

    public void deleteReminder(String name){
        try {
            delete(name, FileConfig.FILE_NAME_REMINDER);
        } catch (BoxException e) {
            e.printStackTrace();
        }
    }

    public void deleteNoteFile(String name){
        try {
            delete(name, FileConfig.FILE_NAME_NOTE);
        } catch (BoxException e) {
            e.printStackTrace();
        }
    }

    public void deleteGroupFile(String name){
        try {
            delete(name, FileConfig.FILE_NAME_GROUP);
        } catch (BoxException e) {
            e.printStackTrace();
        }
    }

    public void deleteBirthFile(String name){
        try {
            delete(name, FileConfig.FILE_NAME_BIRTHDAY);
        } catch (BoxException e) {
            e.printStackTrace();
        }
    }

    private void delete(String name, String ext) throws BoxException {
        if (isLogged()){
            if (!isAuth) authWithId();
            Log.d(Constants.LOG_TAG, "Delete name " + name);
            BoxApiSearch searchApi = new BoxApiSearch(session);
            BoxList searchResults = searchApi.getSearchRequest(name)
                    .setOffset(0)   // default is 0
                    .setLimit(100) // default is 30, max is 200
                            // Optional: Specify advanced search parameters. See BoxRequestsSearch.Search for the full list of parameters supported.
                    .limitAncestorFolderIds(new String[]{getFolderId()}) // only items in these folders will be returned.
                    .limitFileExtensions(new String[]{ext}) // only files with these extensions will be returned.
                    .send();
            if (searchResults != null) {
                Log.d(Constants.LOG_TAG, "Delete found " + searchResults.toJson());
                for (int i = 0; i < searchResults.size(); i++) {
                    BoxJsonObject item = searchResults.get(i);
                    BoxItem boxItem = BoxItem.createBoxItemFromJson(item.toJson());
                    if (boxItem != null && boxItem.getName().endsWith(ext)) {
                        BoxApiFile fileApi = new BoxApiFile(session);
                        fileApi.getDeleteRequest(boxItem.getId()).send();
                    }
                }
            }
        }
    }

    public void clear(){
        if (isLogged()){
            if (!isAuth) authWithId();
            while (true){
                if (isAuth && getFolderId() != null) {
                    BoxApiFolder folderApi = new BoxApiFolder(session);
                    try {
                        folderApi.getDeleteRequest(getFolderId())
                                .setRecursive(true)
                                .send();
                    } catch (BoxException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }
    }

    public void downloadReminder(){
        try {
            download(Constants.DIR_SD_BOX_TMP, FileConfig.FILE_NAME_REMINDER);
        } catch (BoxException e) {
            e.printStackTrace();
        }
    }

    public void downloadNote(){
        try {
            download(Constants.DIR_NOTES_SD_BOX_TMP, FileConfig.FILE_NAME_NOTE);
        } catch (BoxException e) {
            e.printStackTrace();
        }
    }

    public void downloadGroup(){
        try {
            download(Constants.DIR_GROUP_SD_BOX_TMP, FileConfig.FILE_NAME_GROUP);
        } catch (BoxException e) {
            e.printStackTrace();
        }
    }

    public void downloadBirthday(){
        try {
            download(Constants.DIR_BIRTHDAY_SD_BOX_TMP, FileConfig.FILE_NAME_BIRTHDAY);
        } catch (BoxException e) {
            e.printStackTrace();
        }
    }

    public void download(String path, String ext) throws BoxException {
        if (isLogged()){
            authWithId();
            while (true){
                if (isAuth && getFolderId() != null) {
                    BoxApiFolder folderApi = new BoxApiFolder(session);
                    BoxListItems items = folderApi.getItemsRequest(getFolderId()).send();
                    if (items != null){
                        for (BoxItem item : items){
                            String fileName = item.getName();
                            Log.d(Constants.LOG_TAG, fileName);
                            File sdPath = Environment.getExternalStorageDirectory();
                            File file = new File(sdPath.toString() + "/JustReminder/" + path);
                            if (!file.exists()) {
                                file.mkdirs();
                            }
                            if (fileName.endsWith(ext)) {
                                File localFile = new File(file + "/" + fileName);
                                if (!localFile.exists()) {
                                    try {
                                        localFile.createNewFile();
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
                                BoxApiFile fileApi = new BoxApiFile(session);
                                fileApi.getDownloadRequest(outputStream, item.getId())
                                        .send();
                                //restore tmp files after downloading
                                try {
                                    SyncHelper helper = new SyncHelper(mContext);
                                    if (ext.matches(FileConfig.FILE_NAME_REMINDER)){
                                        helper.reminderFromJson(localFile.toString(), fileName);
                                    }
                                    if (ext.matches(FileConfig.FILE_NAME_GROUP)){
                                        helper.groupFromJson(localFile.toString(), fileName);
                                    }
                                    if (ext.matches(FileConfig.FILE_NAME_NOTE)){
                                        helper.noteFromJson(localFile.toString(), fileName);
                                    }
                                    if (ext.matches(FileConfig.FILE_NAME_BIRTHDAY)){
                                        helper.birthdayFromJson(localFile.toString(), fileName);
                                    }
                                } catch (IOException | JSONException e1) {
                                    e1.printStackTrace();
                                }
                            }
                        }
                    }
                    break;
                }
            }
        }
    }

    public String createFolder(){
        String id = getFolderId();
        if (id == null){
            BoxApiFolder folderApi = new BoxApiFolder(session);
            try {
                BoxFolder newFolder = folderApi.getCreateRequest("0", FOLDER).send();
                id = newFolder.getId();
            } catch (BoxException e) {
                e.printStackTrace();
            }
        }
        return id;
    }

    public String getFolderId(){
        BoxApiSearch searchApi = new BoxApiSearch(session);
        Log.d(Constants.LOG_TAG, "search started ");
        String id = null;
        try {
            BoxList searchResults = searchApi.getSearchRequest("Reminder")
                    .setOffset(0)
                    .setLimit(100)
                    .send();
            if (searchResults != null && searchResults.size() > 0){
                Log.d(Constants.LOG_TAG, "search size " + searchResults.size());
                Log.d(Constants.LOG_TAG, "searches " + searchResults.toJson());
                for (int i = 0; i < searchResults.size(); i++){
                    BoxJsonObject item = searchResults.get(i);
                    BoxItem boxItem = BoxItem.createBoxItemFromJson(item.toJson());
                    if (boxItem != null && boxItem.getName().contains(FOLDER)){
                        id = boxItem.getId();
                    }
                }
            }
        } catch (BoxException e) {
            e.printStackTrace();
        }
        Log.d(Constants.LOG_TAG, "folder id " + id);
        return id;
    }

    public static String getCoreName(String name){
        int pos = name.lastIndexOf(".");
        if (pos > 0) {
            name = name.substring(0, pos);
        }
        return name;
    }

    @Override
    public void onRefreshed(BoxAuthentication.BoxAuthenticationInfo info) {
        isAuth = false;
        if (isLogged())authWithId();
    }

    @Override
    public void onAuthCreated(BoxAuthentication.BoxAuthenticationInfo info) {
        isAuth = true;
        if (!isLogged()) saveCredentials(info);
        new Thread(new Runnable() {
            @Override
            public void run() {
                createFolder();
            }
        });
        Log.d(Constants.LOG_TAG, "Auth created");
    }

    @Override
    public void onAuthFailure(BoxAuthentication.BoxAuthenticationInfo info, Exception ex) {
        isAuth = false;
    }

    @Override
    public void onLoggedOut(BoxAuthentication.BoxAuthenticationInfo info, Exception ex) {
        isAuth = false;
    }*/
}
