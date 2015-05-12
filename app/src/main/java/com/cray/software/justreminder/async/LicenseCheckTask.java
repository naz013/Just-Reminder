package com.cray.software.justreminder.async;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.databases.SystemData;
import com.cray.software.justreminder.interfaces.Constants;
import com.google.android.vending.licensing.AESObfuscator;
import com.google.android.vending.licensing.LicenseChecker;
import com.google.android.vending.licensing.LicenseCheckerCallback;
import com.google.android.vending.licensing.ServerManagedPolicy;

import java.io.UnsupportedEncodingException;
import java.util.Random;

public class LicenseCheckTask extends AsyncTask<Void, Void, Void> {

    Context mContext;
    LicenseCheckerCallback mChecker;
    ProgressDialog pd;
    private static final byte[] SALT = new byte[] {92,
            80,
            59,
            88,
            51,
            24,
            98,
            35,
            70,
            7,
            7,
            68,
            73,
            93,
            56,
            68,
            51,
            73,
            76,
            80};
    private static final String BASE64_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAgVPnfP66RH61kCBnssoR+CDRtWv6brLGaJV0PSuDcIe1o/JSuojG+seJFbpsBHONUikm0np+wqzxoZIRy3yiEs9UuenStCqKe+r/AoLCB7Xe5uNOmuZfBx/YhxJ1yOoa3zkMpcOb8RcB18LVTNvEJRRQ5UvICGptkE2h93dlBmthDFmbNdUcG6F6uw3CZtdyrcx8Fz1fegHrTMMHZwAcsHly1QGC2/sWA8h92YnY1VSkhlo9C6HDLQqbGFshtOGLXbn/8LNAMCRKynERpjy8eevo0EdNWKZBdEIm2o7L8MaZxrYzylK6BdzQK4LkOd+EWYRaujdEYOO3ehHHLBOm/QIDAQAB";


    public LicenseCheckTask(Context context, LicenseCheckerCallback licenseCheckerCallback){
        this.mContext = context;
        this.mChecker = licenseCheckerCallback;
        pd = new ProgressDialog(mContext, ProgressDialog.STYLE_SPINNER);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Log.d(Constants.LOG_TAG, "Pre execute");
        pd.setTitle(null);
        pd.setMessage(mContext.getString(R.string.check_license_dialog));
        pd.setCancelable(false);
        pd.setIndeterminate(false);
        pd.show();
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if (pd != null && pd.isShowing()){
            pd.dismiss();
        }
        Log.d(Constants.LOG_TAG, "Post execute");
    }

    @Override
    protected Void doInBackground(Void... params) {
        Log.d(Constants.LOG_TAG, "Back execute");
        String deviceId = Settings.Secure.getString(mContext.getContentResolver(), Settings.Secure.ANDROID_ID);
        SystemData systemData = new SystemData(mContext);
        systemData.open();
        String dialog = randomString(32);
        String license = randomString(32);
        systemData.insertKey(encrypt(deviceId), encrypt(Constants.DEVICE_ID_KEY));
        systemData.insertKey(encrypt(dialog), encrypt(Constants.LICENSE_DIALOG_KEY));
        systemData.insertKey(encrypt(license), encrypt(Constants.LICENSE_KEY));
        Log.d(Constants.LOG_TAG, "License before checking started");
        new LicenseChecker(mContext,
                new ServerManagedPolicy(mContext, new AESObfuscator(SALT, mContext.getPackageName(), deviceId)), BASE64_PUBLIC_KEY);
        Log.d(Constants.LOG_TAG, "License checking started");
        return null;
    }

    private String randomString(int len) {
        StringBuilder sb = new StringBuilder( len );
        for( int i = 0; i < len; i++ ) {
            Random random = new Random();
            String ALPHA = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
            int next = random.nextInt(ALPHA.length());
            sb.append(ALPHA.charAt(next));
        }
        return sb.toString();
    }

    private String encrypt(String string){
        byte[] string_byted = null;
        try {
            string_byted = string.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return Base64.encodeToString(string_byted, Base64.DEFAULT).trim();
    }
}