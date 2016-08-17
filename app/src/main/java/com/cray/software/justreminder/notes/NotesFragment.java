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

package com.cray.software.justreminder.notes;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
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
import com.cray.software.justreminder.roboto_views.RoboTextView;

import java.util.List;

public class NotesFragment extends Fragment implements SyncListener, SimpleListener {

    private RecyclerView currentList;
    private LinearLayout emptyItem;
    private NoteRecyclerAdapter mAdapter;

    private boolean enableGrid = false;

    private NavigationCallbacks mCallbacks;
    private Activity mContext;

    public static NotesFragment newInstance() {
        return new NotesFragment();
    }

    public NotesFragment() {
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
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
            item.setTitle(!enableGrid ? mContext.getString(R.string.grid_view) : mContext.getString(R.string.list_view));
        }
        if (NoteHelper.getInstance(mContext).getCount() != 0) {
            menu.add(Menu.NONE, MENU_ITEM_DELETE, 100, getString(R.string.delete_all));
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_sync:
                new SyncNotesAsync(mContext, this).execute();
                break;
            case R.id.action_order:
                showDialog();
                break;
            case MENU_ITEM_DELETE:
                deleteDialog();
                break;
            case R.id.action_list:
                enableGrid = !enableGrid;
                SharedPrefs.getInstance(mContext).putBoolean(Prefs.REMINDER_CHANGED, enableGrid);
                loaderAdapter();
                mContext.invalidateOptionsMenu();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_screen_manager, container, false);
        enableGrid = SharedPrefs.getInstance(mContext).getBoolean(Prefs.REMINDER_CHANGED);
        currentList = (RecyclerView) rootView.findViewById(R.id.currentList);
        currentList.setLayoutManager(new LinearLayoutManager(mContext));
        rootView.findViewById(R.id.backgroundFragment).setBackgroundColor(ColorSetter.getInstance(mContext).getBackgroundStyle());
        emptyItem = (LinearLayout) rootView.findViewById(R.id.emptyItem);
        emptyItem.setVisibility(View.VISIBLE);
        RoboTextView emptyText = (RoboTextView) rootView.findViewById(R.id.emptyText);
        emptyText.setText(mContext.getString(R.string.no_notes));
        ImageView emptyImage = (ImageView) rootView.findViewById(R.id.emptyImage);
        if (ColorSetter.getInstance(mContext).isDark()) {
            emptyImage.setImageResource(R.drawable.ic_event_note_white_vector);
        } else {
            emptyImage.setImageResource(R.drawable.ic_event_note_black_vector);
        }

        currentList = (RecyclerView) rootView.findViewById(R.id.currentList);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(mContext);
        if (SharedPrefs.getInstance(mContext).getBoolean(Prefs.REMINDER_CHANGED)){
            layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        }
        currentList.setLayoutManager(layoutManager);

