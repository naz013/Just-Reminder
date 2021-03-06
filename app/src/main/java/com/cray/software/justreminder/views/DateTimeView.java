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

package com.cray.software.justreminder.views;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.DatePicker;
import android.widget.RelativeLayout;
import android.widget.TimePicker;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.roboto_views.RoboTextView;
import com.cray.software.justreminder.utils.TimeUtil;

import java.util.Calendar;

public class DateTimeView extends RelativeLayout implements
        DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    private RoboTextView date;
    private RoboTextView time;
    private long mills;
    private Context mContext;
    private OnSelectListener listener;
    private AttributeSet attrs;

    public DateTimeView(Context context) {
        super(context);
        init(context, null);
    }

    public DateTimeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public DateTimeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        if (isInEditMode()) return;
        this.attrs = attrs;
        View.inflate(context, R.layout.date_time_view_layout, this);
        setDescendantFocusability(FOCUS_BLOCK_DESCENDANTS);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
        setLayoutParams(params);
        date = (RoboTextView) findViewById(R.id.dateField);
        time = (RoboTextView) findViewById(R.id.timeField);
        if (ColorSetter.getInstance(context).isDark()) {
            date.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_event_white_24dp, 0, 0, 0);
            time.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_access_time_white_24dp, 0, 0, 0);
        } else {
            date.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_event_black_24dp, 0, 0, 0);
            time.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_access_time_black_24dp, 0, 0, 0);
        }
        date.setOnClickListener(v -> dateDialog());
        time.setOnClickListener(v -> timeDialog());

        this.mContext = context;
        updateDateTime(0);
    }

    /**
     * Set DateTime listener.
     * @param listener OnSelectListener.
     */
    public void setListener(OnSelectListener listener) {
        this.listener = listener;
    }

    /**
     * Set date time to view.
     * @param mills DateTime in mills.
     */
    public void setDateTime(long mills){
        this.mills = mills;
        updateDateTime(mills);
    }

    /**
     * Update views for DateTime.
     * @param mills DateTime in mills.
     */
    private void updateDateTime(long mills){
        updateTime(mills);
        updateDate(mills);
    }

    /**
     * Update date view.
     * @param mills date in mills.
     */
    private void updateDate(long mills){
        final Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(mills);
        if (mills == 0) {
            cal.setTimeInMillis(System.currentTimeMillis());
        }
        if (listener != null) {
            listener.onDateSelect(mills, cal.get(Calendar.DAY_OF_MONTH),
                    cal.get(Calendar.MONTH), cal.get(Calendar.YEAR));
        }
        date.setText(TimeUtil.getDate(cal.getTime()));
    }

    /**
     * Update time view.
     * @param mills time in mills.
     */
    private void updateTime(long mills){
        final Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(mills);
        if (mills == 0) {
            cal.setTimeInMillis(System.currentTimeMillis());
        }
        if (listener != null) {
            listener.onTimeSelect(mills, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));
        }
        time.setText(TimeUtil.getTime(cal.getTime(), SharedPrefs.getInstance(mContext).getBoolean(Prefs.IS_24_TIME_FORMAT)));
    }

    /**
     * Show date picker dialog.
     */
    private void dateDialog() {
        final Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(mills);
        if (mills == 0) {
            cal.setTimeInMillis(System.currentTimeMillis());
        }
        int myYear = cal.get(Calendar.YEAR);
        int myMonth = cal.get(Calendar.MONTH);
        int myDay = cal.get(Calendar.DAY_OF_MONTH);
        new DatePickerDialog(mContext, this, myYear, myMonth, myDay).show();
    }

    /**
     * Show time picker dialog.
     */
    private void timeDialog() {
        final Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(mills);
        if (mills == 0) {
            cal.setTimeInMillis(System.currentTimeMillis());
        }
        int myHour = cal.get(Calendar.HOUR_OF_DAY);
        int myMinute = cal.get(Calendar.MINUTE);
        new TimePickerDialog(mContext, this, myHour, myMinute,
                SharedPrefs.getInstance(mContext).getBoolean(Prefs.IS_24_TIME_FORMAT)).show();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        final Calendar cal = Calendar.getInstance();
        cal.set(year, monthOfYear, dayOfMonth);
        if (listener != null) {
            listener.onDateSelect(cal.getTimeInMillis(), dayOfMonth, monthOfYear, year);
        }
        updateDate(cal.getTimeInMillis());
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        final Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
        cal.set(Calendar.MINUTE, minute);
        if (listener != null) {
            listener.onTimeSelect(cal.getTimeInMillis(), hourOfDay, minute);
        }
        updateTime(cal.getTimeInMillis());
    }

    public interface OnSelectListener{
        void onDateSelect(long mills, int day, int month, int year);
        void onTimeSelect(long mills, int hour, int minute);
    }
}
