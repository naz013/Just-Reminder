/*
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

package com.cray.software.justreminder.reminder;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.databinding.BindingAdapter;
import android.databinding.DataBindingUtil;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.cloud.GoogleTasks;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.constants.TasksConstants;
import com.cray.software.justreminder.databinding.ItemNoteLayoutBinding;
import com.cray.software.justreminder.databinding.ListItemTaskBinding;
import com.cray.software.justreminder.google_tasks.SwitchTaskAsync;
import com.cray.software.justreminder.google_tasks.TaskItem;
import com.cray.software.justreminder.google_tasks.TaskListItem;
import com.cray.software.justreminder.google_tasks.TaskManager;
import com.cray.software.justreminder.google_tasks.TasksHelper;
import com.cray.software.justreminder.groups.GroupHelper;
import com.cray.software.justreminder.groups.GroupItem;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.interfaces.ActionCallbacks;
import com.cray.software.justreminder.modules.Module;
import com.cray.software.justreminder.notes.NoteHelper;
import com.cray.software.justreminder.notes.NoteItem;
import com.cray.software.justreminder.notes.NotePreview;
import com.cray.software.justreminder.reminder.json.JPlace;
import com.cray.software.justreminder.reminder.json.JRecurrence;
import com.cray.software.justreminder.roboto_views.RoboCheckBox;
import com.cray.software.justreminder.roboto_views.RoboSwitchCompat;
import com.cray.software.justreminder.roboto_views.RoboTextView;
import com.cray.software.justreminder.utils.IntervalUtil;
import com.cray.software.justreminder.utils.LocationUtil;
import com.cray.software.justreminder.utils.TimeUtil;
import com.cray.software.justreminder.utils.ViewUtils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ReminderPreview extends AppCompatActivity implements ActionCallbacks {

    private RoboTextView statusText, time, location, group, type, number, repeat, melody;
    private RoboSwitchCompat statusSwitch;
    private LinearLayout mapContainer;
    private Toolbar toolbar;
    private LinearLayout mContainer;

    private long id;
    private ArrayList<Long> list;
    private ReminderItem item;
    private static NoteItem mNoteItem;
    private static TaskItem mTaskItem;

    private OnMapReadyCallback mMapCallback = googleMap -> {
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        showPinsOnMap(googleMap);
    };
    private ClickListener mClickListener = new ClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.noteClick:
                    if (mNoteItem != null) {
                        openNote(mNoteItem.getId());
                    }
                    break;
                case R.id.background:
                    if (mTaskItem != null) {
                        openTask(mTaskItem.getId());
                    }
                    break;
            }
        }
    };

    private void openTask(long id) {
        startActivity(new Intent(this, TaskManager.class).putExtra(Constants.ITEM_ID_INTENT, id)
                .putExtra(TasksConstants.INTENT_ACTION, TasksConstants.EDIT));
    }

    private void showPinsOnMap(GoogleMap map) {
        JPlace place = item.getModel().getPlace();
        double lat = place.getLatitude();
        double lon = place.getLongitude();
        ColorSetter cs = ColorSetter.getInstance(this);
        if (lat != 0.0 && lon != 0.0) {
            if (item.getType().matches(Constants.TYPE_PLACES)) {
                location.setText(String.format(Locale.getDefault(),
                        "%.5f %.5f (%d)", lat, lon, item.getModel().getPlaces().size()));
            } else {
                location.setText(lat + " " + lon);
            }
            mapContainer.setVisibility(View.VISIBLE);
            location.setVisibility(View.VISIBLE);
            ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMapAsync(mMapCallback);

            List<JPlace> places = item.getModel().getPlaces();
            if (places != null) {
                for (JPlace jPlace : places) {
                    LatLng pos = new LatLng(jPlace.getLatitude(), jPlace.getLongitude());
                    int marker = jPlace.getMarker();
                    map.addMarker(new MarkerOptions()
                            .position(pos)
                            .title(jPlace.getName())
                            .icon(BitmapDescriptorFactory.fromResource(cs.getMarkerStyle(marker)))
                            .draggable(false));
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 13));
                    int radius = jPlace.getRadius();
                    if (radius == -1) {
                        radius = SharedPrefs.getInstance(this).getInt(Prefs.LOCATION_RADIUS);
                    }
                    if (radius != -1) {
                        int[] circleColors = cs.getMarkerRadiusStyle(marker);
                        map.addCircle(new CircleOptions()
                                .center(pos)
                                .radius(radius)
                                .strokeWidth(3f)
                                .fillColor(ViewUtils.getColor(this, circleColors[0]))
                                .strokeColor(ViewUtils.getColor(this, circleColors[1])));
                    }
                }
            } else {
                LatLng pos = new LatLng(lat, lon);
                int marker = place.getMarker();
                map.addMarker(new MarkerOptions()
                        .position(pos)
                        .title(item.getSummary())
                        .icon(BitmapDescriptorFactory.fromResource(cs.getMarkerStyle(marker)))
                        .draggable(false));
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 13));
                int radius = place.getRadius();
                if (radius == -1) {
                    radius = SharedPrefs.getInstance(this).getInt(Prefs.LOCATION_RADIUS);
                }
                if (radius != -1) {
                    int[] circleColors = cs.getMarkerRadiusStyle(marker);
                    map.addCircle(new CircleOptions()
                            .center(pos)
                            .radius(radius)
                            .strokeWidth(3f)
                            .fillColor(ViewUtils.getColor(this, circleColors[0]))
                            .strokeColor(ViewUtils.getColor(this, circleColors[1])));
                }
                String mLocation = LocationUtil.getAddress(this, lat, lon);
                if (mLocation != null && !mLocation.matches("")) {
                    location.setText(mLocation + "\n" + "(" + String.format("%.5f", lat) + ", " +
                            String.format("%.5f", lon) + ")");
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ColorSetter cSetter = ColorSetter.getInstance(this);
        setTheme(cSetter.getStyle());
        if (Module.isLollipop()) {
            getWindow().setStatusBarColor(ViewUtils.getColor(this, cSetter.colorPrimaryDark()));
        }
        id = getIntent().getLongExtra(Constants.EDIT_ID, 0);
        setContentView(R.layout.activity_reminder_preview);
        setRequestedOrientation(cSetter.getRequestOrientation());
        mContainer = (LinearLayout) findViewById(R.id.dataContainer);
        initActionBar();
        findViewById(R.id.windowBackground).setBackgroundColor(cSetter.getBackgroundStyle());
        initViews();
    }

    private void initActionBar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setTitle("");
    }

    private void editReminder() {
        if (id != 0) {
            Reminder.edit(id, ReminderPreview.this);
        }
    }

    private void initViews() {
        RelativeLayout switchWrapper = (RelativeLayout) findViewById(R.id.switchWrapper);
        switchWrapper.setOnClickListener(v -> {
            Reminder.toggle(id, ReminderPreview.this, ReminderPreview.this);
            loadInfo();
        });

        statusText = (RoboTextView) findViewById(R.id.statusText);
        statusSwitch = (RoboSwitchCompat) findViewById(R.id.statusSwitch);
        if (Module.isLollipop()) statusSwitch.setTransitionName("toolbar");

        time = (RoboTextView) findViewById(R.id.time);
        location = (RoboTextView) findViewById(R.id.location);
        group = (RoboTextView) findViewById(R.id.group);
        type = (RoboTextView) findViewById(R.id.type);
        number = (RoboTextView) findViewById(R.id.number);
        repeat = (RoboTextView) findViewById(R.id.repeat);
        melody = (RoboTextView) findViewById(R.id.melody);
        mapContainer = (LinearLayout) findViewById(R.id.mapContainer);
        mapContainer.setVisibility(View.GONE);

        if (ColorSetter.getInstance(this).isDark()) {
            time.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_alarm_white_24dp, 0, 0, 0);
            type.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_assignment_white_24dp, 0, 0, 0);
            group.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_local_offer_white_24dp, 0, 0, 0);
            location.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_navigation_white_24dp, 0, 0, 0);
            number.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_account_circle_white_24dp, 0, 0, 0);
            repeat.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_refresh_white_24dp, 0, 0, 0);
            melody.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_music_note_white_24dp, 0, 0, 0);
        } else {
            time.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_alarm_black_24dp, 0, 0, 0);
            type.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_assignment_black_24dp, 0, 0, 0);
            group.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_local_offer_black_24dp, 0, 0, 0);
            location.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_navigation_black_24dp, 0, 0, 0);
            number.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_account_circle_black_24dp, 0, 0, 0);
            repeat.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_refresh_black_24dp, 0, 0, 0);
            melody.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_music_note_black_24dp, 0, 0, 0);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadInfo();
    }

    private void loadInfo() {
        item = ReminderHelper.getInstance(this).getReminder(id);
        if (item != null) {
            if (item.getStatus() == 1) {
                statusSwitch.setChecked(false);
                statusText.setText(R.string.disabled);
            } else {
                statusSwitch.setChecked(true);
                statusText.setText(R.string.enabled4);
            }
            toolbar.setTitle(item.getSummary());
            type.setText(ReminderUtils.getTypeString(this, item.getType()));
            group.setText(GroupHelper.getInstance(this).getCategoryTitle(item.getGroupUuId()));

            long due = item.getDateTime();
            if (due > 0) {
                JRecurrence recurrence = item.getModel().getRecurrence();
                time.setText(TimeUtil.getFullDateTime(due, SharedPrefs.getInstance(this).getBoolean(Prefs.IS_24_TIME_FORMAT)));
                String repeatStr = IntervalUtil.getInterval(this, recurrence.getRepeat());
                if (item.getType().startsWith(Constants.TYPE_WEEKDAY)) {
                    repeatStr = ReminderUtils.getRepeatString(this, recurrence.getWeekdays());
                }
                if (repeatStr != null) {
                    repeat.setText(repeatStr);
                } else {
                    repeat.setVisibility(View.GONE);
                }
            } else {
                time.setVisibility(View.GONE);
                repeat.setVisibility(View.GONE);
            }

            JPlace place = item.getModel().getPlace();
            double lat = place.getLatitude();
            double lon = place.getLongitude();
            if (lat != 0.0 && lon != 0.0) {
                mapContainer.setVisibility(View.VISIBLE);
                location.setVisibility(View.VISIBLE);
                ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMapAsync(mMapCallback);
            } else {
                location.setVisibility(View.GONE);
                mapContainer.setVisibility(View.GONE);
            }
            String numberStr = item.getModel().getAction().getTarget();
            if (numberStr != null && !numberStr.matches("")) {
                number.setText(numberStr);
            } else {
                number.setVisibility(View.GONE);
            }

            String melodyStr = item.getModel().getMelody().getMelodyPath();
            File file;
            if (melodyStr != null && !melodyStr.matches("")) {
                file = new File(melodyStr);
            } else {
                Uri soundUri;
                if (SharedPrefs.getInstance(this).getBoolean(Prefs.CUSTOM_SOUND)) {
                    String path = SharedPrefs.getInstance(this).getString(Prefs.CUSTOM_SOUND_FILE);
                    if (path != null) {
                        file = new File(path);
                    } else {
                        soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                        file = new File(soundUri.getPath());
                    }
                } else {
                    soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    file = new File(soundUri.getPath());
                }
            }

            melodyStr = file.getName();
            melody.setText(melodyStr);
            GroupItem group = GroupHelper.getInstance(this).getGroup(item.getGroupUuId());
            int catColor = 0;
            if (group != null) catColor = group.getColor();
            ColorSetter setter = ColorSetter.getInstance(this);
            int mColor = ViewUtils.getColor(this, setter.getCategoryColor(catColor));
            toolbar.setBackgroundColor(mColor);
            if (Module.isLollipop()) {
                getWindow().setStatusBarColor(setter.getNoteDarkColor(catColor));
            }
            mContainer.removeAllViewsInLayout();
            findNote();
            findTask();
        }
    }

    private static void setNote(NoteItem item) {
        mNoteItem = item;
    }

    private static void setTask(TaskItem item) {
        mTaskItem = item;
    }

    private void findNote() {
        new Thread(new Runnable() {
            NoteItem noteItem = NoteHelper.getInstance(ReminderPreview.this).getNoteByReminder(id);
            @Override
            public void run() {
                runOnUiThread(() -> {
                    if (noteItem != null) {
                        setNote(noteItem);
                        View noteView = DataBindingUtil.inflate(getLayoutInflater(), R.layout.item_note_layout, null, false).getRoot();
                        ItemNoteLayoutBinding binding = DataBindingUtil.bind(noteView);
                        binding.setNote(noteItem);
                        binding.setClick(mClickListener);
                        mContainer.addView(binding.getRoot());
                    }
                });
            }
        }).start();
    }

    private void findTask() {
        new Thread(new Runnable() {
            TaskItem taskItem = TasksHelper.getInstance(ReminderPreview.this).getTaskByReminder(id);
            @Override
            public void run() {
                runOnUiThread(() -> {
                    if (taskItem != null) {
                        setTask(taskItem);
                        View taskView = DataBindingUtil.inflate(getLayoutInflater(), R.layout.list_item_task, null, false).getRoot();
                        ListItemTaskBinding binding = DataBindingUtil.bind(taskView);
                        binding.setTask(taskItem);
                        binding.setClick(mClickListener);
                        mContainer.addView(binding.getRoot());
                    }
                });
            }
        }).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_reminder_preview, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int ids = item.getItemId();
        switch (ids) {
            case R.id.action_delete:
                Reminder.moveToTrash(id, ReminderPreview.this, null);
                closeWindow();
                return true;
            case android.R.id.home:
                closeWindow();
                return true;
            case R.id.action_make_copy:
                makeCopy();
                return true;
            case R.id.action_edit:
                editReminder();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void makeCopy() {
        ReminderItem item = ReminderHelper.getInstance(this).getReminder(id);
        if (item != null) {
            String type = item.getType();
            if (!type.contains(Constants.TYPE_LOCATION) && !type.matches(Constants.TYPE_TIME)) {
                showDialog();
            }
        }
    }

    private void closeWindow() {
        if (Module.isLollipop()) {
            finishAfterTransition();
        } else {
            finish();
        }
    }

    public void showDialog() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        int hour = 0;
        int minute = 0;
        list = new ArrayList<>();
        ArrayList<String> time = new ArrayList<>();
        boolean is24 = SharedPrefs.getInstance(this).getBoolean(Prefs.IS_24_TIME_FORMAT);
        do {
            if (hour == 23 && minute == 30) {
                hour = -1;
            } else {
                long tmp = calendar.getTimeInMillis();
                hour = calendar.get(Calendar.HOUR_OF_DAY);
                minute = calendar.get(Calendar.MINUTE);
                list.add(tmp);
                time.add(TimeUtil.getTime(calendar.getTime(), is24));
                calendar.setTimeInMillis(tmp + AlarmManager.INTERVAL_HALF_HOUR);
            }
        } while (hour != -1);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.choose_time);
        builder.setItems(time.toArray(new String[time.size()]), (dialog, which) -> {
            Reminder.copy(id, list.get(which), ReminderPreview.this, null);
            dialog.dismiss();
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void showSnackbar(int message, int actionTitle, View.OnClickListener listener) {
        Snackbar.make(mContainer, message, Snackbar.LENGTH_LONG)
                .setAction(actionTitle, listener)
                .show();
    }

    @Override
    public void showSnackbar(int message) {
        Snackbar.make(mContainer, message, Snackbar.LENGTH_LONG)
                .show();
    }

    private void openNote(long id) {
        startActivity(new Intent(this, NotePreview.class).putExtra(Constants.EDIT_ID, id));
    }

    @BindingAdapter({"loadMarker"})
    public static void loadMarker(View view, String listId) {
        TaskListItem listItem = TasksHelper.getInstance(view.getContext()).getTaskList(listId);
        if (listItem != null) {
            view.setBackgroundColor(ColorSetter.getInstance(view.getContext()).getNoteColor(listItem.getColor()));
        } else {
            view.setBackgroundColor(ColorSetter.getInstance(view.getContext()).getNoteColor(8));
        }
    }

    @BindingAdapter({"loadCheck"})
    public static void loadCheck(RoboCheckBox checkBox, TaskItem item) {
        if (item.getStatus().matches(GoogleTasks.TASKS_COMPLETE)){
            checkBox.setChecked(true);
        } else {
            checkBox.setChecked(false);
        }
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> switchTask(checkBox.getContext(),
                item.getId(), isChecked, item.getListId(), item.getTaskId()));
    }

    private static void switchTask(Context context, long id, boolean isDone, String listId, String taskId) {
        TasksHelper.getInstance(context).setStatus(id, isDone);
        new SwitchTaskAsync(context, listId, taskId, isDone, null).execute();
    }
}
