package com.hexrain.design.fragments;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.modules.ManageModule;
import com.cray.software.justreminder.reminder.Reminder;
import com.cray.software.justreminder.reminder.ReminderDataProvider;
import com.cray.software.justreminder.reminder.RemindersRecyclerAdapter;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.animator.SwipeDismissItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.decoration.SimpleListDividerDecorator;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager;
import com.h6ah4i.android.widget.advrecyclerview.touchguard.RecyclerViewTouchActionGuardManager;
import com.hexrain.design.NavigationDrawerFragment;
import com.hexrain.design.ScreenManager;

import jp.wasabeef.recyclerview.animators.LandingAnimator;

public class TrashFragment extends Fragment {

    RecyclerView currentList;
    LinearLayout emptyLayout, emptyItem;
    RelativeLayout ads_container;
    private AdView adView;
    ImageView emptyImage;
    TextView emptyText;

    DataBase DB;
    ColorSetter cSetter;
    SharedPrefs sPrefs;

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
        View rootView = inflater.inflate(R.layout.fragment_screen_manager, container, false);

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

        currentList = (RecyclerView) rootView.findViewById(R.id.currentList);
        /*currentList.setEmptyView(emptyItem);
        currentList.setVisibility(View.VISIBLE);
        currentList.setItemsCanFocus(false);*/
        loaderAdapter();
        /*currentList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Reminder.edit(id, getActivity());
            }
        });*/

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

    ReminderDataProvider provider;

    public void loaderAdapter(){
        DB = new DataBase(getActivity());
        if (!DB.isOpen()) DB.open();
        provider = new ReminderDataProvider(getActivity());
        provider.setCursor(DB.getArchivedReminders());
        provider.load();
        int size = provider.getCount();
        if (size > 0){
            currentList.setVisibility(View.VISIBLE);
            emptyItem.setVisibility(View.GONE);
        } else {
            currentList.setVisibility(View.GONE);
            emptyItem.setVisibility(View.VISIBLE);
        }
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        RecyclerViewTouchActionGuardManager mRecyclerViewTouchActionGuardManager = new RecyclerViewTouchActionGuardManager();
        mRecyclerViewTouchActionGuardManager.setInterceptVerticalScrollingWhileAnimationRunning(true);
        mRecyclerViewTouchActionGuardManager.setEnabled(true);
        RecyclerViewSwipeManager mRecyclerViewSwipeManager = new RecyclerViewSwipeManager();
        RemindersRecyclerAdapter myItemAdapter = new RemindersRecyclerAdapter(getActivity(), provider);
        myItemAdapter.setEventListener(new RemindersRecyclerAdapter.EventListener() {
            @Override
            public void onItemEdit(int position) {
                Reminder.edit(provider.getItem(position).getId(), getActivity());
            }

            @Override
            public void onItemRemoved(int position) {
                Reminder.delete(provider.getItem(position).getId(), getActivity());
                loaderAdapter();
            }

            @Override
            public void onItemSwitched(int position) {
            }

            @Override
            public void onItemClicked(int position, SwitchCompat check) {
                Reminder.edit(provider.getItem(position).getId(), getActivity());
            }

            @Override
            public void onItemLongClicked(int position) {
                Reminder.edit(provider.getItem(position).getId(), getActivity());
            }
        });
        RecyclerView.Adapter mWrappedAdapter = mRecyclerViewSwipeManager.createWrappedAdapter(myItemAdapter);
        final GeneralItemAnimator animator = new SwipeDismissItemAnimator();
        animator.setSupportsChangeAnimations(false);
        currentList.setLayoutManager(mLayoutManager);
        currentList.setAdapter(mWrappedAdapter);  // requires *wrapped* adapter
        currentList.setItemAnimator(new LandingAnimator());
        currentList.addItemDecoration(new SimpleListDividerDecorator(new ColorDrawable(android.R.color.transparent), true));
        mRecyclerViewTouchActionGuardManager.attachRecyclerView(currentList);
        mRecyclerViewSwipeManager.attachRecyclerView(currentList);
        /*final SwipeActionAdapter mAdapter = new SwipeActionAdapter(archiveCursorAdapter);
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
                        case SwipeDirections.DIRECTION_NORMAL_LEFT |
                                SwipeDirections.DIRECTION_FAR_LEFT:
                            removeReminder(itId);
                            break;
                        case SwipeDirections.DIRECTION_NORMAL_RIGHT |
                                SwipeDirections.DIRECTION_FAR_RIGHT:
                            Reminder.edit(itId, getActivity());
                            break;
                    }
                }
            }
        });
        currentList.setAdapter(mAdapter);*/
        if (mCallbacks != null) mCallbacks.onListChange(currentList);
    }

    private void removeReminder(long itId){
        if (itId != 0) {
            Reminder.delete(itId, getActivity());
            Toast.makeText(getActivity(), getString(R.string.swipe_delete),
                    Toast.LENGTH_SHORT).show();
            loaderAdapter();
        }
    }

    private void deleteAll(){
        DB = new DataBase(getActivity());
        if (!DB.isOpen()) DB.open();
        Cursor c = DB.getArchivedReminders();
        if (c != null && c.moveToFirst()){
            do{
                long rowId = c.getLong(c.getColumnIndex(Constants.COLUMN_ID));
                removeReminder(rowId);
            }while (c.moveToNext());
        }
        if (c != null) c.close();
    }
}
