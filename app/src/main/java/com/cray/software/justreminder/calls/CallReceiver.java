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

package com.cray.software.justreminder.calls;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import com.cray.software.justreminder.templates.QuickSMSActivity;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.templates.TemplateHelper;

public class CallReceiver extends BroadcastReceiver {

    private Context mContext;
    private String mIncomingNumber;
    private int prevState;
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
            SharedPrefs prefs = SharedPrefs.getInstance(mContext);
            if (incomingNumber != null && incomingNumber.length() > 0) mIncomingNumber = incomingNumber;
            else return;

            switch(state){
                case TelephonyManager.CALL_STATE_RINGING:
                    prevState = state;
                    startCallTime = System.currentTimeMillis();
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    prevState = state;
                    break;
                case TelephonyManager.CALL_STATE_IDLE:
                    if((prevState == TelephonyManager.CALL_STATE_OFFHOOK)){
                        prevState = state;
                        //Answered Call which is ended
                        //Start quick contact reminder window
                        boolean isFollow = prefs.getBoolean(Prefs.FOLLOW_REMINDER);
                        if (mIncomingNumber != null && isFollow ) {
                            mContext.startActivity(new Intent(mContext, FollowReminderActivity.class)
                                    .putExtra(Constants.SELECTED_CONTACT_NUMBER, mIncomingNumber)
                                    .putExtra(Constants.SELECTED_RADIUS, startCallTime)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                                            Intent.FLAG_ACTIVITY_SINGLE_TOP));
                            break;
                        }
                    }
                    if((prevState == TelephonyManager.CALL_STATE_RINGING)){
                        prevState = state;
                        //Rejected or Missed call
                        long currTime = System.currentTimeMillis();
                        if (currTime - startCallTime >= 1000 * 10){
                            //missed call
                            //Set missed call reminder
                            if (prefs.getBoolean(Prefs.MISSED_CALL_REMINDER) &&
                                    mIncomingNumber != null){
                                DataBase db = new DataBase(mContext);
                                db.open();

                                Cursor c = db.getMissedCall(mIncomingNumber);
                                MissedCallAlarmReceiver alarm = new MissedCallAlarmReceiver();
                                if (c != null && c.moveToFirst()){
                                    do {
                                        long id = c.getLong(c.getColumnIndex(Constants.COLUMN_ID));
                                        db.deleteMissedCall(id);
                                        alarm.cancelAlarm(mContext, id);
                                    } while (c.moveToNext());

                                    long id = db.addMissedCall(mIncomingNumber, currTime);
                                    alarm.setAlarm(mContext, id, mIncomingNumber, currTime);
                                } else {
                                    long id = db.addMissedCall(mIncomingNumber, currTime);
                                    alarm.setAlarm(mContext, id, mIncomingNumber, currTime);
                                }
                                if (c != null) c.close();
                                db.close();
                                break;
                            }
                        } else {
                            //rejected call
                            //Show quick SMS sending window
                            if (mIncomingNumber != null && prefs.getBoolean(Prefs.QUICK_SMS)) {
                                int size = TemplateHelper.getInstance(mContext).getAll().size();
                                if (size > 0) {
                                    mContext.startActivity(new Intent(mContext, QuickSMSActivity.class)
                                            .putExtra(Constants.ITEM_ID_INTENT, mIncomingNumber)
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