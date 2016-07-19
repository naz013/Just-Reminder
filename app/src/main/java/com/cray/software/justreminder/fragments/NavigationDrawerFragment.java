/**
 * Copyright 2016 Nazar Suhovich
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cray.software.justreminder.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.ScreenManager;
import com.cray.software.justreminder.cloud.GTasksHelper;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.interfaces.NavigationCallbacks;
import com.cray.software.justreminder.modules.Module;
import com.cray.software.justreminder.places.PlacesHelper;
import com.cray.software.justreminder.roboto_views.RoboTextView;
import com.cray.software.justreminder.templates.TemplateHelper;
import com.cray.software.justreminder.utils.QuickReturnUtils;
import com.cray.software.justreminder.utils.ViewUtils;
import com.squareup.picasso.Picasso;

public class NavigationDrawerFragment extends Fragment implements View.OnClickListener {

    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";
    private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";

    private NavigationCallbacks mCallbacks;
    private ActionBarDrawerToggle mDrawerToggle;

    private DrawerLayout mDrawerLayout;
    private RoboTextView archiveScreen;
    private RoboTextView activeScreen;
    private RoboTextView geoScreen;
    private RoboTextView calendar;
    private RoboTextView manageBackup;
    private RoboTextView notes;
    private RoboTextView googleTasks;
    private RoboTextView templates;
    private RoboTextView places;
    private RoboTextView categories;
    private RoboTextView prefsButton;
    private RoboTextView appNameBanner;
    private View mFragmentContainerView;
    private ImageView image;

    private String mCurrentSelectedPosition = "";
    private boolean mFromSavedInstanceState;
    private boolean mUserLearnedDrawer;

    private Activity mContext;

    public NavigationDrawerFragment() {
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Indicate that this fragment would like to influence the set of actions in the action bar.
        setHasOptionsMenu(true);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
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

        rootView.findViewById(R.id.drawerBg).setBackgroundColor(new ColorSetter(mContext).getBackgroundStyle());
        image = (ImageView) rootView.findViewById(R.id.image);
        appNameBanner = (RoboTextView) rootView.findViewById(R.id.appNameBanner);
        String appName;
        if (Module.isPro()) {
            appName = getString(R.string.app_name_pro);
        } else {
            appName = getString(R.string.app_name);
        }
        appNameBanner.setText(appName.toUpperCase());

        prefsButton = (RoboTextView) rootView.findViewById(R.id.settings);
        prefsButton.setOnClickListener(this);

        RoboTextView helpButton = (RoboTextView) rootView.findViewById(R.id.help);
        helpButton.setOnClickListener(v -> selectItem(ScreenManager.HELP, false));

        RoboTextView feedButton = (RoboTextView) rootView.findViewById(R.id.feed);
        feedButton.setOnClickListener(this);

        geoScreen = (RoboTextView) rootView.findViewById(R.id.geoScreen);
        geoScreen.setOnClickListener(this);

        manageBackup = (RoboTextView) rootView.findViewById(R.id.manageBackup);
        manageBackup.setOnClickListener(this);

        calendar = (RoboTextView) rootView.findViewById(R.id.calendar);
        calendar.setOnClickListener(this);

        notes = (RoboTextView) rootView.findViewById(R.id.notes);
        notes.setOnClickListener(this);

        googleTasks = (RoboTextView) rootView.findViewById(R.id.googleTasks);
        googleTasks.setOnClickListener(this);

        activeScreen = (RoboTextView) rootView.findViewById(R.id.activeScreen);
        activeScreen.setOnClickListener(this);
        activeScreen.setEnabled(false);

        archiveScreen = (RoboTextView) rootView.findViewById(R.id.archiveScreen);
        archiveScreen.setVisibility(View.VISIBLE);
        archiveScreen.setOnClickListener(this);

        categories = (RoboTextView) rootView.findViewById(R.id.categories);
        categories.setOnClickListener(v -> {
            selectItem(ScreenManager.FRAGMENT_GROUPS, true);
            disableItem(ScreenManager.FRAGMENT_GROUPS);
        });

        places = (RoboTextView) rootView.findViewById(R.id.places);
        places.setOnClickListener(v -> {
            selectItem(ScreenManager.FRAGMENT_PLACES, true);
            disableItem(ScreenManager.FRAGMENT_PLACES);
        });

        templates = (RoboTextView) rootView.findViewById(R.id.templates);
        templates.setOnClickListener(v -> {
            selectItem(ScreenManager.FRAGMENT_TEMPLATES, true);
            disableItem(ScreenManager.FRAGMENT_TEMPLATES);
        });
        reloadItems();

        if (!Module.isPro()){
            RelativeLayout ads_container = (RelativeLayout) rootView.findViewById(R.id.ads_container);
            ImageView basket = (ImageView) rootView.findViewById(R.id.basket);
            if (new ColorSetter(mContext).isDark()){
                basket.setImageResource(R.drawable.market_icon_white);
            } else {
                basket.setImageResource(R.drawable.market_icon);
            }

            if (!isAppInstalled("com.cray.software.justreminderpro")){
                ads_container.setVisibility(View.VISIBLE);
                ads_container.setOnClickListener(v -> selectItem(ScreenManager.MARKET, false));
            }
        }

        loadMenu();
        return rootView;
    }

    private void reloadItems(){
        appNameBanner.setTextColor(ViewUtils.getColor(mContext, R.color.whitePrimary));
        DisplayMetrics metrics = new DisplayMetrics();
        mContext.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        double width = metrics.widthPixels * 0.5;
        int height = QuickReturnUtils.dp2px(mContext, 275);
        Picasso.with(mContext)
                .load(R.drawable.photo_main)
                .resize((int) width, height)
                .into(image);
        image.setVisibility(View.VISIBLE);
        if (new GTasksHelper(mContext).isLinked()) {
            googleTasks.setVisibility(View.VISIBLE);
        }
        int size = TemplateHelper.getInstance(mContext).getAll().size();
        if (size > 0 && SharedPrefs.getInstance(mContext).getBoolean(Prefs.QUICK_SMS)){
            templates.setVisibility(View.VISIBLE);
        }
        int placesCount = PlacesHelper.getInstance(mContext).getAll().size();
        if (placesCount > 0){
            places.setVisibility(View.VISIBLE);
        }
    }

    private boolean isAppInstalled(String packageName) {
        PackageManager pm = mContext.getPackageManager();
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
        if (!new ColorSetter(mContext).isDark()){
            activeScreen.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_notifications_black_24dp, 0, 0, 0);
            archiveScreen.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_delete_black_24dp, 0, 0, 0);
            calendar.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_today_black_vector, 0, 0, 0);
            geoScreen.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_navigation_black_24dp, 0, 0, 0);
            manageBackup.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_history_black_24dp, 0, 0, 0);
            notes.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_event_note_black_24dp, 0, 0, 0);
            googleTasks.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_event_available_black_24dp, 0, 0, 0);
            templates.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_textsms_black_vector, 0, 0, 0);
            places.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_place_black_vector, 0, 0, 0);
            categories.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_local_offer_black_24dp, 0, 0, 0);
            prefsButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_settings_black_24dp, 0, 0, 0);
        } else {
            activeScreen.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_notifications_white_24dp, 0, 0, 0);
            archiveScreen.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_delete_white_24dp, 0, 0, 0);
            calendar.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_today_white_vector, 0, 0, 0);
            geoScreen.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_navigation_white_24dp, 0, 0, 0);
            manageBackup.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_history_white_24dp, 0, 0, 0);
            notes.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_event_note_white_24dp, 0, 0, 0);
            googleTasks.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_event_available_white_24dp, 0, 0, 0);
            templates.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_textsms_white_vector, 0, 0, 0);
            places.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_place_white_vector, 0, 0, 0);
            categories.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_local_offer_white_24dp, 0, 0, 0);
            prefsButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_settings_white_24dp, 0, 0, 0);
        }
    }

    public void setUp(int fragmentId, DrawerLayout drawerLayout, Toolbar toolbar) {
        mFragmentContainerView = mContext.findViewById(fragmentId);
        mDrawerLayout = drawerLayout;
        mDrawerToggle = new ActionBarDrawerToggle(
                mContext,                    /* host Activity */
                mDrawerLayout,                    /* DrawerLayout object */
                toolbar,             /* nav drawer image to replace 'Up' caret */
                R.string.app_name,  /* "open drawer" description for accessibility */
                R.string.app_name  /* "close drawer" description for accessibility */
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
                            .getDefaultSharedPreferences(mContext);
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
        mDrawerLayout.post(() -> mDrawerToggle.syncState());
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
            new Handler().postDelayed(() -> {
                if (tag != null) {
                    try {
                        mCallbacks.onItemSelected(tag);
                    } catch (NullPointerException e){
                        e.printStackTrace();
                    }
                } else {
                    mCallbacks.onItemSelected(ScreenManager.FRAGMENT_ACTIVE);
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
    public void onAttach(Context context) {
        super.onAttach(context);
        if (mContext == null) {
            mContext = (Activity) context;
        }
        if (mCallbacks == null) {
            try {
                mCallbacks = (NavigationCallbacks) context;
            } catch (ClassCastException e) {
                throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
            }
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (mContext == null) {
            mContext = activity;
        }
        if (mCallbacks == null) {
            try {
                mCallbacks = (NavigationCallbacks) activity;
            } catch (ClassCastException e) {
                throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
            }
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
                selectItem(ScreenManager.REPORT, false);
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
                if (SharedPrefs.getInstance(mContext).getInt(Prefs.LAST_CALENDAR_VIEW) == 1) {
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
        if (mCallbacks != null) {
            mCallbacks.onItemSelected(mCurrentSelectedPosition);
        }
    }
}
