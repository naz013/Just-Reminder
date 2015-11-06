package com.cray.software.justreminder;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CursorAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import com.cray.software.justreminder.async.TaskAsync;
import com.cray.software.justreminder.cloud.GTasksHelper;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.databases.TasksData;
import com.cray.software.justreminder.datas.CategoryModel;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.Messages;
import com.cray.software.justreminder.helpers.Notifier;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.interfaces.Prefs;
import com.cray.software.justreminder.interfaces.TasksConstants;
import com.cray.software.justreminder.services.AlarmReceiver;
import com.cray.software.justreminder.utils.AssetsUtil;
import com.cray.software.justreminder.utils.TimeUtil;
import com.cray.software.justreminder.utils.ViewUtils;
import com.cray.software.justreminder.widgets.UpdatesHelper;

import java.util.ArrayList;
import java.util.Calendar;

public class TaskManager extends AppCompatActivity {
    private ColorSetter cSetter = new ColorSetter(TaskManager.this);
    private SharedPrefs sPrefs = new SharedPrefs(TaskManager.this);

    private long id;
    private Toolbar toolbar;
    private EditText editField, noteField;
    private TextView dateField;
    private TextView timeField;
    private TextView listText;
    private LinearLayout dueContainer;
    private LinearLayout reminderContainer;
    private CheckBox reminderCheck, dueCheck;

    private int color;
    private int myHour = 0;
    private int myMinute = 0;
    private int myYear = 0;
    private int myMonth = 0;
    private int myDay = 1;
    private String listId = null;
    private String initListId = null;
    private String taskId;
    private String action;

    private TasksData data = new TasksData(TaskManager.this);

    private static final int MENU_ITEM_DELETE = 12;
    private static final int MENU_ITEM_MOVE = 14;

