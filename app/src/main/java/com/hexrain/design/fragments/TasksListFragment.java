package com.hexrain.design.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.TaskManager;
import com.cray.software.justreminder.adapters.TasksRecyclerAdapter;
import com.cray.software.justreminder.datas.ListItems;
import com.cray.software.justreminder.datas.TaskListData;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.interfaces.TasksConstants;

import java.util.ArrayList;

public class TasksListFragment extends Fragment{

    ListView mList;
    TasksRecyclerAdapter customAdapter;
    ArrayList<ListItems> datas;
    static final String ARGUMENT_PAGE_NUMBER = "arg_page_number";
    static final String ARGUMENT_PAGE_DATA = "arg_page_data";
    int pageNumber;

    public static TasksListFragment newInstance(int page, ArrayList<ListItems> datas) {
        TasksListFragment pageFragment = new TasksListFragment();
        Bundle arguments = new Bundle();
        arguments.putInt(ARGUMENT_PAGE_NUMBER, page);
        arguments.putParcelableArrayList(ARGUMENT_PAGE_DATA, datas);
        pageFragment.setArguments(arguments);
        return pageFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle intent = getArguments();
        datas = intent.getParcelableArrayList(ARGUMENT_PAGE_DATA);
        pageNumber = intent.getInt(ARGUMENT_PAGE_NUMBER);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_simple_list_layout, container, false);

        TextView textView = (TextView) view.findViewById(R.id.emptyList);
        textView.setText(getString(R.string.string_no_tasks));
        mList = (ListView) view.findViewById(R.id.listView);
        mList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        mList.setItemsCanFocus(true);
        mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startActivity(new Intent(getActivity(), TaskManager.class)
                        .putExtra(Constants.ITEM_ID_INTENT, id)
                        .putExtra(TasksConstants.INTENT_ACTION, TasksConstants.EDIT));
            }
        });

        loaderAdapter(datas);

        return view;
    }

    public void loaderAdapter(ArrayList<ListItems> calendarDatas){
        customAdapter = new TasksRecyclerAdapter(getActivity(), calendarDatas, null);
        mList.setAdapter(customAdapter);
    }
}
