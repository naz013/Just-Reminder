package com.cray.software.justreminder.activities;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.async.GetTasksListsAsync;
import com.cray.software.justreminder.cloud.DropboxHelper;
import com.cray.software.justreminder.cloud.GDriveHelper;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.constants.TasksConstants;
import com.cray.software.justreminder.databases.TasksData;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.Permissions;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.modules.Module;
import com.cray.software.justreminder.utils.ViewUtils;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;
import com.google.api.client.googleapis.extensions.android.accounts.GoogleAccountManager;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.tasks.TasksScopes;

import java.io.IOException;

public class CloudDrives extends AppCompatActivity {

    private DropboxHelper dbx = new DropboxHelper(CloudDrives.this);
    private GDriveHelper gdx = new GDriveHelper(CloudDrives.this);
    private SharedPrefs prefs = new SharedPrefs(CloudDrives.this);

    private Button linkDropbox, linkGDrive;
    private TextView gDriveTitle, dropboxTitle;
    private static final int REQUEST_AUTHORIZATION = 1;
    private static final int REQUEST_ACCOUNT_PICKER = 3;

    private String MARKET_APP_JUSTREMINDER = "com.cray.software.justreminder";
    private String MARKET_APP_JUSTREMINDER_PRO = "com.cray.software.justreminderpro";

    private String accountName;
    private Context ctx = this;
    private Activity a = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ColorSetter cs = new ColorSetter(CloudDrives.this);
        setTheme(cs.getStyle());
        if (Module.isLollipop()) {
            getWindow().setStatusBarColor(ViewUtils.getColor(this, cs.colorPrimaryDark()));
        }
        setContentView(R.layout.clouds_dialog_layout);
        setRequestedOrientation(cs.getRequestOrientation());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setTitle(getString(R.string.cloud_services));

        findViewById(R.id.windowBackground).setBackgroundColor(cs.getBackgroundStyle());

        dbx.startSession();

