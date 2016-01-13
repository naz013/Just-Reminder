package com.cray.software.justreminder.datas;

import android.content.Context;
import android.os.Environment;

import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.datas.models.FileModel;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.SyncHelper;
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
            File sdPath = Environment.getExternalStorageDirectory();
            File sdPathDr = new File(sdPath.toString() + "/JustReminder/" + where);
            if (sdPathDr.exists()) {
                File[] files = sdPathDr.listFiles();
                for (File file : files) {
                    data.add(new FileModel(file.getName(),
                            TimeUtil.getFullDateTime(file.lastModified(), is24), file.getPath()));
                }
            }
        }
    }
}
