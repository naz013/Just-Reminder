package com.cray.software.justreminder;

import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.cray.software.justreminder.adapters.MarkersCursorAdapter;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.interfaces.Constants;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class GeolocationTasks extends AppCompatActivity {
    private MapFragment googleMap;

    DataBase DB = new DataBase(GeolocationTasks.this);
    ColorSetter cSetter = new ColorSetter(GeolocationTasks.this);
    SharedPrefs sPrefs;
    ListView geoTasks;
    private MarkersCursorAdapter markersCursorAdapter;

    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cSetter = new ColorSetter(GeolocationTasks.this);
        setTheme(cSetter.getStyle());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(cSetter.colorStatus());
        }
        setContentView(R.layout.geo_tasks_map_layout);
        setRequestedOrientation(cSetter.getRequestOrientation());

        findViewById(R.id.windowBackground).setBackgroundColor(cSetter.getBackgroundStyle());

        geoTasks = (ListView) findViewById(R.id.geoTasks);
        if (!DB.isOpen())DB.open();
        if (DB.getCount() == 0){
            geoTasks.setVisibility(View.GONE);
        }
        if (DB.getCount() != 0){
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
        geoTasks.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                long i = markersCursorAdapter.getItemId(position);
                if (i != 0){
                    Cursor c = DB.getTask(i);
                    if (c != null && c.moveToFirst()){
                        googleMap.getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(
                                new LatLng(c.getDouble(c.getColumnIndex(Constants.COLUMN_LATITUDE)),
                                        c.getDouble(c.getColumnIndex(Constants.COLUMN_LONGITUDE))), 13));
                    }
                    if (c != null) c.close();
                }
            }
        });
        loaderAdapter();

        googleMap = (MapFragment) getFragmentManager().findFragmentById(R.id.markersMap);
        sPrefs = new SharedPrefs(GeolocationTasks.this);
        String type = sPrefs.loadPrefs(Constants.APP_UI_PREFERENCES_MAP_TYPE);
        if (type.matches(Constants.MAP_TYPE_NORMAL)){
            googleMap.getMap().setMapType(GoogleMap.MAP_TYPE_NORMAL);
        } else if (type.matches(Constants.MAP_TYPE_SATELLITE)){
            googleMap.getMap().setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        } else if (type.matches(Constants.MAP_TYPE_HYBRID)){
            googleMap.getMap().setMapType(GoogleMap.MAP_TYPE_HYBRID);
        } else if (type.matches(Constants.MAP_TYPE_TERRAIN)){
            googleMap.getMap().setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        } else {
            googleMap.getMap().setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }
        googleMap.getMap().setMyLocationEnabled(true);
        loadMarkers();

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setTitle(getString(R.string.geo_fragment));
    }

    private void loadMarkers(){
        if (!DB.isOpen()) DB.open();
        Cursor c = DB.queryGroup();
        if (c != null && c.moveToFirst()){
            do {
                String task = c.getString(c.getColumnIndex(Constants.COLUMN_TEXT));
                double latitude = c.getDouble(c.getColumnIndex(Constants.COLUMN_LATITUDE));
                double longitude = c.getDouble(c.getColumnIndex(Constants.COLUMN_LONGITUDE));
                if (longitude != 0 && latitude != 0) {
                    googleMap.getMap().addMarker(new MarkerOptions()
                            .position(new LatLng(latitude, longitude))
                            .title(task)
                            .icon(BitmapDescriptorFactory.fromResource(cSetter.getMarkerStyle())));
                }
            } while (c.moveToNext());
            if (googleMap.getMap().getMyLocation() != null) {
                double lat = googleMap.getMap().getMyLocation().getLatitude();
                double lon = googleMap.getMap().getMyLocation().getLongitude();
                LatLng pos = new LatLng(lat, lon);
                googleMap.getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 15));
            }
        } else {
            if (googleMap.getMap().getMyLocation() != null) {
                double lat = googleMap.getMap().getMyLocation().getLatitude();
                double lon = googleMap.getMap().getMyLocation().getLongitude();
                LatLng pos = new LatLng(lat, lon);
                googleMap.getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 15));
            }
        }
        if (c != null) c.close();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void loaderAdapter(){
        DB = new DataBase(GeolocationTasks.this);
        if (!DB.isOpen()) DB.open();
        markersCursorAdapter = new MarkersCursorAdapter(GeolocationTasks.this, DB.getMarkers(Constants.TYPE_LOCATION,
                Constants.TYPE_LOCATION_CALL, Constants.TYPE_LOCATION_MESSAGE));
        geoTasks.setAdapter(markersCursorAdapter);
    }

    @Override
    protected void onDestroy() {
        if (DB != null && DB.isOpen()) DB.close();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMarkers();
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
