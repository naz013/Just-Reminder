package com.hexrain.design.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
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
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.async.SyncTask;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.interfaces.Prefs;
import com.cray.software.justreminder.modules.Module;
import com.cray.software.justreminder.reminder.CustomCursorAdapter;
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

import java.util.ArrayList;

import jp.wasabeef.recyclerview.animators.LandingAnimator;

public class ActiveFragment extends Fragment {

    RecyclerView currentList;
    LinearLayout emptyLayout, emptyItem;
    RelativeLayout ads_container;
    private AdView adView;
    ImageView emptyImage;

    DataBase DB;
    ColorSetter cSetter;
    SharedPrefs sPrefs;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.Adapter mWrappedAdapter;
    private RecyclerViewSwipeManager mRecyclerViewSwipeManager;
    private RecyclerViewTouchActionGuardManager mRecyclerViewTouchActionGuardManager;

    private NavigationDrawerFragment.NavigationDrawerCallbacks mCallbacks;

    public static ActiveFragment newInstance() {
        return new ActiveFragment();
    }

    public ActiveFragment() {
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Indicate that this fragment would like to influence the set of actions in the action bar.
        setHasOptionsMenu(true);
        DB = new DataBase(getActivity());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                startSync();
                return true;
            case R.id.action_voice:
                if (mCallbacks != null){
                    mCallbacks.onNavigationDrawerItemSelected(ScreenManager.VOICE_RECOGNIZER);
                }
                return true;
            case R.id.action_order:
                showDialog();
                return true;
            case R.id.action_filter:
                filterDialog();
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

        emptyImage = (ImageView) rootView.findViewById(R.id.emptyImage);
        if (sPrefs.loadBoolean(Prefs.USE_DARK_THEME)) {
            emptyImage.setImageResource(R.drawable.ic_notifications_white_24dp);
        } else {
            emptyImage.setImageResource(R.drawable.ic_notifications_grey600_24dp);
        }

        currentList = (RecyclerView) rootView.findViewById(R.id.currentList);
        /*currentList.setEmptyView(emptyItem);
        currentList.setItemsCanFocus(true);
        currentList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                sPrefs = new SharedPrefs(getActivity());
                if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_ITEM_PREVIEW)) {
                    startActivity(new Intent(getActivity(), ReminderPreviewFragment.class)
                            .putExtra(Constants.EDIT_ID, id));
                } else {
                    Reminder.toggle(id, getActivity());
                    loaderAdapter(null);
                }
            }
        });
        currentList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Reminder.edit(id, getActivity());
                return true;
            }
        });*/

        loaderAdapter(null);

        if (!Module.isPro()) {
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
        ((ScreenManager)activity).onSectionAttached(ScreenManager.FRAGMENT_ACTIVE);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!new Module().isPro()){
            if (adView != null) {
                adView.resume();
            }
        }
        loaderAdapter(null);
    }

    @Override
    public void onDestroy() {
        if (!new Module().isPro()) {
            if (adView != null) {
                adView.destroy();
            }
        }
        super.onDestroy();
    }

    @Override
    public void onPause() {
        if (!new Module().isPro()) {
            if (adView != null) {
                adView.pause();
            }
        }
        super.onPause();
    }

    CustomCursorAdapter customAdapter;
    ReminderDataProvider provider;

