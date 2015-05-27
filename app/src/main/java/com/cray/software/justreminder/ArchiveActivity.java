package com.cray.software.justreminder;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cray.software.justreminder.adapters.RemindersRecyclerAdapter;
import com.cray.software.justreminder.async.DeleteReminder;
import com.cray.software.justreminder.cloud.GTasksHelper;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.datas.ReminderItem;
import com.cray.software.justreminder.dialogs.CategoriesList;
import com.cray.software.justreminder.dialogs.PlacesList;
import com.cray.software.justreminder.dialogs.TemplatesList;
import com.cray.software.justreminder.helpers.CalendarManager;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.Interval;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.helpers.TimeCount;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.modules.ManageModule;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.animator.SwipeDismissItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.decoration.SimpleListDividerDecorator;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager;
import com.h6ah4i.android.widget.advrecyclerview.touchguard.RecyclerViewTouchActionGuardManager;

import java.util.ArrayList;

public class ArchiveActivity extends AppCompatActivity
        implements View.OnClickListener {

    RecyclerView currentList;
    DataBase DB = new DataBase(ArchiveActivity.this);
    ImageView basket;
    RelativeLayout ads_container;
    TextView activeScreen, archiveScreen, fragmentSettings, geoScreen, calendar,
            manageBackup, notes, helpTranslate, googleTasks, moreApps, templates, places,
            categories;
    RelativeLayout drawerBg;
    LinearLayout emptyLayout;
    SharedPrefs sPrefs = new SharedPrefs(ArchiveActivity.this);

    ColorSetter cSetter = new ColorSetter(ArchiveActivity.this);
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    private AdView adView;
    Typeface typeface;
    private static final String MARKET_APP_PASSWORDS_PRO = "com.cray.software.justreminderpro";
    Toolbar toolbar;
    boolean isOpened = false;
    private boolean shouldGoInvisible;
    TextView appNameBanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cSetter = new ColorSetter(ArchiveActivity.this);
        setTheme(cSetter.getStyle());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(cSetter.colorStatus());
        }
        setContentView(R.layout.new_activity_main);
        setRequestedOrientation(cSetter.getRequestOrientation());

        getIntent().setAction("JustArchiveActivity Created");

        toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);

        toolbar.setOnMenuItemClickListener(
                new Toolbar.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.action_delete_all:
                                new Handler().post(new Runnable() {
                                    @Override
                                    public void run() {
                                        mDrawerLayout.closeDrawers();
                                    }
                                });
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        deleteAll();
                                        loaderAdapter();
                                    }
                                }, 200);
                                return true;
                        }
                        return true;
                    }
                });

        toolbar.inflateMenu(R.menu.main);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setTitle(getString(R.string.drawer_archive_reminder));

        drawerBg = (RelativeLayout) findViewById(R.id.drawerBg);
        drawerBg.setBackgroundColor(cSetter.getBackgroundStyle());
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setBackgroundColor(cSetter.getBackgroundStyle());
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                toolbar, R.string.app_name_pro, R.string.drawer_archive_reminder) {

            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                //currentList.setEnabled(true);
                shouldGoInvisible = false;
                invalidateOptionsMenu();
                isOpened = false;
            }
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                //currentList.setEnabled(false);
                shouldGoInvisible = true;
                invalidateOptionsMenu();
                isOpened = true;
            }

            float mPreviousOffset = 0f;

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
                if(slideOffset > mPreviousOffset && !shouldGoInvisible){
                    shouldGoInvisible = true;
                    invalidateOptionsMenu();
                }else if(mPreviousOffset > slideOffset && slideOffset < 0.5f && shouldGoInvisible){
                    shouldGoInvisible = false;
                    invalidateOptionsMenu();
                }
                mPreviousOffset = slideOffset;
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        currentList = (RecyclerView) findViewById(R.id.currentList);
        emptyLayout = (LinearLayout) findViewById(R.id.emptyLayout);
        emptyLayout.setVisibility(View.GONE);
        currentList.setVisibility(View.VISIBLE);
        loaderAdapter();

        typeface = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf");

        appNameBanner = (TextView) findViewById(R.id.appNameBanner);
        appNameBanner.setTypeface(typeface);
        if (new ManageModule().isPro()) {
            appNameBanner.setText(R.string.app_name_pro);
        } else {
            appNameBanner.setText(R.string.app_name);
        }
        activeScreen = (TextView) findViewById(R.id.activeScreen);
        activeScreen.setOnClickListener(this);

        fragmentSettings = (TextView) findViewById(R.id.fragmentSettings);
        fragmentSettings.setOnClickListener(this);

        geoScreen = (TextView) findViewById(R.id.geoScreen);
        geoScreen.setOnClickListener(this);

        googleTasks = (TextView) findViewById(R.id.googleTasks);
        if (new GTasksHelper(ArchiveActivity.this).isLinked()) googleTasks.setVisibility(View.VISIBLE);
        googleTasks.setOnClickListener(this);

        calendar = (TextView) findViewById(R.id.calendar);
        calendar.setOnClickListener(this);

        notes = (TextView) findViewById(R.id.notes);
        notes.setOnClickListener(this);

        TextView help = (TextView) findViewById(R.id.help);
        help.setOnClickListener(this);
        help = (TextView) findViewById(R.id.help);
        help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        mDrawerLayout.closeDrawers();
                    }
                });
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (checkGooglePlayServicesAvailability()) {
                            startActivity(new Intent(ArchiveActivity.this, Help.class));
                        }
                    }
                }, 250);
            }
        });
        help.setTypeface(typeface);

        manageBackup = (TextView) findViewById(R.id.manageBackup);
        manageBackup.setOnClickListener(this);

        archiveScreen = (TextView) findViewById(R.id.archiveScreen);
        archiveScreen.setOnClickListener(this);
        archiveScreen.setEnabled(false);

        fragmentSettings.setTypeface(typeface);
        geoScreen.setTypeface(typeface);
        calendar.setTypeface(typeface);
        notes.setTypeface(typeface);
        activeScreen.setTypeface(typeface);
        archiveScreen.setTypeface(typeface);
        manageBackup.setTypeface(typeface);
        googleTasks.setTypeface(typeface);

        helpTranslate = (TextView) findViewById(R.id.helpTranslate);
        helpTranslate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        mDrawerLayout.closeDrawers();
                    }
                });
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        translationDialog().show();
                    }
                }, 250);
            }
        });
        helpTranslate.setTypeface(typeface);

        moreApps = (TextView) findViewById(R.id.moreApps);
        moreApps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        mDrawerLayout.closeDrawers();
                    }
                });
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse("market://search?q=pub:Nazar Suhovich"));
                        try {
                            startActivity(intent);
                        } catch (ActivityNotFoundException e) {
                            Toast.makeText(ArchiveActivity.this, "Couldn't launch market", Toast.LENGTH_LONG).show();
                        }
                    }
                }, 250);
            }
        });
        moreApps.setTypeface(typeface);

        categories = (TextView) findViewById(R.id.categories);
        categories.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        mDrawerLayout.closeDrawers();
                    }
                });
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(ArchiveActivity.this, CategoriesList.class));
                    }
                }, 250);
            }
        });
        categories.setTypeface(typeface);

        places = (TextView) findViewById(R.id.places);
        places.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        mDrawerLayout.closeDrawers();
                    }
                });
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(ArchiveActivity.this, PlacesList.class));
                    }
                }, 250);
            }
        });
        places.setTypeface(typeface);

        templates = (TextView) findViewById(R.id.templates);
        templates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        mDrawerLayout.closeDrawers();
                    }
                });
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(ArchiveActivity.this, TemplatesList.class));
                    }
                }, 250);
            }
        });
        templates.setTypeface(typeface);
        if (!DB.isOpen()) DB.open();
        Cursor c = DB.queryTemplates();
        if (c != null && c.moveToFirst() && sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_QUICK_SMS)){
            templates.setVisibility(View.VISIBLE);
        }
        if (c != null) c.close();

        if (!DB.isOpen()) DB.open();
        c = DB.queryPlaces();
        if (c != null && c.moveToFirst()){
            places.setVisibility(View.VISIBLE);
        }
        if (c != null) c.close();

        if (!new ManageModule().isPro()){
            ads_container = (RelativeLayout) findViewById(R.id.ads_container);
            basket = (ImageView) findViewById(R.id.basket);
            if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_USE_DARK_THEME)){
                basket.setImageResource(R.drawable.market_icon_white);
            } else basket.setImageResource(R.drawable.market_icon);

            showMarket();

            if (!new ManageModule().isPro()) {
                setUpAds();
            }
        }
    }

    private void editReminder(long id){
        Intent intentId = new Intent(ArchiveActivity.this, ReminderManager.class);
        intentId.putExtra(Constants.EDIT_ID, id);
        startActivity(intentId);
    }

    protected Dialog marketDialog() {
        return new AlertDialog.Builder(this)
                .setTitle(getString(R.string.buy_pro_string))
                .setMessage(getString(R.string.pro_explanation_1) + "\n" +
                        getString(R.string.pro_explanation_2) + "\n" +
                        getString(R.string.pro_explanation_3) + "\n" +
                        getString(R.string.pro_explanation_4) + "\n" +
                        getString(R.string.pro_explanation_5) + "\n" +
                        getString(R.string.pro_explanation_7) + "\n" +
                        getString(R.string.pro_explanation_6))
                .setPositiveButton(getString(R.string.dialog_button_buy), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse("market://details?id=" + MARKET_APP_PASSWORDS_PRO));
                        startActivity(intent);
                    }
                })
                .setNegativeButton(getString(R.string.button_close), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setCancelable(true)
                .create();
    }

    private void showMarket() {
        if (!isAppInstalled(MARKET_APP_PASSWORDS_PRO)){
            ads_container.setVisibility(View.VISIBLE);
            ads_container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    marketDialog().show();
                }
            });
        }
    }

    private boolean isAppInstalled(String packageName) {
        PackageManager pm = getPackageManager();
        boolean installed;
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            installed = false;
        }
        return installed;
    }

    private void setUpAds(){
        adView = (AdView) findViewById(R.id.adView);
        adView.setVisibility(View.GONE);

        AdRequest adRequest = new AdRequest.Builder()
                .build();
        adView.loadAd(adRequest);
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(int errorCode) {
                adView.setVisibility(View.GONE);
            }

            @Override
            public void onAdLoaded() {
                adView.setVisibility(View.VISIBLE);
            }
        });
    }

    protected Dialog translationDialog() {
        return new AlertDialog.Builder(this)
                .setTitle(getString(R.string.translation_dialog_title))
                .setMessage(getString(R.string.translation_dialog_message))
                .setPositiveButton(getString(R.string.settings_help_point), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                                Uri.parse("https://docs.google.com/spreadsheets/d/1MeSehWucAsNEA67pIUzf3arL4-EtYh7SjwF-I-417UU/edit#gid=0"));
                        try {
                            startActivity(browserIntent);
                        } catch (ActivityNotFoundException e){
                            e.printStackTrace();
                        }
                    }
                })
                .setNegativeButton(getString(R.string.hide_dialog_button), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        sPrefs = new SharedPrefs(ArchiveActivity.this);
                        sPrefs.saveBoolean(Constants.APP_UI_PREFERENCES_HIDE_TRANSLATION_MENU, true);
                        dialog.dismiss();
                    }
                })
                .setCancelable(true)
                .create();
    }

    private void setImage(){
        sPrefs = new SharedPrefs(ArchiveActivity.this);
        if (!sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_USE_DARK_THEME)){
            activeScreen.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_access_time_grey600_24dp, 0, 0, 0);
            archiveScreen.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_storage_grey600_24dp, 0, 0, 0);
            calendar.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_today_grey600_24dp, 0, 0, 0);
            geoScreen.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_navigation_grey600_24dp, 0, 0, 0);
            fragmentSettings.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_settings_grey600_24dp, 0, 0, 0);
            manageBackup.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_history_grey600_24dp, 0, 0, 0);
            notes.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_event_note_grey600_24dp, 0, 0, 0);
            googleTasks.setCompoundDrawablesWithIntrinsicBounds(R.drawable.google_tasks_grey, 0, 0, 0);
        } else {
            activeScreen.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_access_time_white_24dp, 0, 0, 0);
            archiveScreen.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_storage_white_24dp, 0, 0, 0);
            calendar.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_today_white_24dp, 0, 0, 0);
            geoScreen.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_navigation_white_24dp, 0, 0, 0);
            fragmentSettings.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_settings_white_24dp, 0, 0, 0);
            manageBackup.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_history_white_24dp, 0, 0, 0);
            notes.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_event_note_white_24dp, 0, 0, 0);
            googleTasks.setCompoundDrawablesWithIntrinsicBounds(R.drawable.google_tasks_white, 0, 0, 0);
        }
    }

    ArrayList<ReminderItem> arrayList;

    public void loaderAdapter(){
        if (!DB.isOpen()) DB.open();
        Cursor c =  DB.queryArchived();

        SyncHelper helper = new SyncHelper(this);
        arrayList = new ArrayList<>();

        if (c != null && c.moveToFirst()){
            findViewById(R.id.emptyItem).setVisibility(View.GONE);
            do {
                String title = c.getString(c.getColumnIndex(Constants.COLUMN_TEXT));
                String type = c.getString(c.getColumnIndex(Constants.COLUMN_TYPE));
                String number = c.getString(c.getColumnIndex(Constants.COLUMN_NUMBER));
                String weekdays = c.getString(c.getColumnIndex(Constants.COLUMN_WEEKDAYS));
                String catId = c.getString(c.getColumnIndex(Constants.COLUMN_CATEGORY));
                String uuId = c.getString(c.getColumnIndex(Constants.COLUMN_TECH_VAR));
                int hour = c.getInt(c.getColumnIndex(Constants.COLUMN_HOUR));
                int minute = c.getInt(c.getColumnIndex(Constants.COLUMN_MINUTE));
                int seconds = c.getInt(c.getColumnIndex(Constants.COLUMN_SECONDS));
                int day = c.getInt(c.getColumnIndex(Constants.COLUMN_DAY));
                int month = c.getInt(c.getColumnIndex(Constants.COLUMN_MONTH));
                int year = c.getInt(c.getColumnIndex(Constants.COLUMN_YEAR));
                int repCode = c.getInt(c.getColumnIndex(Constants.COLUMN_REPEAT));
                int repTime = c.getInt(c.getColumnIndex(Constants.COLUMN_REMIND_TIME));
                int isDone = c.getInt(c.getColumnIndex(Constants.COLUMN_IS_DONE));
                double lat = c.getDouble(c.getColumnIndex(Constants.COLUMN_LATITUDE));
                double lon = c.getDouble(c.getColumnIndex(Constants.COLUMN_LONGITUDE));
                int repCount = c.getInt(c.getColumnIndex(Constants.COLUMN_REMINDERS_COUNT));
                int delay = c.getInt(c.getColumnIndex(Constants.COLUMN_DELAY));
                long mId = c.getLong(c.getColumnIndex(Constants.COLUMN_ID));

                TimeCount mCount = new TimeCount(this);
                Interval mInterval = new Interval(this);
                long due;
                String repeat = null;
                if (!type.startsWith(Constants.TYPE_WEEKDAY)) {
                    due = mCount.getEventTime(year, month, day, hour, minute, seconds, repTime,
                            repCode, repCount, delay);
                    if (type.matches(Constants.TYPE_CALL) || type.matches(Constants.TYPE_MESSAGE) ||
                            type.matches(Constants.TYPE_REMINDER) || type.startsWith(Constants.TYPE_SKYPE) ||
                            type.startsWith(Constants.TYPE_APPLICATION)) {
                        repeat = mInterval.getInterval(repCode);
                    } else if (type.matches(Constants.TYPE_TIME)) {
                        repeat = mInterval.getTimeInterval(repCode);
                    } else {
                        repeat = getString(R.string.interval_zero);
                    }
                } else {
                    due = mCount.getNextWeekdayTime(hour, minute, weekdays, delay);
                    if (weekdays.length() == 7) {
                        repeat = helper.getRepeatString(weekdays);
                    }
                }

                ReminderItem item = new ReminderItem(title, type, repeat, catId, uuId, isDone, due, mId,
                        new double[]{lat, lon}, number);
                item.setArchived(1);

                arrayList.add(item);
            } while (c.moveToNext());
        } else findViewById(R.id.emptyItem).setVisibility(View.VISIBLE);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);

        // touch guard manager  (this class is required to suppress scrolling while swipe-dismiss animation is running)
        RecyclerViewTouchActionGuardManager mRecyclerViewTouchActionGuardManager =
                new RecyclerViewTouchActionGuardManager();
        mRecyclerViewTouchActionGuardManager.setInterceptVerticalScrollingWhileAnimationRunning(true);
        mRecyclerViewTouchActionGuardManager.setEnabled(true);

        // swipe manager
        RecyclerViewSwipeManager mRecyclerViewSwipeManager = new RecyclerViewSwipeManager();

        //adapter
        final RemindersRecyclerAdapter myItemAdapter = new RemindersRecyclerAdapter(this, arrayList);
        myItemAdapter.setEventListener(new RemindersRecyclerAdapter.EventListener() {
            @Override
            public void onItemRemoved(int position) {
                final long id = arrayList.get(position).getId();
                editReminder(id);
            }

            @Override
            public void onItemPinned(int position) {
                final long id = arrayList.get(position).getId();
                removeReminder(id);
            }

            @Override
            public void onItemClicked(int position) {
                final long id = arrayList.get(position).getId();
                editReminder(id);
            }

            @Override
            public void onItemLongClicked(int position) {
                final long id = arrayList.get(position).getId();
                editReminder(id);
            }

            @Override
            public void onItemViewClicked(View v, boolean isPinned) {

            }
        });

        RecyclerView.Adapter mWrappedAdapter = mRecyclerViewSwipeManager.createWrappedAdapter(myItemAdapter);

        final GeneralItemAnimator animator = new SwipeDismissItemAnimator();
        animator.setSupportsChangeAnimations(false);

        currentList.setLayoutManager(mLayoutManager);
        currentList.setAdapter(mWrappedAdapter);  // requires *wrapped* adapter
        currentList.setItemAnimator(animator);

        currentList.addItemDecoration(new SimpleListDividerDecorator(getResources()
                .getDrawable(R.drawable.list_divider), true));

        mRecyclerViewTouchActionGuardManager.attachRecyclerView(currentList);
        mRecyclerViewSwipeManager.attachRecyclerView(currentList);
    }

    private void removeReminder(long itId){
        if (itId != 0) {
            if (!DB.isOpen()) DB.open();
            new CalendarManager(ArchiveActivity.this).deleteEvents(itId);
            Cursor c = DB.getTask(itId);
            String uuId = null;
            if (c != null && c.moveToFirst()){
                uuId = c.getString(c.getColumnIndex(Constants.COLUMN_TECH_VAR));
            }
            DB.deleteTask(itId);
            new DeleteReminder(ArchiveActivity.this, uuId).execute();
            Toast.makeText(ArchiveActivity.this, getString(R.string.swipe_delete),
                    Toast.LENGTH_SHORT).show();
            loaderAdapter();
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean drawerOpen = shouldGoInvisible;
        hideMenuItems(menu, !drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    private void hideMenuItems(Menu menu, boolean visible) {
        for(int i = 0; i < menu.size(); i++){
            menu.getItem(i).setVisible(visible);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.archive_menu, menu);
        return true;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    private void deleteAll(){
        if (!DB.isOpen()) DB.open();
        Cursor c = DB.queryArchived();
        if (c != null && c.moveToFirst()){
            do{
                long rowId = c.getLong(c.getColumnIndex(Constants.COLUMN_ID));
                String uuId = c.getString(c.getColumnIndex(Constants.COLUMN_TECH_VAR));
                new CalendarManager(ArchiveActivity.this).deleteEvents(rowId);
                DB.deleteTask(rowId);
                new DeleteReminder(ArchiveActivity.this, uuId).execute();
            }while (c.moveToNext());
        }
        if (c != null) c.close();
    }

    @Override
    protected void onResume() {
        String action = getIntent().getAction();
        if(action == null || !action.equals("JustArchiveActivity Created")) {
            Intent intent = new Intent(this, ArchiveActivity.class);
            startActivity(intent);
            finish();
        }
        else
            getIntent().setAction(null);

        setRequestedOrientation(cSetter.getRequestOrientation());
        setImage();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(cSetter.colorStatus());
        }

        if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_HIDE_TRANSLATION_MENU)) {
            helpTranslate.setVisibility(View.GONE);
        }

        loaderAdapter();
        isOpened = false;

        if (!new ManageModule().isPro()){
            if (adView != null) {
                adView.resume();
            }
        }
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.activeScreen:
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        mDrawerLayout.closeDrawers();
                    }
                });
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent openActive = new Intent(ArchiveActivity.this, MainActivity.class);
                        startActivity(openActive);
                        finish();
                    }
                }, 250);
                break;
            case R.id.fragmentSettings:
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        mDrawerLayout.closeDrawers();
                    }
                });
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intentS = new Intent(ArchiveActivity.this, SettingsActivity.class);
                        startActivity(intentS);
                    }
                }, 250);
                break;
            case R.id.notes:
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        mDrawerLayout.closeDrawers();
                    }
                });
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(ArchiveActivity.this, NotesActivity.class));
                    }
                }, 250);
                break;
            case R.id.geoScreen:
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        mDrawerLayout.closeDrawers();
                    }
                });
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (checkGooglePlayServicesAvailability()) {
                            startActivity(new Intent(ArchiveActivity.this, GeolocationTasks.class));
                        }
                    }
                }, 250);
                break;
            case R.id.calendar:
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        mDrawerLayout.closeDrawers();
                    }
                });
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(ArchiveActivity.this, CalendarActivity.class));
                    }
                }, 250);
                break;
            case R.id.googleTasks:
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        mDrawerLayout.closeDrawers();
                    }
                });
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(ArchiveActivity.this, TasksActivity.class));
                    }
                }, 250);
                break;
            case R.id.manageBackup:
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        mDrawerLayout.closeDrawers();
                    }
                });
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(ArchiveActivity.this, BackupManager.class));
                    }
                }, 250);
                break;
        }
    }

    public boolean checkGooglePlayServicesAvailability()
    {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if(resultCode != ConnectionResult.SUCCESS)
        {
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, 69);
            dialog.setCancelable(false);
            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    dialog.dismiss();
                }
            });
            dialog.show();
            return false;
        } else {
            Log.d("GooglePlayServicesUtil", "Result is: " + resultCode);
            return true;
        }
    }

    @Override
    protected void onDestroy() {
        if (!new ManageModule().isPro()) {
            if (adView != null) {
                adView.destroy();
            }
        }
        if (DB != null && DB.isOpen()) DB.close();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        if (!new ManageModule().isPro()) {
            if (adView != null) {
                adView.pause();
            }
        }
        super.onPause();
    }

    private boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            finish();
            return;
        }
        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, getString(R.string.press_again), Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }
}