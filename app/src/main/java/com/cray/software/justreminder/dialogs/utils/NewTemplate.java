package com.cray.software.justreminder.dialogs.utils;

import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.views.FloatingEditText;
import com.getbase.floatingactionbutton.FloatingActionButton;

public class NewTemplate extends AppCompatActivity {

    private ColorSetter cs = new ColorSetter(NewTemplate.this);
    private FloatingEditText placeName;
    private SharedPrefs sPrefs = new SharedPrefs(NewTemplate.this);
    private DataBase db = new DataBase(NewTemplate.this);
    private TextView leftCharacters;
    private long id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(cs.getStyle());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(cs.colorStatus());
        }
        setContentView(R.layout.new_template_activity_layout);
        setRequestedOrientation(cs.getRequestOrientation());

        Intent intent = getIntent();
        id = intent.getLongExtra(Constants.ITEM_ID_INTENT, 0);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationIcon(R.drawable.ic_clear_white_24dp);
        toolbar.setTitle(getString(R.string.new_template_title));

        findViewById(R.id.windowBackground).setBackgroundColor(cs.getBackgroundStyle());

        leftCharacters = (TextView) findViewById(R.id.leftCharacters);

        placeName = (FloatingEditText) findViewById(R.id.placeName);
        if (id != 0) {
            db.open();
            Cursor c = db.getTemplate(id);
            if (c != null && c.moveToFirst()){
                placeName.setText(c.getString(c.getColumnIndex(Constants.COLUMN_TEXT)));
            }
            if (c != null) c.close();
        }
        placeName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int length = s.length();
                leftCharacters.setText(getString(R.string.string_left_characters) + " " + (120 - length));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        FloatingActionButton mFab = new FloatingActionButton(NewTemplate.this);
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
    }

    private void addTemplate(){
        String text = placeName.getText().toString().trim();
        if (text.length() == 0) {
            placeName.setError(getString(R.string.empty_field_error));
            return;
        }
        db.open();
        if (id != 0){
            db.updateTemplate(id, text, System.currentTimeMillis());
        } else {
            db.addTemplate(text, System.currentTimeMillis());
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
}
