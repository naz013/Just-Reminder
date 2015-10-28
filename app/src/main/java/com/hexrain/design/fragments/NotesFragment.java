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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cray.software.justreminder.NotesManager;
import com.cray.software.justreminder.R;
import com.cray.software.justreminder.adapters.NoteRecyclerAdapter;
import com.cray.software.justreminder.async.SyncNotes;
import com.cray.software.justreminder.databases.NotesBase;
import com.cray.software.justreminder.datas.NoteModel;
import com.cray.software.justreminder.datas.NoteDataProvider;
import com.cray.software.justreminder.helpers.Messages;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.interfaces.Prefs;
import com.cray.software.justreminder.interfaces.SimpleListener;
import com.cray.software.justreminder.interfaces.SyncListener;
import com.cray.software.justreminder.modules.Module;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.hexrain.design.NavigationDrawerFragment;
import com.hexrain.design.ScreenManager;

public class NotesFragment extends Fragment implements SyncListener, SimpleListener {

    private NotesBase db;
    private SharedPrefs sPrefs;
    private RecyclerView currentList;
    private LinearLayout emptyLayout, emptyItem;
    private AdView adView;
    private NoteDataProvider provider;

    private boolean onCreate = false;

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

        loaderAdapter();
        onCreate = true;

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
        if (sPrefs.loadBoolean("isNew") || !onCreate) loaderAdapter();
        onCreate = false;
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
        provider = new NoteDataProvider(getActivity());
        reloadView();
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        NoteRecyclerAdapter adapter = new NoteRecyclerAdapter(getActivity(), provider);
        adapter.setEventListener(this);
        currentList.setLayoutManager(mLayoutManager);
        currentList.setAdapter(adapter);
        currentList.setItemAnimator(new DefaultItemAnimator());
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
                NoteModel.deleteNote(rowId, getActivity(), mCallbacks);

            }while (c.moveToNext());
        }
        if (c != null) c.close();
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
        if (result) {
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
                            if (mCallbacks != null) mCallbacks.showSnackbar(R.string.error_sharing_note);
                            else Messages.toast(getActivity(), R.string.error_sharing_note);
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
                getString(R.string.color_grey), getString(R.string.led_color_pink),
                getString(R.string.color_dark_green), getString(R.string.color_brown)};
        if (Module.isPro()){
            items = new CharSequence[]{getString(R.string.led_color_red), getString(R.string.color_purple),
                    getString(R.string.led_color_green), getString(R.string.led_color_green_light),
                    getString(R.string.led_color_blue), getString(R.string.led_color_blue_light),
                    getString(R.string.led_color_yellow), getString(R.string.led_color_orange),
                    getString(R.string.color_grey), getString(R.string.led_color_pink),
                    getString(R.string.color_dark_green), getString(R.string.color_brown),
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
