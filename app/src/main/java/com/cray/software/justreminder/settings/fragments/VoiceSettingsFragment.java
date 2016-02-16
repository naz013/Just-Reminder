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
import com.cray.software.justreminder.dialogs.VoiceHelp;
import com.cray.software.justreminder.helpers.Dialogues;

public class VoiceSettingsFragment extends Fragment {

    private ActionBar ab;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView =  inflater.inflate(R.layout.settings_voice, container, false);

        ab = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if (ab != null){
            ab.setTitle(R.string.voice_control);
        }

        TextView voiceLanguage = (TextView) rootView.findViewById(R.id.voiceLanguage);
        voiceLanguage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialogues.language(getActivity());
            }
        });
        voiceLanguage.setEnabled(true);

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

    @Override
    public void onDetach() {
        super.onDetach();
        ab = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if (ab != null){
            ab.setTitle(R.string.action_settings);
        }
    }
}
