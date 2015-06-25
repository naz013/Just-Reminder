package com.cray.software.justreminder.dialogs.utils;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.view.View;
import android.view.ViewGroup;
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

public class LedColor extends Activity{

    SharedPrefs sPrefs;
    ListView musicList;
    TextView musicDialogOk;
    TextView dialogTitle;
    ColorSetter cs;
    NotificationManagerCompat mNotifyMgr;
    NotificationCompat.Builder builder;
    int id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cs = new ColorSetter(LedColor.this);
        setTheme(cs.getDialogStyle());
        setContentView(R.layout.music_list_dilog);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        final Intent intent = getIntent();
        id = intent.getIntExtra(Constants.BIRTHDAY_INTENT_ID, 0);

        dialogTitle = (TextView) findViewById(R.id.dialogTitle);
        dialogTitle.setText(getString(R.string.select_led_color_title));

        musicList = (ListView) findViewById(R.id.musicList);
        musicList.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);

        String[] colors = new String[]{getString(R.string.led_color_white),
                getString(R.string.led_color_red),
                getString(R.string.led_color_green),
                getString(R.string.led_color_blue),
                getString(R.string.led_color_orange),
                getString(R.string.led_color_yellow),
                getString(R.string.led_color_pink),
                getString(R.string.led_color_green_light),
                getString(R.string.led_color_blue_light)};

        final ArrayAdapter<String> adapter = new ArrayAdapter<>(LedColor.this,
                android.R.layout.simple_list_item_single_choice, colors);
        musicList.setAdapter(adapter);

        sPrefs = new SharedPrefs(LedColor.this);

        musicList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (i != -1) {
                    Toast.makeText(LedColor.this, getString(R.string.turn_screen_warm), Toast.LENGTH_SHORT).show();
                    builder = new NotificationCompat.Builder(LedColor.this);
                    if (i == 0){
                        showLED(Constants.ColorConstants.COLOR_WHITE);
                    } else if (i == 1){
                        showLED(Constants.ColorConstants.COLOR_RED);
                    } else if (i == 2){
                        showLED(Constants.ColorConstants.COLOR_GREEN);
                    } else if (i == 3){
                        showLED(Constants.ColorConstants.COLOR_BLUE);
                    } else if (i == 4){
                        showLED(Constants.ColorConstants.COLOR_ORANGE);
                    } else if (i == 5){
                        showLED(Constants.ColorConstants.COLOR_YELLOW);
                    } else if (i == 6){
                        showLED(Constants.ColorConstants.COLOR_PINK);
                    } else if (i == 7){
                        showLED(Constants.ColorConstants.COLOR_GREEN_LIGHT);
                    } else if (i == 8){
                        showLED(Constants.ColorConstants.COLOR_BLUE_LIGHT);
                    } else {
                        showLED(Constants.ColorConstants.COLOR_BLUE);
                    }
                }
            }
        });

        musicDialogOk = (TextView) findViewById(R.id.musicDialogOk);
        musicDialogOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectedPosition = musicList.getCheckedItemPosition();
                if (selectedPosition != -1) {
                    if (id != 4) {
                        sPrefs = new SharedPrefs(LedColor.this);
                        String prefs;
                        if (id == 3) prefs = Constants.APP_UI_PREFERENCES_BIRTHDAY_LED_COLOR;
                        else prefs = Constants.APP_UI_PREFERENCES_LED_COLOR;
                        if (selectedPosition == 0) {
                            sPrefs.saveInt(prefs, Constants.ColorConstants.COLOR_WHITE);
                        } else if (selectedPosition == 1) {
                            sPrefs.saveInt(prefs, Constants.ColorConstants.COLOR_RED);
                        } else if (selectedPosition == 2) {
                            sPrefs.saveInt(prefs, Constants.ColorConstants.COLOR_GREEN);
                        } else if (selectedPosition == 3) {
                            sPrefs.saveInt(prefs, Constants.ColorConstants.COLOR_BLUE);
                        } else if (selectedPosition == 4) {
                            sPrefs.saveInt(prefs, Constants.ColorConstants.COLOR_ORANGE);
                        } else if (selectedPosition == 5) {
                            sPrefs.saveInt(prefs, Constants.ColorConstants.COLOR_YELLOW);
                        } else if (selectedPosition == 6) {
                            sPrefs.saveInt(prefs, Constants.ColorConstants.COLOR_PINK);
                        } else if (selectedPosition == 7) {
                            sPrefs.saveInt(prefs, Constants.ColorConstants.COLOR_GREEN_LIGHT);
                        } else if (selectedPosition == 8) {
                            sPrefs.saveInt(prefs, Constants.ColorConstants.COLOR_BLUE_LIGHT);
                        } else {
                            sPrefs.saveInt(prefs, Constants.ColorConstants.COLOR_BLUE);
                        }
                    } else {
                        Intent i = new Intent();
                        i.putExtra(Constants.SELECTED_LED_COLOR, selectedPosition);
                        setResult(RESULT_OK, i);
                    }
                    mNotifyMgr = NotificationManagerCompat.from(LedColor.this);
                    mNotifyMgr.cancel(1);
                    finish();
                } else {
                    Toast.makeText(LedColor.this, getString(R.string.select_item_warming), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showLED(int color){
        musicDialogOk.setEnabled(false);
        mNotifyMgr = NotificationManagerCompat.from(LedColor.this);
        mNotifyMgr.cancel(1);
        builder.setLights(color, 500, 1000);
        mNotifyMgr = NotificationManagerCompat.from(LedColor.this);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mNotifyMgr.notify(1, builder.build());
                musicDialogOk.setEnabled(true);
            }
        }, 3000);
    }
}