    private ArrayList<CategoryModel> categories = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cSetter = new ColorSetter(TaskManager.this);
        setTheme(cSetter.getStyle());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(cSetter.colorStatus());
        }
        setContentView(R.layout.task_manager_layout);
        setRequestedOrientation(cSetter.getRequestOrientation());

        sPrefs = new SharedPrefs(TaskManager.this);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_clear_white_24dp);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        editField = (EditText) findViewById(R.id.editField);
        noteField = (EditText) findViewById(R.id.noteField);
        listText = (TextView) findViewById(R.id.listText);
        listText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectList(false);
            }
        });
        reminderCheck = (CheckBox) findViewById(R.id.reminderCheck);
        reminderCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) ViewUtils.expand(reminderContainer);
                else ViewUtils.collapse(reminderContainer);
            }
        });
        dueCheck = (CheckBox) findViewById(R.id.dueCheck);
        dueCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) ViewUtils.expand(dueContainer);
                else ViewUtils.collapse(dueContainer);
            }
        });

        findViewById(R.id.windowBackground).setBackgroundColor(cSetter.getBackgroundStyle());

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        myHour = calendar.get(Calendar.HOUR_OF_DAY);
        myMinute = calendar.get(Calendar.MINUTE);
        myYear = calendar.get(Calendar.YEAR);
        myMonth = calendar.get(Calendar.MONTH);
        myDay = calendar.get(Calendar.DAY_OF_MONTH);

        reminderContainer = (LinearLayout) findViewById(R.id.reminderContainer);
        reminderContainer.setVisibility(View.GONE);

        dueContainer = (LinearLayout) findViewById(R.id.dueContainer);
        dueContainer.setVisibility(View.GONE);

        dateField = (TextView) findViewById(R.id.dateField);
        dateField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateDialog().show();
            }
        });

        dateField.setText(TimeUtil.getDate(calendar.getTime()));
        dateField.setTypeface(AssetsUtil.getMediumTypeface(this));

        timeField = (TextView) findViewById(R.id.timeField);
        timeField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timeDialog().show();
            }
        });
        timeField.setTypeface(AssetsUtil.getMediumTypeface(this));

        timeField.setText(TimeUtil.getTime(calendar.getTime(),
                sPrefs.loadBoolean(Prefs.IS_24_TIME_FORMAT)));

        Intent intent = getIntent();
        long tmp = intent.getLongExtra(Constants.ITEM_ID_INTENT, 0);
        action = intent.getStringExtra(TasksConstants.INTENT_ACTION);

        if (action == null) action = TasksConstants.CREATE;

        if (action.matches(TasksConstants.CREATE)){
            toolbar.setTitle(getString(R.string.string_add_task));
            if (tmp == 0) {
                data.open();
                Cursor c = data.getDefaultTasksList();
                if (c != null && c.moveToFirst()) {
                    initListId = c.getString(c.getColumnIndex(TasksConstants.COLUMN_LIST_ID));
                    color = c.getInt(c.getColumnIndex(TasksConstants.COLUMN_COLOR));
                    String listTitle = c.getString(c.getColumnIndex(TasksConstants.COLUMN_TITLE));
                    listText.setText(listTitle);
                    setColor(color);
                }
                if (c != null) c.close();
            } else {
                data.open();
                Cursor c = data.getTasksList(tmp);
                if (c != null && c.moveToFirst()) {
                    initListId = c.getString(c.getColumnIndex(TasksConstants.COLUMN_LIST_ID));
                    color = c.getInt(c.getColumnIndex(TasksConstants.COLUMN_COLOR));
                    String listTitle = c.getString(c.getColumnIndex(TasksConstants.COLUMN_TITLE));
                    listText.setText(listTitle);
                    setColor(color);
                }
                if (c != null) c.close();
            }
        } else {
            toolbar.setTitle(getString(R.string.string_edit_task));
            id = tmp;
            if (id != 0) {
                data.open();
                Cursor c = data.getTask(id);
                if (c != null && c.moveToFirst()) {
                    editField.setText(c.getString(c.getColumnIndex(TasksConstants.COLUMN_TITLE)));
                    taskId = c.getString(c.getColumnIndex(TasksConstants.COLUMN_TASK_ID));
                    String note = c.getString(c.getColumnIndex(TasksConstants.COLUMN_NOTES));
                    if (note != null) {
                        noteField.setText(note);
                        noteField.setSelection(noteField.getText().length());
                    }

                    long time = c.getLong(c.getColumnIndex(TasksConstants.COLUMN_DUE));
                    if (time != 0) {
                        calendar.setTimeInMillis(time);
                        myHour = calendar.get(Calendar.HOUR_OF_DAY);
                        myMinute = calendar.get(Calendar.MINUTE);
                        myYear = calendar.get(Calendar.YEAR);
                        myMonth = calendar.get(Calendar.MONTH);
                        myDay = calendar.get(Calendar.DAY_OF_MONTH);

                        dateField.setText(TimeUtil.getDate(calendar.getTime()));

                        dueCheck.setChecked(true);
                    }

                    initListId = c.getString(c.getColumnIndex(TasksConstants.COLUMN_LIST_ID));
                    Cursor x = data.getTasksList(initListId);
                    if (x != null && x.moveToFirst()) {
                        color = x.getInt(x.getColumnIndex(TasksConstants.COLUMN_COLOR));
                        String listTitle = x.getString(x.getColumnIndex(TasksConstants.COLUMN_TITLE));
                        listText.setText(listTitle);
                        setColor(color);
                    }
                    if (x != null) x.close();
                }
                if (c != null) c.close();
            }
        }
    }

    private void moveTask(String listId) {
        if (!listId.matches(initListId)) {
            data.open();
            data.updateTask(id, listId);
            new TaskAsync(TaskManager.this, null, listId, taskId, TasksConstants.MOVE_TASK,
                    0, null, id).execute();
            data.close();
            finish();
        } else {
            Messages.toast(this, getString(R.string.same_list_warming));
        }
    }

    private void selectList(final boolean move) {
        Cursor c = data.getTasksLists();
        if (c != null && c.moveToFirst()){
            do {
                String listTitle = c.getString(c.getColumnIndex(TasksConstants.COLUMN_TITLE));
                String listId = c.getString(c.getColumnIndex(TasksConstants.COLUMN_LIST_ID));
                categories.add(new CategoryModel(listTitle, listId));
            } while (c.moveToNext());
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.select_list));
        builder.setAdapter(new SimpleAdapter(TaskManager.this,
                data.getTasksLists()), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if (move) moveTask(categories.get(which).getUuID());
                else {
                    listText.setText(categories.get(which).getTitle());
                    listId = categories.get(which).getUuID();
                    reloadColor(listId);
                }
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void reloadColor(String listId) {
        TasksData db = new TasksData(this);
        db.open();
        Cursor x = db.getTasksList(listId);
        if (x != null && x.moveToFirst()) {
            color = x.getInt(x.getColumnIndex(TasksConstants.COLUMN_COLOR));
            setColor(color);
        }
        if (x != null) x.close();
        db.close();
    }

    private void saveTask() {
        sPrefs = new SharedPrefs(this);
        sPrefs.saveBoolean(Prefs.TASK_CHANGED, true);
        String taskName = editField.getText().toString().trim();
        if (taskName.matches("")) {
            editField.setError(getString(R.string.empty_field_error));
            return;
        }

        String note = noteField.getText().toString().trim();

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(myYear, myMonth, myDay, 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        long due = 0;
        if (dueCheck.isChecked()) due = calendar.getTimeInMillis();
        long remId = 0;
        if (reminderCheck.isChecked()) remId = saveReminder(taskName);
        data.open();
        if (action.matches(TasksConstants.CREATE)){
            long localId = data.addTask(taskName, null, 0, false, due, null, null, note,
                    null, null, null, 0, remId, listId != null ? listId : initListId, GTasksHelper.TASKS_NEED_ACTION, false);
            new TaskAsync(TaskManager.this, taskName, listId != null ? listId : initListId, null, TasksConstants.INSERT_TASK,
                    due, note, localId).execute();
        }
        if (action.matches(TasksConstants.EDIT)) {
            if (id != 0) {
                if (listId != null){
                    data.updateTask(id, taskName, due, note, GTasksHelper.TASKS_NEED_ACTION, remId, listId);
                    new TaskAsync(TaskManager.this, taskName, initListId, taskId, TasksConstants.UPDATE_TASK,
                            due, note, id).execute();
                    new TaskAsync(TaskManager.this, taskName, listId, taskId, TasksConstants.MOVE_TASK,
                            due, note, id).execute();
                } else {
                    data.updateTask(id, taskName, due, note, GTasksHelper.TASKS_NEED_ACTION, remId);
                    new TaskAsync(TaskManager.this, taskName, initListId, taskId, TasksConstants.UPDATE_TASK,
                            due, note, id).execute();
                }
            }
        }
        if (data != null) data.close();
        finish();
    }

    private long saveReminder(String task){
        String type = Constants.TYPE_REMINDER;
        DataBase DB = new DataBase(TaskManager.this);
        DB.open();
        String uuID = SyncHelper.generateID();
        Cursor cf = DB.queryCategories();
        String categoryId = null;
        if (cf != null && cf.moveToFirst()) {
            categoryId = cf.getString(cf.getColumnIndex(Constants.COLUMN_TECH_VAR));
        }
        if (cf != null) cf.close();
        long id = DB.insertReminder(task, type, myDay, myMonth, myYear, myHour, myMinute, 0, null,
                0, 0, 0, 0, 0, uuID, null, 0, null, 0, 0, 0, categoryId, null);
        DB.updateReminderDateTime(id);
        new AlarmReceiver().setAlarm(TaskManager.this, id);
        DB.close();
        UpdatesHelper updatesHelper = new UpdatesHelper(TaskManager.this);
        updatesHelper.updateWidget();
        new Notifier(TaskManager.this).recreatePermanent();
        return id;
    }

    private void deleteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(getString(R.string.string_delete_task));
        builder.setMessage(getString(R.string.delete_task_question));
        builder.setNegativeButton(getString(R.string.import_dialog_button_no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setPositiveButton(getString(R.string.import_dialog_button_yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteTask();
                dialog.dismiss();
                finish();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void deleteTask() {
        data.open();
        Cursor c = data.getTask(id);
        if (c != null && c.moveToFirst()){
            String taskId = c.getString(c.getColumnIndex(TasksConstants.COLUMN_TASK_ID));
            long id = c.getLong(c.getColumnIndex(TasksConstants.COLUMN_ID));
            data.deleteTask(id);
            new TaskAsync(TaskManager.this, null, initListId, taskId, TasksConstants.DELETE_TASK,
                    0, null, id).execute();
        }
        if (c != null) c.close();
    }

    private void setColor(int i){
        color = i;
        toolbar.setBackgroundColor(cSetter.getNoteColor(i));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(cSetter.getNoteDarkColor(i));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.save_menu, menu);
        if (id != 0) {
            menu.add(Menu.NONE, MENU_ITEM_DELETE, 100, getString(R.string.string_delete_task));
            menu.add(Menu.NONE, MENU_ITEM_MOVE, 100, getString(R.string.move_to_list));
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_ITEM_DELETE:
                deleteDialog();
                return true;
            case MENU_ITEM_MOVE:
                selectList(true);
                return true;
            case R.id.action_add:
                saveTask();
                return true;
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected Dialog dateDialog() {
        return new DatePickerDialog(this, myDateCallBack, myYear, myMonth, myDay);
    }

    DatePickerDialog.OnDateSetListener myDateCallBack = new DatePickerDialog.OnDateSetListener() {

        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            myYear = year;
            myMonth = monthOfYear;
            myDay = dayOfMonth;

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(year, monthOfYear, dayOfMonth);

            dateField.setText(TimeUtil.getDate(calendar.getTime()));
        }
    };

    protected Dialog timeDialog() {
        return new TimePickerDialog(this, myCallBack, myHour, myMinute,
                new SharedPrefs(TaskManager.this).loadBoolean(Prefs.IS_24_TIME_FORMAT));
    }

    TimePickerDialog.OnTimeSetListener myCallBack = new TimePickerDialog.OnTimeSetListener() {
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            myHour = hourOfDay;
            myMinute = minute;

            Calendar c = Calendar.getInstance();
            c.set(Calendar.HOUR_OF_DAY, hourOfDay);
            c.set(Calendar.MINUTE, minute);

            timeField.setText(TimeUtil.getTime(c.getTime(),
                    sPrefs.loadBoolean(Prefs.IS_24_TIME_FORMAT)));
        }
    };

    @Override
    protected void onDestroy() {
        new UpdatesHelper(TaskManager.this).updateTasksWidget();
        super.onDestroy();
    }

    public class SimpleAdapter extends CursorAdapter {

        LayoutInflater inflater;
        private Cursor c;
        Context cContext;
        ColorSetter cs;

        public SimpleAdapter(Context context, Cursor c) {
            super(context, c);
            this.cContext = context;
            cs = new ColorSetter(context);
            inflater = LayoutInflater.from(context);
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            this.c = c;
            c.moveToFirst();
        }

        @Override
        public int getCount() {
            return c.getCount();
        }

        @Override
        public Object getItem(int position) {
            return super.getItem(position);
        }

        @Override
        public long getItemId(int position) {
            Cursor cursor = getCursor();
            cursor.moveToPosition(position);
            return cursor.getLong(cursor.getColumnIndex("_id"));
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return inflater.inflate(android.R.layout.simple_list_item_1, null);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            c.moveToPosition(position);
            if (convertView == null) {
                convertView = inflater.inflate(android.R.layout.simple_list_item_1, null);
            }

            String text = c.getString(c.getColumnIndex(TasksConstants.COLUMN_TITLE));

            TextView textView = (TextView) convertView.findViewById(android.R.id.text1);
            textView.setText(text);

            return convertView;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {

        }
    }
}