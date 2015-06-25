package com.cray.software.justreminder.interfaces;

public class Intervals {
    public static final int REPEAT_CODE_ONCE = 0;
    public static final int INTERVAL_DAY = 1;
    public static final int INTERVAL_WEEK = INTERVAL_DAY * 7;
    public static final int INTERVAL_TWO_WEEKS = INTERVAL_WEEK * 2;
    public static final int INTERVAL_THREE_WEEKS = INTERVAL_WEEK * 3;
    public static final int INTERVAL_MONTH = INTERVAL_DAY * 30;
    public static final int INTERVAL_TWO_MONTH = INTERVAL_MONTH * 2;
    public static final int INTERVAL_THREE_MONTH = INTERVAL_MONTH * 3;
    public static final int INTERVAL_FOUR_MONTH = INTERVAL_MONTH * 4;
    public static final int INTERVAL_FIVE_MONTH = INTERVAL_MONTH * 5;
    public static final int INTERVAL_HALF_YEAR = INTERVAL_MONTH * 6;
    public static final int INTERVAL_YEAR = INTERVAL_DAY * 365;

    public static final int MILLS_INTERVAL_SECOND = 1000;
    public static final int MILLS_INTERVAL_MINUTE = MILLS_INTERVAL_SECOND * 60;
    public static final int MILLS_INTERVAL_HOUR = MILLS_INTERVAL_MINUTE * 60;
    public static final int MILLS_INTERVAL_HALF_DAY = MILLS_INTERVAL_HOUR * 12;
    public static final int MILLS_INTERVAL_DAY = MILLS_INTERVAL_HALF_DAY * 2;

    public static final int INTERVAL_MINUTE = 1;
    public static final int INTERVAL_FIVE_MINUTES = INTERVAL_MINUTE * 5;
    public static final int INTERVAL_TEN_MINUTES = INTERVAL_FIVE_MINUTES * 2;
    public static final int INTERVAL_FIFTEEN_MINUTES = INTERVAL_FIVE_MINUTES * 3;
    public static final int INTERVAL_HALF_HOUR = INTERVAL_FIFTEEN_MINUTES * 2;
    public static final int INTERVAL_HOUR = INTERVAL_HALF_HOUR * 2;
    public static final int INTERVAL_TWO_HOURS = INTERVAL_HOUR * 2;
    public static final int INTERVAL_FIVE_HOURS = INTERVAL_HOUR * 5;
}
