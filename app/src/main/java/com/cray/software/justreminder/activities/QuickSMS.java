package com.cray.software.justreminder.activities;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.view.WindowManager;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.Contacts;
import com.cray.software.justreminder.helpers.Messages;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.utils.AssetsUtil;
import com.cray.software.justreminder.utils.SuperUtil;

public class QuickSMS extends Activity {

    private DataBase DB;
    private TextView buttonSend;
    private ListView messagesList;
    private SharedPrefs sPrefs;
    private String number;
    private ColorSetter cs = new ColorSetter(QuickSMS.this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(cs.getFullscreenStyle());
        sPrefs = new SharedPrefs(QuickSMS.this);
        runOnUiThread(new Runnable() {
            public void run() {
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
            }
        });

        setContentView(R.layout.quick_message_layout);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(cs.colorPrimaryDark());
        }
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Intent i = getIntent();
        number = i.getStringExtra(Constants.ITEM_ID_INTENT);

        Typeface typeface = AssetsUtil.getLightTypeface(this);

        messagesList = (ListView) findViewById(R.id.messagesList);

        buttonSend = (TextView) findViewById(R.id.buttonSend);
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
        buttonSend.setTypeface(typeface);

        DB = new DataBase(QuickSMS.this);
        sPrefs = new SharedPrefs(QuickSMS.this);

        DB.open();
        String name = Contacts.getContactNameFromNumber(number, QuickSMS.this);

        TextView contactInfo = (TextView) findViewById(R.id.contactInfo);
        contactInfo.setTypeface(typeface);
        contactInfo.setText(SuperUtil.appendString(name, "\n", number));

        loadTemplates();
        if (messagesList.getAdapter().getCount() > 0) {
            messagesList.setItemChecked(0, true);
        }
    }

    private void loadTemplates(){
        DB.open();
        SimpleCursorAdapter simpleCursorAdapter = new SimpleCursorAdapter(
                QuickSMS.this,
                android.R.layout.simple_list_item_single_choice,
                DB.queryTemplates(),
                new String[] {Constants.COLUMN_TEXT},
                new int[] { android.R.id.text1 }, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        messagesList.setAdapter(simpleCursorAdapter);
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

        BroadcastReceiver sentReceiver;
        registerReceiver(sentReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context arg0, Intent arg1) {
                sPrefs = new SharedPrefs(QuickSMS.this);
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        removeFlags();
                        finish();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        buttonSend.setText(getString(R.string.dialog_button_retry));
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        buttonSend.setText(getString(R.string.dialog_button_retry));
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        buttonSend.setText(getString(R.string.dialog_button_retry));
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        buttonSend.setText(getString(R.string.dialog_button_retry));
                        break;

                }
            }
        }, new IntentFilter(SENT));

        // ---when the SMS has been delivered---
        BroadcastReceiver deliveredReceiver;
        registerReceiver( deliveredReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Messages.toast(QuickSMS.this, "SMS delivered");
                        break;
                    case Activity.RESULT_CANCELED:
                        Messages.toast(QuickSMS.this, "SMS not delivered");
                        break;
                }
            }
        }, new IntentFilter(DELIVERED));

        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(number, null, message, sentPI, deliveredPI);
    }

    @Override
    public void onBackPressed() {
        removeFlags();
    }
}