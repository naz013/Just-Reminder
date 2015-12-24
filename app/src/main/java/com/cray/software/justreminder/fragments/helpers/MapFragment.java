package com.cray.software.justreminder.fragments.helpers;

import android.content.Context;
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
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.interfaces.MapListener;
import com.cray.software.justreminder.utils.ViewUtils;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapFragment extends Fragment implements View.OnLongClickListener {

    /**
     * UI elements;
     */
    private GoogleMap map;
    private LinearLayout layersContainer;
    private AutoCompleteTextView cardSearch;
    private Spinner placesList;

    /**
     * Array of user frequently used places;
     */
    private ArrayList<String> spinnerArray = new ArrayList<>();

    /**
     * init variables and flags;
     */
    private boolean isTouch = true;
    private boolean isClose = true;
    private boolean isAll = true;
    private boolean isList = true;
    private boolean isSearch = true;
    private String markerTitle;
    private int markerRadius = -1;
    private LatLng lastPos;
    private float strokeWidth = 3f;

    /**
     * UI helper class;
     */
    private ColorSetter cSetter;

    /**
     * Arrays of place search results;
     */
    private List<Address> foundPlaces;
    private ArrayAdapter<String> adapter;
    private GeocoderTask task;
    private ArrayList<String> namesList;

    /**
     * MapListener link;
     */
    private MapListener listener;

    public static final String ENABLE_TOUCH = "enable_touch";
    public static final String ENABLE_CLOSE = "enable_close";
    public static final String ENABLE_ALL_UI = "enable_all_ui";
    public static final String ENABLE_SEARCH = "enable_search";
    public static final String ENABLE_LIST = "enable_list";

    public static MapFragment newInstance() {
        return new MapFragment();
    }

    public MapFragment() {
    }

    /**
     * Set listener for map fragment;
     * @param listener listener for map fragment
     */
    public void setListener(MapListener listener){
        this.listener = listener;
    }

    /**
     * Set title for markers;
     * @param markerTitle marker title
     */
    public void setMarkerTitle(String markerTitle){
        this.markerTitle = markerTitle;
    }

    /**
     * Set radius for marker;
     * @param markerRadius radius for drawing circle around marker
     */
    public void setMarkerRadius(int markerRadius){
        this.markerRadius = markerRadius;
    }

    /**
     * Add marker to map;
     * @param pos coordinates
     * @param title marker title
     * @param clear remove previous markers flag
     * @param animate animate to marker position
     * @param radius radius for circle around marker
     */
    public void addMarker(LatLng pos, String title, boolean clear, boolean animate, int radius){
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
            if (radius != -1) {
                int[] circleColors = cSetter.getMarkerRadiusStyle();
                map.addCircle(new CircleOptions()
                        .center(pos)
                        .radius(radius)
                        .strokeWidth(strokeWidth)
                        .fillColor(ViewUtils.getColor(getActivity(), circleColors[0]))
                        .strokeColor(ViewUtils.getColor(getActivity(), circleColors[1])));
            }
            if (animate) animate(pos);
        }
    }

    /**
     * Add marker to map with custom marker icon;
     * @param pos coordinates
     * @param title marker title
     * @param clear remove previous markers flag
     * @param markerStyle marker icon
     * @param animate animate to marker position
     * @param radius radius for circle around marker
     */
    public void addMarker(LatLng pos, String title, boolean clear, int markerStyle, boolean animate, int radius){
        if (map != null) {
            Log.d(Constants.LOG_TAG, "map not null");
            if (clear) map.clear();
            if (title == null || title.matches("")) title = pos.toString();
            lastPos = pos;
            if (listener != null) listener.place(pos);
            map.addMarker(new MarkerOptions()
                    .position(pos)
                    .title(title)
                    .icon(BitmapDescriptorFactory.fromResource(cSetter.getMarkerStyle(markerStyle)))
                    .draggable(clear));
            if (radius != -1) {
                int[] circleColors = cSetter.getMarkerRadiusStyle(markerStyle);
                map.addCircle(new CircleOptions()
                        .center(pos)
                        .radius(radius)
                        .strokeWidth(strokeWidth)
                        .fillColor(ViewUtils.getColor(getActivity(), circleColors[0]))
                        .strokeColor(ViewUtils.getColor(getActivity(), circleColors[1])));
            }
            if (animate) animate(pos);
        } else Log.d(Constants.LOG_TAG, "map is null");
    }

    /**
     * Recreate last added marker with new circle radius;
     * @param radius radius for a circle
     */
    public void recreateMarker(int radius){
        markerRadius = radius;
        if (map != null && lastPos != null) {
            map.clear();
            if (markerTitle == null || markerTitle.matches("")) markerTitle = lastPos.toString();
            if (listener != null) listener.place(lastPos);
            map.addMarker(new MarkerOptions()
                    .position(lastPos)
                    .title(markerTitle)
                    .icon(BitmapDescriptorFactory.fromResource(cSetter.getMarkerStyle()))
                    .draggable(true));
            if (radius != -1) {
                int[] circleColors = cSetter.getMarkerRadiusStyle();
                map.addCircle(new CircleOptions()
                        .center(lastPos)
                        .radius(radius)
                        .strokeWidth(strokeWidth)
                        .fillColor(ViewUtils.getColor(getActivity(), circleColors[0]))
                        .strokeColor(ViewUtils.getColor(getActivity(), circleColors[1])));
            }
            animate(lastPos);
        }
    }

    /**
     * Move camera to coordinates;
     * @param pos coordinates
     */
    public void moveCamera(LatLng pos){
        if (map != null) map.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 13));
    }

    /**
     * Move camera to coordinates with animation;
     * @param latLng coordinates
     */
    public void animate(LatLng latLng){
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latLng, 13);
        if (map != null) map.animateCamera(update);
    }

    /**
     * Move camera to user current coordinates with animation;
     */
    public void moveToMyLocation(){
        if (map != null && map.getMyLocation() != null){
            double lat = map.getMyLocation().getLatitude();
            double lon = map.getMyLocation().getLongitude();
            LatLng pos = new LatLng(lat, lon);
            animate(pos);
        }
    }

    /**
     * Move camera to user current coordinates with animation;
     * @param animate animation flag
     */
    public void moveToMyLocation(boolean animate){
        if (map != null && map.getMyLocation() != null){
            double lat = map.getMyLocation().getLatitude();
            double lon = map.getMyLocation().getLongitude();
            LatLng pos = new LatLng(lat, lon);
            if (animate) animate(pos);
        }
    }

    /**
     * Enable/Disable on map click listener;
     * @param isTouch flag
     */
    public void enableTouch(boolean isTouch){
        this.isTouch = isTouch;
    }

    /**
     * Enable/Disable close map button;
     * @param isClose flag
     */
    public void enableCloseButton(boolean isClose){
        this.isClose = isClose;
    }

    /**
     * Enable/Disable list of user frequently used places;
     * @param isList flag
     */
    public void enablePlaceList(boolean isList){
        this.isList = isList;
    }

    /**
     * Method that allows to hide all custom UI;
     * @param isAll flag to enable/disable additional UI elements.
     */
    public void enableUI(boolean isAll){
        this.isAll = isAll;
    }

    /**
     * Method that allows to search panel;
     * @param isSearch flag to enable/disable searchBar.
     */
    public void enableSearch(boolean isSearch){
        this.isSearch = isSearch;
    }

    /**
     * Clear map;
     */
    public void clear(){
        if (map != null) map.clear();
    }

    /**
     * On back pressed interface for map;
     * @return
     */
    public boolean onBackPressed(){
        if(isLayersVisible()) {
            ViewUtils.hideOver(layersContainer);
            return false;
        } else return true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        initArguments(getArguments());

        View rootView = inflater.inflate(R.layout.fragment_map, container, false);

        final SharedPrefs sPrefs = new SharedPrefs(getActivity());
        cSetter = new ColorSetter(getActivity());

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
                if (isLayersVisible()) ViewUtils.hideOver(layersContainer);
                if (isTouch) {
                    if (!spinnerArray.isEmpty()) {
                        placesList.setSelection(0);
                    }
                    addMarker(latLng, markerTitle, true, true, markerRadius);
                }
            }
        });

        if (lastPos != null) {
            addMarker(lastPos, lastPos.toString(), true, false, markerRadius);
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
        ImageButton zoomOut = (ImageButton) rootView.findViewById(R.id.zoomOut);
        ImageButton layers = (ImageButton) rootView.findViewById(R.id.layers);
        ImageButton myLocation = (ImageButton) rootView.findViewById(R.id.myLocation);

        cardClear.setOnLongClickListener(this);
        zoomOut.setOnLongClickListener(this);
        layers.setOnLongClickListener(this);
        myLocation.setOnLongClickListener(this);

        zoomOut.setBackgroundColor(cSetter.getBackgroundStyle());
        layers.setBackgroundColor(cSetter.getBackgroundStyle());
        myLocation.setBackgroundColor(cSetter.getBackgroundStyle());

        boolean isDark = sPrefs.loadBoolean(Prefs.USE_DARK_THEME);

        if (isDark){
            cardClear.setImageResource(R.drawable.ic_clear_white_24dp);
            zoomOut.setImageResource(R.drawable.ic_fullscreen_exit_white_24dp);
            layers.setImageResource(R.drawable.ic_layers_white_24dp);
            myLocation.setImageResource(R.drawable.ic_my_location_white_24dp);
            layersContainer.setBackgroundResource(R.drawable.popup_dark);
        } else {
            cardClear.setImageResource(R.drawable.ic_clear_black_24dp);
            zoomOut.setImageResource(R.drawable.ic_fullscreen_exit_black_24dp);
            layers.setImageResource(R.drawable.ic_layers_black_24dp);
            myLocation.setImageResource(R.drawable.ic_my_location_black_24dp);
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
                if (isLayersVisible()) ViewUtils.hideOver(layersContainer);
                else ViewUtils.showOver(layersContainer);
            }
        });
        myLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isLayersVisible()) ViewUtils.hideOver(layersContainer);
                Location location = map.getMyLocation();
                if (location != null) {
                    double lat = location.getLatitude();
                    double lon = location.getLongitude();
                    LatLng pos = new LatLng(lat, lon);
                    animate(pos);
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
                ViewUtils.hideOver(layersContainer);
            }
        });
        typeSatellite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                sPrefs.savePrefs(Prefs.MAP_TYPE, Constants.MAP_TYPE_SATELLITE);
                ViewUtils.hideOver(layersContainer);
            }
        });
        typeHybrid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                sPrefs.savePrefs(Prefs.MAP_TYPE, Constants.MAP_TYPE_HYBRID);
                ViewUtils.hideOver(layersContainer);
            }
        });
        typeTerrain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                map.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                sPrefs.savePrefs(Prefs.MAP_TYPE, Constants.MAP_TYPE_TERRAIN);
                ViewUtils.hideOver(layersContainer);
            }
        });

        cardSearch = (AutoCompleteTextView) rootView.findViewById(R.id.cardSearch);
        if (isDark) cardSearch.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_search_white_24dp, 0, 0, 0);
        else cardSearch.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_search_black_24dp, 0, 0, 0);
        cardSearch.setThreshold(3);
        adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_dropdown_item_1line, namesList);
        adapter.setNotifyOnChange(true);
        cardSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (isLayersVisible()) ViewUtils.hideOver(layersContainer);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (task != null && !task.isCancelled()) task.cancel(true);
                if (s.length() != 0) {
                    task = new GeocoderTask(getActivity());
                    task.execute(s.toString());
                }
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
                addMarker(pos, markerTitle, true, true, markerRadius);
                if (listener != null) listener.placeName(namesList.get(position));
            }
        });

        loadPlaces();

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
                    if (isLayersVisible()) ViewUtils.hideOver(layersContainer);
                    if (position > 0){
                        String placeName = spinnerArray.get(position);
                        DataBase DB = new DataBase(getActivity());
                        DB.open();
                        Cursor c = DB.getPlace(placeName);
                        if (c != null && c.moveToFirst()) {
                            double latitude = c.getDouble(c.getColumnIndex(Constants.LocationConstants.COLUMN_LOCATION_LATITUDE));
                            double longitude = c.getDouble(c.getColumnIndex(Constants.LocationConstants.COLUMN_LOCATION_LONGITUDE));
                            LatLng latLng = new LatLng(latitude, longitude);
                            addMarker(latLng, markerTitle, true, true, markerRadius);
                        }
                        if (c != null) c.close();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                    if (isLayersVisible()) ViewUtils.hideOver(layersContainer);
                }
            });
        }

        if (!isAll){
            card.setVisibility(View.GONE);
            card1.setVisibility(View.GONE);
            card2.setVisibility(View.GONE);
            placesList.setVisibility(View.GONE);
        }

        if (!isList) placesList.setVisibility(View.GONE);

        if (!isClose) card1.setVisibility(View.GONE);

        if (!isSearch) card.setVisibility(View.GONE);

        return rootView;
    }

    private void initArguments(Bundle arguments) {
        if (arguments != null) {
            isTouch = arguments.getBoolean(ENABLE_TOUCH, true);
            isAll = arguments.getBoolean(ENABLE_ALL_UI, true);
            isClose = arguments.getBoolean(ENABLE_CLOSE, true);
            isList = arguments.getBoolean(ENABLE_LIST, true);
            isSearch = arguments.getBoolean(ENABLE_SEARCH, true);
        }
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

        private Context context;

        public GeocoderTask(Context context){
            this.context = context;
        }

        @Override
        protected List<Address> doInBackground(String... locationName) {
            // Creating an instance of Geocoder class
            Geocoder geocoder = new Geocoder(context);
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