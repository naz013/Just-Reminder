package com.cray.software.justreminder.activities;

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
import com.cray.software.justreminder.fragments.helpers.MapFragment;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.interfaces.MapListener;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.utils.ViewUtils;
import com.google.android.gms.maps.model.LatLng;

public class NewPlace extends AppCompatActivity implements MapListener {

    private ColorSetter cs = new ColorSetter(NewPlace.this);
    private EditText placeName;
    private SharedPrefs sPrefs = new SharedPrefs(NewPlace.this);

    private LatLng place;
    private String placeTitle;
    private long id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(cs.getStyle());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ViewUtils.getColor(this, cs.colorPrimaryDark()));
        }
        setContentView(R.layout.new_place_activity_layout);
        setRequestedOrientation(cs.getRequestOrientation());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationIcon(R.drawable.ic_clear_white_24dp);

        findViewById(R.id.windowBackground).setBackgroundColor(cs.getBackgroundStyle());

        id = getIntent().getLongExtra(Constants.ITEM_ID_INTENT, 0);

        placeName = (EditText) findViewById(R.id.placeName);
        MapFragment googleMap = MapFragment.newInstance(false, false, false, false,
                sPrefs.loadInt(Prefs.MARKER_STYLE), sPrefs.loadBoolean(Prefs.USE_DARK_THEME));
        googleMap.setListener(this);
        int radius = sPrefs.loadInt(Prefs.LOCATION_RADIUS);
        googleMap.setMarkerRadius(radius);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, googleMap)
                .addToBackStack(null)
                .commit();

        if (id != 0){
            DataBase db = new DataBase(NewPlace.this);
            db.open();
            Cursor c = db.getPlace(id);
            if (c != null && c.moveToFirst()){
                String text = c.getString(c.getColumnIndex(Constants.LocationConstants.COLUMN_LOCATION_NAME));
                double latitude = c.getDouble(c.getColumnIndex(Constants.LocationConstants.COLUMN_LOCATION_LATITUDE));
                double longitude = c.getDouble(c.getColumnIndex(Constants.LocationConstants.COLUMN_LOCATION_LONGITUDE));
                googleMap.addMarker(new LatLng(latitude, longitude), text, true, true, radius);
                placeName.setText(text);
            }
            if (c != null) c.close();
            db.close();
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

            DataBase db = new DataBase(NewPlace.this);
            db.open();
            if (id != 0){
                db.updatePlace(id, task, latitude, longitude);
            } else {
                db.insertPlace(task, latitude, longitude);
            }
            db.close();
            new SharedPrefs(this).saveBoolean(Prefs.PLACE_CHANGED, true);
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
    public void placeChanged(LatLng place) {
        this.place = place;
    }

    @Override
    public void onBackClick() {

    }

    @Override
    public void onZoomClick(boolean isFull) {

    }

    @Override
    public void placeName(String name) {
        this.placeTitle = name;
    }
}
