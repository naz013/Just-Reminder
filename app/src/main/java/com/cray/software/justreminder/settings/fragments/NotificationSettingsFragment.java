package com.cray.software.justreminder.settings.fragments;


import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.helpers.Dialogues;
import com.cray.software.justreminder.helpers.Notifier;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.modules.Module;

import java.io.File;

public class NotificationSettingsFragment extends Fragment implements View.OnClickListener, DialogInterface.OnDismissListener {

    private SharedPrefs sPrefs;
    private ActionBar ab;
    private LinearLayout lewColorWrapper, chooseLedColor;
    private RelativeLayout repeatInterval;
    private TextView delayForText, repeatIntervalText;
    private TextView textLed2, textLed3, repeatText, repeatText1;
    private CheckBox wakeScreenCheck, silentSMSCheck, ledCheck, repeatNotificationCheck, autoLaunchCheck,
            unlockScreenCheck, ttsCheck, extraNotificationCheck;
    private RelativeLayout statusIcon;
    private CheckBox notificationDismissCheck, permanentNotificationCheck, statusIconCheck;
    private TextView locale;
    private RelativeLayout infiniteSoundOption;
    private RelativeLayout infiniteVibrateOption;
    private TextView showMelody;
    private TextView vText, vText1, sText, sText1, i1, i2;
    private CheckBox vibrationCheck, soundCheck, infiniteSoundCheck, infiniteVibrateCheck;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView =  inflater.inflate(R.layout.settings_notification, container, false);

        ab = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if (ab != null){
            ab.setTitle(R.string.notification_settings);
        }

        sPrefs = new SharedPrefs(getActivity().getApplicationContext());

        RelativeLayout wakeScreenOption = (RelativeLayout) rootView.findViewById(R.id.wakeScreenOption);
        wakeScreenOption.setOnClickListener(this);

        wakeScreenCheck = (CheckBox) rootView.findViewById(R.id.wakeScreenCheck);
        wakeScreenCheck.setChecked(sPrefs.loadBoolean(Prefs.WAKE_STATUS));

        RelativeLayout unlockScreen = (RelativeLayout) rootView.findViewById(R.id.unlockScreen);
        unlockScreen.setOnClickListener(this);

        unlockScreenCheck = (CheckBox) rootView.findViewById(R.id.unlockScreenCheck);
        unlockScreenCheck.setChecked(sPrefs.loadBoolean(Prefs.UNLOCK_DEVICE));

        RelativeLayout silentSMSOption = (RelativeLayout) rootView.findViewById(R.id.silentSMSOption);
        silentSMSOption.setOnClickListener(this);

        silentSMSCheck = (CheckBox) rootView.findViewById(R.id.silentSMSCheck);
        silentSMSCheck.setChecked(sPrefs.loadBoolean(Prefs.SILENT_SMS));

        RelativeLayout delayFor = (RelativeLayout) rootView.findViewById(R.id.delayFor);
        delayFor.setOnClickListener(this);

        delayForText = (TextView) rootView.findViewById(R.id.delayForText);

        RelativeLayout repeatNotificationOption = (RelativeLayout) rootView.findViewById(R.id.repeatNotificationOption);
        repeatNotificationOption.setOnClickListener(this);

        repeatNotificationCheck = (CheckBox) rootView.findViewById(R.id.repeatNotificationCheck);
        repeatNotificationCheck.setChecked(sPrefs.loadBoolean(Prefs.NOTIFICATION_REPEAT));

        repeatInterval = (RelativeLayout) rootView.findViewById(R.id.repeatInterval);
        repeatInterval.setOnClickListener(this);

        repeatIntervalText = (TextView) rootView.findViewById(R.id.repeatIntervalText);
        repeatText = (TextView) rootView.findViewById(R.id.repeatText);
        repeatText1 = (TextView) rootView.findViewById(R.id.repeatText1);

        RelativeLayout autoLaunch = (RelativeLayout) rootView.findViewById(R.id.autoLaunch);
        autoLaunch.setOnClickListener(this);

        autoLaunchCheck = (CheckBox) rootView.findViewById(R.id.autoLaunchCheck);
        autoLaunchCheck.setChecked(sPrefs.loadBoolean(Prefs.APPLICATION_AUTO_LAUNCH));

