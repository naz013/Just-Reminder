package com.cray.software.justreminder.activities;

import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.utils.SuperUtil;

public class NewTemplate extends AppCompatActivity {

    private ColorSetter cs = new ColorSetter(NewTemplate.this);
    private EditText placeName;
    private TextView leftCharacters;
    private long id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(cs.getStyle());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(cs.colorPrimaryDark());
        }
        setContentView(R.layout.new_template_activity_layout);
        setRequestedOrientation(cs.getRequestOrientation());

        Intent intent = getIntent();
        id = intent.getLongExtra(Constants.ITEM_ID_INTENT, 0);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationIcon(R.drawable.ic_clear_white_24dp);

        findViewById(R.id.windowBackground).setBackgroundColor(cs.getBackgroundStyle());

        leftCharacters = (TextView) findViewById(R.id.leftCharacters);
        leftCharacters.setText("");

        placeName = (EditText) findViewById(R.id.placeName);
        if (id != 0) {
            DataBase db = new DataBase(NewTemplate.this);
            db.open();
            Cursor c = db.getTemplate(id);
            if (c != null && c.moveToFirst()){
                placeName.setText(c.getString(c.getColumnIndex(Constants.COLUMN_TEXT)));
            }
            if (c != null) c.close();
            db.close();
        }
        placeName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int length = s.length();
                leftCharacters.setText(SuperUtil.appendString(getString(R.string.string_left_characters), " " , String.valueOf(120 - length)));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void addTemplate(){
        String text = placeName.getText().toString().trim();
        if (text.length() == 0) {
            placeName.setError(getString(R.string.empty_field_error));
            return;
        }
        DataBase db = new DataBase(NewTemplate.this);
        db.open();
        if (id != 0){
            db.updateTemplate(id, text, System.currentTimeMillis());
        } else {
            db.addTemplate(text, System.currentTimeMillis());
        }
        db.close();
        new SharedPrefs(this).saveBoolean(Prefs.TEMPLATE_CHANGED, true);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.save_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                addTemplate();
                return true;
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
