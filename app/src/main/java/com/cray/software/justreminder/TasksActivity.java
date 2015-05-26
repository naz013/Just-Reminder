package com.cray.software.justreminder;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.cray.software.justreminder.adapters.TasksRecyclerAdapter;
import com.cray.software.justreminder.async.DelayedAsync;
import com.cray.software.justreminder.async.GetTasksListsAsync;
import com.cray.software.justreminder.async.TaskListAsync;
import com.cray.software.justreminder.cloud.GTasksHelper;
import com.cray.software.justreminder.databases.TasksData;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.datas.ListItems;
import com.cray.software.justreminder.interfaces.SyncListener;
import com.cray.software.justreminder.datas.TaskListData;
import com.cray.software.justreminder.interfaces.TasksConstants;
import com.cray.software.justreminder.widgets.UpdatesHelper;
import com.getbase.floatingactionbutton.AddFloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;
import com.google.api.client.googleapis.extensions.android.accounts.GoogleAccountManager;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.tasks.TasksScopes;

import java.io.IOException;
import java.util.ArrayList;

public class TasksActivity extends AppCompatActivity implements SyncListener, View.OnTouchListener {

    ColorSetter cSetter;
    SharedPrefs sPrefs = new SharedPrefs(TasksActivity.this);
    Toolbar toolbar;
    ListView mList;
    AddFloatingActionButton mFab;
    TasksData data = new TasksData(TasksActivity.this);
    ArrayList<TaskListData> taskListDatum;
    int currentPos;
    TasksRecyclerAdapter adapter;

