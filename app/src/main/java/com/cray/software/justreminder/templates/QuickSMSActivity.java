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

package com.cray.software.justreminder.templates;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.WindowManager;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.contacts.Contacts;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.modules.Module;
import com.cray.software.justreminder.roboto_views.RoboButton;
import com.cray.software.justreminder.roboto_views.RoboTextView;
import com.cray.software.justreminder.utils.SuperUtil;
import com.cray.software.justreminder.utils.ViewUtils;

import java.util.List;

public class QuickSMSActivity extends Activity {

    private RecyclerView messagesList;
    private SelectableRecyclerAdapter mAdapter;
    private String number;
    private ColorSetter cs = new ColorSetter(QuickSMSActivity.this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(cs.getFullscreenStyle());
        runOnUiThread(() -> getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD));
        setContentView(R.layout.quick_message_layout);
        if (Module.isLollipop()) {
            getWindow().setStatusBarColor(ViewUtils.getColor(this, cs.colorPrimaryDark()));
        }
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Intent i = getIntent();
        number = i.getStringExtra(Constants.ITEM_ID_INTENT);
        messagesList = (RecyclerView) findViewById(R.id.messagesList);
        messagesList.setLayoutManager(new LinearLayoutManager(this));
        RoboButton buttonSend = (RoboButton) findViewById(R.id.buttonSend);
        buttonSend.setOnClickListener(v -> {
            startSending();
        });

        String name = Contacts.getNameFromNumber(number, QuickSMSActivity.this);

        RoboTextView contactInfo = (RoboTextView) findViewById(R.id.contactInfo);
        contactInfo.setText(SuperUtil.appendString(name, "\n", number));

        loadTemplates();
        if (mAdapter.getItemCount() > 0) {
            mAdapter.selectItem(0);
        }
    }

    private void startSending() {
        int position = mAdapter.getSelectedPosition();
        TemplateItem item = mAdapter.getItem(position);
        if (item != null) {
            Log.d("TAG", "startSending: " + item.getTitle());
            sendSMS(number, item.getTitle());
        }
        removeFlags();
    }

    private void loadTemplates(){
        List<TemplateItem> list = TemplateHelper.getInstance(this).getAll();
        mAdapter = new SelectableRecyclerAdapter(this, list);
        messagesList.setAdapter(mAdapter);
    }

    public void removeFlags(){
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        finish();
    }

    private void sendSMS(String number, String message){
        String SENT = "SMS_SENT";
        String DELIVERED = "SMS_DELIVERED";

        PendingIntent sentPI = PendingIntent.getBroadcast(QuickSMSActivity.this, 0,
                new Intent(SENT), 0);

        PendingIntent deliveredPI = PendingIntent.getBroadcast(QuickSMSActivity.this,
                0, new Intent(DELIVERED), 0);

        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(number, null, message, sentPI, deliveredPI);
    }

    @Override
    public void onBackPressed() {
        removeFlags();
    }
}
