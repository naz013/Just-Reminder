/**
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

package com.cray.software.justreminder.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.ScreenManager;
import com.cray.software.justreminder.adapters.PlaceRecyclerAdapter;
import com.cray.software.justreminder.datas.PlaceDataProvider;
import com.cray.software.justreminder.fragments.helpers.MapFragment;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.interfaces.SimpleListener;
import com.cray.software.justreminder.reminder.Reminder;

public class GeolocationFragment extends Fragment implements SimpleListener {

    private PlaceDataProvider provider;
    private MapFragment fragment;

    public static GeolocationFragment newInstance() {
        return new GeolocationFragment();
    }

    public GeolocationFragment() {
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
        View rootView = inflater.inflate(R.layout.fragment_geolocation_layout, container, false);

        fragment = MapFragment.newInstance(false, true, false, false, false, false,
                new ColorSetter(getActivity()).isDark());
        fragment.setAdapter(loadPlaces());

        getChildFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
        return rootView;
    }

    private PlaceRecyclerAdapter loadPlaces(){
        provider = new PlaceDataProvider(getActivity(), false);
        PlaceRecyclerAdapter adapter = new PlaceRecyclerAdapter(getActivity(), provider, true);
        adapter.setEventListener(this);
        return adapter;
    }

    private void editPlace(int position){
        Reminder.edit(provider.getItem(position).getId(), getActivity());
    }

    private void moveToPlace(int position){
        fragment.moveCamera(provider.getItem(position).getPosition());
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((ScreenManager)activity).onSectionAttached(ScreenManager.FRAGMENT_LOCATIONS);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadPlaces();
    }

    @Override
    public void onItemClicked(int position, View view) {
        moveToPlace(position);
    }

    @Override
    public void onItemLongClicked(final int position, View view) {
        final CharSequence[] items = {getString(R.string.edit)};
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if (which == 0) {
                    editPlace(position);
                }
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }
}
