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
import android.os.Handler;
import android.os.PowerManager;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.CardView;
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
import com.cray.software.justreminder.ReminderApp;
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
import com.cray.software.justreminder.json.JAction;
import com.cray.software.justreminder.json.JLed;
import com.cray.software.justreminder.json.JMelody;
import com.cray.software.justreminder.json.JModel;
import com.cray.software.justreminder.json.JRecurrence;
import com.cray.software.justreminder.modules.Module;
import com.cray.software.justreminder.reminder.Reminder;
import com.cray.software.justreminder.reminder.Type;
import com.cray.software.justreminder.services.DeliveredReceiver;
import com.cray.software.justreminder.services.RepeatNotificationReceiver;
import com.cray.software.justreminder.services.SendReceiver;
import com.cray.software.justreminder.utils.TimeUtil;
import com.cray.software.justreminder.utils.ViewUtils;
import com.cray.software.justreminder.views.RoundImageView;
import com.cray.software.justreminder.views.TextDrawable;
import com.cray.software.justreminder.widgets.utils.UpdatesHelper;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.squareup.picasso.Picasso;

import jp.wasabeef.picasso.transformations.BlurTransformation;

public class ReminderDialog extends Activity implements TextToSpeech.OnInitListener, SendListener {
    private static final int MY_DATA_CHECK_CODE = 111;

    private FloatingActionButton buttonCall;
    private TextView remText;
    private RecyclerView todoList;

    private RepeatNotificationReceiver repeater = new RepeatNotificationReceiver();
    private BroadcastReceiver deliveredReceiver, sentReceiver;

    private long id;
    private int color = -1, vibration, voice, notificationRepeat, wake, unlock, auto;
    private long count, limit;
    private long repeaCode;
    private int isMelody;
    private String melody, number, name, task, reminderType, subject, attachment;

    private int currVolume;
    private int streamVol;
    private int mVolume;
    private int mStream;

    private Type reminder;
    private JModel item;

    private ShoppingListDataProvider provider;

    private ColorSetter cs = new ColorSetter(ReminderDialog.this);
    private Notifier notifier = new Notifier(ReminderDialog.this);
    private TextToSpeech tts;
    private Handler handler = new Handler();

    private Tracker mTracker;

