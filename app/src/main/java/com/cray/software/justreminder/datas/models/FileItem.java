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

package com.cray.software.justreminder.datas.models;

public class FileItem {
    private String fileName, lastModified, filePath;

    public FileItem(String fileName, String lastModified, String filePath){
        this.fileName = fileName;
        this.filePath = filePath;
        this.lastModified = lastModified;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }

    public String getFileName() {
        return fileName;
    }

    public String getLastModified() {
        return lastModified;
    }
}
