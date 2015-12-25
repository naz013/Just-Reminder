package com.cray.software.justreminder.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.speech.tts.TextToSpeech;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.SmsManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.adapters.TaskListRecyclerAdapter;
import com.cray.software.justreminder.constants.Configs;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.Language;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.datas.ShoppingListDataProvider;
import com.cray.software.justreminder.datas.models.ShoppingList;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.Contacts;
import com.cray.software.justreminder.helpers.Messages;
import com.cray.software.justreminder.helpers.Notifier;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.Telephony;
import com.cray.software.justreminder.interfaces.SendListener;
import com.cray.software.justreminder.modules.Module;
import com.cray.software.justreminder.reminder.Reminder;
import com.cray.software.justreminder.reminder.Type;
import com.cray.software.justreminder.services.AlarmReceiver;
import com.cray.software.justreminder.services.DeliveredReceiver;
import com.cray.software.justreminder.services.RepeatNotificationReceiver;
import com.cray.software.justreminder.services.SendReceiver;
import com.cray.software.justreminder.utils.ViewUtils;
import com.cray.software.justreminder.views.RoundImageView;
import com.cray.software.justreminder.views.TextDrawable;
import com.cray.software.justreminder.widgets.utils.UpdatesHelper;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.squareup.picasso.Picasso;

import jp.wasabeef.picasso.transformations.BlurTransformation;

public class ReminderDialog extends Activity implements TextToSpeech.OnInitListener, SendListener {
    private static final int MY_DATA_CHECK_CODE = 111;

    private FloatingActionButton buttonCall;
    private TextView remText;
    private RecyclerView todoList;

    private AlarmReceiver alarm = new AlarmReceiver();
    private RepeatNotificationReceiver repeater = new RepeatNotificationReceiver();
    private BroadcastReceiver deliveredReceiver, sentReceiver;

    private long id;
    private int repCode, color = -1, vibration, voice, notificationRepeat, wake, unlock, auto;
    private long count, limit;
    private int isMelody;
    private String melody, number, name, task, reminderType;
    private boolean isDark = false;
    private boolean isExtra = false;
    private int currVolume;

    private Type reminder;
    private Reminder item;

    private ShoppingListDataProvider provider;

    private SharedPrefs sPrefs;
    private ColorSetter cs = new ColorSetter(ReminderDialog.this);
    private Notifier notifier = new Notifier(ReminderDialog.this);
    private TextToSpeech tts;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sPrefs = new SharedPrefs(ReminderDialog.this);

        AudioManager am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        currVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        int prefsVol = sPrefs.loadInt(Prefs.VOLUME);
        float volPercent = (float) prefsVol / Configs.MAX_VOLUME;
        int maxVol = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int streamVol = (int) (maxVol * volPercent);
        am.setStreamVolume(AudioManager.STREAM_MUSIC, streamVol, 0);

        Intent res = getIntent();
        id = res.getLongExtra(Constants.ITEM_ID_INTENT, 0);
        isMelody = res.getIntExtra("int", 0);

        reminder = new Type(this);

        item = reminder.getItem(id);
        if (item != null) {
            task = item.getTitle();
            reminderType = item.getType();
            number = item.getNumber();
            melody = item.getMelody();
            repCode = item.getRepCode();
            color = item.getColor();
            vibration = item.getVibration();
            voice = item.getVoice();
            notificationRepeat = item.getNotificationRepeat();
            wake = item.getWake();
            unlock = item.getUnlock();
            auto = item.getAuto();
            limit = item.getLimit();
            count = item.getCount();
        } else {
            notifier.discardNotification(id);
            finish();
        }

        boolean isFull = sPrefs.loadBoolean(Prefs.UNLOCK_DEVICE);
        isExtra = sPrefs.loadBoolean(Prefs.EXTRA_OPTIONS);
        if (isExtra) {
            isFull = unlock == 1;
        }
        if (isFull) {
            runOnUiThread(new Runnable() {
                public void run() {
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                            | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                            | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
                }
            });
        }

        boolean isWake = sPrefs.loadBoolean(Prefs.WAKE_STATUS);
        if (isExtra) {
            isWake = wake == 1;
        }
        if (isWake) {
            PowerManager.WakeLock screenLock = ((PowerManager)getSystemService(POWER_SERVICE)).newWakeLock(
                    PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "TAG");
            screenLock.acquire();
            screenLock.release();
        }

        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setTheme(cs.getTransparentStyle());
        setContentView(R.layout.reminder_dialog_layout);

