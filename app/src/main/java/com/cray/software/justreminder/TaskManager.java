package com.cray.software.justreminder;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import com.cray.software.justreminder.async.TaskAsync;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.databases.TasksData;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.Notifier;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.interfaces.TasksConstants;
import com.cray.software.justreminder.services.AlarmReceiver;
import com.cray.software.justreminder.views.FloatingEditText;
import com.cray.software.justreminder.widgets.UpdatesHelper;
import com.getbase.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;


public class TaskManager extends AppCompatActivity {
    ColorSetter cSetter = new ColorSetter(TaskManager.this);
    SharedPrefs sPrefs = new SharedPrefs(TaskManager.this);

    long id;
    Toolbar toolbar;
    FloatingEditText editField, noteField;
    TextView listText, dateField, dateYearField, timeField;
    LinearLayout dateRing, dueContainer, reminderContainer;
    CheckBox reminderCheck, dueCheck;

    int color;
    int myHour = 0;
    int myMinute = 0;
    int myYear = 0;
    int myMonth = 0;
    int myDay = 1;
    String listId;
    String taskId;
    String action;

    TasksData data = new TasksData(TaskManager.this);
    Typeface typeface;

    public static final int MENU_ITEM_DELETE = 12;
    SimpleDateFormat full24Format = new SimpleDateFormat("EEE, dd MMMM");

    FloatingActionButton mFab;

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
        setSupportActionBar(toolbar);

