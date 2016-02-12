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

package com.cray.software.justreminder.dialogs;

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