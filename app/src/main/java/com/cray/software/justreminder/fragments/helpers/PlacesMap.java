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

package com.cray.software.justreminder.fragments.helpers;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.adapters.GooglePlacesAdapter;
import com.cray.software.justreminder.async.PlacesTask;
import com.cray.software.justreminder.constants.Configs;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.datas.models.PlaceModel;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.Messages;
import com.cray.software.justreminder.helpers.Permissions;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.interfaces.ExecutionListener;
import com.cray.software.justreminder.interfaces.MapListener;
import com.cray.software.justreminder.interfaces.SimpleListener;
import com.cray.software.justreminder.json.JPlace;
import com.cray.software.justreminder.modules.Module;
import com.cray.software.justreminder.roboto_views.RoboEditText;
import com.cray.software.justreminder.roboto_views.RoboTextView;
import com.cray.software.justreminder.utils.QuickReturnUtils;
import com.cray.software.justreminder.utils.ViewUtils;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

public class PlacesMap extends Fragment implements View.OnClickListener, ExecutionListener {

    private static final String HAS_SHOWCASE = "places_showcase";

    private Activity mContext;

    /**
     * UI elements;
     */
    private GoogleMap mMap;
    private CardView layersContainer;
    private CardView styleCard;
    private CardView placesListCard;
    private RoboEditText cardSearch;
    private ImageButton zoomOut;
    private ImageButton places;
    private ImageButton markers;
    private LinearLayout groupOne, groupTwo, groupThree;
    private RecyclerView placesList;
    private LinearLayout emptyItem;

    /**
     * Array of user frequently used places;
     */
    private ArrayList<PlaceModel> spinnerArray = new ArrayList<>();

    /**
     * init variables and flags;
     */
    private boolean isZoom = true;
    private boolean isFullscreen = false;
    private boolean isDark = false;
    private int mRadius = -1;
    private int markerStyle = -1;
    private int mMapType = GoogleMap.MAP_TYPE_NORMAL;
    private double mLat, mLng;

    /**
     * UI helper class;
     */
    private ColorSetter mColor;

    /**
     * Arrays of place search results;
     */
    private PlacesTask mPlacesAsync;
    private LocationManager mLocationManager;
    private LocationListener mLocList;

    /**
     * MapListener link;
     */
    private MapListener mMapListener;
    private MapCallback mCallback;

    public static final String ENABLE_ZOOM = "enable_zoom";
    public static final String MARKER_STYLE = "marker_style";
    public static final String THEME_MODE = "theme_mode";

