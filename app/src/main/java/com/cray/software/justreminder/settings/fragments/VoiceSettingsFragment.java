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
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.activities.TimesOfDayActivity;
import com.cray.software.justreminder.dialogs.VoiceHelp;
import com.cray.software.justreminder.helpers.Dialogues;

public class VoiceSettingsFragment extends Fragment {

    private ActionBar ab;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView =  inflater.inflate(R.layout.settings_voice, container, false);
        ab = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (ab != null){
            ab.setTitle(R.string.voice_control);
        }

        TextView voiceLanguage = (TextView) rootView.findViewById(R.id.voiceLanguage);
        voiceLanguage.setOnClickListener(v -> Dialogues.language(getActivity()));
        voiceLanguage.setEnabled(true);

        TextView voiceTime = (TextView) rootView.findViewById(R.id.voiceTime);
        voiceTime.setOnClickListener(v -> getActivity().getApplicationContext()
                .startActivity(new Intent(getActivity().getApplicationContext(),
                        TimesOfDayActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)));

        TextView voiceCommands = (TextView) rootView.findViewById(R.id.voiceCommands);
        voiceCommands.setOnClickListener(v -> getActivity().getApplicationContext()
                .startActivity(new Intent(getActivity().getApplicationContext(),
                        VoiceHelp.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)));
        return rootView;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        ab = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (ab != null){
            ab.setTitle(R.string.action_settings);
        }
    }
}