        RelativeLayout notificationDismiss = (RelativeLayout) rootView.findViewById(R.id.notificationDismiss);
        notificationDismiss.setOnClickListener(this);

        notificationDismissCheck = (CheckBox) rootView.findViewById(R.id.notificationDismissCheck);
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        notificationDismissCheck.setChecked(sPrefs.loadBoolean(Prefs.NOTIFICATION_REMOVE));

        sText = (TextView) rootView.findViewById(R.id.sText);
        sText1 = (TextView) rootView.findViewById(R.id.sText1);

        RelativeLayout permanentNotification = (RelativeLayout) rootView.findViewById(R.id.permanentNotification);
        permanentNotification.setOnClickListener(this);

        permanentNotificationCheck = (CheckBox) rootView.findViewById(R.id.permanentNotificationCheck);
        permanentNotificationCheck.setChecked(sPrefs.loadBoolean(Prefs.STATUS_BAR_NOTIFICATION));

        statusIcon = (RelativeLayout) rootView.findViewById(R.id.statusIcon);
        statusIcon.setOnClickListener(this);

        statusIconCheck = (CheckBox) rootView.findViewById(R.id.statusIconCheck);
        statusIconCheck.setChecked(sPrefs.loadBoolean(Prefs.STATUS_BAR_ICON));

        RelativeLayout vibrationOption = (RelativeLayout) rootView.findViewById(R.id.vibrationOption);
        vibrationOption.setOnClickListener(this);

        vText = (TextView) rootView.findViewById(R.id.vText);
        vText1 = (TextView) rootView.findViewById(R.id.vText1);

        vibrationCheck = (CheckBox) rootView.findViewById(R.id.vibrationCheck);
        vibrationCheck.setChecked(sPrefs.loadBoolean(Prefs.VIBRATION_STATUS));

        infiniteVibrateOption = (RelativeLayout) rootView.findViewById(R.id.infiniteVibrateOption);
        infiniteVibrateOption.setOnClickListener(this);

        infiniteVibrateCheck = (CheckBox) rootView.findViewById(R.id.infiniteVibrateCheck);
        infiniteVibrateCheck.setChecked(sPrefs.loadBoolean(Prefs.INFINITE_VIBRATION));

        RelativeLayout soundOption = (RelativeLayout) rootView.findViewById(R.id.soundOption);
        soundOption.setOnClickListener(this);

        soundCheck = (CheckBox) rootView.findViewById(R.id.soundCheck);
        soundCheck.setChecked(sPrefs.loadBoolean(Prefs.SILENT_SOUND));

        LinearLayout chooseSound = (LinearLayout) rootView.findViewById(R.id.chooseSound);
        chooseSound.setOnClickListener(this);

        TextView volume = (TextView) rootView.findViewById(R.id.volume);
        volume.setOnClickListener(this);

        showMelody = (TextView) rootView.findViewById(R.id.showMelody);
        showMelody();

        infiniteSoundOption = (RelativeLayout) rootView.findViewById(R.id.infiniteSoundOption);
        infiniteSoundOption.setOnClickListener(this);

        infiniteSoundCheck = (CheckBox) rootView.findViewById(R.id.infiniteSoundCheck);
        infiniteSoundCheck.setChecked(sPrefs.loadBoolean(Prefs.INFINITE_SOUND));

