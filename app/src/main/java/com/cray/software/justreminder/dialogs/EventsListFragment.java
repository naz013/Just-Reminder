package com.cray.software.justreminder.dialogs;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.adapters.CalendarEventsAdapter;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.datas.EventsDataProvider;
import com.cray.software.justreminder.helpers.Messages;
import com.cray.software.justreminder.interfaces.SimpleListener;
import com.cray.software.justreminder.reminder.Reminder;

import java.util.ArrayList;

public class EventsListFragment extends Fragment implements SimpleListener {

    private ArrayList<EventsDataProvider.EventsItem> datas;
    static final String ARGUMENT_PAGE_NUMBER = "arg_page_number";
    private RecyclerView listView;
    private LinearLayout emptyItem;

    public void setData(ArrayList<EventsDataProvider.EventsItem> datas){
        this.datas = datas;
    }

    public static EventsListFragment newInstance(int page) {
        EventsListFragment pageFragment = new EventsListFragment();
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

        emptyItem = (LinearLayout) view.findViewById(R.id.emptyItem);
        emptyItem.setVisibility(View.VISIBLE);

        TextView emptyText = (TextView) view.findViewById(R.id.emptyText);
        emptyText.setText(getString(R.string.events_empty_text));

        ImageView emptyImage = (ImageView) view.findViewById(R.id.emptyImage);
        emptyImage.setImageResource(R.drawable.today);

        listView = (RecyclerView) view.findViewById(R.id.currentList);

        loaderAdapter();

        return view;
    }

    public void loaderAdapter(){
        CalendarEventsAdapter customAdapter = new CalendarEventsAdapter(getActivity(), datas);
        customAdapter.setmEventListener(this);
        listView.setLayoutManager(new LinearLayoutManager(getActivity()));
        listView.setItemAnimator(new DefaultItemAnimator());
        listView.setAdapter(customAdapter);
        reloadView();
    }

    private void reloadView() {
        int size = datas.size();
        if (size > 0){
            listView.setVisibility(View.VISIBLE);
            emptyItem.setVisibility(View.GONE);
        } else {
            listView.setVisibility(View.GONE);
            emptyItem.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onItemClicked(int position, View view) {
        if (datas.get(position).getType().matches("birthday")) {
            startActivity(new Intent(getActivity(), AddBirthday.class)
                    .putExtra("BDid", datas.get(position).getId())
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        } else {
            Reminder.edit(datas.get(position).getId(), getActivity());
        }
    }

    @Override
    public void onItemLongClicked(int position, View view) {
        if (datas.get(position).getType().matches("birthday")) {
            DataBase db = new DataBase(getActivity());
            db.open();
            db.deleteBirthday(datas.get(position).getId());
            datas.remove(position);
            loaderAdapter();
            Messages.toast(getActivity(), getString(R.string.swipe_delete));
        }
    }
}
