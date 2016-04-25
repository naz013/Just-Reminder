package com.cray.software.justreminder.app_widgets.events;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cray.software.justreminder.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Formatter;
import java.util.GregorianCalendar;
import java.util.Locale;

public class EventsThemeFragment extends Fragment{

    static final String ARGUMENT_PAGE_NUMBER = "arg_page_number";
    static final String ARGUMENT_DATA = "arg_data";
    private int mPageNumber;
    private ArrayList<EventsTheme> mList;

    public static EventsThemeFragment newInstance(int page, ArrayList<EventsTheme> list) {
        EventsThemeFragment pageFragment = new EventsThemeFragment();
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
        mPageNumber = intent.getInt(ARGUMENT_PAGE_NUMBER);
        mList = intent.getParcelableArrayList(ARGUMENT_DATA);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_events_widget_preview, container, false);

        LinearLayout header = (LinearLayout) view.findViewById(R.id.headerBg);
        LinearLayout widgetBackground = (LinearLayout) view.findViewById(R.id.widgetBg);
        LinearLayout itemBackgroundView = (LinearLayout) view.findViewById(R.id.listItemCard);
        RelativeLayout background = (RelativeLayout) view.findViewById(R.id.background);

        ImageButton plusButton = (ImageButton) view.findViewById(R.id.plusButton);
        ImageButton voiceButton = (ImageButton) view.findViewById(R.id.voiceButton);
        ImageButton settingsButton = (ImageButton) view.findViewById(R.id.optionsButton);

        TextView widgetTitle = (TextView) view.findViewById(R.id.widgetDate);
        TextView themeTitle = (TextView) view.findViewById(R.id.themeTitle);
        TextView themeTip = (TextView) view.findViewById(R.id.themeTip);

        TextView taskText = (TextView) view.findViewById(R.id.taskText);
        TextView taskNumber = (TextView) view.findViewById(R.id.taskNumber);
        TextView taskDate = (TextView) view.findViewById(R.id.taskDate);
        TextView taskTime = (TextView) view.findViewById(R.id.taskTime);

        EventsTheme eventsTheme = mList.get(mPageNumber);

        int windowColor = eventsTheme.getWindowColor();
        background.setBackgroundResource(windowColor);
        int windowTextColor = eventsTheme.getWindowTextColor();
        themeTitle.setTextColor(windowTextColor);
        themeTip.setTextColor(windowTextColor);

        int headerColor = eventsTheme.getHeaderColor();
        int backgroundColor = eventsTheme.getBackgroundColor();
        int titleColor = eventsTheme.getTitleColor();
        int itemTextColor = eventsTheme.getItemTextColor();
        int itemBackground = eventsTheme.getItemBackground();

        int settingsIcon = eventsTheme.getSettingsIcon();
        int plusIcon = eventsTheme.getPlusIcon();
        int voiceIcon = eventsTheme.getVoiceIcon();

        widgetTitle.setTextColor(titleColor);
        taskText.setTextColor(itemTextColor);
        taskNumber.setTextColor(itemTextColor);
        taskDate.setTextColor(itemTextColor);
        taskTime.setTextColor(itemTextColor);

        header.setBackgroundResource(headerColor);
        widgetBackground.setBackgroundResource(backgroundColor);
        itemBackgroundView.setBackgroundResource(itemBackground);

        plusButton.setImageResource(plusIcon);
        settingsButton.setImageResource(settingsIcon);
        voiceButton.setImageResource(voiceIcon);

        themeTitle.setText(eventsTheme.getTitle());

        StringBuilder monthYearStringBuilder = new StringBuilder(50);
        Formatter monthYearFormatter = new Formatter(
                monthYearStringBuilder, Locale.getDefault());
        int MONTH_YEAR_FLAG = DateUtils.FORMAT_SHOW_DATE
                | DateUtils.FORMAT_NO_MONTH_DAY | DateUtils.FORMAT_SHOW_YEAR;
        Calendar cal = new GregorianCalendar();
        String monthTitle = DateUtils.formatDateRange(getActivity(),
                monthYearFormatter, cal.getTimeInMillis(), cal.getTimeInMillis(), MONTH_YEAR_FLAG).toString();
        widgetTitle.setText(monthTitle.toUpperCase());
        return view;
    }
}
