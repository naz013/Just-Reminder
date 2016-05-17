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

package com.cray.software.justreminder.settings.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.helpers.Dialogues;
import com.cray.software.justreminder.helpers.Notifier;
import com.cray.software.justreminder.helpers.Permissions;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.modules.Module;
import com.cray.software.justreminder.views.PrefsView;

import java.io.File;

public class NotificationSettingsFragment extends Fragment implements View.OnClickListener, 
        DialogInterface.OnDismissListener {

    private SharedPrefs sPrefs;
    private ActionBar ab;
    private TextView locale, volume;
    
    private PrefsView blurPrefs, notificationDismissPrefs, permanentNotificationPrefs, 
            statusIconPrefs, vibrationOptionPrefs, infiniteVibrateOptionPrefs, 
            soundOptionPrefs, infiniteSoundOptionPrefs, ttsPrefs, wakeScreenOptionPrefs, 
            unlockScreenPrefs, silentSMSOptionPrefs, autoLaunchPrefs, ledPrefs, 
            repeatNotificationOptionPrefs, repeatIntervalPrefs,
            chooseSoundPrefs, delayForPrefs, chooseLedColorPrefs, streamPrefs, systemPrefs,
            increasePrefs;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView =  inflater.inflate(R.layout.settings_notification, container, false);

        ab = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if (ab != null){
            ab.setTitle(R.string.notification);
        }

        sPrefs = new SharedPrefs(getActivity().getApplicationContext());

        TextView selectImage = (TextView) rootView.findViewById(R.id.selectImage);
        selectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Permissions.checkPermission(getActivity(), Permissions.MANAGE_DOCUMENTS)) {
                    Dialogues.imageDialog(getActivity(), null);
                } else {
                    Permissions.requestPermission(getActivity(), 117, Permissions.MANAGE_DOCUMENTS);
                }
            }
        });

        blurPrefs = (PrefsView) rootView.findViewById(R.id.blurPrefs);
        if (Module.isPro()) {
            blurPrefs.setOnClickListener(this);
            blurPrefs.setChecked(sPrefs.loadBoolean(Prefs.REMINDER_IMAGE_BLUR));
            blurPrefs.setVisibility(View.VISIBLE);
        } else {
            blurPrefs.setVisibility(View.GONE);
        }

        notificationDismissPrefs = (PrefsView) rootView.findViewById(R.id.notificationDismissPrefs);
        notificationDismissPrefs.setOnClickListener(this);
        notificationDismissPrefs.setChecked(sPrefs.loadBoolean(Prefs.NOTIFICATION_REMOVE));

        permanentNotificationPrefs = (PrefsView) rootView.findViewById(R.id.permanentNotificationPrefs);
        permanentNotificationPrefs.setOnClickListener(this);
        permanentNotificationPrefs.setChecked(sPrefs.loadBoolean(Prefs.STATUS_BAR_NOTIFICATION));

        statusIconPrefs = (PrefsView) rootView.findViewById(R.id.statusIconPrefs);
        statusIconPrefs.setOnClickListener(this);
        statusIconPrefs.setChecked(sPrefs.loadBoolean(Prefs.STATUS_BAR_ICON));

        vibrationOptionPrefs = (PrefsView) rootView.findViewById(R.id.vibrationOptionPrefs);
        vibrationOptionPrefs.setOnClickListener(this);
        vibrationOptionPrefs.setChecked(sPrefs.loadBoolean(Prefs.VIBRATION_STATUS));

        infiniteVibrateOptionPrefs = (PrefsView) rootView.findViewById(R.id.infiniteVibrateOptionPrefs);
        infiniteVibrateOptionPrefs.setOnClickListener(this);
        infiniteVibrateOptionPrefs.setChecked(sPrefs.loadBoolean(Prefs.INFINITE_VIBRATION));

        soundOptionPrefs = (PrefsView) rootView.findViewById(R.id.soundOptionPrefs);
        soundOptionPrefs.setOnClickListener(this);
        soundOptionPrefs.setChecked(sPrefs.loadBoolean(Prefs.SILENT_SOUND));

        infiniteSoundOptionPrefs = (PrefsView) rootView.findViewById(R.id.infiniteSoundOptionPrefs);
        infiniteSoundOptionPrefs.setOnClickListener(this);
        infiniteSoundOptionPrefs.setChecked(sPrefs.loadBoolean(Prefs.INFINITE_SOUND));

        ttsPrefs = (PrefsView) rootView.findViewById(R.id.ttsPrefs);
        ttsPrefs.setOnClickListener(this);
        ttsPrefs.setChecked(sPrefs.loadBoolean(Prefs.TTS));

        wakeScreenOptionPrefs = (PrefsView) rootView.findViewById(R.id.wakeScreenOptionPrefs);
        wakeScreenOptionPrefs.setOnClickListener(this);
        wakeScreenOptionPrefs.setChecked(sPrefs.loadBoolean(Prefs.WAKE_STATUS));

        unlockScreenPrefs = (PrefsView) rootView.findViewById(R.id.unlockScreenPrefs);
        unlockScreenPrefs.setOnClickListener(this);
        unlockScreenPrefs.setChecked(sPrefs.loadBoolean(Prefs.UNLOCK_DEVICE));

        silentSMSOptionPrefs = (PrefsView) rootView.findViewById(R.id.silentSMSOptionPrefs);
        silentSMSOptionPrefs.setOnClickListener(this);
        silentSMSOptionPrefs.setChecked(sPrefs.loadBoolean(Prefs.SILENT_SMS));

        autoLaunchPrefs = (PrefsView) rootView.findViewById(R.id.autoLaunchPrefs);
        autoLaunchPrefs.setOnClickListener(this);
        autoLaunchPrefs.setChecked(sPrefs.loadBoolean(Prefs.APPLICATION_AUTO_LAUNCH));

        repeatNotificationOptionPrefs = (PrefsView) rootView.findViewById(R.id.repeatNotificationOptionPrefs);
        repeatNotificationOptionPrefs.setOnClickListener(this);
        repeatNotificationOptionPrefs.setChecked(sPrefs.loadBoolean(Prefs.NOTIFICATION_REPEAT));

        systemPrefs = (PrefsView) rootView.findViewById(R.id.systemPrefs);
        systemPrefs.setOnClickListener(this);
        systemPrefs.setChecked(sPrefs.loadBoolean(Prefs.SYSTEM_VOLUME));

        streamPrefs = (PrefsView) rootView.findViewById(R.id.streamPrefs);
        streamPrefs.setOnClickListener(this);

        increasePrefs = (PrefsView) rootView.findViewById(R.id.increasePrefs);
        increasePrefs.setOnClickListener(this);
        increasePrefs.setChecked(sPrefs.loadBoolean(Prefs.INCREASING_VOLUME));

        repeatIntervalPrefs = (PrefsView) rootView.findViewById(R.id.repeatIntervalPrefs);
        repeatIntervalPrefs.setOnClickListener(this);

        chooseSoundPrefs = (PrefsView) rootView.findViewById(R.id.chooseSoundPrefs);
        chooseSoundPrefs.setOnClickListener(this);

        showMelody();

        delayForPrefs = (PrefsView) rootView.findViewById(R.id.delayForPrefs);
        delayForPrefs.setOnClickListener(this);

        volume = (TextView) rootView.findViewById(R.id.volume);
        volume.setOnClickListener(this);

        locale = (TextView) rootView.findViewById(R.id.locale);
        locale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialogues.ttsLocale(getActivity(), Prefs.TTS_LOCALE);
            }
        });

        ledPrefs = (PrefsView) rootView.findViewById(R.id.ledPrefs);
        chooseLedColorPrefs = (PrefsView) rootView.findViewById(R.id.chooseLedColorPrefs);

        checkVibrate();

        checkRepeat();

        checkInfinite();

        checkTTS();

        checkIcon();

        checkSystem();

        if (Module.isPro()){
            ledPrefs.setOnClickListener(this);
            ledPrefs.setVisibility(View.VISIBLE);
            ledPrefs.setChecked(sPrefs.loadBoolean(Prefs.LED_STATUS));
            
            chooseLedColorPrefs.setVisibility(View.VISIBLE);
            chooseLedColorPrefs.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Dialogues.ledColor(getActivity(), Prefs.LED_COLOR);
                }
            });

            checkEnabling();
        } else {
            ledPrefs.setVisibility(View.GONE);
            chooseLedColorPrefs.setVisibility(View.GONE);
        }

        return rootView;
    }

    private void increaseChange() {
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        if (increasePrefs.isChecked()){
            sPrefs.saveBoolean(Prefs.INCREASING_VOLUME, false);
            increasePrefs.setChecked(false);
        } else {
            sPrefs.saveBoolean(Prefs.INCREASING_VOLUME, true);
            increasePrefs.setChecked(true);
        }
        checkSystem();
    }

    private void systemChange() {
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        if (systemPrefs.isChecked()){
            sPrefs.saveBoolean(Prefs.SYSTEM_VOLUME, false);
            systemPrefs.setChecked(false);
        } else {
            sPrefs.saveBoolean(Prefs.SYSTEM_VOLUME, true);
            systemPrefs.setChecked(true);
        }
        checkSystem();
    }

    private void checkSystem(){
        if (!systemPrefs.isChecked()){
            volume.setEnabled(true);
            streamPrefs.setEnabled(false);
        } else {
            volume.setEnabled(false);
            streamPrefs.setEnabled(true);
        }
    }

    private void checkInfinite(){
        if (!repeatNotificationOptionPrefs.isChecked()){
            infiniteSoundOptionPrefs.setEnabled(true);
        } else {
            infiniteVibrateOptionPrefs.setEnabled(false);
            infiniteSoundOptionPrefs.setEnabled(false);
        }
    }

    private void checkIcon(){
        if (permanentNotificationPrefs.isChecked()){
            statusIconPrefs.setEnabled(true);
        } else {
            statusIconPrefs.setEnabled(false);
        }
    }

    private void checkTTS(){
        if (ttsPrefs.isChecked()){
            locale.setEnabled(true);
        } else {
            locale.setEnabled(false);
        }
    }

    private void checkVibrate(){
        if (vibrationOptionPrefs.isChecked()){
            infiniteVibrateOptionPrefs.setEnabled(true);
        } else {
            infiniteVibrateOptionPrefs.setEnabled(false);
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
                    chooseSoundPrefs.setDetailText(fileNameS);
                } else {
                    chooseSoundPrefs.setDetailText(getResources().getString(R.string.default_string));
                }
            }
        } else {
            chooseSoundPrefs.setDetailText(getResources().getString(R.string.default_string));
        }
    }

    private void vibrationChange (){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        if (vibrationOptionPrefs.isChecked()){
            sPrefs.saveBoolean(Prefs.VIBRATION_STATUS, false);
            vibrationOptionPrefs.setChecked(false);
        } else {
            sPrefs.saveBoolean(Prefs.VIBRATION_STATUS, true);
            vibrationOptionPrefs.setChecked(true);
        }
        checkVibrate();
    }

    private void infiniteVibrationChange (){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        if (infiniteVibrateOptionPrefs.isChecked()){
            sPrefs.saveBoolean(Prefs.INFINITE_VIBRATION, false);
            infiniteVibrateOptionPrefs.setChecked(false);
        } else {
            sPrefs.saveBoolean(Prefs.INFINITE_VIBRATION, true);
            infiniteVibrateOptionPrefs.setChecked(true);
        }
    }

    private void ttsChange (){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        if (ttsPrefs.isChecked()){
            sPrefs.saveBoolean(Prefs.TTS, false);
            ttsPrefs.setChecked(false);
        } else {
            sPrefs.saveBoolean(Prefs.TTS, true);
            ttsPrefs.setChecked(true);
            Dialogues.ttsLocale(getActivity(), Prefs.TTS_LOCALE);
        }
        checkTTS();
    }

    private void blurChange (){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        if (blurPrefs.isChecked()){
            sPrefs.saveBoolean(Prefs.REMINDER_IMAGE_BLUR, false);
            blurPrefs.setChecked(false);
        } else {
            sPrefs.saveBoolean(Prefs.REMINDER_IMAGE_BLUR, true);
            blurPrefs.setChecked(true);
        }
    }

    private void soundChange (){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        if (soundOptionPrefs.isChecked()){
            sPrefs.saveBoolean(Prefs.SILENT_SOUND, false);
            soundOptionPrefs.setChecked(false);
        } else {
            sPrefs.saveBoolean(Prefs.SILENT_SOUND, true);
            soundOptionPrefs.setChecked(true);
        }
    }

    private void infiniteSoundChange (){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        if (infiniteSoundOptionPrefs.isChecked()){
            sPrefs.saveBoolean(Prefs.INFINITE_SOUND, false);
            infiniteSoundOptionPrefs.setChecked(false);
        } else {
            if (!sPrefs.loadBoolean(Prefs.NOTIFICATION_REPEAT)) {
                sPrefs.saveBoolean(Prefs.INFINITE_SOUND, true);
                infiniteSoundOptionPrefs.setChecked(true);
            }
        }
    }

    private void iconChange (){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        if (statusIconPrefs.isChecked()){
            sPrefs.saveBoolean(Prefs.STATUS_BAR_ICON, false);
            statusIconPrefs.setChecked(false);
            new Notifier(getActivity()).recreatePermanent();
        } else {
            sPrefs.saveBoolean(Prefs.STATUS_BAR_ICON, true);
            statusIconPrefs.setChecked(true);
            new Notifier(getActivity()).recreatePermanent();
        }
    }

    private void notificationChange (){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        if (permanentNotificationPrefs.isChecked()){
            sPrefs.saveBoolean(Prefs.STATUS_BAR_NOTIFICATION, false);
            permanentNotificationPrefs.setChecked(false);
            new Notifier(getActivity()).hidePermanent();
        } else {
            sPrefs.saveBoolean(Prefs.STATUS_BAR_NOTIFICATION, true);
            permanentNotificationPrefs.setChecked(true);
            new Notifier(getActivity()).recreatePermanent();
        }
        checkIcon();
    }

    private void notificationDismissChange (){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        if (notificationDismissPrefs.isChecked()){
            sPrefs.saveBoolean(Prefs.NOTIFICATION_REMOVE, false);
            notificationDismissPrefs.setChecked(false);
        } else {
            sPrefs.saveBoolean(Prefs.NOTIFICATION_REMOVE, true);
            notificationDismissPrefs.setChecked(true);
        }
    }

    private void checkEnabling(){
        if (ledPrefs.isChecked()){
            chooseLedColorPrefs.setEnabled(true);
        } else {
            chooseLedColorPrefs.setEnabled(false);
        }
    }

    private void showDays(){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        int days;
        if (sPrefs.isString(Prefs.DELAY_TIME)) {
            days = sPrefs.loadInt(Prefs.DELAY_TIME);
        } else days = 0;
        delayForPrefs.setValueText(String.valueOf(days));
    }

    private void showRepeat(){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        int days;
        if (sPrefs.isString(Prefs.NOTIFICATION_REPEAT_INTERVAL)) {
            days = sPrefs.loadInt(Prefs.NOTIFICATION_REPEAT_INTERVAL);
        } else days = 0;
        repeatIntervalPrefs.setValueText(String.valueOf(days));
    }

    private void ledChange (){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        if (ledPrefs.isChecked()){
            sPrefs.saveBoolean(Prefs.LED_STATUS, false);
            ledPrefs.setChecked(false);
        } else {
            sPrefs.saveBoolean(Prefs.LED_STATUS, true);
            ledPrefs.setChecked(true);
        }
        checkEnabling();
    }

    private void silentSMSChange (){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        if (silentSMSOptionPrefs.isChecked()){
            sPrefs.saveBoolean(Prefs.SILENT_SMS, false);
            silentSMSOptionPrefs.setChecked(false);
        } else {
            sPrefs.saveBoolean(Prefs.SILENT_SMS, true);
            silentSMSOptionPrefs.setChecked(true);
        }
    }

    private void autoLaunchChange (){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        if (autoLaunchPrefs.isChecked()){
            sPrefs.saveBoolean(Prefs.APPLICATION_AUTO_LAUNCH, false);
            autoLaunchPrefs.setChecked(false);
        } else {
            sPrefs.saveBoolean(Prefs.APPLICATION_AUTO_LAUNCH, true);
            autoLaunchPrefs.setChecked(true);
        }
    }

    private void checkRepeat(){
        if (repeatNotificationOptionPrefs.isChecked()){
            repeatIntervalPrefs.setEnabled(true);
        } else {
            repeatIntervalPrefs.setEnabled(false);
        }
    }

    private void wakeChange (){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        if (wakeScreenOptionPrefs.isChecked()){
            sPrefs.saveBoolean(Prefs.WAKE_STATUS, false);
            wakeScreenOptionPrefs.setChecked(false);
        } else {
            sPrefs.saveBoolean(Prefs.WAKE_STATUS, true);
            wakeScreenOptionPrefs.setChecked(true);
        }
    }

    private void unlockChange (){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        if (unlockScreenPrefs.isChecked()){
            sPrefs.saveBoolean(Prefs.UNLOCK_DEVICE, false);
            unlockScreenPrefs.setChecked(false);
        } else {
            sPrefs.saveBoolean(Prefs.UNLOCK_DEVICE, true);
            unlockScreenPrefs.setChecked(true);
        }
    }

    private void repeatChange (){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        if (repeatNotificationOptionPrefs.isChecked()){
            sPrefs.saveBoolean(Prefs.NOTIFICATION_REPEAT, false);
            repeatNotificationOptionPrefs.setChecked(false);
        } else {
            sPrefs.saveBoolean(Prefs.NOTIFICATION_REPEAT, true);
            repeatNotificationOptionPrefs.setChecked(true);
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
            case R.id.blurPrefs:
                blurChange();
                break;
            case R.id.increasePrefs:
                increaseChange();
                break;
            case R.id.vibrationOptionPrefs:
                vibrationChange();
                break;
            case R.id.soundOptionPrefs:
                soundChange();
                break;
            case R.id.infiniteSoundOptionPrefs:
                infiniteSoundChange();
                break;
            case R.id.chooseSoundPrefs:
                Dialogues.melodyType(getActivity(), Prefs.CUSTOM_SOUND, this, 201);
                break;
            case R.id.systemPrefs:
                systemChange();
                break;
            case R.id.streamPrefs:
                Dialogues.streamDialog(getActivity());
                break;
            case R.id.infiniteVibrateOptionPrefs:
                infiniteVibrationChange();
                break;
            case R.id.statusIconPrefs:
                iconChange();
                break;
            case R.id.permanentNotificationPrefs:
                notificationChange();
                break;
            case R.id.notificationDismissPrefs:
                notificationDismissChange();
                break;
            case R.id.silentSMSOptionPrefs:
                silentSMSChange();
                break;
            case R.id.autoLaunchPrefs:
                autoLaunchChange();
                break;
            case R.id.wakeScreenOptionPrefs:
                wakeChange();
                break;
            case R.id.unlockScreenPrefs:
                unlockChange();
                break;
            case R.id.repeatNotificationOptionPrefs:
                repeatChange();
                break;
            case R.id.delayForPrefs:
                Dialogues.dialogWithSeek(getActivity(), 60, Prefs.DELAY_TIME, getString(R.string.snooze_time), this);
                break;
            case R.id.volume:
                Dialogues.dialogWithSeek(getActivity(), 25, Prefs.VOLUME, getString(R.string.loudness), this);
                break;
            case R.id.repeatIntervalPrefs:
                Dialogues.dialogWithSeek(getActivity(), 60, Prefs.NOTIFICATION_REPEAT_INTERVAL, getString(R.string.interval), this);
                break;
            case R.id.ttsPrefs:
                ttsChange();
                break;
            case R.id.ledPrefs:
                ledChange();
                break;
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        showDays();
        showRepeat();
        showMelody();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 101:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    if (Module.isKitkat()) {
                        intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                        intent.addCategory(Intent.CATEGORY_OPENABLE);
                    }
                    intent.setType("image/*");
                    Intent chooser = Intent.createChooser(intent, getActivity().getString(R.string.image));
                    getActivity().startActivityForResult(chooser, Constants.ACTION_REQUEST_GALLERY);
                }
                break;
        }
    }
}
