package com.hexrain.design.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.adapters.PlaceRecyclerAdapter;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.datas.PlaceDataProvider;
import com.cray.software.justreminder.dialogs.utils.NewPlace;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.interfaces.Prefs;
import com.cray.software.justreminder.interfaces.SwipeListener;
import com.cray.software.justreminder.modules.Module;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.animator.SwipeDismissItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.decoration.SimpleListDividerDecorator;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager;
import com.h6ah4i.android.widget.advrecyclerview.touchguard.RecyclerViewTouchActionGuardManager;
import com.hexrain.design.NavigationDrawerFragment;
import com.hexrain.design.ScreenManager;

import jp.wasabeef.recyclerview.animators.LandingAnimator;

public class PlacesFragment extends Fragment implements SwipeListener {

    private RecyclerView listView;
    private LinearLayout emptyLayout, emptyItem;
    private AdView adView;

    private PlaceRecyclerAdapter adapter;
    private PlaceDataProvider provider;

    private NavigationDrawerFragment.NavigationDrawerCallbacks mCallbacks;

    public static PlacesFragment newInstance() {
        return new PlacesFragment();
    }

    public PlacesFragment() {
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
        View rootView = inflater.inflate(R.layout.fragment_screen_manager, container, false);

        ColorSetter cSetter = new ColorSetter(getActivity());
        SharedPrefs sPrefs = new SharedPrefs(getActivity());

        emptyItem = (LinearLayout) rootView.findViewById(R.id.emptyItem);
        emptyItem.setVisibility(View.VISIBLE);

        TextView emptyText = (TextView) rootView.findViewById(R.id.emptyText);
        emptyText.setText(getActivity().getString(R.string.empty_places_list_text));

        ImageView emptyImage = (ImageView) rootView.findViewById(R.id.emptyImage);
        if (sPrefs.loadBoolean(Prefs.USE_DARK_THEME)) {
            emptyImage.setImageResource(R.drawable.ic_place_white_24dp);
        } else {
            emptyImage.setImageResource(R.drawable.ic_place_grey600_24dp);
        }

        listView = (RecyclerView) rootView.findViewById(R.id.currentList);

        if (!Module.isPro()) {
            emptyLayout = (LinearLayout) rootView.findViewById(R.id.emptyLayout);
            emptyLayout.setVisibility(View.GONE);

            adView = (AdView) rootView.findViewById(R.id.adView);
            adView.setVisibility(View.GONE);

            AdRequest adRequest = new AdRequest.Builder()
                    .build();
            adView.loadAd(adRequest);
            adView.setAdListener(new AdListener() {
                @Override
                public void onAdFailedToLoad(int errorCode) {
                    adView.setVisibility(View.GONE);
                    emptyLayout.setVisibility(View.GONE);
                }

                @Override
                public void onAdLoaded() {
                    emptyLayout.setVisibility(View.VISIBLE);
                    adView.setVisibility(View.VISIBLE);
                }
            });

            RelativeLayout ads_container = (RelativeLayout) rootView.findViewById(R.id.ads_container);
        }

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallbacks = (NavigationDrawerFragment.NavigationDrawerCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
        }
        ((ScreenManager)activity).onSectionAttached(ScreenManager.FRAGMENT_PLACES);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadPlaces();
        if (!Module.isPro()){
            if (adView != null) {
                adView.resume();
            }
        }
    }

    @Override
    public void onDestroy() {
        if (!Module.isPro()) {
            if (adView != null) {
                adView.destroy();
            }
        }
        super.onDestroy();
    }

    @Override
    public void onPause() {
        if (!Module.isPro()) {
            if (adView != null) {
                adView.pause();
            }
        }
        super.onPause();
    }

    private void loadPlaces(){
        DataBase db = new DataBase(getActivity());
        db.open();
        provider = new PlaceDataProvider(getActivity());
        reloadView();

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        RecyclerViewTouchActionGuardManager mRecyclerViewTouchActionGuardManager = new RecyclerViewTouchActionGuardManager();
        mRecyclerViewTouchActionGuardManager.setInterceptVerticalScrollingWhileAnimationRunning(true);
        mRecyclerViewTouchActionGuardManager.setEnabled(true);
        RecyclerViewSwipeManager mRecyclerViewSwipeManager = new RecyclerViewSwipeManager();

        adapter = new PlaceRecyclerAdapter(getActivity(), provider);
        adapter.setEventListener(this);
        RecyclerView.Adapter mWrappedAdapter = mRecyclerViewSwipeManager.createWrappedAdapter(adapter);
        final GeneralItemAnimator animator = new SwipeDismissItemAnimator();
        animator.setSupportsChangeAnimations(false);
        listView.setLayoutManager(mLayoutManager);
        listView.setAdapter(mWrappedAdapter);  // requires *wrapped* adapter
        listView.setItemAnimator(new LandingAnimator());
        listView.addItemDecoration(new SimpleListDividerDecorator(new ColorDrawable(android.R.color.transparent), true));
        mRecyclerViewTouchActionGuardManager.attachRecyclerView(listView);
        mRecyclerViewSwipeManager.attachRecyclerView(listView);
        db.close();
        if (mCallbacks != null) mCallbacks.onListChange(listView);
    }

    private void reloadView() {
        int size = provider.getCount();
        if (size > 0){
            listView.setVisibility(View.VISIBLE);
            emptyItem.setVisibility(View.GONE);
        } else {
            listView.setVisibility(View.GONE);
            emptyItem.setVisibility(View.VISIBLE);
        }
    }

    private void deletePlace(int position){
        long id = provider.getItem(position).getId();
        if (id != 0) {
            DataBase db = new DataBase(getActivity());
            db.open();
            db.deletePlace(id);
            provider.removeItem(position);
            adapter.notifyItemRemoved(position);
            Toast.makeText(getActivity(), getString(R.string.delete_place_toast), Toast.LENGTH_SHORT).show();
            db.close();
            reloadView();
        }
    }

    private void editPlace(int position){
        startActivity(new Intent(getActivity(), NewPlace.class)
            .putExtra(Constants.ITEM_ID_INTENT, provider.getItem(position).getId()));
    }

    @Override
    public void onSwipeToRight(int position) {
        editPlace(position);
    }

    @Override
    public void onSwipeToLeft(int position) {
        deletePlace(position);
    }

    @Override
    public void onItemClicked(int position, View view) {
        editPlace(position);
    }

    @Override
    public void onItemLongClicked(int position) {
        deletePlace(position);
    }
}
