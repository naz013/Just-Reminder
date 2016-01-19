package com.cray.software.justreminder.fragments;

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
import com.cray.software.justreminder.ScreenManager;
import com.cray.software.justreminder.activities.ReminderPreview;
import com.cray.software.justreminder.activities.ShopsPreview;
import com.cray.software.justreminder.adapters.RemindersRecyclerAdapter;
import com.cray.software.justreminder.async.SyncTask;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.datas.models.ReminderModel;
import com.cray.software.justreminder.helpers.Messages;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.interfaces.NavigationCallbacks;
import com.cray.software.justreminder.interfaces.RecyclerListener;
import com.cray.software.justreminder.interfaces.SyncListener;
import com.cray.software.justreminder.reminder.Reminder;
import com.cray.software.justreminder.reminder.ReminderDataProvider;

import java.util.ArrayList;

/**
 * Show all active reminders.
 */
public class ActiveFragment extends Fragment implements RecyclerListener, SyncListener {

    /**
     * Recycler view field.
     */
    private RecyclerView currentList;

    /**
     * Containers.
     */
    private LinearLayout emptyItem;

    /**
     * Reminder data provider for recycler view.
     */
    private ReminderDataProvider provider;

    /**
     * List of group identifiers.
     */
    private ArrayList<String> ids;

    /**
     * Last selected group identifier.
     */
    private String lastId;

    /**
     * Navigation drawer callbacks.
     */
    private NavigationCallbacks mCallbacks;

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
                new SyncTask(getActivity(), this).execute();
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
                getActivity().finish();
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

        SharedPrefs prefs = new SharedPrefs(getActivity());

        emptyItem = (LinearLayout) rootView.findViewById(R.id.emptyItem);
        emptyItem.setVisibility(View.VISIBLE);

        ImageView emptyImage = (ImageView) rootView.findViewById(R.id.emptyImage);
        if (prefs.loadBoolean(Prefs.USE_DARK_THEME)) {
            emptyImage.setImageResource(R.drawable.ic_alarm_off_48px_white);
        } else {
            emptyImage.setImageResource(R.drawable.ic_alarm_off_48px);
        }

        currentList = (RecyclerView) rootView.findViewById(R.id.currentList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        currentList.setLayoutManager(mLayoutManager);

        loaderAdapter(lastId);
        return rootView;
    }

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);
        try {
            mCallbacks = (NavigationCallbacks) activity;
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
        if (new SharedPrefs(getActivity()).loadBoolean(Prefs.REMINDER_CHANGED)) {
            loaderAdapter(lastId);
        }
    }

    /**
     * Load data to recycler view.
     * @param groupId group identifier.
     */
    public void loaderAdapter(final String groupId){
        lastId = groupId;
        new SharedPrefs(getActivity()).saveBoolean(Prefs.REMINDER_CHANGED, false);
        provider = new ReminderDataProvider(getActivity(), false, groupId);
        reloadView();
        RemindersRecyclerAdapter adapter = new RemindersRecyclerAdapter(getActivity(), provider);
        adapter.setEventListener(this);
        currentList.setHasFixedSize(true);
        currentList.setItemAnimator(new DefaultItemAnimator());
        currentList.setAdapter(adapter);
        if (mCallbacks != null) {
            mCallbacks.onListChanged(currentList);
        }
    }

    /**
     * Hide/show recycler view depends on data.
     */
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

    /**
     * Show reminder only for selected group.
     */
    private void filterDialog(){
        ids = new ArrayList<>();
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                getActivity(),
                android.R.layout.select_dialog_item);
        DataBase db = new DataBase(getActivity());
        db.open();
        arrayAdapter.add(getString(R.string.all));
        Cursor c = db.queryCategories();
        if (c != null && c.moveToFirst()){
            do {
                String title = c.getString(c.getColumnIndex(Constants.COLUMN_TEXT));
                String catId = c.getString(c.getColumnIndex(Constants.COLUMN_TECH_VAR));
                arrayAdapter.add(title);
                ids.add(catId);
            } while (c.moveToNext());
        }
        if (c != null) c.close();
        db.close();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.choose_group));
        builder.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    loaderAdapter(null);
                } else {
                    String catId = ids.get(which - 1);
                    loaderAdapter(catId);
                }
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
        ids = new ArrayList<>();
        ids.clear();
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                getActivity(),
                android.R.layout.select_dialog_item);
        DataBase db = new DataBase(getActivity());
        db.open();
        Cursor c = db.queryCategories();
        if (c != null && c.moveToFirst()){
            do {
                String title = c.getString(c.getColumnIndex(Constants.COLUMN_TEXT));
                String catId = c.getString(c.getColumnIndex(Constants.COLUMN_TECH_VAR));
                arrayAdapter.add(title);
                ids.add(catId);
            } while (c.moveToNext());
        }
        if (c != null) c.close();
        db.close();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.choose_group));
        builder.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                String catId = ids.get(which);
                if (oldUuId.matches(catId)) {
                    Messages.toast(getActivity(), getString(R.string.same_group));
                    return;
                }
                Reminder.setNewGroup(getActivity(), id, catId);
                loaderAdapter(lastId);
            }
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
            Intent intent = new Intent(getActivity(), ReminderPreview.class);
            intent.putExtra(Constants.EDIT_ID, id);
            String transitionName = "toolbar";
            if (type.matches(Constants.TYPE_SHOPPING_LIST)){
                intent = new Intent(getActivity(), ShopsPreview.class);
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
                        new Intent(getActivity(), ShopsPreview.class)
                                .putExtra(Constants.EDIT_ID, id));
            } else {
                getActivity().startActivity(
                        new Intent(getActivity(), ReminderPreview.class)
                                .putExtra(Constants.EDIT_ID, id));
            }
        }
    }

    @Override
    public void onItemSwitched(final int position, final View switchCompat) {
        Reminder.toggle(provider.getItem(position).getId(), getActivity(), mCallbacks);
        loaderAdapter(lastId);
    }

    @Override
    public void onItemClicked(final int position, final View view) {
        SharedPrefs prefs = new SharedPrefs(getActivity());
        ReminderModel item = provider.getItem(position);
        if (prefs.loadBoolean(Prefs.ITEM_PREVIEW)) {
            previewReminder(view, item.getId(), item.getType());
        } else {
            if (item.getType().matches(Constants.TYPE_SHOPPING_LIST)){
                previewReminder(view, item.getId(), item.getType());
            } else {
                Reminder.toggle(item.getId(), getActivity(), mCallbacks);
                loaderAdapter(lastId);
            }
        }
    }

    @Override
    public void onItemLongClicked(final int position, final View view) {
        final CharSequence[] items = {getString(R.string.open), getString(R.string.edit),
                getString(R.string.change_group), getString(R.string.move_to_trash)};
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
    public void endExecution(final boolean result) {
        if (getActivity() != null) {
            loaderAdapter(lastId);
        }
    }
}
