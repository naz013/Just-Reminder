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
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.fragments.helpers.MapCallback;
import com.cray.software.justreminder.fragments.helpers.MapFragment;
import com.cray.software.justreminder.helpers.Permissions;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.interfaces.MapListener;
import com.cray.software.justreminder.reminder.json.JsonModel;
import com.cray.software.justreminder.reminder.json.JPlace;
import com.cray.software.justreminder.roboto_views.RoboCheckBox;
import com.cray.software.justreminder.roboto_views.RoboRadioButton;
import com.cray.software.justreminder.roboto_views.RoboTextView;
import com.cray.software.justreminder.utils.LocationUtil;
import com.cray.software.justreminder.utils.ViewUtils;
import com.cray.software.justreminder.views.ActionView;
import com.cray.software.justreminder.views.DateTimeView;
import com.google.android.gms.maps.model.LatLng;

public class OutLocationFragment extends BaseFragment implements MapListener,
        CompoundButton.OnCheckedChangeListener, MapCallback {

    private MapListener mCallbacks;
    private DateTimeView.OnSelectListener mSelect;
    private ActionView.OnActionListener mAction;

    /**
     * Location out reminder variables.
     */
    private RelativeLayout mapContainerOut;
    private ScrollView specsContainerOut;
    private RoboTextView currentLocation;
    private RoboTextView radiusMark;
    private RoboCheckBox attachDelayOut;
    private RoboRadioButton currentCheck, mapCheck;
    private ActionView actionViewLocationOut;
    private RoboTextView mapLocation;
    private SeekBar pointRadius;
    private DateTimeView dateViewLocationOut;

    private int radius;
    private LatLng curPlace;
    private boolean isDelayed;

    private LocationManager mLocationManager;
    private LocationListener mLocList;

    private Activity mContext;

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
            this.pointRadius.setProgress(SharedPrefs.getInstance(mContext).getInt(Prefs.LOCATION_RADIUS));
        } else this.pointRadius.setProgress(pointRadius);
    }

    public boolean isDelayed() {
        return isDelayed;
    }

    public static OutLocationFragment newInstance(JsonModel item, boolean isDark, boolean hasCalendar,
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
        mapFragment = new MapFragment();
        mapFragment.setListener(this);
        mapFragment.setMarkerStyle(SharedPrefs.getInstance(mContext).getInt(Prefs.MARKER_STYLE));
        mapFragment.setMarkerTitle(eventTask);
        mapFragment.setCallback(this);
        FragmentManager fragMan = getChildFragmentManager();
        FragmentTransaction fragTransaction = fragMan.beginTransaction();
        fragTransaction.replace(R.id.mapOut, mapFragment);
        fragTransaction.commitAllowingStateLoss();

        LinearLayout delayLayoutOut = (LinearLayout) view.findViewById(R.id.delayLayoutOut);
        specsContainerOut = (ScrollView) view.findViewById(R.id.specsContainerOut);
        mapContainerOut = (RelativeLayout) view.findViewById(R.id.mapContainerOut);
        delayLayoutOut.setVisibility(View.GONE);
        mapContainerOut.setVisibility(View.GONE);

        attachDelayOut = (RoboCheckBox) view.findViewById(R.id.attachDelayOut);
        attachDelayOut.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) delayLayoutOut.setVisibility(View.VISIBLE);
            else delayLayoutOut.setVisibility(View.GONE);
        });

        if (attachDelayOut.isChecked()) delayLayoutOut.setVisibility(View.VISIBLE);

        ImageButton mapButtonOut = (ImageButton) view.findViewById(R.id.mapButtonOut);
        if (isDark) mapButtonOut.setImageResource(R.drawable.ic_map_white_24dp);
        else mapButtonOut.setImageResource(R.drawable.ic_map_black_24dp);

        mapButtonOut.setOnClickListener(v -> {
            if (mapCheck.isChecked()) {
                toggleMap();
            }
            mapCheck.setChecked(true);
        });
        currentLocation = (RoboTextView) view.findViewById(R.id.currentLocation);
        mapLocation = (RoboTextView) view.findViewById(R.id.mapLocation);
        radiusMark = (RoboTextView) view.findViewById(R.id.radiusMark);

        currentCheck = (RoboRadioButton) view.findViewById(R.id.currentCheck);
        mapCheck = (RoboRadioButton) view.findViewById(R.id.mapCheck);
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
        if (pointRadius.getProgress() == 0) {
            pointRadius.setProgress(SharedPrefs.getInstance(mContext).getInt(Prefs.LOCATION_RADIUS));
        }

        actionViewLocationOut = (ActionView) view.findViewById(R.id.actionViewLocationOut);
        actionViewLocationOut.setListener(mAction);
        actionViewLocationOut.setActivity(mContext);

        dateViewLocationOut = (DateTimeView) view.findViewById(R.id.dateViewLocationOut);
        dateViewLocationOut.setListener(mSelect);
        eventTime = System.currentTimeMillis();
        dateViewLocationOut.setDateTime(updateCalendar(eventTime, false));
        return view;
    }

    private void toggleMap() {
        if (mapContainerOut != null && mapContainerOut.getVisibility() == View.VISIBLE) {
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
        if (mContext == null) {
            mContext = activity;
        }
        try {
            if (mCallbacks == null) mCallbacks = (MapListener) activity;
            if (mAction == null) mAction = (ActionView.OnActionListener) activity;
            if (mSelect == null) mSelect = (DateTimeView.OnSelectListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement listeners.");
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (mContext == null) {
            mContext = (Activity) context;
        }
        try {
            if (mCallbacks == null) mCallbacks = (MapListener) context;
            if (mAction == null) mAction = (ActionView.OnActionListener) context;
            if (mSelect == null) mSelect = (DateTimeView.OnSelectListener) context;
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

    @Override
    public void onMapReady() {
        if (curPlace != null) {
            if (mapFragment != null) {
                mapFragment.addMarker(curPlace, null, true, true, radius);
                toggleMap();
            }
            mapLocation.setText(LocationUtil.getAddress(curPlace.latitude, curPlace.longitude));
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
                if (type.matches(Constants.TYPE_LOCATION_OUT_CALL)) {
                    actionViewLocationOut.setType(ActionView.TYPE_CALL);
                } else actionViewLocationOut.setType(ActionView.TYPE_MESSAGE);
            } else actionViewLocationOut.setAction(false);

            Log.d(Constants.LOG_TAG, "lon " + longitude + ", lat " + latitude);
            LatLng pos = new LatLng(latitude, longitude);
            if (mapFragment != null) {
                Log.d(Constants.LOG_TAG, "Map not null");
                mapFragment.setMarkerRadius(radius);
                mapFragment.addMarker(pos, text, true, true, radius);
            }

            mapLocation.setText(LocationUtil.getAddress(pos.latitude, pos.longitude));
            mapCheck.setChecked(true);
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
            if (TextUtils.isEmpty(text)) text = _Location;
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
            if (Permissions.checkPermission(mContext, Permissions.ACCESS_COARSE_LOCATION, Permissions.ACCESS_FINE_LOCATION)) {
                mLocationManager.removeUpdates(mLocList);
            } else {
                Permissions.requestPermission(mContext, 201, Permissions.ACCESS_FINE_LOCATION, Permissions.ACCESS_COARSE_LOCATION);
            }
        }
    }

    private void updateListener() {
        if (mContext == null) return;
        mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        long time = (SharedPrefs.getInstance(mContext).getInt(Prefs.TRACK_TIME) * 1000) * 2;
        int distance = SharedPrefs.getInstance(mContext).getInt(Prefs.TRACK_DISTANCE) * 2;
        if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, time, distance, mLocList);
        } else {
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, time, distance, mLocList);
        }
    }
}
