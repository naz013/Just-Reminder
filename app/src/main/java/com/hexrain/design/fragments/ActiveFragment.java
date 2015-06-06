package com.hexrain.design.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.ReminderManager;
import com.cray.software.justreminder.adapters.CustomCursorAdapter;
import com.cray.software.justreminder.async.DisableAsync;
import com.cray.software.justreminder.async.SyncTask;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.Notifier;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.TimeCount;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.interfaces.SyncListener;
import com.cray.software.justreminder.modules.ManageModule;
import com.cray.software.justreminder.services.AlarmReceiver;
import com.cray.software.justreminder.services.CheckPosition;
import com.cray.software.justreminder.services.DelayReceiver;
import com.cray.software.justreminder.services.GeolocationService;
import com.cray.software.justreminder.services.PositionDelayReceiver;
import com.cray.software.justreminder.services.RepeatNotificationReceiver;
import com.cray.software.justreminder.services.WeekDayReceiver;
import com.cray.software.justreminder.widgets.UpdatesHelper;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.hexrain.design.NavigationDrawerFragment;
import com.hexrain.design.ScreenManager;
import com.wdullaer.swipeactionadapter.SwipeActionAdapter;
import com.wdullaer.swipeactionadapter.SwipeDirections;

import java.util.ArrayList;
import java.util.Calendar;

public class ActiveFragment extends Fragment implements SyncListener {

    ListView currentList;
    LinearLayout emptyLayout, emptyItem;
    RelativeLayout ads_container;
    private AdView adView;
    ImageView emptyImage;

    DataBase DB;
    UpdatesHelper updatesHelper;
    AlarmReceiver alarm = new AlarmReceiver();
    NotificationManager mNotifyMgr;
    ColorSetter cSetter;
    SharedPrefs sPrefs;

    private NavigationDrawerFragment.NavigationDrawerCallbacks mCallbacks;

    public static ActiveFragment newInstance() {
        return new ActiveFragment();
    }

    public ActiveFragment() {
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Indicate that this fragment would like to influence the set of actions in the action bar.
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                startSync();
                return true;
            case R.id.action_voice:
                if (mCallbacks != null){
                    mCallbacks.onNavigationDrawerItemSelected(ScreenManager.VOICE_RECOGNIZER);
                }
                return true;
            case R.id.action_order:
                showDialog();
                return true;
            case R.id.action_filter:
                filterDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_with_listview, container, false);

        cSetter = new ColorSetter(getActivity());
        sPrefs = new SharedPrefs(getActivity());

        emptyItem = (LinearLayout) rootView.findViewById(R.id.emptyItem);
        emptyItem.setVisibility(View.VISIBLE);

