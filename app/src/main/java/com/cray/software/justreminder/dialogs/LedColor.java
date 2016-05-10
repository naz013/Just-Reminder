package com.cray.software.justreminder.dialogs;

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

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.LED;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.Messages;
import com.cray.software.justreminder.roboto_views.RoboButton;
import com.cray.software.justreminder.roboto_views.RoboTextView;

public class LedColor extends Activity{

    private ListView musicList;
    private RoboButton musicDialogOk;
    private NotificationManagerCompat mNotifyMgr;
    private NotificationCompat.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ColorSetter cs = new ColorSetter(LedColor.this);
        setTheme(cs.getDialogStyle());
        setContentView(R.layout.music_list_dilog);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        RoboTextView dialogTitle = (RoboTextView) findViewById(R.id.dialogTitle);
        dialogTitle.setText(getString(R.string.led_color));

        musicList = (ListView) findViewById(R.id.musicList);
        musicList.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);

        String[] colors = new String[LED.NUM_OF_LEDS];
        for (int i = 0; i < LED.NUM_OF_LEDS; i++) {
            colors[i] = LED.getTitle(this, i);
        }

        final ArrayAdapter<String> adapter = new ArrayAdapter<>(LedColor.this,
                android.R.layout.simple_list_item_single_choice, colors);
        musicList.setAdapter(adapter);

        musicList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (i != -1) {
                    Messages.toast(LedColor.this, getString(R.string.turn_screen_off_to_see_led_light));
                    showLED(LED.getLED(i));
                }
            }
        });

        musicDialogOk = (RoboButton) findViewById(R.id.musicDialogOk);
        musicDialogOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectedPosition = musicList.getCheckedItemPosition();
                if (selectedPosition != -1) {
                    mNotifyMgr = NotificationManagerCompat.from(LedColor.this);
                    mNotifyMgr.cancel(1);
                    Intent i = new Intent();
                    i.putExtra(Constants.SELECTED_LED_COLOR, selectedPosition);
                    setResult(RESULT_OK, i);
                    finish();
                } else {
                    Messages.toast(LedColor.this, getString(R.string.select_one_of_item));
                }
            }
        });
    }

    private void showLED(int color){
        musicDialogOk.setEnabled(false);
        mNotifyMgr = NotificationManagerCompat.from(LedColor.this);
        mNotifyMgr.cancel(1);
        builder = new NotificationCompat.Builder(LedColor.this);
        builder.setLights(color, 500, 1000);
        builder.setSmallIcon(R.drawable.ic_lightbulb_outline_white_24dp);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mNotifyMgr.notify(1, builder.build());
                musicDialogOk.setEnabled(true);
            }
        }, 2000);
    }
}
