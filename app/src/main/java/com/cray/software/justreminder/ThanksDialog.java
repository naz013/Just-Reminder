package com.cray.software.justreminder;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.EditText;

import com.cray.software.justreminder.helpers.ColorSetter;

public class ThanksDialog extends AppCompatActivity {

    private ColorSetter cSetter = new ColorSetter(ThanksDialog.this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(cSetter.getStyle());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(cSetter.colorStatus());
        }
        setContentView(R.layout.help_layout);
        setRequestedOrientation(cSetter.getRequestOrientation());

        int code = getIntent().getIntExtra("int", 0);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setLogo(R.drawable.ic_security_white_24dp);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.thanks_settings_title));
        if (code == 1) getSupportActionBar().setTitle(getString(R.string.permissions));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        WebView helpView = (WebView) findViewById(R.id.helpView);
        String url = "file:///android_asset/files/LICENSE.html";
        if (code == 1) url = "file:///android_asset/files/permissions.html";
        helpView.loadUrl(url);

        EditText searchEdit = (EditText) findViewById(R.id.searchEdit);
        searchEdit.setVisibility(View.GONE);
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