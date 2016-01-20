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

package com.cray.software.justreminder.activities;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.TaskManager;
import com.cray.software.justreminder.async.SwitchTaskAsync;
import com.cray.software.justreminder.cloud.GTasksHelper;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.constants.TasksConstants;
import com.cray.software.justreminder.databases.NextBase;
import com.cray.software.justreminder.databases.NotesBase;
import com.cray.software.justreminder.databases.TasksData;
import com.cray.software.justreminder.datas.models.CategoryModel;
import com.cray.software.justreminder.datas.models.ReminderModel;
import com.cray.software.justreminder.datas.models.ReminderNote;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.interfaces.ActionCallbacks;
import com.cray.software.justreminder.modules.Module;
import com.cray.software.justreminder.reminder.Reminder;
import com.cray.software.justreminder.reminder.ReminderDataProvider;
import com.cray.software.justreminder.reminder.ReminderUtils;
import com.cray.software.justreminder.utils.IntervalUtil;
import com.cray.software.justreminder.utils.LocationUtil;
import com.cray.software.justreminder.utils.TimeUtil;
import com.cray.software.justreminder.utils.ViewUtils;
import com.cray.software.justreminder.views.CircularProgress;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
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

    private SharedPrefs sPrefs;

    private TextView statusText, time, location, group, type, number, repeat, melody;
    private SwitchCompat statusSwitch;
    private LinearLayout tasksContainer, notesContainer, mapContainer, background;
    private TextView listColor, taskText, taskNote, taskDate, noteText;
    private CheckBox checkDone;
    private ImageView imageView;
    private CircularProgress progress;
    private Toolbar toolbar;
    private CollapsingToolbarLayout toolbarLayout;
    private FloatingActionButton mFab;
    private AppBarLayout appBarLayout;

    private long id;
    private ArrayList<Long> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ColorSetter cSetter = new ColorSetter(this);
        setTheme(cSetter.getStyle());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ViewUtils.getColor(this, cSetter.colorPrimaryDark()));
        }
        setContentView(R.layout.activity_reminder_preview);
        setRequestedOrientation(cSetter.getRequestOrientation());

        appBarLayout = (AppBarLayout) findViewById(R.id.app_bar);
        toolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.drawable.ic_clear_white_24dp);
        toolbar.setTitle("");

        sPrefs = new SharedPrefs(this);

        id = getIntent().getLongExtra(Constants.EDIT_ID, 0);

        findViewById(R.id.windowBackground).setBackgroundColor(cSetter.getBackgroundStyle());

        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setBackgroundTintList(ViewUtils.getFabState(this, cSetter.colorAccent(), cSetter.colorAccent()));
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (id != 0) {
                    Reminder.edit(id, ReminderPreview.this);
                }
            }
        });

        initViews();
    }

    private void initViews() {
        RelativeLayout switchWrapper = (RelativeLayout) findViewById(R.id.switchWrapper);
        switchWrapper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Reminder.toggle(id, ReminderPreview.this, ReminderPreview.this);
                loadInfo();
            }
        });

        statusText = (TextView) findViewById(R.id.statusText);
        statusSwitch = (SwitchCompat) findViewById(R.id.statusSwitch);
        if (Module.isLollipop()) statusSwitch.setTransitionName("toolbar");

        time = (TextView) findViewById(R.id.time);
        location = (TextView) findViewById(R.id.location);
        group = (TextView) findViewById(R.id.group);
        type = (TextView) findViewById(R.id.type);
        number = (TextView) findViewById(R.id.number);
        repeat = (TextView) findViewById(R.id.repeat);
        melody = (TextView) findViewById(R.id.melody);

        tasksContainer = (LinearLayout) findViewById(R.id.tasksContainer);
        tasksContainer.setVisibility(View.GONE);
        listColor = (TextView) findViewById(R.id.listColor);
        taskText = (TextView) findViewById(R.id.taskText);
        taskNote = (TextView) findViewById(R.id.taskNote);
        taskDate = (TextView) findViewById(R.id.taskDate);
        checkDone = (CheckBox) findViewById(R.id.checkDone);

        notesContainer = (LinearLayout) findViewById(R.id.notesContainer);
        notesContainer.setVisibility(View.GONE);
        noteText = (TextView) findViewById(R.id.noteText);
        imageView = (ImageView) findViewById(R.id.imageView);

        progress = (CircularProgress) findViewById(R.id.progress);

        background = (LinearLayout) findViewById(R.id.background);
        mapContainer = (LinearLayout) findViewById(R.id.mapContainer);
        mapContainer.setVisibility(View.GONE);

        if (sPrefs.loadBoolean(Prefs.USE_DARK_THEME)) {
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
        ReminderModel item = ReminderDataProvider.getItem(this, id);
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
            group.setText(CategoryModel.getCategoryTitle(this, item.getGroupId()));

            long due = item.getDue();
            if (due > 0) {
                time.setText(TimeUtil.getFullDateTime(due, new SharedPrefs(this).loadBoolean(Prefs.IS_24_TIME_FORMAT)));
                String repeatStr = IntervalUtil.getInterval(this, item.getRepeat());
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
                location.setText(lat + "\n" + lon);
                mapContainer.setVisibility(View.VISIBLE);
                location.setVisibility(View.VISIBLE);
                GoogleMap map = ((SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.map)).getMap();
                map.getUiSettings().setMyLocationButtonEnabled(false);

                LatLng pos = new LatLng(lat, lon);
                ColorSetter cs = new ColorSetter(this);
                map.addMarker(new MarkerOptions()
                        .position(pos)
                        .title(item.getTitle())
                        .icon(BitmapDescriptorFactory.fromResource(cs.getMarkerStyle()))
                        .draggable(false));
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 13));
                int radius = item.getRadius();
                if (radius == -1) {
                    radius = new SharedPrefs(this).loadInt(Prefs.LOCATION_RADIUS);
                }
                if (radius != -1) {
                    int[] circleColors = cs.getMarkerRadiusStyle();
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
                if (new SharedPrefs(this).loadBoolean(Prefs.CUSTOM_SOUND)) {
                    String path = new SharedPrefs(this).loadPrefs(Prefs.CUSTOM_SOUND_FILE);
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                finishAfterTransition();
            } else {
                finish();
            }
            return true;
        }
        if (ids == android.R.id.home) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                finishAfterTransition();
            } else {
                finish();
            }
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
        boolean is24 = new SharedPrefs(this).loadBoolean(Prefs.IS_24_TIME_FORMAT);
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
        builder.setItems(time.toArray(new String[time.size()]), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                Reminder.copy(id, list.get(item), ReminderPreview.this, null);
                dialog.dismiss();
            }
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

        Context mContext;
        CircularProgress mProgress;
        long mId;
        SimpleDateFormat full24Format = new SimpleDateFormat("EEE,\ndd/MM", Locale.getDefault());

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
            NotesBase notesBase = new NotesBase(mContext);
            notesBase.open();
            ReminderNote reminderNote = new ReminderNote();
            Cursor c = notesBase.getNoteByReminder(mId);
            if (c != null && c.moveToFirst()) {
                String note = c.getString(c.getColumnIndex(Constants.COLUMN_NOTE));
                byte[] image = c.getBlob(c.getColumnIndex(Constants.COLUMN_IMAGE));
                long noteId = c.getLong(c.getColumnIndex(Constants.COLUMN_ID));
                reminderNote.setNoteText(note);
                reminderNote.setImage(image);
                reminderNote.setNoteId(noteId);
            }
            if (c != null) {
                c.close();
            }
            notesBase.close();

            TasksData data = new TasksData(mContext);
            data.open();
            c = data.getTaskByReminder(mId);
            if (c != null && c.moveToFirst()) {
                String task = c.getString(c.getColumnIndex(TasksConstants.COLUMN_TITLE));
                String taskNote = c.getString(c.getColumnIndex(TasksConstants.COLUMN_NOTES));
                String status = c.getString(c.getColumnIndex(TasksConstants.COLUMN_STATUS));
                String taskListId = c.getString(c.getColumnIndex(TasksConstants.COLUMN_LIST_ID));
                String taskId = c.getString(c.getColumnIndex(TasksConstants.COLUMN_TASK_ID));
                long due = c.getLong(c.getColumnIndex(TasksConstants.COLUMN_DUE));
                long id = c.getLong(c.getColumnIndex(TasksConstants.COLUMN_ID));
                reminderNote.setTaskTitle(task);
                reminderNote.setTaskNote(taskNote);
                reminderNote.setTaskStatus(status);
                reminderNote.setTaskDate(due);
                reminderNote.setTaskId(id);
                reminderNote.setTaskListId(taskListId);
                reminderNote.setTaskIdentifier(taskId);
            }
            if (c != null) c.close();

            c = data.getTasksList(reminderNote.getTaskListId());
            if (c != null && c.moveToFirst()) {
                reminderNote.setColor(c.getInt(c.getColumnIndex(TasksConstants.COLUMN_COLOR)));
            } else {
                reminderNote.setColor(8);
            }
            if (c != null) c.close();
            data.close();
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
                    listColor.setBackgroundColor(new ColorSetter(mContext)
                            .getNoteColor(reminderNote.getColor()));

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
                    checkDone.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            final TasksData data = new TasksData(mContext);
                            data.open();
                            if (isChecked) {
                                data.setTaskDone(reminderNote.getTaskId());
                            } else {
                                data.setTaskUnDone(reminderNote.getTaskId());
                            }

                            new SwitchTaskAsync(mContext, reminderNote.getTaskListId(),
                                    reminderNote.getTaskIdentifier(), isChecked, null).execute();
                            loadInfo();
                        }
                    });
                    background.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mContext.startActivity(new Intent(mContext, TaskManager.class)
                                    .putExtra(Constants.ITEM_ID_INTENT, reminderNote.getTaskId())
                                    .putExtra(TasksConstants.INTENT_ACTION, TasksConstants.EDIT));
                        }
                    });
                }

                if (reminderNote.getNoteId() > 0) {
                    notesContainer.setVisibility(View.VISIBLE);
                    String note = reminderNote.getNoteText();
                    if (new SharedPrefs(mContext).loadBoolean(Prefs.NOTE_ENCRYPT)) {
                        note = SyncHelper.decrypt(note);
                    }
                    noteText.setText(note);
                    noteText.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            openNote(reminderNote.getNoteId());
                        }
                    });
                    byte[] image = reminderNote.getImage();
                    if (image != null) {
                        final Bitmap imgB = BitmapFactory.decodeByteArray(image, 0,
                                image.length);
                        imageView.setImageBitmap(imgB);
                        imageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                openNote(reminderNote.getNoteId());
                            }
                        });
                    } else imageView.setVisibility(View.GONE);
                }
            }
        }
    }

    private void openNote(long id) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
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
}
