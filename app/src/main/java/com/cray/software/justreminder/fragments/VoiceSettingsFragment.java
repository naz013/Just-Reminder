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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.VoiceHelp;
import com.cray.software.justreminder.dialogs.AboutDialog;
import com.cray.software.justreminder.dialogs.utils.TimesOfDay;
import com.cray.software.justreminder.dialogs.utils.VoiceLanguage;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.interfaces.Constants;

public class VoiceSettingsFragment extends Fragment {

    ActionBar ab;
    TextView voiceCommands, assignContacts, voiceLanguage, voiceTime;
    RelativeLayout autoLanguage;
    CheckBox autoLanguageCheck;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView =  inflater.inflate(R.layout.voice_settings_layout, container, false);

        ab = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if (ab != null){
            ab.setTitle(R.string.voice_block);
        }

        autoLanguage = (RelativeLayout) rootView.findViewById(R.id.autoLanguage);
        autoLanguage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                languageChange();
            }
        });

        autoLanguageCheck = (CheckBox) rootView.findViewById(R.id.autoLanguageCheck);
        SharedPrefs sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        boolean auto = sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_AUTO_LANGUAGE);
        autoLanguageCheck.setChecked(auto);

        voiceLanguage = (TextView) rootView.findViewById(R.id.voiceLanguage);
        voiceLanguage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getApplicationContext()
                        .startActivity(new Intent(getActivity().getApplicationContext(),
                                VoiceLanguage.class)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            }
        });

        if (auto){
            voiceLanguage.setEnabled(false);
        } else voiceLanguage.setEnabled(true);

        voiceTime = (TextView) rootView.findViewById(R.id.voiceTime);
        voiceTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getApplicationContext()
                        .startActivity(new Intent(getActivity().getApplicationContext(),
                                TimesOfDay.class)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            }
        });

        assignContacts = (TextView) rootView.findViewById(R.id.assignContacts);
        assignContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getApplicationContext()
                        .startActivity(new Intent(getActivity().getApplicationContext(),
                                AboutDialog.class)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            }
        });

        voiceCommands = (TextView) rootView.findViewById(R.id.voiceCommands);
        voiceCommands.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getApplicationContext()
                        .startActivity(new Intent(getActivity().getApplicationContext(),
                                VoiceHelp.class)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            }
        });
        return rootView;
    }

    private void languageChange (){
        SharedPrefs sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        if (autoLanguageCheck.isChecked()){
            sPrefs.saveBoolean(Constants.APP_UI_PREFERENCES_AUTO_LANGUAGE, false);
            autoLanguageCheck.setChecked(false);
            voiceLanguage.setEnabled(true);
        } else {
            sPrefs.saveBoolean(Constants.APP_UI_PREFERENCES_AUTO_LANGUAGE, true);
            autoLanguageCheck.setChecked(true);
            voiceLanguage.setEnabled(false);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        ab = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if (ab != null){
            ab.setTitle(R.string.action_settings);
        }
    }
}
