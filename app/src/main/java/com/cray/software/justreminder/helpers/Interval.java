package com.cray.software.justreminder.helpers;

import android.content.Context;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.interfaces.Intervals;

public class Interval {
    Context mContext;
    public Interval(Context context){
        this.mContext = context;
    }

    public String getWeekRepeat(boolean mon, boolean tue, boolean wed, boolean thu, boolean fri,
                                boolean sat, boolean sun){
        StringBuilder sb = new StringBuilder();
        if (mon) sb.append(Constants.DAY_CHECKED);
        else sb.append(Constants.DAY_UNCHECKED);
        if (tue) sb.append(Constants.DAY_CHECKED);
        else sb.append(Constants.DAY_UNCHECKED);
        if (wed) sb.append(Constants.DAY_CHECKED);
        else sb.append(Constants.DAY_UNCHECKED);
        if (thu) sb.append(Constants.DAY_CHECKED);
        else sb.append(Constants.DAY_UNCHECKED);
        if (fri) sb.append(Constants.DAY_CHECKED);
        else sb.append(Constants.DAY_UNCHECKED);
        if (sat) sb.append(Constants.DAY_CHECKED);
        else sb.append(Constants.DAY_UNCHECKED);
        if (sun) sb.append(Constants.DAY_CHECKED);
        else sb.append(Constants.DAY_UNCHECKED);
        return sb.toString();
    }

    public String getInterval(int code){
        String interval;
        if (code == Intervals.REPEAT_CODE_ONCE){
            interval = mContext.getString(R.string.interval_zero);
        } else if (code == Intervals.INTERVAL_WEEK){
            interval = mContext.getString(R.string.string_week);
        } else if (code == Intervals.INTERVAL_TWO_WEEKS){
            interval = mContext.getString(R.string.repeat_code_two_weeks);
        } else if (code == Intervals.INTERVAL_THREE_WEEKS){
            interval = mContext.getString(R.string.repeat_code_three_weeks);
        } else if (code == Intervals.INTERVAL_MONTH){
            interval = mContext.getString(R.string.string__month);
        } else if (code == Intervals.INTERVAL_TWO_MONTH){
            interval = mContext.getString(R.string.string_two_month);
        } else if (code == Intervals.INTERVAL_THREE_MONTH){
            interval = mContext.getString(R.string.string_three_month);
        } else if (code == Intervals.INTERVAL_FOUR_MONTH){
            interval = mContext.getString(R.string.string_four_month);
        } else if (code == Intervals.INTERVAL_FIVE_MONTH){
            interval = mContext.getString(R.string.string_five_month);
        } else if (code == Intervals.INTERVAL_HALF_YEAR){
            interval = mContext.getString(R.string.string_six_month);
        } else if(code == Intervals.INTERVAL_YEAR){
            interval = mContext.getString(R.string.string_year);
        } else {
            interval = "" + code + mContext.getString(R.string.character_d);
        }
        return interval;
    }

    public String getTimeInterval(int code){
        if (code == 0) return mContext.getString(R.string.interval_zero);
        else return code + mContext.getString(R.string.string_minute);
    }
}