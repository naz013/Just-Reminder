/**
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

package com.cray.software.justreminder.places;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.enums.QuickReturnViewType;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.Dialogues;
import com.cray.software.justreminder.helpers.Messages;
import com.cray.software.justreminder.helpers.Permissions;
import com.cray.software.justreminder.interfaces.SimpleListener;
import com.cray.software.justreminder.helpers.Module;
import com.cray.software.justreminder.roboto_views.RoboTextView;
import com.cray.software.justreminder.utils.LocationUtil;
import com.cray.software.justreminder.utils.QuickReturnUtils;
import com.cray.software.justreminder.utils.ViewUtils;
import com.cray.software.justreminder.views.ReturnScrollListener;

import java.util.List;

public class PlacesActivity extends AppCompatActivity implements SimpleListener {

    private RecyclerView mRecyclerView;
    private LinearLayout mEmptyView;
    private ColorSetter cs = ColorSetter.getInstance(PlacesActivity.this);
    private FloatingActionButton mFab;

    private PlaceRecyclerAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(cs.getStyle());
        if (Module.isLollipop()) {
            getWindow().setStatusBarColor(ViewUtils.getColor(this, cs.colorPrimaryDark()));
        }
        setContentView(R.layout.places_activity_layout);
        setRequestedOrientation(cs.getRequestOrientation());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setTitle(getString(R.string.places));

        findViewById(R.id.windowBackground).setBackgroundColor(cs.getBackgroundStyle());

        mEmptyView = (LinearLayout) findViewById(R.id.emptyItem);
        mEmptyView.setVisibility(View.VISIBLE);
        RoboTextView emptyText = (RoboTextView) findViewById(R.id.emptyText);
        emptyText.setText(getString(R.string.no_places));
        ImageView emptyImage = (ImageView) findViewById(R.id.emptyImage);
        if (cs.isDark()) {
            emptyImage.setImageResource(R.drawable.ic_place_white_vector);
        } else {
            emptyImage.setImageResource(R.drawable.ic_place_black_vector);
        }

        mRecyclerView = (RecyclerView) findViewById(R.id.currentList);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setOnClickListener(v -> {
            if (LocationUtil.checkGooglePlayServicesAvailability(PlacesActivity.this)) {
                if (Permissions.checkPermission(PlacesActivity.this, Permissions.ACCESS_FINE_LOCATION)) {
                    startActivity(new Intent(PlacesActivity.this, AddPlaceActivity.class)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                } else {
                    Permissions.requestPermission(PlacesActivity.this, 101,
                            Permissions.ACCESS_FINE_LOCATION);
                }
            }
        });
    }

    private void loadPlaces(){
        List<PlaceItem> list = PlacesHelper.getInstance(this).getAll();
        mAdapter = new PlaceRecyclerAdapter(this, list, false);
        reloadView();
        mAdapter.setEventListener(this);
        mRecyclerView.setAdapter(mAdapter);
        ReturnScrollListener scrollListener = new
                ReturnScrollListener.Builder(QuickReturnViewType.FOOTER)
                .footer(mFab)
                .minFooterTranslation(QuickReturnUtils.dp2px(this, 88))
                .isSnappable(true)
                .build();
        mRecyclerView.setOnScrollListener(scrollListener);
    }

    private void reloadView() {
        int size = mAdapter.getItemCount();
        if (size > 0){
            mRecyclerView.setVisibility(View.VISIBLE);
            mEmptyView.setVisibility(View.GONE);
        } else {
            mRecyclerView.setVisibility(View.GONE);
            mEmptyView.setVisibility(View.VISIBLE);
        }
    }

    private void deletePlace(int position){
        long id = mAdapter.getItem(position).getId();
        if (id != 0) {
            DataBase db = new DataBase(this);
            db.open();
            db.deletePlace(id);
            db.close();
            Messages.toast(this, getString(R.string.deleted));
            loadPlaces();
        }
    }

    private void editPlace(int position){
        startActivity(new Intent(this, AddPlaceActivity.class)
                .putExtra(Constants.ITEM_ID_INTENT, mAdapter.getItem(position).getId()));
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
    protected void onResume() {
        super.onResume();
        loadPlaces();
    }

    @Override
    public void onItemClicked(int position, View view) {
        editPlace(position);
    }

    @Override
    public void onItemLongClicked(final int position, View view) {
        final String[] items = {getString(R.string.edit), getString(R.string.delete)};
        Dialogues.showLCAM(this, item -> {
            if (item == 0) {
                editPlace(position);
            }
            if (item == 1) {
                deletePlace(position);
            }
        }, items);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 101:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    startActivity(new Intent(PlacesActivity.this, AddPlaceActivity.class));
                }
                break;
        }
    }
}
