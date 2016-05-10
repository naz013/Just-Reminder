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
import android.location.Address;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.async.GeocoderTask;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.fragments.helpers.MapFragment;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.interfaces.MapListener;
import com.cray.software.justreminder.json.JModel;
import com.cray.software.justreminder.json.JPlace;
import com.cray.software.justreminder.roboto_views.RoboCheckBox;
import com.cray.software.justreminder.utils.ViewUtils;
import com.cray.software.justreminder.views.ActionView;
import com.cray.software.justreminder.views.DateTimeView;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class LocationFragment extends BaseFragment implements GeocoderTask.GeocoderListener, MapListener {

    private MapListener mCallbacks;
    private DateTimeView.OnSelectListener mSelect;
    private ActionView.OnActionListener mAction;

    /**
     * Location reminder variables.
     */
    private LinearLayout delayLayout;
    private RelativeLayout mapContainer;
    private ScrollView specsContainer;
    private AutoCompleteTextView searchField;
    private ActionView actionViewLocation;

    private int radius;
    private LatLng curPlace;
    private boolean isDelayed;
    private List<Address> foundPlaces;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> namesList;

    private GeocoderTask task;

    public void setNumber(String number){
        super.number = number;
        actionViewLocation.setNumber(number);
    }

    public void recreateMarkers(int radius) {
        this.radius = radius;
        if (mapFragment != null) mapFragment.recreateMarker(radius);
    }

    public int getMarker() {
        if (mapFragment != null) return mapFragment.getMarkerStyle();
        else return 0;
    }

    public boolean isDelayed() {
        return isDelayed;
    }

    public static LocationFragment newInstance(JModel item, boolean isDark, boolean hasCalendar,
                                                  boolean hasStock, boolean hasTasks) {
        LocationFragment fragment = new LocationFragment();
        Bundle args = new Bundle();
        args.putBoolean(THEME, isDark);
        args.putBoolean(CALENDAR, hasCalendar);
        args.putBoolean(STOCK, hasStock);
        args.putBoolean(TASKS, hasTasks);
        fragment.setItem(item);
        fragment.setArguments(args);
        return fragment;
    }

    public LocationFragment() {
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
        View view = inflater.inflate(R.layout.reminder_location_layout, container, false);

        SharedPrefs prefs = new SharedPrefs(getActivity());

        mapFragment = new MapFragment();
        mapFragment.setListener(this);
        mapFragment.setMarkerStyle(prefs.loadInt(Prefs.MARKER_STYLE));
        mapFragment.setMarkerTitle(eventTask);
        FragmentManager fragMan = getChildFragmentManager();
        FragmentTransaction fragTransaction = fragMan.beginTransaction();
        fragTransaction.replace(R.id.map, mapFragment);
        fragTransaction.commitAllowingStateLoss();

        delayLayout = (LinearLayout) view.findViewById(R.id.delayLayout);
        mapContainer = (RelativeLayout) view.findViewById(R.id.mapContainer);
        specsContainer = (ScrollView) view.findViewById(R.id.specsContainer);
        delayLayout.setVisibility(View.GONE);
        mapContainer.setVisibility(View.GONE);

        RoboCheckBox attackDelay = (RoboCheckBox) view.findViewById(R.id.attackDelay);
        attackDelay.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) delayLayout.setVisibility(View.VISIBLE);
                else delayLayout.setVisibility(View.GONE);
            }
        });

        if (attackDelay.isChecked()) ViewUtils.expand(delayLayout);

        ImageButton clearField = (ImageButton) view.findViewById(R.id.clearButton);
        ImageButton mapButton = (ImageButton) view.findViewById(R.id.mapButton);

        if (isDark){
            clearField.setImageResource(R.drawable.ic_backspace_white_24dp);
            mapButton.setImageResource(R.drawable.ic_map_white_24dp);
        } else {
            clearField.setImageResource(R.drawable.ic_backspace_black_24dp);
            mapButton.setImageResource(R.drawable.ic_map_black_24dp);
        }

        clearField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchField.setText("");
            }
        });
        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleMap();
            }
        });

        searchField = (AutoCompleteTextView) view.findViewById(R.id.searchField);
        searchField.setThreshold(3);
        adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_dropdown_item_1line, namesList);
        adapter.setNotifyOnChange(true);
        searchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (task != null && !task.isCancelled())
                    task.cancel(true);
                task = new GeocoderTask(getActivity(), LocationFragment.this);
                task.execute(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        searchField.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Address sel = foundPlaces.get(position);
                double lat = sel.getLatitude();
                double lon = sel.getLongitude();
                LatLng pos = new LatLng(lat, lon);
                curPlace = pos;
                String title = eventTask;
                if (title.matches(""))
                    title = pos.toString();
                if (mapFragment != null)
                    mapFragment.addMarker(pos, title, true, true, radius);
            }
        });

        actionViewLocation = (ActionView) view.findViewById(R.id.actionViewLocation);
        actionViewLocation.setListener(mAction);
        actionViewLocation.setActivity(getActivity());

        DateTimeView dateViewLocation = (DateTimeView) view.findViewById(R.id.dateViewLocation);
        dateViewLocation.setListener(mSelect);
        eventTime = System.currentTimeMillis();
        dateViewLocation.setDateTime(updateCalendar(eventTime, false));

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
                dateViewLocation.setDateTime(updateCalendar(eventTime, true));
                attackDelay.setChecked(true);
                isDelayed = true;
            } else attackDelay.setChecked(false);

            if (type.matches(Constants.TYPE_LOCATION_CALL) || type.matches(Constants.TYPE_LOCATION_MESSAGE)){
                actionViewLocation.setAction(true);
                actionViewLocation.setNumber(number);
                if (type.matches(Constants.TYPE_LOCATION_CALL))
                    actionViewLocation.setType(ActionView.TYPE_CALL);
                else actionViewLocation.setType(ActionView.TYPE_MESSAGE);
            } else {
                actionViewLocation.setAction(false);
            }

            if (mapFragment != null) {
                mapFragment.setMarkerRadius(radius);
                mapFragment.addMarker(new LatLng(latitude, longitude), text, true, false, radius);
                toggleMap();
            }
        }
        return view;
    }

    private void toggleMap() {
        if (mapContainer != null && mapContainer.getVisibility() == View.VISIBLE) {
            ViewUtils.fadeOutAnimation(mapContainer);
            ViewUtils.fadeInAnimation(specsContainer);
        } else {
            ViewUtils.fadeOutAnimation(specsContainer);
            ViewUtils.fadeInAnimation(mapContainer);
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
        mCallbacks = null;
        mAction = null;
        mSelect = null;
    }

    @Override
    public void onAddressReceived(List<Address> addresses) {
        foundPlaces = addresses;

        namesList = new ArrayList<>();
        namesList.clear();
        for (Address selected:addresses){
            String addressText = String.format("%s, %s%s",
                    selected.getMaxAddressLineIndex() > 0 ? selected.getAddressLine(0) : "",
                    selected.getMaxAddressLineIndex() > 1 ? selected.getAddressLine(1) + ", " : "",
                    selected.getCountryName());
            namesList.add(addressText);
        }
        adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_dropdown_item_1line, namesList);
        searchField.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void placeChanged(LatLng place) {
        curPlace = place;
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
            if (mCallbacks != null) mCallbacks.onBackClick();
        }
        ViewUtils.fadeOutAnimation(mapContainer);
        ViewUtils.fadeInAnimation(specsContainer);
    }
}
