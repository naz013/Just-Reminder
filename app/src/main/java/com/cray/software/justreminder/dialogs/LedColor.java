/**
 * Copyright 2016 Nazar Suhovich
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cray.software.justreminder.dialogs;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.view.ViewGroup;
import android.widget.AbsListView;
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
        findViewById(R.id.windowBackground).setBackgroundColor(cs.getBackgroundStyle());
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

        musicList.setOnItemClickListener((adapterView, view, i, l) -> {
            if (i != -1) {
                Messages.toast(LedColor.this, getString(R.string.turn_screen_off_to_see_led_light));
                showLED(LED.getLED(i));
            }
        });

        musicDialogOk = (RoboButton) findViewById(R.id.musicDialogOk);
        musicDialogOk.setOnClickListener(v -> {
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
        });
    }

    private void showLED(int color){
        musicDialogOk.setEnabled(false);
        mNotifyMgr = NotificationManagerCompat.from(LedColor.this);
        mNotifyMgr.cancel(1);
        builder = new NotificationCompat.Builder(LedColor.this);
        builder.setLights(color, 500, 1000);
        builder.setSmallIcon(R.drawable.ic_lightbulb_outline_white_24dp);
        new Handler().postDelayed(() -> {
            mNotifyMgr.notify(1, builder.build());
            musicDialogOk.setEnabled(true);
        }, 2000);
    }
}
