package com.cray.software.justreminder;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.dialogs.ImagePreview;
import com.cray.software.justreminder.dialogs.utils.ColorPicker;
import com.cray.software.justreminder.dialogs.utils.FontStyleDialog;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.interfaces.Prefs;
import com.cray.software.justreminder.note.Note;
import com.cray.software.justreminder.note.NotesBase;
import com.cray.software.justreminder.reminder.Telephony;
import com.cray.software.justreminder.services.AlarmReceiver;
import com.cray.software.justreminder.utils.AssetsUtil;
import com.cray.software.justreminder.utils.TimeUtil;
import com.cray.software.justreminder.utils.ViewUtils;
import com.cray.software.justreminder.views.FloatingEditText;
import com.cray.software.justreminder.widgets.UpdatesHelper;
import com.getbase.floatingactionbutton.FloatingActionButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;


public class NotesManager extends AppCompatActivity {

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
    private RelativeLayout layoutContainer, imageContainer;
    private LinearLayout remindContainer;
    private TextView remindDate, remindTime;
    private ImageButton discardReminder;
    private ImageView noteImage;
    private AlarmReceiver alarm = new AlarmReceiver();

    private NotesBase DB = new NotesBase(NotesManager.this);

    private ColorSetter cSetter = new ColorSetter(NotesManager.this);
    private SharedPrefs sPrefs = new SharedPrefs(NotesManager.this);

    private long id;
    private Toolbar toolbar;
    private FloatingEditText taskField;
    private FloatingActionButton mFab;

