package com.cray.software.justreminder;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.cray.software.justreminder.adapters.NotesRecyclerAdapter;
import com.cray.software.justreminder.adapters.QuickReturnRecyclerViewOnScrollListener;
import com.cray.software.justreminder.async.SyncNotes;
import com.cray.software.justreminder.databases.NotesBase;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.Notifier;
import com.cray.software.justreminder.helpers.QuickReturnUtils;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.datas.NoteItem;
import com.cray.software.justreminder.interfaces.QuickReturnViewType;
import com.cray.software.justreminder.interfaces.SyncListener;
import com.cray.software.justreminder.modules.ManageModule;
import com.getbase.floatingactionbutton.AddFloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;

public class NotesActivity extends AppCompatActivity implements SyncListener {

    ColorSetter cSetter;
    RecyclerView currentList;
    ImageView emptyImage;
    NotesBase db = new NotesBase(NotesActivity.this);
    SharedPrefs sPrefs;
    Toolbar toolbar;
    private AdView adView;
    LinearLayout emptyLayout;
    AddFloatingActionButton mFab;
    ArrayList<NoteItem> data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
        }
        super.onCreate(savedInstanceState);
        cSetter = new ColorSetter(NotesActivity.this);
        setTheme(cSetter.getStyle());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(cSetter.colorStatus());
        }
        setContentView(R.layout.notes_layout);
        setRequestedOrientation(cSetter.getRequestOrientation());

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setTitle(getString(R.string.fragment_notes));

        sPrefs = new SharedPrefs(NotesActivity.this);

        findViewById(R.id.windowBackground).setBackgroundColor(cSetter.getBackgroundStyle());

        emptyImage = (ImageView) findViewById(R.id.emptyImage);
        if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_USE_DARK_THEME)){
            emptyImage.setImageResource(R.drawable.note_icon_white);
        } else {
            emptyImage.setImageResource(R.drawable.note_icon);
        }

        currentList = (RecyclerView) findViewById(R.id.currentList);
        currentList.setLayoutManager(new LinearLayoutManager(this));

        mFab = new AddFloatingActionButton(NotesActivity.this);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(NotesActivity.this, NotesManager.class));
            }
        });
        mFab.setColorNormal(cSetter.colorSetter());
        mFab.setColorPressed(cSetter.colorChooser());
        mFab.setSize(FloatingActionButton.SIZE_NORMAL);

        RelativeLayout wrapper = (RelativeLayout) findViewById(R.id.wrapper);
        wrapper.addView(mFab);

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mFab.getLayoutParams();
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        //params.addRule(RelativeLayout.ABOVE, R.id.emptyLayout);

        if (!new ManageModule().isPro()){
            emptyLayout = (LinearLayout) findViewById(R.id.emptyLayout);
            emptyLayout.setVisibility(View.GONE);

            adView = (AdView) findViewById(R.id.adView);
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
        }

        loaderAdapter();
    }

    private void showDialog(){
        final CharSequence[] items = {getString(R.string.sort_item_by_date_a_z),
                getString(R.string.sort_item_by_date_z_a),
                getString(R.string.sort_name_a_z),
                getString(R.string.sort_name_z_a)};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.menu_order_by));
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                SharedPrefs prefs = new SharedPrefs(NotesActivity.this);
                if (item == 0) {
                    prefs.savePrefs(Constants.APP_UI_PREFERENCES_NOTES_ORDER, Constants.ORDER_DATE_A_Z);
                } else if (item == 1) {
                    prefs.savePrefs(Constants.APP_UI_PREFERENCES_NOTES_ORDER, Constants.ORDER_DATE_Z_A);
                } else if (item == 2) {
                    prefs.savePrefs(Constants.APP_UI_PREFERENCES_NOTES_ORDER, Constants.ORDER_NAME_A_Z);
                } else if (item == 3) {
                    prefs.savePrefs(Constants.APP_UI_PREFERENCES_NOTES_ORDER, Constants.ORDER_NAME_Z_A);
                }
                dialog.dismiss();
                loaderAdapter();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void loaderAdapter(){
        if (!db.isOpen()) db.open();
        data = new ArrayList<>();
        Cursor c = db.getNotes();
        if (c != null && c.moveToFirst()){
            do {
                String note = c.getString(c.getColumnIndex(Constants.COLUMN_NOTE));
                int color = c.getInt(c.getColumnIndex(Constants.COLUMN_COLOR));
                int style = c.getInt(c.getColumnIndex(Constants.COLUMN_FONT_STYLE));
                byte[] image = c.getBlob(c.getColumnIndex(Constants.COLUMN_IMAGE));
                long id = c.getLong(c.getColumnIndex(Constants.COLUMN_ID));
                data.add(new NoteItem(note, color, style, image, id));
            } while (c.moveToNext());
            NotesRecyclerAdapter adapter = new NotesRecyclerAdapter(NotesActivity.this, data);
            currentList.setAdapter(adapter);
            currentList.setItemAnimator(new DefaultItemAnimator());
            QuickReturnRecyclerViewOnScrollListener scrollListener = new
                    QuickReturnRecyclerViewOnScrollListener.Builder(QuickReturnViewType.FOOTER)
                    .footer(mFab)
                    .minFooterTranslation(QuickReturnUtils.dp2px(this, 88))
                    .isSnappable(true)
                    .build();
            if (adapter.getItemCount() > 0) {
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                    currentList.addOnScrollListener(scrollListener);
                } else {
                    currentList.setOnScrollListener(scrollListener);
                }
            }
        }
        if (c != null) c.close();
    }

    @Override
    protected void onResume() {
        super.onResume();
        sPrefs = new SharedPrefs(NotesActivity.this);
        if (sPrefs.loadBoolean("isNew")) loaderAdapter();
        sPrefs.saveBoolean("isNew", false);
        invalidateOptionsMenu();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    public static final int MENU_ITEM_DELETE = 12;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.notes_menu, menu);
        if (!db.isOpen()) db.open();
        if (db.getCount() != 0) {
            menu.add(Menu.NONE, MENU_ITEM_DELETE, 100, getString(R.string.delete_all_notes));
        }
        return true;
    }

    private void deleteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
                deleteAll();
                dialog.dismiss();
                loaderAdapter();
                invalidateOptionsMenu();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_sync:
                new SyncNotes(NotesActivity.this, this).execute();
                return true;
            case R.id.action_order:
                showDialog();
                return true;
            case MENU_ITEM_DELETE:
                deleteDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void deleteAll(){
        if (!db.isOpen()) db.open();
        Cursor c = db.getNotes();
        if (c != null && c.moveToFirst()){
            do{
                long rowId = c.getLong(c.getColumnIndex(Constants.COLUMN_ID));
                db.deleteNote(rowId);
                new Notifier(NotesActivity.this).discardStatusNotification(rowId);

            }while (c.moveToNext());
        }
        if (c != null) c.close();
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (db != null && db.isOpen()) db.close();
    }

    @Override
    public void endExecution(boolean result) {
        if (result) {
            loaderAdapter();
            invalidateOptionsMenu();
        }
    }
}
