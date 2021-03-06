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

package com.cray.software.justreminder.reminder;

import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.adapters.TaskListRecyclerAdapter;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.datas.ShoppingListDataProvider;
import com.cray.software.justreminder.groups.GroupHelper;
import com.cray.software.justreminder.groups.GroupItem;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.Module;
import com.cray.software.justreminder.reminder.json.JShopping;
import com.cray.software.justreminder.roboto_views.RoboCheckBox;
import com.cray.software.justreminder.roboto_views.RoboSwitchCompat;
import com.cray.software.justreminder.roboto_views.RoboTextView;
import com.cray.software.justreminder.utils.TimeUtil;
import com.cray.software.justreminder.utils.ViewUtils;
import com.cray.software.justreminder.views.WrapLayoutManager;

import java.util.List;

public class ShopsPreview extends AppCompatActivity {

    private RoboTextView time;
    private RecyclerView todoList;
    private Toolbar toolbar;
    private CollapsingToolbarLayout toolbarLayout;
    private FloatingActionButton mFab;
    private AppBarLayout appBarLayout;
    private RelativeLayout reminderContainer;
    private RoboSwitchCompat reminderSwitch;

    private ShoppingListDataProvider provider;
    private ColorSetter cSetter;

    private long id;
    private boolean isHidden = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cSetter = ColorSetter.getInstance(this);
        setTheme(cSetter.getStyle());
        if (Module.isLollipop()) {
            getWindow().setStatusBarColor(ViewUtils.getColor(this, cSetter.colorPrimaryDark()));
        }
        setContentView(R.layout.activity_shops_preview);
        setRequestedOrientation(cSetter.getRequestOrientation());

        appBarLayout = (AppBarLayout) findViewById(R.id.app_bar);
        toolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);

        id = getIntent().getLongExtra(Constants.EDIT_ID, 0);

        reminderSwitch = (RoboSwitchCompat) findViewById(R.id.reminderSwitch);
        reminderSwitch.setOnClickListener(v -> {
            Reminder.toggle(id, ShopsPreview.this, null);
            loadUi();
        });
        reminderContainer = (RelativeLayout) findViewById(R.id.reminderContainer);

        time = (RoboTextView) findViewById(R.id.time);
        if (cSetter.isDark()) {
            time.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_alarm_white_24dp, 0, 0, 0);
        } else {
            time.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_alarm_black_24dp, 0, 0, 0);
        }

        reminderContainer.setVisibility(View.GONE);

        RoboCheckBox showHidden = (RoboCheckBox) findViewById(R.id.showHidden);
        showHidden.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isHidden = isChecked;
            loadUi();
        });

        todoList = (RecyclerView) findViewById(R.id.todoList);
        todoList.setLayoutManager(new WrapLayoutManager(this));
        todoList.setNestedScrollingEnabled(false);
        todoList.setHasFixedSize(false);

        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setBackgroundTintList(ViewUtils.getFabState(this, cSetter.colorAccent(), cSetter.colorAccent()));
        mFab.setVisibility(View.GONE);
        mFab.setOnClickListener(v -> {
            if (id != 0) {
                Reminder.edit(id, ShopsPreview.this);
            }
        });

        findViewById(R.id.windowBackground).setBackgroundColor(cSetter.getBackgroundStyle());
        new android.os.Handler().postDelayed(() -> mFab.show(), 500);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUi();
    }

    private void loadUi() {
        ReminderItem item = ReminderHelper.getInstance(this).getReminder(id);
        if (item != null) {
            String title = item.getSummary();
            toolbar.setTitle(title);
            long due = item.getDateTime();
            if (due > 0) {
                time.setText(TimeUtil.getFullDateTime(due, SharedPrefs.getInstance(this).getBoolean(Prefs.IS_24_TIME_FORMAT)));
                reminderContainer.setVisibility(View.VISIBLE);
            } else {
                reminderContainer.setVisibility(View.GONE);
            }

            if (item.getStatus() == 1){
                reminderSwitch.setChecked(false);
            } else {
                reminderSwitch.setChecked(true);
            }
            GroupItem group = GroupHelper.getInstance(this).getGroup(item.getGroupUuId());
            int catColor = 0;
            if (group != null) catColor = group.getColor();
            int mColor = ViewUtils.getColor(this, cSetter.getCategoryColor(catColor));
            toolbar.setBackgroundColor(mColor);
            toolbarLayout.setBackgroundColor(mColor);
            toolbarLayout.setContentScrimColor(mColor);
            appBarLayout.setBackgroundColor(mColor);
            if (Module.isLollipop()) {
                getWindow().setStatusBarColor(cSetter.getNoteDarkColor(catColor));
            }
            mFab.setBackgroundTintList(ViewUtils.getFabState(this, cSetter.colorAccent(catColor),
                    cSetter.colorAccent(catColor)));

            List<JShopping> list = item.getModel().getShoppings();
            provider = new ShoppingListDataProvider(list, isHidden);
            TaskListRecyclerAdapter shoppingAdapter = new TaskListRecyclerAdapter(this, provider, new TaskListRecyclerAdapter.ActionListener() {
                @Override
                public void onItemCheck(int position, boolean isChecked) {
                    Reminder.switchItem(ShopsPreview.this, id, isChecked, provider.getItem(position).getUuId());
                    loadUi();
                }

                @Override
                public void onItemDelete(int position) {
                    Reminder.hideItem(ShopsPreview.this, id, provider.getItem(position).getUuId());
                    loadUi();
                }

                @Override
                public void onItemChange(int position) {
                    Reminder.showItem(ShopsPreview.this, id, provider.getItem(position).getUuId());
                    loadUi();
                }
            });
            todoList.setAdapter(shoppingAdapter);
        } else closeScreen();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_reminder_preview, menu);
        menu.getItem(0).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int ids = item.getItemId();
        if (ids == R.id.action_delete) {
            Reminder.moveToTrash(id, ShopsPreview.this, null);
            closeScreen();
            return true;
        }
        if (ids == android.R.id.home){
            closeScreen();
        }
        return super.onOptionsItemSelected(item);
    }

    private void closeScreen() {
        mFab.hide();
        new android.os.Handler().postDelayed(() -> {
            if (Module.isLollipop()) {
                finishAfterTransition();
            } else {
                finish();
            }
        }, 350);
    }

    @Override
    public void onBackPressed() {
        closeScreen();
    }
}