        toolbar.setOnMenuItemClickListener(
                new Toolbar.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case MENU_ITEM_DELETE:
                                deleteDialog();
                                break;
                            case R.id.action_add:
                                saveTask();
                                break;
                        }
                        return true;
                    }
                });

        toolbar.inflateMenu(R.menu.save_menu);
        toolbar.setTitle(getString(R.string.string_add_task));

        editField = (FloatingEditText) findViewById(R.id.editField);
        noteField = (FloatingEditText) findViewById(R.id.noteField);
        listText = (TextView) findViewById(R.id.listText);
        reminderCheck = (CheckBox) findViewById(R.id.reminderCheck);
        reminderCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) expand(reminderContainer);
                else collapse(reminderContainer);
            }
        });
        dueCheck = (CheckBox) findViewById(R.id.dueCheck);
        dueCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) expand(dueContainer);
                else collapse(dueContainer);
            }
        });

        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setElevation(0f);

        findViewById(R.id.windowBackground).setBackgroundColor(cSetter.getBackgroundStyle());

        mFab = new FloatingActionButton(TaskManager.this);
        mFab.setSize(FloatingActionButton.SIZE_NORMAL);
        mFab.setIcon(R.drawable.ic_done_white_24dp);

        RelativeLayout wrapper = (RelativeLayout) findViewById(R.id.wrapper);
        wrapper.addView(mFab);

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mFab.getLayoutParams();
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);

        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveTask();
            }
        });
        mFab.setColorNormal(cSetter.colorSetter());
        mFab.setColorPressed(cSetter.colorChooser());

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        myHour = calendar.get(Calendar.HOUR_OF_DAY);
        myMinute = calendar.get(Calendar.MINUTE);
        myYear = calendar.get(Calendar.YEAR);
        myMonth = calendar.get(Calendar.MONTH);
        myDay = calendar.get(Calendar.DAY_OF_MONTH);

        reminderContainer = (LinearLayout) findViewById(R.id.reminderContainer);
        reminderContainer.setVisibility(View.GONE);

        dateRing = (LinearLayout) findViewById(R.id.dateRing);
        dueContainer = (LinearLayout) findViewById(R.id.dueContainer);
        dueContainer.setVisibility(View.GONE);
        dateRing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dateDialog().show();
            }
        });

        dateField = (TextView) findViewById(R.id.dateField);
        dateField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateDialog().show();
            }
        });

        dateField.setText(full24Format.format(calendar.getTime()));
        typeface = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Medium.ttf");
        dateField.setTypeface(typeface);

        timeField = (TextView) findViewById(R.id.timeField);
        timeField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timeDialog().show();
            }
        });
        timeField.setTypeface(typeface);
        String formattedTime;
        if (new SharedPrefs(TaskManager.this).loadBoolean(Constants.APP_UI_PREFERENCES_IS_24_TIME_FORMAT)){
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            formattedTime = sdf.format(calendar.getTime());
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("K:mm a");
            formattedTime = sdf.format(calendar.getTime());
        }

        timeField.setText(formattedTime);

        dateYearField = (TextView) findViewById(R.id.dateYearField);
        dateYearField.setText(String.valueOf(myYear));
        typeface = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Thin.ttf");
        dateYearField.setTypeface(typeface);

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
                    listId = c.getString(c.getColumnIndex(TasksConstants.COLUMN_LIST_ID));
                    color = c.getInt(c.getColumnIndex(TasksConstants.COLUMN_COLOR));
                    String listTitle = c.getString(c.getColumnIndex(TasksConstants.COLUMN_TITLE));
                    listText.setText(getString(R.string.string_list) + ": " + listTitle);
                    setColor(color);
                }
                if (c != null) c.close();
            } else {
                data.open();
                Cursor c = data.getTasksList(tmp);
                if (c != null && c.moveToFirst()) {
                    listId = c.getString(c.getColumnIndex(TasksConstants.COLUMN_LIST_ID));
                    color = c.getInt(c.getColumnIndex(TasksConstants.COLUMN_COLOR));
                    String listTitle = c.getString(c.getColumnIndex(TasksConstants.COLUMN_TITLE));
                    listText.setText(getString(R.string.string_list) + ": " + listTitle);
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

                        dateField.setText(full24Format.format(calendar.getTime()));

                        dueCheck.setChecked(true);
                    }

                    listId = c.getString(c.getColumnIndex(TasksConstants.COLUMN_LIST_ID));
                    Cursor x = data.getTasksList(listId);
                    if (x != null && x.moveToFirst()) {
                        color = x.getInt(x.getColumnIndex(TasksConstants.COLUMN_COLOR));
                        String listTitle = x.getString(x.getColumnIndex(TasksConstants.COLUMN_TITLE));
                        listText.setText(getString(R.string.string_list) + ": " + listTitle);
                        setColor(color);
                    }
                    if (x != null) x.close();
                }
                if (c != null) c.close();
            }
        }
    }

    private void saveTask() {
        sPrefs = new SharedPrefs(this);
        sPrefs.saveBoolean(Constants.APP_UI_PREFERENCES_TASK_CHANGED, true);
        String taskName = editField.getText().toString().trim();
        if (taskName.matches("")) {
            editField.setError(getString(R.string.empty_field_error));
            return;
        }

        String note = noteField.getText().toString().trim();

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, myYear);
        calendar.set(Calendar.MONTH, myMonth);
        calendar.set(Calendar.DAY_OF_MONTH, myDay);
        calendar.set(Calendar.HOUR, myHour);
        calendar.set(Calendar.MINUTE, myMinute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        long due = 0;
        if (dueCheck.isChecked()) due = calendar.getTimeInMillis();
        long remId = 0;
        if (reminderCheck.isChecked()) remId = saveReminder(taskName);
        data.open();
        if (action.matches(TasksConstants.CREATE)){
            long localId = data.addTask(taskName, null, 0, false, due, null, null, note,
                    null, null, null, 0, remId, listId, Constants.TASKS_NEED_ACTION, false);
            new TaskAsync(TaskManager.this, taskName, listId, null, TasksConstants.INSERT_TASK,
                    due, note, localId).execute();
        }
        if (action.matches(TasksConstants.EDIT)) {
            if (id != 0) {
                data.updateTask(id, taskName, due, note, Constants.TASKS_NEED_ACTION, remId);
                new TaskAsync(TaskManager.this, taskName, listId, taskId, TasksConstants.UPDATE_TASK,
                        due, note, id).execute();
            }
        }

        finish();
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

    private long saveReminder(String task){
        String type = Constants.TYPE_REMINDER;
        DataBase DB = new DataBase(TaskManager.this);
        DB.open();
        SyncHelper sHelp = new SyncHelper(TaskManager.this);
        String uuID = sHelp.generateID();
        Cursor cf = DB.queryCategories();
        String categoryId = null;
        if (cf != null && cf.moveToFirst()) {
            categoryId = cf.getString(cf.getColumnIndex(Constants.COLUMN_TECH_VAR));
        }
        if (cf != null) cf.close();
        long id = DB.insertTask(task, type, myDay, myMonth, myYear, myHour, myMinute, 0, null,
                0, 0, 0, 0, 0, uuID, null, 0, null, 0, 0, 0, categoryId);
        DB.updateDateTime(id);
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
            new TaskAsync(TaskManager.this, null, listId, taskId, TasksConstants.DELETE_TASK,
                    0, null, id).execute();
        }
        if (c != null) c.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /*MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.save_menu, menu);*/
        if (id != 0) {
            menu.add(Menu.NONE, MENU_ITEM_DELETE, 100, getString(R.string.string_delete_task));
            //menu.add(Menu.NONE, MENU_ITEM_MOVE, 100, getString(R.string.string_move_to_list));
        }
        return true;
    }

    private void setColor(int i){
        color = i;
        int colorTmp = cSetter.getNoteColor(i);
        toolbar.setBackgroundColor(getResources().getColor(colorTmp));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(cSetter.getNoteDarkColor(colorTmp));
        }

        mFab.setColorNormal(getResources().getColor(colorTmp));
        mFab.setColorPressed(cSetter.getNoteDarkColor(colorTmp));
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
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, monthOfYear);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            dateField.setText(full24Format.format(calendar.getTime()));
            dateYearField.setText(String.valueOf(myYear));
        }
    };

    protected Dialog timeDialog() {
        return new TimePickerDialog(this, myCallBack, myHour, myMinute,
                new SharedPrefs(TaskManager.this).loadBoolean(Constants.APP_UI_PREFERENCES_IS_24_TIME_FORMAT));
    }

    TimePickerDialog.OnTimeSetListener myCallBack = new TimePickerDialog.OnTimeSetListener() {
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            myHour = hourOfDay;
            myMinute = minute;

            Calendar c = Calendar.getInstance();
            c.set(Calendar.HOUR_OF_DAY, hourOfDay);
            c.set(Calendar.MINUTE, minute);

            String formattedTime;
            if (new SharedPrefs(TaskManager.this).loadBoolean(Constants.APP_UI_PREFERENCES_IS_24_TIME_FORMAT)){
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                formattedTime = sdf.format(c.getTime());
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("K:mm a");
                formattedTime = sdf.format(c.getTime());
            }

            timeField.setText(formattedTime);
        }
    };

    public static void expand(final View v) {
        v.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        final int targetHeight = v.getMeasuredHeight();

        v.getLayoutParams().height = 0;
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.getLayoutParams().height = interpolatedTime == 1
                        ? ViewGroup.LayoutParams.WRAP_CONTENT
                        : (int)(targetHeight * interpolatedTime);
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration((int)(targetHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }

    public static void collapse(final View v) {
        final int initialHeight = v.getMeasuredHeight();

        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if(interpolatedTime == 1){
                    v.setVisibility(View.GONE);
                } else{
                    v.getLayoutParams().height = initialHeight - (int)(initialHeight * interpolatedTime);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration((int)(initialHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }

    @Override
    protected void onDestroy() {
        new UpdatesHelper(TaskManager.this).updateTasksWidget();
        super.onDestroy();
    }
}