package com.cray.software.justreminder;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.EditText;

import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.views.FloatingEditText;

import java.util.Locale;

public class Help extends AppCompatActivity {

    ColorSetter cSetter;
    WebView helpView;
    FloatingEditText searchEdit;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cSetter = new ColorSetter(Help.this);
        setTheme(cSetter.getStyle());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(cSetter.colorStatus());
        }
        setContentView(R.layout.help_layout);
        setRequestedOrientation(cSetter.getRequestOrientation());

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setLogo(R.drawable.ic_help_white_24dp);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.settings_help_point));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        findViewById(R.id.windowBg).setBackgroundColor(cSetter.getBackgroundStyle());

        SharedPrefs prefs = new SharedPrefs(Help.this);
        boolean isDark = prefs.loadBoolean(Constants.APP_UI_PREFERENCES_USE_DARK_THEME);

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

        searchEdit = (FloatingEditText) findViewById(R.id.searchEdit);
        searchEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                helpView.findAllAsync(s.toString());
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
