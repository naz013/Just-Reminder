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
package com.cray.software.justreminder.reminder;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.ScreenManager;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.contacts.FilterCallback;
import com.cray.software.justreminder.datas.AdapterItem;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.Dialogues;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.interfaces.NavigationCallbacks;
import com.cray.software.justreminder.interfaces.RecyclerListener;
import com.cray.software.justreminder.roboto_views.RoboTextView;

import java.util.List;

public class TrashFragment extends Fragment implements RecyclerListener {

    private RecyclerView mRecyclerView;
    private LinearLayout emptyItem;

    private List<AdapterItem> mDataList;
    private TrashRecyclerAdapter mAdapter;

    private NavigationCallbacks mCallbacks;
    private Activity mContext;

    private SearchView mSearchView = null;
    private MenuItem mSearchMenu = null;

    private SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String query) {
            if (mAdapter != null) mAdapter.filter(query, mDataList);
            if (mSearchMenu != null) {
                mSearchMenu.collapseActionView();
            }
            return false;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            if (mAdapter != null) mAdapter.filter(newText, mDataList);
            return false;
        }
    };
    private FilterCallback mFilterCallback = new FilterCallback() {
        @Override
        public void filter(int size) {
            mRecyclerView.scrollToPosition(0);
            reloadView();
        }
    };

    public static TrashFragment newInstance() {
        return new TrashFragment();
    }

    public TrashFragment() {
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.archive_menu, menu);
        mSearchMenu = menu.findItem(R.id.action_search);
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        if (mSearchMenu != null) {
            mSearchView = (SearchView) mSearchMenu.getActionView();
        }
        if (mSearchView != null) {
            mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
            mSearchView.setOnQueryTextListener(queryTextListener);
        }
        if (ReminderHelper.getInstance(mContext).getRemindersArchived().size() == 0){
            menu.findItem(R.id.action_delete_all).setVisible(false);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete_all:
                deleteAll();
                loaderAdapter();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_screen_manager, container, false);
        emptyItem = (LinearLayout) rootView.findViewById(R.id.emptyItem);
        emptyItem.setVisibility(View.VISIBLE);
        RoboTextView emptyText = (RoboTextView) rootView.findViewById(R.id.emptyText);
        emptyText.setText(R.string.trash_is_empty);
        ImageView emptyImage = (ImageView) rootView.findViewById(R.id.emptyImage);
        if (new ColorSetter(mContext).isDark()) {
            emptyImage.setImageResource(R.drawable.ic_delete_white_vector);
        } else {
            emptyImage.setImageResource(R.drawable.ic_delete_black_vector);
        }
        rootView.findViewById(R.id.backgroundFragment).setBackgroundColor(new ColorSetter(mContext).getBackgroundStyle());
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.currentList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(mContext);
        mRecyclerView.setLayoutManager(mLayoutManager);
        loaderAdapter();
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (mContext == null) {
            mContext = activity;
        }
        if (mCallbacks == null) {
            try {
                mCallbacks = (NavigationCallbacks) activity;
            } catch (ClassCastException e) {
                throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
            }
        }
        ((ScreenManager) activity).onSectionAttached(ScreenManager.FRAGMENT_ARCHIVE);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (mContext == null) {
            mContext = (Activity) context;
        }
        if (mCallbacks == null) {
            try {
                mCallbacks = (NavigationCallbacks) context;
            } catch (ClassCastException e) {
                throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
            }
        }
        ((ScreenManager) context).onSectionAttached(ScreenManager.FRAGMENT_ARCHIVE);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (SharedPrefs.getInstance(mContext).getBoolean(Prefs.REMINDER_CHANGED)) {
            loaderAdapter();
        }
    }

    public void loaderAdapter(){
        SharedPrefs.getInstance(mContext).putBoolean(Prefs.REMINDER_CHANGED, false);
        mDataList = SimpleProvider.getInstance(mContext).getTrashed();
        mAdapter = new TrashRecyclerAdapter(mContext, mDataList, mFilterCallback);
        mAdapter.setEventListener(this);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        if (mCallbacks != null) {
            mCallbacks.onListChanged(mRecyclerView);
        }
        reloadView();
        getActivity().invalidateOptionsMenu();
    }

    private void reloadView() {
        if (mAdapter.getItemCount() > 0){
            mRecyclerView.setVisibility(View.VISIBLE);
            emptyItem.setVisibility(View.GONE);
        } else {
            mRecyclerView.setVisibility(View.GONE);
            emptyItem.setVisibility(View.VISIBLE);
        }
    }

    private void deleteAll(){
        for (ReminderItem item : ReminderHelper.getInstance(mContext).getRemindersArchived()) {
            Reminder.delete(item.getId(), mContext);
        }
        if (mCallbacks != null) {
            mCallbacks.showSnackbar(getString(R.string.trash_cleared));
        }
        loaderAdapter();
    }

    @Override
    public void onItemClicked(int position, View view) {
        Reminder.edit(mAdapter.getItem(position).getId(), mContext);
    }

    @Override
    public void onItemLongClicked(final int position, View view) {
        final String[] items = {getString(R.string.edit), getString(R.string.delete)};
        Dialogues.showLCAM(mContext, item -> {
            ReminderItem item1 = mAdapter.getItem(position);
            if (item == 0) {
                Reminder.edit(item1.getId(), mContext);
            }
            if (item == 1) {
                Reminder.delete(item1.getId(), mContext);
                if (mCallbacks != null) {
                    mCallbacks.showSnackbar(R.string.deleted);
                }
                loaderAdapter();
            }
        }, items);
    }

    @Override
    public void onItemSwitched(int position, View switchCompat) {

    }
}
