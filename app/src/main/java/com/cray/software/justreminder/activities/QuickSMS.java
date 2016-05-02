package com.cray.software.justreminder.activities;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.WindowManager;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.contacts.Contacts;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.modules.Module;
import com.cray.software.justreminder.utils.AssetsUtil;
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

        Typeface typeface = AssetsUtil.getLightTypeface(this);

        messagesList = (ListView) findViewById(R.id.messagesList);

        TextView buttonSend = (TextView) findViewById(R.id.buttonSend);
        buttonSend.setOnClickListener(v -> {
            int position = messagesList.getCheckedItemPosition();
            Cursor c = (Cursor) messagesList.getAdapter().getItem(position);
            if (c != null) {
                String message = c.getString(c.getColumnIndex(Constants.COLUMN_TEXT));
                sendSMS(number, message);
            }
            if (c != null) c.close();
        });
        buttonSend.setTypeface(typeface);

        String name = Contacts.getNameFromNumber(number, QuickSMS.this);

        TextView contactInfo = (TextView) findViewById(R.id.contactInfo);
        contactInfo.setTypeface(typeface);
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
