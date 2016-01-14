package com.cray.software.justreminder.helpers;

import android.content.Context;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.Intervals;

import java.util.ArrayList;

/**
 * Helper class.
 */
public class Interval {

    private Context mContext;

    public Interval(Context context){
        this.mContext = context;
    }

    /**
     * Build weekday string by selection.
     * @param mon monday.
     * @param tue tuesday.
     * @param wed wednesday.
     * @param thu thursday.
     * @param fri friday.
     * @param sat saturday.
     * @param sun sunday.
     * @return list of integers for each day of week
     */
    public ArrayList<Integer> getWeekRepeat(boolean mon, boolean tue, boolean wed, boolean thu, boolean fri,
                                   boolean sat, boolean sun){
        ArrayList<Integer> sb = new ArrayList<>();
        if (mon) sb.add(Constants.DAY_CHECKED);
        else sb.add(Constants.DAY_UNCHECKED);
        if (tue) sb.add(Constants.DAY_CHECKED);
        else sb.add(Constants.DAY_UNCHECKED);
        if (wed) sb.add(Constants.DAY_CHECKED);
        else sb.add(Constants.DAY_UNCHECKED);
        if (thu) sb.add(Constants.DAY_CHECKED);
        else sb.add(Constants.DAY_UNCHECKED);
        if (fri) sb.add(Constants.DAY_CHECKED);
        else sb.add(Constants.DAY_UNCHECKED);
        if (sat) sb.add(Constants.DAY_CHECKED);
        else sb.add(Constants.DAY_UNCHECKED);
        if (sun) sb.add(Constants.DAY_CHECKED);
        else sb.add(Constants.DAY_UNCHECKED);
        return sb;
    }

    public boolean isWeekday(ArrayList<Integer> weekday) {
        boolean is = false;
        for (int day : weekday) {
            if (day == Constants.DAY_CHECKED) {
                is = true;
                break;
            }
        }
        return is;
    }

    /**
     * Get repeat interval string by repeat code.
     * @param code repeat code.
     * @return String that represent repeat interval for reminder
     */
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

    /**
     * Get repeat interval string for time by repeat code.
     * @param code repeat code.
     * @return String that represent time interval for reminder (TIME)
     */
    public String getTimeInterval(int code){
        if (code == 0) return mContext.getString(R.string.interval_zero);
        else return code + mContext.getString(R.string.string_minute);
    }
}