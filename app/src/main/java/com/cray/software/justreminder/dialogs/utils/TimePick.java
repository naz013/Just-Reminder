package com.cray.software.justreminder.dialogs.utils;

import android.app.Activity;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.widget.TimePicker;

import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.services.SetBirthdays;

import java.util.Calendar;

public class TimePick extends DialogFragment implements TimePickerDialog.OnTimeSetListener {
    private TimePickedListener mListener;
    Activity mActivity;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // use the current time as the default values for the picker
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        // create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(getActivity(), this, hour, minute, DateFormat.is24HourFormat(getActivity()));
    }

    @Override
    public void onAttach(Activity activity) {
        // when the fragment is initially shown (i.e. attached to the activity), cast the activity to the callback interface type
        mActivity = activity;
        super.onAttach(activity);
        try {
            mListener = (TimePickedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement " + TimePickedListener.class.getName());
        }
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        // when the time is selected, send it to the activity via its callback interface method
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, hourOfDay);
        c.set(Calendar.MINUTE, minute);
        SharedPrefs sPrefs = new SharedPrefs(mActivity.getApplicationContext());
        sPrefs.saveInt(Constants.APP_UI_PREFERENCES_BIRTHDAY_REMINDER_HOUR, hourOfDay);
        sPrefs.saveInt(Constants.APP_UI_PREFERENCES_BIRTHDAY_REMINDER_MINUTE, minute);
        mActivity.startService(new Intent(mActivity.getApplicationContext(), SetBirthdays.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        mListener.onTimePicked(c);
    }

    public static interface TimePickedListener {
        public void onTimePicked(Calendar time);
    }
}