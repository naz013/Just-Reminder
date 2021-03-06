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

package com.cray.software.justreminder.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.Module;

public class ChangeDialog extends Activity {

    private DialogInterface.OnCancelListener mCancelListener = dialogInterface -> finish();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ColorSetter cs = ColorSetter.getInstance(this);
        setTheme(cs.getDialogStyle());
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(getString(R.string.changes));
        WebView wv = new WebView(this);
        wv.setBackgroundColor(cs.getBackgroundStyle());
        String url = "file:///android_asset/files/change_log.html";
        if (Module.isPro()) url = "file:///android_asset/files/change_log_pro.html";
        wv.loadUrl(url);
        wv.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
        alert.setView(wv);
        alert.setCancelable(true);
        alert.setNegativeButton(getString(R.string.ok), (dialog, id) -> {
            dialog.dismiss();
            finish();
        });
        AlertDialog alertDialog = alert.create();
        alertDialog.setOnCancelListener(mCancelListener);
        alertDialog.show();
    }
}