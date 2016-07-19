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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.modules.Module;
import com.cray.software.justreminder.utils.ViewUtils;

public class TemplateManager extends AppCompatActivity {

    private ColorSetter cs = new ColorSetter(TemplateManager.this);
    private EditText placeName;
    private TextView leftCharacters;
    private TemplateItem mItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(cs.getStyle());
        if (Module.isLollipop()) {
            getWindow().setStatusBarColor(ViewUtils.getColor(this, cs.colorPrimaryDark()));
        }
        setContentView(R.layout.new_template_activity_layout);
        setRequestedOrientation(cs.getRequestOrientation());

        Intent intent = getIntent();
        long id = intent.getLongExtra(Constants.ITEM_ID_INTENT, 0);
        mItem = TemplateHelper.getInstance(this).getTemplate(id);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);

        findViewById(R.id.windowBackground).setBackgroundColor(cs.getBackgroundStyle());

        leftCharacters = (TextView) findViewById(R.id.leftCharacters);
        leftCharacters.setText("");

        placeName = (EditText) findViewById(R.id.placeName);
        if (mItem != null) {
            placeName.setText(mItem.getTitle());
        }
        placeName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int length = s.length();
                leftCharacters.setText(String.format(getString(R.string.left_characters_x), (120 - length)));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void addTemplate(){
        String text = placeName.getText().toString().trim();
        if (text.length() == 0) {
            placeName.setError(getString(R.string.must_be_not_empty));
            return;
        }
        if (mItem != null){
            mItem.setDate(System.currentTimeMillis());
            mItem.setTitle(text);
        } else {
            mItem = new TemplateItem(text, 0, System.currentTimeMillis());
        }
        TemplateHelper.getInstance(this).saveTemplate(mItem);
        SharedPrefs.getInstance(this).putBoolean(Prefs.TEMPLATE_CHANGED, true);
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
                addTemplate();
                return true;
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
