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
import com.cray.software.justreminder.dialogs.PlacesList;
import com.cray.software.justreminder.dialogs.utils.MapType;
import com.cray.software.justreminder.dialogs.utils.MarkerStyle;
import com.cray.software.justreminder.dialogs.utils.TargetRadius;
import com.cray.software.justreminder.dialogs.utils.TrackerOption;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.interfaces.Prefs;
import com.cray.software.justreminder.modules.Module;

public class LocationSettingsFragment extends Fragment implements View.OnClickListener {

    SharedPrefs sPrefs;
    ActionBar ab;
    RelativeLayout notificationOption, radius, markerStyleContainer, widgetDistance;
    TextView mapType, radiusText, places, markerStyle, tracker;
    CheckBox notifCheck, widgetDistanceCheck;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView =  inflater.inflate(R.layout.location_settings_layout, container, false);
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());

        ab = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if (ab != null){
            ab.setTitle(R.string.location_settings);
        }

        mapType = (TextView) rootView.findViewById(R.id.mapType);
        mapType.setOnClickListener(this);

        notificationOption = (RelativeLayout) rootView.findViewById(R.id.notificationOption);
        notificationOption.setOnClickListener(this);

        notifCheck = (CheckBox) rootView.findViewById(R.id.notifCheck);
        notifCheck.setChecked(sPrefs.loadBoolean(Prefs.TRACKING_NOTIFICATION));

        radius = (RelativeLayout) rootView.findViewById(R.id.radius);
        radius.setOnClickListener(this);

        radiusText = (TextView) rootView.findViewById(R.id.radiusText);

        places = (TextView) rootView.findViewById(R.id.places);
        places.setOnClickListener(this);

        tracker = (TextView) rootView.findViewById(R.id.tracker);
        tracker.setOnClickListener(this);

        widgetDistance = (RelativeLayout) rootView.findViewById(R.id.widgetDistance);
        widgetDistance.setOnClickListener(this);

        widgetDistanceCheck = (CheckBox) rootView.findViewById(R.id.widgetDistanceCheck);
        widgetDistanceCheck.setChecked(sPrefs.loadBoolean(Prefs.WIDGET_DISTANCE));

        if (Module.isPro()){
            markerStyleContainer = (RelativeLayout) rootView.findViewById(R.id.markerStyleContainer);
            markerStyleContainer.setVisibility(View.VISIBLE);

            markerStyle = (TextView) rootView.findViewById(R.id.markerStyle);
            markerStyle.setOnClickListener(this);
        }

        return rootView;
    }

    private void distanceChange (){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        if (widgetDistanceCheck.isChecked()){
            sPrefs.saveBoolean(Prefs.WIDGET_DISTANCE, false);
            widgetDistanceCheck.setChecked(false);
        } else {
            sPrefs.saveBoolean(Prefs.WIDGET_DISTANCE, true);
            widgetDistanceCheck.setChecked(true);
        }
    }

    private void notificationChange (){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        if (notifCheck.isChecked()){
            sPrefs.saveBoolean(Prefs.TRACKING_NOTIFICATION, false);
            notifCheck.setChecked(false);
        } else {
            sPrefs.saveBoolean(Prefs.TRACKING_NOTIFICATION, true);
            notifCheck.setChecked(true);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        radiusText.setText(sPrefs.loadInt(Prefs.LOCATION_RADIUS) + getString(R.string.meter));
    }

    @Override
    public void onDetach() {
        super.onDetach();
        ab = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if (ab != null){
            ab.setTitle(R.string.action_settings);
        }
    }

    private void chooseMapType(){
        getActivity().getApplicationContext()
                .startActivity(new Intent(getActivity().getApplicationContext(), MapType.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mapType:
                chooseMapType();
                break;
            case R.id.notificationOption:
                notificationChange();
                break;
            case R.id.widgetDistance:
                distanceChange();
                break;
            case R.id.radius:
                getActivity().getApplicationContext()
                        .startActivity(new Intent(getActivity().getApplicationContext()
                                , TargetRadius.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                break;
            case R.id.places:
                getActivity().getApplicationContext()
                        .startActivity(new Intent(getActivity().getApplicationContext()
                                , PlacesList.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                break;
            case R.id.tracker:
                getActivity().getApplicationContext()
                        .startActivity(new Intent(getActivity().getApplicationContext()
                                , TrackerOption.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                break;
            case R.id.markerStyle:
                getActivity().getApplicationContext()
                        .startActivity(new Intent(getActivity().getApplicationContext()
                                , MarkerStyle.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                break;
        }
    }
}
