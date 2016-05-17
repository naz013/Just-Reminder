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
import com.cray.software.justreminder.activities.AddBirthday;
import com.cray.software.justreminder.adapters.CalendarEventsAdapter;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.datas.models.EventsItem;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.Messages;
import com.cray.software.justreminder.interfaces.SimpleListener;
import com.cray.software.justreminder.reminder.Reminder;

import java.util.ArrayList;

public class EventsListFragment extends Fragment implements SimpleListener {

    private ArrayList<EventsItem> mDataList;
    static final String ARGUMENT_PAGE_NUMBER = "arg_page_number";
    private RecyclerView mEventsList;
    private LinearLayout mEmptyItem;
    private boolean isCreate = false;

    public void setData(ArrayList<EventsItem> datas){
        this.mDataList = new ArrayList<>(datas);
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

        mEmptyItem = (LinearLayout) view.findViewById(R.id.emptyItem);
        mEmptyItem.setVisibility(View.VISIBLE);

        TextView emptyText = (TextView) view.findViewById(R.id.emptyText);
        emptyText.setText(getString(R.string.no_events));

        ImageView emptyImage = (ImageView) view.findViewById(R.id.emptyImage);
        if (new ColorSetter(getActivity()).isDark())
            emptyImage.setImageResource(R.drawable.today_white);
        else
            emptyImage.setImageResource(R.drawable.today);

        mEventsList = (RecyclerView) view.findViewById(R.id.currentList);

        loaderAdapter();
        isCreate = true;

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isCreate) loaderAdapter();
        isCreate = false;
    }

    public void loaderAdapter(){
        CalendarEventsAdapter customAdapter = new CalendarEventsAdapter(getActivity(), mDataList);
        customAdapter.setmEventListener(this);
        mEventsList.setLayoutManager(new LinearLayoutManager(getActivity()));
        mEventsList.setItemAnimator(new DefaultItemAnimator());
        mEventsList.setAdapter(customAdapter);
        reloadView();
    }

    private void reloadView() {
        int size = mDataList != null ? mDataList.size() : 0;
        if (size > 0){
            mEventsList.setVisibility(View.VISIBLE);
            mEmptyItem.setVisibility(View.GONE);
        } else {
            mEventsList.setVisibility(View.GONE);
            mEmptyItem.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onItemClicked(int position, View view) {
        if (mDataList.get(position).getType().matches("birthday")) {
            startActivity(new Intent(getActivity(), AddBirthday.class)
                    .putExtra("BDid", mDataList.get(position).getId())
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        } else {
            Reminder.edit(mDataList.get(position).getId(), getActivity());
        }
    }

    @Override
    public void onItemLongClicked(int position, View view) {
        if (mDataList.get(position).getType().matches("birthday")) {
            DataBase db = new DataBase(getActivity());
            db.open();
            db.deleteBirthday(mDataList.get(position).getId());
            db.close();
            mDataList.remove(position);
            loaderAdapter();
            Messages.toast(getActivity(), getString(R.string.deleted));
        }
    }
}