        linkDropbox = (Button) findViewById(R.id.linkDropbox);
        linkDropbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isIn;
                if (Module.isPro()) isIn = isAppInstalled(MARKET_APP_JUSTREMINDER);
                else isIn = isAppInstalled(MARKET_APP_JUSTREMINDER_PRO);
                if (isIn) {
                    checkDialog().show();
                } else {
                    if (dbx.isLinked()) {
                        if (dbx.unlink()) {
                            linkDropbox.setText(getString(R.string.connect));
                        }
                    } else {
                        dbx.startLink();
                    }
                }
                prefs.saveBoolean(Prefs.UI_CHANGED, true);
            }
        });

        linkGDrive = (Button) findViewById(R.id.linkGDrive);
        linkGDrive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Permissions.checkPermission(CloudDrives.this,
                        Permissions.GET_ACCOUNTS, Permissions.READ_EXTERNAL,
                        Permissions.WRITE_EXTERNAL)) {
                    switchGdrive();
                } else {
                    Permissions.requestPermission(CloudDrives.this, 103,
                            Permissions.GET_ACCOUNTS, Permissions.READ_EXTERNAL,
                            Permissions.WRITE_EXTERNAL);
                }
            }
        });

        dropboxTitle = (TextView) findViewById(R.id.dropboxTitle);
        gDriveTitle = (TextView) findViewById(R.id.gDriveTitle);

        setUpButton();

        setImage();
    }

    private void switchGdrive() {
        if (gdx.isLinked()){
            gdx.unlink();
            setUpButton();
            TasksData data = new TasksData(CloudDrives.this);
            Cursor c = data.getTasks();
            if (c != null && c.moveToFirst()){
                do {
                    data.deleteTask(c.getLong(c.getColumnIndex(TasksConstants.COLUMN_ID)));
                } while (c.moveToNext());
            }
            if (c != null) c.close();

            Cursor s = data.getTasksLists();
            if (s != null && s.moveToFirst()){
                do {
                    data.deleteTasksList(s.getLong(s.getColumnIndex(TasksConstants.COLUMN_ID)));
                } while (s.moveToNext());
            }
            data.close();
        } else {
            Intent intent = AccountPicker.newChooseAccountIntent(null, null,
                    new String[]{"com.google"}, false, null, null, null, null);
            startActivityForResult(intent, REQUEST_AUTHORIZATION);
        }
        prefs.saveBoolean(Prefs.UI_CHANGED, true);
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
                    switchGdrive();
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
                .setPositiveButton(getString(R.string.open), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent i;
                        PackageManager manager = getPackageManager();
                        if (Module.isPro()) i = manager.getLaunchIntentForPackage(MARKET_APP_JUSTREMINDER);
                        else i = manager.getLaunchIntentForPackage(MARKET_APP_JUSTREMINDER_PRO);
                        i.addCategory(Intent.CATEGORY_LAUNCHER);
                        startActivity(i);
                    }
                })
                .setNegativeButton(getString(R.string.delete), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        if (Module.isPro()) intent.setData(Uri.parse("package:" + MARKET_APP_JUSTREMINDER));
                        else intent.setData(Uri.parse("package:" + MARKET_APP_JUSTREMINDER_PRO));
                        startActivity(intent);
                    }
                })
                .setNeutralButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setCancelable(true)
                .create();
    }

    private void setUpButton(){
        if (gdx.isLinked()){
            linkGDrive.setText(R.string.disconnect);
        } else {
            linkGDrive.setText(getString(R.string.connect));
        }
    }

    private void setImage(){
        if (!new ColorSetter(this).isDark()){
            dropboxTitle.setCompoundDrawablesWithIntrinsicBounds(R.drawable.dropbox_icon, 0, 0, 0);
            gDriveTitle.setCompoundDrawablesWithIntrinsicBounds(R.drawable.google_grey, 0, 0, 0);
        } else {
            dropboxTitle.setCompoundDrawablesWithIntrinsicBounds(R.drawable.dropbox_icon_white, 0, 0, 0);
            gDriveTitle.setCompoundDrawablesWithIntrinsicBounds(R.drawable.google_white, 0, 0, 0);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!dbx.isLinked()) {
            if (dbx.checkLink()) {
                linkDropbox.setText(getString(R.string.disconnect));
                linkDropbox.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (dbx.unlink()){
                            linkDropbox.setText(getString(R.string.connect));
                        }
                    }
                });
            }
        } else {
            linkDropbox.setText(getString(R.string.disconnect));
        }

        setUpButton();
    }

    void getAndUseAuthTokenInAsyncTask(Account account) {
        AsyncTask<Account, String, String> task = new AsyncTask<Account, String, String>() {
            ProgressDialog progressDlg;
            AsyncTask<Account, String, String> me = this;

            @Override
            protected void onPreExecute() {
                progressDlg = new ProgressDialog(CloudDrives.this, ProgressDialog.STYLE_SPINNER);
                progressDlg.setMax(100);
                progressDlg.setMessage(getString(R.string.trying_to_log_in));
                progressDlg.setCancelable(false);
                progressDlg.setIndeterminate(false);
                progressDlg.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        progressDlg.dismiss();
                        me.cancel(true);
                    }
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
                    accountName = s;
                }
                progressDlg.dismiss();
            }
        };
        task.execute(account);
    }

    private String getAccessToken(Account account) {
        try {
            return GoogleAuthUtil.getToken(ctx, account.name, "oauth2:" + DriveScopes.DRIVE + " " + TasksScopes.TASKS);
        } catch (UserRecoverableAuthException e) {
            a.startActivityForResult(e.getIntent(), REQUEST_ACCOUNT_PICKER);
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
            accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
            GoogleAccountManager gam = new GoogleAccountManager(this);
            getAndUseAuthTokenInAsyncTask(gam.getAccountByName(accountName));
            startSync(accountName);
        } else if (requestCode == REQUEST_ACCOUNT_PICKER && resultCode == RESULT_OK) {
            accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
            startSync(accountName);
        }
    }

    private void startSync(String accountName) {
        prefs.savePrefs(Prefs.DRIVE_USER, SyncHelper.encrypt(accountName));
        new GetTasksListsAsync(CloudDrives.this, null).execute();
    }
}
