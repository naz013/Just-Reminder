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

package com.cray.software.justreminder.fragments.helpers;

import android.app.Activity;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.widget.TimePicker;

import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.services.BirthdayAlarm;

import java.util.Calendar;

public class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

    private TimePickedListener mListener;
    private Activity mContext;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        boolean is24 = SharedPrefs.getInstance(mContext).getBoolean(Prefs.IS_24_TIME_FORMAT);
        return new TimePickerDialog(getActivity(), this, hour, minute, is24);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (mContext == null) {
            mContext = activity;
        }
        if (mListener == null) {
            try {
                mListener = (TimePickedListener) activity;
            } catch (ClassCastException e) {
                throw new ClassCastException(activity.toString() + " must implement " + TimePickedListener.class.getName());
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (mContext == null) {
            mContext = (Activity) context;
        }
        if (mListener == null) {
            try {
                mListener = (TimePickedListener) context;
            } catch (ClassCastException e) {
                throw new ClassCastException(context.toString() + " must implement " + TimePickedListener.class.getName());
            }
        }
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, hourOfDay);
        c.set(Calendar.MINUTE, minute);
        SharedPrefs sPrefs = SharedPrefs.getInstance(mContext);
        sPrefs.putInt(Prefs.BIRTHDAY_REMINDER_HOUR, hourOfDay);
        sPrefs.putInt(Prefs.BIRTHDAY_REMINDER_MINUTE, minute);
        new BirthdayAlarm().setAlarm(getActivity());
        mListener.onTimePicked(c);
    }

    public interface TimePickedListener {
        void onTimePicked(Calendar time);
    }
}
