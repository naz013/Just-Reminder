package com.cray.software.justreminder.dialogs;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.TextView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.interfaces.Configs;
import com.cray.software.justreminder.modules.ManageModule;
import com.cray.software.justreminder.utils.Utils;

public class AboutDialog extends Activity {

    TextView appVersion, appName;
    ColorSetter cs = new ColorSetter(AboutDialog.this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTheme(cs.getDialogStyle());
        setContentView(R.layout.about_dialog_layout);

        appName = (TextView) findViewById(R.id.appName);
        appName.setTypeface(Utils.getMediumTypeface(this));
        String name;
        if (new ManageModule().isPro()) name = getString(R.string.app_name_pro);
        else name = getString(R.string.app_name);
        appName.setText(name.toUpperCase());

        appVersion = (TextView) findViewById(R.id.appVersion);
        appVersion.setTypeface(Utils.getThinTypeface(this));
        PackageInfo pInfo;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = pInfo.versionName;
            appVersion.setText(getString(R.string.app_version_strings) + " " + version + " (" + Configs.CODENAME + ")");
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        TextView rights = (TextView) findViewById(R.id.rights);
        rights.setTypeface(Utils.getThinTypeface(this));
    }
}