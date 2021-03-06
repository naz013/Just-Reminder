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

package com.cray.software.justreminder.helpers;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.TaskStackBuilder;
import android.view.View;
import android.widget.RemoteViews;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.StartActivity;
import com.cray.software.justreminder.birthdays.BirthdayHelper;
import com.cray.software.justreminder.birthdays.BirthdayItem;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.notes.NoteItem;
import com.cray.software.justreminder.notes.NotesActivity;
import com.cray.software.justreminder.reminder.ReminderDialogActivity;
import com.cray.software.justreminder.reminder.ReminderHelper;
import com.cray.software.justreminder.reminder.ReminderItem;
import com.cray.software.justreminder.reminder.ReminderActivity;
import com.cray.software.justreminder.services.BirthdayPermanentService;
import com.cray.software.justreminder.utils.TimeUtil;
import com.cray.software.justreminder.utils.ViewUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Helper class for status bar notifications.
 */
public class Notifier {

    private Context mContext;
    private int NOT_ID = 0;
    private Sound sound;

    public Notifier(Context context){
        this.mContext = context;
        sound = new Sound(context);
    }

    /**
     * Status bar notification to use when enabled tts.
     * @param task task string.
     * @param typePrefs type of reminder.
     * @param itemId reminder identifier.
     * @param color LED lights color.
     */
    public void showTTSNotification(final String task, String typePrefs, long itemId,
                                    int color, boolean vibrate){
        SharedPrefs sPrefs = SharedPrefs.getInstance(mContext);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext);
        builder.setContentTitle(task);
        if (sPrefs.getBoolean(Prefs.SMART_FOLD)) {
            Intent notificationIntent = new Intent(mContext, ReminderDialogActivity.class);
            notificationIntent.putExtra(Constants.ITEM_ID_INTENT, itemId);
            notificationIntent.putExtra("int", 1);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            PendingIntent intent = PendingIntent.getActivity(mContext, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(intent);
        }
        builder.setAutoCancel(false);
        builder.setPriority(NotificationCompat.PRIORITY_MAX);
        if (sPrefs.getBoolean(Prefs.NOTIFICATION_REMOVE)){
            builder.setOngoing(false);
        } else {
            builder.setOngoing(true);
        }
        String app;
        if (Module.isPro()){
            app = mContext.getString(R.string.app_name_pro);
        } else app = mContext.getString(R.string.app_name);
        builder.setContentText(app);
        builder.setSmallIcon(ViewUtils.getIcon(typePrefs));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setColor(ViewUtils.getColor(mContext, R.color.bluePrimary));
        }
        AudioManager am = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        if (am.getRingerMode() == AudioManager.RINGER_MODE_NORMAL){
            try {
                AssetFileDescriptor afd = mContext.getAssets().openFd("sounds/beep.mp3");
                sound.playAlarm(afd, false);
            } catch (IOException e) {
                e.printStackTrace();
                sound.playAlarm(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), false);
            }
        } else {
            if (sPrefs.getBoolean(Prefs.SILENT_SOUND)) {
                try {
                    AssetFileDescriptor afd = mContext.getAssets().openFd("sounds/beep.mp3");
                    sound.playAlarm(afd, false);
                } catch (IOException e) {
                    e.printStackTrace();
                    sound.playAlarm(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), false);
                }
            }
        }
        if (vibrate){
            long[] pattern;
            if (sPrefs.getBoolean(Prefs.INFINITE_VIBRATION)){
                pattern = new long[]{150, 86400000};
            } else {
                pattern = new long[]{150, 400, 100, 450, 200, 500, 300, 500};
            }
            builder.setVibrate(pattern);
        }
        if (Module.isPro()){
            if (sPrefs.getBoolean(Prefs.LED_STATUS)){
                if (color != 0) {
                    builder.setLights(color, 500, 1000);
                } else {
                    builder.setLights(sPrefs.getInt(Prefs.LED_COLOR), 500, 1000);
                }
            }
        }
        boolean isWear = sPrefs.getBoolean(Prefs.WEAR_NOTIFICATION);
        if (isWear) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                builder.setOnlyAlertOnce(true);
                builder.setGroup("GROUP");
                builder.setGroupSummary(true);
            }
        }
        NotificationManagerCompat mNotifyMgr = NotificationManagerCompat.from(mContext);
        Integer it = (int) (long) itemId;
        mNotifyMgr.notify(it, builder.build());
        if (isWear){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                final NotificationCompat.Builder wearableNotificationBuilder = new NotificationCompat.Builder(mContext);
                wearableNotificationBuilder.setSmallIcon(R.mipmap.ic_launcher);
                wearableNotificationBuilder.setContentTitle(task);
                wearableNotificationBuilder.setContentText(app);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    wearableNotificationBuilder.setColor(ViewUtils.getColor(mContext, R.color.bluePrimary));
                }
                wearableNotificationBuilder.setOngoing(false);
                wearableNotificationBuilder.setOnlyAlertOnce(true);
                wearableNotificationBuilder.setGroup("GROUP");
                wearableNotificationBuilder.setGroupSummary(false);
                mNotifyMgr.notify(10100, wearableNotificationBuilder.build());
            }
        }
    }

    /**
     * Standard status bar notification for reminder.
     * @param task reminder task.
     * @param type reminder type.
     * @param i flag for enabling sounds (1 - enabled).
     * @param itemId reminder identifier.
     * @param melody reminder custom melody file.
     * @param color LED lights color.
     */
    public void showReminder(final String task, String type, int i, long itemId,
                             String melody, int color, boolean vibrate){
        SharedPrefs sPrefs = SharedPrefs.getInstance(mContext);
        Uri soundUri;
        if (melody != null && !melody.matches("")){
            File sound = new File(melody);
            soundUri = Uri.fromFile(sound);
        } else {
            if (sPrefs.getBoolean(Prefs.CUSTOM_SOUND)) {
                String path = sPrefs.getString(Prefs.CUSTOM_SOUND_FILE);
                if (path != null) {
                    File sound = new File(path);
                    soundUri = Uri.fromFile(sound);
                } else {
                    soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                }
            } else {
                soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            }
        }
        Intent notificationIntent = new Intent(mContext, ReminderDialogActivity.class);
        notificationIntent.putExtra(Constants.ITEM_ID_INTENT, itemId);
        notificationIntent.putExtra("int", 1);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        PendingIntent intent = PendingIntent.getActivity(mContext, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext);
        builder.setContentTitle(task);
        builder.setContentIntent(intent);
        builder.setAutoCancel(false);
        builder.setPriority(NotificationCompat.PRIORITY_MAX);
        if (sPrefs.getBoolean(Prefs.NOTIFICATION_REMOVE)){
            builder.setOngoing(false);
        } else {
            builder.setOngoing(true);
        }
        String app;
        if (Module.isPro()){
            app = mContext.getString(R.string.app_name_pro);
        } else {
            app = mContext.getString(R.string.app_name);
        }
        builder.setContentText(app);
        builder.setSmallIcon(ViewUtils.getIcon(type));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setColor(ViewUtils.getColor(mContext, R.color.bluePrimary));
        }
        if (i == 1) {
            AudioManager am = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
            if (am.getRingerMode() == AudioManager.RINGER_MODE_NORMAL){
                sound.playAlarm(soundUri, sPrefs.getBoolean(Prefs.INFINITE_SOUND));
            } else {
                if (sPrefs.getBoolean(Prefs.SILENT_SOUND)) {
                    sound.playAlarm(soundUri, sPrefs.getBoolean(Prefs.INFINITE_SOUND));
                }
            }
        }
        if (vibrate){
            long[] pattern;
            if (sPrefs.getBoolean(Prefs.INFINITE_VIBRATION)){
                pattern = new long[]{150, 86400000};
            } else {
                pattern = new long[]{150, 400, 100, 450, 200, 500, 300, 500};
            }
            builder.setVibrate(pattern);
        }
        if (Module.isPro()){
            if (sPrefs.getBoolean(Prefs.LED_STATUS)){
                if (color != 0) {
                    builder.setLights(color, 500, 1000);
                } else {
                    builder.setLights(sPrefs.getInt(Prefs.LED_COLOR), 500, 1000);
                }
            }
        }
        boolean isWear = sPrefs.getBoolean(Prefs.WEAR_NOTIFICATION);
        if (isWear) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                builder.setOnlyAlertOnce(true);
                builder.setGroup("GROUP");
                builder.setGroupSummary(true);
            }
        }
        NotificationManagerCompat mNotifyMgr = NotificationManagerCompat.from(mContext);
        Integer it = (int) (long) itemId;
        mNotifyMgr.notify(it, builder.build());
        if (isWear){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                final NotificationCompat.Builder wearableNotificationBuilder = new NotificationCompat.Builder(mContext);
                wearableNotificationBuilder.setSmallIcon(R.mipmap.ic_launcher);
                wearableNotificationBuilder.setContentTitle(task);
                wearableNotificationBuilder.setContentText(app);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    wearableNotificationBuilder.setColor(ViewUtils.getColor(mContext, R.color.bluePrimary));
                }
                wearableNotificationBuilder.setOngoing(false);
                wearableNotificationBuilder.setOnlyAlertOnce(true);
                wearableNotificationBuilder.setGroup("GROUP");
                wearableNotificationBuilder.setGroupSummary(false);
                mNotifyMgr.notify(10100, wearableNotificationBuilder.build());
            }
        }
    }

    /**
     * Status bar notification for missed calls.
     * @param name contact name.
     * @param itemId reminder identifier.
     */
    public void showMissedReminder(final String name, long itemId){
        SharedPrefs sPrefs = SharedPrefs.getInstance(mContext);
        Uri soundUri;
        if (sPrefs.getBoolean(Prefs.CUSTOM_SOUND)) {
            String path = sPrefs.getString(Prefs.CUSTOM_SOUND_FILE);
            if (path != null) {
                File sound = new File(path);
                soundUri = Uri.fromFile(sound);
            } else {
                soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            }
        } else {
            soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext);
        builder.setContentTitle(name);
        builder.setAutoCancel(false);
        builder.setPriority(NotificationCompat.PRIORITY_MAX);
        if (sPrefs.getBoolean(Prefs.NOTIFICATION_REMOVE)){
            builder.setOngoing(false);
        } else {
            builder.setOngoing(true);
        }
        builder.setContentText(mContext.getString(R.string.missed_call));

        int icon = R.drawable.ic_call_white_24dp;
        builder.setSmallIcon(icon);
        AudioManager am = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        if (am.getRingerMode() == AudioManager.RINGER_MODE_NORMAL){
            sound.playAlarm(soundUri, sPrefs.getBoolean(Prefs.INFINITE_SOUND));
        } else {
            if (sPrefs.getBoolean(Prefs.SILENT_SOUND)) {
                sound.playAlarm(soundUri, sPrefs.getBoolean(Prefs.INFINITE_SOUND));
            }
        }
        if (sPrefs.getBoolean(Prefs.VIBRATION_STATUS)){
            long[] pattern;
            if (sPrefs.getBoolean(Prefs.INFINITE_VIBRATION)){
                pattern = new long[]{150, 86400000};
            } else {
                pattern = new long[]{150, 400, 100, 450, 200, 500, 300, 500};
            }
            builder.setVibrate(pattern);
        }
        if (Module.isPro()){
            if (sPrefs.getBoolean(Prefs.LED_STATUS)){
                builder.setLights(sPrefs.getInt(Prefs.LED_COLOR), 500, 1000);
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setColor(ViewUtils.getColor(mContext, R.color.bluePrimary));
        }
        boolean isWear = sPrefs.getBoolean(Prefs.WEAR_NOTIFICATION);
        if (isWear) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                builder.setOnlyAlertOnce(true);
                builder.setGroup("GROUP");
                builder.setGroupSummary(true);
            }
        }
        NotificationManagerCompat mNotifyMgr = NotificationManagerCompat.from(mContext);
        Integer it = (int) (long) itemId;
        mNotifyMgr.notify(it, builder.build());
        if (isWear){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                final NotificationCompat.Builder wearableNotificationBuilder = new NotificationCompat.Builder(mContext);
                wearableNotificationBuilder.setSmallIcon(R.mipmap.ic_launcher);
                wearableNotificationBuilder.setContentTitle(name);
                wearableNotificationBuilder.setContentText(mContext.getString(R.string.missed_call));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    wearableNotificationBuilder.setColor(ViewUtils.getColor(mContext, R.color.bluePrimary));
                }
                wearableNotificationBuilder.setOngoing(false);
                wearableNotificationBuilder.setOnlyAlertOnce(true);
                wearableNotificationBuilder.setGroup("GROUP");
                wearableNotificationBuilder.setGroupSummary(false);
                mNotifyMgr.notify(10000, wearableNotificationBuilder.build());
            }
        }
    }

    /**
     * Status bar notification for birthdays.
     * @param years user ages.
     * @param name user name.
     */
    public void showNotification(int years, String name){
        SharedPrefs sPrefs = SharedPrefs.getInstance(mContext);
        Uri soundUri;
        boolean soundC;
        if (Module.isPro()){
            if (!sPrefs.getBoolean(Prefs.BIRTHDAY_USE_GLOBAL)){
                soundC = sPrefs.getBoolean(Prefs.BIRTHDAY_CUSTOM_SOUND);
            } else soundC = sPrefs.getBoolean(Prefs.CUSTOM_SOUND);
        } else soundC = sPrefs.getBoolean(Prefs.CUSTOM_SOUND);
        if (soundC){
            String path;
            if (Module.isPro()) {
                if (!sPrefs.getBoolean(Prefs.BIRTHDAY_USE_GLOBAL)){
                    path = sPrefs.getString(Prefs.BIRTHDAY_CUSTOM_SOUND_FILE);
                } else path = sPrefs.getString(Prefs.CUSTOM_SOUND_FILE);
            } else path = sPrefs.getString(Prefs.CUSTOM_SOUND_FILE);
            if (path != null){
                File sound = new File(path);
                soundUri = Uri.fromFile(sound);
            } else {
                soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            }
        } else {
            soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext);
        builder.setContentTitle(name);
        builder.setContentText(TimeUtil.getAgeFormatted(mContext, years));
        builder.setSmallIcon(R.drawable.ic_cake_white_24dp);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setColor(ViewUtils.getColor(mContext, R.color.bluePrimary));
        }
        AudioManager am = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        if (am.getRingerMode() == AudioManager.RINGER_MODE_NORMAL){
            boolean isLooping;
            if (Module.isPro()){
                if (!sPrefs.getBoolean(Prefs.BIRTHDAY_USE_GLOBAL)){
                    isLooping = sPrefs.getBoolean(Prefs.BIRTHDAY_INFINITE_SOUND);
                } else isLooping = sPrefs.getBoolean(Prefs.INFINITE_SOUND);
            } else isLooping = sPrefs.getBoolean(Prefs.INFINITE_SOUND);

            sound.playAlarm(soundUri, isLooping);
        } else {
            boolean soundS;
            if (Module.isPro()){
                if (!sPrefs.getBoolean(Prefs.BIRTHDAY_USE_GLOBAL)){
                    soundS = sPrefs.getBoolean(Prefs.BIRTHDAY_SOUND_STATUS);
                } else soundS = sPrefs.getBoolean(Prefs.SILENT_SOUND);
            } else soundS = sPrefs.getBoolean(Prefs.SILENT_SOUND);

            if (soundS) {
                boolean isLooping;
                if (Module.isPro()){
                    if (!sPrefs.getBoolean(Prefs.BIRTHDAY_USE_GLOBAL)){
                        isLooping = sPrefs.getBoolean(Prefs.BIRTHDAY_INFINITE_SOUND);
                    } else isLooping = sPrefs.getBoolean(Prefs.INFINITE_SOUND);
                } else isLooping = sPrefs.getBoolean(Prefs.INFINITE_SOUND);
                sound.playAlarm(soundUri, isLooping);
            }
        }
        boolean vibrate;
        if (Module.isPro()){
            if (!sPrefs.getBoolean(Prefs.BIRTHDAY_USE_GLOBAL)){
                vibrate = sPrefs.getBoolean(Prefs.BIRTHDAY_VIBRATION_STATUS);
            } else vibrate = sPrefs.getBoolean(Prefs.VIBRATION_STATUS);
        } else vibrate = sPrefs.getBoolean(Prefs.VIBRATION_STATUS);
        if (vibrate){
            long[] pattern;
            if (sPrefs.getBoolean(Prefs.BIRTHDAY_USE_GLOBAL)){
                if (sPrefs.getBoolean(Prefs.INFINITE_VIBRATION)){
                    pattern = new long[]{150, 86400000};
                } else {
                    pattern = new long[]{150, 400, 100, 450, 200, 500, 300, 500};
                }
            } else {
                if (sPrefs.getBoolean(Prefs.BIRTHDAY_INFINITE_VIBRATION)) {
                    pattern = new long[]{150, 86400000};
                } else {
                    pattern = new long[]{150, 400, 100, 450, 200, 500, 300, 500};
                }
            }
            builder.setVibrate(pattern);
        }
        if (Module.isPro()){
            if (!sPrefs.getBoolean(Prefs.BIRTHDAY_USE_GLOBAL)) {
                if (sPrefs.getBoolean(Prefs.BIRTHDAY_LED_STATUS)) {
                    builder.setLights(sPrefs.getInt(Prefs.BIRTHDAY_LED_COLOR), 500, 1000);
                }
            } else {
                if (sPrefs.getBoolean(Prefs.LED_STATUS)) {
                    builder.setLights(sPrefs.getInt(Prefs.LED_COLOR), 500, 1000);
                }
            }
        }
        boolean isWear = sPrefs.getBoolean(Prefs.WEAR_NOTIFICATION);
        if (isWear) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                builder.setOnlyAlertOnce(true);
                builder.setGroup("GROUP");
                builder.setGroupSummary(true);
            }
        }
        NotificationManagerCompat mNotifyMgr = NotificationManagerCompat.from(mContext);
        mNotifyMgr.notify(NOT_ID, builder.build());
        if (isWear){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                final NotificationCompat.Builder wearableNotificationBuilder = new NotificationCompat.Builder(mContext);
                wearableNotificationBuilder.setSmallIcon(R.mipmap.ic_launcher);
                wearableNotificationBuilder.setContentTitle(name);
                wearableNotificationBuilder.setContentText(TimeUtil.getAgeFormatted(mContext, years));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    wearableNotificationBuilder.setColor(ViewUtils.getColor(mContext, R.color.bluePrimary));
                }
                wearableNotificationBuilder.setOngoing(false);
                wearableNotificationBuilder.setOnlyAlertOnce(true);
                wearableNotificationBuilder.setGroup("GROUP");
                wearableNotificationBuilder.setGroupSummary(false);
                mNotifyMgr.notify(10001, wearableNotificationBuilder.build());
            }
        }
    }

    /**
     * Simple status bar notification for reminders.
     * @param content notification title.
     * @param id reminder identifier.
     */
    public void showReminderNotification(String content, long id){
        SharedPrefs sPrefs = SharedPrefs.getInstance(mContext);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext);
        builder.setContentTitle(content);
        String app;
        if (Module.isPro()){
            app = mContext.getString(R.string.app_name_pro);
        } else app = mContext.getString(R.string.app_name);
        builder.setContentText(app);
        builder.setSmallIcon(R.drawable.ic_notifications_white_24dp);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setColor(ViewUtils.getColor(mContext, R.color.bluePrimary));
        }
        boolean isWear = sPrefs.getBoolean(Prefs.WEAR_NOTIFICATION);
        if (isWear) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                builder.setOnlyAlertOnce(true);
                builder.setGroup("GROUP");
                builder.setGroupSummary(true);
            }
        }
        NotificationManagerCompat mNotifyMgr = NotificationManagerCompat.from(mContext);
        Integer it = (int) (long) id;
        mNotifyMgr.notify(it, builder.build());
        if (isWear){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                final NotificationCompat.Builder wearableNotificationBuilder = new NotificationCompat.Builder(mContext);
                wearableNotificationBuilder.setSmallIcon(R.mipmap.ic_launcher);
                wearableNotificationBuilder.setContentTitle(content);
                wearableNotificationBuilder.setContentText(app);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    wearableNotificationBuilder.setColor(ViewUtils.getColor(mContext, R.color.bluePrimary));
                }
                wearableNotificationBuilder.setOngoing(false);
                wearableNotificationBuilder.setOnlyAlertOnce(true);
                wearableNotificationBuilder.setGroup("GROUP");
                wearableNotificationBuilder.setGroupSummary(false);
                mNotifyMgr.notify(it + 10, wearableNotificationBuilder.build());
            }
        }
    }

    /**
     * Status bar notification for notes.
     */
    public void showNoteNotification(NoteItem item){
        SharedPrefs sPrefs = SharedPrefs.getInstance(mContext);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext);
        builder.setContentText(mContext.getString(R.string.note));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setColor(ViewUtils.getColor(mContext, R.color.bluePrimary));
        }
        String content = sPrefs.getBoolean(Prefs.NOTE_ENCRYPT) ? SyncHelper.decrypt(item.getNote()) : item.getNote();
        builder.setSmallIcon(R.drawable.ic_event_note_white_24dp);
        builder.setContentTitle(content);
        boolean isWear = sPrefs.getBoolean(Prefs.WEAR_NOTIFICATION);
        if (isWear) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                builder.setOnlyAlertOnce(true);
                builder.setGroup("GROUP");
                builder.setGroupSummary(true);
            }
        }
        if (item.getImage() != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(item.getImage(), 0, item.getImage().length);
            builder.setLargeIcon(bitmap);
            NotificationCompat.BigPictureStyle s = new NotificationCompat.BigPictureStyle();
            s.bigLargeIcon(bitmap);
            s.bigPicture(bitmap);
            builder.setStyle(s);
        }
        NotificationManagerCompat mNotifyMgr = NotificationManagerCompat.from(mContext);
        Integer it = (int) (long) item.getId();
        mNotifyMgr.notify(it, builder.build());
        if (isWear){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                final NotificationCompat.Builder wearableNotificationBuilder = new NotificationCompat.Builder(mContext);
                wearableNotificationBuilder.setSmallIcon(R.mipmap.ic_launcher);
                wearableNotificationBuilder.setContentTitle(content);
                wearableNotificationBuilder.setContentText(mContext.getString(R.string.note));
                wearableNotificationBuilder.setOngoing(false);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    wearableNotificationBuilder.setColor(ViewUtils.getColor(mContext, R.color.bluePrimary));
                }
                wearableNotificationBuilder.setOnlyAlertOnce(true);
                wearableNotificationBuilder.setGroup("GROUP");
                wearableNotificationBuilder.setGroupSummary(false);
                mNotifyMgr.notify(it + 10, wearableNotificationBuilder.build());
            }
        }
    }

    /**
     * Recreates current permanent status bar notification.
     */
    public void recreatePermanent(){
        if (SharedPrefs.getInstance(mContext).getBoolean(Prefs.STATUS_BAR_NOTIFICATION)) showPermanent();
    }

    /**
     * Create permanent notification in status bar.
     */
    public void showPermanent(){
        RemoteViews remoteViews = new RemoteViews(mContext.getPackageName(),
                R.layout.notification_layout);
        NotificationCompat.Builder notification = new NotificationCompat.Builder(mContext);
        notification.setAutoCancel(false);
        notification.setSmallIcon(R.drawable.ic_notifications_white_24dp);
        notification.setContent(remoteViews);
        notification.setOngoing(true);
        if (SharedPrefs.getInstance(mContext).getBoolean(Prefs.STATUS_BAR_ICON)) {
            notification.setPriority(NotificationCompat.PRIORITY_MAX);
        } else {
            notification.setPriority(NotificationCompat.PRIORITY_MIN);
        }
        Intent resultIntent = new Intent(mContext, ReminderActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);
        stackBuilder.addParentStack(ReminderActivity.class);
        stackBuilder.addNextIntentWithParentStack(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,
                PendingIntent.FLAG_ONE_SHOT);
        remoteViews.setOnClickPendingIntent(R.id.notificationAdd, resultPendingIntent);
        Intent noteIntent = new Intent(mContext, NotesActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        TaskStackBuilder noteBuilder = TaskStackBuilder.create(mContext);
        noteBuilder.addParentStack(NotesActivity.class);
        noteBuilder.addNextIntent(noteIntent);
        PendingIntent notePendingIntent = noteBuilder.getPendingIntent(0,
                PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.noteAdd, notePendingIntent);
        Intent resInt = new Intent(mContext, StartActivity.class);
        resInt.putExtra("tag", StartActivity.FRAGMENT_ACTIVE);
        TaskStackBuilder stackInt = TaskStackBuilder.create(mContext);
        stackInt.addParentStack(StartActivity.class);
        stackInt.addNextIntent(resInt);
        PendingIntent resultPendingInt = stackInt.getPendingIntent(0,
                PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.text, resultPendingInt);
        remoteViews.setOnClickPendingIntent(R.id.featured, resultPendingInt);
        ArrayList<Long> dates = new ArrayList<>();
        ArrayList<String> tasks = new ArrayList<>();
        List<ReminderItem> list = ReminderHelper.getInstance(mContext).getRemindersEnabled();
        int count = list.size();
        for (ReminderItem item : list) {
            long eventTime = item.getDateTime();
            String summary = item.getSummary();
            if (eventTime > 0) {
                dates.add(eventTime);
                tasks.add(summary);
            }
        }
        String event = "";
        long prevTime = 0;
        for (int i = 0; i < dates.size(); i++) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            long currTime = calendar.getTimeInMillis();
            calendar.setTimeInMillis(dates.get(i));
            if (calendar.getTimeInMillis() > currTime){
                if (prevTime == 0){
                    prevTime = dates.get(i);
                    event = tasks.get(i);
                } else {
                    if (dates.get(i) < prevTime){
                        prevTime = dates.get(i);
                        event = tasks.get(i);
                    }
                }

            }
        }
        if (count != 0){
            if (!event.matches("")){
                remoteViews.setTextViewText(R.id.text, event);
                remoteViews.setViewVisibility(R.id.featured, View.VISIBLE);
            } else {
                remoteViews.setTextViewText(R.id.text, mContext.getString(R.string.active_reminders) + " " + String.valueOf(count));
                remoteViews.setViewVisibility(R.id.featured, View.GONE);
            }
        } else {
            remoteViews.setTextViewText(R.id.text, mContext.getString(R.string.no_events));
            remoteViews.setViewVisibility(R.id.featured, View.GONE);
        }
        ColorSetter cs = ColorSetter.getInstance(mContext);
        remoteViews.setInt(R.id.notificationBg, "setBackgroundColor", cs.getColor(cs.colorPrimary()));
        NotificationManagerCompat notifier = NotificationManagerCompat.from(mContext);
        notifier.notify(1, notification.build());
    }

    /**
     * Show status bar notification with feature birthdays.
     */
    public void showBirthdayPermanent(){
        Intent dismissIntent = new Intent(mContext, BirthdayPermanentService.class);
        PendingIntent piDismiss = PendingIntent.getService(mContext, 0, dismissIntent, 0);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);
        List<BirthdayItem> list = BirthdayHelper.getInstance(mContext).getBirthdays(day, month);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext);
        builder.setSmallIcon(R.drawable.ic_cake_white_24dp);
        builder.setAutoCancel(false);
        builder.setOngoing(true);
        builder.setPriority(NotificationCompat.PRIORITY_HIGH);
        builder.setContentTitle(mContext.getString(R.string.events));
        if (list.size() > 0) {
            BirthdayItem item = list.get(0);
            builder.setContentText(item.getDate() + " | " + item.getName() + " | " + TimeUtil.getAgeFormatted(mContext, item.getDate()));
            if (list.size() > 1) {
                StringBuilder stringBuilder = new StringBuilder();
                for (BirthdayItem birthdayItem : list){
                    stringBuilder.append(birthdayItem.getDate()).append(" | ").
                            append(birthdayItem.getName()).append(" | ")
                            .append(TimeUtil.getAgeFormatted(mContext, birthdayItem.getDate()));
                    stringBuilder.append("\n");
                }
                builder.setStyle(new NotificationCompat.BigTextStyle().bigText(stringBuilder.toString()));
            }
            builder.addAction(R.drawable.ic_clear_white_vector, mContext.getString(R.string.ok), piDismiss);
            NotificationManagerCompat notifier = NotificationManagerCompat.from(mContext);
            notifier.notify(1115, builder.build());
        } else hideBirthdayPermanent();
    }

    /**
     * Hide status bar notification with feature birthdays.
     */
    public void hideBirthdayPermanent(){
        ((NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(1115);
    }

    /**
     * Remove permanent notification from status bar.
     */
    public void hidePermanent(){
        ((NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(1);
    }

    public void discardNotification(){
        discardMedia();
        NotificationManagerCompat mNotifyMgr = NotificationManagerCompat.from(mContext);
        mNotifyMgr.cancel(NOT_ID);
    }

    public void discardStatusNotification(long id){
        Integer i = (int) (long) id;
        NotificationManagerCompat mNotifyMgr = NotificationManagerCompat.from(mContext);
        mNotifyMgr.cancel(i);
    }

    public void discardNotification(long id){
        discardMedia();
        Integer i = (int) (long) id;
        NotificationManagerCompat mNotifyMgr = NotificationManagerCompat.from(mContext);
        mNotifyMgr.cancel(i);
    }

    /**
     * Stops playing notification sound.
     */
    public void discardMedia(){
        sound.stop();
    }
}
