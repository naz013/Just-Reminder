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

package com.cray.software.justreminder.theme;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.modules.Module;
import com.cray.software.justreminder.roboto_views.RoboRadioButton;
import com.cray.software.justreminder.roboto_views.RoboTextView;
import com.cray.software.justreminder.utils.ViewUtils;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainImageActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {
    public static final String DEFAULT_PHOTO = "https://hd.unsplash.com/photo-1460500063983-994d4c27756c";
    private static final String NONE_PHOTO = "";
    private static final String TAG = "MainImageActivity";
    private static final int START_SIZE = 50;

    private LinearLayout emptyItem;
    private RadioGroup selectGroup;
    private RecyclerView imagesList;
    private ImagesRecyclerAdapter mAdapter;

    private List<ImageItem> mPhotoList = new ArrayList<>();
    private int mPointer;

    private int position = -1;
    private RecyclerView.OnScrollListener mOnScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            GridLayoutManager layoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
            int visiblePosition = layoutManager.findLastVisibleItemPosition();
            int count = mAdapter.getItemCount();
            if (visiblePosition >= count - 10 && mPointer < mPhotoList.size() -1 && mPointer < count + (START_SIZE / 2) - 1) {
                int endPoint = mPointer + (START_SIZE / 2);
                boolean last = endPoint >= mPhotoList.size();
                if (last) endPoint = mPhotoList.size() - 1;
                List<ImageItem> nextChunk = mPhotoList.subList(mPointer, endPoint);
                mPointer += (START_SIZE / 2);
                if (last) mPointer = mPhotoList.size() - 1;
                mAdapter.addItems(nextChunk);
            }
        }
    };
    private Call<List<ImageItem>> mCall;
    private Callback<List<ImageItem>> mPhotoCallback = new Callback<List<ImageItem>>() {
        @Override
        public void onResponse(Call<List<ImageItem>> call, Response<List<ImageItem>> response) {
            if (response.code() == Api.OK) {
                mPhotoList = new ArrayList<>(response.body());
                if (position != -1) mPhotoList.get(position).setSelected(true);
                loadDataToList();
            }
        }

        @Override
        public void onFailure(Call<List<ImageItem>> call, Throwable t) {

        }
    };
    private SelectListener mListener = new SelectListener() {
        @Override
        public void onImageSelected(boolean b) {
            if (b) {
                selectGroup.clearCheck();
            } else {
                ((RoboRadioButton) findViewById(R.id.defaultCheck)).setChecked(true);
            }
        }

        @Override
        public void deselectOverItem(int position) {
            mPhotoList.get(position).setSelected(false);
        }
    };

    private void loadDataToList() {
        mPointer = START_SIZE - 1;
        mAdapter = new ImagesRecyclerAdapter(this, mPhotoList.subList(0, mPointer), mListener);
        mAdapter.setPrevSelected(position);
        imagesList.setAdapter(mAdapter);
        emptyItem.setVisibility(View.GONE);
        imagesList.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ColorSetter cs = ColorSetter.getInstance(MainImageActivity.this);
        setTheme(cs.getStyle());
        if (Module.isLollipop()) {
            getWindow().setStatusBarColor(ViewUtils.getColor(this, cs.colorPrimaryDark()));
        }
        setContentView(R.layout.activity_main_image_layout);
        setRequestedOrientation(cs.getRequestOrientation());
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setTitle(getString(R.string.main_image));
        findViewById(R.id.windowBackground).setBackgroundColor(cs.getBackgroundStyle());

        selectGroup = (RadioGroup) findViewById(R.id.selectGroup);
        RoboRadioButton defaultCheck = (RoboRadioButton) findViewById(R.id.defaultCheck);
        RoboRadioButton noneCheck = (RoboRadioButton) findViewById(R.id.noneCheck);
        defaultCheck.setOnCheckedChangeListener(this);
        noneCheck.setOnCheckedChangeListener(this);
        position = SharedPrefs.getInstance(this).getInt(Prefs.MAIN_IMAGE_ID);
        String path = SharedPrefs.getInstance(this).getString(Prefs.MAIN_IMAGE_PATH);
        if (position == -1 && path.matches(DEFAULT_PHOTO)) {
            defaultCheck.setChecked(true);
        } else if (position == -1 && path.matches(NONE_PHOTO)) {
            noneCheck.setChecked(true);
        }
        emptyItem = (LinearLayout) findViewById(R.id.emptyItem);
        emptyItem.setVisibility(View.VISIBLE);
        RoboTextView emptyText = (RoboTextView) findViewById(R.id.emptyText);
        emptyText.setText(R.string.no_images);
        ImageView emptyImage = (ImageView) findViewById(R.id.emptyImage);
        if (ColorSetter.getInstance(this).isDark()) {
            emptyImage.setImageResource(R.drawable.ic_broken_image_white);
        } else {
            emptyImage.setImageResource(R.drawable.ic_broken_image);
        }
        initRecyclerView();
    }

    private void initRecyclerView() {
        imagesList = (RecyclerView) findViewById(R.id.imagesList);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3);
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                switch (position % 6) {
                    case 5:
                        return 3;
                    case 3:
                        return 2;
                    default:
                        return 1;
                }
            }
        });
        imagesList.setLayoutManager(gridLayoutManager);
        imagesList.addItemDecoration(new GridMarginDecoration(
                getResources().getDimensionPixelSize(R.dimen.grid_item_spacing)));
        imagesList.setHasFixedSize(true);
        imagesList.setItemAnimator(new DefaultItemAnimator());
        imagesList.setOnScrollListener(mOnScrollListener);
        imagesList.setVisibility(View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCall = RetrofitBuilder.getApi().getAllImages();
        mCall.enqueue(mPhotoCallback);
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
    protected void onPause() {
        super.onPause();
        if (mCall != null && !mCall.isExecuted()) {
            mCall.cancel();
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        switch (compoundButton.getId()) {
            case R.id.defaultCheck:
                if (b) setImageUrl(DEFAULT_PHOTO, 0);
                break;
            case R.id.noneCheck:
                if (b) setImageUrl(NONE_PHOTO, 0);
                break;
        }
    }

    private void setImageUrl(String imageUrl, long id) {
        SharedPrefs.getInstance(this).putString(Prefs.MAIN_IMAGE_PATH, imageUrl);
        SharedPrefs.getInstance(this).putLong(Prefs.MAIN_IMAGE_ID, id);
    }
}