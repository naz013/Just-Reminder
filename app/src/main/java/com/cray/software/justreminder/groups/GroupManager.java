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

package com.cray.software.justreminder.groups;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.roboto_views.RoboEditText;
import com.cray.software.justreminder.utils.ViewUtils;
import com.cray.software.justreminder.views.ColorPickerView;

import org.json.JSONException;

public class GroupManager extends AppCompatActivity implements ColorPickerView.OnColorListener {

    private RoboEditText editField;
    private Toolbar toolbar;

    private int color = 0;
    private GroupItem mItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ColorSetter cs = ColorSetter.getInstance(GroupManager.this);
        setTheme(cs.getStyle());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ViewUtils.getColor(this, cs.colorPrimaryDark()));
        }
        setContentView(R.layout.category_manager_layout);
        setRequestedOrientation(cs.getRequestOrientation());

        Intent intent = getIntent();
        long id = intent.getLongExtra(Constants.ITEM_ID_INTENT, 0);
        String filePath = intent.getStringExtra(Constants.EDIT_PATH);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);

        findViewById(R.id.windowBackground).setBackgroundColor(cs.getBackgroundStyle());

        editField = (RoboEditText) findViewById(R.id.editField);
        if (id != 0) {
            mItem = GroupHelper.getInstance(this).getGroup(id);
        } else if (filePath != null) {
            try {
                mItem = SyncHelper.getGroup(filePath);
                if (mItem == null) finish();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        editField.setText(mItem.getTitle());
        color = mItem.getColor();
        ColorPickerView pickerView = (ColorPickerView) findViewById(R.id.pickerView);
        pickerView.setListener(this);
        pickerView.setSelectedColor(color);
        setColor(color);
    }

    private void saveCategory(){
        String text = editField.getText().toString().trim();
        if (text.length() == 0) {
            editField.setError(getString(R.string.must_be_not_empty));
            return;
        }
        if (mItem == null) {
            mItem = new GroupItem(text, SyncHelper.generateID(), color, 0, System.currentTimeMillis());
        } else {
            mItem.setColor(color);
            mItem.setDateTime(System.currentTimeMillis());
            mItem.setTitle(text);
        }
        GroupHelper.getInstance(this).saveGroup(mItem);
        SharedPrefs.getInstance(this).putBoolean(Prefs.GROUP_CHANGED, true);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.save_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                saveCategory();
                return true;
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setColor(int i){
        color = i;
        ColorSetter cs = ColorSetter.getInstance(GroupManager.this);
        toolbar.setBackgroundColor(ViewUtils.getColor(this, cs.getCategoryColor(i)));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(cs.getNoteDarkColor(i));
        }
    }

    @Override
    public void onColorSelect(int code, @ColorRes int color) {
        setColor(code);
    }
}
