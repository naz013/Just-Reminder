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
import android.widget.ImageButton;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.birthdays.AddBirthdayActivity;
import com.cray.software.justreminder.reminder.AddReminderActivity;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.Messages;
import com.cray.software.justreminder.helpers.SharedPrefs;

/**
 * Select action for calendar view.
 */
public class ActionPickerDialog extends Activity {

    /**
     * Date in milliseconds.
     */
    private long receivedDate;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ColorSetter cs = ColorSetter.getInstance(ActionPickerDialog.this);
        setTheme(cs.getDialogStyle());
        setContentView(R.layout.dialog_action_picker);
        findViewById(R.id.windowBackground).setBackgroundColor(cs.getBackgroundStyle());
        Intent i = getIntent();
        receivedDate = i.getLongExtra("date", 0);
        ImageButton addEvent = (ImageButton) findViewById(R.id.addEvent);
        addEvent.setOnClickListener(v -> {
            startActivity(new Intent(ActionPickerDialog.this, AddReminderActivity.class)
                    .putExtra("date", receivedDate));
            finish();
        });
        ImageButton addBirth = (ImageButton) findViewById(R.id.addBirth);
        addBirth.setOnClickListener(v -> {
            SharedPrefs.getInstance(this).putBoolean(Prefs.BIRTHDAY_REMINDER, true);
            startActivity(new Intent(ActionPickerDialog.this, AddBirthdayActivity.class)
                    .putExtra("date", receivedDate));
            finish();
        });
        addBirth.setOnLongClickListener(v -> {
            Messages.toast(ActionPickerDialog.this, R.string.add_birthday);
            return false;
        });
        addEvent.setOnLongClickListener(v -> {
            Messages.toast(ActionPickerDialog.this, R.string.add_reminder);
            return false;
        });
    }
}
