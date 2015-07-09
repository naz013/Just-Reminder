package com.hexrain.design.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ActionMode;
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
import com.cray.software.justreminder.modules.ManageModule;
import com.cray.software.justreminder.reminder.Reminder;
import com.cray.software.justreminder.reminder.ReminderDataProvider;
import com.cray.software.justreminder.reminder.RemindersRecyclerAdapter;
import com.cray.software.justreminder.services.AlarmReceiver;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.hexrain.design.NavigationDrawerFragment;
import com.hexrain.design.ScreenManager;

import java.util.ArrayList;

public class ActiveFragment extends Fragment {

    RecyclerView currentList;
    LinearLayout emptyLayout, emptyItem;
    RelativeLayout ads_container;
    private AdView adView;
    ImageView emptyImage;

    DataBase DB;
    AlarmReceiver alarm = new AlarmReceiver();
    ColorSetter cSetter;
    SharedPrefs sPrefs;

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
        if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_USE_DARK_THEME)) {
            emptyImage.setImageResource(R.drawable.ic_notifications_white_24dp);
        } else {
            emptyImage.setImageResource(R.drawable.ic_notifications_grey600_24dp);
        }

        currentList = (RecyclerView) rootView.findViewById(R.id.currentList);
        currentList.setLayoutManager(new LinearLayoutManager(getActivity()));

        loaderAdapter(null);

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
        if (!new ManageModule().isPro()){
            if (adView != null) {
                adView.resume();
            }
        }
        loaderAdapter(null);
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
    RemindersRecyclerAdapter adapter;

    public void loaderAdapter(String categoryId){
        DB = new DataBase(getActivity());
        if (!DB.isOpen()) DB.open();
        provider = new ReminderDataProvider(getActivity());
        if (categoryId != null) {
            provider.setCursor(DB.queryGroup(categoryId));
        } else {
            provider.setCursor(DB.queryGroup());
        }
        provider.load();
        adapter = new RemindersRecyclerAdapter(getActivity(), provider);
        adapter.setEventListener(new RemindersRecyclerAdapter.EventListener() {
            @Override
            public void onItemClicked(int position) {
                if (mActionMode == null) {
                    long id = adapter.getItemId(position);
                    sPrefs = new SharedPrefs(getActivity());
                    if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_ITEM_PREVIEW)) {
                        getActivity().startActivity(new Intent(getActivity(), ReminderPreviewFragment.class)
                                .putExtra(Constants.EDIT_ID, id));
                    } else {
                        Reminder.toggle(id, getActivity());
                        loaderAdapter(null);
                    }
                }
            }

            @Override
            public void onItemLongClicked(int position) {
                long id = adapter.getItemId(position);
                editReminder(id);
                if (mActionMode != null) {
                    mActionMode.finish();
                    mActionMode = null;
                }
            }

            @Override
            public void actionMode() {
                if (mActionMode != null) {
                    return;
                }
                mActionMode = getActivity().startActionMode(mActionModeCallback);
            }

            @Override
            public void invalidate() {
                if (mActionMode == null) return;

                mActionMode.invalidate();
            }

            @Override
            public void itemChange() {
                loaderAdapter(null);
            }
        });
        currentList.setAdapter(adapter);
        currentList.setItemAnimator(new DefaultItemAnimator());

        if (provider.getCount() > 0){
            currentList.setVisibility(View.VISIBLE);
            emptyItem.setVisibility(View.GONE);
        } else {
            currentList.setVisibility(View.GONE);
            emptyItem.setVisibility(View.VISIBLE);
        }

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
                    prefs.savePrefs(Constants.APP_UI_PREFERENCES_LIST_ORDER, Constants.ORDER_DATE_A_Z);
                } else if (item == 1) {
                    prefs.savePrefs(Constants.APP_UI_PREFERENCES_LIST_ORDER, Constants.ORDER_DATE_Z_A);
                } else if (item == 2) {
                    prefs.savePrefs(Constants.APP_UI_PREFERENCES_LIST_ORDER, Constants.ORDER_DATE_WITHOUT_DISABLED_A_Z);
                } else if (item == 3) {
                    prefs.savePrefs(Constants.APP_UI_PREFERENCES_LIST_ORDER, Constants.ORDER_DATE_WITHOUT_DISABLED_Z_A);
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

    ActionMode mActionMode;

    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate a menu resource providing context menu items
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.context_menu, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            int size = provider.getSelected().size();
            if (size > 0){
                menu.findItem(R.id.action_title).setTitle(size + " " +
                        getActivity().getString(R.string.string_selected));
                if (provider.hasActive()) menu.findItem(R.id.action_disable).setVisible(true);
                else menu.findItem(R.id.action_disable).setVisible(false);
                if (size == 1){
                    if (provider.getSelected().get(0).getCompleted() == 0)
                        menu.findItem(R.id.action_disable).setVisible(true);
                    else menu.findItem(R.id.action_disable).setVisible(false);
                    menu.findItem(R.id.action_edit).setVisible(true);
                } else {
                    menu.findItem(R.id.action_edit).setVisible(false);
                }
            } else {
                if (mActionMode != null) {
                    mActionMode.finish();
                }
            }
            return false; // Return false if nothing is done
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_edit:
                    if (provider.getSelected().size() == 1){
                        for (ReminderDataProvider.ReminderItem reminderItem : provider.getSelected()){
                            editReminder(reminderItem.getId());
                            loaderAdapter(null);
                        }
                        mode.finish();
                    } else return true;
                    return true;
                case R.id.action_delete:
                    if (provider.getSelected().size() > 0){
                        for (ReminderDataProvider.ReminderItem reminderItem : provider.getSelected()){
                            provider.removeItem(reminderItem);
                            disableReminder(reminderItem.getId());
                            loaderAdapter(null);
                        }
                        mode.finish();
                    } else return true;
                    return true;
                case R.id.action_disable:
                    if (provider.getSelected().size() > 0){
                        for (ReminderDataProvider.ReminderItem reminderItem : provider.getSelected()){
                            int position = provider.getPosition(reminderItem);
                            if (provider.isActive(position)){
                                Reminder.toggle(reminderItem.getId(), getActivity());
                                loaderAdapter(null);
                            }
                        }
                        mode.finish();
                    } else return true;
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
            provider.deselectItems();
            loaderAdapter(null);
        }
    };
}
