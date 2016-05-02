package com.cray.software.justreminder;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.cray.software.justreminder.app_widgets.UpdatesHelper;
import com.cray.software.justreminder.async.TaskListAsync;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.constants.TasksConstants;
import com.cray.software.justreminder.databases.TasksData;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.modules.Module;
import com.cray.software.justreminder.utils.ViewUtils;


public class TaskListManager extends AppCompatActivity {
    private ColorSetter cSetter = new ColorSetter(TaskListManager.this);
    private SharedPrefs sPrefs = new SharedPrefs(TaskListManager.this);
    private ImageButton red_checkbox, violet_checkbox, green_checkbox, light_green_checkbox,
            blue_checkbox, light_blue_checkbox, yellow_checkbox, orange_checkbox, grey_checkbox,
            pink_checkbox, sand_checkbox, brown_checkbox, deepPurple, indigoCheckbox, limeCheckbox,
            deepOrange;
    private CheckBox defaultCheck;

    private long id;
    private Toolbar toolbar;
    private EditText editField;
    private int color, sysDef;
    private int prevId;

    private static final int MENU_ITEM_DELETE = 12;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cSetter = new ColorSetter(TaskListManager.this);
        setTheme(cSetter.getStyle());
        if (Module.isLollipop()) {
            getWindow().setStatusBarColor(ViewUtils.getColor(this, cSetter.colorPrimaryDark()));
        }
        setContentView(R.layout.task_list_manager_layout);
        setRequestedOrientation(cSetter.getRequestOrientation());

        sPrefs = new SharedPrefs(TaskListManager.this);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setOnMenuItemClickListener(
                item -> {
                    switch (item.getItemId()) {
                        case MENU_ITEM_DELETE:
                            deleteDialog();
                            break;
                        case R.id.action_add:
                            saveTaskList();
                            break;
                    }
                    return true;
                });

        toolbar.inflateMenu(R.menu.save_menu);

        editField = (EditText) findViewById(R.id.editField);
        defaultCheck = (CheckBox) findViewById(R.id.defaultCheck);

        findViewById(R.id.windowBackground).setBackgroundColor(cSetter.getBackgroundStyle());

        Intent intent = getIntent();
        id = intent.getLongExtra(Constants.ITEM_ID_INTENT, 0);
        color = 4;
        if (id != 0){
            TasksData data = new TasksData(TaskListManager.this);
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
            if (c != null) c.close();
            data.close();
        }

