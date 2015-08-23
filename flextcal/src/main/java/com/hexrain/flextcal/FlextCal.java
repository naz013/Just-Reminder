package com.hexrain.flextcal;


import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.CardView;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.antonyt.infiniteviewpager.InfinitePagerAdapter;
import com.antonyt.infiniteviewpager.InfiniteViewPager;
import com.flaviofaria.kenburnsview.KenBurnsView;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

import hirondelle.date4j.DateTime;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FlextCal#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FlextCal extends Fragment implements MonthListener{

    /**
     * Weekday conventions
     */
    public static int SUNDAY = 1;
    public static int MONDAY = 2;
    public static int TUESDAY = 3;
    public static int WEDNESDAY = 4;
    public static int THURSDAY = 5;
    public static int FRIDAY = 6;
    public static int SATURDAY = 7;

    /**
     * Flags to display month
     */
    private static final int MONTH_YEAR_FLAG = DateUtils.FORMAT_SHOW_DATE
            | DateUtils.FORMAT_NO_MONTH_DAY | DateUtils.FORMAT_SHOW_YEAR;

    /**
     * To customize the selected background drawable and text color
     */
    public static int selectedBackgroundDrawable = -1;
    public static int selectedTextColor = Color.BLACK;

    public final static int NUMBER_OF_PAGES = 4;

    /**
     * To customize the disabled background drawable and text color
     */
    public static int disabledBackgroundDrawable = -1;
    public static int disabledTextColor = Color.GRAY;

    /**
     * First day of month time
     */
    private Time firstMonthTime = new Time();

    /**
     * Reuse formatter to print "MMMM yyyy" format
     */
    private final StringBuilder monthYearStringBuilder = new StringBuilder(50);
    private Formatter monthYearFormatter = new Formatter(
            monthYearStringBuilder, Locale.getDefault());

    protected int month = -1;
    protected int year = -1;

    protected DateTime minDateTime;
    protected DateTime maxDateTime;

    private boolean sixWeeksInCalendar = true;

    protected boolean enableSwipe = true;
    protected boolean showNavigationArrows = true;
    protected boolean enableClickOnDisabledDates = false;
    protected boolean enableImage = true;
    protected boolean enableAnimation = true;

    protected ArrayList<DateTime> dateInMonthsList;
    protected ArrayList<DateTime> disableDates = new ArrayList<>();
    protected ArrayList<DateTime> selectedDates = new ArrayList<>();

    /**
     * Initial params key
     */
    public final static String MONTH = "month";
    public final static String YEAR = "year";
    public final static String SHOW_NAVIGATION_ARROWS = "showNavigationArrows";
    public final static String DISABLE_DATES = "disableDates";
    public final static String SELECTED_DATES = "selectedDates";
    public final static String MIN_DATE = "minDate";
    public final static String MAX_DATE = "maxDate";
    public final static String ENABLE_SWIPE = "enableSwipe";
    public final static String START_DAY_OF_WEEK = "startDayOfWeek";
    public final static String SIX_WEEKS_IN_CALENDAR = "sixWeeksInCalendar";
    public final static String ENABLE_CLICK_ON_DISABLED_DATES = "enableClickOnDisabledDates";
    public final static String ENABLE_IMAGES = "enableImages";
    public final static String ENABLE_ANIMATIONS = "enableAnimation";

    /**
     * For internal use
     */
    public final static String _MIN_DATE_TIME = "_minDateTime";
    public final static String _MAX_DATE_TIME = "_maxDateTime";
    public final static String _BACKGROUND_FOR_EVENT_ONE_ = "_backgroundForEventOne";
    public final static String _BACKGROUND_FOR_EVENT_TWO_ = "_backgroundForEventTwo";
    public final static String _BACKGROUND_FOR_TODAY_ = "_backgroundForToday";
    public final static String _TEXT_FOR_EVENT_ONE = "_taskForEventOne";
    public final static String _TEXT_FOR_EVENT_TWO = "_taskForEventTwo";

    /**
     * datePagerAdapters hold 4 adapters, meant to be reused
     */
    protected ArrayList<FlextGridAdapter> datePagerAdapters = new ArrayList<>();

    /**
     * Declare views
     */
    private TextView monthTitleTextView;
    private GridView weekdayGridView;
    private InfiniteViewPager dateViewPager;
    private DatePageChangeListener pageChangeListener;
    private ArrayList<DateGridFragment> fragments;
    private KenBurnsView image;

    /**
     * caldroidData belongs to Caldroid
     */
    protected HashMap<String, Object> caldroidData = new HashMap<>();

    /**
     * extraData belongs to client
     */
    protected HashMap<String, Object> extraData = new HashMap<>();

    protected int backgroundForEventOne, backgroundForEventTwo, backgroundForToday = 0;
    protected HashMap<DateTime, String> textMapForEventOne = new HashMap<>();
    protected HashMap<DateTime, String> textMapForEventTwo = new HashMap<>();

    protected int startDayOfWeek = SUNDAY;

    private AdapterView.OnItemClickListener dateItemClickListener;

    /**
     * dateItemLongClickListener is fired when user does a longclick on the date
     * cell
     */
    private AdapterView.OnItemLongClickListener dateItemLongClickListener;

    /**
     * caldroidListener inform library client of the event happens inside
     * Caldroid
     */
    private FlextListener caldroidListener;

    public FlextListener getCaldroidListener() {
        return caldroidListener;
    }

    /**
     * Meant to be subclassed. User who wants to provide custom view, need to
     * provide custom adapter here
     */
    public FlextGridAdapter getNewDatesGridAdapter(int month, int year) {
        return new FlextGridAdapter(getActivity(), month, year,
                getCaldroidData(), extraData);
    }

    /**
     * Meant to be subclassed. User who wants to provide custom view, need to
     * provide custom adapter here
     */
    public WeekdayArrayAdapter getNewWeekdayAdapter() {
        return new WeekdayArrayAdapter(
                getActivity(), android.R.layout.simple_list_item_1,
                getDaysOfWeek());
    }

    /**
     * For client to customize the weekDayGridView
     *
     * @return
     */
    public GridView getWeekdayGridView() {
        return weekdayGridView;
    }

    /**
     * For client to access array of rotating fragments
     */
    public ArrayList<DateGridFragment> getFragments() {
        return fragments;
    }

    /**
     * To let client customize month title textview
     */
    public TextView getMonthTitleTextView() {
        return monthTitleTextView;
    }

    public void setMonthTitleTextView(TextView monthTitleTextView) {
        this.monthTitleTextView = monthTitleTextView;
    }

    /**
     * Get 4 adapters of the date grid views. Useful to set custom data and
     * refresh date grid view
     *
     * @return
     */
    public ArrayList<FlextGridAdapter> getDatePagerAdapters() {
        return datePagerAdapters;
    }

    /**
     * caldroidData return data belong to Caldroid
     *
     * @return
     */
    public HashMap<String, Object> getCaldroidData() {
        caldroidData.clear();
        caldroidData.put(DISABLE_DATES, disableDates);
        caldroidData.put(SELECTED_DATES, selectedDates);
        caldroidData.put(_MIN_DATE_TIME, minDateTime);
        caldroidData.put(_MAX_DATE_TIME, maxDateTime);
        caldroidData.put(START_DAY_OF_WEEK, startDayOfWeek);
        caldroidData.put(SIX_WEEKS_IN_CALENDAR,
                sixWeeksInCalendar);

        // Extra maps
        caldroidData.put(_BACKGROUND_FOR_EVENT_ONE_, backgroundForEventOne);
        caldroidData.put(_BACKGROUND_FOR_EVENT_TWO_, backgroundForEventTwo);
        caldroidData.put(_BACKGROUND_FOR_TODAY_, backgroundForToday);
        caldroidData.put(_TEXT_FOR_EVENT_ONE, textMapForEventOne);
        caldroidData.put(_TEXT_FOR_EVENT_TWO, textMapForEventTwo);

        return caldroidData;
    }

    /**
     * Extra data is data belong to Client
     *
     * @return
     */
    public HashMap<String, Object> getExtraData() {
        return extraData;
    }

    /**
     * Client can set custom data in this HashMap
     *
     * @param extraData
     */
    public void setExtraData(HashMap<String, Object> extraData) {
        this.extraData = extraData;
    }

    /**
     * Set backgroundForDateMap
     */
    public void setBackgroundForToday(int backgroundForToday){
        this.backgroundForToday = backgroundForToday;
    }

    public void setBackgroundResourceForEventOne(
            int backgroundForDateMap) {
        this.backgroundForEventOne = backgroundForDateMap;
    }

    public void setBackgroundResourceForEventTwo(
            int backgroundForDateMap) {
        this.backgroundForEventTwo = backgroundForDateMap;
    }

    public void setTaskForEventOne(String task, Date date){
        this.textMapForEventOne.put(FlextHelper.convertDateToDateTime(date), task);
    }

    public void setTaskForEventOne(HashMap<DateTime, String> map){
        if (map != null) {
            textMapForEventOne.putAll(map);
        }
    }

    public void setTaskForEventTwo(String task, Date date){
        textMapForEventTwo.put(FlextHelper.convertDateToDateTime(date), task);
    }

    public void setTaskForEventTwo(HashMap<DateTime, String> map){
        if (map != null) {
            textMapForEventTwo.putAll(map);
        }
    }

    /**
     * Set caldroid listener when user click on a date
     *
     * @param caldroidListener
     */
    public void setCaldroidListener(FlextListener caldroidListener) {
        this.caldroidListener = caldroidListener;
    }

    /**
     * Get current saved sates of the Caldroid. Useful for handling rotation
     */
    public Bundle getSavedStates() {
        Bundle bundle = new Bundle();
        bundle.putInt(MONTH, month);
        bundle.putInt(YEAR, year);

        if (selectedDates != null && selectedDates.size() > 0) {
            bundle.putStringArrayList(SELECTED_DATES,
                    FlextHelper.convertToStringList(selectedDates));
        }

        if (disableDates != null && disableDates.size() > 0) {
            bundle.putStringArrayList(DISABLE_DATES,
                    FlextHelper.convertToStringList(disableDates));
        }

        if (minDateTime != null) {
            bundle.putString(MIN_DATE, minDateTime.format("YYYY-MM-DD"));
        }

        if (maxDateTime != null) {
            bundle.putString(MAX_DATE, maxDateTime.format("YYYY-MM-DD"));
        }

        bundle.putBoolean(SHOW_NAVIGATION_ARROWS, showNavigationArrows);
        bundle.putBoolean(ENABLE_SWIPE, enableSwipe);
        bundle.putBoolean(ENABLE_IMAGES, enableImage);
        bundle.putBoolean(ENABLE_ANIMATIONS, enableAnimation);
        bundle.putInt(START_DAY_OF_WEEK, startDayOfWeek);
        bundle.putInt(_BACKGROUND_FOR_EVENT_ONE_, backgroundForEventOne);
        bundle.putInt(_BACKGROUND_FOR_EVENT_TWO_, backgroundForEventTwo);
        bundle.putBoolean(SIX_WEEKS_IN_CALENDAR, sixWeeksInCalendar);

        return bundle;
    }

    /**
     * Save current state to bundle outState
     *
     * @param outState
     * @param key
     */
    public void saveStatesToKey(Bundle outState, String key) {
        outState.putBundle(key, getSavedStates());
    }

    /**
     * Restore current states from savedInstanceState
     *
     * @param savedInstanceState
     * @param key
     */
    public void restoreStatesFromKey(Bundle savedInstanceState, String key) {
        if (savedInstanceState != null && savedInstanceState.containsKey(key)) {
            Bundle caldroidSavedState = savedInstanceState.getBundle(key);
            setArguments(caldroidSavedState);
        }
    }

    /**
     * Get current virtual position of the month being viewed
     */
    public int getCurrentVirtualPosition() {
        int currentPage = dateViewPager.getCurrentItem();
        return pageChangeListener.getCurrent(currentPage);
    }

    /**
     * Move calendar to the specified date
     *
     * @param date
     */
    public void moveToDate(Date date) {
        moveToDateTime(FlextHelper.convertDateToDateTime(date));
    }

    /**
     * Move calendar to specified dateTime, with animation
     *
     * @param dateTime
     */
    public void moveToDateTime(DateTime dateTime) {

        DateTime firstOfMonth = new DateTime(year, month, 1, 0, 0, 0, 0);
        DateTime lastOfMonth = firstOfMonth.getEndOfMonth();

        // To create a swipe effect
        // Do nothing if the dateTime is in current month

        // Calendar swipe left when dateTime is in the past
        if (dateTime.lt(firstOfMonth)) {
            // Get next month of dateTime. When swipe left, month will
            // decrease
            DateTime firstDayNextMonth = dateTime.plus(0, 1, 0, 0, 0, 0, 0,
                    DateTime.DayOverflow.LastDay);

            // Refresh adapters
            pageChangeListener.setCurrentDateTime(firstDayNextMonth);
            int currentItem = dateViewPager.getCurrentItem();
            pageChangeListener.refreshAdapters(currentItem);

            // Swipe left
            dateViewPager.setCurrentItem(currentItem - 1);
        }

        // Calendar swipe right when dateTime is in the future
        else if (dateTime.gt(lastOfMonth)) {
            // Get last month of dateTime. When swipe right, the month will
            // increase
            DateTime firstDayLastMonth = dateTime.minus(0, 1, 0, 0, 0, 0, 0,
                    DateTime.DayOverflow.LastDay);

            // Refresh adapters
            pageChangeListener.setCurrentDateTime(firstDayLastMonth);
            int currentItem = dateViewPager.getCurrentItem();
            pageChangeListener.refreshAdapters(currentItem);

            // Swipe right
            dateViewPager.setCurrentItem(currentItem + 1);
        }

    }

    /**
     * Set month and year for the calendar. This is to avoid naive
     * implementation of manipulating month and year. All dates within same
     * month/year give same result
     *
     * @param date
     */
    public void setCalendarDate(Date date) {
        setCalendarDateTime(FlextHelper.convertDateToDateTime(date));
    }

    public void setCalendarDateTime(DateTime dateTime) {
        month = dateTime.getMonth();
        year = dateTime.getYear();

        // Notify listener
        if (caldroidListener != null) {
            caldroidListener.onMonthChanged(month, year);
            this.onMonthSelect(month);
        }

        refreshView();
    }

    /**
     * Set calendar to previous month
     */
    public void prevMonth() {
        dateViewPager.setCurrentItem(pageChangeListener.getCurrentPage() - 1);
    }

    /**
     * Set calendar to next month
     */
    public void nextMonth() {
        dateViewPager.setCurrentItem(pageChangeListener.getCurrentPage() + 1);
    }

    /**
     * Clear all disable dates. Notice this does not refresh the calendar, need
     * to explicitly call refreshView()
     */
    public void clearDisableDates() {
        disableDates.clear();
    }

    /**
     * Set disableDates from ArrayList of Date
     *
     * @param disableDateList
     */
    public void setDisableDates(ArrayList<Date> disableDateList) {
        disableDates.clear();
        if (disableDateList == null || disableDateList.size() == 0) {
            return;
        }

        for (Date date : disableDateList) {
            DateTime dateTime = FlextHelper.convertDateToDateTime(date);
            disableDates.add(dateTime);
        }

    }

    /**
     * Set disableDates from ArrayList of String. By default, the date formatter
     * is yyyy-MM-dd. For e.g 2013-12-24
     *
     * @param disableDateStrings
     */
    public void setDisableDatesFromString(ArrayList<String> disableDateStrings) {
        setDisableDatesFromString(disableDateStrings, null);
    }

    /**
     * Set disableDates from ArrayList of String with custom date format. For
     * example, if the date string is 06-Jan-2013, use date format dd-MMM-yyyy.
     * This method will refresh the calendar, it's not necessary to call
     * refreshView()
     *
     * @param disableDateStrings
     * @param dateFormat
     */
    public void setDisableDatesFromString(ArrayList<String> disableDateStrings,
                                          String dateFormat) {
        disableDates.clear();
        if (disableDateStrings == null) {
            return;
        }

        for (String dateString : disableDateStrings) {
            DateTime dateTime = FlextHelper.getDateTimeFromString(
                    dateString, dateFormat);
            disableDates.add(dateTime);
        }
    }

    /**
     * To clear selectedDates. This method does not refresh view, need to
     * explicitly call refreshView()
     */
    public void clearSelectedDates() {
        selectedDates.clear();
    }

    /**
     * Select the dates from fromDate to toDate. By default the background color
     * is holo_blue_light, and the text color is black. You can customize the
     * background by changing CaldroidFragment.selectedBackgroundDrawable, and
     * change the text color CaldroidFragment.selectedTextColor before call this
     * method. This method does not refresh view, need to call refreshView()
     *
     * @param fromDate
     * @param toDate
     */
    public void setSelectedDates(Date fromDate, Date toDate) {
        // Ensure fromDate is before toDate
        if (fromDate == null || toDate == null || fromDate.after(toDate)) {
            return;
        }

        selectedDates.clear();

        DateTime fromDateTime = FlextHelper.convertDateToDateTime(fromDate);
        DateTime toDateTime = FlextHelper.convertDateToDateTime(toDate);

        DateTime dateTime = fromDateTime;
        while (dateTime.lt(toDateTime)) {
            selectedDates.add(dateTime);
            dateTime = dateTime.plusDays(1);
        }
        selectedDates.add(toDateTime);
    }

    /**
     * Convenient method to select dates from String
     *
     * @param fromDateString
     * @param toDateString
     * @param dateFormat
     * @throws ParseException
     */
    public void setSelectedDateStrings(String fromDateString,
                                       String toDateString, String dateFormat) throws ParseException {

        Date fromDate = FlextHelper.getDateFromString(fromDateString,
                dateFormat);
        Date toDate = FlextHelper
                .getDateFromString(toDateString, dateFormat);
        setSelectedDates(fromDate, toDate);
    }

    protected ArrayList<String> getDaysOfWeek() {
        ArrayList<String> list = new ArrayList<>();

        SimpleDateFormat fmt = new SimpleDateFormat("EEE", Locale.getDefault());

        // 17 Feb 2013 is Sunday
        DateTime sunday = new DateTime(2013, 2, 17, 0, 0, 0, 0);
        DateTime nextDay = sunday.plusDays(startDayOfWeek - SUNDAY);

        for (int i = 0; i < 7; i++) {
            Date date = FlextHelper.convertDateTimeToDate(nextDay);
            list.add(fmt.format(date).toUpperCase());
            nextDay = nextDay.plusDays(1);
        }

        return list;
    }

    /**
     * Callback to listener when date is valid (not disable, not outside of
     * min/max date)
     *
     * @return
     */
    private AdapterView.OnItemClickListener getDateItemClickListener() {
        if (dateItemClickListener == null) {
            dateItemClickListener = new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {

                    DateTime dateTime = dateInMonthsList.get(position);

                    if (caldroidListener != null) {
                        if (!enableClickOnDisabledDates) {
                            if ((minDateTime != null && dateTime
                                    .lt(minDateTime))
                                    || (maxDateTime != null && dateTime
                                    .gt(maxDateTime))
                                    || (disableDates != null && disableDates
                                    .indexOf(dateTime) != -1)) {
                                return;
                            }
                        }

                        Date date = FlextHelper
                                .convertDateTimeToDate(dateTime);
                        caldroidListener.onClickDate(date, view);
                    }
                }
            };
        }

        return dateItemClickListener;
    }

    /**
     * Callback to listener when date is valid (not disable, not outside of
     * min/max date)
     *
     * @return
     */
    private AdapterView.OnItemLongClickListener getDateItemLongClickListener() {
        if (dateItemLongClickListener == null) {
            dateItemLongClickListener = new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent,
                                               View view, int position, long id) {

                    DateTime dateTime = dateInMonthsList.get(position);

                    if (caldroidListener != null) {
                        if (!enableClickOnDisabledDates) {
                            if ((minDateTime != null && dateTime
                                    .lt(minDateTime))
                                    || (maxDateTime != null && dateTime
                                    .gt(maxDateTime))
                                    || (disableDates != null && disableDates
                                    .indexOf(dateTime) != -1)) {
                                return false;
                            }
                        }
                        Date date = FlextHelper
                                .convertDateTimeToDate(dateTime);
                        caldroidListener.onLongClickDate(date, view);
                    }

                    return true;
                }
            };
        }

        return dateItemLongClickListener;
    }

    /**
     * Refresh month title text view when user swipe
     */
    protected void refreshMonthTitleTextView() {
        // Refresh title view
        firstMonthTime.year = year;
        firstMonthTime.month = month - 1;
        firstMonthTime.monthDay = 1;
        long millis = firstMonthTime.toMillis(true);

        // This is the method used by the platform Calendar app to get a
        // correctly localized month name for display on a wall calendar
        monthYearStringBuilder.setLength(0);
        String monthTitle = DateUtils.formatDateRange(getActivity(),
                monthYearFormatter, millis, millis, MONTH_YEAR_FLAG).toString();

        monthTitleTextView.setText(monthTitle);
    }

    /**
     * Refresh view when parameter changes. You should always change all
     * parameters first, then call this method.
     */
    public void refreshView() {
        // If month and year is not yet initialized, refreshView doesn't do
        // anything
        if (month == -1 || year == -1) {
            return;
        }

        refreshMonthTitleTextView();

        // Refresh the date grid views
        for (FlextGridAdapter adapter : datePagerAdapters) {
            // Reset caldroid data
            adapter.setCaldroidData(getCaldroidData());

            // Reset extra data
            adapter.setExtraData(extraData);

            // Refresh view
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * Retrieve initial arguments to the fragment Data can include: month, year,
     * dialogTitle, showNavigationArrows,(String) disableDates, selectedDates,
     * minDate, maxDate
     */
    protected void retrieveInitialArgs() {
        // Get arguments
        Bundle args = getArguments();
        if (args != null) {
            // Get month, year
            month = args.getInt(MONTH, -1);
            year = args.getInt(YEAR, -1);

            // Get start day of Week. Default calendar first column is SUNDAY
            startDayOfWeek = args.getInt(START_DAY_OF_WEEK, 1);
            if (startDayOfWeek > 7) {
                startDayOfWeek = startDayOfWeek % 7;
            }

            // Should show arrow
            showNavigationArrows = args
                    .getBoolean(SHOW_NAVIGATION_ARROWS, true);

            // Should enable swipe to change month
            enableSwipe = args.getBoolean(ENABLE_SWIPE, true);
            enableImage = args.getBoolean(ENABLE_IMAGES, true);
            enableAnimation = args.getBoolean(ENABLE_ANIMATIONS, true);

            // Get sixWeeksInCalendar
            sixWeeksInCalendar = args.getBoolean(SIX_WEEKS_IN_CALENDAR, true);

            // Get clickable setting
            enableClickOnDisabledDates = args.getBoolean(
                    ENABLE_CLICK_ON_DISABLED_DATES, false);

            // Get disable dates
            ArrayList<String> disableDateStrings = args
                    .getStringArrayList(DISABLE_DATES);
            if (disableDateStrings != null && disableDateStrings.size() > 0) {
                disableDates.clear();
                for (String dateString : disableDateStrings) {
                    DateTime dt = FlextHelper.getDateTimeFromString(
                            dateString, "yyyy-MM-dd");
                    disableDates.add(dt);
                }
            }

            // Get selected dates
            ArrayList<String> selectedDateStrings = args
                    .getStringArrayList(SELECTED_DATES);
            if (selectedDateStrings != null && selectedDateStrings.size() > 0) {
                selectedDates.clear();
                for (String dateString : selectedDateStrings) {
                    DateTime dt = FlextHelper.getDateTimeFromString(
                            dateString, "yyyy-MM-dd");
                    selectedDates.add(dt);
                }
            }

            // Get min date and max date
            String minDateTimeString = args.getString(MIN_DATE);
            if (minDateTimeString != null) {
                minDateTime = FlextHelper.getDateTimeFromString(
                        minDateTimeString, null);
            }

            String maxDateTimeString = args.getString(MAX_DATE);
            if (maxDateTimeString != null) {
                maxDateTime = FlextHelper.getDateTimeFromString(
                        maxDateTimeString, null);
            }

        }
        if (month == -1 || year == -1) {
            DateTime dateTime = DateTime.today(TimeZone.getDefault());
            month = dateTime.getMonth();
            year = dateTime.getYear();
        }
    }

    private void setupDateGridPages(View view) {
        // Get current date time
        DateTime currentDateTime = new DateTime(year, month, 1, 0, 0, 0, 0);

        // Set to pageChangeListener
        pageChangeListener = new DatePageChangeListener();
        pageChangeListener.setCurrentDateTime(currentDateTime);

        // Setup adapters for the grid views
        // Current month
        FlextGridAdapter adapter0 = getNewDatesGridAdapter(
                currentDateTime.getMonth(), currentDateTime.getYear());

        // Setup dateInMonthsList
        dateInMonthsList = adapter0.getDatetimeList();

        // Next month
        DateTime nextDateTime = currentDateTime.plus(0, 1, 0, 0, 0, 0, 0,
                DateTime.DayOverflow.LastDay);
        FlextGridAdapter adapter1 = getNewDatesGridAdapter(
                nextDateTime.getMonth(), nextDateTime.getYear());

        // Next 2 month
        DateTime next2DateTime = nextDateTime.plus(0, 1, 0, 0, 0, 0, 0,
                DateTime.DayOverflow.LastDay);
        FlextGridAdapter adapter2 = getNewDatesGridAdapter(
                next2DateTime.getMonth(), next2DateTime.getYear());

        // Previous month
        DateTime prevDateTime = currentDateTime.minus(0, 1, 0, 0, 0, 0, 0,
                DateTime.DayOverflow.LastDay);
        FlextGridAdapter adapter3 = getNewDatesGridAdapter(
                prevDateTime.getMonth(), prevDateTime.getYear());

        // Add to the array of adapters
        datePagerAdapters.add(adapter0);
        datePagerAdapters.add(adapter1);
        datePagerAdapters.add(adapter2);
        datePagerAdapters.add(adapter3);

        // Set adapters to the pageChangeListener so it can refresh the adapter
        // when page change
        pageChangeListener.setFlextGridAdapters(datePagerAdapters);

        // Setup InfiniteViewPager and InfinitePagerAdapter. The
        // InfinitePagerAdapter is responsible
        // for reuse the fragments
        dateViewPager = (InfiniteViewPager) view
                .findViewById(R.id.months_infinite_pager);

        // Set enable swipe
        dateViewPager.setEnabled(enableSwipe);

        // Set if viewpager wrap around particular month or all months (6 rows)
        dateViewPager.setSixWeeksInCalendar(sixWeeksInCalendar);

        // Set the numberOfDaysInMonth to dateViewPager so it can calculate the
        // height correctly
        dateViewPager.setDatesInMonth(dateInMonthsList);

        // MonthPagerAdapter actually provides 4 real fragments. The
        // InfinitePagerAdapter only recycles fragment provided by this
        // MonthPagerAdapter
        final MonthPagerAdapter pagerAdapter = new MonthPagerAdapter(
                getChildFragmentManager());

        // Provide initial data to the fragments, before they are attached to
        // view.
        fragments = pagerAdapter.getFragments();
        for (int i = 0; i < NUMBER_OF_PAGES; i++) {
            DateGridFragment dateGridFragment = fragments.get(i);
            FlextGridAdapter adapter = datePagerAdapters.get(i);
            dateGridFragment.setGridAdapter(adapter);
            dateGridFragment.setOnItemClickListener(getDateItemClickListener());
            dateGridFragment
                    .setOnItemLongClickListener(getDateItemLongClickListener());
        }

        // Setup InfinitePagerAdapter to wrap around MonthPagerAdapter
        InfinitePagerAdapter infinitePagerAdapter = new InfinitePagerAdapter(
                pagerAdapter);

        // Use the infinitePagerAdapter to provide data for dateViewPager
        dateViewPager.setAdapter(infinitePagerAdapter);

        // Setup pageChangeListener
        dateViewPager.setOnPageChangeListener(pageChangeListener);
    }



    public static FlextCal newInstance() {
        FlextCal fragment = new FlextCal();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public FlextCal() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        retrieveInitialArgs();
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_flext_cal, container, false);
        SharedPreferences prefs = getActivity().getSharedPreferences("ui_settings", Context.MODE_PRIVATE);
        boolean isDark = prefs.getBoolean("dark_theme", false);

        CardView card = (CardView) view.findViewById(R.id.card);
        CardView pagerCard = (CardView) view.findViewById(R.id.pagerCard);
        CardView titleCard = (CardView) view.findViewById(R.id.titleCard);
        if (FlextHelper.is21()) {
            card.setElevation(5f);
            pagerCard.setElevation(5f);
            titleCard.setElevation(5f);
        }

        Resources color = getActivity().getResources();

        if (isDark){
            card.setCardBackgroundColor(color.getColor(R.color.grey_x));
            pagerCard.setCardBackgroundColor(color.getColor(R.color.grey_x));
            titleCard.setCardBackgroundColor(color.getColor(R.color.grey_x));
        } else {
            card.setCardBackgroundColor(color.getColor(R.color.colorWhite));
            pagerCard.setCardBackgroundColor(color.getColor(R.color.colorWhite));
            titleCard.setCardBackgroundColor(color.getColor(R.color.colorWhite));
        }

        image = (KenBurnsView) view.findViewById(R.id.image);

        monthTitleTextView = (TextView) view.findViewById(R.id.monthYear);

        weekdayGridView = (GridView) view.findViewById(R.id.weekday_gridview);
        WeekdayArrayAdapter weekdaysAdapter = getNewWeekdayAdapter();
        weekdayGridView.setAdapter(weekdaysAdapter);

        setupDateGridPages(view);

        refreshView();

        // Inform client that all views are created and not null
        // Client should perform customization for buttons and textviews here
        if (caldroidListener != null) {
            caldroidListener.onCaldroidViewCreated();
        }

        return view;
    }

    @Override
    public void onMonthSelect(int month) {
        if (image != null && enableImage) {
            ImageCheck check = new ImageCheck(getActivity());
            if (check.isImage(month)){
                Picasso.with(getActivity()).load(new File(check.getImage(month))).into(image);
            } else {
                new LoadAsync(getActivity(), month).execute();
            }
        }
    }

    public class DatePageChangeListener implements ViewPager.OnPageChangeListener {
        private int currentPage = InfiniteViewPager.OFFSET;
        private DateTime currentDateTime;
        private ArrayList<FlextGridAdapter> flextGridAdapters;

        /**
         * Return currentPage of the dateViewPager
         *
         * @return
         */
        public int getCurrentPage() {
            return currentPage;
        }

        public void setCurrentPage(int currentPage) {
            this.currentPage = currentPage;
        }

        /**
         * Return currentDateTime of the selected page
         *
         * @return
         */
        public DateTime getCurrentDateTime() {
            return currentDateTime;
        }

        public void setCurrentDateTime(DateTime dateTime) {
            this.currentDateTime = dateTime;
            setCalendarDateTime(currentDateTime);
        }

        /**
         * Return 4 adapters
         *
         * @return
         */
        public ArrayList<FlextGridAdapter> getFlextGridAdapters() {
            return flextGridAdapters;
        }

        public void setFlextGridAdapters(
                ArrayList<FlextGridAdapter> flextGridAdapters) {
            this.flextGridAdapters = flextGridAdapters;
        }

        /**
         * Return virtual next position
         *
         * @param position
         * @return
         */
        private int getNext(int position) {
            return (position + 1) % FlextCal.NUMBER_OF_PAGES;
        }

        /**
         * Return virtual previous position
         *
         * @param position
         * @return
         */
        private int getPrevious(int position) {
            return (position + 3) % FlextCal.NUMBER_OF_PAGES;
        }

        /**
         * Return virtual current position
         *
         * @param position
         * @return
         */
        public int getCurrent(int position) {
            return position % FlextCal.NUMBER_OF_PAGES;
        }

        @Override
        public void onPageScrollStateChanged(int position) {
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        public void refreshAdapters(int position) {
            // Get adapters to refresh
            FlextGridAdapter currentAdapter = flextGridAdapters
                    .get(getCurrent(position));
            FlextGridAdapter prevAdapter = flextGridAdapters
                    .get(getPrevious(position));
            FlextGridAdapter nextAdapter = flextGridAdapters
                    .get(getNext(position));

            if (position == currentPage) {
                // Refresh current adapter

                currentAdapter.setAdapterDateTime(currentDateTime);
                currentAdapter.notifyDataSetChanged();

                // Refresh previous adapter
                prevAdapter.setAdapterDateTime(currentDateTime.minus(0, 1, 0,
                        0, 0, 0, 0, DateTime.DayOverflow.LastDay));
                prevAdapter.notifyDataSetChanged();

                // Refresh next adapter
                nextAdapter.setAdapterDateTime(currentDateTime.plus(0, 1, 0, 0,
                        0, 0, 0, DateTime.DayOverflow.LastDay));
                nextAdapter.notifyDataSetChanged();
            }
            // Detect if swipe right or swipe left
            // Swipe right
            else if (position > currentPage) {
                // Update current date time to next month
                currentDateTime = currentDateTime.plus(0, 1, 0, 0, 0, 0, 0,
                        DateTime.DayOverflow.LastDay);

                // Refresh the adapter of next gridview
                nextAdapter.setAdapterDateTime(currentDateTime.plus(0, 1, 0, 0,
                        0, 0, 0, DateTime.DayOverflow.LastDay));
                nextAdapter.notifyDataSetChanged();

            }
            // Swipe left
            else {
                // Update current date time to previous month
                currentDateTime = currentDateTime.minus(0, 1, 0, 0, 0, 0, 0,
                        DateTime.DayOverflow.LastDay);

                // Refresh the adapter of previous gridview
                prevAdapter.setAdapterDateTime(currentDateTime.minus(0, 1, 0,
                        0, 0, 0, 0, DateTime.DayOverflow.LastDay));
                prevAdapter.notifyDataSetChanged();
            }

            // Update current page
            currentPage = position;
        }

        /**
         * Refresh the fragments
         */
        @Override
        public void onPageSelected(int position) {
            refreshAdapters(position);

            // Update current date time of the selected page
            setCalendarDateTime(currentDateTime);

            // Update all the dates inside current month
            FlextGridAdapter currentAdapter = flextGridAdapters
                    .get(position % FlextCal.NUMBER_OF_PAGES);

            // Refresh dateInMonthsList
            dateInMonthsList.clear();
            dateInMonthsList.addAll(currentAdapter.getDatetimeList());
        }

    }
}