    public void loaderAdapter(String categoryId){
        DB = new DataBase(getActivity());
        if (!DB.isOpen()) DB.open();
        provider = new ReminderDataProvider(getActivity());
        if (categoryId != null) {
            //customAdapter = new CustomCursorAdapter(getActivity(), DB.queryGroup(categoryId), null);
            provider.setCursor(DB.queryGroup(categoryId));
        } else {
            //customAdapter = new CustomCursorAdapter(getActivity(), DB.queryGroup(), null);
            provider.setCursor(DB.queryGroup());
        }
        provider.load();
        int size = provider.getCount();
        if (size > 0){
            currentList.setVisibility(View.VISIBLE);
            emptyItem.setVisibility(View.GONE);
        } else {
            currentList.setVisibility(View.GONE);
            emptyItem.setVisibility(View.VISIBLE);
        }
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerViewTouchActionGuardManager = new RecyclerViewTouchActionGuardManager();
        mRecyclerViewTouchActionGuardManager.setInterceptVerticalScrollingWhileAnimationRunning(true);
        mRecyclerViewTouchActionGuardManager.setEnabled(true);
        mRecyclerViewSwipeManager = new RecyclerViewSwipeManager();

        /*final SwipeActionAdapter mAdapter = new SwipeActionAdapter(customAdapter);
        mAdapter.setListView(currentList);
        currentList.setAdapter(mAdapter);
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
                    DB = new DataBase(getActivity());
                    if (!DB.isOpen()) DB.open();
                    customAdapter = new CustomCursorAdapter(getActivity(),
                            DB.queryGroup(), null);
                    final long id = customAdapter.getItemId(position);

                    switch (direction) {
                        case SwipeDirections.DIRECTION_NORMAL_LEFT:
                            disableReminder(id);
                            break;
                        case SwipeDirections.DIRECTION_FAR_LEFT:
                            disableReminder(id);
                            break;
                        case SwipeDirections.DIRECTION_NORMAL_RIGHT:
                            editReminder(id);
                            break;
                        case SwipeDirections.DIRECTION_FAR_RIGHT:
                            editReminder(id);
                            break;
                    }
                }
            }
        });*/
        RemindersRecyclerAdapter myItemAdapter = new RemindersRecyclerAdapter(getActivity(), provider);
        myItemAdapter.setEventListener(new RemindersRecyclerAdapter.EventListener() {
            @Override
            public void onItemEdit(int position) {
                editReminder(provider.getItem(position).getId());
            }

            @Override
            public void onItemRemoved(int position) {
                Reminder.makeArchive(provider.getItem(position).getId(), getActivity());
                loaderAdapter(null);
            }

            @Override
            public void onItemSwitched(int position) {
                if (Reminder.toggle(provider.getItem(position).getId(), getActivity())){
                    loaderAdapter(null);
                }
            }

            @Override
            public void onItemClicked(int position, SwitchCompat check) {
                sPrefs = new SharedPrefs(getActivity());
                if (sPrefs.loadBoolean(Prefs.ITEM_PREVIEW)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        Intent intent = new Intent(getActivity(), ReminderPreviewFragment.class);
                        intent.putExtra(Constants.EDIT_ID, provider.getItem(position).getId());
                        String transitionName = "switch";
                        ActivityOptionsCompat options =
                                ActivityOptionsCompat.makeSceneTransitionAnimation(
                                        getActivity(), check, transitionName);
                        getActivity().startActivity(intent, options.toBundle());
                    } else {
                        getActivity().startActivity(
                                new Intent(getActivity(), ReminderPreviewFragment.class)
                                        .putExtra(Constants.EDIT_ID, provider.getItem(position).getId()));
                    }
                } else {
                    if (Reminder.toggle(provider.getItem(position).getId(), getActivity())){
                        loaderAdapter(null);
                    }
                }
            }

            @Override
            public void onItemLongClicked(int position) {
                editReminder(provider.getItem(position).getId());
            }
        });
        mAdapter = myItemAdapter;
        mWrappedAdapter = mRecyclerViewSwipeManager.createWrappedAdapter(myItemAdapter);      // wrap for swiping
        final GeneralItemAnimator animator = new SwipeDismissItemAnimator();
        animator.setSupportsChangeAnimations(false);
        currentList.setLayoutManager(mLayoutManager);
        currentList.setAdapter(mWrappedAdapter);  // requires *wrapped* adapter
        currentList.setItemAnimator(new LandingAnimator());
        currentList.addItemDecoration(new SimpleListDividerDecorator(new ColorDrawable(android.R.color.transparent), true));
        mRecyclerViewTouchActionGuardManager.attachRecyclerView(currentList);
        mRecyclerViewSwipeManager.attachRecyclerView(currentList);
        if (mCallbacks != null) mCallbacks.onListChange(currentList);
    }

    private void editReminder(long id){
        if (id != 0) {
            Reminder.edit(id, getActivity());
        }
    }

    private void disableReminder(long id){
        if (id != 0) {
            Reminder.makeArchive(id, getActivity());
            loaderAdapter(null);
        }
    }

    ArrayList<String> ids;
    private void filterDialog(){
        ids = new ArrayList<>();
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                getActivity(),
                android.R.layout.select_dialog_item);
        DB = new DataBase(getActivity());
        if (DB != null) DB.open();
        else {
            DB = new DataBase(getActivity());
            DB.open();
        }
        arrayAdapter.add(getString(R.string.simple_all));
        Cursor c = DB.queryCategories();
        if (c != null && c.moveToFirst()){
            do {
                String title = c.getString(c.getColumnIndex(Constants.COLUMN_TEXT));
                String catId = c.getString(c.getColumnIndex(Constants.COLUMN_TECH_VAR));
                arrayAdapter.add(title);
                ids.add(catId);
            } while (c.moveToNext());
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.string_select_category));
        builder.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) loaderAdapter(null);
                else {
                    String catId = ids.get(which - 1);
                    loaderAdapter(catId);
                }
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void showDialog(){
        final CharSequence[] items = {getString(R.string.sort_item_by_date_a_z),
                getString(R.string.sort_item_by_date_z_a),
                getString(R.string.sort_item_by_date_without_a_z),
                getString(R.string.sort_item_by_date_without_z_a)};
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.menu_order_by));
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                SharedPrefs prefs = new SharedPrefs(getActivity());
                if (item == 0) {
                    prefs.savePrefs(Prefs.LIST_ORDER, Constants.ORDER_DATE_A_Z);
                } else if (item == 1) {
                    prefs.savePrefs(Prefs.LIST_ORDER, Constants.ORDER_DATE_Z_A);
                } else if (item == 2) {
                    prefs.savePrefs(Prefs.LIST_ORDER, Constants.ORDER_DATE_WITHOUT_DISABLED_A_Z);
                } else if (item == 3) {
                    prefs.savePrefs(Prefs.LIST_ORDER, Constants.ORDER_DATE_WITHOUT_DISABLED_Z_A);
                }
                dialog.dismiss();
                loaderAdapter(null);
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void startSync(){
        new SyncTask(getActivity(), null).execute();
    }
}