        loaderAdapter();
        return rootView;
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
        ((ScreenManager) context).onSectionAttached(ScreenManager.FRAGMENT_NOTE);
    }

    @Override
    public void onAttach(Activity activity) {
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
        ((ScreenManager) activity).onSectionAttached(ScreenManager.FRAGMENT_NOTE);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (SharedPrefs.getInstance(mContext).getBoolean(Prefs.NOTE_CHANGED)) {
            loaderAdapter();
        }
        mContext.invalidateOptionsMenu();
    }

    private void showDialog(){
        final CharSequence[] items = {mContext.getString(R.string.by_date_az),
                mContext.getString(R.string.by_date_za),
                mContext.getString(R.string.name_az),
                mContext.getString(R.string.name_za)};
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(getString(R.string.order));
        builder.setItems(items, (dialog, which) -> {
            String value = null;
            if (which == 0) {
                value = Constants.ORDER_DATE_A_Z;
            } else if (which == 1) {
                value = Constants.ORDER_DATE_Z_A;
            } else if (which == 2) {
                value = Constants.ORDER_NAME_A_Z;
            } else if (which == 3) {
                value = Constants.ORDER_NAME_Z_A;
            }
            SharedPrefs.getInstance(mContext).putString(Prefs.NOTES_ORDER, value);
            dialog.dismiss();
            loaderAdapter();
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void loaderAdapter(){
        SharedPrefs.getInstance(mContext).putBoolean(Prefs.NOTE_CHANGED, false);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(mContext);
        if (SharedPrefs.getInstance(mContext).getBoolean(Prefs.REMINDER_CHANGED)){
            layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        }
        currentList.setLayoutManager(layoutManager);
        mAdapter = new NoteRecyclerAdapter(mContext, NoteHelper.getInstance(mContext).getAll());
        mAdapter.setEventListener(this);
        reloadView();
        currentList.setAdapter(mAdapter);
        currentList.setItemAnimator(new DefaultItemAnimator());
        if (mCallbacks != null) {
            mCallbacks.onListChanged(currentList);
        }
    }

    private void reloadView() {
        int size = mAdapter.getItemCount();
        if (size > 0){
            currentList.setVisibility(View.VISIBLE);
            emptyItem.setVisibility(View.GONE);
        } else {
            currentList.setVisibility(View.GONE);
            emptyItem.setVisibility(View.VISIBLE);
        }
    }

    private void deleteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
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
        List<NoteItem> list = NoteHelper.getInstance(mContext).getAll();
        for (NoteItem item : list) {
            NoteHelper.getInstance(mContext).deleteNote(item.getId(), mCallbacks);
        }
    }

    private void previewNote(long id, View view){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Intent intent = new Intent(mContext, NotePreview.class);
            intent.putExtra(Constants.EDIT_ID, id);
            String transitionName = "image";
            ActivityOptionsCompat options =
                    ActivityOptionsCompat.makeSceneTransitionAnimation(mContext, view,
                            transitionName);
            mContext.startActivity(intent, options.toBundle());
        } else {
            mContext.startActivity(new Intent(mContext, NotePreview.class)
                            .putExtra(Constants.EDIT_ID, id));
        }
    }

    @Override
    public void endExecution(boolean result) {
        if (result && mContext != null) {
            loaderAdapter();
            mContext.invalidateOptionsMenu();
        }
    }

    @Override
    public void onItemClicked(int position, View view) {
        long id = mAdapter.getItem(position).getId();
        if (SharedPrefs.getInstance(mContext).getBoolean(Prefs.ITEM_PREVIEW)) {
            previewNote(id, view);
        } else {
            mContext.startActivity(new Intent(mContext, NotesManager.class)
                    .putExtra(Constants.EDIT_ID, id));
        }
    }

    @Override
    public void onItemLongClicked(final int position, final View view) {
        final String[] items = {getString(R.string.open), getString(R.string.share),
                getString(R.string.change_color), getString(R.string.edit), getString(R.string.delete)};
        Dialogues.showLCAM(mContext, item -> {
            long id = mAdapter.getItem(position).getId();
            switch (item){
                case 0:
                    previewNote(id, view);
                    break;
                case 1:
                    if (NoteHelper.getInstance(mContext).shareNote(id)){
                        Messages.toast(mContext, mContext.getString(R.string.sent));
                    } else {
                        if (mCallbacks != null) {
                            mCallbacks.showSnackbar(R.string.error_sending);
                        } else {
                            Messages.toast(mContext, R.string.error_sending);
                        }
                    }
                    break;
                case 2:
                    selectColor(id);
                    break;
                case 3:
                    mContext.startActivity(new Intent(mContext, NotesManager.class)
                            .putExtra(Constants.EDIT_ID, id));
                    break;
                case 4:
                    NoteHelper.getInstance(mContext).deleteNote(id, mCallbacks);
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
        Dialogues.showLCAM(mContext, item -> {
            NoteHelper.getInstance(mContext).changeColor(id, item);
            loaderAdapter();
        }, items);
    }
}
