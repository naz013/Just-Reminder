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
import android.support.annotation.ColorRes;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

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
import com.cray.software.justreminder.views.ColorPickerView;

public class TaskListManager extends AppCompatActivity implements ColorPickerView.OnColorListener {
    private ColorSetter cSetter;
    private RoboCheckBox defaultCheck;

    private Toolbar toolbar;
    private RoboEditText editField;
    private TaskListItem mItem;

    private static final int MENU_ITEM_DELETE = 12;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cSetter = ColorSetter.getInstance(TaskListManager.this);
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
        ColorPickerView pickerView = (ColorPickerView) findViewById(R.id.pickerView);
        pickerView.setListener(this);
        pickerView.setSelectedColor(mItem.getColor());
        setColor(mItem.getColor());
    }

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

    private void setColor(int i){
        mItem.setColor(i);
        toolbar.setBackgroundColor(cSetter.getNoteColor(i));
        if (Module.isLollipop()) {
            getWindow().setStatusBarColor(cSetter.getNoteDarkColor(i));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        UpdatesHelper.getInstance(this).updateTasksWidget();
    }

    @Override
    public void onColorSelect(int code, @ColorRes int color) {
        setColor(code);
    }
}