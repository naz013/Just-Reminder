package com.cray.software.justreminder.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.ScreenManager;
import com.cray.software.justreminder.activities.HelpOverflow;
import com.cray.software.justreminder.cloud.GTasksHelper;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.modules.Module;
import com.cray.software.justreminder.utils.ViewUtils;
import com.squareup.picasso.Picasso;

import jp.wasabeef.picasso.transformations.BlurTransformation;

public class NavigationDrawerFragment extends Fragment implements 
        View.OnClickListener {

    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";
    private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";

    private NavigationDrawerCallbacks mCallbacks;
    private ActionBarDrawerToggle mDrawerToggle;

    private DrawerLayout mDrawerLayout;
    private TextView archiveScreen;
    private TextView activeScreen;
    private TextView geoScreen;
    private TextView calendar;
    private TextView manageBackup;
    private TextView notes;
    private TextView googleTasks;
    private TextView templates;
    private TextView places;
    private TextView categories;
    private TextView prefsButton;
    private TextView appNameBanner;
    private View mFragmentContainerView;
    private ImageView image;

    private String mCurrentSelectedPosition = "";
    private boolean mFromSavedInstanceState;
    private boolean mUserLearnedDrawer;

    public NavigationDrawerFragment() {
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Indicate that this fragment would like to influence the set of actions in the action bar.
        setHasOptionsMenu(true);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mUserLearnedDrawer = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false);

        if (savedInstanceState != null) {
            mCurrentSelectedPosition = savedInstanceState.getString(STATE_SELECTED_POSITION);
            selectItem(mCurrentSelectedPosition, true);
            mFromSavedInstanceState = true;
            disableItem(mCurrentSelectedPosition);
        } else {
            selectItem(ScreenManager.FRAGMENT_ACTIVE, true);
            mFromSavedInstanceState = false;
            disableItem(ScreenManager.FRAGMENT_ACTIVE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_navigation_drawer, container, false);

        rootView.findViewById(R.id.drawerBg).setBackgroundColor(new ColorSetter(getActivity()).getBackgroundStyle());

        Typeface typeface = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Light.ttf");

        image = (ImageView) rootView.findViewById(R.id.image);

        appNameBanner = (TextView) rootView.findViewById(R.id.appNameBanner);
        appNameBanner.setTypeface(typeface);

        String appName;
        if (Module.isPro()) {
            appName = getString(R.string.app_name_pro);
        } else {
            appName = getString(R.string.app_name);
        }
        appNameBanner.setText(appName.toUpperCase());

        prefsButton = (TextView) rootView.findViewById(R.id.settings);
        prefsButton.setOnClickListener(this);

        TextView helpButton = (TextView) rootView.findViewById(R.id.help);
        helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectItem(ScreenManager.HELP, false);
            }
        });

        TextView feedButton = (TextView) rootView.findViewById(R.id.feed);
        feedButton.setOnClickListener(this);

        geoScreen = (TextView) rootView.findViewById(R.id.geoScreen);
        geoScreen.setOnClickListener(this);

        manageBackup = (TextView) rootView.findViewById(R.id.manageBackup);
        manageBackup.setOnClickListener(this);

        calendar = (TextView) rootView.findViewById(R.id.calendar);
        calendar.setOnClickListener(this);

        notes = (TextView) rootView.findViewById(R.id.notes);
        notes.setOnClickListener(this);

        googleTasks = (TextView) rootView.findViewById(R.id.googleTasks);
        googleTasks.setOnClickListener(this);

        activeScreen = (TextView) rootView.findViewById(R.id.activeScreen);
        activeScreen.setOnClickListener(this);
        activeScreen.setEnabled(false);

        archiveScreen = (TextView) rootView.findViewById(R.id.archiveScreen);
        archiveScreen.setVisibility(View.VISIBLE);
        archiveScreen.setOnClickListener(this);

        categories = (TextView) rootView.findViewById(R.id.categories);
        categories.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectItem(ScreenManager.FRAGMENT_GROUPS, true);
                disableItem(ScreenManager.FRAGMENT_GROUPS);
            }
        });
        categories.setTypeface(typeface);

        places = (TextView) rootView.findViewById(R.id.places);
        places.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectItem(ScreenManager.FRAGMENT_PLACES, true);
                disableItem(ScreenManager.FRAGMENT_PLACES);
            }
        });
        places.setTypeface(typeface);

        templates = (TextView) rootView.findViewById(R.id.templates);
        templates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectItem(ScreenManager.FRAGMENT_TEMPLATES, true);
                disableItem(ScreenManager.FRAGMENT_TEMPLATES);
            }
        });
        templates.setTypeface(typeface);
        reloadItems();

        geoScreen.setTypeface(typeface);
        calendar.setTypeface(typeface);
        notes.setTypeface(typeface);
        googleTasks.setTypeface(typeface);
        activeScreen.setTypeface(typeface);
        archiveScreen.setTypeface(typeface);
        manageBackup.setTypeface(typeface);
        googleTasks.setTypeface(typeface);
        prefsButton.setTypeface(typeface);
        feedButton.setTypeface(typeface);
        helpButton.setTypeface(typeface);


        if (!Module.isPro()){
            RelativeLayout ads_container = (RelativeLayout) rootView.findViewById(R.id.ads_container);
            ImageView basket = (ImageView) rootView.findViewById(R.id.basket);
            SharedPrefs sPrefs = new SharedPrefs(getActivity());
            if (sPrefs.loadBoolean(Prefs.USE_DARK_THEME)){
                basket.setImageResource(R.drawable.market_icon_white);
            } else {
                basket.setImageResource(R.drawable.market_icon);
            }

            if (!isAppInstalled("com.cray.software.justreminderpro")){
                ads_container.setVisibility(View.VISIBLE);
                ads_container.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        selectItem(ScreenManager.MARKET, false);
                    }
                });
            }
        }

        loadMenu();
        return rootView;
    }

    private void reloadItems(){
        appNameBanner.setTextColor(ViewUtils.getColor(getActivity(), R.color.colorWhite));
        Picasso.with(getActivity())
                .load(R.drawable.photo_main)
                .resize(1020, 500)
                .transform(new BlurTransformation(getActivity(), 20, 2))
                .into(image);
        image.setVisibility(View.VISIBLE);

        if (new GTasksHelper(getActivity()).isLinked()) {
            googleTasks.setVisibility(View.VISIBLE);
        }

        DataBase DB = new DataBase(getActivity());
        SharedPrefs sPrefs = new SharedPrefs(getActivity());
        if (!DB.isOpen()) {
            DB.open();
        }
        Cursor c = DB.queryTemplates();
        if (c != null && c.moveToFirst() && sPrefs.loadBoolean(Prefs.QUICK_SMS)){
            templates.setVisibility(View.VISIBLE);
        }
        if (!DB.isOpen()) {
            DB.open();
        }
        c = DB.queryPlaces();
        if (c != null && c.moveToFirst()){
            places.setVisibility(View.VISIBLE);
        }
        if (c != null) {
            c.close();
        }
    }

    private boolean isAppInstalled(String packageName) {
        PackageManager pm = getActivity().getPackageManager();
        boolean installed;
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            installed = false;
        }
        return installed;
    }

    private void loadMenu() {
        SharedPrefs prefs = new SharedPrefs(getActivity());
        if (!prefs.loadBoolean(Prefs.USE_DARK_THEME)){
            activeScreen.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_notifications_black_24dp, 0, 0, 0);
            archiveScreen.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_delete_black_24dp, 0, 0, 0);
            calendar.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_today_black_24dp, 0, 0, 0);
            geoScreen.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_navigation_black_24dp, 0, 0, 0);
            manageBackup.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_history_black_24dp, 0, 0, 0);
            notes.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_event_note_black_24dp, 0, 0, 0);
            googleTasks.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_event_available_black_24dp, 0, 0, 0);
            templates.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_textsms_black_24dp, 0, 0, 0);
            places.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_place_black_24dp, 0, 0, 0);
            categories.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_local_offer_black_24dp, 0, 0, 0);
            prefsButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_settings_black_24dp, 0, 0, 0);
        } else {
            activeScreen.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_notifications_white_24dp, 0, 0, 0);
            archiveScreen.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_delete_white_24dp, 0, 0, 0);
            calendar.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_today_white_24dp, 0, 0, 0);
            geoScreen.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_navigation_white_24dp, 0, 0, 0);
            manageBackup.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_history_white_24dp, 0, 0, 0);
            notes.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_event_note_white_24dp, 0, 0, 0);
            googleTasks.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_event_available_white_24dp, 0, 0, 0);
            templates.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_textsms_white_24dp, 0, 0, 0);
            places.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_place_white_24dp, 0, 0, 0);
            categories.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_local_offer_white_24dp, 0, 0, 0);
            prefsButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_settings_white_24dp, 0, 0, 0);
        }
    }

    public boolean isDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mFragmentContainerView);
    }

    public void setUp(int fragmentId, DrawerLayout drawerLayout, Toolbar toolbar) {
        mFragmentContainerView = getActivity().findViewById(fragmentId);
        mDrawerLayout = drawerLayout;

        mDrawerToggle = new ActionBarDrawerToggle(
                getActivity(),                    /* host Activity */
                mDrawerLayout,                    /* DrawerLayout object */
                toolbar,             /* nav drawer image to replace 'Up' caret */
                R.string.navigation_drawer_open,  /* "open drawer" description for accessibility */
                R.string.navigation_drawer_close  /* "close drawer" description for accessibility */
        ) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (!isAdded()) {
                    return;
                }

                getActivity().supportInvalidateOptionsMenu(); // calls onPrepareOptionsMenu()

                if (mCallbacks != null) {
                    mCallbacks.isDrawerOpen(false);
                }
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if (!isAdded()) {
                    return;
                }

                if (!mUserLearnedDrawer) {
                    mUserLearnedDrawer = true;
                    SharedPreferences sp = PreferenceManager
                            .getDefaultSharedPreferences(getActivity());
                    sp.edit().putBoolean(PREF_USER_LEARNED_DRAWER, true).apply();
                }

                getActivity().supportInvalidateOptionsMenu(); // calls onPrepareOptionsMenu()

                if (mCallbacks != null) {
                    mCallbacks.isDrawerOpen(true);
                }
            }
        };

        if (!mUserLearnedDrawer && !mFromSavedInstanceState) {
            mDrawerLayout.openDrawer(mFragmentContainerView);
        }

        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });

        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    private void selectItem(final String tag, boolean select) {
        if (select) {
            mCurrentSelectedPosition = tag;
        }
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(mFragmentContainerView);
        }
        if (mCallbacks != null) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (tag != null) {
                        try {
                            mCallbacks.onNavigationDrawerItemSelected(tag);
                        } catch (NullPointerException e){
                            e.printStackTrace();
                            mCallbacks.onNavigationDrawerItemSelected(ScreenManager.FRAGMENT_ACTIVE);
                        }
                    } else {
                        mCallbacks.onNavigationDrawerItemSelected(ScreenManager.FRAGMENT_ACTIVE);
                    }
                }
            }, 250);
        }
    }

    private void disableItem(String tag) {
        activeScreen.setEnabled(true);
        archiveScreen.setEnabled(true);
        googleTasks.setEnabled(true);
        calendar.setEnabled(true);
        notes.setEnabled(true);
        manageBackup.setEnabled(true);
        geoScreen.setEnabled(true);
        places.setEnabled(true);
        templates.setEnabled(true);
        categories.setEnabled(true);

        if (tag.matches(ScreenManager.FRAGMENT_ACTIVE)){
            activeScreen.setEnabled(false);
        } else if (tag.matches(ScreenManager.FRAGMENT_ARCHIVE)){
            archiveScreen.setEnabled(false);
        } else if (tag.matches(ScreenManager.FRAGMENT_EVENTS) ||
                tag.matches(ScreenManager.ACTION_CALENDAR)){
            calendar.setEnabled(false);
        } else if (tag.matches(ScreenManager.FRAGMENT_NOTE)){
            notes.setEnabled(false);
        } else if (tag.matches(ScreenManager.FRAGMENT_GROUPS)){
            categories.setEnabled(false);
        } else if (tag.matches(ScreenManager.FRAGMENT_PLACES)){
            places.setEnabled(false);
        } else if (tag.matches(ScreenManager.FRAGMENT_TEMPLATES)){
            templates.setEnabled(false);
        } else if (tag.matches(ScreenManager.FRAGMENT_TASKS)){
            googleTasks.setEnabled(false);
        } else if (tag.matches(ScreenManager.FRAGMENT_LOCATIONS)){
            geoScreen.setEnabled(false);
        } else if (tag.matches(ScreenManager.FRAGMENT_BACKUPS)){
            manageBackup.setEnabled(false);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallbacks = (NavigationDrawerCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(STATE_SELECTED_POSITION, mCurrentSelectedPosition);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Forward the new configuration the drawer toggle component.
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.archiveScreen:
                selectItem(ScreenManager.FRAGMENT_ARCHIVE, true);
                disableItem(ScreenManager.FRAGMENT_ARCHIVE);
                break;
            case R.id.activeScreen:
                selectItem(ScreenManager.FRAGMENT_ACTIVE, true);
                disableItem(ScreenManager.FRAGMENT_ACTIVE);
                break;
            case R.id.settings:
                selectItem(ScreenManager.FRAGMENT_SETTINGS, false);
                break;
            case R.id.feed:
                final Intent emailIntent = new Intent( Intent.ACTION_SEND);
                emailIntent.setType("plain/text");
                emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] { "feedback.cray@gmail.com" });
                if (Module.isPro()){
                    emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Reminder PRO");
                } else {
                    emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Reminder");
                }
                getActivity().startActivity(Intent.createChooser(emailIntent, "Send mail..."));
                break;
            case R.id.geoScreen:
                selectItem(ScreenManager.FRAGMENT_LOCATIONS, true);
                disableItem(ScreenManager.FRAGMENT_LOCATIONS);
                break;
            case R.id.notes:
                selectItem(ScreenManager.FRAGMENT_NOTE, true);
                disableItem(ScreenManager.FRAGMENT_NOTE);
                break;
            case R.id.googleTasks:
                selectItem(ScreenManager.FRAGMENT_TASKS, true);
                disableItem(ScreenManager.FRAGMENT_TASKS);
                break;
            case R.id.calendar:
                SharedPrefs sPrefs = new SharedPrefs(getActivity());
                if (sPrefs.loadInt(Prefs.LAST_CALENDAR_VIEW) == 1) {
                    selectItem(ScreenManager.ACTION_CALENDAR, true);
                    disableItem(ScreenManager.ACTION_CALENDAR);
                } else {
                    selectItem(ScreenManager.FRAGMENT_EVENTS, true);
                    disableItem(ScreenManager.FRAGMENT_EVENTS);
                }
                break;
            case R.id.manageBackup:
                selectItem(ScreenManager.FRAGMENT_BACKUPS, true);
                disableItem(ScreenManager.FRAGMENT_BACKUPS);
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        reloadItems();
        SharedPrefs sPrefs = new SharedPrefs(getActivity());
        DataBase DB = new DataBase(getActivity());
        if (!DB.isOpen()) {
            DB.open();
        }
        if (DB.getCountActive() > 0){
            if (isListFirstTime() && sPrefs.loadBoolean(Prefs.THANKS_SHOWN)){
                startActivity(new Intent(getActivity(), HelpOverflow.class)
                        .putExtra(Constants.ITEM_ID_INTENT, 1));
            }
        }

        if (mCallbacks != null) {
            mCallbacks.onNavigationDrawerItemSelected(mCurrentSelectedPosition);
        }
    }

    private boolean isListFirstTime() {
        SharedPrefs sPrefs = new SharedPrefs(getActivity());
        PackageInfo pInfo = null;
        try {
            pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        String version = null;
        if (pInfo != null) {
            version = pInfo.versionName;
        }
        boolean isShown = sPrefs.loadVersionBoolean(version);
        SharedPreferences preferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        boolean ranBefore = preferences.getBoolean("JustListBefore", false);
        if (!ranBefore && isShown) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("JustListBefore", true);
            editor.commit();
        }
        return !ranBefore;
    }


    public interface NavigationDrawerCallbacks {
        void onNavigationDrawerItemSelected(String tag);
        void onTitleChanged(String title);
        void showSnackbar(int message);
        void onDateChanged(long dateMills, int position);
        void onListIdChanged(long listId);
        void onListChanged(RecyclerView list);
        void isDrawerOpen(boolean isOpen);
        void onUiChanged(int colorSetter, int colorStatus, int colorChooser);
    }
}
