package com.cray.software.justreminder.widgets;

import android.app.AlarmManager;
import android.content.Context;
import android.database.Cursor;
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
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.datas.WidgetItem;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.TimeCount;
import com.cray.software.justreminder.interfaces.Configs;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.utils.ReminderUtils;
import com.roomorama.caldroid.CalendarHelper;

import java.text.ParseException;
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
    RelativeLayout widgetBg, header;
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

        widgetBg = (RelativeLayout) view.findViewById(R.id.widgetBg);
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

        widgetBg.setBackgroundColor(widgetBgColor);
        header.setBackgroundColor(headerColor);
        currentDate.setTextColor(titleColor);
        monthGrid.setBackgroundColor(borderColor);

        plusButton.setImageResource(iconPlus);
        nextMonth.setImageResource(rightArrow);
        prevMonth.setImageResource(leftArrow);
        voiceButton.setImageResource(iconVoice);
        settingsButton.setImageResource(iconSettings);

        Calendar cal = new GregorianCalendar();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM yyyy");
        dateFormat.setCalendar(cal);
        String date = dateFormat.format(cal.getTime());
        currentDate.setText(date);

        themeTitle.setText(themeItem.getTitle());

        weekdayGrid.setAdapter(new WeekdayAdapter(getActivity(), itemTextColor));
        monthGrid.setAdapter(new MonthGridAdapter(getActivity(), new int[]{itemTextColor, rowColor}));
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
        int textColor, widgetBgColor;

        public MonthGridAdapter(Context context, int[] resources){
            this.context = context;
            this.textColor = resources[0];
            this.widgetBgColor = resources[1];
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

            showEvents();
        }

        private void showEvents() {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            int currentDay;
            int currentMonth;
            int currentYear;

            SharedPrefs sPrefs = new SharedPrefs(context);
            int hour = sPrefs.loadInt(Constants.APP_UI_PREFERENCES_BIRTHDAY_REMINDER_HOUR);
            int minute = sPrefs.loadInt(Constants.APP_UI_PREFERENCES_BIRTHDAY_REMINDER_MINUTE);
            boolean isFeature = sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_CALENDAR_FEATURE_TASKS);
            boolean isRemindersEnabled = sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_REMINDERS_IN_CALENDAR);

            DataBase db = new DataBase(context);
            if (!db.isOpen()) db.open();

            pagerData.clear();

            int position = 0;
            boolean hasBirthdays;
            boolean hasReminders;
            do {
                hasBirthdays = false;
                hasReminders = false;
                currentDay = calendar.get(Calendar.DAY_OF_MONTH);
                currentMonth = calendar.get(Calendar.MONTH);
                currentYear = calendar.get(Calendar.YEAR);
                Cursor c = db.getEvents(currentDay, currentMonth);
                if (c != null && c.moveToFirst()){
                    do {
                        String birthday = c.getString(c.getColumnIndex(Constants.ContactConstants.COLUMN_CONTACT_BIRTHDAY));
                        Date date1 = null;
                        try {
                            date1 = format.parse(birthday);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        if (date1 != null) {
                            Calendar calendar1 = Calendar.getInstance();
                            calendar1.setTime(date1);
                            int bDay = calendar1.get(Calendar.DAY_OF_MONTH);
                            int bMonth = calendar1.get(Calendar.MONTH);
                            calendar1.setTimeInMillis(System.currentTimeMillis());
                            calendar1.set(Calendar.MONTH, bMonth);
                            calendar1.set(Calendar.DAY_OF_MONTH, bDay);
                            calendar1.set(Calendar.HOUR_OF_DAY, hour);
                            calendar1.set(Calendar.MINUTE, minute);
                            if (bDay == currentDay && currentMonth == bMonth) {
                                hasBirthdays = true;
                            }
                        }
                    } while (c.moveToNext());
                }
                if (c != null) c.close();

                if (isRemindersEnabled) {
                    Cursor s = db.queryGroup();
                    if (s != null && s.moveToFirst()) {
                        do {
                            int myHour = s.getInt(s.getColumnIndex(Constants.COLUMN_HOUR));
                            int myMinute = s.getInt(s.getColumnIndex(Constants.COLUMN_MINUTE));
                            int myDay = s.getInt(s.getColumnIndex(Constants.COLUMN_DAY));
                            int myMonth = s.getInt(s.getColumnIndex(Constants.COLUMN_MONTH));
                            int myYear = s.getInt(s.getColumnIndex(Constants.COLUMN_YEAR));
                            int repCode = s.getInt(s.getColumnIndex(Constants.COLUMN_REPEAT));
                            int remCount = s.getInt(s.getColumnIndex(Constants.COLUMN_REMINDERS_COUNT));
                            long afterTime = s.getLong(s.getColumnIndex(Constants.COLUMN_REMIND_TIME));
                            String mType = s.getString(s.getColumnIndex(Constants.COLUMN_TYPE));
                            String weekdays = s.getString(s.getColumnIndex(Constants.COLUMN_WEEKDAYS));
                            int isDone = s.getInt(s.getColumnIndex(Constants.COLUMN_IS_DONE));
                            if ((mType.startsWith(Constants.TYPE_SKYPE) ||
                                    mType.matches(Constants.TYPE_CALL) ||
                                    mType.startsWith(Constants.TYPE_APPLICATION) ||
                                    mType.matches(Constants.TYPE_MESSAGE) ||
                                    mType.matches(Constants.TYPE_REMINDER) ||
                                    mType.matches(Constants.TYPE_TIME)) && isDone == 0) {
                                long time = TimeCount.getEventTime(myYear, myMonth, myDay, myHour, myMinute, 0,
                                        afterTime, repCode, remCount, 0);
                                Calendar calendar1 = Calendar.getInstance();
                                calendar1.setTimeInMillis(time);
                                int mDay = calendar1.get(Calendar.DAY_OF_MONTH);
                                int mMonth = calendar1.get(Calendar.MONTH);
                                int mYear = calendar1.get(Calendar.YEAR);
                                if (time > 0 && mDay == currentDay && mMonth == currentMonth && mYear == currentYear) {
                                    hasReminders = true;
                                }
                                if (!mType.matches(Constants.TYPE_TIME) && isFeature && repCode > 0) {
                                    int days = 0;
                                    do {
                                        calendar1.setTimeInMillis(calendar1.getTimeInMillis() + (repCode *
                                                AlarmManager.INTERVAL_DAY));
                                        time = calendar1.getTimeInMillis();
                                        mDay = calendar1.get(Calendar.DAY_OF_MONTH);
                                        mMonth = calendar1.get(Calendar.MONTH);
                                        mYear = calendar1.get(Calendar.YEAR);
                                        days = days + repCode;
                                        if (time > 0 && mDay == currentDay && mMonth == currentMonth && mYear == currentYear) {
                                            hasReminders = true;
                                        }
                                    } while (days < Configs.MAX_DAYS_COUNT);
                                }
                            } else if (mType.startsWith(Constants.TYPE_WEEKDAY) && isDone == 0) {
                                long time = TimeCount.getNextWeekdayTime(myHour, myMinute, weekdays, 0);
                                Calendar calendar1 = Calendar.getInstance();
                                calendar1.setTimeInMillis(time);
                                int mDay = calendar1.get(Calendar.DAY_OF_MONTH);
                                int mMonth = calendar1.get(Calendar.MONTH);
                                int mYear = calendar1.get(Calendar.YEAR);
                                if (time > 0 && mDay == currentDay && mMonth == currentMonth && mYear == currentYear) {
                                    hasReminders = true;
                                }
                                int days = 0;
                                if (isFeature) {
                                    ArrayList<Integer> list = ReminderUtils.getRepeatArray(weekdays);
                                    do {
                                        calendar1.setTimeInMillis(calendar1.getTimeInMillis() +
                                                AlarmManager.INTERVAL_DAY);
                                        time = calendar1.getTimeInMillis();
                                        int weekDay = calendar1.get(Calendar.DAY_OF_WEEK);
                                        days = days + 1;
                                        if (list.get(weekDay - 1) == 1) {
                                            int sDay = calendar1.get(Calendar.DAY_OF_MONTH);
                                            int sMonth = calendar1.get(Calendar.MONTH);
                                            int sYear = calendar1.get(Calendar.YEAR);
                                            if (time > 0 && sDay == currentDay && sMonth == currentMonth && sYear == currentYear) {
                                                hasReminders = true;
                                            }
                                        }
                                    } while (days < Configs.MAX_DAYS_COUNT);
                                }
                            } else if (mType.startsWith(Constants.TYPE_MONTHDAY) && isDone == 0){
                                long time = TimeCount.getNextMonthDayTime(myHour, myMinute, myDay, 0);
                                Calendar calendar1 = Calendar.getInstance();
                                if (time > 0) {
                                    calendar1.setTimeInMillis(time);
                                    int mDay = calendar1.get(Calendar.DAY_OF_MONTH);
                                    int mMonth = calendar1.get(Calendar.MONTH);
                                    int mYear = calendar1.get(Calendar.YEAR);
                                    if (time > 0 && mDay == currentDay && mMonth == currentMonth && mYear == currentYear) {
                                        hasReminders = true;
                                    }
                                }
                                int days = 1;
                                if (isFeature){
                                    do {
                                        time = TimeCount.getNextMonthDayTime(myDay, calendar1.getTimeInMillis(), days);
                                        days = days + 1;
                                        calendar1.setTimeInMillis(time);
                                        int sDay = calendar1.get(Calendar.DAY_OF_MONTH);
                                        int sMonth = calendar1.get(Calendar.MONTH);
                                        int sYear = calendar1.get(Calendar.YEAR);
                                        if (time > 0 && sDay == currentDay && sMonth == currentMonth && sYear == currentYear) {
                                            hasReminders = true;
                                        }
                                    } while (days < Configs.MAX_MONTH_COUNT);
                                }
                            }
                        } while (s.moveToNext());
                    }
                    if (s != null) s.close();
                }
                db.close();

                pagerData.add(new WidgetItem(currentDay, currentMonth, currentYear,
                        hasReminders, hasBirthdays));
                position++;
                calendar.setTimeInMillis(calendar.getTimeInMillis() + AlarmManager.INTERVAL_DAY);
            } while (position < Configs.MAX_DAYS_COUNT);
        }

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

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
            int selMonth = datetimeList.get(position).getMonth();
            int selYear = datetimeList.get(position).getYear();

            FrameLayout background = (FrameLayout) convertView.findViewById(R.id.background);
            TextView textView = (TextView) convertView.findViewById(R.id.textView);
            TextView currentMark = (TextView) convertView.findViewById(R.id.currentMark);
            TextView reminderMark = (TextView) convertView.findViewById(R.id.reminderMark);
            TextView birthdayMark = (TextView) convertView.findViewById(R.id.birthdayMark);

            textView.setText(String.valueOf(selDay));
            textView.setTextColor(textColor);
            background.setBackgroundColor(widgetBgColor);

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            int realMonth = calendar.get(Calendar.MONTH);

            currentMark.setBackgroundColor(Color.TRANSPARENT);
            reminderMark.setBackgroundColor(Color.TRANSPARENT);
            birthdayMark.setBackgroundColor(Color.TRANSPARENT);
            if (pagerData.size() > 0){
                for (WidgetItem item : pagerData){
                    int day = item.getDay();
                    int month = item.getMonth() + 1;
                    int year = item.getYear();
                    if (day == selDay && month == selMonth){
                        if (item.isHasReminders() && year == selYear){
                            reminderMark.setBackgroundColor(context.getResources()
                                    .getColor(cs.colorReminderCalendar()));
                        }
                        if (item.isHasBirthdays()){
                            birthdayMark.setBackgroundColor(context.getResources()
                                    .getColor(cs.colorBirthdayCalendar()));
                        }
                        break;
                    }
                }
            }

            if (mDay == selDay && mMonth == selMonth && mYear == selYear && mMonth == realMonth + 1){
                currentMark.setBackgroundColor(context.getResources()
                        .getColor(cs.colorCurrentCalendar()));
            } else {
                currentMark.setBackgroundColor(Color.TRANSPARENT);
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
