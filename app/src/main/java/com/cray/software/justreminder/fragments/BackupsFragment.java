package com.cray.software.justreminder.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.ScreenManager;
import com.cray.software.justreminder.cloud.DropboxHelper;
import com.cray.software.justreminder.cloud.GDriveHelper;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.interfaces.NavigationCallbacks;
import com.cray.software.justreminder.spinner.SpinnerItem;
import com.cray.software.justreminder.spinner.TitleNavigationAdapter;

import java.util.ArrayList;

public class BackupsFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    public static final int LOCAL_INT = 120;
    public static final int DROPBOX_INT = 121;
    public static final int GOOGLE_DRIVE_INT = 122;


    private ArrayList<Item> navIds = new ArrayList<>();

    private Toolbar toolbar;
    private Spinner spinner;

    private NavigationCallbacks mCallbacks;

    public static BackupsFragment newInstance() {
        return new BackupsFragment();
    }

    public BackupsFragment() {
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Indicate that this fragment would like to influence the set of actions in the action bar.
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.backup_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_scan:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.backup_manager_layout, container, false);

        toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        spinner = (Spinner) inflater.inflate(R.layout.spinner, null);
        toolbar.addView(spinner);
        setNavigation();
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
        ((ScreenManager)activity).onSectionAttached(ScreenManager.FRAGMENT_BACKUPS);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    private void setNavigation(){
        navIds.clear();
        SharedPrefs prefs = new SharedPrefs(getActivity());
        boolean isDark = prefs.loadBoolean(Prefs.USE_DARK_THEME);
        if (isDark) {
            navIds.add(new Item(new SpinnerItem(getString(R.string.local),
                    R.drawable.ic_sd_storage_white_24dp), LOCAL_INT,
                    R.drawable.ic_sd_storage_white_24dp));
        } else {
            navIds.add(new Item(new SpinnerItem(getString(R.string.local),
                    R.drawable.ic_sd_storage_black_24dp), LOCAL_INT,
                    R.drawable.ic_sd_storage_white_24dp));
        }
        DropboxHelper dbx = new DropboxHelper(getActivity());
        dbx.startSession();
        if (dbx.isLinked()){
            if (isDark) {
                navIds.add(new Item(new SpinnerItem(getString(R.string.dropbox),
                        R.drawable.dropbox_icon_white), DROPBOX_INT,
                        R.drawable.dropbox_icon_white));
            } else {
                navIds.add(new Item(new SpinnerItem(getString(R.string.dropbox),
                        R.drawable.dropbox_icon), DROPBOX_INT,
                        R.drawable.dropbox_icon_white));
            }
        }
        GDriveHelper gdx = new GDriveHelper(getActivity());
        if (gdx.isLinked()) {
            if (isDark) {
                navIds.add(new Item(new SpinnerItem(getString(R.string.google_drive),
                        R.drawable.gdrive_icon_white),
                        GOOGLE_DRIVE_INT, R.drawable.gdrive_icon_white));
            } else {
                navIds.add(new Item(new SpinnerItem(getString(R.string.google_drive),
                        R.drawable.gdrive_icon), GOOGLE_DRIVE_INT,
                        R.drawable.gdrive_icon_white));
            }
        }

        ArrayList<SpinnerItem> navSpinner = new ArrayList<>();
        for (Item item : navIds){
            navSpinner.add(item.getSpinnerItem());
        }

        TitleNavigationAdapter adapter = new TitleNavigationAdapter(getActivity(), navSpinner);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
    }

    private void addFragment(Fragment fragment) {
        FragmentManager fragMan = getChildFragmentManager();
        FragmentTransaction ft = fragMan.beginTransaction();
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.replace(R.id.windowBackground, fragment);
        ft.commitAllowingStateLoss();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (position >= navIds.size()) {
            return;
        }
        final Item item = navIds.get(position);
        if (item.getId() == LOCAL_INT){
            addFragment(DropboxFragment.newInstance(LOCAL_INT));
            toolbar.setLogo(item.getLogo());
        } else {
            if (item.getId() == DROPBOX_INT){
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final boolean isC = SyncHelper.isConnected(getActivity());
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (isC) {
                                    addFragment(DropboxFragment.newInstance(DROPBOX_INT));
                                    toolbar.setLogo(item.getLogo());
                                } else {
                                    spinner.setSelection(0);
                                }
                            }
                        });
                    }
                }).start();
            }
            if (item.getId() == GOOGLE_DRIVE_INT){
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final boolean isC = SyncHelper.isConnected(getActivity());
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (isC) {
                                    addFragment(DropboxFragment.newInstance(GOOGLE_DRIVE_INT));
                                    toolbar.setLogo(item.getLogo());
                                } else {
                                    spinner.setSelection(0);
                                }
                            }
                        });
                    }
                }).start();
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public class Item {
        private SpinnerItem spinnerItem;
        private int id, logo;

        public Item(SpinnerItem spinnerItem, int id, int logo){
            this.spinnerItem = spinnerItem;
            this.id = id;
            this.logo = logo;
        }

        public int getLogo() {
            return logo;
        }

        public int getId() {
            return id;
        }

        public SpinnerItem getSpinnerItem() {
            return spinnerItem;
        }
    }
}
