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

package com.cray.software.justreminder.apps;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.Module;
import com.cray.software.justreminder.roboto_views.RoboEditText;
import com.cray.software.justreminder.utils.ViewUtils;

import java.util.ArrayList;
import java.util.List;

public class ApplicationActivity extends AppCompatActivity implements LoadListener, RecyclerClickListener {

    private AppsRecyclerAdapter mAdapter;
    private List<ApplicationItem> mData;

    private RoboEditText searchField;
    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ColorSetter cs = ColorSetter.getInstance(this);
        setTheme(cs.getStyle());
        if (Module.isLollipop()) {
            getWindow().setStatusBarColor(ViewUtils.getColor(this, cs.colorPrimaryDark()));
        }
        setContentView(R.layout.activity_application_list);
        setRequestedOrientation(cs.getRequestOrientation());
        initActionBar();
        findViewById(R.id.windowBackground).setBackgroundColor(cs.getBackgroundStyle());
        initSearchView();
        initRecyclerView();
        new AppsAsync(this, this).execute();
    }

    private void initRecyclerView() {
        mRecyclerView = (RecyclerView) findViewById(R.id.contactsList);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setHasFixedSize(true);
    }

    private void initSearchView() {
        searchField = (RoboEditText) findViewById(R.id.searchField);
        searchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterApps(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void filterApps(String q) {
        List<ApplicationItem> res = filter(mData, q);
        mAdapter.animateTo(res);
        mRecyclerView.scrollToPosition(0);
    }

    private List<ApplicationItem> filter(List<ApplicationItem> mData, String q) {
        q = q.toLowerCase();

        List<ApplicationItem> filteredModelList = new ArrayList<>();
        if (q.matches("")) {
            filteredModelList = new ArrayList<>(mData);
        } else {
            filteredModelList.addAll(getFiltered(mData, q));
        }
        return filteredModelList;
    }

    private List<ApplicationItem> getFiltered(List<ApplicationItem> models, String query) {
        List<ApplicationItem> list = new ArrayList<>();
        for (ApplicationItem model : models) {
            final String text = model.getName().toLowerCase();
            if (text.contains(query)) {
                list.add(model);
            }
        }
        return list;
    }

    private void initActionBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
        if (toolbar != null) {
            toolbar.setTitle(getString(R.string.choose_application));
            toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        InputMethodManager imm = (InputMethodManager)getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searchField.getWindowToken(), 0);
    }

    @Override
    public void onLoaded(List<ApplicationItem> list) {
        this.mData = list;
        mAdapter = new AppsRecyclerAdapter(this, mData, this);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onItemClick(int position) {
        Intent intent = new Intent();
        String packageName = mAdapter.getItem(position).getPackageName();
        intent.putExtra(Constants.SELECTED_APPLICATION, packageName);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent();
            setResult(RESULT_CANCELED, intent);
            finish();
        }
        return true;
    }
}
