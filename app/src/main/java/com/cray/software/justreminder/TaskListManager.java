package com.cray.software.justreminder;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.cray.software.justreminder.async.TaskListAsync;
import com.cray.software.justreminder.databases.TasksData;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.interfaces.TasksConstants;
import com.cray.software.justreminder.modules.ManageModule;
import com.cray.software.justreminder.views.FloatingEditText;
import com.cray.software.justreminder.widgets.UpdatesHelper;


public class TaskListManager extends AppCompatActivity {
    ColorSetter cSetter = new ColorSetter(TaskListManager.this);
    SharedPrefs sPrefs = new SharedPrefs(TaskListManager.this);
    RadioButton red_checkbox, violet_checkbox, green_checkbox, light_green_checkbox, blue_checkbox, light_blue_checkbox,
            yellow_checkbox, orange_checkbox, grey_checkbox, pink_checkbox, sand_checkbox, brown_checkbox,
            deepPurple, indigoCheckbox, limeCheckbox, deepOrange;
    RadioGroup themeGroup, themeGroup2, themeGroup3, themeGroupPro;
    CheckBox defaultCheck;

    long id;
    Toolbar toolbar;
    FloatingEditText editField;
    int color, sysDef;

    TasksData data = new TasksData(TaskListManager.this);

    public static final int MENU_ITEM_DELETE = 12;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cSetter = new ColorSetter(TaskListManager.this);
        setTheme(cSetter.getStyle());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(cSetter.colorStatus());
        }
        setContentView(R.layout.task_list_manager_layout);
        setRequestedOrientation(cSetter.getRequestOrientation());

        sPrefs = new SharedPrefs(TaskListManager.this);

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
                                saveTaskList();
                                break;
                        }
                        return true;
                    }
                });

        toolbar.inflateMenu(R.menu.save_menu);

        editField = (FloatingEditText) findViewById(R.id.editField);
        defaultCheck = (CheckBox) findViewById(R.id.defaultCheck);

        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setElevation(0f);

        findViewById(R.id.windowBackground).setBackgroundColor(cSetter.getBackgroundStyle());

        Intent intent = getIntent();
        id = intent.getLongExtra(Constants.ITEM_ID_INTENT, 0);
        color = 4;
        if (id != 0){
            data.open();
            Cursor c = data.getTasksList(id);
            if (c != null && c.moveToFirst()){
                color = c.getInt(c.getColumnIndex(TasksConstants.COLUMN_COLOR));
                sysDef = c.getInt(c.getColumnIndex(TasksConstants.SYSTEM_DEFAULT));
                editField.setText(c.getString(c.getColumnIndex(TasksConstants.COLUMN_TITLE)));
                int check = c.getInt(c.getColumnIndex(TasksConstants.COLUMN_DEFAULT));
                if (check == 1){
                    defaultCheck.setChecked(true);
                    defaultCheck.setEnabled(false);
                }
                setColor(color);
            }
            toolbar.setTitle(getString(R.string.string_editing_task_list));
            if (c != null) c.close();
        } else {
            toolbar.setTitle(getString(R.string.string_new_task_list));
        }

        initRadio();
    }

    private void initRadio(){
        red_checkbox = (RadioButton) findViewById(R.id.red_checkbox);
        violet_checkbox = (RadioButton) findViewById(R.id.violet_checkbox);
        green_checkbox = (RadioButton) findViewById(R.id.green_checkbox);
        light_green_checkbox = (RadioButton) findViewById(R.id.light_green_checkbox);
        blue_checkbox = (RadioButton) findViewById(R.id.blue_checkbox);
        light_blue_checkbox = (RadioButton) findViewById(R.id.light_blue_checkbox);
        yellow_checkbox = (RadioButton) findViewById(R.id.yellow_checkbox);
        orange_checkbox = (RadioButton) findViewById(R.id.orange_checkbox);
        grey_checkbox = (RadioButton) findViewById(R.id.grey_checkbox);
        pink_checkbox = (RadioButton) findViewById(R.id.pink_checkbox);
        sand_checkbox = (RadioButton) findViewById(R.id.sand_checkbox);
        brown_checkbox = (RadioButton) findViewById(R.id.brown_checkbox);

        deepPurple = (RadioButton) findViewById(R.id.deepPurple);
        indigoCheckbox = (RadioButton) findViewById(R.id.indigoCheckbox);
        limeCheckbox = (RadioButton) findViewById(R.id.limeCheckbox);
        deepOrange = (RadioButton) findViewById(R.id.deepOrange);

        themeGroup = (RadioGroup) findViewById(R.id.themeGroup);
        themeGroup2 = (RadioGroup) findViewById(R.id.themeGroup2);
        themeGroup3 = (RadioGroup) findViewById(R.id.themeGroup3);
        themeGroupPro = (RadioGroup) findViewById(R.id.themeGroupPro);
        if (new ManageModule().isPro()) {
            themeGroupPro.setVisibility(View.VISIBLE);
        }

        themeGroup.clearCheck();
        themeGroup2.clearCheck();
        themeGroup3.clearCheck();
        themeGroupPro.clearCheck();
        themeGroup.setOnCheckedChangeListener(listener1);
        themeGroup2.setOnCheckedChangeListener(listener2);
        themeGroup3.setOnCheckedChangeListener(listener3);
        themeGroupPro.setOnCheckedChangeListener(listener4);

        setUpRadio();
    }

    private void saveTaskList() {
        sPrefs = new SharedPrefs(this);
        sPrefs.saveBoolean(Constants.APP_UI_PREFERENCES_TASK_CHANGED, true);
        String listName = editField.getText().toString();
        if (listName.matches("")) {
            editField.setError(getString(R.string.empty_field_error));
            return;
        }

        data.open();
        if (id != 0){
            Cursor c = data.getTasksList(id);
            String listId;
            if (c != null && c.moveToFirst()) {
                listId = c.getString(c.getColumnIndex(TasksConstants.COLUMN_LIST_ID));
                if (defaultCheck.isChecked()){
                    Cursor x = data.getTasksLists();
                    if (x != null && x.moveToFirst()){
                        do {
                            data.setSimple(x.getLong(x.getColumnIndex(TasksConstants.COLUMN_ID)));
                        } while (x.moveToNext());
                    }
                    if (x != null) x.close();
                }
                data.updateTasksList(id, listName, listId, defaultCheck.isChecked() ? 1 : 0,
                        c.getString(c.getColumnIndex(TasksConstants.COLUMN_E_TAG)),
                        c.getString(c.getColumnIndex(TasksConstants.COLUMN_KIND)),
                        c.getString(c.getColumnIndex(TasksConstants.COLUMN_SELF_LINK)),
                        System.currentTimeMillis(),
                        color);
                new TaskListAsync(TaskListManager.this, listName, id, color, listId, TasksConstants.UPDATE_TASK_LIST).execute();
            }
            if (c != null) c.close();
        } else {
            if (defaultCheck.isChecked()){
                Cursor x = data.getTasksLists();
                if (x != null && x.moveToFirst()){
                    do {
                        data.setSimple(x.getLong(x.getColumnIndex(TasksConstants.COLUMN_ID)));
                    } while (x.moveToNext());
                }
                if (x != null) x.close();
            }
            long idN = data.addTasksList(listName, null, defaultCheck.isChecked() ? 1 : 0, null, null, null,
                    System.currentTimeMillis(), color);
            new TaskListAsync(TaskListManager.this, listName, idN, color, null, TasksConstants.INSERT_TASK_LIST).execute();
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

    private void deleteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(getString(R.string.string_delete_task_list));
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
                finish();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void deleteList() {
        data.open();
        Cursor c = data.getTasksList(id);
        if (c != null && c.moveToFirst()){
            String listId = c.getString(c.getColumnIndex(TasksConstants.COLUMN_LIST_ID));
            int def = c.getInt(c.getColumnIndex(TasksConstants.COLUMN_DEFAULT));
            data.deleteTasksList(id);
            new TaskListAsync(TaskListManager.this, null, 0, 0, listId, TasksConstants.DELETE_TASK_LIST).execute();
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.save_menu, menu);
        if (id != 0 && sysDef != 1) {
            menu.add(Menu.NONE, MENU_ITEM_DELETE, 100, getString(R.string.string_delete_task_list));
        }
        return true;
    }

    private RadioGroup.OnCheckedChangeListener listener1 = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (checkedId != -1) {
                themeGroup2.setOnCheckedChangeListener(null);
                themeGroup3.setOnCheckedChangeListener(null);
                themeGroupPro.setOnCheckedChangeListener(null);
                themeGroup2.clearCheck();
                themeGroup3.clearCheck();
                themeGroupPro.clearCheck();
                themeGroup2.setOnCheckedChangeListener(listener2);
                themeGroup3.setOnCheckedChangeListener(listener3);
                themeGroupPro.setOnCheckedChangeListener(listener4);
                colorSwitch(group.getCheckedRadioButtonId());
            }
        }
    };
    private RadioGroup.OnCheckedChangeListener listener2 = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (checkedId != -1) {
                themeGroup.setOnCheckedChangeListener(null);
                themeGroup3.setOnCheckedChangeListener(null);
                themeGroupPro.setOnCheckedChangeListener(null);
                themeGroup.clearCheck();
                themeGroup3.clearCheck();
                themeGroupPro.clearCheck();
                themeGroup.setOnCheckedChangeListener(listener1);
                themeGroup3.setOnCheckedChangeListener(listener3);
                themeGroupPro.setOnCheckedChangeListener(listener4);
                colorSwitch(group.getCheckedRadioButtonId());
            }
        }
    };
    private RadioGroup.OnCheckedChangeListener listener3 = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (checkedId != -1) {
                themeGroup.setOnCheckedChangeListener(null);
                themeGroup2.setOnCheckedChangeListener(null);
                themeGroupPro.setOnCheckedChangeListener(null);
                themeGroup.clearCheck();
                themeGroup2.clearCheck();
                themeGroupPro.clearCheck();
                themeGroup.setOnCheckedChangeListener(listener1);
                themeGroup2.setOnCheckedChangeListener(listener2);
                themeGroupPro.setOnCheckedChangeListener(listener4);
                colorSwitch(group.getCheckedRadioButtonId());
            }
        }
    };
    private RadioGroup.OnCheckedChangeListener listener4 = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (checkedId != -1) {
                themeGroup.setOnCheckedChangeListener(null);
                themeGroup2.setOnCheckedChangeListener(null);
                themeGroup3.setOnCheckedChangeListener(null);
                themeGroup.clearCheck();
                themeGroup2.clearCheck();
                themeGroup3.clearCheck();
                themeGroup.setOnCheckedChangeListener(listener1);
                themeGroup2.setOnCheckedChangeListener(listener2);
                themeGroup3.setOnCheckedChangeListener(listener3);
                colorSwitch(group.getCheckedRadioButtonId());
            }
        }
    };

    private void colorSwitch(int checkId) {
        if (new ManageModule().isPro()) {
            switch (checkId) {
                case R.id.red_checkbox:
                    setColor(0);
                    break;
                case R.id.violet_checkbox:
                    setColor(1);
                    break;
                case R.id.green_checkbox:
                    setColor(2);
                    break;
                case R.id.light_green_checkbox:
                    setColor(3);
                    break;
                case R.id.blue_checkbox:
                    setColor(4);
                    break;
                case R.id.light_blue_checkbox:
                    setColor(5);
                    break;
                case R.id.yellow_checkbox:
                    setColor(6);
                    break;
                case R.id.orange_checkbox:
                    setColor(7);
                    break;
                case R.id.grey_checkbox:
                    setColor(8);
                    break;
                case R.id.pink_checkbox:
                    setColor(9);
                    break;
                case R.id.sand_checkbox:
                    setColor(10);
                    break;
                case R.id.brown_checkbox:
                    setColor(11);
                    break;
                case R.id.deepPurple:
                    setColor(12);
                    break;
                case R.id.deepOrange:
                    setColor(13);
                    break;
                case R.id.limeCheckbox:
                    setColor(14);
                    break;
                case R.id.indigoCheckbox:
                    setColor(15);
                    break;
            }
        } else {
            switch (checkId) {
                case R.id.red_checkbox:
                    setColor(0);
                    break;
                case R.id.violet_checkbox:
                    setColor(1);
                    break;
                case R.id.green_checkbox:
                    setColor(2);
                    break;
                case R.id.light_green_checkbox:
                    setColor(3);
                    break;
                case R.id.blue_checkbox:
                    setColor(4);
                    break;
                case R.id.light_blue_checkbox:
                    setColor(5);
                    break;
                case R.id.yellow_checkbox:
                    setColor(6);
                    break;
                case R.id.orange_checkbox:
                    setColor(7);
                    break;
                case R.id.grey_checkbox:
                    setColor(8);
                    break;
                case R.id.pink_checkbox:
                    setColor(9);
                    break;
                case R.id.sand_checkbox:
                    setColor(10);
                    break;
                case R.id.brown_checkbox:
                    setColor(11);
                    break;
            }
        }
    }

    private void setColor(int i){
        color = i;
        int colorTmp = cSetter.getNoteColor(i);
        toolbar.setBackgroundColor(getResources().getColor(colorTmp));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(cSetter.getNoteDarkColor(colorTmp));
        }
    }

    public void setUpRadio(){
        if (new ManageModule().isPro()) {
            switch (color) {
                case 0:
                    red_checkbox.setChecked(true);
                    break;
                case 1:
                    violet_checkbox.setChecked(true);
                    break;
                case 2:
                    green_checkbox.setChecked(true);
                    break;
                case 3:
                    light_green_checkbox.setChecked(true);
                    break;
                case 4:
                    blue_checkbox.setChecked(true);
                    break;
                case 5:
                    light_blue_checkbox.setChecked(true);
                    break;
                case 6:
                    yellow_checkbox.setChecked(true);
                    break;
                case 7:
                    orange_checkbox.setChecked(true);
                    break;
                case 8:
                    grey_checkbox.setChecked(true);
                    break;
                case 9:
                    pink_checkbox.setChecked(true);
                    break;
                case 10:
                    sand_checkbox.setChecked(true);
                    break;
                case 11:
                    brown_checkbox.setChecked(true);
                    break;
                case 12:
                    deepPurple.setChecked(true);
                    break;
                case 13:
                    deepOrange.setChecked(true);
                    break;
                case 14:
                    limeCheckbox.setChecked(true);
                    break;
                case 15:
                    indigoCheckbox.setChecked(true);
                    break;
                default:
                    green_checkbox.setChecked(true);
                    break;
            }
        } else {
            switch (color) {
                case 0:
                    red_checkbox.setChecked(true);
                    break;
                case 1:
                    violet_checkbox.setChecked(true);
                    break;
                case 2:
                    green_checkbox.setChecked(true);
                    break;
                case 3:
                    light_green_checkbox.setChecked(true);
                    break;
                case 4:
                    blue_checkbox.setChecked(true);
                    break;
                case 5:
                    light_blue_checkbox.setChecked(true);
                    break;
                case 6:
                    yellow_checkbox.setChecked(true);
                    break;
                case 7:
                    orange_checkbox.setChecked(true);
                    break;
                case 8:
                    grey_checkbox.setChecked(true);
                    break;
                case 9:
                    pink_checkbox.setChecked(true);
                    break;
                case 10:
                    sand_checkbox.setChecked(true);
                    break;
                case 11:
                    brown_checkbox.setChecked(true);
                    break;
                default:
                    green_checkbox.setChecked(true);
                    break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        new UpdatesHelper(TaskListManager.this).updateTasksWidget();
        super.onDestroy();
    }
}