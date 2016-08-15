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
import android.support.annotation.ColorRes;
import android.view.MenuItem;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.views.ColorPickerView;

public class ColorPicker extends Activity implements ColorPickerView.OnColorListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ColorSetter cs = ColorSetter.getInstance(ColorPicker.this);
        setTheme(cs.getDialogStyle());
        setContentView(R.layout.color_picker_layout);
        findViewById(R.id.windowBackground).setBackgroundColor(cs.getBackgroundStyle());
        ColorPickerView pickerView = (ColorPickerView) findViewById(R.id.pickerView);
        pickerView.setListener(this);
//        pickerView.setSelectedColor(0);
    }

    void sendColor(int color) {
        Intent intent = new Intent();
        intent.putExtra(Constants.SELECTED_COLOR, color);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onColorSelect(int code, @ColorRes int color) {
        sendColor(code);
    }
}