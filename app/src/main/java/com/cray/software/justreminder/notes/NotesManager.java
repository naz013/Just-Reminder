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

package com.cray.software.justreminder.notes;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TimePicker;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.ReminderApp;
import com.cray.software.justreminder.activities.ImagePreview;
import com.cray.software.justreminder.app_widgets.UpdatesHelper;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.FileConfig;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.dialogs.ColorPicker;
import com.cray.software.justreminder.dialogs.FontStyleDialog;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.Messages;
import com.cray.software.justreminder.helpers.Permissions;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.helpers.Telephony;
import com.cray.software.justreminder.json.JModel;
import com.cray.software.justreminder.modules.Module;
import com.cray.software.justreminder.reminder.DateType;
import com.cray.software.justreminder.roboto_views.RoboTextView;
import com.cray.software.justreminder.utils.LocationUtil;
import com.cray.software.justreminder.utils.SuperUtil;
import com.cray.software.justreminder.utils.TimeUtil;
import com.cray.software.justreminder.utils.ViewUtils;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.Random;

public class NotesManager extends AppCompatActivity {

    public static final int MENU_ITEM_DELETE = 12;
    private static final String KEY_COLOR = "key_color";
    private static final String KEY_STYLE = "key_style";
    private static final String KEY_IMAGE = "key_image";
    private static final int REQUEST_SD_CARD = 1112;

    private int myHour = 0;
    private int myMinute = 0;
    private int myYear = 0;
    private int myMonth = 0;
    private int myDay = 1;
    private int color = 0;
    private int style = 5;
    private String uuID = "";
    private byte[] image = null;
    private Bitmap img;
    private Uri mImageUri;
    private RelativeLayout layoutContainer, imageContainer;
    private LinearLayout remindContainer;
    private RoboTextView remindDate, remindTime;
    private ImageButton discardReminder;
    private ImageView noteImage;

    private ColorSetter cSetter = new ColorSetter(NotesManager.this);
    private SharedPrefs sPrefs = new SharedPrefs(NotesManager.this);

    private long id;
    private Toolbar toolbar;
    private EditText taskField;
    private FloatingActionButton mFab;

    private Tracker mTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cSetter = new ColorSetter(NotesManager.this);
        setTheme(cSetter.getStyle());
        if (Module.isLollipop()) {
            getWindow().setStatusBarColor(ViewUtils.getColor(this, cSetter.colorPrimaryDark()));
        }
        setContentView(R.layout.create_note_layout);
        setRequestedOrientation(cSetter.getRequestOrientation());

        sPrefs = new SharedPrefs(NotesManager.this);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setOnMenuItemClickListener(item -> {
                    switch (item.getItemId()) {
                        case R.id.action_color:
                            startActivityForResult(new Intent(NotesManager.this,
                                    ColorPicker.class), Constants.REQUEST_CODE_THEME);
                            return true;
                        case R.id.action_image:
                            if (Permissions.checkPermission(NotesManager.this, Permissions.READ_EXTERNAL, Permissions.WRITE_EXTERNAL)) {
                                getImage();
                            } else {
                                Permissions.requestPermission(NotesManager.this, REQUEST_SD_CARD, Permissions.READ_EXTERNAL, Permissions.WRITE_EXTERNAL);
                            }
                            return true;
                        case R.id.action_reminder:
                            if (!isReminderAttached()) {
                                setDateTime();
                                ViewUtils.expand(remindContainer);
                            } else {
                                ViewUtils.collapse(remindContainer);
                            }
                            return true;
                        case R.id.action_font:
                            startActivityForResult(new Intent(NotesManager.this,
                                    FontStyleDialog.class), Constants.REQUEST_CODE_FONT_STYLE);
                            return true;
                        case R.id.action_share:
                            shareNote();
                            return true;
                        case MENU_ITEM_DELETE:
                            deleteDialog();
                            return true;
                        default:
                            return false;
                    }
                });

        toolbar.inflateMenu(R.menu.create_note);

        taskField = (EditText) findViewById(R.id.task_message);
        taskField.setTextSize(sPrefs.loadInt(Prefs.TEXT_SIZE) + 12);

        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setElevation(0f);

        toolbar.setVisibility(View.VISIBLE);

        layoutContainer = (RelativeLayout) findViewById(R.id.layoutContainer);
        imageContainer = (RelativeLayout) findViewById(R.id.imageContainer);
        color = new Random().nextInt(15 + 1);
        remindContainer = (LinearLayout) findViewById(R.id.remindContainer);

        ViewUtils.fadeInAnimation(layoutContainer);

