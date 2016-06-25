package com.cray.software.justreminder;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TimePicker;

import com.cray.software.justreminder.app_widgets.UpdatesHelper;
import com.cray.software.justreminder.async.TaskAsync;
import com.cray.software.justreminder.cloud.GTasksHelper;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.constants.TasksConstants;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.databases.NextBase;
import com.cray.software.justreminder.databases.TasksData;
import com.cray.software.justreminder.datas.models.CategoryModel;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.Messages;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.json.JModel;
import com.cray.software.justreminder.modules.Module;
import com.cray.software.justreminder.reminder.DateType;
import com.cray.software.justreminder.roboto_views.RoboEditText;
import com.cray.software.justreminder.roboto_views.RoboTextView;
import com.cray.software.justreminder.utils.TimeUtil;
import com.cray.software.justreminder.utils.ViewUtils;

import java.util.ArrayList;
import java.util.Calendar;

public class TaskManager extends AppCompatActivity {
    private ColorSetter cSetter = new ColorSetter(TaskManager.this);
    private SharedPrefs sPrefs = new SharedPrefs(TaskManager.this);

    private long id;
    private Toolbar toolbar;
    private RoboEditText editField, noteField;
    private RoboTextView dateField;
    private RoboTextView timeField;
    private RoboTextView listText;

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
    private boolean isReminder = false;
    private boolean isDate = false;

    private static final int MENU_ITEM_DELETE = 12;
    private static final int MENU_ITEM_MOVE = 14;

    private ArrayList<CategoryModel> categories = new ArrayList<>();

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

