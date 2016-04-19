package com.cray.software.justreminder.calls;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import com.cray.software.justreminder.activities.QuickSMS;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.helpers.SharedPrefs;

public class CallReceiver extends BroadcastReceiver {

    private Context mContext;
    private String incoming_nr;
    private int prev_state;
    private long startCallTime;

    @Override
    public void onReceive(Context context, Intent intent) {
        TelephonyManager telephony = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE); //TelephonyManager object
        CustomPhoneStateListener customPhoneListener = new CustomPhoneStateListener();
        telephony.listen(customPhoneListener, PhoneStateListener.LISTEN_CALL_STATE); //Register our listener with TelephonyManager
        mContext = context;
    }

    /* Custom PhoneStateListener */
    public class CustomPhoneStateListener extends PhoneStateListener {

        @Override
        public void onCallStateChanged(int state, String incomingNumber){
            SharedPrefs prefs = new SharedPrefs(mContext);
            if (incomingNumber != null && incomingNumber.length() > 0) incoming_nr = incomingNumber;
            else return;

            switch(state){
                case TelephonyManager.CALL_STATE_RINGING:
                    prev_state = state;
                    startCallTime = System.currentTimeMillis();
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    prev_state = state;
                    break;
                case TelephonyManager.CALL_STATE_IDLE:
                    if((prev_state == TelephonyManager.CALL_STATE_OFFHOOK)){
                        prev_state = state;
                        //Answered Call which is ended
                        //Start quick contact reminder window
                        boolean isFollow = prefs.loadBoolean(Prefs.FOLLOW_REMINDER);
                        if (incoming_nr != null && isFollow ) {
                            mContext.startActivity(new Intent(mContext, FollowReminder.class)
                                    .putExtra(Constants.SELECTED_CONTACT_NUMBER, incoming_nr)
                                    .putExtra(Constants.SELECTED_RADIUS, startCallTime)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                                            Intent.FLAG_ACTIVITY_SINGLE_TOP));
                            break;
                        }
                    }
                    if((prev_state == TelephonyManager.CALL_STATE_RINGING)){
                        prev_state = state;
                        //Rejected or Missed call
                        long currTime = System.currentTimeMillis();
                        if (currTime - startCallTime >= 1000 * 10){
                            //missed call
                            //Set missed call reminder
                            if (prefs.loadBoolean(Prefs.MISSED_CALL_REMINDER) &&
                                    incoming_nr != null){
                                DataBase db = new DataBase(mContext);
                                db.open();

                                Cursor c = db.getMissedCall(incoming_nr);
                                MissedCallAlarm alarm = new MissedCallAlarm();
                                if (c != null && c.moveToFirst()){
                                    do {
                                        long id = c.getLong(c.getColumnIndex(Constants.COLUMN_ID));
                                        db.deleteMissedCall(id);
                                        alarm.cancelAlarm(mContext, id);
                                    } while (c.moveToNext());

                                    long id = db.addMissedCall(incoming_nr, currTime);
                                    alarm.setAlarm(mContext, id, incoming_nr, currTime);
                                } else {
                                    long id = db.addMissedCall(incoming_nr, currTime);
                                    alarm.setAlarm(mContext, id, incoming_nr, currTime);
                                }
                                if (c != null) c.close();
                                db.close();
                                break;
                            }
                        } else {
                            //rejected call
                            //Show quick SMS sending window
                            if (incoming_nr != null && prefs.loadBoolean(Prefs.QUICK_SMS)) {
                                DataBase db = new DataBase(mContext);
                                db.open();
                                Cursor c = db.queryTemplates();
                                int size = 0;
                                if (c != null) size = c.getCount();
                                db.close();
                                if (size > 0) {
                                    mContext.startActivity(new Intent(mContext, QuickSMS.class)
                                            .putExtra(Constants.ITEM_ID_INTENT, incoming_nr)
                                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                                }
                                break;
                            }
                        }
                    }
                    break;
            }
        }
    }
}