package com.cray.software.justreminder.dialogs;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.interfaces.QuickReturnListViewOnScrollListener;
import com.cray.software.justreminder.adapters.SimpleAdapter;
import com.cray.software.justreminder.cloud.DropboxHelper;
import com.cray.software.justreminder.cloud.GDriveHelper;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.utils.QuickReturnUtils;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.interfaces.QuickReturnViewType;
import com.getbase.floatingactionbutton.AddFloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.wdullaer.swipeactionadapter.SwipeActionAdapter;
import com.wdullaer.swipeactionadapter.SwipeDirections;

import java.io.File;

public class CategoriesList extends AppCompatActivity {

    ListView listView;
    ColorSetter cs = new ColorSetter(CategoriesList.this);
    DataBase db;
    SharedPrefs sPrefs = new SharedPrefs(CategoriesList.this);
    Toolbar toolbar;
    AddFloatingActionButton mFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(cs.getStyle());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(cs.colorStatus());
        }
        setContentView(R.layout.places_activity_layout);
        setRequestedOrientation(cs.getRequestOrientation());

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationIcon(R.drawable.ic_clear_white_24dp);
        toolbar.setTitle(getString(R.string.string_manage_categories));

        findViewById(R.id.windowBackground).setBackgroundColor(cs.getBackgroundStyle());

        listView = (ListView) findViewById(R.id.listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                startActivity(new Intent(CategoriesList.this, CategoryManager.class)
                        .putExtra(Constants.ITEM_ID_INTENT, id));
            }
        });

        mFab = new AddFloatingActionButton(CategoriesList.this);
        mFab.setSize(FloatingActionButton.SIZE_NORMAL);
        mFab.setColorNormal(cs.colorSetter());
        mFab.setColorPressed(cs.colorChooser());

        RelativeLayout wrapper = (RelativeLayout) findViewById(R.id.wrapper);
        wrapper.addView(mFab);

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mFab.getLayoutParams();
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CategoriesList.this, CategoryManager.class));
            }
        });
    }

    private void loadTemplates(){
        db = new DataBase(CategoriesList.this);
        db.open();
        SimpleAdapter adapter = new SimpleAdapter(CategoriesList.this, db.queryCategories());
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
                db = new DataBase(CategoriesList.this);
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
                    db = new DataBase(CategoriesList.this);
                    if (!db.isOpen()) db.open();
                    SimpleAdapter adapter = new SimpleAdapter(CategoriesList.this, db.queryCategories());
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
                                startActivity(new Intent(CategoriesList.this, CategoryManager.class)
                                        .putExtra(Constants.ITEM_ID_INTENT, itemId));
                            }
                            break;
                        case SwipeDirections.DIRECTION_FAR_RIGHT:
                            if (itemId != 0) {
                                startActivity(new Intent(CategoriesList.this, CategoryManager.class)
                                        .putExtra(Constants.ITEM_ID_INTENT, itemId));
                            }
                            break;
                    }
                }
            }
        });
        listView.setAdapter(mAdapter);
        QuickReturnListViewOnScrollListener scrollListener = new
                QuickReturnListViewOnScrollListener.Builder(QuickReturnViewType.FOOTER)
                .footer(mFab)
                .minFooterTranslation(QuickReturnUtils.dp2px(this, 88))
                .isSnappable(true)
                .build();
        listView.setOnScrollListener(scrollListener);
        db.close();
    }

    private void removeGroup(long itemId){
        if (itemId != 0) {
            Cursor s = db.getCategory(itemId);
            if (s != null && s.moveToFirst()){
                String uuId = s.getString(s.getColumnIndex(Constants.COLUMN_TECH_VAR));
                db.deleteCategory(itemId);
                new DeleteAsync(CategoriesList.this, uuId).execute();
            }
            loadTemplates();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTemplates();
    }

    public class DeleteAsync extends AsyncTask<Void, Void, Void>{

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
            boolean isInternet = SyncHelper.isConnected(CategoriesList.this);
            DropboxHelper dbx = new DropboxHelper(CategoriesList.this);
            GDriveHelper gdx = new GDriveHelper(CategoriesList.this);
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
