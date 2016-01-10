package com.cray.software.justreminder.activities;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.Notifier;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.modules.Module;

public class ThemerDialog extends AppCompatActivity {

    private ImageButton red, green, blue, yellow, greenLight, blueLight, cyan, purple,
            amber, orange, pink, teal, deepPurple, deepOrange, indigo, lime;
    private SharedPrefs sPrefs;
    private ColorSetter cs;
    private Toolbar toolbar;
    private int prevId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cs = new ColorSetter(ThemerDialog.this);
        setTheme(cs.getStyle());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(cs.colorPrimaryDark());
        }
        setContentView(R.layout.theme_color_layout);

        setRequestedOrientation(cs.getRequestOrientation());

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationIcon(R.drawable.ic_clear_white_24dp);
        toolbar.setTitle(getString(R.string.theme_title));

        findViewById(R.id.windowBackground).setBackgroundColor(cs.getBackgroundStyle());

        red = (ImageButton) findViewById(R.id.red_checkbox);
        purple = (ImageButton) findViewById(R.id.violet_checkbox);
        green = (ImageButton) findViewById(R.id.green_checkbox);
        greenLight = (ImageButton) findViewById(R.id.light_green_checkbox);
        blue = (ImageButton) findViewById(R.id.blue_checkbox);
        blueLight = (ImageButton) findViewById(R.id.light_blue_checkbox);
        yellow = (ImageButton) findViewById(R.id.yellow_checkbox);
        orange = (ImageButton) findViewById(R.id.orange_checkbox);
        cyan = (ImageButton) findViewById(R.id.grey_checkbox);
        pink = (ImageButton) findViewById(R.id.pink_checkbox);
        teal = (ImageButton) findViewById(R.id.sand_checkbox);
        amber = (ImageButton) findViewById(R.id.brown_checkbox);

        deepPurple = (ImageButton) findViewById(R.id.deepPurple);
        indigo = (ImageButton) findViewById(R.id.indigoCheckbox);
        lime = (ImageButton) findViewById(R.id.limeCheckbox);
        deepOrange = (ImageButton) findViewById(R.id.deepOrange);

        LinearLayout themeGroupPro = (LinearLayout) findViewById(R.id.themeGroupPro);
        if (Module.isPro()) {
            themeGroupPro.setVisibility(View.VISIBLE);
        } else themeGroupPro.setVisibility(View.GONE);

        setOnClickListener(red, green, blue, yellow, greenLight, blueLight, cyan, purple,
                amber, orange, pink, teal, deepPurple, deepOrange, indigo, lime);

        setUpRadio();
    }

    private void setOnClickListener(View... views){
        for (View view : views){
            view.setOnClickListener(listener);
        }
    }

    private View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            themeColorSwitch(v.getId());
        }
    };

    private void setUpRadio(){
        sPrefs = new SharedPrefs(ThemerDialog.this);
        int loaded = sPrefs.loadInt(Prefs.APP_THEME);
        switch (loaded) {
            case 0:
                red.setSelected(true);
                break;
            case 1:
                purple.setSelected(true);
                break;
            case 2:
                greenLight.setSelected(true);
                break;
            case 3:
                green.setSelected(true);
                break;
            case 4:
                blueLight.setSelected(true);
                break;
            case 5:
                blue.setSelected(true);
                break;
            case 6:
                yellow.setSelected(true);
                break;
            case 7:
                orange.setSelected(true);
                break;
            case 8:
                cyan.setSelected(true);
                break;
            case 9:
                pink.setSelected(true);
                break;
            case 10:
                teal.setSelected(true);
                break;
            case 11:
                amber.setSelected(true);
                break;
            case 12:
                deepPurple.setSelected(true);
                break;
            case 13:
                deepOrange.setSelected(true);
                break;
            case 14:
                lime.setSelected(true);
                break;
            case 15:
                indigo.setSelected(true);
                break;
            default:
                blue.setSelected(true);
                break;
        }
    }

    private void themeColorSwitch(int radio){
        if (radio == prevId) return;
        prevId = radio;
        disableAll();
        setSelected(radio);
        switch (radio){
            case R.id.red_checkbox:
                saveColor(0);
                break;
            case R.id.violet_checkbox:
                saveColor(1);
                break;
            case R.id.light_green_checkbox:
                saveColor(2);
                break;
            case R.id.green_checkbox:
                saveColor(3);
                break;
            case R.id.light_blue_checkbox:
                saveColor(4);
                break;
            case R.id.blue_checkbox:
                saveColor(5);
                break;
            case R.id.yellow_checkbox:
                saveColor(6);
                break;
            case R.id.orange_checkbox:
                saveColor(7);
                break;
            case R.id.grey_checkbox:
                saveColor(8);
                break;
            case R.id.pink_checkbox:
                saveColor(9);
                break;
            case R.id.sand_checkbox:
                saveColor(10);
                break;
            case R.id.brown_checkbox:
                saveColor(11);
                break;
            case R.id.deepPurple:
                saveColor(12);
                break;
            case R.id.deepOrange:
                saveColor(13);
                break;
            case R.id.limeCheckbox:
                saveColor(14);
                break;
            case R.id.indigoCheckbox:
                saveColor(15);
                break;
        }
        cs = new ColorSetter(ThemerDialog.this);
        toolbar.setBackgroundColor(cs.colorPrimary());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(cs.colorPrimaryDark());
        }
    }

    private void setSelected(int radio) {
        findViewById(radio).setSelected(true);
    }

    private void disableAll() {
        red.setSelected(false);
        purple.setSelected(false);
        greenLight.setSelected(false);
        green.setSelected(false);
        blueLight.setSelected(false);
        blue.setSelected(false);
        yellow.setSelected(false);
        orange.setSelected(false);
        cyan.setSelected(false);
        pink.setSelected(false);
        teal.setSelected(false);
        amber.setSelected(false);
        deepOrange.setSelected(false);
        deepPurple.setSelected(false);
        lime.setSelected(false);
        indigo.setSelected(false);
    }

    private void saveColor(int code) {
        sPrefs = new SharedPrefs(ThemerDialog.this);
        sPrefs.saveInt(Prefs.APP_THEME, code);
        sPrefs.saveBoolean(Prefs.UI_CHANGED, true);
    }

    @Override
    public void onBackPressed() {
        if (new SharedPrefs(ThemerDialog.this).loadBoolean(Prefs.STATUS_BAR_NOTIFICATION)) {
            new Notifier(ThemerDialog.this).recreatePermanent();
        }
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (new SharedPrefs(ThemerDialog.this).loadBoolean(Prefs.STATUS_BAR_NOTIFICATION)) {
                    new Notifier(ThemerDialog.this).recreatePermanent();
                }
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}