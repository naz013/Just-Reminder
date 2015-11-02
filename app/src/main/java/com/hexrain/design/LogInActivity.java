package com.hexrain.design;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.async.ScanTask;
import com.cray.software.justreminder.cloud.DropboxHelper;
import com.cray.software.justreminder.cloud.GTasksHelper;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.databases.TasksData;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.Contacts;
import com.cray.software.justreminder.helpers.IOHelper;
import com.cray.software.justreminder.helpers.Permissions;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.interfaces.Prefs;
import com.cray.software.justreminder.interfaces.TasksConstants;
import com.cray.software.justreminder.modules.Module;
import com.cray.software.justreminder.views.CircularProgress;
import com.cray.software.justreminder.views.PaperButton;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;
import com.google.api.client.googleapis.extensions.android.accounts.GoogleAccountManager;
import com.google.api.client.util.DateTime;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.tasks.TasksScopes;
import com.google.api.services.tasks.model.Task;
import com.google.api.services.tasks.model.TaskList;
import com.google.api.services.tasks.model.TaskLists;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class LogInActivity extends Activity {

    private SharedPrefs sPrefs;
    private ColorSetter cs = new ColorSetter(LogInActivity.this);
    private PaperButton connectGDrive, connectDropbox;
    private CheckBox checkBox;
    private TextView skipButton, progressMesage;
    private CircularProgress progress;

    private DropboxHelper dbx = new DropboxHelper(LogInActivity.this);
    private static final int REQUEST_AUTHORIZATION = 1;
    private static final int REQUEST_ACCOUNT_PICKER = 3;

    public static final String MARKET_APP_JUSTREMINDER = "com.cray.software.justreminder";
    public static final String MARKET_APP_JUSTREMINDER_PRO = "com.cray.software.justreminderpro";

    private String accountName;
    private Context ctx = this;
    private Activity a = this;

    private boolean enabled = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(cs.getFullscreenStyle());
        setContentView(R.layout.activity_log_in);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(cs.colorStatus());
        }
        setRequestedOrientation(cs.getRequestOrientation());

        findViewById(R.id.windowBackground).setBackgroundColor(cs.getBackgroundStyle());

        sPrefs = new SharedPrefs(LogInActivity.this);
        dbx.startSession();

        connectGDrive = (PaperButton) findViewById(R.id.connectGDrive);
        connectDropbox = (PaperButton) findViewById(R.id.connectDropbox);
        checkBox = (CheckBox) findViewById(R.id.checkBox);
        checkBox.setChecked(true);
        skipButton = (TextView) findViewById(R.id.skipButton);
        String text = skipButton.getText().toString();
        skipButton.setText(text + " (" + getString(R.string.string_local_sync) + ")");
        progressMesage = (TextView) findViewById(R.id.progressMesage);
        progress = (CircularProgress) findViewById(R.id.progress);
        progress.setVisibility(View.INVISIBLE);

        connectDropbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (enabled) {
                    boolean isIn;
                    if (Module.isPro()) isIn = isAppInstalled(MARKET_APP_JUSTREMINDER);
                    else isIn = isAppInstalled(MARKET_APP_JUSTREMINDER_PRO);
                    if (isIn) {
                        checkDialog().show();
                    } else {
                        dbx.startLink();
                        enabled = false;
                    }
                }
            }
        });

        connectGDrive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (enabled) {
                    if (new Permissions(LogInActivity.this).checkPermission(Permissions.GET_ACCOUNTS)) {
                        Intent intent = AccountPicker.newChooseAccountIntent(null, null,
                                new String[]{"com.google"}, false, null, null, null, null);
                        startActivityForResult(intent, REQUEST_AUTHORIZATION);
                        enabled = false;
                    } else {
                        new Permissions(LogInActivity.this).requestPermission(LogInActivity.this,
                                new String[]{Permissions.GET_ACCOUNTS, Permissions.READ_EXTERNAL,
                                        Permissions.WRITE_EXTERNAL}, 103);
                    }
                }
            }
        });

        skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (enabled) {
                    Permissions permissions = new Permissions(LogInActivity.this);
                    if (permissions.checkPermission(Permissions.READ_EXTERNAL) &&
                            permissions.checkPermission(Permissions.WRITE_EXTERNAL)) {
                        new LocalSync(LogInActivity.this, progress, progressMesage).execute();
                        enabled = false;
                    } else {
                        permissions.requestPermission(LogInActivity.this,
                                new String[]{Permissions.READ_EXTERNAL, Permissions.WRITE_EXTERNAL}, 101);
                    }
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode){
            case 101:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    new LocalSync(LogInActivity.this, progress, progressMesage).execute();
                    enabled = false;
                } else {
                    startActivity(new Intent(LogInActivity.this, ScreenManager.class));
                    finish();
                }
                break;
            case 102:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    new ImportBirthdays(LogInActivity.this).execute();
                    enabled = false;
                } else {
                    startActivity(new Intent(LogInActivity.this, ScreenManager.class));
                    finish();
                }
                break;
            case 103:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Intent intent = AccountPicker.newChooseAccountIntent(null, null,
                            new String[]{"com.google"}, false, null, null, null, null);
                    startActivityForResult(intent, REQUEST_AUTHORIZATION);
                    enabled = false;
                } else {
                    startActivity(new Intent(LogInActivity.this, ScreenManager.class));
                    finish();
                }
                break;
            case 104:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    new SyncTask(LogInActivity.this, progress, progressMesage).execute();
                    enabled = false;
                } else {
                    startActivity(new Intent(LogInActivity.this, ScreenManager.class));
                    finish();
                }
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        dbx = new DropboxHelper(LogInActivity.this);
        sPrefs = new SharedPrefs(LogInActivity.this);
        dbx.startSession();
        if (!dbx.isLinked()) {
            if (dbx.checkLink()) {
                connectDropbox.setEnabled(false);
                connectGDrive.setEnabled(false);
                skipButton.setEnabled(false);
                sPrefs.saveBoolean(Prefs.AUTO_BACKUP, true);
                Permissions permissions = new Permissions(LogInActivity.this);
                if (permissions.checkPermission(Permissions.READ_EXTERNAL) &&
                        permissions.checkPermission(Permissions.WRITE_EXTERNAL)) {
                    new SyncTask(LogInActivity.this, progress, progressMesage).execute();
                    enabled = false;
                } else {
                    permissions.requestPermission(LogInActivity.this,
                            new String[]{Permissions.READ_EXTERNAL, Permissions.WRITE_EXTERNAL}, 104);
                }
            }
        } else {
            connectDropbox.setEnabled(false);
            connectGDrive.setEnabled(false);
            skipButton.setEnabled(false);
            sPrefs.saveBoolean(Prefs.AUTO_BACKUP, true);
            Permissions permissions = new Permissions(LogInActivity.this);
            if (permissions.checkPermission(Permissions.READ_EXTERNAL) &&
                    permissions.checkPermission(Permissions.WRITE_EXTERNAL)) {
                new SyncTask(LogInActivity.this, progress, progressMesage).execute();
                enabled = false;
            } else {
                permissions.requestPermission(LogInActivity.this,
                        new String[]{Permissions.READ_EXTERNAL, Permissions.WRITE_EXTERNAL}, 104);
            }
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
                        if (Module.isPro()) i = manager.getLaunchIntentForPackage(MARKET_APP_JUSTREMINDER);
                        else i = manager.getLaunchIntentForPackage(MARKET_APP_JUSTREMINDER_PRO);
                        i.addCategory(Intent.CATEGORY_LAUNCHER);
                        startActivity(i);
                    }
                })
                .setNegativeButton(getString(R.string.dialog_button_delete), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        if (Module.isPro()) intent.setData(Uri.parse("package:" + MARKET_APP_JUSTREMINDER));
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

    void getAndUseAuthTokenInAsyncTask(Account account) {
        AsyncTask<Account, String, String> task = new AsyncTask<Account, String, String>() {

            @Override
            protected void onPreExecute() {
                progress.setVisibility(View.VISIBLE);
                progressMesage.setText(getString(R.string.string_try_to_log_in));
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
            sPrefs.savePrefs(Prefs.DRIVE_USER, SyncHelper.encrypt(accountName));
            progressMesage.setText(getString(R.string.string_successfully_logged));
            connectDropbox.setEnabled(false);
            connectGDrive.setEnabled(false);
            skipButton.setEnabled(false);
            sPrefs.saveBoolean(Prefs.AUTO_BACKUP, true);
            Permissions permissions = new Permissions(LogInActivity.this);
            if (permissions.checkPermission(Permissions.READ_EXTERNAL) &&
                    permissions.checkPermission(Permissions.WRITE_EXTERNAL)) {
                new SyncTask(LogInActivity.this, progress, progressMesage).execute();
            } else {
                permissions.requestPermission(LogInActivity.this,
                        new String[]{Permissions.READ_EXTERNAL, Permissions.WRITE_EXTERNAL}, 104);
            }
        } else if (requestCode == REQUEST_ACCOUNT_PICKER && resultCode == RESULT_OK) {
            accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
            sPrefs.savePrefs(Prefs.DRIVE_USER, SyncHelper.encrypt(accountName));
            connectDropbox.setEnabled(false);
            connectGDrive.setEnabled(false);
            skipButton.setEnabled(false);
            sPrefs.saveBoolean(Prefs.AUTO_BACKUP, true);
            Permissions permissions = new Permissions(LogInActivity.this);
            if (permissions.checkPermission(Permissions.READ_EXTERNAL) &&
                    permissions.checkPermission(Permissions.WRITE_EXTERNAL)) {
                new SyncTask(LogInActivity.this, progress, progressMesage).execute();
            } else {
                permissions.requestPermission(LogInActivity.this,
                        new String[]{Permissions.READ_EXTERNAL, Permissions.WRITE_EXTERNAL}, 104);
            }
            progressMesage.setText(getString(R.string.string_successfully_logged));
        }
    }

    public class LocalSync extends AsyncTask<Void, String, Void>{

        Context mContext;
        CircularProgress mProgress;
        TextView mText;
        boolean isChecked = false;

        public LocalSync(Context context, CircularProgress progress, TextView textView){
            this.mContext = context;
            this.mProgress = progress;
            this.mText = textView;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgress.setVisibility(View.VISIBLE);
            isChecked = checkBox.isChecked();
        }

        @Override
        protected void onProgressUpdate(final String... values) {
            super.onProgressUpdate(values);
            new android.os.Handler().post(new Runnable() {
                @Override
                public void run() {
                    mText.setText(values[0]);
                }
            });
        }

        @Override
        protected Void doInBackground(Void... params) {
            Looper.prepare();
            DataBase DB = new DataBase(mContext);
            DB.open();
            IOHelper ioHelper = new IOHelper(mContext);

            publishProgress(getString(R.string.string_getting_groups));
            ioHelper.restoreGroup(false);

            Cursor cat = DB.queryCategories();
            if (cat == null || cat.getCount() == 0){
                long time = System.currentTimeMillis();
                String defUiID = SyncHelper.generateID();
                DB.addCategory("General", time, defUiID, 5);
                DB.addCategory("Work", time, SyncHelper.generateID(), 3);
                DB.addCategory("Personal", time, SyncHelper.generateID(), 0);
                Cursor c = DB.queryGroup();
                if (c != null && c.moveToFirst()){
                    do {
                        DB.setGroup(c.getLong(c.getColumnIndex(Constants.COLUMN_ID)), defUiID);
                    } while (c.moveToNext());
                }
                if (c != null) c.close();
            }

            //import reminders
            publishProgress(getString(R.string.string_getting_reminders));
            ioHelper.restoreReminder(false);

            //import notes
            publishProgress(getString(R.string.string_getting_notes));
            ioHelper.restoreNote(false);

            //import birthdays
            if (isChecked) {
                publishProgress(getString(R.string.string_getting_birthdays));
                ioHelper.restoreBirthday(false);
            }

            DB.close();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (checkBox.isChecked()) {
                sPrefs.saveBoolean(Prefs.CONTACT_BIRTHDAYS, true);
                sPrefs.saveBoolean(Prefs.CONTACTS_IMPORT_DIALOG, true);
                sPrefs.saveBoolean(Prefs.AUTO_CHECK_BIRTHDAYS, true);
                sPrefs.saveBoolean(Prefs.WIDGET_BIRTHDAYS, true);
                sPrefs.saveBoolean(Prefs.SYNC_BIRTHDAYS, true);
                if (new Permissions(mContext).checkPermission(Permissions.READ_CONTACTS)) {
                    new ImportBirthdays(LogInActivity.this).execute();
                } else {
                    new Permissions(mContext).requestPermission(LogInActivity.this,
                            new String[]{Permissions.READ_CONTACTS}, 102);
                }
            } else {
                sPrefs.saveBoolean(Prefs.CONTACT_BIRTHDAYS, false);
                sPrefs.saveBoolean(Prefs.CONTACTS_IMPORT_DIALOG, true);
                sPrefs.saveBoolean(Prefs.AUTO_CHECK_BIRTHDAYS, false);
                sPrefs.saveBoolean(Prefs.WIDGET_BIRTHDAYS, false);
                sPrefs.saveBoolean(Prefs.SYNC_BIRTHDAYS, false);
            }
            mProgress.setVisibility(View.INVISIBLE);
            mText.setText(getString(R.string.simple_done));
            startActivity(new Intent(LogInActivity.this, ScreenManager.class));
            finish();
        }
    }

    public class SyncTask extends AsyncTask<Void, String, Void>{

        Context mContext;
        CircularProgress mProgress;
        TextView mText;
        boolean isChecked = false;

        public SyncTask(Context context, CircularProgress progress, TextView textView){
            this.mContext = context;
            this.mProgress = progress;
            this.mText = textView;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgress.setVisibility(View.VISIBLE);
            isChecked = checkBox.isChecked();
        }

        @Override
        protected void onProgressUpdate(final String... values) {
            super.onProgressUpdate(values);
            new android.os.Handler().post(new Runnable() {
                @Override
                public void run() {
                    mText.setText(values[0]);
                }
            });
        }

        @Override
        protected Void doInBackground(Void... params) {
            Looper.prepare();
            DataBase DB = new DataBase(mContext);
            DB.open();

            IOHelper ioHelper = new IOHelper(mContext);

            publishProgress(getString(R.string.string_getting_groups));
            ioHelper.restoreGroup(true);

            Cursor cat = DB.queryCategories();
            if (cat == null || cat.getCount() == 0){
                long time = System.currentTimeMillis();
                String defUiID = SyncHelper.generateID();
                DB.addCategory("General", time, defUiID, 5);
                DB.addCategory("Work", time, SyncHelper.generateID(), 3);
                DB.addCategory("Personal", time, SyncHelper.generateID(), 0);
                Cursor c = DB.queryGroup();
                if (c != null && c.moveToFirst()){
                    do {
                        DB.setGroup(c.getLong(c.getColumnIndex(Constants.COLUMN_ID)), defUiID);
                    } while (c.moveToNext());
                }
                if (c != null) c.close();
            }

            //import reminders
            publishProgress(getString(R.string.string_getting_reminders));
            ioHelper.restoreReminder(true);

            //import notes
            publishProgress(getString(R.string.string_getting_notes));
            ioHelper.restoreNote(true);

            //import birthdays
            if (isChecked) {
                publishProgress(getString(R.string.string_getting_birthdays));
                ioHelper.restoreBirthday(true);
            }

            DB.close();

            //getting Google Tasks
            GTasksHelper helper = new GTasksHelper(ctx);
            TaskLists lists = null;
            try {
                lists = helper.getTaskLists();
            } catch (IOException e) {
                e.printStackTrace();
            }

            TasksData data = new TasksData(ctx);
            data.open();

            if (lists != null && lists.size() > 0) {
                publishProgress(getString(R.string.string_getting_google_tasks));
                for (TaskList item : lists.getItems()) {
                    DateTime dateTime = item.getUpdated();
                    String listId = item.getId();
                    Cursor c = data.getTasksList(listId);
                    if (c != null && c.moveToFirst() && c.getCount() == 1) {
                        data.updateTasksList(c.getLong(c.getColumnIndex(TasksConstants.COLUMN_ID)),
                                item.getTitle(), listId, c.getInt(c.getColumnIndex(TasksConstants.COLUMN_DEFAULT)),
                                item.getEtag(), item.getKind(),
                                item.getSelfLink(), dateTime != null ? dateTime.getValue() : 0,
                                c.getInt(c.getColumnIndex(TasksConstants.COLUMN_COLOR)));
                    } else if (c != null && c.moveToFirst() && c.getCount() > 1) {
                        do {
                            data.deleteTasksList(c.getLong(c.getColumnIndex(TasksConstants.COLUMN_ID)));
                        } while (c.moveToNext());
                        Random r = new Random();
                        int color = r.nextInt(15);
                        data.addTasksList(item.getTitle(), listId, 0, item.getEtag(), item.getKind(),
                                item.getSelfLink(), dateTime != null ? dateTime.getValue() : 0, color);
                    } else {
                        Random r = new Random();
                        int color = r.nextInt(15);
                        data.addTasksList(item.getTitle(), listId, 0, item.getEtag(), item.getKind(),
                                item.getSelfLink(), dateTime != null ? dateTime.getValue() : 0, color);
                    }
                    if (c != null) c.close();

                    Cursor cc = data.getTasksLists();
                    if (cc != null && cc.moveToFirst()) {
                        data.setDefault(cc.getLong(cc.getColumnIndex(TasksConstants.COLUMN_ID)));
                        data.setSystemDefault(cc.getLong(cc.getColumnIndex(TasksConstants.COLUMN_ID)));
                    }
                    if (cc != null) cc.close();

                    List<Task> tasks = helper.getTasks(listId);
                    if (tasks != null && tasks.size() > 0) {
                        for (Task task : tasks) {
                            DateTime dueDate = task.getDue();
                            long due = dueDate != null ? dueDate.getValue() : 0;

                            DateTime completeDate = task.getCompleted();
                            long complete = completeDate != null ? completeDate.getValue() : 0;

                            DateTime updateDate = task.getUpdated();
                            long update = updateDate != null ? updateDate.getValue() : 0;

                            String taskId = task.getId();

                            boolean isDeleted = false;
                            try {
                                isDeleted = task.getDeleted();
                            } catch (NullPointerException e) {
                                e.printStackTrace();
                            }

                            boolean isHidden = false;
                            try {
                                isHidden = task.getHidden();
                            } catch (NullPointerException e) {
                                e.printStackTrace();
                            }

                            Cursor cursor = data.getTask(taskId);
                            if (cursor != null && cursor.moveToFirst() && cursor.getCount() > 1) {
                                do {
                                    data.deleteTask(cursor.getLong(cursor.getColumnIndex(TasksConstants.COLUMN_ID)));
                                } while (cursor.moveToNext());
                                data.addTask(task.getTitle(), taskId, complete, isDeleted, due,
                                        task.getEtag(), task.getKind(), task.getNotes(),
                                        task.getParent(), task.getPosition(), task.getSelfLink(), update, 0,
                                        listId, task.getStatus(), isHidden);
                            } else if (cursor != null && cursor.moveToFirst() && cursor.getCount() == 1) {
                                data.updateFullTask(cursor.getLong(cursor.getColumnIndex(TasksConstants.COLUMN_ID)),
                                        task.getTitle(), taskId, complete, isDeleted, due,
                                        task.getEtag(), task.getKind(), task.getNotes(),
                                        task.getParent(), task.getPosition(), task.getSelfLink(), update,
                                        cursor.getLong(cursor.getColumnIndex(TasksConstants.COLUMN_REMINDER_ID)),
                                        listId, task.getStatus(), isHidden);
                            } else {
                                data.addTask(task.getTitle(), taskId, complete, isDeleted, due,
                                        task.getEtag(), task.getKind(), task.getNotes(),
                                        task.getParent(), task.getPosition(), task.getSelfLink(), update, 0,
                                        listId, task.getStatus(), isHidden);
                            }
                            if (cursor != null) cursor.close();
                        }
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (checkBox.isChecked()) {
                sPrefs.saveBoolean(Prefs.CONTACT_BIRTHDAYS, true);
                sPrefs.saveBoolean(Prefs.CONTACTS_IMPORT_DIALOG, true);
                sPrefs.saveBoolean(Prefs.AUTO_CHECK_BIRTHDAYS, true);
                sPrefs.saveBoolean(Prefs.WIDGET_BIRTHDAYS, true);
                sPrefs.saveBoolean(Prefs.SYNC_BIRTHDAYS, true);
                if (new Permissions(mContext).checkPermission(Permissions.READ_CONTACTS)) {
                    new ImportBirthdays(LogInActivity.this).execute();
                } else {
                    new Permissions(mContext).requestPermission(LogInActivity.this,
                            new String[]{Permissions.READ_CONTACTS}, 102);
                }
            } else {
                sPrefs.saveBoolean(Prefs.CONTACT_BIRTHDAYS, false);
                sPrefs.saveBoolean(Prefs.CONTACTS_IMPORT_DIALOG, true);
                sPrefs.saveBoolean(Prefs.AUTO_CHECK_BIRTHDAYS, false);
                sPrefs.saveBoolean(Prefs.WIDGET_BIRTHDAYS, false);
                sPrefs.saveBoolean(Prefs.SYNC_BIRTHDAYS, false);
            }
            mProgress.setVisibility(View.INVISIBLE);
            mText.setText(getString(R.string.simple_done));
            startActivity(new Intent(LogInActivity.this, ScreenManager.class));
            new ScanTask(LogInActivity.this).execute();
            finish();
        }
    }

    public class ImportBirthdays extends AsyncTask<Void, Void, Void>{

        Context mContext;
        public final SimpleDateFormat[] birthdayFormats = {
                new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()),
                new SimpleDateFormat("yyyyMMdd", Locale.getDefault()),
                new SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()),
                new SimpleDateFormat("yy.MM.dd", Locale.getDefault()),
                new SimpleDateFormat("yy/MM/dd", Locale.getDefault()),
        };

        public ImportBirthdays (Context context){
            this.mContext = context;
        }

        @Override
        protected Void doInBackground(Void... params) {
            ContentResolver cr = getContentResolver(); //getContnetResolver()
            DataBase db = new DataBase(mContext);
            db.open();
            ArrayList<String> names = new ArrayList<>();
            ArrayList<Integer> ids = new ArrayList<>();
            if (db.getCountBirthdays() > 0){
                Cursor c = db.getBirthdays();
                if (c != null && c.moveToFirst()){
                    do {
                        int id = c.getInt(c.getColumnIndex(Constants.ContactConstants.COLUMN_CONTACT_ID));
                        String name = c.getString(c.getColumnIndex(Constants.ContactConstants.COLUMN_CONTACT_NAME));
                        ids.add(id);
                        names.add(name);
                    } while (c.moveToNext());
                    c.close();
                }
            }
            String[] projection = new String[] { ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME};

            Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, projection, null, null,
                    ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC");

            while (cur.moveToNext()) {
                String contactId = cur.getString(cur.getColumnIndex(ContactsContract.Data._ID));

                String columns[] = {
                        ContactsContract.CommonDataKinds.Event.START_DATE,
                        ContactsContract.CommonDataKinds.Event.TYPE,
                        ContactsContract.CommonDataKinds.Event.MIMETYPE,
                        ContactsContract.PhoneLookup.DISPLAY_NAME,
                        ContactsContract.Contacts._ID,
                };

                String where = ContactsContract.CommonDataKinds.Event.TYPE + "=" + ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY +
                        " and " + ContactsContract.CommonDataKinds.Event.MIMETYPE + " = '" + ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE +
                        "' and "                  + ContactsContract.Data.CONTACT_ID + " = " + contactId;

                String sortOrder = ContactsContract.Contacts.DISPLAY_NAME;
                Contacts cc = new Contacts(mContext);
                Cursor birthdayCur = cr.query(ContactsContract.Data.CONTENT_URI, columns, where, null, sortOrder);
                if (birthdayCur.getCount() > 0) {
                    while (birthdayCur.moveToNext()) {
                        // fix error;
                        Date date;
                        String birthday = birthdayCur.getString(birthdayCur.getColumnIndex(ContactsContract.CommonDataKinds.Event.START_DATE));
                        String name = birthdayCur.getString(birthdayCur.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
                        int id = birthdayCur.getInt(birthdayCur.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
                        String number = Contacts.getNumber(name, mContext);
                        String email = cc.getMail(id);
                        if (!names.contains(name) && !ids.contains(id)) {
                            Calendar calendar = Calendar.getInstance();
                            for (SimpleDateFormat f : birthdayFormats) {
                                try {
                                    date = f.parse(birthday);
                                    if (date != null) {
                                        calendar.setTime(date);
                                        int day = calendar.get(Calendar.DAY_OF_MONTH);
                                        int month = calendar.get(Calendar.MONTH);
                                        db.addBirthday(name, id, birthday, day, month, number,
                                                SyncHelper.generateID());
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
                birthdayCur.close();
            }

            cur.close();
            return null;
        }
    }

    @Override
    public void onBackPressed() {

    }
}
