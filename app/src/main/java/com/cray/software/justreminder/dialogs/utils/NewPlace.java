package com.cray.software.justreminder.dialogs.utils;

import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.fragments.MapFragment;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.interfaces.MapListener;
import com.cray.software.justreminder.interfaces.Prefs;
import com.google.android.gms.maps.model.LatLng;

public class NewPlace extends AppCompatActivity implements MapListener {

    private ColorSetter cs = new ColorSetter(NewPlace.this);
    private EditText placeName;
    private SharedPrefs sPrefs = new SharedPrefs(NewPlace.this);
    private DataBase db = new DataBase(NewPlace.this);

    private LatLng place;
    private String placeTitle;
    private long id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(cs.getStyle());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(cs.colorStatus());
        }
        setContentView(R.layout.new_place_activity_layout);
        setRequestedOrientation(cs.getRequestOrientation());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationIcon(R.drawable.ic_clear_white_24dp);
        //toolbar.setTitle(getString(R.string.new_place_title));

        findViewById(R.id.windowBackground).setBackgroundColor(cs.getBackgroundStyle());

        id = getIntent().getLongExtra(Constants.ITEM_ID_INTENT, 0);

        placeName = (EditText) findViewById(R.id.placeName);
        MapFragment googleMap = (MapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        googleMap.enableTouch(true);
        googleMap.enableCloseButton(false);
        googleMap.enablePlaceList(false);
        googleMap.setListener(this);
        googleMap.moveToMyLocation();

        if (id != 0){
            int radius = sPrefs.loadInt(Prefs.LOCATION_RADIUS);
            db.open();
            Cursor c = db.getPlace(id);
            if (c != null && c.moveToFirst()){
                String text = c.getString(c.getColumnIndex(Constants.LocationConstants.COLUMN_LOCATION_NAME));
                double latitude = c.getDouble(c.getColumnIndex(Constants.LocationConstants.COLUMN_LOCATION_LATITUDE));
                double longitude = c.getDouble(c.getColumnIndex(Constants.LocationConstants.COLUMN_LOCATION_LONGITUDE));
                googleMap.addMarker(new LatLng(latitude, longitude), text, true, true, radius);
                placeName.setText(text);
            }
        }
    }

    private void addPlace(){
        if (place != null){
            String task = placeName.getText().toString().trim();
            if (task.matches("")){
                task = placeTitle;
            }
            if (task == null || task.matches("")) {
                placeName.setError(getString(R.string.empty_field_error));
                return;
            }
            Double latitude = place.latitude;
            Double longitude = place.longitude;
            db.open();
            if (id != 0){
                db.updatePlace(id, task, latitude, longitude);
            } else {
                db.insertPlace(task, latitude, longitude);
            }
            db.close();
            finish();
        } else {
            Toast.makeText(NewPlace.this, getString(R.string.point_warning), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_add:
                addPlace();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.save_menu, menu);
        return true;
    }

    @Override
    public void place(LatLng place) {
        this.place = place;
    }

    @Override
    public void onZoomOutClick() {

    }

    @Override
    public void placeName(String name) {
        this.placeTitle = name;
    }
}
