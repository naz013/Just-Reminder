package com.cray.software.justreminder.dialogs;

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
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
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
import com.cray.software.justreminder.databases.TasksData;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.interfaces.ExchangeConstants;
import com.cray.software.justreminder.interfaces.TasksConstants;
import com.cray.software.justreminder.modules.ManageModule;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;
import com.google.api.client.googleapis.extensions.android.accounts.GoogleAccountManager;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.tasks.TasksScopes;

import java.io.IOException;

public class CloudDrives extends AppCompatActivity {

    ColorSetter cs = new ColorSetter(CloudDrives.this);
    DropboxHelper dbx = new DropboxHelper(CloudDrives.this);
    GDriveHelper gdx = new GDriveHelper(CloudDrives.this);
    SharedPrefs prefs = new SharedPrefs(CloudDrives.this);

    Button linkDropbox, linkGDrive, linkExchange;
    TextView gDriveTitle, dropboxTitle, exchangeTitle;
    private static final int REQUEST_AUTHORIZATION = 1;
    private static final int REQUEST_ACCOUNT_PICKER = 3;

    String MARKET_APP_JUSTREMINDER = "com.cray.software.justreminder";
    String MARKET_APP_JUSTREMINDER_PRO = "com.cray.software.justreminderpro";

    String accountName;
    private Context ctx = this;
    private Activity a = this;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cs = new ColorSetter(CloudDrives.this);
        setTheme(cs.getStyle());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(cs.colorStatus());
        }
        setContentView(R.layout.clouds_dialog_layout);
        setRequestedOrientation(cs.getRequestOrientation());

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationIcon(R.drawable.ic_clear_white_24dp);
        toolbar.setTitle(getString(R.string.cloud_drives_title));

        findViewById(R.id.windowBackground).setBackgroundColor(cs.getBackgroundStyle());

        dbx.startSession();

        linkDropbox = (Button) findViewById(R.id.linkDropbox);
        linkDropbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isIn;
                if (new ManageModule().isPro()) isIn = isAppInstalled(MARKET_APP_JUSTREMINDER);
                else isIn = isAppInstalled(MARKET_APP_JUSTREMINDER_PRO);
                if (isIn){
                    checkDialog().show();
                } else {
                    if (dbx.isLinked()){
                        if (dbx.unlink()){
                            linkDropbox.setText(getString(R.string.login_button));
                        }
                    } else {
                        dbx.startLink();
                    }
                }
            }
        });

        linkGDrive = (Button) findViewById(R.id.linkGDrive);
        linkGDrive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                } else {
                    Intent intent = AccountPicker.newChooseAccountIntent(null, null,
                            new String[]{"com.google"}, false, null, null, null, null);
                    startActivityForResult(intent, REQUEST_AUTHORIZATION);
                }
            }
        });

        dropboxTitle = (TextView) findViewById(R.id.dropboxTitle);
        exchangeTitle = (TextView) findViewById(R.id.exchangeTitle);
        gDriveTitle = (TextView) findViewById(R.id.gDriveTitle);
        gDriveTitle.setText(gDriveTitle.getText() + " " + getString(R.string.string_and) +
                " " + getString(R.string.google_tasks_title));

        linkExchange = (Button) findViewById(R.id.linkExchange);
        linkExchange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TasksData data = new TasksData(CloudDrives.this);
                data.open();
                Cursor c = data.getAccount();
                if (c != null && c.moveToFirst()){
                    do {
                        data.deleteAccount(c.getLong(c.getColumnIndex(ExchangeConstants.COLUMN_ID)));
                    } while (c.moveToNext());
                    setUpButton();
                } else {
                    startActivity(new Intent(CloudDrives.this, ExchangeLogIn.class));
                }
            }
        });

        setUpButton();

        setImage();
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
                .setTitle(getString(R.string.other_version_dialog_title))
                .setMessage(getString(R.string.other_version_dialog_text))
                .setPositiveButton(getString(R.string.dialog_button_open), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent i;
                        PackageManager manager = getPackageManager();
                        if (new ManageModule().isPro()) i = manager.getLaunchIntentForPackage(MARKET_APP_JUSTREMINDER);
                        else i = manager.getLaunchIntentForPackage(MARKET_APP_JUSTREMINDER_PRO);
                        i.addCategory(Intent.CATEGORY_LAUNCHER);
                        startActivity(i);
                    }
                })
                .setNegativeButton(getString(R.string.dialog_button_delete), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        if (new ManageModule().isPro()) intent.setData(Uri.parse("package:" + MARKET_APP_JUSTREMINDER));
                        else intent.setData(Uri.parse("package:" + MARKET_APP_JUSTREMINDER_PRO));
                        startActivity(intent);
                    }
                })
                .setNeutralButton(getString(R.string.button_close), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setCancelable(true)
                .create();
    }

    private void setUpButton(){
        if (gdx.isLinked()){
            linkGDrive.setText(getString(R.string.logout_button));
        } else {
            linkGDrive.setText(getString(R.string.login_button));
        }

        TasksData data = new TasksData(CloudDrives.this);
        data.open();
        Cursor c = data.getAccount();
        if (c != null && c.moveToFirst()){
            linkExchange.setText(getString(R.string.logout_button));
        } else {
            linkExchange.setText(getString(R.string.login_button));
        }
    }

    private void setImage(){
        SharedPrefs sPrefs = new SharedPrefs(CloudDrives.this);
        if (!sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_USE_DARK_THEME)){
            dropboxTitle.setCompoundDrawablesWithIntrinsicBounds(R.drawable.dropbox_icon, 0, 0, 0);
            gDriveTitle.setCompoundDrawablesWithIntrinsicBounds(R.drawable.google_grey, 0, 0, 0);
            exchangeTitle.setCompoundDrawablesWithIntrinsicBounds(R.drawable.exchange_grey, 0, 0, 0);
        } else {
            dropboxTitle.setCompoundDrawablesWithIntrinsicBounds(R.drawable.dropbox_icon_white, 0, 0, 0);
            gDriveTitle.setCompoundDrawablesWithIntrinsicBounds(R.drawable.google_white, 0, 0, 0);
            exchangeTitle.setCompoundDrawablesWithIntrinsicBounds(R.drawable.exchange_white, 0, 0, 0);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!dbx.isLinked()) {
            if (dbx.checkLink()) {
                linkDropbox.setText(getString(R.string.logout_button));
                linkDropbox.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (dbx.unlink()){
                            linkDropbox.setText(getString(R.string.login_button));
                        }
                    }
                });
            }
        } else {
            linkDropbox.setText(getString(R.string.logout_button));
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
                progressDlg.setTitle(getString(R.string.connecting_dialog_title));
                progressDlg.setMessage(getString(R.string.application_verifying_text));
                progressDlg.setCancelable(false);
                progressDlg.setIndeterminate(false);
                progressDlg.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    public void onCancel(DialogInterface d) {
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
            prefs.savePrefs(Constants.APP_UI_PREFERENCES_DRIVE_USER, new SyncHelper(CloudDrives.this).encrypt(accountName));
            new GetTasksListsAsync(CloudDrives.this, null).execute();
        } else if (requestCode == REQUEST_ACCOUNT_PICKER && resultCode == RESULT_OK) {
            accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
            prefs.savePrefs(Constants.APP_UI_PREFERENCES_DRIVE_USER, new SyncHelper(CloudDrives.this).encrypt(accountName));
            new GetTasksListsAsync(CloudDrives.this, null).execute();
        }
    }
}
