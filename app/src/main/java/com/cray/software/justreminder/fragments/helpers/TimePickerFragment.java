package com.cray.software.justreminder.fragments.helpers;

import android.app.Activity;
import android.app.Dialog;
import android.app.TimePickerDialog;
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
    private Activity mActivity;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // use the current time as the default values for the picker
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        boolean is24 = new SharedPrefs(getActivity()).loadBoolean(Prefs.IS_24_TIME_FORMAT);
        // create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(getActivity(), this, hour, minute, is24);
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
        sPrefs.saveInt(Prefs.BIRTHDAY_REMINDER_HOUR, hourOfDay);
        sPrefs.saveInt(Prefs.BIRTHDAY_REMINDER_MINUTE, minute);
        new BirthdayAlarm().setAlarm(getActivity());
        mListener.onTimePicked(c);
    }

    public interface TimePickedListener {
        void onTimePicked(Calendar time);
    }
}
