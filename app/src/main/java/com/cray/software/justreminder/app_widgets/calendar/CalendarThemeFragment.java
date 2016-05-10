package com.cray.software.justreminder.app_widgets.calendar;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.roboto_views.RoboTextView;
import com.hexrain.flextcal.FlextHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Formatter;
import java.util.GregorianCalendar;
import java.util.Locale;

import hirondelle.date4j.DateTime;

public class CalendarThemeFragment extends Fragment{

    static final String ARGUMENT_PAGE_NUMBER = "arg_page_number";
    static final String ARGUMENT_DATA = "arg_data";
    private int pageNumber;
    private ArrayList<CalendarTheme> list;

    public static CalendarThemeFragment newInstance(int page, ArrayList<CalendarTheme> list) {
        CalendarThemeFragment pageFragment = new CalendarThemeFragment();
        Bundle arguments = new Bundle();
        arguments.putInt(ARGUMENT_PAGE_NUMBER, page);
        arguments.putParcelableArrayList(ARGUMENT_DATA, list);
        pageFragment.setArguments(arguments);
        return pageFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle intent = getArguments();
        pageNumber = intent.getInt(ARGUMENT_PAGE_NUMBER);
        list = intent.getParcelableArrayList(ARGUMENT_DATA);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar_widget_preview, container, false);

        RelativeLayout header = (RelativeLayout) view.findViewById(R.id.header);
        RelativeLayout background = (RelativeLayout) view.findViewById(R.id.background);

        ImageButton plusButton = (ImageButton) view.findViewById(R.id.plusButton);
        ImageButton voiceButton = (ImageButton) view.findViewById(R.id.voiceButton);
        ImageButton settingsButton = (ImageButton) view.findViewById(R.id.settingsButton);
        ImageButton nextMonth = (ImageButton) view.findViewById(R.id.nextMonth);
        ImageButton prevMonth = (ImageButton) view.findViewById(R.id.prevMonth);

        TextView currentDate = (TextView) view.findViewById(R.id.currentDate);
        RoboTextView themeTitle = (RoboTextView) view.findViewById(R.id.themeTitle);
        RoboTextView helpTip = (RoboTextView) view.findViewById(R.id.note);

        GridView weekdayGrid = (GridView) view.findViewById(R.id.weekdayGrid);
        GridView monthGrid = (GridView) view.findViewById(R.id.monthGrid);

        CalendarTheme calendarTheme = list.get(pageNumber);

        int windowColor = calendarTheme.getWindowColor();
        background.setBackgroundResource(windowColor);
        int windowTextColor = calendarTheme.getWindowTextColor();
        themeTitle.setTextColor(windowTextColor);
        helpTip.setTextColor(windowTextColor);

        int itemTextColor = calendarTheme.getItemTextColor();
        int widgetBgColor = calendarTheme.getWidgetBgColor();
        int headerColor = calendarTheme.getHeaderColor();
        int borderColor = calendarTheme.getBorderColor();
        int titleColor = calendarTheme.getTitleColor();
        int rowColor = calendarTheme.getRowColor();

        int leftArrow = calendarTheme.getLeftArrow();
        int rightArrow = calendarTheme.getRightArrow();
        int iconPlus = calendarTheme.getIconPlus();
        int iconVoice = calendarTheme.getIconVoice();
        int iconSettings = calendarTheme.getIconSettings();

        int currentMark = calendarTheme.getCurrentMark();
        int birthdayMark = calendarTheme.getBirthdayMark();
        int reminderMark = calendarTheme.getReminderMark();

        weekdayGrid.setBackgroundResource(widgetBgColor);
        header.setBackgroundResource(headerColor);
        currentDate.setTextColor(titleColor);
        monthGrid.setBackgroundResource(borderColor);

        plusButton.setImageResource(iconPlus);
        nextMonth.setImageResource(rightArrow);
        prevMonth.setImageResource(leftArrow);
        voiceButton.setImageResource(iconVoice);
        settingsButton.setImageResource(iconSettings);

