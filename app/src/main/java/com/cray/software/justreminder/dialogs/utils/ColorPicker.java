package com.cray.software.justreminder.dialogs.utils;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.modules.ManageModule;

public class ColorPicker extends Activity implements View.OnClickListener {

    ImageButton red_checkbox, violet_checkbox, green_checkbox, light_green_checkbox, blue_checkbox, light_blue_checkbox,
            yellow_checkbox, orange_checkbox, grey_checkbox, pink_checkbox, sand_checkbox, brown_checkbox,
            deepPurple, indigoCheckbox, limeCheckbox, deepOrange;
    LinearLayout themeGroupPro;

    ColorSetter cs;
    boolean isPro = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cs = new ColorSetter(ColorPicker.this);
        setTheme(cs.getDialogStyle());
        setContentView(R.layout.color_picker_layout);

        themeGroupPro = (LinearLayout) findViewById(R.id.themeGroupPro);
        isPro = new ManageModule().isPro();
        if (isPro) themeGroupPro.setVisibility(View.VISIBLE);

        red_checkbox = (ImageButton) findViewById(R.id.red_checkbox);
        red_checkbox.setOnClickListener(this);
        violet_checkbox = (ImageButton) findViewById(R.id.violet_checkbox);
        violet_checkbox.setOnClickListener(this);
        green_checkbox = (ImageButton) findViewById(R.id.green_checkbox);
        green_checkbox.setOnClickListener(this);
        light_green_checkbox = (ImageButton) findViewById(R.id.light_green_checkbox);
        light_green_checkbox.setOnClickListener(this);
        blue_checkbox = (ImageButton) findViewById(R.id.blue_checkbox);
        blue_checkbox.setOnClickListener(this);
        light_blue_checkbox = (ImageButton) findViewById(R.id.light_blue_checkbox);
        light_blue_checkbox.setOnClickListener(this);
        yellow_checkbox = (ImageButton) findViewById(R.id.yellow_checkbox);
        yellow_checkbox.setOnClickListener(this);
        orange_checkbox = (ImageButton) findViewById(R.id.orange_checkbox);
        orange_checkbox.setOnClickListener(this);
        grey_checkbox = (ImageButton) findViewById(R.id.grey_checkbox);
        grey_checkbox.setOnClickListener(this);
        pink_checkbox = (ImageButton) findViewById(R.id.pink_checkbox);
        pink_checkbox.setOnClickListener(this);
        sand_checkbox = (ImageButton) findViewById(R.id.sand_checkbox);
        sand_checkbox.setOnClickListener(this);
        brown_checkbox = (ImageButton) findViewById(R.id.brown_checkbox);
        brown_checkbox.setOnClickListener(this);

        deepPurple = (ImageButton) findViewById(R.id.deepPurple);
        deepPurple.setOnClickListener(this);
        indigoCheckbox = (ImageButton) findViewById(R.id.indigoCheckbox);
        indigoCheckbox.setOnClickListener(this);
        limeCheckbox = (ImageButton) findViewById(R.id.limeCheckbox);
        limeCheckbox.setOnClickListener(this);
        deepOrange = (ImageButton) findViewById(R.id.deepOrange);
        deepOrange.setOnClickListener(this);
    }

    void saveColor(int color) {
        Intent intent = new Intent();
        intent.putExtra(Constants.SELECTED_COLOR, color);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.red_checkbox:
                saveColor(0);
                break;
            case R.id.violet_checkbox:
                saveColor(1);
                break;
            case R.id.green_checkbox:
                saveColor(2);
                break;
            case R.id.light_green_checkbox:
                saveColor(3);
                break;
            case R.id.light_blue_checkbox:
                saveColor(5);
                break;
            case R.id.blue_checkbox:
                saveColor(4);
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
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}