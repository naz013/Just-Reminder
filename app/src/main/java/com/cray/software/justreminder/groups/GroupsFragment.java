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

package com.cray.software.justreminder.groups;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.ScreenManager;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.Dialogues;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.interfaces.NavigationCallbacks;
import com.cray.software.justreminder.interfaces.SimpleListener;
import com.cray.software.justreminder.modules.Module;

public class GroupsFragment extends Fragment implements SimpleListener {

    private RecyclerView listView;
    private GroupsRecyclerAdapter mAdapter;
    private NavigationCallbacks mCallbacks;
    private Activity mContext;

    public static GroupsFragment newInstance() {
        return new GroupsFragment();
    }

    public GroupsFragment() {
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_screen_manager, container, false);
        rootView.findViewById(R.id.backgroundFragment).setBackgroundColor(ColorSetter.getInstance(mContext).getBackgroundStyle());
        LinearLayout emptyItem = (LinearLayout) rootView.findViewById(R.id.emptyItem);
        emptyItem.setVisibility(View.GONE);
        listView = (RecyclerView) rootView.findViewById(R.id.currentList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(mContext);
        listView.setLayoutManager(mLayoutManager);
        loadCategories();
        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (mContext == null) {
            mContext = (Activity) context;
        }
        if (mCallbacks != null) {
            try {
                mCallbacks = (NavigationCallbacks) context;
            } catch (ClassCastException e) {
                throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
            }
        }
        ((ScreenManager) context).onSectionAttached(ScreenManager.FRAGMENT_GROUPS);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (mContext == activity) {
            mContext = activity;
        }
        if (mCallbacks == null) {
            try {
                mCallbacks = (NavigationCallbacks) activity;
            } catch (ClassCastException e) {
                throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
            }
        }
        ((ScreenManager) activity).onSectionAttached(ScreenManager.FRAGMENT_GROUPS);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (SharedPrefs.getInstance(mContext).getBoolean(Prefs.GROUP_CHANGED)) {
            loadCategories();
        }
    }

    private void loadCategories(){
        SharedPrefs.getInstance(mContext).putBoolean(Prefs.GROUP_CHANGED, false);
        mAdapter = new GroupsRecyclerAdapter(mContext, GroupHelper.getInstance(mContext).getAll());
        mAdapter.setEventListener(this);
        listView.setAdapter(mAdapter);
        listView.setItemAnimator(new DefaultItemAnimator());
        if (mCallbacks != null) {
            mCallbacks.onListChanged(listView);
        }
    }

    private void removeGroup(int position){
        long itemId = mAdapter.getItem(position).getId();
        if (itemId != 0) {
            GroupItem group = GroupHelper.getInstance(mContext).getGroup(itemId);
            if (group != null) {
                String uuId = group.getUuId();
                GroupHelper.getInstance(mContext).deleteGroup(itemId);
                new DeleteGroupAsync(mContext, uuId).execute();
            }
            if (mCallbacks != null) {
                mCallbacks.showSnackbar(R.string.deleted);
            }
            loadCategories();
        }
    }

    @Override
    public void onItemClicked(int position, View view) {
        startActivity(new Intent(mContext, GroupManager.class)
                .putExtra(Constants.ITEM_ID_INTENT, mAdapter.getItem(position).getId()));
    }

    @Override
    public void onItemLongClicked(final int position, View view) {
        String[] items = {getString(R.string.change_color), getString(R.string.edit), getString(R.string.delete)};
        if (mAdapter.getItemCount() == 1) items = new String[]{getString(R.string.change_color), getString(R.string.edit)};
        Dialogues.showLCAM(mContext, item -> {
            switch (item){
                case 0:
                    changeColor(mAdapter.getItem(position).getId());
                    break;
                case 1:
                    startActivity(new Intent(mContext, GroupManager.class)
                            .putExtra(Constants.ITEM_ID_INTENT, mAdapter.getItem(position).getId()));
                    break;
                case 2:
                    removeGroup(position);
                    break;
            }
        }, items);
    }

    private void changeColor(final long id) {
        String[] items = {getString(R.string.red), getString(R.string.purple),
                getString(R.string.green), getString(R.string.green_light),
                getString(R.string.blue), getString(R.string.blue_light),
                getString(R.string.yellow), getString(R.string.orange),
                getString(R.string.cyan), getString(R.string.pink),
                getString(R.string.teal), getString(R.string.amber)};
        if (Module.isPro()){
            items = new String[]{getString(R.string.red), getString(R.string.purple),
                    getString(R.string.green), getString(R.string.green_light),
                    getString(R.string.blue), getString(R.string.blue_light),
                    getString(R.string.yellow), getString(R.string.orange),
                    getString(R.string.cyan), getString(R.string.pink),
                    getString(R.string.teal), getString(R.string.amber),
                    getString(R.string.dark_purple), getString(R.string.dark_orange),
                    getString(R.string.lime), getString(R.string.indigo)};
        }
        Dialogues.showLCAM(mContext, item -> {
            GroupHelper.getInstance(mContext).setNewIndicator(id, item);
            loadCategories();
        }, items);
    }
}
