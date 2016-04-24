package com.cray.software.justreminder.app_widgets.notes;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cray.software.justreminder.R;

import java.util.ArrayList;

public class NotesThemeFragment extends Fragment{

    static final String ARGUMENT_PAGE_NUMBER = "arg_page_number";
    static final String ARGUMENT_DATA = "arg_data";
    private int mPageNumber;
    private ArrayList<NotesTheme> mList;

    public static NotesThemeFragment newInstance(int page, ArrayList<NotesTheme> list) {
        NotesThemeFragment pageFragment = new NotesThemeFragment();
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
        View view = inflater.inflate(R.layout.fragment_note_widget_preview, container, false);

        RelativeLayout header = (RelativeLayout) view.findViewById(R.id.headerBg);
        LinearLayout widgetBackground = (LinearLayout) view.findViewById(R.id.widgetBg);
        RelativeLayout background = (RelativeLayout) view.findViewById(R.id.background);

        ImageButton plusButton = (ImageButton) view.findViewById(R.id.tasksCount);
        ImageButton settingsButton = (ImageButton) view.findViewById(R.id.settingsButton);

        TextView widgetTitle = (TextView) view.findViewById(R.id.widgetTitle);
        TextView themeTitle = (TextView) view.findViewById(R.id.themeTitle);
        TextView themeTip = (TextView) view.findViewById(R.id.themeTip);

        NotesTheme calendarTheme = mList.get(mPageNumber);

        int windowColor = calendarTheme.getWindowColor();
        background.setBackgroundResource(windowColor);
        int windowTextColor = calendarTheme.getWindowTextColor();
        themeTitle.setTextColor(windowTextColor);
        themeTip.setTextColor(windowTextColor);

        int headerColor = calendarTheme.getHeaderColor();
        int backgroundColor = calendarTheme.getBackgroundColor();
        int titleColor = calendarTheme.getTitleColor();

        int settingsIcon = calendarTheme.getSettingsIcon();
        int plusIcon = calendarTheme.getPlusIcon();

        widgetTitle.setTextColor(titleColor);
        header.setBackgroundResource(headerColor);
        widgetBackground.setBackgroundResource(backgroundColor);

        plusButton.setImageResource(plusIcon);
        settingsButton.setImageResource(settingsIcon);

        themeTitle.setText(calendarTheme.getTitle());
        return view;
    }
}
