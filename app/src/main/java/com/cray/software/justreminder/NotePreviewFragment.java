package com.cray.software.justreminder;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.databases.NotesBase;
import com.cray.software.justreminder.datas.models.NoteModel;
import com.cray.software.justreminder.activities.ImagePreview;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.Messages;
import com.cray.software.justreminder.helpers.Notifier;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.reminder.Reminder;
import com.cray.software.justreminder.utils.QuickReturnUtils;
import com.cray.software.justreminder.utils.TimeUtil;
import com.cray.software.justreminder.utils.ViewUtils;
import com.getbase.floatingactionbutton.FloatingActionButton;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

public class NotePreviewFragment extends AppCompatActivity {

    private long remId;
    private ColorSetter cSetter;
    private SharedPrefs sPrefs;
    private Toolbar toolbar;
    private long mParam1;
    private Bitmap img;
    private byte[] imageByte;
    private float prevPercent;

    private ScrollView scrollContent;
    private LinearLayout reminderContainer, buttonContainer;
    private ImageView imageView;
    private TextView noteText, reminderTime;
    private FloatingActionButton mFab;
    private NotesBase base;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
        }
        super.onCreate(savedInstanceState);
        cSetter = new ColorSetter(NotePreviewFragment.this);
        setTheme(cSetter.getStyle());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(cSetter.colorPrimaryDark());
        }
        setContentView(R.layout.fragment_note_preview);
        setRequestedOrientation(cSetter.getRequestOrientation());

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationIcon(R.drawable.ic_clear_white_24dp);
        toolbar.setTitle("");
        toolbar.setOnMenuItemClickListener(
                new Toolbar.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
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
                    }
                });

        toolbar.inflateMenu(R.menu.preview_note_menu);

        sPrefs = new SharedPrefs(NotePreviewFragment.this);

        mParam1 = getIntent().getLongExtra(Constants.EDIT_ID, 0);

        findViewById(R.id.windowBackground).setBackgroundColor(cSetter.getBackgroundStyle());

        scrollContent = (ScrollView) findViewById(R.id.scrollContent);
        scrollContent.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {

            @Override
            public void onScrollChanged() {
                int scrollY = scrollContent.getScrollY();
                if (imageByte != null) {
                    toolbar.getBackground().setAlpha(getAlphaforActionBar(scrollY));
                } else {
                    toolbar.getBackground().setAlpha(255);
                }
                float percent = getCurrentPercent(scrollY);
                if (percent >= 60.0 && prevPercent < 60.0){
                    ViewUtils.hide(NotePreviewFragment.this, mFab);
                }
                if (percent <= 75.0 && prevPercent > 75.0){
                    ViewUtils.show(NotePreviewFragment.this, mFab);
                }
                prevPercent = percent;
            }

            private float getCurrentPercent(int scrollY){
                int maxDist = QuickReturnUtils.dp2px(NotePreviewFragment.this, 200);
                return (((float)scrollY / (float)maxDist) * 100.0f);
            }

            private int getAlphaforActionBar(int scrollY) {
                int minDist = 0, maxDist = QuickReturnUtils.dp2px(NotePreviewFragment.this, 200);
                if (scrollY > maxDist) {
                    return 255;
                } else if (scrollY<minDist) {
                    return 0;
                } else {
                    return (int)  ((255.0 / maxDist) * scrollY);
                }
            }
        });

        reminderContainer = (LinearLayout) findViewById(R.id.reminderContainer);
        reminderContainer.setVisibility(View.GONE);
        buttonContainer = (LinearLayout) findViewById(R.id.buttonContainer);
        buttonContainer.setVisibility(View.GONE);

        imageView = (ImageView) findViewById(R.id.imageView);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                        String fileName = SyncHelper.generateID() + Constants.FILE_NAME_IMAGE;
                        File f = new File(sdPathDr
                                + File.separator + fileName);
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

                                startActivity(new Intent(NotePreviewFragment.this, ImagePreview.class)
                                        .putExtra("image", f.toString()));
                            }
                        }
                    }
                }
            }
        });
        noteText = (TextView) findViewById(R.id.noteText);
        reminderTime = (TextView) findViewById(R.id.reminderTime);
        reminderTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (buttonContainer.getVisibility() == View.VISIBLE) {
                    ViewUtils.collapse(buttonContainer);
                } else {
                    ViewUtils.expand(buttonContainer);
                    scrollContent.fullScroll(View.FOCUS_DOWN);
                }
            }
        });

        mFab = new FloatingActionButton(NotePreviewFragment.this);
        mFab.setSize(FloatingActionButton.SIZE_NORMAL);
        mFab.setIcon(R.drawable.ic_create_white_24dp);
        mFab.setVisibility(View.GONE);

        RelativeLayout wrapper = (RelativeLayout) findViewById(R.id.container);
        wrapper.addView(mFab);

        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(NotePreviewFragment.this, NotesManager.class)
                        .putExtra(Constants.EDIT_ID, mParam1));
            }
        });

        Button editReminder = (Button) findViewById(R.id.editReminder);
        editReminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (remId != 0) {
                    Reminder.edit(remId, NotePreviewFragment.this);
                }
            }
        });
        Button deleteReminder = (Button) findViewById(R.id.deleteReminder);
        deleteReminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (remId != 0) {
                    Reminder.delete(remId, NotePreviewFragment.this);
                }
            }
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ViewUtils.show(NotePreviewFragment.this, mFab);
            }
        }, 500);
    }

    private void moveToStatus() {
        base = new NotesBase(this);
        if (!base.isOpen()) {
            base.open();
        }
        Cursor c = base.getNote(mParam1);
        sPrefs = new SharedPrefs(NotePreviewFragment.this);
        if (c != null && c.moveToFirst()){
            new Notifier(this)
                    .showNoteNotification((sPrefs.loadBoolean(Prefs.NOTE_ENCRYPT) ?
                            SyncHelper.decrypt(c.getString(c.getColumnIndex(Constants.COLUMN_NOTE))):
                            c.getString(c.getColumnIndex(Constants.COLUMN_NOTE))), mParam1);
        }
        if (c != null) {
            c.close();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    @Override
    public void onBackPressed() {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                Animation slide = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.scale_zoom_out);
                mFab.startAnimation(slide);
                mFab.setVisibility(View.GONE);
            }
        });
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    finishAfterTransition();
                } else finish();
            }
        }, 300);
    }

    private void loadData(){
        base = new NotesBase(NotePreviewFragment.this);
        base.open();
        imageByte = null;
        img = null;
        Cursor c = base.getNote(mParam1);
        if (c != null && c.moveToFirst()){
            String note = c.getString(c.getColumnIndex(Constants.COLUMN_NOTE));
            SharedPrefs sPrefs = new SharedPrefs(NotePreviewFragment.this);
            if (sPrefs.loadBoolean(Prefs.NOTE_ENCRYPT)){
                note = SyncHelper.decrypt(note);
            }
            noteText.setText(note);
            int color = c.getInt(c.getColumnIndex(Constants.COLUMN_COLOR));
            int style = c.getInt(c.getColumnIndex(Constants.COLUMN_FONT_STYLE));
            remId = c.getLong(c.getColumnIndex(Constants.COLUMN_LINK_ID));
            imageByte = c.getBlob(c.getColumnIndex(Constants.COLUMN_IMAGE));
            noteText.setTypeface(cSetter.getTypeface(style));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setStatusBarColor(cSetter.getNoteDarkColor(color));
            }
            mFab.setColorNormal(cSetter.colorAccent(color));
            mFab.setColorPressed(cSetter.colorAccent(color));
            RelativeLayout.LayoutParams paramsR = (RelativeLayout.LayoutParams) mFab.getLayoutParams();
            paramsR.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            paramsR.setMargins(0, -(QuickReturnUtils.dp2px(NotePreviewFragment.this, 36)), 0, 0);
            if (imageByte != null){
                Bitmap imgB = BitmapFactory.decodeByteArray(imageByte, 0,
                        imageByte.length);
                img = imgB;
                imageView.setImageBitmap(imgB);
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) imageView.getLayoutParams();
                params.height = QuickReturnUtils.dp2px(this, 256);
                imageView.setLayoutParams(params);
                paramsR.addRule(RelativeLayout.BELOW, R.id.imageView);
                toolbar.setBackgroundColor(cSetter.getNoteColor(color));
                toolbar.getBackground().setAlpha(0);
            } else {
                imageView.setBackgroundColor(cSetter.getNoteColor(color));
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) imageView.getLayoutParams();
                params.height = QuickReturnUtils.dp2px(this, 256);
                imageView.setLayoutParams(params);
                imageView.setVisibility(View.INVISIBLE);
                paramsR.addRule(RelativeLayout.BELOW, R.id.imageView);
                toolbar.setBackgroundColor(cSetter.getNoteColor(color));
                toolbar.getBackground().setAlpha(255);
            }

            if (remId != 0){
                DataBase dataBase = new DataBase(NotePreviewFragment.this);
                dataBase.open();
                Cursor r = dataBase.getReminder(remId);
                if (r != null && r.moveToFirst()){
                    long feature = r.getLong(r.getColumnIndex(Constants.COLUMN_FEATURE_TIME));
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(System.currentTimeMillis());
                    if (feature != 0) {
                        calendar.setTimeInMillis(feature);
                    }

                    reminderTime.setText(TimeUtil.getDateTime(calendar.getTime(),
                            sPrefs.loadBoolean(Prefs.IS_24_TIME_FORMAT)));
                    reminderContainer.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    private void shareNote(){
        if (!NoteModel.shareNote(mParam1, this)) {
            Messages.toast(this, getString(R.string.attach_error_message));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                finishAfterTransition();
            } else {
                finish();
            }
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
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        Animation slide = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.scale_zoom_out);
                        mFab.startAnimation(slide);
                        mFab.setVisibility(View.GONE);
                    }
                });
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            finishAfterTransition();
                        } else {
                            finish();
                        }
                    }
                }, 300);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void deleteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(getString(R.string.delete_note_dialog_title));
        builder.setMessage(getString(R.string.delete_note_dialog_message));
        builder.setNegativeButton(getString(R.string.import_dialog_button_no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setPositiveButton(getString(R.string.import_dialog_button_yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteNote();
                dialog.dismiss();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    finishAfterTransition();
                } else {
                    finish();
                }
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void deleteNote() {
        NoteModel.deleteNote(mParam1, this, null);
        sPrefs = new SharedPrefs(this);
        sPrefs.saveBoolean("isNew", true);
    }
}
