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
import android.view.inputmethod.InputMethodManager;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.modules.Module;
import com.cray.software.justreminder.utils.ViewUtils;
import com.cray.software.justreminder.views.FloatingEditText;

import java.util.ArrayList;
import java.util.List;

public class ApplicationActivity extends AppCompatActivity implements LoadListener, RecyclerClickListener {

    private AppsRecyclerAdapter mAdapter;
    private List<AppData> mData;

    private FloatingEditText searchField;
    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ColorSetter cs = new ColorSetter(this);
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
        searchField = (FloatingEditText) findViewById(R.id.searchField);
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
        List<AppData> res = filter(mData, q);
        mAdapter.animateTo(res);
        mRecyclerView.scrollToPosition(0);
    }

    private List<AppData> filter(List<AppData> mData, String q) {
        q = q.toLowerCase();

        List<AppData> filteredModelList = new ArrayList<>();
        if (q.matches("")) {
            filteredModelList = new ArrayList<>(mData);
        } else {
            filteredModelList.addAll(getFiltered(mData, q));
        }
        return filteredModelList;
    }

    private List<AppData> getFiltered(List<AppData> models, String query) {
        List<AppData> list = new ArrayList<>();
        for (AppData model : models) {
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
    public void onLoaded(List<AppData> list) {
        this.mData = list;
        mAdapter = new AppsRecyclerAdapter(this, mData, this);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onItemClick(int position) {
        Intent intent = new Intent();
        String packageName = mData.get(position).getPackageName();
        intent.putExtra(Constants.SELECTED_APPLICATION, packageName);
        setResult(RESULT_OK, intent);
        finish();
    }
}