    public static final int MENU_ITEM_DELETE = 12;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cSetter = new ColorSetter(NotesManager.this);
        setTheme(cSetter.getStyle());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(cSetter.colorStatus());
        }
        setContentView(R.layout.create_note_layout);
        setRequestedOrientation(cSetter.getRequestOrientation());

        sPrefs = new SharedPrefs(NotesManager.this);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setOnMenuItemClickListener(
                new Toolbar.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.action_color:
                                startActivityForResult(new Intent(NotesManager.this,
                                        ColorPicker.class), Constants.REQUEST_CODE_THEME);
                                return true;
                            case R.id.action_image:
                                getImage();
                                return true;
                            case R.id.action_reminder:
                                if (!isReminderAttached()) {
                                    setDateTime();
                                    if (sPrefs.loadBoolean(Prefs.ANIMATIONS)) {
                                        ViewUtils.expand(remindContainer);
                                    } else remindContainer.setVisibility(View.VISIBLE);
                                } else {
                                    if (sPrefs.loadBoolean(Prefs.ANIMATIONS)) {
                                        ViewUtils.collapse(remindContainer);
                                    } else remindContainer.setVisibility(View.GONE);
                                }
                                return true;
                            case R.id.action_font:
                                startActivityForResult(new Intent(NotesManager.this,
                                        FontStyleDialog.class), Constants.REQUEST_CODE_FONT_STYLE);
                                return true;
                            case R.id.action_share:
                                shareNote();
                                break;
                            case MENU_ITEM_DELETE:
                                deleteDialog();
                                break;
                        }
                        return true;
                    }
                });

        toolbar.inflateMenu(R.menu.create_note);

        taskField = (FloatingEditText) findViewById(R.id.task_message);
        taskField.setTextSize(sPrefs.loadInt(Prefs.TEXT_SIZE) + 12);

        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setElevation(0f);

        toolbar.setVisibility(View.VISIBLE);

        layoutContainer = (RelativeLayout) findViewById(R.id.layoutContainer);
        imageContainer = (RelativeLayout) findViewById(R.id.imageContainer);
        color = cSetter.getNoteColor(8);
        remindContainer = (LinearLayout) findViewById(R.id.remindContainer);
        if (sPrefs.loadBoolean(Prefs.ANIMATIONS)) {
            ViewUtils.fadeInAnimation(layoutContainer, sPrefs.loadBoolean(Prefs.ANIMATIONS));
        } else layoutContainer.setVisibility(View.VISIBLE);

        Typeface typeface = AssetsUtil.getLightTypeface(this);

        remindDate = (TextView) findViewById(R.id.remindDate);
        remindDate.setTypeface(typeface);
        remindDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateDialog().show();
            }
        });

        remindTime = (TextView) findViewById(R.id.remindTime);
        remindTime.setTypeface(typeface);
        remindTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timeDialog().show();
            }
        });

        noteImage = (ImageView) findViewById(R.id.noteImage);
        if (image != null){
            imageContainer.setVisibility(View.VISIBLE);
        } else {
            imageContainer.setVisibility(View.GONE);
        }
        noteImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                    Bitmap _bitmapScaled = img;
                    _bitmapScaled.compress(Bitmap.CompressFormat.JPEG, 100, bytes);

                    File sdPath = Environment.getExternalStorageDirectory();
                    File sdPathDr = new File(sdPath.toString() + "/JustReminder/" + Constants.DIR_IMAGE_CACHE);
                    if (!sdPathDr.exists()) {
                        sdPathDr.mkdirs();
                    }
                    String fileName = SyncHelper.generateID() + Constants.FILE_NAME_IMAGE;
                    File f = new File(sdPathDr
                            + File.separator + fileName);
                    f.createNewFile();

                    FileOutputStream fo = new FileOutputStream(f);
                    fo.write(bytes.toByteArray());
                    fo.close();

                    startActivity(new Intent(NotesManager.this, ImagePreview.class)
                            .putExtra("image", f.toString()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        discardReminder = (ImageButton) findViewById(R.id.discardReminder);
        discardReminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sPrefs.loadBoolean(Prefs.ANIMATIONS)) {
                    ViewUtils.collapse(remindContainer);
                } else remindContainer.setVisibility(View.GONE);
            }
        });

        ImageButton deleteButton = (ImageButton) findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isImageAttached()) {
                    if (sPrefs.loadBoolean(Prefs.ANIMATIONS)) {
                        ViewUtils.collapse(imageContainer);
                    } else imageContainer.setVisibility(View.GONE);
                    image = null;
                    img = null;
                }
            }
        });

        setImages();

        mFab = new FloatingActionButton(NotesManager.this);
        mFab.setSize(FloatingActionButton.SIZE_NORMAL);
        mFab.setIcon(R.drawable.ic_done_white_24dp);

        layoutContainer.addView(mFab);

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mFab.getLayoutParams();
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sPrefs = new SharedPrefs(NotesManager.this);
                sPrefs.saveBoolean("isNew", true);
                saveNote();
            }
        });
        mFab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ViewUtils.hide(NotesManager.this, mFab, sPrefs.loadBoolean(Prefs.ANIMATIONS));
                return false;
            }
        });
        mFab.setColorNormal(cSetter.colorSetter());
        mFab.setColorPressed(cSetter.colorChooser());

        Intent intent = getIntent();
        Uri name = null;
        try {
            name = intent.getData();
        } catch (NullPointerException e){
            e.printStackTrace();
        } finally {
            id = intent.getLongExtra(Constants.EDIT_ID, 0);
        }
        if (id != 0){
            DB.open();
            Cursor c = DB.getNote(id);
            if (c != null && c.moveToFirst()){
                String note = c.getString(c.getColumnIndex(Constants.COLUMN_NOTE));
                if (sPrefs.loadBoolean(Prefs.NOTE_ENCRYPT)){
                    note = new SyncHelper(NotesManager.this).decrypt(note);
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
                    if (sPrefs.loadBoolean(Prefs.ANIMATIONS)) {
                        ViewUtils.expand(imageContainer);
                    } else imageContainer.setVisibility(View.VISIBLE);
                }
            }
            if (c != null) c.close();
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
                JSONObject object = null;
                try {
                    object = new JSONObject(file);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                SyncHelper helper = new SyncHelper(NotesManager.this);
                ArrayList<String> data = helper.getNote(null, object);
                String note = data.get(0);
                note = helper.decrypt(note);
                taskField.setText(note);
                taskField.setSelection(taskField.getText().length());
                color = helper.getColor(null, object);
                style = helper.getFontStyle(null, object);
                byte[] imageByte = helper.getImage(null, object);
				image = imageByte;
                if (imageByte != null){
                    img = BitmapFactory.decodeByteArray(imageByte, 0,
                            imageByte.length);
                    noteImage.setImageBitmap(img);
                    if (sPrefs.loadBoolean(Prefs.ANIMATIONS)) {
                        ViewUtils.expand(imageContainer);
                    } else imageContainer.setVisibility(View.VISIBLE);
                }
            } else {
                File file = new File(name.getPath());
                SyncHelper helper = new SyncHelper(NotesManager.this);
                ArrayList<String> data = helper.getNote(file, null);
                String note = data.get(0);
                note = helper.decrypt(note);
                taskField.setText(note);
                taskField.setSelection(taskField.getText().length());
                color = helper.getColor(file, null);
                style = helper.getFontStyle(file, null);
                byte[] imageByte = helper.getImage(file, null);
				image = imageByte;
                if (imageByte != null){
                    img = BitmapFactory.decodeByteArray(imageByte, 0,
                            imageByte.length);
                    noteImage.setImageBitmap(img);
                    if (sPrefs.loadBoolean(Prefs.ANIMATIONS)) {
                        ViewUtils.expand(imageContainer);
                    } else imageContainer.setVisibility(View.VISIBLE);
                }
            }

        }

        taskField.setTypeface(cSetter.getTypeface(style));
        toolbar.setBackgroundColor(cSetter.getNoteLightColor(color));
        layoutContainer.setBackgroundColor(cSetter.getNoteLightColor(color));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(cSetter.getNoteDarkColor(color));
        }
        try {
            mFab.setColorNormal(getResources().getColor(color));
        } catch (Exception e){
            mFab.setColorNormal(cSetter.getNoteColor(color));
        }
        mFab.setColorPressed(cSetter.getNoteDarkColor(color));
    }

    private void shareNote() {
        SyncHelper sHelp = new SyncHelper(NotesManager.this);
        String note = taskField.getText().toString();
        if (note.matches("")) {
            taskField.setError(getString(R.string.empty_field_error));
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
            sendMail(file);
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
    }

    private void sendMail(File file){
        if (!file.exists() || !file.canRead()) {
            Toast.makeText(this, getString(R.string.attach_error_message), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        Telephony.sendMail(file, this);
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

        if (myDay < 10) dayStr = "0" + myDay;
        else dayStr = String.valueOf(myDay);

        if (myMonth < 9) monthStr = "0" + (myMonth + 1);
        else monthStr = String.valueOf(myMonth + 1);

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
        SyncHelper sHelp = new SyncHelper(NotesManager.this);
        String note = taskField.getText().toString();
        if (note.matches("")) {
            taskField.setError(getString(R.string.empty_field_error));
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
        DB.open();
        if (id != 0){
            if (sPrefs.loadBoolean(Prefs.NOTE_ENCRYPT)){
                DB.updateNote(id, sHelp.encrypt(note), date, color, uuID, image, style);
            } else {
                DB.updateNote(id, note, date, color, uuID, image, style);
            }
        } else {
            if (sPrefs.loadBoolean(Prefs.NOTE_ENCRYPT)){
                id = DB.saveNote(sHelp.encrypt(note), date, color, uuID, image, style);
            } else {
                id = DB.saveNote(note, date, color, uuID, image, style);
            }
        }

        if (isReminderAttached()){
            Cursor cf = new DataBase(NotesManager.this).queryCategories();
            String categoryId = null;
            if (cf != null && cf.moveToFirst()) {
                categoryId = cf.getString(cf.getColumnIndex(Constants.COLUMN_TECH_VAR));
            }
            if (cf != null) cf.close();
            long remId = new DataBase(NotesManager.this).insertReminder(note, Constants.TYPE_REMINDER, myDay,
                    myMonth, myYear, myHour, myMinute, 0, null, 0, 0, 0, 0, 0, SyncHelper.generateID(),
                    null, 0, null, 0, 0, 0, categoryId);
            new DataBase(NotesManager.this).updateReminderDateTime(remId);
            DB.linkToReminder(id, remId);
            alarm.setAlarm(NotesManager.this, remId);
        }

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
                Note.deleteNote(id, NotesManager.this);
                dialog.dismiss();
                new SharedPrefs(NotesManager.this).saveBoolean("isNew", true);
                finish();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void setImages(){
        sPrefs = new SharedPrefs(NotesManager.this);
        if (sPrefs.loadBoolean(Prefs.USE_DARK_THEME)){
            discardReminder.setImageResource(R.drawable.ic_clear_white_24dp);
        } else {
            discardReminder.setImageResource(R.drawable.ic_clear_grey600_24dp);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.create_note, menu);
        if (id != 0) {
            menu.add(Menu.NONE, MENU_ITEM_DELETE, 100, getString(R.string.delete_menu_option));
        }
        return true;
    }

    private void getImage(){
        AlertDialog.Builder builder = new AlertDialog.Builder(NotesManager.this);
        builder.setTitle(getString(R.string.image_dialog_title));
        builder.setItems(new CharSequence[] {getString(R.string.image_dialog_gallery),
                        getString(R.string.image_dialog_camera)},
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                                intent.setType("image/*");
                                Intent chooser = Intent.createChooser(intent, getString(R.string.choose_picture_title));
                                startActivityForResult(chooser, Constants.ACTION_REQUEST_GALLERY);
                                break;
                            case 1:
                                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                                startActivityForResult(cameraIntent, Constants.ACTION_REQUEST_CAMERA);
                                break;
                            default:
                                break;
                        }
                    }
                });

        builder.show();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case Constants.ACTION_REQUEST_GALLERY:
                    Uri selectedImage = data.getData();
                    Bitmap bitmapImage = null;
                    try {
                        bitmapImage = decodeUri(selectedImage);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    img = bitmapImage;
                    if (bitmapImage != null){
                        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                        bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                        image = outputStream.toByteArray();
                        noteImage.setImageBitmap(bitmapImage);
                        if (!isImageAttached()) {
                            if (sPrefs.loadBoolean(Prefs.ANIMATIONS)) {
                                ViewUtils.expand(imageContainer);
                            } else imageContainer.setVisibility(View.VISIBLE);
                        }
                    }
                    break;
                case Constants.ACTION_REQUEST_CAMERA:
                    Bitmap cameraImage = (Bitmap) data.getExtras().get("data");
                    img = cameraImage;
                    if (cameraImage != null){
                        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                        cameraImage.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                        image = outputStream.toByteArray();
                        noteImage.setImageBitmap(cameraImage);
                        if (!isImageAttached()) {
                            if (sPrefs.loadBoolean(Prefs.ANIMATIONS)) {
                                ViewUtils.expand(imageContainer);
                            } else imageContainer.setVisibility(View.VISIBLE);
                        }
                    }
                    break;
                case Constants.REQUEST_CODE_THEME:
                    int requestColor = data.getIntExtra(Constants.SELECTED_COLOR, 12);
                    color = cSetter.getNoteColor(requestColor);
                    layoutContainer.setBackgroundColor(cSetter.getNoteLightColor(color));
                    toolbar.setBackgroundColor(cSetter.getNoteLightColor(color));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        getWindow().setStatusBarColor(cSetter.getNoteDarkColor(color));
                    }
                    mFab.setColorNormal(getResources().getColor(color));
                    mFab.setColorPressed(cSetter.getNoteDarkColor(color));
                    break;
                case Constants.REQUEST_CODE_FONT_STYLE:
                    style = data.getIntExtra(Constants.SELECTED_FONT_STYLE, 5);
                    Typeface typeface = cSetter.getTypeface(style);
                    taskField.setTypeface(typeface);
                    break;
            }
        }
    }

    private Bitmap decodeUri(Uri selectedImage) throws FileNotFoundException {
        // Decode image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage), null, o);
        final int REQUIRED_SIZE = 350;
        int width_tmp = o.outWidth, height_tmp = o.outHeight;
        int scale = 1;
        while (true) {
            if (width_tmp / 2 < REQUIRED_SIZE
                    || height_tmp / 2 < REQUIRED_SIZE) {
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

            if (myDay < 10) dayStr = "0" + myDay;
            else dayStr = String.valueOf(myDay);

            if (myMonth < 9) monthStr = "0" + (myMonth + 1);
            else monthStr = String.valueOf(myMonth + 1);
            remindDate.setText(dayStr + "/" + monthStr + "/" + String.valueOf(myYear));
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
        if (DB != null && DB.isOpen()) DB.close();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (mFab.getVisibility() == View.GONE){
            ViewUtils.show(NotesManager.this, mFab, sPrefs.loadBoolean(Prefs.ANIMATIONS));
            return;
        }

        finish();
    }
}