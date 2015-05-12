package com.cray.software.justreminder.dialogs;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.Contacts;
import com.cray.software.justreminder.helpers.Notifier;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.modules.ManageModule;
import com.cray.software.justreminder.services.RepeatNotificationReceiver;
import com.cray.software.justreminder.views.RoundImageView;
import com.getbase.floatingactionbutton.FloatingActionButton;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class ShowBirthday extends Activity implements View.OnClickListener {

    DataBase DB;
    Typeface typeface;
    RoundImageView contactPhoto;
    TextView userName, userNumber, userYears;
    FloatingActionButton buttonOk, buttonCall, buttonSend;
    LinearLayout single_container;
    long id;
    SharedPrefs sPrefs;
    Contacts contacts;
    int contactId;
    String name, number, birthDate;
    ColorSetter cs = new ColorSetter(ShowBirthday.this);
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

    Notifier notifier = new Notifier(ShowBirthday.this);
    boolean isDark = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(cs.getFullscreenStyle());
        sPrefs = new SharedPrefs(ShowBirthday.this);
        boolean isFull = sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_UNLOCK_DEVICE);
        if (isFull) {
            runOnUiThread(new Runnable() {
                public void run() {
                    getWindow().addFlags( WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                            | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                            | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
                }
            });
        }

        setContentView(R.layout.show_birthday_layout);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(cs.getStatusBarStyle());
        }
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Intent i = getIntent();
        id = i.getLongExtra("id", 0);

        single_container = (LinearLayout) findViewById(R.id.single_container);
        single_container.setVisibility(View.VISIBLE);

        typeface = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf");

        buttonOk = (FloatingActionButton) findViewById(R.id.buttonOk);
        buttonOk.setOnClickListener(this);

        buttonCall = (FloatingActionButton) findViewById(R.id.buttonCall);
        buttonCall.setOnClickListener(this);

        buttonSend = (FloatingActionButton) findViewById(R.id.buttonSend);
        buttonSend.setOnClickListener(this);

        isDark = sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_USE_DARK_THEME);
        colorify(buttonOk, buttonCall, buttonSend);
        if (isDark){
            buttonOk.setIconDrawable(getResources().getDrawable(R.drawable.ic_done_grey600_24dp));
            buttonCall.setIconDrawable(getResources().getDrawable(R.drawable.ic_call_grey600_24dp));
            buttonSend.setIconDrawable(getResources().getDrawable(R.drawable.ic_send_grey600_24dp));
        } else {
            buttonOk.setIconDrawable(getResources().getDrawable(R.drawable.ic_done_white_24dp));
            buttonCall.setIconDrawable(getResources().getDrawable(R.drawable.ic_call_white_24dp));
            buttonSend.setIconDrawable(getResources().getDrawable(R.drawable.ic_send_white_24dp));
        }

        DB = new DataBase(ShowBirthday.this);
        sPrefs = new SharedPrefs(ShowBirthday.this);
        contacts = new Contacts(ShowBirthday.this);

        DB.open();
        Cursor c = DB.getEvent(id);
        if (c != null && c.moveToFirst()){
            contactId = c.getInt(c.getColumnIndex(Constants.ContactConstants.COLUMN_CONTACT_ID));
            name = c.getString(c.getColumnIndex(Constants.ContactConstants.COLUMN_CONTACT_NAME));
            number = c.getString(c.getColumnIndex(Constants.ContactConstants.COLUMN_CONTACT_NUMBER));
            birthDate = c.getString(c.getColumnIndex(Constants.ContactConstants.COLUMN_CONTACT_BIRTHDAY));
        }
        if (c != null) c.close();
        if (number == null || number.matches("")) {
            number = contacts.get_Number(name, ShowBirthday.this);
        }
        contactPhoto = (RoundImageView) findViewById(R.id.contactPhoto);
        Bitmap photo = contacts.openPhoto(contactId);
        if (photo != null) {
            contactPhoto.setImageBitmap(photo);
        } else {
            contactPhoto.setVisibility(View.GONE);
        }

        String years = getYears(birthDate) + " " + getString(R.string.years_string);

        userName = (TextView) findViewById(R.id.userName);
        userName.setTypeface(typeface);
        userName.setText(name);
        userNumber = (TextView) findViewById(R.id.userNumber);
        userNumber.setTypeface(typeface);

        userYears = (TextView) findViewById(R.id.userYears);
        userYears.setTypeface(typeface);
        userYears.setText(years);

        if (number == null || number.matches("noNumber")) {
            buttonCall.setVisibility(View.GONE);
            buttonSend.setVisibility(View.GONE);
            userNumber.setVisibility(View.GONE);
        } else {
            userNumber.setText(number);
        }

        wakeScreen();

        notifier.showNotification(getYears(birthDate), name);
    }

    private void colorify(FloatingActionButton... fab){
        for (FloatingActionButton button:fab){
            if (isDark){
                button.setColorNormal(getResources().getColor(R.color.colorWhite));
                button.setColorPressed(getResources().getColor(R.color.colorGrayDark));
            } else {
                button.setColorNormal(getResources().getColor(R.color.colorGrayDark));
                button.setColorPressed(getResources().getColor(R.color.colorWhite));
            }
        }
    }

    private void wakeScreen() {
        boolean wake;
        sPrefs = new SharedPrefs(ShowBirthday.this);
        if (new ManageModule().isPro()){
            if (!sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_USE_GLOBAL)){
                wake = sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_BIRTHDAY_WAKE_STATUS);
            } else wake = sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_WAKE_STATUS);
        } else wake = sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_WAKE_STATUS);

        if (wake) {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            boolean isScreenOn = pm.isScreenOn();
            if (!isScreenOn) {
                PowerManager.WakeLock screenLock = ((PowerManager) getSystemService(POWER_SERVICE)).newWakeLock(
                        PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP |
                                PowerManager.FULL_WAKE_LOCK, "Just");
                screenLock.acquire(2000);
                screenLock.release();
            }
        }
    }

    public void removeFlags(){
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
    }

    private int getYears(String dateOfBirth){
        int years;
        Date date = null;
        try {
            date = format.parse(dateOfBirth);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int yearOfBirth = calendar.get(Calendar.YEAR);

        Calendar calendar1 = Calendar.getInstance();
        calendar1.getTimeInMillis();
        int currentYear = calendar1.get(Calendar.YEAR);
        years = currentYear - yearOfBirth;
        return years;
    }

    private void makeCall(String number){
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + number));
        startActivity(callIntent);
    }

    private void sendSMS(String number){
        Intent smsIntent = new Intent(Intent.ACTION_VIEW);
        smsIntent.setData(Uri.parse("sms:" + number));
        startActivity(smsIntent);
    }

    @Override
    public void onClick(View v) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        switch (v.getId()){
            case R.id.buttonOk:
                notifier.discardNotification();
                DB.open();
                DB.setShown(id, String.valueOf(year));
                removeFlags();
                notifier.recreatePermanent();
                finish();
                break;
            case R.id.buttonCall:
                notifier.discardNotification();
                makeCall(number);
                DB.open();
                DB.setShown(id, String.valueOf(year));
                removeFlags();
                notifier.recreatePermanent();
                finish();
                break;
            case R.id.buttonSend:
                notifier.discardNotification();
                sendSMS(number);
                DB.open();
                DB.setShown(id, String.valueOf(year));
                removeFlags();
                notifier.recreatePermanent();
                finish();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        notifier.discardMedia();
        sPrefs = new SharedPrefs(ShowBirthday.this);
        if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_SMART_FOLD)){
            moveTaskToBack(true);
            new RepeatNotificationReceiver().cancelAlarm(ShowBirthday.this, id);
            new RepeatNotificationReceiver().cancelAlarm(ShowBirthday.this, 0);
            removeFlags();
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.must_click_message), Toast.LENGTH_SHORT).show();
        }
    }
}
