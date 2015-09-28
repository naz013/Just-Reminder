package com.cray.software.justreminder.fragments;


import android.content.Intent;
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
import com.cray.software.justreminder.dialogs.utils.LedColor;
import com.cray.software.justreminder.dialogs.utils.SelectLocale;
import com.cray.software.justreminder.dialogs.utils.SoundType;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.interfaces.Prefs;

import java.io.File;

public class BirthdayNotificationSettingsFragment extends Fragment implements View.OnClickListener {

    private SharedPrefs sPrefs;
    private ActionBar ab;
    private LinearLayout chooseSound, lewColorWrapper, chooseLedColor;
    private RelativeLayout vibrationOption;
    private RelativeLayout soundOption;
    private RelativeLayout wakeScreenOption;
    private RelativeLayout infiniteSoundOption;
    private RelativeLayout infiniteVibrateOption;
    private RelativeLayout tts;
    private RelativeLayout lewWrapper, led;
    private TextView showMelody, locale;
    private TextView textLed2, textLed3, vText, vText1, textC1, textC;
    private TextView textB, textB1, textB2, textB3, textB4, textB5, textB6, textB7, textB10, textB11, textB12;
    private CheckBox vibrationCheck, soundCheck, wakeScreenCheck, infiniteSoundCheck, ledCheck,
            globalCheck, infiniteVibrateCheck, ttsCheck;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView =  inflater.inflate(R.layout.birthday_notification_settings_layout, container, false);

        ab = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if (ab != null){
            ab.setTitle(R.string.notification_settings);
        }

        RelativeLayout globalOption = (RelativeLayout) rootView.findViewById(R.id.globalOption);
        globalOption.setOnClickListener(this);

        globalCheck = (CheckBox) rootView.findViewById(R.id.globalCheck);
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        globalCheck.setChecked(sPrefs.loadBoolean(Prefs.BIRTHDAY_USE_GLOBAL));

        vibrationOption = (RelativeLayout) rootView.findViewById(R.id.vibrationOption);
        vibrationOption.setOnClickListener(this);

        vibrationCheck = (CheckBox) rootView.findViewById(R.id.vibrationCheck);
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        vibrationCheck.setChecked(sPrefs.loadBoolean(Prefs.BIRTHDAY_VIBRATION_STATUS));

        infiniteVibrateOption = (RelativeLayout) rootView.findViewById(R.id.infiniteVibrateOption);
        infiniteVibrateOption.setOnClickListener(this);

        infiniteVibrateCheck = (CheckBox) rootView.findViewById(R.id.infiniteVibrateCheck);
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        infiniteVibrateCheck.setChecked(sPrefs.loadBoolean(Prefs.BIRTHDAY_INFINITE_VIBRATION));

        vText = (TextView) rootView.findViewById(R.id.vText);
        vText1 = (TextView) rootView.findViewById(R.id.vText1);

        checkVibrate();

        soundOption = (RelativeLayout) rootView.findViewById(R.id.soundOption);
        soundOption.setOnClickListener(this);

        soundCheck = (CheckBox) rootView.findViewById(R.id.soundCheck);
        soundCheck.setChecked(sPrefs.loadBoolean(Prefs.BIRTHDAY_SOUND_STATUS));

        chooseSound = (LinearLayout) rootView.findViewById(R.id.chooseSound);
        chooseSound.setOnClickListener(this);

        showMelody = (TextView) rootView.findViewById(R.id.showMelody);
        showMelody();

        wakeScreenOption = (RelativeLayout) rootView.findViewById(R.id.wakeScreenOption);
        wakeScreenOption.setOnClickListener(this);

        wakeScreenCheck = (CheckBox) rootView.findViewById(R.id.wakeScreenCheck);
        wakeScreenCheck.setChecked(sPrefs.loadBoolean(Prefs.BIRTHDAY_WAKE_STATUS));

        infiniteSoundOption = (RelativeLayout) rootView.findViewById(R.id.infiniteSoundOption);
        infiniteSoundOption.setOnClickListener(this);

        infiniteSoundCheck = (CheckBox) rootView.findViewById(R.id.infiniteSoundCheck);
        infiniteSoundCheck.setChecked(sPrefs.loadBoolean(Prefs.BIRTHDAY_INFINITE_SOUND));

