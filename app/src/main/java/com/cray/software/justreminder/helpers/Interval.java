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

    public int getCodeFromProgress(int code){
        int progress = 0;
        if (code == 0){
            progress = Intervals.REPEAT_CODE_ONCE;
        } else if(code == 1){
            progress = Intervals.INTERVAL_DAY;
        } else if(code == 2){
            progress = Intervals.INTERVAL_TWO_DAYS;
        } else if(code == 3){
            progress = Intervals.INTERVAL_TREE_DAYS;
        } else if(code == 4){
            progress = Intervals.INTERVAL_FOUR_DAYS;
        } else if(code == 5){
            progress = Intervals.INTERVAL_FIVE_DAYS;
        } else if(code == 6){
            progress = Intervals.INTERVAL_SIX_DAYS;
        } else if(code == 7){
            progress = Intervals.INTERVAL_WEEK;
        } else if(code == 8){
            progress = Intervals.INTERVAL_TWO_WEEKS;
        } else if(code == 9){
            progress = Intervals.INTERVAL_THREE_WEEKS;
        } else if(code == 10){
            progress = Intervals.INTERVAL_MONTH;
        } else if(code == 11){
            progress = Intervals.INTERVAL_TWO_MONTH;
        } else if(code == 12){
            progress = Intervals.INTERVAL_THREE_MONTH;
        } else if(code == 13){
            progress = Intervals.INTERVAL_FOUR_MONTH;
        } else if(code == 14){
            progress = Intervals.INTERVAL_FIVE_MONTH;
        } else if(code == 15){
            progress = Intervals.INTERVAL_HALF_YEAR;
        } else if(code == 16){
            progress = Intervals.INTERVAL_YEAR;
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
        /*if (code == Intervals.REPEAT_CODE_ONCE){
            interval = mContext.getString(R.string.interval_zero);
        } else if(code == Intervals.INTERVAL_DAY){
            interval = mContext.getString(R.string.string_day);
        } else if(code == Intervals.INTERVAL_TWO_DAYS){
            interval = mContext.getString(R.string.string_two_days);
        } else if(code == Intervals.INTERVAL_TREE_DAYS){
            interval = mContext.getString(R.string.string_tree_days);
        } else if(code == Intervals.INTERVAL_FOUR_DAYS){
            interval = mContext.getString(R.string.string_four_days);
        } else if(code == Intervals.INTERVAL_FIVE_DAYS){
            interval = mContext.getString(R.string.string_five_days);
        } else if(code == Intervals.INTERVAL_SIX_DAYS){
            interval = mContext.getString(R.string.string_six_days);
        } else if(code == Intervals.INTERVAL_WEEK){
            interval = mContext.getString(R.string.string_week);
        } else if(code == Intervals.INTERVAL_TWO_WEEKS){
            interval = mContext.getString(R.string.repeat_code_two_weeks);
        } else if(code == Intervals.INTERVAL_THREE_WEEKS){
            interval = mContext.getString(R.string.repeat_code_three_weeks);
        } else if(code == Intervals.INTERVAL_MONTH){
            interval = mContext.getString(R.string.string__month);
        } else if(code == Intervals.INTERVAL_TWO_MONTH){
            interval = mContext.getString(R.string.string_two_month);
        } else if(code == Intervals.INTERVAL_THREE_MONTH){
            interval = mContext.getString(R.string.string_three_month);
        } else if(code == Intervals.INTERVAL_FOUR_MONTH){
            interval = mContext.getString(R.string.string_four_month);
        } else if(code == Intervals.INTERVAL_FIVE_MONTH){
            interval = mContext.getString(R.string.string_five_month);
        } else if(code == Intervals.INTERVAL_HALF_YEAR){
            interval = mContext.getString(R.string.string_six_month);
        } else if(code == Intervals.INTERVAL_YEAR){
            interval = mContext.getString(R.string.string_year);
        }*/
        return interval;
    }

    public String getRepeat(int progress){
        String interval = null;
        if (progress == 0){
            interval = mContext.getString(R.string.interval_once);
        } else if (progress == 1){
            interval = mContext.getString(R.string.interval_day);
        } else if(progress == 2){
            interval = mContext.getString(R.string.interval_two_days);
        } else if(progress == 3){
            interval = mContext.getString(R.string.interval_three_days);
        } else if(progress == 4){
            interval = mContext.getString(R.string.interval_four_days);
        } else if(progress == 5){
            interval = mContext.getString(R.string.interval_five_days);
        } else if(progress == 6){
            interval = mContext.getString(R.string.interval_six_days);
        } else if (progress == 7){
            interval = mContext.getString(R.string.interval_week);
        } else if (progress == 8){
            interval = mContext.getString(R.string.string_every_two_weeks);
        } else if (progress == 9){
            interval = mContext.getString(R.string.string_every_three_weeks);
        } else if (progress == 10){
            interval = mContext.getString(R.string.interval_month);
        } else if (progress == 11){
            interval = mContext.getString(R.string.string_every_two_month);
        } else if (progress == 12){
            interval = mContext.getString(R.string.string_every_three_month);
        } else if (progress == 13){
            interval = mContext.getString(R.string.string_every_four_month);
        } else if (progress == 14){
            interval = mContext.getString(R.string.string_every_five_month);
        } else if (progress == 15){
            interval = mContext.getString(R.string.string_every_six_month);
        } else if (progress == 16){
            interval = mContext.getString(R.string.interval_year);
        }
        return interval;
    }

    public int getTimeRepeatCode(int progress){
        int code = 0;
        if (progress == 0){
            code = Intervals.REPEAT_CODE_ONCE;
        } else if(progress == 1){
            code = Intervals.INTERVAL_MINUTE;
        } else if(progress == 2){
            code = Intervals.INTERVAL_FIVE_MINUTES;
        } else if(progress == 3){
            code = Intervals.INTERVAL_TEN_MINUTES;
        } else if(progress == 4){
            code = Intervals.INTERVAL_HALF_HOUR;
        } else if(progress == 5){
            code = Intervals.INTERVAL_HOUR;
        } else if(progress == 6){
            code = Intervals.INTERVAL_TWO_HOURS;
        } else if(progress == 7){
            code = Intervals.INTERVAL_FIVE_HOURS;
        }
        return code;
    }

    public int getTimeProgressFromCode(int code){
        int progress = 0;
        if (code == Intervals.REPEAT_CODE_ONCE){
            progress = 0;
        } else if(code == Intervals.INTERVAL_MINUTE){
            progress = 1;
        } else if(code == Intervals.INTERVAL_FIVE_MINUTES){
            progress = 2;
        } else if(code == Intervals.INTERVAL_TEN_MINUTES){
            progress = 3;
        } else if(code == Intervals.INTERVAL_HALF_HOUR){
            progress = 4;
        } else if(code == Intervals.INTERVAL_HOUR){
            progress = 5;
        } else if(code == Intervals.INTERVAL_TWO_HOURS){
            progress = 6;
        } else if(code == Intervals.INTERVAL_FIVE_HOURS){
            progress = 7;
        }
        return progress;
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

    public int getAfterTimeCode(int progress){
        int code = 0;
        if (progress == 0){
            code = Intervals.INTERVAL_MINUTE;
        } else if(progress == 1){
            code = Intervals.INTERVAL_TWO_MINUTES;
        } else if(progress == 2){
            code = Intervals.INTERVAL_FIVE_MINUTES;
        } else if(progress == 3){
            code = Intervals.INTERVAL_TEN_MINUTES;
        } else if(progress == 4){
            code = Intervals.INTERVAL_FIFTEEN_MINUTES;
        } else if(progress == 5){
            code = Intervals.INTERVAL_HALF_HOUR;
        } else if(progress == 6){
            code = Intervals.INTERVAL_HOUR;
        } else if(progress == 7){
            code = Intervals.INTERVAL_TWO_HOURS;
        } else if(progress == 8){
            code = Intervals.INTERVAL_FIVE_HOURS;
        }
        return code;
    }

    public int getAfterTimeProgressFromCode(int code){
        int progress = 0;
        if (code == Intervals.INTERVAL_MINUTE){
            progress = 0;
        } else if(code == Intervals.INTERVAL_TWO_MINUTES){
            progress = 1;
        } else if(code == Intervals.INTERVAL_FIVE_MINUTES){
            progress = 2;
        } else if(code == Intervals.INTERVAL_TEN_MINUTES){
            progress = 3;
        } else if(code == Intervals.INTERVAL_FIFTEEN_MINUTES){
            progress = 4;
        } else if(code == Intervals.INTERVAL_HALF_HOUR){
            progress = 5;
        } else if(code == Intervals.INTERVAL_HOUR){
            progress = 6;
        } else if(code == Intervals.INTERVAL_TWO_HOURS){
            progress = 7;
        } else if(code == Intervals.INTERVAL_FIVE_HOURS){
            progress = 8;
        }
        return progress;
    }
}