        remindDate = (RoboTextView) findViewById(R.id.remindDate);
        remindDate.setOnClickListener(v -> dateDialog().show());

        remindTime = (RoboTextView) findViewById(R.id.remindTime);
        remindTime.setOnClickListener(v -> timeDialog().show());

        noteImage = (ImageView) findViewById(R.id.noteImage);
        if (image != null){
            imageContainer.setVisibility(View.VISIBLE);
        } else {
            imageContainer.setVisibility(View.GONE);
        }
        noteImage.setOnClickListener(v -> {
            try {
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                Bitmap _bitmapScaled = img;
                _bitmapScaled.compress(Bitmap.CompressFormat.JPEG, 100, bytes);

                File sdPath = Environment.getExternalStorageDirectory();
                File sdPathDr = new File(sdPath.toString() + "/JustReminder/" + Constants.DIR_IMAGE_CACHE);
                boolean isDirectory = false;
                if (!sdPathDr.exists()) {
                    isDirectory = sdPathDr.mkdirs();
                }
                if (isDirectory) {
                    String fileName = SyncHelper.generateID() + FileConfig.FILE_NAME_IMAGE;
                    File f = new File(sdPathDr
                            + File.separator + fileName);
                    boolean isCreated = f.createNewFile();
                    if (isCreated) {
                        FileOutputStream fo = new FileOutputStream(f);
                        fo.write(bytes.toByteArray());
                        fo.close();

                        startActivity(new Intent(NotesManager.this, ImagePreview.class)
                                .putExtra("image", f.toString()));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        discardReminder = (ImageButton) findViewById(R.id.discardReminder);
        discardReminder.setOnClickListener(v -> ViewUtils.collapse(remindContainer));

        ImageButton deleteButton = (ImageButton) findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(v -> {
            if (isImageAttached()) {
                ViewUtils.collapse(imageContainer);
                image = null;
                img = null;
            }
        });

        setImages();

        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setOnClickListener(v -> {
            sPrefs = new SharedPrefs(NotesManager.this);
            sPrefs.saveBoolean("isNew", true);
            saveNote();
        });
        mFab.setOnLongClickListener(v -> {
            mFab.hide();
            return false;
        });
        mFab.setBackgroundTintList(ViewUtils.getFabState(this, cSetter.colorPrimary(), cSetter.colorAccent()));

        Intent intent = getIntent();
        String filePath = intent.getStringExtra(Constants.EDIT_PATH);
        Uri name = null;
        try {
            name = intent.getData();
        } catch (NullPointerException e){
            e.printStackTrace();
        } finally {
            id = intent.getLongExtra(Constants.EDIT_ID, 0);
        }
        if (id != 0){
            NotesBase db = new NotesBase(NotesManager.this);
            db.open();
            Cursor c = db.getNote(id);
            if (c != null && c.moveToFirst()){
                String note = c.getString(c.getColumnIndex(Constants.COLUMN_NOTE));
                if (sPrefs.loadBoolean(Prefs.NOTE_ENCRYPT)){
                    note = SyncHelper.decrypt(note);
                }
                uuID = c.getString(c.getColumnIndex(Constants.COLUMN_UUID));
                taskField.setText(note);
                taskField.setSelection(taskField.getText().length());
                color = c.getInt(c.getColumnIndex(Constants.COLUMN_COLOR));
                style = c.getInt(c.getColumnIndex(Constants.COLUMN_FONT_STYLE));
                byte[] imageByte = c.getBlob(c.getColumnIndex(Constants.COLUMN_IMAGE));
				image = imageByte;
                if (imageByte != null){
                    img = BitmapFactory.decodeByteArray(imageByte, 0,
                            imageByte.length);
                    noteImage.setImageBitmap(img);
                    ViewUtils.expand(imageContainer);
                }
            }
            if (c != null) {
                c.close();
            }
            db.close();
        } else if (name != null){
            String scheme = name.getScheme();
            if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
                ContentResolver cr = getApplicationContext().getContentResolver();
                InputStream is = null;
                try {
                    is = cr.openInputStream(name);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                BufferedReader r = null;
                if (is != null) {
                    r = new BufferedReader(new InputStreamReader(is));
                }
                StringBuilder total = new StringBuilder();
                String line;
                try {
                    while ((line = r != null ? r.readLine() : null) != null) {
                        total.append(line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String file = total.toString();
                try {
                    NoteModel model = SyncHelper.getNote(null, file);
                    if (model != null) {
                        taskField.setText(SyncHelper.decrypt(model.getNote()));
                        taskField.setSelection(taskField.getText().length());
                        color = model.getColor();
                        style = model.getStyle();
                        byte[] imageByte = model.getImage();
                        image = imageByte;
                        if (imageByte != null) {
                            img = BitmapFactory.decodeByteArray(imageByte, 0, imageByte.length);
                            noteImage.setImageBitmap(img);
                            ViewUtils.expand(imageContainer);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    NoteModel model = SyncHelper.getNote(name.getPath(), null);
                    if (model != null) {
                        taskField.setText(SyncHelper.decrypt(model.getNote()));
                        taskField.setSelection(taskField.getText().length());
                        color = model.getColor();
                        style = model.getStyle();
                        byte[] imageByte = model.getImage();
                        image = imageByte;
                        if (imageByte != null) {
                            img = BitmapFactory.decodeByteArray(imageByte, 0, imageByte.length);
                            noteImage.setImageBitmap(img);
                            ViewUtils.expand(imageContainer);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } else if (filePath != null) {
            try {
                NoteModel model = SyncHelper.getNote(filePath, null);
                if (model != null) {
                    taskField.setText(SyncHelper.decrypt(model.getNote()));
                    taskField.setSelection(taskField.getText().length());
                    color = model.getColor();
                    style = model.getStyle();
                    byte[] imageByte = model.getImage();
                    image = imageByte;
                    if (imageByte != null) {
                        img = BitmapFactory.decodeByteArray(imageByte, 0, imageByte.length);
                        noteImage.setImageBitmap(img);
                        ViewUtils.expand(imageContainer);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if (savedInstanceState != null) {
            color = savedInstanceState.getInt(KEY_COLOR);
            style = savedInstanceState.getInt(KEY_STYLE);
            image = savedInstanceState.getByteArray(KEY_IMAGE);
            if (image != null) {
                img = BitmapFactory.decodeByteArray(image, 0, image.length);
                noteImage.setImageBitmap(img);
                if (!isImageAttached()) {
                    ViewUtils.expand(imageContainer);
                }
            }
        }

        updateBackground();
        updateTextStyle();

        if (LocationUtil.isGooglePlayServicesAvailable(this)) {
            ReminderApp application = (ReminderApp) getApplication();
            mTracker = application.getDefaultTracker();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        outState.putInt(KEY_COLOR, color);
        outState.putInt(KEY_STYLE, style);
        outState.putByteArray(KEY_IMAGE, image);
        super.onSaveInstanceState(outState, outPersistentState);
    }

    private void shareNote() {
        SyncHelper sHelp = new SyncHelper(NotesManager.this);
        String note = taskField.getText().toString();
        if (note.matches("")) {
            taskField.setError(getString(R.string.must_be_not_empty));
            return;
        }

        Calendar calendar1 = Calendar.getInstance();
        int day = calendar1.get(Calendar.DAY_OF_MONTH);
        int month = calendar1.get(Calendar.MONTH);
        int year = calendar1.get(Calendar.YEAR);
        String date = year + "/" + month + "/" + day;

        if (uuID == null || uuID.matches("")) {
            uuID = SyncHelper.generateID();
        }

        try {
            File file = sHelp.createNote(note, date, uuID, color, image, style);
            sendMail(file, note);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void sendMail(File file, String text){
        if (!file.exists() || !file.canRead()) {
            Messages.toast(this, getString(R.string.error_sending));
            finish();
            return;
        }
        Telephony.sendNote(file, this, text);
    }

    private void setDateTime() {
        Calendar calendar = Calendar.getInstance();
        myDay = calendar.get(Calendar.DAY_OF_MONTH);
        myMonth = calendar.get(Calendar.MONTH);
        myYear = calendar.get(Calendar.YEAR);
        myHour = calendar.get(Calendar.HOUR_OF_DAY);
        myMinute = calendar.get(Calendar.MINUTE);

        String dayStr;
        String monthStr;

        if (myDay < 10) {
            dayStr = "0" + myDay;
        } else {
            dayStr = String.valueOf(myDay);
        }

        if (myMonth < 9) {
            monthStr = "0" + (myMonth + 1);
        } else {
            monthStr = String.valueOf(myMonth + 1);
        }

        remindDate.setText(dayStr + "/" + monthStr + "/" + String.valueOf(myYear));

        remindTime.setText(TimeUtil.getTime(calendar.getTime(),
                sPrefs.loadBoolean(Prefs.IS_24_TIME_FORMAT)));
    }

    private boolean isReminderAttached(){
        return remindContainer.getVisibility() == View.VISIBLE;
    }

    private boolean isImageAttached(){
        return imageContainer.getVisibility() == View.VISIBLE;
    }

    private void saveNote() {
        String note = taskField.getText().toString();
        if (note.matches("") && image == null) {
            taskField.setError(getString(R.string.must_be_not_empty));
            return;
        }

        Calendar calendar1 = Calendar.getInstance();
        int day = calendar1.get(Calendar.DAY_OF_MONTH);
        int month = calendar1.get(Calendar.MONTH);
        int year = calendar1.get(Calendar.YEAR);
        int hour = calendar1.get(Calendar.HOUR_OF_DAY);
        int minute = calendar1.get(Calendar.MINUTE);
        int seconds = calendar1.get(Calendar.SECOND);
        String date = year + "/" + month + "/" + day + " "
                + hour + ":" + minute + ":" + seconds;

        if (uuID == null || uuID.matches("")) {
            uuID = SyncHelper.generateID();
        }
        NotesBase db = new NotesBase(NotesManager.this);
        db.open();
        if (id != 0){
            if (sPrefs.loadBoolean(Prefs.NOTE_ENCRYPT)){
                db.updateNote(id, SyncHelper.encrypt(note), date, color, uuID, image, style);
            } else {
                db.updateNote(id, note, date, color, uuID, image, style);
            }
        } else {
            if (sPrefs.loadBoolean(Prefs.NOTE_ENCRYPT)){
                id = db.saveNote(SyncHelper.encrypt(note), date, color, uuID, image, style);
            } else {
                id = db.saveNote(note, date, color, uuID, image, style);
            }
        }

        if (isReminderAttached()){
            Cursor cf = new DataBase(NotesManager.this).open().queryCategories();
            String categoryId = null;
            if (cf != null && cf.moveToFirst()) {
                categoryId = cf.getString(cf.getColumnIndex(Constants.COLUMN_TECH_VAR));
            }
            if (cf != null) {
                cf.close();
            }

            calendar1.set(myYear, myMonth, myDay, myHour, myMinute);
            long due = calendar1.getTimeInMillis();
            JModel jModel = new JModel(note, Constants.TYPE_REMINDER, categoryId,
                    SyncHelper.generateID(), due, due, null, null, null);
            long remId = new DateType(NotesManager.this, Constants.TYPE_REMINDER).save(jModel);
            db.linkToReminder(id, remId);
        }
        db.close();
        new SharedPrefs(this).saveBoolean(Prefs.NOTE_CHANGED, true);
        new UpdatesHelper(NotesManager.this).updateNotesWidget();
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void deleteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.delete_this_note));
        builder.setPositiveButton(getString(R.string.yes), (dialog, which) -> {
            dialog.dismiss();
            NoteModel.deleteNote(id, NotesManager.this, null);
            new SharedPrefs(NotesManager.this).saveBoolean("isNew", true);
            finish();
        });
        builder.setNegativeButton(getString(R.string.no), (dialog, which) -> {
            dialog.dismiss();
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void setImages(){
        if (new ColorSetter(this).isDark()){
            discardReminder.setImageResource(R.drawable.ic_clear_white_24dp);
        } else {
            discardReminder.setImageResource(R.drawable.ic_clear_black_24dp);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.create_note, menu);
        if (id != 0) {
            menu.add(Menu.NONE, MENU_ITEM_DELETE, 100, getString(R.string.delete));
        }
        return true;
    }

    private void getImage(){
        AlertDialog.Builder builder = new AlertDialog.Builder(NotesManager.this);
        builder.setTitle(getString(R.string.image));
        builder.setItems(new CharSequence[]{getString(R.string.gallery),
                        getString(R.string.take_a_shot)},
                (dialog, which) -> {
                    switch (which) {
                        case 0: {
                            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                            intent.setType("image/*");
                            Intent chooser = Intent.createChooser(intent, getString(R.string.image));
                            startActivityForResult(chooser, Constants.ACTION_REQUEST_GALLERY);
                        }
                            break;
                        case 1: {
                            ContentValues values = new ContentValues();
                            values.put(MediaStore.Images.Media.TITLE, "Picture");
                            values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");
                            mImageUri = getContentResolver().insert(
                                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
                            startActivityForResult(intent, Constants.ACTION_REQUEST_CAMERA);
                        }
                            break;
                        default:
                            break;
                    }
                });

        builder.show();
    }

    public String getRealPathFromURI(Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(contentUri, proj, null, null, null);
        int column_index = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case Constants.ACTION_REQUEST_GALLERY:
                    Uri selectedImage = data.getData();
                    getImageFromGallery(selectedImage);
                    break;
                case Constants.ACTION_REQUEST_CAMERA:
                    getImageFromCamera();
                    break;
                case Constants.REQUEST_CODE_THEME:
                    color = data.getIntExtra(Constants.SELECTED_COLOR, 12);
                    updateBackground();
                    break;
                case Constants.REQUEST_CODE_FONT_STYLE:
                    style = data.getIntExtra(Constants.SELECTED_FONT_STYLE, 5);
                    updateTextStyle();
                    break;
            }
        }
    }

    private void getImageFromGallery(Uri selectedImage) {
        Bitmap bitmapImage = null;
        try {
            bitmapImage = decodeUri(selectedImage);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        img = bitmapImage;
        if (bitmapImage != null) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            image = outputStream.toByteArray();
            noteImage.setImageBitmap(bitmapImage);
            if (!isImageAttached()) {
                ViewUtils.expand(imageContainer);
            }
        }
    }

    private void getImageFromCamera() {
        Bitmap bitmapImage = null;
        try {
            bitmapImage = decodeUri(mImageUri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        img = bitmapImage;
        if (bitmapImage != null) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            image = outputStream.toByteArray();
            noteImage.setImageBitmap(bitmapImage);
            if (!isImageAttached()) {
                ViewUtils.expand(imageContainer);
            }
            String imageurl = getRealPathFromURI(mImageUri);
            File file = new File(imageurl);
            if (file.exists()) {
                file.delete();
            }
        }
    }


    private void updateTextStyle() {
        Typeface typeface = cSetter.getTypeface(style);
        taskField.setTypeface(typeface);
    }

    private void updateBackground() {
        layoutContainer.setBackgroundColor(cSetter.getNoteLightColor(color));
        toolbar.setBackgroundColor(cSetter.getNoteLightColor(color));
        if (Module.isLollipop()) {
            getWindow().setStatusBarColor(cSetter.getNoteDarkColor(color));
        }
        mFab.setBackgroundTintList(ViewUtils.getFabState(this, cSetter.colorPrimary(color),
                cSetter.colorPrimaryDark(color)));
    }

    private Bitmap decodeUri(Uri selectedImage) throws FileNotFoundException {
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage), null, o);
        final int REQUIRED_SIZE = 350;
        int width_tmp = o.outWidth, height_tmp = o.outHeight;
        int scale = 1;
        while (true) {
            if (width_tmp / 2 < REQUIRED_SIZE || height_tmp / 2 < REQUIRED_SIZE) {
                break;
            }
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }

        // Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        return BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage), null, o2);
    }

    protected Dialog dateDialog() {
        return new DatePickerDialog(this, myDateCallBack, myYear, myMonth, myDay);
    }

    DatePickerDialog.OnDateSetListener myDateCallBack = new DatePickerDialog.OnDateSetListener() {

        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            myYear = year;
            myMonth = monthOfYear;
            myDay = dayOfMonth;

            String dayStr;
            String monthStr;

            if (myDay < 10) {
                dayStr = "0" + myDay;
            } else {
                dayStr = String.valueOf(myDay);
            }

            if (myMonth < 9) {
                monthStr = "0" + (myMonth + 1);
            } else {
                monthStr = String.valueOf(myMonth + 1);
            }
            remindDate.setText(SuperUtil.appendString(dayStr, "/", monthStr, "/", String.valueOf(myYear)));
        }
    };

    protected Dialog timeDialog() {
        return new TimePickerDialog(this, myCallBack, myHour, myMinute,
                sPrefs.loadBoolean(Prefs.IS_24_TIME_FORMAT));
    }

    TimePickerDialog.OnTimeSetListener myCallBack = new TimePickerDialog.OnTimeSetListener() {
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            myHour = hourOfDay;
            myMinute = minute;

            Calendar c = Calendar.getInstance();
            c.set(Calendar.HOUR_OF_DAY, hourOfDay);
            c.set(Calendar.MINUTE, minute);

            remindTime.setText(TimeUtil.getTime(c.getTime(),
                    new SharedPrefs(NotesManager.this).loadBoolean(Prefs.IS_24_TIME_FORMAT)));
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        InputMethodManager imm = (InputMethodManager)getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(taskField.getWindowToken(), 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (LocationUtil.isGooglePlayServicesAvailable(this)) {
            mTracker.setScreenName("Create note screen");
            mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_SD_CARD:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getImage();
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (mFab.getVisibility() == View.GONE){
            mFab.show();
            return;
        }

        finish();
    }
}