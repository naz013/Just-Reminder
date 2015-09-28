package com.hexrain.design.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.interfaces.Prefs;
import com.cray.software.justreminder.interfaces.SyncListener;
import com.cray.software.justreminder.modules.Module;
import com.cray.software.justreminder.note.Note;
import com.cray.software.justreminder.note.NotesBase;
import com.cray.software.justreminder.note.NotesRecyclerAdapter;
import com.cray.software.justreminder.note.SyncNotes;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.hexrain.design.NavigationDrawerFragment;
import com.hexrain.design.ScreenManager;

import java.util.ArrayList;

public class NotesFragment extends Fragment implements SyncListener {

    private NotesBase db;
    private SharedPrefs sPrefs;
    private RecyclerView currentList;
    private LinearLayout emptyLayout, emptyItem;
    private AdView adView;

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
        db = new NotesBase(getActivity());
        if (!db.isOpen()) db.open();
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
                return true;
            case R.id.action_order:
                showDialog();
                return true;
            case MENU_ITEM_DELETE:
                deleteDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_screen_manager, container, false);

        sPrefs = new SharedPrefs(getActivity());

        currentList = (RecyclerView) rootView.findViewById(R.id.currentList);
        currentList.setLayoutManager(new LinearLayoutManager(getActivity()));

        emptyItem = (LinearLayout) rootView.findViewById(R.id.emptyItem);
        emptyItem.setVisibility(View.VISIBLE);

        TextView emptyText = (TextView) rootView.findViewById(R.id.emptyText);
        emptyText.setText(getActivity().getString(R.string.no_notes_text));
        emptyItem.setVisibility(View.VISIBLE);

        ImageView emptyImage = (ImageView) rootView.findViewById(R.id.emptyImage);
        if (sPrefs.loadBoolean(Prefs.USE_DARK_THEME)) {
            emptyImage.setImageResource(R.drawable.ic_event_note_white_24dp);
        } else {
            emptyImage.setImageResource(R.drawable.ic_event_note_grey600_24dp);
        }

        currentList = (RecyclerView) rootView.findViewById(R.id.currentList);

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

            RelativeLayout ads_container = (RelativeLayout) rootView.findViewById(R.id.ads_container);
        }

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
        if (!Module.isPro()){
            if (adView != null) {
                adView.resume();
            }
        }
        sPrefs = new SharedPrefs(getActivity());
        if (sPrefs.loadBoolean("isNew")) loaderAdapter();
        sPrefs.saveBoolean("isNew", false);
        getActivity().invalidateOptionsMenu();
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
        db = new NotesBase(getActivity());
        if (!db.isOpen()) db.open();
        else return;
        ArrayList<Note> data = new ArrayList<>();
        data.clear();
        Cursor c = db.getNotes();
        if (c != null && c.moveToFirst()){
            emptyItem.setVisibility(View.GONE);
            currentList.setVisibility(View.VISIBLE);
            do {
                String note = c.getString(c.getColumnIndex(Constants.COLUMN_NOTE));
                int color = c.getInt(c.getColumnIndex(Constants.COLUMN_COLOR));
                int style = c.getInt(c.getColumnIndex(Constants.COLUMN_FONT_STYLE));
                byte[] image = c.getBlob(c.getColumnIndex(Constants.COLUMN_IMAGE));
                long id = c.getLong(c.getColumnIndex(Constants.COLUMN_ID));
                data.add(new Note(note, color, style, image, id));
            } while (c.moveToNext());
            NotesRecyclerAdapter adapter = new NotesRecyclerAdapter(getActivity(), data);
            currentList.setAdapter(adapter);
            currentList.setItemAnimator(new DefaultItemAnimator());
            if (adapter.getItemCount() == 0) {
                emptyItem.setVisibility(View.VISIBLE);
                currentList.setVisibility(View.GONE);
            }
            if (adapter.getItemCount() > 0) {
                if (mCallbacks != null) mCallbacks.onListChange(currentList);
            }
        } else {
            emptyItem.setVisibility(View.VISIBLE);
            currentList.setVisibility(View.GONE);
        }
        if (c != null) c.close();
        db.close();
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
                sPrefs = new SharedPrefs(getActivity());
                sPrefs.saveBoolean("isNew", true);
                if (mCallbacks != null) mCallbacks.onNavigationDrawerItemSelected(ScreenManager.FRAGMENT_NOTE);
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void deleteAll(){
        db = new NotesBase(getActivity());
        if (!db.isOpen()) db.open();
        Cursor c = db.getNotes();
        if (c != null && c.moveToFirst()){
            do{
                long rowId = c.getLong(c.getColumnIndex(Constants.COLUMN_ID));
                Note.deleteNote(rowId, getActivity());

            }while (c.moveToNext());
        }
        if (c != null) c.close();
    }

    @Override
    public void endExecution(boolean result) {
        if (result) {
            loaderAdapter();
            getActivity().invalidateOptionsMenu();
        }
    }
}
