package com.cray.software.justreminder.dialogs;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.Notifier;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.interfaces.Prefs;
import com.cray.software.justreminder.modules.Module;

public class ThemerDialog extends AppCompatActivity {

    private ImageButton red_checkbox, violet_checkbox, green_checkbox, light_green_checkbox,
            blue_checkbox, light_blue_checkbox, yellow_checkbox, orange_checkbox, grey_checkbox,
            pink_checkbox, sand_checkbox, brown_checkbox, deepPurple, indigoCheckbox, limeCheckbox,
            deepOrange;
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
            getWindow().setStatusBarColor(cs.colorStatus());
        }
        setContentView(R.layout.theme_color_layout);

        setRequestedOrientation(cs.getRequestOrientation());

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationIcon(R.drawable.ic_clear_white_24dp);
        toolbar.setTitle(getString(R.string.theme_title));

        findViewById(R.id.windowBackground).setBackgroundColor(cs.getBackgroundStyle());

        red_checkbox = (ImageButton) findViewById(R.id.red_checkbox);
        violet_checkbox = (ImageButton) findViewById(R.id.violet_checkbox);
        green_checkbox = (ImageButton) findViewById(R.id.green_checkbox);
        light_green_checkbox = (ImageButton) findViewById(R.id.light_green_checkbox);
        blue_checkbox = (ImageButton) findViewById(R.id.blue_checkbox);
        light_blue_checkbox = (ImageButton) findViewById(R.id.light_blue_checkbox);
        yellow_checkbox = (ImageButton) findViewById(R.id.yellow_checkbox);
        orange_checkbox = (ImageButton) findViewById(R.id.orange_checkbox);
        grey_checkbox = (ImageButton) findViewById(R.id.grey_checkbox);
        pink_checkbox = (ImageButton) findViewById(R.id.pink_checkbox);
        sand_checkbox = (ImageButton) findViewById(R.id.sand_checkbox);
        brown_checkbox = (ImageButton) findViewById(R.id.brown_checkbox);

        deepPurple = (ImageButton) findViewById(R.id.deepPurple);
        indigoCheckbox = (ImageButton) findViewById(R.id.indigoCheckbox);
        limeCheckbox = (ImageButton) findViewById(R.id.limeCheckbox);
        deepOrange = (ImageButton) findViewById(R.id.deepOrange);

        LinearLayout themeGroupPro = (LinearLayout) findViewById(R.id.themeGroupPro);
        if (Module.isPro()) {
            themeGroupPro.setVisibility(View.VISIBLE);
        } else themeGroupPro.setVisibility(View.GONE);

        setOnClickListener(red_checkbox, violet_checkbox, green_checkbox, light_green_checkbox,
                blue_checkbox, light_blue_checkbox, yellow_checkbox, orange_checkbox, grey_checkbox,
                pink_checkbox, sand_checkbox, brown_checkbox, deepPurple, deepOrange, indigoCheckbox,
                limeCheckbox);

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
        String loaded = sPrefs.loadPrefs(Prefs.THEME);
        switch (loaded) {
            case "1":
                red_checkbox.setSelected(true);
                break;
            case "2":
                violet_checkbox.setSelected(true);
                break;
            case "3":
                light_green_checkbox.setSelected(true);
                break;
            case "4":
                green_checkbox.setSelected(true);
                break;
            case "5":
                light_blue_checkbox.setSelected(true);
                break;
            case "6":
                blue_checkbox.setSelected(true);
                break;
            case "7":
                yellow_checkbox.setSelected(true);
                break;
            case "8":
                orange_checkbox.setSelected(true);
                break;
            case "9":
                grey_checkbox.setSelected(true);
                break;
            case "10":
                pink_checkbox.setSelected(true);
                break;
            case "11":
                sand_checkbox.setSelected(true);
                break;
            case "12":
                brown_checkbox.setSelected(true);
                break;
            case "13":
                deepPurple.setSelected(true);
                break;
            case "14":
                deepOrange.setSelected(true);
                break;
            case "15":
                limeCheckbox.setSelected(true);
                break;
            case "16":
                indigoCheckbox.setSelected(true);
                break;
            default:
                blue_checkbox.setSelected(true);
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
                saveColor("1");
                break;
            case R.id.violet_checkbox:
                saveColor("2");
                break;
            case R.id.green_checkbox:
                saveColor("4");
                break;
            case R.id.light_green_checkbox:
                saveColor("3");
                break;
            case R.id.light_blue_checkbox:
                saveColor("5");
                break;
            case R.id.blue_checkbox:
                saveColor("6");
                break;
            case R.id.yellow_checkbox:
                saveColor("7");
                break;
            case R.id.orange_checkbox:
                saveColor("8");
                break;
            case R.id.grey_checkbox:
                saveColor("9");
                break;
            case R.id.pink_checkbox:
                saveColor("10");
                break;
            case R.id.sand_checkbox:
                saveColor("11");
                break;
            case R.id.brown_checkbox:
                saveColor("12");
                break;
            case R.id.deepPurple:
                saveColor("13");
                break;
            case R.id.deepOrange:
                saveColor("14");
                break;
            case R.id.limeCheckbox:
                saveColor("15");
                break;
            case R.id.indigoCheckbox:
                saveColor("16");
                break;
        }
        cs = new ColorSetter(ThemerDialog.this);
        toolbar.setBackgroundColor(cs.colorSetter());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(cs.colorStatus());
        }
    }

    private void setSelected(int radio) {
        findViewById(radio).setSelected(true);
    }

    private void disableAll() {
        red_checkbox.setSelected(false);
        violet_checkbox.setSelected(false);
        green_checkbox.setSelected(false);
        light_green_checkbox.setSelected(false);
        blue_checkbox.setSelected(false);
        light_blue_checkbox.setSelected(false);
        yellow_checkbox.setSelected(false);
        orange_checkbox.setSelected(false);
        grey_checkbox.setSelected(false);
        pink_checkbox.setSelected(false);
        sand_checkbox.setSelected(false);
        brown_checkbox.setSelected(false);
        deepOrange.setSelected(false);
        deepPurple.setSelected(false);
        limeCheckbox.setSelected(false);
        indigoCheckbox.setSelected(false);
    }

    private void saveColor(String string) {
        sPrefs = new SharedPrefs(ThemerDialog.this);
        sPrefs.savePrefs(Prefs.THEME, string);
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