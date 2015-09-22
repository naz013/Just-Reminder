package com.cray.software.justreminder.fragments;

import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.interfaces.MapListener;
import com.cray.software.justreminder.interfaces.Prefs;
import com.cray.software.justreminder.utils.ViewUtils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapFragment extends Fragment implements View.OnLongClickListener {
    private GoogleMap map;
    private LinearLayout layersContainer;
    private AutoCompleteTextView cardSearch;
    private Spinner placesList;
    private ImageButton zoomOut;
    private ArrayList<String> spinnerArray = new ArrayList<>();

    private boolean isAnimation = true;
    private boolean isTouch = true;
    private boolean isDark = false;
    private String taskTitle;
    private LatLng lastPos;

    private ColorSetter cSetter;

    private List<Address> foundPlaces;
    private ArrayAdapter<String> adapter;
    private GeocoderTask task;
    private ArrayList<String> namesList;

    private MapListener listener;

    public static MapFragment newInstance() {
        return new MapFragment();
    }

    public MapFragment() {
    }

    public void setListener(MapListener listener){
        this.listener = listener;
    }

    public void setTask(String taskTitle){
        this.taskTitle = taskTitle;
    }

    public void addMarker(LatLng pos, String title, boolean clear){
        if (map != null) {
            if (clear) map.clear();
            if (title == null || title.matches("")) title = pos.toString();
            lastPos = pos;
            if (listener != null) listener.place(pos);
            map.addMarker(new MarkerOptions()
                    .position(pos)
                    .title(title)
                    .icon(BitmapDescriptorFactory.fromResource(cSetter.getMarkerStyle()))
                    .draggable(clear));
        }
    }

    public void addMarker(LatLng pos, String title, boolean clear, int markerStyle){
        if (map != null) {
            if (clear) map.clear();
            if (title == null || title.matches("")) title = pos.toString();
            lastPos = pos;
            if (listener != null) listener.place(pos);
            map.addMarker(new MarkerOptions()
                    .position(pos)
                    .title(title)
                    .icon(BitmapDescriptorFactory.fromResource(markerStyle))
                    .draggable(clear));
        }
    }

    public void moveCamera(LatLng pos){
        if (map != null) map.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 13));
    }

    public void moveToMyLocation(){
        if (map != null && map.getMyLocation() != null){
            double lat = map.getMyLocation().getLatitude();
            double lon = map.getMyLocation().getLongitude();
            LatLng pos = new LatLng(lat, lon);
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 15));
        }
    }

    public void enableTouch(boolean isTouch){
        this.isTouch = isTouch;
    }

    public void enableCloseButton(boolean enable){
        if (enable) zoomOut.setVisibility(View.VISIBLE);
        else zoomOut.setVisibility(View.GONE);
    }

    public void clear(){
        if (map != null) map.clear();
    }

    public boolean onBackPressed(){
        if(isLayersVisible()) {
            ViewUtils.hideOver(layersContainer, isAnimation);
            return false;
        } else return true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_map, container, false);

        final SharedPrefs sPrefs = new SharedPrefs(getActivity());
        cSetter = new ColorSetter(getActivity());

        isAnimation = sPrefs.loadBoolean(Prefs.ANIMATIONS);

        map = ((SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map)).getMap();
        map.getUiSettings().setMyLocationButtonEnabled(false);
        String type = sPrefs.loadPrefs(Prefs.MAP_TYPE);
        if (type.matches(Constants.MAP_TYPE_NORMAL)){
            map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        } else if (type.matches(Constants.MAP_TYPE_SATELLITE)){
            map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        } else if (type.matches(Constants.MAP_TYPE_HYBRID)){
            map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        } else if (type.matches(Constants.MAP_TYPE_TERRAIN)){
            map.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        } else {
            map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }
        map.setMyLocationEnabled(true);

        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (isTouch) {
                    if (!spinnerArray.isEmpty()) {
                        placesList.setSelection(0);
                    }
                    if (isLayersVisible()) ViewUtils.hideOver(layersContainer, isAnimation);
                    addMarker(latLng, taskTitle, true);
                }
            }
        });

        if (lastPos != null) {
            addMarker(lastPos, lastPos.toString(), true);
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(lastPos, 13));
        }

        CardView card = (CardView) rootView.findViewById(R.id.card);
        CardView card1 = (CardView) rootView.findViewById(R.id.card1);
        CardView card2 = (CardView) rootView.findViewById(R.id.card2);
        card.setCardBackgroundColor(cSetter.getCardStyle());
        card1.setCardBackgroundColor(cSetter.getCardStyle());
        card2.setCardBackgroundColor(cSetter.getCardStyle());

        layersContainer = (LinearLayout) rootView.findViewById(R.id.layersContainer);
        ScrollView specsContainer = (ScrollView) rootView.findViewById(R.id.specsContainer);
        layersContainer.setVisibility(View.GONE);

        ImageButton cardClear = (ImageButton) rootView.findViewById(R.id.cardClear);
        zoomOut = (ImageButton) rootView.findViewById(R.id.zoomOut);
        ImageButton layers = (ImageButton) rootView.findViewById(R.id.layers);
        ImageButton myLocation = (ImageButton) rootView.findViewById(R.id.myLocation);

        cardClear.setOnLongClickListener(this);
        zoomOut.setOnLongClickListener(this);
        layers.setOnLongClickListener(this);
        myLocation.setOnLongClickListener(this);

        zoomOut.setBackgroundColor(cSetter.getBackgroundStyle());
        layers.setBackgroundColor(cSetter.getBackgroundStyle());
        myLocation.setBackgroundColor(cSetter.getBackgroundStyle());

        isDark = sPrefs.loadBoolean(Prefs.USE_DARK_THEME);

        if (isDark){
            cardClear.setImageResource(R.drawable.ic_clear_white_24dp);
            zoomOut.setImageResource(R.drawable.ic_fullscreen_exit_white_24dp);
            layers.setImageResource(R.drawable.ic_layers_white_24dp);
            myLocation.setImageResource(R.drawable.ic_my_location_white_24dp);
            layersContainer.setBackgroundResource(R.drawable.popup_dark);
        } else {
            cardClear.setImageResource(R.drawable.ic_clear_grey600_24dp);
            zoomOut.setImageResource(R.drawable.ic_fullscreen_exit_grey600_24dp);
            layers.setImageResource(R.drawable.ic_layers_grey600_24dp);
            myLocation.setImageResource(R.drawable.ic_my_location_grey600_24dp);
            layersContainer.setBackgroundResource(R.drawable.popup);
        }

        cardClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cardSearch.setText("");
            }
        });
        zoomOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) listener.onZoomOutClick();
            }
        });
        layers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isLayersVisible()) ViewUtils.hideOver(layersContainer, isAnimation);
                else ViewUtils.showOver(layersContainer, isAnimation);
            }
        });
        myLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isLayersVisible()) ViewUtils.hideOver(layersContainer, isAnimation);
                Location location = map.getMyLocation();
                if (location != null) {
                    double lat = location.getLatitude();
                    double lon = location.getLongitude();
                    LatLng pos = new LatLng(lat, lon);
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 15));
                }
            }
        });

        TextView typeNormal = (TextView) rootView.findViewById(R.id.typeNormal);
        TextView typeSatellite = (TextView) rootView.findViewById(R.id.typeSatellite);
        TextView typeHybrid = (TextView) rootView.findViewById(R.id.typeHybrid);
        TextView typeTerrain = (TextView) rootView.findViewById(R.id.typeTerrain);
        typeNormal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                sPrefs.savePrefs(Prefs.MAP_TYPE, Constants.MAP_TYPE_NORMAL);
                ViewUtils.hideOver(layersContainer, isAnimation);
            }
        });
        typeSatellite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                sPrefs.savePrefs(Prefs.MAP_TYPE, Constants.MAP_TYPE_SATELLITE);
                ViewUtils.hideOver(layersContainer, isAnimation);
            }
        });
        typeHybrid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                sPrefs.savePrefs(Prefs.MAP_TYPE, Constants.MAP_TYPE_HYBRID);
                ViewUtils.hideOver(layersContainer, isAnimation);
            }
        });
        typeTerrain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                map.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                sPrefs.savePrefs(Prefs.MAP_TYPE, Constants.MAP_TYPE_TERRAIN);
                ViewUtils.hideOver(layersContainer, isAnimation);
            }
        });

        cardSearch = (AutoCompleteTextView) rootView.findViewById(R.id.cardSearch);
        if (isDark) cardSearch.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_search_white_24dp, 0, 0, 0);
        else cardSearch.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_search_grey600_24dp, 0, 0, 0);
        cardSearch.setThreshold(3);
        adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_dropdown_item_1line, namesList);
        adapter.setNotifyOnChange(true);
        cardSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (isLayersVisible()) ViewUtils.hideOver(layersContainer, isAnimation);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (task != null && !task.isCancelled()) task.cancel(true);
                task = new GeocoderTask();
                task.execute(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        cardSearch.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Address sel = foundPlaces.get(position);
                double lat = sel.getLatitude();
                double lon = sel.getLongitude();
                LatLng pos = new LatLng(lat, lon);
                addMarker(pos, taskTitle, true);
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 15));
            }
        });

        placesList = (Spinner) rootView.findViewById(R.id.placesList);
        placesList.setBackgroundColor(cSetter.getSpinnerStyle());
        if (spinnerArray.isEmpty()){
            placesList.setVisibility(View.GONE);
        } else {
            placesList.setVisibility(View.VISIBLE);
            ArrayAdapter<String> spinnerArrayAdapter =
                    new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, spinnerArray);
            spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            placesList.setAdapter(spinnerArrayAdapter);
            placesList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                    if (isLayersVisible()) ViewUtils.hideOver(layersContainer, isAnimation);
                    if (position > 0){
                        String placeName = spinnerArray.get(position);
                        DataBase DB = new DataBase(getActivity());
                        DB.open();
                        Cursor c = DB.getPlace(placeName);
                        if (c != null && c.moveToFirst()) {
                            double latitude = c.getDouble(c.getColumnIndex(Constants.LocationConstants.COLUMN_LOCATION_LATITUDE));
                            double longitude = c.getDouble(c.getColumnIndex(Constants.LocationConstants.COLUMN_LOCATION_LONGITUDE));
                            LatLng latLng = new LatLng(latitude, longitude);
                            addMarker(latLng, taskTitle, true);
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                        }
                        if (c != null) c.close();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                    if (isLayersVisible()) ViewUtils.hideOver(layersContainer, isAnimation);
                }
            });
        }
        return rootView;
    }

    private void showMessage(String message){
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    private void loadPlaces(){
        DataBase DB = new DataBase(getActivity());
        DB.open();
        Cursor c = DB.queryPlaces();
        spinnerArray = new ArrayList<>();
        spinnerArray.clear();
        spinnerArray.add(getString(R.string.other_settings));
        if (c != null && c.moveToFirst()){
            do {
                String namePlace = c.getString(c.getColumnIndex(Constants.LocationConstants.COLUMN_LOCATION_NAME));
                spinnerArray.add(namePlace);

            } while (c.moveToNext());
        } else spinnerArray.clear();
        if (c != null) c.close();
    }

    private boolean isLayersVisible(){
        return layersContainer != null && layersContainer.getVisibility() == View.VISIBLE;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public boolean onLongClick(View v) {
        switch (v.getId()){
            case R.id.cardClear:
                showMessage(getActivity().getString(R.string.clear_search_field));
                return true;
            case R.id.zoomOut:
                showMessage(getActivity().getString(R.string.close_map));
                return true;
            case R.id.layers:
                showMessage(getActivity().getString(R.string.change_map_layer));
                return true;
            case R.id.myLocation:
                showMessage(getActivity().getString(R.string.show_my_location));
                return true;
            default:
                return false;
        }
    }

    private class GeocoderTask extends AsyncTask<String, Void, List<Address>> {

        @Override
        protected List<Address> doInBackground(String... locationName) {
            // Creating an instance of Geocoder class
            Geocoder geocoder = new Geocoder(getActivity());
            List<Address> addresses = null;

            try {
                // Getting a maximum of 3 Address that matches the input text
                addresses = geocoder.getFromLocationName(locationName[0], 3);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return addresses;
        }

        @Override
        protected void onPostExecute(List<Address> addresses) {
            if(addresses == null || addresses.size() == 0){
                Log.d(Constants.LOG_TAG, "No Location found");
            } else {
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
                cardSearch.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }
        }
    }
}
