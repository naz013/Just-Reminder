package com.hexrain.design.fragments;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.ReminderManager;
import com.cray.software.justreminder.adapters.RemindersRecyclerAdapter;
import com.cray.software.justreminder.async.DeleteReminder;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.datas.ReminderItem;
import com.cray.software.justreminder.helpers.CalendarManager;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.Interval;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.helpers.TimeCount;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.modules.ManageModule;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.animator.SwipeDismissItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager;
import com.h6ah4i.android.widget.advrecyclerview.touchguard.RecyclerViewTouchActionGuardManager;
import com.hexrain.design.NavigationDrawerFragment;
import com.hexrain.design.ScreenManager;

import java.util.ArrayList;

public class ArchivedRemindersFragment extends Fragment {

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
            emptyImage.setImageResource(R.drawable.ic_history_white_24dp);
        } else {
            emptyImage.setImageResource(R.drawable.ic_history_grey600_24dp);
        }

        currentList = (RecyclerView) rootView.findViewById(R.id.currentList);

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
        loaderAdapter();
        if (!new ManageModule().isPro()){
            if (adView != null) {
                adView.resume();
            }
        }
        super.onResume();
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

    ArrayList<ReminderItem> arrayList;

    public void loaderAdapter(){
        DB = new DataBase(getActivity());
        if (!DB.isOpen()) DB.open();
        Cursor c =  DB.queryArchived();

        SyncHelper helper = new SyncHelper(getActivity());
        arrayList = new ArrayList<>();

        if (c != null && c.moveToFirst()){
            emptyItem.setVisibility(View.GONE);
            do {
                String title = c.getString(c.getColumnIndex(Constants.COLUMN_TEXT));
                String type = c.getString(c.getColumnIndex(Constants.COLUMN_TYPE));
                String number = c.getString(c.getColumnIndex(Constants.COLUMN_NUMBER));
                String weekdays = c.getString(c.getColumnIndex(Constants.COLUMN_WEEKDAYS));
                String catId = c.getString(c.getColumnIndex(Constants.COLUMN_CATEGORY));
                String uuId = c.getString(c.getColumnIndex(Constants.COLUMN_TECH_VAR));
                int hour = c.getInt(c.getColumnIndex(Constants.COLUMN_HOUR));
                int minute = c.getInt(c.getColumnIndex(Constants.COLUMN_MINUTE));
                int seconds = c.getInt(c.getColumnIndex(Constants.COLUMN_SECONDS));
                int day = c.getInt(c.getColumnIndex(Constants.COLUMN_DAY));
                int month = c.getInt(c.getColumnIndex(Constants.COLUMN_MONTH));
                int year = c.getInt(c.getColumnIndex(Constants.COLUMN_YEAR));
                int repCode = c.getInt(c.getColumnIndex(Constants.COLUMN_REPEAT));
                int repTime = c.getInt(c.getColumnIndex(Constants.COLUMN_REMIND_TIME));
                int isDone = c.getInt(c.getColumnIndex(Constants.COLUMN_IS_DONE));
                double lat = c.getDouble(c.getColumnIndex(Constants.COLUMN_LATITUDE));
                double lon = c.getDouble(c.getColumnIndex(Constants.COLUMN_LONGITUDE));
                int repCount = c.getInt(c.getColumnIndex(Constants.COLUMN_REMINDERS_COUNT));
                int delay = c.getInt(c.getColumnIndex(Constants.COLUMN_DELAY));
                long mId = c.getLong(c.getColumnIndex(Constants.COLUMN_ID));

                TimeCount mCount = new TimeCount(getActivity());
                Interval mInterval = new Interval(getActivity());
                long due;
                String repeat = null;
                if (!type.startsWith(Constants.TYPE_WEEKDAY)) {
                    due = mCount.getEventTime(year, month, day, hour, minute, seconds, repTime,
                            repCode, repCount, delay);
                    if (type.matches(Constants.TYPE_CALL) || type.matches(Constants.TYPE_MESSAGE) ||
                            type.matches(Constants.TYPE_REMINDER) || type.startsWith(Constants.TYPE_SKYPE) ||
                            type.startsWith(Constants.TYPE_APPLICATION)) {
                        repeat = mInterval.getInterval(repCode);
                    } else if (type.matches(Constants.TYPE_TIME)) {
                        repeat = mInterval.getTimeInterval(repCode);
                    } else {
                        repeat = getString(R.string.interval_zero);
                    }
                } else {
                    due = mCount.getNextWeekdayTime(hour, minute, weekdays, delay);
                    if (weekdays.length() == 7) {
                        repeat = helper.getRepeatString(weekdays);
                    }
                }

                ReminderItem item = new ReminderItem(title, type, repeat, catId, uuId, isDone, due, mId,
                        new double[]{lat, lon}, number);
                item.setArchived(1);

                arrayList.add(item);
            } while (c.moveToNext());
        } else emptyItem.setVisibility(View.VISIBLE);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());

        // touch guard manager  (this class is required to suppress scrolling while swipe-dismiss animation is running)
        RecyclerViewTouchActionGuardManager mRecyclerViewTouchActionGuardManager =
                new RecyclerViewTouchActionGuardManager();
        mRecyclerViewTouchActionGuardManager.setInterceptVerticalScrollingWhileAnimationRunning(true);
        mRecyclerViewTouchActionGuardManager.setEnabled(true);

        // swipe manager
        RecyclerViewSwipeManager mRecyclerViewSwipeManager = new RecyclerViewSwipeManager();

        //adapter
        final RemindersRecyclerAdapter myItemAdapter = new RemindersRecyclerAdapter(getActivity(), arrayList);
        myItemAdapter.setEventListener(new RemindersRecyclerAdapter.EventListener() {
            @Override
            public void onItemRemoved(int position) {
                final long id = arrayList.get(position).getId();
                editReminder(id);
            }

            @Override
            public void onItemPinned(int position) {
                final long id = arrayList.get(position).getId();
                removeReminder(id);
            }

            @Override
            public void onItemClicked(int position) {
                final long id = arrayList.get(position).getId();
                editReminder(id);
            }

            @Override
            public void onItemLongClicked(int position) {
                final long id = arrayList.get(position).getId();
                editReminder(id);
            }

            @Override
            public void onItemViewClicked(View v, boolean isPinned) {

            }

            @Override
            public void toggleItem(int position) {

            }
        });

        RecyclerView.Adapter mWrappedAdapter = mRecyclerViewSwipeManager.createWrappedAdapter(myItemAdapter);

        final GeneralItemAnimator animator = new SwipeDismissItemAnimator();
        animator.setSupportsChangeAnimations(false);

        currentList.setLayoutManager(mLayoutManager);
        currentList.setAdapter(mWrappedAdapter);  // requires *wrapped* adapter
        currentList.setItemAnimator(animator);

        mRecyclerViewTouchActionGuardManager.attachRecyclerView(currentList);
        mRecyclerViewSwipeManager.attachRecyclerView(currentList);
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
