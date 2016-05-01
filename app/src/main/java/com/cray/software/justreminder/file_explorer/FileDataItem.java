package com.cray.software.justreminder.file_explorer;

public class FileDataItem {
    private String fileName;
    private String filePath;
    private int icon;

    public FileDataItem(String fileName, Integer icon, String filePath) {
        this.fileName = fileName;
        this.filePath = filePath;
        this.icon = icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public int getIcon() {
        return icon;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    @Override
    public String toString() {
        return fileName;
    }
}
