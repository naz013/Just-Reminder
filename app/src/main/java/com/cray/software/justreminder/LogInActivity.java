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

package com.cray.software.justreminder;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.cray.software.justreminder.async.CloudLoginSynchronization;
import com.cray.software.justreminder.async.LocalLoginSynchronization;
import com.cray.software.justreminder.birthdays.BirthdayHelper;
import com.cray.software.justreminder.birthdays.BirthdayItem;
import com.cray.software.justreminder.cloud.Dropbox;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.groups.GroupHelper;
import com.cray.software.justreminder.groups.GroupItem;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.Permissions;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.interfaces.LoginListener;
import com.cray.software.justreminder.helpers.Module;
import com.cray.software.justreminder.reminder.ReminderHelper;
import com.cray.software.justreminder.reminder.ReminderItem;
import com.cray.software.justreminder.roboto_views.RoboCheckBox;
import com.cray.software.justreminder.roboto_views.RoboTextView;
import com.cray.software.justreminder.utils.LocationUtil;
import com.cray.software.justreminder.utils.SuperUtil;
import com.cray.software.justreminder.views.PaperButton;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;
import com.google.api.client.googleapis.extensions.android.accounts.GoogleAccountManager;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.tasks.TasksScopes;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LogInActivity extends Activity implements LoginListener {

    private ColorSetter cs = ColorSetter.getInstance(LogInActivity.this);
    private PaperButton connectGDrive, connectDropbox;
    private RoboCheckBox checkBox;
    private RoboTextView skipButton;

    private Dropbox dbx;
    private static final int REQUEST_AUTHORIZATION = 1;
    private static final int REQUEST_ACCOUNT_PICKER = 3;

    public static final String MARKET_APP_JUSTREMINDER = "com.cray.software.justreminder";
    public static final String MARKET_APP_JUSTREMINDER_PRO = "com.cray.software.justreminderpro";

    private String accountName;
    private Context ctx = this;
    private Activity a = this;

    private boolean enabled = true;

    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(cs.getFullscreenStyle());
        setContentView(R.layout.activity_log_in);
        if (Module.isLollipop()) {
            getWindow().setStatusBarColor(cs.getColor(cs.colorPrimaryDark()));
        }
        setRequestedOrientation(cs.getRequestOrientation());
        findViewById(R.id.windowBackground).setBackgroundColor(cs.getBackgroundStyle());
        dbx = new Dropbox(LogInActivity.this);

        connectGDrive = (PaperButton) findViewById(R.id.connectGDrive);
        connectDropbox = (PaperButton) findViewById(R.id.connectDropbox);
        checkBox = (RoboCheckBox) findViewById(R.id.checkBox);
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!Permissions.checkPermission(LogInActivity.this, Permissions.READ_CONTACTS, Permissions.READ_CALLS)) {
                Permissions.requestPermission(LogInActivity.this, 115, Permissions.READ_CONTACTS, Permissions.READ_CALLS);
            } else {
                checkBox.setChecked(isChecked);
            }
        });
        skipButton = (RoboTextView) findViewById(R.id.skipButton);
        String text = skipButton.getText().toString();
        skipButton.setText(SuperUtil.appendString(text, " (", getString(R.string.local_sync), ")"));

        connectDropbox.setOnClickListener(v -> {
            if (enabled) {
                boolean isIn = SuperUtil.isAppInstalled(LogInActivity.this, Module.isPro() ?
                        MARKET_APP_JUSTREMINDER : MARKET_APP_JUSTREMINDER_PRO);
                if (isIn) {
                    checkDialog().show();
                } else {
                    dbx.startLink();
                    enabled = false;
                }
            }
        });

        connectGDrive.setOnClickListener(v -> {
            if (enabled) {
                if (!LocationUtil.checkGooglePlayServicesAvailability(this)) {
                    Toast.makeText(this, R.string.google_play_services_not_installed, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (Permissions.checkPermission(LogInActivity.this, Permissions.GET_ACCOUNTS,
                        Permissions.READ_EXTERNAL, Permissions.WRITE_EXTERNAL, Permissions.ACCESS_FINE_LOCATION)) {
                    Intent intent = AccountPicker.newChooseAccountIntent(null, null,
                            new String[]{"com.google"}, false, null, null, null, null);
                    startActivityForResult(intent, REQUEST_AUTHORIZATION);
                    enabled = false;
                } else {
                    Permissions.requestPermission(LogInActivity.this, 103,
                            Permissions.GET_ACCOUNTS, Permissions.READ_EXTERNAL,
                            Permissions.WRITE_EXTERNAL, Permissions.ACCESS_FINE_LOCATION);
                }
            }
        });

        skipButton.setOnClickListener(v -> {
            if (enabled) {
                if (Permissions.checkPermission(LogInActivity.this, Permissions.READ_EXTERNAL,
                        Permissions.ACCESS_FINE_LOCATION)) {
                    new LocalLoginSynchronization(LogInActivity.this, checkBox.isChecked(), LogInActivity.this).execute();
                    enabled = false;
                } else {
                    Permissions.requestPermission(LogInActivity.this, 101,
                            Permissions.READ_EXTERNAL, Permissions.WRITE_EXTERNAL,
                            Permissions.ACCESS_FINE_LOCATION);
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 101:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    new LocalLoginSynchronization(LogInActivity.this, checkBox.isChecked(), LogInActivity.this).execute();
                    enabled = false;
                } else {
                    checkGroups();
                    startActivity(new Intent(LogInActivity.this, StartActivity.class));
                    finish();
                }
                break;
            case 102:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    new ImportBirthdays(LogInActivity.this).execute();
                    enabled = false;
                } else {
                    checkGroups();
                    startActivity(new Intent(LogInActivity.this, StartActivity.class));
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
                    checkGroups();
                    startActivity(new Intent(LogInActivity.this, StartActivity.class));
                    finish();
                }
                break;
            case 104:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    new CloudLoginSynchronization(LogInActivity.this, checkBox.isChecked(), this).execute();
                    enabled = false;
                } else {
                    checkGroups();
                    startActivity(new Intent(LogInActivity.this, StartActivity.class));
                    finish();
                }
                break;
            case 115:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    checkBox.setChecked(true);
                } else {
                    checkBox.setChecked(false);
                }
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        dbx.startSession();
        if (!dbx.isLinked()) {
            if (dbx.checkLink()) {
                setButtonStatus(false);
                SharedPrefs.getInstance(this).putBoolean(Prefs.AUTO_BACKUP, true);
                if (Permissions.checkPermission(LogInActivity.this, Permissions.READ_EXTERNAL,
                        Permissions.WRITE_EXTERNAL, Permissions.ACCESS_FINE_LOCATION)) {
                    new CloudLoginSynchronization(LogInActivity.this, checkBox.isChecked(), this).execute();
                    enabled = false;
                } else {
                    Permissions.requestPermission(LogInActivity.this, 104,
                            Permissions.READ_EXTERNAL, Permissions.WRITE_EXTERNAL,
                            Permissions.ACCESS_FINE_LOCATION);
                }
            }
        } else {
            setButtonStatus(false);
        }
    }

    protected Dialog checkDialog() {
        return new AlertDialog.Builder(this)
                .setTitle(R.string.other_version_detected)
                .setPositiveButton(R.string.open, (dialog1, which) -> {
                    Intent i;
                    PackageManager manager = getPackageManager();
                    if (Module.isPro()) {
                        i = manager.getLaunchIntentForPackage(MARKET_APP_JUSTREMINDER);
                    } else {
                        i = manager.getLaunchIntentForPackage(MARKET_APP_JUSTREMINDER_PRO);
                    }
                    i.addCategory(Intent.CATEGORY_LAUNCHER);
                    startActivity(i);
                })
                .setNegativeButton(getString(R.string.delete), (dialog1, which) -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    if (Module.isPro()) {
                        intent.setData(Uri.parse("package:" + MARKET_APP_JUSTREMINDER));
                    } else {
                        intent.setData(Uri.parse("package:" + MARKET_APP_JUSTREMINDER_PRO));
                    }
                    startActivity(intent);
                })
                .setNeutralButton(getString(R.string.cancel), (dialog1, which) -> {
                    dialog1.dismiss();
                })
                .setCancelable(true)
                .create();
    }

    void getAndUseAuthTokenInAsyncTask(Account account) {
        AsyncTask<Account, String, String> task = new AsyncTask<Account, String, String>() {
            @Override
            protected void onPreExecute() {
                dialog = ProgressDialog.show(LogInActivity.this, getString(R.string.please_wait),
                        getString(R.string.trying_to_log_in), true, false);
            }

            @Override
            protected String doInBackground(Account... params) {
                return getAccessToken(params[0]);
            }

            @Override
            protected void onPostExecute(String s) {
                if (dialog != null && dialog.isShowing()) dialog.dismiss();
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
            SharedPrefs.getInstance(this).putString(Prefs.DRIVE_USER, SyncHelper.encrypt(accountName));
            setButtonStatus(false);
            SharedPrefs.getInstance(this).putBoolean(Prefs.AUTO_BACKUP, true);
            if (Permissions.checkPermission(LogInActivity.this,
                    Permissions.READ_EXTERNAL,
                    Permissions.WRITE_EXTERNAL)) {
                new CloudLoginSynchronization(LogInActivity.this, checkBox.isChecked(), this).execute();
            } else {
                Permissions.requestPermission(LogInActivity.this, 104,
                        Permissions.READ_EXTERNAL, Permissions.WRITE_EXTERNAL);
            }
        } else if (requestCode == REQUEST_ACCOUNT_PICKER && resultCode == RESULT_OK) {
            accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
            SharedPrefs.getInstance(this).putString(Prefs.DRIVE_USER, SyncHelper.encrypt(accountName));
            setButtonStatus(false);
            SharedPrefs.getInstance(this).putBoolean(Prefs.AUTO_BACKUP, true);
            if (Permissions.checkPermission(LogInActivity.this,
                    Permissions.READ_EXTERNAL,
                    Permissions.WRITE_EXTERNAL)) {
                new CloudLoginSynchronization(LogInActivity.this, checkBox.isChecked(), this).execute();
            } else {
                Permissions.requestPermission(LogInActivity.this, 104,
                        Permissions.READ_EXTERNAL, Permissions.WRITE_EXTERNAL);
            }
        }
    }

    private void setButtonStatus(boolean b) {
        connectDropbox.setEnabled(b);
        connectGDrive.setEnabled(b);
        skipButton.setEnabled(b);
    }

    @Override
    public void onLocal() {
        SharedPrefs sPrefs = SharedPrefs.getInstance(this);
        if (checkBox.isChecked()) {
            sPrefs.putBoolean(Prefs.CONTACT_BIRTHDAYS, true);
            sPrefs.putBoolean(Prefs.CONTACTS_IMPORT_DIALOG, true);
            sPrefs.putBoolean(Prefs.AUTO_CHECK_BIRTHDAYS, true);
            sPrefs.putBoolean(Prefs.WIDGET_BIRTHDAYS, true);
            sPrefs.putBoolean(Prefs.SYNC_BIRTHDAYS, true);
            if (Permissions.checkPermission(LogInActivity.this, Permissions.READ_CONTACTS)) {
                new ImportBirthdays(LogInActivity.this).execute();
            } else {
                Permissions.requestPermission(LogInActivity.this, 102,
                        Permissions.READ_CONTACTS);
            }
        } else {
            sPrefs.putBoolean(Prefs.CONTACT_BIRTHDAYS, false);
            sPrefs.putBoolean(Prefs.CONTACTS_IMPORT_DIALOG, true);
            sPrefs.putBoolean(Prefs.AUTO_CHECK_BIRTHDAYS, false);
            sPrefs.putBoolean(Prefs.WIDGET_BIRTHDAYS, false);
            sPrefs.putBoolean(Prefs.SYNC_BIRTHDAYS, false);
        }
        startActivity(new Intent(LogInActivity.this, StartActivity.class));
        finish();
    }

    @Override
    public void onCloud() {
        SharedPrefs sPrefs = SharedPrefs.getInstance(this);
        if (checkBox.isChecked()) {
            sPrefs.putBoolean(Prefs.CONTACT_BIRTHDAYS, true);
            sPrefs.putBoolean(Prefs.CONTACTS_IMPORT_DIALOG, true);
            sPrefs.putBoolean(Prefs.AUTO_CHECK_BIRTHDAYS, true);
            sPrefs.putBoolean(Prefs.WIDGET_BIRTHDAYS, true);
            sPrefs.putBoolean(Prefs.SYNC_BIRTHDAYS, true);
            if (Permissions.checkPermission(LogInActivity.this, Permissions.READ_CONTACTS)) {
                new ImportBirthdays(LogInActivity.this).execute();
            } else {
                Permissions.requestPermission(LogInActivity.this, 102,
                        Permissions.READ_CONTACTS);
            }
        } else {
            sPrefs.putBoolean(Prefs.CONTACT_BIRTHDAYS, false);
            sPrefs.putBoolean(Prefs.CONTACTS_IMPORT_DIALOG, true);
            sPrefs.putBoolean(Prefs.AUTO_CHECK_BIRTHDAYS, false);
            sPrefs.putBoolean(Prefs.WIDGET_BIRTHDAYS, false);
            sPrefs.putBoolean(Prefs.SYNC_BIRTHDAYS, false);
        }
        startActivity(new Intent(this, StartActivity.class));
        finish();
    }

    private void checkGroups() {
        GroupHelper helper = GroupHelper.getInstance(this);
        if (helper.getAll().size() == 0){
            long time = System.currentTimeMillis();
            String defUiID = SyncHelper.generateID();
            helper.saveGroup(new GroupItem("General", defUiID, 5, 0, time));
            helper.saveGroup(new GroupItem("Work", SyncHelper.generateID(), 3, 0, time));
            helper.saveGroup(new GroupItem("Personal", SyncHelper.generateID(), 0, 0, time));
            List<ReminderItem> items = ReminderHelper.getInstance(this).getAll();
            for (ReminderItem item : items) {
                item.setGroupId(defUiID);
            }
            ReminderHelper.getInstance(this).saveReminders(items);
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
            ContentResolver cr = getContentResolver();
            List<String> names = BirthdayHelper.getInstance(mContext).getNames();
            List<Integer> ids = BirthdayHelper.getInstance(mContext).getContacts();
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
                Cursor birthdayCur = cr.query(ContactsContract.Data.CONTENT_URI, columns, where, null, sortOrder);
                if (birthdayCur.getCount() > 0) {
                    while (birthdayCur.moveToNext()) {
                        Date date;
                        String birthday = birthdayCur.getString(birthdayCur.getColumnIndex(ContactsContract.CommonDataKinds.Event.START_DATE));
                        String name = birthdayCur.getString(birthdayCur.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
                        int id = birthdayCur.getInt(birthdayCur.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
                        String number = com.cray.software.justreminder.contacts.Contacts.getNumber(name, mContext);
                        if (!names.contains(name) && !ids.contains(id)) {
                            Calendar calendar = Calendar.getInstance();
                            for (SimpleDateFormat f : birthdayFormats) {
                                try {
                                    date = f.parse(birthday);
                                    if (date != null) {
                                        calendar.setTime(date);
                                        int day = calendar.get(Calendar.DAY_OF_MONTH);
                                        int month = calendar.get(Calendar.MONTH);
                                        BirthdayItem item = new BirthdayItem(0, name, birthday, number, SyncHelper.generateID(), null, id, day, month);
                                        BirthdayHelper.getInstance(mContext).saveBirthday(item);
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
