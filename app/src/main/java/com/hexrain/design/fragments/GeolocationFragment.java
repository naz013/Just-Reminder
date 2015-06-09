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
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.interfaces.Constants;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.hexrain.design.NavigationDrawerFragment;
import com.hexrain.design.ScreenManager;

import java.util.ArrayList;

public class GeolocationFragment extends Fragment {

    ListView geoTasks;
    private static GoogleMap googleMap;
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

        geoTasks = (ListView) rootView.findViewById(R.id.geoTasks);
        DataBase DB = new DataBase(getActivity());
        if (!DB.isOpen())DB.open();
        if (DB.getCount() == 0){
            geoTasks.setVisibility(View.GONE);
        } else {
            Cursor c = DB.queryGroup();
            if (c != null && c.moveToFirst()){
                ArrayList<String> types = new ArrayList<>();
                do{
                    String tp = c.getString(c.getColumnIndex(Constants.COLUMN_TYPE));
                    types.add(tp);
                } while (c.moveToNext());
                if (types.contains(Constants.TYPE_LOCATION) ||
                        types.contains(Constants.TYPE_LOCATION_CALL) ||
                        types.contains(Constants.TYPE_LOCATION_MESSAGE)){
                    geoTasks.setVisibility(View.VISIBLE);
                } else {
                    geoTasks.setVisibility(View.GONE);
                }
            }
            if (c != null) c.close();
        }
        DB.close();
        geoTasks.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                long i = markersCursorAdapter.getItemId(position);
                if (i != 0) {
                    DataBase DB = new DataBase(getActivity());
                    if (!DB.isOpen())DB.open();
                    Cursor c = DB.getTask(i);
                    if (c != null && c.moveToFirst()) {
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                new LatLng(c.getDouble(c.getColumnIndex(Constants.COLUMN_LATITUDE)),
                                        c.getDouble(c.getColumnIndex(Constants.COLUMN_LONGITUDE))), 13));
                    }
                    if (c != null) c.close();
                    DB.close();
                }
            }
        });

        googleMap = ((SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.markersMap)).getMap();
        String type = sPrefs.loadPrefs(Constants.APP_UI_PREFERENCES_MAP_TYPE);
        if (type.matches(Constants.MAP_TYPE_NORMAL)){
            googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        } else if (type.matches(Constants.MAP_TYPE_SATELLITE)){
            googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        } else if (type.matches(Constants.MAP_TYPE_HYBRID)){
            googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        } else if (type.matches(Constants.MAP_TYPE_TERRAIN)){
            googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        } else {
            googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }
        googleMap.setMyLocationEnabled(true);
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
        loaderAdapter();
        loadMarkers();
    }

    private void loadMarkers(){
        DataBase DB = new DataBase(getActivity());
        if (!DB.isOpen()) DB.open();
        Cursor c = DB.queryGroup();
        if (c != null && c.moveToFirst()){
            ColorSetter cSetter = new ColorSetter(getActivity());
            do {
                String task = c.getString(c.getColumnIndex(Constants.COLUMN_TEXT));
                double latitude = c.getDouble(c.getColumnIndex(Constants.COLUMN_LATITUDE));
                double longitude = c.getDouble(c.getColumnIndex(Constants.COLUMN_LONGITUDE));
                if (longitude != 0 && latitude != 0) {
                    googleMap.addMarker(new MarkerOptions()
                            .position(new LatLng(latitude, longitude))
                            .title(task)
                            .icon(BitmapDescriptorFactory.fromResource(cSetter.getMarkerStyle())));
                }
            } while (c.moveToNext());
            if (googleMap.getMyLocation() != null) {
                double lat = googleMap.getMyLocation().getLatitude();
                double lon = googleMap.getMyLocation().getLongitude();
                LatLng pos = new LatLng(lat, lon);
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 15));
            }
        } else {
            if (googleMap.getMyLocation() != null) {
                double lat = googleMap.getMyLocation().getLatitude();
                double lon = googleMap.getMyLocation().getLongitude();
                LatLng pos = new LatLng(lat, lon);
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 15));
            }
        }
        if (c != null) c.close();
        DB.close();
    }

    public void loaderAdapter(){
        DataBase DB = new DataBase(getActivity());
        if (!DB.isOpen()) DB.open();
        markersCursorAdapter = new MarkersCursorAdapter(getActivity(), DB.getMarkers(Constants.TYPE_LOCATION,
                Constants.TYPE_LOCATION_CALL, Constants.TYPE_LOCATION_MESSAGE));
        geoTasks.setAdapter(markersCursorAdapter);
        DB.close();
    }
}
