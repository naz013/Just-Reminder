/**
 * Copyright 2015 Nazar Suhovich
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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.backdoor.shared.SharedConst;
import com.cray.software.justreminder.R;
import com.cray.software.justreminder.ReminderApp;
import com.cray.software.justreminder.adapters.TaskListRecyclerAdapter;
import com.cray.software.justreminder.app_widgets.UpdatesHelper;
import com.cray.software.justreminder.constants.Configs;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.Language;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.contacts.Contacts;
import com.cray.software.justreminder.datas.ShoppingListDataProvider;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.Messages;
import com.cray.software.justreminder.helpers.Notifier;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.Telephony;
import com.cray.software.justreminder.interfaces.SendListener;
import com.cray.software.justreminder.modules.Module;
import com.cray.software.justreminder.reminder.json.JAction;
import com.cray.software.justreminder.reminder.json.JLed;
import com.cray.software.justreminder.reminder.json.JMelody;
import com.cray.software.justreminder.reminder.json.JRecurrence;
import com.cray.software.justreminder.reminder.json.JShopping;
import com.cray.software.justreminder.reminder.json.JsonModel;
import com.cray.software.justreminder.services.DeliveredReceiver;
import com.cray.software.justreminder.services.RepeatNotificationReceiver;
import com.cray.software.justreminder.services.SendReceiver;
import com.cray.software.justreminder.utils.LocationUtil;
import com.cray.software.justreminder.utils.TimeUtil;
import com.cray.software.justreminder.utils.ViewUtils;
import com.cray.software.justreminder.views.TextDrawable;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import jp.wasabeef.picasso.transformations.BlurTransformation;

public class ReminderDialog extends Activity implements TextToSpeech.OnInitListener, SendListener,
        GoogleApiClient.ConnectionCallbacks, DataApi.DataListener {
    private static final int MY_DATA_CHECK_CODE = 111;

    private FloatingActionButton buttonCall, buttonDelay, buttonCancel;
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

    private ReminderItem item;
    private List<JShopping> shoppings;

    private ShoppingListDataProvider provider;

    private ColorSetter cs = ColorSetter.getInstance(ReminderDialog.this);
    private Notifier notifier = new Notifier(ReminderDialog.this);
    private TextToSpeech tts;
    private Handler handler = new Handler();

    private Tracker mTracker;
    private GoogleApiClient mGoogleApiClient;
    private ProgressDialog mSendDialog;

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
        Intent res = getIntent();
        id = res.getLongExtra(Constants.ITEM_ID_INTENT, 0);
        isMelody = res.getIntExtra("int", 0);
        if (id == 0) finish();

        Log.d("----ON_REMINDER-----", TimeUtil.getFullDateTime(System.currentTimeMillis(), true));

        SharedPrefs prefs = SharedPrefs.getInstance(this);
        boolean systemVol = prefs.getBoolean(Prefs.SYSTEM_VOLUME);
        boolean increasing = prefs.getBoolean(Prefs.INCREASING_VOLUME);
        if (systemVol) {
            mStream = prefs.getInt(Prefs.SOUND_STREAM);
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
            int prefsVol = prefs.getInt(Prefs.VOLUME);
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

        item = ReminderHelper.getInstance(this).getReminder(id);
        if (item != null) {
            task = item.getSummary();
            reminderType = item.getType();
            JsonModel model = item.getModel();
            JAction jAction = model.getAction();
            number = jAction.getTarget();
            subject = jAction.getSubject();
            attachment = jAction.getAttachment();
            auto = jAction.getAuto();
            JMelody jMelody = model.getMelody();
            melody = jMelody.getMelodyPath();
            JRecurrence jRecurrence = model.getRecurrence();
            repeaCode = jRecurrence.getRepeat();
            limit = jRecurrence.getLimit();
            JLed jLed = model.getLed();
            color = jLed.getColor();
            vibration = model.getVibrate();
            voice = model.getVoice();
            notificationRepeat = model.getNotificationRepeat();
            wake = model.getAwake();
            unlock = model.getUnlock();
            count = model.getCount();
            if (item.getDelay() != 0 && item.getDateTime() > System.currentTimeMillis()) {
                notifier.discardNotification(id);
                finish();
            }
        } else {
            notifier.discardNotification(id);
            finish();
        }

        boolean isFull = prefs.getBoolean(Prefs.UNLOCK_DEVICE);
        if (unlock != -1) {
            isFull = unlock == 1;
        }
        if (isFull) {
            runOnUiThread(() -> getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD));
        }

        boolean isWake = prefs.getBoolean(Prefs.WAKE_STATUS);
        if (wake != -1) {
            isWake = wake == 1;
        }
        if (isWake) {
            PowerManager.WakeLock screenLock = ((PowerManager)getSystemService(POWER_SERVICE)).newWakeLock(
                    PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "TAG");
            screenLock.acquire();
            screenLock.release();
        }

        boolean autoLaunch = prefs.getBoolean(Prefs.APPLICATION_AUTO_LAUNCH);
        boolean silentSMS = prefs.getBoolean(Prefs.SILENT_SMS);
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
        RelativeLayout subjectContainer = (RelativeLayout) findViewById(R.id.subjectContainer);
        single_container.setVisibility(View.VISIBLE);
        container.setVisibility(View.GONE);
        subjectContainer.setVisibility(View.GONE);

        loadImage();

        FloatingActionButton buttonOk = (FloatingActionButton) findViewById(R.id.buttonOk);
        FloatingActionButton buttonEdit = (FloatingActionButton) findViewById(R.id.buttonEdit);
        buttonCancel = (FloatingActionButton) findViewById(R.id.buttonCancel);
        buttonCall = (FloatingActionButton) findViewById(R.id.buttonCall);
        buttonDelay = (FloatingActionButton) findViewById(R.id.buttonDelay);
        FloatingActionButton buttonDelayFor = (FloatingActionButton) findViewById(R.id.buttonDelayFor);
        FloatingActionButton buttonNotification = (FloatingActionButton) findViewById(R.id.buttonNotification);
        colorify(buttonOk, buttonCall, buttonCancel, buttonDelay, buttonDelayFor,
                buttonNotification, buttonEdit);
        int mins = prefs.getInt(Prefs.DELAY_TIME);
        setTextDrawable(buttonDelay, String.valueOf(mins));
        setTextDrawable(buttonDelayFor, "...");
        buttonOk.setImageResource(R.drawable.ic_done_black_24dp);
        buttonEdit.setImageResource(R.drawable.ic_create_black_24dp);
        buttonCancel.setImageResource(R.drawable.ic_clear_black_vector);
        buttonCall.setImageResource(R.drawable.ic_call_black_24dp);
        buttonNotification.setImageResource(R.drawable.ic_favorite_black_24dp);

        CircleImageView contactPhoto = (CircleImageView) findViewById(R.id.contactPhoto);
        contactPhoto.setBorderColor(cs.getColor(cs.colorPrimary()));
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
                Uri photo = Contacts.getPhoto(conID);
                if (photo != null) contactPhoto.setImageURI(photo);
                else contactPhoto.setVisibility(View.GONE);
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
                if (task == null) {
                    messageView.setVisibility(View.GONE);
                    findViewById(R.id.someView).setVisibility(View.GONE);
                }
            }
            container.setVisibility(View.VISIBLE);
        } else if (type.contains(Constants.TYPE_MESSAGE) ||
                type.matches(Constants.TYPE_SKYPE_CHAT)) {
            if (type.contains(Constants.TYPE_MESSAGE)) {
                contactPhoto.setVisibility(View.VISIBLE);
                long conID = Contacts.getIdFromNumber(number, ReminderDialog.this);
                Uri photo = Contacts.getPhoto(conID);
                if (photo != null) contactPhoto.setImageURI(photo);
                else contactPhoto.setVisibility(View.GONE);
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
                Uri photo = Contacts.getPhoto(conID);
                if (photo != null) contactPhoto.setImageURI(photo);
                else contactPhoto.setVisibility(View.GONE);
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
            final String nameA = (String) ((applicationInfo != null) ? packageManager.getApplicationLabel(applicationInfo) : "???");
            remText.setText(task + "\n\n" + nameA + "\n" + number);
            buttonCall.setVisibility(View.VISIBLE);
            buttonCall.setImageResource(R.drawable.ic_open_in_browser_black_24dp);
        } else if (type.matches(Constants.TYPE_APPLICATION_BROWSER)) {
            remText.setText(task + "\n\n" + number);
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

        if (type.startsWith(Constants.TYPE_LOCATION) ||
                type.startsWith(Constants.TYPE_LOCATION_OUT) ||
                type.matches(Constants.TYPE_PLACES)){
            buttonDelay.setVisibility(View.GONE);
            buttonDelayFor.setVisibility(View.GONE);
        }

        if ((repeaCode == 0 || (limit > 0 && (limit - count - 1 == 0))) &&
                !type.startsWith(Constants.TYPE_WEEKDAY) && !type.contains(Constants.TYPE_MONTHDAY)) {
            buttonCancel.setVisibility(View.GONE);
        } else {
            buttonCancel.setVisibility(View.VISIBLE);
        }

        buttonCancel.setOnClickListener(v -> cancel());
        buttonNotification.setOnClickListener(v -> favourite());
        buttonOk.setOnClickListener(v -> ok());
        buttonEdit.setOnClickListener(v -> {
            Reminder.edit(id, ReminderDialog.this);
            update(1);
            finish();
        });
        buttonDelay.setOnClickListener(v -> delay());
        buttonDelayFor.setOnClickListener(v -> {
            showDialog();
            update(0);
        });
        buttonCall.setOnClickListener(v -> call());
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

        boolean isRepeat = prefs.getBoolean(Prefs.NOTIFICATION_REPEAT);
        if (notificationRepeat != -1) {
            isRepeat = notificationRepeat == 1;
        }
        if (isRepeat) {
            repeater.setAlarm(ReminderDialog.this, id);
        }
        boolean isTTS = prefs.getBoolean(Prefs.TTS);
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
        if (prefs.getBoolean(Prefs.WEAR_SERVICE)) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Wearable.API)
                    .addConnectionCallbacks(this)
                    .build();
        }
        if (LocationUtil.isGooglePlayServicesAvailable(this)) {
            ReminderApp application = (ReminderApp) getApplication();
            mTracker = application.getDefaultTracker();
        }
    }

    private void call() {
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

    private void delay() {
        int delay = SharedPrefs.getInstance(this).getInt(Prefs.DELAY_TIME);
        Reminder.setDelay(ReminderDialog.this, id, delay, true);
        update(1);
        finish();
    }

    private void ok() {
        Reminder.update(ReminderDialog.this, id);
        update(1);
        finish();
    }

    private void favourite() {
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

    private void cancel() {
        Reminder.disableReminder(id, ReminderDialog.this);
        update(1);
        finish();
    }

    private void loadImage() {
        ImageView bgImage = (ImageView) findViewById(R.id.bgImage);
        bgImage.setVisibility(View.GONE);
        SharedPrefs prefs = SharedPrefs.getInstance(this);
        String imagePrefs = prefs.getString(Prefs.REMINDER_IMAGE);
        boolean blur = prefs.getBoolean(Prefs.REMINDER_IMAGE_BLUR);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int width = metrics.widthPixels;
        int height = (int) (metrics.heightPixels * 0.75);
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
        shoppings = item.getModel().getShoppings();
        provider = new ShoppingListDataProvider(shoppings, true);
        TaskListRecyclerAdapter shoppingAdapter = new TaskListRecyclerAdapter(this, provider,
                new TaskListRecyclerAdapter.ActionListener() {
            @Override
            public void onItemCheck(int position, boolean isChecked) {
                shoppings.get(position).setStatus(isChecked ? 1 : 0);
                Reminder.switchItem(ReminderDialog.this, id, isChecked, provider.getItem(position).getUuId());
                loadData();
            }

            @Override
            public void onItemDelete(int position) {
                shoppings.remove(position);
                Reminder.hideItem(ReminderDialog.this, id, provider.getItem(position).getUuId());
                loadData();
            }

            @Override
            public void onItemChange(int position) {
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
                    return ReminderHelper.getInstance(this).getReminder(id).getType();
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
        SharedPrefs prefs = SharedPrefs.getInstance(this);
        String type = getType();
        boolean isTTS = prefs.getBoolean(Prefs.TTS);
        if (isMelody == 1) {
            i = 0;
        }
        if (voice != -1) {
            isTTS = voice == 1;
        }
        boolean isV = prefs.getBoolean(Prefs.VIBRATION_STATUS);
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
        builder.setItems(items, (dialog, item1) -> {
            int x = 0;
            if (item1 == 0) {
                x = 5;
            } else if (item1 == 1) {
                x = 10;
            } else if (item1 == 2) {
                x = 15;
            } else if (item1 == 3) {
                x = 30;
            } else if (item1 == 4) {
                x = 45;
            } else if (item1 == 5) {
                x = 60;
            } else if (item1 == 6) {
                x = 90;
            } else if (item1 == 7) {
                x = 120;
            } else if (item1 == 8) {
                x = 60 * 6;
            } else if (item1 == 9) {
                x = 60 * 24;
            } else if (item1 == 10) {
                x = 60 * 24 * 2;
            } else if (item1 == 11) {
                x = 60 * 24 * 7;
            }
            Reminder.setDelay(ReminderDialog.this, id, x, true);
            Messages.toast(ReminderDialog.this, getString(R.string.reminder_snoozed));
            dialog.dismiss();
            removeFlags();
            finish();
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void sendSMS(String phoneNumber, String message) {
        mSendDialog = ProgressDialog.show(this, getString(R.string.sending_message), getString(R.string.please_wait), false, false);
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
        UpdatesHelper.getInstance(this).updateWidget();
        if (SharedPrefs.getInstance(this).getBoolean(Prefs.WEAR_SERVICE)) {
            PutDataMapRequest putDataMapReq = PutDataMapRequest.create(SharedConst.WEAR_STOP);
            DataMap map = putDataMapReq.getDataMap();
            map.putBoolean(SharedConst.KEY_STOP, true);
            PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
            Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq);
        }
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
        if (LocationUtil.isGooglePlayServicesAvailable(this)) {
            mTracker.setScreenName("Reminder " + getType());
            mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        }

        if (SharedPrefs.getInstance(this).getBoolean(Prefs.WEAR_SERVICE))
            mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (SharedPrefs.getInstance(this).getBoolean(Prefs.WEAR_SERVICE)) {
            Wearable.DataApi.removeListener(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
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
        if (!SharedPrefs.getInstance(this).getBoolean(Prefs.SYSTEM_VOLUME)) {
            AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            am.setStreamVolume(mStream, currVolume, 0);
        }
    }

    @Override
    public void onBackPressed() {
        notifier.discardMedia();
        if (SharedPrefs.getInstance(this).getBoolean(Prefs.SMART_FOLD)){
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
                    if (Module.isLollipop()) {
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
        if (mSendDialog != null && mSendDialog.isShowing()) {
            mSendDialog.dismiss();
        }
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

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Wearable.DataApi.addListener(mGoogleApiClient, this);
        boolean silentSMS = SharedPrefs.getInstance(this).getBoolean(Prefs.SILENT_SMS);
        String type = getType();
        if (type != null && type.contains(Constants.TYPE_MESSAGE) && silentSMS)
            return;

        PutDataMapRequest putDataMapReq = PutDataMapRequest.create(SharedConst.WEAR_REMINDER);
        DataMap map = putDataMapReq.getDataMap();
        map.putString(SharedConst.KEY_TYPE, getType());
        map.putString(SharedConst.KEY_TASK, task);
        map.putInt(SharedConst.KEY_COLOR, cs.colorAccent());
        map.putBoolean(SharedConst.KEY_THEME, cs.isDark());
        map.putBoolean(SharedConst.KEY_REPEAT, buttonCancel.getVisibility() == View.VISIBLE);
        map.putBoolean(SharedConst.KEY_TIMED, buttonDelay.getVisibility() == View.VISIBLE);
        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
        Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {
        Log.d(Constants.LOG_TAG, "Data received");
        for (DataEvent event : dataEventBuffer) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                // DataItem changed
                DataItem item = event.getDataItem();
                if (item.getUri().getPath().compareTo(SharedConst.PHONE_REMINDER) == 0) {
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();

                    int keyCode = dataMap.getInt(SharedConst.REQUEST_KEY);
                    if (keyCode == SharedConst.KEYCODE_OK) {
                        ok();
                    } else if (keyCode == SharedConst.KEYCODE_FAVOURITE) {
                        favourite();
                    } else if (keyCode == SharedConst.KEYCODE_CANCEL) {
                        cancel();
                    } else if (keyCode == SharedConst.KEYCODE_SNOOZE) {
                        delay();
                    } else {
                        call();
                    }
                }
            }
        }
    }
}