        StringBuilder monthYearStringBuilder = new StringBuilder(50);
        Formatter monthYearFormatter = new Formatter(
                monthYearStringBuilder, Locale.getDefault());
        int MONTH_YEAR_FLAG = DateUtils.FORMAT_SHOW_DATE
                | DateUtils.FORMAT_NO_MONTH_DAY | DateUtils.FORMAT_SHOW_YEAR;
        Calendar cal = new GregorianCalendar();
        String monthTitle = DateUtils.formatDateRange(getActivity(),
                monthYearFormatter, cal.getTimeInMillis(), cal.getTimeInMillis(), MONTH_YEAR_FLAG).toString();
        currentDate.setText(monthTitle.toUpperCase());

        themeTitle.setText(calendarTheme.getTitle());

        weekdayGrid.setAdapter(new WeekdayAdapter(getActivity(), itemTextColor));
        monthGrid.setAdapter(new MonthGridAdapter(getActivity(), new int[]{itemTextColor, rowColor,
                currentMark, birthdayMark, reminderMark}));
        return view;
    }

    private class WeekdayAdapter extends BaseAdapter{

        ArrayList<String> weekdays;
        int SUNDAY = 1;
        int startDayOfWeek = SUNDAY;
        Context context;
        LayoutInflater inflater;
        int textColor;

        public WeekdayAdapter(Context context, int textColor){
            this.context = context;
            this.textColor = textColor;
            inflater = LayoutInflater.from(context);
            weekdays = new ArrayList<>();
            weekdays.clear();
            SimpleDateFormat fmt = new SimpleDateFormat("EEE", Locale.getDefault());

            // 17 Feb 2013 is Sunday
            DateTime sunday = new DateTime(2013, 2, 17, 0, 0, 0, 0);
            DateTime nextDay = sunday.plusDays(startDayOfWeek - SUNDAY);
            SharedPrefs prefs = new SharedPrefs(context);
            if (prefs.loadInt(Prefs.START_DAY) == 1){
                nextDay = nextDay.plusDays(1);
            }

            for (int i = 0; i < 7; i++) {
                Date date = FlextHelper.convertDateTimeToDate(nextDay);
                weekdays.add(fmt.format(date).toUpperCase());
                nextDay = nextDay.plusDays(1);
            }
        }

        @Override
        public int getCount() {
            return weekdays.size();
        }

        @Override
        public Object getItem(int position) {
            return weekdays.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.weekday_grid, null);
            }

            TextView textView = (TextView) convertView.findViewById(R.id.textView1);
            textView.setText(weekdays.get(position));
            textView.setTextColor(textColor);

            return convertView;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }
    }

    private class MonthGridAdapter extends BaseAdapter{

        ArrayList<DateTime> datetimeList;
        int SUNDAY = 1;
        int startDayOfWeek = SUNDAY;
        int mDay, mMonth, mYear, prefsMonth;
        Context context;
        LayoutInflater inflater;
        int textColor, widgetBgColor, cMark, bMark, rMark;

        public MonthGridAdapter(Context context, int[] resources){
            this.context = context;
            this.textColor = resources[0];
            this.widgetBgColor = resources[1];
            this.cMark = resources[2];
            this.bMark = resources[3];
            this.rMark = resources[4];
            inflater = LayoutInflater.from(context);
            datetimeList = new ArrayList<>();
            datetimeList.clear();

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            int year = calendar.get(Calendar.YEAR);
            mDay = calendar.get(Calendar.DAY_OF_MONTH);
            mMonth = calendar.get(Calendar.MONTH) + 1;
            mYear = year;

            DateTime firstDateOfMonth = new DateTime(year, prefsMonth + 1, 1, 0, 0, 0, 0);
            DateTime lastDateOfMonth = firstDateOfMonth.plusDays(firstDateOfMonth
                    .getNumDaysInMonth() - 1);

            // Add dates of first week from previous month
            int weekdayOfFirstDate = firstDateOfMonth.getWeekDay();

            // If weekdayOfFirstDate smaller than startDayOfWeek
            // For e.g: weekdayFirstDate is Monday, startDayOfWeek is Tuesday
            // increase the weekday of FirstDate because it's in the future
            if (weekdayOfFirstDate < startDayOfWeek) {
                weekdayOfFirstDate += 7;
            }

            while (weekdayOfFirstDate > 0) {
                SharedPrefs prefs = new SharedPrefs(context);
                int temp = startDayOfWeek;
                if (prefs.loadInt(Prefs.START_DAY) == 1){
                    temp = startDayOfWeek + 1;
                }

                DateTime dateTime = firstDateOfMonth.minusDays(weekdayOfFirstDate
                        - temp);
                if (!dateTime.lt(firstDateOfMonth)) {
                    break;
                }

                datetimeList.add(dateTime);
                weekdayOfFirstDate--;
            }

            // Add dates of current month
            for (int i = 0; i < lastDateOfMonth.getDay(); i++) {
                datetimeList.add(firstDateOfMonth.plusDays(i));
            }

            // Add dates of last week from next month
            int endDayOfWeek = startDayOfWeek - 1;

            if (endDayOfWeek == 0) {
                endDayOfWeek = 7;
            }

            if (lastDateOfMonth.getWeekDay() != endDayOfWeek) {
                int i = 1;
                while (true) {
                    DateTime nextDay = lastDateOfMonth.plusDays(i);
                    datetimeList.add(nextDay);
                    i++;
                    if (nextDay.getWeekDay() == endDayOfWeek) {
                        break;
                    }
                }
            }

            // Add more weeks to fill remaining rows
            int size = datetimeList.size();
            int numOfDays = 42 - size;
            DateTime lastDateTime = datetimeList.get(size - 1);
            for (int i = 1; i <= numOfDays; i++) {
                DateTime nextDateTime = lastDateTime.plusDays(i);
                datetimeList.add(nextDateTime);
            }
        }

        @Override
        public int getCount() {
            return datetimeList.size();
        }

        @Override
        public Object getItem(int position) {
            return datetimeList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.month_view_grid, null);
            }

            ColorSetter cs = new ColorSetter(context);

            int selDay = datetimeList.get(position).getDay();

            FrameLayout background = (FrameLayout) convertView.findViewById(R.id.background);
            TextView textView = (TextView) convertView.findViewById(R.id.textView);
            TextView currentMark = (TextView) convertView.findViewById(R.id.currentMark);
            TextView reminderMark = (TextView) convertView.findViewById(R.id.reminderMark);
            TextView birthdayMark = (TextView) convertView.findViewById(R.id.birthdayMark);

            textView.setText(String.valueOf(selDay));
            textView.setTextColor(textColor);
            background.setBackgroundResource(widgetBgColor);

            currentMark.setBackgroundColor(Color.TRANSPARENT);
            reminderMark.setBackgroundColor(Color.TRANSPARENT);
            birthdayMark.setBackgroundColor(Color.TRANSPARENT);
            if (selDay == 15){
                if (rMark != 0){
                    reminderMark.setBackgroundResource(rMark);
                } else {
                    reminderMark.setBackgroundColor(context.getResources()
                            .getColor(cs.colorReminderCalendar()));
                }
            }
            if (selDay == 11){
                if (bMark != 0){
                    birthdayMark.setBackgroundResource(bMark);
                } else {
                    birthdayMark.setBackgroundColor(context.getResources()
                            .getColor(cs.colorBirthdayCalendar()));
                }
            }

            if (11 == selDay){
                if (cMark != 0){
                    currentMark.setBackgroundResource(cMark);
                } else {
                    currentMark.setBackgroundColor(context.getResources()
                            .getColor(cs.colorCurrentCalendar()));
                }
            }

            return convertView;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }
    }
}
