package com.hexrain.design;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cray.software.justreminder.BackupManager;
import com.cray.software.justreminder.CalendarActivity;
import com.cray.software.justreminder.HelpOverflow;
import com.cray.software.justreminder.NotesActivity;
import com.cray.software.justreminder.R;
import com.cray.software.justreminder.TasksActivity;
import com.cray.software.justreminder.async.GetExchangeTasksAsync;
import com.cray.software.justreminder.cloud.GTasksHelper;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.dialogs.CategoriesList;
import com.cray.software.justreminder.dialogs.PlacesList;
import com.cray.software.justreminder.dialogs.TemplatesList;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.modules.ManageModule;

public class NavigationDrawerFragment extends Fragment implements View.OnClickListener {

    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";
    private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";

    private NavigationDrawerCallbacks mCallbacks;
    private ActionBarDrawerToggle mDrawerToggle;

    private DrawerLayout mDrawerLayout;
    ImageView basket;
    TextView archiveScreen, activeScreen, fragmentSettings, geoScreen, calendar,
            manageBackup, notes, help, helpTranslate, googleTasks, moreApps, templates, places,
            categories;
    TextView appNameBanner;
    RelativeLayout ads_container;
    private View mFragmentContainerView;

    private String mCurrentSelectedPosition = "";
    private boolean mFromSavedInstanceState;
    private boolean mUserLearnedDrawer;

