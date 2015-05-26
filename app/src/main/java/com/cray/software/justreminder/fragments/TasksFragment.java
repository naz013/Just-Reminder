package com.cray.software.justreminder.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ListView;

import com.cray.software.justreminder.databases.TasksData;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.datas.ListItems;
import com.cray.software.justreminder.interfaces.SyncListener;
import com.cray.software.justreminder.interfaces.TasksConstants;

import java.util.ArrayList;

public class TasksFragment extends ListFragment implements SyncListener {

    static final String ARGUMENT_PAGE_NUMBER = "arg_page_number";
    static final String ARGUMENT_IDS_LIST = "arg_ids_list";

    int pageNumber;
    String mId;

    public static TasksFragment newInstance(int page, String id) {
        TasksFragment pageFragment = new TasksFragment();
        Bundle arguments = new Bundle();
        arguments.putInt(ARGUMENT_PAGE_NUMBER, page);
        arguments.putString(ARGUMENT_IDS_LIST, id);
        pageFragment.setArguments(arguments);
        return pageFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pageNumber = getArguments().getInt(ARGUMENT_PAGE_NUMBER);
        mId = getArguments().getString(ARGUMENT_IDS_LIST);
        loadData();
    }

    private void loadData() {
        TasksData data = new TasksData(getActivity());
        data.open();
        if (mId.matches(Constants.TASKS_ALL)){
            ArrayList<ListItems> mData = new ArrayList<>();
            mData.clear();
            Cursor c = data.getTasks();

            if (c != null && c.moveToFirst()){
                do {
                    String title = c.getString(c.getColumnIndex(TasksConstants.COLUMN_TITLE));
                    if (title != null && !title.matches("")) {
                        long date = c.getLong(c.getColumnIndex(TasksConstants.COLUMN_DUE));
                        String taskId = c.getString(c.getColumnIndex(TasksConstants.COLUMN_TASK_ID));
                        String listId = c.getString(c.getColumnIndex(TasksConstants.COLUMN_LIST_ID));
                        String checks = c.getString(c.getColumnIndex(TasksConstants.COLUMN_STATUS));
                        String note = c.getString(c.getColumnIndex(TasksConstants.COLUMN_STATUS));
                        long mId = c.getLong(c.getColumnIndex(TasksConstants.COLUMN_ID));
                        mData.add(new ListItems(title, mId, checks, taskId, date, listId, note));
                    }
                } while (c.moveToNext());
            }
            //setListAdapter(new TasksRecyclerAdapter(getActivity(), mData, this));

            if (c != null) {
                c.close();
            }
        } else {
            ArrayList<ListItems> mData = new ArrayList<>();
            mData.clear();
            Cursor c = data.getTasks(mId);

            if (c != null && c.moveToFirst()){
                do {
                    String title = c.getString(c.getColumnIndex(TasksConstants.COLUMN_TITLE));
                    if (title != null && !title.matches("")) {
                        long date = c.getLong(c.getColumnIndex(TasksConstants.COLUMN_DUE));
                        String taskId = c.getString(c.getColumnIndex(TasksConstants.COLUMN_TASK_ID));
                        String listId = c.getString(c.getColumnIndex(TasksConstants.COLUMN_LIST_ID));
                        String checks = c.getString(c.getColumnIndex(TasksConstants.COLUMN_STATUS));
                        String note = c.getString(c.getColumnIndex(TasksConstants.COLUMN_STATUS));
                        long mId = c.getLong(c.getColumnIndex(TasksConstants.COLUMN_ID));
                        mData.add(new ListItems(title, mId, checks, taskId, date, listId, note));
                    }
                } while (c.moveToNext());
            }

            //setListAdapter(new TasksRecyclerAdapter(getActivity(), mData, this));
            if (c != null) {
                c.close();
            }
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {

    }

    @Override
    public void endExecution(boolean result) {
        loadData();
    }
}