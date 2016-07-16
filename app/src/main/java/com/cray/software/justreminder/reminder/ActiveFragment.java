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
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityOptionsCompat;
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
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.ScreenManager;
import com.cray.software.justreminder.async.SyncTask;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.groups.GroupHelper;
import com.cray.software.justreminder.groups.GroupItem;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.Dialogues;
import com.cray.software.justreminder.helpers.Messages;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.TimeCount;
import com.cray.software.justreminder.interfaces.NavigationCallbacks;
import com.cray.software.justreminder.interfaces.RecyclerListener;
import com.cray.software.justreminder.interfaces.SyncListener;
import com.cray.software.justreminder.roboto_views.RoboTextView;
import com.cray.software.justreminder.utils.TimeUtil;
import com.cray.software.justreminder.utils.ViewUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Show all active reminders.
 */
public class ActiveFragment extends Fragment implements
        RecyclerListener, SyncListener, SeekBar.OnSeekBarChangeListener {

    /**
     * Views.
     */
    private RecyclerView mRecyclerView;
    private LinearLayout mEmptyLayout;
    private LinearLayout mFilterLayout;
    private RoboTextView mDateEnd;

    /**
     * Reminder data provider for recycler view.
     */
    private RemindersRecyclerAdapter mAdapter;

    /**
     * List of group identifiers.
     */
    private ArrayList<String> mGroupsIds;

    /**
     * Last selected group identifier.
     */
    private String mLastGroupId;

    /**
     * Navigation drawer callbacks.
     */
    private NavigationCallbacks mCallbacks;
    private Activity mContext;

    private Handler mHandler = new Handler();
    /**
     * Runnable for hiding repeat limit seekbar.
     */
    private Runnable mFilterSeek = new Runnable() {
        @Override
        public void run() {
            if (mFilterLayout.getVisibility() == View.VISIBLE) {
                ViewUtils.collapse(mFilterLayout);
            }
        }
    };

    /**
     * Fragment default instance.
     * @return Fragment.
     */
    public static ActiveFragment newInstance() {
        return new ActiveFragment();
    }

    /**
     * Empty public constructor.
     */
    public ActiveFragment() {
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_active_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                new SyncTask(mContext, this, false).execute();
                break;
            case R.id.action_voice:
                if (mCallbacks != null) {
                    mCallbacks.onItemSelected(ScreenManager.VOICE_RECOGNIZER);
                }
                break;
            case R.id.action_filter:
                filterDialog();
                break;
            case R.id.action_exit:
                mContext.finish();
                break;
            case R.id.action_search:
                if (mFilterLayout.getVisibility() == View.VISIBLE) {
                    ViewUtils.collapse(mFilterLayout);
                    mHandler.removeCallbacks(mFilterSeek);
                } else {
                    ViewUtils.expand(mFilterLayout);
                    mHandler.postDelayed(mFilterSeek, 2000);
                }
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_screen_manager, container, false);
        mFilterLayout = (LinearLayout) rootView.findViewById(R.id.filterLayout);
        RoboTextView dateStart = (RoboTextView) rootView.findViewById(R.id.dateStart);
        mDateEnd = (RoboTextView) rootView.findViewById(R.id.dateEnd);
        SeekBar dateSeek = (SeekBar) rootView.findViewById(R.id.dateSeek);
        dateStart.setText(TimeUtil.getSimpleDate(System.currentTimeMillis()));
        mDateEnd.setText(TimeUtil.getSimpleDate(System.currentTimeMillis()));
        dateSeek.setOnSeekBarChangeListener(this);
        mEmptyLayout = (LinearLayout) rootView.findViewById(R.id.emptyItem);
        mEmptyLayout.setVisibility(View.VISIBLE);
        RoboTextView emptyText = (RoboTextView) rootView.findViewById(R.id.emptyText);
        emptyText.setText(getString(R.string.no_events));
        ImageView emptyImage = (ImageView) rootView.findViewById(R.id.emptyImage);
        if (new ColorSetter(mContext).isDark()) {
            emptyImage.setImageResource(R.drawable.ic_alarm_off_white_vector);
        } else {
            emptyImage.setImageResource(R.drawable.ic_alarm_off_black_vector);
        }
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.currentList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(mContext);
        mRecyclerView.setLayoutManager(mLayoutManager);
        loaderAdapter(mLastGroupId, 0);
        return rootView;
    }

    @Override
    public void onAttach(final Activity activity) {
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
        ((ScreenManager) activity).onSectionAttached(ScreenManager.FRAGMENT_ACTIVE);
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
        ((ScreenManager) context).onSectionAttached(ScreenManager.FRAGMENT_ACTIVE);
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
            loaderAdapter(mLastGroupId, 0);
        }
    }

    /**
     * Load data to recycler view.
     * @param groupId group identifier.
     */
    public void loaderAdapter(final String groupId, long time){
        mLastGroupId = groupId;
        SharedPrefs.getInstance(mContext).putBoolean(Prefs.REMINDER_CHANGED, false);
        ReminderDataProvider provider;
        if (time > 0) provider = new ReminderDataProvider(mContext, time);
        else provider = new ReminderDataProvider(mContext, false, groupId);
        mAdapter = new RemindersRecyclerAdapter(mContext, provider.getData());
        mAdapter.setEventListener(this);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mAdapter);
        if (mCallbacks != null) {
            mCallbacks.onListChanged(mRecyclerView);
        }
        reloadView();
    }

    /**
     * Hide/show recycler view depends on data.
     */
    private void reloadView() {
        int size = mAdapter.getItemCount();
        if (size > 0){
            mRecyclerView.setVisibility(View.VISIBLE);
            mEmptyLayout.setVisibility(View.GONE);
        } else {
            mRecyclerView.setVisibility(View.GONE);
            mEmptyLayout.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Show reminder only for selected group.
     */
    private void filterDialog(){
        mGroupsIds = new ArrayList<>();
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                mContext, android.R.layout.select_dialog_item);
        arrayAdapter.add(getString(R.string.all));
        List<GroupItem> groups = GroupHelper.getInstance(mContext).getAll();
        for (GroupItem item : groups) {
            arrayAdapter.add(item.getTitle());
            mGroupsIds.add(item.getUuId());
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(getString(R.string.choose_group));
        builder.setAdapter(arrayAdapter, (dialog, which) -> {
            if (which == 0) {
                loaderAdapter(null, 0);
            } else {
                String catId = mGroupsIds.get(which - 1);
                loaderAdapter(catId, 0);
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * Change reminder group.
     * @param oldUuId old group unique identifier.
     * @param id reminder identifier.
     */
    private void changeGroup(final String oldUuId, final long id){
        mGroupsIds = new ArrayList<>();
        mGroupsIds.clear();
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                mContext, android.R.layout.select_dialog_item);
        List<GroupItem> groups = GroupHelper.getInstance(mContext).getAll();
        for (GroupItem item : groups) {
            arrayAdapter.add(item.getTitle());
            mGroupsIds.add(item.getUuId());
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(getString(R.string.choose_group));
        builder.setAdapter(arrayAdapter, (dialog, which) -> {
            dialog.dismiss();
            String catId = mGroupsIds.get(which);
            if (oldUuId.matches(catId)) {
                Messages.toast(mContext, getString(R.string.same_group));
                return;
            }
            Reminder.setNewGroup(mContext, id, catId);
            loaderAdapter(mLastGroupId, 0);
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * Open preview screen depending on reminder type.
     * @param view view.
     * @param id reminder identifier.
     * @param type reminder type.
     */
    private void previewReminder(final View view, final long id, final String type){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Intent intent = new Intent(mContext, ReminderPreview.class);
            intent.putExtra(Constants.EDIT_ID, id);
            String transitionName = "toolbar";
            if (type.matches(Constants.TYPE_SHOPPING_LIST)){
                intent = new Intent(mContext, ShopsPreview.class);
                intent.putExtra(Constants.EDIT_ID, id);
                transitionName = "toolbar";
            }
            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                            mContext, view, transitionName);
            mContext.startActivity(intent, options.toBundle());
        } else {
            if (type.matches(Constants.TYPE_SHOPPING_LIST)){
                mContext.startActivity(new Intent(mContext, ShopsPreview.class)
                                .putExtra(Constants.EDIT_ID, id));
            } else {
                mContext.startActivity(new Intent(mContext, ReminderPreview.class)
                                .putExtra(Constants.EDIT_ID, id));
            }
        }
    }

    @Override
    public void onItemSwitched(final int position, final View switchCompat) {
        boolean is = Reminder.toggle(mAdapter.getItem(position).getId(), mContext, mCallbacks);
        if (is) loaderAdapter(mLastGroupId, 0);
        else mAdapter.notifyItemChanged(position);
    }

    @Override
    public void onItemClicked(final int position, final View view) {
        ReminderModel item = mAdapter.getItem(position);
        if (SharedPrefs.getInstance(mContext).getBoolean(Prefs.ITEM_PREVIEW)) {
            previewReminder(view, item.getId(), item.getType());
        } else {
            if (item.getType().matches(Constants.TYPE_SHOPPING_LIST)){
                previewReminder(view, item.getId(), item.getType());
            } else {
                Reminder.toggle(item.getId(), mContext, mCallbacks);
                loaderAdapter(mLastGroupId, 0);
            }
        }
    }

    @Override
    public void onItemLongClicked(final int position, final View view) {
        final String[] items = {getString(R.string.open), getString(R.string.edit),
                getString(R.string.change_group), getString(R.string.move_to_trash)};
        Dialogues.showLCAM(mContext, item -> {
            ReminderModel item1 = mAdapter.getItem(position);
            switch (item){
                case 0:
                    previewReminder(view, item1.getId(), item1.getType());
                    break;
                case 1:
                    Reminder.edit(item1.getId(), mContext);
                    break;
                case 2:
                    changeGroup(item1.getGroupId(), item1.getId());
                    break;
                case 3:
                    mAdapter.removeItem(position);
                    Reminder.moveToTrash(item1.getId(), mContext, mCallbacks);
                    //loaderAdapter(null, 0);
                    break;
            }
        }, items);
    }

    @Override
    public void endExecution(final boolean result) {
        if (mContext != null) {
            loaderAdapter(mLastGroupId, 0);
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        long start = System.currentTimeMillis();
        long target = start + (progress * TimeCount.DAY);
        mDateEnd.setText(TimeUtil.getSimpleDate(target));
        loaderAdapter(null, target);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        mHandler.removeCallbacks(mFilterSeek);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mHandler.postDelayed(mFilterSeek, 2000);
    }
}
