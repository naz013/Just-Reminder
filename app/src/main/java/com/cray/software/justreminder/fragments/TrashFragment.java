package com.cray.software.justreminder.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
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
import android.widget.TextView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.adapters.RemindersRecyclerAdapter;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.datas.models.ReminderModel;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.interfaces.RecyclerListener;
import com.cray.software.justreminder.reminder.Reminder;
import com.cray.software.justreminder.reminder.ReminderDataProvider;
import com.cray.software.justreminder.ScreenManager;

public class TrashFragment extends Fragment implements RecyclerListener{

    private RecyclerView currentList;
    private LinearLayout emptyItem;

    private DataBase DB;
    private ReminderDataProvider provider;

    private NavigationDrawerFragment.NavigationDrawerCallbacks mCallbacks;

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
        DB = new DataBase(getActivity());
        DB.open();
        Cursor c = DB.getArchivedReminders();
        if (c.getCount() == 0){
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

        TextView emptyText = (TextView) rootView.findViewById(R.id.emptyText);
        emptyText.setText(getActivity().getString(R.string.string_no_archived));

        ImageView emptyImage = (ImageView) rootView.findViewById(R.id.emptyImage);
        if (new SharedPrefs(getActivity()).loadBoolean(Prefs.USE_DARK_THEME)) {
            emptyImage.setImageResource(R.drawable.delete_white);
        } else {
            emptyImage.setImageResource(R.drawable.delete);
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
            mCallbacks = (NavigationDrawerFragment.NavigationDrawerCallbacks) activity;
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
        DB = new DataBase(getActivity());
        if (!DB.isOpen()) {
            DB.open();
        }
        provider = new ReminderDataProvider(getActivity());
        provider.setCursor(DB.getArchivedReminders());
        reloadView();
        RemindersRecyclerAdapter adapter = new RemindersRecyclerAdapter(getActivity(), provider);
        adapter.setEventListener(this);
        currentList.setAdapter(adapter);
        currentList.setItemAnimator(new DefaultItemAnimator());
        if (mCallbacks != null) {
            mCallbacks.onListChanged(currentList);
        }
    }

    private void reloadView() {
        int size = provider.getCount();
        if (size > 0){
            currentList.setVisibility(View.VISIBLE);
            emptyItem.setVisibility(View.GONE);
        } else {
            currentList.setVisibility(View.GONE);
            emptyItem.setVisibility(View.VISIBLE);
        }
    }

    private void deleteAll(){
        DB = new DataBase(getActivity());
        if (!DB.isOpen()) {
            DB.open();
        }
        Cursor c = DB.getArchivedReminders();
        if (c != null && c.moveToFirst()){
            do{
                long rowId = c.getLong(c.getColumnIndex(Constants.COLUMN_ID));
                Reminder.delete(rowId, getActivity());
            }while (c.moveToNext());
        }
        if (c != null) {
            c.close();
        }
        if (mCallbacks != null) {
            mCallbacks.showSnackbar(R.string.string_trash_cleared);
        }
        loaderAdapter();
    }

    @Override
    public void onItemClicked(int position, View view) {
        Reminder.edit(provider.getItem(position).getId(), getActivity());
    }

    @Override
    public void onItemLongClicked(final int position, View view) {
        final CharSequence[] items = {getString(R.string.edit), getString(R.string.delete)};
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                dialog.dismiss();
                ReminderModel item1 = provider.getItem(position);
                if (item == 0) {
                    Reminder.edit(item1.getId(), getActivity());
                }
                if (item == 1) {
                    Reminder.delete(item1.getId(), getActivity());
                    if (mCallbacks != null) {
                        mCallbacks.showSnackbar(R.string.string_deleted);
                    }
                    loaderAdapter();
                }
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onItemSwitched(int position, View switchCompat) {

    }
}