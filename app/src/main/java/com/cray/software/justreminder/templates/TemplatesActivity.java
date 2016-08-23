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

package com.cray.software.justreminder.templates;

import android.content.Intent;
import android.os.Bundle;
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
import com.cray.software.justreminder.enums.QuickReturnViewType;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.Dialogues;
import com.cray.software.justreminder.helpers.Messages;
import com.cray.software.justreminder.interfaces.SimpleListener;
import com.cray.software.justreminder.modules.Module;
import com.cray.software.justreminder.roboto_views.RoboTextView;
import com.cray.software.justreminder.utils.QuickReturnUtils;
import com.cray.software.justreminder.utils.ViewUtils;
import com.cray.software.justreminder.views.ReturnScrollListener;

import java.util.List;

public class TemplatesActivity extends AppCompatActivity implements SimpleListener {

    private RecyclerView mTemplatesList;
    private LinearLayout mEmptyView;
    private FloatingActionButton mFab;
    private List<TemplateItem> mDataList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ColorSetter cs = ColorSetter.getInstance(TemplatesActivity.this);
        setTheme(cs.getStyle());
        if (Module.isLollipop()) {
            getWindow().setStatusBarColor(ViewUtils.getColor(this, cs.colorPrimaryDark()));
        }
        setContentView(R.layout.places_activity_layout);
        setRequestedOrientation(cs.getRequestOrientation());
        findViewById(R.id.windowBackground).setBackgroundColor(cs.getBackgroundStyle());
        initActionBar();
        initEmptyView();
        initTemplatesList();
        initFab();
    }

    private void initActionBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setTitle(getString(R.string.messages));
    }

    private void initEmptyView() {
        mEmptyView = (LinearLayout) findViewById(R.id.emptyItem);
        mEmptyView.setVisibility(View.VISIBLE);
        RoboTextView emptyText = (RoboTextView) findViewById(R.id.emptyText);
        emptyText.setText(R.string.no_messages);
        ImageView emptyImage = (ImageView) findViewById(R.id.emptyImage);
        if (ColorSetter.getInstance(TemplatesActivity.this).isDark()) {
            emptyImage.setImageResource(R.drawable.ic_textsms_white_vector);
        } else {
            emptyImage.setImageResource(R.drawable.ic_textsms_black_vector);
        }
    }

    private void initFab() {
        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setOnClickListener(v -> startActivity(new Intent(TemplatesActivity.this, TemplateManager.class)));
    }

    private void initTemplatesList() {
        mTemplatesList = (RecyclerView) findViewById(R.id.currentList);
        mTemplatesList.setLayoutManager(new LinearLayoutManager(this));
        mTemplatesList.setItemAnimator(new DefaultItemAnimator());
        ReturnScrollListener scrollListener = new
                ReturnScrollListener.Builder(QuickReturnViewType.FOOTER)
                .footer(mFab)
                .minFooterTranslation(QuickReturnUtils.dp2px(this, 88))
                .isSnappable(true)
                .build();
        mTemplatesList.setOnScrollListener(scrollListener);
    }

    private void loadTemplates(){
        mDataList = TemplateHelper.getInstance(this).getAll();
        reloadView();
        TemplatesRecyclerAdapter adapter = new TemplatesRecyclerAdapter(this, mDataList);
        adapter.setEventListener(this);
        mTemplatesList.setAdapter(adapter);
    }

    private void reloadView() {
        int size = mDataList.size();
        if (size > 0){
            mTemplatesList.setVisibility(View.VISIBLE);
            mEmptyView.setVisibility(View.GONE);
        } else {
            mTemplatesList.setVisibility(View.GONE);
            mEmptyView.setVisibility(View.VISIBLE);
        }
    }

    private void editTemplate(int position){
        startActivity(new Intent(this, TemplateManager.class)
                .putExtra(Constants.ITEM_ID_INTENT, mDataList.get(position).getId()));
    }

    private void removeTemplate(int position){
        TemplateHelper.getInstance(this).deleteTemplate(mDataList.get(position).getId());
        Messages.toast(this, getString(R.string.deleted));
        loadTemplates();
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
        loadTemplates();
    }

    @Override
    public void onItemClicked(int position, View view) {
        editTemplate(position);
    }

    @Override
    public void onItemLongClicked(final int position, View view) {
        final String[] items = {getString(R.string.edit), getString(R.string.delete)};
        Dialogues.showLCAM(this, item -> {
            if (item == 0) {
                editTemplate(position);
            }
            if (item == 1) {
                removeTemplate(position);
            }
        }, items);
    }
}
