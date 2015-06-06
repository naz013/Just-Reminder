package com.hexrain.design.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.cray.software.justreminder.BackupFileEdit;
import com.cray.software.justreminder.R;
import com.cray.software.justreminder.adapters.FileCursorAdapter;
import com.cray.software.justreminder.async.ScanTask;
import com.cray.software.justreminder.cloud.AccountInfo;
import com.cray.software.justreminder.cloud.DropboxHelper;
import com.cray.software.justreminder.cloud.DropboxQuota;
import com.cray.software.justreminder.cloud.GDriveHelper;
import com.cray.software.justreminder.databases.FilesDataBase;
import com.cray.software.justreminder.graph.PieGraph;
import com.cray.software.justreminder.graph.PieSlice;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.spinnerMenu.SpinnerItem;
import com.cray.software.justreminder.spinnerMenu.TitleNavigationAdapter;
import com.hexrain.design.NavigationDrawerFragment;
import com.hexrain.design.ScreenManager;
import com.wdullaer.swipeactionadapter.SwipeActionAdapter;
import com.wdullaer.swipeactionadapter.SwipeDirections;

import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class BackupsFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    SharedPrefs sPrefs;
    DropboxHelper dbx = new DropboxHelper(getActivity());
    SyncHelper syncHelper;
    ProgressDialog pd;
    ColorSetter cSetter;
    GDriveHelper gdx;

    private ArrayList<Integer> navIds;

    FileCursorAdapter fileCursorAdapter;
    FilesDataBase filesDataBase = new FilesDataBase(getActivity());

    LinearLayout localLayout, cloudLayout, container, cloudContainer, googleContainer, googleLayout;
    TextView localCount, cloudUser, cloudCount, backupText, backupFilesText, cloudText,
            usedSpace, freeSpace, cloudFiles, googleUser, googleSpace, googleFreeSpace,
            googleText, googleCount, googleFiles;
    Button deleteAllButton, deleteAllCloudButton, googleDeleteAllCloudButton;
    ListView filesList, filesCloudList, filesGoogleList;
    PieGraph usedSizeGraph, googleSizeGraph;

    Typeface typefaceLight, typefaceMedium, typefaceThin;
    boolean isDropboxDeleted = false, isGoogleDeleted = false, isLocalDeleted = false;

    Toolbar toolbar;
    Spinner spinner;
    View rootView;

    private NavigationDrawerFragment.NavigationDrawerCallbacks mCallbacks;

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
                new ScanTask(getActivity()).execute();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.backup_manager_layout, container, false);

        cSetter = new ColorSetter(getActivity());
        sPrefs = new SharedPrefs(getActivity());
        dbx = new DropboxHelper(getActivity());
        dbx.startSession();

        typefaceLight = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Light.ttf");
        typefaceMedium = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Medium.ttf");
        typefaceThin = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Thin.ttf");

        toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);

        spinner = (Spinner) inflater.inflate(R.layout.spinner, null);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        layoutParams.setMargins(56, 0, 0, 0);
        toolbar.addView(spinner);

        setNavigation();

        clearForm(rootView);
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
        ((ScreenManager)activity).onSectionAttached(ScreenManager.FRAGMENT_BACKUPS);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        dbx = new DropboxHelper(getActivity());
        if (!dbx.isLinked()) {
            if (dbx.checkLink()) {
                setNavigation();
            } else setNavigation();
        }
        new ScanTask(getActivity()).execute();
    }

    private void setNavigation(){
        ArrayList<SpinnerItem> navSpinner = new ArrayList<>();
        navSpinner.clear();
        navIds = new ArrayList<>();
        navIds.clear();
        sPrefs = new SharedPrefs(getActivity());
        boolean isDark = sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_USE_DARK_THEME);
        if (isDark) {
            navSpinner.add(new SpinnerItem(getString(R.string.local_list_item), R.drawable.ic_devices_white_24dp));
            navIds.add(Constants.LOCAL_INT);
        } else {
            navSpinner.add(new SpinnerItem(getString(R.string.local_list_item), R.drawable.ic_devices_grey600_24dp));
            navIds.add(Constants.LOCAL_INT);
        }
        dbx = new DropboxHelper(getActivity());
        if (dbx.isLinked()){
            if (isDark) {
                navSpinner.add(new SpinnerItem("Dropbox", R.drawable.dropbox_icon_white));
                navIds.add(Constants.DROPBOX_INT);
            } else {
                navSpinner.add(new SpinnerItem("Dropbox", R.drawable.dropbox_icon));
                navIds.add(Constants.DROPBOX_INT);
            }
        }
        gdx = new GDriveHelper(getActivity());
        if (gdx.isLinked()) {
            if (isDark) {
                navSpinner.add(new SpinnerItem(getString(R.string.google_drive_title), R.drawable.gdrive_icon_white));
                navIds.add(Constants.GOOGLE_DRIVE_INT);
            } else {
                navSpinner.add(new SpinnerItem(getString(R.string.google_drive_title), R.drawable.gdrive_icon));
                navIds.add(Constants.GOOGLE_DRIVE_INT);
            }
        }

        TitleNavigationAdapter adapter = new TitleNavigationAdapter(getActivity(), navSpinner);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
    }

    private void setSwipeDismissAdapter(ListView lv, FileCursorAdapter cursorAdapter,
                                        SwipeActionAdapter.SwipeActionListener listener) {
        final SwipeActionAdapter mAdapter = new SwipeActionAdapter(cursorAdapter);
        mAdapter.setListView(lv);
        mAdapter.setFixedBackgrounds(true);
        mAdapter.addBackground(SwipeDirections.DIRECTION_NORMAL_LEFT, R.layout.swipe_delete_layout)
                .addBackground(SwipeDirections.DIRECTION_NORMAL_RIGHT, R.layout.swipe_edit_layout)
                .addBackground(SwipeDirections.DIRECTION_FAR_LEFT, R.layout.swipe_delete_layout)
                .addBackground(SwipeDirections.DIRECTION_FAR_RIGHT, R.layout.swipe_edit_layout);
        mAdapter.setSwipeActionListener(listener);
        lv.setAdapter(mAdapter);
    }

    private void attachDropbox() {
        cloudContainer = (LinearLayout) rootView.findViewById(R.id.cloudContainer);
        cloudContainer.setVisibility(View.VISIBLE);

        cloudLayout = (LinearLayout) rootView.findViewById(R.id.cloudLayout);
        cloudLayout.setVisibility(View.VISIBLE);

        CardView card1 = (CardView) rootView.findViewById(R.id.card1);
        CardView card2 = (CardView) rootView.findViewById(R.id.card2);
        CardView card3 = (CardView) rootView.findViewById(R.id.card3);
        card1.setCardBackgroundColor(cSetter.getCardStyle());
        card2.setCardBackgroundColor(cSetter.getCardStyle());
        card3.setCardBackgroundColor(cSetter.getCardStyle());

        cloudUser = (TextView) rootView.findViewById(R.id.cloudUser);
        cloudUser.setTypeface(typefaceThin);

        usedSizeGraph = (PieGraph) rootView.findViewById(R.id.usedSizeGraph);

        cloudText = (TextView) rootView.findViewById(R.id.cloudText);
        cloudText.setTypeface(typefaceThin);

        cloudCount = (TextView) rootView.findViewById(R.id.cloudCount);
        cloudCount.setTypeface(typefaceMedium);

        usedSpace = (TextView) rootView.findViewById(R.id.usedSpace);
        usedSpace.setTypeface(typefaceThin);

        freeSpace = (TextView) rootView.findViewById(R.id.freeSpace);
        freeSpace.setTypeface(typefaceThin);

        cloudFiles = (TextView) rootView.findViewById(R.id.cloudFiles);
        cloudFiles.setTypeface(typefaceLight);
        cloudFiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (filesCloudList.getVisibility() != View.VISIBLE) {
                    filesCloudList.setVisibility(View.VISIBLE);
                    cloudContainer.setVisibility(View.GONE);
                } else {
                    filesCloudList.setVisibility(View.GONE);
                    if (isDropboxDeleted) {
                        pd = ProgressDialog.show(getActivity(), null, getString(R.string.receiving_data_text), false);
                        loadInfo(pd);
                    } else {
                        cloudContainer.setVisibility(View.VISIBLE);
                        isDropboxDeleted = false;
                    }
                }
            }
        });

        deleteAllCloudButton = (Button) rootView.findViewById(R.id.deleteAllCloudButton);
        deleteAllCloudButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                filesDataBase = new FilesDataBase(getActivity());
                filesDataBase.open();
                Cursor c = filesDataBase.getTask(Constants.FilesConstants.FILE_TYPE_GDRIVE);
                if (c != null && c.moveToFirst()) {
                    do {
                        String uuID;
                        long id = c.getLong(c.getColumnIndex(Constants.COLUMN_ID));
                        uuID = c.getString(c.getColumnIndex(Constants.COLUMN_TECH_VAR));
                        syncHelper = new SyncHelper(getActivity());
                        if (syncHelper.isSdPresent()){
                            File sdPath = Environment.getExternalStorageDirectory();
                            File sdPathDr = new File(sdPath.toString() + "/JustReminder/" + Constants.DIR_SD_DBX_TMP);
                            String exportFileName = uuID + Constants.FILE_NAME;
                            File file = new File(sdPathDr, exportFileName);
                            if (file.exists()){
                                file.delete();
                            }
                        }
                        deleteFromDropbox(uuID);
                        filesDataBase.deleteTask(id);
                    }
                    while (c.moveToNext());
                }
                if (c != null) c.close();
                if (cloudContainer.getVisibility() == View.GONE) {
                    isDropboxDeleted = true;
                }
            }
        });
        deleteAllCloudButton.setTypeface(typefaceLight);

        filesCloudList = (ListView) rootView.findViewById(R.id.filesCloudList);
        filesCloudList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startActivity(new Intent(getActivity(), BackupFileEdit.class).putExtra(Constants.EDIT_ID, id));
            }
        });
        pd = ProgressDialog.show(getActivity(), null, getString(R.string.receiving_data_text), false);
        cloudContainer.setVisibility(View.GONE);
        loadInfo(pd);
        filesDataBase = new FilesDataBase(getActivity());
        filesDataBase.open();
        fileCursorAdapter = new FileCursorAdapter(getActivity(),
                filesDataBase.getTask(Constants.FilesConstants.FILE_TYPE_DROPBOX));
        filesCloudList.setAdapter(fileCursorAdapter);
        setSwipeDismissAdapter(filesCloudList, fileCursorAdapter, new SwipeActionAdapter.SwipeActionListener() {
            @Override
            public boolean hasActions(int position) {
                return true;
            }

            @Override
            public boolean shouldDismiss(int position, int direction) {
                return direction == SwipeDirections.DIRECTION_NORMAL_LEFT;
            }

            @Override
            public void onSwipe(int[] positionList, int[] directionList) {
                for (int ii = 0; ii < positionList.length; ii++) {
                    int direction = directionList[ii];
                    int position = positionList[ii];
                    filesDataBase = new FilesDataBase(getActivity());
                    filesDataBase.open();
                    fileCursorAdapter = new FileCursorAdapter(getActivity(),
                            filesDataBase.getTask(Constants.FilesConstants.FILE_TYPE_DROPBOX));
                    final long itemId = fileCursorAdapter.getItemId(position);

                    switch (direction) {
                        case SwipeDirections.DIRECTION_NORMAL_LEFT:
                            if (itemId != 0) {
                                String uuID = "";
                                Cursor c = filesDataBase.getTask(itemId);
                                if (c != null && c.moveToFirst()){
                                    uuID = c.getString(c.getColumnIndex(Constants.COLUMN_TECH_VAR));
                                }
                                if (c != null) c.close();
                                syncHelper = new SyncHelper(getActivity());
                                if (syncHelper.isSdPresent()){
                                    File sdPath = Environment.getExternalStorageDirectory();
                                    File sdPathDr = new File(sdPath.toString() + "/JustReminder/" + Constants.DIR_SD_DBX_TMP);
                                    String exportFileName = uuID + Constants.FILE_NAME;
                                    File file = new File(sdPathDr, exportFileName);
                                    if (file.exists()){
                                        file.delete();
                                    }
                                }

                                deleteFromDropbox(uuID);
                                filesDataBase.deleteTask(itemId);
                                isDropboxDeleted = true;
                            }
                            break;
                        case SwipeDirections.DIRECTION_FAR_LEFT:
                            if (itemId != 0) {
                                String uuID = "";
                                Cursor c = filesDataBase.getTask(itemId);
                                if (c != null && c.moveToFirst()){
                                    uuID = c.getString(c.getColumnIndex(Constants.COLUMN_TECH_VAR));
                                }
                                if (c != null) c.close();
                                syncHelper = new SyncHelper(getActivity());
                                if (syncHelper.isSdPresent()){
                                    File sdPath = Environment.getExternalStorageDirectory();
                                    File sdPathDr = new File(sdPath.toString() + "/JustReminder/" + Constants.DIR_SD_DBX_TMP);
                                    String exportFileName = uuID + Constants.FILE_NAME;
                                    File file = new File(sdPathDr, exportFileName);
                                    if (file.exists()){
                                        file.delete();
                                    }
                                }

                                deleteFromDropbox(uuID);
                                filesDataBase.deleteTask(itemId);
                                isDropboxDeleted = true;
                            }
                            break;
                        case SwipeDirections.DIRECTION_NORMAL_RIGHT:
                            if (itemId != 0) {
                                startActivity(new Intent(getActivity(),
                                        BackupFileEdit.class).putExtra(Constants.EDIT_ID, itemId));
                            }
                            break;
                        case SwipeDirections.DIRECTION_FAR_RIGHT:
                            if (itemId != 0) {
                                startActivity(new Intent(getActivity(),
                                        BackupFileEdit.class).putExtra(Constants.EDIT_ID, itemId));
                            }
                            break;
                    }
                }
            }
        });
    }

    private void deleteFromDropbox(final String name){
        new Thread(new Runnable() {
            @Override
            public void run() {
                dbx = new DropboxHelper(getActivity());
                dbx.startSession();
                if (dbx.isLinked()){
                    if (SyncHelper.isConnected(getActivity())){
                        dbx.deleteFile(name);
                    }
                }

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        filesDataBase = new FilesDataBase(getActivity());
                        fileCursorAdapter = new FileCursorAdapter(getActivity(),
                                filesDataBase.getTask(Constants.FilesConstants.FILE_TYPE_DROPBOX));
                        filesCloudList.setAdapter(fileCursorAdapter);
                    }
                });
            }
        }).start();
    }

    private void attachGoogleDrive() {
        googleContainer = (LinearLayout) rootView.findViewById(R.id.googleContainer);
        googleContainer.setVisibility(View.VISIBLE);

        googleLayout = (LinearLayout) rootView.findViewById(R.id.googleLayout);
        googleLayout.setVisibility(View.VISIBLE);

        CardView card4 = (CardView) rootView.findViewById(R.id.card4);
        CardView card5 = (CardView) rootView.findViewById(R.id.card5);
        CardView card6 = (CardView) rootView.findViewById(R.id.card6);
        card4.setCardBackgroundColor(cSetter.getCardStyle());
        card5.setCardBackgroundColor(cSetter.getCardStyle());
        card6.setCardBackgroundColor(cSetter.getCardStyle());

        googleUser = (TextView) rootView.findViewById(R.id.googleUser);
        googleUser.setTypeface(typefaceThin);

        googleSizeGraph = (PieGraph) rootView.findViewById(R.id.googleSizeGraph);

        googleText = (TextView) rootView.findViewById(R.id.googleText);
        googleText.setTypeface(typefaceThin);

        googleCount = (TextView) rootView.findViewById(R.id.googleCount);
        googleCount.setTypeface(typefaceMedium);

        googleSpace = (TextView) rootView.findViewById(R.id.googleSpace);
        googleSpace.setTypeface(typefaceThin);

        googleFreeSpace = (TextView) rootView.findViewById(R.id.googleFreeSpace);
        googleFreeSpace.setTypeface(typefaceThin);

        googleFiles = (TextView) rootView.findViewById(R.id.googleFiles);
        googleFiles.setTypeface(typefaceLight);
        googleFiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (filesGoogleList.getVisibility() != View.VISIBLE) {
                    filesGoogleList.setVisibility(View.VISIBLE);
                    googleContainer.setVisibility(View.GONE);
                } else {
                    filesGoogleList.setVisibility(View.GONE);
                    if (isGoogleDeleted) {
                        pd = ProgressDialog.show(getActivity(), null, getString(R.string.receiving_data_text), false);
                        loadGoogleInfo(pd);
                    } else {
                        googleContainer.setVisibility(View.VISIBLE);
                        isGoogleDeleted = false;
                    }
                }
            }
        });

        googleDeleteAllCloudButton = (Button) rootView.findViewById(R.id.googleDeleteAllCloudButton);
        googleDeleteAllCloudButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                filesDataBase = new FilesDataBase(getActivity());
                filesDataBase.open();
                Cursor c = filesDataBase.getTask(Constants.FilesConstants.FILE_TYPE_GDRIVE);
                if (c != null && c.moveToFirst()) {
                    do {
                        String uuID;
                        long id = c.getLong(c.getColumnIndex(Constants.COLUMN_ID));
                        uuID = c.getString(c.getColumnIndex(Constants.COLUMN_TECH_VAR));
                        syncHelper = new SyncHelper(getActivity());
                        if (syncHelper.isSdPresent()) {
                            File sdPath = Environment.getExternalStorageDirectory();
                            File sdPathDr = new File(sdPath.toString() + "/JustReminder/" + Constants.DIR_SD_GDRIVE_TMP);
                            String exportFileName = uuID + Constants.FILE_NAME;
                            File file = new File(sdPathDr, exportFileName);
                            if (file.exists()) {
                                file.delete();
                            }
                        }
                        deleteFromGoogle(uuID);
                        filesDataBase.deleteTask(id);
                    }
                    while (c.moveToNext());
                }
                if (c != null) c.close();
                if (googleContainer.getVisibility() == View.GONE) {
                    isGoogleDeleted = true;
                }
            }
        });
        googleDeleteAllCloudButton.setTypeface(typefaceLight);

        filesGoogleList = (ListView) rootView.findViewById(R.id.filesGoogleList);
        filesGoogleList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startActivity(new Intent(getActivity(), BackupFileEdit.class).putExtra(Constants.EDIT_ID, id));
            }
        });
        pd = ProgressDialog.show(getActivity(), null, getString(R.string.receiving_data_text), false);
        googleContainer.setVisibility(View.GONE);
        loadGoogleInfo(pd);
        filesDataBase = new FilesDataBase(getActivity());
        filesDataBase.open();
        fileCursorAdapter = new FileCursorAdapter(getActivity(), filesDataBase.getTask(Constants.FilesConstants.FILE_TYPE_GDRIVE));
        setSwipeDismissAdapter(filesGoogleList, fileCursorAdapter, new SwipeActionAdapter.SwipeActionListener() {
            @Override
            public boolean hasActions(int position) {
                return true;
            }

            @Override
            public boolean shouldDismiss(int position, int direction) {
                return direction == SwipeDirections.DIRECTION_NORMAL_LEFT;
            }

            @Override
            public void onSwipe(int[] positionList, int[] directionList) {
                for (int ii = 0; ii < positionList.length; ii++) {
                    int direction = directionList[ii];
                    int position = positionList[ii];
                    filesDataBase = new FilesDataBase(getActivity());
                    filesDataBase.open();
                    fileCursorAdapter = new FileCursorAdapter(getActivity(),
                            filesDataBase.getTask(Constants.FilesConstants.FILE_TYPE_GDRIVE));
                    final long itemId = fileCursorAdapter.getItemId(position);

                    switch (direction) {
                        case SwipeDirections.DIRECTION_NORMAL_LEFT:
                            if (itemId != 0) {
                                String uuID = "";
                                Cursor c = filesDataBase.getTask(itemId);
                                if (c != null && c.moveToFirst()){
                                    uuID = c.getString(c.getColumnIndex(Constants.COLUMN_TECH_VAR));
                                }
                                if (c != null) c.close();
                                syncHelper = new SyncHelper(getActivity());
                                if (syncHelper.isSdPresent()){
                                    File sdPath = Environment.getExternalStorageDirectory();
                                    File sdPathDr = new File(sdPath.toString() + "/JustReminder/" + Constants.DIR_SD_GDRIVE_TMP);
                                    String exportFileName = uuID + Constants.FILE_NAME;
                                    File file = new File(sdPathDr, exportFileName);
                                    if (file.exists()){
                                        file.delete();
                                    }
                                }

                                deleteFromGoogle(uuID);
                                filesDataBase.deleteTask(itemId);
                                isGoogleDeleted = true;
                            }
                            break;
                        case SwipeDirections.DIRECTION_FAR_LEFT:
                            if (itemId != 0) {
                                String uuID = "";
                                Cursor c = filesDataBase.getTask(itemId);
                                if (c != null && c.moveToFirst()){
                                    uuID = c.getString(c.getColumnIndex(Constants.COLUMN_TECH_VAR));
                                }
                                if (c != null) c.close();
                                syncHelper = new SyncHelper(getActivity());
                                if (syncHelper.isSdPresent()){
                                    File sdPath = Environment.getExternalStorageDirectory();
                                    File sdPathDr = new File(sdPath.toString() + "/JustReminder/" + Constants.DIR_SD_GDRIVE_TMP);
                                    String exportFileName = uuID + Constants.FILE_NAME;
                                    File file = new File(sdPathDr, exportFileName);
                                    if (file.exists()){
                                        file.delete();
                                    }
                                }

                                deleteFromGoogle(uuID);
                                filesDataBase.deleteTask(itemId);
                                isGoogleDeleted = true;
                            }
                            break;
                        case SwipeDirections.DIRECTION_NORMAL_RIGHT:
                            if (itemId != 0) {
                                startActivity(new Intent(getActivity(),
                                        BackupFileEdit.class).putExtra(Constants.EDIT_ID, itemId));
                            }
                            break;
                        case SwipeDirections.DIRECTION_FAR_RIGHT:
                            if (itemId != 0) {
                                startActivity(new Intent(getActivity(),
                                        BackupFileEdit.class).putExtra(Constants.EDIT_ID, itemId));
                            }
                            break;
                    }
                }
            }
        });
    }

    private void deleteFromGoogle(final String name){
        new Thread(new Runnable() {
            @Override
            public void run() {
                gdx = new GDriveHelper(getActivity());
                if (gdx.isLinked()){
                    if (SyncHelper.isConnected(getActivity())){
                        gdx.deleteFile(name + Constants.FILE_NAME);
                    }
                }

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        filesDataBase = new FilesDataBase(getActivity());
                        fileCursorAdapter = new FileCursorAdapter(getActivity(),
                                filesDataBase.getTask(Constants.FilesConstants.FILE_TYPE_GDRIVE));
                        filesGoogleList.setAdapter(fileCursorAdapter);
                    }
                });
            }
        }).start();
    }

    private void loadGoogleInfo(final ProgressDialog progressDialog){
        new Thread(new Runnable() {
            @Override
            public void run() {
                gdx = new GDriveHelper(getActivity());
                try {
                    gdx.loadFileFromDrive();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                final int count = gdx.countFiles();
                SyncHelper sHelp = new SyncHelper(getActivity());
                try {
                    sHelp.scanFoldersForJSON();
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if (progressDialog != null && progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }

                        googleContainer.setVisibility(View.VISIBLE);
                        googleCount.setText(String.valueOf(count));
                    }
                });
            }
        }).start();
    }

    private void loadInfo(final ProgressDialog progressDialog){
        new Thread(new Runnable() {
            @Override
            public void run() {
                dbx = new DropboxHelper(getActivity());
                dbx.downloadFromCloud();

                String name = null;
                try {
                    name = new AccountInfo(getActivity()).execute().get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }

                final String finalName = name;

                Long[] res = new Long[0];
                try {
                    res = new DropboxQuota(getActivity()).execute().get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }

                long quota = res[0], quotaNormal = res[1], quotaShared = res[2];

                final long usedQ = quotaNormal + quotaShared;
                final long availQ = quota - (quotaNormal + quotaShared);

                final float free = (int)((availQ * 100.0f) / quota);
                final float used = (int)((usedQ * 100.0f) / quota);

                final int count = new DropboxHelper(getActivity()).countFiles();
                SyncHelper sHelp = new SyncHelper(getActivity());
                try {
                    sHelp.scanFoldersForJSON();
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if (progressDialog != null && progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                        cloudContainer.setVisibility(View.VISIBLE);

                        cloudUser.setText(finalName);
                        usedSizeGraph.removeSlices();
                        PieSlice slice = new PieSlice();
                        slice.setTitle(getString(R.string.used_text) + " " + used);
                        slice.setColor(getResources().getColor(R.color.colorRed));
                        slice.setValue(used);
                        usedSizeGraph.addSlice(slice);
                        slice = new PieSlice();
                        slice.setTitle(getString(R.string.available_text) + " " + free);
                        slice.setColor(getResources().getColor(R.color.colorGreen));
                        slice.setValue(free);
                        usedSizeGraph.addSlice(slice);

                        usedSpace.setText(getString(R.string.used_text) + " " + humanReadableByteCount(usedQ, false));
                        freeSpace.setText(getString(R.string.available_text) + " " + humanReadableByteCount(availQ, false));

                        cloudCount.setText(String.valueOf(count));

                        usedSizeGraph.animate();
                    }
                });
            }
        }).start();
    }

    private void attachLocal() {
        container = (LinearLayout) rootView.findViewById(R.id.container);
        container.setVisibility(View.VISIBLE);

        localLayout = (LinearLayout) rootView.findViewById(R.id.localLayout);
        localLayout.setVisibility(View.VISIBLE);

        CardView card7 = (CardView) rootView.findViewById(R.id.card7);
        card7.setCardBackgroundColor(cSetter.getCardStyle());

        localCount = (TextView) rootView.findViewById(R.id.localCount);
        localCount.setTypeface(typefaceMedium);

        backupText = (TextView) rootView.findViewById(R.id.backupText);
        backupText.setTypeface(typefaceThin);

        backupFilesText = (TextView) rootView.findViewById(R.id.backupFilesText);
        backupFilesText.setTypeface(typefaceLight);
        backupFilesText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (filesList.getVisibility() != View.VISIBLE) {
                    filesList.setVisibility(View.VISIBLE);
                    container.setVisibility(View.GONE);
                } else {
                    filesList.setVisibility(View.GONE);
                    container.setVisibility(View.VISIBLE);
                    syncHelper = new SyncHelper(getActivity());
                    if (syncHelper.isSdPresent()) {
                        File sdPath = Environment.getExternalStorageDirectory();
                        File sdPathDr = new File(sdPath.toString() + "/JustReminder/" + Constants.DIR_SD);
                        File[] files = sdPathDr.listFiles();
                        localCount.setText(String.valueOf(files.length));
                    } else {
                        localCount.setText("0");
                    }
                }
            }
        });

        deleteAllButton = (Button) rootView.findViewById(R.id.deleteAllButton);
        deleteAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                filesDataBase = new FilesDataBase(getActivity());
                filesDataBase.open();
                syncHelper = new SyncHelper(getActivity());
                Cursor c = filesDataBase.getTask(Constants.FilesConstants.FILE_TYPE_LOCAL);
                if (c != null && c.moveToFirst()) {
                    do {
                        String uuID;
                        long id = c.getLong(c.getColumnIndex(Constants.COLUMN_ID));
                        uuID = c.getString(c.getColumnIndex(Constants.COLUMN_TECH_VAR));
                        if (syncHelper.isSdPresent()) {
                            File sdPath = Environment.getExternalStorageDirectory();
                            File sdPathDr = new File(sdPath.toString() + "/JustReminder/" + Constants.DIR_SD);
                            String exportFileName = uuID + Constants.FILE_NAME;
                            File file = new File(sdPathDr, exportFileName);
                            if (file.exists()) {
                                file.delete();
                            }
                        }
                        filesDataBase.deleteTask(id);
                    }
                    while (c.moveToNext());
                }
                if (c != null) c.close();

                if (container.getVisibility() == View.GONE) {
                    filesList.setVisibility(View.GONE);
                    container.setVisibility(View.VISIBLE);
                    if (syncHelper.isSdPresent()) {
                        File sdPath = Environment.getExternalStorageDirectory();
                        File sdPathDr = new File(sdPath.toString() + "/JustReminder/" + Constants.DIR_SD);
                        File[] files = sdPathDr.listFiles();
                        localCount.setText(String.valueOf(files.length));
                    } else {
                        localCount.setText("0");
                    }
                }

            }
        });
        deleteAllButton.setTypeface(typefaceLight);

        syncHelper = new SyncHelper(getActivity());
        if (syncHelper.isSdPresent()) {
            File sdPath = Environment.getExternalStorageDirectory();
            File sdPathDr = new File(sdPath.toString() + "/JustReminder/" + Constants.DIR_SD);
            File[] files = sdPathDr.listFiles();
            if (files != null) {
                localCount.setText(String.valueOf(files.length));
            } else localCount.setText("0");
        } else {
            localCount.setText("0");
        }

        filesList = (ListView) rootView.findViewById(R.id.filesList);
        filesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startActivity(new Intent(getActivity(), BackupFileEdit.class).putExtra(Constants.EDIT_ID, id));
            }
        });
        filesDataBase = new FilesDataBase(getActivity());
        filesDataBase.open();
        fileCursorAdapter = new FileCursorAdapter(getActivity(),
                filesDataBase.getTask(Constants.FilesConstants.FILE_TYPE_LOCAL));
        setSwipeDismissAdapter(filesList, fileCursorAdapter, new SwipeActionAdapter.SwipeActionListener() {
            @Override
            public boolean hasActions(int position) {
                return true;
            }

            @Override
            public boolean shouldDismiss(int position, int direction) {
                return direction == SwipeDirections.DIRECTION_NORMAL_LEFT;
            }

            @Override
            public void onSwipe(int[] positionList, int[] directionList) {
                for (int ii = 0; ii < positionList.length; ii++) {
                    int direction = directionList[ii];
                    int position = positionList[ii];
                    filesDataBase = new FilesDataBase(getActivity());
                    filesDataBase.open();
                    fileCursorAdapter = new FileCursorAdapter(getActivity(),
                            filesDataBase.getTask(Constants.FilesConstants.FILE_TYPE_LOCAL));
                    final long itemId = fileCursorAdapter.getItemId(position);

                    switch (direction) {
                        case SwipeDirections.DIRECTION_NORMAL_LEFT:
                            if (itemId != 0) {
                                String uuID = "";
                                Cursor c = filesDataBase.getTask(itemId);
                                if (c != null && c.moveToFirst()){
                                    uuID = c.getString(c.getColumnIndex(Constants.COLUMN_TECH_VAR));
                                }
                                if (c != null) c.close();
                                syncHelper = new SyncHelper(getActivity());
                                if (syncHelper.isSdPresent()){
                                    File sdPath = Environment.getExternalStorageDirectory();
                                    File sdPathDr = new File(sdPath.toString() + "/JustReminder/" + Constants.DIR_SD);
                                    String exportFileName = uuID + Constants.FILE_NAME;
                                    File file = new File(sdPathDr, exportFileName);
                                    if (file.exists()){
                                        file.delete();
                                    }
                                }
                                filesDataBase.deleteTask(itemId);
                                fileCursorAdapter = new FileCursorAdapter(getActivity(),
                                        filesDataBase.getTask(Constants.FilesConstants.FILE_TYPE_LOCAL));
                                filesList.setAdapter(fileCursorAdapter);
                                isLocalDeleted = true;
                            }
                            break;
                        case SwipeDirections.DIRECTION_FAR_LEFT:
                            if (itemId != 0) {
                                String uuID = "";
                                Cursor c = filesDataBase.getTask(itemId);
                                if (c != null && c.moveToFirst()){
                                    uuID = c.getString(c.getColumnIndex(Constants.COLUMN_TECH_VAR));
                                }
                                if (c != null) c.close();
                                syncHelper = new SyncHelper(getActivity());
                                if (syncHelper.isSdPresent()){
                                    File sdPath = Environment.getExternalStorageDirectory();
                                    File sdPathDr = new File(sdPath.toString() + "/JustReminder/" + Constants.DIR_SD);
                                    String exportFileName = uuID + Constants.FILE_NAME;
                                    File file = new File(sdPathDr, exportFileName);
                                    if (file.exists()){
                                        file.delete();
                                    }
                                }
                                filesDataBase.deleteTask(itemId);
                                fileCursorAdapter = new FileCursorAdapter(getActivity(),
                                        filesDataBase.getTask(Constants.FilesConstants.FILE_TYPE_LOCAL));
                                filesList.setAdapter(fileCursorAdapter);
                                isLocalDeleted = true;
                            }
                            break;
                        case SwipeDirections.DIRECTION_NORMAL_RIGHT:
                            if (itemId != 0) {
                                startActivity(new Intent(getActivity(),
                                        BackupFileEdit.class).putExtra(Constants.EDIT_ID, itemId));
                            }
                            break;
                        case SwipeDirections.DIRECTION_FAR_RIGHT:
                            if (itemId != 0) {
                                startActivity(new Intent(getActivity(),
                                        BackupFileEdit.class).putExtra(Constants.EDIT_ID, itemId));
                            }
                            break;
                    }
                }
            }
        });
    }

    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    private void clearForm(View rootView) {
        localLayout = (LinearLayout) rootView.findViewById(R.id.localLayout);
        localLayout.setVisibility(View.GONE);
        cloudLayout = (LinearLayout) rootView.findViewById(R.id.cloudLayout);
        cloudLayout.setVisibility(View.GONE);
    }

    private boolean isLocal(){
        localLayout = (LinearLayout) rootView.findViewById(R.id.localLayout);
        return localLayout.getVisibility() == View.VISIBLE;
    }

    private boolean isDropbox(){
        cloudLayout = (LinearLayout) rootView.findViewById(R.id.cloudLayout);
        return cloudLayout.getVisibility() == View.VISIBLE;
    }

    private boolean isGoogleDrive(){
        googleLayout = (LinearLayout) rootView.findViewById(R.id.googleLayout);
        return googleLayout.getVisibility() == View.VISIBLE;
    }

    private void detachLocal(){
        localLayout = (LinearLayout) rootView.findViewById(R.id.localLayout);
        localLayout.setVisibility(View.GONE);
    }

    private void detachDropbox(){
        cloudLayout = (LinearLayout) rootView.findViewById(R.id.cloudLayout);
        cloudLayout.setVisibility(View.GONE);
        usedSpace.setText("");
        freeSpace.setText("");

        cloudCount.setText("");
    }

    private void detachGoogleDrive(){
        googleLayout = (LinearLayout) rootView.findViewById(R.id.googleLayout);
        googleLayout.setVisibility(View.GONE);
        googleSpace.setText("");
        googleFreeSpace.setText("");

        googleCount.setText("");
    }

    private void detachLayout(){
        if (isLocal()) detachLocal();
        if (isDropbox()) detachDropbox();
        if (isGoogleDrive()) detachGoogleDrive();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        int items = navIds.size();
        if (items == 1){
            detachLayout();
            attachLocal();
            toolbar.setLogo(R.drawable.ic_devices_white_24dp);
        } else if (items == 2){
            switch (position){
                case 0:
                    detachLayout();
                    attachLocal();
                    toolbar.setLogo(R.drawable.ic_devices_white_24dp);
                    break;
                case 1:
                    if (navIds.get(position) == Constants.DROPBOX_INT){
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                final boolean isC = SyncHelper.isConnected(getActivity());
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (isC) {
                                            detachLayout();
                                            attachDropbox();
                                            toolbar.setLogo(R.drawable.dropbox_icon_white);
                                        } else {
                                            spinner.setSelection(0);
                                        }
                                    }
                                });
                            }
                        }).start();
                    } else if (navIds.get(position) == Constants.GOOGLE_DRIVE_INT){
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                final boolean isC = SyncHelper.isConnected(getActivity());
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (isC) {
                                            detachLayout();
                                            attachGoogleDrive();
                                            toolbar.setLogo(R.drawable.gdrive_icon_white);
                                        } else {
                                            spinner.setSelection(0);
                                        }
                                    }
                                });
                            }
                        }).start();
                    }
            }
        } else if (items == 3){
            switch (position){
                case 0:
                    detachLayout();
                    attachLocal();
                    toolbar.setLogo(R.drawable.ic_devices_white_24dp);
                    break;
                case 1:
                    if (navIds.get(position) == Constants.DROPBOX_INT){
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                final boolean isC = SyncHelper.isConnected(getActivity());
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (isC) {
                                            detachLayout();
                                            attachDropbox();
                                            toolbar.setLogo(R.drawable.dropbox_icon_white);
                                        } else {
                                            spinner.setSelection(0);
                                        }
                                    }
                                });
                            }
                        }).start();
                    }
                case 2:
                    if (navIds.get(position) == Constants.GOOGLE_DRIVE_INT){
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                final boolean isC = SyncHelper.isConnected(getActivity());
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (isC) {
                                            detachLayout();
                                            attachGoogleDrive();
                                            toolbar.setLogo(R.drawable.gdrive_icon_white);
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
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
