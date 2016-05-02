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

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cray.software.justreminder.NotesManager;
import com.cray.software.justreminder.R;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.FileConfig;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.databases.NextBase;
import com.cray.software.justreminder.databases.NotesBase;
import com.cray.software.justreminder.datas.models.NoteModel;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.Messages;
import com.cray.software.justreminder.helpers.Notifier;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.modules.Module;
import com.cray.software.justreminder.reminder.Reminder;
import com.cray.software.justreminder.utils.TimeUtil;
import com.cray.software.justreminder.utils.ViewUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

public class NotePreview extends AppCompatActivity {

    private long remId;
    private ColorSetter cSetter;
    private long mParam1;
    private Bitmap img;
    private byte[] imageByte;

    private Toolbar toolbar;
    private LinearLayout reminderContainer, buttonContainer;
    private ImageView imageView;
    private TextView noteText, reminderTime;
    private FloatingActionButton mFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Module.isLollipop()) {
            getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
        }
        super.onCreate(savedInstanceState);
        cSetter = new ColorSetter(this);
        setTheme(cSetter.getStyle());
        if (Module.isLollipop()) {
            getWindow().setStatusBarColor(ViewUtils.getColor(this, cSetter.colorPrimaryDark()));
        }
        setContentView(R.layout.activity_note_preview);
        setRequestedOrientation(cSetter.getRequestOrientation());

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setTitle("");
        toolbar.setOnMenuItemClickListener(
                item -> {
                    switch (item.getItemId()) {
                        case R.id.action_share:
                            shareNote();
                            return true;
                        case R.id.action_delete:
                            deleteDialog();
                            return true;
                        case R.id.action_status:
                            moveToStatus();
                            return true;
                        default:
                            return false;
                    }
                });

        toolbar.inflateMenu(R.menu.preview_note_menu);
        mParam1 = getIntent().getLongExtra(Constants.EDIT_ID, 0);
        findViewById(R.id.windowBackground).setBackgroundColor(cSetter.getBackgroundStyle());

        reminderContainer = (LinearLayout) findViewById(R.id.reminderContainer);
        reminderContainer.setVisibility(View.GONE);
        buttonContainer = (LinearLayout) findViewById(R.id.buttonContainer);
        buttonContainer.setVisibility(View.GONE);

        imageView = (ImageView) findViewById(R.id.imageView);
        if (Module.isLollipop()) imageView.setTransitionName("image");

        imageView.setOnClickListener(v -> openImage());
        noteText = (TextView) findViewById(R.id.noteText);
        reminderTime = (TextView) findViewById(R.id.reminderTime);
        reminderTime.setOnClickListener(v -> {
            if (buttonContainer.getVisibility() == View.VISIBLE) {
                ViewUtils.collapse(buttonContainer);
            } else {
                ViewUtils.expand(buttonContainer);
            }
        });

        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setVisibility(View.GONE);

        mFab.setOnClickListener(v -> startActivity(new Intent(NotePreview.this, NotesManager.class)
                .putExtra(Constants.EDIT_ID, mParam1)));

        Button editReminder = (Button) findViewById(R.id.editReminder);
        editReminder.setOnClickListener(v -> {
            if (remId != 0) {
                Reminder.edit(remId, NotePreview.this);
            }
        });
        Button deleteReminder = (Button) findViewById(R.id.deleteReminder);
        deleteReminder.setOnClickListener(v -> {
            if (remId != 0) {
                Reminder.delete(remId, NotePreview.this);
            }
        });

        new Handler().postDelayed(() -> ViewUtils.show(NotePreview.this, mFab), 500);
    }

    private void openImage() {
        if (imageByte != null) {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            Bitmap _bitmapScaled = img;
            _bitmapScaled.compress(Bitmap.CompressFormat.JPEG, 100, bytes);

            byte[] image = bytes.toByteArray();
            if (image == null) {
                return;
            }

            File sdPath = Environment.getExternalStorageDirectory();
            File sdPathDr = new File(sdPath.toString() + "/JustReminder/" + Constants.DIR_IMAGE_CACHE);
            boolean isDirectory = false;
            if (!sdPathDr.exists()) {
                isDirectory = sdPathDr.mkdirs();
            }
            if (isDirectory) {
                String fileName = SyncHelper.generateID() + FileConfig.FILE_NAME_IMAGE;
                File f = new File(sdPathDr + File.separator + fileName);
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
                            fo.write(image);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
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
        NotesBase base = new NotesBase(this);
        if (!base.isOpen()) {
            base.open();
        }
        Cursor c = base.getNote(mParam1);
        SharedPrefs sPrefs = new SharedPrefs(NotePreview.this);
        if (c != null && c.moveToFirst()){
            new Notifier(this)
                    .showNoteNotification((sPrefs.loadBoolean(Prefs.NOTE_ENCRYPT) ?
                            SyncHelper.decrypt(c.getString(c.getColumnIndex(Constants.COLUMN_NOTE))):
                            c.getString(c.getColumnIndex(Constants.COLUMN_NOTE))), mParam1);
        }
        if (c != null) {
            c.close();
        }
        base.close();
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
        NotesBase base = new NotesBase(NotePreview.this);
        base.open();
        imageByte = null;
        img = null;
        Cursor c = base.getNote(mParam1);
        if (c != null && c.moveToFirst()){
            String note = c.getString(c.getColumnIndex(Constants.COLUMN_NOTE));
            SharedPrefs sPrefs = new SharedPrefs(NotePreview.this);
            if (sPrefs.loadBoolean(Prefs.NOTE_ENCRYPT)){
                note = SyncHelper.decrypt(note);
            }
            noteText.setText(note);
            toolbar.setTitle(note);
            int color = c.getInt(c.getColumnIndex(Constants.COLUMN_COLOR));
            int style = c.getInt(c.getColumnIndex(Constants.COLUMN_FONT_STYLE));
            remId = c.getLong(c.getColumnIndex(Constants.COLUMN_LINK_ID));
            imageByte = c.getBlob(c.getColumnIndex(Constants.COLUMN_IMAGE));
            noteText.setTypeface(cSetter.getTypeface(style));
            if (Module.isLollipop()) {
                getWindow().setStatusBarColor(cSetter.getNoteDarkColor(color));
            }

            mFab.setBackgroundTintList(ViewUtils.getFabState(this, cSetter.colorAccent(color), cSetter.colorAccent(color)));

            int noteColor = cSetter.getNoteColor(color);
            if (imageByte != null){
                Bitmap imgB = BitmapFactory.decodeByteArray(imageByte, 0,
                        imageByte.length);
                img = imgB;
                imageView.setImageBitmap(imgB);
            }
            imageView.setBackgroundColor(noteColor);
            toolbar.setBackgroundColor(noteColor);

            if (remId != 0){
                NextBase dataBase = new NextBase(NotePreview.this);
                dataBase.open();
                Cursor r = dataBase.getReminder(remId);
                if (r != null && r.moveToFirst()){
                    long feature = r.getLong(r.getColumnIndex(NextBase.EVENT_TIME));
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(System.currentTimeMillis());
                    if (feature != 0) {
                        calendar.setTimeInMillis(feature);
                    }

                    reminderTime.setText(TimeUtil.getDateTime(calendar.getTime(),
                            sPrefs.loadBoolean(Prefs.IS_24_TIME_FORMAT)));
                    reminderContainer.setVisibility(View.VISIBLE);
                }
                if (r != null) r.close();
                dataBase.close();
            }
        }
        if (c != null) c.close();
        base.close();
    }

    private void shareNote(){
        if (!NoteModel.shareNote(mParam1, this)) {
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
        new Handler().post(() -> {
            Animation slide = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.scale_zoom_out);
            mFab.startAnimation(slide);
            mFab.setVisibility(View.GONE);
        });
        new Handler().postDelayed(() -> {
            if (Module.isLollipop()) {
                finishAfterTransition();
            } else {
                finish();
            }
        }, 300);
    }

    private void deleteDialog() {
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

    private void deleteNote() {
        NoteModel.deleteNote(mParam1, this, null);
        new SharedPrefs(this).saveBoolean("isNew", true);
    }
}
