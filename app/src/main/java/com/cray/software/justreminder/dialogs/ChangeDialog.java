package com.cray.software.justreminder.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.modules.Module;

public class ChangeDialog extends Activity {

    private AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(getString(R.string.changes_settings_title));

        WebView wv = new WebView(this);
        String url = "file:///android_asset/files/change_log.html";
        if (Module.isBeta()) url = "file:///android_asset/files/change_log_beta.html";
        wv.loadUrl(url);
        wv.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });

        alert.setView(wv);
        alert.setCancelable(false);
        alert.setNegativeButton(getString(R.string.button_close), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                finish();
            }
        });
        alertDialog = alert.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (alertDialog != null && alertDialog.isShowing()){
            alertDialog.dismiss();
        }
        finish();
    }
}