    private static final int REQUEST_AUTHORIZATION = 1;
    private static final int REQUEST_ACCOUNT_PICKER = 3;
    String accountName;
    private Context ctx = this;
    private Activity a = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cSetter = new ColorSetter(TasksActivity.this);
        setTheme(cSetter.getStyle());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(cSetter.colorStatus());
        }
        setContentView(R.layout.tasks_activity_layout);
        setRequestedOrientation(cSetter.getRequestOrientation());

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        sPrefs = new SharedPrefs(TasksActivity.this);
        findViewById(R.id.windowBackground).setBackgroundColor(cSetter.getBackgroundStyle());

        mList = (ListView) findViewById(R.id.list);
        mList.setOnTouchListener(this);
        mList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        mList.setItemsCanFocus(true);
        mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startActivity(new Intent(TasksActivity.this, TaskManager.class)
                        .putExtra(Constants.ITEM_ID_INTENT, id)
                        .putExtra(TasksConstants.INTENT_ACTION, TasksConstants.EDIT));
            }
        });

        mFab = new AddFloatingActionButton(TasksActivity.this);
        mFab.setColorNormal(cSetter.colorSetter());
        mFab.setColorPressed(cSetter.colorChooser());
        mFab.setSize(FloatingActionButton.SIZE_NORMAL);

        RelativeLayout wrapper = (RelativeLayout) findViewById(R.id.wrapper);
        wrapper.addView(mFab);

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mFab.getLayoutParams();
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (new GTasksHelper(TasksActivity.this).isLinked()) {
                    if (currentPos == 0) {
                        long ids = 0;
                        startActivity(new Intent(TasksActivity.this, TaskManager.class)
                                .putExtra(Constants.ITEM_ID_INTENT, ids)
                                .putExtra(TasksConstants.INTENT_ACTION, TasksConstants.CREATE));
                    } else {
                        long idS = taskListDatum.get(currentPos).getId();
                        startActivity(new Intent(TasksActivity.this, TaskManager.class)
                                .putExtra(Constants.ITEM_ID_INTENT, idS)
                                .putExtra(TasksConstants.INTENT_ACTION, TasksConstants.CREATE));
                    }
                } else {
                    Toast.makeText(TasksActivity.this, getString(R.string.string_gtasks_warming),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        loadData();

        if(currentPos == 0) toolbar.setTitle(getString(R.string.string_all_tasks));

        if (!new GTasksHelper(TasksActivity.this).isLinked()){
            Intent intent = AccountPicker.newChooseAccountIntent(null, null,
                    new String[]{"com.google"}, false, null, null, null, null);
            startActivityForResult(intent, REQUEST_AUTHORIZATION);
        }
    }

    private void showDialog(){
        final CharSequence[] items = {getString(R.string.string_default),
                getString(R.string.sort_item_by_date_a_z),
                getString(R.string.sort_item_by_date_z_a),
                getString(R.string.string_active_first),
                getString(R.string.string_completed_first)};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.menu_order_by));
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                SharedPrefs prefs = new SharedPrefs(TasksActivity.this);
                if (item == 0) {
                    prefs.savePrefs(Constants.APP_UI_PREFERENCES_TASKS_ORDER, Constants.ORDER_DEFAULT);
                } else if (item == 1) {
                    prefs.savePrefs(Constants.APP_UI_PREFERENCES_TASKS_ORDER, Constants.ORDER_DATE_A_Z);
                } else if (item == 2) {
                    prefs.savePrefs(Constants.APP_UI_PREFERENCES_TASKS_ORDER, Constants.ORDER_DATE_Z_A);
                } else if (item == 3) {
                    prefs.savePrefs(Constants.APP_UI_PREFERENCES_TASKS_ORDER, Constants.ORDER_COMPLETED_Z_A);
                } else if (item == 4) {
                    prefs.savePrefs(Constants.APP_UI_PREFERENCES_TASKS_ORDER, Constants.ORDER_COMPLETED_A_Z);
                }
                dialog.dismiss();
                loadData();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void deleteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        String title = taskListDatum.get(currentPos).getTitle();
        builder.setTitle(getString(R.string.string_delete_task_list) + " " + title);
        builder.setMessage(getString(R.string.delete_task_list_question));
        builder.setNegativeButton(getString(R.string.import_dialog_button_no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setPositiveButton(getString(R.string.import_dialog_button_yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteList();
                dialog.dismiss();
                loadData();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void deleteList() {
        data.open();
        long id = taskListDatum.get(currentPos).getId();
        Cursor c = data.getTasksList(id);
        if (c != null && c.moveToFirst()){
            String listId = c.getString(c.getColumnIndex(TasksConstants.COLUMN_LIST_ID));
            int def = c.getInt(c.getColumnIndex(TasksConstants.COLUMN_DEFAULT));
            data.deleteTasksList(id);
            new TaskListAsync(TasksActivity.this, null, 0, 0, listId, TasksConstants.DELETE_TASK_LIST).execute();
            Cursor x = data.getTasks(listId);
            if (x != null && x.moveToFirst()){
                do {
                    data.deleteTask(x.getLong(x.getColumnIndex(TasksConstants.COLUMN_ID)));
                } while (x.moveToNext());
            }
            if (x != null) x.close();
            if (def == 1){
                Cursor cc = data.getTasksLists();
                if (cc != null && cc.moveToFirst()){
                    data.setDefault(cc.getLong(cc.getColumnIndex(TasksConstants.COLUMN_ID)));
                }
                if (cc != null) cc.close();
            }
        }
        if (c != null) c.close();
        if (data != null) data.close();
    }

    private void setList(final int position, final int i){
        if (position == 0){
            toolbar.setTitle(getString(R.string.string_all_tasks));
            toolbar.setBackgroundColor(cSetter.colorSetter());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setStatusBarColor(cSetter.colorStatus());
            }
            mFab.setColorNormal(cSetter.colorSetter());
            mFab.setColorPressed(cSetter.colorChooser());
        } else {
            toolbar.setTitle(taskListDatum.get(position).getTitle());
            int tmp = cSetter.getNoteColor(taskListDatum.get(position).getColor());
            toolbar.setBackgroundColor(getResources().getColor(tmp));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setStatusBarColor(cSetter.getNoteDarkColor(tmp));
            }
            mFab.setColorNormal(getResources().getColor(tmp));
            mFab.setColorPressed(cSetter.getNoteLightColor(tmp));
        }

        currentPos = position;
        sPrefs.saveInt(Constants.APP_UI_PREFERENCES_LAST_LIST, position);
        invalidateOptionsMenu();

        if (i != 0) {
            if (i > 0) {
                Animation slide = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_right_bounce_out);
                mList.startAnimation(slide);
                mList.setVisibility(View.GONE);
            } else {
                Animation slide = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_left_bounce_out);
                mList.startAnimation(slide);
                mList.setVisibility(View.GONE);
            }
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                loadList(taskListDatum.get(position).getListId(), i);
            }
        }, 300);
    }

    private void loadData() {
        taskListDatum = new ArrayList<>();
        taskListDatum.clear();
        taskListDatum.add(new TaskListData(getString(R.string.string_all_tasks), 0, Constants.TASKS_ALL, 25));
        data.open();
        Cursor c = data.getTasksLists();
        if (c != null && c.moveToFirst()){
            do {
                taskListDatum.add(new TaskListData(c.getString(c.getColumnIndex(TasksConstants.COLUMN_TITLE)),
                        c.getLong(c.getColumnIndex(TasksConstants.COLUMN_ID)),
                        c.getString(c.getColumnIndex(TasksConstants.COLUMN_LIST_ID)),
                        c.getInt(c.getColumnIndex(TasksConstants.COLUMN_COLOR))));
            } while (c.moveToNext());
        }
        if (c != null) c.close();
        if (data != null) data.close();

        int pos = sPrefs.loadInt(Constants.APP_UI_PREFERENCES_LAST_LIST);

        if (taskListDatum.size() > pos) setList(pos, 0);
        else setList(0, 0);
    }

    private void loadList(String id, int i){
        if (id == null){
            ArrayList<ListItems> mData = new ArrayList<>();
            mData.clear();
            data.open();
            Cursor c = data.getTasks();
            if (c != null && c.moveToFirst()){
                do {
                    String title = c.getString(c.getColumnIndex(TasksConstants.COLUMN_TITLE));
                    if (title != null && !title.matches("")) {
                        long date = c.getLong(c.getColumnIndex(TasksConstants.COLUMN_DUE));
                        String taskId = c.getString(c.getColumnIndex(TasksConstants.COLUMN_TASK_ID));
                        String listId = c.getString(c.getColumnIndex(TasksConstants.COLUMN_LIST_ID));
                        String checks = c.getString(c.getColumnIndex(TasksConstants.COLUMN_STATUS));
                        String note = c.getString(c.getColumnIndex(TasksConstants.COLUMN_NOTES));
                        long mId = c.getLong(c.getColumnIndex(TasksConstants.COLUMN_ID));
                        mData.add(new ListItems(title, mId, checks, taskId, date, listId, note));
                    }
                } while (c.moveToNext());
            }
            adapter = new TasksRecyclerAdapter(TasksActivity.this, mData, this);
            mList.setAdapter(adapter);
            if (i != 0) {
                if (i > 0) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Animation slide = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_right_bounce);
                            mList.startAnimation(slide);
                            mList.setVisibility(View.VISIBLE);
                        }
                    }, 320);
                } else {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Animation slide = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_left_bounce);
                            mList.startAnimation(slide);
                            mList.setVisibility(View.VISIBLE);
                        }
                    }, 320);
                }
            }
            if (c != null) c.close();
            if (data != null) data.close();
        }
        if (id.matches(Constants.TASKS_ALL)){
            ArrayList<ListItems> mData = new ArrayList<>();
            mData.clear();
            data.open();
            Cursor c = data.getTasks();
            if (c != null && c.moveToFirst()){
                do {
                    String title = c.getString(c.getColumnIndex(TasksConstants.COLUMN_TITLE));
                    if (title != null && !title.matches("")) {
                        long date = c.getLong(c.getColumnIndex(TasksConstants.COLUMN_DUE));
                        String taskId = c.getString(c.getColumnIndex(TasksConstants.COLUMN_TASK_ID));
                        String listId = c.getString(c.getColumnIndex(TasksConstants.COLUMN_LIST_ID));
                        String checks = c.getString(c.getColumnIndex(TasksConstants.COLUMN_STATUS));
                        String note = c.getString(c.getColumnIndex(TasksConstants.COLUMN_NOTES));
                        long mId = c.getLong(c.getColumnIndex(TasksConstants.COLUMN_ID));
                        mData.add(new ListItems(title, mId, checks, taskId, date, listId, note));
                    }
                } while (c.moveToNext());
            }
            adapter = new TasksRecyclerAdapter(TasksActivity.this, mData, this);
            mList.setAdapter(adapter);
            if (i != 0) {
                if (i > 0) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Animation slide = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_right_bounce);
                            mList.startAnimation(slide);
                            mList.setVisibility(View.VISIBLE);
                        }
                    }, 320);
                } else {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Animation slide = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_left_bounce);
                            mList.startAnimation(slide);
                            mList.setVisibility(View.VISIBLE);
                        }
                    }, 320);
                }
            }
            if (c != null) c.close();
            if (data != null) data.close();
        } else {
            ArrayList<ListItems> mData = new ArrayList<>();
            mData.clear();
            data.open();
            Cursor c = data.getTasks(id);
            if (c != null && c.moveToFirst()){
                do {
                    String title = c.getString(c.getColumnIndex(TasksConstants.COLUMN_TITLE));
                    if (title != null && !title.matches("")) {
                        long date = c.getLong(c.getColumnIndex(TasksConstants.COLUMN_DUE));
                        String taskId = c.getString(c.getColumnIndex(TasksConstants.COLUMN_TASK_ID));
                        String listId = c.getString(c.getColumnIndex(TasksConstants.COLUMN_LIST_ID));
                        String checks = c.getString(c.getColumnIndex(TasksConstants.COLUMN_STATUS));
                        String note = c.getString(c.getColumnIndex(TasksConstants.COLUMN_NOTES));
                        long mId = c.getLong(c.getColumnIndex(TasksConstants.COLUMN_ID));
                        mData.add(new ListItems(title, mId, checks, taskId, date, listId, note));
                    }
                } while (c.moveToNext());
            }

            adapter = new TasksRecyclerAdapter(TasksActivity.this, mData, this);
            mList.setAdapter(adapter);
            if (c != null) c.close();
            if (data != null) data.close();
            if (i != 0) {
                if (i > 0) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Animation slide = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_right_bounce);
                            mList.startAnimation(slide);
                            mList.setVisibility(View.VISIBLE);
                        }
                    }, 320);
                } else {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Animation slide = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_left_bounce);
                            mList.startAnimation(slide);
                            mList.setVisibility(View.VISIBLE);
                        }
                    }, 320);
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
        new UpdatesHelper(TasksActivity.this).updateTasksWidget();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    public static final int MENU_ITEM_EDIT = 12;
    public static final int MENU_ITEM_DELETE = 13;
    public static final int MENU_ITEM_CLEAR = 14;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean isLinked = new GTasksHelper(TasksActivity.this).isLinked();
        if (isLinked) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.tasks_menu, menu);
            data.open();
            if (currentPos != 0) {
                menu.add(Menu.NONE, MENU_ITEM_EDIT, 100, getString(R.string.string_edit_task_list));
                Cursor c = data.getTasksList(taskListDatum.get(currentPos).getListId());
                if (c != null && c.moveToFirst()) {
                    int def = c.getInt(c.getColumnIndex(TasksConstants.SYSTEM_DEFAULT));
                    if (def != 1)
                        menu.add(Menu.NONE, MENU_ITEM_DELETE, 100, getString(R.string.string_delete_task_list));
                }
                if (c != null) c.close();
                menu.add(Menu.NONE, MENU_ITEM_CLEAR, 100, getString(R.string.string_delete_completed_tasks));
            }
            if (data != null) data.close();
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_sync:
                new DelayedAsync(TasksActivity.this, this).execute();
                return true;
            case R.id.action_add_list:
                startActivity(new Intent(TasksActivity.this, TaskListManager.class));
                return true;
            case MENU_ITEM_EDIT:
                if (currentPos != 0){
                    startActivity(new Intent(TasksActivity.this, TaskListManager.class)
                            .putExtra(Constants.ITEM_ID_INTENT, taskListDatum.get(currentPos).getId()));
                }
                return true;
            case MENU_ITEM_DELETE:
                deleteDialog();
                return true;
            case MENU_ITEM_CLEAR:
                clearList();
                return true;
            case R.id.action_order:
                showDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void clearList() {
        data.open();
        String listId = taskListDatum.get(currentPos).getListId();
        Cursor c = data.getTasks(listId);
        if (c != null && c.moveToFirst()){
            do {
                long ids = c.getLong(c.getColumnIndex(TasksConstants.COLUMN_ID));
                String status = c.getString(c.getColumnIndex(TasksConstants.COLUMN_STATUS));
                if (status.matches(Constants.TASKS_COMPLETE)){
                    data.deleteTask(ids);
                }
            } while (c.moveToNext());
        }

        if (c != null) c.close();
        if (data != null) data.close();
        new TaskListAsync(TasksActivity.this, null, 0, 0, listId, TasksConstants.CLEAR_TASK_LIST).execute();

        loadData();
    }

    @Override
    public void endExecution(boolean result) {
        loadData();
        invalidateOptionsMenu();
    }

    static final int MIN_DISTANCE = 120;
    private float downX;
    private float downY;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch(event.getAction()){
            case MotionEvent.ACTION_DOWN: {
                downX = event.getX();
                downY = event.getY();
                return true;
            }
            case MotionEvent.ACTION_UP: {
                float upX = event.getX();
                float upY = event.getY();

                float deltaX = downX - upX;
                float deltaY = downY - upY;
                if(Math.abs(deltaX) > Math.abs(deltaY)) {
                    if (Math.abs(deltaX) > MIN_DISTANCE) {
                        if (deltaX < 0) {
                            this.onRightSwipe();
                            return true;
                        }
                        if (deltaX > 0) {
                            this.onLeftSwipe();
                            return true;
                        }
                    } else {
                        return false; // We don't consume the event
                    }
                }

                return true;
            }
        }
        return false;
    }

    private void onLeftSwipe() {
        switchIt(-1);
    }

    private void switchIt(int i) {
        int length = taskListDatum.size();
        if (length > 1){
            if (i < 0){
                if (currentPos == 0) {
                    setList(length - 1, i);
                } else {
                    setList(currentPos - 1, i);
                }
            } else {
                if (currentPos == length - 1){
                    setList(0, i);
                } else {
                    setList(currentPos + 1, i);
                }
            }
        }
    }

    private void onRightSwipe() {
        switchIt(1);
    }

    void getAndUseAuthTokenInAsyncTask(Account account) {
        AsyncTask<Account, String, String> task = new AsyncTask<Account, String, String>() {
            ProgressDialog progressDlg;
            AsyncTask<Account, String, String> me = this;

            @Override
            protected void onPreExecute() {
                progressDlg = new ProgressDialog(TasksActivity.this, ProgressDialog.STYLE_SPINNER);
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
            sPrefs.savePrefs(Constants.APP_UI_PREFERENCES_DRIVE_USER, new SyncHelper(TasksActivity.this).encrypt(accountName));
            new GetTasksListsAsync(TasksActivity.this, this).execute();
        } else if (requestCode == REQUEST_ACCOUNT_PICKER && resultCode == RESULT_OK) {
            accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
            sPrefs.savePrefs(Constants.APP_UI_PREFERENCES_DRIVE_USER, new SyncHelper(TasksActivity.this).encrypt(accountName));
            new GetTasksListsAsync(TasksActivity.this, this).execute();
        }
    }
}