        if (Module.isLollipop()) {
            getWindow().setStatusBarColor(cs.getStatusBarStyle());
        }

        LinearLayout single_container = (LinearLayout) findViewById(R.id.single_container);
        single_container.setVisibility(View.VISIBLE);

        loadImage();

        FloatingActionButton buttonOk = (FloatingActionButton) findViewById(R.id.buttonOk);
        FloatingActionButton buttonEdit = (FloatingActionButton) findViewById(R.id.buttonEdit);
        FloatingActionButton buttonCancel = (FloatingActionButton) findViewById(R.id.buttonCancel);
        buttonCall = (FloatingActionButton) findViewById(R.id.buttonCall);
        FloatingActionButton buttonDelay = (FloatingActionButton) findViewById(R.id.buttonDelay);
        FloatingActionButton buttonDelayFor = (FloatingActionButton) findViewById(R.id.buttonDelayFor);
        FloatingActionButton buttonNotification = (FloatingActionButton) findViewById(R.id.buttonNotification);
        isDark = sPrefs.loadBoolean(Prefs.USE_DARK_THEME);
        colorify(buttonOk, buttonCall, buttonCancel, buttonDelay, buttonDelayFor,
                buttonNotification, buttonEdit);
        int mins = sPrefs.loadInt(Prefs.DELAY_TIME);
        setTextDrawable(buttonDelay, String.valueOf(mins));
        setTextDrawable(buttonDelayFor, "...");
        if (isDark){
            buttonOk.setIconDrawable(ViewUtils.getDrawable(this, R.drawable.ic_done_black_24dp));
            buttonEdit.setIconDrawable(ViewUtils.getDrawable(this, R.drawable.ic_create_black_24dp));
            buttonCancel.setIconDrawable(ViewUtils.getDrawable(this, R.drawable.ic_clear_black_24dp));
            buttonCall.setIconDrawable(ViewUtils.getDrawable(this, R.drawable.ic_call_black_24dp));
            buttonNotification.setIconDrawable(ViewUtils.getDrawable(this, R.drawable.ic_favorite_black_24dp));
        } else {
            buttonOk.setIconDrawable(ViewUtils.getDrawable(this, R.drawable.ic_done_white_24dp));
            buttonEdit.setIconDrawable(ViewUtils.getDrawable(this, R.drawable.ic_create_white_24dp));
            buttonCancel.setIconDrawable(ViewUtils.getDrawable(this, R.drawable.ic_clear_white_24dp));
            buttonCall.setIconDrawable(ViewUtils.getDrawable(this, R.drawable.ic_call_white_24dp));
            buttonNotification.setIconDrawable(ViewUtils.getDrawable(this, R.drawable.ic_favorite_white_24dp));
        }

        RoundImageView contactPhoto = (RoundImageView) findViewById(R.id.contactPhoto);
        contactPhoto.setVisibility(View.GONE);

        todoList = (RecyclerView) findViewById(R.id.todoList);
        todoList.setLayoutManager(new LinearLayoutManager(this));
        todoList.setVisibility(View.GONE);

