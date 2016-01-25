package com.hexrain.flextcal;


import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorInt;
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
public class FlextCal extends Fragment {

    /**
     * Weekday conventions
     */
    public static int SUNDAY = 1;
    public static int MONDAY = 2;

    /**
     * Flags to display month
     */
    private static final int MONTH_YEAR_FLAG = DateUtils.FORMAT_SHOW_DATE
            | DateUtils.FORMAT_NO_MONTH_DAY | DateUtils.FORMAT_SHOW_YEAR;

    public final static int NUMBER_OF_PAGES = 4;

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

    /**
     * For internal use
     */
    public final static String _MIN_DATE_TIME = "_minDateTime";
    public final static String _MAX_DATE_TIME = "_maxDateTime";
    public final static String _BACKGROUND_FOR_TODAY_ = "_backgroundForToday";
    public final static String _EVENTS_ = "_events";

    /**
     * datePagerAdapters hold 4 adapters, meant to be reused
     */
    protected ArrayList<FlextGridAdapter> datePagerAdapters = new ArrayList<>();

    /**
     * Declare views
     */
    private TextView monthTitleTextView;
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

    @ColorInt protected int backgroundForToday = 0;
    protected HashMap<DateTime, Events> eventsMap = new HashMap<>();

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
     * For client to access array of rotating fragments
     */
    public ArrayList<DateGridFragment> getFragments() {
        return fragments;
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
        caldroidData.put(_BACKGROUND_FOR_TODAY_, backgroundForToday);
        caldroidData.put(_EVENTS_, eventsMap);

        return caldroidData;
    }

    /**
     * Set backgroundForDateMap
     */
    public void setBackgroundForToday(@ColorInt int backgroundForToday){
        this.backgroundForToday = backgroundForToday;
    }

    public void setEvents(HashMap<DateTime, Events> eventsMap) {
        this.eventsMap = eventsMap;
    }

    /**
     * Set caldroid listener when user click on a date
     *
     * @param caldroidListener
     */
    public void setCaldroidListener(FlextListener caldroidListener) {
        this.caldroidListener = caldroidListener;
    }

    public void setCalendarDateTime(DateTime dateTime) {
        month = dateTime.getMonth();
        year = dateTime.getYear();

        // Notify listener
        if (caldroidListener != null) {
            caldroidListener.onMonthChanged(month, year);
        }

        refreshView();
    }

    /**
     * To clear selectedDates. This method does not refresh view, need to
     * explicitly call refreshView()
     */
    public void clearSelectedDates() {
        selectedDates.clear();
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

        if (image != null && enableImage) {
            ImageCheck check = new ImageCheck();
            if (check.isImage(month)){
                Picasso.with(getActivity()).load(new File(check.getImage(month))).into(image);
            } else {
                new LoadAsync(getActivity(), month).execute();
            }
        }
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
        DatePageChangeListener pageChangeListener = new DatePageChangeListener();
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
        InfiniteViewPager dateViewPager = (InfiniteViewPager) view
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

        image = (KenBurnsView) view.findViewById(R.id.imageView);

        monthTitleTextView = (TextView) view.findViewById(R.id.monthYear);

        GridView weekdayGridView = (GridView) view.findViewById(R.id.weekday_gridview);
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

    public class DatePageChangeListener implements ViewPager.OnPageChangeListener {
        private int currentPage = InfiniteViewPager.OFFSET;
        private DateTime currentDateTime;
        private ArrayList<FlextGridAdapter> flextGridAdapters;

        public void setCurrentDateTime(DateTime dateTime) {
            this.currentDateTime = dateTime;
            setCalendarDateTime(currentDateTime);
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
