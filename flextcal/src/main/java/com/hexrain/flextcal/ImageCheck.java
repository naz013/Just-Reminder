package com.hexrain.flextcal;

import android.content.Context;
import android.os.Environment;

import java.io.File;

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
 */
public class ImageCheck {

    private Context context;

    public ImageCheck(Context context){
        this.context = context;
    }

    public String getImage(int month){
        if (isSdPresent()){
            String res = null;
            File sdPath = Environment.getExternalStorageDirectory();
            File sdPathDr = new File(sdPath.toString() + "/JustReminder/" + "image_cache");
            if (!sdPathDr.exists()) {
                sdPathDr.mkdirs();
            }

            File image = new File(sdPathDr, getImageName(month));
            if (image.exists()) res = image.getAbsolutePath();
            return res;
        } else return null;
    }

    public boolean isImage(int month){
        if (isSdPresent()){
            boolean res = false;
            File sdPath = Environment.getExternalStorageDirectory();
            File sdPathDr = new File(sdPath.toString() + "/JustReminder/" + "image_cache");
            if (!sdPathDr.exists()) {
                sdPathDr.mkdirs();
            }

            File image = new File(sdPathDr, getImageName(month));
            if (image.exists()) res = true;
            return res;
        } else return false;
    }

    public String getImageUrl(int month){
        String name = null;
        switch (month){
            case 1:
                name = "https://images.unsplash.com/photo-1429734160945-4f85244d6a5a?q=80&fm=jpg&s=1e4502a7439b5792e5b7b0a25811baba";
                break;
            case 2:
                name = "https://images.unsplash.com/photo-1437240443155-612416af4d5a?q=80&fm=jpg&s=27a027e3635b2be110ed728c89ca47b3";
                break;
            case 3:
                name = "https://images.unsplash.com/photo-1438216983993-cdcd7dea84ce?q=80&fm=jpg&s=4141165d8e8eed56b8d9012740c4b503";
                break;
            case 4:
                name = "https://images.unsplash.com/photo-1438109491414-7198515b166b?q=80&fm=jpg&s=cbdabf7a79c087a0b060670a6d79726c";
                break;
            case 5:
                name = "https://images.unsplash.com/photo-1436915947297-3a94186c8133?q=80&fm=jpg&s=5c943a0880b97c3c26d2fe7ae218bb5c";
                break;
            case 6:
                name = "https://images.unsplash.com/photo-1431578500526-4d9613015464?q=80&fm=jpg&s=169b4f4e6f3882a03b6b93b2e6848052";
                break;
            case 7:
                name = "https://images.unsplash.com/reserve/Af0sF2OS5S5gatqrKzVP_Silhoutte.jpg?q=80&fm=jpg&s=aa0399d0a07be6afd5470a6dd6092bb3";
                break;
            case 8:
                name = "https://images.unsplash.com/photo-1434394673726-e8232a5903b4?q=80&fm=jpg&s=b154bdf22a4885c8e2dd1b845c5fe996";
                break;
            case 9:
                name = "https://images.unsplash.com/photo-1433424007598-bd5d102e8597?q=80&fm=jpg&s=57558b291a1e7721800eff1702e5d711";
                break;
            case 10:
                name = "https://images.unsplash.com/photo-1433840496881-cbd845929862?q=80&fm=jpg&s=c9a1a21dbf8a9d16477ea4b54afc8a48";
                break;
            case 11:
                name = "https://images.unsplash.com/photo-1432234525151-015bc7937f1c?q=80&fm=jpg&s=a60a1858221f67fcb136f071f28e16d4";
                break;
            case 12:
                name = "https://images.unsplash.com/photo-1438368915865-a852ef86fc42?q=80&fm=jpg&s=15e4744077e36852ba57f46f4660dc7a";
                break;
            case 13:
                name = "https://images.unsplash.com/photo-1432836431433-925d3cc0a5cd?q=80&fm=jpg&s=97a1e6c0e5adecf5dc6d53e17d6bc581";
                break;
        }
        return name;
    }

    public String getImageName(int month){
        String name = null;
        switch (month){
            case 1:
                name = "photo-1429734160945-4f85244d6a5a.jpg";
                break;
            case 2:
                name = "photo-1437240443155-612416af4d5a.jpg";
                break;
            case 3:
                name = "photo-1438216983993-cdcd7dea84ce.jpg";
                break;
            case 4:
                name = "photo-1438109491414-7198515b166b.jpg";
                break;
            case 5:
                name = "photo-1436915947297-3a94186c8133.jpg";
                break;
            case 6:
                name = "photo-1431578500526-4d9613015464.jpg";
                break;
            case 7:
                name = "Af0sF2OS5S5gatqrKzVP_Silhoutte.jpg";
                break;
            case 8:
                name = "photo-1434394673726-e8232a5903b4.jpg";
                break;
            case 9:
                name = "photo-1433424007598-bd5d102e8597.jpg";
                break;
            case 10:
                name = "photo-1433840496881-cbd845929862.jpg";
                break;
            case 11:
                name = "photo-1432234525151-015bc7937f1c.jpg";
                break;
            case 12:
                name = "photo-1438368915865-a852ef86fc42.jpg";
                break;
            case 13:
                name = "photo-1432836431433-925d3cc0a5cd.jpg";
                break;
        }
        return name;
    }

    public boolean isSdPresent() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }
}
