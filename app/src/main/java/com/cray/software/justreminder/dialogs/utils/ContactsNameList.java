package com.cray.software.justreminder.dialogs.utils;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.views.FloatingEditText;

import java.util.ArrayList;

public class ContactsNameList extends AppCompatActivity {

    FloatingEditText searchField;
    ListView contactsList;
    ArrayAdapter<String> adapter;
    ColorSetter cs;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cs = new ColorSetter(ContactsNameList.this);
        setTheme(cs.getStyle());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(cs.colorStatus());
        }
        setContentView(R.layout.contact_list_layout);
        setRequestedOrientation(cs.getRequestOrientation());

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        toolbar.setTitle(getString(R.string.select_phone_number_title));

        findViewById(R.id.windowBackground).setBackgroundColor(cs.getBackgroundStyle());

        Intent intent = getIntent();
        final ArrayList<String> contacts = intent.getStringArrayListExtra(Constants.SELECTED_CONTACT_ARRAY);

        searchField = (FloatingEditText) findViewById(R.id.searchField);
        searchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ContactsNameList.this.adapter.getFilter().filter(s);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        contactsList = (ListView) findViewById(R.id.contactsList);
        adapter = new ArrayAdapter<String>(ContactsNameList.this,
                android.R.layout.simple_list_item_1, contacts);
        contactsList.setAdapter(adapter);

        contactsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position != -1) {
                    String name = (String) parent.getItemAtPosition(position);
                    Intent intent = new Intent();
                    intent.putExtra(Constants.SELECTED_CONTACT_NUMBER, name);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        });
    }
}