    public NavigationDrawerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mUserLearnedDrawer = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false);

        if (savedInstanceState != null) {
            mCurrentSelectedPosition = savedInstanceState.getString(STATE_SELECTED_POSITION);
            mFromSavedInstanceState = true;
        }

        // Select either the default item (0) or the last selected item.
        selectItem(ScreenManager.FRAGMENT_ACTIVE);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Indicate that this fragment would like to influence the set of actions in the action bar.
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_navigation_drawer, container, false);

        rootView.findViewById(R.id.drawerBg).setBackgroundColor(new ColorSetter(getActivity()).getBackgroundStyle());

        Typeface typeface = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Light.ttf");

        appNameBanner = (TextView) rootView.findViewById(R.id.appNameBanner);
        appNameBanner.setTypeface(typeface);
        if (new ManageModule().isPro()) {
            appNameBanner.setText(R.string.app_name_pro);
        } else {
            appNameBanner.setText(R.string.app_name);
        }

        fragmentSettings = (TextView) rootView.findViewById(R.id.fragmentSettings);
        fragmentSettings.setOnClickListener(this);

        geoScreen = (TextView) rootView.findViewById(R.id.geoScreen);
        geoScreen.setOnClickListener(this);

        manageBackup = (TextView) rootView.findViewById(R.id.manageBackup);
        manageBackup.setOnClickListener(this);

        calendar = (TextView) rootView.findViewById(R.id.calendar);
        calendar.setOnClickListener(this);

        notes = (TextView) rootView.findViewById(R.id.notes);
        notes.setOnClickListener(this);

        googleTasks = (TextView) rootView.findViewById(R.id.googleTasks);
        if (new GTasksHelper(getActivity()).isLinked()) googleTasks.setVisibility(View.VISIBLE);
        googleTasks.setOnClickListener(this);

        activeScreen = (TextView) rootView.findViewById(R.id.activeScreen);
        activeScreen.setOnClickListener(this);
        activeScreen.setEnabled(false);

        archiveScreen = (TextView) rootView.findViewById(R.id.archiveScreen);
        archiveScreen.setVisibility(View.VISIBLE);
        archiveScreen.setOnClickListener(this);

        help = (TextView) rootView.findViewById(R.id.help);
        help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectItem(ScreenManager.HELP);
            }
        });
        help.setTypeface(typeface);

        helpTranslate = (TextView) rootView.findViewById(R.id.helpTranslate);
        helpTranslate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectItem(ScreenManager.TRANSLATION);
            }
        });
        helpTranslate.setTypeface(typeface);

        moreApps = (TextView) rootView.findViewById(R.id.moreApps);
        moreApps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectItem(ScreenManager.MORE_APPS);
            }
        });
        moreApps.setTypeface(typeface);

        categories = (TextView) rootView.findViewById(R.id.categories);
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
                        startActivity(new Intent(getActivity(), CategoriesList.class));
                    }
                }, 250);
            }
        });
        categories.setTypeface(typeface);

        places = (TextView) rootView.findViewById(R.id.places);
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
                        startActivity(new Intent(getActivity(), PlacesList.class));
                    }
                }, 250);
            }
        });
        places.setTypeface(typeface);

        templates = (TextView) rootView.findViewById(R.id.templates);
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
                        startActivity(new Intent(getActivity(), TemplatesList.class));
                    }
                }, 250);
            }
        });
        templates.setTypeface(typeface);
        DataBase DB = new DataBase(getActivity());
        SharedPrefs sPrefs = new SharedPrefs(getActivity());
        if (!DB.isOpen()) DB.open();
        Cursor c = DB.queryTemplates();
        if (c != null && c.moveToFirst() && sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_QUICK_SMS)){
            templates.setVisibility(View.VISIBLE);
        }
        if (!DB.isOpen()) DB.open();
        c = DB.queryPlaces();
        if (c != null && c.moveToFirst()){
            places.setVisibility(View.VISIBLE);
        }
        if (c != null) c.close();

        fragmentSettings.setTypeface(typeface);
        geoScreen.setTypeface(typeface);
        calendar.setTypeface(typeface);
        notes.setTypeface(typeface);
        googleTasks.setTypeface(typeface);
        activeScreen.setTypeface(typeface);
        archiveScreen.setTypeface(typeface);
        manageBackup.setTypeface(typeface);
        googleTasks.setTypeface(typeface);


        if (!new ManageModule().isPro()){
            ads_container = (RelativeLayout) rootView.findViewById(R.id.ads_container);
            basket = (ImageView) rootView.findViewById(R.id.basket);
            if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_USE_DARK_THEME)){
                basket.setImageResource(R.drawable.market_icon_white);
            } else basket.setImageResource(R.drawable.market_icon);

            if (!isAppInstalled("com.cray.software.justreminderpro")){
                ads_container.setVisibility(View.VISIBLE);
                ads_container.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        selectItem(ScreenManager.MARKET);
                    }
                });
            }
        }

        loadMenu();
        return rootView;
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
        if (!prefs.loadBoolean(Constants.APP_UI_PREFERENCES_USE_DARK_THEME)){
            activeScreen.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_access_time_grey600_24dp, 0, 0, 0);
            archiveScreen.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_storage_grey600_24dp, 0, 0, 0);
            calendar.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_today_grey600_24dp, 0, 0, 0);
            geoScreen.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_navigation_grey600_24dp, 0, 0, 0);
            fragmentSettings.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_settings_grey600_24dp, 0, 0, 0);
            manageBackup.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_history_grey600_24dp, 0, 0, 0);
            notes.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_event_note_grey600_24dp, 0, 0, 0);
            googleTasks.setCompoundDrawablesWithIntrinsicBounds(R.drawable.google_tasks_grey, 0, 0, 0);
            templates.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_textsms_grey600_24dp, 0, 0, 0);
            places.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_location_on_grey600_24dp, 0, 0, 0);
            categories.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_local_offer_grey600_24dp, 0, 0, 0);
        } else {
            activeScreen.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_access_time_white_24dp, 0, 0, 0);
            archiveScreen.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_storage_white_24dp, 0, 0, 0);
            calendar.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_today_white_24dp, 0, 0, 0);
            geoScreen.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_navigation_white_24dp, 0, 0, 0);
            fragmentSettings.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_settings_white_24dp, 0, 0, 0);
            manageBackup.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_history_white_24dp, 0, 0, 0);
            notes.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_event_note_white_24dp, 0, 0, 0);
            googleTasks.setCompoundDrawablesWithIntrinsicBounds(R.drawable.google_tasks_white, 0, 0, 0);
            templates.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_textsms_white_24dp, 0, 0, 0);
            places.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_location_on_white_24dp, 0, 0, 0);
            categories.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_local_offer_white_24dp, 0, 0, 0);
        }
    }

    public boolean isDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mFragmentContainerView);
    }

    public void setUp(int fragmentId, DrawerLayout drawerLayout, Toolbar toolbar) {
        mFragmentContainerView = getActivity().findViewById(fragmentId);
        mDrawerLayout = drawerLayout;

        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

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

    private void selectItem(final String tag) {
        mCurrentSelectedPosition = tag;
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(mFragmentContainerView);
        }
        if (mCallbacks != null) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mCallbacks.onNavigationDrawerItemSelected(tag);
                }
            }, 250);
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
        super.onSaveInstanceState(outState);
        outState.putString(STATE_SELECTED_POSITION, mCurrentSelectedPosition);
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
                selectItem(ScreenManager.FRAGMENT_ACTIVE);
                break;
            case R.id.fragmentSettings:
                selectItem(ScreenManager.FRAGMENT_SETTINGS);
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
                        /*if (checkGooglePlayServicesAvailability()) {
                            startActivity(new Intent(MainActivity.this, GeolocationTasks.class));
                        }*/
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
                        startActivity(new Intent(getActivity(), NotesActivity.class));
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
                        startActivity(new Intent(getActivity(), TasksActivity.class));
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
                        startActivity(new Intent(getActivity(), CalendarActivity.class));
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
                        startActivity(new Intent(getActivity(), BackupManager.class));
                    }
                }, 250);
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPrefs sPrefs = new SharedPrefs(getActivity());
        if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_HIDE_TRANSLATION_MENU)) {
            helpTranslate.setVisibility(View.GONE);
        }

        DataBase DB = new DataBase(getActivity());
        if (!DB.isOpen()) DB.open();
        if (DB.getCountActive() > 0){
            if (isListFirstTime() && sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_THANKS_SHOWN)){
                startActivity(new Intent(getActivity(), HelpOverflow.class)
                        .putExtra(Constants.ITEM_ID_INTENT, 1));
            }
        }
    }

    private boolean isListFirstTime() {
        SharedPreferences preferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        boolean ranBefore = preferences.getBoolean("JustListBefore", false);
        if (!ranBefore) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("JustListBefore", true);
            editor.commit();
        }
        return !ranBefore;
    }


    public interface NavigationDrawerCallbacks {
        void onNavigationDrawerItemSelected(String tag);
    }
}
