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
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.cloud.GTasksHelper;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.constants.TasksConstants;
import com.cray.software.justreminder.databases.NextBase;
import com.cray.software.justreminder.google_tasks.SwitchTaskAsync;
import com.cray.software.justreminder.google_tasks.TaskItem;
import com.cray.software.justreminder.google_tasks.TaskListItem;
import com.cray.software.justreminder.google_tasks.TaskManager;
import com.cray.software.justreminder.google_tasks.TasksDataBase;
import com.cray.software.justreminder.google_tasks.TasksHelper;
import com.cray.software.justreminder.groups.GroupHelper;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.interfaces.ActionCallbacks;
import com.cray.software.justreminder.json.JPlace;
import com.cray.software.justreminder.modules.Module;
import com.cray.software.justreminder.notes.NoteHelper;
import com.cray.software.justreminder.notes.NoteItem;
import com.cray.software.justreminder.notes.NotePreview;
import com.cray.software.justreminder.roboto_views.RoboCheckBox;
import com.cray.software.justreminder.roboto_views.RoboSwitchCompat;
import com.cray.software.justreminder.roboto_views.RoboTextView;
import com.cray.software.justreminder.utils.IntervalUtil;
import com.cray.software.justreminder.utils.LocationUtil;
import com.cray.software.justreminder.utils.TimeUtil;
import com.cray.software.justreminder.utils.ViewUtils;
import com.cray.software.justreminder.views.CircularProgress;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class ReminderPreview extends AppCompatActivity implements ActionCallbacks {

    private RoboTextView statusText, time, location, group, type, number, repeat, melody;
    private RoboSwitchCompat statusSwitch;
    private LinearLayout tasksContainer, notesContainer, mapContainer, background;
    private RoboTextView listColor, taskText, taskNote, taskDate, noteText;
    private RoboCheckBox checkDone;
    private ImageView imageView;
    private CircularProgress progress;
    private Toolbar toolbar;
    private CollapsingToolbarLayout toolbarLayout;
    private FloatingActionButton mFab;
    private AppBarLayout appBarLayout;

    private long id;
    private ArrayList<Long> list;
    private ReminderModel item;
    private OnMapReadyCallback mMapCallback = googleMap -> {
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        showPinsOnMap(googleMap);
    };

    private void showPinsOnMap(GoogleMap map) {
        double[] place = item.getPlace();
        double lat = place[0];
        double lon = place[1];
        ColorSetter cs = new ColorSetter(this);
        if (lat != 0.0 && lon != 0.0) {
            if (item.getType().matches(Constants.TYPE_PLACES)) {
                location.setText(String.format(Locale.getDefault(),
                        "%.5f %.5f (%d)", lat, lon, item.getTotalPlaces()));
            } else {
                location.setText(lat + " " + lon);
            }
            mapContainer.setVisibility(View.VISIBLE);
            location.setVisibility(View.VISIBLE);
            ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMapAsync(mMapCallback);

            ArrayList<JPlace> places = item.getPlaces();
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
                int marker = item.getMarker();
                map.addMarker(new MarkerOptions()
                        .position(pos)
                        .title(item.getTitle())
                        .icon(BitmapDescriptorFactory.fromResource(cs.getMarkerStyle(marker)))
                        .draggable(false));
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 13));
                int radius = item.getRadius();
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
                    location.setText(mLocation + "\n" + "("
                            + String.format("%.5f", lat) + ", " +
                            String.format("%.5f", lon) + ")");
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ColorSetter cSetter = new ColorSetter(this);
        setTheme(cSetter.getStyle());
        if (Module.isLollipop()) {
            getWindow().setStatusBarColor(ViewUtils.getColor(this, cSetter.colorPrimaryDark()));
        }
        setContentView(R.layout.activity_reminder_preview);
        setRequestedOrientation(cSetter.getRequestOrientation());

        appBarLayout = (AppBarLayout) findViewById(R.id.app_bar);
        toolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setTitle("");

        id = getIntent().getLongExtra(Constants.EDIT_ID, 0);

        findViewById(R.id.windowBackground).setBackgroundColor(cSetter.getBackgroundStyle());

        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setBackgroundTintList(ViewUtils.getFabState(this, cSetter.colorAccent(), cSetter.colorAccent()));
        mFab.setOnClickListener(v -> {
            if (id != 0) {
                Reminder.edit(id, ReminderPreview.this);
            }
        });

        initViews();
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

        tasksContainer = (LinearLayout) findViewById(R.id.tasksContainer);
        tasksContainer.setVisibility(View.GONE);
        listColor = (RoboTextView) findViewById(R.id.listColor);
        taskText = (RoboTextView) findViewById(R.id.taskText);
        taskNote = (RoboTextView) findViewById(R.id.taskNote);
        taskDate = (RoboTextView) findViewById(R.id.taskDate);
        checkDone = (RoboCheckBox) findViewById(R.id.checkDone);

        notesContainer = (LinearLayout) findViewById(R.id.notesContainer);
        notesContainer.setVisibility(View.GONE);
        noteText = (RoboTextView) findViewById(R.id.noteText);
        imageView = (ImageView) findViewById(R.id.imageView);

        progress = (CircularProgress) findViewById(R.id.progress);

        background = (LinearLayout) findViewById(R.id.background);
        mapContainer = (LinearLayout) findViewById(R.id.mapContainer);
        mapContainer.setVisibility(View.GONE);

        if (new ColorSetter(this).isDark()) {
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
        item = ReminderDataProvider.getItem(this, id);
        if (item != null) {
            if (item.getCompleted() == 1) {
                statusSwitch.setChecked(false);
                statusText.setText(R.string.disabled);
            } else {
                statusSwitch.setChecked(true);
                statusText.setText(R.string.enabled4);
            }
            toolbar.setTitle(item.getTitle());
            type.setText(ReminderUtils.getTypeString(this, item.getType()));
            group.setText(GroupHelper.getInstance(this).getCategoryTitle(item.getGroupId()));

            long due = item.getDue();
            if (due > 0) {
                time.setText(TimeUtil.getFullDateTime(due, SharedPrefs.getInstance(this).getBoolean(Prefs.IS_24_TIME_FORMAT)));
                String repeatStr = IntervalUtil.getInterval(this, item.getRepeat());
                if (item.getType().startsWith(Constants.TYPE_WEEKDAY)) {
                    repeatStr = ReminderUtils.getRepeatString(this, item.getWeekdays());
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

            double[] place = item.getPlace();
            double lat = place[0];
            double lon = place[1];
            if (lat != 0.0 && lon != 0.0) {
                mapContainer.setVisibility(View.VISIBLE);
                location.setVisibility(View.VISIBLE);
                ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMapAsync(mMapCallback);
            } else {
                location.setVisibility(View.GONE);
                mapContainer.setVisibility(View.GONE);
            }
            String numberStr = item.getNumber();
            if (numberStr != null && !numberStr.matches("")) {
                number.setText(numberStr);
            } else {
                number.setVisibility(View.GONE);
            }

            String melodyStr = item.getMelody();
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

            int catColor = item.getCatColor();
            ColorSetter setter = new ColorSetter(this);
            int mColor = ViewUtils.getColor(this, setter.getCategoryColor(catColor));
            toolbar.setBackgroundColor(mColor);
            toolbarLayout.setBackgroundColor(mColor);
            toolbarLayout.setContentScrimColor(mColor);
            appBarLayout.setBackgroundColor(mColor);
            if (Module.isLollipop()) {
                getWindow().setStatusBarColor(setter.getNoteDarkColor(catColor));
            }
            mFab.setBackgroundTintList(ViewUtils.getFabState(this, setter.colorAccent(catColor),
                    setter.colorAccent(catColor)));

            new LoadOtherData(this, progress, id).execute();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_reminder_preview, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int ids = item.getItemId();

        if (ids == R.id.action_delete) {
            Reminder.moveToTrash(id, ReminderPreview.this, null);
            closeWindow();
            return true;
        }
        if (ids == android.R.id.home) {
            closeWindow();
        }
        if (ids == R.id.action_make_copy) {
            NextBase db = new NextBase(this);
            if (!db.isOpen()) {
                db.open();
            }
            Cursor c = db.getReminder(id);
            if (c != null && c.moveToFirst()) {
                String type = c.getString(c.getColumnIndex(NextBase.TYPE));
                if (!type.contains(Constants.TYPE_LOCATION) && !type.matches(Constants.TYPE_TIME)) {
                    showDialog();
                }
            }
            if (c != null) c.close();
            db.close();
        }

        return super.onOptionsItemSelected(item);
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
        Snackbar.make(mFab, message, Snackbar.LENGTH_LONG)
                .setAction(actionTitle, listener)
                .show();
    }

    @Override
    public void showSnackbar(int message) {
        Snackbar.make(mFab, message, Snackbar.LENGTH_LONG)
                .show();
    }

    public class LoadOtherData extends AsyncTask<Void, Void, ReminderNote> {

        private Context mContext;
        private CircularProgress mProgress;
        private long mId;
        private SimpleDateFormat full24Format = new SimpleDateFormat("EEE,\ndd/MM", Locale.getDefault());

        public LoadOtherData(Context context, CircularProgress circularProgress, long id) {
            this.mContext = context;
            this.mProgress = circularProgress;
            this.mId = id;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected ReminderNote doInBackground(Void... params) {
            ReminderNote reminderNote = new ReminderNote();
            NoteItem noteItem = NoteHelper.getInstance(mContext).getNoteByReminder(id);
            if (noteItem != null) {
                reminderNote.setNoteText(noteItem.getNote());
                reminderNote.setImage(noteItem.getImage());
                reminderNote.setNoteId(noteItem.getId());
            }
            TaskItem taskItem = TasksHelper.getInstance(mContext).getTaskByReminder(mId);
            if (taskItem != null) {
                reminderNote.setTaskTitle(taskItem.getTitle());
                reminderNote.setTaskNote(taskItem.getNotes());
                reminderNote.setTaskStatus(taskItem.getStatus());
                reminderNote.setTaskDate(taskItem.getDueDate());
                reminderNote.setTaskId(taskItem.getId());
                reminderNote.setTaskListId(taskItem.getListId());
                reminderNote.setTaskIdentifier(taskItem.getTaskId());
                TaskListItem listItem = TasksHelper.getInstance(mContext).getTaskList(taskItem.getListId());
                if (listItem != null) {
                    reminderNote.setColor(listItem.getColor());
                } else {
                    reminderNote.setColor(8);
                }
            }
            return reminderNote;
        }

        @Override
        protected void onPostExecute(final ReminderNote reminderNote) {
            super.onPostExecute(reminderNote);
            mProgress.setVisibility(View.GONE);
            if (reminderNote != null) {
                if (reminderNote.getTaskId() > 0) {
                    tasksContainer.setVisibility(View.VISIBLE);
                    taskText.setText(reminderNote.getTaskTitle());
                    String mNote = reminderNote.getTaskNote();
                    listColor.setBackgroundColor(new ColorSetter(mContext).getNoteColor(reminderNote.getColor()));
                    if (mNote != null && !mNote.matches("")) {
                        taskNote.setText(mNote);
                    } else {
                        taskNote.setVisibility(View.GONE);
                    }
                    long date = reminderNote.getTaskDate();
                    Calendar calendar = Calendar.getInstance();
                    if (date != 0) {
                        calendar.setTimeInMillis(date);
                        String update = full24Format.format(calendar.getTime());
                        taskDate.setText(update);
                    } else {
                        taskDate.setVisibility(View.INVISIBLE);
                    }
                    if (reminderNote.getTaskStatus().matches(GTasksHelper.TASKS_COMPLETE)) {
                        checkDone.setChecked(true);
                    } else {
                        checkDone.setChecked(false);
                    }
                    checkDone.setOnCheckedChangeListener((buttonView, isChecked) -> {
                        final TasksDataBase data = new TasksDataBase(mContext);
                        data.open();
                        if (isChecked) {
                            data.setTaskDone(reminderNote.getTaskId());
                        } else {
                            data.setTaskUnDone(reminderNote.getTaskId());
                        }

                        new SwitchTaskAsync(mContext, reminderNote.getTaskListId(),
                                reminderNote.getTaskIdentifier(), isChecked, null).execute();
                        loadInfo();
                    });
                    background.setOnClickListener(v -> mContext.startActivity(new Intent(mContext, TaskManager.class)
                            .putExtra(Constants.ITEM_ID_INTENT, reminderNote.getTaskId())
                            .putExtra(TasksConstants.INTENT_ACTION, TasksConstants.EDIT)));
                }
                if (reminderNote.getNoteId() > 0) {
                    notesContainer.setVisibility(View.VISIBLE);
                    String note = reminderNote.getNoteText();
                    if (SharedPrefs.getInstance(mContext).getBoolean(Prefs.NOTE_ENCRYPT)) {
                        note = SyncHelper.decrypt(note);
                    }
                    noteText.setText(note);
                    noteText.setOnClickListener(v -> openNote(reminderNote.getNoteId()));
                    byte[] image = reminderNote.getImage();
                    if (image != null) {
                        final Bitmap imgB = BitmapFactory.decodeByteArray(image, 0,
                                image.length);
                        imageView.setImageBitmap(imgB);
                        imageView.setOnClickListener(v -> openNote(reminderNote.getNoteId()));
                    } else imageView.setVisibility(View.GONE);
                }
            }
        }
    }

    private void openNote(long id) {
        if (Module.isLollipop()) {
            Intent intent = new Intent(this, NotePreview.class);
            intent.putExtra(Constants.EDIT_ID, id);
            String transitionName = "image";
            ActivityOptionsCompat options =
                    ActivityOptionsCompat.makeSceneTransitionAnimation(this, imageView,
                            transitionName);
            startActivity(intent, options.toBundle());
        } else {
            startActivity(new Intent(this, NotePreview.class)
                            .putExtra(Constants.EDIT_ID, id));
        }
    }

    class ReminderNote {
        private String noteText;
        private byte[] image;
        private int color;
        private String taskTitle, taskNote, taskStatus, taskListId, taskIdentifier;
        private long taskDate;
        private long noteId, taskId;

        public ReminderNote(){
        }

        public void setColor(int color) {
            this.color = color;
        }

        public int getColor() {
            return color;
        }

        public String getTaskIdentifier(){
            return taskIdentifier;
        }

        public void setTaskIdentifier(String taskIdentifier){
            this.taskIdentifier = taskIdentifier;
        }

        public String getTaskListId(){
            return taskListId;
        }

        public void setTaskListId(String taskListId){
            this.taskListId = taskListId;
        }

        public String getNoteText(){
            return noteText;
        }

        public void setNoteText(String noteText){
            this.noteText = noteText;
        }

        public byte[] getImage(){
            return image;
        }

        public void setImage(byte[] image){
            this.image = image;
        }

        public String getTaskTitle(){
            return taskTitle;
        }

        public void setTaskTitle(String taskTitle){
            this.taskTitle = taskTitle;
        }

        public String getTaskNote(){
            return taskNote;
        }

        public void setTaskNote(String taskNote){
            this.taskNote = taskNote;
        }

        public String getTaskStatus(){
            return taskStatus;
        }

        public void setTaskStatus(String taskStatus){
            this.taskStatus = taskStatus;
        }

        public long getTaskDate(){
            return taskDate;
        }

        public void setTaskDate(long taskDate){
            this.taskDate = taskDate;
        }

        public long getNoteId(){
            return noteId;
        }

        public void setNoteId(long noteId){
            this.noteId = noteId;
        }

        public long getTaskId(){
            return taskId;
        }

        public void setTaskId(long taskId){
            this.taskId = taskId;
        }
    }
}
