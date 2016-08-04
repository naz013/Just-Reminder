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

import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.helpers.Dialogues;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.views.PrefsView;

import java.io.File;

public class BirthdayNotificationSettingsFragment extends Fragment implements View.OnClickListener, DialogInterface.OnDismissListener {

    private SharedPrefs mPrefs;
    private ActionBar ab;
    private TextView locale;
    private PrefsView globalOptionPrefs, vibrationOptionPrefs, infiniteVibrateOptionPrefs, 
            soundOptionPrefs, infiniteSoundOptionPrefs, wakeScreenOptionPrefs, chooseSoundPrefs, 
            ttsPrefs, ledPrefs, chooseLedColorPrefs;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView =  inflater.inflate(R.layout.settings_birthday_notification_layout, container, false);
        ab = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (ab != null){
            ab.setTitle(R.string.birthday_notification);
        }
        mPrefs = SharedPrefs.getInstance(getActivity());

        globalOptionPrefs = (PrefsView) rootView.findViewById(R.id.globalOptionPrefs);
        globalOptionPrefs.setChecked(mPrefs.getBoolean(Prefs.BIRTHDAY_USE_GLOBAL));
        globalOptionPrefs.setOnClickListener(this);

        vibrationOptionPrefs = (PrefsView) rootView.findViewById(R.id.vibrationOptionPrefs);
        vibrationOptionPrefs.setChecked(mPrefs.getBoolean(Prefs.BIRTHDAY_VIBRATION_STATUS));
        vibrationOptionPrefs.setOnClickListener(this);

        infiniteVibrateOptionPrefs = (PrefsView) rootView.findViewById(R.id.infiniteVibrateOptionPrefs);
        infiniteVibrateOptionPrefs.setChecked(mPrefs.getBoolean(Prefs.BIRTHDAY_INFINITE_VIBRATION));
        infiniteVibrateOptionPrefs.setOnClickListener(this);

        soundOptionPrefs = (PrefsView) rootView.findViewById(R.id.soundOptionPrefs);
        soundOptionPrefs.setChecked(mPrefs.getBoolean(Prefs.BIRTHDAY_SOUND_STATUS));
        soundOptionPrefs.setOnClickListener(this);

        infiniteSoundOptionPrefs = (PrefsView) rootView.findViewById(R.id.infiniteSoundOptionPrefs);
        infiniteSoundOptionPrefs.setChecked(mPrefs.getBoolean(Prefs.BIRTHDAY_INFINITE_SOUND));
        infiniteSoundOptionPrefs.setOnClickListener(this);

        wakeScreenOptionPrefs = (PrefsView) rootView.findViewById(R.id.wakeScreenOptionPrefs);
        wakeScreenOptionPrefs.setChecked(mPrefs.getBoolean(Prefs.BIRTHDAY_WAKE_STATUS));
        wakeScreenOptionPrefs.setOnClickListener(this);

        chooseSoundPrefs = (PrefsView) rootView.findViewById(R.id.chooseSoundPrefs);
        chooseSoundPrefs.setOnClickListener(this);

        ttsPrefs = (PrefsView) rootView.findViewById(R.id.ttsPrefs);
        ttsPrefs.setChecked(mPrefs.getBoolean(Prefs.BIRTHDAY_TTS));
        ttsPrefs.setOnClickListener(this);

        ledPrefs = (PrefsView) rootView.findViewById(R.id.ledPrefs);
        ledPrefs.setChecked(mPrefs.getBoolean(Prefs.BIRTHDAY_LED_STATUS));
        ledPrefs.setOnClickListener(this);

        chooseLedColorPrefs = (PrefsView) rootView.findViewById(R.id.chooseLedColorPrefs);
        chooseLedColorPrefs.setOnClickListener(this);
        chooseLedColorPrefs.setOnClickListener(view -> Dialogues.ledColor(getActivity(), Prefs.BIRTHDAY_LED_COLOR));

        locale = (TextView) rootView.findViewById(R.id.locale);
        locale.setOnClickListener(v -> Dialogues.ttsLocale(getActivity(), Prefs.BIRTHDAY_TTS_LOCALE));

        checkVibrate();
        showMelody();
        checkTTS();
        setUpEnables();
        checkEnabling();

