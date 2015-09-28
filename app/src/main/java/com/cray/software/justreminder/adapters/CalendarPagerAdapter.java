package com.cray.software.justreminder.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.cray.software.justreminder.datas.EventsPagerItem;
import com.cray.software.justreminder.dialogs.BirthdaysList;

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
public class CalendarPagerAdapter extends FragmentStatePagerAdapter {

    private ArrayList<EventsPagerItem> datas;

    public CalendarPagerAdapter(FragmentManager fm, ArrayList<EventsPagerItem> datas) {
        super(fm);
        this.datas = datas;
    }

    @Override
    public Fragment getItem(int position) {
        BirthdaysList fragment = new BirthdaysList();
        fragment.setData(datas);
        fragment.setPageNumber(position);
        return fragment;
    }

    @Override
    public int getCount() {
        return datas.size();
    }
}