        emptyImage = (ImageView) rootView.findViewById(R.id.emptyImage);
        if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_USE_DARK_THEME)) {
            emptyImage.setImageResource(R.drawable.ic_notifications_white_24dp);
        } else {
            emptyImage.setImageResource(R.drawable.ic_notifications_grey600_24dp);
        }

        currentList = (ListView) rootView.findViewById(R.id.currentList);
        currentList.setEmptyView(emptyItem);
        currentList.setItemsCanFocus(true);
        currentList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                sPrefs = new SharedPrefs(getActivity());
                if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_ITEM_PREVIEW)) {
                    startActivity(new Intent(getActivity(), ReminderPreviewFragment.class)
                            .putExtra(Constants.EDIT_ID, id));
                } else {
                    toggle(id);
                }
            }
        });
        currentList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                editReminder(id);
                return true;
            }
        });

        if (!new ManageModule().isPro()) {
            emptyLayout = (LinearLayout) rootView.findViewById(R.id.emptyLayout);
            emptyLayout.setVisibility(View.GONE);

            adView = (AdView) rootView.findViewById(R.id.adView);
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

            ads_container = (RelativeLayout) rootView.findViewById(R.id.ads_container);
        }
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
        ((ScreenManager)activity).onSectionAttached(ScreenManager.FRAGMENT_ACTIVE);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onResume() {
        if (!new ManageModule().isPro()){
            if (adView != null) {
                adView.resume();
            }
        }
        loaderAdapter(null);
        super.onResume();

    }

    @Override
    public void onDestroy() {
        if (!new ManageModule().isPro()) {
            if (adView != null) {
                adView.destroy();
            }
        }
        super.onDestroy();
    }

    @Override
    public void onPause() {
        if (!new ManageModule().isPro()) {
            if (adView != null) {
                adView.pause();
            }
        }
        super.onPause();
    }

    private void makeArchive(long id){
        DB = new DataBase(getActivity());
        if (!DB.isOpen()) DB.open();
        mNotifyMgr =
                (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        Integer i = (int) (long) id;
        mNotifyMgr.cancel(i);
        DB.toArchive(id);
        new DisableAsync(getActivity()).execute();
    }

    CustomCursorAdapter customAdapter;

    public void loaderAdapter(String categoryId){
        DB = new DataBase(getActivity());
        if (!DB.isOpen()) DB.open();
        if (categoryId != null) {
            customAdapter = new CustomCursorAdapter(getActivity(), DB.queryGroup(categoryId), this);
        } else {
            customAdapter = new CustomCursorAdapter(getActivity(), DB.queryGroup(), this);
        }
        final SwipeActionAdapter mAdapter = new SwipeActionAdapter(customAdapter);
        mAdapter.setListView(currentList);
        currentList.setAdapter(mAdapter);
        mAdapter.setFixedBackgrounds(true);
        mAdapter.addBackground(SwipeDirections.DIRECTION_NORMAL_LEFT, R.layout.swipe_delete_layout)
                .addBackground(SwipeDirections.DIRECTION_NORMAL_RIGHT, R.layout.swipe_edit_layout)
                .addBackground(SwipeDirections.DIRECTION_FAR_LEFT, R.layout.swipe_delete_layout)
                .addBackground(SwipeDirections.DIRECTION_FAR_RIGHT, R.layout.swipe_edit_layout);
        mAdapter.setSwipeActionListener(new SwipeActionAdapter.SwipeActionListener() {
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
                    DB = new DataBase(getActivity());
                    if (!DB.isOpen()) DB.open();
                    customAdapter = new CustomCursorAdapter(getActivity(),
                            DB.queryGroup(), null);
                    final long id = customAdapter.getItemId(position);
                    new AlarmReceiver().cancelAlarm(getActivity(), id);
                    new WeekDayReceiver().cancelAlarm(getActivity(), id);
                    new DelayReceiver().cancelAlarm(getActivity(), id);
                    new PositionDelayReceiver().cancelDelay(getActivity(), id);

                    switch (direction) {
                        case SwipeDirections.DIRECTION_NORMAL_LEFT:
                            disableReminder(id);
                            break;
                        case SwipeDirections.DIRECTION_FAR_LEFT:
                            disableReminder(id);
                            break;
                        case SwipeDirections.DIRECTION_NORMAL_RIGHT:
                            editReminder(id);
                            break;
                        case SwipeDirections.DIRECTION_FAR_RIGHT:
                            editReminder(id);
                            break;
                    }
                }
            }
        });

        if (mCallbacks != null) mCallbacks.onListChange(currentList);
    }

    private void editReminder(long id){
        Intent intentId = new Intent(getActivity(), ReminderManager.class);
        if (id != 0) {
            intentId.putExtra(Constants.EDIT_ID, id);
            alarm.cancelAlarm(getActivity(), id);
            new WeekDayReceiver().cancelAlarm(getActivity(), id);
            new DelayReceiver().cancelAlarm(getActivity(), id);
            new PositionDelayReceiver().cancelDelay(getActivity(), id);
            startActivity(intentId);
            new DisableAsync(getActivity()).execute();
        }
    }

    private void disableReminder(long id){
        if (id != 0) {
            makeArchive(id);
            updatesHelper = new UpdatesHelper(getActivity());
            updatesHelper.updateWidget();

            Toast.makeText(getActivity(),
                    getString(R.string.archived_result_message),
                    Toast.LENGTH_SHORT).show();

            loaderAdapter(null);
        }
    }

    private void toggle(long id) {
        DB = new DataBase(getActivity());
        if (!DB.isOpen()) DB.open();
        Cursor c = DB.getTask(id);
        if (c != null && c.moveToFirst()) {
            int done = c.getInt(c.getColumnIndex(Constants.COLUMN_IS_DONE));
            if (done == 0) {
                DB.setDone(id);
                AlarmReceiver alarm = new AlarmReceiver();
                WeekDayReceiver week = new WeekDayReceiver();
                DelayReceiver delayReceiver = new DelayReceiver();
                Integer i = (int) (long) id;
                alarm.cancelAlarm(getActivity(), i);
                week.cancelAlarm(getActivity(), i);
                delayReceiver.cancelAlarm(getActivity(), id);
                new RepeatNotificationReceiver().cancelAlarm(getActivity(), i);
                new PositionDelayReceiver().cancelDelay(getActivity(), i);
                NotificationManager mNotifyMgr =
                        (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
                mNotifyMgr.cancel(i);
                loaderAdapter(null);
                new DisableAsync(getActivity()).execute();
            } else {
                String type;
                int hour;
                int minute;
                int seconds;
                int day;
                int month;
                int year;
                int repCode;
                int repTime;
                int repCount;
                repCode = c.getInt(c.getColumnIndex(Constants.COLUMN_REPEAT));
                repCount = c.getInt(c.getColumnIndex(Constants.COLUMN_REMINDERS_COUNT));
                repTime = c.getInt(c.getColumnIndex(Constants.COLUMN_REMIND_TIME));
                day = c.getInt(c.getColumnIndex(Constants.COLUMN_DAY));
                month = c.getInt(c.getColumnIndex(Constants.COLUMN_MONTH));
                year = c.getInt(c.getColumnIndex(Constants.COLUMN_YEAR));
                hour = c.getInt(c.getColumnIndex(Constants.COLUMN_HOUR));
                minute = c.getInt(c.getColumnIndex(Constants.COLUMN_MINUTE));
                seconds = c.getInt(c.getColumnIndex(Constants.COLUMN_SECONDS));
                type = c.getString(c.getColumnIndex(Constants.COLUMN_TYPE));
                if (type.startsWith(Constants.TYPE_WEEKDAY)) {
                    DB.setUnDone(id);
                    DB.updateDateTime(id);
                    new WeekDayReceiver().setAlarm(getActivity(), id);
                    loaderAdapter(null);
                } else if (type.startsWith(Constants.TYPE_LOCATION)) {
                    DB.setUnDone(id);
                    DB.updateDateTime(id);
                    if (year == 0 && month == 0 && day == 0 && hour == 0 && minute == 0) {
                        getActivity().startService(new Intent(getActivity(), GeolocationService.class)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                        getActivity().startService(new Intent(getActivity(), CheckPosition.class)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                    } else {
                        new PositionDelayReceiver().setDelay(getActivity(), id);
                    }
                    loaderAdapter(null);
                } else {
                    if (type.matches(Constants.TYPE_TIME)) {
                        final Calendar calendar1 = Calendar.getInstance();
                        int myYear = calendar1.get(Calendar.YEAR);
                        int myMonth = calendar1.get(Calendar.MONTH);
                        int myDay = calendar1.get(Calendar.DAY_OF_MONTH);
                        int myHour = calendar1.get(Calendar.HOUR_OF_DAY);
                        int myMinute = calendar1.get(Calendar.MINUTE);
                        int mySeconds = calendar1.get(Calendar.SECOND);
                        DB.updateStartTime(id, myDay, myMonth, myYear, myHour, myMinute, mySeconds);
                        DB.updateDateTime(id);
                        new AlarmReceiver().setAlarm(getActivity(), id);
                        loaderAdapter(null);
                    } else {
                        if (new TimeCount(getActivity())
                                .getNextDate(year, month, day, hour, minute, seconds, repTime, repCode, repCount)) {
                            DB.setUnDone(id);
                            DB.updateDateTime(id);
                            new AlarmReceiver().setAlarm(getActivity(), id);
                            loaderAdapter(null);
                        } else {
                            Toast.makeText(getActivity(),
                                    getString(R.string.edit_reminder_toast),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
            new Notifier(getActivity()).recreatePermanent();
            new UpdatesHelper(getActivity()).updateWidget();
        }
        if (c != null) c.close();
    }

    ArrayList<String> ids;
    private void filterDialog(){
        ids = new ArrayList<>();
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                getActivity(),
                android.R.layout.select_dialog_item);
        DB = new DataBase(getActivity());
        if (DB != null) DB.open();
        else {
            DB = new DataBase(getActivity());
            DB.open();
        }
        arrayAdapter.add(getString(R.string.simple_all));
        Cursor c = DB.queryCategories();
        if (c != null && c.moveToFirst()){
            do {
                String title = c.getString(c.getColumnIndex(Constants.COLUMN_TEXT));
                String catId = c.getString(c.getColumnIndex(Constants.COLUMN_TECH_VAR));
                arrayAdapter.add(title);
                ids.add(catId);
            } while (c.moveToNext());
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.string_select_category));
        builder.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) loaderAdapter(null);
                else {
                    String catId = ids.get(which - 1);
                    loaderAdapter(catId);
                }
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void showDialog(){
        final CharSequence[] items = {getString(R.string.sort_item_by_date_a_z),
                getString(R.string.sort_item_by_date_z_a),
                getString(R.string.sort_item_by_date_without_a_z),
                getString(R.string.sort_item_by_date_without_z_a)};
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.menu_order_by));
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                SharedPrefs prefs = new SharedPrefs(getActivity());
                if (item == 0) {
                    prefs.savePrefs(Constants.APP_UI_PREFERENCES_LIST_ORDER, Constants.ORDER_DATE_A_Z);
                } else if (item == 1) {
                    prefs.savePrefs(Constants.APP_UI_PREFERENCES_LIST_ORDER, Constants.ORDER_DATE_Z_A);
                } else if (item == 2) {
                    prefs.savePrefs(Constants.APP_UI_PREFERENCES_LIST_ORDER, Constants.ORDER_DATE_WITHOUT_DISABLED_A_Z);
                } else if (item == 3) {
                    prefs.savePrefs(Constants.APP_UI_PREFERENCES_LIST_ORDER, Constants.ORDER_DATE_WITHOUT_DISABLED_Z_A);
                }
                dialog.dismiss();
                loaderAdapter(null);
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void startSync(){
        new SyncTask(getActivity(), this).execute();
    }

    @Override
    public void endExecution(boolean result) {
        loaderAdapter(null);
    }
}
