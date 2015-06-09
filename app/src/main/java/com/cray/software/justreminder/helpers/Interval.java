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

    public String getWeekRepeat(boolean mon, boolean tue, boolean wed, boolean thu, boolean fri, boolean sat, boolean sun){
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

    public int getProgressFromCode(int code){
        int progress = 0;
        if (code == Intervals.REPEAT_CODE_ONCE){
            progress = 0;
        } else if(code == Intervals.INTERVAL_DAY){
            progress = 1;
        } else if(code == Intervals.INTERVAL_TWO_DAYS){
            progress = 2;
        } else if(code == Intervals.INTERVAL_TREE_DAYS){
            progress = 3;
        } else if(code == Intervals.INTERVAL_FOUR_DAYS){
            progress = 4;
        } else if(code == Intervals.INTERVAL_FIVE_DAYS){
            progress = 5;
        } else if(code == Intervals.INTERVAL_SIX_DAYS){
            progress = 6;
        } else if(code == Intervals.INTERVAL_WEEK){
            progress = 7;
        } else if(code == Intervals.INTERVAL_TWO_WEEKS){
            progress = 8;
        } else if(code == Intervals.INTERVAL_THREE_WEEKS){
            progress = 9;
        } else if(code == Intervals.INTERVAL_MONTH){
            progress = 10;
        } else if(code == Intervals.INTERVAL_TWO_MONTH){
            progress = 11;
        } else if(code == Intervals.INTERVAL_THREE_MONTH){
            progress = 12;
        } else if(code == Intervals.INTERVAL_FOUR_MONTH){
            progress = 13;
        } else if(code == Intervals.INTERVAL_FIVE_MONTH){
            progress = 14;
        } else if(code == Intervals.INTERVAL_HALF_YEAR){
            progress = 15;
        }else if(code == Intervals.INTERVAL_YEAR){
            progress = 16;
        }
        return progress;
    }

    public String getInterval(int code){
        String interval;
        if (code > Intervals.REPEAT_CODE_ONCE && code < Intervals.INTERVAL_WEEK){
            interval = "" + code + mContext.getString(R.string.character_d);
        } else if (code == Intervals.INTERVAL_WEEK){
            interval = mContext.getString(R.string.string_week);
        } else if (code > Intervals.INTERVAL_WEEK && code < Intervals.INTERVAL_TWO_WEEKS){
            interval = "" + code + mContext.getString(R.string.character_d);
        } else if (code == Intervals.INTERVAL_TWO_WEEKS){
            interval = mContext.getString(R.string.repeat_code_two_weeks);
        } else if (code > Intervals.INTERVAL_TWO_WEEKS && code < Intervals.INTERVAL_THREE_WEEKS){
            interval = "" + code + mContext.getString(R.string.character_d);
        } else if (code == Intervals.INTERVAL_THREE_WEEKS){
            interval = mContext.getString(R.string.repeat_code_three_weeks);
        } else if (code > Intervals.INTERVAL_THREE_WEEKS && code < Intervals.INTERVAL_MONTH){
            interval = "" + code + mContext.getString(R.string.character_d);
        } else if (code == Intervals.INTERVAL_MONTH){
            interval = mContext.getString(R.string.string__month);
        } else if (code > Intervals.INTERVAL_MONTH && code < Intervals.INTERVAL_TWO_MONTH){
            interval = "" + code + mContext.getString(R.string.character_d);
        } else if (code == Intervals.INTERVAL_TWO_MONTH){
            interval = mContext.getString(R.string.string_two_month);
        } else if (code > Intervals.INTERVAL_TWO_MONTH && code < Intervals.INTERVAL_THREE_MONTH){
            interval = "" + code + mContext.getString(R.string.character_d);
        } else if (code == Intervals.INTERVAL_THREE_MONTH){
            interval = mContext.getString(R.string.string_three_month);
        } else if (code > Intervals.INTERVAL_THREE_MONTH && code < Intervals.INTERVAL_FOUR_MONTH){
            interval = "" + code + mContext.getString(R.string.character_d);
        } else if (code == Intervals.INTERVAL_FOUR_MONTH){
            interval = mContext.getString(R.string.string_four_month);
        } else if (code > Intervals.INTERVAL_FOUR_MONTH && code < Intervals.INTERVAL_FIVE_MONTH){
            interval = "" + code + mContext.getString(R.string.character_d);
        } else if (code == Intervals.INTERVAL_FIVE_MONTH){
            interval = mContext.getString(R.string.string_five_month);
        } else if (code > Intervals.INTERVAL_FIVE_MONTH && code < Intervals.INTERVAL_HALF_YEAR){
            interval = "" + code + mContext.getString(R.string.character_d);
        } else if (code == Intervals.INTERVAL_HALF_YEAR){
            interval = mContext.getString(R.string.string_six_month);
        } else if (code > Intervals.INTERVAL_HALF_YEAR && code < Intervals.INTERVAL_YEAR){
            interval = "" + code + mContext.getString(R.string.character_d);
        } else if(code == Intervals.INTERVAL_YEAR){
            interval = mContext.getString(R.string.string_year);
        } else {
            interval = mContext.getString(R.string.interval_zero);
        }
        return interval;
    }

    public int getRepeatDays(int progress){
        int interval = 0;
        if (progress == 0){
            interval = 0;
        } else if (progress == 1){
            interval = 1;
        } else if(progress == 2){
            interval = 2;
        } else if(progress == 3){
            interval = 3;
        } else if(progress == 4){
            interval = 4;
        } else if(progress == 5){
            interval = 5;
        } else if(progress == 6){
            interval = 6;
        } else if (progress == 7){
            interval = 7;
        } else if (progress == 8){
            interval = 14;
        } else if (progress == 9){
            interval = 21;
        } else if (progress == 10){
            interval = 30;
        } else if (progress == 11){
            interval = 60;
        } else if (progress == 12){
            interval = 90;
        } else if (progress == 13){
            interval = 120;
        } else if (progress == 14){
            interval = 150;
        } else if (progress == 15){
            interval = 180;
        } else if (progress == 16){
            interval = 365;
        }
        return interval;
    }

    public String getTimeInterval(int code){
        String interval = "";
        if (code == Intervals.REPEAT_CODE_ONCE){
            interval = mContext.getString(R.string.interval_zero);
        } else if(code == Intervals.INTERVAL_MINUTE){
            interval = mContext.getString(R.string.string_min);
        } else if(code == Intervals.INTERVAL_FIVE_MINUTES){
            interval = mContext.getString(R.string.string_five_min);
        } else if(code == Intervals.INTERVAL_TEN_MINUTES){
            interval = mContext.getString(R.string.string_tem_min);
        } else if(code == Intervals.INTERVAL_HALF_HOUR){
            interval = mContext.getString(R.string.string_half_hour);
        } else if(code == Intervals.INTERVAL_HOUR){
            interval = mContext.getString(R.string.string_hour);
        } else if(code == Intervals.INTERVAL_TWO_HOURS){
            interval = mContext.getString(R.string.string_two_hours);
        } else if(code == Intervals.INTERVAL_FIVE_HOURS){
            interval = mContext.getString(R.string.string_five_hours);
        }
        return interval;
    }
}