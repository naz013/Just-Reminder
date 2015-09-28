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
import com.cray.software.justreminder.interfaces.Prefs;
import com.cray.software.justreminder.widgets.UpdatesHelper;

public class FirstDay extends Activity{

    private SharedPrefs sPrefs;
    private ListView musicList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ColorSetter cs = new ColorSetter(FirstDay.this);
        setTheme(cs.getDialogStyle());
        setContentView(R.layout.music_list_dilog);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        TextView dialogTitle = (TextView) findViewById(R.id.dialogTitle);
        dialogTitle.setText(getString(R.string.first_day_dialog_title));

        musicList = (ListView) findViewById(R.id.musicList);
        musicList.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);

        String[] days = new String[]{getString(R.string.start_day_sunday), getString(R.string.start_day_monday)};

        final ArrayAdapter<String> adapter = new ArrayAdapter<>(FirstDay.this,
                android.R.layout.simple_list_item_single_choice, days);
        musicList.setAdapter(adapter);

        sPrefs = new SharedPrefs(FirstDay.this);
        if (sPrefs.isString(Prefs.START_DAY)) {
            int position = sPrefs.loadInt(Prefs.START_DAY);
            musicList.setItemChecked(position, true);
        }

        TextView musicDialogOk = (TextView) findViewById(R.id.musicDialogOk);
        musicDialogOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectedPosition = musicList.getCheckedItemPosition();
                if (selectedPosition != -1) {
                    sPrefs = new SharedPrefs(FirstDay.this);
                    if (selectedPosition == 0) {
                        sPrefs.saveInt(Prefs.START_DAY, 0);
                        new UpdatesHelper(FirstDay.this).updateCalendarWidget();
                        finish();
                    } else {
                        sPrefs.saveInt(Prefs.START_DAY, 1);
                        new UpdatesHelper(FirstDay.this).updateCalendarWidget();
                        finish();
                    }
                } else {
                    Toast.makeText(FirstDay.this, getString(R.string.select_item_warming), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
