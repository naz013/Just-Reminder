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
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.cray.software.justreminder.databases.NextBase;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.Dialogues;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.interfaces.NavigationCallbacks;
import com.cray.software.justreminder.interfaces.RecyclerListener;
import com.cray.software.justreminder.roboto_views.RoboTextView;

public class TrashFragment extends Fragment implements RecyclerListener {

    private RecyclerView currentList;
    private LinearLayout emptyItem;
    private RemindersRecyclerAdapter mAdapter;

    private NavigationCallbacks mCallbacks;

    public static TrashFragment newInstance() {
        return new TrashFragment();
    }

    public TrashFragment() {
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Indicate that this fragment would like to influence the set of actions in the action bar.
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.archive_menu, menu);
        NextBase db = new NextBase(getActivity());
        db.open();
        Cursor c = db.getArchivedReminders();
        if (c.getCount() == 0){
            menu.findItem(R.id.action_delete_all).setVisible(false);
        }
        c.close();
        db.close();
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
        if (new ColorSetter(getActivity()).isDark()) {
            emptyImage.setImageResource(R.drawable.ic_delete_white_vector);
        } else {
            emptyImage.setImageResource(R.drawable.ic_delete_black_vector);
        }
        currentList = (RecyclerView) rootView.findViewById(R.id.currentList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        currentList.setLayoutManager(mLayoutManager);
        loaderAdapter();
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallbacks = (NavigationCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
        }
        ((ScreenManager)activity).onSectionAttached(ScreenManager.FRAGMENT_ARCHIVE);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (new SharedPrefs(getActivity()).loadBoolean(Prefs.REMINDER_CHANGED)) {
            loaderAdapter();
        }
    }

    public void loaderAdapter(){
        new SharedPrefs(getActivity()).saveBoolean(Prefs.REMINDER_CHANGED, false);
        ReminderDataProvider provider = new ReminderDataProvider(getActivity(), true, null);
        mAdapter = new RemindersRecyclerAdapter(getActivity(), provider.getData());
        mAdapter.setEventListener(this);
        currentList.setAdapter(mAdapter);
        currentList.setItemAnimator(new DefaultItemAnimator());
        if (mCallbacks != null) {
            mCallbacks.onListChanged(currentList);
        }
        reloadView();
    }

    private void reloadView() {
        if (mAdapter.getItemCount() > 0){
            currentList.setVisibility(View.VISIBLE);
            emptyItem.setVisibility(View.GONE);
        } else {
            currentList.setVisibility(View.GONE);
            emptyItem.setVisibility(View.VISIBLE);
        }
    }

    private void deleteAll(){
        NextBase db = new NextBase(getActivity());
        if (!db.isOpen()) db.open();
        Cursor c = db.getArchivedReminders();
        if (c != null && c.moveToFirst()){
            do{
                long rowId = c.getLong(c.getColumnIndex(NextBase._ID));
                Reminder.delete(rowId, getActivity());
            }while (c.moveToNext());
        }
        if (c != null) c.close();
        db.close();
        if (mCallbacks != null) {
            mCallbacks.showSnackbar(getString(R.string.trash_cleared));
        }
        loaderAdapter();
    }

    @Override
    public void onItemClicked(int position, View view) {
        Reminder.edit(mAdapter.getItem(position).getId(), getActivity());
    }

    @Override
    public void onItemLongClicked(final int position, View view) {
        final String[] items = {getString(R.string.edit), getString(R.string.delete)};
        Dialogues.showLCAM(getActivity(), item -> {
            ReminderModel item1 = mAdapter.getItem(position);
            if (item == 0) {
                Reminder.edit(item1.getId(), getActivity());
            }
            if (item == 1) {
                Reminder.delete(item1.getId(), getActivity());
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
