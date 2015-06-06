package com.hexrain.design.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.adapters.SimpleAdapter;
import com.cray.software.justreminder.cloud.DropboxHelper;
import com.cray.software.justreminder.cloud.GDriveHelper;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.dialogs.CategoryManager;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.interfaces.Constants;
import com.hexrain.design.NavigationDrawerFragment;
import com.hexrain.design.ScreenManager;
import com.wdullaer.swipeactionadapter.SwipeActionAdapter;
import com.wdullaer.swipeactionadapter.SwipeDirections;

import java.io.File;

public class GroupsFragment extends Fragment {

    ColorSetter cSetter;
    DataBase db;
    SharedPrefs sPrefs;
    ListView listView;

    private NavigationDrawerFragment.NavigationDrawerCallbacks mCallbacks;

    public static GroupsFragment newInstance() {
        return new GroupsFragment();
    }

    public GroupsFragment() {
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Indicate that this fragment would like to influence the set of actions in the action bar.
        setHasOptionsMenu(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_simple_list_layout, container, false);

        cSetter = new ColorSetter(getActivity());
        sPrefs = new SharedPrefs(getActivity());

        listView = (ListView) rootView.findViewById(R.id.listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                startActivity(new Intent(getActivity(), CategoryManager.class)
                        .putExtra(Constants.ITEM_ID_INTENT, id));
            }
        });

        loadTemplates();
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
        ((ScreenManager)activity).onSectionAttached(ScreenManager.FRAGMENT_GROUPS);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void loadTemplates(){
        db = new DataBase(getActivity());
        db.open();
        SimpleAdapter adapter = new SimpleAdapter(getActivity(), db.queryCategories());
        final SwipeActionAdapter mAdapter = new SwipeActionAdapter(adapter);
        mAdapter.setListView(listView);
        mAdapter.setFixedBackgrounds(true);
        mAdapter.addBackground(SwipeDirections.DIRECTION_NORMAL_LEFT, R.layout.swipe_delete_layout)
                .addBackground(SwipeDirections.DIRECTION_NORMAL_RIGHT, R.layout.swipe_edit_layout)
                .addBackground(SwipeDirections.DIRECTION_FAR_LEFT, R.layout.swipe_delete_layout)
                .addBackground(SwipeDirections.DIRECTION_FAR_RIGHT, R.layout.swipe_edit_layout);
        mAdapter.setSwipeActionListener(new SwipeActionAdapter.SwipeActionListener() {
            @Override
            public boolean hasActions(int position) {
                db = new DataBase(getActivity());
                db.open();
                Cursor c = db.queryCategories();
                return c != null && c.getCount() > 1;
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
                    db = new DataBase(getActivity());
                    if (!db.isOpen()) db.open();
                    SimpleAdapter adapter = new SimpleAdapter(getActivity(), db.queryCategories());
                    long itemId = adapter.getItemId(position);

                    switch (direction) {
                        case SwipeDirections.DIRECTION_NORMAL_LEFT:
                            removeGroup(itemId);
                            break;
                        case SwipeDirections.DIRECTION_FAR_LEFT:
                            removeGroup(itemId);
                            break;
                        case SwipeDirections.DIRECTION_NORMAL_RIGHT:
                            if (itemId != 0) {
                                startActivity(new Intent(getActivity(), CategoryManager.class)
                                        .putExtra(Constants.ITEM_ID_INTENT, itemId));
                            }
                            break;
                        case SwipeDirections.DIRECTION_FAR_RIGHT:
                            if (itemId != 0) {
                                startActivity(new Intent(getActivity(), CategoryManager.class)
                                        .putExtra(Constants.ITEM_ID_INTENT, itemId));
                            }
                            break;
                    }
                }
            }
        });
        listView.setAdapter(mAdapter);
        if (mCallbacks != null) mCallbacks.onListChange(listView);
        db.close();
    }

    private void removeGroup(long itemId){
        if (itemId != 0) {
            Cursor s = db.getCategory(itemId);
            if (s != null && s.moveToFirst()){
                String uuId = s.getString(s.getColumnIndex(Constants.COLUMN_TECH_VAR));
                db.deleteCategory(itemId);
                new DeleteAsync(getActivity(), uuId).execute();
            }
            loadTemplates();
        }
    }

    public class DeleteAsync extends AsyncTask<Void, Void, Void> {

        Context mContext;
        String uuId;

        public DeleteAsync(Context context, String uuID){
            this.mContext = context;
            this.uuId = uuID;
        }

        @Override
        protected Void doInBackground(Void... params) {
            SyncHelper syncHelper = new SyncHelper(mContext);
            if (syncHelper.isSdPresent()) {
                File sdPath = Environment.getExternalStorageDirectory();
                File sdPathDr = new File(sdPath.toString() + "/JustReminder/" + Constants.DIR_GROUP_SD);
                String exportFileName = uuId + Constants.FILE_NAME_GROUP;
                File file = new File(sdPathDr, exportFileName);
                if (file.exists()) {
                    file.delete();
                }
                sdPathDr = new File(sdPath.toString() + "/JustReminder/" + Constants.DIR_GROUP_SD_DBX_TMP);
                file = new File(sdPathDr, exportFileName);
                if (file.exists()) {
                    file.delete();
                }
                sdPathDr = new File(sdPath.toString() + "/JustReminder/" + Constants.DIR_GROUP_SD_GDRIVE_TMP);
                file = new File(sdPathDr, exportFileName);
                if (file.exists()) {
                    file.delete();
                }
            }
            boolean isInternet = SyncHelper.isConnected(getActivity());
            DropboxHelper dbx = new DropboxHelper(getActivity());
            GDriveHelper gdx = new GDriveHelper(getActivity());
            if (dbx.isLinked() && isInternet) dbx.deleteGroup(uuId);
            if (gdx.isLinked() && isInternet) gdx.deleteGroup(uuId);
            DataBase dataBase = new DataBase(mContext);
            dataBase.open();
            Cursor c = dataBase.queryGroup(uuId);
            if (c != null && c.moveToFirst()){
                do {
                    String remUUId = c.getString(c.getColumnIndex(Constants.COLUMN_TECH_VAR));
                    dataBase.deleteTask(c.getLong(c.getColumnIndex(Constants.COLUMN_ID)));
                    if (syncHelper.isSdPresent()) {
                        File sdPath = Environment.getExternalStorageDirectory();
                        File sdPathDr = new File(sdPath.toString() + "/JustReminder/" + Constants.DIR_SD);
                        String exportFileName = remUUId + Constants.FILE_NAME;
                        File file = new File(sdPathDr, exportFileName);
                        if (file.exists()) {
                            file.delete();
                        }
                        sdPathDr = new File(sdPath.toString() + "/JustReminder/" + Constants.DIR_SD_DBX_TMP);
                        file = new File(sdPathDr, exportFileName);
                        if (file.exists()) {
                            file.delete();
                        }
                        sdPathDr = new File(sdPath.toString() + "/JustReminder/" + Constants.DIR_SD_GDRIVE_TMP);
                        file = new File(sdPathDr, exportFileName);
                        if (file.exists()) {
                            file.delete();
                        }
                    }
                    if (dbx.isLinked() && isInternet) dbx.deleteFile(remUUId);
                    if (gdx.isLinked() && isInternet) gdx.deleteFile(uuId);
                } while (c.moveToNext());
            }
            return null;
        }
    }
}
