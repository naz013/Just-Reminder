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

package com.cray.software.justreminder.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.webkit.WebView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.modules.Module;
import com.cray.software.justreminder.roboto_views.RoboEditText;
import com.cray.software.justreminder.utils.ViewUtils;

import java.util.Locale;

public class Help extends AppCompatActivity {

    private WebView helpView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ColorSetter cSetter = new ColorSetter(Help.this);
        setTheme(cSetter.getStyle());
        if (Module.isLollipop()) {
            getWindow().setStatusBarColor(ViewUtils.getColor(this, cSetter.colorPrimaryDark()));
        }
        setContentView(R.layout.help_layout);
        setRequestedOrientation(cSetter.getRequestOrientation());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setLogo(R.drawable.ic_help_white_24dp);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.help));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        findViewById(R.id.windowBg).setBackgroundColor(cSetter.getBackgroundStyle());

        boolean isDark = cSetter.isDark();

        helpView = (WebView) findViewById(R.id.helpView);
        String localeCheck = Locale.getDefault().toString().toLowerCase();
        String url;
        if (localeCheck.startsWith("uk")) {
            if (isDark) url = "file:///android_asset/web_page/index.html";
            else url = "file:///android_asset/web_page/index_light.html";
        } else if (localeCheck.startsWith("ru")) {
            if (isDark) url = "file:///android_asset/web_page/index_ru.html";
            else url = "file:///android_asset/web_page/index_light_ru.html";
        } else {
            if (isDark) url = "file:///android_asset/web_page/index_en.html";
            else url = "file:///android_asset/web_page/index_light_en.html";
        }

        helpView.loadUrl(url);

        RoboEditText searchEdit = (RoboEditText) findViewById(R.id.searchEdit);
        searchEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                helpView.findAll(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
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
}
