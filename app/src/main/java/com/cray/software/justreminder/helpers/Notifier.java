package com.cray.software.justreminder.helpers;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.TaskStackBuilder;
import android.view.View;
import android.widget.RemoteViews;

import com.cray.software.justreminder.NotesManager;
import com.cray.software.justreminder.R;
import com.cray.software.justreminder.ReminderManager;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.dialogs.ReminderDialog;
import com.cray.software.justreminder.dialogs.WeekDayDialog;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.interfaces.Prefs;
import com.cray.software.justreminder.modules.Module;
import com.cray.software.justreminder.utils.ViewUtils;
import com.hexrain.design.ScreenManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Helper class for status bar notifications.
 */
public class Notifier {

    private Context mContext;
    private NotificationManagerCompat mNotifyMgr;
    private NotificationCompat.Builder builder;
    private int NOT_ID = 0;
    private SharedPrefs sPrefs;
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
    public void showTTSNotification(final String task, String typePrefs, long itemId, int color,
                                    boolean vibrate, boolean isExtra){
        sPrefs = new SharedPrefs(mContext);
        builder = new NotificationCompat.Builder(mContext);
        builder.setContentTitle(task);
        if (sPrefs.loadBoolean(Prefs.SMART_FOLD)) {
            Intent notificationIntent = new Intent(mContext, ReminderDialog.class);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            PendingIntent intent = PendingIntent.getActivity(mContext, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            builder.setContentIntent(intent);
        }
        builder.setAutoCancel(false);
        builder.setPriority(NotificationCompat.PRIORITY_MAX);
        if (sPrefs.loadBoolean(Prefs.NOTIFICATION_REMOVE)){
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
            builder.setColor(mContext.getResources().getColor(R.color.colorBlue));
        }

        int maxVolume = 26;
        int currVolume = sPrefs.loadInt(Prefs.VOLUME);
        float log1=(float)(Math.log(maxVolume-currVolume)/Math.log(maxVolume));

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
            if (sPrefs.loadBoolean(Prefs.SOUND_STATUS)) {
                try {
                    AssetFileDescriptor afd = mContext.getAssets().openFd("sounds/beep.mp3");
                    sound.playAlarm(afd, false);
                } catch (IOException e) {
                    e.printStackTrace();
                    sound.playAlarm(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), false);
                }
            }
        }

        boolean isV = sPrefs.loadBoolean(Prefs.VIBRATION_STATUS);
        if (isExtra) isV = vibrate;
        if (isV){
            long[] pattern;
            if (sPrefs.loadBoolean(Prefs.INFINITE_VIBRATION)){
                pattern = new long[]{150, 86400000};
            } else {
                pattern = new long[]{150, 400, 100, 450, 200, 500, 300, 500};
            }
            builder.setVibrate(pattern);
        }
        if (Module.isPro()){
            if (sPrefs.loadBoolean(Prefs.LED_STATUS)){
                if (color != 0) {
                    builder.setLights(color, 500, 1000);
                } else {
                    builder.setLights(sPrefs.loadInt(Prefs.LED_COLOR), 500, 1000);
                }
            }
        }

