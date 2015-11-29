package com.cray.software.justreminder.settings.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.activities.TimesOfDay;
import com.cray.software.justreminder.activities.VoiceHelp;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.helpers.Dialogues;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.views.PrefsView;

public class VoiceSettingsFragment extends Fragment {

    private ActionBar ab;
    private TextView voiceLanguage;
    private PrefsView autoLanguagePrefs;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView =  inflater.inflate(R.layout.settings_voice, container, false);

        ab = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if (ab != null){
            ab.setTitle(R.string.voice_block);
        }

        SharedPrefs sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        boolean auto = sPrefs.loadBoolean(Prefs.AUTO_LANGUAGE);

        autoLanguagePrefs = (PrefsView) rootView.findViewById(R.id.autoLanguagePrefs);
        autoLanguagePrefs.setChecked(auto);
        autoLanguagePrefs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                languageChange();
            }
        });

        voiceLanguage = (TextView) rootView.findViewById(R.id.voiceLanguage);
        voiceLanguage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialogues.language(getActivity());
            }
        });

        if (auto){
            voiceLanguage.setEnabled(false);
        } else voiceLanguage.setEnabled(true);

        TextView voiceTime = (TextView) rootView.findViewById(R.id.voiceTime);
        voiceTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getApplicationContext()
                        .startActivity(new Intent(getActivity().getApplicationContext(),
                                TimesOfDay.class)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            }
        });

        TextView voiceCommands = (TextView) rootView.findViewById(R.id.voiceCommands);
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
        if (autoLanguagePrefs.isChecked()){
            sPrefs.saveBoolean(Prefs.AUTO_LANGUAGE, false);
            autoLanguagePrefs.setChecked(false);
            voiceLanguage.setEnabled(true);
        } else {
            sPrefs.saveBoolean(Prefs.AUTO_LANGUAGE, true);
            autoLanguagePrefs.setChecked(true);
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
