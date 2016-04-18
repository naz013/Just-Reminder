package com.cray.software.justreminder.feedback;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.modules.Module;
import com.cray.software.justreminder.utils.ViewUtils;

public class SendReportActivity extends AppCompatActivity {

    private WebView mWeb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ColorSetter cSetter = new ColorSetter(this);
        setTheme(cSetter.getStyle());
        if (Module.isLollipop()) {
            getWindow().setStatusBarColor(ViewUtils.getColor(this, cSetter.colorPrimaryDark()));
        }
        setContentView(R.layout.activity_send_report);
        setRequestedOrientation(cSetter.getRequestOrientation());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setTitle(getString(R.string.feedback));
        toolbar.setNavigationIcon(R.drawable.ic_clear_white_24dp);

        mWeb = (WebView) findViewById(R.id.webView);
        mWeb.getSettings().setJavaScriptEnabled(true); // enable javascript
        mWeb.setWebViewClient(new WebViewClient() {
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {

            }
        });
        mWeb.setWebChromeClient(new WebChromeClient());

        String url = "https://docs.google.com/forms/d/1vOCBU-izJBQ8VAsA1zYtfHFxe9Q1-Qm9rp_pYG13B1s/viewform";
        mWeb.loadUrl(url);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_feedback, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int ids = item.getItemId();
        if (ids == R.id.action_refresh) {
            mWeb.reload();
            return true;
        }
        if (ids == R.id.action_forward) {
            if (mWeb.canGoForward()) {
                mWeb.goForward();
            }
            return true;
        }
        if (ids == R.id.action_back) {
            if (mWeb.canGoBack()) {
                mWeb.goBack();
            }
            return true;
        }
        if (ids == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
