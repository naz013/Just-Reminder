package com.cray.software.justreminder.dialogs.utils;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.interfaces.Constants;

import java.util.ArrayList;
import java.util.Locale;

public class VoiceLanguage extends Activity{

    ListView musicList;
    TextView musicDialogOk;
    TextView dialogTitle;
    ColorSetter cs;
    int uk = 0;
    int ru = 1;
    int en = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cs = new ColorSetter(VoiceLanguage.this);
        setTheme(cs.getDialogStyle());
        setContentView(R.layout.music_list_dilog);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        final ArrayList<String> contacts = new ArrayList<>();
        contacts.clear();

        final String localeCheck = Locale.getDefault().toString().toLowerCase();
        if (localeCheck.startsWith("uk")) {
            uk = 0;
            ru = 2;
            en = 1;
            contacts.add(getString(R.string.language_ukrainian) + " (" + Constants.LANGUAGE_UK + ")");
            contacts.add(getString(R.string.language_english) + " (" + Constants.LANGUAGE_EN + ")");
            contacts.add(getString(R.string.language_russian) + " (" + Constants.LANGUAGE_RU + ")");
        } else if (localeCheck.startsWith("ru")) {
            uk = 2;
            ru = 0;
            en = 1;
            contacts.add(getString(R.string.language_russian) + " (" + Constants.LANGUAGE_RU + ")");
            contacts.add(getString(R.string.language_english) + " (" + Constants.LANGUAGE_EN + ")");
            contacts.add(getString(R.string.language_ukrainian) + " (" + Constants.LANGUAGE_UK + ")");
        } else {
            uk = 1;
            ru = 2;
            en = 0;
            contacts.add(getString(R.string.language_english) + " (" + Constants.LANGUAGE_EN + ")");
            contacts.add(getString(R.string.language_ukrainian) + " (" + Constants.LANGUAGE_UK + ")");
            contacts.add(getString(R.string.language_russian) + " (" + Constants.LANGUAGE_RU + ")");
        }

        final SharedPrefs prefs = new SharedPrefs(VoiceLanguage.this);
        int i;
        String language = prefs.loadPrefs(Constants.APP_UI_PREFERENCES_VOICE_LANGUAGE);
        if (language.matches(Constants.LANGUAGE_EN)){
            i = en;
        } else if (language.matches(Constants.LANGUAGE_RU)){
            i = ru;
        } else if (language.matches(Constants.LANGUAGE_UK)){
            i = uk;
        } else i = 0;

        musicList = (ListView) findViewById(R.id.musicList);
        musicList.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(VoiceLanguage.this,
                android.R.layout.simple_list_item_single_choice, contacts);
        musicList.setAdapter(adapter);
        musicList.setItemChecked(i, true);

        dialogTitle = (TextView) findViewById(R.id.dialogTitle);
        dialogTitle.setText(getString(R.string.select_language_title));

        musicDialogOk = (TextView) findViewById(R.id.musicDialogOk);
        musicDialogOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selected = musicList.getCheckedItemPosition();
                if (selected != -1) {
                    if (localeCheck.startsWith("uk")) {
                        if (selected == 0) prefs.savePrefs(Constants.APP_UI_PREFERENCES_VOICE_LANGUAGE, Constants.LANGUAGE_UK);
                        if (selected == 1) prefs.savePrefs(Constants.APP_UI_PREFERENCES_VOICE_LANGUAGE, Constants.LANGUAGE_EN);
                        if (selected == 2) prefs.savePrefs(Constants.APP_UI_PREFERENCES_VOICE_LANGUAGE, Constants.LANGUAGE_RU);
                    } else if (localeCheck.startsWith("ru")) {
                        if (selected == 0) prefs.savePrefs(Constants.APP_UI_PREFERENCES_VOICE_LANGUAGE, Constants.LANGUAGE_RU);
                        if (selected == 1) prefs.savePrefs(Constants.APP_UI_PREFERENCES_VOICE_LANGUAGE, Constants.LANGUAGE_EN);
                        if (selected == 2) prefs.savePrefs(Constants.APP_UI_PREFERENCES_VOICE_LANGUAGE, Constants.LANGUAGE_UK);
                    } else {
                        if (selected == 0) prefs.savePrefs(Constants.APP_UI_PREFERENCES_VOICE_LANGUAGE, Constants.LANGUAGE_EN);
                        if (selected == 1) prefs.savePrefs(Constants.APP_UI_PREFERENCES_VOICE_LANGUAGE, Constants.LANGUAGE_UK);
                        if (selected == 2) prefs.savePrefs(Constants.APP_UI_PREFERENCES_VOICE_LANGUAGE, Constants.LANGUAGE_RU);
                    }
                    finish();
                } else {
                    Toast.makeText(VoiceLanguage.this, getString(R.string.select_item_warming), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
