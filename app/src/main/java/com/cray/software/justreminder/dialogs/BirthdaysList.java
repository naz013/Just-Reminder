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
import android.widget.Toast;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.ReminderManager;
import com.cray.software.justreminder.adapters.CalendarEventsAdapter;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.datas.EventsDataProvider;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.interfaces.Constants;

import java.util.ArrayList;

public class BirthdaysList extends Fragment{

    ListView contactsList;
    CalendarEventsAdapter customAdapter;
    ColorSetter cs;
    ArrayList<EventsDataProvider.EventsItem> datas;
    static final String ARGUMENT_PAGE_NUMBER = "arg_page_number";
    static final String ARGUMENT_PAGE_DATA = "arg_page_data";
    int pageNumber;

    public static BirthdaysList newInstance(int page, ArrayList<EventsDataProvider.EventsItem> datas) {
        BirthdaysList pageFragment = new BirthdaysList();
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
                    db.deleteEvent(id);
                    datas.remove(position);
                    loaderAdapter(datas);
                    Toast.makeText(getActivity(), getString(R.string.swipe_delete), Toast.LENGTH_SHORT).show();
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
                    Intent intentId = new Intent(getActivity(), ReminderManager.class);
                    intentId.putExtra(Constants.EDIT_WIDGET, 2);
                    intentId.putExtra(Constants.EDIT_ID, id);
                    startActivity(intentId);
                }
            }
        });

        loaderAdapter(datas);

        return view;
    }

    public void loaderAdapter(ArrayList<EventsDataProvider.EventsItem> calendarDatas){
        customAdapter = new CalendarEventsAdapter(getActivity(), calendarDatas);
        contactsList.setAdapter(customAdapter);
    }
}
