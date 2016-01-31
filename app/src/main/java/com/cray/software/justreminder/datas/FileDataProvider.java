package com.cray.software.justreminder.datas;

import android.content.Context;

import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.datas.models.FileModel;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.utils.MemoryUtil;
import com.cray.software.justreminder.utils.TimeUtil;

import java.io.File;
import java.util.ArrayList;

public class FileDataProvider {
    private ArrayList<FileModel> data;
    private Context mContext;
    private String where;

    public FileDataProvider(Context mContext, String where){
        data = new ArrayList<>();
        this.mContext = mContext;
        this.where = where;
        load();
    }

    public String getWhere() {
        return where;
    }

    public ArrayList<FileModel> getData(){
        return data;
    }

    public int getCount(){
        return data != null ? data.size() : 0;
    }

    public int getPosition(FileModel item){
        int res = -1;
        if (data.size() > 0) {
            for (int i = 0; i < data.size(); i++){
                FileModel item1 = data.get(i);
                if (item.getFileName().matches(item1.getFileName())) {
                    res = i;
                    break;
                }
            }
        }
        return res;
    }

    public FileModel getItem(int index) {
        if (index < 0 || index >= getCount()) {
            return null;
        }

        return data.get(index);
    }

    public void load() {
        data.clear();
        boolean is24 = new SharedPrefs(mContext).loadBoolean(Prefs.IS_24_TIME_FORMAT);
        if (SyncHelper.isSdPresent()){
            if (where.matches(Constants.DIR_SD)) {
                File dir = MemoryUtil.getRDir();
                if (dir != null && dir.exists()) {
                    File[] files = dir.listFiles();
                    for (File file : files) {
                        data.add(new FileModel(file.getName(),
                                TimeUtil.getFullDateTime(file.lastModified(), is24), file.getPath()));
                    }
                }
                dir = MemoryUtil.getNDir();
                if (dir != null && dir.exists()) {
                    File[] files = dir.listFiles();
                    for (File file : files) {
                        data.add(new FileModel(file.getName(),
                                TimeUtil.getFullDateTime(file.lastModified(), is24), file.getPath()));
                    }
                }
                dir = MemoryUtil.getBDir();
                if (dir != null && dir.exists()) {
                    File[] files = dir.listFiles();
                    for (File file : files) {
                        data.add(new FileModel(file.getName(),
                                TimeUtil.getFullDateTime(file.lastModified(), is24), file.getPath()));
                    }
                }
                dir = MemoryUtil.getGroupsDir();
                if (dir != null && dir.exists()) {
                    File[] files = dir.listFiles();
                    for (File file : files) {
                        data.add(new FileModel(file.getName(),
                                TimeUtil.getFullDateTime(file.lastModified(), is24), file.getPath()));
                    }
                }
            } else if (where.matches(Constants.DIR_SD_DBX_TMP)) {
                File dir = MemoryUtil.getDRDir();
                if (dir != null && dir.exists()) {
                    File[] files = dir.listFiles();
                    for (File file : files) {
                        data.add(new FileModel(file.getName(),
                                TimeUtil.getFullDateTime(file.lastModified(), is24), file.getPath()));
                    }
                }
                dir = MemoryUtil.getDNDir();
                if (dir != null && dir.exists()) {
                    File[] files = dir.listFiles();
                    for (File file : files) {
                        data.add(new FileModel(file.getName(),
                                TimeUtil.getFullDateTime(file.lastModified(), is24), file.getPath()));
                    }
                }
                dir = MemoryUtil.getDBDir();
                if (dir != null && dir.exists()) {
                    File[] files = dir.listFiles();
                    for (File file : files) {
                        data.add(new FileModel(file.getName(),
                                TimeUtil.getFullDateTime(file.lastModified(), is24), file.getPath()));
                    }
                }
                dir = MemoryUtil.getDGroupsDir();
                if (dir != null && dir.exists()) {
                    File[] files = dir.listFiles();
                    for (File file : files) {
                        data.add(new FileModel(file.getName(),
                                TimeUtil.getFullDateTime(file.lastModified(), is24), file.getPath()));
                    }
                }
            } else if (where.matches(Constants.DIR_SD_GDRIVE_TMP)) {
                File dir = MemoryUtil.getGRDir();
                if (dir != null && dir.exists()) {
                    File[] files = dir.listFiles();
                    for (File file : files) {
                        data.add(new FileModel(file.getName(),
                                TimeUtil.getFullDateTime(file.lastModified(), is24), file.getPath()));
                    }
                }
                dir = MemoryUtil.getGNDir();
                if (dir != null && dir.exists()) {
                    File[] files = dir.listFiles();
                    for (File file : files) {
                        data.add(new FileModel(file.getName(),
                                TimeUtil.getFullDateTime(file.lastModified(), is24), file.getPath()));
                    }
                }
                dir = MemoryUtil.getGBDir();
                if (dir != null && dir.exists()) {
                    File[] files = dir.listFiles();
                    for (File file : files) {
                        data.add(new FileModel(file.getName(),
                                TimeUtil.getFullDateTime(file.lastModified(), is24), file.getPath()));
                    }
                }
                dir = MemoryUtil.getGGroupsDir();
                if (dir != null && dir.exists()) {
                    File[] files = dir.listFiles();
                    for (File file : files) {
                        data.add(new FileModel(file.getName(),
                                TimeUtil.getFullDateTime(file.lastModified(), is24), file.getPath()));
                    }
                }
            }
        }
    }
}
