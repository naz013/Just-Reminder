package com.cray.software.justreminder.app_widgets.tasks;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.modules.Module;
import com.cray.software.justreminder.utils.ViewUtils;

import java.util.ArrayList;

public class TasksWidgetConfig extends AppCompatActivity {

    private int widgetID = AppWidgetManager.INVALID_APPWIDGET_ID;
    private Intent resultValue;
    public final static String TASKS_WIDGET_PREF = "tasks_pref";
    public final static String TASKS_WIDGET_THEME = "tasks_theme_";

    private ViewPager mThemePager;
    private ArrayList<TasksTheme> mThemes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            widgetID = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }
        if (widgetID == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }

        SharedPreferences sp = getSharedPreferences(TASKS_WIDGET_PREF, MODE_PRIVATE);
        int theme = sp.getInt(TASKS_WIDGET_THEME + widgetID, 0);

        resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);

        setResult(RESULT_CANCELED, resultValue);

        ColorSetter cSetter = new ColorSetter(TasksWidgetConfig.this);
        setTheme(cSetter.getStyle());
        setContentView(R.layout.tasks_widget_config_layout);
        if (Module.isLollipop()) {
            getWindow().setStatusBarColor(ViewUtils.getColor(this, cSetter.colorPrimaryDark()));
        }
        findViewById(R.id.windowBackground).setBackgroundColor(cSetter.getBackgroundStyle());
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setTitle(getString(R.string.google_tasks));

        mThemePager = (ViewPager) findViewById(R.id.themePager);
        loadThemes();
        mThemePager.setCurrentItem(theme, true);
    }

    private void loadThemes(){
        mThemes = TasksTheme.getThemes(this);
        MyFragmentPagerAdapter adapter = new MyFragmentPagerAdapter(getSupportFragmentManager(), mThemes);
        mThemePager.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.current_widget_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_save:
                updateWidget();
                return true;
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }

    private void updateWidget() {
        SharedPreferences sp = getSharedPreferences(TASKS_WIDGET_PREF, MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(TASKS_WIDGET_THEME + widgetID, mThemePager.getCurrentItem());
        editor.commit();

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        TasksWidget.updateWidget(TasksWidgetConfig.this, appWidgetManager, sp, widgetID);
        setResult(RESULT_OK, resultValue);
        finish();
    }

    private class MyFragmentPagerAdapter extends FragmentPagerAdapter {

        ArrayList<TasksTheme> arrayList;

        public MyFragmentPagerAdapter(FragmentManager fm, ArrayList<TasksTheme> list) {
            super(fm);
            this.arrayList = list;
        }

        @Override
        public Fragment getItem(int position) {
            return TasksThemeFragment.newInstance(position, mThemes);
        }

        @Override
        public int getCount() {
            return arrayList.size();
        }
    }
}
