/**
 * Copyright 2015 Nazar Suhovich
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

package com.cray.software.justreminder.activities;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.view.WindowManager;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.contacts.Contacts;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.modules.Module;
import com.cray.software.justreminder.roboto_views.RoboButton;
import com.cray.software.justreminder.roboto_views.RoboTextView;
import com.cray.software.justreminder.utils.SuperUtil;
import com.cray.software.justreminder.utils.ViewUtils;

public class QuickSMS extends Activity {

    private ListView messagesList;
    private String number;
    private ColorSetter cs = new ColorSetter(QuickSMS.this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(cs.getFullscreenStyle());
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
            }
        });

        setContentView(R.layout.quick_message_layout);

        if (Module.isLollipop()) {
            getWindow().setStatusBarColor(ViewUtils.getColor(this, cs.colorPrimaryDark()));
        }
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Intent i = getIntent();
        number = i.getStringExtra(Constants.ITEM_ID_INTENT);
        messagesList = (ListView) findViewById(R.id.messagesList);

        RoboButton buttonSend = (RoboButton) findViewById(R.id.buttonSend);
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = messagesList.getCheckedItemPosition();
                Cursor c = (Cursor) messagesList.getAdapter().getItem(position);
                if (c != null) {
                    String message = c.getString(c.getColumnIndex(Constants.COLUMN_TEXT));
                    sendSMS(number, message);
                }
                if (c != null) c.close();
            }
        });

        String name = Contacts.getNameFromNumber(number, QuickSMS.this);

        RoboTextView contactInfo = (RoboTextView) findViewById(R.id.contactInfo);
        contactInfo.setText(SuperUtil.appendString(name, "\n", number));

        loadTemplates();
        if (messagesList.getAdapter().getCount() > 0) {
            messagesList.setItemChecked(0, true);
        }
    }

    private void loadTemplates(){
        DataBase db = new DataBase(QuickSMS.this);
        db.open();
        SimpleCursorAdapter simpleCursorAdapter = new SimpleCursorAdapter(
                QuickSMS.this,
                android.R.layout.simple_list_item_single_choice,
                db.queryTemplates(),
                new String[] {Constants.COLUMN_TEXT},
                new int[] { android.R.id.text1 }, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        messagesList.setAdapter(simpleCursorAdapter);
        db.close();
    }

    public void removeFlags(){
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
    }

    private void sendSMS(String number, String message){
        String SENT = "SMS_SENT";
        String DELIVERED = "SMS_DELIVERED";

        PendingIntent sentPI = PendingIntent.getBroadcast(QuickSMS.this, 0,
                new Intent(SENT), 0);

        PendingIntent deliveredPI = PendingIntent.getBroadcast(QuickSMS.this,
                0, new Intent(DELIVERED), 0);

        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(number, null, message, sentPI, deliveredPI);
    }

    @Override
    public void onBackPressed() {
        removeFlags();
    }
}
