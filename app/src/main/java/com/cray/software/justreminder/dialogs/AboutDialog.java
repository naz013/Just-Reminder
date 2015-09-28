package com.cray.software.justreminder.dialogs;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.TextView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.interfaces.Configs;
import com.cray.software.justreminder.modules.Module;
import com.cray.software.justreminder.utils.AssetsUtil;

public class AboutDialog extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ColorSetter cs = new ColorSetter(AboutDialog.this);

        setTheme(cs.getDialogStyle());
        setContentView(R.layout.about_dialog_layout);

        TextView appName = (TextView) findViewById(R.id.appName);
        appName.setTypeface(AssetsUtil.getMediumTypeface(this));
        String name;
        if (Module.isPro()) name = getString(R.string.app_name_pro);
        else if (Module.isBeta()) name = getString(R.string.app_title);
        else name = getString(R.string.app_name);
        appName.setText(name.toUpperCase());

        TextView appVersion = (TextView) findViewById(R.id.appVersion);
        appVersion.setTypeface(AssetsUtil.getThinTypeface(this));
        PackageInfo pInfo;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = pInfo.versionName;
            appVersion.setText(getString(R.string.app_version_strings) + " " + version + (!Module.isBeta() ? " (" + Configs.CODENAME + ")" : ""));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        TextView rights = (TextView) findViewById(R.id.rights);
        rights.setTypeface(AssetsUtil.getThinTypeface(this));
    }
}