package com.cray.software.justreminder.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.cray.software.justreminder.R;

import java.util.Locale;

/**
 * Dialog for showing voice commands help.
 */
public class VoiceHelp extends Activity {

    /**
     * Alert dialog field.
     */
    private AlertDialog alertDialog;

    @Override
    protected final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(getString(R.string.help));

        WebView wv = new WebView(this);
        String localeCheck = Locale.getDefault().toString().toLowerCase();
        String url;
        if (localeCheck.startsWith("uk")) {
            url = "file:///android_asset/files/voice_uk.html";
        } else if (localeCheck.startsWith("ru")) {
            url = "file:///android_asset/files/voice_ru.html";
        } else {
            url = "file:///android_asset/files/voice_en.html";
        }
        wv.loadUrl(url);
        wv.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(final WebView view, 
                                                    final String url) {
                view.loadUrl(url);
                return true;
            }
        });

        alert.setView(wv);
        alert.setCancelable(false);
        alert.setNegativeButton(getString(R.string.ok),
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int id) {
                dialog.dismiss();
                finish();
            }
        });
        alertDialog = alert.create();
        alertDialog.show();
    }

    @Override
    public final void onBackPressed() {
        super.onBackPressed();
        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }
        finish();
    }
}
