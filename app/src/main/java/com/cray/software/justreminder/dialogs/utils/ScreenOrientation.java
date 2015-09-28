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
import com.cray.software.justreminder.interfaces.Prefs;

public class ScreenOrientation extends Activity{

    private SharedPrefs sPrefs;
    private ListView musicList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ColorSetter cs = new ColorSetter(ScreenOrientation.this);
        setTheme(cs.getDialogStyle());
        setContentView(R.layout.music_list_dilog);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        sPrefs = new SharedPrefs(ScreenOrientation.this);

        TextView dialogTitle = (TextView) findViewById(R.id.dialogTitle);
        dialogTitle.setText(getString(R.string.screen_orientation_title).toUpperCase());

        musicList = (ListView) findViewById(R.id.musicList);
        musicList.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);

        String[] types = new String[]{getString(R.string.screen_auto),
                getString(R.string.screen_portrait),
                getString(R.string.screen_landscape)};

        final ArrayAdapter<String> adapter = new ArrayAdapter<>(ScreenOrientation.this,
                android.R.layout.simple_list_item_single_choice, types);
        musicList.setAdapter(adapter);

        String prefs = sPrefs.loadPrefs(Prefs.SCREEN);
        if (prefs.matches(Constants.SCREEN_AUTO)) {
            musicList.setItemChecked(0, true);
        } else if (prefs.matches(Constants.SCREEN_PORTRAIT)){
            musicList.setItemChecked(1, true);
        } else if (prefs.matches(Constants.SCREEN_LANDSCAPE)){
            musicList.setItemChecked(2, true);
        }

        TextView musicDialogOk = (TextView) findViewById(R.id.musicDialogOk);
        musicDialogOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectedPosition = musicList.getCheckedItemPosition();
                if (selectedPosition != -1) {
                    sPrefs = new SharedPrefs(ScreenOrientation.this);
                    if (selectedPosition == 0) {
                        sPrefs.savePrefs(Prefs.SCREEN, Constants.SCREEN_AUTO);
                    } else if (selectedPosition == 1) {
                        sPrefs.savePrefs(Prefs.SCREEN, Constants.SCREEN_PORTRAIT);
                    } else if (selectedPosition == 2) {
                        sPrefs.savePrefs(Prefs.SCREEN, Constants.SCREEN_LANDSCAPE);
                    }
                    finish();
                } else {
                    Toast.makeText(ScreenOrientation.this, getString(R.string.select_item_warming), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
