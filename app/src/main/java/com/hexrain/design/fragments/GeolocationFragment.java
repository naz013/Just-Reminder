package com.hexrain.design.fragments;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.adapters.MarkersCursorAdapter;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.datas.MarkerItem;
import com.cray.software.justreminder.fragments.MapFragment;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.interfaces.Constants;
import com.google.android.gms.maps.model.LatLng;
import com.hexrain.design.NavigationDrawerFragment;
import com.hexrain.design.ScreenManager;

import java.util.ArrayList;
import java.util.Random;

public class GeolocationFragment extends Fragment {

    private ListView geoTasks;
    private MapFragment googleMap;
    private MarkersCursorAdapter markersCursorAdapter;

    private NavigationDrawerFragment.NavigationDrawerCallbacks mCallbacks;

    public static GeolocationFragment newInstance() {
        return new GeolocationFragment();
    }

    public GeolocationFragment() {
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
        View rootView = inflater.inflate(R.layout.fragment_geolocation_layout, container, false);

        SharedPrefs sPrefs = new SharedPrefs(getActivity());

        googleMap = ((MapFragment) getChildFragmentManager()
                .findFragmentById(R.id.markersMap));
        googleMap.enableTouch(false);
        googleMap.enableCloseButton(false);

        geoTasks = (ListView) rootView.findViewById(R.id.geoTasks);
        geoTasks.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                long i = markersCursorAdapter.getItemId(position);
                if (i != 0) {
                    DataBase DB = new DataBase(getActivity());
                    if (!DB.isOpen()) DB.open();
                    Cursor c = DB.getReminder(i);
                    if (c != null && c.moveToFirst()) {
                        if (googleMap != null) {
                            googleMap.moveCamera(new LatLng(c.getDouble(c.getColumnIndex(Constants.COLUMN_LATITUDE)),
                                    c.getDouble(c.getColumnIndex(Constants.COLUMN_LONGITUDE))));
                        }
                    }
                    if (c != null) c.close();
                    DB.close();
                }
            }
        });
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallbacks = (NavigationDrawerFragment.NavigationDrawerCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
        }
        ((ScreenManager)activity).onSectionAttached(ScreenManager.FRAGMENT_LOCATIONS);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadMarkers();
    }

    private void loadMarkers(){
        if (googleMap != null) googleMap.clear();
        DataBase DB = new DataBase(getActivity());
        if (!DB.isOpen()) DB.open();
        Cursor c = DB.queryGroup();
        Random random = new Random();
        if (c != null && c.moveToFirst()){
            ColorSetter cSetter = new ColorSetter(getActivity());
            ArrayList<MarkerItem> list = new ArrayList<>();
            do {
                String task = c.getString(c.getColumnIndex(Constants.COLUMN_TEXT));
                double latitude = c.getDouble(c.getColumnIndex(Constants.COLUMN_LATITUDE));
                double longitude = c.getDouble(c.getColumnIndex(Constants.COLUMN_LONGITUDE));
                int isDone = c.getInt(c.getColumnIndex(Constants.COLUMN_IS_DONE));
                if (longitude != 0 && latitude != 0) {
                    int rand = random.nextInt(16-2)+1;
                    int marker = cSetter.getMarkerStyle(rand);
                    list.add(new MarkerItem(task, new LatLng(latitude, longitude), marker));
                    if (googleMap != null) {
                        googleMap.addMarker(new LatLng(latitude, longitude), task, false, marker);
                    }
                }
            } while (c.moveToNext());
            loaderAdapter(list);
        } else {
            if (googleMap != null) {
                googleMap.moveToMyLocation();
            }
        }
        if (c != null) c.close();
        DB.close();
    }

    public void loaderAdapter(ArrayList<MarkerItem> list){
        if (list.size() > 0) {
            markersCursorAdapter = new MarkersCursorAdapter(getActivity(), list);
            geoTasks.setAdapter(markersCursorAdapter);
            geoTasks.setVisibility(View.VISIBLE);
        } else {
            geoTasks.setVisibility(View.GONE);
        }
    }
}
