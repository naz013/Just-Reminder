package com.cray.software.justreminder.dialogs;

import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.modules.ManageModule;
import com.cray.software.justreminder.views.FloatingEditText;
import com.getbase.floatingactionbutton.FloatingActionButton;

public class CategoryManager extends AppCompatActivity {

    ColorSetter cs = new ColorSetter(CategoryManager.this);
    FloatingEditText editField;
    RadioButton red_checkbox, violet_checkbox, green_checkbox, light_green_checkbox, blue_checkbox, light_blue_checkbox,
            yellow_checkbox, orange_checkbox, grey_checkbox, pink_checkbox, sand_checkbox, brown_checkbox,
            deepPurple, indigoCheckbox, limeCheckbox, deepOrange;
    RadioGroup themeGroup, themeGroup2, themeGroup3, themeGroupPro;
    SharedPrefs sPrefs = new SharedPrefs(CategoryManager.this);
    DataBase db;
    Toolbar toolbar;
    FloatingActionButton mFab;
    long id;
    int color = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(cs.getStyle());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(cs.colorStatus());
        }
        setContentView(R.layout.category_manager_layout);
        setRequestedOrientation(cs.getRequestOrientation());

        Intent intent = getIntent();
        id = intent.getLongExtra(Constants.ITEM_ID_INTENT, 0);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationIcon(R.drawable.ic_clear_white_24dp);
        toolbar.setTitle(getString(R.string.string_new_category));

        findViewById(R.id.windowBackground).setBackgroundColor(cs.getBackgroundStyle());

        editField = (FloatingEditText) findViewById(R.id.editField);
        if (id != 0) {
            db = new DataBase(CategoryManager.this);
            db.open();
            Cursor c = db.getCategory(id);
            if (c != null && c.moveToFirst()){
                editField.setText(c.getString(c.getColumnIndex(Constants.COLUMN_TEXT)));
                color = c.getInt(c.getColumnIndex(Constants.COLUMN_COLOR));
            }
            if (c != null) c.close();
            db.close();
            toolbar.setTitle(getString(R.string.string_edit_category));
        }

        mFab = new FloatingActionButton(CategoryManager.this);
        mFab.setColorNormal(cs.colorSetter());
        mFab.setColorPressed(cs.colorChooser());
        mFab.setSize(FloatingActionButton.SIZE_NORMAL);
        mFab.setIcon(R.drawable.ic_done_white_24dp);

        RelativeLayout wrapper = (RelativeLayout) findViewById(R.id.wrapper);
        wrapper.addView(mFab);

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mFab.getLayoutParams();
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addTemplate();
            }
        });

        initRadio();
    }

    private void initRadio(){
        red_checkbox = (RadioButton) findViewById(R.id.red_checkbox);
        violet_checkbox = (RadioButton) findViewById(R.id.violet_checkbox);
        green_checkbox = (RadioButton) findViewById(R.id.green_checkbox);
        light_green_checkbox = (RadioButton) findViewById(R.id.light_green_checkbox);
        blue_checkbox = (RadioButton) findViewById(R.id.blue_checkbox);
        light_blue_checkbox = (RadioButton) findViewById(R.id.light_blue_checkbox);
        yellow_checkbox = (RadioButton) findViewById(R.id.yellow_checkbox);
        orange_checkbox = (RadioButton) findViewById(R.id.orange_checkbox);
        grey_checkbox = (RadioButton) findViewById(R.id.grey_checkbox);
        pink_checkbox = (RadioButton) findViewById(R.id.pink_checkbox);
        sand_checkbox = (RadioButton) findViewById(R.id.sand_checkbox);
        brown_checkbox = (RadioButton) findViewById(R.id.brown_checkbox);

        deepPurple = (RadioButton) findViewById(R.id.deepPurple);
        indigoCheckbox = (RadioButton) findViewById(R.id.indigoCheckbox);
        limeCheckbox = (RadioButton) findViewById(R.id.limeCheckbox);
        deepOrange = (RadioButton) findViewById(R.id.deepOrange);

        themeGroup = (RadioGroup) findViewById(R.id.themeGroup);
        themeGroup2 = (RadioGroup) findViewById(R.id.themeGroup2);
        themeGroup3 = (RadioGroup) findViewById(R.id.themeGroup3);
        themeGroupPro = (RadioGroup) findViewById(R.id.themeGroupPro);
        if (new ManageModule().isPro()) {
            themeGroupPro.setVisibility(View.VISIBLE);
        }

        themeGroup.clearCheck();
        themeGroup2.clearCheck();
        themeGroup3.clearCheck();
        themeGroupPro.clearCheck();
        themeGroup.setOnCheckedChangeListener(listener1);
        themeGroup2.setOnCheckedChangeListener(listener2);
        themeGroup3.setOnCheckedChangeListener(listener3);
        themeGroupPro.setOnCheckedChangeListener(listener4);

        setUpRadio();
    }

    private void addTemplate(){
        String text = editField.getText().toString().trim();
        if (text.length() == 0) {
            editField.setError(getString(R.string.empty_field_error));
            return;
        }
        db = new DataBase(CategoryManager.this);
        db.open();
        if (id != 0){
            db.updateCategory(id, text, System.currentTimeMillis(), color);
        } else {
            SyncHelper helper = new SyncHelper(CategoryManager.this);
            db.addCategory(text, System.currentTimeMillis(), helper.generateID(), color);
        }
        db.close();
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

    private RadioGroup.OnCheckedChangeListener listener1 = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (checkedId != -1) {
                themeGroup2.setOnCheckedChangeListener(null);
                themeGroup3.setOnCheckedChangeListener(null);
                themeGroupPro.setOnCheckedChangeListener(null);
                themeGroup2.clearCheck();
                themeGroup3.clearCheck();
                themeGroupPro.clearCheck();
                themeGroup2.setOnCheckedChangeListener(listener2);
                themeGroup3.setOnCheckedChangeListener(listener3);
                themeGroupPro.setOnCheckedChangeListener(listener4);
                colorSwitch(group.getCheckedRadioButtonId());
            }
        }
    };
    private RadioGroup.OnCheckedChangeListener listener2 = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (checkedId != -1) {
                themeGroup.setOnCheckedChangeListener(null);
                themeGroup3.setOnCheckedChangeListener(null);
                themeGroupPro.setOnCheckedChangeListener(null);
                themeGroup.clearCheck();
                themeGroup3.clearCheck();
                themeGroupPro.clearCheck();
                themeGroup.setOnCheckedChangeListener(listener1);
                themeGroup3.setOnCheckedChangeListener(listener3);
                themeGroupPro.setOnCheckedChangeListener(listener4);
                colorSwitch(group.getCheckedRadioButtonId());
            }
        }
    };
    private RadioGroup.OnCheckedChangeListener listener3 = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (checkedId != -1) {
                themeGroup.setOnCheckedChangeListener(null);
                themeGroup2.setOnCheckedChangeListener(null);
                themeGroupPro.setOnCheckedChangeListener(null);
                themeGroup.clearCheck();
                themeGroup2.clearCheck();
                themeGroupPro.clearCheck();
                themeGroup.setOnCheckedChangeListener(listener1);
                themeGroup2.setOnCheckedChangeListener(listener2);
                themeGroupPro.setOnCheckedChangeListener(listener4);
                colorSwitch(group.getCheckedRadioButtonId());
            }
        }
    };
    private RadioGroup.OnCheckedChangeListener listener4 = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (checkedId != -1) {
                themeGroup.setOnCheckedChangeListener(null);
                themeGroup2.setOnCheckedChangeListener(null);
                themeGroup3.setOnCheckedChangeListener(null);
                themeGroup.clearCheck();
                themeGroup2.clearCheck();
                themeGroup3.clearCheck();
                themeGroup.setOnCheckedChangeListener(listener1);
                themeGroup2.setOnCheckedChangeListener(listener2);
                themeGroup3.setOnCheckedChangeListener(listener3);
                colorSwitch(group.getCheckedRadioButtonId());
            }
        }
    };

    private void colorSwitch(int checkId) {
        if (new ManageModule().isPro()) {
            switch (checkId) {
                case R.id.red_checkbox:
                    setColor(0);
                    break;
                case R.id.violet_checkbox:
                    setColor(1);
                    break;
                case R.id.green_checkbox:
                    setColor(2);
                    break;
                case R.id.light_green_checkbox:
                    setColor(3);
                    break;
                case R.id.blue_checkbox:
                    setColor(4);
                    break;
                case R.id.light_blue_checkbox:
                    setColor(5);
                    break;
                case R.id.yellow_checkbox:
                    setColor(6);
                    break;
                case R.id.orange_checkbox:
                    setColor(7);
                    break;
                case R.id.grey_checkbox:
                    setColor(8);
                    break;
                case R.id.pink_checkbox:
                    setColor(9);
                    break;
                case R.id.sand_checkbox:
                    setColor(10);
                    break;
                case R.id.brown_checkbox:
                    setColor(11);
                    break;
                case R.id.deepPurple:
                    setColor(12);
                    break;
                case R.id.deepOrange:
                    setColor(13);
                    break;
                case R.id.limeCheckbox:
                    setColor(14);
                    break;
                case R.id.indigoCheckbox:
                    setColor(15);
                    break;
            }
        } else {
            switch (checkId) {
                case R.id.red_checkbox:
                    setColor(0);
                    break;
                case R.id.violet_checkbox:
                    setColor(1);
                    break;
                case R.id.green_checkbox:
                    setColor(2);
                    break;
                case R.id.light_green_checkbox:
                    setColor(3);
                    break;
                case R.id.blue_checkbox:
                    setColor(4);
                    break;
                case R.id.light_blue_checkbox:
                    setColor(5);
                    break;
                case R.id.yellow_checkbox:
                    setColor(6);
                    break;
                case R.id.orange_checkbox:
                    setColor(7);
                    break;
                case R.id.grey_checkbox:
                    setColor(8);
                    break;
                case R.id.pink_checkbox:
                    setColor(9);
                    break;
                case R.id.sand_checkbox:
                    setColor(10);
                    break;
                case R.id.brown_checkbox:
                    setColor(11);
                    break;
            }
        }
    }

    private void setColor(int i){
        color = i;
    }

    public void setUpRadio(){
        if (new ManageModule().isPro()) {
            switch (color) {
                case 0:
                    red_checkbox.setChecked(true);
                    break;
                case 1:
                    violet_checkbox.setChecked(true);
                    break;
                case 2:
                    green_checkbox.setChecked(true);
                    break;
                case 3:
                    light_green_checkbox.setChecked(true);
                    break;
                case 4:
                    blue_checkbox.setChecked(true);
                    break;
                case 5:
                    light_blue_checkbox.setChecked(true);
                    break;
                case 6:
                    yellow_checkbox.setChecked(true);
                    break;
                case 7:
                    orange_checkbox.setChecked(true);
                    break;
                case 8:
                    grey_checkbox.setChecked(true);
                    break;
                case 9:
                    pink_checkbox.setChecked(true);
                    break;
                case 10:
                    sand_checkbox.setChecked(true);
                    break;
                case 11:
                    brown_checkbox.setChecked(true);
                    break;
                case 12:
                    deepPurple.setChecked(true);
                    break;
                case 13:
                    deepOrange.setChecked(true);
                    break;
                case 14:
                    limeCheckbox.setChecked(true);
                    break;
                case 15:
                    indigoCheckbox.setChecked(true);
                    break;
                default:
                    green_checkbox.setChecked(true);
                    break;
            }
        } else {
            switch (color) {
                case 0:
                    red_checkbox.setChecked(true);
                    break;
                case 1:
                    violet_checkbox.setChecked(true);
                    break;
                case 2:
                    green_checkbox.setChecked(true);
                    break;
                case 3:
                    light_green_checkbox.setChecked(true);
                    break;
                case 4:
                    blue_checkbox.setChecked(true);
                    break;
                case 5:
                    light_blue_checkbox.setChecked(true);
                    break;
                case 6:
                    yellow_checkbox.setChecked(true);
                    break;
                case 7:
                    orange_checkbox.setChecked(true);
                    break;
                case 8:
                    grey_checkbox.setChecked(true);
                    break;
                case 9:
                    pink_checkbox.setChecked(true);
                    break;
                case 10:
                    sand_checkbox.setChecked(true);
                    break;
                case 11:
                    brown_checkbox.setChecked(true);
                    break;
                default:
                    green_checkbox.setChecked(true);
                    break;
            }
        }
    }
}