    private OnMapReadyCallback mMapCallback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(GoogleMap googleMap) {
            mMap = googleMap;
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            mMap.getUiSettings().setCompassEnabled(true);
            mMap.setMapType(mMapType);
            setMyLocation();
            mMap.setOnMapClickListener(latLng -> {
                hideLayers();
                hidePlaces();
                hideStyles();
            });
            if (mCallback != null) {
                mCallback.onMapReady();
            }
        }
    };

    public static PlacesMap newInstance(boolean isZoom, boolean isDark) {
        PlacesMap fragment = new PlacesMap();
        Bundle args = new Bundle();
        args.putBoolean(ENABLE_ZOOM, isZoom);
        args.putBoolean(THEME_MODE, isDark);
        fragment.setArguments(args);
        return fragment;
    }

    public static PlacesMap newInstance(boolean isZoom, int markerStyle, boolean isDark) {
        PlacesMap fragment = new PlacesMap();
        Bundle args = new Bundle();
        args.putBoolean(ENABLE_ZOOM, isZoom);
        args.putBoolean(THEME_MODE, isDark);
        args.putInt(MARKER_STYLE, markerStyle);
        fragment.setArguments(args);
        return fragment;
    }

    public PlacesMap() {

    }

    /**
     * Set listener for map fragment;
     * @param listener listener for map fragment
     */
    public void setListener(MapListener listener) {
        this.mMapListener = listener;
    }

    /**
     * Set listener for map fragment;
     * @param callback listener for map fragment
     */
    public void setCallback(MapCallback callback) {
        this.mCallback = callback;
    }

    /**
     * Set radius for marker;
     * @param mRadius radius for drawing circle around marker
     */
    public void setRadius(int mRadius) {
        this.mRadius = mRadius;
    }

    /**
     * Set style for marker;
     * @param markerStyle code of style for marker
     */
    public void setMarkerStyle(int markerStyle) {
        this.markerStyle = markerStyle;
    }

    /**
     * Add marker to map;
     * @param pos coordinates
     * @param title marker title
     * @param clear remove previous markers flag
     * @param animate animate to marker position
     * @param radius radius for circle around marker
     */
    public void addMarker(LatLng pos, String title, boolean clear, boolean animate, int radius) {
        if (mMap != null && pos != null) {
            if (pos.latitude == 0.0 && pos.longitude == 0.0) return;
            mRadius = radius;
            if (mRadius == -1) {
                mRadius = SharedPrefs.getInstance(mContext).getInt(Prefs.LOCATION_RADIUS);
            }
            if (clear) {
                mMap.clear();
            }
            if (title == null || title.matches("")) {
                title = pos.toString();
            }
            mMap.addMarker(new MarkerOptions()
                    .position(pos)
                    .title(title)
                    .icon(BitmapDescriptorFactory.fromResource(mColor.getMarkerStyle(markerStyle)))
                    .draggable(clear));
            int[] circleColors = mColor.getMarkerRadiusStyle(markerStyle);
            float strokeWidth = 3f;
            mMap.addCircle(new CircleOptions()
                    .center(pos)
                    .radius(mRadius)
                    .strokeWidth(strokeWidth)
                    .fillColor(ViewUtils.getColor(mContext, circleColors[0]))
                    .strokeColor(ViewUtils.getColor(mContext, circleColors[1])));
            if (animate) {
                animate(pos);
            }
        }
    }

    /**
     * Recreate last added marker with new circle radius;
     * @param radius radius for a circle
     */
    public void recreateMarker(int radius) {
        mRadius = radius;
        if (mRadius == -1) {
            mRadius = SharedPrefs.getInstance(mContext).getInt(Prefs.LOCATION_RADIUS);
        }
        if (mMap != null) {
            addMarkers();
        }
    }

    /**
     * Recreate last added marker with new marker style;
     * @param style marker style.
     */
    public void recreateStyle(int style) {
        markerStyle = style;
        if (mMap != null) {
            addMarkers();
        }
    }

    /**
     * Add markers to map from JSON objects.
     * @param list list of objects.
     */
    public void addMarkers(ArrayList<JPlace> list) {
        mMap.clear();
        toModels(list, false);
        refreshAdapter(false);
    }

    /**
     * Add markers to map from JSON objects.
     * @param list list of objects.
     */
    public void selectMarkers(ArrayList<JPlace> list) {
        mMap.clear();
        toModels(list, true);
        refreshAdapter(false);
    }

    /**
     * Move camera to coordinates with animation;
     * @param latLng coordinates
     */
    public void animate(LatLng latLng) {
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latLng, 13);
        if (mMap != null) {
            mMap.animateCamera(update);
        }
    }

    /**
     * On back pressed interface for map;
     * @return boolean
     */
    public boolean onBackPressed() {
        if (isLayersVisible()) {
            hideLayers();
            return false;
        } else if (isMarkersVisible()) {
            hideStyles();
            return false;
        } else if (isPlacesVisible()) {
            hidePlaces();
            return false;
        } else {
            return true;
        }
    }

    public void showShowcase() {
        if (!SharedPrefs.getInstance(mContext).getBoolean(HAS_SHOWCASE)) {
            ColorSetter coloring = new ColorSetter(mContext);
            ShowcaseConfig config = new ShowcaseConfig();
            config.setDelay(350);
            config.setMaskColor(coloring.getColor(coloring.colorAccent()));
            config.setContentTextColor(coloring.getColor(R.color.whitePrimary));
            config.setDismissTextColor(coloring.getColor(R.color.whitePrimary));

            MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(mContext);
            sequence.setConfig(config);

            sequence.addSequenceItem(zoomOut,
                    mContext.getString(R.string.click_to_expand_collapse_map),
                    mContext.getString(R.string.got_it));

            sequence.addSequenceItem(markers,
                    mContext.getString(R.string.select_style_for_marker),
                    mContext.getString(R.string.got_it));

            sequence.addSequenceItem(places,
                    mContext.getString(R.string.select_place_from_list),
                    mContext.getString(R.string.got_it));
            sequence.start();
            SharedPrefs.getInstance(mContext).putBoolean(HAS_SHOWCASE, true);
        }
    }

    private void initArgs() {
        Bundle args = getArguments();
        if (args != null) {
            isZoom = args.getBoolean(ENABLE_ZOOM, true);
            isDark = args.getBoolean(THEME_MODE, false);
            markerStyle = args.getInt(MARKER_STYLE,
                    SharedPrefs.getInstance(mContext).getInt(Prefs.MARKER_STYLE));
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (mContext == null) {
            mContext = (Activity) context;
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (mContext == null) {
            mContext = activity;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        initArgs();
        View view = inflater.inflate(R.layout.fragment_places_map, container, false);
        final SharedPrefs prefs = SharedPrefs.getInstance(mContext);
        mRadius = prefs.getInt(Prefs.LOCATION_RADIUS);
        mMapType = prefs.getInt(Prefs.MAP_TYPE);

        mColor = new ColorSetter(mContext);
        isDark = mColor.isDark();

        ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map)).getMapAsync(mMapCallback);

        initViews(view);
        cardSearch = (RoboEditText) view.findViewById(R.id.cardSearch);
        cardSearch.setHint(R.string.search_place);
        cardSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                hideLayers();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                loadPlaces();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        cardSearch.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER){
                hideKeyboard();
                loadPlaces();
                return true;
            } else return false;
        });

        placesList = (RecyclerView) view.findViewById(R.id.placesList);
        return view;
    }

    private void initViews(View view) {
        groupOne = (LinearLayout) view.findViewById(R.id.groupOne);
        groupTwo = (LinearLayout) view.findViewById(R.id.groupTwo);
        groupThree = (LinearLayout) view.findViewById(R.id.groupThree);
        emptyItem = (LinearLayout) view.findViewById(R.id.emptyItem);

        ImageView emptyImage = (ImageView) view.findViewById(R.id.emptyImage);
        if (isDark) {
            emptyImage.setImageResource(R.drawable.ic_directions_white_24dp);
        } else {
            emptyImage.setImageResource(R.drawable.ic_directions_black_24dp);
        }

        placesList = (RecyclerView) view.findViewById(R.id.placesList);

        CardView zoomCard = (CardView) view.findViewById(R.id.zoomCard);
        CardView searchCard = (CardView) view.findViewById(R.id.searchCard);
        CardView layersCard = (CardView) view.findViewById(R.id.layersCard);
        CardView placesCard = (CardView) view.findViewById(R.id.placesCard);
        CardView backCard = (CardView) view.findViewById(R.id.backCard);
        styleCard = (CardView) view.findViewById(R.id.styleCard);
        placesListCard = (CardView) view.findViewById(R.id.placesListCard);
        CardView markersCard = (CardView) view.findViewById(R.id.markersCard);
        placesListCard.setVisibility(View.GONE);
        styleCard.setVisibility(View.GONE);

        zoomCard.setCardBackgroundColor(mColor.getCardStyle());
        searchCard.setCardBackgroundColor(mColor.getCardStyle());
        layersCard.setCardBackgroundColor(mColor.getCardStyle());
        placesCard.setCardBackgroundColor(mColor.getCardStyle());
        styleCard.setCardBackgroundColor(mColor.getCardStyle());
        placesListCard.setCardBackgroundColor(mColor.getCardStyle());
        markersCard.setCardBackgroundColor(mColor.getCardStyle());
        backCard.setCardBackgroundColor(mColor.getCardStyle());

        layersContainer = (CardView) view.findViewById(R.id.layersContainer);
        layersContainer.setVisibility(View.GONE);
        layersContainer.setCardBackgroundColor(mColor.getCardStyle());

        if (Module.isLollipop()) {
            zoomCard.setCardElevation(Configs.CARD_ELEVATION);
            searchCard.setCardElevation(Configs.CARD_ELEVATION);
            layersContainer.setCardElevation(Configs.CARD_ELEVATION);
            layersCard.setCardElevation(Configs.CARD_ELEVATION);
            placesCard.setCardElevation(Configs.CARD_ELEVATION);
            styleCard.setCardElevation(Configs.CARD_ELEVATION);
            placesListCard.setCardElevation(Configs.CARD_ELEVATION);
            markersCard.setCardElevation(Configs.CARD_ELEVATION);
            backCard.setCardElevation(Configs.CARD_ELEVATION);
        }

        int style = mColor.getCardStyle();
        zoomCard.setCardBackgroundColor(style);
        searchCard.setCardBackgroundColor(style);
        layersContainer.setCardBackgroundColor(style);
        layersCard.setCardBackgroundColor(style);
        placesCard.setCardBackgroundColor(style);
        styleCard.setCardBackgroundColor(style);
        placesListCard.setCardBackgroundColor(style);
        markersCard.setCardBackgroundColor(style);
        backCard.setCardBackgroundColor(style);

        ImageButton cardClear = (ImageButton) view.findViewById(R.id.cardClear);
        zoomOut = (ImageButton) view.findViewById(R.id.mapZoom);
        ImageButton layers = (ImageButton) view.findViewById(R.id.layers);
        markers = (ImageButton) view.findViewById(R.id.markers);
        places = (ImageButton) view.findViewById(R.id.places);
        ImageButton backButton = (ImageButton) view.findViewById(R.id.backButton);

        if (isDark) {
            cardClear.setImageResource(R.drawable.ic_search_white_24dp);
            zoomOut.setImageResource(R.drawable.ic_arrow_upward_white_24dp);
            layers.setImageResource(R.drawable.ic_layers_white_24dp);
            markers.setImageResource(R.drawable.ic_palette_white_24dp);
            places.setImageResource(R.drawable.ic_directions_white_24dp);
            backButton.setImageResource(R.drawable.ic_keyboard_arrow_left_white_24dp);
        } else {
            cardClear.setImageResource(R.drawable.ic_search_black_24dp);
            zoomOut.setImageResource(R.drawable.ic_arrow_upward_black_24dp);
            layers.setImageResource(R.drawable.ic_layers_black_24dp);
            markers.setImageResource(R.drawable.ic_palette_black_24dp);
            places.setImageResource(R.drawable.ic_directions_black_24dp);
            backButton.setImageResource(R.drawable.ic_keyboard_arrow_left_black_24dp);
        }

        cardClear.setOnClickListener(this);
        zoomOut.setOnClickListener(this);
        layers.setOnClickListener(this);
        markers.setOnClickListener(this);
        places.setOnClickListener(this);

        RoboTextView typeNormal = (RoboTextView) view.findViewById(R.id.typeNormal);
        RoboTextView typeSatellite = (RoboTextView) view.findViewById(R.id.typeSatellite);
        RoboTextView typeHybrid = (RoboTextView) view.findViewById(R.id.typeHybrid);
        RoboTextView typeTerrain = (RoboTextView) view.findViewById(R.id.typeTerrain);
        typeNormal.setOnClickListener(this);
        typeSatellite.setOnClickListener(this);
        typeHybrid.setOnClickListener(this);
        typeTerrain.setOnClickListener(this);

        backCard.setVisibility(View.GONE);
        if (!Module.isPro()) {
            markersCard.setVisibility(View.GONE);
        }
        if (!isZoom) {
            zoomCard.setVisibility(View.GONE);
        }

        loadMarkers();
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager)
                mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(cardSearch.getWindowToken(), 0);
    }

    private void loadMarkers() {
        groupOne.removeAllViewsInLayout();
        groupTwo.removeAllViewsInLayout();
        groupThree.removeAllViewsInLayout();

        for (int i = 0; i < ColorSetter.NUM_OF_MARKERS; i++) {
            ImageButton ib = new ImageButton(mContext);
            ib.setBackgroundResource(android.R.color.transparent);
            ib.setImageResource(new ColorSetter(mContext).getMarkerStyle(i));
            ib.setId(i + ColorSetter.NUM_OF_MARKERS);
            ib.setOnClickListener(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    QuickReturnUtils.dp2px(mContext, 35),
                    QuickReturnUtils.dp2px(mContext, 35));
            int px = QuickReturnUtils.dp2px(mContext, 2);
            params.setMargins(px, px, px, px);
            ib.setLayoutParams(params);

            if (i < 5) {
                groupOne.addView(ib);
            } else if (i < 10) {
                groupTwo.addView(ib);
            } else {
                groupThree.addView(ib);
            }
        }
    }

    private void setMapType(int type) {
        if (mMap != null) {
            mMap.setMapType(type);
            SharedPrefs.getInstance(mContext).putInt(Prefs.MAP_TYPE, type);
            ViewUtils.hideOver(layersContainer);
        }
    }

    private void setMyLocation() {
        if (ActivityCompat.checkSelfPermission(mContext,
                Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(mContext,
                        Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {
            Permissions.requestPermission(mContext, 205,
                    Permissions.ACCESS_FINE_LOCATION,
                    Permissions.ACCESS_COARSE_LOCATION);
        } else {
            mMap.setMyLocationEnabled(true);
        }
    }

    private void loadPlaces(){
        String req = cardSearch.getText().toString().trim().toLowerCase();
        if (req.matches("")) return;
        cancelSearchTask();
        mPlacesAsync = new PlacesTask(this, req, mLat, mLng);
        mPlacesAsync.execute();
    }

    private void cancelSearchTask() {
        if (mPlacesAsync != null && !mPlacesAsync.isCancelled()) {
            mPlacesAsync.cancel(true);
        }
    }

    private void refreshAdapter(boolean show) {
        GooglePlacesAdapter placesAdapter = new GooglePlacesAdapter(mContext, spinnerArray);
        placesAdapter.setEventListener(new SimpleListener() {
            @Override
            public void onItemClicked(int position, View view) {
                hideLayers();
                hidePlaces();
                animate(spinnerArray.get(position).getPosition());
            }

            @Override
            public void onItemLongClicked(int position, View view) {

            }
        });

        if (spinnerArray != null && spinnerArray.size() > 0) {
            emptyItem.setVisibility(View.GONE);
            placesList.setVisibility(View.VISIBLE);
            placesList.setLayoutManager(new LinearLayoutManager(mContext));
            placesList.setAdapter(placesAdapter);
            addMarkers();
            if (!isPlacesVisible() && show) ViewUtils.slideInUp(mContext, placesListCard);
        } else {
            placesList.setVisibility(View.GONE);
            emptyItem.setVisibility(View.VISIBLE);
        }
    }

    public ArrayList<JPlace> getPlaces() {
        ArrayList<JPlace> places = new ArrayList<>();
        if (spinnerArray != null && spinnerArray.size() > 0) {
            for (PlaceModel model : spinnerArray) {
                if (model.getSelected() == 1) {
                    if (model.getPosition() != null) {
                        places.add(new JPlace(model.getName(), model.getPosition().latitude,
                                model.getPosition().longitude, model.getAddress(), model.getId(),
                                mRadius, markerStyle, model.getTypes()));
                    }
                }
            }
        }
        return places;
    }

    private void toModels(ArrayList<JPlace> list, boolean select) {
        spinnerArray = new ArrayList<>();
        if (list != null && list.size() > 0) {
            for (JPlace model : list) {
                spinnerArray.add(new PlaceModel(model.getName(), model.getId(),
                        null, model.getAddress(), new LatLng(model.getLatitude(),
                        model.getLongitude()), model.getTypes(), select ? 1 : 0));
            }
        }
    }

    private void addMarkers() {
        mMap.clear();
        if (spinnerArray != null && spinnerArray.size() > 0) {
            for (PlaceModel model : spinnerArray) {
                addMarker(model.getPosition(), model.getName(), false, false, mRadius);
            }
        }
    }

    private void toggleMarkers() {
        if (isLayersVisible()) {
            hideLayers();
        }
        if (isPlacesVisible()) {
            hidePlaces();
        }
        if (isMarkersVisible()) {
            hideStyles();
        } else {
            ViewUtils.slideInUp(mContext, styleCard);
        }
    }

    private void hideStyles() {
        if (isMarkersVisible()) {
            ViewUtils.slideOutDown(mContext, styleCard);
        }
    }

    private boolean isMarkersVisible() {
        return styleCard != null && styleCard.getVisibility() == View.VISIBLE;
    }

    private void togglePlaces() {
        if (isMarkersVisible()) {
            hideStyles();
        }
        if (isLayersVisible()) {
            hideLayers();
        }
        if (isPlacesVisible()) {
            hidePlaces();
        } else {
            ViewUtils.slideInUp(mContext, placesListCard);
        }
    }

    private void hidePlaces() {
        if (isPlacesVisible()) {
            ViewUtils.slideOutDown(mContext, placesListCard);
        }
    }

    private boolean isPlacesVisible() {
        return placesListCard != null && placesListCard.getVisibility() == View.VISIBLE;
    }

    private void toggleLayers() {
        if (isMarkersVisible()) {
            hideStyles();
        }
        if (isPlacesVisible()) {
            hidePlaces();
        }
        if (isLayersVisible()) {
            hideLayers();
        } else {
            ViewUtils.showOver(layersContainer);
        }
    }

    private void hideLayers() {
        if (isLayersVisible()) {
            ViewUtils.hideOver(layersContainer);
        }
    }

    private void zoomClick() {
        isFullscreen = !isFullscreen;
        if (mMapListener != null) {
            mMapListener.onZoomClick(isFullscreen);
        }
        if (isFullscreen) {
            if (isDark) zoomOut.setImageResource(R.drawable.ic_arrow_downward_white_24dp);
            else zoomOut.setImageResource(R.drawable.ic_arrow_downward_black_24dp);
        } else {
            if (isDark) zoomOut.setImageResource(R.drawable.ic_arrow_upward_white_24dp);
            else zoomOut.setImageResource(R.drawable.ic_arrow_upward_black_24dp);
        }
    }

    private boolean isLayersVisible() {
        return layersContainer != null && layersContainer.getVisibility() == View.VISIBLE;
    }

    @Override
    public void onResume() {
        super.onResume();
        mLocList = new CurrentLocation();
        updateListener();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 205:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    setMyLocation();
                } else {
                    Messages.toast(mContext, R.string.cant_access_location_services);
                }
                break;
            case 200:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    updateListener();
                } else {
                    Messages.toast(mContext, R.string.cant_access_location_services);
                }
                break;
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id >= ColorSetter.NUM_OF_MARKERS && id < ColorSetter.NUM_OF_MARKERS * 2) {
            recreateStyle(v.getId() - ColorSetter.NUM_OF_MARKERS);
            hideStyles();
        }

        switch (id) {
            case R.id.cardClear:
                loadPlaces();
                break;
            case R.id.mapZoom:
                zoomClick();
                break;
            case R.id.layers:
                toggleLayers();
                break;
            case R.id.typeNormal:
                setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
            case R.id.typeHybrid:
                setMapType(GoogleMap.MAP_TYPE_HYBRID);
                break;
            case R.id.typeSatellite:
                setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                break;
            case R.id.typeTerrain:
                setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                break;
            case R.id.places:
                togglePlaces();
                break;
            case R.id.markers:
                toggleMarkers();
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        removeUpdates();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        removeUpdates();
        cancelSearchTask();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        removeUpdates();
    }

    @Override
    public void onStop() {
        super.onStop();
        removeUpdates();
    }

    @Override
    public void onPause() {
        super.onPause();
        removeUpdates();
    }

    private void removeUpdates() {
        if (mLocList != null) {
            if (Permissions.checkPermission(mContext,
                    Permissions.ACCESS_COARSE_LOCATION, Permissions.ACCESS_FINE_LOCATION)) {
                mLocationManager.removeUpdates(mLocList);
            } else {
                Permissions.requestPermission(mContext, 201,
                        Permissions.ACCESS_FINE_LOCATION,
                        Permissions.ACCESS_COARSE_LOCATION);
            }
        }
    }

    @Override
    public void onFinish(ArrayList<PlaceModel> places) {
        spinnerArray = places;
        if (spinnerArray.size() == 0)
            Messages.toast(mContext, mContext.getString(R.string.no_places_found));

        if (spinnerArray != null && spinnerArray.size() > 1) {
            spinnerArray.add(new PlaceModel(mContext.getString(R.string.add_all), null, null, null, null, null, 0));
        }
        refreshAdapter(true);
    }

    public class CurrentLocation implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            mLat = location.getLatitude();
            mLng = location.getLongitude();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            updateListener();
        }

        @Override
        public void onProviderEnabled(String provider) {
            updateListener();
        }

        @Override
        public void onProviderDisabled(String provider) {
            updateListener();
        }
    }

    private void updateListener() {
        if (mContext != null) {
            mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
            SharedPrefs prefs = SharedPrefs.getInstance(mContext);
            long time = (prefs.getInt(Prefs.TRACK_TIME) * 1000) * 2;
            int distance = prefs.getInt(Prefs.TRACK_DISTANCE) * 2;
            if (Permissions.checkPermission(mContext,
                    Permissions.ACCESS_COARSE_LOCATION,
                    Permissions.ACCESS_FINE_LOCATION)) {
                if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, time, distance, mLocList);
                } else {
                    mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, time, distance, mLocList);
                }
            } else {
                Permissions.requestPermission(mContext, 200,
                        Permissions.ACCESS_COARSE_LOCATION,
                        Permissions.ACCESS_FINE_LOCATION);
            }
        }
    }
}
