package com.cray.software.justreminder.dialogs.utils;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.helpers.CalendarManager;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.interfaces.Constants;

import java.util.ArrayList;

public class SelectCalendar extends Activity{

    SharedPrefs sPrefs;
    ListView musicList;
    TextView musicDialogOk;
    TextView dialogTitle;
    ColorSetter cs;
    ArrayList<String> names = new ArrayList<>();
    ArrayList<String> ids;
    CalendarManager calendar = new CalendarManager(SelectCalendar.this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH, WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH);
        cs = new ColorSetter(SelectCalendar.this);
        setTheme(cs.getDialogStyle());
        setContentView(R.layout.music_list_dilog);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        dialogTitle = (TextView) findViewById(R.id.dialogTitle);
        dialogTitle.setText(getString(R.string.select_calendar_settings_title));

        musicList = (ListView) findViewById(R.id.musicList);
        musicList.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);

        ids = calendar.getCalendars();
        for (String id:ids){
            names.add(calendar.getCalendarName(id));
        }

        ArrayAdapter<String> l_arrayAdapter = new ArrayAdapter<>(SelectCalendar.this,
                android.R.layout.simple_list_item_single_choice, names);
        musicList.setAdapter(l_arrayAdapter);

        musicDialogOk = (TextView) findViewById(R.id.musicDialogOk);
        musicDialogOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectedPosition = musicList.getCheckedItemPosition();
                if (selectedPosition != -1) {
                    sPrefs = new SharedPrefs(SelectCalendar.this);
                    sPrefs.savePrefs(Constants.APP_UI_PREFERENCES_CALENDAR_NAME, names.get(selectedPosition));
                    sPrefs.savePrefs(Constants.APP_UI_PREFERENCES_CALENDAR_ID, ids.get(selectedPosition));
                    finish();
                } else {
                    Toast.makeText(
                            SelectCalendar.this,
                            getString(R.string.select_calendar_warming),
                            Toast.LENGTH_SHORT)
                            .show();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(SelectCalendar.this, getString(R.string.select_item_warming), Toast.LENGTH_SHORT).show();
    }
}
