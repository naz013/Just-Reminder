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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.places.PlacesActivity;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.dialogs.MarkerStyle;
import com.cray.software.justreminder.dialogs.TargetRadius;
import com.cray.software.justreminder.dialogs.TrackerOption;
import com.cray.software.justreminder.helpers.Dialogues;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.modules.Module;
import com.cray.software.justreminder.views.PrefsView;

public class LocationSettingsFragment extends Fragment implements View.OnClickListener {

    private SharedPrefs sPrefs;
    private ActionBar ab;
    private PrefsView notificationOptionPrefs, radiusPrefs;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView =  inflater.inflate(R.layout.settings_location, container, false);
        sPrefs = SharedPrefs.getInstance(getActivity());
        ab = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (ab != null){
            ab.setTitle(R.string.location);
        }
        TextView mapType = (TextView) rootView.findViewById(R.id.mapType);
        mapType.setOnClickListener(this);

        notificationOptionPrefs = (PrefsView) rootView.findViewById(R.id.notificationOptionPrefs);
        notificationOptionPrefs.setChecked(sPrefs.getBoolean(Prefs.TRACKING_NOTIFICATION));
        notificationOptionPrefs.setOnClickListener(this);

        radiusPrefs = (PrefsView) rootView.findViewById(R.id.radiusPrefs);
        radiusPrefs.setOnClickListener(this);

        TextView places = (TextView) rootView.findViewById(R.id.places);
        places.setOnClickListener(this);

        TextView tracker = (TextView) rootView.findViewById(R.id.tracker);
        tracker.setOnClickListener(this);

        if (Module.isPro()){
            LinearLayout markerStyleContainer = (LinearLayout) rootView.findViewById(R.id.markerStyleContainer);
            markerStyleContainer.setVisibility(View.VISIBLE);

            TextView markerStyle = (TextView) rootView.findViewById(R.id.markerStyle);
            markerStyle.setOnClickListener(this);
        }

        return rootView;
    }

    private void notificationChange (){
        if (notificationOptionPrefs.isChecked()){
            sPrefs.putBoolean(Prefs.TRACKING_NOTIFICATION, false);
            notificationOptionPrefs.setChecked(false);
        } else {
            sPrefs.putBoolean(Prefs.TRACKING_NOTIFICATION, true);
            notificationOptionPrefs.setChecked(true);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        radiusPrefs.setValueText(String.valueOf(sPrefs.getInt(Prefs.LOCATION_RADIUS)));
    }

    @Override
    public void onDetach() {
        super.onDetach();
        ab = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (ab != null){
            ab.setTitle(R.string.action_settings);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mapType:
                Dialogues.mapType(getActivity());
                break;
            case R.id.notificationOptionPrefs:
                notificationChange();
                break;
            case R.id.radiusPrefs:
                getActivity().getApplicationContext()
                        .startActivity(new Intent(getActivity().getApplicationContext(),
                                TargetRadius.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                break;
            case R.id.places:
                getActivity().getApplicationContext()
                        .startActivity(new Intent(getActivity().getApplicationContext(),
                                PlacesActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                break;
            case R.id.tracker:
                getActivity().getApplicationContext()
                        .startActivity(new Intent(getActivity().getApplicationContext(),
                                TrackerOption.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                break;
            case R.id.markerStyle:
                getActivity().getApplicationContext()
                        .startActivity(new Intent(getActivity().getApplicationContext(),
                                MarkerStyle.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                break;
        }
    }
}
