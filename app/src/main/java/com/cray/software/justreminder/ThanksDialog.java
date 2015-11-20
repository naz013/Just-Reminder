
package com.cray.software.justreminder;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.EditText;

import com.cray.software.justreminder.helpers.ColorSetter;

/**
 * Show all open source libraries used in project.
 */
public final class ThanksDialog extends AppCompatActivity {

    /**
     * Helper method initialization.
     */
    private ColorSetter cSetter = new ColorSetter(ThanksDialog.this);

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(cSetter.getStyle());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(cSetter.colorPrimaryDark());
        }
        setContentView(R.layout.help_layout);
        setRequestedOrientation(cSetter.getRequestOrientation());

        int code = getIntent().getIntExtra("int", 0);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setLogo(R.drawable.ic_security_white_24dp);
        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setDisplayShowTitleEnabled(true);
            bar.setTitle(getString(R.string.thanks_settings_title));
            if (code == 1) {
                bar.setTitle(getString(R.string.permissions));
            }
            bar.setDisplayHomeAsUpEnabled(true);
            bar.setHomeButtonEnabled(true);
            bar.setDisplayShowHomeEnabled(true);
        }

        WebView helpView = (WebView) findViewById(R.id.helpView);
        String url = "file:///android_asset/files/LICENSE.html";
        if (code == 1) {
            url = "file:///android_asset/files/permissions.html";
        }
        helpView.loadUrl(url);

        EditText searchEdit = (EditText) findViewById(R.id.searchEdit);
        searchEdit.setVisibility(View.GONE);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}