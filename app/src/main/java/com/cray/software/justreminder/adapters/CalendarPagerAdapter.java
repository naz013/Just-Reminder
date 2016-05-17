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

package com.cray.software.justreminder.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.cray.software.justreminder.datas.models.EventsPagerItem;
import com.cray.software.justreminder.fragments.EventsListFragment;

import java.util.ArrayList;

/**
 * Pager adapter for day view fragment.
 */
public class CalendarPagerAdapter extends FragmentStatePagerAdapter {

    /**
     * List of pager item models.
     */
    private ArrayList<EventsPagerItem> datas;

    /**
     * Pager adapter constructor.
     * @param fm fragment manager.
     * @param datas list of models.
     */
    public CalendarPagerAdapter(final FragmentManager fm, final ArrayList<EventsPagerItem> datas) {
        super(fm);
        this.datas = datas;
    }

    @Override
    public Fragment getItem(final int position) {
        EventsListFragment fragment = new EventsListFragment();
        fragment.setData(datas.get(position).getDatas());
        return fragment;
    }

    @Override
    public int getCount() {
        return datas.size();
    }
}
