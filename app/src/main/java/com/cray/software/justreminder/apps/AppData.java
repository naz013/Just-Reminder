package com.cray.software.justreminder.apps;

import android.graphics.drawable.Drawable;

public class AppData {
    private String name;
    private String packageName;
    private Drawable drawable;

    public AppData(String name, String packageName, Drawable drawable) {
        this.name = name;
        this.packageName = packageName;
        this.drawable = drawable;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public Drawable getDrawable() {
        return drawable;
    }

    public void setDrawable(Drawable drawable) {
        this.drawable = drawable;
    }
}