        initRadio();
    }

    private void initRadio(){
        red_checkbox = (ImageButton) findViewById(R.id.red_checkbox);
        violet_checkbox = (ImageButton) findViewById(R.id.violet_checkbox);
        green_checkbox = (ImageButton) findViewById(R.id.green_checkbox);
        light_green_checkbox = (ImageButton) findViewById(R.id.light_green_checkbox);
        blue_checkbox = (ImageButton) findViewById(R.id.blue_checkbox);
        light_blue_checkbox = (ImageButton) findViewById(R.id.light_blue_checkbox);
        yellow_checkbox = (ImageButton) findViewById(R.id.yellow_checkbox);
        orange_checkbox = (ImageButton) findViewById(R.id.orange_checkbox);
        grey_checkbox = (ImageButton) findViewById(R.id.grey_checkbox);
        pink_checkbox = (ImageButton) findViewById(R.id.pink_checkbox);
        sand_checkbox = (ImageButton) findViewById(R.id.sand_checkbox);
        brown_checkbox = (ImageButton) findViewById(R.id.brown_checkbox);

        deepPurple = (ImageButton) findViewById(R.id.deepPurple);
        indigoCheckbox = (ImageButton) findViewById(R.id.indigoCheckbox);
        limeCheckbox = (ImageButton) findViewById(R.id.limeCheckbox);
        deepOrange = (ImageButton) findViewById(R.id.deepOrange);

        LinearLayout themeGroupPro = (LinearLayout) findViewById(R.id.themeGroupPro);
        if (Module.isPro()) {
            themeGroupPro.setVisibility(View.VISIBLE);
        } else themeGroupPro.setVisibility(View.GONE);

        setOnClickListener(red_checkbox, violet_checkbox, green_checkbox, light_green_checkbox,
                blue_checkbox, light_blue_checkbox, yellow_checkbox, orange_checkbox, grey_checkbox,
                pink_checkbox, sand_checkbox, brown_checkbox, deepPurple, deepOrange, indigoCheckbox,
                limeCheckbox);

        setUpRadio();
    }

    private void setOnClickListener(View... views){
        for (View view : views){
            view.setOnClickListener(listener);
        }
    }

    private View.OnClickListener listener = v -> colorSwitch(v.getId());

    private void saveTaskList() {
        sPrefs = new SharedPrefs(this);
        sPrefs.saveBoolean(Prefs.TASK_CHANGED, true);
        String listName = editField.getText().toString();
        if (listName.matches("")) {
            editField.setError(getString(R.string.must_be_not_empty));
            return;
        }
        TasksData data = new TasksData(TaskListManager.this);
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
        data.close();
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
        builder.setMessage(getString(R.string.delete_this_list));
        builder.setPositiveButton(getString(R.string.yes), (dialog, which) -> {
            dialog.dismiss();
            deleteList();
            finish();
        });
        builder.setNegativeButton(getString(R.string.no), (dialog, which) -> {
            dialog.dismiss();
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void deleteList() {
        TasksData data = new TasksData(TaskListManager.this);
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
        data.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.save_menu, menu);
        if (id != 0 && sysDef != 1) {
            menu.add(Menu.NONE, MENU_ITEM_DELETE, 100, R.string.delete_list);
        }
        return true;
    }

    private void colorSwitch(int radio) {
        if (radio == prevId) return;
        prevId = radio;
        disableAll();
        setSelected(radio);
        switch (radio) {
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
            default:
                if (Module.isPro()) {
                    switch (radio) {
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
                }
                break;
        }
    }

    private void setSelected(int radio) {
        findViewById(radio).setSelected(true);
    }

    private void disableAll() {
        red_checkbox.setSelected(false);
        violet_checkbox.setSelected(false);
        green_checkbox.setSelected(false);
        light_green_checkbox.setSelected(false);
        blue_checkbox.setSelected(false);
        light_blue_checkbox.setSelected(false);
        yellow_checkbox.setSelected(false);
        orange_checkbox.setSelected(false);
        grey_checkbox.setSelected(false);
        pink_checkbox.setSelected(false);
        sand_checkbox.setSelected(false);
        brown_checkbox.setSelected(false);
        deepOrange.setSelected(false);
        deepPurple.setSelected(false);
        limeCheckbox.setSelected(false);
        indigoCheckbox.setSelected(false);
    }

    private void setColor(int i){
        color = i;
        toolbar.setBackgroundColor(cSetter.getNoteColor(i));
        if (Module.isLollipop()) {
            getWindow().setStatusBarColor(cSetter.getNoteDarkColor(i));
        }
    }

    public void setUpRadio(){
        switch (color) {
            case 0:
                red_checkbox.setSelected(true);
                break;
            case 1:
                violet_checkbox.setSelected(true);
                break;
            case 2:
                green_checkbox.setSelected(true);
                break;
            case 3:
                light_green_checkbox.setSelected(true);
                break;
            case 4:
                blue_checkbox.setSelected(true);
                break;
            case 5:
                light_blue_checkbox.setSelected(true);
                break;
            case 6:
                yellow_checkbox.setSelected(true);
                break;
            case 7:
                orange_checkbox.setSelected(true);
                break;
            case 8:
                grey_checkbox.setSelected(true);
                break;
            case 9:
                pink_checkbox.setSelected(true);
                break;
            case 10:
                sand_checkbox.setSelected(true);
                break;
            case 11:
                brown_checkbox.setSelected(true);
                break;
            default:
                if (Module.isPro()) {
                    switch (color) {
                        case 12:
                            deepPurple.setSelected(true);
                            break;
                        case 13:
                            deepOrange.setSelected(true);
                            break;
                        case 14:
                            limeCheckbox.setSelected(true);
                            break;
                        case 15:
                            indigoCheckbox.setSelected(true);
                            break;
                        default:
                            green_checkbox.setSelected(true);
                            break;
                    }
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        new UpdatesHelper(TaskListManager.this).updateTasksWidget();
    }
}