        sPrefs = new SharedPrefs(TaskManager.this);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

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
                TasksData data = new TasksData(TaskManager.this);
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
                data.close();
            } else {
                TasksData data = new TasksData(TaskManager.this);
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
                data.close();
            }
        } else {
            toolbar.setTitle(R.string.edit_task);
            id = tmp;
            if (id != 0) {
                TasksData data = new TasksData(TaskManager.this);
                data.open();
                Cursor c = data.getTask(id);
                if (c != null && c.moveToFirst()) {
                    editField.setText(c.getString(c.getColumnIndex(TasksConstants.COLUMN_TITLE)));
                    taskId = c.getString(c.getColumnIndex(TasksConstants.COLUMN_TASK_ID));
                    String note = c.getString(c.getColumnIndex(TasksConstants.COLUMN_NOTES));
                    long remId = c.getLong(c.getColumnIndex(TasksConstants.COLUMN_REMINDER_ID));
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
                        isDate = true;
                        dateField.setText(TimeUtil.getDate(calendar.getTime()));
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

                    if (remId > 0) {
                        NextBase db = new NextBase(this);
                        db.open();
                        Cursor r = db.getReminder(remId);
                        if (r != null && r.moveToFirst()){
                            long eventTime = r.getLong(r.getColumnIndex(NextBase.EVENT_TIME));
                            calendar.setTimeInMillis(eventTime);
                            timeField.setText(TimeUtil.getTime(calendar.getTime(),
                                    sPrefs.loadBoolean(Prefs.IS_24_TIME_FORMAT)));
                            isReminder = true;
                        }
                        if (r != null) r.close();
                        db.close();
                    }
                }
                if (c != null) c.close();
                data.close();
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
        if (!listId.matches(initListId)) {
            TasksData data = new TasksData(TaskManager.this);
            data.open();
            data.updateTask(id, listId);
            new TaskAsync(TaskManager.this, null, listId, taskId, TasksConstants.MOVE_TASK,
                    0, null, id, initListId).execute();
            data.close();
            finish();
        } else {
            Messages.toast(this, getString(R.string.this_is_same_list));
        }
    }

    private void selectList(final boolean move) {
        TasksData data = new TasksData(TaskManager.this);
        data.open();
        Cursor c = data.getTasksLists();
        if (c != null && c.moveToFirst()){
            do {
                String listTitle = c.getString(c.getColumnIndex(TasksConstants.COLUMN_TITLE));
                String listId = c.getString(c.getColumnIndex(TasksConstants.COLUMN_LIST_ID));
                categories.add(new CategoryModel(listTitle, listId));
            } while (c.moveToNext());
        }
        if (c != null) c.close();
        data.close();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.choose_list);
        builder.setAdapter(new SimpleAdapter(TaskManager.this,
                data.getTasksLists()), (dialog, which) -> {
                    dialog.dismiss();
                    if (move) moveTask(categories.get(which).getUuID());
                    else {
                        listText.setText(categories.get(which).getTitle());
                        listId = categories.get(which).getUuID();
                        reloadColor(listId);
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
        TasksData data = new TasksData(TaskManager.this);
        data.open();
        if (action.matches(TasksConstants.CREATE)) {
            long localId = data.addTask(taskName, null, 0, false, due, null, null, note,
                    null, null, null, 0, remId, listId != null ? listId : initListId, GTasksHelper.TASKS_NEED_ACTION, false);
            new TaskAsync(TaskManager.this, taskName, listId != null ? listId : initListId, null, TasksConstants.INSERT_TASK,
                    due, note, localId, null).execute();
        }
        if (action.matches(TasksConstants.EDIT)) {
            if (id != 0) {
                if (listId != null) {
                    data.updateTask(id, taskName, due, note, GTasksHelper.TASKS_NEED_ACTION, remId, listId);
                    new TaskAsync(TaskManager.this, taskName, initListId, taskId, TasksConstants.UPDATE_TASK,
                            due, note, id, null).execute();
                    if (!listId.matches(initListId)) {
                        new TaskAsync(TaskManager.this, taskName, listId, taskId, TasksConstants.MOVE_TASK,
                                due, note, id, initListId).execute();
                    }
                } else {
                    data.updateTask(id, taskName, due, note, GTasksHelper.TASKS_NEED_ACTION, remId);
                    new TaskAsync(TaskManager.this, taskName, initListId, taskId, TasksConstants.UPDATE_TASK,
                            due, note, id, null).execute();
                }
            }
        }
        data.close();
        finish();
    }

    private long saveReminder(String task){
        DataBase db = new DataBase(TaskManager.this);
        db.open();
        Cursor cf = db.queryCategories();
        String categoryId = null;
        if (cf != null && cf.moveToFirst()) {
            categoryId = cf.getString(cf.getColumnIndex(Constants.COLUMN_TECH_VAR));
        }
        if (cf != null) cf.close();
        db.close();

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(myYear, myMonth, myDay, myHour, myMinute);
        long due = calendar.getTimeInMillis();
        JModel jModel = new JModel(task, Constants.TYPE_REMINDER, categoryId,
                SyncHelper.generateID(), due, due, null, null, null);
        return new DateType(TaskManager.this, Constants.TYPE_REMINDER).save(jModel);
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
        TasksData data = new TasksData(TaskManager.this);
        data.open();
        Cursor c = data.getTask(id);
        if (c != null && c.moveToFirst()){
            String taskId = c.getString(c.getColumnIndex(TasksConstants.COLUMN_TASK_ID));
            data.deleteTask(id);
            new TaskAsync(TaskManager.this, null, initListId, taskId, TasksConstants.DELETE_TASK,
                    0, null, id, null).execute();
        }
        if (c != null) c.close();
        data.close();
    }

    private void setColor(int i){
        color = i;
        toolbar.setBackgroundColor(cSetter.getNoteColor(i));
        if (Module.isLollipop()) {
            getWindow().setStatusBarColor(cSetter.getNoteDarkColor(i));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.save_menu, menu);
        if (id != 0) {
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
            return inflater.inflate(R.layout.list_item_simple_text1, null);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            c.moveToPosition(position);
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.list_item_simple_text1, null);
            }
            String text = c.getString(c.getColumnIndex(TasksConstants.COLUMN_TITLE));
            RoboTextView textView = (RoboTextView) convertView.findViewById(R.id.text1);
            textView.setText(text);
            return convertView;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {

        }
    }
}