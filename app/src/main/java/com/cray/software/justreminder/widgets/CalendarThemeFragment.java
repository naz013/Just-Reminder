package com.cray.software.justreminder.widgets;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import com.cray.software.justreminder.datas.WidgetItem;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.interfaces.Constants;
import com.roomorama.caldroid.CalendarHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import hirondelle.date4j.DateTime;

public class CalendarThemeFragment extends Fragment{

    static final String ARGUMENT_PAGE_NUMBER = "arg_page_number";
    static final String ARGUMENT_DATA = "arg_data";
    int pageNumber;
    RelativeLayout header;
    ImageButton plusButton, voiceButton, settingsButton, prevMonth, nextMonth;
    TextView currentDate, themeTitle;
    GridView weekdayGrid, monthGrid;
    ArrayList<CalendarWidgetConfig.ThemeItem> list;

    public static CalendarThemeFragment newInstance(int page, ArrayList<CalendarWidgetConfig.ThemeItem> list) {
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

        header = (RelativeLayout) view.findViewById(R.id.header);

        plusButton = (ImageButton) view.findViewById(R.id.plusButton);
        voiceButton = (ImageButton) view.findViewById(R.id.voiceButton);
        settingsButton = (ImageButton) view.findViewById(R.id.settingsButton);
        nextMonth = (ImageButton) view.findViewById(R.id.nextMonth);
        prevMonth = (ImageButton) view.findViewById(R.id.prevMonth);

        currentDate = (TextView) view.findViewById(R.id.currentDate);
        themeTitle = (TextView) view.findViewById(R.id.themeTitle);

        weekdayGrid = (GridView) view.findViewById(R.id.weekdayGrid);
        monthGrid = (GridView) view.findViewById(R.id.monthGrid);

        CalendarWidgetConfig.ThemeItem themeItem = list.get(pageNumber);

        int itemTextColor = themeItem.getItemTextColor();
        int widgetBgColor = themeItem.getWidgetBgColor();
        int headerColor = themeItem.getHeaderColor();
        int borderColor = themeItem.getBorderColor();
        int titleColor = themeItem.getTitleColor();
        int rowColor = themeItem.getRowColor();

        int leftArrow = themeItem.getLeftArrow();
        int rightArrow = themeItem.getRightArrow();
        int iconPlus = themeItem.getIconPlus();
        int iconVoice = themeItem.getIconVoice();
        int iconSettings = themeItem.getIconSettings();

        int currentMark = themeItem.getCurrentMark();
        int birthdayMark = themeItem.getBirthdayMark();
        int reminderMark = themeItem.getReminderMark();

        weekdayGrid.setBackgroundResource(widgetBgColor);
        header.setBackgroundResource(headerColor);
        currentDate.setTextColor(titleColor);
        monthGrid.setBackgroundResource(borderColor);

        plusButton.setImageResource(iconPlus);
        nextMonth.setImageResource(rightArrow);
        prevMonth.setImageResource(leftArrow);
        voiceButton.setImageResource(iconVoice);
        settingsButton.setImageResource(iconSettings);

        Calendar cal = new GregorianCalendar();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM yyyy");
        dateFormat.setCalendar(cal);
        String date = dateFormat.format(cal.getTime()).toUpperCase();
        currentDate.setText(date);

        themeTitle.setText(themeItem.getTitle());

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
            if (prefs.loadInt(Constants.APP_UI_PREFERENCES_START_DAY) == 1){
                nextDay = nextDay.plusDays(1);
            }

            for (int i = 0; i < 7; i++) {
                Date date = CalendarHelper.convertDateTimeToDate(nextDay);
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
        ArrayList<WidgetItem> pagerData = new ArrayList<>();
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
                if (prefs.loadInt(Constants.APP_UI_PREFERENCES_START_DAY) == 1){
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
