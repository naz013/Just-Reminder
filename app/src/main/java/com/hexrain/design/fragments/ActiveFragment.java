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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.ReminderManager;
import com.cray.software.justreminder.adapters.RemindersRecyclerAdapter;
import com.cray.software.justreminder.async.DisableAsync;
import com.cray.software.justreminder.async.SyncTask;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.datas.ReminderItem;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.Interval;
import com.cray.software.justreminder.helpers.Notifier;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.SyncHelper;
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
import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.animator.SwipeDismissItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.decoration.SimpleListDividerDecorator;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager;
import com.h6ah4i.android.widget.advrecyclerview.touchguard.RecyclerViewTouchActionGuardManager;
import com.hexrain.design.NavigationDrawerFragment;
import com.hexrain.design.ScreenManager;

import java.util.ArrayList;
import java.util.Calendar;

public class ActiveFragment extends Fragment implements SyncListener {

    RecyclerView currentList;
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
        View rootView = inflater.inflate(R.layout.fragment_screen_manager, container, false);

        cSetter = new ColorSetter(getActivity());
        sPrefs = new SharedPrefs(getActivity());

        emptyItem = (LinearLayout) rootView.findViewById(R.id.emptyItem);
        emptyItem.setVisibility(View.VISIBLE);

        emptyImage = (ImageView) rootView.findViewById(R.id.emptyImage);
        if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_USE_DARK_THEME)) {
            emptyImage.setImageResource(R.drawable.bell_icon);
        } else {
            emptyImage.setImageResource(R.drawable.bell_icon_dark);
        }

        currentList = (RecyclerView) rootView.findViewById(R.id.currentList);

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
        super.onResume();
        loaderAdapter(null);
    }

    private void makeArchive(long id) {
        DB = new DataBase(getActivity());
        if (!DB.isOpen()) DB.open();
        mNotifyMgr =
                (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        Integer i = (int) (long) id;
        mNotifyMgr.cancel(i);
        DB.toArchive(id);
        new DisableAsync(getActivity()).execute();
    }

    ArrayList<ReminderItem> arrayList;

    public void loaderAdapter(String categoryId) {
        DB = new DataBase(getActivity());
        if (!DB.isOpen()) DB.open();
        Cursor c;
        if (categoryId != null) {
            c = DB.queryGroup(categoryId);
        } else {
            c = DB.queryGroup();
        }

        SyncHelper helper = new SyncHelper(getActivity());

        arrayList = new ArrayList<>();

        if (c != null && c.moveToFirst()) {
            emptyItem.setVisibility(View.GONE);
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

                TimeCount mCount = new TimeCount(getActivity());
                Interval mInterval = new Interval(getActivity());
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

                arrayList.add(new ReminderItem(title, type, repeat, catId, uuId, isDone, due, mId,
                        new double[]{lat, lon}, number));
            } while (c.moveToNext());
        } else emptyItem.setVisibility(View.VISIBLE);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());

        // touch guard manager  (this class is required to suppress scrolling while swipe-dismiss animation is running)
        RecyclerViewTouchActionGuardManager mRecyclerViewTouchActionGuardManager =
                new RecyclerViewTouchActionGuardManager();
        mRecyclerViewTouchActionGuardManager.setInterceptVerticalScrollingWhileAnimationRunning(true);
        mRecyclerViewTouchActionGuardManager.setEnabled(true);

        // swipe manager
        RecyclerViewSwipeManager mRecyclerViewSwipeManager = new RecyclerViewSwipeManager();

        //adapter
        final RemindersRecyclerAdapter myItemAdapter =
                new RemindersRecyclerAdapter(getActivity(), arrayList);
        myItemAdapter.setEventListener(new RemindersRecyclerAdapter.EventListener() {
            @Override
            public void onItemRemoved(int position) {
                onRemovedEdit(position);
            }

            @Override
            public void onItemPinned(int position) {
                onPinnedDelete(position);
            }

            @Override
            public void onItemClicked(int position) {
                final long id = arrayList.get(position).getId();
                if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_ITEM_PREVIEW)) {
                    startActivity(new Intent(getActivity(), ReminderPreviewFragment.class)
                            .putExtra(Constants.EDIT_ID, id));
                } else {
                    toggle(id);
                }
            }

            @Override
            public void onItemLongClicked(int position) {
                final long id = arrayList.get(position).getId();
                editReminder(id);
            }

            @Override
            public void toggleItem(int position) {
                final long id = arrayList.get(position).getId();
                toggle(id);
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

        /*QuickReturnRecyclerViewOnScrollListener scrollListener = new
                QuickReturnRecyclerViewOnScrollListener.Builder(QuickReturnViewType.FOOTER)
                .footer(sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_EXTENDED_BUTTON) ? mainMenu : mFab)
                .minFooterTranslation(QuickReturnUtils.dp2px(getActivity(), 88))
                .listener(new CollapseListener() {
                    @Override
                    public void onStartScroll(boolean result) {
                        if (mainMenu.isExpanded()) mainMenu.collapse();
                    }
                })
                .isSnappable(true)
                .build();
        if (mWrappedAdapter.getItemCount() > 0) {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                currentList.addOnScrollListener(scrollListener);
            } else {
                currentList.setOnScrollListener(scrollListener);
            }
        }*/
    }

    public void onRemovedEdit(int position) {
        final long id = arrayList.get(position).getId();
        new AlarmReceiver().cancelAlarm(getActivity(), id);
        new WeekDayReceiver().cancelAlarm(getActivity(), id);
        new DelayReceiver().cancelAlarm(getActivity(), id);
        new PositionDelayReceiver().cancelDelay(getActivity(), id);
        editReminder(id);
    }

    public void onPinnedDelete(int position) {
        final long id = arrayList.get(position).getId();
        new AlarmReceiver().cancelAlarm(getActivity(), id);
        new WeekDayReceiver().cancelAlarm(getActivity(), id);
        new DelayReceiver().cancelAlarm(getActivity(), id);
        new PositionDelayReceiver().cancelDelay(getActivity(), id);
        disableReminder(id);
    }

    private void editReminder(long id) {
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

    private void disableReminder(long id) {
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
