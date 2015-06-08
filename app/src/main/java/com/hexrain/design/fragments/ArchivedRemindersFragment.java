package com.hexrain.design.fragments;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.ReminderManager;
import com.cray.software.justreminder.adapters.CustomCursorAdapter;
import com.cray.software.justreminder.async.DeleteReminder;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.helpers.CalendarManager;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.modules.ManageModule;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.hexrain.design.NavigationDrawerFragment;
import com.hexrain.design.ScreenManager;
import com.wdullaer.swipeactionadapter.SwipeActionAdapter;
import com.wdullaer.swipeactionadapter.SwipeDirections;

public class ArchivedRemindersFragment extends Fragment {

    ListView currentList;
    private CustomCursorAdapter archiveCursorAdapter;
    LinearLayout emptyLayout, emptyItem;
    RelativeLayout ads_container;
    private AdView adView;
    ImageView emptyImage;
    TextView emptyText;

    DataBase DB;
    ColorSetter cSetter;
    SharedPrefs sPrefs;

    private NavigationDrawerFragment.NavigationDrawerCallbacks mCallbacks;

    public static ArchivedRemindersFragment newInstance() {
        return new ArchivedRemindersFragment();
    }

    public ArchivedRemindersFragment() {
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
        Cursor c = DB.queryArchived();
        if (c.getCount() == 0) menu.findItem(R.id.action_delete_all).setVisible(false);
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
        View rootView = inflater.inflate(R.layout.fragment_with_listview, container, false);

        cSetter = new ColorSetter(getActivity());
        sPrefs = new SharedPrefs(getActivity());

        emptyItem = (LinearLayout) rootView.findViewById(R.id.emptyItem);
        emptyItem.setVisibility(View.VISIBLE);

        emptyText = (TextView) rootView.findViewById(R.id.emptyText);
        emptyText.setText(getActivity().getString(R.string.string_no_archived));
        emptyItem.setVisibility(View.VISIBLE);

        emptyImage = (ImageView) rootView.findViewById(R.id.emptyImage);
        if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_USE_DARK_THEME)) {
            emptyImage.setImageResource(R.drawable.ic_delete_white_24dp);
        } else {
            emptyImage.setImageResource(R.drawable.ic_delete_grey600_24dp);
        }

        currentList = (ListView) rootView.findViewById(R.id.currentList);
        currentList.setEmptyView(emptyItem);
        currentList.setVisibility(View.VISIBLE);
        currentList.setItemsCanFocus(false);
        loaderAdapter();
        currentList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                editReminder(id);
            }
        });

        if (!new ManageModule().isPro()) {
            emptyLayout = (LinearLayout) rootView.findViewById(R.id.emptyLayout);
            emptyLayout.setVisibility(View.GONE);

            adView = (AdView) rootView.findViewById(R.id.adView);
            adView.setVisibility(View.GONE);

            AdRequest adRequest = new AdRequest.Builder()
                    .build();
            adView.loadAd(adRequest);
            adView.setAdListener(new AdListener() {
                @Override
                public void onAdFailedToLoad(int errorCode) {
                    adView.setVisibility(View.GONE);
                    emptyLayout.setVisibility(View.GONE);
                }

                @Override
                public void onAdLoaded() {
                    emptyLayout.setVisibility(View.VISIBLE);
                    adView.setVisibility(View.VISIBLE);
                }
            });

            ads_container = (RelativeLayout) rootView.findViewById(R.id.ads_container);
        }
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
        loaderAdapter();
        if (!new ManageModule().isPro()){
            if (adView != null) {
                adView.resume();
            }
        }
    }

    @Override
    public void onDestroy() {
        if (!new ManageModule().isPro()) {
            if (adView != null) {
                adView.destroy();
            }
        }
        super.onDestroy();
    }

    @Override
    public void onPause() {
        if (!new ManageModule().isPro()) {
            if (adView != null) {
                adView.pause();
            }
        }
        super.onPause();
    }

    private void editReminder(long id){
        Intent intentId = new Intent(getActivity(), ReminderManager.class);
        intentId.putExtra(Constants.EDIT_ID, id);
        startActivity(intentId);
    }

    public void loaderAdapter(){
        DB = new DataBase(getActivity());
        if (!DB.isOpen()) DB.open();
        archiveCursorAdapter = new CustomCursorAdapter(getActivity(), DB.queryArchived(), null);
        final SwipeActionAdapter mAdapter = new SwipeActionAdapter(archiveCursorAdapter);
        mAdapter.setListView(currentList);
        mAdapter.setFixedBackgrounds(true);
        mAdapter.addBackground(SwipeDirections.DIRECTION_NORMAL_LEFT, R.layout.swipe_delete_layout)
                .addBackground(SwipeDirections.DIRECTION_NORMAL_RIGHT, R.layout.swipe_edit_layout)
                .addBackground(SwipeDirections.DIRECTION_FAR_LEFT, R.layout.swipe_delete_layout)
                .addBackground(SwipeDirections.DIRECTION_FAR_RIGHT, R.layout.swipe_edit_layout);
        mAdapter.setSwipeActionListener(new SwipeActionAdapter.SwipeActionListener() {
            @Override
            public boolean hasActions(int position) {
                return true;
            }

            @Override
            public boolean shouldDismiss(int position, int direction) {
                return direction == SwipeDirections.DIRECTION_NORMAL_LEFT;
            }

            @Override
            public void onSwipe(int[] positionList, int[] directionList) {
                for (int ii = 0; ii < positionList.length; ii++) {
                    int direction = directionList[ii];
                    int position = positionList[ii];
                    long itId = archiveCursorAdapter.getItemId(position);

                    switch (direction) {
                        case SwipeDirections.DIRECTION_NORMAL_LEFT:
                            removeReminder(itId);
                            break;
                        case SwipeDirections.DIRECTION_FAR_LEFT:
                            removeReminder(itId);
                            break;
                        case SwipeDirections.DIRECTION_NORMAL_RIGHT:
                            editReminder(itId);
                            break;
                        case SwipeDirections.DIRECTION_FAR_RIGHT:
                            editReminder(itId);
                            break;
                    }
                }
            }
        });
        currentList.setAdapter(mAdapter);
        if (mCallbacks != null) mCallbacks.onListChange(currentList);
    }

    private void removeReminder(long itId){
        if (itId != 0) {
            DB = new DataBase(getActivity());
            if (!DB.isOpen()) DB.open();
            new CalendarManager(getActivity()).deleteEvents(itId);
            Cursor c = DB.getTask(itId);
            String uuId = null;
            if (c != null && c.moveToFirst()){
                uuId = c.getString(c.getColumnIndex(Constants.COLUMN_TECH_VAR));
            }
            DB.deleteTask(itId);
            new DeleteReminder(getActivity(), uuId).execute();
            Toast.makeText(getActivity(), getString(R.string.swipe_delete),
                    Toast.LENGTH_SHORT).show();
            loaderAdapter();
        }
    }

    private void deleteAll(){
        DB = new DataBase(getActivity());
        if (!DB.isOpen()) DB.open();
        Cursor c = DB.queryArchived();
        if (c != null && c.moveToFirst()){
            do{
                long rowId = c.getLong(c.getColumnIndex(Constants.COLUMN_ID));
                String uuId = c.getString(c.getColumnIndex(Constants.COLUMN_TECH_VAR));
                new CalendarManager(getActivity()).deleteEvents(rowId);
                DB.deleteTask(rowId);
                new DeleteReminder(getActivity(), uuId).execute();
            }while (c.moveToNext());
        }
        if (c != null) c.close();
    }
}
