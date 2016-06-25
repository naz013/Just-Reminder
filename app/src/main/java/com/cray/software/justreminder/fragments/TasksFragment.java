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

package com.cray.software.justreminder.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.ScreenManager;
import com.cray.software.justreminder.TaskListManager;
import com.cray.software.justreminder.adapters.TasksPagerAdapter;
import com.cray.software.justreminder.async.DelayedAsync;
import com.cray.software.justreminder.async.TaskListAsync;
import com.cray.software.justreminder.cloud.GTasksHelper;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.constants.TasksConstants;
import com.cray.software.justreminder.databases.TasksData;
import com.cray.software.justreminder.datas.models.Task;
import com.cray.software.justreminder.datas.models.TaskList;
import com.cray.software.justreminder.datas.models.TaskListData;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.interfaces.NavigationCallbacks;
import com.cray.software.justreminder.utils.ViewUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TasksFragment extends Fragment {

    private ViewPager pager;

    private ArrayList<TaskListData> taskListDatum;
    private Map<String, Integer> map = new HashMap<>();
    private int currentPos;
    private Activity activity;

    private boolean onCreate = false;

    private NavigationCallbacks mCallbacks;

    public static TasksFragment newInstance() {
        return new TasksFragment();
    }

    public TasksFragment() {
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Indicate that this fragment would like to influence the set of actions in the action bar.
        setHasOptionsMenu(true);
    }

    public static final int MENU_ITEM_EDIT = 12;
    public static final int MENU_ITEM_DELETE = 13;
    public static final int MENU_ITEM_CLEAR = 14;

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.tasks_menu, menu);
        TasksData db = new TasksData(activity);
        db.open();
        if (currentPos != 0) {
            menu.add(Menu.NONE, MENU_ITEM_EDIT, 100, R.string.edit_list);
            Cursor c = db.getTasksList(taskListDatum.get(currentPos).getTaskList().getListId());
            if (c != null && c.moveToFirst()) {
                int def = c.getInt(c.getColumnIndex(TasksConstants.SYSTEM_DEFAULT));
                if (def != 1) {
                    menu.add(Menu.NONE, MENU_ITEM_DELETE, 100, getString(R.string.delete_list));
                }
            }
            if (c != null) {
                c.close();
            }
            menu.add(Menu.NONE, MENU_ITEM_CLEAR, 100, R.string.delete_completed_tasks);
        }
        db.close();
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_sync:
                new DelayedAsync(activity, null).execute();
                return true;
            case R.id.action_add_list:
                startActivity(new Intent(activity, TaskListManager.class));
                return true;
            case MENU_ITEM_EDIT:
                if (currentPos != 0){
                    startActivity(new Intent(activity, TaskListManager.class)
                            .putExtra(Constants.ITEM_ID_INTENT, taskListDatum.get(currentPos).getTaskList().getId()));
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
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_calendar, container, false);
        pager = (ViewPager) rootView.findViewById(R.id.pager);
        loadData();
        onCreate = true;
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;
        try {
            mCallbacks = (NavigationCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
        }
        ((ScreenManager)activity).onSectionAttached(ScreenManager.FRAGMENT_TASKS);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!onCreate) {
            loadData();
        }
        onCreate = false;

        SharedPrefs sPrefs = new SharedPrefs(activity);
        if (sPrefs.loadBoolean(Prefs.TASK_CHANGED)) {
            sPrefs.saveBoolean(Prefs.TASK_CHANGED, false);
            if (mCallbacks != null) {
                mCallbacks.onItemSelected(ScreenManager.FRAGMENT_TASKS);
            }
        }
    }

    private void showDialog(){
        final String[] items = {getString(R.string.default_string),
                getString(R.string.by_date_az),
                getString(R.string.by_date_za),
                getString(R.string.active_first),
                getString(R.string.completed_first)};
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(getString(R.string.order));
        builder.setItems(items, (dialog, which) -> {
            SharedPrefs prefs = new SharedPrefs(activity);
            if (which == 0) {
                prefs.savePrefs(Prefs.TASKS_ORDER, Constants.ORDER_DEFAULT);
            } else if (which == 1) {
                prefs.savePrefs(Prefs.TASKS_ORDER, Constants.ORDER_DATE_A_Z);
            } else if (which == 2) {
                prefs.savePrefs(Prefs.TASKS_ORDER, Constants.ORDER_DATE_Z_A);
            } else if (which == 3) {
                prefs.savePrefs(Prefs.TASKS_ORDER, Constants.ORDER_COMPLETED_Z_A);
            } else if (which == 4) {
                prefs.savePrefs(Prefs.TASKS_ORDER, Constants.ORDER_COMPLETED_A_Z);
            }
            dialog.dismiss();
            loadData();
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void deleteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setCancelable(true);
        builder.setMessage(getString(R.string.delete_this_list));
        builder.setNegativeButton(getString(R.string.no), (dialog, which) -> {
            dialog.dismiss();
        });
        builder.setPositiveButton(getString(R.string.yes), (dialog, which) -> {
            deleteList();
            dialog.dismiss();
            if (mCallbacks != null) {
                mCallbacks.onItemSelected(ScreenManager.FRAGMENT_TASKS);
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void deleteList() {
        TasksData db = new TasksData(activity);
        db.open();
        long id = taskListDatum.get(currentPos).getTaskList().getId();
        Cursor c = db.getTasksList(id);
        if (c != null && c.moveToFirst()){
            String listId = c.getString(c.getColumnIndex(TasksConstants.COLUMN_LIST_ID));
            int def = c.getInt(c.getColumnIndex(TasksConstants.COLUMN_DEFAULT));
            db.deleteTasksList(id);
            new TaskListAsync(activity, null, 0, 0, listId, TasksConstants.DELETE_TASK_LIST).execute();
            Cursor x = db.getTasks(listId);
            if (x != null && x.moveToFirst()){
                do {
                    db.deleteTask(x.getLong(x.getColumnIndex(TasksConstants.COLUMN_ID)));
                } while (x.moveToNext());
            }
            if (x != null) x.close();
            if (def == 1){
                Cursor cc = db.getTasksLists();
                if (cc != null && cc.moveToFirst()){
                    db.setDefault(cc.getLong(cc.getColumnIndex(TasksConstants.COLUMN_ID)));
                }
                if (cc != null) cc.close();
            }
        }
        if (c != null) c.close();
        db.close();
    }

    private void loadData() {
        SharedPrefs sPrefs = new SharedPrefs(activity);
        taskListDatum = new ArrayList<>();
        taskListDatum.clear();

        ArrayList<TaskList> taskLists = getTaskLists();
        if (taskLists == null || taskLists.size() == 0) return;
        taskListDatum.add(new TaskListData(taskLists.get(0), getList(null), 0));
        for (int position = 1; position < taskLists.size(); position++){
            taskListDatum.add(new TaskListData(taskLists.get(position), getList(taskLists.get(position)), position));
        }

        int pos = sPrefs.loadInt(Prefs.LAST_LIST);

        final TasksPagerAdapter pagerAdapter =
                new TasksPagerAdapter(getChildFragmentManager(), taskListDatum);
        pagerAdapter.setCallbacks(mCallbacks);
        pager.setAdapter(pagerAdapter);
        pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {

            }

            @Override
            public void onPageSelected(int i) {
                if (mCallbacks != null) {
                    ColorSetter mColor = new ColorSetter(activity);
                    if (i == 0) {
                        mCallbacks.onTitleChanged(getString(R.string.all));
                        mCallbacks.onUiChanged(ViewUtils.getColor(getActivity(), mColor.colorPrimary()),
                                ViewUtils.getColor(getActivity(), mColor.colorPrimaryDark()),
                                ViewUtils.getColor(getActivity(), mColor.colorAccent()));
                        mCallbacks.onListIdChanged(0);
                    } else {
                        TaskList taskList = taskListDatum.get(i).getTaskList();
                        mCallbacks.onTitleChanged(taskList.getTitle());
                        int tmp = taskList.getColor();
                        mCallbacks.onUiChanged(mColor.getNoteColor(tmp), mColor.getNoteDarkColor(tmp),
                                ViewUtils.getColor(getActivity(), mColor.colorAccent(tmp)));
                        long idS = taskList.getId();
                        mCallbacks.onListIdChanged(idS);
                    }
                }

                SharedPrefs sPrefs = new SharedPrefs(activity);
                sPrefs.saveInt(Prefs.LAST_LIST, i);
                currentPos = i;
                getActivity().invalidateOptionsMenu();
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

        pager.setCurrentItem(pos < taskListDatum.size() ? pos : 0);
        if (mCallbacks != null) {
            ColorSetter mColor = new ColorSetter(activity);
            if (pos == 0) {
                mCallbacks.onTitleChanged(getString(R.string.all));
                mCallbacks.onUiChanged(ViewUtils.getColor(getActivity(), mColor.colorPrimary()),
                        ViewUtils.getColor(getActivity(), mColor.colorPrimaryDark()),
                        ViewUtils.getColor(getActivity(), mColor.colorAccent()));
                mCallbacks.onListIdChanged(0);
            } else {
                TaskList taskList = taskListDatum.get(pos).getTaskList();
                mCallbacks.onTitleChanged(taskList.getTitle());
                int tmp = taskList.getColor();
                mCallbacks.onUiChanged(ViewUtils.getColor(getActivity(), mColor.colorPrimary(tmp)),
                        ViewUtils.getColor(getActivity(), mColor.colorPrimaryDark(tmp)),
                        ViewUtils.getColor(getActivity(), mColor.colorAccent(tmp)));
                long idS = taskList.getId();
                mCallbacks.onListIdChanged(idS);
            }
        }
    }

    private ArrayList<TaskList> getTaskLists() {
        ArrayList<TaskList> lists = new ArrayList<>();
        lists.clear();
        map = new HashMap<>();
        map.clear();
        lists.add(new TaskList(getString(R.string.all), 0, GTasksHelper.TASKS_ALL, 25));
        TasksData db = new TasksData(activity);
        db.open();
        Cursor c = db.getTasksLists();
        if (c != null && c.moveToFirst()){
            do {
                String listId = c.getString(c.getColumnIndex(TasksConstants.COLUMN_LIST_ID));
                String title = c.getString(c.getColumnIndex(TasksConstants.COLUMN_TITLE));
                long id = c.getLong(c.getColumnIndex(TasksConstants.COLUMN_ID));
                int color = c.getInt(c.getColumnIndex(TasksConstants.COLUMN_COLOR));
                lists.add(new TaskList(title, id, listId, color));
                map.put(listId, color);
            } while (c.moveToNext());
        }
        if (c != null) c.close();
        db.close();
        return lists;
    }

    private ArrayList<Task> getList(TaskList taskList) {
        TasksData db = new TasksData(activity);
        db.open();
        ArrayList<Task> mData = new ArrayList<>();
        mData.clear();
        if (taskList == null) {
            Cursor c = db.getTasks();
            if (c != null && c.moveToFirst()) {
                do {
                    String title = c.getString(c.getColumnIndex(TasksConstants.COLUMN_TITLE));
                    if (title != null && !title.matches("")) {
                        long date = c.getLong(c.getColumnIndex(TasksConstants.COLUMN_DUE));
                        String taskId = c.getString(c.getColumnIndex(TasksConstants.COLUMN_TASK_ID));
                        String listID = c.getString(c.getColumnIndex(TasksConstants.COLUMN_LIST_ID));
                        String checks = c.getString(c.getColumnIndex(TasksConstants.COLUMN_STATUS));
                        String note = c.getString(c.getColumnIndex(TasksConstants.COLUMN_NOTES));
                        long mId = c.getLong(c.getColumnIndex(TasksConstants.COLUMN_ID));
                        int color = 0;
                        if (map.containsKey(listID)) color = map.get(listID);
                        mData.add(new Task(title, mId, checks, taskId, date, listID, note, color));
                    }
                } while (c.moveToNext());
            }
            if (c != null) c.close();
        } else {
            Cursor c = db.getTasks(taskList.getListId());
            if (c != null && c.moveToFirst()){
                do {
                    String title = c.getString(c.getColumnIndex(TasksConstants.COLUMN_TITLE));
                    if (title != null && !title.matches("")) {
                        long date = c.getLong(c.getColumnIndex(TasksConstants.COLUMN_DUE));
                        String taskId = c.getString(c.getColumnIndex(TasksConstants.COLUMN_TASK_ID));
                        String checks = c.getString(c.getColumnIndex(TasksConstants.COLUMN_STATUS));
                        String note = c.getString(c.getColumnIndex(TasksConstants.COLUMN_NOTES));
                        long mId = c.getLong(c.getColumnIndex(TasksConstants.COLUMN_ID));
                        mData.add(new Task(title, mId, checks, taskId, date, taskList.getListId(),
                                note, taskList.getColor()));
                    }
                } while (c.moveToNext());
            }
            if (c != null) c.close();
        }
        db.close();
        return mData;
    }

    private void clearList() {
        TasksData db = new TasksData(activity);
        db.open();
        String listId = taskListDatum.get(currentPos).getTaskList().getListId();
        Cursor c = db.getTasks(listId);
        if (c != null && c.moveToFirst()){
            do {
                long ids = c.getLong(c.getColumnIndex(TasksConstants.COLUMN_ID));
                String status = c.getString(c.getColumnIndex(TasksConstants.COLUMN_STATUS));
                if (status.matches(GTasksHelper.TASKS_COMPLETE)){
                    db.deleteTask(ids);
                }
            } while (c.moveToNext());
        }

        if (c != null) c.close();
        db.close();
        new TaskListAsync(activity, null, 0, 0, listId, TasksConstants.CLEAR_TASK_LIST).execute();

        if (mCallbacks != null) mCallbacks.onItemSelected(ScreenManager.FRAGMENT_TASKS);
    }
}
