package com.cray.software.justreminder.dialogs.utils;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.fragments.MapFragment;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.interfaces.MapListener;
import com.cray.software.justreminder.views.FloatingEditText;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.android.gms.maps.model.LatLng;

public class NewPlace extends AppCompatActivity implements MapListener {

    private ColorSetter cs = new ColorSetter(NewPlace.this);
    private FloatingEditText placeName;
    private SharedPrefs sPrefs = new SharedPrefs(NewPlace.this);
    private DataBase db = new DataBase(NewPlace.this);

    private LatLng place;
    private String placeTitle;

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
        toolbar.setTitle(getString(R.string.new_place_title));

        findViewById(R.id.windowBackground).setBackgroundColor(cs.getBackgroundStyle());

        placeName = (FloatingEditText) findViewById(R.id.placeName);
        MapFragment googleMap = (MapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        googleMap.enableTouch(true);
        googleMap.enableCloseButton(false);
        googleMap.enablePlaceList(false);
        googleMap.setListener(this);
        googleMap.moveToMyLocation();

        FloatingActionButton mFab = new FloatingActionButton(NewPlace.this);
        mFab.setColorNormal(cs.colorSetter());
        mFab.setColorPressed(cs.colorChooser());
        mFab.setSize(FloatingActionButton.SIZE_NORMAL);
        mFab.setIcon(R.drawable.ic_done_white_24dp);

        RelativeLayout wrapper = (RelativeLayout) findViewById(R.id.wrapper);
        wrapper.addView(mFab);

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mFab.getLayoutParams();
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);

        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addPlace();
            }
        });
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
            db.insertPlace(task, latitude, longitude);
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
            default:
                return super.onOptionsItemSelected(item);
        }
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
