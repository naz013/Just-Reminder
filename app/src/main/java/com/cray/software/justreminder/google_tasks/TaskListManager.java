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

package com.cray.software.justreminder.google_tasks;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.app_widgets.UpdatesHelper;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.constants.TasksConstants;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.modules.Module;
import com.cray.software.justreminder.roboto_views.RoboCheckBox;
import com.cray.software.justreminder.roboto_views.RoboEditText;
import com.cray.software.justreminder.utils.ViewUtils;

public class TaskListManager extends AppCompatActivity {
    private ColorSetter cSetter = new ColorSetter(TaskListManager.this);
    private ImageButton red_checkbox, violet_checkbox, green_checkbox, light_green_checkbox,
            blue_checkbox, light_blue_checkbox, yellow_checkbox, orange_checkbox, grey_checkbox,
            pink_checkbox, sand_checkbox, brown_checkbox, deepPurple, indigoCheckbox, limeCheckbox,
            deepOrange;
    private RoboCheckBox defaultCheck;

    private Toolbar toolbar;
    private RoboEditText editField;
    private TaskListItem mItem;
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
        editField = (RoboEditText) findViewById(R.id.editField);
        defaultCheck = (RoboCheckBox) findViewById(R.id.defaultCheck);
        findViewById(R.id.windowBackground).setBackgroundColor(cSetter.getBackgroundStyle());
        Intent intent = getIntent();
        long id = intent.getLongExtra(Constants.ITEM_ID_INTENT, 0);
        mItem = TasksHelper.getInstance(this).getTaskList(id);
        if (mItem != null){
            editField.setText(mItem.getTitle());
            if (mItem.getDef() == 1){
                defaultCheck.setChecked(true);
                defaultCheck.setEnabled(false);
            }
        } else {
            mItem = new TaskListItem();
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
        SharedPrefs.getInstance(this).putBoolean(Prefs.TASK_CHANGED, true);
        String listName = editField.getText().toString();
        if (listName.matches("")) {
            editField.setError(getString(R.string.must_be_not_empty));
            return;
        }
        if (mItem == null) {
            mItem = new TaskListItem();
        }
        mItem.setTitle(listName);
        mItem.setUpdated(System.currentTimeMillis());
        if (defaultCheck.isChecked()){
            mItem.setDef(1);
            TaskListItem defList = TasksHelper.getInstance(this).getDefaultTaskList();
            if (defList != null) {
                defList.setDef(0);
                TasksHelper.getInstance(this).saveTaskList(defList);
            }
        }
        boolean isNew = mItem.getId() == 0;
        long id = TasksHelper.getInstance(this).saveTaskList(mItem);
        if (isNew) {
            new TaskListAsync(TaskListManager.this, listName, id, mItem.getColor(), null, TasksConstants.INSERT_TASK_LIST).execute();
        } else {
            new TaskListAsync(TaskListManager.this, listName, id, mItem.getColor(), mItem.getListId(), TasksConstants.UPDATE_TASK_LIST).execute();
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
        if (mItem != null){
            String listId = mItem.getListId();
            int def = mItem.getDef();
            TasksHelper.getInstance(this).deleteTaskList(mItem.getId());
            TasksHelper.getInstance(this).deleteTasks(listId);
            new TaskListAsync(TaskListManager.this, null, 0, 0, listId, TasksConstants.DELETE_TASK_LIST).execute();
            if (def == 1) {
                TaskListItem listItem = TasksHelper.getInstance(this).getTaskLists().get(0);
                TasksHelper.getInstance(this).setDefault(listItem.getId());
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.save_menu, menu);
        if (mItem.getId() != 0 && mItem.getSystemDefault() != 1) {
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
        mItem.setColor(i);
        toolbar.setBackgroundColor(cSetter.getNoteColor(i));
        if (Module.isLollipop()) {
            getWindow().setStatusBarColor(cSetter.getNoteDarkColor(i));
        }
    }

    public void setUpRadio(){
        switch (mItem.getColor()) {
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
                    switch (mItem.getColor()) {
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
        UpdatesHelper.getInstance(this).updateTasksWidget();
    }
}