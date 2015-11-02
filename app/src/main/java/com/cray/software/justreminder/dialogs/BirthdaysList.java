package com.cray.software.justreminder.dialogs;

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
import com.cray.software.justreminder.adapters.CalendarEventsAdapter;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.datas.EventsPagerItem;
import com.cray.software.justreminder.helpers.Messages;
import com.cray.software.justreminder.reminder.Reminder;

import java.util.ArrayList;

public class BirthdaysList extends Fragment{

    private ListView contactsList;
    private CalendarEventsAdapter customAdapter;
    private ArrayList<EventsPagerItem> datas;
    static final String ARGUMENT_PAGE_NUMBER = "arg_page_number";
    private int pageNumber;

    public void setData(ArrayList<EventsPagerItem> datas){
        this.datas = datas;
    }

    public void setPageNumber(int number){
        this.pageNumber = number;
    }

    public static BirthdaysList newInstance(int page) {
        BirthdaysList pageFragment = new BirthdaysList();
        Bundle arguments = new Bundle();
        arguments.putInt(ARGUMENT_PAGE_NUMBER, page);
        pageFragment.setArguments(arguments);
        return pageFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle intent = getArguments();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.birthdays_list_fragment, container, false);

        TextView textView = (TextView) view.findViewById(R.id.textView);

        contactsList = (ListView) view.findViewById(R.id.contactsList);
        contactsList.setEmptyView(textView);
        contactsList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (customAdapter.getItem(position).toString().matches("birthday")) {
                    DataBase db = new DataBase(getActivity());
                    db.open();
                    db.deleteBirthday(id);
                    datas.remove(position);
                    loaderAdapter();
                    Messages.toast(getActivity(), getString(R.string.swipe_delete));
                }
                return true;
            }
        });

        contactsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if (customAdapter.getItem(position).toString().matches("birthday")) {
                    startActivity(new Intent(getActivity(), AddBirthday.class)
                            .putExtra("BDid", id)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                } else {
                    Reminder.edit(id, getActivity());
                }
            }
        });

        loaderAdapter();

        return view;
    }

    public void loaderAdapter(){
        customAdapter = new CalendarEventsAdapter(getActivity(), datas.get(pageNumber).getDatas());
        contactsList.setAdapter(customAdapter);
    }
}