        remText = (TextView) findViewById(R.id.remText);
        remText.setText("");
        String type = getType();
        if (type != null) {
            if (type.matches(Constants.TYPE_CALL) || type.matches(Constants.TYPE_LOCATION_CALL) ||
                    type.matches(Constants.TYPE_SKYPE) || type.matches(Constants.TYPE_SKYPE_VIDEO) ||
                    type.matches(Constants.TYPE_LOCATION_OUT_CALL)) {
                if (!type.startsWith(Constants.TYPE_SKYPE)) {
                    contactPhoto.setVisibility(View.VISIBLE);
                    long conID = Contacts.getContactIDFromNumber(number, ReminderDialog.this);
                    Bitmap photo = Contacts.getPhoto(this, conID);
                    if (photo != null) {
                        contactPhoto.setImageBitmap(photo);
                    } else {
                        contactPhoto.setVisibility(View.GONE);
                    }
                }
                name = Contacts.getContactNameFromNumber(number, ReminderDialog.this);
                remText.setText(task + "\n" + name + "\n" + number);
            } else if (type.matches(Constants.TYPE_LOCATION_MESSAGE) ||
                    type.matches(Constants.TYPE_LOCATION_OUT_MESSAGE) ||
                    type.matches(Constants.TYPE_MESSAGE) || type.matches(Constants.TYPE_SKYPE_CHAT)) {
                if (!sPrefs.loadBoolean(Prefs.SILENT_SMS)) {
                    remText.setText(task + "\n" + number);
                    buttonCall.setVisibility(View.VISIBLE);
                    if (isDark) {
                        buttonCall.setIconDrawable(ViewUtils.getDrawable(this, R.drawable.ic_send_black_24dp));
                    } else {
                        buttonCall.setIconDrawable(ViewUtils.getDrawable(this, R.drawable.ic_send_white_24dp));
                    }
                } else {
                    remText.setText(task + "\n" + number);
                    buttonCall.setVisibility(View.GONE);
                    buttonDelay.setVisibility(View.GONE);
                    buttonDelayFor.setVisibility(View.GONE);
                }
            } else if (type.matches(Constants.TYPE_APPLICATION)) {
                PackageManager packageManager = getPackageManager();
                ApplicationInfo applicationInfo = null;
                try {
                    applicationInfo = packageManager.getApplicationInfo(number, 0);
                } catch (final PackageManager.NameNotFoundException ignored) {
                }
                final String nameA = (String) ((applicationInfo != null) ?
                        packageManager.getApplicationLabel(applicationInfo) : "???");
                remText.setText(nameA);
                buttonCall.setVisibility(View.VISIBLE);
                if (isDark) {
                    buttonCall.setIconDrawable(ViewUtils.getDrawable(this, R.drawable.ic_open_in_browser_black_24dp));
                } else {
                    buttonCall.setIconDrawable(ViewUtils.getDrawable(this, R.drawable.ic_open_in_browser_white_24dp));
                }
            } else if (type.matches(Constants.TYPE_APPLICATION_BROWSER)) {
                remText.setText(number);
                buttonCall.setVisibility(View.VISIBLE);
                if (isDark) {
                    buttonCall.setIconDrawable(ViewUtils.getDrawable(this, R.drawable.ic_open_in_browser_black_24dp));
                } else {
                    buttonCall.setIconDrawable(ViewUtils.getDrawable(this, R.drawable.ic_open_in_browser_white_24dp));
                }
            } else if (type.matches(Constants.TYPE_SHOPPING_LIST)) {
                remText.setText(task);
                buttonCall.setVisibility(View.GONE);
                loadData();
            } else {
                remText.setText(task);
                buttonCall.setVisibility(View.GONE);
            }
        } else {
            remText.setText(task);
            buttonCall.setVisibility(View.GONE);
        }

        if (type.startsWith(Constants.TYPE_LOCATION) || type.startsWith(Constants.TYPE_LOCATION_OUT)){
            buttonDelay.setVisibility(View.GONE);
            buttonDelayFor.setVisibility(View.GONE);
        }

