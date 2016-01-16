package com.cray.software.justreminder.activities;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.webkit.WebView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.utils.ViewUtils;
import com.cray.software.justreminder.views.FloatingEditText;

import java.util.Locale;

public class Help extends AppCompatActivity {

    private WebView helpView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ColorSetter cSetter = new ColorSetter(Help.this);
        setTheme(cSetter.getStyle());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ViewUtils.getColor(this, cSetter.colorPrimaryDark()));
        }
        setContentView(R.layout.help_layout);
        setRequestedOrientation(cSetter.getRequestOrientation());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setLogo(R.drawable.ic_help_white_24dp);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.settings_help_point));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        findViewById(R.id.windowBg).setBackgroundColor(cSetter.getBackgroundStyle());

        SharedPrefs prefs = new SharedPrefs(Help.this);
        boolean isDark = prefs.loadBoolean(Prefs.USE_DARK_THEME);

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

        FloatingEditText searchEdit = (FloatingEditText) findViewById(R.id.searchEdit);
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
