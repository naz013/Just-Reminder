package com.cray.software.justreminder.dialogs.utils;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.interfaces.Language;

import java.util.ArrayList;

public class SelectLocale extends Activity{

    SharedPrefs sPrefs;
    ListView musicList;
    TextView musicDialogOk;
    TextView dialogTitle;
    ColorSetter cs;
    ArrayList<String> names = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH, WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH);
        cs = new ColorSetter(SelectLocale.this);
        setTheme(cs.getDialogStyle());
        setContentView(R.layout.music_list_dilog);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        dialogTitle = (TextView) findViewById(R.id.dialogTitle);
        dialogTitle.setText(getString(R.string.select_language_title));

        sPrefs = new SharedPrefs(SelectLocale.this);

        names.add(getString(R.string.locale_english));
        names.add(getString(R.string.locale_french));
        names.add(getString(R.string.locale_german));
        names.add(getString(R.string.locale_italian));
        names.add(getString(R.string.locale_japanese));
        names.add(getString(R.string.locale_korean));
        names.add(getString(R.string.locale_polish));
        names.add(getString(R.string.locale_russian));
        names.add(getString(R.string.locale_spanish));

        musicList = (ListView) findViewById(R.id.musicList);
        musicList.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);

        ArrayAdapter<String> l_arrayAdapter = new ArrayAdapter<>(SelectLocale.this,
                android.R.layout.simple_list_item_single_choice, names);
        musicList.setAdapter(l_arrayAdapter);

        musicList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String locale = Language.ENGLISH;
                if (position == 0) locale = Language.ENGLISH;
                if (position == 1) locale = Language.FRENCH;
                if (position == 2) locale = Language.GERMAN;
                if (position == 3) locale = Language.ITALIAN;
                if (position == 4) locale = Language.JAPANESE;
                if (position == 5) locale = Language.KOREAN;
                if (position == 6) locale = Language.POLISH;
                if (position == 7) locale = Language.RUSSIAN;
                if (position == 8) locale = Language.SPANISH;
                sPrefs.savePrefs(Constants.APP_UI_PREFERENCES_TTS_LOCALE, locale);
            }
        });

        int position = 1;
        String locale = sPrefs.loadPrefs(Constants.APP_UI_PREFERENCES_TTS_LOCALE);
        if (locale.matches(Language.ENGLISH)) position = 0;
        if (locale.matches(Language.FRENCH)) position = 1;
        if (locale.matches(Language.GERMAN)) position = 2;
        if (locale.matches(Language.ITALIAN)) position = 3;
        if (locale.matches(Language.JAPANESE)) position = 4;
        if (locale.matches(Language.KOREAN)) position = 5;
        if (locale.matches(Language.POLISH)) position = 6;
        if (locale.matches(Language.RUSSIAN)) position = 7;
        if (locale.matches(Language.SPANISH)) position = 8;

        musicList.setItemChecked(position, true);

        musicDialogOk = (TextView) findViewById(R.id.musicDialogOk);
        musicDialogOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(SelectLocale.this, getString(R.string.select_item_warming), Toast.LENGTH_SHORT).show();
    }
}
