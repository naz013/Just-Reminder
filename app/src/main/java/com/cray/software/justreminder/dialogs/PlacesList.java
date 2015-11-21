package com.cray.software.justreminder.dialogs;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.adapters.PlaceRecyclerAdapter;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.datas.PlaceDataProvider;
import com.cray.software.justreminder.dialogs.utils.NewPlace;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.Messages;
import com.cray.software.justreminder.helpers.Permissions;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.interfaces.Prefs;
import com.cray.software.justreminder.interfaces.QuickReturnRecyclerViewOnScrollListener;
import com.cray.software.justreminder.interfaces.QuickReturnViewType;
import com.cray.software.justreminder.interfaces.SimpleListener;
import com.cray.software.justreminder.utils.LocationUtil;
import com.cray.software.justreminder.utils.QuickReturnUtils;
import com.getbase.floatingactionbutton.AddFloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionButton;

public class PlacesList extends AppCompatActivity implements SimpleListener {

    private RecyclerView listView;
    private LinearLayout emptyItem;
    private ColorSetter cs = new ColorSetter(PlacesList.this);
    private AddFloatingActionButton mFab;

    private PlaceDataProvider provider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(cs.getStyle());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(cs.colorPrimaryDark());
        }
        setContentView(R.layout.places_activity_layout);
        setRequestedOrientation(cs.getRequestOrientation());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationIcon(R.drawable.ic_clear_white_24dp);
        toolbar.setTitle(getString(R.string.settings_places));

        findViewById(R.id.windowBackground).setBackgroundColor(cs.getBackgroundStyle());

        emptyItem = (LinearLayout) findViewById(R.id.emptyItem);
        emptyItem.setVisibility(View.VISIBLE);

        TextView emptyText = (TextView) findViewById(R.id.emptyText);
        emptyText.setText(getString(R.string.empty_places_list_text));

        ImageView emptyImage = (ImageView) findViewById(R.id.emptyImage);
        if (new SharedPrefs(this).loadBoolean(Prefs.USE_DARK_THEME)) {
            emptyImage.setImageResource(R.drawable.place_white);
        } else {
            emptyImage.setImageResource(R.drawable.place);
        }

        listView = (RecyclerView) findViewById(R.id.currentList);

        mFab = new AddFloatingActionButton(PlacesList.this);
        mFab.setSize(FloatingActionButton.SIZE_NORMAL);

        RelativeLayout wrapper = (RelativeLayout) findViewById(R.id.wrapper);
        wrapper.addView(mFab);

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mFab.getLayoutParams();
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (LocationUtil.checkGooglePlayServicesAvailability(PlacesList.this)) {
                    Permissions permissions = new Permissions(PlacesList.this);
                    if (permissions.checkPermission(Permissions.ACCESS_COURSE_LOCATION)) {
                        startActivity(new Intent(PlacesList.this, NewPlace.class));
                    } else {
                        permissions.requestPermission(PlacesList.this, new String[]{Permissions.ACCESS_COURSE_LOCATION,
                                Permissions.ACCESS_FINE_LOCATION}, 101);
                    }
                }
            }
        });
        mFab.setColorNormal(cs.colorAccent());
        mFab.setColorPressed(cs.colorAccent());
    }

    private void loadPlaces(){
        provider = new PlaceDataProvider(this);
        reloadView();
        PlaceRecyclerAdapter adapter = new PlaceRecyclerAdapter(this, provider);
        adapter.setEventListener(this);
        listView.setLayoutManager(new LinearLayoutManager(this));
        listView.setAdapter(adapter);
        listView.setItemAnimator(new DefaultItemAnimator());
        QuickReturnRecyclerViewOnScrollListener scrollListener = new
                QuickReturnRecyclerViewOnScrollListener.Builder(QuickReturnViewType.FOOTER)
                .footer(mFab)
                .minFooterTranslation(QuickReturnUtils.dp2px(this, 88))
                .isSnappable(true)
                .build();
        listView.setOnScrollListener(scrollListener);
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
            DataBase db = new DataBase(this);
            db.open();
            db.deletePlace(id);
            db.close();
            Messages.toast(this, getString(R.string.delete_place_toast));
            loadPlaces();
        }
    }

    private void editPlace(int position){
        startActivity(new Intent(this, NewPlace.class)
                .putExtra(Constants.ITEM_ID_INTENT, provider.getItem(position).getId()));
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
        final CharSequence[] items = {getString(R.string.edit), getString(R.string.delete)};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                dialog.dismiss();
                if (item == 0) {
                    editPlace(position);
                }
                if (item == 1) {
                    deletePlace(position);
                }
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 101:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    startActivity(new Intent(PlacesList.this, NewPlace.class));
                } else {
                    new Permissions(PlacesList.this).showInfo(PlacesList.this, Permissions.READ_CALENDAR);
                }
                break;
        }
    }
}
