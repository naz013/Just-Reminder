package com.cray.software.justreminder.app_widgets.events;

import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.utils.ViewUtils;

import java.util.ArrayList;

public class EventsWidgetConfig extends AppCompatActivity {

    private int widgetID = AppWidgetManager.INVALID_APPWIDGET_ID;
    private Intent resultValue;
    public final static String EVENTS_WIDGET_PREF = "widget_pref";
    public final static String EVENTS_WIDGET_TEXT_SIZE = "widget_text_size_";
    public final static String EVENTS_WIDGET_THEME = "widget_theme_";

    private int textSize;

    private ViewPager mThemePager;
    private ArrayList<EventsTheme> mThemes;

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

        SharedPreferences sp = getSharedPreferences(EVENTS_WIDGET_PREF, MODE_PRIVATE);
        int theme = sp.getInt(EVENTS_WIDGET_THEME + widgetID, 0);

        resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);

        setResult(RESULT_CANCELED, resultValue);

        ColorSetter cSetter = new ColorSetter(EventsWidgetConfig.this);
        setTheme(cSetter.getStyle());
        setContentView(R.layout.current_widget_config_layout);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ViewUtils.getColor(this, cSetter.colorPrimaryDark()));
        }
        findViewById(R.id.windowBackground).setBackgroundColor(cSetter.getBackgroundStyle());
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setTitle(getString(R.string.active_reminders));

        mThemePager = (ViewPager) findViewById(R.id.themePager);
        loadThemes();
        mThemePager.setCurrentItem(theme, true);
    }

    private void loadThemes(){
        mThemes = EventsTheme.getThemes(this);
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
                showTextSizeDialog();
                return true;
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }

    private void showTextSizeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(R.string.text_size);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.dialog_seekbar, null);
        final TextView textView = (TextView) layout.findViewById(R.id.seekValue);
        SeekBar seekBar = (SeekBar) layout.findViewById(R.id.dialogSeek);
        seekBar.setMax(13);
        seekBar.setProgress(2);
        textSize = 2 + 12;
        textView.setText(String.valueOf(textSize));
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textSize = progress + 12;
                textView.setText(String.valueOf(textSize));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        builder.setView(layout);
        builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                updateWidget();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void updateWidget() {
        SharedPreferences sp = getSharedPreferences(EVENTS_WIDGET_PREF, MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(EVENTS_WIDGET_THEME + widgetID, mThemePager.getCurrentItem());
        editor.putFloat(EVENTS_WIDGET_TEXT_SIZE + widgetID, textSize);
        editor.commit();

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        EventsWidget.updateWidget(EventsWidgetConfig.this, appWidgetManager, sp, widgetID);
        setResult(RESULT_OK, resultValue);
        finish();
    }

    private class MyFragmentPagerAdapter extends FragmentPagerAdapter {

        ArrayList<EventsTheme> arrayList;

        public MyFragmentPagerAdapter(FragmentManager fm, ArrayList<EventsTheme> list) {
            super(fm);
            this.arrayList = list;
        }

        @Override
        public Fragment getItem(int position) {
            return EventsThemeFragment.newInstance(position, mThemes);
        }

        @Override
        public int getCount() {
            return arrayList.size();
        }
    }
}
