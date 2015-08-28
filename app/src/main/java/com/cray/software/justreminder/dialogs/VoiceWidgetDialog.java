package com.cray.software.justreminder.dialogs;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.widget.Toast;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.helpers.Notifier;
import com.cray.software.justreminder.helpers.Recognizer;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.interfaces.Prefs;

import java.util.ArrayList;

public class VoiceWidgetDialog extends Activity {

    public static final int VOICE_RECOGNITION_REQUEST_CODE = 109;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startVoiceRecognitionActivity();
    }

    public void startVoiceRecognitionActivity() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        SharedPrefs sPrefs = new SharedPrefs(VoiceWidgetDialog.this);
        if (!sPrefs.loadBoolean(Prefs.AUTO_LANGUAGE)) {
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, sPrefs.loadPrefs(Prefs.VOICE_LANGUAGE));
        } else intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.voice_say_something));
        try {
            startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
        } catch (ActivityNotFoundException e){
            Toast t = Toast.makeText(getApplicationContext(),
                    getString(R.string.recognizer_not_found_error_message),
                    Toast.LENGTH_SHORT);
            t.show();
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {

            ArrayList matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

            new Recognizer(VoiceWidgetDialog.this).selectTask(matches, true);
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