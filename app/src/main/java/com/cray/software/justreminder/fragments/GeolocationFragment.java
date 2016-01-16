package com.cray.software.justreminder.fragments;

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
import com.cray.software.justreminder.ScreenManager;
import com.cray.software.justreminder.adapters.MarkersCursorAdapter;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.databases.NextBase;
import com.cray.software.justreminder.datas.models.MarkerModel;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.interfaces.NavigationCallbacks;
import com.cray.software.justreminder.json.JsonParser;
import com.cray.software.justreminder.json.JsonPlace;
import com.cray.software.justreminder.utils.ViewUtils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Random;

public class GeolocationFragment extends Fragment {

    private ListView geoTasks;
    private GoogleMap googleMap;
    private MarkersCursorAdapter markersCursorAdapter;

    private boolean onCreate = false;

    private NavigationCallbacks mCallbacks;

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

        googleMap = ((SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.markersMap)).getMap();
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);

        geoTasks = (ListView) rootView.findViewById(R.id.geoTasks);
        geoTasks.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                MarkerModel item = (MarkerModel) markersCursorAdapter.getItem(position);
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(item.getPosition(), 13));
            }
        });

        loadMarkers();
        onCreate = true;
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallbacks = (NavigationCallbacks) activity;
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
        if (!onCreate) {
            loadMarkers();
        }
        onCreate = false;
    }

    private void loadMarkers(){
        googleMap.clear();
        NextBase db = new NextBase(getActivity());
        if (!db.isOpen()) db.open();
        Cursor c = db.queryAllLocations();
        Random random = new Random();
        ArrayList<MarkerModel> list = new ArrayList<>();
        if (c != null && c.moveToFirst()){
            ColorSetter cSetter = new ColorSetter(getActivity());
            do {
                String task = c.getString(c.getColumnIndex(NextBase.SUMMARY));
                String type = c.getString(c.getColumnIndex(NextBase.TYPE));
                String json = c.getString(c.getColumnIndex(NextBase.JSON));
                long id = c.getLong(c.getColumnIndex(NextBase._ID));

                JsonPlace jsonPlace = new JsonParser(json).getPlace();

                double latitude = jsonPlace.getLatitude();
                double longitude = jsonPlace.getLongitude();
                int radius = jsonPlace.getRadius();
                if (radius == -1) {
                    radius = new SharedPrefs(getActivity()).loadInt(Prefs.LOCATION_RADIUS);
                }

                if (type.contains(Constants.TYPE_LOCATION)) {
                    int rand = random.nextInt(16 - 2) + 1;
                    LatLng pos = new LatLng(latitude, longitude);
                    list.add(new MarkerModel(task, pos, rand, id));
                    googleMap.addMarker(new MarkerOptions()
                            .position(pos)
                            .title(task)
                            .icon(BitmapDescriptorFactory.fromResource(cSetter.getMarkerStyle(rand)))
                            .draggable(false));
                    if (radius != -1) {
                        int[] circleColors = cSetter.getMarkerRadiusStyle(rand);
                        googleMap.addCircle(new CircleOptions()
                                .center(pos)
                                .radius(radius)
                                .strokeWidth(3f)
                                .fillColor(ViewUtils.getColor(getActivity(), circleColors[0]))
                                .strokeColor(ViewUtils.getColor(getActivity(), circleColors[1])));
                    }
                }
            } while (c.moveToNext());
        }
        loaderAdapter(list);
        if (c != null) c.close();
        db.close();
    }

    public void loaderAdapter(ArrayList<MarkerModel> list){
        if (list.size() > 0) {
            markersCursorAdapter = new MarkersCursorAdapter(getActivity(), list);
            geoTasks.setAdapter(markersCursorAdapter);
            geoTasks.setVisibility(View.VISIBLE);
        } else {
            geoTasks.setVisibility(View.GONE);
        }
    }
}
