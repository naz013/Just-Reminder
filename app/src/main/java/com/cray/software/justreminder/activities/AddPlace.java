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

package com.cray.software.justreminder.activities;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.fragments.helpers.MapFragment;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.interfaces.MapListener;
import com.cray.software.justreminder.modules.Module;
import com.cray.software.justreminder.roboto_views.RoboEditText;
import com.cray.software.justreminder.utils.LocationUtil;
import com.google.android.gms.maps.model.LatLng;

public class AddPlace extends AppCompatActivity implements MapListener {

    private ColorSetter cs = new ColorSetter(AddPlace.this);
    private RoboEditText placeName;
    private MapFragment googleMap;

    private LatLng place;
    private String placeTitle;
    private long id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(cs.getStyle());
        if (Module.isLollipop()) {
            getWindow().setStatusBarColor(cs.getColor(cs.colorPrimaryDark()));
        }
        setContentView(R.layout.new_place_activity_layout);
        setRequestedOrientation(cs.getRequestOrientation());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);

        findViewById(R.id.windowBackground).setBackgroundColor(cs.getBackgroundStyle());

        id = getIntent().getLongExtra(Constants.ITEM_ID_INTENT, 0);

        placeName = (RoboEditText) findViewById(R.id.placeName);
        googleMap = MapFragment.newInstance(false, false, false, false,
                SharedPrefs.getInstance(this).getInt(Prefs.MARKER_STYLE), cs.isDark());
        googleMap.setListener(this);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, googleMap)
                .addToBackStack(null)
                .commit();
    }

    private void loadPlace() {
        if (id != 0){
            DataBase db = new DataBase(AddPlace.this);
            db.open();
            Cursor c = db.getPlace(id);
            if (c != null && c.moveToFirst()){
                String text = c.getString(c.getColumnIndex(Constants.LocationConstants.COLUMN_LOCATION_NAME));
                double latitude = c.getDouble(c.getColumnIndex(Constants.LocationConstants.COLUMN_LOCATION_LATITUDE));
                double longitude = c.getDouble(c.getColumnIndex(Constants.LocationConstants.COLUMN_LOCATION_LONGITUDE));
                googleMap.addMarker(new LatLng(latitude, longitude), text, true, true, -1);
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
                placeName.setError(getString(R.string.must_be_not_empty));
                return;
            }
            Double latitude = place.latitude;
            Double longitude = place.longitude;

            DataBase db = new DataBase(AddPlace.this);
            db.open();
            if (id != 0){
                db.updatePlace(id, task, latitude, longitude);
            } else {
                db.insertPlace(task, latitude, longitude);
            }
            db.close();
            SharedPrefs.getInstance(this).putBoolean(Prefs.PLACE_CHANGED, true);
            finish();
        } else {
            Toast.makeText(AddPlace.this, getString(R.string.you_dont_select_place), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(Constants.LOG_TAG, "Map is ready");
        loadPlace();
    }

    @Override
    public void onBackPressed() {
        finish();
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
        placeTitle = LocationUtil.getAddress(place.latitude, place.longitude);
    }

    @Override
    public void onBackClick() {

    }

    @Override
    public void onZoomClick(boolean isFull) {

    }
}