        lewWrapper = (RelativeLayout) rootView.findViewById(R.id.lewWrapper);
        lewWrapper.setVisibility(View.VISIBLE);
        led = (RelativeLayout) rootView.findViewById(R.id.led);
        led.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ledChange();
                }
            });

        ledCheck = (CheckBox) rootView.findViewById(R.id.ledCheck);
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        ledCheck.setChecked(sPrefs.loadBoolean(Prefs.BIRTHDAY_LED_STATUS));

        lewColorWrapper = (LinearLayout) rootView.findViewById(R.id.lewColorWrapper);
        lewColorWrapper.setVisibility(View.VISIBLE);

        chooseLedColor = (LinearLayout) rootView.findViewById(R.id.chooseLedColor);
         chooseLedColor.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getActivity().getApplicationContext()
                            .startActivity(new Intent(getActivity().getApplicationContext(), LedColor.class)
                                    .putExtra(Constants.BIRTHDAY_INTENT_ID, 3)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
             }
            });
        textLed2 = (TextView) rootView.findViewById(R.id.textLed2);
        textLed3 = (TextView) rootView.findViewById(R.id.textLed3);

        textB = (TextView) rootView.findViewById(R.id.textB);
        textB1 = (TextView) rootView.findViewById(R.id.textB1);
        textB2 = (TextView) rootView.findViewById(R.id.textB2);
        textB3 = (TextView) rootView.findViewById(R.id.textB3);
        textB4 = (TextView) rootView.findViewById(R.id.textB4);
        textB5 = (TextView) rootView.findViewById(R.id.textB5);
        textB6 = (TextView) rootView.findViewById(R.id.textB6);
        textB7 = (TextView) rootView.findViewById(R.id.textB7);
        textB10 = (TextView) rootView.findViewById(R.id.textB10);
        textB11 = (TextView) rootView.findViewById(R.id.textB11);
        textB12 = (TextView) rootView.findViewById(R.id.textB12);

        textC = (TextView) rootView.findViewById(R.id.textC);
        textC1 = (TextView) rootView.findViewById(R.id.textC1);

        tts = (RelativeLayout) rootView.findViewById(R.id.tts);
        tts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ttsChange();
            }
        });

        ttsCheck = (CheckBox) rootView.findViewById(R.id.ttsCheck);
        ttsCheck.setChecked(sPrefs.loadBoolean(Prefs.BIRTHDAY_TTS));

        locale = (TextView) rootView.findViewById(R.id.locale);
        locale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().startActivity(new Intent(getActivity(), SelectLocale.class)
                        .putExtra("tts", 1)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            }
        });

        checkTTS();

        setUpEnables();
        checkEnabling();

        return rootView;
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

    private void setUpEnables(){
        if (globalCheck.isChecked()){
            vibrationOption.setEnabled(false);
            vibrationCheck.setEnabled(false);
            soundOption.setEnabled(false);
            soundCheck.setEnabled(false);
            chooseSound.setEnabled(false);
            showMelody.setEnabled(false);
            wakeScreenOption.setEnabled(false);
            wakeScreenCheck.setEnabled(false);
            infiniteSoundOption.setEnabled(false);
            infiniteSoundCheck.setEnabled(false);
            lewWrapper.setEnabled(false);
            led.setEnabled(false);
            ledCheck.setEnabled(false);
            lewColorWrapper.setEnabled(false);
            chooseLedColor.setEnabled(false);
            textLed2.setEnabled(false);
            textLed3.setEnabled(false);
            textB.setEnabled(false);
            textB1.setEnabled(false);
            textB2.setEnabled(false);
            textB3.setEnabled(false);
            textB4.setEnabled(false);
            textB5.setEnabled(false);
            textB6.setEnabled(false);
            textB7.setEnabled(false);
            textB10.setEnabled(false);
            textB11.setEnabled(false);
            textB12.setEnabled(false);
            textC.setEnabled(false);
            textC1.setEnabled(false);
            tts.setEnabled(false);
            ttsCheck.setEnabled(false);
            locale.setEnabled(false);
            infiniteVibrateOption.setEnabled(false);
            infiniteVibrateCheck.setEnabled(false);
            vText.setEnabled(false);
            vText1.setEnabled(false);
        } else {
            vibrationOption.setEnabled(true);
            vibrationCheck.setEnabled(true);
            soundOption.setEnabled(true);
            soundCheck.setEnabled(true);
            chooseSound.setEnabled(true);
            showMelody.setEnabled(true);
            wakeScreenOption.setEnabled(true);
            wakeScreenCheck.setEnabled(true);
            infiniteSoundOption.setEnabled(true);
            infiniteSoundCheck.setEnabled(true);
            lewWrapper.setEnabled(true);
            led.setEnabled(true);
            ledCheck.setEnabled(true);
            lewColorWrapper.setEnabled(true);
            chooseLedColor.setEnabled(true);
            textLed2.setEnabled(true);
            textLed3.setEnabled(true);
            textB.setEnabled(true);
            textB1.setEnabled(true);
            textB2.setEnabled(true);
            textB3.setEnabled(true);
            textB4.setEnabled(true);
            textB5.setEnabled(true);
            textB6.setEnabled(true);
            textB7.setEnabled(true);
            textB10.setEnabled(true);
            textB11.setEnabled(true);
            textB12.setEnabled(true);
            textC.setEnabled(true);
            textC1.setEnabled(true);
            tts.setEnabled(true);
            ttsCheck.setEnabled(true);
            locale.setEnabled(true);
            infiniteVibrateOption.setEnabled(true);
            infiniteVibrateCheck.setEnabled(true);
            vText.setEnabled(true);
            vText1.setEnabled(true);
        }
    }

    private void checkTTS(){
        if (ttsCheck.isChecked()){
            locale.setEnabled(true);
        } else {
            locale.setEnabled(false);
        }
    }

    private void ttsChange (){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        if (ttsCheck.isChecked()){
            sPrefs.saveBoolean(Prefs.BIRTHDAY_TTS, false);
            ttsCheck.setChecked(false);
        } else {
            sPrefs.saveBoolean(Prefs.BIRTHDAY_TTS, true);
            ttsCheck.setChecked(true);
            getActivity().startActivity(new Intent(getActivity(), SelectLocale.class)
                    .putExtra("tts", 1)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }
        checkTTS();
    }

    private void checkEnabling(){
        if (ledCheck.isEnabled()) {
            if (ledCheck.isChecked()) {
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
    }

    private void showMelody(){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        if (sPrefs.loadBoolean(Prefs.BIRTHDAY_CUSTOM_SOUND)){
            if (sPrefs.isString(Prefs.BIRTHDAY_CUSTOM_SOUND_FILE)) {
                String path = sPrefs.loadPrefs(Prefs.BIRTHDAY_CUSTOM_SOUND_FILE);
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

    private void ledChange (){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        if (ledCheck.isChecked()){
            sPrefs.saveBoolean(Prefs.BIRTHDAY_LED_STATUS, false);
            ledCheck.setChecked(false);
            checkEnabling();
        } else {
            sPrefs.saveBoolean(Prefs.BIRTHDAY_LED_STATUS, true);
            ledCheck.setChecked(true);
            checkEnabling();
        }
    }

    private void vibrationChange (){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        if (vibrationCheck.isChecked()){
            sPrefs.saveBoolean(Prefs.BIRTHDAY_VIBRATION_STATUS, false);
            vibrationCheck.setChecked(false);
            checkVibrate();
        } else {
            sPrefs.saveBoolean(Prefs.BIRTHDAY_VIBRATION_STATUS, true);
            vibrationCheck.setChecked(true);
            checkVibrate();
        }
    }

    private void infiniteVibrationChange (){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        if (infiniteVibrateCheck.isChecked()){
            sPrefs.saveBoolean(Prefs.BIRTHDAY_INFINITE_VIBRATION, false);
            infiniteVibrateCheck.setChecked(false);
        } else {
            sPrefs.saveBoolean(Prefs.BIRTHDAY_INFINITE_VIBRATION, true);
            infiniteVibrateCheck.setChecked(true);
        }
    }

    private void soundChange (){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        if (soundCheck.isChecked()){
            sPrefs.saveBoolean(Prefs.BIRTHDAY_SOUND_STATUS, false);
            soundCheck.setChecked(false);
        } else {
            sPrefs.saveBoolean(Prefs.BIRTHDAY_SOUND_STATUS, true);
            soundCheck.setChecked(true);
        }
    }

    private void wakeChange (){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        if (wakeScreenCheck.isChecked()){
            sPrefs.saveBoolean(Prefs.BIRTHDAY_WAKE_STATUS, false);
            wakeScreenCheck.setChecked(false);
        } else {
            sPrefs.saveBoolean(Prefs.BIRTHDAY_WAKE_STATUS, true);
            wakeScreenCheck.setChecked(true);
        }
    }

    private void infiniteSoundChange (){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        if (infiniteSoundCheck.isChecked()){
            sPrefs.saveBoolean(Prefs.BIRTHDAY_INFINITE_SOUND, false);
            infiniteSoundCheck.setChecked(false);
        } else {
            sPrefs.saveBoolean(Prefs.BIRTHDAY_INFINITE_SOUND, true);
            infiniteSoundCheck.setChecked(true);
        }
    }

    private void globalChange (){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        if (globalCheck.isChecked()){
            sPrefs.saveBoolean(Prefs.BIRTHDAY_USE_GLOBAL, false);
            globalCheck.setChecked(false);
        } else {
            sPrefs.saveBoolean(Prefs.BIRTHDAY_USE_GLOBAL, true);
            globalCheck.setChecked(true);
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
        ab = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if (ab != null){
            ab.setTitle(R.string.birthday_settings);
        }
    }

    void showDialog() {
        getActivity().getApplicationContext()
                .startActivity(new Intent(getActivity().getApplicationContext(), SoundType.class)
                        .putExtra(Constants.BIRTHDAY_INTENT_ID, 3)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.globalOption:
                globalChange();
                break;
            case R.id.vibrationOption:
                vibrationChange();
                break;
            case R.id.soundOption:
                soundChange();
                break;
            case R.id.wakeScreenOption:
                wakeChange();
                break;
            case R.id.infiniteVibrateOption:
                infiniteVibrationChange();
                break;
            case R.id.infiniteSoundOption:
                infiniteSoundChange();
                break;
            case R.id.chooseSound:
                showDialog();
                break;
        }
    }
}
