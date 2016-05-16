/*
 * Copyright 2015 Nazar Suhovich
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

package com.cray.software.justreminder.utils;

import android.content.Context;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.Intervals;

import java.util.ArrayList;

/**
 * Helper class.
 */
public class IntervalUtil {

    public IntervalUtil(){
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
    public static ArrayList<Integer> getWeekRepeat(boolean mon, boolean tue, boolean wed, boolean thu, boolean fri,
                                   boolean sat, boolean sun){
        ArrayList<Integer> sb = new ArrayList<>(7);
        sb.add(0, sun ? 1 : 0);
        sb.add(1, mon ? 1 : 0);
        sb.add(2, tue ? 1 : 0);
        sb.add(3, wed ? 1 : 0);
        sb.add(4, thu ? 1 : 0);
        sb.add(5, fri ? 1 : 0);
        sb.add(6, sat ? 1 : 0);
        return sb;
    }

    /**
     * Check if one of weekdays is selected.
     * @param weekday list of weekdays.
     * @return Boolean
     */
    public static boolean isWeekday(ArrayList<Integer> weekday) {
        for (int day : weekday) {
            if (day == Constants.DAY_CHECKED) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get repeat interval string by repeat code.
     * @param code repeat code.
     * @return String that represent repeat interval for reminder
     */
    public static String getInterval(Context mContext, long code){
        long minute = 1000 * 60;
        long day = minute * 60 * 24;
        long tmp = code / minute;
        String interval;
        if (tmp > 1000) {
            code /= day;
            if (code == Intervals.REPEAT_CODE_ONCE) {
                interval = "0";
            } else if (code == Intervals.INTERVAL_WEEK) {
                interval = String.format(mContext.getString(R.string.xW), 1);
            } else if (code == Intervals.INTERVAL_TWO_WEEKS) {
                interval = String.format(mContext.getString(R.string.xW), 2);
            } else if (code == Intervals.INTERVAL_THREE_WEEKS) {
                interval = String.format(mContext.getString(R.string.xW), 3);
            } else if (code == Intervals.INTERVAL_FOUR_WEEKS) {
                interval = String.format(mContext.getString(R.string.xW), 4);
            } else {
                interval = String.format(mContext.getString(R.string.xD), code);
            }
        } else {
            if (tmp == 0) return "0";
            else return String.format(mContext.getString(R.string.x_min), tmp);
        }
        return interval;
    }
}