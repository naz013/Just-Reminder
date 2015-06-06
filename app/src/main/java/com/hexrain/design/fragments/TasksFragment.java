package com.hexrain.design.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.TaskListManager;
import com.cray.software.justreminder.TaskManager;
import com.cray.software.justreminder.adapters.TasksRecyclerAdapter;
import com.cray.software.justreminder.async.DelayedAsync;
import com.cray.software.justreminder.async.TaskListAsync;
import com.cray.software.justreminder.databases.TasksData;
import com.cray.software.justreminder.datas.ListItems;
import com.cray.software.justreminder.datas.TaskListData;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.interfaces.SyncListener;
import com.cray.software.justreminder.interfaces.TasksConstants;
import com.hexrain.design.NavigationDrawerFragment;
import com.hexrain.design.ScreenManager;

import java.util.ArrayList;

public class TasksFragment extends Fragment implements View.OnTouchListener, SyncListener {

    ListView mList;
    TasksData DB;

    ArrayList<TaskListData> taskListDatum;
    int currentPos;
    Activity activity;

    private NavigationDrawerFragment.NavigationDrawerCallbacks mCallbacks;

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
        DB = new TasksData(getActivity());
    }

    public static final int MENU_ITEM_EDIT = 12;
    public static final int MENU_ITEM_DELETE = 13;
    public static final int MENU_ITEM_CLEAR = 14;

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.tasks_menu, menu);
        DB = new TasksData(activity);
        DB.open();
        if (currentPos != 0) {
            menu.add(Menu.NONE, MENU_ITEM_EDIT, 100, getString(R.string.string_edit_task_list));
            Cursor c = DB.getTasksList(taskListDatum.get(currentPos).getListId());
            if (c != null && c.moveToFirst()) {
                int def = c.getInt(c.getColumnIndex(TasksConstants.SYSTEM_DEFAULT));
                if (def != 1)
                    menu.add(Menu.NONE, MENU_ITEM_DELETE, 100, getString(R.string.string_delete_task_list));
            }
            if (c != null) c.close();
            menu.add(Menu.NONE, MENU_ITEM_CLEAR, 100, getString(R.string.string_delete_completed_tasks));
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_sync:
                new DelayedAsync(activity, this).execute();
                return true;
            case R.id.action_add_list:
                startActivity(new Intent(activity, TaskListManager.class));
                return true;
            case MENU_ITEM_EDIT:
                if (currentPos != 0){
                    startActivity(new Intent(activity, TaskListManager.class)
                            .putExtra(Constants.ITEM_ID_INTENT, taskListDatum.get(currentPos).getId()));
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
        View rootView = inflater.inflate(R.layout.fragment_simple_list_layout, container, false);

        TextView textView = (TextView) rootView.findViewById(R.id.emptyList);
        textView.setText(getString(R.string.string_no_tasks));
        mList = (ListView) rootView.findViewById(R.id.listView);
        mList.setOnTouchListener(this);
        mList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        mList.setItemsCanFocus(true);
        mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startActivity(new Intent(activity, TaskManager.class)
                        .putExtra(Constants.ITEM_ID_INTENT, id)
                        .putExtra(TasksConstants.INTENT_ACTION, TasksConstants.EDIT));
            }
        });

        if (mCallbacks != null)
            mCallbacks.onNavigationDrawerItemSelected(ScreenManager.TASKS_AUTHORIZATION);

        loadData();
        SharedPrefs sPrefs = new SharedPrefs(activity);
        sPrefs.saveBoolean(Constants.APP_UI_PREFERENCES_TASK_CHANGED, false);

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;
        try {
            mCallbacks = (NavigationDrawerFragment.NavigationDrawerCallbacks) activity;
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
        SharedPrefs sPrefs = new SharedPrefs(activity);
        if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_TASK_CHANGED)) {
            if (mCallbacks != null) mCallbacks.onNavigationDrawerItemSelected(ScreenManager.FRAGMENT_TASKS);
            sPrefs.saveBoolean(Constants.APP_UI_PREFERENCES_TASK_CHANGED, true);
        }
        super.onResume();
    }

    private void showDialog(){
        final CharSequence[] items = {getString(R.string.string_default),
                getString(R.string.sort_item_by_date_a_z),
                getString(R.string.sort_item_by_date_z_a),
                getString(R.string.string_active_first),
                getString(R.string.string_completed_first)};
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(getString(R.string.menu_order_by));
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                SharedPrefs prefs = new SharedPrefs(activity);
                if (item == 0) {
                    prefs.savePrefs(Constants.APP_UI_PREFERENCES_TASKS_ORDER, Constants.ORDER_DEFAULT);
                } else if (item == 1) {
                    prefs.savePrefs(Constants.APP_UI_PREFERENCES_TASKS_ORDER, Constants.ORDER_DATE_A_Z);
                } else if (item == 2) {
                    prefs.savePrefs(Constants.APP_UI_PREFERENCES_TASKS_ORDER, Constants.ORDER_DATE_Z_A);
                } else if (item == 3) {
                    prefs.savePrefs(Constants.APP_UI_PREFERENCES_TASKS_ORDER, Constants.ORDER_COMPLETED_Z_A);
                } else if (item == 4) {
                    prefs.savePrefs(Constants.APP_UI_PREFERENCES_TASKS_ORDER, Constants.ORDER_COMPLETED_A_Z);
                }
                dialog.dismiss();
                loadData();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void deleteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setCancelable(true);
        String title = taskListDatum.get(currentPos).getTitle();
        builder.setTitle(getString(R.string.string_delete_task_list) + " " + title);
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
                if (mCallbacks != null)
                    mCallbacks.onNavigationDrawerItemSelected(ScreenManager.FRAGMENT_TASKS);
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void deleteList() {
        DB = new TasksData(activity);
        DB.open();
        long id = taskListDatum.get(currentPos).getId();
        Cursor c = DB.getTasksList(id);
        if (c != null && c.moveToFirst()){
            String listId = c.getString(c.getColumnIndex(TasksConstants.COLUMN_LIST_ID));
            int def = c.getInt(c.getColumnIndex(TasksConstants.COLUMN_DEFAULT));
            DB.deleteTasksList(id);
            new TaskListAsync(activity, null, 0, 0, listId, TasksConstants.DELETE_TASK_LIST).execute();
            Cursor x = DB.getTasks(listId);
            if (x != null && x.moveToFirst()){
                do {
                    DB.deleteTask(x.getLong(x.getColumnIndex(TasksConstants.COLUMN_ID)));
                } while (x.moveToNext());
            }
            if (x != null) x.close();
            if (def == 1){
                Cursor cc = DB.getTasksLists();
                if (cc != null && cc.moveToFirst()){
                    DB.setDefault(cc.getLong(cc.getColumnIndex(TasksConstants.COLUMN_ID)));
                }
                if (cc != null) cc.close();
            }
        }
        if (c != null) c.close();
    }

    private void loadData() {
        SharedPrefs sPrefs = new SharedPrefs(activity);
        taskListDatum = new ArrayList<>();
        taskListDatum.clear();
        taskListDatum.add(new TaskListData(getString(R.string.string_all_tasks), 0, Constants.TASKS_ALL, 25));
        DB = new TasksData(activity);
        DB.open();
        Cursor c = DB.getTasksLists();
        if (c != null && c.moveToFirst()){
            do {
                taskListDatum.add(new TaskListData(c.getString(c.getColumnIndex(TasksConstants.COLUMN_TITLE)),
                        c.getLong(c.getColumnIndex(TasksConstants.COLUMN_ID)),
                        c.getString(c.getColumnIndex(TasksConstants.COLUMN_LIST_ID)),
                        c.getInt(c.getColumnIndex(TasksConstants.COLUMN_COLOR))));
            } while (c.moveToNext());
        }
        if (c != null) c.close();

        int pos = sPrefs.loadInt(Constants.APP_UI_PREFERENCES_LAST_LIST);

        if (taskListDatum.size() > pos) setList(pos, 0);
        else setList(0, 0);
    }

    private void setList(final int position, final int i){
        if (mCallbacks != null) {
            ColorSetter cSetter = new ColorSetter(activity);
            if (position == 0) {
                mCallbacks.onTitleChanged(getString(R.string.string_all_tasks));
                mCallbacks.onUiChanged(cSetter.colorSetter(), cSetter.colorStatus(), cSetter.colorChooser());
            } else {
                mCallbacks.onTitleChanged(taskListDatum.get(position).getTitle());
                int tmp = cSetter.getNoteColor(taskListDatum.get(position).getColor());
                mCallbacks.onUiChanged(getResources().getColor(tmp), cSetter.getNoteDarkColor(tmp),
                        cSetter.getNoteLightColor(tmp));
            }
        }

        currentPos = position;
        if (currentPos == 0) {
            long ids = 0;
            if (mCallbacks != null) mCallbacks.onListIdChanged(ids);
        } else {
            long idS = taskListDatum.get(currentPos).getId();
            if (mCallbacks != null) mCallbacks.onListIdChanged(idS);
        }
        SharedPrefs sPrefs = new SharedPrefs(activity);
        sPrefs.saveInt(Constants.APP_UI_PREFERENCES_LAST_LIST, position);
        getActivity().invalidateOptionsMenu();

        if (i != 0) {
            if (i > 0) {
                Animation slide = AnimationUtils.loadAnimation(activity, R.anim.slide_right_bounce_out);
                mList.startAnimation(slide);
                mList.setVisibility(View.GONE);
            } else {
                Animation slide = AnimationUtils.loadAnimation(activity, R.anim.slide_left_bounce_out);
                mList.startAnimation(slide);
                mList.setVisibility(View.GONE);
            }
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                loadList(taskListDatum.get(position).getListId(), i);
            }
        }, 300);
    }

    TasksRecyclerAdapter adapter;

    private void loadList(String id, int i){
        if (id == null){
            ArrayList<ListItems> mData = new ArrayList<>();
            mData.clear();
            DB.open();
            Cursor c = DB.getTasks();
            if (c != null && c.moveToFirst()){
                do {
                    String title = c.getString(c.getColumnIndex(TasksConstants.COLUMN_TITLE));
                    if (title != null && !title.matches("")) {
                        long date = c.getLong(c.getColumnIndex(TasksConstants.COLUMN_DUE));
                        String taskId = c.getString(c.getColumnIndex(TasksConstants.COLUMN_TASK_ID));
                        String listId = c.getString(c.getColumnIndex(TasksConstants.COLUMN_LIST_ID));
                        String checks = c.getString(c.getColumnIndex(TasksConstants.COLUMN_STATUS));
                        String note = c.getString(c.getColumnIndex(TasksConstants.COLUMN_NOTES));
                        long mId = c.getLong(c.getColumnIndex(TasksConstants.COLUMN_ID));
                        mData.add(new ListItems(title, mId, checks, taskId, date, listId, note));
                    }
                } while (c.moveToNext());
            }
            adapter = new TasksRecyclerAdapter(activity, mData, this);
            mList.setAdapter(adapter);
            if (mCallbacks != null) mCallbacks.onListChange(mList);
            if (c != null) c.close();
        } else if (id.matches(Constants.TASKS_ALL)){
            ArrayList<ListItems> mData = new ArrayList<>();
            mData.clear();
            DB.open();
            Cursor c = DB.getTasks();
            if (c != null && c.moveToFirst()){
                do {
                    String title = c.getString(c.getColumnIndex(TasksConstants.COLUMN_TITLE));
                    if (title != null && !title.matches("")) {
                        long date = c.getLong(c.getColumnIndex(TasksConstants.COLUMN_DUE));
                        String taskId = c.getString(c.getColumnIndex(TasksConstants.COLUMN_TASK_ID));
                        String listId = c.getString(c.getColumnIndex(TasksConstants.COLUMN_LIST_ID));
                        String checks = c.getString(c.getColumnIndex(TasksConstants.COLUMN_STATUS));
                        String note = c.getString(c.getColumnIndex(TasksConstants.COLUMN_NOTES));
                        long mId = c.getLong(c.getColumnIndex(TasksConstants.COLUMN_ID));
                        mData.add(new ListItems(title, mId, checks, taskId, date, listId, note));
                    }
                } while (c.moveToNext());
            }
            adapter = new TasksRecyclerAdapter(activity, mData, this);
            mList.setAdapter(adapter);
            if (mCallbacks != null) mCallbacks.onListChange(mList);
            if (c != null) c.close();
        } else {
            ArrayList<ListItems> mData = new ArrayList<>();
            mData.clear();
            DB = DB.open();
            Cursor c = DB.getTasks(id);
            if (c != null && c.moveToFirst()){
                do {
                    String title = c.getString(c.getColumnIndex(TasksConstants.COLUMN_TITLE));
                    if (title != null && !title.matches("")) {
                        long date = c.getLong(c.getColumnIndex(TasksConstants.COLUMN_DUE));
                        String taskId = c.getString(c.getColumnIndex(TasksConstants.COLUMN_TASK_ID));
                        String listId = c.getString(c.getColumnIndex(TasksConstants.COLUMN_LIST_ID));
                        String checks = c.getString(c.getColumnIndex(TasksConstants.COLUMN_STATUS));
                        String note = c.getString(c.getColumnIndex(TasksConstants.COLUMN_NOTES));
                        long mId = c.getLong(c.getColumnIndex(TasksConstants.COLUMN_ID));
                        mData.add(new ListItems(title, mId, checks, taskId, date, listId, note));
                    }
                } while (c.moveToNext());
            }

            adapter = new TasksRecyclerAdapter(activity, mData, this);
            mList.setAdapter(adapter);
            if (mCallbacks != null) mCallbacks.onListChange(mList);
            if (c != null) c.close();
        }
        if (i != 0) {
            if (i > 0) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Animation slide = AnimationUtils.loadAnimation(activity, R.anim.slide_right_bounce);
                        mList.startAnimation(slide);
                        mList.setVisibility(View.VISIBLE);
                    }
                }, 320);
            } else {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Animation slide = AnimationUtils.loadAnimation(activity, R.anim.slide_left_bounce);
                        mList.startAnimation(slide);
                        mList.setVisibility(View.VISIBLE);
                    }
                }, 320);
            }
        }
    }

    private void clearList() {
        DB = new TasksData(activity);
        DB.open();
        String listId = taskListDatum.get(currentPos).getListId();
        Cursor c = DB.getTasks(listId);
        if (c != null && c.moveToFirst()){
            do {
                long ids = c.getLong(c.getColumnIndex(TasksConstants.COLUMN_ID));
                String status = c.getString(c.getColumnIndex(TasksConstants.COLUMN_STATUS));
                if (status.matches(Constants.TASKS_COMPLETE)){
                    DB.deleteTask(ids);
                }
            } while (c.moveToNext());
        }

        if (c != null) c.close();
        new TaskListAsync(activity, null, 0, 0, listId, TasksConstants.CLEAR_TASK_LIST).execute();

        if (mCallbacks != null) mCallbacks.onNavigationDrawerItemSelected(ScreenManager.FRAGMENT_TASKS);
    }

    static final int MIN_DISTANCE = 120;
    private float downX;
    private float downY;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch(event.getAction()){
            case MotionEvent.ACTION_DOWN: {
                downX = event.getX();
                downY = event.getY();
                return true;
            }
            case MotionEvent.ACTION_UP: {
                float upX = event.getX();
                float upY = event.getY();

                float deltaX = downX - upX;
                float deltaY = downY - upY;
                if(Math.abs(deltaX) > Math.abs(deltaY)) {
                    if (Math.abs(deltaX) > MIN_DISTANCE) {
                        if (deltaX < 0) {
                            this.onRightSwipe();
                            return true;
                        }
                        if (deltaX > 0) {
                            this.onLeftSwipe();
                            return true;
                        }
                    } else {
                        return false; // We don't consume the event
                    }
                }

                return true;
            }
        }
        return false;
    }

    private void onLeftSwipe() {
        switchIt(-1);
    }

    private void switchIt(int i) {
        int length = taskListDatum.size();
        if (length > 1){
            if (i < 0){
                if (currentPos == 0) {
                    setList(length - 1, i);
                } else {
                    setList(currentPos - 1, i);
                }
            } else {
                if (currentPos == length - 1){
                    setList(0, i);
                } else {
                    setList(currentPos + 1, i);
                }
            }
        }
    }

    private void onRightSwipe() {
        switchIt(1);
    }

    @Override
    public void endExecution(boolean result) {
        if (mCallbacks != null) mCallbacks.onNavigationDrawerItemSelected(ScreenManager.FRAGMENT_TASKS);
    }
}
