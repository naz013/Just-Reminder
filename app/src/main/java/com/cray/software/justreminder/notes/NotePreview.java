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

package com.cray.software.justreminder.notes;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.activities.ImagePreview;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.FileConfig;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.Messages;
import com.cray.software.justreminder.helpers.Notifier;
import com.cray.software.justreminder.helpers.Permissions;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.modules.Module;
import com.cray.software.justreminder.reminder.Reminder;
import com.cray.software.justreminder.reminder.ReminderHelper;
import com.cray.software.justreminder.reminder.ReminderItem;
import com.cray.software.justreminder.roboto_views.RoboButton;
import com.cray.software.justreminder.roboto_views.RoboTextView;
import com.cray.software.justreminder.utils.MemoryUtil;
import com.cray.software.justreminder.utils.QuickReturnUtils;
import com.cray.software.justreminder.utils.TimeUtil;
import com.cray.software.justreminder.utils.ViewUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

public class NotePreview extends AppCompatActivity {

    private static final int REQUEST_SD_CARD = 1122;
    private NoteItem mItem;
    private ColorSetter cSetter;
    private Bitmap img;
    private long mId;

    private Toolbar toolbar;
    private ScrollView scrollContent;
    private LinearLayout reminderContainer;
    private ImageView imageView;
    private RoboTextView reminderTime;
    private TextView noteText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Module.isLollipop()) {
            getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
        }
        super.onCreate(savedInstanceState);
        cSetter = ColorSetter.getInstance(this);
        setTheme(cSetter.getStyle());
        if (Module.isLollipop()) {
            getWindow().setStatusBarColor(ViewUtils.getColor(this, cSetter.colorPrimaryDark()));
        }
        mId = getIntent().getLongExtra(Constants.EDIT_ID, 0);
        setContentView(R.layout.activity_note_preview);
        setRequestedOrientation(cSetter.getRequestOrientation());
        initActionBar();
        findViewById(R.id.windowBackground).setBackgroundColor(cSetter.getBackgroundStyle());
        initScrollView();
        initImageView();
        noteText = (TextView) findViewById(R.id.noteText);
        initReminderCard();
    }

    private void initReminderCard() {
        reminderContainer = (LinearLayout) findViewById(R.id.reminderContainer);
        reminderContainer.setVisibility(View.GONE);
        reminderTime = (RoboTextView) findViewById(R.id.reminderTime);
        RoboButton editReminder = (RoboButton) findViewById(R.id.editReminder);
        editReminder.setOnClickListener(v -> {
            if (mItem.getLinkId() != 0) {
                Reminder.edit(mItem.getLinkId(), NotePreview.this);
            }
        });
        RoboButton deleteReminder = (RoboButton) findViewById(R.id.deleteReminder);
        deleteReminder.setOnClickListener(v -> {
            if (mItem.getLinkId() != 0) {
                showReminderDeleteDialog();
            }
        });
    }

    private void initImageView() {
        imageView = (ImageView) findViewById(R.id.imageView);
        if (Module.isLollipop()) imageView.setTransitionName("image");
        imageView.setOnClickListener(v -> {
            if (Permissions.checkPermission(NotePreview.this, Permissions.READ_EXTERNAL, Permissions.WRITE_EXTERNAL)) {
                openImage();
            } else {
                Permissions.requestPermission(NotePreview.this, REQUEST_SD_CARD, Permissions.READ_EXTERNAL, Permissions.WRITE_EXTERNAL);
            }
        });
    }

    private void initScrollView() {
        scrollContent = (ScrollView) findViewById(R.id.scrollContent);
        scrollContent.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                int scrollY = scrollContent.getScrollY();
                if (mItem.getImage() != null) {
                    toolbar.getBackground().setAlpha(getAlphaForActionBar(scrollY));
                } else {
                    toolbar.getBackground().setAlpha(255);
                }
            }

            private int getAlphaForActionBar(int scrollY) {
                int minDist = 0, maxDist = QuickReturnUtils.dp2px(NotePreview.this, 200);
                if (scrollY > maxDist) {
                    return 255;
                } else if (scrollY<minDist) {
                    return 0;
                } else {
                    return (int)  ((255.0 / maxDist) * scrollY);
                }
            }
        });
    }

    private void initActionBar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setTitle("");
        toolbar.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.action_share:
                    shareNote();
                    return true;
                case R.id.action_delete:
                    showDeleteDialog();
                    return true;
                case R.id.action_status:
                    moveToStatus();
                    return true;
                case R.id.action_edit:
                    editNote();
                    return true;
                default:
                    return false;
            }
        });
        toolbar.inflateMenu(R.menu.preview_note_menu);
    }

    private void editNote() {
        startActivity(new Intent(NotePreview.this, NotesManager.class)
                .putExtra(Constants.EDIT_ID, mItem.getId()));
    }

    private void openImage() {
        if (mItem.getImage() != null) {
            File path = MemoryUtil.getImageCacheDir();
            boolean isDirectory = true;
            if (!path.exists()) {
                isDirectory = path.mkdirs();
            }
            if (isDirectory) {
                String fileName = SyncHelper.generateID() + FileConfig.FILE_NAME_IMAGE;
                File f = new File(path + File.separator + fileName);
                boolean isFile = false;
                try {
                    isFile = f.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (isFile) {
                    FileOutputStream fo = null;
                    try {
                        fo = new FileOutputStream(f);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    if (fo != null) {
                        try {
                            fo.write(mItem.getImage());
                            fo.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        startActivity(new Intent(NotePreview.this, ImagePreview.class)
                                .putExtra("image", f.toString()));
                    }
                }
            }
        }
    }

    private void moveToStatus() {
        if (mItem != null){
            new Notifier(this).showNoteNotification((SharedPrefs.getInstance(this).getBoolean(Prefs.NOTE_ENCRYPT) ?
                            SyncHelper.decrypt(mItem.getNote()): mItem.getNote()), mItem.getId());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    @Override
    public void onBackPressed() {
        closeWindow();
    }

    private void loadData(){
        img = null;
        mItem = NoteHelper.getInstance(this).getNote(mId);
        if (mItem != null){
            showNote();
            showImage();
            showReminder();
        } else {
            finish();
        }
    }

    private void showNote() {
        String note = mItem.getNote();
        if (SharedPrefs.getInstance(this).getBoolean(Prefs.NOTE_ENCRYPT)){
            note = SyncHelper.decrypt(note);
        }
        noteText.setText(note);
        int color = mItem.getColor();
        int style = mItem.getStyle();
        noteText.setTypeface(cSetter.getTypeface(style));
        if (Module.isLollipop()) {
            getWindow().setStatusBarColor(cSetter.getNoteDarkColor(color));
        }
    }

    private void showReminder() {
        if (mItem.getLinkId() != 0){
            ReminderItem reminderItem = ReminderHelper.getInstance(this).getReminder(mItem.getLinkId());
            if (reminderItem != null){
                long feature = reminderItem.getDateTime();
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());
                if (feature != 0) {
                    calendar.setTimeInMillis(feature);
                }
                reminderTime.setText(TimeUtil.getDateTime(calendar.getTime(),
                        SharedPrefs.getInstance(this).getBoolean(Prefs.IS_24_TIME_FORMAT)));
                reminderContainer.setVisibility(View.VISIBLE);
            }
        }
    }

    private void showImage() {
        byte[] imageByte = mItem.getImage();
        if (imageByte != null){
            img = BitmapFactory.decodeByteArray(imageByte, 0, imageByte.length);
            imageView.setImageBitmap(img);
            imageView.setVisibility(View.VISIBLE);
            toolbar.setBackgroundColor(cSetter.getNoteColor(mItem.getColor()));
            toolbar.getBackground().setAlpha(0);
        } else {
            imageView.setVisibility(View.GONE);
            toolbar.setBackgroundColor(cSetter.getNoteColor(mItem.getColor()));
            toolbar.getBackground().setAlpha(255);
        }
    }

    private void shareNote(){
        if (!NoteHelper.getInstance(this).shareNote(mItem.getId())) {
            Messages.toast(this, getString(R.string.error_sending));
            closeWindow();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.preview_note_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                closeWindow();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void closeWindow() {
        if (Module.isLollipop()) {
            finishAfterTransition();
        } else {
            finish();
        }
    }

    private void showDeleteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.delete_this_note));
        builder.setPositiveButton(getString(R.string.yes), (dialog, which) -> {
            dialog.dismiss();
            deleteNote();
            closeWindow();
        });
        builder.setNegativeButton(getString(R.string.no), (dialog, which) -> {
            dialog.dismiss();
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showReminderDeleteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_this_reminder);
        builder.setPositiveButton(getString(R.string.yes), (dialog, which) -> {
            dialog.dismiss();
            Reminder.delete(mItem.getLinkId(), NotePreview.this);
            NoteHelper.getInstance(this).linkReminder(mItem.getId(), 0);
            reminderContainer.setVisibility(View.GONE);
        });
        builder.setNegativeButton(getString(R.string.no), (dialog, which) -> {
            dialog.dismiss();
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void deleteNote() {
        NoteHelper.getInstance(this).deleteNote(mItem.getId());
        SharedPrefs.getInstance(this).putBoolean("isNew", true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_SD_CARD:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openImage();
                }
                break;
        }
    }
}