        if (repCode == 0 || (limit > 0 && (limit - count - 1 == 0))) {
            buttonCancel.setVisibility(View.GONE);
        } else {
            buttonCancel.setVisibility(View.VISIBLE);
        }

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alarm.cancelAlarm(getApplicationContext(), id);
                update(1);
                Reminder.disableReminder(id, ReminderDialog.this);
                Reminder.backup(ReminderDialog.this);
                Reminder.updateCount(ReminderDialog.this, id);
                finish();
            }
        });

        buttonNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                make();
                update(1);
                Reminder.updateCount(ReminderDialog.this, id);
                if ((task == null || task.trim().matches("")) &&
                        (number != null && !number.trim().matches(""))) {
                    notifier.showReminderNotification(name + " " + number, id);
                } else {
                    notifier.showReminderNotification(task, id);
                }
                finish();
            }
        });

        buttonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                make();
                update(1);
                Reminder.updateCount(ReminderDialog.this, id);
                finish();
            }
        });

        buttonEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                make();
                update(1);
                Reminder.updateCount(ReminderDialog.this, id);
                Reminder.edit(id, ReminderDialog.this);
                finish();
            }
        });

        buttonDelay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int delay = sPrefs.loadInt(Prefs.DELAY_TIME);
                Reminder.setDelay(ReminderDialog.this, id, delay, true);
                Reminder.backup(ReminderDialog.this);
                update(1);
                finish();
            }
        });
        buttonDelayFor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
                Reminder.backup(ReminderDialog.this);
                update(0);
            }
        });
        buttonCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String type = getType();
                if (type.matches(Constants.TYPE_LOCATION_MESSAGE) ||
                        type.matches(Constants.TYPE_MESSAGE) || type.matches(Constants.TYPE_LOCATION_OUT_MESSAGE)){
                    sendSMS(number, task);
                } else if (type.matches(Constants.TYPE_SKYPE)){
                    Telephony.skypeCall(number, ReminderDialog.this);
                } else if (type.matches(Constants.TYPE_SKYPE_VIDEO)){
                    Telephony.skypeVideoCall(number, ReminderDialog.this);
                } else if (type.matches(Constants.TYPE_SKYPE_CHAT)){
                    Telephony.skypeChat(number, ReminderDialog.this);
                } else if (type.matches(Constants.TYPE_APPLICATION)){
                    openApplication(number);
                } else if (type.matches(Constants.TYPE_APPLICATION_BROWSER)){
                    openLink(number);
                } else {
                    Telephony.makeCall(number, ReminderDialog.this);
                }
                make();
                Reminder.updateCount(ReminderDialog.this, id);
                update(1);
                if (!type.contains(Constants.TYPE_MESSAGE)){
                    finish();
                }
            }
        });
        boolean autoLaunch = sPrefs.loadBoolean(Prefs.APPLICATION_AUTO_LAUNCH);
        boolean silentSMS = sPrefs.loadBoolean(Prefs.SILENT_SMS);
        if (isExtra) {
            autoLaunch = auto == 1;
            silentSMS = auto == 1;
        }
        if (type != null) {
            if (type.matches(Constants.TYPE_MESSAGE) || type.matches(Constants.TYPE_LOCATION_MESSAGE) ||
                    type.matches(Constants.TYPE_LOCATION_OUT_MESSAGE)) {
                if (silentSMS) {
                    sendSMS(number, task);
                } else {
                    showReminder(1);
                }
            } else if (type.matches(Constants.TYPE_APPLICATION)) {
                if (autoLaunch) {
                    openApplication(number);
                } else {
                    showReminder(1);
                }
            } else if (type.matches(Constants.TYPE_APPLICATION_BROWSER)) {
                if (autoLaunch) {
                    openLink(number);
                } else {
                    showReminder(1);
                }
            } else {
                showReminder(1);
            }
        } else {
            showReminder(1);
        }

        boolean isRepeat = sPrefs.loadBoolean(Prefs.NOTIFICATION_REPEAT);
        if (isExtra) {
            isRepeat = notificationRepeat == 1;
        }
        if (isRepeat) {
            repeater.setAlarm(ReminderDialog.this, id);
        }

        boolean isTTS = sPrefs.loadBoolean(Prefs.TTS);
        if (isExtra) {
            isTTS = voice == 1;
        }
        if (isTTS) {
            Intent checkTTSIntent = new Intent();
            checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
            try {
                startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);
            } catch (ActivityNotFoundException e){
                e.printStackTrace();
            }
        }
    }

    private void loadImage() {
        ImageView bgImage = (ImageView) findViewById(R.id.bgImage);
        bgImage.setVisibility(View.GONE);
        String imagePrefs = sPrefs.loadPrefs(Prefs.REMINDER_IMAGE);
        boolean blur = sPrefs.loadBoolean(Prefs.REMINDER_IMAGE_BLUR);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        if (imagePrefs.matches(Constants.DEFAULT)){
            if (blur && Module.isPro()) {
                Picasso.with(ReminderDialog.this)
                        .load(R.drawable.photo)
                        .resize(metrics.heightPixels, metrics.widthPixels)
                        .transform(new BlurTransformation(this, 15, 2))
                        .into(bgImage);
            } else {
                Picasso.with(ReminderDialog.this)
                        .load(R.drawable.photo)
                        .resize(metrics.heightPixels, metrics.widthPixels)
                        .into(bgImage);
            }
            bgImage.setVisibility(View.VISIBLE);
        } else if (imagePrefs.matches(Constants.NONE)){
            bgImage.setVisibility(View.GONE);
        } else {
            if (blur && Module.isPro()) {
                Picasso.with(ReminderDialog.this)
                        .load(Uri.parse(imagePrefs))
                        .resize(metrics.heightPixels, metrics.widthPixels)
                        .transform(new BlurTransformation(this, 15, 2))
                        .into(bgImage);
            } else {
                Picasso.with(ReminderDialog.this)
                        .load(Uri.parse(imagePrefs))
                        .resize(metrics.heightPixels, metrics.widthPixels)
                        .into(bgImage);
            }
            bgImage.setVisibility(View.VISIBLE);
        }
    }

    private void loadData() {
        provider = new ShoppingListDataProvider(this, id, ShoppingList.ACTIVE);
        TaskListRecyclerAdapter shoppingAdapter = new TaskListRecyclerAdapter(this, provider, new TaskListRecyclerAdapter.ActionListener() {
            @Override
            public void onItemCheck(int position, boolean isChecked) {
                ShoppingList.switchItem(ReminderDialog.this, provider.getItem(position).getId(), isChecked);
                loadData();
            }

            @Override
            public void onItemDelete(int position) {
                ShoppingList.hideItem(ReminderDialog.this, provider.getItem(position).getId());
                loadData();
            }

            @Override
            public void onItemChange(int position) {
                ShoppingList.showItem(ReminderDialog.this, provider.getItem(position).getId());
                loadData();
            }
        });
        todoList.setAdapter(shoppingAdapter);
        todoList.setVisibility(View.VISIBLE);
    }

    private String getType(){
        if (reminderType != null) {
            return reminderType;
        } else {
            if (item != null){
                return item.getType();
            } else {
                if (id != 0){
                    return reminder.getItem(id).getType();
                } else {
                    return "";
                }
            }
        }
    }

    private void update(int i){
        if (i == 1) {
            removeFlags();
        }
        repeater.cancelAlarm(ReminderDialog.this, id);
        notifier.discardNotification(id);
    }

    private void make(){
        if (repCode == 0 || (limit > 0 && (limit - count - 1 == 0))){
            Reminder.disableReminder(id, ReminderDialog.this);
        } else {
            Reminder.generateToCalendar(id, this);
        }
        Reminder.backup(this);
    }

    private void setTextDrawable(FloatingActionButton button, String text){
        TextDrawable drawable = TextDrawable.builder()
                .beginConfig()
                .textColor(isDark ? Color.DKGRAY : Color.WHITE)
                .useFont(Typeface.DEFAULT)
                .fontSize(30) /* size in px */
                .bold()
                .toUpperCase()
                .endConfig()
                .buildRound(text, Color.TRANSPARENT);
        button.setIconDrawable(drawable);
    }

    private void colorify(FloatingActionButton... fab){
        for (FloatingActionButton button : fab){
            if (isDark){
                button.setColorNormal(getResources().getColor(R.color.whitePrimary));
                button.setColorPressed(getResources().getColor(R.color.material_divider));
            } else {
                button.setColorNormal(getResources().getColor(R.color.material_divider));
                button.setColorPressed(getResources().getColor(R.color.whitePrimary));
            }
        }
    }

    private void showReminder(int i){
        sPrefs = new SharedPrefs(ReminderDialog.this);
        String type = getType();
        boolean isTTS = sPrefs.loadBoolean(Prefs.TTS);
        if (isMelody == 1) {
            i = 0;
        }
        if (isExtra) {
            isTTS = voice == 1;
        }
        if (!isTTS) {
            notifier.showReminder(task, type, i, id, melody, color, vibration == 1, isExtra);
        } else {
            notifier.showTTSNotification(task, type, id, color, vibration == 1, isExtra);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MY_DATA_CHECK_CODE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                tts = new TextToSpeech(this, this);
            } else {
                Intent installTTSIntent = new Intent();
                installTTSIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                try {
                    startActivity(installTTSIntent);
                } catch (ActivityNotFoundException e){
                    e.printStackTrace();
                }
            }
        }
    }

    public void openLink(String number) {
        Telephony.openLink(number, this);
        notifier.discardNotification(id);
        make();
        repeater.cancelAlarm(ReminderDialog.this, id);
        finish();
    }

    public void openApplication(String number) {
        Telephony.openApp(number, this);
        notifier.discardNotification(id);
        make();
        repeater.cancelAlarm(ReminderDialog.this, id);
        finish();
    }

    public void showDialog(){
        final CharSequence[] items = {getString(R.string.repeat_5_min),
                getString(R.string.repeat_10_min),
                getString(R.string.repeat_15_min),
                getString(R.string.repeat_30_min),
                getString(R.string.repeat_45_min),
                getString(R.string.repeat_60_min),
                getString(R.string.repeat_90_min),
                getString(R.string.repeat_120_min),
                getString(R.string.string_5_hours),
                getString(R.string.string_1_day),
                getString(R.string.string_2_days),
                getString(R.string.string_1_week)};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.time_dialog_title));
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                int x = 0;
                if (item == 0) {
                    x = 5;
                } else if (item == 1) {
                    x = 10;
                } else if (item == 2) {
                    x = 15;
                } else if (item == 3) {
                    x = 30;
                } else if (item == 4) {
                    x = 45;
                } else if (item == 5) {
                    x = 60;
                } else if (item == 6) {
                    x = 90;
                } else if (item == 7) {
                    x = 120;
                } else if (item == 8) {
                    x = 60 * 5;
                } else if (item == 9) {
                    x = 60 * 24;
                } else if (item == 10) {
                    x = 60 * 24 * 2;
                } else if (item == 11) {
                    x = 60 * 24 * 7;
                }

                Reminder.setDelay(ReminderDialog.this, id, x, true);

                if (x < 100) {
                    Messages.toast(ReminderDialog.this, getString(R.string.repeat_toast_start) + " " +
                            x + " " + getString(R.string.repeat_toast_end));
                }
                if (x > 120 && x < 60 * 24) {
                    Messages.toast(ReminderDialog.this, getString(R.string.repeat_toast_start) + " " +
                            (x / 60) + " " + getString(R.string.string_hours));
                }
                if (x >= 60 * 24 && x < 60 * 24 * 7) {
                    Messages.toast(ReminderDialog.this, getString(R.string.repeat_toast_start) + " " +
                            (x / (60 * 24)) + " " + getString(R.string.string_days));
                }
                if (x == 60 * 24 * 7) {
                    Messages.toast(ReminderDialog.this, getString(R.string.repeat_toast_start) + " " +
                            1 + " " + getString(R.string.simple_week));
                }
                dialog.dismiss();
                removeFlags();
                finish();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void sendSMS(String phoneNumber, String message) {
        String SENT = "SMS_SENT";
        String DELIVERED = "SMS_DELIVERED";

        PendingIntent sentPI = PendingIntent.getBroadcast(ReminderDialog.this, 0,
                new Intent(SENT), 0);
        PendingIntent deliveredPI = PendingIntent.getBroadcast(ReminderDialog.this,
                0, new Intent(DELIVERED), 0);
        
        registerReceiver(sentReceiver = new SendReceiver(this), new IntentFilter(SENT));
        registerReceiver(deliveredReceiver = new DeliveredReceiver(), new IntentFilter(DELIVERED));

        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);
    }

    public void removeFlags(){
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }

        new UpdatesHelper(ReminderDialog.this).updateWidget();
        Reminder.updateDate(this, id);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (MotionEvent.ACTION_DOWN == event.getAction()){
            notifier.discardMedia();
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onDestroy() {
        if (sentReceiver != null) {
            unregisterReceiver(sentReceiver);
        }
        if (deliveredReceiver != null) {
            unregisterReceiver(deliveredReceiver);
        }
        notifier.recreatePermanent();
        removeFlags();
        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        am.setStreamVolume(AudioManager.STREAM_MUSIC, currVolume, 0);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        notifier.discardMedia();
        if (new SharedPrefs(ReminderDialog.this)
                .loadBoolean(Prefs.SMART_FOLD)){
            moveTaskToBack(true);
            repeater.cancelAlarm(ReminderDialog.this, id);
            removeFlags();
        } else {
            Messages.toast(ReminderDialog.this, getString(R.string.must_click_message));
        }
    }

    @Override
    public void onInit(int status) {
        sPrefs = new SharedPrefs(ReminderDialog.this);
        if(status == TextToSpeech.SUCCESS){
            int result = tts.setLanguage(new Language().getLocale(ReminderDialog.this, false));
            if(result == TextToSpeech.LANG_MISSING_DATA ||
                    result == TextToSpeech.LANG_NOT_SUPPORTED){
                Log.e("error", "This Language is not supported");
            } else{
                if (task != null && !task.matches("")) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        tts.speak(task, TextToSpeech.QUEUE_FLUSH, null, null);
                    } else {
                        tts.speak(task, TextToSpeech.QUEUE_FLUSH, null);
                    }
                }
            }
        } else {
            Log.e("error", "Initialization Failed!");
        }
    }

    @Override
    public void messageSendResult(boolean isSent) {
        if (isSent) {
            finish();
        } else {
            showReminder(0);
            remText.setText(getString(R.string.message_send_error));
            if (isDark) {
                buttonCall.setIconDrawable(ViewUtils.getDrawable(ReminderDialog.this, R.drawable.ic_cached_black_24dp));
            } else {
                buttonCall.setIconDrawable(ViewUtils.getDrawable(ReminderDialog.this, R.drawable.ic_cached_white_24dp));
            }
            if (buttonCall.getVisibility() == View.GONE) {
                buttonCall.setVisibility(View.VISIBLE);
            }
        }
    }
}