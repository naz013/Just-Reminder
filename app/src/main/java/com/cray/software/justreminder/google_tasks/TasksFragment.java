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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.StartActivity;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.constants.TasksConstants;
import com.cray.software.justreminder.datas.models.TaskListItem;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.interfaces.NavigationCallbacks;
import com.cray.software.justreminder.utils.ViewUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TasksFragment extends Fragment {

    private static final String TAG = "TasksFragment";
    private ViewPager pager;
    private ArrayList<TaskListItem> taskListDatum;
    private int currentPos;
    private boolean onCreate = false;
    private NavigationCallbacks mCallbacks;
    private Activity mContext;

    public static TasksFragment newInstance() {
        return new TasksFragment();
    }

    public TasksFragment() {
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public static final int MENU_ITEM_EDIT = 12;
    public static final int MENU_ITEM_DELETE = 13;
    public static final int MENU_ITEM_CLEAR = 14;

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.tasks_menu, menu);
        if (currentPos != 0) {
            menu.add(Menu.NONE, MENU_ITEM_EDIT, 100, R.string.edit_list);
            String listId = taskListDatum.get(currentPos).getTaskList().getListId();
            com.cray.software.justreminder.google_tasks.TaskListItem listItem = TasksHelper.getInstance(getActivity()).getTaskList(listId);
            if (listItem != null) {
                if (listItem.getDef() != 1) {
                    menu.add(Menu.NONE, MENU_ITEM_DELETE, 100, getString(R.string.delete_list));
                }
            }
            menu.add(Menu.NONE, MENU_ITEM_CLEAR, 100, R.string.delete_completed_tasks);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_sync:
                new DelayedAsync(mContext, null).execute();
                return true;
            case R.id.action_add_list:
                startActivity(new Intent(mContext, TaskListActivity.class));
                return true;
            case MENU_ITEM_EDIT:
                if (currentPos != 0){
                    startActivity(new Intent(mContext, TaskListActivity.class)
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
        rootView.findViewById(R.id.wrapper).setBackgroundColor(ColorSetter.getInstance(mContext).getBackgroundStyle());
        loadData();
        onCreate = true;
        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (mContext == null) {
            mContext = (Activity) context;
        }
        if (mCallbacks == null) {
            try {
                mCallbacks = (NavigationCallbacks) context;
            } catch (ClassCastException e) {
                throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
            }
        }
        ((StartActivity) context).onSectionAttached(StartActivity.FRAGMENT_TASKS);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (mContext == null) {
            this.mContext = activity;
        }
        if (mCallbacks == null) {
            try {
                mCallbacks = (NavigationCallbacks) activity;
            } catch (ClassCastException e) {
                throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
            }
        }
        ((StartActivity) activity).onSectionAttached(StartActivity.FRAGMENT_TASKS);
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
        if (SharedPrefs.getInstance(mContext).getBoolean(Prefs.TASK_CHANGED)) {
            SharedPrefs.getInstance(mContext).putBoolean(Prefs.TASK_CHANGED, false);
            if (mCallbacks != null) {
                mCallbacks.onItemSelected(StartActivity.FRAGMENT_TASKS);
            }
        }
    }

    private void showDialog(){
        final String[] items = {getString(R.string.default_string),
                getString(R.string.by_date_az),
                getString(R.string.by_date_za),
                getString(R.string.active_first),
                getString(R.string.completed_first)};
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(getString(R.string.order));
        builder.setItems(items, (dialog, which) -> {
            SharedPrefs prefs = SharedPrefs.getInstance(mContext);
            if (which == 0) {
                prefs.putString(Prefs.TASKS_ORDER, Constants.ORDER_DEFAULT);
            } else if (which == 1) {
                prefs.putString(Prefs.TASKS_ORDER, Constants.ORDER_DATE_A_Z);
            } else if (which == 2) {
                prefs.putString(Prefs.TASKS_ORDER, Constants.ORDER_DATE_Z_A);
            } else if (which == 3) {
                prefs.putString(Prefs.TASKS_ORDER, Constants.ORDER_COMPLETED_Z_A);
            } else if (which == 4) {
                prefs.putString(Prefs.TASKS_ORDER, Constants.ORDER_COMPLETED_A_Z);
            }
            dialog.dismiss();
            loadData();
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void deleteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setCancelable(true);
        builder.setMessage(getString(R.string.delete_this_list));
        builder.setNegativeButton(getString(R.string.no), (dialog, which) -> {
            dialog.dismiss();
        });
        builder.setPositiveButton(getString(R.string.yes), (dialog, which) -> {
            deleteList();
            dialog.dismiss();
            if (mCallbacks != null) {
                mCallbacks.onItemSelected(StartActivity.FRAGMENT_TASKS);
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void deleteList() {
        com.cray.software.justreminder.google_tasks.TaskListItem taskListItem = taskListDatum.get(currentPos).getTaskList();
        if (taskListItem != null){
            String listId = taskListItem.getListId();
            int def = taskListItem.getDef();
            TasksHelper.getInstance(getActivity()).deleteTaskList(taskListItem.getId());
            TasksHelper.getInstance(getActivity()).deleteTasks(listId);
            new TaskListAsync(mContext, null, 0, 0, listId, TasksConstants.DELETE_TASK_LIST).execute();
            if (def == 1) {
                com.cray.software.justreminder.google_tasks.TaskListItem listItem = TasksHelper.getInstance(getActivity()).getTaskLists().get(0);
                TasksHelper.getInstance(getActivity()).setDefault(listItem.getId());
            }
        }
    }

    private void loadData() {
        taskListDatum = new ArrayList<>();
        List<com.cray.software.justreminder.google_tasks.TaskListItem> taskLists = getTaskLists();
        if (taskLists == null || taskLists.size() == 0) return;
        Map<String, Integer> colors = new HashMap<>();
        for (int i = 0; i < taskLists.size(); i++){
            com.cray.software.justreminder.google_tasks.TaskListItem item = taskLists.get(i);
            taskListDatum.add(new TaskListItem(item, getList(item.getListId()), i));
            if (i > 0) colors.put(item.getListId(), item.getColor());
        }
        Log.d(TAG, "loadData: " + colors.toString());
        int pos = SharedPrefs.getInstance(mContext).getInt(Prefs.LAST_LIST);
        final TasksPagerAdapter pagerAdapter = new TasksPagerAdapter(getChildFragmentManager(), taskListDatum, colors);
        pagerAdapter.setCallbacks(mCallbacks);
        pager.setAdapter(pagerAdapter);
        pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {

            }

            @Override
            public void onPageSelected(int i) {
                updateScreen(i);
                SharedPrefs.getInstance(mContext).putInt(Prefs.LAST_LIST, i);
                currentPos = i;
                mContext.invalidateOptionsMenu();
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
        pager.setCurrentItem(pos < taskListDatum.size() ? pos : 0);
        updateScreen(pos);
    }

    private void updateScreen(int pos) {
        if (mCallbacks != null) {
            ColorSetter mColor = ColorSetter.getInstance(mContext);
            if (pos == 0) {
                mCallbacks.onTitleChanged(getString(R.string.all));
                mCallbacks.onUiChanged(ViewUtils.getColor(mContext, mColor.colorPrimary()),
                        ViewUtils.getColor(mContext, mColor.colorPrimaryDark()),
                        ViewUtils.getColor(mContext, mColor.colorAccent()));
                mCallbacks.onListIdChanged(0);
            } else {
                com.cray.software.justreminder.google_tasks.TaskListItem taskList = taskListDatum.get(pos).getTaskList();
                mCallbacks.onTitleChanged(taskList.getTitle());
                int tmp = taskList.getColor();
                mCallbacks.onUiChanged(ViewUtils.getColor(mContext, mColor.colorPrimary(tmp)),
                        ViewUtils.getColor(mContext, mColor.colorPrimaryDark(tmp)),
                        ViewUtils.getColor(mContext, mColor.colorAccent(tmp)));
                long idS = taskList.getId();
                mCallbacks.onListIdChanged(idS);
            }
        }
    }

    private ArrayList<com.cray.software.justreminder.google_tasks.TaskListItem> getTaskLists() {
        ArrayList<com.cray.software.justreminder.google_tasks.TaskListItem> lists = new ArrayList<>();
        com.cray.software.justreminder.google_tasks.TaskListItem zeroItem = new com.cray.software.justreminder.google_tasks.TaskListItem();
        zeroItem.setTitle(getString(R.string.all));
        zeroItem.setColor(25);
        lists.add(zeroItem);
        for (com.cray.software.justreminder.google_tasks.TaskListItem item : TasksHelper.getInstance(getActivity()).getTaskLists()) lists.add(item);
        return lists;
    }

    private List<TaskItem> getList(String listId) {
        List<TaskItem> mData = new ArrayList<>();
        Log.d(TAG, "getList: " + listId);
        if (listId == null) {
            List<TaskItem> list = TasksHelper.getInstance(getActivity()).getTasks();
            mData.addAll(list);
        } else {
            List<TaskItem> list = TasksHelper.getInstance(getActivity()).getTasks(listId);
            mData.addAll(list);
        }
        return mData;
    }

    private void clearList() {
        String listId = taskListDatum.get(currentPos).getTaskList().getListId();
        TasksHelper.getInstance(getActivity()).deleteCompletedTasks(listId);
        new TaskListAsync(mContext, null, 0, 0, listId, TasksConstants.CLEAR_TASK_LIST).execute();
        if (mCallbacks != null) mCallbacks.onItemSelected(StartActivity.FRAGMENT_TASKS);
    }
}
