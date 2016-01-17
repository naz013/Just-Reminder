package com.cray.software.justreminder.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.cray.software.justreminder.CategoryManager;
import com.cray.software.justreminder.R;
import com.cray.software.justreminder.ScreenManager;
import com.cray.software.justreminder.adapters.CategoryRecyclerAdapter;
import com.cray.software.justreminder.cloud.DropboxHelper;
import com.cray.software.justreminder.cloud.GDriveHelper;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.FileConfig;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.databases.NextBase;
import com.cray.software.justreminder.datas.CategoryDataProvider;
import com.cray.software.justreminder.datas.models.CategoryModel;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.interfaces.NavigationCallbacks;
import com.cray.software.justreminder.interfaces.SimpleListener;
import com.cray.software.justreminder.modules.Module;

import java.io.File;

public class GroupsFragment extends Fragment implements SimpleListener {

    private RecyclerView listView;

    private CategoryDataProvider provider;

    private NavigationCallbacks mCallbacks;

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
        View rootView = inflater.inflate(R.layout.fragment_screen_manager, container, false);

        LinearLayout emptyItem = (LinearLayout) rootView.findViewById(R.id.emptyItem);
        emptyItem.setVisibility(View.GONE);

        listView = (RecyclerView) rootView.findViewById(R.id.currentList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        listView.setLayoutManager(mLayoutManager);

        loadCategories();
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
        if (new SharedPrefs(getActivity()).loadBoolean(Prefs.GROUP_CHANGED)) {
            loadCategories();
        }
    }

    private void loadCategories(){
        new SharedPrefs(getActivity()).saveBoolean(Prefs.GROUP_CHANGED, false);
        provider = new CategoryDataProvider(getActivity());
        CategoryRecyclerAdapter adapter = new CategoryRecyclerAdapter(getActivity(), provider);
        adapter.setEventListener(this);
        listView.setAdapter(adapter);  // requires *wrapped* adapter
        listView.setItemAnimator(new DefaultItemAnimator());
        if (mCallbacks != null) {
            mCallbacks.onListChanged(listView);
        }
    }

    private void removeGroup(int position){
        long itemId = provider.getItem(position).getId();
        if (itemId != 0) {
            DataBase db = new DataBase(getActivity());
            db.open();
            Cursor s = db.getCategory(itemId);
            if (s != null && s.moveToFirst()) {
                String uuId = s.getString(s.getColumnIndex(Constants.COLUMN_TECH_VAR));
                db.deleteCategory(itemId);
                new DeleteAsync(getActivity(), uuId).execute();
            }
            if (s != null) s.close();
            db.close();
            if (mCallbacks != null) {
                mCallbacks.showSnackbar(R.string.deleted);
            }
            loadCategories();
        }
    }

    @Override
    public void onItemClicked(int position, View view) {
        startActivity(new Intent(getActivity(), CategoryManager.class)
                .putExtra(Constants.ITEM_ID_INTENT, provider.getItem(position).getId()));
    }

    @Override
    public void onItemLongClicked(final int position, View view) {
        final CharSequence[] items = {getString(R.string.change_color), getString(R.string.edit), getString(R.string.delete)};
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                dialog.dismiss();
                switch (item){
                    case 0:
                        changeColor(provider.getItem(position).getId());
                        break;
                    case 1:
                        startActivity(new Intent(getActivity(), CategoryManager.class)
                                .putExtra(Constants.ITEM_ID_INTENT, provider.getItem(position).getId()));
                        break;
                    case 2:
                        removeGroup(position);
                        break;
                }
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void changeColor(final long id) {
        CharSequence[] items = {getString(R.string.red), getString(R.string.purple),
                getString(R.string.green), getString(R.string.green_light),
                getString(R.string.blue), getString(R.string.blue_light),
                getString(R.string.yellow), getString(R.string.orange),
                getString(R.string.cyan), getString(R.string.pink),
                getString(R.string.teal), getString(R.string.amber)};
        if (Module.isPro()){
            items = new CharSequence[]{getString(R.string.red), getString(R.string.purple),
                    getString(R.string.green), getString(R.string.green_light),
                    getString(R.string.blue), getString(R.string.blue_light),
                    getString(R.string.yellow), getString(R.string.orange),
                    getString(R.string.cyan), getString(R.string.pink),
                    getString(R.string.teal), getString(R.string.amber),
                    getString(R.string.dark_purple), getString(R.string.dark_orange),
                    getString(R.string.lime), getString(R.string.indigo)};
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                dialog.dismiss();
                CategoryModel.setNewIndicator(getActivity(), id, item);
                loadCategories();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public class DeleteAsync extends AsyncTask<Void, Void, Void> {

        private Context mContext;
        private String uuId;

        public DeleteAsync(Context context, String uuID){
            this.mContext = context;
            this.uuId = uuID;
        }

        @Override
        protected Void doInBackground(Void... params) {
            if (SyncHelper.isSdPresent()) {
                File sdPath = Environment.getExternalStorageDirectory();
                File sdPathDr = new File(sdPath.toString() + "/JustReminder/" + Constants.DIR_GROUP_SD);
                String exportFileName = uuId + FileConfig.FILE_NAME_GROUP;
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
            if (dbx.isLinked() && isInternet) {
                dbx.deleteGroup(uuId);
            }
            if (gdx.isLinked() && isInternet) {
                gdx.deleteGroup(uuId);
            }
            NextBase db = new NextBase(mContext);
            db.open();
            Cursor c = db.queryGroup(uuId);
            if (c != null && c.moveToFirst()){
                do {
                    String remUUId = c.getString(c.getColumnIndex(NextBase.UUID));
                    db.deleteReminder(c.getLong(c.getColumnIndex(NextBase._ID)));
                    if (SyncHelper.isSdPresent()) {
                        File sdPath = Environment.getExternalStorageDirectory();
                        File sdPathDr = new File(sdPath.toString() + "/JustReminder/" + Constants.DIR_SD);
                        String exportFileName = remUUId + FileConfig.FILE_NAME_REMINDER;
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
                    if (dbx.isLinked() && isInternet) {
                        dbx.deleteReminder(remUUId);
                    }
                    if (gdx.isLinked() && isInternet) {
                        gdx.deleteReminder(uuId);
                    }
                } while (c.moveToNext());
            }
            db.close();
            return null;
        }
    }
}
