package com.cray.software.justreminder.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.cray.software.justreminder.datas.TaskListData;
import com.hexrain.design.NavigationDrawerFragment;
import com.hexrain.design.fragments.TaskListFragment;

import java.util.ArrayList;

/**
 * Copyright 2015 Nazar Suhovich
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

/**
 * Pager adapter for Google Tasks pager.
 */
public class TasksPagerAdapter extends FragmentStatePagerAdapter {

    /**
     * List of Google Tasks models.
     */
    private ArrayList<TaskListData> datas;

    /**
     * Navigation drawer callback.
     */
    private NavigationDrawerFragment.NavigationDrawerCallbacks mCallbacks;

    /**
     * Adapter constructor.
     * @param fm fragment manager.
     * @param datas list of models.
     */
    public TasksPagerAdapter(final FragmentManager fm, final ArrayList<TaskListData> datas) {
        super(fm);
        this.datas = datas;
    }

    /**
     * Set navigation drawer callback for adapter.
     * @param callbacks navigation drawer callback.
     */
    public void setCallbacks(final NavigationDrawerFragment.NavigationDrawerCallbacks callbacks){
        this.mCallbacks = callbacks;
    }

    @Override
    public Fragment getItem(final int position) {
        TaskListFragment fragment = new TaskListFragment();
        fragment.setData(datas.get(position).getmData());
        fragment.setmCallbacks(mCallbacks);
        return fragment;
    }

    @Override
    public int getCount() {
        return datas.size();
    }
}
