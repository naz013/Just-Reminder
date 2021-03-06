/**
 * Copyright 2016 Nazar Suhovich
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cray.software.justreminder.app_widgets.tasks;

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
import com.cray.software.justreminder.roboto_views.RoboTextView;

import java.util.ArrayList;

public class TasksThemeFragment extends Fragment{

    static final String ARGUMENT_PAGE_NUMBER = "arg_page_number";
    static final String ARGUMENT_DATA = "arg_data";
    private int mPageNumber;
    private ArrayList<TasksTheme> mList;

    public static TasksThemeFragment newInstance(int page, ArrayList<TasksTheme> list) {
        TasksThemeFragment pageFragment = new TasksThemeFragment();
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
        View view = inflater.inflate(R.layout.fragment_tasks_widget_preview, container, false);

        LinearLayout header = (LinearLayout) view.findViewById(R.id.headerBg);
        LinearLayout widgetBackground = (LinearLayout) view.findViewById(R.id.widgetBg);
        RelativeLayout background = (RelativeLayout) view.findViewById(R.id.background);

        ImageButton plusButton = (ImageButton) view.findViewById(R.id.tasksCount);
        ImageButton settingsButton = (ImageButton) view.findViewById(R.id.optionsButton);

        TextView widgetTitle = (TextView) view.findViewById(R.id.widgetTitle);
        RoboTextView themeTitle = (RoboTextView) view.findViewById(R.id.themeTitle);
        RoboTextView themeTip = (RoboTextView) view.findViewById(R.id.themeTip);

        TextView taskText = (TextView) view.findViewById(R.id.task);
        TextView note = (TextView) view.findViewById(R.id.note);
        TextView taskDate = (TextView) view.findViewById(R.id.taskDate);

        TasksTheme eventsTheme = mList.get(mPageNumber);

        int windowColor = eventsTheme.getWindowColor();
        background.setBackgroundResource(windowColor);
        int windowTextColor = eventsTheme.getWindowTextColor();
        themeTitle.setTextColor(windowTextColor);
        themeTip.setTextColor(windowTextColor);

        int headerColor = eventsTheme.getHeaderColor();
        int backgroundColor = eventsTheme.getBackgroundColor();
        int titleColor = eventsTheme.getTitleColor();
        int itemTextColor = eventsTheme.getItemTextColor();

        int settingsIcon = eventsTheme.getSettingsIcon();
        int plusIcon = eventsTheme.getPlusIcon();

        widgetTitle.setTextColor(titleColor);
        taskText.setTextColor(itemTextColor);
        note.setTextColor(itemTextColor);
        taskDate.setTextColor(itemTextColor);

        header.setBackgroundResource(headerColor);
        widgetBackground.setBackgroundResource(backgroundColor);

        plusButton.setImageResource(plusIcon);
        settingsButton.setImageResource(settingsIcon);

        themeTitle.setText(eventsTheme.getTitle());
        return view;
    }
}
