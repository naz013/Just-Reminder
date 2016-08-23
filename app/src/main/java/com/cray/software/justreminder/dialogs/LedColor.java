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

public class LedColor extends Activity {

    private ListView mColorsList;
    private RoboButton mButtonOk;
    private NotificationManagerCompat mNotifyMgr;
    private NotificationCompat.Builder mBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ColorSetter cs = ColorSetter.getInstance(LedColor.this);
        setTheme(cs.getDialogStyle());
        setContentView(R.layout.music_list_dilog);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        findViewById(R.id.windowBackground).setBackgroundColor(cs.getBackgroundStyle());
        RoboTextView dialogTitle = (RoboTextView) findViewById(R.id.dialogTitle);
        dialogTitle.setText(getString(R.string.led_color));
        initColorsList();
        initOkButton();
        loadDataToList();
    }

    private void loadDataToList() {
        String[] colors = new String[LED.NUM_OF_LEDS];
        for (int i = 0; i < LED.NUM_OF_LEDS; i++) {
            colors[i] = LED.getTitle(this, i);
        }
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(LedColor.this,
                android.R.layout.simple_list_item_single_choice, colors);
        mColorsList.setAdapter(adapter);
    }

    private void initOkButton() {
        mButtonOk = (RoboButton) findViewById(R.id.musicDialogOk);
        mButtonOk.setOnClickListener(v -> {
            int selectedPosition = mColorsList.getCheckedItemPosition();
            if (selectedPosition != -1) {
                cancelNotification();
                sendResult(selectedPosition);
            } else {
                showToast(getString(R.string.select_one_of_item));
            }
        });
    }

    private void showToast(String s) {
        Messages.toast(LedColor.this, s);
    }

    private void sendResult(int selectedPosition) {
        Intent intent = new Intent();
        intent.putExtra(Constants.SELECTED_LED_COLOR, selectedPosition);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void cancelNotification() {
        mNotifyMgr = NotificationManagerCompat.from(LedColor.this);
        mNotifyMgr.cancel(1);
    }

    private void initColorsList() {
        mColorsList = (ListView) findViewById(R.id.musicList);
        mColorsList.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        mColorsList.setOnItemClickListener((adapterView, view, i, l) -> {
            if (i != -1) {
                selectColor(i);
            }
        });
    }

    private void selectColor(int i) {
        showToast(getString(R.string.turn_screen_off_to_see_led_light));
        showLED(LED.getLED(i));
    }

    private void showLED(int color){
        mButtonOk.setEnabled(false);
        cancelNotification();
        mBuilder = new NotificationCompat.Builder(LedColor.this);
        mBuilder.setLights(color, 500, 1000);
        mBuilder.setSmallIcon(R.drawable.ic_lightbulb_outline_white_24dp);
        new Handler().postDelayed(() -> {
            mNotifyMgr.notify(1, mBuilder.build());
            mButtonOk.setEnabled(true);
        }, 2000);
    }
}
