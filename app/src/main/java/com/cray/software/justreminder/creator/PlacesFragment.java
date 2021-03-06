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
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.fragments.helpers.MapCallback;
import com.cray.software.justreminder.fragments.helpers.PlacesMapFragment;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.interfaces.MapListener;
import com.cray.software.justreminder.reminder.json.JsonModel;
import com.cray.software.justreminder.reminder.json.JPlace;

import java.util.ArrayList;
import java.util.List;

public class PlacesFragment extends BaseFragment implements MapCallback {

    private MapListener mListener;

    public void recreateMarkers(int radius) {
        if (placesMap != null) placesMap.recreateMarker(radius);
    }

    public ArrayList<JPlace> getPlaces() {
        if (placesMap != null) return placesMap.getPlaces();
        else return null;
    }

    public static PlacesFragment newInstance(JsonModel item, boolean isDark, boolean hasCalendar,
                                             boolean hasStock, boolean hasTasks) {
        PlacesFragment fragment = new PlacesFragment();
        Bundle args = new Bundle();
        args.putBoolean(THEME, isDark);
        args.putBoolean(CALENDAR, hasCalendar);
        args.putBoolean(STOCK, hasStock);
        args.putBoolean(TASKS, hasTasks);
        fragment.setItem(item);
        fragment.setArguments(args);
        return fragment;
    }

    public PlacesFragment() {
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
        View view = inflater.inflate(R.layout.reminder_places_layout, container, false);
        SharedPrefs prefs = SharedPrefs.getInstance(getActivity());
        placesMap = new PlacesMapFragment();
        placesMap.setListener(mListener);
        placesMap.setCallback(this);
        placesMap.setRadius(prefs.getInt(Prefs.LOCATION_RADIUS));
        placesMap.setMarkerStyle(prefs.getInt(Prefs.MARKER_STYLE));
        FragmentManager fragMan = getChildFragmentManager();
        FragmentTransaction fragTransaction = fragMan.beginTransaction();
        fragTransaction.replace(R.id.mapPlace, placesMap);
        fragTransaction.commitAllowingStateLoss();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        placesMap.showShowcase();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (mListener == null) {
            try {
                mListener = (MapListener) activity;
            } catch (ClassCastException e) {
                throw new ClassCastException("Activity must implement listeners.");
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (mListener == null) {
            try {
                mListener = (MapListener) context;
            } catch (ClassCastException e) {
                throw new ClassCastException("Activity must implement listeners.");
            }
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onMapReady() {
        if (item != null) {
            List<JPlace> list = item.getPlaces();
            placesMap.selectMarkers(list);
        }
    }
}
