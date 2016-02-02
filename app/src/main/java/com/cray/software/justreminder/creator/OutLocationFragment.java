/*
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

package com.cray.software.justreminder.creator;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.fragments.helpers.MapFragment;
import com.cray.software.justreminder.helpers.Permissions;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.interfaces.MapListener;
import com.cray.software.justreminder.json.JModel;
import com.cray.software.justreminder.json.JPlace;
import com.cray.software.justreminder.utils.LocationUtil;
import com.cray.software.justreminder.utils.ViewUtils;
import com.cray.software.justreminder.views.ActionView;
import com.cray.software.justreminder.views.DateTimeView;
import com.google.android.gms.maps.model.LatLng;

public class OutLocationFragment extends BaseFragment implements MapListener,
        CompoundButton.OnCheckedChangeListener {

    private MapListener mCallbacks;
    private DateTimeView.OnSelectListener mSelect;
    private ActionView.OnActionListener mAction;

    /**
     * Location out reminder variables.
     */
    private RelativeLayout mapContainerOut;
    private ScrollView specsContainerOut;
    private TextView currentLocation;
    private TextView radiusMark;
    private CheckBox attachDelayOut;
    private RadioButton currentCheck, mapCheck;
    private ActionView actionViewLocationOut;
    private TextView mapLocation;
    private SeekBar pointRadius;

    private int radius;
    private LatLng curPlace;
    private boolean isDelayed;

    private LocationManager mLocationManager;
    private LocationListener mLocList;

    public void setNumber(String number){
        super.number = number;
        actionViewLocationOut.setNumber(number);
    }

    public void recreateMarkers(int radius) {
        this.radius = radius;
        if (mapFragment != null) mapFragment.recreateMarker(radius);
    }

    public int getMarker() {
        if (mapFragment != null) return mapFragment.getMarkerStyle();
        else return 0;
    }

    public void setPointRadius(int pointRadius) {
        if (pointRadius == -1) {
            this.pointRadius.setProgress(new SharedPrefs(getActivity()).loadInt(Prefs.LOCATION_RADIUS));
        } else this.pointRadius.setProgress(pointRadius);
    }

    public boolean isDelayed() {
        return isDelayed;
    }

    public static OutLocationFragment newInstance(JModel item, boolean isDark, boolean hasCalendar,
                                                  boolean hasStock, boolean hasTasks) {
        OutLocationFragment fragment = new OutLocationFragment();
        Bundle args = new Bundle();
        args.putBoolean(THEME, isDark);
        args.putBoolean(CALENDAR, hasCalendar);
        args.putBoolean(STOCK, hasStock);
        args.putBoolean(TASKS, hasTasks);
        fragment.setItem(item);
        fragment.setArguments(args);
        return fragment;
    }

    public OutLocationFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            hasCalendar = args.getBoolean(CALENDAR);
            hasStock = args.getBoolean(STOCK);
            hasTasks = args.getBoolean(TASKS);
            isDark = args.getBoolean(THEME);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Indicate that this fragment would like to influence the set of actions in the action bar.
        setHasOptionsMenu(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.reminder_location_out_layout, container, false);

        SharedPrefs prefs = new SharedPrefs(getActivity());

        mapFragment = new MapFragment();
        mapFragment.setListener(this);
        mapFragment.setMarkerStyle(prefs.loadInt(Prefs.MARKER_STYLE));
        mapFragment.setMarkerTitle(eventTask);
        FragmentManager fragMan = getChildFragmentManager();
        FragmentTransaction fragTransaction = fragMan.beginTransaction();
        fragTransaction.replace(R.id.mapOut, mapFragment);
        fragTransaction.commitAllowingStateLoss();

        LinearLayout delayLayoutOut = (LinearLayout) view.findViewById(R.id.delayLayoutOut);
        specsContainerOut = (ScrollView) view.findViewById(R.id.specsContainerOut);
        mapContainerOut = (RelativeLayout) view.findViewById(R.id.mapContainerOut);
        delayLayoutOut.setVisibility(View.GONE);
        mapContainerOut.setVisibility(View.GONE);

        attachDelayOut = (CheckBox) view.findViewById(R.id.attachDelayOut);
        attachDelayOut.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) attachDelayOut.setVisibility(View.VISIBLE);
                else attachDelayOut.setVisibility(View.GONE);
            }
        });

        if (attachDelayOut.isChecked()) ViewUtils.expand(delayLayoutOut);

        ImageButton mapButtonOut = (ImageButton) view.findViewById(R.id.mapButtonOut);
        if (isDark)
            mapButtonOut.setImageResource(R.drawable.ic_map_white_24dp);
        else
            mapButtonOut.setImageResource(R.drawable.ic_map_black_24dp);

        mapButtonOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mapCheck.isChecked()) {
                    toggleMap();
                }
                mapCheck.setChecked(true);
            }
        });
        currentLocation = (TextView) view.findViewById(R.id.currentLocation);
        mapLocation = (TextView) view.findViewById(R.id.mapLocation);
        radiusMark = (TextView) view.findViewById(R.id.radiusMark);

        currentCheck = (RadioButton) view.findViewById(R.id.currentCheck);
        mapCheck = (RadioButton) view.findViewById(R.id.mapCheck);
        currentCheck.setOnCheckedChangeListener(this);
        mapCheck.setOnCheckedChangeListener(this);
        currentCheck.setChecked(true);

        pointRadius = (SeekBar) view.findViewById(R.id.pointRadius);
        pointRadius.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                radiusMark.setText(String.format(getString(R.string.radius_x_meters), progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        if (pointRadius.getProgress() == 0)
            pointRadius.setProgress(prefs.loadInt(Prefs.LOCATION_RADIUS));

        actionViewLocationOut = (ActionView) view.findViewById(R.id.actionViewLocationOut);
        actionViewLocationOut.setListener(mAction);
        actionViewLocationOut.setActivity(getActivity());

        DateTimeView dateViewLocationOut = (DateTimeView) view.findViewById(R.id.dateViewLocationOut);
        dateViewLocationOut.setListener(mSelect);
        eventTime = System.currentTimeMillis();
        dateViewLocationOut.setDateTime(updateCalendar(eventTime, false));

        if (curPlace != null) {
            if (mapFragment != null)
                mapFragment.addMarker(curPlace, null, true, true, radius);
            mapLocation.setText(LocationUtil.getAddress(curPlace.latitude, curPlace.longitude));
        }

        if (curPlace != null) {
            if (mapFragment != null) {
                mapFragment.addMarker(curPlace, null, true, true, radius);
                toggleMap();
            }
        }

        if (item != null) {
            String text = item.getSummary();
            number = item.getAction().getTarget();
            JPlace jPlace = item.getPlace();
            double latitude = jPlace.getLatitude();
            double longitude = jPlace.getLongitude();
            radius = jPlace.getRadius();
            String type = item.getType();

            long eventTime = item.getEventTime();

            if (item != null && eventTime > 0) {
                dateViewLocationOut.setDateTime(updateCalendar(eventTime, true));
                attachDelayOut.setChecked(true);
                isDelayed = true;
            } else attachDelayOut.setChecked(false);

            if (type.matches(Constants.TYPE_LOCATION_OUT_CALL) || type.matches(Constants.TYPE_LOCATION_OUT_MESSAGE)){
                actionViewLocationOut.setAction(true);
                actionViewLocationOut.setNumber(number);
                if (type.matches(Constants.TYPE_LOCATION_OUT_CALL))
                    actionViewLocationOut.setType(ActionView.TYPE_CALL);
                else
                    actionViewLocationOut.setType(ActionView.TYPE_MESSAGE);
            } else actionViewLocationOut.setAction(false);

            LatLng pos = new LatLng(latitude, longitude);
            if (mapFragment != null) {
                mapFragment.setMarkerRadius(radius);
                mapFragment.addMarker(pos, text, true, true, radius);
            }

            mapLocation.setText(LocationUtil.getAddress(pos.latitude, pos.longitude));
            mapCheck.setChecked(true);
        }
        return view;
    }

    private void toggleMap() {
        if (mapContainerOut != null &&
                mapContainerOut.getVisibility() == View.VISIBLE) {
            ViewUtils.fadeOutAnimation(mapContainerOut);
            ViewUtils.fadeInAnimation(specsContainerOut);
        } else {
            ViewUtils.fadeOutAnimation(specsContainerOut);
            ViewUtils.fadeInAnimation(mapContainerOut);
            if (mapFragment != null) {
                mapFragment.showShowcase();
            }
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallbacks = (MapListener) activity;
            mAction = (ActionView.OnActionListener) activity;
            mSelect = (DateTimeView.OnSelectListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement listeners.");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        removeUpdates();
        mCallbacks = null;
        mAction = null;
        mSelect = null;
    }

    @Override
    public void placeChanged(LatLng place) {
        curPlace = place;
        mapLocation.setText(LocationUtil.getAddress(place.latitude, place.longitude));
        if (mCallbacks != null) mCallbacks.placeChanged(place);
    }

    @Override
    public void onZoomClick(boolean isFull) {
        if (mCallbacks != null) mCallbacks.onZoomClick(isFull);
    }

    @Override
    public void onBackClick() {
        if (mapFragment.isFullscreen()) {
            mapFragment.setFullscreen(false);
            mCallbacks.onBackClick();
        }
        ViewUtils.fadeOutAnimation(mapContainerOut);
        ViewUtils.fadeInAnimation(specsContainerOut);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()){
            case R.id.currentCheck:
                if (currentCheck.isChecked()) {
                    mapCheck.setChecked(false);
                    mLocList = new CurrentLocation();
                    updateListener();
                }
                break;
            case R.id.mapCheck:
                if (mapCheck.isChecked()) {
                    currentCheck.setChecked(false);
                    toggleMap();
                    removeUpdates();
                }
                break;
        }
    }

    public class CurrentLocation implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            double currentLat = location.getLatitude();
            double currentLong = location.getLongitude();
            curPlace = new LatLng(currentLat, currentLong);
            String _Location = LocationUtil.getAddress(currentLat, currentLong);
            String text = eventTask;
            if (text.matches("")) text = _Location;
            currentLocation.setText(_Location);
            if (mapFragment != null) {
                mapFragment.addMarker(new LatLng(currentLat, currentLong), text, true, true, radius);
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            updateListener();
        }

        @Override
        public void onProviderEnabled(String provider) {
            updateListener();
        }

        @Override
        public void onProviderDisabled(String provider) {
            updateListener();
        }
    }

    private void removeUpdates() {
        if (mLocList != null) {
            if (Permissions.checkPermission(getActivity(),
                    Permissions.ACCESS_COARSE_LOCATION, Permissions.ACCESS_FINE_LOCATION)) {
                mLocationManager.removeUpdates(mLocList);
            } else {
                Permissions.requestPermission(getActivity(), 201,
                        Permissions.ACCESS_FINE_LOCATION,
                        Permissions.ACCESS_COARSE_LOCATION);
            }
        }
    }

    private void updateListener() {
        mLocationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        SharedPrefs prefs = new SharedPrefs(getActivity());
        long time = (prefs.loadInt(Prefs.TRACK_TIME) * 1000) * 2;
        int distance = prefs.loadInt(Prefs.TRACK_DISTANCE) * 2;
        if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, time, distance, mLocList);
        } else {
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, time, distance, mLocList);
        }
    }
}
