/*
 * Copyright 2015 Nazar Suhovich
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
import android.location.Address;
import android.location.Criteria;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.async.GeocoderTask;
import com.cray.software.justreminder.constants.Configs;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.Messages;
import com.cray.software.justreminder.helpers.Permissions;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.interfaces.MapListener;
import com.cray.software.justreminder.interfaces.SimpleListener;
import com.cray.software.justreminder.helpers.Module;
import com.cray.software.justreminder.places.PlaceAdapter;
import com.cray.software.justreminder.places.PlaceItem;
import com.cray.software.justreminder.places.PlaceRecyclerAdapter;
import com.cray.software.justreminder.places.PlacesHelper;
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
import java.util.List;

import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

public class MapFragment extends Fragment implements View.OnClickListener {

    private static final String HAS_SHOWCASE = "has_showcase";

    private Activity mContext;

    /**
     * UI elements;
     */
    private GoogleMap mMap;
    private CardView layersContainer;
    private CardView styleCard;
    private CardView placesListCard;
    private AutoCompleteTextView cardSearch;
    private ImageButton zoomOut;
    private ImageButton backButton;
    private ImageButton places;
    private ImageButton markers;
    private LinearLayout groupOne, groupTwo, groupThree;
    private RecyclerView placesList;
    private LinearLayout emptyItem;

    /**
     * Array of user frequently used places;
     */
    private ArrayList<String> spinnerArray = new ArrayList<>();
    private PlaceRecyclerAdapter placeRecyclerAdapter;

    /**
     * init variables and flags;
     */
    private boolean isTouch = true;
    private boolean isZoom = true;
    private boolean isBack = true;
    private boolean isStyles = true;
    private boolean isPlaces = true;
    private boolean isSearch = true;
    private boolean isFullscreen = false;
    private boolean isDark = false;
    private String markerTitle;
    private int markerRadius = -1;
    private int markerStyle = -1;
    private int mMapType = GoogleMap.MAP_TYPE_NORMAL;
    private LatLng lastPos;
    private float strokeWidth = 3f;

    /**
     * UI helper class;
     */
    private ColorSetter mColor;

    /**
     * Arrays of place search results;
     */
    private List<Address> mFoundPlaces;
    private ArrayAdapter<String> mAdapter;
    private GeocoderTask mAddressTask;
    private ArrayList<String> mAddressNames;

    /**
     * MapListener link;
     */
    private MapListener mListener;
    private MapCallback mCallback;

    public static final String ENABLE_TOUCH = "enable_touch";
    public static final String ENABLE_PLACES = "enable_places";
    public static final String ENABLE_SEARCH = "enable_search";
    public static final String ENABLE_STYLES = "enable_styles";
    public static final String ENABLE_BACK = "enable_back";
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
            mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {
                    hideLayers();
                    hidePlaces();
                    hideStyles();
                    if (isTouch) {
                        addMarker(latLng, markerTitle, true, true, markerRadius);
                    }
                }
            });

            if (lastPos != null) {
                addMarker(lastPos, lastPos.toString(), true, false, markerRadius);
            }
            if (mCallback != null) {
                mCallback.onMapReady();
            }
        }
    };

    public static MapFragment newInstance(boolean isTouch, boolean isPlaces,
                                          boolean isSearch, boolean isStyles,
                                          boolean isBack, boolean isZoom, boolean isDark) {
        MapFragment fragment = new MapFragment();
        Bundle args = new Bundle();
        args.putBoolean(ENABLE_TOUCH, isTouch);
        args.putBoolean(ENABLE_PLACES, isPlaces);
        args.putBoolean(ENABLE_SEARCH, isSearch);
        args.putBoolean(ENABLE_STYLES, isStyles);
        args.putBoolean(ENABLE_BACK, isBack);
        args.putBoolean(ENABLE_ZOOM, isZoom);
        args.putBoolean(THEME_MODE, isDark);
        fragment.setArguments(args);
        return fragment;
    }

    public static MapFragment newInstance(boolean isPlaces, boolean isStyles, boolean isBack,
                                          boolean isZoom, int markerStyle, boolean isDark) {
        MapFragment fragment = new MapFragment();
        Bundle args = new Bundle();
        args.putBoolean(ENABLE_PLACES, isPlaces);
        args.putBoolean(ENABLE_STYLES, isStyles);
        args.putBoolean(ENABLE_BACK, isBack);
        args.putBoolean(ENABLE_ZOOM, isZoom);
        args.putBoolean(THEME_MODE, isDark);
        args.putInt(MARKER_STYLE, markerStyle);
        fragment.setArguments(args);
        return fragment;
    }

    public MapFragment() {

    }

    public void setAdapter(PlaceRecyclerAdapter adapter) {
        this.placeRecyclerAdapter = adapter;
    }

    /**
     * Set listener for map fragment;
     * @param listener listener for map fragment
     */
    public void setListener(MapListener listener) {
        this.mListener = listener;
    }

    /**
     * Set listener for map fragment;
     * @param callback listener for map fragment
     */
    public void setCallback(MapCallback callback) {
        this.mCallback = callback;
    }

    /**
     * Set title for markers;
     * @param markerTitle marker title
     */
    public void setMarkerTitle(String markerTitle) {
        this.markerTitle = markerTitle;
    }

    /**
     * Set radius for marker;
     * @param markerRadius radius for drawing circle around marker
     */
    public void setMarkerRadius(int markerRadius) {
        this.markerRadius = markerRadius;
    }

    /**
     * Set style for marker;
     * @param markerStyle code of style for marker
     */
    public void setMarkerStyle(int markerStyle) {
        this.markerStyle = markerStyle;
    }

    /**
     * Get currently used marker style.
     * @return marker code.
     */
    public int getMarkerStyle() {
        return markerStyle;
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
        if (mMap != null) {
            markerRadius = radius;
            if (markerRadius == -1)
                markerRadius = SharedPrefs.getInstance(mContext).getInt(Prefs.LOCATION_RADIUS);
            if (clear) mMap.clear();
            if (title == null || title.matches("")) title = pos.toString();
            if (!Module.isPro()) markerStyle = 5;
            lastPos = pos;
            if (mListener != null) mListener.placeChanged(pos);
            mMap.addMarker(new MarkerOptions()
                    .position(pos)
                    .title(title)
                    .icon(BitmapDescriptorFactory.fromResource(mColor.getMarkerStyle(markerStyle)))
                    .draggable(clear));
            int[] circleColors = mColor.getMarkerRadiusStyle(markerStyle);
            mMap.addCircle(new CircleOptions()
                    .center(pos)
                    .radius(markerRadius)
                    .strokeWidth(strokeWidth)
                    .fillColor(ViewUtils.getColor(mContext, circleColors[0]))
                    .strokeColor(ViewUtils.getColor(mContext, circleColors[1])));
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
    public void addMarker(LatLng pos, String title, boolean clear, int markerStyle, boolean animate, int radius) {
        if (mMap != null) {
            markerRadius = radius;
            if (markerRadius == -1) {
                markerRadius = SharedPrefs.getInstance(mContext).getInt(Prefs.LOCATION_RADIUS);
            }
            if (!Module.isPro()) markerStyle = 5;
            this.markerStyle = markerStyle;
            if (clear) mMap.clear();
            if (title == null || title.matches(""))
                title = pos.toString();
            lastPos = pos;
            if (mListener != null) mListener.placeChanged(pos);
            mMap.addMarker(new MarkerOptions()
                    .position(pos)
                    .title(title)
                    .icon(BitmapDescriptorFactory.fromResource(mColor.getMarkerStyle(markerStyle)))
                    .draggable(clear));
            int[] circleColors = mColor.getMarkerRadiusStyle(markerStyle);
            mMap.addCircle(new CircleOptions()
                    .center(pos)
                    .radius(markerRadius)
                    .strokeWidth(strokeWidth)
                    .fillColor(ViewUtils.getColor(mContext, circleColors[0]))
                    .strokeColor(ViewUtils.getColor(mContext, circleColors[1])));
            if (animate) animate(pos);
        } else {
            Log.d(Constants.LOG_TAG, "map is null");
        }
    }

    /**
     * Recreate last added marker with new circle radius;
     * @param radius radius for a circle
     */
    public void recreateMarker(int radius) {
        markerRadius = radius;
        if (markerRadius == -1)
            markerRadius = SharedPrefs.getInstance(mContext).getInt(Prefs.LOCATION_RADIUS);
        if (mMap != null && lastPos != null) {
            mMap.clear();
            if (markerTitle == null || markerTitle.matches(""))
                markerTitle = lastPos.toString();
            if (mListener != null) mListener.placeChanged(lastPos);
            if (!Module.isPro()) markerStyle = 5;
            mMap.addMarker(new MarkerOptions()
                    .position(lastPos)
                    .title(markerTitle)
                    .icon(BitmapDescriptorFactory.fromResource(mColor.getMarkerStyle(markerStyle)))
                    .draggable(true));
            int[] circleColors = mColor.getMarkerRadiusStyle(markerStyle);
            mMap.addCircle(new CircleOptions()
                    .center(lastPos)
                    .radius(markerRadius)
                    .strokeWidth(strokeWidth)
                    .fillColor(ViewUtils.getColor(mContext, circleColors[0]))
                    .strokeColor(ViewUtils.getColor(mContext, circleColors[1])));
            animate(lastPos);
        }
    }

    /**
     * Recreate last added marker with new marker style;
     * @param style marker style.
     */
    public void recreateStyle(int style) {
        markerStyle = style;
        if (mMap != null && lastPos != null) {
            mMap.clear();
            if (markerTitle == null || markerTitle.matches(""))
                markerTitle = lastPos.toString();
            if (mListener != null) mListener.placeChanged(lastPos);
            if (!Module.isPro()) markerStyle = 5;
            mMap.addMarker(new MarkerOptions()
                    .position(lastPos)
                    .title(markerTitle)
                    .icon(BitmapDescriptorFactory.fromResource(mColor.getMarkerStyle(markerStyle)))
                    .draggable(true));
            if (markerStyle >= 0) {
                int[] circleColors = mColor.getMarkerRadiusStyle(markerStyle);
                if (markerRadius == -1) {
                    markerRadius = SharedPrefs.getInstance(mContext).getInt(Prefs.LOCATION_RADIUS);
                }
                mMap.addCircle(new CircleOptions()
                        .center(lastPos)
                        .radius(markerRadius)
                        .strokeWidth(strokeWidth)
                        .fillColor(ViewUtils.getColor(mContext, circleColors[0]))
                        .strokeColor(ViewUtils.getColor(mContext, circleColors[1])));
            }
            animate(lastPos);
        }
    }

    /**
     * Move camera to coordinates;
     * @param pos coordinates
     */
    public void moveCamera(LatLng pos) {
        if (mMap != null) animate(pos);
    }

    /**
     * Move camera to coordinates with animation;
     * @param latLng coordinates
     */
    public void animate(LatLng latLng) {
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latLng, 13);
        if (mMap != null) mMap.animateCamera(update);
    }

    /**
     * Move camera to user current coordinates with animation;
     */
    public void moveToMyLocation() {
        if (mMap != null) {
            LocationManager locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
            Criteria criteria = new Criteria();
            android.location.Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
            if (location != null) {
                LatLng pos = new LatLng(location.getLatitude(), location.getLongitude());
                animate(pos);
            }
        }
    }

    public boolean isFullscreen() {
        return isFullscreen;
    }

    public void setFullscreen(boolean fullscreen) {
        isFullscreen = fullscreen;
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
        if (mContext == null) {
            return;
        }
        if (!SharedPrefs.getInstance(mContext).getBoolean(HAS_SHOWCASE) && isBack) {
            ColorSetter coloring = ColorSetter.getInstance(mContext);
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

            sequence.addSequenceItem(backButton,
                    mContext.getString(R.string.click_when_add_place),
                    mContext.getString(R.string.got_it));

            if (Module.isPro()) {
                sequence.addSequenceItem(markers,
                        mContext.getString(R.string.select_style_for_marker),
                        mContext.getString(R.string.got_it));
            }

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
            isTouch = args.getBoolean(ENABLE_TOUCH, true);
            isPlaces = args.getBoolean(ENABLE_PLACES, true);
            isSearch = args.getBoolean(ENABLE_SEARCH, true);
            isStyles = args.getBoolean(ENABLE_STYLES, true);
            isBack = args.getBoolean(ENABLE_BACK, true);
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
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        final SharedPrefs prefs = SharedPrefs.getInstance(mContext);
        markerRadius = prefs.getInt(Prefs.LOCATION_RADIUS);
        mMapType = prefs.getInt(Prefs.MAP_TYPE);
        if (!Module.isPro()) {
            markerStyle = prefs.getInt(Prefs.MARKER_STYLE);
        }

        mColor = ColorSetter.getInstance(mContext);
        isDark = mColor.isDark();

        SupportMapFragment fragment = SupportMapFragment.newInstance();
        fragment.getMapAsync(mMapCallback);
        getChildFragmentManager().beginTransaction()
                .add(R.id.map, fragment)
                .commit();
        initViews(view);
        cardSearch = (AutoCompleteTextView) view.findViewById(R.id.cardSearch);
        cardSearch.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_search_white_24dp, 0, 0, 0);
        cardSearch.setThreshold(3);
        mAdapter = new ArrayAdapter<>(mContext, android.R.layout.simple_dropdown_item_1line, mAddressNames);
        mAdapter.setNotifyOnChange(true);
        cardSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                hideLayers();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (mAddressTask != null && !mAddressTask.isCancelled()) {
                    mAddressTask.cancel(true);
                }
                if (s.length() != 0) {
                    mAddressTask = new GeocoderTask(mContext, addresses -> {
                        mFoundPlaces = addresses;

                        mAddressNames = new ArrayList<>();
                        mAddressNames.clear();
                        for (Address selected : addresses) {
                            String addressText = String.format("%s, %s%s",
                                    selected.getMaxAddressLineIndex() > 0 ? selected.getAddressLine(0) : "",
                                    selected.getMaxAddressLineIndex() > 1 ? selected.getAddressLine(1) + ", " : "",
                                    selected.getCountryName());
                            mAddressNames.add(addressText);
                        }
                        mAdapter = new ArrayAdapter<>(mContext, android.R.layout.simple_dropdown_item_1line, mAddressNames);
                        cardSearch.setAdapter(mAdapter);
                        mAdapter.notifyDataSetChanged();
                    });
                    mAddressTask.execute(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        cardSearch.setOnItemClickListener((parent, view1, position, id) -> {
            Address sel = mFoundPlaces.get(position);
            double lat = sel.getLatitude();
            double lon = sel.getLongitude();
            LatLng pos = new LatLng(lat, lon);
            addMarker(pos, markerTitle, true, true, markerRadius);
        });

        placesList = (RecyclerView) view.findViewById(R.id.placesList);
        if (isPlaces) loadPlaces();

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
        CardView myCard = (CardView) view.findViewById(R.id.myCard);
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
        myCard.setCardBackgroundColor(mColor.getCardStyle());
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
            myCard.setCardElevation(Configs.CARD_ELEVATION);
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
        myCard.setCardBackgroundColor(style);
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
        ImageButton myLocation = (ImageButton) view.findViewById(R.id.myLocation);
        markers = (ImageButton) view.findViewById(R.id.markers);
        places = (ImageButton) view.findViewById(R.id.places);
        backButton = (ImageButton) view.findViewById(R.id.backButton);

        if (isDark) {
            cardClear.setImageResource(R.drawable.ic_clear_white_vector);
            zoomOut.setImageResource(R.drawable.ic_arrow_upward_white_24dp);
            layers.setImageResource(R.drawable.ic_layers_white_24dp);
            myLocation.setImageResource(R.drawable.ic_my_location_white_24dp);
            markers.setImageResource(R.drawable.ic_palette_white_24dp);
            places.setImageResource(R.drawable.ic_directions_white_24dp);
            backButton.setImageResource(R.drawable.ic_keyboard_arrow_left_white_24dp);
        } else {
            cardClear.setImageResource(R.drawable.ic_clear_black_vector);
            zoomOut.setImageResource(R.drawable.ic_arrow_upward_black_24dp);
            layers.setImageResource(R.drawable.ic_layers_black_24dp);
            myLocation.setImageResource(R.drawable.ic_my_location_black_24dp);
            markers.setImageResource(R.drawable.ic_palette_black_24dp);
            places.setImageResource(R.drawable.ic_directions_black_24dp);
            backButton.setImageResource(R.drawable.ic_keyboard_arrow_left_black_24dp);
        }

        cardClear.setOnClickListener(this);
        zoomOut.setOnClickListener(this);
        layers.setOnClickListener(this);
        myLocation.setOnClickListener(this);
        markers.setOnClickListener(this);
        places.setOnClickListener(this);
        backButton.setOnClickListener(this);

        RoboTextView typeNormal = (RoboTextView) view.findViewById(R.id.typeNormal);
        RoboTextView typeSatellite = (RoboTextView) view.findViewById(R.id.typeSatellite);
        RoboTextView typeHybrid = (RoboTextView) view.findViewById(R.id.typeHybrid);
        RoboTextView typeTerrain = (RoboTextView) view.findViewById(R.id.typeTerrain);
        typeNormal.setOnClickListener(this);
        typeSatellite.setOnClickListener(this);
        typeHybrid.setOnClickListener(this);
        typeTerrain.setOnClickListener(this);

        if (!isPlaces) {
            placesCard.setVisibility(View.GONE);
        }

        if (!isBack) {
            backCard.setVisibility(View.GONE);
        }

        if (!isSearch) {
            searchCard.setVisibility(View.GONE);
        }

        if (!isStyles || !Module.isPro()) {
            markersCard.setVisibility(View.GONE);
        }

        if (!isZoom) {
            zoomCard.setVisibility(View.GONE);
        }

        loadMarkers();
    }

    private void loadMarkers() {
        groupOne.removeAllViewsInLayout();
        groupTwo.removeAllViewsInLayout();
        groupThree.removeAllViewsInLayout();

        for (int i = 0; i < ColorSetter.NUM_OF_MARKERS; i++) {
            ImageButton ib = new ImageButton(mContext);
            ib.setBackgroundResource(android.R.color.transparent);
            ib.setImageResource(ColorSetter.getInstance(mContext).getMarkerStyle(i));
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
        if (placeRecyclerAdapter == null) {
            List<PlaceItem> list = PlacesHelper.getInstance(mContext).getAll();
            spinnerArray = new ArrayList<>();
            spinnerArray.clear();
            for (PlaceItem item : list) {
                spinnerArray.add(item.getTitle());
            }
            if (spinnerArray.isEmpty()) {
                placesList.setVisibility(View.GONE);
                emptyItem.setVisibility(View.VISIBLE);
            } else {
                emptyItem.setVisibility(View.GONE);
                placesList.setVisibility(View.VISIBLE);
                PlaceAdapter adapter = new PlaceAdapter(mContext, spinnerArray);
                adapter.setEventListener(new SimpleListener() {
                    @Override
                    public void onItemClicked(int position, View view) {
                        hideLayers();
                        hidePlaces();
                        String placeName = spinnerArray.get(position);
                        PlaceItem item = PlacesHelper.getInstance(mContext).getPlace(placeName);
                        if (item != null) {
                            addMarker(item.getPosition(), markerTitle, true, true, markerRadius);
                        }
                    }

                    @Override
                    public void onItemLongClicked(int position, View view) {

                    }
                });
                placesList.setLayoutManager(new LinearLayoutManager(mContext));
                placesList.setAdapter(adapter);
            }
        } else {
            if (placeRecyclerAdapter.getItemCount() > 0) {
                emptyItem.setVisibility(View.GONE);
                placesList.setVisibility(View.VISIBLE);
                placesList.setLayoutManager(new LinearLayoutManager(mContext));
                placesList.setAdapter(placeRecyclerAdapter);
                addMarkers(placeRecyclerAdapter.getData());
            } else {
                placesList.setVisibility(View.GONE);
                emptyItem.setVisibility(View.VISIBLE);
            }
        }
    }

    private void addMarkers(List<PlaceItem> list) {
        if (list != null && list.size() > 0) {
            for (PlaceItem model : list) {
                addMarker(model.getPosition(), model.getTitle(), false,
                        model.getIcon(), false, model.getRadius());
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
        if (mListener != null) {
            mListener.onZoomClick(isFullscreen);
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
                cardSearch.setText("");
                break;
            case R.id.mapZoom:
                zoomClick();
                break;
            case R.id.layers:
                toggleLayers();
                break;
            case R.id.myLocation:
                hideLayers();
                moveToMyLocation();
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
            case R.id.backButton:
                restoreScaleButton();
                if (mListener != null) {
                    mListener.onBackClick();
                }
                break;
        }
    }

    private void restoreScaleButton() {
        if (isDark) {
            zoomOut.setImageResource(R.drawable.ic_arrow_upward_white_24dp);
        } else {
            zoomOut.setImageResource(R.drawable.ic_arrow_upward_black_24dp);
        }
    }
}