        boolean isWear = sPrefs.loadBoolean(Prefs.WEAR_NOTIFICATION);
        if (isWear) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                builder.setOnlyAlertOnce(true);
                builder.setGroup("GROUP");
                builder.setGroupSummary(true);
            }
        }

        mNotifyMgr = NotificationManagerCompat.from(mContext);
        Integer it = (int) (long) itemId;
        mNotifyMgr.notify(it, builder.build());

        if (isWear){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                final NotificationCompat.Builder wearableNotificationBuilder = new NotificationCompat.Builder(mContext);
                wearableNotificationBuilder.setSmallIcon(R.mipmap.ic_launcher);
                wearableNotificationBuilder.setContentTitle(task);
                wearableNotificationBuilder.setContentText(app);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    wearableNotificationBuilder.setColor(mContext.getResources().getColor(R.color.colorBlue));
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
    public void showReminder(final String task, String type, int i, long itemId, String melody,
                             int color, boolean vibrate, boolean isExtra){
        sPrefs = new SharedPrefs(mContext);
        Uri soundUri;
        if (melody != null && !melody.matches("")){
            File sound = new File(melody);
            soundUri = Uri.fromFile(sound);
        } else {
            if (sPrefs.loadBoolean(Prefs.CUSTOM_SOUND)) {
                String path = sPrefs.loadPrefs(Prefs.CUSTOM_SOUND_FILE);
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

        Intent notificationIntent = new Intent(mContext, ReminderDialog.class);
        notificationIntent.putExtra("int", 1);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        PendingIntent intent = PendingIntent.getActivity(mContext, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        builder = new NotificationCompat.Builder(mContext);
        builder.setContentTitle(task);
        builder.setContentIntent(intent);
        builder.setAutoCancel(false);
        builder.setPriority(NotificationCompat.PRIORITY_MAX);
        if (sPrefs.loadBoolean(Prefs.NOTIFICATION_REMOVE)){
            builder.setOngoing(false);
        } else {
            builder.setOngoing(true);
        }
        String app;
        if (Module.isPro()){
            app = mContext.getString(R.string.app_name_pro);
        } else app = mContext.getString(R.string.app_name);
        builder.setContentText(app);
        builder.setSmallIcon(ViewUtils.getIcon(type));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setColor(mContext.getResources().getColor(R.color.colorBlue));
        }

        int maxVolume = 26;
        int currVolume = sPrefs.loadInt(Prefs.VOLUME);
        float log1=(float)(Math.log(maxVolume-currVolume)/Math.log(maxVolume));

        if (i == 1) {
            AudioManager am = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
            if (am.getRingerMode() == AudioManager.RINGER_MODE_NORMAL){
                sound.playAlarm(soundUri, sPrefs.loadBoolean(Prefs.INFINITE_SOUND));
            } else {
                if (sPrefs.loadBoolean(Prefs.SOUND_STATUS)) {
                    sound.playAlarm(soundUri, sPrefs.loadBoolean(Prefs.INFINITE_SOUND));
                }
            }
        }

        boolean isV = sPrefs.loadBoolean(Prefs.VIBRATION_STATUS);
        if (isExtra) isV = vibrate;
        if (isV){
            long[] pattern;
            if (sPrefs.loadBoolean(Prefs.INFINITE_VIBRATION)){
                pattern = new long[]{150, 86400000};
            } else {
                pattern = new long[]{150, 400, 100, 450, 200, 500, 300, 500};
            }
            builder.setVibrate(pattern);
        }
        if (Module.isPro()){
            if (sPrefs.loadBoolean(Prefs.LED_STATUS)){
                if (color != 0) {
                    builder.setLights(color, 500, 1000);
                } else {
                    builder.setLights(sPrefs.loadInt(Prefs.LED_COLOR), 500, 1000);
                }
            }
        }

        boolean isWear = sPrefs.loadBoolean(Prefs.WEAR_NOTIFICATION);
        if (isWear) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                builder.setOnlyAlertOnce(true);
                builder.setGroup("GROUP");
                builder.setGroupSummary(true);
            }
        }

        mNotifyMgr = NotificationManagerCompat.from(mContext);
        Integer it = (int) (long) itemId;
        mNotifyMgr.notify(it, builder.build());

        if (isWear){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                final NotificationCompat.Builder wearableNotificationBuilder = new NotificationCompat.Builder(mContext);
                wearableNotificationBuilder.setSmallIcon(R.mipmap.ic_launcher);
                wearableNotificationBuilder.setContentTitle(task);
                wearableNotificationBuilder.setContentText(app);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    wearableNotificationBuilder.setColor(mContext.getResources().getColor(R.color.colorBlue));
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
        sPrefs = new SharedPrefs(mContext);
        Uri soundUri;
        if (sPrefs.loadBoolean(Prefs.CUSTOM_SOUND)) {
            String path = sPrefs.loadPrefs(Prefs.CUSTOM_SOUND_FILE);
            if (path != null) {
                File sound = new File(path);
                soundUri = Uri.fromFile(sound);
            } else {
                soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            }
        } else {
            soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        }

        builder = new NotificationCompat.Builder(mContext);
        builder.setContentTitle(name);
        builder.setAutoCancel(false);
        builder.setPriority(NotificationCompat.PRIORITY_MAX);
        if (sPrefs.loadBoolean(Prefs.NOTIFICATION_REMOVE)){
            builder.setOngoing(false);
        } else {
            builder.setOngoing(true);
        }

        builder.setContentText(mContext.getString(R.string.missed_call_event_title));

        int icon = R.drawable.ic_call_white_24dp;
        builder.setSmallIcon(icon);

        int maxVolume = 26;
        int currVolume = sPrefs.loadInt(Prefs.VOLUME);
        float log1=(float)(Math.log(maxVolume-currVolume)/Math.log(maxVolume));

        AudioManager am = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        if (am.getRingerMode() == AudioManager.RINGER_MODE_NORMAL){
            sound.playAlarm(soundUri, sPrefs.loadBoolean(Prefs.INFINITE_SOUND));
        } else {
            if (sPrefs.loadBoolean(Prefs.SOUND_STATUS)) {
                sound.playAlarm(soundUri, sPrefs.loadBoolean(Prefs.INFINITE_SOUND));
            }
        }

        if (sPrefs.loadBoolean(Prefs.VIBRATION_STATUS)){
            long[] pattern;
            if (sPrefs.loadBoolean(Prefs.INFINITE_VIBRATION)){
                pattern = new long[]{150, 86400000};
            } else {
                pattern = new long[]{150, 400, 100, 450, 200, 500, 300, 500};
            }
            builder.setVibrate(pattern);
        }
        if (Module.isPro()){
            if (sPrefs.loadBoolean(Prefs.LED_STATUS)){
                builder.setLights(sPrefs.loadInt(Prefs.LED_COLOR), 500, 1000);
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setColor(mContext.getResources().getColor(R.color.colorBlue));
        }

        boolean isWear = sPrefs.loadBoolean(Prefs.WEAR_NOTIFICATION);
        if (isWear) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                builder.setOnlyAlertOnce(true);
                builder.setGroup("GROUP");
                builder.setGroupSummary(true);
            }
        }

        mNotifyMgr = NotificationManagerCompat.from(mContext);
        Integer it = (int) (long) itemId;
        mNotifyMgr.notify(it, builder.build());

        if (isWear){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                final NotificationCompat.Builder wearableNotificationBuilder = new NotificationCompat.Builder(mContext);
                wearableNotificationBuilder.setSmallIcon(R.mipmap.ic_launcher);
                wearableNotificationBuilder.setContentTitle(name);
                wearableNotificationBuilder.setContentText(mContext.getString(R.string.missed_call_event_title));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    wearableNotificationBuilder.setColor(mContext.getResources().getColor(R.color.colorBlue));
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
     * Status bar notification for weekday reminder type.
     * @param task reminder task.
     * @param typePrefs reminder type.
     * @param i flag for sounds (1 - enabled).
     * @param itemId reminder identifier.
     * @param melody reminder custom melody file.
     * @param color LED light color.
     */
    public void showNotification(String task, String typePrefs, int i, long itemId, String melody,
                                 int color, boolean vibrate, boolean isExtra){
        sPrefs = new SharedPrefs(mContext);
        Uri soundUri;
        if (melody != null && !melody.matches("")){
            File sound = new File(melody);
            soundUri = Uri.fromFile(sound);
        } else {
            if (sPrefs.loadBoolean(Prefs.CUSTOM_SOUND)) {
                String path = sPrefs.loadPrefs(Prefs.CUSTOM_SOUND_FILE);
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

        Intent notificationIntent = new Intent(mContext, WeekDayDialog.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        PendingIntent intent = PendingIntent.getActivity(mContext, 0, notificationIntent, 0);
        builder = new NotificationCompat.Builder(mContext);
        builder.setContentTitle(task);
        builder.setContentIntent(intent);
        builder.setAutoCancel(false);
        builder.setPriority(NotificationCompat.PRIORITY_MAX);
        if (sPrefs.loadBoolean(Prefs.NOTIFICATION_REMOVE)){
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
            builder.setColor(mContext.getResources().getColor(R.color.colorBlue));
        }

        int maxVolume = 26;
        int currVolume = sPrefs.loadInt(Prefs.VOLUME);
        float log1=(float)(Math.log(maxVolume-currVolume)/Math.log(maxVolume));

        if (i == 1) {
            AudioManager am = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
            if (am.getRingerMode() == AudioManager.RINGER_MODE_NORMAL){
                sound.playAlarm(soundUri, sPrefs.loadBoolean(Prefs.INFINITE_SOUND));
            } else {
                if (sPrefs.loadBoolean(Prefs.SOUND_STATUS)) {
                    sound.playAlarm(soundUri, sPrefs.loadBoolean(Prefs.INFINITE_SOUND));
                }
            }
        }

        boolean isV = sPrefs.loadBoolean(Prefs.VIBRATION_STATUS);
        if (isExtra) isV = vibrate;
        if (isV){
            long[] pattern;
            if (sPrefs.loadBoolean(Prefs.INFINITE_VIBRATION)){
                pattern = new long[]{150, 86400000};
            } else {
                pattern = new long[]{150, 400, 100, 450, 200, 500, 300, 500};
            }
            builder.setVibrate(pattern);
        }

        if (Module.isPro()){
            if (sPrefs.loadBoolean(Prefs.LED_STATUS)){
                if (color != 0) {
                    builder.setLights(color, 500, 1000);
                } else {
                    builder.setLights(sPrefs.loadInt(Prefs.LED_COLOR), 500, 1000);
                }
            }
        }
        boolean isWear = sPrefs.loadBoolean(Prefs.WEAR_NOTIFICATION);
        if (isWear) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                builder.setOnlyAlertOnce(true);
                builder.setGroup("GROUP");
                builder.setGroupSummary(true);
            }
        }

        mNotifyMgr = NotificationManagerCompat.from(mContext);
        Integer it = (int) (long) itemId;
        mNotifyMgr.notify(it, builder.build());

        if (isWear){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                final NotificationCompat.Builder wearableNotificationBuilder = new NotificationCompat.Builder(mContext);
                wearableNotificationBuilder.setSmallIcon(R.mipmap.ic_launcher);
                wearableNotificationBuilder.setContentTitle(task);
                wearableNotificationBuilder.setContentText(app);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    wearableNotificationBuilder.setColor(mContext.getResources().getColor(R.color.colorBlue));
                }
                wearableNotificationBuilder.setOngoing(false);
                wearableNotificationBuilder.setOnlyAlertOnce(true);
                wearableNotificationBuilder.setGroup("GROUP");
                wearableNotificationBuilder.setGroupSummary(false);
                mNotifyMgr.notify(10010, wearableNotificationBuilder.build());
            }
        }
    }

    /**
     * Status bar notification for birthdays.
     * @param years user ages.
     * @param name user name.
     */
    public void showNotification(int years, String name){
        sPrefs = new SharedPrefs(mContext);
        Uri soundUri;
        boolean soundC;
        if (Module.isPro()){
            if (!sPrefs.loadBoolean(Prefs.BIRTHDAY_USE_GLOBAL)){
                soundC = sPrefs.loadBoolean(Prefs.BIRTHDAY_CUSTOM_SOUND);
            } else soundC = sPrefs.loadBoolean(Prefs.CUSTOM_SOUND);
        } else soundC = sPrefs.loadBoolean(Prefs.CUSTOM_SOUND);
        if (soundC){
            String path;
            if (Module.isPro()) {
                if (!sPrefs.loadBoolean(Prefs.BIRTHDAY_USE_GLOBAL)){
                    path = sPrefs.loadPrefs(Prefs.BIRTHDAY_CUSTOM_SOUND_FILE);
                } else path = sPrefs.loadPrefs(Prefs.CUSTOM_SOUND_FILE);
            } else path = sPrefs.loadPrefs(Prefs.CUSTOM_SOUND_FILE);
            if (path != null){
                File sound = new File(path);
                soundUri = Uri.fromFile(sound);
            } else {
                soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            }
        } else {
            soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        }
        builder = new NotificationCompat.Builder(mContext);
        builder.setContentTitle(name);
        builder.setContentText(years + " " + mContext.getString(R.string.years_string));
        builder.setSmallIcon(R.drawable.ic_cake_white_24dp);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setColor(mContext.getResources().getColor(R.color.colorBlue));
        }

        int maxVolume = 26;
        int currVolume = sPrefs.loadInt(Prefs.VOLUME);
        float log1=(float)(Math.log(maxVolume-currVolume)/Math.log(maxVolume));

        AudioManager am = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        if (am.getRingerMode() == AudioManager.RINGER_MODE_NORMAL){
            boolean isLooping;
            if (Module.isPro()){
                if (!sPrefs.loadBoolean(Prefs.BIRTHDAY_USE_GLOBAL)){
                    isLooping = sPrefs.loadBoolean(Prefs.BIRTHDAY_INFINITE_SOUND);
                } else isLooping = sPrefs.loadBoolean(Prefs.INFINITE_SOUND);
            } else isLooping = sPrefs.loadBoolean(Prefs.INFINITE_SOUND);

            sound.playAlarm(soundUri, isLooping);
        } else {
            boolean soundS;
            if (Module.isPro()){
                if (!sPrefs.loadBoolean(Prefs.BIRTHDAY_USE_GLOBAL)){
                    soundS = sPrefs.loadBoolean(Prefs.BIRTHDAY_SOUND_STATUS);
                } else soundS = sPrefs.loadBoolean(Prefs.SOUND_STATUS);
            } else soundS = sPrefs.loadBoolean(Prefs.SOUND_STATUS);

            if (soundS) {
                boolean isLooping;
                if (Module.isPro()){
                    if (!sPrefs.loadBoolean(Prefs.BIRTHDAY_USE_GLOBAL)){
                        isLooping = sPrefs.loadBoolean(Prefs.BIRTHDAY_INFINITE_SOUND);
                    } else isLooping = sPrefs.loadBoolean(Prefs.INFINITE_SOUND);
                } else isLooping = sPrefs.loadBoolean(Prefs.INFINITE_SOUND);
                sound.playAlarm(soundUri, isLooping);
            }
        }

        boolean vibrate;
        if (Module.isPro()){
            if (!sPrefs.loadBoolean(Prefs.BIRTHDAY_USE_GLOBAL)){
                vibrate = sPrefs.loadBoolean(Prefs.BIRTHDAY_VIBRATION_STATUS);
            } else vibrate = sPrefs.loadBoolean(Prefs.VIBRATION_STATUS);
        } else vibrate = sPrefs.loadBoolean(Prefs.VIBRATION_STATUS);
        if (vibrate){
            long[] pattern;
            if (sPrefs.loadBoolean(Prefs.BIRTHDAY_USE_GLOBAL)){
                if (sPrefs.loadBoolean(Prefs.INFINITE_VIBRATION)){
                    pattern = new long[]{150, 86400000};
                } else {
                    pattern = new long[]{150, 400, 100, 450, 200, 500, 300, 500};
                }
            } else {
                if (sPrefs.loadBoolean(Prefs.BIRTHDAY_INFINITE_VIBRATION)) {
                    pattern = new long[]{150, 86400000};
                } else {
                    pattern = new long[]{150, 400, 100, 450, 200, 500, 300, 500};
                }
            }
            builder.setVibrate(pattern);
        }
        if (Module.isPro()){
            if (!sPrefs.loadBoolean(Prefs.BIRTHDAY_USE_GLOBAL)) {
                if (sPrefs.loadBoolean(Prefs.BIRTHDAY_LED_STATUS)) {
                    builder.setLights(sPrefs.loadInt(Prefs.BIRTHDAY_LED_COLOR), 500, 1000);
                }
            } else {
                if (sPrefs.loadBoolean(Prefs.LED_STATUS)) {
                    builder.setLights(sPrefs.loadInt(Prefs.LED_COLOR), 500, 1000);
                }
            }
        }

        boolean isWear = sPrefs.loadBoolean(Prefs.WEAR_NOTIFICATION);
        if (isWear) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                builder.setOnlyAlertOnce(true);
                builder.setGroup("GROUP");
                builder.setGroupSummary(true);
            }
        }

        mNotifyMgr = NotificationManagerCompat.from(mContext);
        mNotifyMgr.notify(NOT_ID, builder.build());

        if (isWear){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                final NotificationCompat.Builder wearableNotificationBuilder = new NotificationCompat.Builder(mContext);
                wearableNotificationBuilder.setSmallIcon(R.mipmap.ic_launcher);
                wearableNotificationBuilder.setContentTitle(name);
                wearableNotificationBuilder.setContentText(years + " " + mContext.getString(R.string.years_string));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    wearableNotificationBuilder.setColor(mContext.getResources().getColor(R.color.colorBlue));
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
        sPrefs = new SharedPrefs(mContext);

        builder = new NotificationCompat.Builder(mContext);
        builder.setContentTitle(content);
        String app;
        if (Module.isPro()){
            app = mContext.getString(R.string.app_name_pro);
        } else app = mContext.getString(R.string.app_name);
        builder.setContentText(app);
        builder.setSmallIcon(R.drawable.ic_notifications_white_24dp);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setColor(mContext.getResources().getColor(R.color.colorBlue));
        }

        boolean isWear = sPrefs.loadBoolean(Prefs.WEAR_NOTIFICATION);
        if (isWear) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                builder.setOnlyAlertOnce(true);
                builder.setGroup("GROUP");
                builder.setGroupSummary(true);
            }
        }

        mNotifyMgr = NotificationManagerCompat.from(mContext);
        Integer it = (int) (long) id;
        mNotifyMgr.notify(it, builder.build());

        if (isWear){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                final NotificationCompat.Builder wearableNotificationBuilder = new NotificationCompat.Builder(mContext);
                wearableNotificationBuilder.setSmallIcon(R.mipmap.ic_launcher);
                wearableNotificationBuilder.setContentTitle(content);
                wearableNotificationBuilder.setContentText(app);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    wearableNotificationBuilder.setColor(mContext.getResources().getColor(R.color.colorBlue));
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
     * @param content notification title.
     * @param id note identifier.
     */
    public void showNoteNotification(String content, long id){
        sPrefs = new SharedPrefs(mContext);

        builder = new NotificationCompat.Builder(mContext);

        builder.setContentText(mContext.getString(R.string.notification_note_string));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setColor(mContext.getResources().getColor(R.color.colorBlue));
        }
        builder.setSmallIcon(R.drawable.ic_event_note_white_24dp);
        builder.setContentTitle(content);

        boolean isWear = sPrefs.loadBoolean(Prefs.WEAR_NOTIFICATION);
        if (isWear) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                builder.setOnlyAlertOnce(true);
                builder.setGroup("GROUP");
                builder.setGroupSummary(true);
            }
        }

        mNotifyMgr = NotificationManagerCompat.from(mContext);
        Integer it = (int) (long) id;
        mNotifyMgr.notify(it, builder.build());

        if (isWear){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                final NotificationCompat.Builder wearableNotificationBuilder = new NotificationCompat.Builder(mContext);
                wearableNotificationBuilder.setSmallIcon(R.mipmap.ic_launcher);
                wearableNotificationBuilder.setContentTitle(content);
                wearableNotificationBuilder.setContentText(mContext.getString(R.string.notification_note_string));
                wearableNotificationBuilder.setOngoing(false);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    wearableNotificationBuilder.setColor(mContext.getResources().getColor(R.color.colorBlue));
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
        SharedPrefs prefs = new SharedPrefs(mContext);
        if (prefs.loadBoolean(Prefs.STATUS_BAR_NOTIFICATION)) showPermanent();
    }

    /**
     * Create permanent notification in status bar.
     */
    public void showPermanent(){
        RemoteViews remoteViews = new RemoteViews(mContext.getPackageName(),
                R.layout.notification_layout);
        NotificationCompat.Builder notification = new NotificationCompat.Builder(mContext);
        notification.setAutoCancel(false);
        SharedPrefs prefs = new SharedPrefs(mContext);
        notification.setSmallIcon(R.drawable.ic_notifications_white_24dp);
        notification.setContent(remoteViews);
        notification.setOngoing(true);
        if (prefs.loadBoolean(Prefs.STATUS_BAR_ICON))
            notification.setPriority(NotificationCompat.PRIORITY_MAX);
        else notification.setPriority(NotificationCompat.PRIORITY_MIN);

        Intent resultIntent = new Intent(mContext, ReminderManager.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);
        stackBuilder.addParentStack(ReminderManager.class);
        stackBuilder.addNextIntentWithParentStack(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,
                PendingIntent.FLAG_ONE_SHOT);
        remoteViews.setOnClickPendingIntent(R.id.notificationAdd, resultPendingIntent);

        Intent noteIntent = new Intent(mContext, NotesManager.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        TaskStackBuilder noteBuilder = TaskStackBuilder.create(mContext);
        noteBuilder.addParentStack(NotesManager.class);
        noteBuilder.addNextIntent(noteIntent);
        PendingIntent notePendingIntent = noteBuilder.getPendingIntent(0,
                PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.noteAdd, notePendingIntent);

        Intent resInt = new Intent(mContext, ScreenManager.class);
        resInt.putExtra("tag", ScreenManager.FRAGMENT_ACTIVE);
        TaskStackBuilder stackInt = TaskStackBuilder.create(mContext);
        stackInt.addParentStack(ScreenManager.class);
        stackInt.addNextIntent(resInt);
        PendingIntent resultPendingInt = stackInt.getPendingIntent(0,
                PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.text, resultPendingInt);
        remoteViews.setOnClickPendingIntent(R.id.featured, resultPendingInt);
        DataBase db = new DataBase(mContext);
        db.open();
        int count = db.getCountActive();
        ArrayList<Long> dates = new ArrayList<>();
        ArrayList<String> tasks = new ArrayList<>();
        dates.clear();
        tasks.clear();
        Cursor c = db.queryGroup();
        if (c != null && c.moveToFirst()){
            do {
                int myHour = c.getInt(c.getColumnIndex(Constants.COLUMN_HOUR));
                int myMinute = c.getInt(c.getColumnIndex(Constants.COLUMN_MINUTE));
                int myDay = c.getInt(c.getColumnIndex(Constants.COLUMN_DAY));
                int myMonth = c.getInt(c.getColumnIndex(Constants.COLUMN_MONTH));
                int myYear = c.getInt(c.getColumnIndex(Constants.COLUMN_YEAR));
                int repCode = c.getInt(c.getColumnIndex(Constants.COLUMN_REPEAT));
                long remCount = c.getLong(c.getColumnIndex(Constants.COLUMN_REMINDERS_COUNT));
                long afterTime = c.getLong(c.getColumnIndex(Constants.COLUMN_REMIND_TIME));
                String type = c.getString(c.getColumnIndex(Constants.COLUMN_TYPE));
                String text = c.getString(c.getColumnIndex(Constants.COLUMN_TEXT));
                String weekdays = c.getString(c.getColumnIndex(Constants.COLUMN_WEEKDAYS));
                int isDone = c.getInt(c.getColumnIndex(Constants.COLUMN_IS_DONE));
                if ((type.startsWith(Constants.TYPE_SKYPE) ||
                        type.matches(Constants.TYPE_CALL) ||
                        type.startsWith(Constants.TYPE_APPLICATION) ||
                        type.matches(Constants.TYPE_MESSAGE) ||
                        type.matches(Constants.TYPE_REMINDER) ||
                        type.matches(Constants.TYPE_TIME)) && isDone == 0) {
                    long time = TimeCount.getEventTime(myYear, myMonth, myDay, myHour, myMinute, 0,
                            afterTime, repCode, remCount, 0);
                    if (time > 0) {
                        dates.add(time);
                        tasks.add(text);
                    }
                } else if (type.startsWith(Constants.TYPE_WEEKDAY) && isDone == 0){
                    long time = TimeCount.getNextWeekdayTime(myHour, myMinute, weekdays, 0);
                    if (time > 0) {
                        dates.add(time);
                        tasks.add(text);
                    }
                } else if (type.startsWith(Constants.TYPE_MONTHDAY) && isDone == 0){
                    long time = TimeCount.getNextMonthDayTime(myHour, myMinute, myDay, 0);
                    if (time > 0) {
                        dates.add(time);
                        tasks.add(text);
                    }
                }
            } while (c.moveToNext());
        }
        if (c != null) {
            c.close();
        }
        db.close();
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
                remoteViews.setTextViewText(R.id.text, mContext.getString(R.string.drawer_active_reminder) + " " + String.valueOf(count));
                remoteViews.setViewVisibility(R.id.featured, View.GONE);
            }
        } else {
            remoteViews.setTextViewText(R.id.text, mContext.getString(R.string.no_active_text));
            remoteViews.setViewVisibility(R.id.featured, View.GONE);
        }
        ColorSetter cs = new ColorSetter(mContext);
        remoteViews.setInt(R.id.notificationBg, "setBackgroundColor", cs.colorSetter());
        NotificationManagerCompat notifier = NotificationManagerCompat.from(mContext);
        notifier.notify(1, notification.build());
    }

    /**
     * Remove permanent norification from status bar.
     */
    public void hidePermanent(){
        ((NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(1);
    }

    public void discardNotification(){
        discardMedia();
        mNotifyMgr = NotificationManagerCompat.from(mContext);
        mNotifyMgr.cancel(NOT_ID);
    }

    public void discardStatusNotification(long id){
        Integer i = (int) (long) id;
        mNotifyMgr = NotificationManagerCompat.from(mContext);
        mNotifyMgr.cancel(i);
    }

    public void discardNotification(long id){
        discardMedia();
        Integer i = (int) (long) id;
        mNotifyMgr = NotificationManagerCompat.from(mContext);
        mNotifyMgr.cancel(i);
    }

    /**
     * Stops playing notification sound.
     */
    public void discardMedia(){
        sound.stop();
    }
}
