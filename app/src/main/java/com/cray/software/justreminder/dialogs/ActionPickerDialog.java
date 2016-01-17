package com.cray.software.justreminder.dialogs;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.activities.AddBirthday;
import com.cray.software.justreminder.activities.QuickAddReminder;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.Messages;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.constants.Prefs;

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

        ColorSetter cs = new ColorSetter(ActionPickerDialog.this);
        setTheme(cs.getDialogStyle());
        setContentView(R.layout.dialog_action_picker);

        Intent i = getIntent();
        receivedDate = i.getLongExtra("date", 0);

        ImageButton addEvent = (ImageButton) findViewById(R.id.addEvent);
        addEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ActionPickerDialog.this, QuickAddReminder.class)
                        .putExtra("date", receivedDate));
                finish();
            }
        });

        ImageButton addBirth = (ImageButton) findViewById(R.id.addBirth);
        addBirth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SharedPrefs(ActionPickerDialog.this).saveBoolean(Prefs.BIRTHDAY_REMINDER, true);
                startActivity(new Intent(ActionPickerDialog.this, AddBirthday.class)
                        .putExtra("date", receivedDate));
                finish();
            }
        });

        addBirth.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Messages.toast(ActionPickerDialog.this, R.string.add_birthday);
                return false;
            }
        });

        addEvent.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Messages.toast(ActionPickerDialog.this, R.string.add_reminder);
                return false;
            }
        });
    }
}
