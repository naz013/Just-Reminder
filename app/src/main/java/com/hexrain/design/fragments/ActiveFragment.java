package com.hexrain.design.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
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

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.adapters.RemindersRecyclerAdapter;
import com.cray.software.justreminder.async.SyncTask;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.datas.ReminderModel;
import com.cray.software.justreminder.helpers.Messages;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.interfaces.Prefs;
import com.cray.software.justreminder.interfaces.RecyclerListener;
import com.cray.software.justreminder.interfaces.SyncListener;
import com.cray.software.justreminder.modules.Module;
import com.cray.software.justreminder.reminder.Reminder;
import com.cray.software.justreminder.reminder.ReminderDataProvider;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.hexrain.design.NavigationDrawerFragment;
import com.hexrain.design.ScreenManager;
import com.hexrain.design.TestActivity;

import java.util.ArrayList;

public class ActiveFragment extends Fragment implements RecyclerListener, SyncListener {

    private RecyclerView currentList;
    private LinearLayout emptyLayout, emptyItem;
    private AdView adView;

    private DataBase DB;
    private SharedPrefs sPrefs;
    private ReminderDataProvider provider;

    private ArrayList<String> ids;

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
        inflater.inflate(R.menu.fragment_active_menu, menu);
        //menu.add(Menu.NONE, 55, 100, "Test List");
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                startSync();
                break;
            case R.id.action_voice:
                if (mCallbacks != null){
                    mCallbacks.onNavigationDrawerItemSelected(ScreenManager.VOICE_RECOGNIZER);
                }
                break;
            case R.id.action_filter:
                filterDialog();
                break;
            case R.id.action_exit:
                getActivity().finish();
                break;
            case 55:
                startActivity(new Intent(getActivity(), TestActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_screen_manager, container, false);

        sPrefs = new SharedPrefs(getActivity());

        emptyItem = (LinearLayout) rootView.findViewById(R.id.emptyItem);
        emptyItem.setVisibility(View.VISIBLE);

        ImageView emptyImage = (ImageView) rootView.findViewById(R.id.emptyImage);
        emptyImage.setImageResource(R.drawable.alarm);

        currentList = (RecyclerView) rootView.findViewById(R.id.currentList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        currentList.setLayoutManager(mLayoutManager);

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
        if (!Module.isPro()){
            if (adView != null) {
                adView.resume();
            }
        }
        loaderAdapter(null);
    }

    @Override
    public void onDestroy() {
        if (!Module.isPro()) {
            if (adView != null) {
                adView.destroy();
            }
        }
        super.onDestroy();
    }

    @Override
    public void onPause() {
        if (!Module.isPro()) {
            if (adView != null) {
                adView.pause();
            }
        }
        super.onPause();
    }

    public void loaderAdapter(String categoryId){
        DB = new DataBase(getActivity());
        if (!DB.isOpen()) DB.open();
        provider = new ReminderDataProvider(getActivity());
        if (categoryId != null) {
            provider.setCursor(DB.queryGroup(categoryId));
        } else {
            provider.setCursor(DB.queryGroup());
        }
        reloadView();
        RemindersRecyclerAdapter adapter = new RemindersRecyclerAdapter(getActivity(), provider);
        adapter.setEventListener(this);
        currentList.setHasFixedSize(true);
        currentList.setItemAnimator(new DefaultItemAnimator());
        currentList.setAdapter(adapter);
        if (mCallbacks != null) mCallbacks.onListChanged(currentList);
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

    private void filterDialog(){
        ids = new ArrayList<>();
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                getActivity(),
                android.R.layout.select_dialog_item);
        DB = new DataBase(getActivity());
        DB.open();
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

    private void startSync(){
        new SyncTask(getActivity(), this).execute();
    }

    private void changeGroup(final String oldUuId, final long id){
        ids = new ArrayList<>();
        ids.clear();
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                getActivity(),
                android.R.layout.select_dialog_item);
        DB = new DataBase(getActivity());
        DB.open();
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
                dialog.dismiss();
                String catId = ids.get(which);
                if (oldUuId.matches(catId)) {
                    Messages.toast(getActivity(), R.string.you_have_select_same_group);
                    return;
                }
                Reminder.setNewGroup(getActivity(), id, catId);
                loaderAdapter(null);
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void previewReminder(View view, long id, String type){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Intent intent = new Intent(getActivity(), ReminderPreviewFragment.class);
            intent.putExtra(Constants.EDIT_ID, id);
            String transitionName = "toolbar";
            if (type.matches(Constants.TYPE_SHOPPING_LIST)){
                intent = new Intent(getActivity(), ShoppingListPreview.class);
                intent.putExtra(Constants.EDIT_ID, id);
                transitionName = "toolbar";
            }
            ActivityOptionsCompat options =
                    ActivityOptionsCompat.makeSceneTransitionAnimation(
                            getActivity(), view, transitionName);
            getActivity().startActivity(intent, options.toBundle());
        } else {
            if (type.matches(Constants.TYPE_SHOPPING_LIST)){
                getActivity().startActivity(
                        new Intent(getActivity(), ShoppingListPreview.class)
                                .putExtra(Constants.EDIT_ID, id));
            } else {
                getActivity().startActivity(
                        new Intent(getActivity(), ReminderPreviewFragment.class)
                                .putExtra(Constants.EDIT_ID, id));
            }
        }
    }

    @Override
    public void onItemSwitched(int position, View switchCompat) {
        Reminder.toggle(provider.getItem(position).getId(), getActivity(), mCallbacks);
        loaderAdapter(null);
    }

    @Override
    public void onItemClicked(int position, View view) {
        sPrefs = new SharedPrefs(getActivity());
        ReminderModel item = provider.getItem(position);
        if (sPrefs.loadBoolean(Prefs.ITEM_PREVIEW)) {
            previewReminder(view, item.getId(), item.getType());
        } else {
            if (item.getType().matches(Constants.TYPE_SHOPPING_LIST)){
                previewReminder(view, item.getId(), item.getType());
            } else {
                Reminder.toggle(item.getId(), getActivity(), mCallbacks);
                loaderAdapter(null);
            }
        }
    }

    @Override
    public void onItemLongClicked(final int position, final View view) {
        final CharSequence[] items = {getString(R.string.open), getString(R.string.edit),
                getString(R.string.change_group), getString(R.string.move_to_archive)};
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                dialog.dismiss();
                ReminderModel item1 = provider.getItem(position);
                switch (item){
                    case 0:
                        previewReminder(view, item1.getId(), item1.getType());
                        break;
                    case 1:
                        Reminder.edit(item1.getId(), getActivity());
                        break;
                    case 2:
                        changeGroup(item1.getGroupId(), item1.getId());
                        break;
                    case 3:
                        Reminder.moveToTrash(item1.getId(), getActivity(), mCallbacks);
                        loaderAdapter(null);
                        break;
                }
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void endExecution(boolean result) {
        if (getActivity() != null) loaderAdapter(null);
    }
}