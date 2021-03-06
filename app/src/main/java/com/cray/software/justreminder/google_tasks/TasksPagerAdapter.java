/**
 * Copyright 2015 Nazar Suhovich
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

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.cray.software.justreminder.datas.models.TaskListWrapperItem;
import com.cray.software.justreminder.interfaces.NavigationCallbacks;

import java.util.ArrayList;
import java.util.Map;

/**
 * Pager adapter for Google Tasks pager.
 */
public class TasksPagerAdapter extends FragmentStatePagerAdapter {

    /**
     * List of Google Tasks models.
     */
    private ArrayList<TaskListWrapperItem> mData;

    /**
     * Navigation drawer callback.
     */
    private NavigationCallbacks mCallbacks;

    private Map<String, Integer> colors;

    /**
     * Adapter constructor.
     * @param fm fragment manager.
     * @param data list of models.
     */
    public TasksPagerAdapter(final FragmentManager fm, final ArrayList<TaskListWrapperItem> data, Map<String, Integer> colors) {
        super(fm);
        this.mData = data;
        this.colors = colors;
    }

    /**
     * Set navigation drawer callback for adapter.
     * @param callbacks navigation drawer callback.
     */
    public void setCallbacks(NavigationCallbacks callbacks){
        this.mCallbacks = callbacks;
    }

    @Override
    public Fragment getItem(final int position) {
        TaskListFragment fragment = new TaskListFragment();
        fragment.setData(mData.get(position).getmData(), colors);
        fragment.setCallbacks(mCallbacks);
        return fragment;
    }

    @Override
    public int getCount() {
        return mData.size();
    }
}
