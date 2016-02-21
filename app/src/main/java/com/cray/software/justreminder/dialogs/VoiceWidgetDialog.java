package com.cray.software.justreminder.dialogs;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;

import com.cray.software.justreminder.ReminderApp;
import com.cray.software.justreminder.helpers.Notifier;
import com.cray.software.justreminder.helpers.Recognize;
import com.cray.software.justreminder.utils.LocationUtil;
import com.cray.software.justreminder.utils.SuperUtil;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.ArrayList;

public class VoiceWidgetDialog extends Activity {

    public static final int VOICE_RECOGNITION_REQUEST_CODE = 109;

    private Tracker mTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (LocationUtil.isGooglePlayServicesAvailable(this)) {
            ReminderApp application = (ReminderApp) getApplication();
            mTracker = application.getDefaultTracker();
        }
        startVoiceRecognitionActivity();
    }

    public void startVoiceRecognitionActivity() {
        if (LocationUtil.isGooglePlayServicesAvailable(this)) {
            mTracker.send(new HitBuilders.EventBuilder()
                    .setCategory("Voice control")
                    .setAction("Widget")
                    .setLabel("Widget")
                    .build());
        }
        SuperUtil.startVoiceRecognitionActivity(this, VOICE_RECOGNITION_REQUEST_CODE, false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {

            ArrayList matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

            new Recognize(VoiceWidgetDialog.this).parseResults(matches, true);
            super.onActivityResult(requestCode, resultCode, data);
        }
        new Notifier(VoiceWidgetDialog.this).recreatePermanent();
        finish();
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}