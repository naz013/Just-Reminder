package com.cray.software.justreminder.dialogs;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.modules.Module;
import com.cray.software.justreminder.roboto_views.RoboTextView;

/**
 * About application dialog.
 */
public class AboutDialog extends Activity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ColorSetter cs = new ColorSetter(AboutDialog.this);
        setTheme(cs.getDialogStyle());
        setContentView(R.layout.about_dialog_layout);
        RoboTextView appName = (RoboTextView) findViewById(R.id.appName);
        String name;
        if (Module.isPro()) name = getString(R.string.app_name_pro);
        else name = getString(R.string.app_name);
        appName.setText(name.toUpperCase());
        RoboTextView appVersion = (RoboTextView) findViewById(R.id.appVersion);
        PackageInfo pInfo;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            appVersion.setText(pInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }
}
