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
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cray.software.justreminder.NotesManager;
import com.cray.software.justreminder.R;
import com.cray.software.justreminder.adapters.NoteRecyclerAdapter;
import com.cray.software.justreminder.async.SyncNotes;
import com.cray.software.justreminder.databases.NotesBase;
import com.cray.software.justreminder.datas.NoteDataProvider;
import com.cray.software.justreminder.datas.models.NoteModel;
import com.cray.software.justreminder.helpers.Messages;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.interfaces.SimpleListener;
import com.cray.software.justreminder.interfaces.SyncListener;
import com.cray.software.justreminder.modules.Module;
import com.cray.software.justreminder.ScreenManager;
import com.cray.software.justreminder.NotePreviewFragment;

public class NotesFragment extends Fragment implements SyncListener, SimpleListener {

    private NotesBase db;
    private SharedPrefs sPrefs;
    private RecyclerView currentList;
    private LinearLayout emptyItem;
    private NoteDataProvider provider;

    private boolean enableGrid = false;

    private NavigationDrawerFragment.NavigationDrawerCallbacks mCallbacks;

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
            item.setTitle(!enableGrid ? getActivity().getString(R.string.show_grid) : getActivity().getString(R.string.show_list));
        }
        db = new NotesBase(getActivity());
        if (!db.isOpen()) {
            db.open();
        }
        if (db.getCount() != 0) {
            menu.add(Menu.NONE, MENU_ITEM_DELETE, 100, getString(R.string.delete_all_notes));
        }
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

        TextView emptyText = (TextView) rootView.findViewById(R.id.emptyText);
        emptyText.setText(getActivity().getString(R.string.no_notes_text));
        emptyItem.setVisibility(View.VISIBLE);

        ImageView emptyImage = (ImageView) rootView.findViewById(R.id.emptyImage);
        if (new SharedPrefs(getActivity()).loadBoolean(Prefs.USE_DARK_THEME)) {
            emptyImage.setImageResource(R.drawable.note_white);
        } else {
            emptyImage.setImageResource(R.drawable.note);
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
            mCallbacks = (NavigationDrawerFragment.NavigationDrawerCallbacks) activity;
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
        final CharSequence[] items = {getString(R.string.sort_item_by_date_a_z),
                getString(R.string.sort_item_by_date_z_a),
                getString(R.string.sort_name_a_z),
                getString(R.string.sort_name_z_a)};
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.menu_order_by));
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                SharedPrefs prefs = new SharedPrefs(getActivity());
                if (item == 0) {
                    prefs.savePrefs(Prefs.NOTES_ORDER, Constants.ORDER_DATE_A_Z);
                } else if (item == 1) {
                    prefs.savePrefs(Prefs.NOTES_ORDER, Constants.ORDER_DATE_Z_A);
                } else if (item == 2) {
                    prefs.savePrefs(Prefs.NOTES_ORDER, Constants.ORDER_NAME_A_Z);
                } else if (item == 3) {
                    prefs.savePrefs(Prefs.NOTES_ORDER, Constants.ORDER_NAME_Z_A);
                }
                dialog.dismiss();
                loaderAdapter();
            }
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
        builder.setTitle(getString(R.string.delete_all_notes));
        builder.setMessage(getString(R.string.delete_all_dialog_message));
        builder.setNegativeButton(getString(R.string.import_dialog_button_no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setPositiveButton(getString(R.string.import_dialog_button_yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                deleteAll();
                loaderAdapter();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void deleteAll(){
        db = new NotesBase(getActivity());
        if (!db.isOpen()) {
            db.open();
        }
        Cursor c = db.getNotes();
        if (c != null && c.moveToFirst()){
            do{
                long rowId = c.getLong(c.getColumnIndex(Constants.COLUMN_ID));
                NoteModel.deleteNote(rowId, getActivity(), mCallbacks);

            }while (c.moveToNext());
        }
        if (c != null) {
            c.close();
        }
    }

    private void previewNote(long id, View view){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Intent intent = new Intent(getActivity(), NotePreviewFragment.class);
            intent.putExtra(Constants.EDIT_ID, id);
            String transitionName = "image";
            ActivityOptionsCompat options =
                    ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(), view,
                            transitionName);
            getActivity().startActivity(intent, options.toBundle());
        } else {
            getActivity().startActivity(
                    new Intent(getActivity(), NotePreviewFragment.class)
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
        final CharSequence[] items = {getString(R.string.open), getString(R.string.share_note_title),
                getString(R.string.change_color), getString(R.string.edit), getString(R.string.delete)};
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                dialog.dismiss();
                long id = provider.getItem(position).getId();
                switch (item){
                    case 0:
                        previewNote(id, view);
                        break;
                    case 1:
                        if (NoteModel.shareNote(id, getActivity())){
                            Messages.toast(getActivity(), R.string.message_note_shared);
                        } else {
                            if (mCallbacks != null) {
                                mCallbacks.showSnackbar(R.string.error_sharing_note);
                            } else {
                                Messages.toast(getActivity(), R.string.error_sharing_note);
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
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void selectColor(final long id) {
        CharSequence[] items = {getString(R.string.led_color_red), getString(R.string.color_purple),
                getString(R.string.led_color_green), getString(R.string.led_color_green_light),
                getString(R.string.led_color_blue), getString(R.string.led_color_blue_light),
                getString(R.string.led_color_yellow), getString(R.string.led_color_orange),
                getString(R.string.color_cyan), getString(R.string.led_color_pink),
                getString(R.string.color_dark_green), getString(R.string.color_amber)};
        if (Module.isPro()){
            items = new CharSequence[]{getString(R.string.led_color_red), getString(R.string.color_purple),
                    getString(R.string.led_color_green), getString(R.string.led_color_green_light),
                    getString(R.string.led_color_blue), getString(R.string.led_color_blue_light),
                    getString(R.string.led_color_yellow), getString(R.string.led_color_orange),
                    getString(R.string.color_cyan), getString(R.string.led_color_pink),
                    getString(R.string.color_dark_green), getString(R.string.color_amber),
                    getString(R.string.color_deep_purple), getString(R.string.color_deep_orange),
                    getString(R.string.color_lime), getString(R.string.color_indigo)};
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                dialog.dismiss();
                NoteModel.setNewColor(getActivity(), id, item);
                loaderAdapter();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }
}