    /**
     * Runnable for increasing volume in stream.
     */
    private Runnable increaseVolume = new Runnable() {
        @Override
        public void run() {
            if (mVolume < streamVol) {
                mVolume++;
                handler.postDelayed(increaseVolume, 750);
                AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                am.setStreamVolume(mStream, mVolume, 0);
            } else handler.removeCallbacks(increaseVolume);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("----ON_REMINDER-----", TimeUtil.getFullDateTime(System.currentTimeMillis(), true));

        SharedPrefs prefs = new SharedPrefs(ReminderDialog.this);
        boolean systemVol = prefs.loadBoolean(Prefs.SYSTEM_VOLUME);
        boolean increasing = prefs.loadBoolean(Prefs.INCREASING_VOLUME);
        if (systemVol) {
            mStream = prefs.loadInt(Prefs.SOUND_STREAM);
            AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            currVolume = am.getStreamVolume(mStream);
            streamVol = currVolume;
            mVolume = currVolume;
            if (increasing) {
                mVolume = 0;
                handler.postDelayed(increaseVolume, 750);
            }
            am.setStreamVolume(mStream, mVolume, 0);
        } else {
            AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            mStream = 3;
            currVolume = am.getStreamVolume(mStream);
            int prefsVol = prefs.loadInt(Prefs.VOLUME);
            float volPercent = (float) prefsVol / Configs.MAX_VOLUME;
            int maxVol = am.getStreamMaxVolume(mStream);
            streamVol = (int) (maxVol * volPercent);
            mVolume = streamVol;
            if (increasing) {
                mVolume = 0;
                handler.postDelayed(increaseVolume, 750);
            }
            am.setStreamVolume(mStream, mVolume, 0);
        }

        Intent res = getIntent();
        id = res.getLongExtra(Constants.ITEM_ID_INTENT, 0);
        isMelody = res.getIntExtra("int", 0);

        reminder = new Type(this);

        item = reminder.getItem(id);
        if (item != null) {
            task = item.getSummary();
            reminderType = item.getType();

            JAction jAction = item.getAction();
            number = jAction.getTarget();
            subject = jAction.getSubject();
            attachment = jAction.getAttachment();
            auto = jAction.getAuto();

            JMelody jMelody = item.getMelody();
            melody = jMelody.getMelodyPath();

            JRecurrence jRecurrence = item.getRecurrence();
            repeaCode = jRecurrence.getRepeat();
            limit = jRecurrence.getLimit();

            JLed jLed = item.getLed();
            color = jLed.getColor();

            vibration = item.getVibrate();
            voice = item.getVoice();
            notificationRepeat = item.getNotificationRepeat();
            wake = item.getAwake();
            unlock = item.getUnlock();

            count = item.getCount();
        } else {
            notifier.discardNotification(id);
            finish();
        }

        boolean isFull = prefs.loadBoolean(Prefs.UNLOCK_DEVICE);
        if (unlock != -1) {
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

        boolean isWake = prefs.loadBoolean(Prefs.WAKE_STATUS);
        if (wake != -1) {
            isWake = wake == 1;
        }
        if (isWake) {
            PowerManager.WakeLock screenLock = ((PowerManager)getSystemService(POWER_SERVICE)).newWakeLock(
                    PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "TAG");
            screenLock.acquire();
            screenLock.release();
        }

        boolean autoLaunch = prefs.loadBoolean(Prefs.APPLICATION_AUTO_LAUNCH);
        boolean silentSMS = prefs.loadBoolean(Prefs.SILENT_SMS);
        if (auto != -1) {
            autoLaunch = auto == 1;
            silentSMS = auto == 1;
        }

        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setTheme(cs.getTransparentStyle());
        setContentView(R.layout.reminder_dialog_layout);

        if (Module.isLollipop()) {
            getWindow().setStatusBarColor(cs.getStatusBarStyle());
        }

        CardView card = (CardView) findViewById(R.id.card);
        card.setCardBackgroundColor(cs.getCardStyle());
        if (Module.isLollipop()) card.setCardElevation(Configs.CARD_ELEVATION_REMINDER);

        LinearLayout single_container = (LinearLayout) findViewById(R.id.single_container);
        LinearLayout container = (LinearLayout) findViewById(R.id.container);
        LinearLayout subjectContainer = (LinearLayout) findViewById(R.id.subjectContainer);
        single_container.setVisibility(View.VISIBLE);
        container.setVisibility(View.GONE);
        subjectContainer.setVisibility(View.GONE);

        loadImage();

        FloatingActionButton buttonOk = (FloatingActionButton) findViewById(R.id.buttonOk);
        FloatingActionButton buttonEdit = (FloatingActionButton) findViewById(R.id.buttonEdit);
        FloatingActionButton buttonCancel = (FloatingActionButton) findViewById(R.id.buttonCancel);
        buttonCall = (FloatingActionButton) findViewById(R.id.buttonCall);
        FloatingActionButton buttonDelay = (FloatingActionButton) findViewById(R.id.buttonDelay);
        FloatingActionButton buttonDelayFor = (FloatingActionButton) findViewById(R.id.buttonDelayFor);
        FloatingActionButton buttonNotification = (FloatingActionButton) findViewById(R.id.buttonNotification);
        colorify(buttonOk, buttonCall, buttonCancel, buttonDelay, buttonDelayFor,
                buttonNotification, buttonEdit);
        int mins = prefs.loadInt(Prefs.DELAY_TIME);
        setTextDrawable(buttonDelay, String.valueOf(mins));
        setTextDrawable(buttonDelayFor, "...");
        buttonOk.setImageResource(R.drawable.ic_done_black_24dp);
        buttonEdit.setImageResource(R.drawable.ic_create_black_24dp);
        buttonCancel.setImageResource(R.drawable.ic_clear_black_24dp);
        buttonCall.setImageResource(R.drawable.ic_call_black_24dp);
        buttonNotification.setImageResource(R.drawable.ic_favorite_black_24dp);

        RoundImageView contactPhoto = (RoundImageView) findViewById(R.id.contactPhoto);
        contactPhoto.setVisibility(View.GONE);

        todoList = (RecyclerView) findViewById(R.id.todoList);
        todoList.setLayoutManager(new LinearLayoutManager(this));
        todoList.setVisibility(View.GONE);

        remText = (TextView) findViewById(R.id.remText);
        TextView contactInfo = (TextView) findViewById(R.id.contactInfo);
        TextView subjectView = (TextView) findViewById(R.id.subjectView);
        TextView messageView = (TextView) findViewById(R.id.messageView);
        remText.setText("");

        String type = getType();
        if (type.contains(Constants.TYPE_CALL) || type.matches(Constants.TYPE_SKYPE) ||
                type.matches(Constants.TYPE_SKYPE_VIDEO)) {
            if (!type.startsWith(Constants.TYPE_SKYPE)) {
                contactPhoto.setVisibility(View.VISIBLE);
                long conID = Contacts.getIdFromNumber(number, ReminderDialog.this);
                Bitmap photo = Contacts.getPhoto(this, conID);
                if (photo != null) {
                    contactPhoto.setImageBitmap(photo);
                } else {
                    contactPhoto.setVisibility(View.GONE);
                }
                name = Contacts.getNameFromNumber(number, ReminderDialog.this);
                if (name == null) name = "";
                remText.setText(R.string.make_call);
                contactInfo.setText(name + "\n" + number);
                messageView.setText(task);
            } else {
                if (type.matches(Constants.TYPE_SKYPE_VIDEO))
                    remText.setText(R.string.video_call);
                else remText.setText(R.string.skype_call);
                contactInfo.setText(number);
                messageView.setText(task);
            }
            container.setVisibility(View.VISIBLE);
        } else if (type.contains(Constants.TYPE_MESSAGE) ||
                type.matches(Constants.TYPE_SKYPE_CHAT)) {
            if (type.contains(Constants.TYPE_MESSAGE)) {
                contactPhoto.setVisibility(View.VISIBLE);
                long conID = Contacts.getIdFromNumber(number, ReminderDialog.this);
                Bitmap photo = Contacts.getPhoto(this, conID);
                if (photo != null) {
                    contactPhoto.setImageBitmap(photo);
                } else {
                    contactPhoto.setVisibility(View.GONE);
                }
                name = Contacts.getNameFromNumber(number, ReminderDialog.this);
                if (name == null) name = "";
                remText.setText(R.string.send_sms);
                contactInfo.setText(name + "\n" + number);
                messageView.setText(task);
            } else {
                remText.setText(R.string.skype_chat);
                contactInfo.setText(number);
                messageView.setText(task);
            }
            if (!silentSMS) {
                buttonCall.setVisibility(View.VISIBLE);
                buttonCall.setImageResource(R.drawable.ic_send_black_24dp);
            } else {
                buttonCall.setVisibility(View.GONE);
                buttonDelay.setVisibility(View.GONE);
                buttonDelayFor.setVisibility(View.GONE);
            }
            container.setVisibility(View.VISIBLE);
        } else if (type.matches(Constants.TYPE_MAIL)) {
            buttonCall.setVisibility(View.VISIBLE);
            buttonCall.setImageResource(R.drawable.ic_send_black_24dp);
            remText.setText(R.string.e_mail);
            int conID = Contacts.getIdFromMail(number, this);
            if (conID != 0) {
                Bitmap photo = Contacts.getPhoto(this, conID);
                if (photo != null) {
                    contactPhoto.setImageBitmap(photo);
                } else {
                    contactPhoto.setVisibility(View.GONE);
                }
                name = Contacts.getNameFromMail(number, ReminderDialog.this);
                if (name == null) name = "";
                contactInfo.setText(name + "\n" + number);
            } else {
                contactInfo.setText(number);
            }

            messageView.setText(task);
            subjectView.setText(subject);
            container.setVisibility(View.VISIBLE);
            subjectContainer.setVisibility(View.VISIBLE);
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
            buttonCall.setImageResource(R.drawable.ic_open_in_browser_black_24dp);
        } else if (type.matches(Constants.TYPE_APPLICATION_BROWSER)) {
            remText.setText(number);
            buttonCall.setVisibility(View.VISIBLE);
            buttonCall.setImageResource(R.drawable.ic_open_in_browser_black_24dp);
        } else if (type.matches(Constants.TYPE_SHOPPING_LIST)) {
            remText.setText(task);
            buttonCall.setVisibility(View.GONE);
            loadData();
        } else {
            remText.setText(task);
            buttonCall.setVisibility(View.GONE);
        }

        if (type.startsWith(Constants.TYPE_LOCATION) || type.startsWith(Constants.TYPE_LOCATION_OUT)){
            buttonDelay.setVisibility(View.GONE);
            buttonDelayFor.setVisibility(View.GONE);
        }

        if ((repeaCode == 0 || (limit > 0 && (limit - count - 1 == 0))) &&
                !type.startsWith(Constants.TYPE_WEEKDAY) && !type.contains(Constants.TYPE_MONTHDAY)) {
            buttonCancel.setVisibility(View.GONE);
        } else {
            buttonCancel.setVisibility(View.VISIBLE);
        }

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Reminder.disableReminder(id, ReminderDialog.this);
                update(1);
                finish();
            }
        });

        buttonNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Reminder.update(ReminderDialog.this, id);
                if ((task == null || task.trim().matches("")) &&
                        (number != null && !number.trim().matches(""))) {
                    notifier.showReminderNotification(name + " " + number, id);
                } else {
                    notifier.showReminderNotification(task, id);
                }
                update(1);
                finish();
            }
        });

        buttonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Reminder.update(ReminderDialog.this, id);
                update(1);
                finish();
            }
        });

        buttonEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Reminder.edit(id, ReminderDialog.this);
                update(1);
                finish();
            }
        });

        buttonDelay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int delay = new SharedPrefs(ReminderDialog.this).loadInt(Prefs.DELAY_TIME);
                Reminder.setDelay(ReminderDialog.this, id, delay, true);
                update(1);
                finish();
            }
        });
        buttonDelayFor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
                update(0);
            }
        });
        buttonCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Reminder.update(ReminderDialog.this, id);
                String type = getType();
                if (type.contains(Constants.TYPE_MESSAGE)){
                    sendSMS(number, task);
                } else if (type.matches(Constants.TYPE_SKYPE)){
                    Telephony.skypeCall(number, ReminderDialog.this);
                } else if (type.matches(Constants.TYPE_SKYPE_VIDEO)){
                    Telephony.skypeVideoCall(number, ReminderDialog.this);
                } else if (type.matches(Constants.TYPE_SKYPE_CHAT)){
                    Telephony.skypeChat(number, ReminderDialog.this);
                } else if (type.contains(Constants.TYPE_APPLICATION)){
                    openApplication(number);
                } else if (type.matches(Constants.TYPE_MAIL)){
                    Telephony.sendMail(ReminderDialog.this, number, subject, task, attachment);
                } else {
                    Telephony.makeCall(number, ReminderDialog.this);
                }
                update(1);
                if (!type.contains(Constants.TYPE_MESSAGE)){
                    finish();
                }
            }
        });

        if (type.contains(Constants.TYPE_MESSAGE)) {
            if (silentSMS) {
                sendSMS(number, task);
            } else {
                showReminder(1);
            }
        } else if (type.contains(Constants.TYPE_APPLICATION)) {
            if (autoLaunch) {
                openApplication(number);
            } else {
                showReminder(1);
            }
        } else {
            showReminder(1);
        }

        boolean isRepeat = prefs.loadBoolean(Prefs.NOTIFICATION_REPEAT);
        if (notificationRepeat != -1) {
            isRepeat = notificationRepeat == 1;
        }
        if (isRepeat) {
            repeater.setAlarm(ReminderDialog.this, id);
        }

        boolean isTTS = prefs.loadBoolean(Prefs.TTS);
        if (voice != -1) {
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

        ReminderApp application = (ReminderApp) getApplication();
        mTracker = application.getDefaultTracker();
    }

    private void loadImage() {
        ImageView bgImage = (ImageView) findViewById(R.id.bgImage);
        bgImage.setVisibility(View.GONE);
        SharedPrefs prefs = new SharedPrefs(this);
        String imagePrefs = prefs.loadPrefs(Prefs.REMINDER_IMAGE);
        boolean blur = prefs.loadBoolean(Prefs.REMINDER_IMAGE_BLUR);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;
        if (imagePrefs.matches(Constants.DEFAULT)){
            if (blur && Module.isPro()) {
                Picasso.with(ReminderDialog.this)
                        .load(R.drawable.photo)
                        .resize(width, height)
                        .transform(new BlurTransformation(this, 15, 2))
                        .into(bgImage);
            } else {
                Picasso.with(ReminderDialog.this)
                        .load(R.drawable.photo)
                        .resize(width, height)
                        .into(bgImage);
            }
            bgImage.setVisibility(View.VISIBLE);
        } else if (imagePrefs.matches(Constants.NONE)){
            bgImage.setVisibility(View.GONE);
        } else {
            if (blur && Module.isPro()) {
                Picasso.with(ReminderDialog.this)
                        .load(Uri.parse(imagePrefs))
                        .resize(width, height)
                        .transform(new BlurTransformation(this, 15, 2))
                        .into(bgImage);
            } else {
                Picasso.with(ReminderDialog.this)
                        .load(Uri.parse(imagePrefs))
                        .resize(width, height)
                        .into(bgImage);
            }
            bgImage.setVisibility(View.VISIBLE);
        }
    }

    private void loadData() {
        provider = new ShoppingListDataProvider(item.getShoppings(), false);
        TaskListRecyclerAdapter shoppingAdapter = new TaskListRecyclerAdapter(this, provider, new TaskListRecyclerAdapter.ActionListener() {
            @Override
            public void onItemCheck(int position, boolean isChecked) {
                ShoppingList.switchItem(ReminderDialog.this, id, isChecked, provider.getItem(position).getUuId());
                loadData();
            }

            @Override
            public void onItemDelete(int position) {
                ShoppingList.hideItem(ReminderDialog.this, id, provider.getItem(position).getUuId());
                loadData();
            }

            @Override
            public void onItemChange(int position) {
                ShoppingList.showItem(ReminderDialog.this, id, provider.getItem(position).getUuId());
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
                    return Constants.TYPE_REMINDER;
                }
            }
        }
    }

    private void update(int i){
        if (i == 1) {
            removeFlags();
        }
        handler.removeCallbacks(increaseVolume);
        new RepeatNotificationReceiver().cancelAlarm(ReminderDialog.this, id);
        notifier.discardNotification(id);
    }

    private void setTextDrawable(FloatingActionButton button, String text){
        TextDrawable drawable = TextDrawable.builder()
                .beginConfig()
                .textColor(Color.BLACK)
                .useFont(Typeface.MONOSPACE)
                .fontSize(30) /* size in px */
                .bold()
                .toUpperCase()
                .endConfig()
                .buildRound(text, Color.TRANSPARENT);
        button.setImageDrawable(drawable);
    }

    private void colorify(FloatingActionButton... fab){
        for (FloatingActionButton button : fab){
            button.setBackgroundTintList(ViewUtils.getFabState(this, cs.colorAccent(), cs.colorPrimary()));
        }
    }

    private void showReminder(int i){
        SharedPrefs prefs = new SharedPrefs(ReminderDialog.this);
        String type = getType();
        boolean isTTS = prefs.loadBoolean(Prefs.TTS);
        if (isMelody == 1) {
            i = 0;
        }
        if (voice != -1) {
            isTTS = voice == 1;
        }
        boolean isV = prefs.loadBoolean(Prefs.VIBRATION_STATUS);
        if (vibration != -1) isV = vibration == 1;
        if (!isTTS) {
            notifier.showReminder(task, type, i, id, melody, color, isV);
        } else {
            notifier.showTTSNotification(task, type, id, color, isV);
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

    public void openApplication(String number) {
        if (getType().matches(Constants.TYPE_APPLICATION)) {
            Telephony.openApp(number, this);
        } else {
            Telephony.openLink(number, this);
        }

        notifier.discardNotification(id);
        repeater.cancelAlarm(ReminderDialog.this, id);
        finish();
    }

    public void showDialog(){
        final CharSequence[] items = {String.format(getString(R.string.x_minutes), 5),
                String.format(getString(R.string.x_minutes), 10),
                String.format(getString(R.string.x_minutes), 15),
                String.format(getString(R.string.x_minutes), 30),
                String.format(getString(R.string.x_minutes), 45),
                String.format(getString(R.string.x_minutes), 60),
                String.format(getString(R.string.x_minutes), 90),
                String.format(getString(R.string.x_hours), 2),
                String.format(getString(R.string.x_hours), 6),
                String.format(getString(R.string.x_hours), 24),
                String.format(getString(R.string.x_days), 2),
                String.format(getString(R.string.x_days), 7)};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.choose_time));
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
                    x = 60 * 6;
                } else if (item == 9) {
                    x = 60 * 24;
                } else if (item == 10) {
                    x = 60 * 24 * 2;
                } else if (item == 11) {
                    x = 60 * 24 * 7;
                }

                Reminder.setDelay(ReminderDialog.this, id, x, true);
                Messages.toast(ReminderDialog.this, getString(R.string.reminder_snoozed));
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
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (MotionEvent.ACTION_DOWN == event.getAction()){
            notifier.discardMedia();
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTracker.setScreenName("Reminder " + getType());
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (sentReceiver != null) {
            unregisterReceiver(sentReceiver);
        }
        if (deliveredReceiver != null) {
            unregisterReceiver(deliveredReceiver);
        }
        notifier.recreatePermanent();
        removeFlags();
        if (!new SharedPrefs(this).loadBoolean(Prefs.SYSTEM_VOLUME)) {
            AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            am.setStreamVolume(mStream, currVolume, 0);
        }
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
            Messages.toast(ReminderDialog.this, getString(R.string.select_one_of_item));
        }
    }

    @Override
    public void onInit(int status) {
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
            remText.setText(getString(R.string.error_sending));
            buttonCall.setImageResource(R.drawable.ic_cached_black_24dp);
            if (buttonCall.getVisibility() == View.GONE) {
                buttonCall.setVisibility(View.VISIBLE);
            }
        }
    }
}