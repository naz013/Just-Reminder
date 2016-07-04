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

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.adapters.TasksRecyclerAdapter;
import com.cray.software.justreminder.datas.models.Task;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.interfaces.NavigationCallbacks;
import com.cray.software.justreminder.interfaces.SyncListener;
import com.cray.software.justreminder.roboto_views.RoboTextView;

import java.util.ArrayList;

public class TaskListFragment extends Fragment implements SyncListener {

    private RecyclerView currentList;
    private LinearLayout emptyItem;
    private ArrayList<Task> datas;

    private NavigationCallbacks mCallbacks;

    public void setData(ArrayList<Task> datas){
        this.datas = datas;
    }

    public void setmCallbacks(NavigationCallbacks mCallbacks) {
        this.mCallbacks = mCallbacks;
    }

    public static TaskListFragment newInstance() {
        TaskListFragment pageFragment = new TaskListFragment();
        return pageFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle intent = getArguments();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_screen_manager, container, false);

        emptyItem = (LinearLayout) view.findViewById(R.id.emptyItem);
        emptyItem.setVisibility(View.VISIBLE);
        RoboTextView emptyText = (RoboTextView) view.findViewById(R.id.emptyText);
        emptyText.setText(R.string.no_google_tasks);
        emptyItem.setVisibility(View.VISIBLE);
        ImageView emptyImage = (ImageView) view.findViewById(R.id.emptyImage);
        if (new ColorSetter(getActivity()).isDark()) {
            emptyImage.setImageResource(R.drawable.ic_clear_white_vector);
        } else {
            emptyImage.setImageResource(R.drawable.ic_clear_black_vector);
        }

        currentList = (RecyclerView) view.findViewById(R.id.currentList);
        loaderAdapter();
        return view;
    }

    public void loaderAdapter(){
        TasksRecyclerAdapter customAdapter = new TasksRecyclerAdapter(getActivity(), datas);
        customAdapter.setListener(this);
        currentList.setLayoutManager(new LinearLayoutManager(getActivity()));
        currentList.setAdapter(customAdapter);
        currentList.setItemAnimator(new DefaultItemAnimator());
        if (mCallbacks != null) {
            mCallbacks.onListChanged(currentList);
        }
        reloadView();
    }

    private void reloadView() {
        if (datas != null && datas.size() > 0){
            currentList.setVisibility(View.VISIBLE);
            emptyItem.setVisibility(View.GONE);
        } else {
            currentList.setVisibility(View.GONE);
            emptyItem.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void endExecution(boolean result) {
        loaderAdapter();
    }
}
