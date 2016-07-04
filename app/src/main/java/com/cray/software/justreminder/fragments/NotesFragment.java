/**
 * Copyright 2016 Nazar Suhovich
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

package com.cray.software.justreminder.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
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
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.Dialogues;
import com.cray.software.justreminder.helpers.Messages;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.interfaces.NavigationCallbacks;
import com.cray.software.justreminder.interfaces.SimpleListener;
import com.cray.software.justreminder.interfaces.SyncListener;
import com.cray.software.justreminder.modules.Module;
import com.cray.software.justreminder.notes.NoteDataProvider;
import com.cray.software.justreminder.notes.NoteModel;
import com.cray.software.justreminder.notes.NotePreview;
import com.cray.software.justreminder.notes.NoteRecyclerAdapter;
import com.cray.software.justreminder.notes.NotesBase;
import com.cray.software.justreminder.notes.NotesManager;
import com.cray.software.justreminder.notes.SyncNotes;
import com.cray.software.justreminder.roboto_views.RoboTextView;

public class NotesFragment extends Fragment implements SyncListener, SimpleListener {

    private SharedPrefs sPrefs;
    private RecyclerView currentList;
    private LinearLayout emptyItem;
    private NoteDataProvider provider;

    private boolean enableGrid = false;

    private NavigationCallbacks mCallbacks;

    public static NotesFragment newInstance() {
        return new NotesFragment();
    }

    public NotesFragment() {
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Indicate that this fragment would like to influence the set of actions in the action bar.
        setHasOptionsMenu(true);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
    }

    public static final int MENU_ITEM_DELETE = 12;

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.notes_menu, menu);
        MenuItem item = menu.findItem(R.id.action_list);
        if (item != null){
            item.setIcon(!enableGrid ? R.drawable.ic_view_quilt_white_24dp : R.drawable.ic_view_list_white_24dp);
            item.setTitle(!enableGrid ? getActivity().getString(R.string.grid_view) : getActivity().getString(R.string.list_view));
        }
        NotesBase db = new NotesBase(getActivity());
        if (!db.isOpen()) {
            db.open();
        }
        if (db.getCount() != 0) {
            menu.add(Menu.NONE, MENU_ITEM_DELETE, 100, getString(R.string.delete_all));
        }
        db.close();
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_sync:
                new SyncNotes(getActivity(), this).execute();
                break;
            case R.id.action_order:
                showDialog();
                break;
            case MENU_ITEM_DELETE:
                deleteDialog();
                break;
            case R.id.action_list:
                enableGrid = !enableGrid;
                new SharedPrefs(getActivity()).saveBoolean(Prefs.REMINDER_CHANGED, enableGrid);
                loaderAdapter();
                getActivity().invalidateOptionsMenu();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_screen_manager, container, false);

        sPrefs = new SharedPrefs(getActivity());
        enableGrid = sPrefs.loadBoolean(Prefs.REMINDER_CHANGED);

        currentList = (RecyclerView) rootView.findViewById(R.id.currentList);
        currentList.setLayoutManager(new LinearLayoutManager(getActivity()));

        emptyItem = (LinearLayout) rootView.findViewById(R.id.emptyItem);
        emptyItem.setVisibility(View.VISIBLE);
        RoboTextView emptyText = (RoboTextView) rootView.findViewById(R.id.emptyText);
        emptyText.setText(getActivity().getString(R.string.no_notes));
        emptyItem.setVisibility(View.VISIBLE);
        ImageView emptyImage = (ImageView) rootView.findViewById(R.id.emptyImage);
        if (new ColorSetter(getActivity()).isDark()) {
            emptyImage.setImageResource(R.drawable.ic_event_note_white_vector);
        } else {
            emptyImage.setImageResource(R.drawable.ic_event_note_black_vector);
        }

        currentList = (RecyclerView) rootView.findViewById(R.id.currentList);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        if (new SharedPrefs(getActivity()).loadBoolean(Prefs.REMINDER_CHANGED)){
            layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        }
        currentList.setLayoutManager(layoutManager);

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
        ((ScreenManager)activity).onSectionAttached(ScreenManager.FRAGMENT_NOTE);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        sPrefs = new SharedPrefs(getActivity());
        if (sPrefs.loadBoolean(Prefs.NOTE_CHANGED)) {
            loaderAdapter();
        }
        getActivity().invalidateOptionsMenu();
    }

    private void showDialog(){
        final CharSequence[] items = {getActivity().getString(R.string.by_date_az),
                getActivity().getString(R.string.by_date_za),
                getActivity().getString(R.string.name_az),
                getActivity().getString(R.string.name_za)};
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.order));
        builder.setItems(items, (dialog, which) -> {
            SharedPrefs prefs = new SharedPrefs(getActivity());
            if (which == 0) {
                prefs.savePrefs(Prefs.NOTES_ORDER, Constants.ORDER_DATE_A_Z);
            } else if (which == 1) {
                prefs.savePrefs(Prefs.NOTES_ORDER, Constants.ORDER_DATE_Z_A);
            } else if (which == 2) {
                prefs.savePrefs(Prefs.NOTES_ORDER, Constants.ORDER_NAME_A_Z);
            } else if (which == 3) {
                prefs.savePrefs(Prefs.NOTES_ORDER, Constants.ORDER_NAME_Z_A);
            }
            dialog.dismiss();
            loaderAdapter();
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void loaderAdapter(){
        new SharedPrefs(getActivity()).saveBoolean(Prefs.NOTE_CHANGED, false);
        provider = new NoteDataProvider(getActivity());
        reloadView();
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        if (new SharedPrefs(getActivity()).loadBoolean(Prefs.REMINDER_CHANGED)){
            layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        }
        currentList.setLayoutManager(layoutManager);
        NoteRecyclerAdapter adapter = new NoteRecyclerAdapter(getActivity(), provider);
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

    private void deleteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(true);
        builder.setMessage(R.string.delete_all_notes);
        builder.setNegativeButton(getString(R.string.no), (dialog, which) -> {
            dialog.dismiss();
        });
        builder.setPositiveButton(getString(R.string.yes), (dialog, which) -> {
            dialog.dismiss();
            deleteAll();
            loaderAdapter();
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void deleteAll(){
        NotesBase db = new NotesBase(getActivity());
        if (!db.isOpen()) db.open();
        Cursor c = db.getNotes();
        if (c != null && c.moveToFirst()) {
            do {
                long rowId = c.getLong(c.getColumnIndex(Constants.COLUMN_ID));
                NoteModel.deleteNote(rowId, getActivity(), mCallbacks);

            } while (c.moveToNext());
        }
        if (c != null) c.close();
        db.close();
    }

    private void previewNote(long id, View view){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Intent intent = new Intent(getActivity(), NotePreview.class);
            intent.putExtra(Constants.EDIT_ID, id);
            String transitionName = "image";
            ActivityOptionsCompat options =
                    ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(), view,
                            transitionName);
            getActivity().startActivity(intent, options.toBundle());
        } else {
            getActivity().startActivity(
                    new Intent(getActivity(), NotePreview.class)
                            .putExtra(Constants.EDIT_ID, id));
        }
    }

    @Override
    public void endExecution(boolean result) {
        if (result && getActivity() != null) {
            loaderAdapter();
            getActivity().invalidateOptionsMenu();
        }
    }

    @Override
    public void onItemClicked(int position, View view) {
        long id = provider.getItem(position).getId();
        sPrefs = new SharedPrefs(getActivity());
        if (sPrefs.loadBoolean(Prefs.ITEM_PREVIEW)) {
            previewNote(id, view);
        } else {
            getActivity().startActivity(new Intent(getActivity(), NotesManager.class)
                    .putExtra(Constants.EDIT_ID, id));
        }
    }

    @Override
    public void onItemLongClicked(final int position, final View view) {
        final String[] items = {getString(R.string.open), getString(R.string.share),
                getString(R.string.change_color), getString(R.string.edit), getString(R.string.delete)};
        Dialogues.showLCAM(getActivity(), item -> {
            long id = provider.getItem(position).getId();
            switch (item){
                case 0:
                    previewNote(id, view);
                    break;
                case 1:
                    if (NoteModel.shareNote(id, getActivity())){
                        Messages.toast(getActivity(), getActivity().getString(R.string.sent));
                    } else {
                        if (mCallbacks != null) {
                            mCallbacks.showSnackbar(R.string.error_sending);
                        } else {
                            Messages.toast(getActivity(), R.string.error_sending);
                        }
                    }
                    break;
                case 2:
                    selectColor(id);
                    break;
                case 3:
                    getActivity().startActivity(new Intent(getActivity(), NotesManager.class)
                            .putExtra(Constants.EDIT_ID, id));
                    break;
                case 4:
                    NoteModel.deleteNote(id, getActivity(), mCallbacks);
                    loaderAdapter();
                    break;
            }
        }, items);
    }

    private void selectColor(final long id) {
        String[] items = {getString(R.string.red), getString(R.string.purple),
                getString(R.string.green), getString(R.string.green_light),
                getString(R.string.blue), getString(R.string.blue_light),
                getString(R.string.yellow), getString(R.string.orange),
                getString(R.string.cyan), getString(R.string.pink),
                getString(R.string.teal), getString(R.string.amber)};
        if (Module.isPro()){
            items = new String[]{getString(R.string.red), getString(R.string.purple),
                    getString(R.string.green), getString(R.string.green_light),
                    getString(R.string.blue), getString(R.string.blue_light),
                    getString(R.string.yellow), getString(R.string.orange),
                    getString(R.string.cyan), getString(R.string.pink),
                    getString(R.string.teal), getString(R.string.amber),
                    getString(R.string.dark_purple), getString(R.string.dark_orange),
                    getString(R.string.lime), getString(R.string.indigo)};
        }
        Dialogues.showLCAM(getActivity(), item -> {
            NoteModel.setNewColor(getActivity(), id, item);
            loaderAdapter();
        }, items);
    }
}
