package com.cray.software.justreminder.google_tasks;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TimePicker;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.app_widgets.UpdatesHelper;
import com.cray.software.justreminder.cloud.GTasksHelper;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.constants.TasksConstants;
import com.cray.software.justreminder.reminder.NextBase;
import com.cray.software.justreminder.groups.GroupHelper;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.Messages;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.reminder.json.JsonModel;
import com.cray.software.justreminder.modules.Module;
import com.cray.software.justreminder.reminder.DateType;
import com.cray.software.justreminder.roboto_views.RoboEditText;
import com.cray.software.justreminder.roboto_views.RoboTextView;
import com.cray.software.justreminder.utils.TimeUtil;
import com.cray.software.justreminder.utils.ViewUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class TaskManager extends AppCompatActivity {
    private ColorSetter cSetter = new ColorSetter(TaskManager.this);

    private Toolbar toolbar;
    private RoboEditText editField, noteField;
    private RoboTextView dateField;
    private RoboTextView timeField;
    private RoboTextView listText;

    private int myHour = 0;
    private int myMinute = 0;
    private int myYear = 0;
    private int myMonth = 0;
    private int myDay = 1;
    private String listId = null;
    private String taskId;
    private String action;
    private boolean isReminder = false;
    private boolean isDate = false;

    private TaskItem mItem;

    private static final int MENU_ITEM_DELETE = 12;
    private static final int MENU_ITEM_MOVE = 14;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cSetter = new ColorSetter(TaskManager.this);
        setTheme(cSetter.getStyle());
        if (Module.isLollipop()) {
            getWindow().setStatusBarColor(ViewUtils.getColor(this, cSetter.colorPrimaryDark()));
        }
        setContentView(R.layout.task_manager_layout);
        setRequestedOrientation(cSetter.getRequestOrientation());
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        editField = (RoboEditText) findViewById(R.id.editField);
        noteField = (RoboEditText) findViewById(R.id.noteField);
        listText = (RoboTextView) findViewById(R.id.listText);
        listText.setOnClickListener(v -> selectList(false));
        findViewById(R.id.windowBackground).setBackgroundColor(cSetter.getBackgroundStyle());

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        myHour = calendar.get(Calendar.HOUR_OF_DAY);
        myMinute = calendar.get(Calendar.MINUTE);
        myYear = calendar.get(Calendar.YEAR);
        myMonth = calendar.get(Calendar.MONTH);
        myDay = calendar.get(Calendar.DAY_OF_MONTH);

        dateField = (RoboTextView) findViewById(R.id.dateField);
        dateField.setOnClickListener(v -> selectDateAction(1));

        timeField = (RoboTextView) findViewById(R.id.timeField);
        timeField.setOnClickListener(v -> selectDateAction(2));

        ImageView noteIcon = (ImageView) findViewById(R.id.noteIcon);
        ImageView dateIcon = (ImageView) findViewById(R.id.dateIcon);
        ImageView timeIcon = (ImageView) findViewById(R.id.timeIcon);
        ImageView listIcon = (ImageView) findViewById(R.id.listIcon);
        if (cSetter.isDark()){
            noteIcon.setImageResource(R.drawable.ic_event_note_white_24dp);
            dateIcon.setImageResource(R.drawable.ic_event_white_24dp);
            timeIcon.setImageResource(R.drawable.ic_alarm_white_24dp);
            listIcon.setImageResource(R.drawable.ic_view_list_white_24dp);
        } else {
            noteIcon.setImageResource(R.drawable.ic_event_note_black_24dp);
            dateIcon.setImageResource(R.drawable.ic_event_black_24dp);
            timeIcon.setImageResource(R.drawable.ic_alarm_black_24dp);
            listIcon.setImageResource(R.drawable.ic_view_list_black_24dp);
        }

        Intent intent = getIntent();
        long tmp = intent.getLongExtra(Constants.ITEM_ID_INTENT, 0);
        action = intent.getStringExtra(TasksConstants.INTENT_ACTION);
        if (action == null) action = TasksConstants.CREATE;
        if (action.matches(TasksConstants.CREATE)){
            toolbar.setTitle(R.string.new_task);
            if (tmp == 0) {
                TaskListItem listItem = TasksHelper.getInstance(this).getDefaultTaskList();
                if (listItem != null) {
                    listId = listItem.getListId();
                    listText.setText(listItem.getTitle());
                    setColor(listItem.getColor());
                }
            } else {
                TaskListItem listItem = TasksHelper.getInstance(this).getTaskList(tmp);
                if (listItem != null) {
                    listId = listItem.getListId();
                    listText.setText(listItem.getTitle());
                    setColor(listItem.getColor());
                }
            }
        } else {
            toolbar.setTitle(R.string.edit_task);
            mItem = TasksHelper.getInstance(this).getTask(tmp);
            if (mItem != null) {
                editField.setText(mItem.getTitle());
                taskId = mItem.getTaskId();
                listId = mItem.getListId();
                String note = mItem.getNotes();
                long remId = mItem.getReminderId();
                if (note != null) {
                    noteField.setText(note);
                    noteField.setSelection(noteField.getText().length());
                }

                long time = mItem.getDueDate();
                if (time != 0) {
                    calendar.setTimeInMillis(time);
                    myHour = calendar.get(Calendar.HOUR_OF_DAY);
                    myMinute = calendar.get(Calendar.MINUTE);
                    myYear = calendar.get(Calendar.YEAR);
                    myMonth = calendar.get(Calendar.MONTH);
                    myDay = calendar.get(Calendar.DAY_OF_MONTH);
                    isDate = true;
                    dateField.setText(TimeUtil.getDate(calendar.getTime()));
                }
                TaskListItem listItem = TasksHelper.getInstance(this).getTaskList(mItem.getListId());
                if (listItem != null) {
                    listText.setText(listItem.getTitle());
                    setColor(listItem.getColor());
                }
                if (remId > 0) {
                    NextBase db = new NextBase(this);
                    db.open();
                    Cursor r = db.getReminder(remId);
                    if (r != null && r.moveToFirst()){
                        long eventTime = r.getLong(r.getColumnIndex(NextBase.EVENT_TIME));
                        calendar.setTimeInMillis(eventTime);
                        timeField.setText(TimeUtil.getTime(calendar.getTime(),
                                SharedPrefs.getInstance(this).getBoolean(Prefs.IS_24_TIME_FORMAT)));
                        isReminder = true;
                    }
                    if (r != null) r.close();
                    db.close();
                }
            }
        }
        switchDate();
    }

    private void selectDateAction(final int type) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String[] types = new String[]{getString(R.string.no_date), getString(R.string.select_date)};
        if (type == 2){
            types = new String[]{getString(R.string.no_reminder), getString(R.string.select_time)};
        }
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_single_choice, types);
        int selection = 0;
        if (type == 1){
            if (isDate) selection = 1;
            else selection = 0;
        }
        if (type == 2){
            if (isReminder) selection = 1;
            else selection = 0;
        }
        builder.setSingleChoiceItems(adapter, selection, (dialog, which) -> {
            if (which != -1) {
                dialog.dismiss();
                if (type == 1){
                    switch (which){
                        case 0:
                            isDate = false;
                            switchDate();
                            break;
                        case 1:
                            isDate = true;
                            dateDialog().show();
                            break;
                    }
                }
                if (type == 2){
                    switch (which){
                        case 0:
                            isReminder = false;
                            switchDate();
                            break;
                        case 1:
                            isReminder = true;
                            timeDialog().show();
                            break;
                    }
                }
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void switchDate() {
        if (!isDate) dateField.setText(getString(R.string.no_date));
        if (!isReminder) timeField.setText(getString(R.string.no_reminder));
    }

    private void moveTask(String listId) {
        String initListId = mItem.getListId();
        if (!listId.matches(initListId)) {
            mItem.setListId(listId);
            TasksHelper.getInstance(this).saveTask(mItem);
            new TaskAsync(TaskManager.this, null, listId, taskId, TasksConstants.MOVE_TASK,
                    0, null, mItem.getId(), initListId).execute();
            finish();
        } else {
            Messages.toast(this, getString(R.string.this_is_same_list));
        }
    }

    private void selectList(final boolean move) {
        List<TaskListItem> list = TasksHelper.getInstance(this).getTaskLists();
        List<String> names = new ArrayList<>();
        int position = 0;
        for (int i = 0; i < list.size(); i++) {
            TaskListItem item = list.get(i);
            names.add(item.getTitle());
            if (item.getListId().matches(listId)) position = i;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.choose_list);
        builder.setSingleChoiceItems(new ArrayAdapter<>(this, android.R.layout.simple_list_item_single_choice, names), position, (dialog, which) -> {
            dialog.dismiss();
            if (move) moveTask(list.get(which).getListId());
            else {
                listId = list.get(which).getListId();
                listText.setText(list.get(which).getTitle());
                reloadColor(list.get(which).getListId());
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void reloadColor(String listId) {
        TaskListItem item = TasksHelper.getInstance(this).getTaskList(listId);
        if (item != null) {
            setColor(item.getColor());
        }
    }

    private void saveTask() {
        String initListId = mItem.getListId();
        SharedPrefs.getInstance(this).putBoolean(Prefs.TASK_CHANGED, true);
        String taskName = editField.getText().toString().trim();
        if (taskName.matches("")) {
            editField.setError(getString(R.string.must_be_not_empty));
            return;
        }
        String note = noteField.getText().toString().trim();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(myYear, myMonth, myDay, 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long due = 0;
        if (isDate) due = calendar.getTimeInMillis();
        long remId = 0;
        if (isReminder) remId = saveReminder(taskName);
        if (action.matches(TasksConstants.CREATE)) {
            mItem = new TaskItem();
        }
        mItem.setListId(listId);
        mItem.setStatus(GTasksHelper.TASKS_NEED_ACTION);
        mItem.setTitle(taskName);
        mItem.setNotes(note);
        mItem.setDueDate(due);
        mItem.setReminderId(remId);
        if (action.matches(TasksConstants.EDIT)) {
            if (mItem != null) {
                if (listId != null) {
                    long id = mItem.getId();
                    TasksHelper.getInstance(this).saveTask(mItem);
                    new TaskAsync(TaskManager.this, taskName, initListId, taskId, TasksConstants.UPDATE_TASK,
                            due, note, id, null).execute();
                    if (!listId.matches(initListId)) {
                        new TaskAsync(TaskManager.this, taskName, listId, taskId, TasksConstants.MOVE_TASK,
                                due, note, id, initListId).execute();
                    }
                } else {
                    TasksHelper.getInstance(this).saveTask(mItem);
                    new TaskAsync(TaskManager.this, taskName, initListId, taskId, TasksConstants.UPDATE_TASK,
                            due, note, mItem.getId(), null).execute();
                }
            }
        } else {
            long localId = TasksHelper.getInstance(this).saveTask(mItem);
            new TaskAsync(TaskManager.this, taskName, listId, null, TasksConstants.INSERT_TASK,
                    due, note, localId, null).execute();
        }
        finish();
    }

    private long saveReminder(String task){
        String categoryId = GroupHelper.getInstance(this).getDefaultUuId();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(myYear, myMonth, myDay, myHour, myMinute);
        long due = calendar.getTimeInMillis();
        JsonModel jsonModel = new JsonModel(task, Constants.TYPE_REMINDER, categoryId,
                SyncHelper.generateID(), due, due, null, null, null);
        return new DateType(TaskManager.this, Constants.TYPE_REMINDER).save(jsonModel);
    }

    private void deleteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.delete_this_task));
        builder.setPositiveButton(getString(R.string.yes), (dialog, which) -> {
            dialog.dismiss();
            deleteTask();
            finish();
        });
        builder.setNegativeButton(getString(R.string.no), (dialog, which) -> {
            dialog.dismiss();
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void deleteTask() {
        if (mItem != null){
            String taskId = mItem.getTaskId();
            TasksHelper.getInstance(this).deleteTask(mItem.getId());
            new TaskAsync(TaskManager.this, null, mItem.getListId(), taskId, TasksConstants.DELETE_TASK,
                    0, null, mItem.getId(), null).execute();
        }
    }

    private void setColor(int i){
        toolbar.setBackgroundColor(cSetter.getNoteColor(i));
        if (Module.isLollipop()) {
            getWindow().setStatusBarColor(cSetter.getNoteDarkColor(i));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.save_menu, menu);
        if (mItem != null) {
            menu.add(Menu.NONE, MENU_ITEM_DELETE, 100, R.string.delete_task);
            menu.add(Menu.NONE, MENU_ITEM_MOVE, 100, R.string.move_to_another_list);
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
                SharedPrefs.getInstance(this).getBoolean(Prefs.IS_24_TIME_FORMAT));
    }

    TimePickerDialog.OnTimeSetListener myCallBack = new TimePickerDialog.OnTimeSetListener() {
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            myHour = hourOfDay;
            myMinute = minute;

            Calendar c = Calendar.getInstance();
            c.set(Calendar.HOUR_OF_DAY, hourOfDay);
            c.set(Calendar.MINUTE, minute);

            timeField.setText(TimeUtil.getTime(c.getTime(),
                    SharedPrefs.getInstance(TaskManager.this).getBoolean(Prefs.IS_24_TIME_FORMAT)));
        }
    };

    @Override
    protected void onDestroy() {
        UpdatesHelper.getInstance(this).updateTasksWidget();
        super.onDestroy();
    }
}