/**
 * Copyright 2015 Nazar Suhovich
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

package com.cray.software.justreminder.activities;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.cloud.Dropbox;
import com.cray.software.justreminder.cloud.GoogleDrive;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.google_tasks.GetTasksListsAsync;
import com.cray.software.justreminder.google_tasks.TasksHelper;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.Permissions;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.helpers.Module;
import com.cray.software.justreminder.roboto_views.RoboButton;
import com.cray.software.justreminder.roboto_views.RoboTextView;
import com.cray.software.justreminder.utils.LocationUtil;
import com.cray.software.justreminder.utils.ViewUtils;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;
import com.google.api.client.googleapis.extensions.android.accounts.GoogleAccountManager;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.tasks.TasksScopes;

import java.io.IOException;

public class CloudDrivesActivity extends AppCompatActivity {

    private static final int REQUEST_AUTHORIZATION = 1;
    private static final int REQUEST_ACCOUNT_PICKER = 3;
    private static final String MARKET_APP_JUSTREMINDER = "com.cray.software.justreminder";
    private static final String MARKET_APP_JUSTREMINDER_PRO = "com.cray.software.justreminderpro";

    private Dropbox mDropbox = new Dropbox(CloudDrivesActivity.this);
    private GoogleDrive mGoogleDrive = new GoogleDrive(CloudDrivesActivity.this);

    private RoboButton mDropboxButton, mGoogleDriveButton;
    private RoboTextView mGoogleDriveTitle, mDropboxTitle;

    private String mAccountName;
    private Context mContext = this;
    private Activity mActivity = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDropbox.startSession();
        ColorSetter cs = ColorSetter.getInstance(CloudDrivesActivity.this);
        setTheme(cs.getStyle());
        if (Module.isLollipop()) {
            getWindow().setStatusBarColor(ViewUtils.getColor(this, cs.colorPrimaryDark()));
        }
        setContentView(R.layout.clouds_dialog_layout);
        setRequestedOrientation(cs.getRequestOrientation());
        findViewById(R.id.windowBackground).setBackgroundColor(cs.getBackgroundStyle());
        initActionBar();
        initDropboxButton();
        initGoogleDriveButton();
        initTitles();
        checkGoogleStatus();
        setImage();
    }

    private void initTitles() {
        mDropboxTitle = (RoboTextView) findViewById(R.id.dropboxTitle);
        mGoogleDriveTitle = (RoboTextView) findViewById(R.id.gDriveTitle);
    }

    private void initGoogleDriveButton() {
        mGoogleDriveButton = (RoboButton) findViewById(R.id.linkGDrive);
        mGoogleDriveButton.setOnClickListener(v -> {
            if (Permissions.checkPermission(CloudDrivesActivity.this,
                    Permissions.GET_ACCOUNTS, Permissions.READ_EXTERNAL,
                    Permissions.WRITE_EXTERNAL)) {
                switchGoogleStatus();
            } else {
                Permissions.requestPermission(CloudDrivesActivity.this, 103,
                        Permissions.GET_ACCOUNTS, Permissions.READ_EXTERNAL,
                        Permissions.WRITE_EXTERNAL);
            }
        });
    }

    private void initDropboxButton() {
        mDropboxButton = (RoboButton) findViewById(R.id.linkDropbox);
        mDropboxButton.setOnClickListener(v -> dropboxClick());
    }

    private void dropboxClick() {
        boolean isIn = isAppInstalled(MARKET_APP_JUSTREMINDER_PRO);
        if (Module.isPro()) isIn = isAppInstalled(MARKET_APP_JUSTREMINDER);
        if (isIn) {
            checkDialog().show();
        } else {
            performDropboxLinking();
        }
        SharedPrefs.getInstance(this).putBoolean(Prefs.UI_CHANGED, true);
    }

    private void performDropboxLinking() {
        if (mDropbox.isLinked()) {
            if (mDropbox.unlink()) {
                mDropboxButton.setText(getString(R.string.connect));
            }
        } else {
            mDropbox.startLink();
        }
    }

    private void initActionBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setTitle(getString(R.string.cloud_services));
    }

    private void switchGoogleStatus() {
        if (!LocationUtil.checkGooglePlayServicesAvailability(this)) {
            Toast.makeText(this, R.string.google_play_services_not_installed, Toast.LENGTH_SHORT).show();
            return;
        }
        if (mGoogleDrive.isLinked()){
            disconnectFromGoogleServices();
        } else {
            requestGoolgeConnection();
        }
        SharedPrefs.getInstance(this).putBoolean(Prefs.UI_CHANGED, true);
    }

    private void requestGoolgeConnection() {
        Intent intent = AccountPicker.newChooseAccountIntent(null, null,
                new String[]{"com.google"}, false, null, null, null, null);
        startActivityForResult(intent, REQUEST_AUTHORIZATION);
    }

    private void disconnectFromGoogleServices() {
        mGoogleDrive.unlink();
        checkGoogleStatus();
        TasksHelper.getInstance(this).deleteTasks();
        TasksHelper.getInstance(this).deleteTaskLists();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode){
            case 103:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    switchGoogleStatus();
                }
                break;
        }
    }

    private boolean isAppInstalled(String packageName) {
        PackageManager pm = getPackageManager();
        boolean installed;
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            installed = false;
        }
        return installed;
    }

    protected Dialog checkDialog() {
        return new AlertDialog.Builder(this)
                .setMessage(getString(R.string.other_version_detected))
                .setPositiveButton(getString(R.string.open), (dialog, which) -> {
                    Intent i;
                    PackageManager manager = getPackageManager();
                    if (Module.isPro()) i = manager.getLaunchIntentForPackage(MARKET_APP_JUSTREMINDER);
                    else i = manager.getLaunchIntentForPackage(MARKET_APP_JUSTREMINDER_PRO);
                    i.addCategory(Intent.CATEGORY_LAUNCHER);
                    startActivity(i);
                })
                .setNegativeButton(getString(R.string.delete), (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    if (Module.isPro()) intent.setData(Uri.parse("package:" + MARKET_APP_JUSTREMINDER));
                    else intent.setData(Uri.parse("package:" + MARKET_APP_JUSTREMINDER_PRO));
                    startActivity(intent);
                })
                .setNeutralButton(getString(R.string.cancel), (dialog, which) -> {
                    dialog.dismiss();
                })
                .setCancelable(true)
                .create();
    }

    private void checkGoogleStatus(){
        if (mGoogleDrive.isLinked()){
            mGoogleDriveButton.setText(R.string.disconnect);
        } else {
            mGoogleDriveButton.setText(getString(R.string.connect));
        }
    }

    private void setImage(){
        if (!ColorSetter.getInstance(this).isDark()){
            mDropboxTitle.setCompoundDrawablesWithIntrinsicBounds(R.drawable.dropbox_icon, 0, 0, 0);
            mGoogleDriveTitle.setCompoundDrawablesWithIntrinsicBounds(R.drawable.google_grey, 0, 0, 0);
        } else {
            mDropboxTitle.setCompoundDrawablesWithIntrinsicBounds(R.drawable.dropbox_icon_white, 0, 0, 0);
            mGoogleDriveTitle.setCompoundDrawablesWithIntrinsicBounds(R.drawable.google_white, 0, 0, 0);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkDropboxStatus();
        checkGoogleStatus();
    }

    private void checkDropboxStatus() {
        if (!mDropbox.isLinked() && mDropbox.checkLink()) {
            mDropboxButton.setText(getString(R.string.disconnect));
            mDropboxButton.setOnClickListener(v -> {
                if (mDropbox.unlink()){
                    mDropboxButton.setText(getString(R.string.connect));
                }
            });
        } else {
            mDropboxButton.setText(getString(R.string.disconnect));
        }
    }

    void getAndUseAuthTokenInAsyncTask(Account account) {
        AsyncTask<Account, String, String> task = new AsyncTask<Account, String, String>() {
            ProgressDialog progressDlg;
            AsyncTask<Account, String, String> me = this;

            @Override
            protected void onPreExecute() {
                progressDlg = new ProgressDialog(CloudDrivesActivity.this, ProgressDialog.STYLE_SPINNER);
                progressDlg.setMax(100);
                progressDlg.setMessage(getString(R.string.trying_to_log_in));
                progressDlg.setCancelable(false);
                progressDlg.setIndeterminate(false);
                progressDlg.setOnCancelListener(dialog -> {
                    progressDlg.dismiss();
                    me.cancel(true);
                });
                progressDlg.show();
            }

            @Override
            protected String doInBackground(Account... params) {
                return getAccessToken(params[0]);
            }

            @Override
            protected void onPostExecute(String s) {
                if (s != null) {
                    mAccountName = s;
                }
                progressDlg.dismiss();
            }
        };
        task.execute(account);
    }

    private String getAccessToken(Account account) {
        try {
            return GoogleAuthUtil.getToken(mContext, account.name, "oauth2:" + DriveScopes.DRIVE + " " + TasksScopes.TASKS);
        } catch (UserRecoverableAuthException e) {
            mActivity.startActivityForResult(e.getIntent(), REQUEST_ACCOUNT_PICKER);
            e.printStackTrace();
            return null;
        } catch (GoogleAuthException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_AUTHORIZATION && resultCode == RESULT_OK) {
            mAccountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
            GoogleAccountManager gam = new GoogleAccountManager(this);
            getAndUseAuthTokenInAsyncTask(gam.getAccountByName(mAccountName));
            startSync(mAccountName);
        } else if (requestCode == REQUEST_ACCOUNT_PICKER && resultCode == RESULT_OK) {
            mAccountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
            startSync(mAccountName);
        }
    }

    private void startSync(String accountName) {
        SharedPrefs.getInstance(this).putString(Prefs.DRIVE_USER, SyncHelper.encrypt(accountName));
        new GetTasksListsAsync(CloudDrivesActivity.this, null).execute();
    }
}