        return rootView;
    }

    private void checkVibrate(){
        if (vibrationOptionPrefs.isChecked()){
            infiniteVibrateOptionPrefs.setEnabled(true);
        } else {
            infiniteVibrateOptionPrefs.setEnabled(false);
        }
    }

    private void setUpEnables(){
        if (globalOptionPrefs.isChecked()){
            vibrationOptionPrefs.setEnabled(false);
            infiniteVibrateOptionPrefs.setEnabled(false);
            soundOptionPrefs.setEnabled(false);
            infiniteSoundOptionPrefs.setEnabled(false);
            wakeScreenOptionPrefs.setEnabled(false);
            chooseSoundPrefs.setEnabled(false);
            ttsPrefs.setEnabled(false);
            ledPrefs.setEnabled(false);
            chooseLedColorPrefs.setEnabled(false);
            locale.setEnabled(false);
        } else {
            vibrationOptionPrefs.setEnabled(true);
            infiniteVibrateOptionPrefs.setEnabled(true);
            soundOptionPrefs.setEnabled(true);
            infiniteSoundOptionPrefs.setEnabled(true);
            wakeScreenOptionPrefs.setEnabled(true);
            chooseSoundPrefs.setEnabled(true);
            ttsPrefs.setEnabled(true);
            ledPrefs.setEnabled(true);
            chooseLedColorPrefs.setEnabled(true);
            locale.setEnabled(true);
        }
        checkVibrate();
        checkTTS();
        checkEnabling();
    }

    private void checkTTS(){
        if (ttsPrefs.isChecked()){
            locale.setEnabled(true);
        } else {
            locale.setEnabled(false);
        }
    }

    private void ttsChange (){
        if (ttsPrefs.isChecked()){
            mPrefs.putBoolean(Prefs.BIRTHDAY_TTS, false);
            ttsPrefs.setChecked(false);
        } else {
            mPrefs.putBoolean(Prefs.BIRTHDAY_TTS, true);
            ttsPrefs.setChecked(true);
            Dialogues.ttsLocale(getActivity(), Prefs.BIRTHDAY_TTS_LOCALE);
        }
        checkTTS();
    }

    private void checkEnabling(){
        if (ledPrefs.isChecked()) {
            chooseLedColorPrefs.setEnabled(true);
        } else {
            chooseLedColorPrefs.setEnabled(false);
        }
    }

    private void showMelody(){
        if (mPrefs.getBoolean(Prefs.BIRTHDAY_CUSTOM_SOUND)){
            if (mPrefs.hasKey(Prefs.BIRTHDAY_CUSTOM_SOUND_FILE)) {
                String path = mPrefs.getString(Prefs.BIRTHDAY_CUSTOM_SOUND_FILE);
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

    private void ledChange (){
        if (ledPrefs.isChecked()){
            mPrefs.putBoolean(Prefs.BIRTHDAY_LED_STATUS, false);
            ledPrefs.setChecked(false);
            checkEnabling();
        } else {
            mPrefs.putBoolean(Prefs.BIRTHDAY_LED_STATUS, true);
            ledPrefs.setChecked(true);
            checkEnabling();
        }
    }

    private void vibrationChange (){
        if (vibrationOptionPrefs.isChecked()){
            mPrefs.putBoolean(Prefs.BIRTHDAY_VIBRATION_STATUS, false);
            vibrationOptionPrefs.setChecked(false);
            checkVibrate();
        } else {
            mPrefs.putBoolean(Prefs.BIRTHDAY_VIBRATION_STATUS, true);
            vibrationOptionPrefs.setChecked(true);
            checkVibrate();
        }
    }

    private void infiniteVibrationChange (){
        if (infiniteVibrateOptionPrefs.isChecked()){
            mPrefs.putBoolean(Prefs.BIRTHDAY_INFINITE_VIBRATION, false);
            infiniteVibrateOptionPrefs.setChecked(false);
        } else {
            mPrefs.putBoolean(Prefs.BIRTHDAY_INFINITE_VIBRATION, true);
            infiniteVibrateOptionPrefs.setChecked(true);
        }
    }

    private void soundChange (){
        if (soundOptionPrefs.isChecked()){
            mPrefs.putBoolean(Prefs.BIRTHDAY_SOUND_STATUS, false);
            soundOptionPrefs.setChecked(false);
        } else {
            mPrefs.putBoolean(Prefs.BIRTHDAY_SOUND_STATUS, true);
            soundOptionPrefs.setChecked(true);
        }
    }

    private void wakeChange (){
        if (wakeScreenOptionPrefs.isChecked()){
            mPrefs.putBoolean(Prefs.BIRTHDAY_WAKE_STATUS, false);
            wakeScreenOptionPrefs.setChecked(false);
        } else {
            mPrefs.putBoolean(Prefs.BIRTHDAY_WAKE_STATUS, true);
            wakeScreenOptionPrefs.setChecked(true);
        }
    }

    private void infiniteSoundChange (){
        if (infiniteSoundOptionPrefs.isChecked()){
            mPrefs.putBoolean(Prefs.BIRTHDAY_INFINITE_SOUND, false);
            infiniteSoundOptionPrefs.setChecked(false);
        } else {
            mPrefs.putBoolean(Prefs.BIRTHDAY_INFINITE_SOUND, true);
            infiniteSoundOptionPrefs.setChecked(true);
        }
    }

    private void globalChange (){
        if (globalOptionPrefs.isChecked()){
            mPrefs.putBoolean(Prefs.BIRTHDAY_USE_GLOBAL, false);
            globalOptionPrefs.setChecked(false);
        } else {
            mPrefs.putBoolean(Prefs.BIRTHDAY_USE_GLOBAL, true);
            globalOptionPrefs.setChecked(true);
        }
        setUpEnables();
    }

    @Override
    public void onResume() {
        showMelody();
        super.onResume();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        ab = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (ab != null){
            ab.setTitle(R.string.birthday_notification);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.globalOptionPrefs:
                globalChange();
                break;
            case R.id.vibrationOptionPrefs:
                vibrationChange();
                break;
            case R.id.soundOptionPrefs:
                soundChange();
                break;
            case R.id.wakeScreenOptionPrefs:
                wakeChange();
                break;
            case R.id.infiniteVibrateOptionPrefs:
                infiniteVibrationChange();
                break;
            case R.id.infiniteSoundOptionPrefs:
                infiniteSoundChange();
                break;
            case R.id.chooseSoundPrefs:
                Dialogues.melodyType(getActivity(), Prefs.BIRTHDAY_CUSTOM_SOUND, this, 200);
                break;
            case R.id.ledPrefs:
                ledChange();
                break;
            case R.id.ttsPrefs:
                ttsChange();
                break;
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        showMelody();
    }
}