        RelativeLayout tts = (RelativeLayout) rootView.findViewById(R.id.tts);
        tts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ttsChange();
            }
        });

        ttsCheck = (CheckBox) rootView.findViewById(R.id.ttsCheck);
        ttsCheck.setChecked(sPrefs.loadBoolean(Prefs.TTS));

        RelativeLayout extraNotification = (RelativeLayout) rootView.findViewById(R.id.extraNotification);
        extraNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                extraChange();
            }
        });

        extraNotificationCheck = (CheckBox) rootView.findViewById(R.id.extraNotificationCheck);
        extraNotificationCheck.setChecked(sPrefs.loadBoolean(Prefs.EXTRA_OPTIONS));

        locale = (TextView) rootView.findViewById(R.id.locale);
        locale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialogues.ttsLocale(getActivity(), Prefs.TTS_LOCALE);
            }
        });

        sText = (TextView) rootView.findViewById(R.id.sText);
        sText1 = (TextView) rootView.findViewById(R.id.sText1);

        i1 = (TextView) rootView.findViewById(R.id.i1);
        i2 = (TextView) rootView.findViewById(R.id.i2);

        checkVibrate();

        checkRepeat();

        checkInfinite();

        checkTTS();

        checkIcon();

        if (Module.isPro()){
            RelativeLayout lewWrapper = (RelativeLayout) rootView.findViewById(R.id.lewWrapper);
            lewWrapper.setVisibility(View.VISIBLE);
            RelativeLayout led = (RelativeLayout) rootView.findViewById(R.id.led);
            led.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ledChange();
                }
            });

            ledCheck = (CheckBox) rootView.findViewById(R.id.ledCheck);
            ledCheck.setChecked(sPrefs.loadBoolean(Prefs.LED_STATUS));

            lewColorWrapper = (LinearLayout) rootView.findViewById(R.id.lewColorWrapper);
            lewColorWrapper.setVisibility(View.VISIBLE);

            chooseLedColor = (LinearLayout) rootView.findViewById(R.id.chooseLedColor);
            chooseLedColor.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Dialogues.ledColor(getActivity(), Prefs.LED_COLOR);
                }
            });
            textLed2 = (TextView) rootView.findViewById(R.id.textLed2);
            textLed3 = (TextView) rootView.findViewById(R.id.textLed3);

            checkEnabling();
        }

        return rootView;
    }

    private void extraChange() {
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        if (extraNotificationCheck.isChecked()){
            sPrefs.saveBoolean(Prefs.EXTRA_OPTIONS, false);
            extraNotificationCheck.setChecked(false);
        } else {
            sPrefs.saveBoolean(Prefs.EXTRA_OPTIONS, true);
            extraNotificationCheck.setChecked(true);
        }
    }

    private void checkInfinite(){
        if (!repeatNotificationCheck.isChecked()){
            sText.setEnabled(true);
            sText1.setEnabled(true);
            infiniteSoundCheck.setEnabled(true);
            infiniteSoundOption.setEnabled(true);
        } else {
            sText.setEnabled(false);
            sText1.setEnabled(false);
            infiniteSoundCheck.setEnabled(false);
            infiniteSoundOption.setEnabled(false);
        }
    }

    private void checkIcon(){
        if (permanentNotificationCheck.isChecked()){
            statusIcon.setEnabled(true);
            statusIconCheck.setEnabled(true);
            i1.setEnabled(true);
            i2.setEnabled(true);
        } else {
            statusIcon.setEnabled(false);
            statusIconCheck.setEnabled(false);
            i1.setEnabled(false);
            i2.setEnabled(false);
        }
    }

    private void checkTTS(){
        if (ttsCheck.isChecked()){
            locale.setEnabled(true);
        } else {
            locale.setEnabled(false);
        }
    }

    private void checkVibrate(){
        if (vibrationCheck.isChecked()){
            infiniteVibrateOption.setEnabled(true);
            infiniteVibrateCheck.setEnabled(true);
            vText.setEnabled(true);
            vText1.setEnabled(true);
        } else {
            infiniteVibrateOption.setEnabled(false);
            infiniteVibrateCheck.setEnabled(false);
            vText.setEnabled(false);
            vText1.setEnabled(false);
        }
    }

    private void showMelody(){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        if (sPrefs.loadBoolean(Prefs.CUSTOM_SOUND)){
            if (sPrefs.isString(Prefs.CUSTOM_SOUND_FILE)) {
                String path = sPrefs.loadPrefs(Prefs.CUSTOM_SOUND_FILE);
                if (!path.matches("")) {
                    File sound = new File(path);
                    String fileName = sound.getName();
                    int pos = fileName.lastIndexOf(".");
                    String fileNameS = fileName.substring(0, pos);
                    showMelody.setText(fileNameS);
                } else {
                    showMelody.setText(getResources().getString(R.string.sound_default));
                }
            }
        } else {
            showMelody.setText(getResources().getString(R.string.sound_default));
        }
    }

    private void vibrationChange (){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        if (vibrationCheck.isChecked()){
            sPrefs.saveBoolean(Prefs.VIBRATION_STATUS, false);
            vibrationCheck.setChecked(false);
        } else {
            sPrefs.saveBoolean(Prefs.VIBRATION_STATUS, true);
            vibrationCheck.setChecked(true);
        }
        checkVibrate();
    }

    private void infiniteVibrationChange (){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        if (infiniteVibrateCheck.isChecked()){
            sPrefs.saveBoolean(Prefs.INFINITE_VIBRATION, false);
            infiniteVibrateCheck.setChecked(false);
        } else {
            sPrefs.saveBoolean(Prefs.INFINITE_VIBRATION, true);
            infiniteVibrateCheck.setChecked(true);
        }
    }

    private void ttsChange (){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        if (ttsCheck.isChecked()){
            sPrefs.saveBoolean(Prefs.TTS, false);
            ttsCheck.setChecked(false);
        } else {
            sPrefs.saveBoolean(Prefs.TTS, true);
            ttsCheck.setChecked(true);
            Dialogues.ttsLocale(getActivity(), Prefs.TTS_LOCALE);
        }
        checkTTS();
    }

    private void soundChange (){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        if (soundCheck.isChecked()){
            sPrefs.saveBoolean(Prefs.SILENT_SOUND, false);
            soundCheck.setChecked(false);
        } else {
            sPrefs.saveBoolean(Prefs.SILENT_SOUND, true);
            soundCheck.setChecked(true);
        }
    }

    private void infiniteSoundChange (){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        if (infiniteSoundCheck.isChecked()){
            sPrefs.saveBoolean(Prefs.INFINITE_SOUND, false);
            infiniteSoundCheck.setChecked(false);
        } else {
            if (!sPrefs.loadBoolean(Prefs.NOTIFICATION_REPEAT)) {
                sPrefs.saveBoolean(Prefs.INFINITE_SOUND, true);
                infiniteSoundCheck.setChecked(true);
            }
        }
    }

    private void iconChange (){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        if (statusIconCheck.isChecked()){
            sPrefs.saveBoolean(Prefs.STATUS_BAR_ICON, false);
            statusIconCheck.setChecked(false);
            new Notifier(getActivity()).recreatePermanent();
        } else {
            sPrefs.saveBoolean(Prefs.STATUS_BAR_ICON, true);
            statusIconCheck.setChecked(true);
            new Notifier(getActivity()).recreatePermanent();
        }
    }

    private void notificationChange (){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        if (permanentNotificationCheck.isChecked()){
            sPrefs.saveBoolean(Prefs.STATUS_BAR_NOTIFICATION, false);
            permanentNotificationCheck.setChecked(false);
            new Notifier(getActivity()).hidePermanent();
        } else {
            sPrefs.saveBoolean(Prefs.STATUS_BAR_NOTIFICATION, true);
            permanentNotificationCheck.setChecked(true);
            new Notifier(getActivity()).recreatePermanent();
        }
        checkIcon();
    }

    private void notificationDismissChange (){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        if (notificationDismissCheck.isChecked()){
            sPrefs.saveBoolean(Prefs.NOTIFICATION_REMOVE, false);
            notificationDismissCheck.setChecked(false);
        } else {
            sPrefs.saveBoolean(Prefs.NOTIFICATION_REMOVE, true);
            notificationDismissCheck.setChecked(true);
        }
    }

    private void checkEnabling(){
        if (ledCheck.isChecked()){
            lewColorWrapper.setEnabled(true);
            chooseLedColor.setEnabled(true);
            textLed2.setEnabled(true);
            textLed3.setEnabled(true);
        } else {
            lewColorWrapper.setEnabled(false);
            chooseLedColor.setEnabled(false);
            textLed2.setEnabled(false);
            textLed3.setEnabled(false);
        }
    }

    private void showDays(){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        int days;
        if (sPrefs.isString(Prefs.DELAY_TIME)) {
            days = sPrefs.loadInt(Prefs.DELAY_TIME);
        } else days = 0;
        delayForText.setText(String.valueOf(days));
    }

    private void showRepeat(){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        int days;
        if (sPrefs.isString(Prefs.NOTIFICATION_REPEAT_INTERVAL)) {
            days = sPrefs.loadInt(Prefs.NOTIFICATION_REPEAT_INTERVAL);
        } else days = 0;
        repeatIntervalText.setText(String.valueOf(days));
    }

    private void ledChange (){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        if (ledCheck.isChecked()){
            sPrefs.saveBoolean(Prefs.LED_STATUS, false);
            ledCheck.setChecked(false);
        } else {
            sPrefs.saveBoolean(Prefs.LED_STATUS, true);
            ledCheck.setChecked(true);
        }
        checkEnabling();
    }

    private void silentSMSChange (){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        if (silentSMSCheck.isChecked()){
            sPrefs.saveBoolean(Prefs.SILENT_SMS, false);
            silentSMSCheck.setChecked(false);
        } else {
            sPrefs.saveBoolean(Prefs.SILENT_SMS, true);
            silentSMSCheck.setChecked(true);
        }
    }

    private void autoLaunchChange (){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        if (autoLaunchCheck.isChecked()){
            sPrefs.saveBoolean(Prefs.APPLICATION_AUTO_LAUNCH, false);
            autoLaunchCheck.setChecked(false);
        } else {
            sPrefs.saveBoolean(Prefs.APPLICATION_AUTO_LAUNCH, true);
            autoLaunchCheck.setChecked(true);
        }
    }

    private void checkRepeat(){
        if (repeatNotificationCheck.isChecked()){
            repeatInterval.setEnabled(true);
            repeatIntervalText.setEnabled(true);
            repeatText.setEnabled(true);
            repeatText1.setEnabled(true);
        } else {
            repeatInterval.setEnabled(false);
            repeatIntervalText.setEnabled(false);
            repeatText.setEnabled(false);
            repeatText1.setEnabled(false);
        }
    }

    private void wakeChange (){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        if (wakeScreenCheck.isChecked()){
            sPrefs.saveBoolean(Prefs.WAKE_STATUS, false);
            wakeScreenCheck.setChecked(false);
        } else {
            sPrefs.saveBoolean(Prefs.WAKE_STATUS, true);
            wakeScreenCheck.setChecked(true);
        }
    }

    private void unlockChange (){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        if (unlockScreenCheck.isChecked()){
            sPrefs.saveBoolean(Prefs.UNLOCK_DEVICE, false);
            unlockScreenCheck.setChecked(false);
        } else {
            sPrefs.saveBoolean(Prefs.UNLOCK_DEVICE, true);
            unlockScreenCheck.setChecked(true);
        }
    }

    private void repeatChange (){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        if (repeatNotificationCheck.isChecked()){
            sPrefs.saveBoolean(Prefs.NOTIFICATION_REPEAT, false);
            repeatNotificationCheck.setChecked(false);
        } else {
            sPrefs.saveBoolean(Prefs.NOTIFICATION_REPEAT, true);
            repeatNotificationCheck.setChecked(true);
            sPrefs.saveBoolean(Prefs.INFINITE_SOUND, false);
        }
        checkRepeat();
        checkInfinite();
    }

    @Override
    public void onResume() {
        showDays();
        showRepeat();
        super.onResume();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        ab = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if (ab != null){
            ab.setTitle(R.string.action_settings);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.vibrationOption:
                vibrationChange();
                break;
            case R.id.soundOption:
                soundChange();
                break;
            case R.id.infiniteSoundOption:
                infiniteSoundChange();
                break;
            case R.id.chooseSound:
                Dialogues.melodyType(getActivity(), Prefs.CUSTOM_SOUND, this, 201);
                break;
            case R.id.infiniteVibrateOption:
                infiniteVibrationChange();
                break;
            case R.id.statusIcon:
                iconChange();
                break;
            case R.id.permanentNotification:
                notificationChange();
                break;
            case R.id.notificationDismiss:
                notificationDismissChange();
                break;
            case R.id.silentSMSOption:
                silentSMSChange();
                break;
            case R.id.autoLaunch:
                autoLaunchChange();
                break;
            case R.id.wakeScreenOption:
                wakeChange();
                break;
            case R.id.unlockScreen:
                unlockChange();
                break;
            case R.id.repeatNotificationOption:
                repeatChange();
                break;
            case R.id.delayFor:
                Dialogues.dialogWithSeek(getActivity(), 60, Prefs.DELAY_TIME, getString(R.string.delay_dialog_title), this);
                break;
            case R.id.volume:
                Dialogues.dialogWithSeek(getActivity(), 25, Prefs.VOLUME, getString(R.string.settings_volume_title), this);
                break;
            case R.id.repeatInterval:
                Dialogues.dialogWithSeek(getActivity(), 60, Prefs.NOTIFICATION_REPEAT_INTERVAL, getString(R.string.repeat_interval_dialog_title), this);
                break;
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        showDays();
        showRepeat();
        showMelody();
    }
}
